# Session State - VisionTestingAndCalibration

## 2026-09-02 spatial-handoff reverse incident corrected with a measured-start path

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
