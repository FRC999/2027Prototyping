# Deployed stop-ownership validation — September 28, 2026

Logs: H4 68e2 / 4792, H3 bc61, H5 86c1.

## Decision

Keep d06e5b4. The deployment and the intended fix are verified in all four logs:
Drive/CommandOwner changes from the auto group to DriveManuallyCommand,
Drive/PrecisionAngleHoldActive stays true, Drive/ManualDriveAllowed stays false,
and no post-completion steering-target change occurs.

No robot-code changes are made from this batch. Remaining H4 and H5 settling have
different causes; a blanket gain or tolerance change is not supported. H3 is a
good regression baseline. Independent physical X/Y/yaw measurements are missing.

## Results

All four completed successfully, with FinishQualified=true and TimedOut=false.
Both cameras were connected in the examined runs; camera-jitter capture was off
and gyro yaw-rate status was OK at completion. Headings are logged estimates,
not independent ruler/angle measurements.

| Run | Total command s | Final controller s | First tight pose to completion s | First hold to completion s | Hold releases | Finish yaw / +1 s yaw |
|---|---:|---:|---:|---:|---:|---|
| H4-1 68e2 | 3.875 | 1.371 | 0.888 | 0.341 | 0 | -20.10° / -19.94° |
| H4-2 4792 | 3.514 | 1.132 | 0.611 | 0.281 | 0 | -19.14° / -18.77° |
| H3 bc61 | 3.218 | 0.650 | 0.087 | 0.067 | 0 | -0.42° / -0.35° |
| H5 86c1 | 7.903 | 1.919 | 1.210 | 0.790 | 1 | -0.47° / -0.43° |

Definitions:
- Total includes setup/alignment, not just chassis travel.
- Final controller includes useful remaining travel, so it is not all jitter.
- First tight pose means the first logged simultaneous position/heading tolerance
  pass, regardless of speed; it can be a transient pass while still moving.
- First hold begins when pose AND entry-speed checks pass. The hold tail includes
  any release and correction before completing.
Neither interval is an exact video-based settling measurement.

The H4 hold tails are now about 8–9% of total command time, but earlier angular
corrections are not counted by that metric. Do not claim the full visible-settling
goal has been achieved from the short hold tails alone.

At completion the estimated radial XY errors were 1.63 / 3.09 / 2.54 / 1.46 cm.
H4/H3 still use 4 cm, H5 2 cm, with 1.5° estimated yaw tolerance. An estimate
inside tolerance does not prove the physical robot is inside the same tolerance.

## The post-completion bug is fixed

| Run | Largest post-finish target-angle change | Largest post-finish yaw change | Enabled post-window |
|---|---:|---:|---:|
| 68e2 | 0° | 0.20° | 2.071 s |
| 4792 | 0° | 0.37° | 3.000 s |
| bc61 | 0° | 0.08° | 3.000 s |
| 86c1 | 0° | 0.11° | 2.350 s |

This contrasts with pre-deployment 14c2: its targets jumped 67.65° and heading
changed from +1.05° to +2.37° after completion. Current H5 stays near -0.43°.
There can still be brief braking motion with unchanged wheel targets; preserving
a hold does not make all mechanical motion cease instantaneously.

The measured maximum wheel-speed threshold of .05 m/s was last exceeded after
completion at approximately +.096 s (4792), +.031 s (bc61) and +.045 s (86c1);
68e2 never exceeded it in its post-window. These wheel-speed observations do not
by themselves prove every steering motor was perfectly motionless.

## What remains on H4

The handoffs still leave about 15.94° / 16.35° of yaw correction with gyro rates
-44.81°/s / -42.45°/s. The controller therefore has meaningful turning and braking
left to do while translation is coming to a stop.

In 68e2, representative yaw samples during the final controller are:
about -20.36° at +0.5 s, -22.33° at +0.7 s, -18.04° at +0.9 s,
then near -20° again. The gyro confirms real turning, not just a camera heading jump.
Vision rotation fusion is disabled while enabled. 4792 has a smaller similar cycle.

Both eventually latch zero hold once and do not release it. The strict finish gate
waits for wheel/gyro calm; the default command then preserves the hold. Simply ending
earlier or loosening the yaw gate would conceal some motion rather than remove it.

A future controlled experiment can address angular approach tracking (including
scheduling the turn earlier while respecting footprint/tag visibility), separately
from H5 localization. Do not increase global kD or change all route finish limits
from these two runs. The existing velocity-damping term already opposes gyro rate,
and logged command intervals still have substantial outliers.

