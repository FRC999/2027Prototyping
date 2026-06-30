# FRC Swerve Drivetrain Skill

Use this skill when modifying the CTRE Phoenix 6 swerve drivetrain, odometry, PathPlanner wiring, SysId,
or the simulation thread.

## Required Reads

- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\AGENTS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\ARCHITECTURE_AND_DEPLOYMENT.md` (Part 2.4)

## Architecture

`DriveSubsystem extends SwerveDrivetrain<TalonFX, TalonFX, CANcoder> implements Subsystem`. Built from the
2025 module map in `Constants.SwerveConstants` via `DriveSubsystem.create()` (explicit per-corner module
constants — keep it boring and pit-verifiable). Idea: CTRE Phoenix 6 generated-project shape.

## Design Rules

- Use the **2025 chassis** CAN IDs, bus name (`canivore1`), Pigeon ID (40), and module offsets exactly.
  Do not guess hardware constants; mark unmeasured values provisional.
- Odometry at **250 Hz** (`ODOMETRY_UPDATE_FREQUENCY_HZ`) — this is the roboRIO/CANivore sampling thread,
  NOT a camera/Orange Pi rate.
- Keep CTRE's built-in pose estimator; vision enters through `addVisionMeasurement(pose,
  Utils.fpgaToCurrentTime(t), stdDevs)`. (Replacing it with a custom estimator is a documented, deferred
  option — see `DESIGN_DECISIONS_AND_REJECTED_IDEAS.md`.)
- Expose `getPose`, `getFieldRelativeSpeeds`, `getRobotRelativeSpeeds`, `driveFieldRelative`,
  `driveRobotRelative`, `stop`.
- PathPlanner via `AutoBuilder.configure(...)`, passing CTRE wheel-force feedforwards; missing config must
  warn, not crash.
- Simulation: a 5 ms `Notifier` runs `updateSimState`. SysId via the three CTRE swerve requests.
- `periodic()` logs `Drive/Pose`, `Drive/Speeds`, module states/targets (replay + AdvantageScope).

## Verification

- `./gradlew.bat compileJava` and `./gradlew.bat test` (Java 17).
- Drive in simulation; confirm `Drive/Pose` tracks commands (`CALIBRATION_AND_TEST_PROCESS.md` Stage 1).
- Characterize with SysId before trusting precision gains (Stage 5).
