# Agent Contract

This repository is a Team 999 MechaRAMS FRC vision, localization, trajectory, and calibration prototype.

## Current Architecture

- Robot code: Java WPILib 2026 command robot.
- Drivetrain: 2025 chassis, CTRE Phoenix 6 swerve, SDS MK4 L3 modules, 2025 CAN IDs.
- Vision: PhotonVision on one Orange Pi with two USB2 Arducam OV9782 global-shutter color cameras.
- Final fusion and drivetrain control stay on the roboRIO.
- Logging/replay: AdvantageKit plus concise NetworkTables/dashboard outputs.

## Required Development Rules

- Read `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\SESSION_STATE.md` before changing behavior.
- Update that session-state file before and after every substantial task.
- Put mentor prompts and meaningful follow-up prompts in `AI_PROMPTS.md`.
- Keep hardware changes explicit. Do not guess CAN IDs, module order, camera transforms, gear ratios, tag coordinates, or controller bindings.
- Keep final drivetrain control on the roboRIO unless a later timing audit proves otherwise.
- Prefer measured constants and replayable logs over tuning by impression.
- Do not add high-frequency SmartDashboard spam. Use AdvantageKit logs for high-rate data.

## Vision Rules

- Consume every unread frame from every camera, sorted by timestamp before fusion.
- Reject physically impossible poses: outside field bounds, impossible Z, too far, or high single-tag ambiguity.
- For single-tag observations, trust XY only and set theta standard deviation effectively infinite.
- Trust theta only from genuine multi-tag solves.
- Record accepted/rejected frame counts and rejection reasons.

## Trajectory Rules

- PathPlanner/Choreo trajectories are coarse motion tools.
- Final precision should use a separate pose/tolerance/settle command.
- Every precision test must log target pose, measured final pose, translation error, rotation error, and settle time.

## Verification Expectations

- Try `.\gradlew.bat compileJava` with Java 17 or newer.
- If Java 17 is not available, record that as an environment blocker and still run file/static checks.
- For robot-affecting changes, update `ROBOT_CONTROLS.md` and `VISION_AND_TRAJECTORY_TEST_PLAN.md`.
