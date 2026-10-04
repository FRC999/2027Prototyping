# Camera mounting calibration: X, Y, Z, roll, pitch and yaw

October 3, 2026. For the build/programming team. This procedure works on the
prototype and on a different chassis. It estimates the pose of each camera
relative to the robot; it is not another trajectory PID tuning exercise.

**Deferred by the mentor until the new chassis/surveyed setup is ready.** For the
current prototype session use [relative start/point measurements](RELATIVE_LOCALIZATION_TEST_CARD.md).
Those require no known absolute starting X/Y and do not certify absolute mounts.

## What is already available?

The [commissioning playbook](COMPETITION_CHASSIS_CALIBRATION_PLAYBOOK.md) provided
the overall order. This manual adds the detailed mounting procedure and an
[offline calculator](tools/calculate_camera_mount.py). The calculator only reads
measurements and prints candidate values. It does not connect to the robot,
change constants, or calibrate the camera lens.

The existing SmartDashboard **Start Camera Jitter Capture (Disabled Only)**
records 100 accepted MultiTag ROBOT poses per camera. Its displayed statistics
are X/Y/yaw only. Its automatic completion currently waits for cameras 0 and 1.
It is useful for repeatability, but is NOT a six-axis mounting-calibration button.
The NEW **Localization Check** notebook records selected-camera 3D sample sets and
active mount transforms. After the team deploys it, its WPILOG is an alternative
to collecting PV table readings below. It does not solve absolute mounts without
independently surveyed robot poses. Raw field-to-camera samples are reconstructed
by undoing the configured mount on the IO candidate, not from the fused estimate.

## Two different calibrations

| Calibration | What it describes | When to revisit it |
|---|---|---|
| Lens/image calibration, using ChArUco | How image pixels correspond to viewing directions | New physical camera, changed lens/focus or processing image mode |
| Mounting calibration, this manual | Where the optical center is and which way the camera points on this robot | Camera remounted, bracket bent/changed, robot reference changed |

