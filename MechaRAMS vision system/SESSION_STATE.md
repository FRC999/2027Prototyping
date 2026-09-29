# Session State - VisionTestingAndCalibration

## 2026-09-28 deployed stop fix validated: 68e2/4792/bc61/86c1

All four contain ownership telemetry: default takes over, precision hold remains true,
manual mode false, post-finish steering-target change 0°. No timeouts; keep d06e5b4.
No robot changes this turn. [Detailed analysis](DEPLOYED_STOP_VALIDATION_2026-09-28.md).
H4 total/final controller: 3.875/1.371 s and 3.514/1.132 s; first-hold tails
0.341/0.281 s, no releases. H3 total/final: 3.218/0.650 s, tail 0.067 s,
no releases. H5 total/final: 7.903/1.919 s, tail 0.790 s, one release.
Yaw finish/+1 s: H4 -20.10/-19.94° and -19.14/-18.77°;
H3 -0.42/-0.35°; H5 -0.47/-0.43°. Max post yaw change: 0.20/0.37/0.08/0.11°.
This is a successful ownership fix, not proof all pre-finish settling is solved.

H4 still receives ~16° correction/~43°/s yaw and rings before first hold. First
tight-pose-to-completion: 0.888/0.611 s, not merely the 0.341/0.281 s hold tails.
Do not claim total visible settling <10% just from hold tails. H3 first-pose tail: 0.087 s.
H5 release at precision +1.540 s is pose requalification: controller error 3.43 cm,
outside 2 cm for 200 ms, yaw only 0.80°; not speed escape. During approx +1.309 to
+1.540 s, fused X changes 1.96 cm while integrated wheel velocity suggests
0.074 cm X/0.039 cm Y.
Evidence supports estimator-driven correction; physical ruler data absent, so do
not freeze vision/widen tolerance or assert physical position from fused estimate.
Drive/Pose and controller MeasuredPose have within-loop skew; use controller flags
for release. At +0.5 s after finish: fused error ~0.2 cm, stable yaw, wheels stopped.

Next unchanged-code H5 twice from marks with measured corner X/Y/independent yaw.
Capture OFF during driving; normal logging >=3 s enabled after completion, supervise,
then disable without repositioning and perform existing disabled-only 100-sample
camera jitter capture at endpoint; rotate after ComparisonReady true. One log/run.
Optional one measured H4; no immediate H3 retune. Same Auto Finish Ownership layout.
Disabled fusion differs in heading: do not substitute disabled fused heading for
enabled finish heading. Raw camera statistics help assess noise; physical measurements
remain the accuracy reference. No new logging fields required.
Analyze only; no robot build/test/sim/deploy. Loop outliers remain 61–89 ms, no
causal claim from maxima alone. Boot test remains deferred.

## 2026-09-28 3a29/be40/e73d/14c2: stop fix not demonstrated in deployed build

Mentor subsequently confirmed they forgot to deploy and will retest with the
deployed fix. These are pre-fix results, not a regression of d06e5b4.

All four logs lack all three unconditional new Drive ownership/hold/mode field names,
verified in parsed records and raw bytes. Local built JAR also lacks those strings
in DriveSubsystem.class. No Git SHA metadata; cannot name exact old deployed revision.
All still show post-finish steering-target jumps. Do not credit d06e5b4 for the better
visual results or tune H5 as a regression from it. No robot changes this turn.
Details: [stop-ownership validation](STOP_OWNERSHIP_VALIDATION_2026-09-28.md).

Total/final-controller seconds: H4 3a29 3.358/.980, be40 3.791/1.458;
H3 e73d 3.580/1.079; H5 14c2 7.152/1.113. All successful, no timeout.
Hold tails .385/.844/.567/.286s; releases0/1/1/0. be40/e73d release for sustained
heading error beyond1.5deg (about2.02/1.65deg), not confirmed speed escape.
H5 target remains sampled start/zero yaw, translation2cm, yaw1.5deg, strictfalse.
At finish H5 yaw+1.046deg, gyro+.80deg/s, maxwheel.002m/s. At+68ms targets jump
67.65deg; gyro peaks18.41deg/s, maxwheel.463m/s, yaw+2.37deg at+1s. This is the
old post-completion disturbance. Other target jumps69.26/37.15/31.53deg.
Available enabled postwindows3/2.957/1.252/1.754s; H3/H5 shorter than requested.

Next manual build/deploy current VisionTestingAndCalibration source. While disabled,
verify Drive/CommandOwner, PrecisionAngleHoldActive, ManualDriveAllowed exist before
any test. Same Auto Finish Ownership layout, no new logging fields. H5 once first,
both cameras/captureOFF/normal marks; supervised >=3s enabled after finish, then
disable/rotate. If hold remains and targets do not jump, H4 twice,H3 once, physical
X/Y/yaw measurements. No retune until correct binary is verified. No build/deploy
or robot tests/simulation by Codex; analysis script/static checks only.

## 2026-09-28 H4 historical audit: preserve stop ownership after auto

Audited 101 local logs, 13 completed H4 attempts; details and calibration roadmap:
[H4 history and calibration status](H4_HISTORY_AND_CALIBRATION_STATUS_2026-09-28.md).
Mentor is correct: 0e7b total/final3.045/.656s, 12b2 3.259/.707s, 0b40 3.506/1.041s
were faster than fresh-battery b2ab4.296/1.711s and b2283.895/1.540s. Older versions
also produced slow runs; no wholesale rollback justified. Current first-hold tails
.521/.628s have zero releases. Handoff still has ~16.6deg correction/~43deg/s turn.

Concrete code/telemetry defect: after auto completes, DriveManuallyCommand sends
FieldCentric even in autonomous with zero input, clearing precisionAngleHold.
Targets jump68.37deg at+40ms in6e71,81.95deg at+42ms inb2ab,87.84deg at+148ms inb228;
post-finish max wheel speeds .327/.454/.471m/s. Earlier6e71 ~6deg drift cannot be
explained only as early finish/coasting; the default-command transition disturbs it.
Fix: default drive calls stop outside enabled teleop and for neutral sticks in teleop,
preserving an existing measured-angle hold. Real input/new driving commands still clear it.
No gains, paths, tolerances, strict gate, recovery, timing frequency or camera changes.
Added Drive/CommandOwner, PrecisionAngleHoldActive, ManualDriveAllowed telemetry.
ManualDriveAllowed reports teleop-enabled mode, not exclusive command ownership.

New layout: C:\MechaRAMS\temp\AdvantageScope 9-28-2026 - Auto Finish Ownership.json.
Previous layout preserved; live/saved tables include exact new paths and graphs use
continuous Drive gyro, not frozen post-end DriveToPose gyro. Next manual deploy:
H4 twice,H3 once,H5 once, normal marks,both cameras,captureOFF; supervise >=3s enabled
after success, then disable/rotate separate logs. Measure physical X/Y/yaw after rest.
No build, robot tests/simulation or deployment by Codex. Log-reader execution and
static source/layout checks only. Stop-owner fix awaits robot validation and does
not promise to shorten pre-completion yaw corrections.

Calibration: forward scale has good local evidence; lateral/rotated physical accuracy,
H4 repeatability, full-speed/general-heading tests and timing remain open. First isolate
stop ownership; then consider earlier PP turning/low-level tracking if pre-hold ringing
persists, independently measured repeatability, and a safe speed ladder in more space.
Two front cameras suffice now; additional coverage needed for headings/occlusion later.
Boot-without-teleop investigation remains deferred.

## 2026-09-28 conditional finish robot validation: H4 a6ab/be88, H3 bed2

Robot logs confirm strict policy true on both H4 starts (handoff yaw correction
~17.3/15.5 deg, gyro ~-43.9/-47.7 deg/s) and false on H3 (yaw ~0.31 deg,
gyro ~0.9 deg/s). H4 a6ab total4.887s, precision2.404s: hold first entered
at +0.981s, requalified/released twice at +1.372 and +1.834s after heading
error grew to 2.27 and1.76 deg, then held at +1.971s and finished without
timeout. Its first-hold-to-finish tail was1.423s; battery minimum9.66V and
precision loop max101ms, possible contributors but not proven causes. H4 be88
total3.541s, precision1.171s, one hold at +0.618s, no release, finish tail
0.553s, battery minimum10.21V, precision loop max59ms. Both H4 post-finish
yaws were stable over the next second: a6ab -18.84 -> -18.79 deg and be88
-19.20 -> -19.09 deg; old 6e71 drifted about6 deg. H3 bed2 total3.195s,
precision0.848s, one hold at +0.666s, zero exits, no timeout, finish tail
0.182s. No code change: one slow H4 and one acceptable H4 are insufficient
evidence for another generic controller tune, especially while H3/H5 appear good.

Mentor cites H5 log suffix1166, but no such file exists in C:\MechaRAMS\temp or
Downloads. The next chronological log ends1177 and identifies itself as
OUT_AND_RETURN, so it is a likely H5 candidate, not confirmed identical to
the mentor's cited log. It selected normal finish, total6.929s, final precision
0.932s, one hold at +0.823s, zero exits, no timeout; yaw 0.32 at finish and
0.73 deg1s later. Confirm file identity before claiming this as the 1166 run.

## 2026-09-28 H3/H4 measured-angle retest; stricter finish-motion gate implemented

4747 H3 total/precision3.388/1.048 s, first hold tail.072 s, no releases. Fused
delta +2.2697/+0.7599 m and -1.203 deg; physical front X +2.19/+2.19 m; Y unmeasured.
6e71 H4 total/precision3.651/1.363 s, first hold tail.069 s, no releases. At completion
yaw -19.771 deg vs -20 target, but while still enabled it rotates to -13.8 deg within
~0.8 s. Gyro peaks +19.5 deg/s after completion, modules still move with zero targets.
Do not interpret H4 front-corner X+2.32/+2.165 m as center X; physical Y/yaw not supplied.
Implemented conditional final-motion gate: at precision initialize, if heading
correction >2.5 deg OR gyro yaw rate >8 deg/s, select strict completion for this
attempt. H4 handoff was about16.3 deg /42.6 deg/s and selects it. H3 handoff was
about .18 deg/.7 deg/s and H5 recent handoffs .3-.6 deg/under3.1 deg/s; they
retain the prior finish check. Route name, path curvature and intended final yaw
do not select it. This applies to any direct or handed-off precision command,
not to PathPlanner-only motion or H5's intermediate reversal.
Hold still enters at <=.12 m/s chassis and <=8 deg/s gyro; for selected attempts,
successful completion now also needs each measured module <=.05 m/s and gyro
<=1.5 deg/s continuously for the existing50ms. This would have blocked logged H4
finish at .16 m/s wheel and3.5 deg/s yaw. Wider escape/200ms requalification can then resume
correction if drift persists. No path, gains, camera policy, pose tolerance or entry change.
New primed/logged finish-motion and strict-policy flags, wheel speed and both
configured limits; invalid handoff readings select strict rather than a lenient
finish. Pure regression added but not run. New Post-Finish Motion
layout in C:\MechaRAMS\temp. This is a generic state-based rule, not H4-specific.
Manual deploy,
then H4 twice, H3 once and H5 once from marks; observe 3 seconds still enabled after command, measure
independent yaw after wheels have stopped, then disable/rotate. Different command motion
may lengthen settling and is not guaranteed to cure mechanical steering disturbances.

## 2026-09-28 measured-angle H5 validation 2bc5/c34b; no new code change

Both logs contain VelocityAngleHold, 2 cm H5 tolerance and successful finish without timeout.
Total/PP/precision times: 2bc5 6.584/5.482/.756 s; c34b 6.553/5.533/.860 s.
First hold-to-finish .068/.063 s, zero hold releases in each. Compared with prior
55e2/0d1f 8.887/7.542 s total, 2.802/1.557 s precision and four/one releases,
results support the stop-angle experiment. Different starting battery and run conditions
prevent attributing every time difference to that code. Precision-loop median23.15/24.19 ms,
max92.21/68.19 ms; full-cycle max96.68/73.07 ms, so RIO1 loop outliers persist.
Physical X left/right -2/-2 cm and -3/-3 cm; Y -3/-3 cm. Fused final relative target
X/Y +.440/-.269 cm and -.835/+.201 cm, yaw -.322/-.682 deg: physical return is
2-3 cm beyond the original X and 3 cm right while fused controller reports within 2 cm.
Both camera stationary means generally see return X/Y shifts too, but their start absolute
Y disagree 4.04/6.59 cm; do not silently retune transforms. Single Camera0 seed Y differs
from preceding mean by -1.52/-.40 cm. No change to localization yet.
2bc5's observed outbound jitter is at deliberate PP stop/reverse, not final hold: around
return phase start target module speeds pass near zero but actual max wheel speed briefly
~.9 m/s and gyro rate ~20 deg/s. 0d1f comparison is not a turnaround retest. H5 needs
one reversal; keep geometry/PP gains for now. Next H3 once, H4 once from original marks,
both cameras open, capture off, physical X/Y and independent yaw; separate finalized logs.
No build/test/deploy by Codex. Boot investigation remains deferred.

## 2026-09-27 55e2/0d1f measured-angle stop hold implemented, validation pending

Both verify continuous finish and H5 2 cm tolerance; jitter capture off. Total/precision times
8.887/2.802 and 7.542/1.557 s; four/one hold releases. Steering continues toward old targets
while drive targets are zero, with measured gyro rotation. Implement isolated measured-angle
stop hold; keep gains, path, tolerances and finish/recovery gates unchanged. No build/deploy.
Each hold snapshots measured module angles once, uses zero Velocity/Position module requests;
releases resume normal control, new entry recaptures. Custom odometry callback has no refresh,
logging or allocations; monitor RIO1 timing. Snapshot regression added, not run. New
Drive/MaxAbsSteeringErrorDegrees and dynamic DriveRequestType=VelocityAngleHold.
Fused errors must use logged target, not pre-reset pose: 55e2(-.512,+.234) cm and -.546 deg;
0d1f(-1.982,+.015) cm and -.995 deg. No timeouts, no confirmed speed escapes.
Single seed Camera0 Y differs from pre-start mean by +3.41/-1.96 cm; possible seed-noise
contribution to physical return offsets. Do not alter camera transforms from these logs.
Next manual deploy H5 twice, original marks, both cameras, clear floor, capture off,
measure X/Y, >=3 s post-stop normal logging; separate finalized logs. New Steering Stop Hold
layout in C:\MechaRAMS\temp. Boot deferred. Treat steering fix as experimental until retested.

## 2026-09-27 H5 2 cm accuracy retest; continuous finish window implemented

User suffix3ec7 not found; adjacent matching first run file actually3ce7. Assumed mapping to
physical X -1/0 cm,Y +5 cm; explicitly disclose. 75d2 X +1/0 cm,Y -1.5 cm.
Both verify2 cm, qualified finish,no timeout/no hold releases. Total/precision3ce7 7.337/.904 s,
75d2 7.675/1.732 s. Fused return3ce7(-.112,+1.484) cm,yaw+.500;75d2(+.034,-1.347) cm,yaw+.888.
Post-stop camera mean Y delta3ce7 +5.58/+4.83 cm supports measured+5 cm despite finish
estimate+1.48 cm. These post windows mix enabled/disabled in3ce7; 75d2 postwindow enabled,
stationary after wheel stop. Don't infer absolute extrinsics from these means.
75d2 active jitter precedes firsthold, yaw-rate sample41.84 deg/s; neither recoverytimer triggers.

Fixed a distinct finish-check weakness: hold age survived brief tight-check failures, allowing
finish on a single recovered good sample. Added separate continuous tight pose/speed confirmation
using existing0.05 s. Reset on any failure/pending speed escape/inactive hold; no immediate
wheel restart for a brief failure. Primed FinishQualificationSeconds; reset on initialize.
No PID/path/camera/tolerance/speed changes. Regression case added (not run); no build/deploy.
Next manual deploy,H5 twice,original marks,both cameras,measuredX/Y,log >=3 s stationary
after finish,one finalized log each. New Continuous Finish layout in C:\MechaRAMS\temp.
This improves finish correctness, not a proven fix for all pre-hold jitter. Boot remains deferred.

## 2026-09-27 forward localization scale checked; H5 tighter endpoint experiment implemented

31ea is a valid disabled 100-sample capture. 62ff did not restart capture (old frozen100 values),
but raw accepted poses remain usable after disabling at178.299 s with module speeds zero.
Analyzed last100 camera observations around183.34..186.05 s; adjacent stationary windows agree.
Mean X delta: Camera0 1.000127 m, Camera1 1.003934 m, fused1.001957 m for measured1 m.
No X scale/encoder/extrinsic change. Y delta -6.601/-4.593/-5.068 cm, yaw +.911/+.628/+.908 deg;
independent physical Y/yaw unchanged were not confirmed, so do not tune lateral/heading geometry.

Implemented route-specific translation tolerance: H5 OUT_AND_RETURN 0.02 m, all default callers
0.04 m. Same radius used for tight pose/finish and requalification; wider escape0.06 unchanged.
No PID, speed, feedforward, camera weighting, heading or handoff change. Prime/log
ConfiguredTranslationToleranceMeters. Added pure validation regression cases (not run).
Manual deployment then H5 twice, separate logs, original marks, both cameras, clear floor,
measured both-corner X and Y. Compare accuracy versus extra settling; no absolute2 cm guarantee.
New layout C:\MechaRAMS\temp\AdvantageScope 9-27-2026 - Return Accuracy.json, old files preserved.
Boot investigation remains deferred. No build/deploy by Codex.

## 2026-09-27 78e3/4c92/3f56 verify 200 ms code; remaining jitter precedes hold

