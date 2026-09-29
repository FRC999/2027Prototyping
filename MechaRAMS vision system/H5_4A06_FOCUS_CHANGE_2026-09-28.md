# H5 4a06 after left-camera refocusing — September 28, 2026

Source: `C:\MechaRAMS\temp\akit_rotated_1790642598986_205d4a06.wpilog`.
Mentor reports less jitter after slightly adjusting the left camera's focus.
Measuring references are confirmed to be frame corners, not outer bumper corners.
Mentor confirms no ChArUco recalibration after the slight focus change, and no
ruler measurements for this run. Visually it returned close to the start, slightly
farther back. Do not infer numerical displacement from that description.

## Decision and calibration prerequisite

One H5 OUT_AND_RETURN completed successfully, with the same logged controller
configuration as dd3c. No robot tuning changes, new layout, or robot deployment.
Treat this as a changed optical configuration, not an unchanged-camera replication.

The left camera was NOT recalibrated after focusing. Keep the improved focus;
redo its ChArUco calibration at the actual AprilTag processing resolution before
further centimeter-level accuracy/PID tuning. The previous screenshots
used 800x600, but verify the currently selected resolution. Changing only focus
does not justify editing tag coordinates or robot-to-camera mount transforms.

PhotonVision explicitly warns that refocusing makes the previous calibration
inaccurate. Calibration is camera- and resolution-specific. Sources checked today:
[Camera focusing](https://docs.photonvision.org/en/latest/docs/quick-start/camera-focusing.html)
and [Camera calibration](https://docs.photonvision.org/en/latest/docs/calibration/calibration.html).
This warning does not prove how large any error is in this run. The small adjustment
may have a small effect; the logs cannot quantify it. There is no justification for
undoing the improved focus or claiming that this run failed. Out-and-back performance
also cannot independently rule out a shared vision bias: the return target was
created from the same vision system used to estimate the endpoint.

## Timing and stopping behavior

| Metric | dd3c | 4a06 |
|---|---:|---:|
| Total command time | 6.820 s | 6.684 s |
| Final-controller interval, including travel | 0.868 s | 0.784 s |
| First tight-pose pass to completion | 0.067 s | 0.064 s |
| First stopping hold to completion | 0.047 s | 0.064 s |
| Hold releases / renewed corrections | 0 | 0 |
| Last wheel-speed sample >0.05 m/s after completion | +0.271 s | +0.081 s |

The wheel-speed comparison supports the reported reduction in visible end motion.
It is not proof that every steering motor stopped exactly at that instant. The
command's hold tail is not the same as all mechanical settling time.

Start: 2064.733518 s; final precision: 2070.633958 s; completion: 2071.417677 s.
FinishQualified=true; TimedOut=false. The normal straight-handoff finish gate was
used, not the strict rotating-approach gate. No post-completion steering-target
angle change; default command continues the precision hold with manual drive off.

Stored return target: (2.232056, 1.906253, 0°). At completion, the fused pose is
(2.239644, 1.918554, -0.489°): relative X/Y +0.759/+1.230 cm, radial error 1.445 cm.
At +1 s, yaw is -0.343°. Maximum post-completion yaw change is 0.171°.
These are estimates, not ruler-verified accuracy. Do not reuse dd3c's ruler values
as measurements for 4a06.

The robot was disabled 1.451 s after completion. Use >=3 s supervised enabled
post-completion logging in the next test unless safety requires earlier disable.

## Completed stationary capture

The new capture starts at 2080.705662 s and completes at 2083.473767 s, disabled,
with 100 accepted MultiTag samples per camera. Both cameras are connected; wheel
speeds are zero during the capture; maximum gyro rate is 0.129°/s. Capture was OFF
during the drive. ComparisonReady starts this rotated file as true from the prior
capture, resets false when the new capture starts, then becomes true again; the
statistics below use the NEW completed capture, not its inherited initial values.

| Stationary statistic | Left dd3c | Left 4a06 | Right dd3c | Right 4a06 |
|---|---:|---:|---:|---:|
| X standard deviation | 0.60 cm | 0.44 cm | 0.27 cm | 0.41 cm |
| Y standard deviation | 1.99 cm | 1.32 cm | 0.79 cm | 1.07 cm |
| Combined XY standard deviation | 2.07 cm | 1.39 cm | 0.84 cm | 1.15 cm |
| Yaw standard deviation | 0.266° | 0.178° | 0.108° | 0.147° |

Left XY noise is approximately 33% lower in this sample; right XY noise increased.
Left/right XY noise ratio is now 1.22 versus 2.48 previously. This is encouraging
but not a controlled proof that focus caused the timing change or removed bias:
viewpoint, lighting/exposure, sample variation, and battery loading also differ.
These short correlated samples do not independently establish absolute accuracy.
Retain current XY camera factors 2.15/1.0 pending post-calibration repeated captures.

The two endpoint camera means disagree by 1.83 cm X, 0.30 cm Y (1.85 cm total),
and 1.071° yaw. The old dd3c separation was 1.56 cm total, so smaller random noise
has not demonstrated better agreement in mean position.

Relative to the stored return target, the new static camera means are:
- Camera0/front-left: +2.272 cm X, -2.005 cm Y, absolute field yaw +0.240°.
- Camera1/front-right: +0.446 cm X, -2.301 cm Y, absolute field yaw -0.832°.

Absolute camera yaw is not rotation relative to the normalized auto start. Disabled
heading fusion differs from the enabled policy. Do not use the disabled fused
heading as the robot's heading at command completion.

## Position estimate still changes after physical stopping

From +0.30 to +1.00 s after completion, fused Y changes -5.14 cm and X +1.88 cm.
Over the same interval, integrated logged chassis velocity gives approximately
-0.0048 cm X/-0.0006 cm Y, and maximum recorded wheel speed is 0.0124 m/s.
This strongly suggests estimator corrections, not comparable chassis translation.
Sampled velocity is only a motion proxy; it is not independent surveyed ground truth.
It does not prove that the refocusing caused this estimate change. No post-finish
controller correction is triggered because the command has already completed.

The logged final error therefore cannot substitute for the physical endpoint.
Do not change PID, loosen the pose gate, or reduce the left-camera noise factor
just because its short stationary capture is steadier.

Battery minimum during the command is 10.37 V, compared with 9.70 V in dd3c;
this is another uncontrolled difference, not proof of a timing cause. Final-controller
median/max loop intervals are 32.0/61.6 ms. No causal attribution from maxima alone.

## Next action

1. Recalibrate ONLY the affected left camera first. Keep the final lens focus fixed
   and verify calibration
   uses the AprilTag processing resolution, with measured board dimensions and
   varied board angles/coverage. No change to the unchanged right camera is required
   solely because the left camera was refocused.
2. With the robot disabled and at the usual marked start, take a fresh 100-sample
   camera capture. Confirm ComparisonReady resets and returns true after starting.
3. Run H5 on unchanged robot code with capture OFF during movement. Keep >=3 s of
   supervised enabled logging after completion, then disable without repositioning.
4. Measure both front-frame X displacements and the same frame-corner Y reference,
   then take the disabled stationary endpoint capture. Finalize the log afterward.
   Retain start and endpoint capture events in the log; starting the second capture
   resets the live displayed statistics, but the log retains the first capture.

No numerical 4a06 endpoint was measured, so obtain new ruler data after recalibration
rather than assigning previous measurements to this run. Existing layout remains
`C:\MechaRAMS\temp\AdvantageScope 9-28-2026 - Auto Finish Ownership.json`.

For dd3c, the now-confirmed frame references and previously recorded 0.6072 m width
make the 2 cm corner difference approximately +1.89° counterclockwise. This estimate
belongs to dd3c, not 4a06. Frame-corner Y is still not automatically robot-center Y.

No robot builds, tests, simulation, deployment, PhotonVision writes, or tuning
changes were performed. Analysis uses the existing read-only WPILOG reader.
