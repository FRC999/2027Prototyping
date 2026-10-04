# Relative localization check: record start, move, measure

October 3, 2026. This is the NEXT classroom experiment. Absolute camera-mount
calibration is deferred until the new chassis and surveyed field are ready.

The new code adds a disabled-only measurement notebook on SmartDashboard. It
records a reference and numbered stationary points. It does NOT drive the robot,
reset localization, seed heading, change camera calibration, or retune trajectories.
Robot build/deployment must be done by the team; this change was not compiled here.

## What we measure

All distances are **signed displacements from the ORIGINAL rear FRAME corner
marks**, with the rear bumper removed. X is forward along the robot's initial
direction, Y is left perpendicular to that direction. These floor axes stay fixed
after the robot turns. Measure projected X/Y distances, not diagonal tape length
between an old and new corner.

The three manual inputs are:

1. Rear-left frame corner's X displacement from its own original mark.
2. Rear-right frame corner's X displacement from its own original mark.
3. Rear-right frame corner's Y displacement from its own original mark.

These determine relative robot-center X/Y and yaw for a centered rectangular frame
and a modest turn. The frame need not be square, but its width and center-to-rear
distance must be correct. The calculator assumes the total turn from the reference
is within +/-60 degrees. Do not use it for a half/full turn: three readings can
otherwise select the wrong orientation. Positive yaw means counterclockwise/left.

If you provide only the two X readings, we can infer heading and center X offline,
but cannot verify center Y. The dashboard's complete comparison needs all three.
If sending measurements afterward, a rear-LEFT Y measurement is also usable:
explicitly label the corner. Do not enter left Y into the dashboard's right-Y
field; turning makes those corner displacements different.

## One-time setup after deploying

1. Keep existing camera settings, mounts, controller gains and two-camera setup.
   Both cameras should see both tags. No new ChArUco exercise is required for this
   relative experiment; existing absolute-calibration limitations still apply.
2. Open the NEW layout, leaving your previous layout unchanged:
   `C:\MechaRAMS\temp\AdvantageScope 10-03-2026 - Relative Localization Check.json`.
   Use its **Live** tabs while connected and **Saved Log** tabs after downloading.
   The code records data; opening a layout does not turn recording on.
