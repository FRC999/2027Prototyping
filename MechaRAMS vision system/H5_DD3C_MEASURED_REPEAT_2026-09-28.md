# H5 dd3c: first measured repeat — September 28, 2026

Source: `C:\MechaRAMS\temp\akit_rotated_1790641695216_19cfdd3c.wpilog`.
The mentor explicitly confirms only ONE run was completed. The log contains one
completed OUT_AND_RETURN and one subsequent disabled stationary camera capture.

## Decision

Keep the deployed code, camera weights, and current layout. This was a faster H5
with no stopping-hold release, using the same logged controller configuration as
86c1. The stop-ownership fix still works. Do not retune from one successful repeat;
complete the second measured H5 on unchanged settings.

## Physical measurements

| Reference | Measured displacement |
|---|---:|
| Left front bumper reference, X | -2.5 cm |
| Right front bumper reference, X | -0.5 cm |
| Reported Y reference | -0.5 cm |
| Average of the two X references | -1.5 cm |

The X average is the front-reference midpoint displacement, not exactly the robot
center displacement when yaw changes. The 2 cm right-minus-left difference indicates
a small counterclockwise rotation relative to the starting orientation. IF these
are the same frame-corner references previously recorded 0.6072 m apart, it gives
approximately +1.89°. If they are outer bumper corners, their actual spacing is
needed instead. Confirm which physical point supplied Y before converting that
measurement to a robot-center error: rotating a front point changes its Y even
without the same amount of center translation.

## Logged timing and completion

| Metric | Previous H5 86c1 | Measured H5 dd3c |
|---|---:|---:|
| Total command | 7.903 s | 6.820 s |
| Final-controller interval | 1.919 s | 0.868 s |
| First stopping hold to command completion | 0.790 s | 0.047 s |
| Hold releases / renewed corrections | 1 | 0 |

Total command includes initial setup/alignment. Final-controller time includes
remaining travel. Neither is solely jitter. dd3c first reaches simultaneous tight
position/heading tolerance 0.067 s before completion. The internal uninterrupted
finish qualification is 0.051 s; the 0.047 s interval above uses logger timestamps,
which are not identical to the controller's intra-loop timer reads.

The run begins at 1152.502227 s, final precision at 1158.454069 s, and completes at
1159.321838 s. FinishQualified=true; TimedOut=false; strict rotating-approach finish
gate=false. Pose tolerance is 2 cm and 1.5°. No parameter change explains the faster
result: all logged configured controller values match 86c1.

Target and accepted normalized start are both (2.229695, 1.945870, 0°). At completion:
- Fused relative X/Y: +0.408/+0.369 cm; radial error 0.550 cm.
- Logged yaw: +1.115°, gyro rate -0.113°/s, chassis translation speed 0.056 m/s.
- Largest individual module speed: 0.236 m/s. The straight-handoff finish gate uses
  chassis speed, not the strict all-module-speed gate; do not say every wheel was
  already stopped when the command completed.

Post-completion steering targets do not change: default command takes ownership,
precision hold remains true, and manual drive remains disallowed. Yaw is +1.339°
at +1 s and +1.338° at +2 s. Maximum post-completion yaw change is 0.274°.
The last recorded wheel-speed sample above 0.05 m/s is +0.271 s after completion;
this is brief braking/remaining wheel motion, not a restarted position correction.
Do not equate the 47 ms command hold tail to all visible wheel settling.

The robot is disabled 2.013 s after completion, rather than the requested >=3 s.
That is enough to see the preserved stop hold here, but use >=3 s for the next run
unless immediate disable is needed for safety.

## Stationary capture is complete and usable

Capture starts at 1173.674567 s while disabled, about 14.35 s after completion.
Both cameras reach 100 accepted MultiTag samples, ComparisonReady becomes true,
and capture automatically stops at 1176.665995 s. Both cameras are connected,
measured wheel speeds are zero throughout capture, and maximum gyro rate is
0.129°/s. Capture was OFF during the trajectory.

These are un-fused per-camera robot-pose statistics, not raw pixel measurements.

| Statistic | Camera0 / front-left | Camera1 / front-right |
|---|---:|---:|
| X standard deviation | 0.60 cm | 0.27 cm |
| Y standard deviation | 1.99 cm | 0.79 cm |
| Combined XY standard deviation | 2.07 cm | 0.84 cm |
| Y peak-to-peak range | 10.50 cm | 3.88 cm |
| Yaw standard deviation | 0.266° | 0.108° |

The left camera's combined XY noise is 2.48 times the right's in this sample.
Current XY standard-deviation factors are already 2.15 and 1.0 respectively;
left-camera measurements already receive less estimator weight. One 100-frame
capture is not enough to replace the existing multi-distance characterization.
These frames are temporally correlated; do not treat standard deviation as an
independent-sample confidence interval or as absolute position accuracy.

Camera means are separated by 0.30 cm X and 1.54 cm Y (1.56 cm total), and 0.853°
yaw. Relative to the stored return target, their endpoint means are:
- Camera0: -0.56 cm X, +2.74 cm Y, absolute field yaw +2.103°.
- Camera1: -0.26 cm X, +1.20 cm Y, absolute field yaw +1.250°.

Do not treat those absolute camera yaws as rotation since start. Auto normalizes
its initial heading to zero; the cameras still report their field-frame solutions.
Disabled vision heading fusion also differs from the enabled control policy.

The target is an accepted starting Camera0 pose, not the mean of the endpoint
capture or a surveyed ground-truth pose. As context, a nearly stationary 1.85 s
pre-start window has 72/61 updated accepted camera poses with mean Y 1.960607 /
1.917369 m, versus target Y 1.945870 m. This is not a formal matched static capture;
it illustrates why endpoint-minus-target alone cannot identify absolute camera
bias or justify changing transforms from this run.

Minimum roboRIO battery reading during the command is 9.70 V, from about 12.34 V
before enabling. This is a recorded load dip, not proof of a battery fault or the
cause of the motion. PDH voltage in this file is zero and is not a valid substitute.
Final-controller median/max loop intervals are 23.7/56.2 ms.

## Next action

One more H5, unchanged code and both cameras open, from the same marked start.
Use the same physical measuring points, state whether they are frame or outer
bumper corners, and identify the Y reference point. Capture OFF while driving;
retain >=3 s supervised enabled logging after completion, disable without moving,
measure X/Y, then start the disabled-only camera capture. Wait for ComparisonReady
before finalizing that run's log. Disable immediately for unsafe motion.

Use the existing layout:
`C:\MechaRAMS\temp\AdvantageScope 9-28-2026 - Auto Finish Ownership.json`.
No additional logging fields or deployment needed. H4's separate angular-approach
issue remains open; this H5 result does not change the H4 conclusion.

Analysis uses `tools/audit_h4_history.py` and its read-only Log reader. No robot
build, tests, simulation, deployment, camera writes, or tuning changes performed.