Final-controller median/max loop intervals:
68e2 22.6/87.3 ms; 4792 21.9/88.9 ms; bc61 25.6/65.7 ms; 86c1 23.1/61.4 ms.
These can affect consistency but are not proof of the cause of each oscillation.

## What remains on H5

H5 retains its original return target: accepted starting X/Y and normalized 0° yaw.
Its path, 2 cm translation tolerance and 1.5° yaw tolerance were not changed by the
stop-ownership fix.

Timeline relative to final-controller activation:
- +1.129 s: first hold; controller error 1.89 cm, yaw error 0.27°.
- +1.540 s: hold releases after the estimated position stays outside 2 cm for the
  existing 200 ms confirmation. Controller error is 3.43 cm; yaw error only 0.80°.
  PoseRequalificationConfirmed=true, VelocityEscapeConfirmed=false.
- +1.856 s: correction reaches tolerance and hold is entered again.
- +1.919 s: command completes; the hold remains in effect afterward.

For roughly +1.309 through +1.540 s, Drive/Pose X changes about 1.96 cm while
integrating the logged chassis velocity yields only about 0.074 cm X and 0.039 cm Y.
Wheel speeds are near zero in much of this interval. Camera estimates also vary
over centimeter scales. That strongly supports an estimator-driven correction rather
than a comparable amount of actual chassis travel.

This is not independent physical proof: sampled wheel velocities have uncertainty,
and Drive/Pose and controller pose are read at slightly different instants. The two
pose channels can differ by several millimeters during an update. The release error
above is the controller's own logged value, not a value reconstructed from an earlier
subsystem-periodic snapshot.

During a short held segment, six updated accepted poses per camera have approximately
1.48/2.89 cm (Camera0 X/Y) and 2.15/5.41 cm (Camera1 X/Y) peak-to-peak spread.
The sample is too short, and follows braking too closely, to treat it as a full static
camera calibration or independently fit new covariance constants.

About half a second after completion, the fused XY error is near 0.2 cm while wheels
are stopped. This further shows why instantaneous finish estimates and settled camera
estimates need to be compared against actual floor measurements.

Do not freeze vision, widen the 2 cm radius, or suppress a persistent pose correction
just to shorten the log. Those changes could hide a real physical error. First measure
the endpoint and camera repeatability at this return distance.

## Next test: measured repeatability, unchanged code

No new deployment or layout is needed if this batch used d06e5b4.
Use C:\MechaRAMS\temp\AdvantageScope 9-28-2026 - Auto Finish Ownership.json.

1. Mark the normal starting robot pose on the floor. Use the same reference points
   and square the robot to the board; both cameras remain open.
2. Run H5 twice from those marks. Camera-jitter capture stays OFF during both runs.
3. On each run, leave normal logging active and the robot untouched for at least
   3 seconds after command completion, supervised with disable ready. Then disable.
   Do not move it before measuring. Disable immediately for unsafe motion.
4. Measure left/right front-corner X displacement and a clearly identified Y
   displacement. Record final yaw independently if possible. H5 targets zero
   displacement and zero yaw relative to its accepted start.
5. While still disabled and stationary at the endpoint, press
   Start Camera Jitter Capture (Disabled Only). Wait for
   Vision/JitterCapture/ComparisonReady=true (100 accepted MultiTag samples per
   camera). Capture stops automatically; do not enable during capture. If either
   camera cannot complete, report that rather than treating partial results as ready.
6. Rotate/finalize the log only after the capture completes, so the drive and its
   stationary endpoint capture are in the same log. Use a separate file for each run.
7. Run one measured H4 from the usual marks if practical; target +2.25 m X,
   +0.75 m Y and -20° yaw. Measure independent yaw and use a center reference for
   X/Y where possible: rotated front corners are not the robot center. Keep the
   same post-completion normal-logging window.

H3 does not need another immediate tuning run. Its .650 s final controller and
stable post-finish heading are the behavior to preserve.

The stationary capture in step 5 is explicitly AFTER disabling and stopping,
not the earlier practice of enabling jitter capture while driving. Its disabled
fused pose must not be substituted for the enabled controller's finish pose:
vision heading-fusion policy changes with enable state. Per-camera raw means/spread
remain useful, and physical X/Y/yaw measurements are the accuracy reference.

Decision after this batch: separate localization accuracy/noise from final-control
tracking, then make one isolated change to the appropriate component. More cameras
or higher speeds are not the next action in the current confined space.

## Reproducibility

Reader: tools/audit_h4_history.py, --all-modes --suffix 68e2 4792 bc61 86c1.
Derived data: C:\MechaRAMS\temp\deployed-stop-ownership-validation-2026-09-28.json.
No robot build, tests, simulation, or deployment were performed by Codex.