All three report ConfiguredPoseRequalificationSeconds=0.20, qualified finish, no timeout,
and no pose-requalification event or hold release. New policy did not trigger, so cannot
attribute the differing performance to it. Overall/precision: H3 78e3 2.940/0.604 s,
H5 4c92 7.085/0.807 s, H5 3f56 7.476/1.493 s. First hold to finish .133/.080/.056 s.
3f56's extra corrections occur before first hold while speed/heading checks fail; yaw-rate
samples include 32.5 deg/s and yaw error reaches roughly 2.76 deg. Damping alternates with
measured yaw rate; this is not proof of wrong gain or sensor noise. No further gains changed.

H3 fused delta +2.2526/+0.7397 m, yaw +1.361 deg; no physical measurements.
H5 4c92 fused return +2.276/+1.220 cm, yaw +0.126 deg; physical front corners +4.5/+5 cm,
Y +3 cm. H5 3f56 fused return +1.713/-0.681 cm, yaw +0.834 deg; physical corners +2/+1.5 cm,
Y +1 cm. Positive X means short of original start on backward return, not forward overshoot.
Existing 4 cm estimated radial tolerance permits a residual return offset. 4c92 physical X
exceeds its estimated X by roughly 2.5 cm; avoid equating this entirely with controller error.
Keep current code and layout; next diagnostic is two stationary captures at independently
measured X positions (normal start and exactly 1 m forward, same Y/yaw), both cameras open,
to separate forward-axis localization scale/scatter from early finish. No new hardware/logging.
Boot diagnosis remains deferred. No build/deploy by Codex.

## 2026-09-27 2e8a/122d/94d2 lack new pose-requalification telemetry

None of these logs includes ConfiguredPoseRequalificationSeconds or pending/confirmed outputs
primed unconditionally by 112c37e. Treat these as previous-code runs, not validation of the fix.
H3 2e8a overall/precision 3.005/0.653 s, no hold releases, fused delta +2.2500/+0.7458 m,
yaw -0.916 deg after reset-to-zero. Camera0/1 final yaw +1.634/-1.641 deg disagree; no physical
heading measurement. H5 122d 6.995/0.860 s, zero releases, fused return +0.542/+0.669 cm,
yaw -0.152 deg; physical front-corner X -4/0 cm, Y not supplied. H5 94d2 7.513/1.550 s,
one confirmed speed escape (14.08 deg/s at release), no pose escape. Fused return -1.798/
+2.513 cm, yaw +1.112 deg; physical corners -2/-0.5 cm, Y +3.5 cm.
All finish qualified without timeout. No new behavior change; manually deploy 112c37e and
verify ConfiguredPoseRequalificationSeconds=0.20 before H5 twice and H3 once. Same new layout.
Boot issue deferred. Do not tune camera angles or gains from these unmeasured heading impressions.

## 2026-09-27 bounded pose requalification implemented; robot validation pending

Mentor reiterated autonomous implementation authority. Added provisional 0.20 s continuous
loss-of-tight-pose confirmation during zero hold, closing e707's inactive 1.86-degree yaw gap.
Tight pose recovery or inactive hold resets it. Immediate wider pose escape and 80 ms sustained
speed escape remain; finish pose/speed limits, gains, trajectories, camera weights unchanged.
Primed/logged PoseRequalificationPending/Confirmed/Seconds and configured interval; initialize
clears state. Regression cases added for brief noise, sustained failure, inactive/reset behavior;
not executed under no-build instruction. Manual deployment then H5 twice and H3 once,
original marks, both cameras open, clear floor, measured X/Y/heading, separate logs.
New layout: C:\MechaRAMS\temp\AdvantageScope 9-27-2026 - Pose Requalification.json.
Boot investigation remains deferred. This policy needs robot validation; do not claim jitter solved.

## 2026-09-27 stationary H5 capture 5c13; boot investigation deferred by mentor

5c13 completed 100 samples per camera, disabled throughout, max module speed zero.
Camera0 X/Y std dev 0.572/2.057 cm, peak-to-peak 3.241/9.403 cm, yaw std dev 0.277 deg.
Camera1 X/Y std dev 0.279/0.853 cm, peak-to-peak 1.406/3.864 cm, yaw std dev 0.117 deg.
Means differ 0.967 cm X, 0.520 cm Y, 1.121 deg yaw. Fused capture Y range 1.975 cm,
X range 0.893 cm; no physical motion. Existing XY factors 2.15/1.0 already downweight
Camera0; do not retune based on one 3-second sample or treat stable mean as ground truth.
This supports a localization contribution to e707, not proof that every e707 escape was noise.
No code/layout change, no build/deploy. Boot-log investigation explicitly deferred by user.

## 2026-09-27 second stop-hold batch: H3 clean; H5 intermittent localization/hold tail

Reviewed 388d/8928/0dca/e707. All use 80 ms speed confirmation, finish qualified, no timeout.
Overall/precision times respectively: 2.860/0.569, 3.177/0.874, 6.773/0.790, 8.993/3.074 s.
Only e707 releases hold: twice, immediate pose escapes at 6.123/6.082 cm. First hold to end
2.436 s. Second hold lasts 0.866 s with yaw around +1.86 deg: outside tight 1.5 deg finish
tolerance but inside 2.5 deg escape tolerance, holding zero without qualifying completion.
Late in that hold module speeds are zero yet fused Y changes; Camera1 accepted Y approaches
1.997 m versus Camera0 around 1.935 m. First release also includes wheel/yaw motion.
Do not classify all jitter as camera noise or retune PID alone. No behavior code changed.
Next: stationary return-position 100-sample camera capture and independent Y/heading
measurements, both cameras open. Existing Stop Hold Confirmation layout/logging suffices.
Physical 0dca X is +3/0 cm left/right, Y -2 cm; fused return +2.447/+0.044 cm, yaw -1.415 deg.
Do not equate bumper-corner midpoint with robot-center translation under rotation.

## 2026-09-27 stop-hold comparison passed; residual approach motion needs physical measurements

All five retest logs contain `ConfiguredVelocityEscapeConfirmSeconds=0.08`, complete normally,
and finish with `FinishQualified=true`. H4 `12b2`/`0b40` took 3.259/3.506 s overall and
0.707/1.041 s in precision, with zero hold releases and only 0.080/0.052 s from first hold to
finish. This improves the comparable pre-battery-change H4 baseline (4.959/2.638 s, five releases).
Keep the 80 ms confirmation policy; do not increase it or widen tolerances from these results.

After the battery replacement, H3 `9771` took 3.412 s overall and 1.103 s precision. One immediate
pose escape occurred at 6.268 cm translation error, not speed escape. Fused pose moved about
3.3 cm X/7.7 cm Y between first hold and release; Camera0 also moved, while Camera1's last
accepted pose did not update across those endpoints. This does not prove how much was physical
motion versus localization correction. H5 `58bc` crossed wires and is excluded from accuracy
comparisons; it took 7.000/0.906 s, zero hold releases. Clean H5 `6356` took 7.448/1.307 s,
zero hold releases, and only 0.061 s from first hold to finish. Its fused return delta was
(+1.54,+3.27) cm and +0.18 deg; independent physical measurements were not supplied.

Remaining H5 motion occurs before the first qualified hold or while wheels decelerate after
command completion, not repeated zero-hold cancellation. Strict WheelsStopped uses 0.02 m/s
maximum module speed and was first true 0.443 s after clean H5 command end. Module target speeds
were zero during that post-command tail. Do not retune PD/steer/vision from visual jitter alone.
Next: H3 twice and H5 twice, clear wires, both cameras open, precise X/Y measurements and separate
logs. No more logging or cameras required. Existing September 27 layout suffices. Retest results
are appended to STOP_HOLD_BASELINE_2026-09-27.md. No motor/controller code changed in this review.

## 2026-09-27 stop-hold persistence implemented; physical validation pending

Baseline logs: H3 `83fd` completed in 4.355 s with seven hold releases; H4 `5a1b`
completed in 4.959 s with five releases; H5 `76a2` completed in 7.624 s with two releases.
Many velocity escape excursions lasted 20..70 ms, but sustained 100..233 ms excursions and
real pose escapes also occurred. Implemented an 80 ms continuous velocity-escape confirmation
while holding zero, preserve immediate pose escapes, and prevent successful completion until
the current tight pose/speed checks pass and no velocity confirmation is pending. Do not
increase tolerances or assume all reported motion is sensor noise. No build/deploy from Codex.

Added pure regression cases for short/sustained excursions, reset across clear samples/inactive
holds, and success qualification. Added primed pending/confirmed/elapsed escape telemetry and
`FinishQualified`, and made timeout reporting use the full qualification rather than timer age.
Updated the operator and functional decision guides. Static diff/delimiter checks and JSON/path
validation passed; Java tests were added but not run per mentor instruction. New layout:
`C:\MechaRAMS\temp\AdvantageScope 9-27-2026 - Stop Hold Confirmation.json` (live and saved-log tabs).
Next: manually deploy, H4 twice, H3 once, H5 once from original 1A marks with both cameras open;
finalize one log per run and measure both front-corner X displacements, Y, and final angle.

Stationary pair `fef2`/`e3b0` completed 100 samples per camera. Camera0 Y delta was 0.75056 m;
Camera1 was 0.70985 m. The actual ruler displacement has not been independently supplied, so
do not change camera transforms or lateral scale. Full baseline is in STOP_HOLD_BASELINE_2026-09-27.md.

## 2026-09-07 next test series published on SmartDashboard

Added disabled-only `NEXT 1A` and `NEXT 1B` commands for the two-position static Y test. Each command
selects baseline PnP/isotropic vision, labels the capture as start or `+0.75 m Y`, records fused and
fresh trusted request poses, and resets/starts the existing 100-sample per-camera jitter capture.
Also published the ordered sequence and expected relative H3/H4/H5 endpoints as dashboard strings.
The moving tests remain the existing autonomous chooser commands and continue to generate all
targets from the fresh measured start. No trajectory, controller, vision-filter, or finish behavior
changed. Per mentor instruction, do not build, compile, test, simulate, or deploy from Codex.

## 2026-09-07 H2 aligned-handoff validation passed (`c6eb`, `19fd`)

Both post-`77cf406` H2 runs confirm the final-approach gate. Handoff occurred at `0.2649 m` and
`0.2842 m` remaining instead of the prior `0.54 m`. At handoff, X cross-track error was about
`0.8 cm` in `c6eb` and `4.7 cm` in `19fd`, both inside the configured `5 cm` gate. DriveToPose time
fell from `1.718 s` in `4df4` to `1.088/1.028 s`; total command time fell from `3.997 s` to
`3.495/3.285 s`. No visible forward overrun remained in `c6eb`. Keep the aligned-handoff code.

Physical X was `1.50/1.52 m` and `1.51/1.505 m`. Updated approximate physical Y was `0.77 m` and
`0.81 m` versus the `0.75 m` target. Fused Y changes were only `0.7144/0.7320 m`; Camera0 reported
`0.7075/0.7163 m` and Camera1 `0.7225/0.7518 m`. Because the physical Y values remain approximate,
do not tune lateral scale yet. Next isolate localization with two disabled stationary captures at
known positions exactly `0.75 m` apart in Y while holding X and yaw fixed. The small final settling
in both runs was caused by yaw-rate escape from zero hold, not large translation error. Per mentor
instruction, do not build, compile, test, simulate, or deploy from Codex.

## 2026-09-07 holonomic handoff now requires an aligned final approach

The command composition does not intentionally run PathPlanner after DriveToPose begins:
`coarse.until(condition).andThen(precision)` transfers ownership at the command-scheduler boundary,
and DriveToPose initializes its profiles from measured field velocity. The apparent delayed
correction in `4df4` came from entering the old radial band before H2 completed its turn, not from
both algorithms commanding the drivetrain together.

In addition to H2's new `0.30 m` distance, all holonomic spatial handoffs now require the measured
robot to have entered the final straight, remain within `0.05 m` cross-track of it, and move within
30 degrees of the final-straight direction. Speeds at or below `0.10 m/s` bypass the direction check
because direction is then insignificant. If the gate never becomes true, normal PathPlanner
completion still transfers to DriveToPose for final pose qualification. No speeds, gains, route
geometry, camera policy, or finish tolerances changed. Per mentor instruction, do not build,
compile, test, simulate, or deploy from Codex.

## 2026-09-07 H2 `4df4` delayed-handoff correction

H2 log `4df4` physically finished at `1.53/1.52 m` X with an approximate `0.87 m` Y measurement and
little settling. The fused estimator finished at `+1.5108 m X`, `+0.7513 m Y`, and `-0.64 deg`, so
DriveToPose considered the `(+1.50, +0.75, 0 deg)` target reached. Camera0 and Camera1 observed
approximately `+0.7466/+0.7237 m` Y change; the imprecise physical Y value is not grounds for tuning.

The reported forward overshoot followed by backward motion during strafe is confirmed. At the
`0.54 m` spatial handoff, actual pose was `9.4 cm` beyond PathPlanner's target X and `11.7 cm` short
of target Y; peak X overshoot reached `10.8 cm`. H2's handoff threshold is therefore reduced from
`0.55 m` to `0.30 m`, allowing PathPlanner to own more of the lateral leg and correct its own corner
tracking error. All other holonomic handoff distances, speeds, gains, tolerances, route geometry,
and vision settings remain unchanged. Retest H2 twice with precise X/Y measurements at both frame
corners. Per mentor instruction, do not build, compile, test, simulate, or deploy from Codex.

## 2026-09-07 H5 physical return passed; H2 lateral measurement needs precision

H5 log `e073` returned to `+2.0 cm` X at both front corners with no measurable Y offset. Its unusual
slow-start/sudden-acceleration event was a scheduler stall, not path geometry: `FullCycleMS` reached
`471.1 ms` about `0.29 s` after outbound motion began, `Vision/Timing/Camera0IoUpdateMs` consumed
`226.8 ms`, and total Vision periodic time was `244.0 ms`. Camera1 accumulated eight unread results
while the main loop was blocked. The final DriveToPose phase then took `1.602 s` with one speed-driven
hold release. Among the twelve recent holonomic logs checked, this was the only comparable in-motion
camera-read stall; do not redesign camera threading from one event, but treat another occurrence as
a runtime-priority defect.

H2 log `c79e` finished at `1.5275 m` average physical X versus the `1.50 m` target. The physical Y
estimate was approximately `0.90 m` versus the commanded `0.75 m`, but it was explicitly approximate.
The fused pose reported `+1.5111 m X`, `+0.7469 m Y`, and `+0.45 deg`; Camera0's last accepted pose
changed `+0.7169 m Y` and Camera1's changed `+0.8170 m Y`. The controller therefore believed it met
the Y target. Do not tune lateral control from the approximate ruler value. Repeat H2 with marked,
perpendicular floor references and measure Y at both frame corners. Per mentor instruction, do not
build, compile, test, simulate, or deploy from Codex.

## 2026-09-07 H5 rollback confirmed; physical lateral error exposed

The post-rollback H5 logs `d6d6` and `016d` were repeatable: total command times were `6.713 s` and
`6.878 s`, and final DriveToPose times were `0.773 s` and `0.795 s`. Log `d6d6` entered hold once
without escaping. Log `016d` had one roughly `23 ms` speed-driven hold release but no visible
settling. The front-corner X averages were about `-1.07 cm` and `-3.25 cm`, so both passed the
existing 5 cm return criterion. Keep the reverted `PRECISE` yaw behavior and the H5-only `0.70 m/s`
final return zone.

H5 is relative in both X and Y. `createHolonomicTestTargets` copies the fresh measured start
translation, and `OUT_AND_RETURN` selects that exact start pose as the final target. In `016d` the
target Y was `1.9969 m`; the logged fused finish was within roughly `0.4-1.9 cm` of it depending on
the end-of-cycle sample, while the physical measurement reported `+7.5 cm`. The final accepted camera
poses straddled the target: Camera0 was about `+2.8 cm` in Y and Camera1 about `-2.7 cm`, whose simple
average is essentially the target. Earlier `+4.8/+3.3 cm` last-pose deltas used asynchronous samples
that did not equal the reset target and must not be interpreted as camera consensus on displacement.
The physical offset is therefore not commanded and was not seen by localization; it exposes a
lateral localization/measurement discrepancy. Do not change motion control from this single Y
measurement. Repeat a controlled physical Y measurement and run a measured-Y H2 validation next.
Per mentor instruction, do not build, compile, test, simulate, or deploy from Codex.

## 2026-09-07 H5 yaw-deadband experiment rejected and rolled back

The isolated `RELAXED_WITH_DEADBAND` experiment was not repeatable. H5 log `53f6` took `8.475 s`
overall and `2.348 s` in DriveToPose. The new suppression switched on five times and off four times;
it first disabled yaw control immediately at the roughly `0.53 m` handoff, then repeatedly restored
correction as yaw rate crossed the `12 deg/s` boundary. That produced the reported forceful settling.
H5 log `827f` happened to finish very well (`6.791 s` overall, `0.819 s` in DriveToPose), but the
run-to-run split proves the logic is not a reliable improvement. Both physical finishes were within
about `1.5 cm` of the start by the front-corner average.

H4 log `6ffd` used `PRECISE`, never used the H5 deadband, and therefore is a separate result. It took
`4.317 s` overall and `1.946 s` in DriveToPose, entering hold three times as measured motion escaped
the velocity limits. At handoff it still had about `14 deg` of yaw error and was turning about
`49 deg/s`; this is the next H4 handoff condition to investigate, not evidence that the H5-only
change altered H4.

Revert the deadband experiment and restore H5 to its previously tested `PRECISE` yaw. Preserve the `1.2/1.2`
route profile, H5-only `0.70 m/s` final return zone, `0.55 m` spatial handoff, controller gains,
settling safety gates, and vision policy. Re-run H5 twice before attempting a separately isolated
H4 handoff change. Per mentor instruction, do not build, compile, test, simulate, or deploy from
Codex.

## 2026-09-07 faster holonomic profile passed; return finish isolated

