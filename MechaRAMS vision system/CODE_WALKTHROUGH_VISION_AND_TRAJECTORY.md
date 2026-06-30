# Code Walkthrough — Vision and Trajectory Driving (for students)

Author: Claude (Opus 4.8) session, 2026-06-30
Purpose: A step-by-step, line-specific tour of the code that was generated for Team 999, so a mentor can
explain to students exactly *what happens where* and *why each block was written that way*. Read it with
the files open. Line numbers are accurate as of the committed rebuild; if you edit a file, the numbers
near your edit will shift, but the block structure will not.

Two journeys are traced end to end:

- **Part A — Vision:** a camera frame on the Orange Pi becomes a correction to the robot's believed pose.
- **Part B — Trajectory:** a path file on disk becomes wheel motion that ends precisely on target.

All paths are under `VisionTestingAndCalibration/src/main/java/frc/robot/`.

---

# Part A — Vision: from NetworkTables to a fused pose

The journey: **PhotonVision (Orange Pi) → NetworkTables → `VisionIOPhotonVision` → `Vision` (validate →
weight → fuse) → CTRE pose estimator.** Four files: `subsystems/vision/VisionIO.java`,
`VisionIOPhotonVision.java`, `Vision.java`, and the wiring in `RobotContainer.java`.

## A0. The data contract — `VisionIO.java`

Before any logic, we define *what one camera reports each loop*. `VisionIO.java` declares an `@AutoLog`
inputs class and two record types:

- `VisionIOInputs` (lines 24–42): `connected`, `latestTargetObservation`, `poseObservations[]`,
  `tagIds[]`.
- `PoseObservation(timestamp, pose, ambiguity, tagCount, averageTagDistance)` (lines 60–61, javadoc
  50–59): one field-relative robot-pose estimate from one frame.
- `TargetObservation(tx, ty, hasTarget)` (line 48): the best target bearing, with a `hasTarget` flag so
  a future boresight loop can tell "no target" from "target at 0°".

**Decision — why an interface with `@AutoLog`:** this is the AdvantageKit "IO layer." Capturing the raw
inputs each loop makes the logs *replayable* — the fusion logic can be re-run identically against a
recorded log, off-robot. The `@AutoLog` annotation generates the serialization companion
`VisionIOInputsAutoLogged` at build time (enabled by the annotation processor in `build.gradle`).

## A1. Reading PhotonVision from NetworkTables — `VisionIOPhotonVision.updateInputs()`

File `VisionIOPhotonVision.java`. PhotonVision on the Orange Pi publishes detection results over
NetworkTables; PhotonLib's `PhotonCamera` (constructed at line 39 with the camera's NT name) exposes them.

- **Line 45** `inputs.connected = camera.isConnected();` — health flag for the pit "disconnected" alert.
- **Line 50** `for (var result : camera.getAllUnreadResults())` — pulls **every** unread frame queued
  since the last loop.
  - *Decision:* `getAllUnreadResults()` (not "get latest") because PhotonVision can buffer several frames
    between our 50 Hz robot loops. Dropping them throws away real corrections — this was a core lesson
    from 6328/1768.
- **Lines 52–61** record the best single-target bearing into `latestTargetObservation`, with `hasTarget`
  true/false (lines 57 / 60). *Decision:* not used for fusion; kept so a future boresight loop can servo
  on a tag bearing — and `hasTarget` keeps it from mistaking "no target" for "target at 0°".
- **Lines 63–82 — multi-tag branch** (`result.multitagResult.isPresent()`): PhotonVision already solved a
  combined `field→camera` transform on the coprocessor (line 66). We convert it to a robot pose by
  composing with the inverse robot→camera transform (line 67: `fieldToCamera.plus(robotToCamera.inverse())`),
  average the tag distances (lines 70–73), and emit a `PoseObservation` carrying the **PhotonVision
  capture timestamp** `result.getTimestampSeconds()` (line 78), the ambiguity, the tag count, and the
  average distance.
  - *Decision:* trust the coprocessor's multi-tag PnP directly — multi-tag solves are well-constrained and
    give good heading; doing the solve on the Pi is the whole point of offloading.
- **Lines 84–104 — single-tag branch** (`!result.targets.isEmpty()`): there is no combined solve, so we
  reconstruct the robot pose from the *known* tag pose in our custom layout (line 87
  `CUSTOM_FIELD_LAYOUT.getTagPose(...)`), composing `field→tag`, the inverse `camera→tag`, and the inverse
  `robot→camera` (lines 89–94). Emits a `PoseObservation` with `tagCount = 1` (line 102).
  - *Decision:* the 1768 template hard-codes a blocklist of game-specific tag IDs here; we removed it
    (see the class comment, lines 30–32). We have no game tags, and single-tag quality is governed later
    by the ambiguity gate in `Vision`, which is cleaner.
