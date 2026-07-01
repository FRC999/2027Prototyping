# VisionTestingAndCalibration Robot Controls

Controller: one Xbox controller on port 0.

## Driving

| Control | Action |
| --- | --- |
| Left stick Y | Field-relative forward/back drive |
| Left stick X | Field-relative left/right strafe |
| Right stick X | Rotate |
| Right bumper held | Slow mode, 35 percent speed |
| Y held | Stop drivetrain |

## Pose and Precision Testing

| Control | Action |
| --- | --- |
| A press | Reset pose to test start: `(1.5 m, 2.0 m, 0 deg)` — **teleop/disabled only** (ignored during enabled autonomous so it can't corrupt a running auto) |
| B press | Seed operator perspective as blue-forward — **teleop/disabled only** |
| X press | Run precision drive to tag-board test pose: `(4.25 m, 2.0 m, 0 deg)` |

## Aiming (chassis only — no turret/mechanism)

Aims the whole chassis at the configurable virtual goal (`AimConstants.GOAL_POSITION`).

| Control | Action |
| --- | --- |
| Right trigger held | Drive normally (left stick) while the robot auto-faces the goal and leads its motion (shoot-on-move) |
| Right stick press | Stationary "square up to the goal" until settled |

## SysId Selection

| Control | Action |
| --- | --- |
| D-pad up | Select translation SysId |
| D-pad right | Select steer SysId |
| D-pad down | Select rotation SysId |

## SysId Run Commands

Use only with the robot safely lifted or in a clear test area as appropriate for the selected routine.

| Control | Action |
| --- | --- |
| Left bumper + Back held | Quasistatic reverse |
| Left bumper + Start held | Quasistatic forward |
| Left trigger + Back held | Dynamic reverse |
| Left trigger + Start held | Dynamic forward |

## Dashboard Commands

SmartDashboard also exposes:

- `Reset Pose - Test Start`
- `Precision Drive To Tag Board`
- `Aim At Goal - Stationary`
- `SysId Select Translation`
- `SysId Select Steer`
- `SysId Select Rotation`
- `Autonomous Mode`
