# September 27 stationary and holonomic baseline

## September 28 robot validation: H5 2bc5 / c34b

| Log | Total | PathPlanner | Final controller | First hold to finish | Hold releases | Physical return X left/right, Y |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| 2bc5 | 6.584 s | 5.482 s | 0.756 s | 0.068 s | 0 | -2/-2 cm, -3 cm |
| c34b | 6.553 s | 5.533 s | 0.860 s | 0.063 s | 0 | -3/-3 cm, -3 cm |

Both logs verify `DriveRequestType=VelocityAngleHold` during final hold, no timeout and
`FinishQualified=true`. The previous no-angle-hold tests 55e2/0d1f had 8.887/7.542 s
overall, 2.802/1.557 s final control and four/one hold releases. This is strong evidence
that the measured-angle hold reduced the repeated final corrections in these two trials.
It does not isolate all timing differences: start/voltage and runtime conditions varied.
End-of-command fused X/Y error relative to logged target: 2bc5 +0.440/-0.269 cm,
c34b -0.835/+0.201 cm, with yaw -0.322/-0.682 degrees. The physical return measurements
remain about 2-3 cm beyond the original X and 3 cm to robot-right. Do not conflate the
2 cm *estimated* tolerance with measured placement. Both cameras' pre/post stationary
mean poses also shift on X/Y; Camera0/Camera1 absolute pre-start Y differs by about
4.04/6.59 cm, and the single Camera0 seed is 1.52/0.40 cm below its preceding mean Y.
Neither observation alone determines which transform is wrong or the physical origin.

2bc5's reported little jitter occurs at the required PathPlanner reversal, before final
precision. Around return phase start the commanded maximum module speed briefly reaches
zero while actual maximum speed is roughly 0.9 m/s and gyro yaw rate roughly 20 deg/s;
the subsequent reverse accelerates. H5 has two paths because it must stop and reverse;
do not call this a failure of the final stop controller or remove the safety stop based on
one observation. c34b had very little jitter at final stop.

RIO1 timing remains uneven: precision-loop max 92.21/68.19 ms, full-cycle max
96.68/73.07 ms, with median precision loops 23.15/24.19 ms. It did not obviously
regress versus the prior logs' larger outliers. No further gain, speed, camera or
route change. Next validation: H3 and H4 once each from original marks, both cameras
open, camera jitter capture off. Measure X/Y and yaw independently, collect separate
finalized logs with >=3 s ordinary stationary logging. The new steering-hold layout works.

## H5 55e2 / 0d1f: continuous finish verified, steering-at-stop experiment

| Log | Total command | PathPlanner | Precision | First hold to finish | Releases |
| --- | ---: | ---: | ---: | ---: | ---: |
| 55e2 | 8.887 s | 5.767 s | 2.802 s | 1.898 s | 4 |
| 0d1f | 7.542 s | 5.731 s | 1.557 s | 0.769 s | 1 |

Both use 2 cm radial tolerance, uninterrupted 50 ms finish confirmation, no timeout,
and jitter capture inactive. Physical left/right X and Y: 55e2 +1.5/0 cm, +4.5 cm;
0d1f -2/-3 cm, -2 cm. Final fused errors relative to the actual logged target:
55e2 X -0.512 cm, Y +0.234 cm, yaw -0.546 degrees; 0d1f X -1.982 cm,
Y +0.015 cm, yaw -0.995 degrees. Do not use the pre-reset Drive/Pose as the route origin.

55e2 releases: two confirmed 200 ms tight-pose failures, two immediate yaw escapes
above 2.5 degrees. 0d1f releases once at yaw 2.558 degrees. Neither has a confirmed
80 ms speed escape. Final uninterrupted confirmation is 62.8/51.6 ms respectively.
The stricter finish check is working; relaxing it would mask motion, not solve its cause.

