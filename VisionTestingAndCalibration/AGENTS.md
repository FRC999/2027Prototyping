# Agent Contract

This repository is a Team 999 MechaRAMS FRC vision, localization, trajectory, and calibration prototype.

## Current Architecture

- Robot code: Java WPILib 2026 command robot.
- Drivetrain: 2025 chassis, CTRE Phoenix 6 `SwerveDrivetrain`, SDS MK4 L3 modules, 2025 CAN IDs, 250 Hz
  odometry (roboRIO/CANivore rate — NOT a camera/Orange Pi rate).
- Vision: PhotonVision on Orange Pi(s) with Arducam OV9782 global-shutter color USB2 cameras, behind the
  **AdvantageKit IO-layer** (`subsystems/vision/{VisionIO, VisionIOPhotonVision, VisionIOPhotonVisionSim,
  Vision}`). Start config = 2 cameras / 1 Pi; scalable to 4 cameras / 2 Pis.
- A **working PhotonVision simulation** (`VisionIOPhotonVisionSim`) means the whole pipeline is desktop-
  testable; validate in sim first.
- Final fusion and drivetrain control stay on the roboRIO (no Mac-mini-style offboard brain).
- Precision driving (`DriveToPosePrecisionCommand`) and chassis aiming (`AimingCalculator` + aim commands,
  no turret/GPM) build on the fused pose.
- Logging/replay: AdvantageKit plus concise NetworkTables/dashboard outputs.
- Authoritative design doc: `MechaRAMS vision system/ARCHITECTURE_AND_DEPLOYMENT.md`.

## Required Development Rules

- Read `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\SESSION_STATE.md` before changing behavior.
- Update that session-state file before and after every substantial task.
- Put mentor prompts and meaningful follow-up prompts in `AI_PROMPTS.md`.
- Keep hardware changes explicit. Do not guess CAN IDs, module order, camera transforms, gear ratios, tag coordinates, or controller bindings.
- Keep final drivetrain control on the roboRIO unless a later timing audit proves otherwise.
- Prefer measured constants and replayable logs over tuning by impression.
- Do not add high-frequency SmartDashboard spam. Use AdvantageKit logs for high-rate data.

## Vision Rules

- Keep the AdvantageKit IO-layer shape; capture inputs with `Logger.processInputs` so logs replay.
- Consume every unread frame from every camera (`getAllUnreadResults`). Fuse each at its own PhotonVision
  timestamp; CTRE's odometry buffer handles time alignment (no explicit sort needed unless you replace the
  estimator with a sequential custom one).
- **Convert PhotonVision (FPGA) timestamps to the CTRE time base with `Utils.fpgaToCurrentTime` before
  `addVisionMeasurement`.** This is a correctness requirement, not a nicety.
- Reject NaN/Inf, impossible Z, off-field, too far, high single-tag ambiguity — log a `RejectionReason`
  enum, not just a string.
- Single-tag heading std dev = `Double.POSITIVE_INFINITY` (trust XY only). Trust theta only from multi-tag.
- Covariance = `baseline * dist^2 / tagCount^2 * cameraFactor` (per-camera factors; tag count squared, per 6328/6995).
- Record accepted/rejected counts, accepted/rejected poses, tag poses, and innovation distance.
- Keep `VisionPolicyTest` passing.

## Aiming Rules (chassis only — no turret/GPM)

- Aim the whole chassis at the configurable `AimConstants.GOAL_POSITION`; keep `AimingCalculator` pure and
  unit-tested (`AimingCalculatorTest`).
- Shoot-on-move lookahead is a teaching artifact; no shooter/hood/turret/mechanism code.
- Log target heading, heading error, distance, and lead/goal poses.

## Trajectory Rules

- PathPlanner/Choreo trajectories are coarse motion tools.
- Final precision uses `DriveToPosePrecisionCommand`: profiled x/y/theta + velocity FF, settle gate, and
  a safety timeout; a time-based path must never *finish* a precise move.
- Coarse->precise handoff via `handoffFrom(path, spatialCondition)`.
- Every precision test must log target pose, measured final pose, translation error, rotation error, and
  settle time.

## Verification Expectations

- Run `.\gradlew.bat compileJava` AND `.\gradlew.bat test` with Java 17+ (`JAVA_HOME` =
  `C:\Users\Public\wpilib\2026\jdk`). Keep the unit tests green.
- Validate behavior in simulation first (`CALIBRATION_AND_TEST_PROCESS.md` Stage 1).
- For robot-affecting changes, update `ROBOT_CONTROLS.md`, `VISION_AND_TRAJECTORY_TEST_PLAN.md`, and
  `ARCHITECTURE_AND_DEPLOYMENT.md`.
