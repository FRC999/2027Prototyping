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

Use `SystemStats/BatteryVoltage` for supply voltage. PDH ID 1 was only a documented default, not a
verified hardware measurement; direct polling caused CAN errors and loop overruns. Do not use or restore
PDH current channels until the installed module ID/bus is verified.

## Measured-distance feedforward validation (2026-08-20)

The translation profile feedforward now fades linearly from full strength at 0.35 m remaining to zero
at the 0.04 m tolerance. Pose feedback is not faded. Use the same `Forward 1m - PnP + Iso` and
`Forward 2m - PnP + Iso` chooser entries; constraints, gains, and wheel radius remain unchanged.

New log checks are `DriveToPose/Controller/TranslationFeedforwardScale` (1 far away, 0 at the target),
`RawProfileVxFieldMetersPerSecond` (before fade), and `ProfileVxFieldMetersPerSecond` (after fade and
actually added to feedback). Follow the ordered gate in `VISION_AND_TRAJECTORY_TEST_PLAN.md` before
running any SysId or increasing speed.

## Damped stop and velocity-qualified settle (2026-08-24)

The first fade run reduced fused X overshoot to 3.6 cm but still visibly crossed, returned, and spent
too long correcting. Near-target translation commands now include measured-velocity damping; rotation
has its own rate damping. `AtGoal` now requires both pose tolerance and chassis speed below 0.12 m/s and
8 deg/s. Once qualified, the command latches closed-loop zero velocity during a 0.05 s confirmation instead of
chasing pose noise inside the tolerance window. Ordinary velocity/vision noise cannot release the hold;
active correction resumes only outside the wider 0.06 m or 2.5 degree pose escape envelope.

No direct PDH polling is active. `SystemStats/BatteryVoltage` remains available without adding CAN
traffic or another HAL allocation.

## Wheel-defined settling telemetry (2026-08-24)

The 4 cm translation tolerance is the **radial X/Y error**, not X error by itself, and the robot must
also be within 1.5 degrees of the target heading. It can therefore be inside the X band while lateral
or heading error still commands wheel motion.

Use `Drive/MeanAbsModuleSpeedMetersPerSecond` and `Drive/MaxAbsModuleSpeedMetersPerSecond` to measure
physical settling. Their matching `MeanAbsModuleTargetSpeedMetersPerSecond` and
`MaxAbsModuleTargetSpeedMetersPerSecond` fields distinguish commanded correction from a module that is
still coasting. `Drive/WheelsStopped` becomes true only when every measured module speed is at or below
0.02 m/s. This is diagnostic telemetry only; it does not alter drivetrain control.

The 2026-08-24 `kP=0.20` braking comparison increased overshoot, command time, and velocity reversals,
so drive velocity `kP` is restored to 0.10 V/rps. The remaining gains stay `kI=0`, `kD=0`, `kS=0`,
`kV=0.124`, `kA=0`. The shortened 0.05 s post-qualification confirmation remains for a separate
one-variable validation.

## Selectable terminal yaw precision (2026-08-24)

`DriveToPosePrecisionCommand` has two explicit terminal-heading modes. `PRECISE` is the default and
requires 1.5 degrees; `RELAXED` requires 1.8 degrees. The current-position `Forward 1m` and `Forward
2m` chooser options use `RELAXED`, because their primary measurement is straight-line distance.
Tag-board commands and the final precision portion of sequential/spatial-handoff autos remain
`PRECISE`, because their terminal orientation matters.

For multipart command construction, pass `YawPrecision.RELAXED` only to an intermediate
`DriveToPosePrecisionCommand` whose heading is not an end requirement. Return to `PRECISE` for the
final placement or aiming segment. The selected value is recorded as
`DriveToPose/Controller/YawPrecisionMode`; the numeric gate is
`DriveToPose/Controller/ConfiguredRotationToleranceDegrees`.

## Camera jitter calibration controls (2026-08-31)

SmartDashboard now exposes:

- `Start Camera Jitter Capture (Disabled Only)`
- `Stop Camera Jitter Capture (Disabled Only)`

Square the robot to the surveyed board, disable it, keep both tags visible in both cameras, and press
Start once. Do not touch the robot or board. Each camera freezes after 100 accepted MultiTag robot-pose
samples. `Vision/Camera0` is physical front-left; `Vision/Camera1` is physical front-right. Watch
`Vision/Camera*/Jitter/SampleCount` and wait for both `Ready` values plus
`Vision/JitterCapture/ComparisonReady` to become true. Starting while enabled is rejected; enabling the
robot during a capture stops it so samples from different physical positions cannot mix.