- **Lines 108–114** copy the collected observations and tag IDs into the logged `inputs` object.

**End state of A1:** for each camera, a loggable list of candidate robot poses, each with a timestamp,
tag count, ambiguity, and distance — *no filtering yet*. Filtering is deliberately a separate stage.

## A2. The simulation source — `VisionIOPhotonVisionSim.java`

In simulation, `VisionIOPhotonVisionSim` (which **extends** `VisionIOPhotonVision`) substitutes for the
real camera. Its constructor builds a shared `VisionSystemSim` with the tag layout and a `PhotonCameraSim`
modeled on the Arducam OV9782, and `updateInputs()` calls `visionSim.update(poseSupplier.get())` to render
the world from the true robot pose **before** delegating to the real `super.updateInputs(...)`.

**Decision — why subclass:** the synthetic frames flow back through the same `PhotonCamera`/NetworkTables
path, so the *real* ingestion code in A1 runs unchanged. Simulation exercises the production code, not a
mock. This is what makes "validate in simulation first" trustworthy.

## A3. Validation — `Vision.rejectionReason()` (lines 196–227 of `Vision.java`)

Each candidate pose is checked by `rejectionReason(...)`, which returns `RejectionReason.ACCEPTED` or the
first gate it fails. The gates, in order:

- **Line 197** `tagCount == 0` → `NO_TAGS`.
- **Lines 202–206** any of x/y/z/θ not finite → `NON_FINITE`.
  - *Decision:* a degenerate PnP solve can emit NaN/Inf; feeding that to the estimator poisons it
    permanently. Codex's first pass omitted this check — it was a real gap.
- **Lines 208–210** `|z| > MAX_ACCEPTED_Z_METERS` → `BAD_Z` (the robot can't be floating/sunk).
- **Lines 214–218** outside the field plus a margin → `OUTSIDE_FIELD`.
- **Lines 220–222** average tag distance too large → `TOO_FAR` (far tags are noisy).
- **Lines 223–225** single tag with ambiguity above threshold → `SINGLE_TAG_AMBIGUOUS`.

**Decision — `RejectionReason` is an enum** (declared lines 59–68), not a string, so logs can be filtered
and rejections counted by category over a match (idea: 3467). **Decision — reject physical impossibility,
not "disagreement with odometry":** a correct vision fix that disagrees with drifted odometry is exactly
the fix we *want*; only physically impossible poses are thrown out.

## A4. Weighting — `Vision.standardDeviations()` (lines 237–251)

Accepted poses are not all equally trustworthy, so each gets a measurement standard-deviation vector
`[σx, σy, σθ]` (smaller = trusted more):

- **Lines 239–240** `distanceFactor = dist² / tagCount²` — trust falls off with distance squared and
  falls *fast* as tags are added (tag count is **squared**, matching 6328/6995).
- **Lines 241–244** multiply by a per-camera `cameraFactor` so a worse-calibrated camera counts less
  (idea: 6328 `stdDevFactor`).
- **Line 245** `xy = LINEAR_STD_DEV_BASELINE * distanceFactor * cameraFactor`.
- **Lines 246–249 — the most important line:** `θ` is the weighted value **only when `trustRotation`**
  (set at line 167 to `tagCount ≥ 2`); for a single tag it is `Double.POSITIVE_INFINITY`.
  - *Decision:* a single tag gives a weak, noisy heading. Fusing it created the circular-feedback heading
    drift that wrecked our 2026 aiming. Setting σθ = ∞ tells the estimator "use this for position, ignore
    its heading entirely." This single decision is the heart of the whole project (idea: 6328 / 125).

## A5. Ordering and fusion — `Vision.periodic()` (lines 119–193)

This is the per-loop driver:

- **Lines 121–124** pull inputs from every camera IO and `Logger.processInputs(...)` them (the replay hook).
- **Early-auto guard (enforced):** the timer restarts whenever we're not in enabled autonomous (lines
  128–130), and `acceptDuringAuto` is computed by the pure helper `shouldAcceptDuringAuto(...)` (lines
  131–132; helper at 115–117, unit-tested in `VisionPolicyTest`). The gate at **lines 158–165** sends a
  validated-but-withheld pose to a *separate* `AutoSuppressedPoses` channel (line 189) and `continue`s
  past `consumer.accept` during the first `AUTO_VISION_IGNORE_SECONDS` of autonomous, so a stray early
  frame cannot move the known start pose (idea: 6328). The flag is logged at line 192.
