# Vision and Trajectory Test Plan

> **2026-06-30 update.** This plan covers the on-robot bring-up specifics (camera placement, tag board,
> precision/aiming tests). The full **staged, simulation-first** process — toolchain, sim validation,
> camera intrinsic calibration, extrinsic measurement, localization accuracy, characterization — now
> lives in `CALIBRATION_AND_TEST_PROCESS.md`. Do **Stage 1 (simulation)** there before any hardware.
> Log channels were renamed in the vision rebuild: cameras are `Vision/Camera0`, `Vision/Camera1`, …
> (index order in `RobotContainer`), with `Vision/Summary/*` aggregates.

## Hardware Baseline

- Coprocessor: one Orange Pi to start (scalable to two Pis for four cameras).
- Cameras: two Arducam OV9782 global-shutter color USB cameras to start.
- Pipeline: PhotonVision AprilTag.
- Robot controller: roboRIO runs final fusion and drivetrain control.
- Logging: AdvantageKit WPILOG plus NT4 for live AdvantageScope.

## Camera Placement

Mount the two cameras near the front-left and front-right corners, above or just inboard of the front swerve modules.

Recommended starting transforms from robot center:

| Camera | X forward | Y left | Z up | Roll | Pitch | Yaw |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| front-left | +0.23 m | +0.24 m | +0.43 m | 0 deg | -18 deg | +18 deg |
| front-right | +0.23 m | -0.24 m | +0.43 m | 0 deg | -18 deg | -18 deg |

Notes:

- Keep both cameras rigidly mounted. Flex will look like vision noise.
- Avoid bumper occlusion at low pitch angles.
- Measure final transforms from the robot coordinate origin after mounting; update `Constants.VisionConstants`.
- The Arducam color camera should work for a pilot because it is global shutter and USB UVC. It may need more lighting/exposure discipline than a monochrome AprilTag camera.

## Tag Board Layout

Use two 6.5 inch AprilTags on one flat board.

Code/deploy layout:

- Field size: 8.0 m by 4.0 m.
- Tag 1 pose: `(6.0 m, 2.25 m, 1.05 m)`, on the left when viewed from the robot start area and facing it.
- Tag 2 pose: `(6.0 m, 1.75 m, 1.05 m)`, on the right when viewed from the robot start area and facing it.
- Tag center spacing: 0.50 m horizontally.

Physical setup:

1. Put the board vertical and flat.
2. Put the two tag centers at the same height.
3. Space the tag centers 0.50 m apart.
4. Set tag center height to 1.05 m from the floor.
5. Mark the floor coordinate of the board so the field layout can be reproduced.

## PhotonVision Setup

1. Connect both cameras to the Orange Pi.
2. In PhotonVision, name the cameras exactly:
   - `front-left`
   - `front-right`
3. Set each camera to MJPEG and start around 1280x800 at 50 fps.
4. Calibrate each camera in PhotonVision using the exact resolution used for testing.
5. Load the custom AprilTag layout from:
   `src/main/deploy/apriltags/mecharams-two-tag-layout.json`
6. Set each camera's robot-to-camera transform to match the measured mount.
7. Start with exposure low enough that tag borders are crisp and not washed out.

## First Bring-Up

1. Build/deploy after Java 17 compile verification succeeds.
2. Put the robot at `(1.5 m, 2.0 m, 0 deg)`.
3. Press `A` to reset pose to the test start.
4. Open AdvantageScope and watch:
   - `Vision/Camera0/AcceptedFrames`, `Vision/Camera0/RejectedFrames`, `Vision/Camera0/LastRejectionReason`
   - `Vision/Camera1/AcceptedFrames`, `Vision/Camera1/RejectedFrames`, `Vision/Camera1/LastRejectionReason`
   - `Vision/Camera0/LastInnovationMeters` (how far each accepted frame pulls the estimate)
   - `Vision/Summary/AcceptedPoses`, `Vision/Summary/RejectedPoses`, `Vision/Summary/TagPoses`
   - `Drive/Pose` (the fused robot pose)
5. Rotate the robot slowly and verify each camera sees both tags over a useful range.

## Precision Test

1. Place the robot at the marked start.
2. Press `A`.
3. Press `X` to run precision drive to `(4.25 m, 2.0 m, 0 deg)`.
4. Measure final robot position with a tape measure.
5. Save the AdvantageKit log.
6. Repeat with vision enabled and with tags blocked.
7. Compare final error, rejected frame reasons, and tag distances.

## Coarse Path Plus Precision

PathPlanner should be used for coarse motion. The final 0.5-1.0 m should be handled by the precision command.

