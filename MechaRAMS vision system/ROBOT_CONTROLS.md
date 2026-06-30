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
| A press | Reset pose to test start: `(1.5 m, 2.0 m, 0 deg)` |
| B press | Seed operator perspective as blue-forward |
| X press | Run precision drive to tag-board test pose: `(4.25 m, 2.0 m, 0 deg)` |

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
- `SysId Select Translation`
- `SysId Select Steer`
- `SysId Select Rotation`
- `Autonomous Mode`
