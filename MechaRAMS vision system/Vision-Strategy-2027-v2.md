# Vision and Localization Strategy v2

FRC Team 999 MechaRAMS - 2027 planning update  
Prepared June 29, 2026  
Supersedes: Vision-Strategy-2027.docx

## 1. Executive Summary

The new 6328 Darwin technical binder changes the emphasis, but not the core recommendation.
6328 did gain a major competitive advantage from offloading work to an onboard M4 Mac mini. Their
Mac mini did not just run a camera pipeline; it ran the Java robot program, Northstar vision, object
detection, logging, dynamic autonomous planning, and 200 Hz control while the roboRIO handled
hardware IO through Idun. That is a serious architecture, and it explains part of why Darwin could do
things most robots could not.

That does not mean MechaRAMS should try to copy the whole Mac mini architecture on Orange Pi this
season. The part we should copy immediately is the localization discipline:

- Fuse every valid observation from every camera and every frame.
- Use timestamped measurements and replayable logs.
- Weight vision by distance squared and tag count squared.
- Trust rotation only from genuine multi-tag solves; do not feed gyro-seeded yaw back into the
  estimator as independent vision.
- Reject physically impossible poses, not good measurements that disagree with stale odometry.
- Calibrate camera intrinsics and robot-to-camera extrinsics from measured data.

The revised recommendation is:

1. Keep final drivetrain control and final pose fusion on the roboRIO for 2027 unless timing data proves
   that a bigger migration is unavoidable.
2. Use Orange Pi 5 Plus units as dedicated vision coprocessors for PhotonVision or a lightweight custom
   pipeline, with one or two USB2 global-shutter cameras per Pi.
3. Do not offload ordinary trajectory controllers to the coprocessor. The path-following math is not the
   likely CPU bottleneck, and moving closed-loop drivetrain control offboard adds latency and failure
   modes.
4. Attack roboRIO overruns first by measuring loop timing, reducing NetworkTables and SmartDashboard
   traffic, removing synchronous debug work from periodic paths, and moving only compute-heavy
   planning or perception offboard.
5. Build the 2027 architecture around logging, replay, camera calibration, and a measured CPU budget.

In short: yes, offload the heavy perception work. No, do not start by offloading all vision fusion and
trajectory control. Treat 6328's Mac mini architecture as a long-term research direction, not the first
summer implementation.

## 2. What Changed Since v1

Version 1 correctly identified PhotonVision plus compact global-shutter cameras as a promising
direction, but it underweighted two new facts:

- 6328's 2026 binder explicitly says Darwin eliminates roboRIO performance constraints by running
  almost all processing on an M4 Mac mini, with Java robot code on the Mac and C++ hardware IO on the
  roboRIO.
- MechaRAMS already sees high roboRIO CPU utilization and cycle overruns, so compute placement is not
  just a camera packaging question.

The updated conclusion is more nuanced. Offboard compute was a key enabler for 6328, especially for
Northstar, object detection, dynamic autonomous planning, high-rate logging, and 200 Hz control. But
for our constraints, the near-term win is not "move the robot brain to Orange Pi." The near-term win is
"stop asking the roboRIO to do expensive non-control work, and feed it clean, timestamped observations
that are cheap to fuse."

## 3. What 6328 Actually Did

Darwin used a three-layer architecture:

- The M4 Mac mini ran the main Java robot code and Northstar vision.
- The roboRIO ran C++ hardware IO and communicated with the Mac through Idun over Ethernet.
- Basler USB3 global-shutter cameras fed Northstar; four cameras produced AprilTag localization and
  three color cameras also ran fuel/object detection pipelines.

Their binder calls out these concrete advantages:

- 200 Hz robot loop.
- No roboRIO loop overruns or memory-related crashes.
- High-throughput AprilTag and object detection.
- Hardware-accelerated video recording.
- High-rate compressed AdvantageKit logs.
- Real-time fuel localization and dynamic autonomous planning.

Their code also shows the localization details that matter most to us:

- `VisionIONorthstar` reads all queued NetworkTables observations, not just one latest value.
- `Vision.periodic()` loops through every frame from every camera.
- Poses outside the field border or with impossible Z are rejected.
- Single-tag dual-solution ambiguity is resolved against current robot heading, but single-tag rotation is
  not fused as trusted heading.