Analyzed the `1.2 m/s`, `1.2 m/s^2` runs `1b05`, `0e7b`, and `d2a8`. Total command durations were
`2.982 s`, `3.045 s`, and `7.572 s`, improvements of about `20%`, `29%`, and `19%` over the matching
slow-profile baselines. Holonomic 2 and 4 had no intermediate settling and their final DriveToPose
phases were only `0.745 s` and `0.656 s`. Holonomic 4 ended at `-20.32 deg` fused yaw.

Holonomic 5 remained accurate by ruler (front-corner average `-0.25 cm`) but DriveToPose took
`2.066 s`. It entered above `1.1 m/s`, entered the pose window six times, and entered zero hold three
times. Both hold releases were speed-driven while pose remained inside the escape envelope: one at
`21.7 deg/s`, the other at `0.203 m/s` and `17.5 deg/s`. The gyro signal was valid and the cameras
had no rejected frames, so do not weaken the safety escape gate or retune camera validity from this
run.

Keep the global holonomic profile at `1.2/1.2`. On only the final straight segment of the return path,
apply a `0.70 m/s` PathPlanner constraints zone and use a matching `0.70 m/s` planned handoff speed.
Keep the `0.55 m` handoff distance, DriveToPose gains/tolerances, vision policy, and all one-way
routes unchanged. Re-run Holonomic 5 first; use Holonomic 2 only as a regression check after it
passes. Per mentor instruction, do not build, compile, test, simulate, or deploy from Codex.

## 2026-09-06 continuous holonomic baseline passed; speed stage prepared

Analyzed the post-`58c45db` continuous-route logs `ec9d`, `a06b`, and `86e3`. Holonomic 2 had no
observable settling and ended with about `+3.5 cm` physical forward error. Holonomic 4 had no middle
settling; the two front-corner measurements average `2.2425 m` versus the intended `2.25 m` center-X
travel, and its unequal corner distances are consistent with the commanded `-20 deg` final yaw.
Holonomic 5 had only final settling and returned to about `+0.5 cm` center-X by the corner average.
The fused final translation errors were about `2.96 cm`, `4.34 cm`, and `2.75 cm`, respectively.

The continuous geometry therefore passes its deliberately slow baseline. Raise only the main
PathPlanner profile from `0.8 m/s`, `0.8 m/s^2` to `1.2 m/s`, `1.2 m/s^2`. Preserve the proven
`0.80 m/s` planned handoff speed, `0.55 m` handoff distance, final DriveToPose gains/tolerances, route
geometry, and vision settings. The next comparison is Holonomic 2, 4, and 5 at the faster profile;
physically measure Y at least once because fused-pose agreement is not independent ground truth.
Per mentor instruction, do not build, compile, test, simulate, or deploy from Codex.

## 2026-09-06 continuous holonomic route correction complete

The post-`e5a5c1b` runs `7206`, `6106`, and `1c6f` reduced precision-controller settling but still
paused or slowed at each geometric part boundary. Code inspection confirmed why: each part remained
a separate `PathPlannerPath` with a zero-speed `GoalEndState`; only DriveToPose had been removed from
the intermediate boundary. Replace each one-way multipart test with one rounded continuous
PathPlanner path and use the established spatial handoff to DriveToPose near the final destination.
Out-and-return requires two continuous paths because it must physically stop and reverse at its far
point, but it must not stop at the internal straight/diagonal junctions. Keep the `0.8 m/s`,
`0.8 m/s²` constraints for the first geometry/handoff verification. Per mentor instruction, do not
build, compile, test, simulate, or deploy from Codex.

Implemented `0.30 m` rounded corners inside the existing safe envelope. Holonomic 1–4 now build one
continuous path. Holonomic 5 builds one continuous outbound and one continuous return path, with its
only expected intermediate stop at the far reversal. The final PathPlanner path retains a nonzero
`0.80 m/s` goal-end speed and uses `DriveToPosePrecisionCommand.handoffFrom(...)` at `0.55 m` from
the final target. A `1.00 m` arming distance prevents the return-to-start route from triggering at
time zero. Added path-count, expected-stop, continuous-geometry, and handoff-state telemetry.

## 2026-09-06 first holonomic ladder analyzed; final-only precision implemented

Analyzed `0a8f`, `48aa`, `85b2`, `54ea`, and `bb4c`. The approximately `2.93 s` PathPlanner time for
each `1.50 m` forward leg matched the intentionally low `0.8 m/s`, `0.8 m/s²` profile. The avoidable
delay came from running DriveToPose at intermediate endpoints: `48aa` spent about `1.50 s` at its
forward midpoint, `54ea` about `1.90 s`, and `bb4c` about `1.59 s` across intermediate precision
phases. The `48aa` module targets had eight changes greater than 120 degrees, but six occurred with
at least one wheel below `0.05 m/s`; this is low-speed direction/optimization chatter, not evidence
that a wheel selected a long turn under normal travel speed.

Changed multipart holonomic routes so PathPlanner owns every intermediate segment and DriveToPose
runs only once, after the final segment. PathPlanner still requests zero planned speed at sharp
segment boundaries. Retained `0.8 m/s`, `0.8 m/s²` for the first one-variable comparison. There is no
robot-speed gate in the vision acceptance policy and enabled vision heading fusion is off, so after
Holonomic 2/4/5 confirm the handoff change, the next isolated test may raise holonomic limits to
`1.2 m/s`, `1.2 m/s²`. Per mentor instruction, do not build, compile, test, simulate, or deploy from
Codex.

## 2026-09-06 current-start holonomic and return-test implementation complete

Implement the accepted next test stage without changing the validated straight-drive or
spatial-handoff tuning: add conservative current-start holonomic autos for forward entry, left
strafe, diagonal motion, a camera-facing yaw sweep, and an out-and-return sequence. Every route must
require a fresh trusted MultiTag start, normalize only the known squared starting yaw, validate all
generated targets against the measured practice area, and log the selected phase and targets. Add
disabled-only PnP/TrigSolve selection controls and schedule PathPlanner's official no-output warmup
command during disabled startup to reduce first-use stalls on the roboRIO 1. Keep the two front
cameras enabled and defer X-wheel braking so the first holonomic baseline changes only motion
geometry. Update the code-facing documentation and provide an ordered physical test plan. Per mentor
instruction, do not build, compile, test, simulate, or deploy from Codex.

Implemented the five current-start chooser entries, official no-output PathPlanner warmup, complete
generated-route preflight gate, disabled-only static localization controls, route/phase telemetry,
pure target-safety checks, and the out-and-return-to-start test. Added the matching September 6
AdvantageScope layout at `C:\MechaRAMS\temp\AdvantageScope 9-6-2026 - Holonomic And Return Test.json`
without replacing an existing layout. Updated the operator, test, architecture, configuration, and
functional-handoff references. Static verification covered whitespace, balanced Java delimiters,
layout JSON parsing, visualization JavaScript syntax, chooser removal/addition, and telemetry-key
coverage. Per mentor instruction, no build, compile, unit test, simulation, or deployment was run.

## 2026-09-06 `89ad` unchanged spatial-handoff confirmation passed

The unchanged `0.70` rotation-damping confirmation, log `319589ad`, physically traveled
`2.015/2.000 m` at the left/right frame corners (`2.0075 m` average) with approximately `1.4 deg`
clockwise ruler skew and almost no visible settling. DriveToPose was active for `0.881 s`, reached
the full pose-and-velocity qualification at `+0.651 s`, entered the zero-output settling hold once,
never escaped it, and the wheels met the strict stopped threshold `0.050 s` after command finish.
After first translation qualification, logged yaw error stayed below `0.60 deg` and Pigeon rate below
`6.01 deg/s`. There was no timeout. This independently confirms the large improvement from `e974`;
retain rotation damping `0.70` and freeze the current spatial-handoff controller constants.

Do not interpret the `0.230 s` hold-to-finish timestamp interval as corrective settling. The cycle
immediately after hold entry took `207.1 ms` (`180.4 ms` user code, `26.7 ms` log periodic), and the
next controller interval was `219.4 ms`. The WPILib console attributed the surrounding overruns to a
command-scheduler overrun followed by `robotPeriodic`/SmartDashboard tracing. The hold remained
latched and the requested drivetrain velocity stayed zero throughout. Treat this as a separate,
intermittent loop-timing issue, not a reason to alter the successful motion gains.

The measured-start auto is field-targeted rather than a fixed 2 m displacement: this run logged a
normalized start X of `2.184732 m` and therefore `2.065268 m` expected travel to x=`4.25 m`. It ended
at fused x approximately `4.225 m` (about `2.5 cm` inside the target), while the ruler measured
`2.0075 m`. Preserve both numbers when comparing localization accuracy; the nominal 2 m ruler result
alone does not test the camera-derived starting pose. No source behavior changed and no build,
compile, test, simulation, or deployment was performed.

## 2026-09-05 Localization decision map complete

Create a generic interactive localization companion and GitHub decision trees from VisionIOPhotonVision,
VisionPolicy, Vision, and drivetrain integration. Show individual rejection gates, timing suppression,
solver/weight fallbacks, heading eligibility, and separate seed/jitter operations. Documentation only.

Added 29-box localization-decision-map.html with four navigable stages and complete definitions for
its listed terms. LOCALIZATION_DECISION_MAP.md mirrors the decisions with GitHub Mermaid and
expandable explanations. Branch targets and every node selection were checked with a DOM stub;
JavaScript syntax checked without building or running robot code. Clarified that innovation is
diagnostic, connection is not a rejection gate, and jitter does not automatically retune trust.

## 2026-09-05 Selected-box glossary definitions

User reported that selected-box terms appeared without meanings. Replaced partial dictionary and
bare label chips with a complete dictionary for every currently referenced term and readable
term/definition cards. Documentation UI only; no robot behavior changes. Definition coverage and
JavaScript syntax are checked before committing.

## 2026-09-04 separate generic navigation from test configuration

User found fixed practice-field distances confusing in the generic documentation. Moved the existing
numerical walkthrough to VISIONTEST_CONFIGURATION_EXAMPLE.md and rewrote FUNCTIONAL_ALGORITHM_HANDOFFS.md
as a generic drive-to-stop guide. Removed all numerical example panels from the interactive map;
the GitHub map now points to the separate reference. Real log paths remain implementation identifiers,
not generic-route requirements. Targets and handoff conditions are route responsibilities; many
controller parameters remain shared constants. Passing-waypoint and automatic braking-distance
handoff behavior are explicitly not claimed as implemented. No robot code or behavior changed.

Checked interactive JavaScript syntax and node/detail coverage, and updated the maintenance rule to
keep test-specific values out of the generic explanations.

## 2026-09-04 GitHub presentation polish complete

Prepare the existing generic decision map as a teammate-facing GitHub page with navigation,
expandable explanations, and clear instructions distinguishing GitHub Markdown from the local
interactive HTML. Documentation only; preserve current robot testing behavior.

Added a teammate-facing introduction, color legend, section navigation, eight expandable sections,
and a prominent README entry. Existing source-backed diagrams remain intact. The Markdown is the
direct GitHub share target; the HTML download remains the richer offline interactive version.
No robot code or tests were run or changed.

## 2026-09-04 generic decision map expansion complete

Documentation task: replace route-specific visual conditions with configurable generic conditions,
retain present VisionTest numbers only as labeled examples, and expose the smaller behavior-changing
mechanisms inside DriveToPose as individual selectable boxes. Source review confirms distance-scaled
translation damping and continuously applied rotational damping during active correction, profile
timing catch-up, separate translation/rotation clamps, four settle gates, and hold/escape behavior.
Robot testing continues separately; this task edits explanatory documentation only.

The interactive map now has 34 selectable boxes, including an expanded last-leg decision section.
The GitHub Mermaid companion has separate ownership and internal-mechanism diagrams. Shared
constants are distinguished from route settings; automatic speed-aware handoff and passing-waypoint
behavior are not claimed as implemented. JavaScript syntax and all 34 detail selections were checked
without building or running robot code. Future behavior changes must keep these views and the
functional guide synchronized.

## 2026-09-04 interactive algorithm decision map complete

Created `algorithm-decision-map.html`, a self-contained, responsive presentation of the current
localization and autonomous control sequence. It separates the always-running localization layer
from drivetrain ownership, uses stable colors for localization, PathPlanner, DriveToPose, decisions,
safety stops, and outputs, and presents the spatial handoff as explicit yes/no branches. Selecting
any of 22 nodes updates a side panel with the node's purpose, IF/THEN behavior, effect, terminology,
and source-verified AdvantageKit log paths. A narrow-screen view replaces the large SVG tree with a
vertical selectable sequence.

Created `ALGORITHM_DECISION_MAP.md` as the noninteractive GitHub version using a color-coded Mermaid
flowchart. Linked both views from the documentation index and the functional algorithm guide. Added a
repository maintenance rule requiring all three representations to stay synchronized when algorithm,
handoff, tolerance, fallback, or chooser behavior changes. JavaScript syntax, interactive-node detail
coverage, and desktop browser rendering were checked. This was documentation only; no robot code,
behavior, constants, build, compile, test, simulation, or deployment changed.

## 2026-09-04 `107d` rotation-damping validation passed

The first spatial-handoff run with rotation damping `0.70`, log `0a46107d`, physically traveled
`2.045/2.045 m`: the two frame corners matched, so no yaw drift was measurable by ruler. The logged
current-start X distance was `2.0056 m`, making physical forward error about `+3.9 cm`. DriveToPose
was active for `0.803 s`, reached the 4 cm pose window at `+0.614 s`, and finished `0.189 s` later.
From first wheel motion through command finish the run was `1.897 s`, so that tail was approximately
`9.97%` of total motion time and meets the team's 10% target. Compared with `e974`, post-arrival peak
yaw error fell from `3.46` to `0.45 deg`, peak Pigeon rate fell from `27.48` to `3.68 deg/s`, and
DriveToPose time fell from `2.219` to `0.803 s`.

The hold entered twice and exited once, but the exit was a one-cycle translation-rate event
(`0.204 m/s` versus the `0.18 m/s` escape gate), not angular ringing; it immediately re-entered and
produced no visible settling. Retain rotation damping `0.70` and all current safety gates. One
unchanged confirmation run is appropriate before considering this spatial-handoff tuning closed.
No code changed and no build, compile, test, simulation, or deployment was performed during analysis.

## 2026-09-04 `e974` rotational-settling tuning implemented

The measured-start spatial-handoff run `f346e974` traveled `2.000 m` at the left frame corner and
`1.975 m` at the right, so mean forward distance was accurate to about `-1.25 cm`, but visible
settling remained at least `0.5 s`. Direct log analysis found that DriveToPose reached its `4 cm`
translation window `0.672 s` after handoff but did not finish until `1.547 s` later. The yaw error
peaked at `3.46 deg` and Pigeon rate at `27.48 deg/s` after translation first qualified; the settling
hold entered twice and exited once because angular speed crossed the `12 deg/s` escape threshold.
The Pigeon signal was valid during the drive. The next one-variable A/B doubles the existing
gyro-based rotation damping from `0.35` to `0.70`. Translation gains, theta Kp, tolerances, handoff
geometry, speed, acceleration, and vision policy are unchanged. The existing Spatial Handoff Timing
Retest layout already contains the required channels; manually build/deploy and repeat exactly one
`VisionTest (spatial handoff)` with both cameras open. No build, compile, test, simulation, or
deployment was performed here.

## 2026-09-04 plain-English algorithm handoff guide complete

Created `FUNCTIONAL_ALGORITHM_HANDOFFS.md`, a new living Markdown document for management,
build-team members, and high-school students. It explains the current localization, trajectory,
handoff, final-pose, settling, aiming, and safety logic functionally rather than mathematically, using
explicit IF/THEN rules. It corrects the common
misunderstanding that final angle correction stops based only on angular speed: the current code
enters a zero-command settling hold only when position, heading, translation speed, and rotation speed
all qualify, and resumes if a wider pose or speed escape threshold is crossed. Exact current
thresholds are included in plain language, and experimental algorithms are clearly marked. The guide
is linked from the documentation index and the older conceptual student guide. Its maintenance
checklist and change history explicitly require future algorithm/threshold/handoff changes to update
it. This was documentation only; no robot behavior changed and no build, compile, test, simulation,
or deployment was performed.

## 2026-09-04 `0586` handoff overrun correction implemented

The first `1.4 m/s` spatial-handoff run was smooth through the transition but physically traveled
`2.213/2.310 m`. Log analysis found a deterministic software stall, not an unnoticed position error:
`StartAccepted` occurred at `109.115218 s`, spatial handoff initialized precision control at
`110.736989 s` and robot x=`3.315 m`, then the next control cycle did not arrive for about `0.469 s`.
The robot coasted at approximately `1.56 m/s` to near x=`4.154 m` before precision output could brake.
The same handoff cycle logged `FullCycleMS=468.016`, `UserCodeMS=461.282`, and the WPILib tracer
identified `SequentialCommandGroup.execute()=406.691 ms`.

That cycle also registered 22 `DriveToPose` AdvantageKit channels for the first time; the first
execute registered 69 more and produced additional `88-96 ms` cycles. Source now pre-registers all 92
existing DriveToPose output types once during `RobotContainer` construction, after `Logger.start()`
and before the robot can move. This deliberately pays the one-time schema/NetworkTables cost at safe
startup rather than at a high-speed handoff and sends no drivetrain request. The two priming
diagnostics bring the registered total to 94:
`DriveToPose/Controller/TelemetrySchemaPrimed` and
`DriveToPose/Controller/TelemetryPrimeDurationMilliseconds`.

The `1.4 m/s` goal-end speed, handoff/target geometry, constraints, gains, tolerances, vision, and
direct-distance behavior remain unchanged. Re-test once using
`C:\MechaRAMS\temp\AdvantageScope 9-4-2026 - Spatial Handoff Timing Retest.json`; require the primed
flag before enabling, then record both front-corner distances and the wpilog. The handoff should no
longer have a hundreds-of-milliseconds scheduler gap; target under `60 ms`, and stop if it remains
above `100 ms`. Per mentor instruction, no build, compile, test, simulation, or deploy was performed.