During zero holds the module drive targets become zero but steering keeps seeking the last
nonzero-motion targets. At the first 55e2 hold, the front-left wrapped angle error is about
82 degrees. Gyro yaw rate subsequently rises again, reaching about -19 degrees/s during the
second hold and -31 degrees/s around the fourth release. 0d1f reaches about -28 degrees/s
during its first hold. This is measured rotation, not merely camera heading scatter; enabled
camera heading fusion remains off. Steering-at-stop is a plausible contributor, not a proven
exclusive cause. Some signals were sampled in separate loop phases; instantaneous target/state
comparisons at the hold edge may show the preceding request. Later zero-target samples confirm
the old angle targets persist.

Implemented isolated experiment: capture measured module angles once when entering precision
hold and maintain zero closed-loop drive velocity at those angles. On release, normal precision
control resumes; the next hold captures again. No X-lock, gain/speed/path/tolerance/fusion change.
The CTRE 26.3.0 local source confirms SwerveRequest.apply, SwerveModule.ModuleRequest and its
Velocity/Position setters. Four module applications run in the vendor odometry callback without
refreshes, logging or allocation; check loop timing because this is a custom Java request on RIO1.
Snapshot regression added but not run. Final stop/end preserves an existing angle hold; ordinary
motion clears it so cleanup cannot restore old steering targets after precision completion.

Localization also matters: first-run target Camera0 Y=1.9710 m versus preceding 2.9-second
Camera0 mean 1.9369 m (3.41 cm difference). Second target Y=1.9144 versus mean 1.9340 m
(-1.96 cm). A single fresh camera seed can shift the return reference. These means are diagnostic,
not independently surveyed robot-center truth. Post-stop mean camera Y shifts relative to before
are +2.91/+4.30 cm for 55e2 and -3.83/+1.26 cm for 0d1f; disagreement precludes blind extrinsic
tuning. A stationary-window seed is a possible follow-up, deliberately not changed in this experiment.

Minimum battery voltage 9.36/9.69 V. Precision execute intervals reached 99.6/95.3 ms.
Gyro status remains OK in both; maximum logged full-cycle durations during the routes are
294.1/220.3 ms. These precede the custom-request experiment and are a separate timing concern.
Keep battery comparable and separately track custom-request timing; no faster robot-loop change.
Next: manually deploy, H5 twice from the original marks, both cameras open, clear floor,
capture off, measure both-corner X and Y, normal logging >=3 s after stopping, disable and rotate.
New layout: C:\MechaRAMS\temp\AdvantageScope 9-27-2026 - Steering Stop Hold.json.

## Stationary pair

`fef2` (1A) and `e3b0` (1B) each completed a disabled 100-sample capture per camera.
Camera0 mean displacement was (-0.02581, +0.75056) m with +0.561 degree yaw change.
Camera1 mean displacement was (-0.00652, +0.70985) m with +1.057 degree yaw change.
Actual ruler displacement between these captures has not been separately confirmed by the user.
The requested displacement was +0.750 m Y. Do not treat the requested value as measured truth.
No camera transform, lateral scale, or vision weight adjustment is justified yet.

## Moving baseline

| Test / log | Total command | Continuous PathPlanner | Final controller | First hold to command finish | Hold releases |
| --- | ---: | ---: | ---: | ---: | ---: |
| H3 / 83fd | 4.355 s | 2.181 s | 1.806 s | 1.054 s | 7 |
| H4 / 5a1b | 4.959 s | 2.194 s | 2.638 s | 1.906 s | 5 |
| H5 / 76a2 | 7.624 s | 5.738 s outbound plus return | 1.622 s | 0.941 s | 2 |

Totals also include startup/reset overhead. First hold to finish is a controller measure, not an
independent measurement of visually observed jitter or time until every wheel stops.
All three runs completed without timeout, with valid gyro-rate status at the recorded hold exits.

| Test | Physical left/right front-corner X | Physical Y | Fused center displacement X/Y | Fused final yaw |
| --- | --- | --- | --- | --- |
| H3 | 2.260 / 2.285 m | approximately +0.810 m | +2.23796 / +0.77859 m | +1.146 deg |
| H4 | 2.380 / 2.165 m | not supplied | +2.24065 / +0.73331 m | -20.009 deg |
| H5 | 0 / 0 m | -0.015 m | -0.00744 / +0.01042 m | -0.655 deg |