- XY standard deviation scales like `0.01 * avgDistance^2 / tagCount^2 * cameraFactor`.
- Theta standard deviation scales like `0.03 * avgDistance^2 / tagCount^2 * cameraFactor` only when
  the solve has trustworthy vision rotation; otherwise theta is infinity.
- Observations are sorted by timestamp before being passed to `RobotState.addVisionObservation()`.
- `RobotState` applies the vision update against a pose-history buffer, then replays odometry forward.

That fusion pattern is feasible for us without a Mac mini. The Mac mini is what lets them run that
pattern at extreme throughput while also doing object detection, path search, high-rate logging, and
whole-robot control.

## 4. Other Team Pattern

The public-code pattern remains:

- Image processing runs off the roboRIO.
- Final pose fusion commonly remains on the robot controller or drivetrain estimator.
- The best teams differ on camera vendor, but converge on logging, calibration, and measurement
  weighting.

Useful examples:

- 125 ran a deliberately simple Limelight MegaTag2 style system: ignore theta, conservative distance
  weighting, and log everything.
- 1768 and similar AdvantageKit-template teams use PhotonVision as a camera pipeline but keep the
  robot-side fusion straightforward.
- 3467 and 5687 use custom estimators and odometry threads, but the valuable ideas are still the same:
  timestamped observations, logged verdicts, covariance-aware rejection, and explicit pose validation.
- 6328 is the high-end architecture: custom vision plus offboard robot code plus custom logging and
  control infrastructure.

The implication for MechaRAMS is that we do not need 6328's full compute stack to improve sharply.
We need their measurement discipline and enough coprocessor capacity to avoid starving the roboRIO.

## 5. Answering the CPU Question

### 5.1 Is vision the biggest CPU eater?

Raw image processing would be a huge CPU eater if it ran on the roboRIO, but it does not in our current
Limelight architecture. Limelight already performs image capture, AprilTag detection, and pose solving
on the camera. The roboRIO cost is mostly:

- NetworkTables polling.
- Double-array parsing and object allocation.
- Repeated per-camera selection logic.
- SmartDashboard/NetworkTables publishing.
- Pose gating and calls to CTRE `addVisionMeasurement()`.
- QuestNav frame polling and fusion.

That is not free, but it is much smaller than running AprilTag detection on the roboRIO. Replacing
Limelights with Orange Pi PhotonVision does not automatically reduce roboRIO load; it can even increase
NT traffic if we publish more cameras and more frames without discipline.

### 5.2 Are trajectory controllers the biggest CPU eater?

Almost certainly not. A normal holonomic trajectory controller is mostly interpolation, a few PID
calculations, feedforward, and kinematics. That is cheap compared with:

- Heavy NetworkTables traffic.
- Console prints in scheduled paths.
- Dashboard writes every 20 ms.
- Device status polling.
- JSON parsing or path file loading during active robot operation.
- Large object allocation in periodic loops.
- Multiple subsystems doing expensive telemetry when debug flags are enabled.

Path planning can be expensive if we generate or search many candidate paths online. Path following is
not. Therefore:

- Pre-generate trajectories offboard or at build time.
- It is fine to use a coprocessor for large dynamic path search or game-piece route optimization.
- Keep the final drivetrain controller on the roboRIO unless we have a measured reason to move it.

### 5.3 Would offloading fusion and trajectory control solve cycle overruns?

It might hide some symptoms, but it is the wrong first move. Moving final closed-loop control offboard
requires deterministic networking, safety fallbacks, synchronized timestamps, and very careful failure
behavior. 6328 solved those problems with Idun, auto-generated IO, custom UDP protocols, deployment
tooling, diagnostics, and fallback modes. That is an infrastructure project, not a camera project.

The first move should be a timing audit. Measure each periodic block before deciding what to move.

Minimum timing signals to log:

- Total robot loop time and loop overrun count.
- CommandScheduler runtime.
- Each subsystem `periodic()` runtime.
- Odometry update runtime.
- Limelight/PhotonVision polling runtime per camera.
- QuestNav polling runtime.
- SmartDashboard/NetworkTables publish count and runtime.
- CTRE drivetrain update and odometry sample timing.
- Garbage collection or allocation spikes if visible.