- **Lines 140–186 — per-camera loop:** for each observation, call `rejectionReason` (line 150); rejects
  are logged with their reason and drawn to `RejectedPoses` (lines 151–156); a pose that passes the auto
  gate gets its `stdDevs` (line 168), is handed to the `consumer` (line 170), and is added to
  `AcceptedPoses` (line 173) — so **`AcceptedPoses` means "actually fused"** and matches the
  `AcceptedFrames` count (the fix for Codex's accepted-vs-fused ambiguity).
- **Lines 175–180 — innovation logging:** the distance between the accepted vision pose and the current
  estimate. *Decision:* a pragmatic stand-in for 3467's n-σ gate — CTRE's estimator doesn't expose its
  covariance, so we log the raw "how far did this frame pull us" signal for tuning instead.
- **Lines 188–192** summary logs (fused `AcceptedPoses`, `AutoSuppressedPoses`, `RejectedPoses`,
  `TagPoses`, and the auto-accept flag) for AdvantageScope.

**Decision — about "time ordering" (important to explain correctly):** notice there is **no explicit sort
by timestamp** here. Each accepted observation is fused immediately, carrying its own capture timestamp
(passed through to the estimator at line 170 → A6). Correct time-ordering comes from the estimator: CTRE's
`SwerveDrivetrain` keeps an interpolating **odometry pose-history buffer**, and `addVisionMeasurement`
applies each correction *at the robot's pose for that timestamp*, then rolls odometry forward. So the
order we insert two frames within one loop does not change the result. 6328's custom estimator sorts
explicitly because it applies samples sequentially; because we kept CTRE's buffer-based estimator (see
`DESIGN_DECISIONS_AND_REJECTED_IDEAS.md`), the buffer does the time alignment for us. This is the one place
students often picture an explicit sort that isn't needed here — the timestamp on each measurement is what
matters, not insertion order.

## A6. The timestamp time-base fix and the estimator handoff

The `consumer` is defined in `RobotContainer.createVision()` (lines 181–183):

```java
Vision.VisionConsumer consumer =
    (pose, timestampSeconds, stdDevs) ->
        drive.addVisionMeasurement(pose, Utils.fpgaToCurrentTime(timestampSeconds), stdDevs);
```

- **Line 183 — `Utils.fpgaToCurrentTime(timestampSeconds)`** is a genuine bug fix. PhotonVision timestamps
  are in the WPILib **FPGA** time base; CTRE's odometry buffer is on the **Phoenix** time base. Without
  this conversion every vision sample is matched to the *wrong* moment of odometry history, silently
  breaking latency compensation. (The first pass passed the raw timestamp.)
- `DriveSubsystem.addVisionMeasurement(...)` (lines 265–267) is a thin pass-through to CTRE's estimator;
  its comment (lines 256–264) stresses that **validation lives in `Vision`, not here** — so a bad fused
  pose can be diagnosed as either a vision-policy problem or an estimator problem.

The IO choice itself is at `createVision()` (lines 180–end): `RobotBase.isSimulation()` (line 192) picks
`VisionIOPhotonVisionSim`, otherwise `VisionIOPhotonVision`; the two front cameras are active and the rear
pair is commented for the 2→4 camera upgrade.

**One-sentence summary for students:** *A camera frame becomes a list of candidate poses
(`VisionIOPhotonVision`), the impossible ones are dropped and the rest are weighted by distance/tag-count
with single-tag heading thrown away (`Vision`), and each survivor is fused at its own corrected timestamp
into CTRE's pose estimator (`createVision` → `addVisionMeasurement`).*

---

# Part B — Trajectory driving: from path files to motion

The journey: **`.path`/`.auto`/`settings.json` on disk → PathPlanner reads them → `AutoBuilder` follows
the path through CTRE → near the end we hand off to `DriveToPosePrecisionCommand`, which finishes on
position tolerance.** Files: `src/main/deploy/pathplanner/...`, `subsystems/DriveSubsystem.java`,
`RobotContainer.java`, `commands/DriveToPosePrecisionCommand.java`.

## B0. The trajectory files on disk

- `src/main/deploy/pathplanner/settings.json` — robot config PathPlanner uses to make paths followable
  (track width, mass, MOI, wheel radius, gearing, max speed). These are read back at runtime to build the
  follower's `RobotConfig`.