1. Open PathPlanner for this project.
2. Create an auto named `VisionTest`.
3. Start at `(1.5 m, 2.0 m, 0 deg)`.
4. End the coarse path around `(3.6 m, 2.0 m, 0 deg)`.
5. Keep constraints conservative: 1.6 m/s, 1.2 m/s^2, 120 deg/s, 180 deg/s^2.
6. Run the auto and then run `Precision Drive To Tag Board`.
7. If the coarse path drifts, fix drivetrain characterization before tuning vision.

## Chassis Aiming Test (no turret/mechanism)

Aims the whole robot at `AimConstants.GOAL_POSITION` (a configurable virtual goal).

1. Set `AimConstants.GOAL_POSITION` to the target you want to face.
2. Press `A` to reset pose.
3. Stationary: press the right stick (`Aim At Goal - Stationary`). Watch `Aim/HeadingErrorDegrees`
   settle below the tolerance and `Aim/Aimed` go true.
4. Moving: hold the right trigger and translate with the left stick. Confirm the robot keeps facing the
   goal and `Aim/LeadPose` shifts ahead of the robot when moving (shoot-on-move lead).
5. Save the log. When a boresight/turret camera + real target exist later, also log
   `poseBearing - cameraBearing` as the independent aim check.

## Characterization

Run SysId in this order:

1. Translation quasistatic forward/reverse.
2. Translation dynamic forward/reverse.
3. Steer tests only when modules are safely supported.
4. Rotation tests in a clear area.

Controls are documented in `ROBOT_CONTROLS.md`.

After characterization:

- Update wheel radius if distance is biased.
- Update drive feedforward/gains.
- Rerun the precision test and compare logs.

## 2026-07-16 A/B Validation Plan: TrigSolve + Anisotropic Covariance

Two new vision options are implemented behind runtime toggles (set automatically by the auto chooser;
logged every loop at `Vision/Modes/*`):

- **TrigSolve** (single-tag strategy): single-tag XY recomputed from camera-to-tag translation + the
  odometry-buffer heading. Expect better single-tag XY, identical multi-tag behavior.
- **AnisoCov** (covariance model): X/Y noise weighted along/across the camera->tag ray. PROVISIONAL
  coefficients until stage R2 fits them from real logs.

Read `ALGORITHM_COMPARISON_AND_EXPECTATIONS.md` first: it explains the logical differences, the
error budgets, and a per-test prediction table ("prediction -> what a deviation means") that turns
each run below into a diagnosis, not just a pass/fail.

Baseline = "PNP + Isotropic" (the validated 2026-06-30 behavior). Every baseline auto now sets its
modes explicitly, so old and new logs stay comparable.

### Metrics to record for EVERY run

- End-pose error to target (4.25, 2.0) at `DriveToPose/Finished`, and again at disable (drift check).
- `DriveToPose/*`: settle time, timedOut flag.
- `Vision/Summary/AcceptedPoses` scatter (tightness), `TrigSolvedPoses` count vs single-tag frames.
- `Vision/Camera*/LastInnovationMeters` envelope during steady tracking.
- `Vision/Modes/*` (confirms configuration), selected auto name (logged by the chooser).

### Sim sequence (run first, in order)

- **S1 — Regression**: build + `gradlew test` green (45/45), then rerun the three baseline autos
  ("Precision To Tag Board", "VisionTest then Precision (sequential)", "VisionTest (spatial
  handoff)"). PASS: end-pose errors match the 2026-07-01 results (~0.03-0.05 m), no timeouts, and
  `TrigSolvedPoses` stays EMPTY (proves the default path is untouched).
- **S2 — TrigSolve precision-only**: run "AB: Precision To Tag Board (TrigSolve)" vs baseline
  "Precision To Tag Board", 3 runs each. PASS: TrigSolve end-pose error <= baseline; no timeout;
  `LastUsedTrigSolve` true on single-tag frames; no oscillation in `Drive/Pose` near the board.
- **S3 — TrigSolve handoff**: "AB: VisionTest spatial handoff (TrigSolve)" vs baseline spatial
  handoff, 3 runs each. Watch the handoff region (x > 3.3) where single-tag views dominate. PASS:
  equal-or-better end-pose error and equal-or-tighter AcceptedPoses scatter.
- **S4 — AnisoCov**: "AB: VisionTest spatial handoff (AnisoCov)", 3 runs. This mostly checks for
  REGRESSION in sim (sim noise is roughly isotropic, so big wins are not expected before R2 fits the
  coefficients). PASS: end-pose error within tolerance of baseline; no new timeouts; post-command
  drift (finish -> disable delta) no worse than baseline.
- **S5 — Combined**: "AB: VisionTest spatial handoff (TrigSolve+AnisoCov)", 3 runs. PASS: no
  regression vs the better of S3/S4. Also do one reset test (drive away, press A in teleop) in
  TrigSolve mode: pose must snap and stay (quarantine already covers the heading-buffer race).

Sim verdict rule: if S2/S3 show TrigSolve >= baseline, promote TrigSolve to the default single-tag
strategy for robot testing (keep the toggle). AnisoCov stays experimental until R2.

