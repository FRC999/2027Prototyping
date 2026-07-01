# Session State - VisionTestingAndCalibration

Last updated: 2026-06-30 (Claude Opus 4.8 best-in-breed rebuild)

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

Build/test verified: `compileJava` SUCCESS; `test` 27/27 PASS (Java 17 WPILib JDK).

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