- `src/main/deploy/pathplanner/paths/VisionTestPath.path` — the waypoints + constraints (a straight move
  from `(1.5, 2.0)` to `(3.6, 2.0)`).
- `src/main/deploy/pathplanner/autos/VisionTest.auto` — a sequence that runs `VisionTestPath` and resets
  odometry at the start.

These deploy files are copied to the roboRIO and read at runtime; in simulation they are read from the
project.

## B1. Reading the files

- **`RobotConfig.fromGUISettings()`** — `DriveSubsystem.configurePathPlanner()`, line 183. Loads
  `settings.json` into the object the follower needs. It is wrapped in try/catch (lines 182–198) so a
  missing/invalid config *warns* instead of crashing.
- **`AutoBuilder.buildAuto("VisionTest")`** — `RobotContainer.configureAutos()`, line 137. Parses the
  `.auto` (and the `.path` it references) into a runnable `Command`. It is wrapped in `Commands.defer(...)`
  with a try/catch (lines 134–142) so a missing auto prints a message rather than crashing startup.
  - *Decision:* during development, robot code must survive a not-yet-created path file. Hence the lazy,
    fault-tolerant chooser entries.

## B2. Wiring the follower — `DriveSubsystem.configurePathPlanner()` (lines 181–200)

`AutoBuilder.configure(...)` (lines 184–196) connects PathPlanner to the drivetrain by passing five things:

- `this::getPose` (185) — where the robot thinks it is (the fused estimate from Part A).
- `this::resetPose` (186) — how the auto sets the starting pose.
- `() -> getState().Speeds` (187) — current robot-relative speeds.
- **the output consumer** (188–192) — given the follower's target `speeds` and `feedforwards`, command
  CTRE's `ApplyRobotSpeeds` request (field `pathApplyRobotSpeeds`, declared line 68), **passing the wheel
  force feedforwards through** (`withWheelForceFeedforwardsX/Y`, lines 191–192).
  - *Decision:* forwarding the force feedforwards (rather than dropping them) lets CTRE compensate each
    module properly — better path tracking, straight from the PathPlanner/CTRE integration guidance.
- `new PPHolonomicDriveController(new PIDConstants(5,0,0), new PIDConstants(7,0,0))` (193) — the controller
  that turns "where the path says I should be at time t" into a velocity correction (translation P=5,
  rotation P=7).

This runs once, in the constructor (called at line 166).

## B3. How a path actually executes (and where it ends)

When an auto command runs, each loop PathPlanner samples the trajectory at the current time, asks
`PPHolonomicDriveController` for the chassis speeds to stay on it, and the output consumer (B2) feeds those
to CTRE via `setControl(pathApplyRobotSpeeds...)`. CTRE's modules realize the speeds.

