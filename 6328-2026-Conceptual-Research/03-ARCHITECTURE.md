# Architecture diagrams

These diagrams describe the reviewed October 17, 2026 source. Arrows represent data or software commands, not electrical wiring. GitHub renders the fenced Mermaid blocks in normal Markdown view. See [vision](01-VISION-WALKTHROUGH.md), [power](02-POWER-WALKTHROUGH.md), and [source versions](06-EVIDENCE-AND-HISTORY.md) for qualifications.

## 1. Vision deployment and control boundary

```mermaid
flowchart TD
    subgraph sensing ["Sensing"]
        cameras["Camera images"]
        wheels["Wheel encoders and gyro"]
    end
    subgraph macMini ["Mac mini"]
        northstar["Northstar Python and native detector"]
        vision["Java Vision filtering"]
        robotState["RobotState pose fusion"]
        commands["Robot control commands"]
    end
    subgraph rio ["roboRIO and motor controllers"]
        hardware["C++ hardware IO"]
        motors["Motor controllers"]
    end
    cameras --> northstar
    northstar -->|"Timestamped poses via NetworkTables"| vision
    vision --> robotState
    wheels --> hardware
    hardware -->|"Idun sensor observations"| robotState
    robotState --> commands
    commands -->|"Idun outputs"| hardware
    hardware --> motors
```

The detector and robot pose estimator are separate programs/layers even though the deployment places both on the Mac mini. The power algorithms do not intrinsically require that architecture. Sources: [Northstar main][M-NSMAIN], [Vision][M-VISION], [RobotState][M-STATE], [robot scheduling][M-ROBOT], [hardware IO][M-MODULECPP].

## 2. Aruco Max detection within Northstar

```mermaid
flowchart TD
    imageIn["Grayscale frame"] --> threshold["Metal: adaptive threshold"]
    threshold --> contours["C++: contour tracing"]
    contours --> quads["Keep sufficiently large convex quadrilaterals"]
    quads --> decode["Sample cells and decode 36h11 dictionary"]
    decode --> retries["Retry sampling for undecoded candidates"]
    retries --> uniqueMarkers["Remove nested duplicate detections"]
    uniqueMarkers --> corners["OpenCV: refine corners to subpixel positions"]
    corners --> detectionOut["Tag IDs and image corners"]
    detectionOut --> poseSolve["Separate OpenCV pose solver"]
```

Only the thresholding stage shown is explicitly implemented with Metal. Rejected candidates do not become accepted tags merely by reaching the next box; each stage retains only candidates satisfying its checks. Sources: [detector][M-DETECT], [Metal kernels][M-METAL], [worker wiring][M-WORKER].

## 3. From detected corners to a corrected robot position

```mermaid
flowchart TD
    detections["IDs and corners plus calibration and field layout"] --> countTags{"Known tags in this image?"}
    countTags -->|"None"| noResult["No pose result"]
    countTags -->|"One"| single["IPPE square: two candidates and errors"]
    countTags -->|"Multiple"| multiple["SQPnP: one field-camera pose"]
    single --> ambiguity["Error-ratio gate then heading-based choice"]
    ambiguity --> noHeading["Disable vision heading correction"]
    multiple --> withHeading["Allow vision heading correction"]
    noHeading --> transform["Camera-to-robot transform at frame time"]
    withHeading --> transform
    transform --> gates["Field and Z checks; camera and auto policies"]
    gates --> weights["Distance and tag-count weighting"]
    weights --> sortResults["Sort observations by timestamp"]
    sortResults --> history["Correct at measurement time; move forward with odometry"]
    history --> poseOut["Updated robot pose"]
```

This is the dependency flow, not a line-by-line call-order diagram. Failed gates discard the observation. Camera 1 is normally withheld unless camera 0 disconnects, and fusion is withheld for the first two autonomous seconds. Sources: [pose solve][M-PNP], [robot-side policies][M-VISION], [constants][M-VCONST], [history correction][M-STATE].

## 4. Power estimation and feedback

