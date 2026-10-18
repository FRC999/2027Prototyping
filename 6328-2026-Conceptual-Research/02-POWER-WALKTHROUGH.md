# How 6328's power management works

Review date: October 17, 2026. This walkthrough follows Mechanical Advantage `b2923d6`, including its mode exceptions. The [evidence record](06-EVIDENCE-AND-HISTORY.md) distinguishes public code from verified competition deployment. Diagrams are in [Architecture](03-ARCHITECTURE.md).

## 1. Three different problems are being addressed

| Problem | What happens physically | What their software does |
|---|---|---|
| Voltage sag / brownout | Large battery-side current pulls the supply voltage down | Estimate a current allowance that keeps modeled voltage above a threshold |
| Main breaker heating | Sustained overload accumulates stress; a later burst can trip an already stressed breaker | Track a stress state and estimate an allowance for a short future interval |
| Competing mechanisms | Driving and shooting can demand substantial current together | Give drive motors what remains after other measured loads |

Battery charge depletion, instantaneous voltage sag, and breaker stress are related but different. A battery can have charge left and still sag under load. A breaker can remain stressed after current falls. This controller handles those effects continuously, not only at the end of a match. [Battery model][M-BAT], [breaker model][M-BREAK], [manager][M-FIN].

This is software current budgeting through motor controllers. It does not switch power physically between branches or replace the robot's electrical protection devices.

## 2. Data collection: what does it actually measure?

The drive modules, flywheel motors, rollers, hood, and slam mechanism report **supply current**. Drive-wheel motors are marked as drive loads; the steering motors are marked as other loads. The manager clips each reported motor current at zero before accumulating it, so negative readings are not treated as reusable regeneration credit. Disconnected motor inputs are generally reported as zero. [Module reporting][M-MODULE], [flywheel reporting][M-FLY], [manager][M-FIN].

The manager also obtains robot supply voltage, roboRIO input current, and the roboRIO brownout indication. The logger adds estimates for the radio, CAN devices, and Mac mini. Despite the variable name `macMiniCurrent`, that input is power in watts; the logger divides by battery voltage and an assumed 0.9 conversion efficiency to estimate battery-side current. [Finance inputs][M-FIN], [BatteryLogger][M-LOGGER].

**The implemented total is a sum of device readings plus auxiliary estimates, not a direct PDH total-current measurement.** The battery estimator's input comment mentions PDP/PDH voltage, but the actual caller supplies `RobotController.getBatteryVoltage()`. When source comments and call sites differ, this document follows the executable call path.

For logging, power is approximated by `voltage * current`, and energy by integrating power over the 0.005 s configured loop period. Energy is reported in watt-hours. Those logs help explain consumption; the allocator itself operates in amperes. [Logger][M-LOGGER], [loop constant][M-CONSTANTS].

Supply current and motor/stator current must not be interchanged. Supply current describes battery load; stator current is closely related to motor torque. At low motor speed they can be substantially different. CTRE documents both limits separately. [CTRE current-limit explanation][H-CTRE].

## 3. Battery algorithm: one-RC Thevenin model

The model represents battery terminal voltage as:

```text
Vterminal = OCV(SOC) - I * R0(SOC) - Vp
```

| Symbol | Meaning |
|---|---|
| `SOC` | Estimated state of charge, clamped between 0 and 1 |
| `OCV` | Open-circuit voltage predicted from state of charge |
| `I` | Total estimated battery-side current, amps |
| `R0` | Effective instantaneous resistance, ohms |
| `Vp` | Slower polarization voltage drop, volts |
| `Rp`, `Cp` | Parameters controlling buildup and recovery of polarization |

This separates an immediate current-dependent drop from a slower history-dependent drop. When current falls, the immediate drop disappears quickly and the modeled polarization relaxes over time. The code cites a battery-model paper and battery characterization work; this review verifies the implementation, not an independent reproduction of that parameter fit. [BatteryEstimator][M-BAT].

### Step A: estimate charge consumption

The nominal capacity is 19.75 Ah, converted to ampere-seconds. At high discharge current, Peukert correction increases the effective current used for charge accounting:

```text
Inominal = 19.75 / 20
p = 1.07 + 0.1 * (1 - SOC)
Ieffective = I * (I / Inominal)^(p - 1)     when I > Inominal
Ieffective = I                            otherwise
SOCnext = clamp(SOC - Ieffective * dt / capacityAs, 0, 1)
```