H4 front-corner displacements include rotation of the measurement points. Their average is the
front-edge midpoint movement, not automatically the robot-center movement. H3 Y was explicitly
very approximate, so it cannot calibrate lateral scale.

## Evidence for the next isolated change

Six H3 hold releases were yaw-speed driven and one translation-speed driven. Four H4 releases
were yaw-speed driven and one translation-speed driven. Both H5 releases involved excessive
yaw speed; one also exceeded translation speed. After first hold, raw speed escape intervals
included many 20..70 ms excursions and some 100..233 ms excursions. H4/H5 also subsequently
crossed the wider pose envelope. These logs do not establish that all motion was sensor noise.

The proposed 80 ms continuous speed confirmation keeps zero commanded through short excursions,
preserves immediate pose escape and correction for sustained motion, and blocks success while
speed confirmation is pending. Success rechecks current tight pose/speed qualification. Physical
retest is required because changed motor commands change future measurements; an old-log policy
comparison cannot predict the new robot trajectory.

Retest H4 twice, H3 once, H5 once, each from the 1A marks in a separate finalized log. No speed,
PID gain, route, camera transform, or localization weight changes are part of this experiment.

## Retest after 5ce210c

All five logs identify the deployed 80 ms confirmation setting, finish with current qualification
true, and do not time out. All preserve the baseline geometry and speed profile.

| Test / log | Total command | PathPlanner | Final controller | First hold to finish | Hold releases |
| --- | ---: | ---: | ---: | ---: | ---: |
| H4 run 1 / 12b2 | 3.259 s | 2.211 s | 0.707 s | 0.080 s | 0 |
| H4 run 2 / 0b40 | 3.506 s | 2.179 s | 1.041 s | 0.052 s | 0 |
| H3 / 9771 | 3.412 s | 2.189 s | 1.103 s | 0.617 s | 1 (pose) |
| H5 run 1 / 58bc | 7.000 s | 5.757 s | 0.906 s | 0.072 s | 0 |
| H5 run 2 / 6356 | 7.448 s | 5.793 s | 1.307 s | 0.061 s | 0 |

The user observed very little H4 jitter, some H3 jitter, and more H5 jitter than H4. `58bc`
crossed wires near the endpoint and was physically about 5 cm off; exclude it from accuracy
validation. Battery replacement occurred after H4, so H3/H5 timing comparisons are confounded
by improved supply voltage. H4 starts were 11.95/11.93 V with loaded minima 9.51/9.60 V;
post-replacement H3/H5 starts were approximately 12.40..12.42 V, loaded minima 10.81/10.78/10.19 V.

| Log | Fused X/Y displacement | Final yaw | Final controller translation error |
| --- | --- | ---: | ---: |
| 12b2 | +2.2473 / +0.7346 m | -19.82 deg | 2.67 cm |
| 0b40 | +2.2445 / +0.7617 m | -20.81 deg | 1.30 cm |
| 9771 | +2.2465 / +0.7561 m | +0.21 deg | 0.72 cm |
| 58bc (wires) | +0.0266 / +0.0032 m | +0.02 deg | 2.88 cm |
| 6356 | +0.0154 / +0.0327 m | +0.18 deg | 3.63 cm |

These are localization estimates, not independent physical accuracy measurements. New ruler
measurements were not supplied except the approximate wire-affected error.

H4 run 1 recorded two roughly 20 ms pending speed excursions; neither restarted correction,
and the command did not finish while they were pending. H4 run 2 and both H5 runs had no
post-entry pending speed event. H3 had one short pending speed event that cleared, then
correctly released hold for translation error of 6.268 cm. Its reported translation/rotation
speeds at that release were only 0.0526 m/s and 0.199 deg/s. Camera0's last accepted pose Y
changed about 9 cm between first hold and release; Camera1's last accepted pose remained
unchanged. Physical motion and camera/fusion contributions require further separation.

Clean H5 still made small active corrections before qualifying at the end. After command finish,
module targets stayed zero, but the strict maximum-wheel-speed stopped criterion (0.02 m/s)
was first reached after 0.443 s. This is not a repeated stop-hold release. It does not alone
justify changing low-level drive/steer gains. Loop stalls remain intermittent: moving maximum
cycles were 131/136/101/170/96 ms, respectively; do not attribute all differences to the controller.

