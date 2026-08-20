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
| Left-stick press | Seed drivetrain X/Y/yaw from the freshest accepted MultiTag camera pose; works while disabled and is blocked during enabled autonomous |
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
- `Seed Pose From Vision`
- `Precision Drive To Tag Board`
- `Aim At Goal - Stationary`
- `SysId Select Translation`
- `SysId Select Steer`
- `SysId Select Rotation`
- `Close Current Log And Start New (Disabled Only)`
- `Delete Stored Logs And Start Fresh (Disabled Only)`
- `Autonomous Mode`

### Finalizing a test log

After all intended runs, disable the robot and press `Close Current Log And Start New (Disabled Only)`
once. The command is rejected while enabled. In AdvantageScope, wait for
`RealOutputs/Logging/RotationPending` to return to `false` and confirm
`RealOutputs/Logging/RotationCount` increased by one. The previous file in `/home/lvuser/logs` is then
closed and safe to download; a new file is already recording subsequent data. Confirm
`RealOutputs/Logging/LastRotationError` is empty and use `RealOutputs/Logging/ActiveLogPath` to identify
the new `akit_rotated_*` file.

### Deleting stored logs

After downloading every log you want to keep, disable the robot and press
`Delete Stored Logs And Start Fresh (Disabled Only)`. This is irreversible on the roboRIO. The command
briefly detaches file logging, closes the former writer, recursively deletes every file and subdirectory
under `/home/lvuser/logs`, then opens one fresh active WPILOG in the background. This ordering allows
recovery even when the filesystem is already full. It is rejected while enabled; live NT4 remains active.
Wait for `RealOutputs/Logging/PurgePending = false`, verify `PurgeCount` incremented, and confirm
`LastRotationError` is empty. The folder will contain only the new active log.

## Autonomous Mode Options (2026-07-16)

The `Autonomous Mode` chooser now includes vision A/B experiment autos. Every option (baseline or AB)
sets the vision configuration explicitly at start, and the active configuration is logged at
`Vision/Modes/*`, so each log names the setup that produced it.

Current-pose forward tests (added 2026-08-09):

- `Forward 1m - PnP + Iso`
- `Forward 1m - TrigSolve + Iso`
- `Forward 1m - PnP + Aniso`
- `Forward 1m - TrigSolve + Aniso`
- `Forward 2m - PnP + Iso`
- `Forward 2m - TrigSolve + Iso`
- `Forward 2m - PnP + Aniso`
- `Forward 2m - TrigSolve + Aniso`

These commands do not reset pose. At autonomous start they capture the current fused `Drive/Pose`,
hold its Y coordinate, add the selected distance to field X, and target a final heading of 0 degrees.
Place the robot facing field +X before enabling and leave clear travel space beyond the target.

Baselines (PnP single-tag + isotropic covariance — the validated 2026-06-30 behavior):

- `No Auto`, `Precision To Tag Board`, `PathPlanner Auto: VisionTest`,
  `VisionTest then Precision (sequential)`, `VisionTest (spatial handoff)`

A/B experiments (see VISION_AND_TRAJECTORY_TEST_PLAN.md, "2026-07-16 A/B validation plan"):

- `AB: Precision To Tag Board (TrigSolve)` — single-tag trig solve, isotropic covariance
- `AB: VisionTest spatial handoff (TrigSolve)` — trig solve during the primary handoff pattern
- `AB: VisionTest spatial handoff (AnisoCov)` — PnP + anisotropic (ray-aligned) covariance
- `AB: VisionTest spatial handoff (TrigSolve+AnisoCov)` — both experiments together

Curved-trajectory variants (S-curve dip to y=1.25 + 25° mid-path rotation sweep — the vision-stress
transit; same handoff and precision finish as the straight runs):

- `VisionTestCurved (spatial handoff)` — curved baseline (PnP + isotropic)
- `AB: Curved handoff (TrigSolve)`
- `AB: Curved handoff (TrigSolve+AnisoCov)`

Exact run order for all of these: VISION_AND_TRAJECTORY_TEST_PLAN.md, "Execution checklist".

## Closed-loop precision validation (2026-08-19)

The 1 m/2 m precision command now uses a dedicated CTRE closed-loop `Velocity` request. The generic
robot-relative request used by other commands is unchanged. For the first comparison, keep using the
same `Forward 1m - PnP + Iso` and `Forward 2m - PnP + Iso` selections and do not change speed,
acceleration, pose PID gains, wheel radius, or vision settings.

Before each run, confirm `DriveToPose/Controller/DriveRequestType = Velocity`. During the run,
`DriveToPose/Controller/Active` is true. The exact AdvantageScope line-graph and measurement checklist
is in `VISION_AND_TRAJECTORY_TEST_PLAN.md`, section "2026-08-19 closed-loop velocity validation."

AdvantageKit now registers the REV PDH explicitly before the logger starts, using CAN ID 1 (the
documented REV default). Check that `PowerDistribution/ChannelCount = 24`, and that
`PowerDistribution/Voltage` and `PowerDistribution/TotalCurrent` are nonzero. If the robot's PDH was
changed from CAN ID 1, update `Constants.HardwareConstants.POWER_DISTRIBUTION_CAN_ID` to the value shown
by REV Hardware Client before running characterization.

## Measured-distance feedforward validation (2026-08-20)

The translation profile feedforward now fades linearly from full strength at 0.35 m remaining to zero
at the 0.04 m tolerance. Pose feedback is not faded. Use the same `Forward 1m - PnP + Iso` and
`Forward 2m - PnP + Iso` chooser entries; constraints, gains, and wheel radius remain unchanged.

New log checks are `DriveToPose/Controller/TranslationFeedforwardScale` (1 far away, 0 at the target),
`RawProfileVxFieldMetersPerSecond` (before fade), and `ProfileVxFieldMetersPerSecond` (after fade and
actually added to feedback). Follow the ordered gate in `VISION_AND_TRAJECTORY_TEST_PLAN.md` before
running any SysId or increasing speed.
