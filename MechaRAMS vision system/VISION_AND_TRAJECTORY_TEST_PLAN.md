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
- Tag 1 pose: `(6.0 m, 1.75 m, 1.05 m)`, facing toward the robot start area.
- Tag 2 pose: `(6.0 m, 2.25 m, 1.05 m)`, facing toward the robot start area.
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