Decision: retain the 80 ms confirmation; make no further behavior change in this review.
Next run H3 twice and H5 twice, with clear floor/wires, both cameras open, original start marks,
precise physical X/Y displacement, and a separate finalized log for each run. Existing telemetry
and the September 27 Stop Hold Confirmation layout are sufficient.

## Second retest batch: 388d, 8928, 0dca, e707

| Test/log | Overall command | Precision phase | First hold to finish | Hold releases |
| --- | ---: | ---: | ---: | ---: |
| H3 388d | 2.860 s | 0.569 s | 0.081 s | 0 |
| H3 8928 | 3.177 s | 0.874 s | 0.054 s | 0 |
| H5 0dca | 6.773 s | 0.790 s | 0.118 s | 0 |
| H5 e707 | 8.993 s | 3.074 s | 2.436 s | 2 |

All finish qualified without timeout and use the 80 ms speed confirmation. H3 fused deltas
are (+2.2385,+0.7317) m / +0.247 deg and (+2.2452,+0.7421) m / -0.380 deg.
No independent H3 measurements were supplied. H5 0dca fused return is (+2.447,+0.044) cm,
yaw -1.415 deg; physical front-corner X +3/0 cm, Y -2 cm. Rotation means these corner
measurements cannot be directly substituted for robot-center translation.

H5 e707 final fused delta is (+0.848,+0.065) cm, yaw +0.553 deg. Its two hold releases
are immediate translation escapes at 6.123/6.082 cm, not confirmed speed escapes. During
the second 0.866 s hold, yaw around +1.86 deg lies between the tight 1.5 deg finish and
2.5 deg escape limits: no correction is commanded, but successful finish is blocked.
Late in this hold module actual and target speeds are zero while fused Y moves toward
the escape limit. Camera1 accepted Y reaches 1.997 m versus Camera0 around 1.935 m.
This demonstrates a localization contribution; the first release also has wheel/yaw motion.
Do not label the entire tail sensor noise. Moving maximum loop cycles are 66.5/73.4/98.5/93.8 ms;
battery loaded minima 10.26/10.24/10.25/10.01 V. No extreme stall explains the e707 tail.

Retain current behavior pending stationary camera-scatter isolation at the return position.
Use the existing 100-sample capture, both cameras open, and independently measure Y/heading.
No new logging/cameras/layout or redeploy required. A future hold-requalification fix should
address sustained out-of-tight-tolerance conditions without declaring an inaccurate finish
or reacting immediately to camera fluctuations.

## Stationary H5 capture 5c13

File: akit_rotated_1790539519506_0c475c13.wpilog. Capture 32.441..35.435 s; both cameras
reach 100 samples. Disabled throughout; maximum module speed zero.

| Metric | Camera0 | Camera1 |
| --- | ---: | ---: |
| X standard deviation | 0.572 cm | 0.279 cm |
| Y standard deviation | 2.057 cm | 0.853 cm |
| X peak-to-peak | 3.241 cm | 1.406 cm |
| Y peak-to-peak | 9.403 cm | 3.864 cm |
| Yaw standard deviation | 0.277 deg | 0.117 deg |
| Mean X/Y | 2.2572 / 1.9136 m | 2.2669 / 1.9188 m |
| Mean yaw | 1.979 deg | 0.859 deg |

Fused pose X/Y peak-to-peak during capture: 0.893/1.975 cm; yaw range 0.255 deg.
Camera0 mean minus Camera1 is -0.967 cm X, -0.520 cm Y, +1.121 deg yaw.
Thus stationary localization moves despite zero wheel speed. Existing camera XY factors
2.15/1.0 already give the noisier Camera0 less influence. The 3-second sample does not establish
absolute accuracy, a new extrinsic transform, or optimal covariance. Enabled camera-heading
fusion remains disabled; static camera yaw scatter alone does not explain enabled yaw oscillation.
Keep current camera weights and stop tolerances. No behavior/layout change in this review.
Mentor explicitly deferred the separate cold-boot/auto-start investigation.