### Real-robot sequence (when robot time is available)

- **R0 — Prereqs** (CALIBRATION_AND_TEST_PROCESS.md Stages 2-3): PhotonVision intrinsic calibration
  per camera (use the mrcal-based calibration; verify reprojection error), measure + update
  robot-to-camera transforms, verify tag board coordinates. Without R0 all pose numbers are noise.
- **R1 — Static localization grid (the key TrigSolve test)**: mark ~6 floor positions at 1-4 m from
  the board, some angled so only ONE tag is visible (single-tag is where TrigSolve differs). At each
  mark: log 10+ s in baseline mode, then 10+ s in TrigSolve mode (teleop; toggle via the AB autos or
  a temporary dashboard switch). Measure `Drive/Pose` error vs the tape-measured truth and the
  AcceptedPoses scatter. PASS: TrigSolve single-tag XY error and scatter <= PnP at every mark.
- **R2 — Covariance fitting (turns AnisoCov from provisional to real)**: from the R1 logs, for each
  distance bucket compute the std dev of accepted-pose error parallel and perpendicular to the
  camera->tag ray (AdvantageScope export -> spreadsheet/Python). Fit `sigma = C * d^E` for each
  direction, single-tag and multi-tag separately; write the fitted values into
  `VisionConstants.ANISO_*` (replace the PROVISIONAL comment with the fit date + log file names).
  IMPORTANT: fit with the single-tag strategy you intend to compete with (fit under TrigSolve if
  S2/S3 promoted it) — TrigSolve changes the shape of single-tag error, so coefficients fitted under
  PnP would bake the wrong ellipse in (see ALGORITHM_COMPARISON_AND_EXPECTATIONS.md section 2).
- **R3 — Precision A/B on carpet**: repeat S2/S3 on the robot, 5 runs per configuration; record
  mean + worst end-pose error. PASS: TrigSolve mean error <= baseline and no new timeouts.
- **R4 — AnisoCov + combined**: with R2-fitted coefficients, repeat S4/S5 on the robot. PASS:
  equal-or-better end-pose error AND reduced post-command drift (this directly targets the
  2026-07-01 drift watch item).
- **R5 — Decision + cleanup**: pick the default configuration from R3/R4 data, update
  SESSION_STATE + DESIGN_DECISIONS with the numbers, and keep the losing modes available as
  chooser options for regression testing.

Order rationale: R1 isolates localization from driving; R2 must precede any serious AnisoCov
judgment; only then do the driving A/Bs (R3/R4) measure the end-to-end effect.

### Execution checklist (exact run order — 2026-07-16)

The three test motions (all share START_POSE (1.5, 2.0, 0°) and the precision target (4.25, 2.0, 0°),
so end-pose numbers compare 1:1 across every step):

- **M1 — Straight precision-only**: no path; `DriveToPosePrecisionCommand` drives 2.75 m straight
  forward (1.5, 2.0) → (4.25, 2.0). Chooser: `Precision To Tag Board` / `AB: ... (TrigSolve)`.
- **M2 — Straight path + handoff**: PathPlanner `VisionTest` (straight, (1.5, 2.0) → (3.6, 2.0)),
  spatial handoff at x > 3.3, precision finish. Chooser: `VisionTest (spatial handoff)` + AB variants.
- **M3 — Curved path + handoff**: PathPlanner `VisionTestCurved` — an S-curve dipping to (2.55, 1.25)
  with a **25° rotation sweep at mid-path** (heading 0° → 25° → 0°), then the same x > 3.3 handoff and
  precision finish. This is the vision-stress trajectory: lateral motion + rotation changes which
  camera sees which tag, creating the single-tag stretches where the strategies actually differ.
  Chooser: `VisionTestCurved (spatial handoff)` / `AB: Curved handoff (TrigSolve)` /
  `AB: Curved handoff (TrigSolve+AnisoCov)`.

For every run record the metrics listed at the top of this plan (end-pose error at finish AND at
disable, settle time, timeout flag, AcceptedPoses/TrigSolvedPoses, innovation envelope, Modes).

**Simulation (do in this order):**

1. Build gate: `gradlew.bat compileJava test jacocoTestCoverageVerification` — all green (45 tests,
   coverage gate) before any sim run.
2. M1 baseline — `Precision To Tag Board`, 3 runs. PASS: matches 2026-07-01 (~0.045 m), no timeout,
   `TrigSolvedPoses` EMPTY.
3. M1 TrigSolve — `AB: Precision To Tag Board (TrigSolve)`, 3 runs. Compare to step 2.
4. M2 baseline — `VisionTest (spatial handoff)`, 3 runs. PASS: ~0.03-0.09 m, no timeout.
5. M2 TrigSolve — `AB: VisionTest spatial handoff (TrigSolve)`, 3 runs. Compare to step 4.
6. M2 AnisoCov — `AB: VisionTest spatial handoff (AnisoCov)`, 3 runs. Regression check only
   (provisional coefficients — expect no visible change).