The new layout is
`C:\MechaRAMS\Temp\AdvantageScope 8-31-2026 - Camera Jitter Calibration.json`. It includes per-camera
mean pose, X/Y/combined translation and yaw standard deviation, peak-to-peak ranges, the signed
Camera0-minus-Camera1 mean X/Y/translation/yaw differences, and the configured XY/angular trust
controls.

Current measured validity configuration (2026-08-31): Camera0/front-left XY factor `2.15`, angular
factor `2.10`, rotation enabled; Camera1/front-right XY/angular factors `1.0`, rotation disabled.
Front-right observations are still accepted for XY. The disabled rotation applies only to its theta
component.

After the `c75a` moving validation, camera theta is fused only while disabled. During enabled teleop or
auto, both cameras continue contributing XY but gyro heading is authoritative. Camera0 remains eligible
to update `Seed Pose From Vision` / the driver's left-stick manual seed while disabled; Camera1 does
not. Verify the runtime mode at `Vision/Modes/FuseRotationWhileEnabled`.

## Relative-forward heading and camera backlog behavior (2026-08-31)

The `Forward 1m - ...` and `Forward 2m - ...` entries are ruler calibration autos. Place the chassis
physically square to the tag-board plane before running them. At auto start they now preserve the
current fused X/Y but reset the estimator heading to `0 degrees`, then construct the +X target from
that normalized pose. This prevents a camera-seed yaw bias from commanding an artificial turn. The
pre-normalization and normalized poses are logged at `DriveToPose/Calibration/MeasuredStartPose` and
`NormalizedStartPose`; `HeadingNormalized` must be true. This behavior is specific to the relative
forward calibration options, not general PathPlanner or tag-board autos.

Every unread PhotonVision NetworkTables result is drained each loop. To prevent a burst of old poses
from blocking one 20 ms scheduler cycle—or starving the estimator behind a persistent queue—each camera
fuses only the newest solvable pose in that loop's burst. Older poses from the same burst are counted,
not silently hidden. In file replay, watch input fields `/Vision/Camera*/UnreadResultCount` and
`/Vision/Camera*/SupersededPoseObservationCount`. A superseded count above zero identifies a camera or
CPU burst; there must never be a cross-loop pose backlog. Timing outputs are
`/RealOutputs/Vision/Timing/Camera*IoUpdateMs`, `Camera*FusionMs`, and `PeriodicMs`.

Use the separate layout
`C:\MechaRAMS\Temp\AdvantageScope 8-31-2026 - Vision Backlog Timing.json`. It includes saved-log and
live tables plus a saved-log line graph; the existing camera-jitter layout is unchanged.

## Precision profile clock synchronization (2026-08-31)

The precision command still runs from the normal robot scheduler; it does not create a second motor
control thread. However, each `ProfiledPIDController` now advances by the number of nominal 20 ms
profile steps represented by actual elapsed scheduler time, capped at five steps (100 ms) in one
execute. This prevents a slow/irregular robot loop from leaving the motion profile behind the physical
robot and then commanding a reverse/forward correction near the target.

The next one-run validation uses
`C:\MechaRAMS\Temp\AdvantageScope 8-31-2026 - Profile Clock Sync.json`. Confirm
`DriveToPose/Controller/ConfiguredProfilePeriodSeconds = 0.020`; graph
`LoopDtMilliseconds`, `ProfileStepsThisExecute`, raw profile velocity, feedback velocity, requested vx,
measured vx, and translation error. Gains, feedforward fade, damping, and tolerances are unchanged.

## Gyro-rate precision validation (2026-09-02)

The precision controller now uses the Pigeon's mount-corrected Z-world angular velocity for theta
damping and the angular velocity finish gate. Module-kinematic omega remains logged, but the `4844`
2 m run showed it integrating in the opposite net direction from the gyro-owned pose. Translation
vx/vy still come from CTRE's module-derived chassis speeds. The Pigeon status signal is explicitly
requested at 100 Hz; an unhealthy signal falls back to the prior kinematic rate.

After manual deployment, load
`C:\MechaRAMS\Temp\AdvantageScope 9-2-2026 - Gyro Rate Validation.json`. Before enabling, confirm
`Drive/GyroYawRateSignalOK=true` and `Drive/GyroYawRateAppliedUpdateFrequencyHz` is approximately
`100`. Run `Forward 2m - PnP + Iso` once with both cameras open. Record left/right endpoint distance,
visible settling, and the log suffix. Do not change yaw tolerance, gains, damping, speed, acceleration,
or vision modes for this A/B run. Stop if heading visibly diverges or the Pigeon signal is unhealthy.

The ordinary relative-distance tests still use `RELAXED` at 1.8 degrees. Commands that require final
tag-board/placement alignment still use the separate `PRECISE` 1.5-degree mode.