3. In SmartDashboard find the four buttons beginning **Localization Check -**
   and the values beneath **Localization Check/**. There is no new Xbox binding.
4. Verify `RearFrameWidthMeters` and `CenterToRearFrameMeters`. Defaults are
   0.6072 m and 0.3036 m, from the prototype module spacing plus the previously
   measured 7.5 cm wheel-center-to-frame-edge distances. Verify/change these if
   actual frame dimensions differ. They affect this calculator ONLY.
5. Leave `IncludeCamera0` and `IncludeCamera1` true. Camera 0 is front-left;
   camera 1 is front-right. Selection and frame dimensions freeze when recording
   a new start. Later cameras can be selected separately for their visible views.
6. Disabled: finalize/rotate the old log, note the new active filename, and keep
   this entire start-and-points sequence in ONE log. There is no need to rotate
   between points. Clear the known corridor and keep a driver ready to disable.

## Record the beginning once

1. Place the robot near its normal start, facing the board. An exact absolute
   field X or Y is NOT required. Choose a sensible square starting direction for
   easy floor measurements. Mark the floor axes and both rear frame corners.
2. Disable and keep the robot still, hands off.
3. Click **Localization Check - Record Start (Disabled Only)** once.
4. Wait for `ReferenceReady=true`, `CaptureActive=false`, and Status beginning
   `START_RECORDED`. Collection waits 0.5 s, then needs 100 fresh MultiTag frames
   from EACH selected camera (usually several seconds; timeout 15 s).
5. Note `ReferenceId`. `PointIndex=0` is the reference. Each estimate now reports
   zero relative displacement using its own averaged initial pose.

Do not press Seed Pose, Reset Pose, or Record Start again between measurement
points. A hard estimator reset invalidates this notebook's reference. A reboot
also requires a new reference. Ordinary teleop repositioning does not.

## At each new position

1. Use gentle manual teleop to reposition in the clear area. This tool never moves
   the robot. Do not use a yaw-normalizing H auto for a turned-start experiment.
2. Disable, release the controls, let the robot stop, and keep it still.
3. Click **Localization Check - Capture Point (Disabled Only)**.
4. Wait for `MeasurementReady=true`, `CaptureActive=false`, Status `POINT_RECORDED`.
   Note `PointIndex` (1, 2, ...). Counts and readiness belong to this new capture,
   not the separate legacy Start Camera Jitter Capture button. Do NOT run that
   legacy capture in parallel; it is unnecessary here.
5. Measure all three rear-frame displacements from the ORIGINAL marks, in meters.
   For example +0.25 means forward/left, -0.25 means backward/right. Do not measure
   from the previous point or along the robot's newly rotated axes.
6. Either send those three numbers with reference ID, point index and WPILOG name,
   OR enter them into SmartDashboard:
   - `Localization Check/RearLeftFrameX_m`
   - `Localization Check/RearRightFrameX_m`
   - `Localization Check/RearRightFrameY_m`
7. For on-dashboard comparison, confirm `TurnWithin60DegreesConfirmed=true`, then
   click **Localization Check - Save Tape Measurements (Disabled Only)**.
   Check `ManualComparisonValid=true` and Status `TAPE_SAVED_FOR_POINT_...`.
   Unknown/reset input values appear as NaN; replace ALL three before saving.
8. Compare Manual with Fused, Camera0 and Camera1 in the layout. The dashboard also
   shows those estimates as predicted rear-corner displacements, directly matching
   what your ruler measures. Error means **estimate minus manual measurement**.
   Sending measurements later is completely fine; the robot log already contains
   the captured estimates and reference poses.

A new point clears the manual entries/validity so old tape values are not silently
reused. Results are FROZEN snapshots, not live motion displays. Check readiness,
Included and PointIndex before interpreting a retained number. Cancel Capture
stops a pending sample window; enabling, detected motion or timeout also aborts it.
If a capture fails, stay still and retry; do not increase fusion gates to make it pass.

For a timeout, check each camera's sample count and PhotonVision view BEFORE
retrying. A connected camera can see one tag but produce no MultiTag samples.
In the 35a9 clockwise test (~17 degrees), the left camera produced only tag-2
single-tag poses: left samples stayed zero while right reached100. Reduce the
turn until each selected camera again reports both tags and a MultiTag solve.
An incomplete capture keeps `MeasurementReady=false`; displayed pose/corner
values remain from the last COMPLETED point and must not be used for the new one.

## First short sequence

1. Record the start at the normal position.
2. Point 1: approximately 0.75-1 m forward along the known clear corridor, same heading.
3. Point 2: return near the original marks, same heading. It need not be exact;
   measure the residual offsets. This tests closure without an autonomous controller.
4. Point 3: a small clockwise turn, approximately 10-15 degrees, at a location with
   clear swept corners and both tags visible. Translation while turning is allowed;
   the three measurements account for it. Do not assume there is room to turn at
   the original position; choose a clear location first.
5. Optional Point 4: a small counterclockwise heading, measured from the SAME start.
   A lateral point is useful later in the left-side space after the forward entry;
   do not strafe into unavailable space at the start.

Exact endpoint distances and turn angles are not targets to hit. We need the actual
measurements. Return/turn tests are optional if time, visibility or clearance is
limited. First make sure one reference and one point capture correctly.

Finally, disabled: rotate/finalize the log and download the OLD log containing the
sequence. Send the suffix and the point measurements. No extra 3 s enabled wait is
needed for stationary captures; wait for the capture-ready flags and finalization.

## What is in the log?

Saved WPILOG prefix: `/RealOutputs/Calibration/RelativePose/`.
Live NT prefix: `NT:/AdvantageKit/RealOutputs/Calibration/RelativePose/`.

- `ReferenceId`, `PointIndex`, `Status`, `ReferenceReady`, `MeasurementReady`,
  `CaptureActive`, `ManualComparisonValid`, capture start/end times and frame dimensions.
- `Fused`, `Camera0`, `Camera1`: `RelativeXMeters`, `RelativeYMeters`,
  `RelativeYawDegrees`; predicted `RearLeftFrameXMeters`, `RearRightFrameXMeters`,
  `RearRightFrameYMeters`; after manual entry, `ErrorXMeters`, `ErrorYMeters`,
  `ErrorYawDegrees`.
- `Manual`: calculated center/heading plus the recorded corner readings.
- Each selected camera: `Included`, `Samples`, `Name`, `ReferenceRobotPose3d`,
  `RobotPoseMean3d`, `RobotPoseSamples`, `FieldToCameraMean3d`, `FieldToCameraSamples`,
  `ConfiguredRobotToCamera`, first/last frame times and `NormalGateRejectedSamples`.

The raw field-to-camera samples are reconstructed by undoing the configured mount
on the IO's MultiTag robot-pose candidate. This avoids consuming PhotonVision's
unread queue a second time. Their geometry should match the original combined
solve; they do not depend on the fused drivetrain pose. Captures require finite,
fresh MultiTag candidates, but deliberately retain candidates rejected by the
normal geometry gate and count them. This is DIAGNOSTIC data, not permission to
fuse a rejected pose. Read that count before declaring the camera healthy.

The existing fusion, seed eligibility and trajectory behavior are unchanged.
Additional dashboard updates run only during a requested disabled capture (progress
10 Hz) or on button/completion events. The finite 3D sample arrays are written once
per completed capture, not during normal driving. Low wheel/gyro speeds are only
a guard: the operator must still verify the chassis is stationary.

## What this experiment does / does not establish

It compares a sensor's observed CHANGE from its own recorded start against actual
movement. Each sensor's XY offsets are expressed in its own estimated initial robot
heading frame, matching the intended initially robot-aligned tape axes. Initial
heading error can therefore appear as a cross-axis error; it is not silently
corrected using another camera. Original absolute poses are retained for analysis.

This can reveal scale, heading-change, repeatability and cross-axis discrepancies.
It cannot by itself certify absolute field location or uniquely determine every
camera-mount offset. A constant bias may cancel in a relative comparison.
Absolute XYZ/roll/pitch/yaw calibration remains a later, separately surveyed task:
[camera mounting manual](CAMERA_MOUNT_CALIBRATION_MANUAL.md).
