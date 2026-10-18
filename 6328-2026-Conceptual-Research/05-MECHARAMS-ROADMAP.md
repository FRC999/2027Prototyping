# What MechaRAMS could adopt

This is a proposed research path, not an implemented change or a claim that 6328 follows these exact steps. It builds on our [existing research handoff](../MechaRAMS%20vision%20system/RESEARCH_HANDOFF.md), [calibration playbook](../MechaRAMS%20vision%20system/COMPETITION_CHASSIS_CALIBRATION_PLAYBOOK.md), and [vision strategy](../MechaRAMS%20vision%20system/Vision-Strategy-2027-v2.md).

## 1. Recommended order

**Keep the present vision baseline while measuring its actual failure modes. Start power research with logging and advisory calculations.** Neither undertaking requires us to move final robot control to a Mac mini.

| Candidate idea | Value to investigate | Prerequisite / cost | Recommendation |
|---|---|---|---|
| Better frame timing, calibration, result queues, and fusion diagnostics | Directly improves our ability to trust localization | Ground-truth measurements and existing estimator integration | Continue the existing work |
| PhotonVision AprilTag versus ArUco pipeline comparison | Tests a detector choice within our current toolset | Matched camera settings and repeatable scenes | Low-cost comparison before custom development |
| Aruco Max | May improve detection throughput or corner quality in its intended environment | Apple-specific build, or a substantial port; representative benchmark | Research only if a detector bottleneck is measured |
| Current and voltage accounting | Makes power problems explainable | Reliable supply-current inputs and timestamps | First power-management milestone |
| Advisory battery/breaker models | Tests predictions without changing outputs | Battery data, current validation, model fitting | Second milestone |
| Adaptive drive limits | Can reduce competing demand | Tested model, explicit mode policy, verified actuator response | Controlled experiment after advisory validation |
| Power-aware shooter speed ramp | Commands more achievable flywheel acceleration | Shooter inertia/motor model and repeatable spin-up tests | Separate experiment from drive budgeting |

This ordering follows the capabilities and limitations found in [Northstar](01-VISION-WALKTHROUGH.md) and [FinanceDepartment](02-POWER-WALKTHROUGH.md), not a claim that one brand is universally superior.

## 2. Vision: determine whether we need a new detector

Use the same field layout, measured camera mounting transform, lens calibration, image resolution, and exposure when comparing detector choices. Record the exact software versions and camera settings. If settings differ intentionally, label the result as a whole-configuration comparison.

Run scenes with near and far tags, one and several tags, oblique views, robot translation, fast rotation, changing lighting, and partial obstruction. Retain representative images or video where practical. Use surveyed static positions and an independent motion reference where available; a fused robot estimate cannot independently validate itself.

Measure separate layers:

| Layer | Useful observations | What it can tell us |
|---|---|---|
| Image/detection | Correct IDs, missed tags, false detections, corner stability | Whether changing detection is likely to help |
| Timing | Capture-to-receive age, processing time, queue backlog, drops | Whether results arrive late or inconsistently |
| Pose solve | Position/orientation error before fusion; single/multi-tag behavior | Whether calibration and geometry are credible |
| Fusion | Accepted/rejected results, rejection reason, error after fusion | Whether robot-side policies are helping |
| Operations | Boot/reconnect, CPU load, thermal throttling, recovery | Whether students can operate it reliably |

Use distributions and worst recurring cases, not only an average FPS or one good stationary plot. A higher frame rate is not sufficient if corner bias grows or delayed frames produce worse motion error.

PhotonVision v2026.3.4 already has an ArUco pipeline; it is a more accessible first comparison than porting Aruco Max to Orange Pi. If comparing against Aruco Max on a Mac, separate **same-image detector quality** from **whole-system performance on different hardware**. A result from those two machines cannot isolate software speed alone. [PV ArUco path][P-ARUCO], [Aruco Max build][M-SETUP].

Success criterion: a proposed change improves our required pose accuracy, detection reliability, or timing under representative conditions without unacceptable maintenance burden. The team should set numerical targets from the scoring task and existing baseline; no target has been invented here.

## 3. Power milestone A: trustworthy measurement

Inventory motor counts, hardware current limits, branch assignments, battery types, and auxiliary loads. Log:

- Battery/robot bus voltage and brownout state.
- Per-motor **supply current**, with explicit units and measurement timestamps.
- Direct PDH/PDP total current, if available, as an independent check against the subsystem sum.
- Drive requests and measured velocity; flywheel speed, speed goal, and feed state.
- Actual robot mode and any driver-selected priority mode.
- Battery identity, charge/rest history, and relevant battery test results.
- Requested current limits, application status, and measured response.

Record sample periods and freshness. A disconnected or stale sensor should be visible; it should not silently make the robot appear to have more spare current.

Collect comparable sessions: drive alone, shooter alone, simultaneous operation, short bursts, and sustained game-like cycles across multiple known batteries. Use existing hardware limits and a supervised test procedure. We do not need to force a real brownout or trip a breaker to learn whether an advisory prediction is credible.

Deliverable: a current-accounting table, time-aligned voltage/current plots, and a list of observed failure triggers. CTRE's documentation also recommends empirical supply-limit tuning from mechanism logs. [CTRE guidance][H-CTRE].

## 4. Power milestone B: advisory models