7. M2 combined — `AB: VisionTest spatial handoff (TrigSolve+AnisoCov)`, 3 runs.
8. M3 baseline — `VisionTestCurved (spatial handoff)`, 3 runs. Establishes the curved baseline
   (expect slightly worse than M2 — the rotation sweep degrades vision coverage mid-path).
9. M3 TrigSolve — `AB: Curved handoff (TrigSolve)`, 3 runs. This is where sim should show the
   clearest TrigSolve gap (most single-tag frames of any sim test).
10. M3 combined — `AB: Curved handoff (TrigSolve+AnisoCov)`, 3 runs.
11. Reset test in TrigSolve mode: teleop, drive ~2 m away from a known pose, press A once. PASS:
    pose snaps and STAYS (no bounce-back within 2 s).
12. Sim verdict: fill the results table (below) and apply the rule — if steps 3/5/9 are
    equal-or-better than 2/4/8, promote TrigSolve to default for robot testing. Close the sim log
    cleanly each session (disable, stop sim, then open the .wpilog).

**Real robot (after sim verdict; do in this order):**

13. R0 prerequisites: PhotonVision intrinsics per camera (mrcal), measured robot-to-camera
    transforms, verified tag-board coordinates, drivetrain characterization values entered.
14. Static localization grid (R1): ~6 taped floor positions at 1-4 m, several angled so only ONE tag
    is visible. At each: 10+ s logged in baseline, 10+ s in TrigSolve. PASS: TrigSolve error/scatter
    <= PnP at every position.
15. Covariance fit (R2): from step-14 logs, fit ANISO_* power laws parallel/perpendicular to the
    ray — **with the single-tag strategy that won step 14 enabled**. Update Constants + provenance
    comment.
16. M1 on robot — 5 runs baseline, 5 runs winning strategy. Compare mean AND worst case.
17. M2 on robot — 5 runs each: baseline, TrigSolve, combined (with fitted coefficients).
18. M3 (curved) on robot — 5 runs each: baseline, TrigSolve, combined. Watch the mid-curve
    AcceptedPoses scatter and the post-command drift at disable.
19. Decision (R5): adopt the configuration with the best WORST-CASE end-pose error across steps
    16-18; record numbers in SESSION_STATE + DESIGN_DECISIONS; keep losing modes as chooser options
    for regression.

Results table template (copy per environment):

| Step | Motion | Config | Run 1 | Run 2 | Run 3 (4/5) | Worst | Timeout? | Drift at disable |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 2 | M1 | PNP+ISO | | | | | | |
| 3 | M1 | TRIG+ISO | | | | | | |
| 4 | M2 | PNP+ISO | | | | | | |
| 5 | M2 | TRIG+ISO | | | | | | |
| 6 | M2 | PNP+ANISO | | | | | | |
| 7 | M2 | TRIG+ANISO | | | | | | |
| 8 | M3 | PNP+ISO | | | | | | |
| 9 | M3 | TRIG+ISO | | | | | | |
| 10 | M3 | TRIG+ANISO | | | | | | |

### What the results decide (outcome policy)

- One **fixed default configuration** is adopted at step 19 (single-tag strategy + covariance
  model); no driver-switched vision modes. The `AB:` chooser options remain as regression/practice
  tools — re-run this checklist after any vision change.
- Per-frame algorithm selection (multi-tag vs single-tag vs reject vs PnP-fallback) is automatic
  and stays as-is in every configuration.
- Conditional follow-ups, only if the data demands them: heading-health PnP fallback (if TrigSolve
  degrades in low-multi-tag stretches), ambiguity-gate relaxation (if TrigSolve wins), Constrained
  SolvePnP, SwerveSetpointGenerator (post-characterization).
- Full rationale: ALGORITHM_COMPARISON_AND_EXPECTATIONS.md section 4.

### Step 20 — Camera-placement analysis (same logs, post-processing only)

From the step 8-10 (curved) and step 14 (static grid) .wpilog files, per camera and per robot-pose
bucket, count loops with 0 / 1 / 2 visible tags (`Vision/Camera*` tagIds) and plot the per-camera
error-vs-distance curves from step 15's fit. Decisions fed: cross-eye yaw (currently +/-18 deg
provisional), the 2-vs-4 camera scale-up (rear coverage gaps = the trigger), per-camera
CAMERA_STD_DEV_FACTORS from data, and pitch sanity (tag clipping near the board). Record
conclusions in ARCHITECTURE_AND_DEPLOYMENT.md + Constants with provenance. Details:
ALGORITHM_COMPARISON_AND_EXPECTATIONS.md section 5.
