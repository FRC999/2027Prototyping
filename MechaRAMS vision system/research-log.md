# Vision research log — what was checked and found

Maintained so future searches (by us or by Claude) don't redo work. Update this file whenever new code releases are checked.
Last updated: 2026-06-12.

## Repos found, cloned, and reviewed

| Team | Repo | Stack | Reviewed |
|------|------|-------|----------|
| 6328 Mechanical Advantage | github.com/Mechanical-Advantage/RobotCode2026Public | Custom Northstar coprocessor (Python/OpenCV SolvePnP), 4 cams (1 on moving mechanism), AdvantageKit | Vision.java, VisionConstants.java, RobotState.addVisionObservation, northstar/ pipeline |
| 125 NUTRONs | gitlab.com/nutrons125/nu25 (2025 season) | Limelight MT2-only, AdvantageKit, deliberately simple | VisionSubsystem, VisionIOLimelight, Robot.java fusion block |
| 1768 Nashoba (NE #1 2026) | github.com/Nashoba-Robotics/2026NashobaRobotics | PhotonVision, 3 cameras (Front/Left/...), AdvantageKit vision template, Pigeon2 | Vision.java, VisionConstants.java |
| 5687 The Outliers (NE #2) | github.com/frc5687/2026-Robot | C++; Limelights with custom fusion; PhotonVision used in sim | file listing only (include/subsystem/vision) |
| 3467 Windham Windup (NE #3, 51-0) | github.com/WHS-FRC-3467/Skip-5.16-Perry | 4× ThriftyCam (1600×1304 global shutter) on custom "c2" coprocessor (flatbuffer over NT); PhotonVision for object detection + sim; AdvantageKit; per-camera intrinsics matrices in code | VisionConstants.java, VisionIOC2.java |
| 7407 Choate Wired Boars | github.com/Choate-Robotics/7407-DriveCode-Rebuilt | RobotPy (Python), PhotonVision (sensors/photonvision.py, field_odometry.py) | file listing only |
| 195 CyberKnights | gitlab.team195.com/cyberknights (self-hosted GitLab) | Custom ROS stack, Mac Mini coprocessor | NOT cloneable from this environment: group page needs JS; guessed repo paths return auth prompt. Release thread: chiefdelphi.com/t/521390. Use browser or Claude CLI to enumerate repos. |

## Checked, nothing public found (as of 2026-06-12)

- **254, 2910, 1690, 2056**: no 2026 public release yet (typically release post-season/fall). Watch github.com/flamingchickens1540/frc-software-releases
- **176 Aces High**: no GitHub org/repos found (searched aceshigh176, Team176, FRC176 variants)
- **230 Gaelhawks**: nothing found (frc230, Gaelhawks, gaelhawks230 variants)
- **6329 Bucks' Wrath (NE #4)**: no repo found (FRC6329, BucksWrath variants); has CD reveal thread "ROMAN" (t/515342)
- **2168 Aluminum Falcons**: no 2026 repo found (Team2168/2026_Main_Robot, 2026-Robot, 2026_Robot_Code tried)
- **971**: uses own Gerrit/Bazel infra (github.com/frc971/971-Robot-Code), not season-repo model
- **1678**: robot-code-public is stale/old

Repo-name patterns already tried via `git ls-remote` (don't redo): `<org>/{2026,Robot2026,FRC-2026,2026-Robot,2026-Robot-Code,2026-Public,FRC2026}` for orgs listed above.

## Data sources that work from this environment

- frc-events.firstinspires.org district pages ARE fetchable (giant markdown table; parse with grep). NE 2026 final standings order: 1768, 5687, 3467, 6329 (top 4).
- thebluealliance.com pages are client-rendered → empty fetch. api.statbotics.io returned empty. api.github.com returned empty. GitHub *org pages* (github.com/orgs/X/repositories) fetch fine.
- `git clone`/`git ls-remote` to github.com and gitlab.com work from the sandbox. gitlab.team195.com prompts for auth on unknown paths.

## Key technical findings (details in localization-gap-analysis-2026.md)

1. Our biggest precision bugs: MT2 yaw fused with finite std dev (circular feedback); only one camera fused per loop; no logging.
2. Universal top-team pattern: multiple compact global-shutter cameras + fuse-everything with dist²/tagCount² std devs + rotation only from multi-tag + physical rejection (field bounds/Z) + no mid-match pose resets + AdvantageKit replay.
3. Camera vendor varies at the top (PV: 1768, 7407; custom: 6328, 3467, 195; LL: 5687, 125) → fusion + logging are the differentiators, not vendor.
4. Orange Pi 5 capacity (Chief Delphi consensus): 2× OV9281 AprilTag streams at 1280×800 ~30fps is the established config; OPi5 can additionally run 1–2 object-detection streams; nobody documented 4 tag cams on one OPi5. Sources: CD t/507551, t/482792.
5. 3467 ran 22 fps cameras with ~30 ms assumed latency and per-camera intrinsics in robot code; 6328 uses per-camera pitch "fudge" tunables (−4.5°…0°) — extrinsics refinement against ground truth is normal practice.
6. 6328 championship talk: "Beyond the Coprocessor: Lessons in FRC Vision & Localization" (slides in their build thread t/509595).

## Custom fusion internals (reviewed 2026-06-12)

- **6328 RobotState (custom estimator, MIT-style license):** closed-form Kalman update (A=0, C=I, per wpimath/algorithms.md) on top of a 2 s odometry pose buffer. `addOdometryObservation`: builds twist from wheel kinematics, **scales translation down toward 0 as robot tilt (acos(cos pitch · cos roll)) approaches 25°** (wheel odometry distrusted during climbs/impacts), overrides dtheta with gyro delta when available, has a fallback second gyro path. Vision applied at its timestamp via buffer transform; never a hard reset mid-match.
- **3467 frc/lib/posestimator (standalone reusable lib, GPLv3):** custom `SwerveOdometry` + `PoseEstimator`. Features our system lacks: **per-wheel skid rejection** (`badWheels[]` — a skidding module is excluded from the kinematics solve), **odometry std-dev inflation per bad wheel** (×0.75 linear per wheel, capped ×4), **n-sigma statistical innovation gate** (vision rejected only if the residual exceeds k·σ of current uncertainty — replaces fixed-meters gates; uncertainty grows while blind, so recovery is automatic), pluggable `poseValidator` predicate (field bounds), and **logged rejection-reason enums** for every observation (ACCEPTED / RESET_LOCKED / MISSING_ODOMETRY_SAMPLE / BELOW_MIN_N_SIGMA / POSE_VALIDATOR_REJECTED).
- **5687 (C++):** own `OdometryThread` + `PoseEstimator` classes feeding Limelight measurements — same architecture (custom estimator on rio), different camera vendor.
- **Common to all three:** image processing stays on the coprocessor; **fusion always runs on the roboRIO**, fed by a ~250 Hz odometry sampling thread; nobody fuses on the Pi.

## Local vs global localization + trajectory precision (reviewed 2026-06-23)

Full writeup: **Localization-and-Trajectory-Precision-2027.docx**. Triggered by two 2026 pain points: (a) global-only aiming, 2-3° heading error mattered, no turret cam to check; (b) PathPlanner is time-based and stops when the clock runs out, not when you arrive.

- **Local vs global IS a recognized split.** 6328 "Beyond the Coprocessor" talk (youtube fwEUqL_waPk) sorts needs into *global field localization* (~5-20cm, few°, for driving) vs *precise local positioning* (sub-cm/°, relative to one target, for scoring/aiming). Different accuracy budgets → different tools. Our 2026 robot collapsed both into the global estimate.
- **6328 aiming = global-pose, NO turret, NO target-relative camera.** `LaunchCalculator.java`: aims whole chassis at fixed field constant `FieldConstants.Hub.topCenterPoint` computed from `RobotState.getEstimatedPose()`; velocity×TOF lookahead loop (20 iters) for shoot-on-move; off-center launcher correction in `getDriveAngleWithLauncherOffset`. Entire shot depends on global pose being correct — no inner loop on the goal. Works for them ONLY because fusion is tight + everything is logged/replayed. We had neither → why it failed us.
- **Alternative = boresight/turret camera** closes loop on the goal tag bearing directly (drift-proof angular measurement). CD consensus (thread t/512667): pure tag-relative servoing is flaky because the tag leaves frame on close approach; most 2025 teams fell back to MT2-pose+PID. BUT that was close-range reef alignment, not long-shot aiming where angular precision dominates — the one case boresight earns its keep.
- **VERDICT for us: hybrid.** Global-pose coarse aim (6328 style) + shooter-boresight global-shutter cam for the last ~3° fine correction, and log `poseBearing − camBearing` as the independent check we lacked. Plus carry the prior global-pose fixes (MT2 yaw σ=∞, fuse all cams, dist² weighting) to shrink error at source.

### Trajectory precision — KEY FINDING
- **Every mainstream follower ends on TIME, including 6328's.** `DriveTrajectory.isFinished()` = `timer.hasElapsed(trajectory.getTotalTime())`. PathPlanner `PPHolonomicDriveController` is the same shape (FF from time-sampled state + PID on error). Choreo too. So switching follower does NOT fix the "stops on the clock" problem.
- **The fix top teams use = trajectory for transit, then hand off to a position-based end-controller.** 6328 `AutoCommands`: `new DriveTrajectory(...).until(() -> xCrossed(handoffLine)).andThen(new DriveToPose(...).until(withinTolerance))`. Bail out of the timed path EARLY on a spatial condition, finish with `DriveToPose`.
- **`DriveToPose` ends on POSITION tolerance, not time.** 6328 defaults: 0.01m (1cm) / 1°; profiled PID, trapezoid profile regenerated toward target each loop, FF fades near goal. 3467 ships a reusable version with `.withTolerance(Distance, Angle)` builder; `isFinished()` true only when BOTH linear & angular error within tol. Both files are cloned references.
- Code skeletons (DriveToPose + FineAimToGoal boresight) are in the docx §5.3-5.4, ready to adapt.

## Multi-team comparison: aiming + trajectory precision (reviewed 2026-06-23, beyond 6328)

Cloned & read: 1768 (Choreo), 3467, 5687 (NE 2026); 2910 (2024 turret), 1678 (2024, 254-lineage). Patterns:

**Aiming — local (target-relative) vs global (pose):**
- **2910 Jack in the Bot (TURRET, the cleanest hybrid):** `Superstructure.trackSpeakerWithTurret()` — IF the speaker camera sees the tag, closes the turret loop on the **measured AprilTag angle** (`currentTurretAngle.plus(aprilTagAngle)`) = pure target-relative, drift-proof. ELSE falls back to `trackPoseWithTurret()` = atan2 from global odometry pose to the tag's field position. `RobotState.getAimingParameters(turretLookahead, pivotLookahead)` does velocity-compensated shoot-while-moving (lookahead pose → effectiveDistance + turretAimingAngle). This IS exactly the global-coarse + vision-fine hybrid we recommend, in production, on a turret.
- **1678 Citrus (no turret 2024):** keeps `limelight/GoalTracker`+`GoalTrack` (254-lineage vision target tracking) SEPARATE from `vision/VisionDeviceManager`+`VisionPoseAcceptor` (global pose). Aims by `Drive.snapHeading(angle)` (drivetrain heading-snap, HeadingController) — whole-chassis aim like 6328 but with a dedicated goal-tracker as the local signal.
- **1768 Nashoba (no turret):** aims via `joystickDriveAtAngle(rotationSupplier=ShootingUtil)` = global-pose chassis heading; `atShootingSetpoint()` gates wheel-lock. No target-relative camera. Also has a `driveToPose` (ProfiledPID x/y/theta).
- **6328:** global-pose chassis aim, no turret (already logged above).
- Takeaway: turret teams (2910) get the cleanest local/global split because the turret can hold the goal in frame; chassis-aim teams (6328/1768/1678) lean on global pose + (1678) a separate goal tracker. Validates our hybrid rec from a non-6328 source.

**Driving precision — three distinct patterns for "arrive, don't time-out":**
1. **6328 — handoff:** `DriveTrajectory.until(spatialCond).andThen(DriveToPose.until(withinTolerance))`. Timed path bailed early, precise `DriveToPose` (1cm/1°) finishes.
2. **1768 — position+settle-gated trajectory (arguably cleanest):** `AutoModeBase.cmdWithAccuracy()` overrides the Choreo cmd's finish: completes only when `translationIsFinished` (dist<epsilon) AND `rotationIsFinished` (angle<epsilon), HELD for `kDelayTime` (settle stopwatch), with `withTimeout(totalTime + slack)` as a SAFETY fallback. Time is the backstop, not the trigger. Bakes precision into the trajectory command itself.
3. **3467 — reusable `DriveToPose.withTolerance(Distance, Angle)`** (already logged).
- All confirm: don't let a time-based path *finish* a precise move. 1768's settle-timer idea (require the robot to be inside tolerance for N ms before declaring done) is worth stealing — kills "finished while still drifting."

## Choreo vs PathPlanner / AutoBuilder (reviewed 2026-06-23)

- **Core difference:** Choreo = time-OPTIMAL (solves for the fastest dynamically-feasible trajectory; "follows accurately from the start, less tuning"). PathPlanner = respects swerve dynamics but is NOT time-optimal; more manual control, more tuning, and you CAN draw a path the robot can't follow.
- **2026 specifically:** game paths are mostly straight-line → the two generate ~equivalent paths this season. Not game-changing this year; use what you know.
- **Interop:** PathPlannerLib can FOLLOW Choreo trajectories (`.traj` import) and use them in GUI autos → not either/or; many teams use both. Both real teams here use Choreo (6328 via `choreo.trajectory.SwerveSample` + `AllianceFlipUtil.shouldFlip()`; 1768 via Choreo `AutoTrajectory`).
- **ALLIANCE FLIPPING — our 2026 belief was WRONG/outdated:** ChoreoLib has built-in alliance flipping. `Choreo.createAutoFactory(... BooleanSupplier useAllianceFlipping, Supplier<Optional<Alliance>> ...)` and `trajectory.sampleAt(t, flipped)` / `getFinalPose(flipped)` take a flip boolean. Supports BOTH "rotate-around" (rotationally-symmetric fields) and "mirror" (reflect across centerline). 6328 & 1768 both auto-flip Choreo paths in the cloned code. So Choreo CAN auto-reverse red/blue — no blocker.
- Both follow on TIME regardless (orthogonal to the DriveToPose end-controller decision).
- Sources: CD pathplanner-v-choreo t/484118; pathplanner.dev/pplib-choreo-interop; choreo.autos/choreolib/trajectory-api; choreo.autos/contributing/sample-flipping.

## Repo re-check for newer releases (2026-06-24)

Re-probed top teams for newer localization/trajectory code. NEW since the 06-12 pass:

- **2910 2026** — `FRCTeam2910/2026CompetitionRobot-Public`, published **2026-06-07** (squashed "Initial commit"). EARLY-SEASON / incomplete: **no turret this year** (chassis aim; only flywheel/hood/shooter). Vision = Limelight → `RobotState.addVisionObservation`, but currently a one-time pose "reset," with an explicit in-code TODO: *"Fusing vision in a pose estimator would help, and we want to do that in a separate pose estimator."* → validates our roboRIO-fused-estimator direction. **Choreo path following is stubbed/commented ("not yet implemented").** Has a `driveToPoint` (DriveToPose-style). Not a new source of best practice yet — watch for updates.
- **1678 2026** — `frc1678/C2026-Public`, published **2026-05-31** ("import code for release"). Moved to **CTRE `GeneratedDrivetrain` + `addVisionMeasurement`** (MegaTag) with PhotonVision+Limelight IO; no turret. More vanilla than their 2024 254-style custom (the "black box" estimator we critiqued). Likely early-season.
- **6995 Robot-2026** — `frc6995/Robot-2026`, real-season through **2026-04-15** (dcmp branch). **Live Choreo user via `AutoFactory`** (trigger-based workflow) on CTRE `CommandSwerveDrivetrain`. **TURRET team:** `TurretS(Supplier<Pose2d> robotPose, ...)` → turret aims from the **global pose** (pose-fed turret, not raw vision-direct). **CONCRETE alliance-flip proof:** `new AutoFactory(poseSupplier, resetPose, followPath, true /*useAllianceFlipping*/, this, trajLogger)` — one boolean flips Choreo paths red/blue. Kills the "Choreo can't auto-reverse" myth with a real 2026 example.
- **6328** — still publishing ~daily (through **2026-06-15**). Re-checked the key files: `DriveTrajectory.isFinished()` STILL `timer.hasElapsed(trajectory.getTotalTime())`; `LaunchCalculator` STILL aims at `Hub.topCenterPoint` from `getEstimatedPose()`. **Architecture unchanged — all prior conclusions hold.**

Still NOT released for 2026 (as of 06-24): 254 (only FRC-2025-Public up), 1690 (no public repo found under Team1690/FRC1690), 2056, 1323, 6329/176/230 (NE), 2168.

**Net:** nothing overturns the analysis. 2910's own TODO + 6995's pose-fed turret reinforce the global-pose + (optional) turret framing; 6995 is a citable live alliance-flipping example. Re-check 2910/1678 again later in the season once their pose estimators / Choreo following are fleshed out.

## Open questions / next checks

- Re-check 2910 2026 + 1678 2026 later this season — both are early releases (2910's fused estimator is a TODO; Choreo following stubbed).
- Evaluate Choreo vs PathPlanner for transit trajectories (dynamics-aware, faster) — orthogonal to the DriveToPose end-controller decision.
- Summer test: does the goal tag stay in a fixed boresight cam's frame through a realistic aim, or do we need a turret? (decides turret vs fixed mount).
- Enumerate 195's GitLab repos (needs browser/CLI) — their ROS stack details.
- Re-search 254/2910/1690/2056/6329/176/230/2168 after post-season releases (fall 2026).
- 5687's C++ Limelight fusion details (Camera.h, VisionMeasurement.h) — worth a deeper read if we stay hybrid LL+PV.
- 3467's "c2" coprocessor: what hardware does it run on? (build blog t/508474 likely documents it)