## Latest reported tests: 2e8a, 122d, 94d2 — previous telemetry version

No ConfiguredPoseRequalificationSeconds or PoseRequalificationPending/Confirmed output exists
in any of these logs. 112c37e primes those keys at startup even without triggering recovery.
Therefore these do not validate the new 200 ms recovery logic; deploy and verify version first.

| Test/log | Overall command | Precision phase | First hold to finish | Hold releases |
| --- | ---: | ---: | ---: | ---: |
| H3 2e8a | 3.005 s | 0.653 s | 0.074 s | 0 |
| H5 122d | 6.995 s | 0.860 s | 0.061 s | 0 |
| H5 94d2 | 7.513 s | 1.550 s | 0.364 s | 1 |

All finish qualified without timeout. H3 fused displacement is +2.2500 X/+0.7458 Y m,
final reset-relative yaw -0.916 deg. Final Camera0 yaw +1.634 deg and Camera1 -1.641 deg
disagree by 3.275 deg; no independent physical angle provided. Do not calibrate mounts from this.
H5 122d fused return +0.542/+0.669 cm, yaw -0.152 deg; physical front-corner X -4/0 cm.
H5 94d2 fused return -1.798/+2.513 cm, yaw +1.112 deg; physical corners -2/-0.5 cm,
Y +3.5 cm. Corner measurements are affected by rotation and not exact center translation.

94d2 releases hold for confirmed yaw-speed escape: 14.08 deg/s, translation speed 0.066 m/s,
translation error 2.712 cm and yaw error 1.478 deg. This is not an immediate pose escape.
Moving max loop times 65.2/96.0/92.2 ms; battery loaded minima 10.44/9.53/10.19 V.
No additional code tuning. Next manually deploy 112c37e, verify configured interval 0.20 s,
then H5 twice and H3 once with physical X/Y and heading measurements where practical.

## Deployed retest: 78e3, 4c92, 3f56

All three contain configured pose-requalification interval 0.20 s. All finish qualified,
no timeout, no hold releases and no pose-requalification events. The new policy never
triggered; differences in these runs cannot be attributed to its recovery behavior.

| Test/log | Overall | Precision | First hold to finish | Fused X/Y delta | Fused final yaw |
| --- | ---: | ---: | ---: | --- | ---: |
| H3 78e3 | 2.940 s | 0.604 s | 0.133 s | +2.2526/+0.7397 m | +1.361 deg |
| H5 4c92 | 7.085 s | 0.807 s | 0.080 s | +2.276/+1.220 cm | +0.126 deg |
| H5 3f56 | 7.476 s | 1.493 s | 0.056 s | +1.713/-0.681 cm | +0.834 deg |

Physical H5 front-corner X +4.5/+5 cm and +2/+1.5 cm; Y +3 cm and +1 cm respectively.
Positive residual X means the backward return stopped short of the start. The command
allows 4 cm estimated radial translation error; it does not promise an exact zero return.
4c92 physical mean front-corner X ~4.75 cm versus fused X 2.28 cm shows an additional
localization/measurement discrepancy, not explained by tolerance alone. Corner midpoint is
only a proxy for center displacement under rotation. No H3 physical measurement supplied.

3f56 active tail remains before first zero hold, not a cancellation/requalification loop.
Around total time 6.776 s: translation speed 0.155 m/s, yaw rate 32.5 deg/s; near 7.276 s:
yaw error 2.76 deg. Both prevent tight pose/speed qualification. Heading damping follows
alternating measured rates; it is not enough evidence to call the damping gain incorrect.
After completion module targets remain zero; strict WheelsStopped first occurs +0.399/
0.143/0.085 s for H3/H5/H5, with some subsequent low-speed toggles.
Moving max loops 50.96/124.25/134.53 ms; battery minima 10.26/10.58/10.48 V.

No additional tuning or tolerance changes. Next diagnostic: stationary captures at normal
start and independently measured exactly +1 m X, same Y and yaw, both cameras open.
Compare per-camera mean delta, fused delta, scatter, and physical separation before adjusting
camera transforms/scale or shrinking tolerance (which may increase noise chasing).
Existing capture command/layout suffices. Boot investigation stays deferred.

