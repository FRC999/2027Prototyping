# Session State - VisionTestingAndCalibration

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
