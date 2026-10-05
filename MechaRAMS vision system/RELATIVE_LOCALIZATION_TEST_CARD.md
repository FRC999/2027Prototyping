# Relative localization check: record start, move, measure

October 3, 2026. Reusable classroom experiment; its first sequence is now complete
(results below). Absolute camera-mount calibration is deferred until the new
chassis and surveyed field are ready.

The new code adds a disabled-only measurement notebook on SmartDashboard. It
records a reference and numbered stationary points. It does NOT drive the robot,
reset localization, seed heading, change camera calibration, or retune trajectories.
Robot build/deployment is done by the team. The October 3 captures verify that the
diagnostic was deployed and used; no robot build was run by the coding agent.

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

## If X agrees but Y does not: diagnostic order

Today's data do not prove a particular wrong mounting angle. Yaw is a credible
candidate: if our idea of the starting forward direction is wrong, real forward
travel appears partly sideways. For example, a 3-degree axis error over 1 meter
produces about 5.2 cm of apparent sideways movement while changing forward distance
by only 1.4 mm. This is an illustration, not a proposed 3-degree mount correction.
The relative capture uses each source's estimated start heading, so this effect
matters even though the robot's physical starting marks were recorded correctly.

Pitch/roll, optical calibration, tag-layout geometry, and survey error are also
possible contributors. A fixed error in the camera's XYZ mounting translation
mostly cancels when comparing two poses with the same orientation; it becomes
more visible when turning. Do not simply subtract a constant Y value from poses.

Two checks from the existing data argue against declaring a yaw-only solution:

- Rotating the reported straight displacement direction to match the tape would
  take about 3.6/3.5 degrees for left/right at 1 meter, but 4.6/5.5 degrees at the
  half-meter point. These are direction-only diagnostic calculations, NOT fitted
  mount angles. Rough tape readings and additional geometry effects limit them.
- Raw PhotonVision camera height rises with the forward move: left about 0.332 ->
  0.351 -> 0.371 m; right about 0.409 -> 0.420 -> 0.434 m (start, halfway, 1 m).
  Assuming level chassis/floor and rigid mounts, camera height should stay fixed.
  These are field-to-camera heights, not the derived robot-center heights. Changing
  our robot-to-camera yaw cannot fix a bias already present in PV's raw camera pose.

At the next session, work on localization before further trajectory PID tuning:

1. Verify the reference line and perpendicular floor measuring axes, actual frame
   width, tag center spacing/heights, flat board and its orientation versus the
   uploaded layout. Preserve today's data as the before-correction baseline.
2. Resolve optical calibration before fitting mounting angles. The left camera's
   focus was changed without a new ChArUco calibration. Recalibrate it at the actual
   pipeline resolution before treating a mount fit as calibration-grade. Do not
   change both cameras' mounting constants at the same time to force agreement.
3. Repeat only a short static start / roughly half-meter / roughly one-meter / return
   sequence, with independently checked heading and a level chassis, measuring
   actual rear-corner displacements. The existing capture tool/log fields suffice.
   This distinguishes a heading-axis error from distance-dependent camera-pose bias.
4. If a consistent mounting-angle error remains, use the independently surveyed
   multi-pose procedure in the mounting manual, reserving at least one position
   for checking rather than fitting. The full absolute survey can still wait for
   the new chassis. Verify roll; do not force it to zero to disguise another error.

No additional cameras or faster control loop are needed to investigate this bias.
Camera-only stationary results already contain it, so a trajectory PID change
cannot solve its cause. Both cameras agreeing is useful but is not independent
proof of accuracy when they share the same tag board and layout.

