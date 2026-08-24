# Session State - VisionTestingAndCalibration

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
