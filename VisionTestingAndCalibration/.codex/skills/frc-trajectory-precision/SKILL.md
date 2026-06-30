# FRC Trajectory Precision Skill

Use this skill when modifying PathPlanner/Choreo, autonomous driving, the precision final-pose
controller, the coarse→precise handoff, or SysId/characterization.

## Required Reads

- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\AGENTS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\ROBOT_CONTROLS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\DESIGN_DECISIONS_AND_REJECTED_IDEAS.md`

## Design Rules

- Trajectories (PathPlanner now; Choreo is the documented upgrade) are **coarse** motion only.
- Finish precise moves with `DriveToPosePrecisionCommand`, which must keep:
  - **profiled** x/y/θ control with velocity feedforward (trapezoid decelerates to 0 at the goal). Idea:
    1768 `driveToPose`; 6328 `DriveToPose` FF-fade.
  - a **settle gate** (hold tolerance for `PRECISION_SETTLE_SECONDS`). Idea: 1768 `cmdWithAccuracy`.
  - a **safety timeout** (`PRECISION_SAFETY_TIMEOUT_SECONDS`) so a bad target can't hang it. Idea: 1768
    `.withTimeout(totalTime + slack)`.
  - full logging: target, measured, translation/rotation error, settle, finished/timedOut. Idea: 6328.
- Coarse→precise handoff: run the path until a spatial condition, then `DriveToPose`
  (`DriveToPosePrecisionCommand.handoffFrom`). Idea: 6328 `DriveTrajectory.andThen(DriveToPose)`.
- A time-based path must NEVER be what *finishes* a precise move.
- Keep speed/accel constraints conservative until SysId characterization is trusted.
- Missing PathPlanner autos must be non-fatal (the chooser catches and prints).

## Choreo (if/when adopted)

Add the ChoreoLib vendordep and use `new AutoFactory(pose, reset, follow, true /*useAllianceFlipping*/,
drive, logger)`. One boolean flips red/blue (proven by 6995). PathPlannerLib can also follow `.traj`.

## Verification

- `./gradlew.bat compileJava` and `./gradlew.bat test` (Java 17).
- Validate precision + handoff in simulation (`CALIBRATION_AND_TEST_PROCESS.md` Stages 1, 6) first.
- Confirm default autonomous is safe (`No Auto`).