## 2026-09-02 `cb20` handoff-velocity correction implemented

The accepted isolated `cb20` recommendation is now in source. The generated straight path requests a
`1.4 m/s` `GoalEndState` only for `SPATIAL_HANDOFF`, preventing PathPlanner from braking toward zero
for the unused x=`3.6 m` endpoint before the x=`3.3 m` interruption. Coarse-only and sequential modes
still request zero endpoint speed. The selected value is logged at
`PathPlanner/VisionTest/CoarseGoalEndVelocityMetersPerSecond` for deployed-artifact verification.

No handoff location, final target, constraints, PID/damping, tolerances, vision behavior, or direct
distance auto changed. The separate
`C:\MechaRAMS\temp\AdvantageScope 9-2-2026 - Spatial Handoff Velocity Retest.json` layout adds the
value to its graph and saved/live tables without overwriting the prior layout.
The first re-test is one spatial-handoff run from the same squared start, recording both front-corner
ruler distances and the wpilog. Confirm the value is `1.4`, the preflight is ready, motion begins
forward, and the module target/measured speed no longer makes a down/up valley at the handoff. Per
mentor instruction, no build, compile, test, simulation, or deployment was performed.

## 2026-09-02 `cb20`: measured-start spatial handoff is accurate; zero-speed path end causes mid-run dip

Analyzed `akit_rotated_1788394878528_adbccb20.wpilog`. The measured MultiTag robot start was
`(2.2416, 2.0811, 1.799 degrees)` and the auto correctly normalized only yaw to zero. It logged
`ExpectedTotalTravelMeters=2.00836`, `StartAccepted=true`, and final camera-to-board X distance
`1.598 m`. The ruler result was `1.995/2.008 m` at the left/right front frame corners: `2.0015 m`
center travel (only `-6.9 mm`, `-0.34%`, versus the camera-derived target) and approximately
`+1.23 degrees` physical CCW yaw. The command ended with logged errors `8.1 mm X`, `22.8 mm Y`,
`24.2 mm translation`, and `-1.217 degrees`, closely agreeing with the physical yaw result.

The visible middle slowdown is confirmed and is not a camera jump. Spatial handoff occurred at
`245.4208 s`, robot x=`3.320 m`, after `1.714 s` of PathPlanner. Because the runtime path still has a
zero-velocity goal at x=`3.6 m`, PathPlanner's maximum module target fell from `1.379 m/s` to
`0.746 m/s` during the final `0.40 s` before handoff; measured maximum module speed fell from
`1.430 m/s` to `0.950 m/s`. The precision controller then had `0.905 m` remaining and accelerated its
target back to `1.606 m/s` within `0.30 s`; measured maximum module speed rose to `1.633 m/s`. This is
a planned velocity valley caused by treating an intentionally interrupted coarse path as if it must
stop at its unused endpoint.

The final stable at-goal hold was about `0.061 s` against a configured `0.050 s`, so the operator's
low-settling observation is accurate. Precision control ran `2.023 s`; earlier pose/velocity window
entries were transient, but it finished without timeout at `0.0242 m`, `0.0197 m/s`, and
`0.52 degrees/s`. Wheels reached the strict stopped threshold about `0.19 s` after command end.

There is also a separate startup latency: autonomous initialization produced `295.9 ms` and `174.4 ms`
user-loop cycles, and wheels did not exceed the strict stopped threshold until about `0.495 s` after
the start was accepted. Mid-path cycles also reached `55-61 ms`. Do not attribute these timing gaps to
the spatial handoff velocity profile; preserve them as a later runtime-performance investigation.

Recommended isolated next change, subsequently accepted and implemented as recorded above: for
`SPATIAL_HANDOFF` only, give the coarse PathPlanner path a nonzero goal-end velocity around the
already observed `1.4 m/s` cruise speed. The
precision controller initializes from measured velocity and has `0.905 m` plus `2.5 m/s^2` available
to stop, so it can accept that velocity without changing the target or handoff position. Keep
coarse-only/sequential end velocity zero, and keep all gains, vision settings, tolerances, target poses,
and the x=`3.3 m` handoff unchanged for the A/B run. No source behavior, build, compile, test,
simulation, or deploy was performed in this analysis.

## 2026-09-02 spatial-handoff reverse incident corrected with a measured-start path

Follow-up complete: the first live pre-run view did not contain
`PathPlanner/VisionTest/StartAccepted` because that output was created only when the deferred auto
initialized. It is now initialized at startup as `false` with `AbortReason=NOT_RUN`. A continuously
logged disabled-state `PathPlanner/VisionTest/Preflight/*` group exposes fresh MultiTag availability,
safe robot-center start, `ReadyToEnable`, pose, board distance, and expected travel before motion. The
updated AdvantageScope layout uses `/AdvantageKit/RealOutputs/...` for its live line graph rather than
the saved-log `/RealOutputs/...` namespace. No build, compile, test, simulation, or deploy was run.

On the first real `VisionTest (spatial handoff)` attempt, the robot drove backward into an obstacle
before proceeding forward. The driver station was on blue, and `DriveSubsystem` configures
PathPlanner with `shouldFlipPath = () -> false`, so alliance flipping was not the cause. The chooser
previously built an absolute `.auto` with `resetOdom=true`, fixed start `(1.5, 2.0, 0 degrees)`, and a
fixed-time path. PhotonVision subsequently showed field-to-camera X `2.399 m`; with the measured
robot-to-camera X offset `0.152 m`, robot-center X is approximately `2.247 m`. An absolute reset to
`1.5 m` followed by fresh vision correction therefore put the fused robot about `0.75 m` ahead of the
time-parameterized path and commanded the observed backward correction.

The straight `VisionTest` chooser variants now require a fresh trusted MultiTag robot pose, normalize
only its yaw to zero, and generate an on-the-fly PathPlanner path from that actual translation toward
the existing `(3.6, 2.0)` coarse endpoint. Spatial handoff remains at x=`3.3 m` and the precision target
remains `(4.25, 2.0, 0 degrees)`. At that target the front camera lenses are about `1.60 m` in X from
the tag plane, where both tags remain visible. The start must be within x=`1.2..2.6 m`,
y=`1.5..2.5 m`, and absolute yaw <=`15 degrees`; otherwise it stops and reports an explicit abort
without moving. The PhotonVision dashboard value is field-to-camera pose, while this gate and path use
robot-center pose. The board-distance diagnostic now correctly uses the tag plane at x=`6.0 m`, not
field length `8.0 m`. The fixed `.auto` remains only as a PathPlanner editing reference, and the curved
test remains the separate absolute-path test. The validated direct-distance controller, gains, and
vision weights are unchanged. Per mentor instruction, no build, compile, test, simulation, or
deployment was performed.

## 2026-09-02 `1bd6`: velocity-safe 1 m regression passes with a yaw caveat

`akit_rotated_1788391992562_e5651bd6.wpilog` physically traveled `1.005/1.027 m` at the left/right
frame corners: `1.016 m` center and about `+2.08 degrees` counterclockwise across `0.6072 m`. This is
materially better than `0b06` (`1.0225 m`, `+4.24 degrees`). The command lasted `1.4637 s`. It first
entered the complete pose window at +`1.0802 s`, briefly left it during a yaw excursion to about
`2.76 degrees`, then made its final stable pose entry at +`1.3552 s`. It qualified and latched once at
+`1.3954 s`, never escaped, and ended after `0.0683 s` of hold. The final-stable-entry tail was
`0.1085 s`, or `7.41%` of command time, which passes the established <10% metric; the longer
first-entry-to-end interval was `0.3835 s` and documents the remaining transient rather than hiding it.

At command end, fused error was `0.01784 m / 0.851 degrees`, measured translation speed was
`0.0605 m/s`, Pigeon rate was `2.17 deg/s`, and max module speed was `0.137 m/s`. Strict
`WheelsStopped` first asserted `0.100 s` after command end. `AtGoalEntryCount=1` and
`SettlingHoldExitCount=0`; the new velocity escape was active during high-speed correction but did not
need to release the final hold. Peak terminal Pigeon rates were about `+16.7/-19.9 deg/s`, and one
controller interval reached `91.5 ms`; final profile-clock lag was only `-16.3 ms`.

Accept this as the direct 1 m validation with a documented approximately 2-degree physical-yaw
caveat. Do not retune PID, damping, motion constraints, yaw tolerance, or the escape limits from this
single run. No source behavior changed. Static log analysis only was performed; no build, compile,
test, simulation, deploy, or push was performed. The next useful test is the real PathPlanner-to-
precision spatial handoff, not another direct-distance tuning change.

## 2026-09-02 `0b06`: settling latch can finish while motion is increasing

The 1 m regression `akit_rotated_1788391044418_48d30b06.wpilog` looked visually fast, but the
left/right frame-corner measurements (`1.000/1.045 m`) imply about `+4.24 degrees` counterclockwise
yaw across the measured `0.6072 m` frame width. The command lasted `1.214 s`; it first met the
pose+velocity gate at +`1.152 s` and completed the 0.05 s settling hold at +`1.214 s`. However, after
the hold latched, Pigeon yaw rate rose from `7.73 deg/s` to `21.39 deg/s`, and the command ended at
`19.11 deg/s` with max module speed `0.409 m/s`. Fused heading was already `+2.24 degrees` at command
end and continued past `+3.1 degrees` afterward. The existing latch releases only for a widened pose
error, so it incorrectly ignores renewed physical motion inside that pose envelope.

Implemented behavior change: the noise-resistant pose hysteresis remains, but a latched hold now also
abandons settling when measured translation speed exceeds `0.18 m/s` or Pigeon rotation speed exceeds
`12 deg/s`. These limits are 1.5x the unchanged entry limits (`0.12 m/s`, `8 deg/s`), so ordinary
noise remains tolerated while the sustained `0b06` acceleration cannot finish. Pose escape and
velocity escape are logged separately, and the configured velocity limits are logged. Motion-profile
constraints, PID/damping gains, entry tolerances, and yaw modes are unchanged.

The non-overwriting validation layout is `C:\MechaRAMS\Temp\AdvantageScope 9-2-2026 - Settling
Velocity Escape.json`; it contains saved-log and live-table paths plus a graph of rates, module speeds,
hold state, and both escape causes. JSON parsing and exact source-key inspection passed. Static diff
checks passed (line-ending warnings only). Per mentor instruction, no build, compile, test, simulation,
deploy, or push was performed. Next: manually build/deploy and run one fresh-log 1 m validation with
both cameras open.

## 2026-09-02 `0caf`: Pigeon-rate validation passes

`akit_rotated_1788390703391_c2800caf.wpilog` validates the Pigeon yaw-rate correction. Physical travel
was `2.015/1.995 m` at the left/right frame corners: `2.005 m` center, only `+0.005 m` from the 2 m
target. The 2 cm corner difference across `0.6072 m` corresponds to about `-1.89 degrees` clockwise,
which is acceptable for this `RELAXED=1.8 degree` straight-distance test given ruler uncertainty and
the mentor's stated tolerance for roughly 2.2 degrees. Command time fell from `2.808 s` in `4844` to
`1.941 s`, close to the theoretical approximately `1.9 s` motion profile. Peak yaw excursion fell from
`4.97 degrees` to `2.27 degrees`.

The new signal stayed healthy: `GyroYawRateSignalOK=true` and the shared-frame applied rate was
`250 Hz`. Integrated Pigeon rate was `-0.90 degrees`, agreeing with the gyro-owned pose change of
`-1.06 degrees` within `0.16 degrees`; module-kinematic omega still incorrectly integrated
`+7.32 degrees`. The first combined pose entry was at +`1.837 s`, pose+velocity qualified once at
+`1.877 s`, the hold never escaped, and the command ended at +`1.941 s`. The `0.104 s` post-pose-entry
tail was `5.36%` of command time and the actual latched hold was `0.064 s` (`3.30%`), passing the
mentor's <10% settling goal. Final fused error was `0.0332 m / 1.063 degrees`; measured chassis speed
was `0.0224 m/s`, Pigeon rate `2.31 deg/s`, and max module speed `0.0701 m/s`. The strict telemetry-only
`WheelsStopped` threshold asserted `0.316 s` after command end, but the mentor observed almost no
physical settling.

One controller gap reached `89.2 ms` and full cycle reached `98.9 ms`; there were no >100 ms gaps and
final profile-clock lag was only `1.26 ms`. This remains a performance-monitoring item but did not
prevent the pass. Freeze translation gains, damping, profile constraints, gyro-rate source, and yaw
tolerances. No source behavior change, build, compile, test, simulation, deploy, or push was performed
for this analysis. Next run should be one unchanged 1 m regression; if it remains near the prior
`1.274 s` result without visible settling, move on from direct-distance tuning to the real
PathPlanner-to-precision handoff tests.

## 2026-09-02 first gyro-rate deployment: cached signal was not initialized

Before driving, live telemetry correctly exposed `Drive/GyroYawRateSignalOK=false`, a zero yaw rate,
and an applied update frequency of `250 Hz`. Do not use this deployment for the A/B trajectory. The
signal was obtained with Phoenix's `refresh=false` overload and then read only through
`getValue()`, so its local `StatusSignal` cache never received an initial refresh. Change construction
to the refreshing getter and non-blockingly refresh the cached signal whenever the control value is
read. The applied `250 Hz` rate is valid and should be retained: Phoenix reports the fastest request
for signals sharing a status frame, and CTRE's 250 Hz swerve odometry configuration can therefore
raise the applied rate above this code's 100 Hz minimum. Keep the unhealthy-signal fallback.

## 2026-09-02 `4844`: 2 m delay traced to the wrong angular-rate source

`akit_rotated_1788389139668_c4d24844.wpilog` physically traveled `2.045 m` center (left/right frame
corners `2.050/2.040 m`) and ended about `-0.94 degrees` clockwise across the measured `0.6072 m`
frame width. The command lasted `2.808 s`. Translation first reached <=4 cm at +`1.672 s` and then
remained near the target, but gyro-owned pose heading continued to `-4.97 degrees` at +`1.917 s`.
The 1.8-degree combined pose gate therefore did not first pass until +`2.555 s`; pose+velocity
qualified once at +`2.747 s`, and the command completed its clean `0.061 s` hold at +`2.808 s`.
Final fused error was `0.00996 m / 0.682 degrees`. Thus the observed roughly `0.5 s` terminal motion
was heading correction, not X PID settling or an overly long configured hold.

