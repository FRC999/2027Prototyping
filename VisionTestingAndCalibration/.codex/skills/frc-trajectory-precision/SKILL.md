# FRC Trajectory Precision Skill

Use this skill when modifying PathPlanner, Choreo, autonomous driving, final pose tolerance, or SysId/characterization behavior.

## Required Reads

Before editing, read:

- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\AGENTS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\ROBOT_CONTROLS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\VISION_AND_TRAJECTORY_TEST_PLAN.md`

## Design Rules

- Use trajectories for coarse motion.
- Use a separate final pose controller for last-step precision.
- Require explicit tolerance and settle time.
- Keep speed constraints conservative until drivetrain characterization is trusted.
- Make missing path files non-fatal during development.
- Document controls for every data-collection or SysId routine.

## Verification

- Run compile with Java 17 or newer.
- Confirm default autonomous is safe.
- Log target pose, current pose, final translation error, and final rotation error when adding new precision tests.
