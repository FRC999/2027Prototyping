# Codex Code Review + Top-Team Gap Analysis

Author: Claude (Opus 4.8) session, 2026-06-29
Purpose: Evaluate the code Codex generated for `VisionTestingAndCalibration` against the **actual published code** of the top teams that prior research identified, find what was missed / could be improved / is wrong, and define a sim-first roadmap toward genuinely high-precision localization, aiming, and autos.

This is written as a **teaching document**. Every recommendation cites the real source file it came from so a student can open the original and read it.

## How this review was done

The repos catalogued in `research-log.md` were shallow-cloned to `S:\MechaRAMS\_research_clones\` (outside the project git root) and read directly:

| Team | Repo | Clone dir |
| --- | --- | --- |
| 6328 Mechanical Advantage | `Mechanical-Advantage/RobotCode2026Public` | `_research_clones/6328` |
| 3467 Windham Windup | `WHS-FRC-3467/Skip-5.16-Perry` | `_research_clones/3467` |
| 1768 Nashoba | `Nashoba-Robotics/2026NashobaRobotics` | `_research_clones/1768` |
| 6995 | `frc6995/Robot-2026` | `_research_clones/6995` |

(These clones are scratch; they are not committed. Re-clone with `git -c http.sslBackend=schannel clone --depth 1 <url>` if they are gone.)

---

## 1. Overall verdict

**Your instinct was correct.** Codex's localization *fusion core* is conceptually right and captures the single most important top-team idea (fuse all frames, timestamp-ordered, distance²/tag-count² covariance, multi-tag-only heading, physical rejection). That part is **not** too simple — it is the exact thing your 2026 robot got wrong.

But the generated code implements roughly the **foundation layer only**. Compared with the real code of the teams in the research, it is missing most of the sophistication — and it contains a few concrete bugs. The biggest omissions line up exactly with your stated pain points: **aiming** and **trajectory precision**, plus your hard requirement that **everything works in simulation** (which the current code cannot do at all).

---

## 2. Confirmed bugs / correctness issues in Codex's code

These are not stylistic — they will measurably hurt precision or can break behavior.

### BUG 1 (HIGH) — Vision timestamp is in the wrong time base for CTRE

`VisionSubsystem.periodic()` passes `observation.timestampSeconds()` (a PhotonVision/WPILib **FPGA** timestamp) straight into `drive.addVisionMeasurement(...)`. CTRE Phoenix 6's `SwerveDrivetrain` keeps its odometry buffer on the **Phoenix time base** (`Utils.getCurrentTimeSeconds()`), which is offset from FPGA time. CTRE provides `com.ctre.phoenix6.Utils.fpgaToCurrentTime(double)` specifically to convert. Feeding raw FPGA time means every vision sample is fused against the **wrong odometry sample** — latency compensation is silently broken, which is precisely the kind of "a few cm/degrees off" error that ruined 2026 aiming.

Fix: `double ctreTime = Utils.fpgaToCurrentTime(observation.timestampSeconds());` and pass `ctreTime`. (Verify the sign/offset on your installed Phoenix 6 version with a quick sim log.)

### BUG 2 (MEDIUM) — No NaN / infinite rejection

`Vision-Strategy-2027-v2.md` §7 lists "NaN/infinite values" as a required rejection gate, and the `AGENTS.md` vision rules imply it, but `VisionSubsystem.rejectionReason(...)` never checks. A degenerate solve (`NaN`/`Inf` pose) will poison CTRE's estimator and is hard to recover from. Add an explicit `!Double.isFinite(...)` gate on x/y/z/theta.

### BUG 3 (MEDIUM) — Precision command can run forever + logs nothing

`DriveToPosePrecisionCommand` finishes **only** on settle. With bad tuning, an obstacle, or a pose it cannot reach, `isFinished()` never returns true → the command hangs until interrupted. Every reference team uses a **timeout as a safety backstop** (1768 `AutoModeBase.cmdWithAccuracy` wraps `.withTimeout(totalTime + slack)`; 1768 §80 research note). It also logs **nothing**, violating `AGENTS.md` ("Every precision test must log target pose, measured final pose, translation error, rotation error, and settle time"). 6328's `DriveToPose` logs all of these every loop (`DriveToPose.java:240-255`).

### ISSUE 4 (MEDIUM) — Fused drivetrain pose is never logged to AdvantageKit

Nothing calls `Logger.recordOutput("Drive/Pose", ...)`. For a project whose whole point is replayable localization debugging, the estimator output is invisible in logs (only vision *inputs* are logged). 6328 logs `odometryPose` and `estimatedPose` via `@AutoLogOutput` (`RobotState.java:41-42`).

### ISSUE 5 (LOW) — `theta` std-dev for single tag is a large finite number, not infinity

Codex uses `SINGLE_TAG_THETA_STD_DEV = 999999.0`. 6328 uses literal `Double.POSITIVE_INFINITY` (`Vision.java:253`). A finite-but-huge value still lets a tiny amount of bad heading leak in over many frames; true infinity is cleaner and is what the strategy doc §7.4 specifies ("set theta standard deviation to infinity").

### ISSUE 6 (LOW) — Dead code + per-loop string allocation

`VisionConstants.FALLBACK_STD_DEVS` is defined but never used. `VisionSubsystem` rebuilds log keys (`"Vision/" + name + "/..."`) every loop → needless allocation in a periodic path (the strategy doc's own §8 warns about periodic allocation). 6328 precomputes/uses indexed keys.

---

## 3. What the real top-team code does that Codex omitted

### 3.1 Vision ingestion — 6328 `Vision.java`

| Pattern | 6328 (`subsystems/vision/Vision.java`) | Codex `VisionSubsystem` | Adopt? |
| --- | --- | --- | --- |
| Single-tag **dual-pose disambiguation** | Gets BOTH PnP solutions, picks the one whose rotation is closest to current gyro heading (`Vision.java:169-203`) | Throws away ambiguous single tags entirely | **Yes** — keeps usable single-tag data your code discards |
| **Per-camera std-dev factor** | `cameras[i].stdDevFactor()` multiplies XY+theta σ (`Vision.java:246, 252`) | Both cameras identical | **Yes** — a worse-calibrated camera should count less |
| **Ignore vision early in auto** | Skips fusion until `autoTimer > autoIgnoreTimeSecs` (`Vision.java:357`) | Always fuses | **Yes** — protects a known auto start pose from a bad early frame |
| **Disconnect alerts + latency log** | Per-camera `Alert`, "connected to NT but not publishing", latency `Logger` (`Vision.java:113-129, 260-262`) | Logs a `Connected` bool only | **Yes** — pit-debuggable health |
| theta = **+∞** for non-vision-rotation | `Double.POSITIVE_INFINITY` (`Vision.java:253`) | `999999.0` | Yes (trivial) |

### 3.2 The estimator itself — two reference designs

Codex relies on **CTRE's built-in `SwerveDrivetrain` pose estimator** and only feeds it. That is a legitimate, lower-effort choice. The top teams instead run a **custom estimator on a 2 s odometry buffer**, which buys precision and debuggability:

- **6328 `RobotState.java`** — closed-form Kalman update (A=0, C=I) on a `TimeInterpolatableBuffer` pose history (`RobotState.java:198-257`). Vision is applied *at its capture timestamp* against the buffer, then odometry replays forward. Crucially: **odometry translation is scaled toward 0 as robot tilt approaches 25°** (`acos(cosPitch·cosRoll)`, `RobotState.java:133-157`) so collisions/climbs don't corrupt the pose, and **dtheta is overridden by the gyro delta** (`RobotState.java:159-168`).
- **3467 `frc/lib/posestimator/PoseEstimator.java`** (reusable, GPLv3) — adds three things Codex has none of:
  - **Per-wheel skid handling**: a `badWheels[]` flag inflates odometry σ (×0.75 linear / ×0.50 angular per bad wheel, capped) so vision gains authority during a skid (`PoseEstimator.java:78-81, 187-206`).
  - **n-σ innovation diagnostics**: computes `translationNSigma`/`rotationNSigma` of each vision correction vs. current uncertainty and **logs them** (`PoseEstimator.java:309-319, 463-464`) — replaces fixed-meter gates with a statistical view, and recovers automatically because uncertainty grows while blind.
  - **Structured rejection-reason enums** logged per observation: `ACCEPTED / RESET_LOCKED / MISSING_ODOMETRY_SAMPLE / BELOW_MIN_N_SIGMA / POSE_VALIDATOR_REJECTED` (`PoseEstimator.java:62-68`). Codex logs only a last-reason string per camera.

Recommendation for us: **keep CTRE's estimator for the pilot** (it does interpolate against an internal odometry buffer, so BUG 1's fix restores correct latency comp), but **port 3467's diagnostics layer** (n-σ logging, rejection enums, skid σ-inflation hook) on top, since 3467's `PoseEstimator` is a standalone library designed to be reused. This gives best-in-breed observability without rewriting the CTRE estimator.

### 3.3 Trajectory precision — 6328 `DriveToPose` vs Codex `DriveToPosePrecisionCommand`

6328's `DriveToPose.java` is far more capable than Codex's plain-PID version:
- **Trapezoid motion profile** on distance-to-goal, regenerated each loop toward the target (`DriveToPose.java:159-200`).
- **Feedforward that fades to zero near the goal** via `linearFFScaler` / `thetaFFScaler` (`DriveToPose.java:172-183, 201-203`) — eliminates the end-of-move overshoot/oscillation a raw PID gives.
- **Profiled theta** with continuous input (`ProfiledPIDController`, `DriveToPose.java:92-94`).
- **Tunable everything** via `LoggedTunableNumber` and **full per-loop logging** of measured/setpoint/goal (`DriveToPose.java:240-255`); tolerances 1 cm / 1°.

Codex's version has the right *shape* (field-relative PID → robot speeds, settle timer — the settle timer is a genuinely good 1768-derived idea) but none of the profiling, FF-fade, tuning, logging, or safety timeout.

**Settle-gated finish (1768 `AutoModeBase.cmdWithAccuracy`)**: completes only when translation AND rotation are within epsilon **held for `kDelayTime`**, with `.withTimeout(totalTime + slack)` as the safety backstop (`AutoModeBase.java:87-100, 173-193`). This is the cleanest "arrive, don't time-out" pattern and it bakes precision into the Choreo command itself. 1768 also ships `antiBeach` (un-stick after a bump) and `cmdWithInterrupt`.

### 3.4 Aiming — completely absent in Codex (your #1 2026 pain point)

There is **no aiming code at all** in the project. The references show two production patterns:

- **Chassis-aim + velocity-compensated lookahead (no turret)** — 6328 `LaunchCalculator.java` and 1768 `ShootingUtil.java`. Both:
  - Iterate **~20-25 times** over time-of-flight to converge a "future pose" lead for **shoot-on-the-move** (`LaunchCalculator.java:297-312`; `ShootingUtil.java:130-141`).
  - Interpolate hood angle / flywheel speed / TOF from **distance lookup maps** (`LaunchCalculator.java:155-205`; `ShootingUtil.java:55-100`).
  - Correct for the **launcher being off the robot center** (`LaunchCalculator.getDriveAngleWithLauncherOffset`, `:378-392`).
  - 6328 adds a **drag time constant** (`effectiveTOF = (1 - e^(-tof·k))/k`, `:302-304`) and a **phase-delay** lead (`:262-270`), and "boxes of bad" where it refuses to shoot (`:334-339`).
- **Pose-fed turret (cleaner local/global split)** — 6995 `TurretS.java`: `aimAtFieldPose()` computes `atan2(target − robotPose)` and converts field→robot-relative (`TurretS.java:69-94`), with `atSetpoint()` tolerance gating the shot (`:132-136`). 2910's 2024 turret went one better (closes the turret loop on the *measured tag angle* when visible, falls back to pose — see `research-log.md`).

For us (no turret on the 2025 chassis right now): build a **chassis-aim `LaunchCalculator`-style module** first, validated in sim, structured so a turret or boresight camera can be dropped in later. Log `poseBearing − cameraBearing` as the independent check you lacked in 2026 (`Vision-Strategy-2027-v2.md` §10).

### 3.5 Simulation — the missing keystone (1768 + 3467)

Codex's `SIMULATION_RUNBOOK.md` admits vision produces **zero frames** in sim. Both 1768 and 3467 ship the fix and it is tiny:

**1768 `VisionIOPhotonVisionSim.java`** (entire file ~55 lines):
```java
visionSim = new VisionSystemSim("main");
visionSim.addAprilTags(aprilTagLayout);
cameraSim = new PhotonCameraSim(camera, new SimCameraProperties(), aprilTagLayout);
visionSim.addCamera(cameraSim, robotToCamera);
// each loop:
visionSim.update(poseSupplier.get());   // feed it the true sim pose; PhotonVision returns synthetic frames
```
This is the single highest-leverage thing to add: once it exists, **every** localization/aiming/auto improvement becomes testable on the desktop against your real two-tag layout. 3467 has the same idea plus configured `SimCameraProperties` (resolution, FPS, latency, calibration error) in `VisionIOPhotonVisionSim.java`, which lets you simulate the OV9782's actual specs.

### 3.6 Choreo + alliance flipping (6328, 1768, 6995)

Codex uses PathPlanner only and the `VisionTest` auto doesn't exist. The references favor **Choreo** (time-optimal): 6995 drives it live via `new AutoFactory(pose, reset, follow, true /*useAllianceFlipping*/, …)` — one boolean flips red/blue (`research-log.md` confirms, kills our old "Choreo can't auto-flip" belief). 1768 uses Choreo `AutoTrajectory` with `.mirrorY()`. Not urgent for the pilot, but the **IO/architecture should not hard-assume PathPlanner**.

---

## 4. Architecture gap: AdvantageKit is a logger, not a replay harness

Codex reads `PhotonCamera` and CTRE devices **directly** inside subsystems. Every reference team (6328, 3467, 1768) uses the **AdvantageKit IO-layer split** (`VisionIO` + `VisionIOPhotonVision` + `VisionIOPhotonVisionSim` + `…InputsAutoLogged`). Without it, logs are not deterministically replayable — you lose the single biggest debugging superpower the strategy doc keeps citing. Since we're going ambitious + sim-first, **adopt the IO-layer pattern now** (it is also what makes the PhotonVision sim drop-in clean, as 1768/3467 show).

---

## 5. Hardware verdict (your Amazon cameras)

The camera you specified — **Arducam B0CLXZ29F9: OV9782, 1 MP (1280×800), global shutter, color, USB 2.0 UVC, low-distortion M12 lens, up to 100 fps** — is **sufficient for a strong localization pilot**:
- Global shutter ✅ (essential on a moving robot; rolling shutter smears tag corners).
- 1280×800 ✅ is the established AprilTag resolution (`research-log.md` pt 4).
- USB 2.0 ⚠️ limits you to ~2 tag cameras per Orange Pi 5 at 1280×800 ~30 fps (CD consensus, `research-log.md` pt 4). This matches your 2-cam/1-Pi plan. To scale to 3-4 cams, add Orange Pis, not cameras-per-Pi.
- Color (not mono) ⚠️ works, but mono OV9281 is mildly preferred for tags (no Bayer softening, better low light). Keep color for the pilot; revisit only if low-light tag detection disappoints.

**For high-precision *aiming* specifically:** the front-corner 2-camera layout is a *localization* geometry, not an *aiming* geometry. The research's clear conclusion is a **hybrid**: global-pose coarse aim + a **boresight/turret-mounted camera** for the final few degrees, logging the bearing residual. So the one likely hardware addition for the aiming goal is **one more camera as a boresight/turret cam** (and a 2nd/3rd Orange Pi for coverage) — **not** a Mac-mini-class compute change. You can do all the *software* (incl. the aiming math and its sim validation) with current hardware first.

Net: **proceed with current hardware for the localization + sim + autos work; plan one boresight/turret camera (+ extra Orange Pi) when you move from coarse aim to fine aim.**

---

## 6. Prioritized, sim-first roadmap

Each step is verifiable in simulation before any hardware.

1. **Fix the confirmed bugs** (§2): timestamp time base, NaN gate, precision-command timeout + logging, log the fused pose. Small, high-value, no new deps.
2. **Wire PhotonVision simulation** (§3.5) — port 1768/3467 `VisionIOPhotonVisionSim`; refactor `VisionSubsystem` to the **AdvantageKit IO-layer split** (§4) so sim is a drop-in. *Keystone — unblocks everything.*
3. **Upgrade `DriveToPose`** (§3.3) — trapezoid profile + FF-fade + tunables + logging + safety timeout; add the **settle-gated Choreo/PathPlanner finish** (1768) and a spatial trajectory→DriveToPose handoff (6328).
4. **Add the aiming module** (§3.4) — chassis-aim `LaunchCalculator`-style with TOF-lookahead shoot-on-move and distance maps, turret/boresight-ready, with the `poseBearing − camBearing` log.
5. **Harden fusion with 3467's diagnostics** (§3.2) — per-camera σ factor, n-σ logging, rejection-reason enums, skid σ-inflation hook, single-tag gyro disambiguation (6328), early-auto vision ignore, +∞ single-tag theta; bump odometry to 250 Hz on the CANivore.
6. **Choreo path + autos** (§3.6) — make the trajectory layer vendor-agnostic; build the real `VisionTest` auto.
7. **Convert to skills/prompts** — once each module works and is documented, distill into `.claude`/`.codex` skills + prompt templates (your stated end goal: AI-generated robot code).

Every new file will carry inline `// Idea: <team> <file:line> — <what/why>` comments so the final code is self-documenting for students.

---

## 7. Sources (actual files read this session)

- 6328: `subsystems/vision/Vision.java`, `RobotState.java`, `subsystems/launcher/LaunchCalculator.java`, `commands/DriveToPose.java`
- 3467: `frc/lib/posestimator/PoseEstimator.java`, `frc/lib/io/vision/VisionIOPhotonVisionSim.java` (listing)
- 1768: `subsystems/vision/VisionIOPhotonVisionSim.java`, `autos/AutoModeBase.java`, `util/ShootingUtil.java`
- 6995: `subsystems/turret/TurretS.java`
- Plus the prior research already in-repo: `research-log.md`, `Vision-Strategy-2027-v2.md`.