The angular-rate input was invalid for this use. Integrating CTRE's module-kinematic
`SwerveDriveState.Speeds.omega` across the command predicts `+8.09 degrees`, while the gyro-owned pose
actually changed `-0.67 degrees`. During the deceleration yaw excursion, rotational damping could
therefore oppose the correction required by the measured heading. The precision controller now keeps
module-derived vx/vy but uses the Pigeon's mount-corrected Z-world angular velocity for theta-profile
seeding, rotational damping, the angular stop gate, and measured-omega telemetry. The status signal is
explicitly requested at 100 Hz (Phoenix 6's documented CAN-FD default); an unhealthy signal falls back
to kinematic omega. Both rates, their difference, signal health, and applied update frequency are
logged. `RELAXED` remains 1.8 degrees and `PRECISE` remains 1.5 degrees so this isolated test does not
hide the sensor-source defect by widening tolerance.

The new non-overwriting layout is
`C:\MechaRAMS\Temp\AdvantageScope 9-2-2026 - Gyro Rate Validation.json`. JSON parsing, logged-key
inspection, official Phoenix 6 API inspection, and `git diff --check` passed (line-ending warnings
only). Per mentor instruction, no build, compile, test, simulation, deploy, or push was performed. After a
manual build/deploy, run one 2 m A/B validation first; then run 1 m only if the gyro signal is healthy
and the 2 m yaw excursion/settling improves.

## 2026-08-31 `c346`: profile-clock correction passes the settling-time gate

`akit_rotated_1788223658639_c3edc346.wpilog` physically traveled `1.010 m` center (left/right
`1.025/0.995 m`). The command completed in `1.274 s`, essentially the theoretical `1.265 s` 1 m
profile time. It first reached <=6 cm at +`1.160 s` and finished `0.114 s` later, so the terminal tail
was `8.95%` of total command time and passed the mentor's <10% goal. It first entered the 4 cm pose
gate at +`1.180 s`, latched the pose+velocity hold once at +`1.207 s`, never exited that hold, and
finished `0.067 s` later. Final fused error was `0.00314 m` (`X=-0.00208 m`, `Y=+0.00235 m`) and
`0.194 degrees`; there was only one requested-X sign change after <=6 cm and no measured-X reversal
above `0.02 m/s`. This validates commit `39ab164`; do not retune translation gains/damping/profile.

Two issues remain separate from the now-passing settle behavior. First, controller-loop gaps of
`180.3/116.6 ms` occurred during early acceleration; the five-step safety cap kept the run controlled
but left about `94 ms` of profile-clock lag at finish. Do not increase motion constraints until those
user-code stalls are isolated. Second, the `0.030 m` ruler endpoint difference across the `0.6072 m`
frame implies about `-2.83 degrees` clockwise physical rotation, while the gyro-owned fused heading
ended about `+0.19 degrees`. Treat this as a yaw-measurement discrepancy, not a reason to change theta
PID or tolerance yet. Next controlled test: keep both cameras open and run one fresh-log 2 m move with
identical settings; record maximum and final left/right distances plus lateral displacement. If the
corner difference repeats, add raw-Pigeon-versus-wheel-only yaw-delta logging before tuning rotation.
No source behavior changed in this analysis; no build, compile, test, simulation, deploy, or push was
performed.

## 2026-08-31 `7b6d` analysis; wall-clock profile synchronization implemented

`akit_rotated_1788222606174_3be87b6d.wpilog` physically traveled `0.9725 m` center (left/right
`0.985/0.960 m`) and ended about `-2.36 degrees` clockwise by the ruler endpoints. The fused estimator
reported `0.9661 m` along-track travel, only `0.0064 m` different from the ruler result, so newest-only
vision ingestion fixed the dangerous localization lag from `7881`. The command still lasted `2.275 s`;
first <=6 cm error was at +`1.703 s`, leaving a `0.572 s` terminal tail with `0.126 m` fused path for
only `0.025 m` net progress, three measured-vx reversals and four measured-omega reversals.

The decisive control defect is profile-clock lag. Controller executions averaged `34.5 ms` and had a
`96.1 ms` maximum gap, but each `ProfiledPIDController.calculate` advanced its trapezoid by exactly
`20 ms`. At `0.141 m` remaining the robot was already ahead of the stale profile, so profile feedback
was `-0.272 m/s`; the controller commanded reverse even though the final target remained forward.
Later the stale profile caught up, the feedback sign reversed, and the chassis corrected forward again.
Implement one isolated fix: advance the existing X/Y/theta profiled controllers by the number of
nominal 20 ms steps represented by measured wall-clock time, with a bounded catch-up count and explicit
timing/step logging. Leave gains, feedforward fade, damping, pose/velocity tolerances, vision weighting,
and camera transforms unchanged for the next one-run A/B validation. This is implemented with a five-
step/100 ms maximum per execute and logs loop delta, steps, accumulator, wall/profile elapsed time, and
signed clock lag. The dedicated non-overwriting layout is
`C:\MechaRAMS\Temp\AdvantageScope 8-31-2026 - Profile Clock Sync.json`. JSON parsing, exact source-key
inspection, API-signature inspection, and `git diff --check` passed (line-ending warnings only). Per
mentor instruction, no build, compile, test, simulation, deploy, or push was performed.

## 2026-08-31 `7881`: FIFO regression rejected; newest-frame replacement implemented

`akit_rotated_1788222113401_716d7881.wpilog` physically traveled `1.1425 m` center (left/right
`1.140/1.145 m`), a dangerous `14.25 cm` overshoot, while fused along-track progress ended at only
`0.9660 m`. Heading normalization worked: measured start yaw changed from `+1.583 degrees` to zero,
and the `0.005 m` corner difference across `0.6072 m` is only about `+0.47 degrees`. The <=6 cm tail
improved from `0.530 s` in `1c79` to `0.279 s`, with zero measured-vx reversals, but total command time
worsened to `2.411 s`.

The per-camera FIFO is the cause of the distance regression. It began auto with old observations and
ended with 119 front-left and 52 front-right poses still pending. Nearly all delivered frames were
pre-reset and correctly suppressed, leaving essentially no enabled vision correction; odometry then
underreported the physical motion by `0.1765 m`. Replace the persistent FIFO with a bounded latest-pose
policy: drain every unread NetworkTables result, fuse only the newest solvable pose from each camera in
that loop, and log the number of older same-burst poses superseded. Retain heading normalization and
timing telemetry. Do not tune controller gains/tolerances from this invalid localization run. The
persistent FIFO has now been removed and `SupersededPoseObservationCount` added. The dedicated backlog
layout was updated for this replacement workflow; the separate camera-jitter layout remains untouched.
Static diff/JSON inspection only; no build, compile, test, simulation, deploy, or push was performed.

## 2026-08-31 `1c79` analysis and control-loop backlog mitigation implemented; validation pending

Analyzing `akit_rotated_1788220769491_a0331c79.wpilog` with physical endpoints left `1.010 m`
and right `0.985 m`. The measured center travel is `0.9975 m`. With the measured `0.4572 m`
wheel track and each wheel center `0.075 m` inboard of the frame edge, the two ruler points are
`0.6072 m` apart; their `-0.025 m` right-minus-left displacement corresponds to approximately
`-2.36 degrees` (clockwise) physical yaw. The deployed log confirms enabled vision rotation fusion
is disabled, but the command lasted `2.242 s` and contained controller gaps of `0.218/0.200 s` with
user-code times of `171/181 ms`. Those gaps coincided with bursts of 18 and 16 queued camera
observations. Implemented a per-camera FIFO that still drains and eventually fuses every unread frame
in timestamp order but delivers at most one pose per camera per robot loop. Added unread/backlog and
IO/fusion/total Vision timing telemetry. Relative-forward calibration autos now preserve measured X/Y,
normalize only the start heading to zero, then form the +X target; this removes the artificial rotation
from the `+1.55 degree` camera-seeded starting yaw. Controller gains, tolerances, transforms, and
covariance are unchanged. Static diff inspection and `git diff --check` passed (line-ending warnings
only). Created the separate validated AdvantageScope 26.0 layout
`C:\MechaRAMS\Temp\AdvantageScope 8-31-2026 - Vision Backlog Timing.json` without overwriting the
camera-jitter layout. Per mentor instruction, no build, compile, test, simulation, deploy, or push was
performed.

Tail-specific reanalysis: the estimator first reached `0.0599 m` translation error at +`1.7125 s`
and the command ended at +`2.2424 s`, so the final nominal 6 cm consumed `0.5299 s`. During that tail
the fused pose accumulated `0.2175 m` of path for only `0.0462 m` net displacement, with four measured
vx sign changes above `0.02 m/s` and eight measured-omega sign changes above `2 deg/s`. Translation
feedforward was already only `0-6.4%` while velocity damping was `93.6-100%`; the robot therefore
spent the tail in low-speed feedback/damping correction. It first entered the actual 4 cm pose gate at
+`2.0998 s`, qualified pose+velocity at +`2.1824 s`, and completed the 0.05 s confirmation at
+`2.2424 s`. Thus the post-4-cm qualification/hold was only `0.1426 s`; the objectionable half-second
is the 6-to-4-cm approach and repeated motion, not the configured settle timer. Validate the already
committed FIFO pacing and zero-heading start before changing damping/feedforward; if the tail remains,
make one isolated terminal-damping/feedforward change rather than widening position tolerance.

## 2026-08-31 `fe31` / `c75a`: measured validity deployed; enabled vision-theta suppression implemented

Stationary `1c14fe31` confirms the deployed Camera0/front-left factors `2.15/2.10`, Camera1 factors
`1.0/1.0`, and Camera1 rotation eligibility false. Both completed 100 samples. Camera0 minus Camera1
mean was `+0.02153 m X`, `+0.00311 m Y`, `0.02175 m translation`, and `+0.932 degrees yaw`.
Translation/yaw sigma was `0.01778 m / 0.223 degrees` left and `0.01178 m / 0.154 degrees` right.

Trajectory `657ac75a` used the new factors and excluded Camera1 theta as intended. The 1 m command ran
1.806 s, first qualified pose+velocity at +1.746 s, and ended 1.98 cm / 1.27 degrees from target. Fused
along-track maximum was 1.35 cm beyond target. There were no measured-omega sign reversals above
2 deg/s after pose tolerance, an improvement over prior jittery runs. At command end measured chassis
translation was `0.0049 m/s`, omega `0.98 deg/s`, and max module speed `0.0266 m/s`; the stricter
debounced `WheelsStopped` became true 0.322 s later. Thus most visible post-target pose movement was
not sustained wheel motion.

The remaining delay is enabled heading estimation, not translation: the ideal 1 m profile is about
1.27 s and translation was near tolerance by about 1.35 s, but yaw rose to 5.07 degrees during final
deceleration and did not enter the 1.8-degree gate until +1.746 s. Integrated measured omega predicts
2.71 degrees less yaw change than the fused pose actually logged; 2.44 degrees of that residual accrued
after +1.2 s. Centimeter-scale accepted-pose innovations also coincide with the apparent end-position
jumps. Implement one isolated follow-up: retain front-left MultiTag heading for disabled/manual seed,
but set enabled estimator fusion to camera XY only (`FUSE_VISION_ROTATION_WHILE_ENABLED=false`) so the
gyro owns running heading. Do not change controller gains/tolerances in the same test. No build,
compile, test, simulation, deploy, or push was performed.

## 2026-08-31 two-distance camera validity policy implemented; robot validation pending

Far `160c1546` and close `cb76b12e` each completed 100 stationary MultiTag samples per camera while
disabled, at roughly 3.9 m and 2.9 m average tag distance. Front-left/front-right translation sigma
ratios were `2.38` far and `1.93` close; yaw sigma ratios were `2.35` and `1.87`. Front-left is therefore
configured with measured XY/angular factors `2.15/2.10`; front-right remains `1.0/1.0`. Across the
approximately 1 m X move, front-left mean yaw changed only `-0.052 degrees` while front-right changed
`-0.472 degrees`. Front-right rotation trust is now false, but its lower-noise XY remains accepted;
front-left is the sole MultiTag vision-theta source. Camera transforms, drivetrain gains, trajectory
constraints, and tolerances are unchanged. The existing camera-jitter layout already contains all
configuration and validation fields. Static diff inspection is required, but per mentor instruction
do not build, compile, test, simulate, deploy, or push. Next: manual deploy, one disabled capture to
confirm active settings, then one dual-camera 1 m trajectory and log.

## 2026-08-31 `1788218509562_cce4a8da` first valid stationary jitter capture

The disabled capture ran from log time 13.160 to 15.848 seconds and completed 100 accepted MultiTag
samples per camera before the robot was enabled. Camera0/front-left mean was `(2.28659, 2.12976,
+0.74673 deg)`; Camera1/front-right mean was `(2.27592, 2.17198, -0.79563 deg)`. Front-left minus
front-right was `+0.01067 m X`, `-0.04222 m Y`, `0.04355 m translation`, and `+1.54236 deg yaw`.
The average of the two yaw means was approximately `-0.02445 deg`, close to the expected zero if the
chassis was still square, but the individual camera means remain separated.

Front-left random scatter was materially worse: X/Y/translation sigma `0.00704/0.02024/0.02143 m`,
yaw sigma `0.27335 deg`, and X/Y/yaw peak-to-peak `0.03248/0.12128 m/1.63841 deg`. Front-right was
`0.00295/0.00950/0.00995 m`, `0.12907 deg`, and `0.01441/0.04591 m/0.62779 deg`. Thus front-left was
about 2.15x worse in combined translation sigma and 2.12x worse in yaw sigma. This was not solely one
outlier: front-left translation sigma was already about 1.84 cm at sample 10 and settled around
2.14 cm, although its largest Y range jump occurred near sample 45.

Do not change transforms or covariance from this single placement. Repeat once without moving the
robot to establish repeatability, then repeat at a second surveyed distance/view angle. If the sigma
ratios and signed mean differences persist, correct systematic yaw/translation bias first; only then
consider front-left XY/angular factors near the measured ratio. Weighting before correcting the
1.54-degree mean separation would bias fusion toward the front-right mean.

## 2026-08-31 `cbe4b267` camera-jitter capture attempt was not triggered

The 52.075-second log contains the deployed jitter instrumentation, but the capture never started:
`Active` remained false, both `SampleCount` values remained 0, both `Ready` values remained false, and
`ComparisonReady` remained false. The robot was disabled for the first 39.471 seconds, enabled until
43.067 seconds, then disabled again. Both cameras were otherwise healthy: each repeatedly produced
accepted observations, logged trusted MultiTag rotation, and logged zero rejected frames. Therefore
tag visibility was not the blocker; the SmartDashboard Start command did not reach the capture logic.
Next attempt: while disabled, press Start and confirm `Active=true` plus increasing sample counts before
waiting for both counts to reach 100. If Start was definitely pressed while disabled, replace or add a
more directly observable trigger before collecting another log. No source behavior changed from this
invalid attempt.

## 2026-08-31 front-right yaw correction and camera-jitter capture implemented; robot validation pending

Mentor approved the measured one-variable front-right yaw correction and requested an objective way
to measure each camera's jitter and adjust per-camera measurement validity. The front-right
robot-to-camera yaw is now +15.74 degrees; measured XYZ, pitch, roll, drivetrain gains, trajectory
constraints, and yaw tolerance are unchanged. SmartDashboard has disabled-only Start/Stop Camera
Jitter Capture commands. The fixed capture freezes 100 accepted MultiTag poses per front camera and
logs mean pose, X/Y/combined translation/yaw population standard deviations, peak-to-peak ranges,
and signed camera0-camera1 mean X/Y/translation/yaw differences. Explicit logged per-camera XY
covariance, angular covariance, and rotation-trust controls are implemented; all factors remain 1.0
and both headings remain trusted until the corrected capture supplies evidence. No validity is learned
automatically from moving data. The new non-overwriting layout is
`C:\MechaRAMS\Temp\AdvantageScope 8-31-2026 - Camera Jitter Calibration.json`. Source and layout were
checked statically. Per mentor instruction, no build, compile, test, simulation, deploy, or push was
performed; manual robot validation is next.

## 2026-08-24 isolated-camera `84e33292` / `0d086421` comparison

Compared the requested controlled pair. Camera order is confirmed from `RobotContainer`: Camera0 is
physical front-left and Camera1 is physical front-right. `84e33292` covered front-left, so it is a
front-right-only run; `0d086421` covered front-right, so it is a front-left-only run. Both deployed
`RELAXED`/1.8 degrees and each accepted only the intended camera with zero rejections.

Front-right-only (`84e33292`) was worse: 2.140 s command, hold at +2.080 s, strict wheel stop 0.160 s
after command end, max measured omega 59.66 deg/s, max yaw error 6.16 degrees, 15 measured-omega sign
changes above 2 deg/s, max post-first-pose module speed 0.715 m/s, and minimum battery 9.63 V. At its
first pose-tolerance entry it still moved 0.189 m/s and 19.30 deg/s, with mean measured module speed
0.292 m/s against a 0.092 m/s target.

Front-left-only (`0d086421`) was materially calmer: 1.974 s command, hold at +1.874 s, strict wheel
stop only 0.042 s after command end, max measured omega 39.79 deg/s, max yaw error 3.50 degrees, nine
measured-omega sign changes, max post-first-pose module speed 0.312 m/s, and minimum battery 10.32 V.
It entered pose tolerance at 0.083 m/s translation; angular speed 15.25 deg/s was the only velocity
gate still failing, and it qualified about 0.068 s later.

The same-position dual-camera pre-run window in `85e18116` independently shows a systematic extrinsic
difference: over 78 time-nearest stationary samples, the front-left robot pose minus front-right robot
pose averaged +0.0038 m X, +0.0777 m Y, and -2.054 degrees yaw (yaw range -2.80 to -1.50 degrees).
Thus the right camera reported robot yaw about 2.05 degrees higher and robot Y about 7.8 cm lower than
the left camera at the same physical state. This supports the mentor's observation and identifies the
front-right extrinsic/solve as the dominant camera-side contributor, though the lower 9.63 V supply in
its isolated run is a secondary confound.

Do not widen the command yaw tolerance or change drivetrain gains from this comparison. The next
controlled camera correction should change only the front-right transform yaw, using the stationary
signed difference: increasing robot-to-front-right yaw from +13.69 to approximately +15.74 degrees
would reduce its estimated robot yaw by about 2.05 degrees. Keep manually measured XYZ and pitch
unchanged. Before editing, obtain mentor approval for this measured one-variable correction; then run
one stationary dual-camera check and one dual-camera 1 m test. A fallback if the static check does not
converge is to fuse front-right XY but set its theta standard deviation to infinity until a full
extrinsic calibration is completed. No robot source change, build, compile, test, simulation, deploy,
or push was performed during this analysis.

## 2026-08-24 `85e18116` relaxed-yaw run analyzed

Analyzed `akit_rotated_1787618271910_85e18116.wpilog`, with physical endpoints left 1.020 m and
right 1.0175 m. The log proves commit `d77f768` was deployed: `YawPrecisionMode=RELAXED`, rotation
tolerance 1.8 degrees, drive velocity `kP=0.10`, and the 0.05 s confirmation. Endpoint distance and
straightness were good, but the command remained active 2.327 s (60.885944-63.213413 s) and visible
jitter was real active correction rather than completion delay. Pose tolerance entered three times;
the zero-velocity hold latched only once at 63.153496 s, never escaped, and the command ended 0.060 s
later. Strict wheel stop occurred at 63.438535 s, 0.225 s after command end.

At the first pose entry (62.207650 s), radial error 0.0370 m and yaw error 1.502 degrees passed the
relaxed gate, but translation speed was 0.170 m/s and angular speed 32.80 deg/s. The controller already
requested -18.24 deg/s while the gyro measured +32.80 deg/s. At the second entry (62.720224 s), pose
error was only 0.0049 m/1.617 degrees, but translation speed 0.1271 m/s narrowly failed the 0.120 limit;
mean measured module speed was 0.320 m/s against a 0.134 m/s target. The active run reached +/-39.4
deg/s angular speed. Supply voltage dipped to 9.90 V.

Both cameras were active with 36/34 accepted and zero rejected frames. Time-nearest accepted camera
poses differed by 0.046 m on average and 0.125 m maximum, with heading disagreement averaging 0.91
degree and reaching 2.70 degrees. Therefore do not widen yaw tolerance again and do not change gains
yet: the next controlled sequence is one right-camera-only 1 m run (cover left), then one
left-camera-only run (cover right), otherwise identical. Compare command duration, pose entries,
omega reversal, camera innovation, wheel-stop time, and physical left/right endpoints. No robot source
change, build, compile, test, simulation, deploy, or push was performed during this analysis.

## 2026-08-24 selectable precision-command yaw tolerance implemented

Mentor requested that terminal yaw importance be explicit instead of applying one tolerance to every
precision move. `DriveToPosePrecisionCommand.YawPrecision` now provides `PRECISE` (the existing 1.5
degree gate) and `RELAXED` (1.8 degrees, motivated by the `20011a08` 37.700 s gate audit). The original
two-argument constructor defaults to `PRECISE`, so every tag-board and final coarse-to-precise handoff
remains unchanged. Only current-position 1 m/2 m straight test autos explicitly request `RELAXED`.
Future multipart command groups can make an intermediate precision segment relaxed without loosening
the final segment.

Each run now logs `DriveToPose/Controller/YawPrecisionMode` and
`ConfiguredRotationToleranceDegrees`. Created and JSON-validated the non-destructive layout
`C:\MechaRAMS\Temp\AdvantageScope 8-24-2026 - Selectable Yaw Precision.json`; both exact source keys
are present in its table and the numeric tolerance is also on the precision graph. Static source/path
inspection and `git diff --check` passed (line-ending warnings only). No build, compile, test,
simulation, deploy, or push was run; mentor performs the manual build/deploy.

## 2026-08-24 `20011a08` confirmed gain failure and 37.700 s gate audit

The deployed log confirms `Drive/ConfiguredDriveGains/KP=0.20` and
`DriveToPose/Controller/ConfiguredSettleSeconds=0.05`. It regressed versus `ef00a32a`: command time
1.988 s, physical endpoint 1.03 m, fused peak X overshoot 0.0277 m, five measured-vx sign changes after
crossing, and about 0.90 s from X-band arrival to strict wheel stop. Restore only drive `kP` to 0.10;
retain the 0.05 s confirmation because peak overshoot occurred before the final hold.

At the mentor's 37.700 s cursor, radial translation error was 0.01185 m, translation speed 0.02325 m/s,
and rotation speed 0.245 deg/s, all passing. Rotation error was 1.5791 degrees, just 0.0791 degree
outside the 1.5 degree tolerance, so `WithinPoseTolerance=false` and the controller still requested
-7.02 deg/s. This precisely explains why it did not stop. A separately authorized 2.0 degree capture
tolerance would have qualified at that state and is the next targeted candidate; do not change it in
the same commit as the `kP` reversion. No build, compile, test, simulation, deploy, or push was run.

## 2026-08-24 `7ddca884` pre-experiment run analyzed

Analyzed `akit_rotated_1787617053342_7ddca884.wpilog`. This log does not contain commit `b2e575b`:
`Drive/ConfiguredDriveGains/KP`, `Drive/ConfiguredDriveGains/KV`, and
`DriveToPose/Controller/ConfiguredSettleSeconds` are all absent, and the measured hold remains 0.16 s,
consistent with the previous 0.15 s constant. Do not attribute this run to the new `kP=0.20` or
0.05 s confirmation and do not revert the untested experiment.

The old-code run lasted 2.125 s, fused peak X overshoot was 0.0308 m, and physical endpoints were both
1.05 m even though final fused X error was only 0.0014 m beyond target. X entered its 4 cm band at
+1.177 s, but full pose tolerance did not first occur until +1.797 s and the hold did not latch until
+1.964 s. Heading error reached 3.67 degrees, omega tracking RMS was 18.44 deg/s, pose tolerance entered
three times, and the cameras differed by up to roughly 0.079 m near the endpoint. The long visible
correction therefore occurred primarily before qualification; after the hold latched, visible braking
was again about 0.2 s. Manually build/deploy `b2e575b`, verify the three configuration fields before
motion, and run one fresh 1 m comparison. No code change, build, compile, test, simulation, deploy, or
push was performed during this analysis.

Mentor subsequently confirmed that the new code had not been deployed. Publish
`DriveToPose/Controller/ConfiguredSettleSeconds` continuously while disabled alongside the configured
drive gains, so future preflight can prove the deployed revision before enabling motion.

## 2026-08-24 controlled drive-velocity `kP` and command-time experiment implemented

Mentor authorized a controlled CTRE gain change after the `ef00a32a` run. Change only drive velocity
`kP` from the generated provisional 0.10 V/rps to 0.20 V/rps; keep `kS=0`, `kV=0.124`, `kA=0`, all
outer precision gains, vision settings, and constraints unchanged. This doubles proportional braking
authority while remaining a conservative one-variable test. Log the configured drive gains explicitly.
Also reduce the post-qualification zero-output confirmation from 0.15 s to 0.05 s. The existing
2.5 m/s^2 motion constraint has an ideal 1 m triangular-profile time of 1.265 s, so this removes an
artificial 0.10 s while retaining multiple 20 ms qualification observations. Run one 1 m PnP+Iso
comparison only and revert if peak overshoot, wheel oscillation, or audible jitter worsens. This
experiment does not replace translation SysId. No build, compile, test, simulation, deploy, or push was
run; mentor performs the manual build/deploy.

## 2026-08-24 `ef00a32a` wheel-settling run analyzed

Analyzed `akit_rotated_1787616151167_ef00a32a.wpilog`. The 1 m command lasted 1.852 s, down from
2.203 s in `cca9ab7b`. Fused peak X overshoot was 0.0101 m and final fused X error was 0.0050 m short;
the physical left/right endpoints were 0.9975/0.9900 m (0.99375 m center), so fused and tape endpoints
agree closely. The settling hold latched once at +1.692 s with no exit, and module targets were zero by
+1.712 s. Visible module motion fell below approximately 0.05 m/s around +1.892 s, matching the
mentor's approximately 0.2 s observation. The strict all-modules <=0.02 m/s flag toggled briefly on
encoder values up to about 0.04 m/s after visible motion had ended, so retain both numeric module-speed
fields rather than judging only the Boolean.

The remaining tail is dominated by low-level velocity tracking: at +1.405 s, mean commanded module
speed was only 0.0578 m/s while mean measured module speed was 0.4696 m/s. Current drive gains remain
provisional (`kP=0.1`, `kS=0`, `kV=0.124`, `kA=0`). Do not change the 4 cm radial pose tolerance,
add outer-loop derivative, or increase the 20 ms command rate. Translation SysId followed by a
velocity-step validation is the evidence-backed route to a shorter physical braking tail.

Both cameras remained active in this log: Camera0/Camera1 accepted 32/30 frames with zero rejections.
Therefore this was not a camera-isolated A/B run; terminal camera-pose separation still reached roughly
0.027-0.068 m. No robot code, controller constant, build, compile, test, simulation, deploy, or push was
performed during this analysis.

## 2026-08-24 PDH CAN-error hotfix and wheel-settling telemetry implemented

The existing-handle JNI experiment returned zero voltage/current and then produced repeated
`CAN: Message not found` errors plus 20 ms robot-loop overruns from `PowerDistributionJNI.getVoltage`.
Removed every direct PDH poll, the explicit ID 1 registration, and the guessed hardware constant. Use
`SystemStats/BatteryVoltage` for the next controller test; do not restore PDH current telemetry until
the actual device ID/bus is verified from hardware configuration. This is a source-only hotfix; the
robot must remain disabled until the mentor manually builds and deploys it.

Reanalyzed `akit_rotated_1787614931289_cca9ab7b.wpilog` using decoded module-state samples. Active time
was 2.203 s. Fused peak X overshoot was only 0.0037 m and final fused X was 0.0296 m short, while the
physical center finished about 0.010 m short. However, the first `abs(ErrorX)<=0.04 m` occurred at
+1.424 s and mean absolute module speed remained above 0.02 m/s until +2.142 s: 0.719 s of physical
wheel motion, or 32.6% of the command, fails the <10% settling target. This does not contradict the
4 cm threshold: the command uses radial X/Y error and a 1.5 degree heading tolerance. At +1.5 s the
3.7 cm X and 4.45 cm Y errors produced 5.79 cm radial error; at +1.8 s heading error was 2.74 degrees.

Added graph-friendly mean/max measured and target module-speed outputs plus `Drive/WheelsStopped`
(all measured modules <=0.02 m/s). The terminal per-camera accepted poses differed by approximately
0.034-0.071 m, comparable to the 0.04 m capture tolerance. Before changing gains or tolerance, run a
fresh-log 1 m right-camera-only test and a fresh-log 1 m left-camera-only test. No build, compile, test,
simulation, deploy, or push command was run.

## 2026-08-24 Latched-settle and existing-handle PDH follow-up implemented

Do not repeat the unchanged 1 m test. Implemented the two issues isolated by `4b2a639a`: latch the
zero-velocity settling phase after the first pose+velocity qualification, allowing only a wider pose
escape threshold (0.06 m/2.5 degrees) to resume correction; and read dynamic PDH values through the
already-owned Conduit HAL handle without allocating a second device. New logs separate raw per-loop goal
qualification from the latched hold and count hold exits. The acceptance definition is now quantitative:
first target crossing until pose and speed enter and remain within limits must be <10% of total active
move time.

Retain the direct 1 m auto as the next single-variable validation before any PathPlanner hybrid or SysId
sequence. Created the non-destructive configuration copy
`C:\MechaRAMS\Temp\AdvantageScope 8-24-2026 - Latched Settle.json`; it parses and all five added leaf
names match source output keys. `git diff --check` passed with only line-ending warnings. No build,
compile, test, simulation, deploy, or push command was run per mentor instruction.

## 2026-08-24 Damped-stop robot log analyzed

Analyzed `akit_rotated_1787613657663_4b2a639a.wpilog` against the prior `7d778d31` 1 m run. The new
run lasted 2.326 s. Its fused X crossed the target at +1.305 s, peaked 4.64 cm beyond it 0.100 s later,
and did not finish until 1.021 s after the first crossing. The controller had already reversed at
10.86 cm remaining, but at target crossing requested -0.155 m/s while the chassis still measured
+0.161 m/s. This is a 0.316 m/s signed velocity-tracking mismatch, not a 20 ms scheduler-resolution
problem. Controller/applied vx changed sign four times after crossing and measured vx changed sign
eight times.

The settle implementation is visibly flapping: pose tolerance was entered three times and full
`AtGoal` five times, repeatedly resetting the timer before it finally accumulated 0.150 s. A latched
settling state with a wider escape threshold is the next logic correction. Physical final distances
102.75/101.75 cm average 102.25 cm, with a 1 cm left/right difference; fused final X reported only
0.49 cm beyond target, so physical and fused endpoint differ by about 1.76 cm. Both cameras accepted
frames with zero rejections and innovation maxima 0.077/0.108 m.

The `Forward 1m` chooser is direct `DriveToPosePrecisionCommand` from the captured current pose; it
does not execute PathPlanner and has no handoff threshold. Keep the outer command loop at 20 ms: CTRE
odometry already runs at 250 Hz and TalonFX velocity control runs on-device. Do not add profile PID kD
on top of the explicit 0.45 measured-velocity damping yet. First latch settle, then characterize the
unvalidated drive velocity loop (`kS=0`, `kA=0`, provisional kP/kV) with translation SysId, then retune
fade/damping from a controlled 1 m comparison.

The PDH mirror fields still emitted only one unchanged sample before the run. `SystemStats/BatteryVoltage`
was valid and reached 10.51 V. Before characterization, replace snapshot-value mirroring with reads
through the already-owned Conduit HAL handle (no second allocation), and validate dynamic voltage/current.
No robot source behavior was changed and no build, compile, test, simulation, or deploy command was run.

## 2026-08-24 PDH double-allocation hotfix implemented

Deployed code crashed during `Robot` construction with HAL allocation error -1029 because
`LoggedPowerDistribution` allocated REV PDH 1 through AdvantageKit Conduit and the new WPILib
`PowerDistribution` object attempted to allocate the same device again. Removed the second HAL object.
The single explicit `LoggedPowerDistribution` owner remains, and `Robot` now mirrors its already-captured
Conduit voltage/current/channel values into the unchanged `PowerDistributionDirect/*` graph paths. This
preserves the AdvantageScope configuration without a second device handle. Source inspection confirms
there is no `new PowerDistribution(...)` call and the Conduit method names match AdvantageKit 26.0.2.
Documentation and prompt history were synchronized. No build, compile, test, simulation, or deploy
command was run per mentor instruction.

## 2026-08-24 Stop-and-settle damping follow-up implemented

Mentor reports the remaining 1 m behavior is still visibly unacceptable: cross the target, reverse,
and spend roughly one second returning. The `7d778d31` log shows why: pose-only `AtGoal` can become true
while the chassis is still moving; the controller continues issuing correction throughout the settle
window; and translation/theta have no measured-velocity damping. Implement near-target translation
damping, theta-rate damping, a velocity-qualified settle gate, zero closed-loop velocity while settling,
and explicit phase/damping telemetry. Translation damping gain 0.45 ramps in as feedforward fades;
theta-rate damping gain 0.35 is always active. `AtGoal` now requires <=0.12 m/s translation and <=8
deg/s rotation in addition to the existing 0.04 m/1.5 deg pose limits. A qualified goal applies a
closed-loop zero request during a 0.15 s hold; pre-hold controller requests remain separately logged.
Entry counters show whether pose or full-goal qualification flapped.

Added graph-friendly REV-PDH sampling at the explicit ID/type under
`RealOutputs/PowerDistributionDirect`, sourced from AdvantageKit's sole Conduit allocation.
Created the non-destructive layout copy
`C:\MechaRAMS\Temp\AdvantageScope 8-24-2026 - Damped Stop.json`; JSON parsed and all 15 added leaf
names match exact source keys. Controls, architecture, test plan, and prompt history were synchronized.
Pure damping math coverage was added. `git diff --check` passed with only line-ending warnings. No
build, compile, test, simulation, or deploy command was run per mentor instruction.

## 2026-08-24 First measured-distance fade run analyzed

Analyzed `akit_rotated_1787611878395_7d778d31.wpilog`, one 1 m PnP+Iso run. The feedforward fade fixed
the primary longitudinal failure: fused-pose peak X overshoot fell from the previous 0.115 m to
0.0357 m, the request crossed into braking 0.0457 m before the target, and at the first target crossing
requested/measured vx were -0.051/+0.005 m/s instead of the previous +0.371/+0.633 m/s. The command
settled without timeout in 1.903 s and ended with 0.0138 m fused translation error. Physical left/right
edges were +0.030/+0.000 m, so center travel averaged approximately +0.015 m and indicates residual
clockwise yaw rather than a uniform distance-scale error.

Rotation is now the main dynamic issue: signed theta error ranged -2.88 to +2.43 degrees, requested vs
measured omega RMS error was 13.2 deg/s, and the measured omega briefly reached 32.5 deg/s. Both cameras
accepted MultiTag frames with no rejections, though camera 1 innovation reached 0.112 m. Explicit REV
PDH registration correctly reported 24 channels, but built-in voltage/current still emitted no samples;
`SystemStats/BatteryVoltage` remained valid and reached a 10.37 V minimum. Keep code unchanged for one
controlled 2 m safety run before tuning theta, so distance scaling remains a one-variable comparison.

## 2026-08-20 Precision stopping-profile and explicit PDH follow-up implemented

Mentor accepted the recommendation from the first closed-loop velocity logs. The 1 m and 2 m runs
still crossed the target while requesting roughly 0.37-0.39 m/s forward; their measured speeds were
0.58-0.63 m/s, and the request did not reverse until the fused pose was already 0.076-0.090 m past
the target. Implemented a measured-distance feedforward fade for the translational motion profile while
retaining pose feedback, CTRE velocity mode, the validated wheel radius, and the current gains and
constraints. Translation feedforward is 1.0 at 0.35 m, linear inside that radius, and 0.0 at the 0.04 m
tolerance; pose feedback remains full strength. New telemetry records raw/faded profile velocities,
remaining distance, and fade scale, with pure-math unit cases added for the endpoints and midpoint.

Replaced automatic power-module detection with AdvantageKit's explicit
`LoggedPowerDistribution.getInstance(1, PowerDistribution.ModuleType.kRev)` API. CAN ID 1 is isolated
as `HardwareConstants.POWER_DISTRIBUTION_CAN_ID` and is the documented REV default; verify 24 channels
and nonzero voltage/current before motion, and use REV Hardware Client rather than guessing if this
robot was reconfigured. Controls, architecture, test plan, and prompt history were synchronized. The
ordered gate is one 1 m safety run, one 2 m safety run, two repeats of each, then translation SysId only
after peak overshoot is below 0.05 m without repeated correction. `git diff --check` passed with only
line-ending warnings. No build, compile, test, simulation, or deploy command was run per mentor
instruction.

## 2026-08-20 Disabled-safe roboRIO log purge implemented

Mentor authorized a SmartDashboard button that removes all files and subdirectories under the robot's
log folder. Implement it as a disabled-only background operation integrated with
`RotatingWPILOGWriter`. The final full-disk-safe order is: detach this file receiver between complete
tables, close the former active writer, recursively delete every item under the guarded log folder, then
open and attach a fresh explicit WPILOG. This avoids both unlinking an open writer and requiring free
space for a replacement before cleanup. Live NT4 remains active while a few file-log tables may be
dropped. No build/compile was run per mentor instruction.

Added SmartDashboard `Delete Stored Logs And Start Fresh (Disabled Only)`, rejected while enabled.
Status outputs are `Logging/PurgePending`, `PurgeCount`, `LastPurgeTimestampSeconds`, the shared
`LastRotationError`, and `ActiveLogPath`. The purge implementation refuses any folder other than the
exact roboRIO `/home/lvuser/logs` or project `logs/sim`, preserves the folder itself, and leaves only the
new active log. Controls, test plan, architecture, prompt record, and the custom AdvantageScope layout
were synchronized. JSON parsing and `git diff --check` passed with only line-ending warnings; Java brace
counts and configured status paths were statically verified. No Gradle/build/test/deploy was run.

## 2026-08-19 Closed-loop precision-drive validation implemented

Mentor accepted the controlled follow-up from the six-run baseline: change the precision robot-relative
drive request from CTRE open-loop voltage to closed-loop velocity, add high-rate AdvantageKit telemetry
that separates motion-profile feedforward from pose-feedback correction and compares requested versus
measured chassis motion, and initialize AdvantageKit PDH logging. Keep the validated 2.100-inch wheel
radius, 1.6 m/s speed constraint, 2.5 m/s^2 acceleration constraint, and current pose PID gains unchanged
for the first comparison runs. No build/compile will be run per mentor instruction.

Implementation keeps the generic robot-relative request unchanged and adds a dedicated precision
`RobotCentric` request with `DriveRequestType.Velocity`. `DriveToPosePrecisionCommand` now logs the
profile setpoint and velocity, pose-feedback contribution, unclamped/clamped requests, requested and
measured field/robot chassis speeds, vx/vy/omega tracking errors, signed pose errors, and clamp flags.
Graph-friendly scalar channels accompany structured values. `Robot` initializes
`LoggedPowerDistribution.getInstance()` before `Logger.start()` so the default PDH module can be logged
without guessing its CAN ID. The controls, architecture, test plan, and mentor prompt record were
synchronized. Static `git diff --check` passed with only existing line-ending warnings; no Gradle,
compile, test, simulation, or deployment command was run.

Created a non-destructive AdvantageScope layout copy at
`C:\MechaRAMS\Temp\AdvantageScope 8-19-2026 - Precision Velocity.json`. It preserves the original
layout, adds all new precision-controller scalars to the Table, and adds a selected `Precision Velocity
Tracking` graph. The JSON parsed successfully as AdvantageScope version 26.0.0; all 16 new leaf names
were matched exactly against `Logger.recordOutput` calls. The existing PDH voltage/current paths were
retained.

## 2026-08-19 Six-run real-robot precision baseline analyzed

Analyzed six complete, separately rotated WPILOGs (three 1 m and three 2 m PnP+Iso runs) against tape
measurements. Physical endpoints were 1.000/1.030/0.987 m and 1.985/2.010/2.007 m. A through-origin
fit gives actual/commanded distance = 1.0014, so the current 2.100-inch effective wheel radius should
not change. Dynamic behavior is the issue: every log showed 0.148-0.184 m estimator peak overshoot,
then reverse correction; finish times were 2.01-2.10 s (1 m) and 2.63-2.72 s (2 m), without timeout.
At first target crossing, measured chassis speed remained 0.762-0.849 m/s while module targets had
already decelerated to 0.422-0.483 m/s. Inspection found the precision `RobotCentric` request uses its
CTRE default `OpenLoopVoltage`, not the configured velocity closed loop. Next controlled change should
set only the precision request to `DriveRequestType.Velocity`, retain 1.6 m/s and 2.5 m/s^2 for the
first validation, and add profile/command logging before increasing constraints or tuning pose PID.

Yaw/lateral remain secondary watch items: tape edge differences imply roughly 0.6-1.7 degrees yaw
magnitude (sign was not consistent in the supplied left/right distances); paired camera poses differed
by 3.2-4.8 cm and 0.51-0.90 degrees on average, with individual accepted innovations up to 0.144 m.
Battery minima were 9.49-10.16 V with no brownout. PDH voltage/current channels were present but stuck
at zero because `LoggedPowerDistribution` is not initialized; only roboRIO battery voltage was valid.

## 2026-08-16 Disabled-safe WPILOG rotation after truncated download

The uploaded `akit_26-08-16_17-45-44.wpilog` is not the six-run test log: it is exactly 98,304 bytes,
fails structural parsing at the end of that buffer, and its last readable timestamp is only 14.475459
seconds (the requested runs start at 835-2024 seconds). Added a `RotatingWPILOGWriter` receiver and a
SmartDashboard command, `Close Current Log And Start New (Disabled Only)`. The initial synchronous
close/open design blocked the AdvantageKit receiver queue on the real roboRIO and was replaced immediately:
background threads now open the replacement and close the old file, while the receiver performs only an
in-memory writer swap between complete tables. Rotated files use an explicit unique `akit_rotated_*`
filename whose existence is checked before handoff. Status is logged at `Logging/RotationPending`,
`RotationCount`, `LastRotationTimestampSeconds`, `ActiveLogPath`, and `LastRotationError`. No build was
run per mentor instruction.

Real-hardware follow-up: the apparent SFTP/deploy and rotation failures had an underlying storage cause.
Read-only SSH diagnostics showed the roboRIO root filesystem at 100% usage (386.8 MB used, 8 KB free),
with roughly 250 MB in `/home/lvuser/logs`. Ping, TCP/22, SSH, and SFTP all succeeded. The failed deploy
copied the JAR but left `/home/lvuser/robotCommand` empty and no Java robot process running; Driver Station
connectivity in that state reflects the NetComm daemon, not running user code. Free log storage, then
rerun the mentor's manual deploy; do not troubleshoot credentials or networking first.

## 2026-08-12 Measured front-camera extrinsics implemented

Updated the physical front-camera robot transforms. Direct robot-center measurements remain authoritative
for translation: left `(0.152, +0.266, 0.420)` m and right `(0.152, -0.266, 0.435)` m. Stationary
MultiTag observations with the chassis squared to the surveyed board supplied the hard-to-measure angles:
left pitch/yaw `-18.88/-14.80` degrees and right pitch/yaw `-17.10/+13.69` degrees; roll is constrained
to zero. Architecture, test-plan, and prompt documentation were synchronized. No build was run per mentor
instruction.

## 2026-08-10 Measured wheel-distance correction and camera-extrinsic diagnosis

Real-robot 1 m and 2 m tests traveled 1.05 m and 2.10 m physically, a repeatable +5% distance bias.
Applying the measured effective-wheel-radius correction in the CTRE and PathPlanner configurations
without changing trajectory-controller gains. Four-run AdvantageKit analysis also found a consistent
approximately 7.0-7.3 degree yaw disagreement between the two accepted MultiTag camera poses. A test with
one camera covered reduced endpoint correction from about four seconds to about two seconds, confirming
that the rigid mounts' actual extrinsic angles need measurement. The new effective wheel radius is 2.100
in / 0.05334 m (previously 2.000 in / 0.0508 m); documentation was synchronized. Camera intrinsic Charuco
calibration is already complete. No build was run per mentor instruction.

## 2026-08-09 Raised test-board tags to 1.500 m

The mentor measured the repositioned tag centers at exactly 1.500 m above the floor. Updating the
in-code field layout, deploy/import JSON, and physical test instructions to match. The JSON parsed
successfully and contains tag 1 at `(6.0, 2.25, 1.5)` and tag 2 at `(6.0, 1.75, 1.5)`, both facing
negative X. Static `git diff --check` passed; no build was run.

## 2026-08-09 Controller vision-pose seed

Added a disabled-safe Xbox left-stick-press action that resets the drivetrain estimator from the
freshest accepted MultiTag robot pose. Single-tag poses are deliberately ineligible because their
heading is not trusted. The action is blocked during enabled autonomous, rejects observations older
than 0.25 seconds, and logs success/failure plus the applied pose. A matching `Seed Pose From Vision`
dashboard command and Driver Station status messages were added. Controls, architecture, test plan,
and prompt log were synchronized. Static inspection and `git diff --check` passed; no build was run.

## 2026-08-09 Current-pose forward autonomous comparisons

Added selectable 1 m and 2 m forward autonomous tests for all four existing vision configurations.
Each command captures `Drive/Pose` when autonomous actually starts, holds the captured Y coordinate,
targets field +X by the requested distance, and corrects final yaw to 0 degrees. No fixed starting pose
or automatic odometry reset is used. Eight chooser entries were added (PnP/TrigSolve crossed with
isotropic/anisotropic covariance at both distances), and controls, architecture, test-plan, and prompt
documentation were synchronized. Static inspection and `git diff --check` passed; compilation/testing
was intentionally left to the mentor's manual build workflow.

## 2026-08-09 Corrected physical tag left/right order

Updated the custom two-tag layout so tag 1 is physically left and tag 2 is physically right from a
robot at lower X looking in the +X direction. In WPILib coordinates, that means tag 1 gets the higher
Y coordinate: tag 1 is now `(6.0, 2.25, 1.05)` and tag 2 is `(6.0, 1.75, 1.05)`. The in-code layout,
deploy JSON, and real-hardware test plan are synchronized. Static inspection and `git diff --check`
passed. Compilation/testing was intentionally not performed per the mentor's instruction.

Last updated: 2026-07-16 (Claude Fable 5 / Cowork session — trig-solve + anisotropic covariance implemented)

## 2026-07-16 (evening, 2) Adoption policy + camera-placement analysis documented

Answered "what do we do with all these algorithms": they are LAYERS, not all competitors — the
evaluation picks one winner per contested layer (single-tag strategy, covariance model), the robot
competes with ONE fixed configuration (no driver-switched modes; AB options stay as regression
tools), and the real algorithm switching is per-frame and automatic (2 tags -> multi-tag; 1 tag ->
winner; no heading -> PnP fallback; bad frame -> reject). Named the one conditional future rule:
heading-health PnP fallback, only if logs show TrigSolve degrading in low-multi-tag stretches.
Results also calibrate: R2 aniso fit, measured CAMERA_STD_DEV_FACTORS, possible ambiguity-gate
relaxation. NEW test plan step 20: camera-placement analysis from the SAME logs (0/1/2-tag coverage
map per pose bucket -> cross-eye yaw decision, 2-vs-4 camera trigger, per-camera mount quality).
Comparison doc sections 4-5 + student guide "What happens after the tests?" added. Docs only.

## 2026-07-16 (evening) Student-level algorithm guide

New `ALGORITHMS_FOR_STUDENTS.md`: all 10 algorithms under test (odometry, multi-tag, single-tag PnP,
TrigSolve, isotropic/anisotropic trust, path following, drive-to-pose, sequential/spatial handoff)
explained for 15-year-olds with analogies (street signs, eyes-closed walking, the poster-tilt mirror
problem, headlights-at-night for bearing-vs-range, dancer-vs-parker for trajectories), a 10-row
comparison table, and a when-to-use cheat sheet (single tag -> TrigSolve; curved/rotating -> TrigSolve;
high speed -> timestamping + spatial handoff; parked near board -> fitted AnisoCov; after gyro glitch
-> PnP). Cross-linked from the comparison doc and walkthrough A7. Docs only.

## 2026-07-16 (later still) Curved test trajectory + exact execution checklist

- New PathPlanner path/auto: `VisionTestCurvedPath` + `VisionTestCurved` — S-curve (1.5,2.0) →
  dip (2.55,1.25) → (3.6,2.0) with a **25° rotation sweep at mid-path**, same start/end/constraints
  as the straight path. Purpose: lateral motion + rotation changes camera->tag views, creating the
  single-tag stretches where PnP and TrigSolve actually differ (the straight path barely does).
- `RobotContainer.spatialHandoffAuto(...)` parametrized by auto name; 3 new chooser options:
  "VisionTestCurved (spatial handoff)", "AB: Curved handoff (TrigSolve)",
  "AB: Curved handoff (TrigSolve+AnisoCov)".
- Test plan gained an **Execution checklist**: numbered steps 1-19 (sim 1-12: build gate, M1
  straight precision-only, M2 straight handoff, M3 curved handoff, reset test, verdict; robot 13-19:
  calibration, static grid, covariance fit under the winning strategy, M1/M2/M3 on carpet, decision
  by best worst-case) + a fill-in results table. Motions M1/M2/M3 all share the same start pose and
  precision target so numbers compare 1:1.
- Comparison doc: added curved-trajectory prediction row. Build re-verified: compile + 45/45 tests +
  coverage gate green.

## 2026-07-16 (later) Docs: algorithm comparison + test expectations

New `ALGORITHM_COMPARISON_AND_EXPECTATIONS.md`: logical PnP-vs-TrigSolve difference (the PnP-rotation
"lever arm" around the tag; trig solve removes it), isotropic-vs-anisotropic reasoning (error ellipse
elongated along the camera->tag ray; what a mismatched model does to the Kalman gain), failure-mode
duality (ambiguity flip vs heading drift), error budgets at 2 m, and a per-test prediction table
(S1-S5/R1-R4: prediction -> what a deviation means). Key process rule added to the test plan: R2 must
fit ANISO_* coefficients UNDER the winning single-tag strategy (TrigSolve changes the error shape).
Walkthrough A7 now opens with the logical framing + pointer. Docs only, no code changes.

## 2026-07-16 Implementation: single-tag trig solve + anisotropic covariance (A/B-testable)

Follow-up to the same-day survey (next section): both top camera-based precision candidates are now
implemented behind runtime toggles, defaulting to the validated 2026-06-30 baseline. Build verified in
the Cowork sandbox: `compileJava` SUCCESS; `test` **45/45 PASS** (VisionPolicyTest 31,
SingleTagTrigSolverTest 6, AimingCalculatorTest 5, DriveToPosePrecisionMathTest 3). New JaCoCo gate
(`jacocoTestCoverageVerification`, wired into `check`) requires >= 90% line coverage on the pure-logic
classes; current: **VisionPolicy 100%, SingleTagTrigSolver 100%, AimingCalculator 100%**.

What changed in code:

- **`VisionPolicy.java` (new)**: all pure fusion decisions (rejection gates, both covariance models,
  timing rules, `freshTargetX`) extracted from `Vision` so they are fully unit-coverable. `Vision` now
  orchestrates + logs only. Enums `SingleTagStrategy {PNP, TRIG_SOLVE}` and `CovarianceModel
  {ISOTROPIC, ANISOTROPIC}` live here.
- **`SingleTagTrigSolver.java` (new)**: pure trig-solve math (idea: 6328 via PhotonVision
  `PNP_DISTANCE_TRIG_SOLVE`; 1678 C2026 production). `reconstructCameraToTag(...)` inverts the IO
  composition exactly (no new logged transform needed); `solve(...)` re-anchors single-tag XY on the
  known tag pose using the odometry-buffer heading. PnP rotation drops out — ambiguity-immune XY.
  Unit test proves a corrupted PnP rotation does not move the solution.
- **`VisionIO.PoseObservation`** gained `primaryTagId` (single-tag: THE tag; multi-tag: first used
  tag; -1 unknown) — anchors trig-solve reconstruction + anisotropic ray angle, and names the tag in
  logs. Both IO branches in `VisionIOPhotonVision` populate it.
- **`Vision`**: new `HeadingSampler` constructor arg (wired to `DriveSubsystem.sampleHeadingAt`, which
  samples CTRE's odometry pose-history buffer at the frame's FPGA timestamp via `fpgaToCurrentTime` —
  same time-base fix as fusion). In TRIG_SOLVE mode single-tag XY is replaced post-gates (same frames
  fused in both modes — fair A/B); falls back to PnP when the buffer/tag lookup cannot answer.
  New logs: `Vision/Summary/TrigSolvedPoses`, `Vision/Camera*/LastUsedTrigSolve`, `Vision/Modes/*`
  (active strategy + covariance model every loop, so every log names its configuration).
- **Anisotropic covariance** (idea: 5940 2026): `sigma = C * d^E` parallel/perpendicular to the
  camera->tag ray, rotated into field axes; theta keeps the baseline model; single-tag theta stays
  +Infinity. Coefficients in `VisionConstants.ANISO_*` are **PROVISIONAL** (match isotropic at ~2 m)
  until fitted from robot logs (test plan stage R2).
- **`RobotContainer`**: every chooser option now runs through `withVisionModes(...)` (baselines set
  PNP+ISOTROPIC explicitly — a leftover experiment mode can never contaminate a run). Four new A/B
  autos: "AB: Precision To Tag Board (TrigSolve)", "AB: VisionTest spatial handoff (TrigSolve)",
  "AB: VisionTest spatial handoff (AnisoCov)", "AB: VisionTest spatial handoff (TrigSolve+AnisoCov)".
  Shared `spatialHandoffAuto()` helper.
- **`build.gradle`**: jacoco plugin + report (`build/reports/jacoco/test/html/index.html`) + the 90%
  gate on `VisionPolicy`, `SingleTagTrigSolver`, `AimingCalculator` (hardware-bound classes are
  validated by the sim/robot test plan instead — see comment in build.gradle).

Deliberately NOT changed: `CONSTRAINED_SOLVEPNP` (documented follow-up, needs the PhotonPoseEstimator
API path), PathPlanner `SwerveSetpointGenerator` (deferred — needs real-robot characterization values
to be meaningful; see DESIGN_DECISIONS), single-tag ambiguity gate kept ON in trig mode (fair A/B;
loosening it is a later knob).

**Next steps (human): run the A/B test sequence** in `VISION_AND_TRAJECTORY_TEST_PLAN.md`
("2026-07-16 A/B validation plan") — sim first (S1–S5), then the real-robot sequence (R0–R5) when
robot time is available. The walkthrough (`CODE_WALKTHROUGH_VISION_AND_TRAJECTORY.md`) has a new A7
section + re-synced line references for the changed files.

## 2026-07-16 Software/technique survey (no code changes yet)

Mentor asked: (a) newer versions of software we use, (b) new/updated relevant code from top teams,
(c) newer/better *camera-based* precision techniques for localization + trajectory driving
(QuestNav explicitly excluded — worked fine but too big; PhotonVision remains the platform).

**Dependency check — everything is current, no upgrades available:** WPILib/GradleRIO 2026.2.1
(latest, Jan 16), AdvantageKit 26.0.2 (latest, Mar 19), PhotonLib/PhotonVision v2026.3.4 (latest,
Apr 10), PathPlanner(Lib) 2026.1.2 (latest, Jan 12), Phoenix 6 26.3.0 (latest, May 26). Choreo (not
used, documented upgrade) is at v2026.0.3 — added `warmupCmd()` (fixes first-auto classload delay)
and `mirrorY()` left/right trajectory flipping.

**Research clones refreshed (`S:\MechaRAMS\_research_clones`):** 1768, 3467, 6328, 6995 — all
already at origin tips; no new commits since the 2026-06-30 review (6328's last publish 06-27).
**New clones added:** `2910-2026` (2026CompetitionRobot-Public, Einstein finalist — Limelight-based,
less directly relevant), `1678-2026` (C2026-Public — PhotonVision), `5940-2026` (2026-Onseason —
PhotonVision + they maintain their own photonvision fork). 4414 (world champion) and 254 have no
public 2026 robot code. 2026 champs: 4414/1323/4065 def. 2910/2046/868.

**Camera-based precision techniques worth adopting (ranked):**

1. **PhotonPoseEstimator `PNP_DISTANCE_TRIG_SOLVE`** (in our PhotonLib v2026.3.4 already; roboRIO-
   side; needs `addHeadingData(...)` every loop). Uses gyro heading + tag distance to solve XY —
   the 6328 trig-solve idea, now upstream in PhotonVision. **1678 runs it in production** (see
   `frc/lib/io/vision/photon/AprilTagPhotonCameraIO.java`). Fits our thesis exactly: we already
   refuse single-tag headings; this makes single-tag *XY* better instead of just gating it.
   Caveats: must feed heading each frame and invalidate after pose reset (our reset quarantine
   already provides the hook). -> IMPLEMENTED same day, see entry above.
2. **`CONSTRAINED_SOLVEPNP`** (same PhotonLib, roboRIO-side, <= 2 ms): re-solves PnP with a
   "drivebase flat on floor" constraint + heading prior. Docs-recommended flow: coproc multitag ->
   fallback single-tag -> constrained refine. Candidate second step after (1).
3. **Anisotropic, log-fitted covariance** (5940 `subsystems/vision/Vision.java` ~495-555): fitted
   power-law sigmas *parallel vs perpendicular to the camera->tag ray*, rotated into field axes;
   theta sigma blended by ray angle. Strictly better than our isotropic `dist^2/tagCount^2` and gives
   a concrete recipe for our open "tune covariance from logs" follow-up — likely also the fix for
   the post-command drift watch item. -> IMPLEMENTED same day (theta blending deferred), see above.
4. **PathPlanner `SwerveSetpointGenerator`** (254-derived, in PPLib we already ship): limits module
   accel/torque to the friction envelope -> prevents wheel slip -> cleaner odometry between vision
   frames. Benefits both path following and the precision command. Integration point: wrap the
   output consumer in `configurePathPlanner` and `DriveToPosePrecisionCommand`.
5. Minor (1678 `PIDToPoseCommand`): feed the controller a *lookahead-interpolated* pose
   (latency compensation). Their settle gate (DelayedBoolean) matches ours — design confirmed.
6. Hardware-stage note: PhotonVision's mrcal-based calibration for Stage 2 intrinsics.



## 2026-07-01 Sim validation results (Codex analysis of `logs/sim/akit_26-07-01_16-27-18.wpilog`)

The human reran the reset test + all four autos and Codex analyzed the log. Outcome: **validation pass
closed; no architecture changes needed.**

- **Reset quarantine works**: every reset showed `acc100=0`, `resetSupp100>0` — no accepted vision poses
  fused in the first 100 ms after reset; no A-press reset occurred during the evaluated autos (binding
  gate also confirmed).
- **Precision timeout fixed**: all four `DriveToPose` runs finished `timedOut=false` (the 0.03→0.04 m
  tolerance loosening did its job).
- **Auto results**: Precision To Tag Board ended 0.045 m from (4.25, 2.0); pure PathPlanner "VisionTest"
  ended 0.066 m from its own path endpoint (3.6, 2.0) — correct, it never targets the tag board;
  sequential handoff ended 0.027 m from target; spatial handoff's `DriveToPose` finished 0.034 m from
  target with no timeout.
- **Design decision (mentor + Codex)**: pure PathPlanner is transit-only, never the final authority.
  **Spatial handoff (`handoffFrom`, x > 3.3 trigger) is the primary competition pattern going forward**;
  sequential handoff kept as a debug/reference auto; pure PathPlanner kept as a baseline test only.
- **Watch item (not urgent)**: after the spatial-handoff command finished at (4.217, 2.007), the pose
  drifted to (4.180, 1.938) by disable — the command ended cleanly but vision/estimator updates kept
  moving the pose. If this persists, look at vision covariance/noise tuning, not the command.
- **Log hygiene**: the log still ends with a small EOF `READ_WARNING newLimit > capacity` (not perfectly
  finalized) but all auto data was readable. For a pristine log: disable, stop the sim, then open.

Build re-verified 2026-07-01 in the Cowork Linux sandbox (Temurin JDK 17.0.19, Gradle 8.11):
`compileJava` SUCCESS; `test` 30/30 PASS (VisionPolicyTest 22, AimingCalculatorTest 5,
DriveToPosePrecisionMathTest 3). Note: the sandbox build ran from a copy in `/tmp/vtc`; the Windows
`.\gradlew.bat` + wpilib JDK path in `AGENTS.md` remains the canonical build.

Next candidates (tuning only, per Codex): PathPlanner path-following gains if coarse accuracy < ~6 cm
matters; vision covariance if post-command drift shows up again. Otherwise proceed to real-hardware
calibration (Stages 2–7 of `CALIBRATION_AND_TEST_PROCESS.md`).

## 2026-06-30 Claude Rebuild Summary (read this first)

The Codex pilot was reviewed against the **actual** top-team code (cloned to `S:\MechaRAMS\_research_clones`:
6328, 3467, 1768, 6995) and substantially upgraded. Full write-ups:
`CODEX_CODE_REVIEW_AND_GAP_ANALYSIS.md` and `DESIGN_DECISIONS_AND_REJECTED_IDEAS.md`.

What changed in code:

- **Bug fixes**: vision timestamp now converted to CTRE time base (`Utils.fpgaToCurrentTime`); NaN/Inf
  rejection; precision command got a safety timeout + full logging; fused `Drive/Pose` is now logged.
- **Vision rebuilt on the AdvantageKit IO-layer** (`subsystems/vision/`): `VisionIO` (`@AutoLog`),
  `VisionIOPhotonVision`, `VisionIOPhotonVisionSim` (**working PhotonVision simulation** — the keystone),
  and a `Vision` subsystem with single-tag heading = ∞, per-camera std-dev factors, rejection-reason
  enums, innovation logging, and early-auto vision ignore. Old `VisionSubsystem.java` deleted.
- **Precision controller** `DriveToPosePrecisionCommand` rewritten: profiled x/y/θ + velocity FF +
  settle gate + safety timeout + logging; `handoffFrom(...)` coarse→precise helper.
- **Chassis aiming (no turret/GPM)**: `util/AimingCalculator` + `AimAtGoalCommand` +
  `DriveAndAimCommand`, configurable `AimConstants.GOAL_POSITION`, shoot-on-move lookahead (teaching).
- **Odometry 100 → 250 Hz** (roboRIO/CANivore rate — NOT an Orange Pi rate).
- **4-camera-ready** (front + back transforms in `VisionConstants`); **active config = 2 cameras / 1
  Orange Pi** (recommended start; scale to 4/2 by uncommenting in `RobotContainer`).
- **`VisionTest` PathPlanner path + auto** authored; sequential + spatial-handoff auto options added.
- **Headless JUnit tests** (`src/test/...`): vision policy + aiming geometry. `./gradlew.bat test` green.

Build/test verified: `compileJava` SUCCESS; `test` 30/30 PASS (Java 17 WPILib JDK).

Second sim log follow-up (2026-07-01, from Codex analysis): (1) reset still leaked because sim-delayed
frames whose timestamp slipped past the reset got fused -> added a fixed post-reset **quarantine**
(`RESET_QUARANTINE_SECONDS = 0.35`) on top of the timestamp check (`isResetSuppressed` helper + tests).
(2) An A-press during enabled auto reset the pose mid-run and invalidated the trajectory -> A/B pose
bindings are now gated to non-autonomous. (3) The log couldn't tell which chooser option ran each auto
period -> switched the auto chooser to AdvantageKit `LoggedDashboardChooser`, which logs the selected auto
name. (4) Precision translation tolerance loosened 0.03 -> 0.04 m (runs landed ~0.027 m but timed out
holding the tighter window). Confirmed non-bug: PathPlanner "VisionTest" auto correctly ends at the path
endpoint (3.6, 2.0), not the tag-board target. Next: rerun reset test + the two precision autos from a
cleanly-finalized log (disable, stop sim, then open).

Sim log follow-up (2026-07-01): the first real sim run validated the pipeline (precision final error
0.027 m / 0.006 deg; vision accepted poses present) but exposed a real bug -- pressing A (reset) bounced
the pose back because stale in-flight PhotonVision frames (timestamped before the reset) were fused. Fix:
`DriveSubsystem` records `lastResetTimeSeconds`; `Vision` discards frames captured before the last reset
(pure `isPreResetFrame` helper + test) and logs them to `Vision/Summary/ResetSuppressedPoses`. Also added
`Vision/Layout/TagPoses` (all layout tags, every loop) so AdvantageScope can render the board even when no
camera sees a tag, and documented the AdvantageScope `/RealOutputs/` file-replay prefix. Next: rerun the
reset test (drive away, press A once -> should snap and stay), then the autos. Minor open item: several
`DriveToPose` runs hit the 4 s safety timeout while already near the target -- worth a small gains/settle
tune later.

Codex peer review round 4 incorporated 2026-06-30: single-tag ambiguity gate now also rejects unknown
(`-1`, PhotonVision "uncomputable") ambiguity, not just above-threshold; `freshTargetX` uses `abs(...)`
so a future-dated bearing (clock glitch) is rejected too (fully bounded). +2 tests. Declined the
negative-`averageTagDistance` guard on purpose: that value is a vector norm (always ≥ 0), so it can't be
negative — the guard would be dead code (unlike ambiguity's real `-1`). Static review is now closed; next
step is the sim/log validation pass.

Codex peer review round 3 incorporated 2026-06-30: `rejectionReason` now also rejects non-finite
`averageTagDistance` and `ambiguity` (a NaN distance → NaN std-devs; a NaN ambiguity silently passes the
ambiguity gate) + 2 tests; multi-tag IO branch guards `!targets.isEmpty()` before dividing. Next review is
simulation/log-based (run both handoff autos; inspect `Drive/Pose`, `Vision/Summary/*`, `DriveToPose/*`,
and that the handoff fires near x > 3.3), not more code-review loops.

Codex deep (algorithmic) review round 1 incorporated 2026-06-30: `getTargetX` returns `Optional` + a
`hasTarget` flag (no phantom-zero); vision logs split fused `AcceptedPoses` from `AutoSuppressedPoses`;
covariance is now `dist²/tagCount²` (matches 6328/6995 + the docs); precision command clamps translation
as a **vector** (no √2× diagonal), resets stale log flags in `initialize()`; `AimingCalculator` recomputes
TOF after the convergence loop; added a real spatial-interrupting handoff auto ("VisionTest (spatial
handoff)") using `handoffFrom`.

Codex peer review round 2 incorporated 2026-06-30: `getTargetX` now also rejects **stale** bearings
(`TargetObservation` carries a frame timestamp; pure `freshTargetX` helper + tests); extracted the
vector clamp to a unit-tested `clampTranslationToMax`; added tests for `getTargetX`, the `/tagCount²`
covariance, and the TOF-matches-final-distance fix (16 → 23 tests); synced AGENTS/skills/ARCHITECTURE
covariance + "timestamp-ordered" wording and the renamed auto options. Walkthrough line numbers re-synced.

Documentation + AI patterns fully synced to the rebuild (2026-06-30):

- Architecture rewritten (`ARCHITECTURE_AND_DEPLOYMENT.md`, high-level + detailed). Test plan + sim
  runbook updated (new `Vision/Camera*`, `Drive/Pose`, `DriveToPose/*`, `Aim/*` channels; aiming test;
  sim now produces frames; `VisionTest` auto exists). `AGENTS.md` rules updated; `.claude/commands`
  refreshed (vision/trajectory/safety/session) + new `aiming-review`.
- New docs: `CODEX_CODE_REVIEW_AND_GAP_ANALYSIS.md`, `DESIGN_DECISIONS_AND_REJECTED_IDEAS.md`,
  `CALIBRATION_AND_TEST_PROCESS.md`, `ADVANTAGESCOPE_SETUP.md`, `AI_REGENERATION_PROMPTS.md`,
  `CODE_WALKTHROUGH_VISION_AND_TRAJECTORY.md` (line-specific student walkthrough of the vision +
  trajectory code, with the design decision behind each block).
- **AI generation kit** for fully AI-generated code: skills `frc-project-bootstrap`,
  `frc-swerve-drivetrain`, `frc-vision-localization`, `frc-trajectory-precision`, `frc-aiming`,
  `frc-simulation-and-testing`, plus an ordered master regeneration playbook in
  `AI_REGENERATION_PROMPTS.md`.

Hardware decisions: Orange Pi only (no Mac mini). PhotonVision cameras run ~30–50 fps; 250 Hz is the
roboRIO odometry thread. Color OV9782 kept for pilot.

Remaining (handed to next session): per-doc updates to ARCHITECTURE/TEST_PLAN; skills/prompts conversion;
real-hardware calibration (Stages 2–7 of `CALIBRATION_AND_TEST_PROCESS.md`).

---


## Current Objective

Create a 2027 prototyping Java project for the 2025 MechaRAMS chassis that tests PhotonVision AprilTag localization, CTRE swerve trajectory driving, precision final-pose control, AdvantageKit logging/replay, and AI-assisted development process.

## Current Architecture Decisions

- Use PhotonVision on one Orange Pi with two USB2 Arducam OV9782 global-shutter color cameras.
- Start with two cameras, not four cameras/two Orange Pis. Add more only if logs show coverage or bandwidth limits.
- Mount cameras at the front-left and front-right corners, above/inside the front swerve modules, cross-eyed toward the robot centerline.
- Keep final pose fusion and drivetrain control on the roboRIO.
- Use 2025 drivetrain CAN IDs, Pigeon ID, and module offsets.
- Treat SDS MK4 L3 as 6.12:1 drive, 12.8:1 steer, 4 inch wheel until characterization replaces it.
- Use AdvantageKit for logs and replay-oriented debugging.
- Use PathPlanner for coarse motion and a separate tolerance/settle command for final precision.

## Current Dependency Versions

- CTRE Phoenix 6: `26.3.0` (`vendordeps\Phoenix6-26.3.0.json`)
- AdvantageKit: `26.0.2` (`vendordeps\AdvantageKit.json`)
- PhotonLib: `v2026.3.4` (`vendordeps\photonlib.json`)
- PathPlannerLib: `2026.1.2` (`vendordeps\PathplannerLib-2026.1.2.json`)

## Implemented Files

- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\AGENTS.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\CLAUDE.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.claude\commands\session-update.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.claude\commands\vision-review.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.claude\commands\trajectory-review.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.claude\commands\safety-audit.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.codex\skills\frc-vision-localization\SKILL.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\.codex\skills\frc-trajectory-precision\SKILL.md`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\Constants.java`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\Robot.java`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\RobotContainer.java`
- `...\src\main\java\frc\robot\subsystems\DriveSubsystem.java`
- `...\src\main\java\frc\robot\subsystems\vision\VisionIO.java` (post-rebuild; replaced `VisionSubsystem.java`)
- `...\src\main\java\frc\robot\subsystems\vision\VisionIOPhotonVision.java`
- `...\src\main\java\frc\robot\subsystems\vision\VisionIOPhotonVisionSim.java`
- `...\src\main\java\frc\robot\subsystems\vision\Vision.java`
- `...\src\main\java\frc\robot\commands\DriveManuallyCommand.java`
- `...\src\main\java\frc\robot\commands\DriveToPosePrecisionCommand.java`
- `...\src\main\java\frc\robot\commands\AimAtGoalCommand.java`
- `...\src\main\java\frc\robot\commands\DriveAndAimCommand.java`
- `...\src\main\java\frc\robot\util\AimingCalculator.java`
- `...\src\test\java\frc\robot\subsystems\vision\VisionPolicyTest.java`
- `...\src\test\java\frc\robot\util\AimingCalculatorTest.java`
- `...\src\main\deploy\pathplanner\paths\VisionTestPath.path` + `autos\VisionTest.auto`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\deploy\apriltags\mecharams-two-tag-layout.json`
- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\deploy\pathplanner\settings.json`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\AI_PROMPTS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\INITIAL_PROMPT_REORGANIZED.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\ROBOT_CONTROLS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\VISION_AND_TRAJECTORY_TEST_PLAN.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\SIMULATION_RUNBOOK.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\ARCHITECTURE_AND_DEPLOYMENT.md`

## Verification State

- `.\gradlew.bat compileJava` was run with `JAVA_HOME=C:\Users\Public\wpilib\2026\jdk`.
- Build succeeded on 2026-06-30.
- Deprecated command scheduling and PhotonVision pose-estimator API calls were removed after the first successful compile reported warnings.
- Code-level "Idea traceability" comments were added to the main robot, controls, drivetrain, vision, constants, manual drive, and precision-drive files.
- Latest compile output after documentation pass: `BUILD SUCCESSFUL`, 1 actionable task executed, no deprecation warnings printed.
- User updated CTRE Phoenix 6 and AdvantageKit to latest available versions; compile still worked.

## Known Follow-Ups

Open (real-hardware / tuning work):

- Confirm actual measured camera mounts and update robot-to-camera transforms.
- Run camera intrinsic calibration in PhotonVision for each Arducam.
- Run drivetrain characterization and replace wheel radius/feedforward gains.
- Tune vision covariance baselines + per-camera factors from logs once cameras are mounted.

Done in the 2026-06-30 rebuild (kept here for history): IO-layer split, PhotonVision simulation, the
`VisionTest` path/auto, the early-auto vision gate (now enforced + unit-tested), and Java 17 build/test
verification.

## Rules For Next AI Session

- Read this file first.
- Update `AI_PROMPTS.md` with any new mentor prompts that affect design.
- Update this file before ending the session.
- Do not guess hardware constants. If a value is not measured, mark it as provisional.