PhotonVision references: [MultiTag and field-layout requirements](https://docs.photonvision.org/en/latest/docs/apriltag-pipelines/multitag.html),
[recalibrate after refocusing](https://docs.photonvision.org/en/latest/docs/quick-start/camera-focusing.html),
[camera/resolution-specific intrinsic calibration](https://docs.photonvision.org/en/latest/docs/calibration/calibration.html).

## First completed sequence: what we learned

### October 5 repeat: first endpoint (29d0)

New reference ID 95814397 was recorded in fb56; that log contained the move but
no endpoint snapshot. Point 1 was successfully captured in 29d0 at 697.316 s with
100 fresh samples per camera, disabled and stationary, no normal-gate rejections.
Laser-axis use was planned but not yet explicitly confirmed by the mentor.

All values below are rear-FRAME corner displacements in meters, using LEFT Y:

| Source | Rear-left X | Rear-right X | Rear-left Y |
|---|---:|---:|---:|
| Physical measurement | 0.560 | 0.540 | -0.010 |
| Left camera | 0.5852 | 0.5677 | -0.0430 |
| Right camera | 0.5660 | 0.5474 | -0.0192 |
| Fused localization | 0.5696 | 0.5518 | -0.0374 |

Using the recorded frame dimensions, physical center displacement is
(0.54984, -0.01984) m, clockwise turn 1.888 degrees. Camera turns are clockwise
1.653/1.761 degrees, fused 1.678 degrees. Right-camera corner disagreement is
under 1 cm; left-camera disagreement is about 2.5-3.3 cm. Do not infer one universal
Y correction or mounting-angle change from this point. Next capture is roughly
1 m TOTAL from the original marks, same reference/settings, not 1 m additional.

### October 5 repeat: second endpoint (6541)

Same reference 95814397, point 2 completed at 1104.760 s with 100 fresh samples
per camera, both connected, disabled/stationary, no normal-gate rejections.

| Source | Rear-left X (m) | Rear-right X (m) | Rear-left Y (m) |
|---|---:|---:|---:|
| Physical measurement | 1.060 | 1.040 | -0.023 |
| Left camera | 1.0777 | 1.0577 | -0.0788 |
| Right camera | 1.0441 | 1.0228 | -0.0426 |
| Fused localization | 1.0520 | 1.0320 | -0.0758 |

Physical center displacement is (1.04984, -0.03284) m with clockwise turn 1.888
degrees. Left/right/fused relative turns are clockwise 1.887/2.004/1.891 degrees.
Good agreement on the CHANGE in heading does not prove correct initial heading.
Rear-left Y discrepancies grow from 3.30/0.92/2.74 cm at point 1 to 5.58/1.96/5.28
cm at point 2 (left/right/fused, all farther right than tape). Right camera is
still closer in Y; no universal mount correction is justified by these two points.
Next is return near original marks, capture and measure actual residual offsets
without re-recording the start. Confirm laser-axis use before calling these
laser-verified measurements. No controller or calibration changes made.

### October 5 repeat: return (e102 / f917)

The capture straddled log rotation. e102 ended with capture active; f917 contains
point 3 completion at 1463.556 s, 100 fresh samples per camera and the SAME reference
95814397. Do not mistake retained one-meter results before completion for a return.
The mentor reports approximately zero on all three rear-frame measurements.

| Source | Rear-left X (m) | Rear-right X (m) | Rear-left Y (m) |
|---|---:|---:|---:|
| Physical measurement | approximately 0 | approximately 0 | approximately 0 |
| Left camera | +0.0281 | +0.0229 | +0.0282 |
| Right camera | +0.0069 | +0.0051 | +0.0047 |
| Fused localization | +0.0139 | +0.0082 | +0.0115 |

Robot-center return distances from the recorded reference: left 3.61 cm, right
0.71 cm, fused 1.41 cm. This completes the start / half-meter / one-meter / return
block. The right camera agrees better with tape throughout this sequence, but
good return closure is not proof of accuracy at other positions. The left result
does not return exactly to its original estimate; approximate physical return,
viewpoint changes and temporal variation cannot be fully separated here.

Subsequent mentor clarification: these readings were NOT taken against fixed laser
reference axes. The numerical comparisons above remain sensor-minus-tape results,
not proven camera errors. Lasers are not mandatory: a verified straight reference
and perpendicular measuring direction can also be established with string/marks
and a square. The actual method used for these readings still needs clarification.

Before extending to 2 m or changing calibration, verify the original-axis measurement
geometry. If it was not established accurately, repeat the short static sequence
with fixed verified axes and unchanged camera settings/mounts. Record a NEW start
if establishing a new physical reference, then retain it across all points.
Do not simultaneously change optics and measurement method: preserve a controlled
before/after comparison. The refocused left camera still needs ChArUco calibration
at the pipeline resolution before final accuracy certification; a NEW reference
is also required after that later calibration. No extra cameras or trajectory PID
changes needed for this check. Absolute mount survey remains the separate later task.

### October 5 follow-up: recalibration deferred today

The mentor clarified that Y was measured perpendicularly with a ruler. Do not
reject this method merely because it used no laser. Camera-minus-ruler comparisons
remain useful sanity checks, without certifying either source as exact truth.

The left camera will not be recalibrated today. Existing f917 data already provide
a stationary comparison: mean field Y between 1470-1500 s and 1950-1980 s changed
by -1.81 cm for the left camera and +0.66 cm for the right. These windows contain
1044/1110 left and 940/976 right fresh MultiTag frames. Robot was disabled and wheel
speed essentially zero. This suggests temporal variation if the robot/board were
not disturbed; it does not identify its physical cause. No extra long stationary
test is required just to obtain that evidence.

With the return check now complete, an optional approximately 2 m total-X point
is useful if the route is clear and both tags remain visible. Keep both cameras
open and all settings/constants unchanged. Capture a fresh point at the returned
start first, then drive manually, disable, capture at approximately 2 m and measure
rear-left X, rear-right X and rear-left Y from the original marks. Wait for
MeasurementReady=true and CaptureActive=false BEFORE closing/downloading the log.
Keep the original reference; both captures can share one log with distinct point
indices. If rebooted or the physical reference changed, establish a new reference
instead. This tests distance-dependent discrepancy; it does not uniquely determine
mounting yaw/pitch. Calibration-grade intrinsic and mount verification remain later.

### October 5 two-meter point (612b)

Point 4 at the returned start completed 3508.129 s; point 5 at the longer-distance
endpoint completed 3722.046 s. Both have 100 fresh frames per camera and the same
reference 95814397. Capture health checks passed; no geometry-gate rejects.

Mentor gives offsets from the 2 m line: left +0.022 m, right -0.040 m. The table
converts these to original-start displacements. LEFT Y was VERY approximate zero.

| Source | Rear-left X (m) | Rear-right X (m) | Rear-left Y (m) |
|---|---:|---:|---:|
| Physical measurement | 2.022 | 1.960 | very approximately 0 |
| Left camera | 2.0148 | 1.9604 | -0.1334 |
| Right camera | 2.0009 | 1.9481 | -0.0914 |
| Fused localization | 2.0025 | 1.9481 | -0.1439 |

X agreement is within about 2.1 cm. Tape-X difference implies clockwise 5.86
degrees using recorded frame width; cameras report 5.14/4.98 degrees. The table
already accounts for corner motion caused by turning. Its Y predictions must NOT
be labeled proven 9-14 cm camera errors because the physical Y reading is imprecise.
The fresh near-start snapshot remains close to zero; recomputing relative motion
from it also leaves a large predicted sideways component.

Next useful action is a careful ruler remeasurement of rear-LEFT Y while the
robot remains at the same endpoint, disabled. No repeated drive/capture is needed
if it has not moved. If it moved, capture and measure a new pose together. Do not
change camera mounting, calibration, fusion or PID to fit the approximate zero.

### October 3 sequence

All points shared reference ID 281041948. The mentor measured exposed rear-left X,
rear-right X and rear-LEFT Y, from original corner marks along original robot-aligned
axes. Left Y was converted correctly offline; it was not entered as dashboard right Y.
Each successful capture contains 100 samples per selected camera. Physical readings
are rough sanity checks, not a surveyed calibration fixture.

The table below compares the COMBINED robot-center estimate with the center derived
from the ruler readings, not raw corner displacements. Errors are estimate minus
manual; XY units are centimeters, yaw degrees. Geometry uses recorded frame width
0.6072 m and center-to-rear 0.3036 m. Verify these physical dimensions before fitting
angles or calibration parameters on either chassis.

| Log / point | Test | Center X error (cm) | Center Y error (cm) | Yaw error (deg) |
|---|---|---:|---:|---:|
| fae0 / 1 | About 1 m forward | -0.89 | -8.16 | -0.47 |
| cb3f / 3 | Return to original marks | -1.04 | +0.11 | +0.13 |
| 23b1 / 4 | About 0.52 m forward | -0.38 | -5.80 | -0.26 |
| ff0c / 6 | About 12 degrees clockwise, at halfway location | +0.22 | -6.70 | +1.17 |
| ae96 / 7 | About 15 degrees counterclockwise, at halfway location | -0.88 | -5.65 | -1.28 |

Additional evidence:

- cb3f point 2 repeated the one-meter endpoint without moving: camera mean XY
  changed only about 0.8/1.2 mm. Repeatability is not the same as physical accuracy.
- cb3f return mean center errors were 1.64/1.43 cm for the two cameras and 1.05 cm
  combined. A good return alone does not certify positions away from the start.
- 35a9 point 5 (~17 degrees clockwise) timed out with left 0/right 100 samples; left
  camera produced only single-tag observations. The subsequent smaller turn worked.
  Check coverage and readiness before accepting a saved pose; do not lower gates.
- At the halfway position, right-camera center Y discrepancies remained about
  -4.9 cm at straight, clockwise and counterclockwise headings. Left-camera errors
  varied more with heading. The one-meter lateral discrepancy was larger.

We have demonstrated how to collect and distinguish forward scale, static
repeatability, return consistency, rotation agreement, viewpoint-dependent
discrepancies and tag-visibility failures. We have NOT independently certified
full XY accuracy, determined absolute mount offsets, or tested trajectory timing
with these stationary captures. Do not tune PID to compensate for this result or
fit a universal Y/yaw offset from these few rough measurements.

There is no need to repeat this entire block merely to perfect this prototype.
Carry this card to the new chassis, use its measured frame geometry, perform the
separate intrinsic/mount/field checks, then repeat these checks at unused poses.
The next distinct commissioning layer is wheel-only/independent gyro measurement;
see the [generic next-tests plan](GENERIC_COMMISSIONING_NEXT_TESTS_2026-10-03.md).