Moving an unchanged camera to another chassis does not itself change its lens
calibration. Preserve that camera's own calibration and verify it at the processing
resolution. Never substitute another camera's calibration because the model matches.
PhotonVision documents [camera/resolution-specific calibration](https://docs.photonvision.org/en/latest/docs/calibration/calibration.html)
and requires [recalibration after refocusing](https://docs.photonvision.org/en/latest/docs/quick-start/camera-focusing.html).
Our left camera was refocused without recalibration: it can demonstrate this
workflow, but final mounting accuracy remains provisional until that is corrected.

## 1. Prepare the reference geometry

Keep the robot disabled whenever recording stationary measurements. Reposition
only with a driver supervising, a clear swept area and appropriate low-speed
manual control. Do not run the existing autos from turned starting poses: they
currently normalize heading to zero.

You need a rigid tag board, tape/ruler, square, plumb line or laser level, and a
level/inclinometer. A laser helps establish floor lines; do not point it into eyes
or camera sensors. A housing-mounted laser is not automatically aligned with the
camera's optical axis.

1. Choose the **same robot origin used by drivetrain geometry**. For our prototype
   it is the chassis center projected onto the floor. Mark its location relative
   to the frame; do not assume the new chassis has the same dimensions or symmetry.
2. Use robot axes: X forward, Y left, Z up. Positive yaw turns left/counterclockwise
   viewed from above. A forward camera tilted UP has negative pitch. An inward
   front-left camera has negative yaw; an inward front-right has positive yaw.
   These are [WPILib conventions](https://docs.wpilib.org/en/stable/docs/software/basic-programming/coordinate-system.html).
3. Verify the board is vertical, the tags are flat and upright, and their centers,
   separation, printed size and orientation match the layout on EVERY Pi and in
   robot code. Our practice layout is tag 1=(6,2.25,1.5), tag 2=(6,1.75,1.5) meters;
   both face -X with yaw 180 degrees. These are practice-field values, not defaults
   for another field. Tag height means tag CENTER height above the floor.
4. Check the chassis reference plane is level front-to-back and side-to-side under
   normal load. If it is not, measure its actual orientation or use a suitable level
   location. Do not silently treat chassis tilt as camera mounting tilt.
5. Label each camera and confirm its PV name by covering that lens while disabled.
   Uncover it before collecting data. Record focus, resolution, camera name, Pi,
   layout, code version and date; leave all of these unchanged during the experiment.

For ordinary trajectory runs your approximate rear-corner measurements remain
sanity checks. This particular calibration needs a few more carefully measured
reference poses: the camera cannot prove its own accuracy against itself.

## 2. Measure camera X/Y/Z mechanically

Measure straight along the robot axes, not along the slanted bracket or the
direction the lens points. The reference is the camera's optical center, not a
mount screw or the front edge of the plastic case. Use manufacturer geometry if
available; otherwise record the uncertainty in locating that center.

- X: optical center forward/back from robot origin; forward is positive.
- Y: optical center left/right from robot origin; left is positive.
- Z: optical center above robot origin; with our floor origin this is height above floor.

Use a plumb line to project the optical center onto the floor. Measure its forward
and sideways separation from the origin mark, then its height. Record all three
and the realistic measurement uncertainty for each camera. Photograph the reference
points. Accurate measured distances remain the default translations; a noisy pose
solve must not silently replace them.

Current prototype constants are reference only:

| Camera | X m | Y m | Z m | Roll deg | Pitch deg | Yaw deg |
|---|---:|---:|---:|---:|---:|---:|
| front-left | 0.152 | +0.266 | 0.420 | 0, assumed | -18.88 | -14.80 |
| front-right | 0.152 | -0.266 | 0.435 | 0, assumed | -17.10 | +15.74 |

These angles are historical prototype settings, not newly certified measurements.
Both zero rolls were imposed in code; they have not been independently demonstrated.

## 3. Establish known robot poses without using vision as the reference

Start with the robot square to the board, facing +X. Measure board distance from
two corresponding left/right frame corners: equal distances establish square
alignment within the measurement uncertainty. Also locate the chassis center
sideways relative to the midpoint between tag centers. Equal board distances
alone do NOT tell us the robot's Y coordinate.

For a level, square robot and board at field X=6:

- If measuring from the FRONT frame edge, robot-center X = 6 - gap - origin-to-front-edge.
- If measuring from the REAR frame edge, robot-center X = 6 - gap + rear-edge-to-origin.
- Robot-center Y = 2.0 + measured center offset to the LEFT of the tag midpoint.
- Robot pose Z=0, roll=0, pitch=0, yaw=0 only when the stated floor-origin,
  level and square conditions have actually been established.

Do not mix the front and rear gap formulas. Our normal run reports use REAR frame
corners; keep that convention on the measurement sheet. At turned poses, survey
the center and heading using floor lines or measured corner positions and the
known frame geometry. Do not reuse the square-robot gap formula after turning.
If full turned-pose position is unknown, the angle observations are useful evidence
but cannot produce a validated six-axis transform through the calculator.

Use four distinct poses, plus an optional repeat:

| Pose | Setup | Use |
|---|---|---|
| A | Square, both tags clear, moderate distance | Fit |
| B | Square, about 0.75-1 m farther/nearer along the clear X corridor | Fit |
| C | Independently measured small heading change, about 10-15 degrees if both tags remain visible | Fit |
| D | Different distance/lateral location or opposite small heading; independently surveyed | Validation, NOT fit |
| A-repeat | Return to measured A | Check repeatability |

The exact distances/headings are not magic numbers. Use positions with good tag
images and enough room for all frame corners. Our left-side area only opens after
the forward entry; do not strafe at the normal start or assume space on the right.
No existing autonomous route is required for this stationary experiment.

## 4. Capture the correct PhotonVision numbers at each pose

1. Keep the robot disabled and stationary. Verify live camera connection, AprilTag
   3D processing, correct layout, and **Do Multi-Target Estimation** enabled.
2. Select the physically identified camera. Find the row labeled
   **Multi-tag pose, field-to-camera**. Its Tags list must show the intended two
   tags. Two detections in the individual target table do not alone prove a
   combined MultiTag solution. This output is documented by
   [PhotonVision](https://docs.photonvision.org/en/latest/docs/apriltag-pipelines/multitag.html).
3. Record the six columns **X meters, Y meters, Z meters, X Angle, Y Angle, Z Angle**.
   The angles are roll, pitch and yaw respectively. Record the camera name and
   pose A/B/C/D along with them. Do not use the individual-tag X/Y/Z-angle rows.
4. Capture at least five live readings spread over about 5-10 seconds per camera
   per position. A readable screen recording is fine; extract frames later. Verify
   the stream is live and not showing a frozen disconnected image. These are
   repeated readings, not five independently surveyed experiments.
5. If using **Reset Samples** in PV, its lower table describes variability over
   the sample window. It is NOT an averaged camera position: the upper six-number
   pose row is the live pose, and the lower six numbers are standard deviations.
6. Optionally also make the existing 100-sample SmartDashboard capture at each
   position for robot-pose repeatability. This does not replace the six-number
   PV capture needed for pitch/roll. Let Ready finish; do not leave capture on
   while moving. Save the WPILOG and identify the stationary intervals.

For an added rear-facing camera, turn the stationary robot to face that camera
toward the board and survey that robot pose. Do not alter the field layout to
pretend the board moved. The calculator handles arbitrary orientations.

## 5. Calculate the mount and compare with the ruler

The idea is simple: PhotonVision tells us where the CAMERA is in the field.
We independently measured where the ROBOT is. Removing the robot's pose leaves
the camera's mounting pose on the robot.

When the robot is truly level and facing field +X, subtract robot XYZ from camera
XYZ; PV's three angles then directly estimate the mounting angles. When the robot
is turned or tilted, ordinary XYZ subtraction is wrong: use a rigid 3D transform.
The calculator uses robot-to-camera = inverse(field-to-robot) * field-to-camera,
equivalent to [WPILib Transform3d between two Pose3d values](https://github.wpilib.org/allwpilib/docs/release/java/edu/wpi/first/math/geometry/Transform3d.html).

1. Copy [the blank measurement template](tools/camera_mount_measurements.template.json)
   to a NEW file for each camera. Keep the template unchanged.
2. Fill measured_robot_to_camera_xyz_m with the independently measured translation.
3. Fill field_to_robot at each position with the surveyed ROBOT pose. Fill
   field_to_camera_samples with the PV readings. Every six-number array is
   **X, Y, Z in meters, then roll, pitch, yaw in degrees**. Add one array per reading.
   Missing values stay unknown; never replace unknowns with guessed zeros.
4. Keep A/B/C as role="fit" and D/A-repeat as role="check". Check positions must
   not be used to select angles and then presented as independent validation.
5. Give the filled files to the analysis assistant, or run this offline command
   with your actual input filename:

```powershell
python "C:\MechaRAMS\2027Prototyping\MechaRAMS vision system\tools\calculate_camera_mount.py" "C:\MechaRAMS\temp\front-left-mount-measurements.json"
```

The calculator reports each position's inferred six-axis mount, a combined
estimate with equal weight per fit position, and the difference between inferred
XYZ and mechanically measured XYZ. Its candidate preserves measured XYZ and uses
the inferred orientation. It then computes robot-pose errors on every position,
including the unused validation positions. It also compares forcing roll to zero.
It is a rigid-transform averaging/check tool, not a full image-based bundle
adjustment or a statistical certification tool. All outputs remain candidates.

**If/then:**

- If the angles/XYZ change appreciably with distance, do not average the problem
  away: check intrinsics, focus, tag geometry, image quality and survey errors.
- If a single pose is inconsistent, repeat it and inspect the data; do not silently
  discard it only because removing it makes the answer look better.
- If inferred XYZ consistently disagrees with measured XYZ, resolve optical-center,
  robot-origin, board and lens-calibration errors before changing measured distances.
- If independent check positions improve and agree with measurement uncertainty,
  approve a single mount transform for that camera. Re-test after applying it.

## 6. Verify roll instead of assuming it

With a verified level, +X-facing robot, **PV's MultiTag X Angle is an estimate of
camera roll relative to the robot**. A nonzero result can also come from board
alignment, intrinsic error, chassis tilt or solve bias. A tilted tag edge in the
image is NOT by itself a roll measurement: pitch/yaw and perspective affect it.

1. Check the chassis plane and tag board with the level. A level on an arbitrary
   camera-case surface is only a mechanical cross-check unless that surface's
   relationship to the optical/sensor axes is known.
2. Calculate roll independently at A, B and C; check D. The full transform removes
   measured robot tilt/heading. Do not just subtract Euler angles on a tilted robot.
3. Compare the fitted-roll and zero-roll results at the unused check positions.
4. If roll is consistently nonzero beyond the uncertainty of the setup and improves
   validation, enter that nonzero roll in code. The software supports it.
5. If the result is near zero or unresolved within measurement uncertainty, document
   "consistent with zero within [measured uncertainty]"; do not claim exactly zero.

There is no universal roll-pass number. At a 3 m viewing distance a 0.2-degree
pointing error corresponds to about 1 cm of transverse ray error; that is a scale
illustration, not a guaranteed robot-pose error bound or a threshold for every axis.
Choose a measurement/error budget appropriate to the desired few-centimeter pose
accuracy. The current 1.5-degree trajectory yaw tolerance is NOT a suitable camera
mounting-calibration tolerance.

## 7. Apply approved values and verify

The current code location is
`VisionTestingAndCalibration/src/main/java/frc/robot/Constants.java`,
`VisionConstants.ROBOT_TO_*_CAMERA`. `Translation3d` uses meters and `Rotation3d`
uses **roll, pitch, yaw in radians**. Convert the calculated degrees once.

Robot code converts PV field-to-camera to field-to-robot with the inverse mount.
Do not apply the same mount transform a second time in PhotonVision. Recheck the
independent validation poses with the deployed constants before tuning weights.
Keep the old calibration, input data, candidate, approved values and deployed
commit together. No new physical measurements were invented or applied by writing
this manual.

## 8. Adding a third/fourth camera or another Pi

The core `Vision` fusion loop supports multiple cameras. The active roster in
`RobotContainer.createVision` is currently two; adding a camera needs configuration
and verification, not a new trajectory algorithm.

1. Give every camera a globally unique PV name and identify its physical lens.
   Give each Pi a unique network identity; point all Pis at the same robot NT
   server. Keep compatible PV/PhotonLib versions and the same surveyed layout.
2. Calibrate the new camera's lens, then its mount using this procedure. Existing
   untouched cameras do not need ChArUco redone simply because a camera was added.
3. Register its real and simulation IO, transform, matching-index XY/angular
   factors, and heading eligibility. Do NOT enable the existing placeholder rear
   transforms as if they were measured values. The currently listed rear heading
   flags are also not evidence of validation.
4. Before full integration, compare its estimates with known stationary poses and
   the existing cameras. The new Localization Check tool waits for each selected
   camera and saves 3D samples. The legacy jitter tool still finishes on 0/1 and
   cannot fully qualify rear-camera views by itself.
5. Verify fresh timestamps, connection/recovery, per-camera acceptance and runtime
   cost. Recheck combined localization weights/heading policy; additional views
   are not automatically independent evidence if they share tag/layout errors.
6. Re-run representative no-yaw, moving-yaw and return routes. Do not retune motor
   PID solely because another camera was added.

Three cameras on one Pi may work, but measure it at the actual processing settings
with all cameras running and tags visible: per-camera FPS/latency, USB stability,
processor temperature/load, network and roboRIO loop timing. Do not treat the
existing source comment claiming a fixed two-camera budget as a universal limit.
PhotonVision has no fixed camera-count limit; CPU and USB throughput commonly
limit it, and streams are asynchronous. See
[official multiple-camera guidance](https://docs.photonvision.org/en/latest/docs/hardware/selecting-hardware.html#using-multiple-cameras).
For four cameras, two Pis with two cameras each is a reasonable starting design,
not a performance guarantee. No additional cameras are required for the stationary
mounting experiment described here.

## Measurement handoff checklist

Send: per-camera filled template or clearly labeled PV captures; robot-origin and
camera XYZ measurements with uncertainty; surveyed A/B/C/D robot poses; board
layout and physical check; focus/calibration/resolution details; and associated
WPILOG names. Include roll/pitch measurements of the robot reference plane or
evidence it was level. State which poses are independent validation.

Success is one stable transform per camera that works across views, plus a
repeatable procedure another student can execute. It is not one attractive
stationary reading, low jitter alone, or perfect agreement between two cameras.