Compute budgets and record them without applying them. Compare a simple baseline against the richer 6328-style model:

| Model | What it captures | What it misses |
|---|---|---|
| Static current limits | Predictable per-device bounds | Battery condition and recent loading |
| Voltage-feedback derating | Observed low voltage | Acts after sag begins; needs filtering and recovery logic |
| OCV plus effective resistance | First-order prediction of sag | Slower voltage relaxation and charge history |
| One-RC battery model | Immediate sag plus polarization/recovery and SOC dependence | Requires fitting; uncertainty and aging remain |
| Breaker stress model | Accumulated overload history | Requires credible trip/cooling assumptions; not a physical temperature measurement |

These are conceptual choices. The 6328 code specifically implements the one-RC/Peukert/scalar-correction combination and the stress model described in the [power walkthrough](02-POWER-WALKTHROUGH.md).

Fit parameters with our data, then evaluate predictions on different sessions and batteries. Report voltage prediction residuals, occasions when predicted voltage was too optimistic, and how often a suggested limit would have reduced drive capability. An open-loop replay of a lower proposed current limit cannot prove the counterfactual voltage improvement: the recorded robot still drew the original current. Replay tests logic and calibration; a model or physical A/B test is needed to test the changed behavior.

Review the breaker model's near-threshold sentinel branch, table bounds, initial-state handling, and restart behavior before enabling it. Avoid interpreting its dimensionless state as a measured temperature.

## 5. Power milestone C: make the policy explicit

6328's observable policy is “other loads first; drive gets the remainder,” with floors and a fixed autonomous override. Our policy should be chosen for our robot rather than inherited accidentally. [Allocation source][M-FIN].

Decide these points before implementing automatic changes:

| Decision | Why it matters |
|---|---|
| Which load may yield in each mode? | Shooting, escaping defense, and autonomous transit can have different priorities |
| How much control/steering headroom is reserved? | Non-drive demand can rise between measurements |
| Is a drive floor permitted to violate the computed budget? | Mobility and voltage protection can conflict |
| Does autonomous use adaptive limits? | Changing acceleration capability affects path tracking and timing |
| How are stale readings handled? | Missing current data must not create fictitious capacity |
| What happens when an update fails? | A calculated limit is useless if hardware never applies it |
| How does a limit recover? | Immediate increases can cause repeated sag or oscillation |

For a first experiment, keep the allocation rule simple and observable. Log every budget component and the reason for limiting. Use a feature flag and a known baseline configuration so the comparison can be repeated.

A later **general priority allocator** could accept explicit subsystem demand requests and minimum/maximum allowances. For example, it could reserve control power, serve the currently important mechanism, then distribute the remainder. That would be a new MechaRAMS extension; it is not an algorithm found in the reviewed `FinanceDepartment`.

## 6. Power milestone D: verify physical effects

Compare baseline and adaptive behavior using matched batteries and repeatable driving/shooting cycles. Rotate test order so battery depletion does not systematically favor one option. Record:

- Minimum voltage, time below the team's chosen voltage target, and any actual brownout events.
- Flywheel spin-up time, speed recovery during feeding, and scoring throughput.
- Drivetrain acceleration, path/endpoint error, and driver ability to reposition.
- Requested/applied-limit latency and current-response delay.
- Time spent limiting, limit oscillation, and behavior after stale data or failed configuration updates.

A useful outcome is better electrical margin while retaining acceptable scoring and driving performance. Merely reducing current or making a graph smoother is not enough.

Treat the flywheel acceleration model as its own experiment. Measure spin-up at different bus voltages, fit or measure inertia, and distinguish aggregate modeled supply allowance from each controller's hardware limit. Do not assume 6328's four-motor model or constants describe our shooter. [Flywheel source][M-FLY].

## 7. Questions to take back to 6328

The source answers the mechanism questions. These remaining questions require team knowledge or measurements; they are not reasons to invent a conclusion:

1. Which exact PhotonVision version, pipeline, camera, and operating condition failed the requirement discussed in our meeting?
2. Was the meeting reference specifically Aruco Max, or another unpublished/current detector? What matched-image or matched-hardware results can they share?
3. Which events actually used Aruco Max and the integrated `FinanceDepartment`? Public commits alone cannot answer that.
4. How were battery and breaker parameters fitted and checked, especially for different battery ages and ambient temperatures?
5. Why does autonomous use fixed 50 A drive limits, and what evidence supported that tradeoff?
6. How do they handle warm-breaker software restarts, and was the near-threshold sentinel branch intentional?
7. Did the latest power manager materially improve scoring, prevent brownouts, or reduce breaker trips in controlled comparisons?

These questions are prepared for the team; no messages have been sent to 6328.

<!-- Source references: generated from pinned research baselines. -->

[H-CTRE]: https://v6.docs.ctr-electronics.com/en/stable/docs/hardware-reference/talonfx/improving-performance-with-current-limits.html
[M-FIN]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/FinanceDepartment.java
[M-FLY]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/launcher/flywheel/Flywheel.java
[M-SETUP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/setup.py
[P-ARUCO]: https://github.com/PhotonVision/photonvision/blob/a5be3d062ce5c145f4a759b0207057f218cda6b3/photon-core/src/main/java/org/photonvision/vision/pipeline/ArucoPipeline.java