## Forward stationary pair 31ea / 62ff

31ea capture 97.132..100.138 s: disabled, 100 samples per camera, wheel speeds zero.
62ff retains the previous frozen capture outputs; no new capture was started. It nevertheless
contains raw camera data, disabled from178.299 s, wheel speeds zero at the final position.
Used the final100 accepted poses per camera (183.443..186.054 s Camera0,
183.343..186.054 s Camera1). Adjacent179..182 and182..185 s means agree closely.

| Source | Initial mean X | Final mean X | X delta | Y delta | Yaw delta |
| --- | ---: | ---: | ---: | ---: | ---: |
| Camera0 | 2.261207 m | 3.261334 m | 1.000127 m | -6.601 cm | +0.911 deg |
| Camera1 | 2.242019 m | 3.245953 m | 1.003934 m | -4.593 cm | +0.628 deg |
| Fused pose | 2.247650 m | 3.249606 m | 1.001957 m | -5.068 cm | +0.908 deg |

Measured physical X separation1 m. No meaningful X scale discrepancy; do not adjust encoder
radius or camera scale. Physical Y and heading preservation were not independently supplied;
the Y/yaw changes cannot be assumed entirely localization bias or entirely physical drift.
Camera0 X/Y standard deviation decreases from0.509/1.428 cm to0.350/0.714 cm; Camera1
from0.285/0.941 cm to0.183/0.517 cm. Stationary means are more useful than a single frame.

Proceed with single-variable H5 return-accuracy experiment: estimated radial finish2 cm
instead of4 cm; all other callers remain4 cm. No gains, speeds, handoff, heading checks,
feedforward fade, camera weighting or extrinsics changed. Same selected tolerance governs
200 ms pose requalification. Wider escape remains6 cm. This may increase settling; evaluate
physical X/Y and command time in H5 twice after manual deployment. No absolute2 cm guarantee.

## H5 tighter finish retests: 3ce7 (reported 3ec7), 75d2

No3ec7 file found; nearby first file is akit_rotated_1790541921880_bcbf3ce7.wpilog. Treat
its association with first physical measurements as an explicitly disclosed assumption.
Both contain2 cm tolerance, qualified finish, no timeout and no hold releases.

| Log | Total | Precision | First hold to end | Fused X/Y return | Final yaw |
| --- | ---: | ---: | ---: | --- | ---: |
| 3ce7 | 7.337 s | 0.904 s | 0.148 s | -0.112/+1.484 cm | +0.500 deg |
| 75d2 | 7.675 s | 1.732 s | 0.079 s | +0.034/-1.347 cm | +0.888 deg |

Physical front-corner X -1/0 cm,Y +5 cm and +1/0 cm,Y -1.5 cm respectively. X improved.
Stationary camera means in3ce7: before85.322..88.222 s versus after95.960..98.660 s,
Camera0 Y1.93788->1.99364 (+5.575 cm),Camera1 1.91665->1.96497 (+4.832 cm).
Postwindow starts after wheel-stop and spans disable97.124 s. These means support physical
Y displacement, unlike the transient completion estimate. 75d2 equivalent stationary windows
Camera0 Y1.93309->1.92467 (-.842 cm),Camera1 1.88163->1.90176 (+2.012 cm), showing
camera disagreement. Postwindow precedes disable220.603 s but wheel stop is established.
No extrinsic/scale calibration inferred from means.

75d2 jitter precedes firsthold, with yaw rate up to41.84 deg/s in sampled tail and repeated
translation speed failures. Pose/speed recovery confirmations never release either run.
3ce7 briefly loses tight pose qualification during hold; existing hold-age timer keeps aging.
Fix logical weakness by requiring continuous good tight pose/speed checks for existing50 ms
before success, with no pending speed escape. Failed samples reset finish clock without
immediately restarting wheels. Add FinishQualificationSeconds, preserving hold-age SettleSeconds.
No gains, tolerances, speed, camera or path change. No claim this solves pre-hold oscillation.
Moving max loops181.12/120.54 ms; battery minima10.09/10.15 V.
