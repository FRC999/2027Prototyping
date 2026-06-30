# Design Decisions and Deliberately-Rejected Ideas

Author: Claude (Opus 4.8) session, 2026-06-30
Purpose: For every strong idea seen in the top-team code, record whether it was implemented, and if
**not**, *why* — and what would make us revisit it. This lets Team 999 change course if a chosen path
does not prove fruitful, instead of rediscovering the trade-offs later.

Legend: ✅ implemented · ◑ partial / hook only · ❌ not implemented (with reason)

## Localization / fusion

| Idea (source) | Status | Decision and why |
| --- | --- | --- |
| Fuse all frames (each at its own timestamp via CTRE's odometry buffer — no explicit sort), dist²/tagCount² covariance, multi-tag-only heading, physical rejection (6328/3467/1768) | ✅ | The core thesis; implemented in `Vision`. This is what our 2026 robot got wrong. |
| AdvantageKit IO-layer for replay (6328/1768 template) | ✅ | `VisionIO` + `VisionIOPhotonVision` + `VisionIOPhotonVisionSim`. Restores deterministic replay of vision. |
| Single-tag heading = +∞ std dev (6328) | ✅ | We *improved on* the 1768 template, which trusts angular even for single tags — exactly our 2026 bug. |
| Per-camera std-dev factor (6328 `stdDevFactor`) | ✅ | `CAMERA_STD_DEV_FACTORS`. |
| NaN/Inf rejection (strategy doc rule 7) | ✅ | Codex omitted it; a degenerate solve can poison the estimator. |
| Custom roboRIO pose estimator replacing CTRE's (6328 `RobotState`, 3467 `PoseEstimator`) | ❌ | We kept **CTRE's built-in `SwerveDrivetrain` estimator** and feed it. Reason: CTRE runs its own 250 Hz odometry-buffer estimator with tight sim integration; replacing it means re-implementing odometry/kinematics and losing that integration, for a prototype on a flat floor. **Revisit if** logs show CTRE's estimator is the limit (e.g. we need skid/tilt logic *inside* the filter). |
| Per-wheel skid rejection + odometry σ inflation (3467) | ◑ | Concept documented + per-camera trust added, but live skid detection needs per-module access *inside* the filter, which CTRE's estimator doesn't expose. **Revisit** with a custom estimator if skidding corrupts pose on carpet. |
| Tilt-based odometry distrust to 25° (6328) | ❌ | Lives inside the custom filter. Low value on a flat bench. **Revisit** for a real field with ramps / heavy contact. |
| Single-tag dual-solution disambiguation vs gyro (6328 Northstar) | ❌ (N/A) | PhotonVision returns a single best single-tag pose on the coprocessor (lowest reprojection), so we never receive the two candidates Northstar emits. We gate single tags by ambiguity instead. **Revisit** only if we switch to a pipeline that returns both solutions. |
| Statistical n-σ innovation gate (3467) | ◑ | We log an innovation *distance* per accepted frame, but a true n-σ needs the estimator's covariance, which CTRE doesn't expose. **Revisit** with a custom estimator. |
| Disconnect alerts + early-auto vision ignore (6328) | ✅ | `Alert` per camera; `AUTO_VISION_IGNORE_SECONDS`. |

## Aiming

| Idea (source) | Status | Decision and why |
| --- | --- | --- |
| Chassis aim at a field point via `atan2(goal − robot)` (6995/1768/6328) | ✅ | `AimingCalculator` + `AimAtGoalCommand` / `DriveAndAimCommand`. |
| Velocity-compensated shoot-on-move lookahead (6328 `LaunchCalculator`, 1768 `ShootingUtil`) | ✅ | Implemented as a documented teaching artifact with a modeled time-of-flight; fully unit-tested. |
| Turret (2910/6995) | ❌ | **Explicitly excluded by mentor** — no turret or game-piece mechanism in this prototype. Code keeps `latestTargetObservation` (tag bearing) so a turret/boresight loop can be added later. |
| Launcher-off-center correction, drag time constant, hood/flywheel maps, "boxes of bad" (6328) | ❌ | These belong to a real **shooter** mechanism we are not building. **Revisit** when (if) the 2027 game is a shooting game and a launcher exists. |
| Boresight/turret camera for the final few degrees of aim (research hybrid rec) | ❌ (hardware) | Needs an added camera. The software is structured for it (`Vision.getTargetX`). **Revisit** for fine aiming once a target/goal exists; this is the one likely hardware addition. |

## Trajectory / autos

| Idea (source) | Status | Decision and why |
| --- | --- | --- |
| Profiled DriveToPose w/ FF fade + tolerance + logging (6328) | ✅ | Implemented via profiled PID on x/y/θ (1768's approach), which decelerates to zero at the goal. |
| Settle-gated finish + safety timeout (1768 `cmdWithAccuracy`) | ✅ | Settle timer + hard `PRECISION_SAFETY_TIMEOUT_SECONDS`. |
| Coarse trajectory → DriveToPose spatial handoff (6328 `AutoCommands`) | ✅ | `handoffFrom(...)` helper + the "VisionTest (spatial handoff)" auto (interrupts the path at x>3.3 m). |
| **Choreo** time-optimal trajectories + alliance flipping (6328/1768/6995) | ❌ (documented) | Kept **PathPlanner** as the active tool. Reasons: it was already wired; authoring Choreo `.traj` needs the Choreo GUI (cannot run headless here); and on the straight two-tag bench Choreo's time-optimal advantage is marginal (research-log: 2026 paths are mostly straight → near-equivalent). **Revisit** when paths get complex — add the ChoreoLib vendordep + an `AutoFactory(..., true /*useAllianceFlipping*/, ...)` exactly as 6995 does. |
| `LoggedTunableNumber` dashboard-tunable gains (6328) | ❌ | Used plain `Constants` to keep the teaching code self-contained. **Revisit** for on-the-fly field tuning; it is a small, additive change. |
| 1768 `antiBeach` (un-stick after a bump) | ❌ | Game/field-specific; no bumps on our bench. **Revisit** if a real field has obstacles. |

## Architecture / compute

| Idea (source) | Status | Decision and why |
| --- | --- | --- |
| Offboard robot brain on a Mac mini + custom Northstar + Idun IO bridge (6328) | ❌ | **Explicitly out** — Orange Pi only. The research already concluded: copy 6328's *fusion discipline*, not its *compute architecture*. **Revisit** never for this prototype; it is a multi-year infrastructure project. |
| Object detection / game-piece pipelines (6328) | ❌ | Game-specific, no 2027 game. Out of scope for a localization/precision prototype. |
| Full AdvantageKit replay for the **drivetrain** (IO-split the swerve) | ◑ | Vision is replayable; the CTRE drivetrain is not behind an IO layer because CTRE manages its own threads/sim and does not split cleanly. Vision replay is the high-value part. **Revisit** only if drivetrain-input replay becomes necessary. |
| 250 Hz odometry thread (6328/3467/5687) | ✅ | Set on CTRE/CANivore. NOTE: this is roboRIO + CAN-FD work, **not** an Orange Pi / PhotonVision rate — the cameras stay at ~30–50 fps and are fused by timestamp against the dense odometry history. |

## Camera hardware

| Idea | Status | Decision and why |
| --- | --- | --- |
| 2 cameras / 1 Orange Pi to start | ✅ (recommended) | Simplest thing that proves the pipeline; the established-safe OPi5 budget. Active config. |
| 4 cameras / 2 Orange Pis | ◑ (ready) | Transforms + names + factors exist; wiring is a one-line uncomment. Worth it for **competition** coverage + failure isolation, not for the bench test. |
| Monochrome global-shutter (OV9281) instead of color OV9782 | ❌ (current hardware kept) | Color works; mono is mildly better for tags (no Bayer softening, better low light). **Revisit** only if low-light tag detection disappoints in testing. |

## Summary of the "we chose differently" calls

The two biggest judgment calls a future team might reverse:

1. **Keep CTRE's estimator vs. build a custom one (6328/3467).** We kept CTRE's for integration + simplicity
   and layered diagnostics on top. If precision plateaus and logs implicate the estimator (skid, tilt,
   no covariance access), porting 3467's standalone `PoseEstimator` (GPLv3, designed to be reused) is the
   clean next step.
2. **PathPlanner vs. Choreo.** We kept PathPlanner for the pilot. If autos get complex/time-critical,
   Choreo is the documented upgrade and interoperates (PathPlannerLib can follow `.traj`).