Conceptually, high-current discharge uses the battery less efficiently than the low-rate capacity rating implies. These constants are tuned assumptions, not a universal battery specification. [Charge update][M-BAT].

### Step B: update resistance, voltage curve, and polarization

Open-circuit voltage comes from linear interpolation between six SOC/voltage knots. The executable resistance formulas are:

```text
R0 = 0.01181 + 0.00335 * exp(1.36726 * (0.4 - SOC))
Rp = 0.00274 * (1 + 2.10529 * (1 - SOC)^2)
Cp = 400
decay = exp(-dt / (Rp * Cp))
VpPredicted = I * Rp - (I * Rp - VpPrevious) * decay
```

`R0` therefore increases as SOC falls. There is a sign discrepancy between the source's introductory R0 comment and its executable `getR0()` expression; the formula above follows the executable expression. [Functions and update][M-BAT].

### Step C: correct the polarization estimate using measured voltage

The model predicts terminal voltage, compares it with the measurement, and corrects `Vp` with a scalar Kalman-style gain. If the battery measures lower than predicted, `Vp` is increased, reducing the future current allowance.

```text
Ppred = decay^2 * Pprevious + Qvp * dt
K = Ppred / (Ppred + Rvoltage + R0uncertainty * I^2)
Vp = VpPredicted - K * (Vmeasured - Vpredicted)
P = (1 - K) * Ppred
```

`Qvp = 0.02^2`, `Rvoltage = 0.1^2`, and `R0uncertainty = 0.0015^2`. Increasing uncertainty at high current reduces the amount attributed to polarization. **This filter corrects polarization voltage; SOC remains open-loop charge counting between resets. It is not a full SOC-and-temperature EKF.** [Battery correction][M-BAT].

### Step D: invert the voltage equation to obtain a current allowance

For the manager's 7.0 V model threshold:

```text
Ibattery = max(0, (OCV - Vp - 7.0) / R0)
```

Illustrative arithmetic, not measured robot data: if `OCV = 12.4 V`, `Vp = 0.4 V`, and `R0 = 0.020 ohm`, the allowance is 250 A. That result is only one input to the final budget. The 7.0 V value is a chosen software target, not a statement that every device has the same brownout threshold. [Inversion][M-BAT], [threshold][M-FIN].

### Initialization and limits

The manager initializes the battery estimate from voltage and current at startup and repeatedly reinitializes it while disabled after ten seconds. This can help adapt the estimate to the presently installed battery, but the shown mechanism is not a battery identity/health database. The parameters are described in source as fitted for an MK Powered battery. There is no battery-temperature state or explicit aging-resistance adaptation in this class. [Initialization][M-BAT], [reset calls][M-ROBOT].

It also does not plan energy expenditure over the remaining match or calculate a dependable time-to-empty. Its current allowance is an inversion of the present estimated voltage equation; the three-second look-ahead discussed below belongs to the breaker model. Even SOC zero does not directly force the battery allowance to zero in this implementation. Charge estimates near exhaustion therefore require particular validation.

## 4. Breaker algorithm: accumulated stress with cooling

The main breaker model uses a **dimensionless state `D`**, named `damageState`. It increases during overload and decays during low current. “Damage” is the model's accumulated trip-stress variable; it is not a measurement of permanent component damage or a temperature in Celsius. [BreakerModel][M-BREAK].

The code normalizes current to a 120 A rating and interpolates these points in log-current/log-time space:

| Current relative to 120 A | Current | Modeled trip time |
|---|---|---|
| 1.35 | 162 A | 1,800 s |
| 2.00 | 240 A | 70 s |
| 2.25 | 270 A | 38 s |
| 2.50 | 300 A | 25 s |
| 3.00 | 360 A | 15 s |
| 4.00 | 480 A | 10 s |
| 5.00 | 600 A | 7 s |

The source labels these “maximum hold times from breaker datasheet.” This review did not independently establish a conservative trip envelope for the actual breaker population or ambient conditions. The table should not be read as permission to draw those currents for those durations. [Table and interpolation][M-BREAK].

During overload:

```text
Dnext = D + dt / tripTime(I / 120)
```

At or below 120 A:

```text
Dnext = D * exp(-dt / 60)
```

The state is clamped to `[0, 1]`. At a constant 240 A, ten seconds adds `10/70`, or approximately 0.143. If the state is 0.5, sixty seconds at or below rated current reduces it to approximately 0.184. These are calculations of the model, not physical breaker tests.

To obtain a current allowance, the manager requests a three-second horizon and uses a stress threshold of `1 - 0.05 = 0.95`:

```text
remaining = 0.95 - D
requiredTripTime = 3.0 / remaining
Ibreaker = inverseTripCurve(requiredTripTime)
```

At `D = 0.90`, another three seconds at 240 A would add about 0.043 and stay below 0.95. At `D = 0.93`, the same burst would cross the modeled threshold, so the calculated allowance must fall. This is why overload history matters even if the battery voltage looks acceptable. [Budget calculation][M-BREAK].

The published implementation has edge conditions worth retaining in any source discussion:

- When remaining stress allowance is zero or negative, it returns zero.
- Very short required trip times are capped at the table's 600 A endpoint.
- When required trip time is at least 1,000,000 s, it returns `Double.MAX_VALUE`. With a three-second horizon, that can occur when positive remaining allowance is at most about 0.000003. The mathematical intent would suggest a restrictive allowance there; the code instead makes the breaker constraint nonbinding until the zero-remaining case. This is an implementation edge to review before reuse, not a demonstrated cause of a match failure.
- State starts at zero when a new model is constructed. The battery reset does not reset the breaker state; a process restart does. There is no saved cross-restart thermal history in this class.

The model does not independently model the temperatures of wires, connectors, motors, individual branch breakers, or the battery. [Complete breaker implementation][M-BREAK].

## 5. Combining the models and assigning the remainder to drive

`FinanceDepartment.periodicAfterScheduler()` updates both models and computes:

```text
B = min(0.9 * min(Ibattery, Ibreaker), 190 A)
otherCurrent = totalCurrent - driveWheelCurrent
driveBudgetTarget = max(0, B - otherCurrent)
```

The 0.9 factor leaves headroom. The 190 A cap is the October 17 value; the June 26 EDT snapshot used 200 A. Steering current is included in `otherCurrent`. This is a subtraction of observed consumption, not an optimizer predicting every mechanism's next demand. [Budget manager][M-FIN], [report classification][M-MODULE].

Outside autonomous, the requested per-drive-motor limit is:

```text
driveLimit = floor(clamp(driveBudget / 4, 5 A, 35 A) / 0.5 A) * 0.5 A
```

Illustrative examples at a 190 A budget, with no active recovery ramp:

| Other loads | Remaining drive budget | Per-motor limit | Interpretation |
|---|---|---|---|
| 50 A | 140 A | 35 A | Normal maximum for all four drive motors |
| 110 A | 80 A | 20 A | Less available drive demand while mechanisms draw more |
| 150 A | 40 A | 10 A | Drive acceleration is more strongly constrained |
| 185 A | 5 A | 5 A | The floor allows 20 A across four motors, exceeding the 5 A remainder |

A current limit is an upper permission, not a command to consume that current. Four motors limited to 20 A do not necessarily draw 80 A.

**Shooter priority is implicit:** the flywheel's measured draw is subtracted before the drive allowance is calculated. The manager does not increase the flywheel hardware current limit to match what it removed from drive, and it does not expose a general list of subsystem requests and priorities. [Manager API][M-FIN], [flywheel hardware limits][M-FLYCPP].

## 6. Brownout recovery and autonomous are different paths

The brownout signal becomes active immediately and remains active until it has been clear for two seconds. While this debounced condition is active, drive budget can fall immediately but can rise by only **50 A/s total across the drivetrain**. Outside that condition, this particular recovery rate limit is not applied. At a 5 ms loop period, the maximum increase is 0.25 A in total budget per iteration, before division and quantization. [Recovery logic][M-FIN], [rate constant][M-LIMITS].

This asymmetric response avoids immediately asking for all the available drive current again after a brownout. It is a recovery mechanism, not a guarantee that the first voltage dip will be avoided.

**Autonomous overrides the resulting per-motor limit with 50 A.** The models continue to run, but `getDriveLimit()` returns the fixed autonomous value. Consequently, do not describe the adaptive budget as enforced in all modes or as a hard total-current ceiling. The source proves this policy; its rationale and event-by-event effectiveness were not verified. [Override][M-FIN].

## 7. How a software budget reaches a real motor controller

The Java module writes the result to `driveSupplyCurrentLimit`. Through the Idun IO boundary, C++ `ModuleIOTalonFX` copies it into the Talon FX supply-current configuration. A background configurator applies changed limits, skips identical requests, replaces pending values with newer ones rather than building a queue, and retries failed writes with a 20 ms delay. [Module][M-MODULE], [IO output][M-MODULEIO], [C++ application][M-MODULECPP], [configurator][M-CONFIGURATOR].