```mermaid
flowchart TD
    sensors["Motor supply currents and auxiliary estimates"] --> sumLoads["BatteryLogger: total and drive current"]
    voltage["Measured robot supply voltage"] --> battery["BatteryEstimator: SOC, resistance, polarization"]
    sumLoads --> battery
    sumLoads --> breaker["BreakerModel: accumulated stress and cooling"]
    battery --> batteryLimit["Current allowed by 7 V model target"]
    breaker --> breakerLimit["Current allowed over 3-second horizon"]
    batteryLimit --> totalBudget["Smaller allowance times 0.9; cap at 190 A"]
    breakerLimit --> totalBudget
    sumLoads --> otherLoads["Other current = total minus drive wheels"]
    totalBudget --> remainder["Drive receives remaining current budget"]
    otherLoads --> remainder
    remainder --> policy["Brownout recovery and mode policy"]
    policy --> hardwareLimit["Supply-current limits sent to four drive controllers"]
    hardwareLimit -.->|"Changes future measured demand"| sensors
```

The returned drive limits have floors, caps, and an autonomous override. Consequently the total-budget box is a control target, not an enforced electrical ceiling. Sources: [FinanceDepartment][M-FIN], [logger][M-LOGGER], [battery][M-BAT], [breaker][M-BREAK].

## 5. Drive allocation policy, including the exception

```mermaid
flowchart TD
    target["Compute total budget minus other measured loads"] --> recovery{"Brownout or within clear-delay window?"}
    recovery -->|"Yes"| ramp["Decrease immediately; increase at most 50 A per second total"]
    recovery -->|"No"| direct["Use calculated drive budget"]
    ramp --> floorBudget["Clamp total drive budget to zero or above"]
    direct --> floorBudget
    floorBudget --> split["Divide by four; clamp to 5-35 A; round down by 0.5 A"]
    split --> autoMode{"Autonomous mode?"}
    autoMode -->|"Yes"| fixed["Override with 50 A per drive motor"]
    autoMode -->|"No"| adaptive["Keep calculated per-motor limit"]
    fixed --> applyLimit["Apply through asynchronous Talon configurator"]
    adaptive --> applyLimit
```

The recovery delay is two seconds after brownout clears. It affects the computed drive budget, but autonomous subsequently overrides the returned limit. Sources: [manager][M-FIN], [constants][M-LIMITS], [configurator][M-CONFIGURATOR].

## 6. Separate power-aware flywheel setpoint

```mermaid
flowchart TD
    supply["Mode-dependent supply allowance"] --> power["Power allowance from bus voltage and efficiency"]
    volts["Measured bus voltage"] --> power
    power --> current["Motor model: power and voltage current ceilings"]
    speed["Current speed setpoint and back EMF"] --> current
    current --> acceleration["Torque minus friction divided by inertia"]
    acceleration --> ramp["Limit velocity-setpoint step"]
    requested["Requested shooter speed"] --> ramp
    ramp --> output["Velocity and feedforward outputs"]
```

This trajectory shaping does not receive a dynamically allocated shooter budget from `FinanceDepartment`. It uses its own mode-selected allowance and the manager's measured voltage. Source: [Flywheel][M-FLY].

<!-- Source references: generated from pinned research baselines. -->

[M-BAT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/BatteryEstimator.java
[M-BREAK]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/BreakerModel.java
[M-CONFIGURATOR]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/cpp/util/TalonFXCurrentConfigurator.cpp
[M-DETECT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/aruco_max.h
[M-FIN]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/FinanceDepartment.java
[M-FLY]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/launcher/flywheel/Flywheel.java
[M-LIMITS]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/CurrentLimits.java
[M-LOGGER]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/BatteryLogger.java
[M-METAL]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/metal_context.cpp
[M-MODULECPP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/cpp/subsystems/ModuleIOTalonFX.cpp
[M-NSMAIN]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/__init__.py
[M-PNP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/pipeline/CameraPoseEstimator.py
[M-ROBOT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/Robot.java
[M-STATE]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/RobotState.java
[M-VCONST]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/vision/VisionConstants.java
[M-VISION]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/vision/Vision.java
[M-WORKER]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/apriltag_worker.py