**Key teaching point — the path ends on *time*, not arrival.** Every mainstream follower (PathPlanner,
Choreo, even 6328's) finishes when the trajectory clock runs out, even if the robot is a few centimeters
short. That is fine for transit but not for scoring/aligning — which is why we hand off.

## B4. The precision finish — `DriveToPosePrecisionCommand` (`commands/DriveToPosePrecisionCommand.java`)

This command finishes a move on **position tolerance**, not time.

- **Controllers (lines 45–68):** one `ProfiledPIDController` each for field x, y, and θ. Each has a
  trapezoid-profile constraint (max velocity + max acceleration from `AutoConstants`).
  - *Decision:* a *profiled* controller follows a velocity profile that **decelerates to zero exactly at
    the goal**, which removes the end-of-move overshoot a plain PID causes (idea: 1768 `driveToPose`).
- **`initialize()` (lines 84–98):** seeds each controller with the current pose and current field velocity
  (`reset(...)`, lines 89–91), starts the safety timer (94), and clears stale `Finished`/`TimedOut` log
  flags (lines 95–97) so the log reflects this run. *Decision:* seeding with the live velocity makes the
  first command continuous — no jerk when the command takes over from a moving robot (idea: 6328
  `DriveToPose.initialize`).
- **`execute()` (lines 100–148):**
  - Lines 106–110 compute each axis speed as `controller.calculate(...)` **plus**
    `controller.getSetpoint().velocity` — the profile velocity acts as feedforward.
  - **Lines 112–122 — vector speed clamp:** the translational speed is clamped as a **vector** (scale the
    `(xSpeed, ySpeed)` pair by `maxSpeed / hypot`) rather than per-axis. *Decision:* per-axis clamping
    would let a diagonal command reach √2 × the configured max; clamping the norm preserves direction
    while bounding magnitude (Codex deep-review fix). Omega is clamped separately (line 121).
  - **Line 124** converts the field-relative `(x, y, θ)` speeds to robot-relative and commands the
    drivetrain (`drive.driveRobotRelative(ChassisSpeeds.fromFieldRelativeSpeeds(...))`).
  - Lines 126–130 compute translation/rotation error and `atGoal`.
  - **Lines 132–140 — settle gate:** the settle timer only runs while inside tolerance; leaving tolerance
    resets it. *Decision:* success requires *staying* in tolerance, not one instantaneous touch while
    still moving (idea: 1768 settle stopwatch).
  - Lines 142–147 log target, measured, errors, and settle (satisfies the AGENTS.md precision-logging
    rule; idea: 6328).
- **`isFinished()` (lines 150–154):** true when the settle timer exceeds `PRECISION_SETTLE_SECONDS` **or**
  the safety timer exceeds `PRECISION_SAFETY_TIMEOUT_SECONDS`.
  - *Decision:* the safety timeout guarantees the command can never hang forever on an unreachable target
    (idea: 1768's `.withTimeout(...)`). `end()` (156–163) stops the robot and logs whether it timed out.

## B5. The coarse→precise handoff

Two ways the handoff is expressed:

- **Reusable helper** — `DriveToPosePrecisionCommand.handoffFrom(coarse, condition)` (lines 174–176):
  `coarse.until(condition).andThen(this)`. Run the timed path until a spatial condition, bail out, finish
  precisely.
- **Two chooser options** in `RobotContainer.configureAutos()`:
  - *"VisionTest then Precision (sequential)"* — runs the **full** timed path, then precision. Simple, but
    the path still runs to its time-based end first.
  - *"VisionTest (spatial handoff)"* — the real interrupting pattern:
    `new DriveToPosePrecisionCommand(...).handoffFrom(path, () -> drive.getPose().getX() > 3.3)` — bail out
    of the path the instant the robot crosses x = 3.3 m, then finish precisely. (Codex deep-review: the
    earlier single "handoff" option was actually only sequential; this adds the genuine spatial one.)

*Decision:* this is the 6328 pattern — a time-based path gets you *close* efficiently; a position-tolerance
controller *finishes the job*. A time-based path must never be what declares a precise move "done."

## B6. Where Choreo would plug in (not active, by design)

Choreo is the documented upgrade, not the active tool (`DESIGN_DECISIONS_AND_REJECTED_IDEAS.md` explains
why). The clean insertion points: add the ChoreoLib vendordep and a Choreo `AutoFactory(pose, resetPose,
followPath, true /*useAllianceFlipping*/, drive, logger)` (6995's exact pattern — one boolean flips
red/blue). The follower output would reuse the **same** `ApplyRobotSpeeds` consumer from B2, and the
**same** `DriveToPosePrecisionCommand` would still do the precise finish — only the *transit* trajectory
source changes. Nothing downstream of B3 cares whether the path came from PathPlanner or Choreo.

**One-sentence summary for students:** *PathPlanner reads the `.path`/`.auto`/`settings.json` files
(`RobotConfig.fromGUISettings`, `AutoBuilder.buildAuto`), `AutoBuilder.configure` drives the robot along
the path through CTRE's `ApplyRobotSpeeds`, and just before the end we hand off to
`DriveToPosePrecisionCommand`, which uses profiled controllers + a settle gate to finish exactly on
target instead of when the clock runs out.*

---

# Part C — The two loops side by side

- **Vision loop (≈50 Hz, every robot periodic):** cameras → `VisionIOPhotonVision.updateInputs` →
  `Vision.periodic` validate/weight → `consumer` (timestamp fix) → CTRE estimator updates `Drive/Pose`.
  Meanwhile a **250 Hz** odometry thread (`DriveSubsystem` constructor, line 160) keeps the pose-history
  buffer dense so each vision frame fuses against an accurate "where was I then."
- **Trajectory loop (during autonomous/precision):** path sample → `PPHolonomicDriveController` →
  `ApplyRobotSpeeds` → modules; then `DriveToPosePrecisionCommand` profiled control → `driveRobotRelative`
  → modules, ending on settle.

Both loops read the **same fused pose** (`getPose()` / `Drive/Pose`). That is the whole point: better
vision (Part A) directly makes driving and aiming (Part B) more precise, and every step is logged so you
can replay and explain exactly what happened.

See also: `ARCHITECTURE_AND_DEPLOYMENT.md` (the big picture), `CALIBRATION_AND_TEST_PROCESS.md` (how to
test each stage), `DESIGN_DECISIONS_AND_REJECTED_IDEAS.md` (why these choices).