This keeps potentially blocking configuration calls out of the main control path. It also means **calculated limit and applied hardware limit are not automatically simultaneous**. A MechaRAMS implementation should observe successful application and response latency, not just the number returned by its budget formula.

The main loop's nominal period is 5 ms. That is not proof that CAN configuration updates, current telemetry, and physical current response all occur with that same latency. [Robot scheduling][M-ROBOT], [loop period][M-CONSTANTS].

## 8. The related flywheel algorithm

There is another useful power-aware control feature in `Flywheel.runVelocity()`: the velocity setpoint is ramped according to what its motor model says can be achieved under a power allowance. [Flywheel model and controller][M-FLY].

The published controller uses a four-Kraken FOC motor model, an inertia of 0.02 kg m², and efficiency 0.8. It selects an aggregate supply allowance for trajectory shaping: 160 A in normal teleop, 60 A in autonomous, or 50 A when `sportMode` is true. These are tunable trajectory-model inputs. Separately, the hardware is configured with a 40 A supply limit per flywheel motor. Those two layers must not be conflated. [Mode selection][M-FLY], [hardware configuration][M-FLYCPP], [constants][M-LIMITS].

The calculations are:

```text
Pavailable = supplyAllowance * measuredBusVoltage * efficiency
backEMF = setpointSpeed / motorKv
Ipower = (-backEMF + sqrt(backEMF^2 + 4 * R * Pavailable)) / (2 * R)
Ivoltage = max(0, (busVoltage - backEMF) / R)
Iavailable = min(Ipower, Ivoltage)
maxAcceleration = clamp((motorTorque(Iavailable) - frictionTorque) / inertia,
                        0, configuredAccelerationCeiling)
```

The first current solution follows the positive-power equation `P = R*I^2 + backEMF*I`. The comments immediately above the executable quadratic contain a sign inconsistency; the expression above follows the executable calculation.

The controller limits the speed-setpoint step to `maxAcceleration * dt`, smooths acceleration for feedforward with a 50 ms time constant, and generates the velocity/feedforward output. The configured acceleration ceiling is 350 rad/s². Lower available voltage or higher back EMF reduces the modeled acceleration capability. This helps avoid commanding a speed trajectory that the allowed electrical power cannot support. It does not add a new battery energy source or guarantee shooter-speed recovery while feeding balls. [Executable flywheel algorithm][M-FLY].

This is separate from `FinanceDepartment`: the flywheel uses bus voltage and its selected allowance, while the robot-wide manager subtracts its measured supply current when setting drive limits.

## 9. What the innovation amounts to

The constituent ideas—equivalent-circuit battery models, Peukert correction, scalar filtering, accumulated overload models, and motor physics—are established engineering methods. The meaningful contribution visible here is their integration into a practical robot control loop with logging, mode policies, a drivetrain actuator path, and a power-aware flywheel setpoint.

It is more sophisticated than one fixed current limit or a simple “voltage is low, slow down” rule. It is not a full model-predictive controller, a per-wire temperature simulator, a guarantee against brownouts, or a general subsystem allocation optimizer. Those distinctions define what MechaRAMS can reasonably reuse.

The most consequential validation questions are whether total current is being estimated accurately, whether parameters match our batteries, whether breaker state is credible, and whether hardware limit changes arrive before a damaging demand transient. The [MechaRAMS roadmap](05-MECHARAMS-ROADMAP.md) turns those questions into staged experiments.

<!-- Source references: generated from pinned research baselines. -->

[H-CTRE]: https://v6.docs.ctr-electronics.com/en/stable/docs/hardware-reference/talonfx/improving-performance-with-current-limits.html
[M-BAT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/BatteryEstimator.java
[M-BREAK]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/BreakerModel.java
[M-CONFIGURATOR]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/cpp/util/TalonFXCurrentConfigurator.cpp
[M-CONSTANTS]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/Constants.java
[M-FIN]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/FinanceDepartment.java
[M-FLY]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/launcher/flywheel/Flywheel.java
[M-FLYCPP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/cpp/subsystems/FlywheelIOTalonFX.cpp
[M-LIMITS]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/CurrentLimits.java
[M-LOGGER]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/BatteryLogger.java
[M-MODULE]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/drive/Module.java
[M-MODULECPP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/cpp/subsystems/ModuleIOTalonFX.cpp
[M-MODULEIO]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/drive/ModuleIO.java
[M-ROBOT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/Robot.java