If the timing audit shows the Limelight/PhotonVision polling path is expensive, optimize that path first.
If it shows dashboard output is the problem, offloading cameras will not fix it.

## 6. Recommended 2027 Architecture

### 6.1 Robot-side architecture

Keep on the roboRIO:

- Final drivetrain command generation.
- Final pose estimator or CTRE `addVisionMeasurement()` calls.
- Safety-critical subsystem state machines.
- Final autonomous command sequencing.
- Driver controls and fallback behavior.

Move off the roboRIO:

- AprilTag image processing.
- Object detection.
- Video streaming and recording.
- Camera calibration tools.
- Large dynamic route search, if the game rewards it.
- Offline log replay and tuning.

This gives us most of the practical benefit without recreating Idun.

### 6.2 Vision hardware

Use the hardware in tiers:

Tier 1 - Near-term baseline:

- Keep the LL4 cameras as a known-good baseline and fallback.
- Fix the robot-side fusion and logging first.
- Use this as the benchmark for any Orange Pi solution.

Tier 2 - Orange Pi PhotonVision pilot:

- One Orange Pi 5 Plus per one or two USB2 global-shutter cameras.
- Prefer global-shutter monochrome cameras for AprilTags.
- Avoid high-resolution/high-FPS USB2 ambitions. Stability beats frame rate.
- Connect Orange Pis by Ethernet through a robot switch.
- Use unique camera names and fixed IPs.
- Publish only the data the robot needs in competition mode.

Tier 3 - Expanded 2027 competition layout:

- Three or four tag cameras for field coverage.
- One optional boresight camera if the game has a shot or target where angular precision dominates.
- LL4 retained as fallback or game-piece/object-detection hardware if useful.

### 6.3 Camera count and Orange Pi parallelism

Several Orange Pis working in parallel can help in three ways:

- They avoid USB2 bandwidth contention.
- They reduce per-device CPU load and thermal throttling.
- They isolate failures: one Pi or camera can die without taking out the whole vision system.

They only improve localization if the robot code fuses all observations correctly. Four cameras with
"best camera wins" logic are much less valuable than four cameras with timestamped all-frame fusion.

Recommended wiring:

- Camera to local Orange Pi over USB.
- Orange Pi to robot switch over Ethernet.
- roboRIO, LL4s, and driver station traffic on the normal FRC network.
- Avoid Pi-to-Pi dependency in the match path. Each Pi should independently publish observations to
  NetworkTables or a simple robot-facing protocol.

Do not use USB between Orange Pis for match-critical communication. Ethernet is easier to diagnose,
works naturally with NetworkTables, and matches FRC networking practice.

## 7. Robot-Side Fusion Rules

These are the rules the v2 architecture should enforce regardless of camera vendor.

1. Initial pose seeding may use a hard reset while disabled or at the start of autonomous.
2. During enabled operation, avoid hard pose resets. Fuse measurements with appropriate standard
   deviations instead.
3. Fuse every accepted observation from every camera, timestamp-sorted.
4. If using Limelight MegaTag2 or any gyro-seeded pose, set theta standard deviation to infinity.
5. Trust heading only from multi-tag solves with independent vision rotation.
6. Use `k * avgDistance^2 / tagCount^2` for XY standard deviation, with per-camera factors from
   calibration data.
7. Reject physically impossible poses:
   - outside the field plus margin,
   - impossible Z,
   - NaN/infinite values,
   - excessive single-tag ambiguity,
   - stale timestamps.
8. Replace fixed innovation gates with either no innovation gate or a statistical gate that accounts for
   current uncertainty.
9. Log accepted and rejected observations with reason enums.
10. Make field layout selectable per event and verify camera-side and robot-side layouts match.

## 8. Performance Plan for roboRIO Overruns

### Phase 0 - Timing audit before architecture migration

Add lightweight timing instrumentation before moving more code offboard. The goal is to know the top
three time consumers during a real practice run.

Actions:

- Enable AdvantageKit or a minimal DataLog timing channel.
- Add a `LoopTiming` or `Perf` log group.
- Time each subsystem `periodic()` with `System.nanoTime()`.
- Publish timing at a low rate, not every loop to SmartDashboard.
- Capture CPU utilization, loop overrun count, and memory use if available.
- Run three cases:
  - robot disabled with dashboards connected,
  - teleop driving with vision enabled,
  - auto path with shooting/vision enabled.

Do not optimize blind. The current code already has debug flags and some throttling; the next step is
measured triage.

### Phase 1 - Remove common roboRIO load sources

Likely low-risk wins:

- Keep SmartDashboard disabled by default in matches.
- Replace high-frequency dashboard output with AdvantageKit/DataLog.
- Remove or guard `System.out.println()` in path scheduling and periodic code.
- Avoid calling expensive camera APIs twice in one loop.
- Cache per-loop drive state instead of repeatedly asking CTRE for state in nested helpers.
- Use NetworkTables subscriber queues only where we need all frames; otherwise read the latest value once.
- Avoid parsing Limelight JSON in match loops.
- Avoid path file loading during enabled operation except at command scheduling time.
- Keep image streams off during matches unless needed by drivers.
- Run object detection only when the game strategy actually needs it.

### Phase 2 - Move perception to Orange Pi

The Orange Pis should own:

- Camera capture.
- AprilTag detection.
- Per-camera pose solving.
- Optional object detection.
- Camera health and FPS reporting.

They should not own:

- Final drivetrain control.
- Final safety decisions.
- Autonomous command sequencing.
- Robot-wide state machines.

The data sent to the roboRIO should be compact:

- timestamp,
- camera id,
- robot pose estimate or camera pose estimate,
- tag ids,
- tag count,
- average distance,
- ambiguity or reprojection error,
- latency,
- optional per-observation standard deviation if computed offboard.

### Phase 3 - Consider advanced offload only if needed

Only consider a 6328-style offboard robot brain if all of these are true:

- Timing logs show the roboRIO cannot meet the loop budget after telemetry and vision cleanup.
- The team has enough software depth to build and maintain a robust IO bridge.
- There is a tested fallback mode that can safely drive if the coprocessor dies.
- Deployment, boot, diagnostics, and pit replacement procedures are rehearsed.
- The season game gives enough performance upside to justify the risk.

For 2027, this should be a research branch, not the competition baseline.

## 9. PhotonVision Decision

The v1 verdict still holds, with one addition: PhotonVision is not primarily a roboRIO CPU fix. It is a
packaging, camera-coverage, calibration, and data-access fix. It can reduce total robot-system load if it
replaces LL-side complexity with cleaner observation publishing, but the roboRIO will still need to
consume and fuse the results.

Adopt PhotonVision as the 2027 primary tag path if the pilot meets these gates:

- At least two global-shutter cameras run reliably on Orange Pi for full practice sessions.
- End-to-end latency is measured and stable.
- Stationary surveyed-position error beats or matches the LL4 baseline.
- Moving-path pose error beats or matches the LL4 baseline.
- Boot time and reconnect behavior are acceptable for events.
- A spare imaged Pi can be swapped quickly.
- The robot can fall back to LL4 or odometry-only mode if a Pi fails.

If the pilot fails these gates, keep LL4s as the primary tag source and still apply the robot-side fusion
fixes. The fusion fixes are vendor-independent.

## 10. Local vs Global Strategy

Keep the conceptual split from the trajectory/localization writeup.

Global localization:

- Purpose: field-relative driving, autonomous paths, coarse scoring setup.
- Tools: multi-camera AprilTag fusion, wheel odometry, gyro, optional QuestNav if logs prove value.
- Accuracy target: stable enough for driving and coarse aim.

Local positioning:

- Purpose: final scoring/aiming where a few degrees or centimeters decide success.
- Tools: boresight camera, turret camera, direct target bearing, beam-break/indexer sensors, local
  mechanism sensors.
- Accuracy target: direct measurement of the target or mechanism relationship.

For a shooting game, the recommended architecture is still hybrid:

