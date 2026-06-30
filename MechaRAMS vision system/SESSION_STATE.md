# Session State - VisionTestingAndCalibration

Last updated: 2026-06-30

## Current Objective

Create a 2027 prototyping Java project for the 2025 MechaRAMS chassis that tests PhotonVision AprilTag localization, CTRE swerve trajectory driving, precision final-pose control, AdvantageKit logging/replay, and AI-assisted development process.

## Current Architecture Decisions

- Use PhotonVision on one Orange Pi with two USB2 Arducam OV9782 global-shutter color cameras.
- Start with two cameras, not four cameras/two Orange Pis. Add more only if logs show coverage or bandwidth limits.
- Mount cameras at the front-left and front-right corners, above/inside the front swerve modules, cross-eyed toward the robot centerline.
- Keep final pose fusion and drivetrain control on the roboRIO.
- Use 2025 drivetrain CAN IDs, Pigeon ID, and module offsets.
- Treat SDS MK4 L3 as 6.12:1 drive, 12.8:1 steer, 4 inch wheel until characterization replaces it.
- Use AdvantageKit for logs and replay-oriented debugging.
- Use PathPlanner for coarse motion and a separate tolerance/settle command for final precision.

## Current Dependency Versions

- CTRE Phoenix 6: `26.3.0` (`vendordeps\Phoenix6-26.3.0.json`)
- AdvantageKit: `26.0.2` (`vendordeps\AdvantageKit.json`)
- PhotonLib: `v2026.3.4` (`vendordeps\photonlib.json`)
- PathPlannerLib: `2026.1.2` (`vendordeps\PathplannerLib-2026.1.2.json`)

## Implemented Files

- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\AGENTS.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\CLAUDE.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.claude\commands\session-update.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.claude\commands\vision-review.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.claude\commands\trajectory-review.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.claude\commands\safety-audit.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.codex\skills\frc-vision-localization\SKILL.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.codex\skills\frc-trajectory-precision\SKILL.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\Constants.java`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\Robot.java`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\RobotContainer.java`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\subsystems\DriveSubsystem.java`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\subsystems\VisionSubsystem.java`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\commands\DriveManuallyCommand.java`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\commands\DriveToPosePrecisionCommand.java`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\deploy\apriltags\mecharams-two-tag-layout.json`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\deploy\pathplanner\settings.json`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\AI_PROMPTS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\INITIAL_PROMPT_REORGANIZED.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\ROBOT_CONTROLS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\VISION_AND_TRAJECTORY_TEST_PLAN.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\SIMULATION_RUNBOOK.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\ARCHITECTURE_AND_DEPLOYMENT.md`

## Verification State

- `.\gradlew.bat compileJava` was run with `JAVA_HOME=C:\Users\Public\wpilib\2026\jdk`.
- Build succeeded on 2026-06-30.
- Deprecated command scheduling and PhotonVision pose-estimator API calls were removed after the first successful compile reported warnings.
- Code-level "Idea traceability" comments were added to the main robot, controls, drivetrain, vision, constants, manual drive, and precision-drive files.
- Latest compile output after documentation pass: `BUILD SUCCESSFUL`, 1 actionable task executed, no deprecation warnings printed.
- User updated CTRE Phoenix 6 and AdvantageKit to latest available versions; compile still worked.

## Known Follow-Ups

- Confirm actual measured camera mounts and update robot-to-camera transforms.
- Run camera calibration in PhotonVision for each Arducam.
- Run drivetrain characterization and replace wheel radius/feedforward gains.
- Create the `VisionTest` PathPlanner auto after opening the project in PathPlanner.
- Add full PhotonVision camera simulation once compile verification reaches PhotonLib simulation APIs.
- Decide whether to add a full AdvantageKit IO-layer split after the pilot compiles and drives.
- Re-run simulation instructions after Java 17 is available and update `SIMULATION_RUNBOOK.md` with screenshots or exact WPILib menu names if the UI differs.

## Rules For Next AI Session

- Read this file first.
- Update `AI_PROMPTS.md` with any new mentor prompts that affect design.
- Update this file before ending the session.
- Do not guess hardware constants. If a value is not measured, mark it as provisional.