- Use global pose for coarse chassis aim and shoot-on-the-move lead.
- Use a shooter-boresight global-shutter camera as a fine angular correction and independent check.
- Log `poseBearing - cameraBearing` so we can see whether misses are caused by global pose, shooter
  model, or mechanical repeatability.

## 11. Trajectory Strategy

Do not move ordinary trajectory following to the Orange Pi. Improve how trajectories finish.

The problem with PathPlanner/Choreo is not CPU. The problem is semantic: time-parameterized paths end
when time expires, even if the robot has not reached the endpoint.

Recommended pattern:

- Use PathPlanner or Choreo for transit.
- Interrupt or complete the transit path near the final region.
- Hand off to a `DriveToPose` command that finishes on position and angle tolerance.
- Require the robot to remain inside tolerance for a short settle time.
- Use timeout only as a safety backstop, not as the definition of success.

If the 2027 game requires dynamic game-piece routing, the search/planning portion may run on a
coprocessor. The final velocity commands should still be generated or validated on the roboRIO.

## 12. Roadmap

### Summer 2026

1. Add timing instrumentation and structured vision logs.
2. Fix the Limelight fusion bugs from the gap analysis:
   - theta infinity for MT2,
   - fuse all accepted cameras,
   - distance-squared/tag-count-squared standard deviations,
   - physical rejection,
   - remove hard mid-match resets.
3. Measure LL4 baseline at surveyed field positions.
4. Build Orange Pi 5 Plus PhotonVision pilot with two global-shutter USB2 cameras.
5. Compare LL4 and Orange Pi results using the same ground-truth protocol.
6. Implement `DriveToPose` final approach and settle-gated trajectory endings.

### Fall 2026

1. Choose the 2027 primary tag stack from measured data.
2. Decide whether QuestNav stays based on logs.
3. Create camera mount CAD and calibration SOP.
4. Train students on AdvantageScope, replay, and camera calibration.
5. Re-check additional public 2026 code releases.

### Kickoff 2027

1. Put camera placement into drivetrain and superstructure CAD in week 1.
2. Reserve protected wiring, cooling, and service access for Orange Pis and cameras.
3. Carry over the tested IO-layer vision abstraction instead of rewriting vision during build season.
4. Keep LL4 fallback or spare Orange Pi fallback ready for the first event.

## 13. Go / No-Go Criteria

Use these criteria before committing to a competition architecture.

Orange Pi PhotonVision is a go if:

- It improves or matches LL4 localization error in the surveyed-position test.
- It runs a full practice session without thermal throttling or reconnect failures.
- roboRIO loop timing remains inside budget with all cameras enabled.
- Students can calibrate and replace a camera/Pi without mentor-only knowledge.

Offboard trajectory search is a go if:

- The game requires dynamic target/game-piece routing.
- The search is too expensive for the roboRIO.
- The roboRIO still owns final command validation and fallback behavior.

Offboard final control is a no-go for 2027 unless:

- A complete Idun-like IO bridge and fallback mode exists before kickoff,
- it has been tested for many hours,
- and timing logs show the roboRIO cannot otherwise meet requirements.

## 14. Practical Bottom Line

The biggest 2027 success factor is not choosing between Limelight and PhotonVision. It is building a
measurement-driven localization system that the team can debug under match pressure.

For MechaRAMS, the best path is:

- Use Orange Pis for camera processing.
- Keep final fusion and drivetrain control on the roboRIO.
- Reduce roboRIO overruns with timing data and telemetry discipline.
- Copy 6328's fusion rules before copying 6328's compute architecture.
- Add local sensing for final scoring/aiming when global pose is not enough.

That gives us most of the competitive upside with a risk level we can realistically own.

## 15. Sources Reviewed

- Local `Vision-Strategy-2027.docx`.
- Local `Localization-and-Trajectory-Precision-2027.docx`.
- Local `localization-gap-analysis-2026.md`.
- Local `research-log.md`.
- Local `Darwin Technical Binder.pdf` and extracted text.
- Mechanical Advantage `RobotCode2026Public`, commit `bf32451`, published June 26, 2026 at 9:08 PM EDT.
- MechaRAMS 2026 robot code in `T:\Projects\MechaRAMS\2026Competition\2026Competition`.
