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
- Tag 1 pose: `(6.0 m, 2.25 m, 1.50 m)`, on the left when viewed from the robot start area and facing it.
- Tag 2 pose: `(6.0 m, 1.75 m, 1.50 m)`, on the right when viewed from the robot start area and facing it.
- Tag center spacing: 0.50 m horizontally.

Physical setup:

1. Put the board vertical and flat.
2. Put the two tag centers at the same height.
3. Space the tag centers 0.50 m apart.
4. Set both tag centers to the measured height of 1.50 m from the floor.
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

### 2026-08-10 measured straight-line distance correction

The real robot traveled 1.05 m for a reported 1.00 m and 2.10 m for a reported 2.00 m. Because the
actual/reported ratio was a repeatable 1.05 at both distances, the effective rolling radius was updated
from 2.000 in to 2.100 in in both CTRE and PathPlanner configuration. Before changing precision-controller
gains, rerun 1 m and 2 m with one camera covered and record both the maximum excursion and final physical
distance. Expected result: the systematic +5% excursion is removed; any remaining overshoot is then a
controller-damping/acceleration issue rather than encoder scale.

### 2026-08-12 measured front-camera transforms

The physical offsets retained from direct measurement are left `(0.152, +0.266, 0.420)` m and right
`(0.152, -0.266, 0.435)` m. With the chassis square to the tag-board plane, stable two-tag solves gave
left pitch/yaw `-18.88/-14.80` degrees and initially gave right pitch/yaw `-17.10/+13.69` degrees; roll
is constrained to zero. The 2026-08-31 paired-log correction changes only right yaw to `+15.74`
degrees. After deployment, use the 100-sample jitter capture below before rerunning the 1 m test. Both
cameras should yield materially closer robot poses and endpoint `AtGoal` should stop repeatedly
resetting.

### 2026-08-19 closed-loop velocity validation

The six-run open-loop baseline ended at the correct physical distance overall (through-origin scale
error only +0.14%), but every estimator trace went 0.148-0.184 m beyond the target before reversing.
At first target crossing, requested chassis speed was only 0.422-0.483 m/s while measured speed was
still 0.762-0.849 m/s. The next controlled version therefore changes only the precision drivetrain
request from CTRE's default `OpenLoopVoltage` to `Velocity`. Wheel radius remains 2.100 inches;
precision constraints remain 1.6 m/s and 2.5 m/s^2; pose gains remain unchanged.

Run three `Forward 1m - PnP + Iso` tests followed by three `Forward 2m - PnP + Iso` tests. Use both
cameras for this first comparison. Do not tune between runs.

For every run, record these physical measurements:

- center forward displacement;
- forward displacement at the left and right bumper reference points (needed to quantify yaw);
- center lateral displacement from the starting line (needed to distinguish strafe drift from yaw).

In an AdvantageScope **Line Graph**, put these scalar fields on the left axis:

- `DriveToPose/Controller/RequestedVxRobotMetersPerSecond`;
- `DriveToPose/Controller/MeasuredVxRobotMetersPerSecond`;
- `DriveToPose/Controller/TrackingErrorVxRobotMetersPerSecond`;
- `DriveToPose/ErrorXFieldMeters`;
- `DriveToPose/Controller/RequestedVyRobotMetersPerSecond`;
- `DriveToPose/Controller/MeasuredVyRobotMetersPerSecond`.

Put these angular fields on the right axis:

- `DriveToPose/Controller/RequestedOmegaDegreesPerSecond`;
- `DriveToPose/Controller/MeasuredOmegaDegreesPerSecond`;
- `DriveToPose/ErrorThetaSignedDegrees`.

Add these values to a **Table** or keep them available for the saved log:

- `DriveToPose/Controller/DriveRequestType` (must be `Velocity`) and `Active`;
- `DriveToPose/Controller/ProfileVxFieldMetersPerSecond` and `FeedbackVxFieldMetersPerSecond`;
- `DriveToPose/Controller/ProfileVyFieldMetersPerSecond` and `FeedbackVyFieldMetersPerSecond`;
- `DriveToPose/Controller/TranslationCommandClamped` and `RotationCommandClamped`;
- `DriveToPose/TranslationErrorMeters`, `RotationErrorDegrees`, `SettleSeconds`, `Finished`, `TimedOut`;
- `SystemStats/BatteryVoltage`;
- both cameras' `AcceptedFrames`, `RejectedFrames`, `LastInnovationMeters`, and
  `LastRejectionReason`.

Before motion, verify `SystemStats/BatteryVoltage` is plausible. PDH current logging is intentionally
disabled until the actual module ID/bus is verified; do not try alternate CAN IDs by trial and error.

Primary comparison: peak X excursion, time from first target crossing until the robot remains inside
tolerance, and requested-versus-measured vx during deceleration. A useful first-pass result is peak
overshoot below 0.05 m with measured vx closely following requested vx. If velocity tracking is still
poor, tune/characterize the TalonFX drive velocity loop before changing the pose PID. If tracking is
good but the pose still overshoots, tune the pose profile/PID next. Only after longitudinal response is
stable should the test be repeated with left-only and right-only vision to isolate the remaining yaw.

## 2026-08-20 measured-distance feedforward validation

Run these in order. Do not advance to the next gate if the robot oscillates violently, times out, or
the measured peak excursion is more than 0.10 m beyond the target.

1. **Disabled preflight:** manually build/deploy, then open live AdvantageScope. Confirm
   `DriveToPose/Controller/DriveRequestType = Velocity` and `SystemStats/BatteryVoltage` is plausible.
   Confirm both cameras are connected
   and accepting MultiTag frames. Rotate to a fresh log while disabled.
2. **One 1 m safety run:** select `Forward 1m - PnP + Iso`, seed pose from vision, then run once. Mark
   and measure (a) maximum center excursion before any return and (b) final settled center position.
   Also record final left/right bumper distances and lateral displacement. Stop here and upload the log
   if physical peak overshoot exceeds 0.05 m or it makes more than one clear return correction.
3. **One 2 m safety run:** only after step 2 passes, repeat with `Forward 2m - PnP + Iso`. Take the same
   maximum and settled measurements. Stop if physical peak overshoot exceeds 0.05 m.
4. **Repeatability set:** if both safety runs pass, collect two additional 1 m runs and two additional
   2 m runs, each in a separately rotated log. This produces three runs at each distance. Keep battery
   state similar and seed from the same stationary MultiTag view before each run.
5. **Analysis handoff:** provide the six log suffixes and, for every run, maximum center excursion,
   settled center distance, final left/right bumper distances, and lateral displacement. The analysis
   gate is estimator peak overshoot below 0.05 m, no repeated target crossings, and requested/measured
   vx converging during the faded region.
6. **Only after the profile gate passes:** run translation SysId in a long clear lane, in this order:
   quasistatic forward, quasistatic reverse, dynamic forward, dynamic reverse. Select translation with
   D-pad up and use the documented Xbox SysId hold controls. Start a fresh log before the four tests.
   These data tune the TalonFX drive velocity feedforward/feedback; do not increase trajectory speed
   until that characterization is reviewed.

Add these new scalars to the existing precision table/graph:

- `DriveToPose/Controller/TranslationFeedforwardScale`;
- `DriveToPose/Controller/RawProfileVxFieldMetersPerSecond`;
- `DriveToPose/Controller/RawProfileVyFieldMetersPerSecond`;
- `DriveToPose/Controller/DistanceToTargetMeters`;

The existing `ProfileVx/VyFieldMetersPerSecond` values now mean the faded feedforward actually used by
the controller; the new `RawProfileVx/Vy...` values preserve the internal profile before the fade.

## 2026-08-24 damped-stop validation (supersedes the next 2 m step above)

After deploying this revision, restart with **one 1 m run only**. Do not run 2 m until this gate passes.

1. Disable, rotate to a fresh log, and verify the new fields below exist. Confirm
   `SystemStats/BatteryVoltage` is plausible. Direct PDH polling is intentionally disabled.
2. Seed from a stationary MultiTag observation, select `Forward 1m - PnP + Iso`, and run once.
3. Measure maximum center excursion, final settled center distance, final left/right bumper distances,
   and lateral displacement. Also time from first physical crossing of 1.000 m until all visible motion
   stops; phone video is the easiest reliable measurement.
4. Stop and upload that single log. Define settling time as first target crossing until pose and chassis
   speed enter their limits and stay there. Pass gates: settling time <10% of total active move time,
   peak physical overshoot <=0.03 m, no timeout, and the latched settling hold remains continuously
   true for its final 0.05 s. `SettlingHoldExitCount = 0` is preferred, but an exit is correct when a
   widened pose or measured-motion escape limit is genuinely crossed.

New fields to add to the AdvantageScope table/graph:

- `DriveToPose/WithinPoseTolerance` and `WithinVelocityTolerance`;
- `DriveToPose/SettlingHoldActive`;
- `DriveToPose/GoalQualifiedThisLoop`, `OutsideSettlingEscapeTolerance`,
  `OutsideSettlingEscapePoseTolerance`, and `OutsideSettlingEscapeVelocityTolerance`;
- `DriveToPose/PoseToleranceEntryCount`, `AtGoalEntryCount`, and `SettlingHoldExitCount`;
- `DriveToPose/MeasuredTranslationSpeedMetersPerSecond`;
- `DriveToPose/MeasuredRotationSpeedDegreesPerSecond`;
- `DriveToPose/Controller/TranslationDampingScale`;
- `DriveToPose/Controller/DampingVxFieldMetersPerSecond`;
- `DriveToPose/Controller/DampingOmegaDegreesPerSecond`;
- `DriveToPose/Controller/ControllerRequestedVxRobotMetersPerSecond`;
- `DriveToPose/Controller/ControllerRequestedOmegaDegreesPerSecond`;
- `SystemStats/BatteryVoltage`.

Existing `RequestedVxRobot...` and `RequestedOmega...` are now the commands actually applied after the
settling hold; the new `ControllerRequested...` fields preserve what the controller would have requested
before the hold forced zero.

## 2026-08-24 wheel-defined settling and camera A/B

The `cca9ab7b` 1 m run reached `abs(ErrorX) <= 0.04 m` at +1.424 s, but the mean absolute measured
module speed remained above 0.02 m/s until +2.142 s. The resulting 0.719 s visible-motion tail is 32.6%
of the 2.203 s active command, so it fails the mentor's <10% goal even though fused peak X overshoot was
only 0.37 cm. The full goal check is radial translation error <=0.04 m **and** rotation error <=1.5
degrees, plus the velocity qualification; X alone is not the threshold.

After manually building and deploying the PDH hotfix, first verify while disabled that no repeated
`CAN: Message not found` errors occur and that `SystemStats/BatteryVoltage` is plausible. Add these exact
new fields to the table:

- `Drive/MeanAbsModuleSpeedMetersPerSecond`;
- `Drive/MaxAbsModuleSpeedMetersPerSecond`;
- `Drive/MeanAbsModuleTargetSpeedMetersPerSecond`;
- `Drive/MaxAbsModuleTargetSpeedMetersPerSecond`;
- `Drive/WheelsStopped`.

Do not widen the pose tolerance or add profile PID derivative yet. The terminal camera poses differed
by approximately 0.034-0.071 m, comparable to the 0.04 m goal tolerance. Run two controlled 1 m tests,
each in a fresh log: (1) cover the left camera and use the right camera only; (2) cover the right camera
and use the left camera only. Keep all other settings unchanged. For each, record maximum and final
left/right bumper travel, lateral displacement, first apparent arrival time, and the time every wheel
physically stops. Upload both logs before changing covariance, capture tolerance, or low-level gains.

### Controlled drive-velocity `kP` and command-time comparison — `kP` rejected

The `ef00a32a` run showed mean measured module speed 0.4696 m/s while mean target speed was only
0.0578 m/s at +1.405 s. For one controlled braking test, only the TalonFX drive velocity `kP` changes
from 0.10 to 0.20 V/rps. `kS=0`, `kV=0.124`, `kA=0`, outer pose gains, vision modes, and trajectory
constraints remain unchanged. The post-qualification confirmation changes from 0.15 s to 0.05 s. The
ideal 1 m profile at 2.5 m/s^2 is 1.265 s, so approximately 1.3 s is the best-case command target before
raising motion constraints.

`20011a08` confirmed `kP=0.20` and `SettleSeconds=0.05`. It failed: 1.988 s command time, 0.03 m
physical overshoot, five measured-vx sign changes after crossing, and approximately 0.90 s from X-band
arrival to strict wheel stop. Restore `kP=0.10` and retain only the 0.05 s confirmation.

Next run: manually build/deploy, verify `KP=0.10` and `ConfiguredSettleSeconds=0.05` while disabled,
then run exactly one fresh-log `Forward 1m - PnP + Iso` with both cameras. This isolates confirmation
time from the rejected motor-gain change. Do not run 2 m or change status-signal rates first.

## 2026-08-31 wall-clock profile synchronization validation

`7b6d` is the before-run: physical center travel `0.9725 m`, fused travel `0.9661 m`, command time
`2.275 s`, and a `0.572 s` tail after first reaching 6 cm. Controller calls averaged `34.5 ms`, but the
internal profile advanced only one 20 ms step per call. At 14.1 cm remaining, stale-profile feedback
was already `-0.272 m/s`, so the controller requested reverse before the physical robot reached the
target and later corrected forward again.

After manually building/deploying the profile-clock revision:

1. Keep both cameras open, square the frame to the board, seed once, and rotate to a fresh log.
2. Select `Forward 1m - PnP + Iso` and run exactly once. Do not run 2 m yet.
3. Measure maximum and final left/right frame-corner travel and lateral displacement. Phone video is
   useful but optional; the two ruler endpoints remain required.
4. Upload the log suffix and measurements. Pass gates are physical center `0.97-1.03 m`, peak excursion
   no more than `1.05 m`, no more than one visible direction reversal, no timeout, and time from first
   <=6 cm error to command finish below 10% of total command time.
5. Verify `ConfiguredProfilePeriodSeconds = 0.020`; `ProfileStepsThisExecute` should commonly be 1 or 2
   and may reach 5 after a long loop. The raw profile must no longer remain behind the measured robot
   long enough to create a sustained negative feedback request while the target is still forward.

Add these exact saved-log keys to the line graph/table:

- `/RealOutputs/DriveToPose/Controller/LoopDtMilliseconds`;
- `/RealOutputs/DriveToPose/Controller/ProfileStepsThisExecute`;
- `/RealOutputs/DriveToPose/Controller/ProfileTimeAccumulatorMilliseconds`;
- `/RealOutputs/DriveToPose/Controller/ProfileElapsedSeconds`;
- `/RealOutputs/DriveToPose/Controller/WallElapsedSeconds`;
- `/RealOutputs/DriveToPose/Controller/ProfileClockLagMilliseconds`;
- `/RealOutputs/DriveToPose/Controller/ConfiguredProfilePeriodSeconds`;
- `/RealOutputs/DriveToPose/Controller/ConfiguredMaxProfileStepsPerExecute`.

Use `C:\MechaRAMS\Temp\AdvantageScope 8-31-2026 - Profile Clock Sync.json` for this A/B run.

### `c346` result and next gate

The first deployed profile-clock run passed: `1.274 s` total command time, `0.114 s` from first <=6 cm
to finish (`8.95%`), one hold entry, zero hold exits, and `0.0031 m` final fused error. Physical center
travel was `1.010 m`. Keep commit `39ab164` and do not change profile, translation gains, damping,
tolerances, camera weighting, or status-signal rates for the next run.

Run one `Forward 2m - PnP + Iso` in a fresh log with both cameras open and the same squared setup. No
new deploy is needed. Record maximum and final left/right frame-corner travel and lateral displacement.
Stop if peak center travel exceeds `2.05 m` or if repeated terminal corrections return. The 1 m corner
difference was `0.030 m` (about `-2.83 degrees` over the measured frame width), while fused gyro-owned
heading ended near `+0.19 degrees`; if a comparable signed difference repeats at 2 m, instrument raw
Pigeon heading delta versus wheel-only kinematic yaw before changing theta gains or yaw tolerance.

Also retain the loop-timing graph: `c346` contained early `180.3/116.6 ms` controller gaps despite its
successful endpoint. Those stalls must be isolated before increasing speed or acceleration.

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

### Current-pose forward checkout (2026-08-09)

Before the longer fixed-field tests, use the `Forward 1m - ...` and `Forward 2m - ...` chooser options
for a controlled real-robot checkout. Each option captures the fused pose at autonomous start and
targets `(startX + distance, startY, 0 degrees)` without resetting odometry. Mark the physical robot
center before the run and measure the center after it. Run the 1 m PnP+Iso option first at low-risk
clearance, then the other 1 m modes, and only then repeat at 2 m. For every run record:

- captured starting `Drive/Pose` immediately before enable;
- `DriveToPose/TargetPose`, `MeasuredPose`, translation/rotation error, timeout, and settle time;
- `Vision/Modes/*`, both cameras' accepted/rejected counts and last rejection reasons;
- tape-measured travel and lateral deviation.

All four variants at a given distance use the same profiled precision motion controller; only the
vision/localization algorithm changes, keeping the comparison controlled.

Immediately before selecting/enabling a forward run, keep both board tags visible and press the Xbox
left stick. Confirm the Driver Station message `Seeded drivetrain pose from fresh MultiTag vision`,
`Vision/ManualSeed/Succeeded = true`, and that `Drive/Pose` matches the surveyed pose. If the seed is
rejected, do not run the auto; restore a fresh two-tag view and resolve camera/layout rejection first.

### Execution checklist (exact run order — 2026-07-16)

### Straight spatial-handoff safety revalidation (2026-09-02)

The old fixed `(1.5, 2.0)` straight auto caused an initial reverse move on the real robot after vision
restored the actual pose. Validate the replacement before resuming the older A/B sequence:

Use `C:\MechaRAMS\temp\AdvantageScope 9-2-2026 - Spatial Handoff Velocity Retest.json`; it contains
separate saved-log and live NetworkTables tables plus the selected coarse goal-end velocity. The prior
`Current Start Spatial Handoff` layout remains untouched.

1. Deploy the new robot code manually; keep the robot disabled, remove obstacles both in front and
   behind, and keep both tags visible.
2. Select `VisionTest (spatial handoff)`. Confirm the live PhotonVision MultiTag solve is stable. Its
   displayed field-to-camera pose is not the robot-center pose used by the auto. Before enabling,
   require `PathPlanner/VisionTest/Preflight/ReadyToEnable=true` and `Preflight/Status=READY`; inspect
   `Preflight/RobotPose` and `Preflight/ExpectedTotalTravelMeters`.
3. Enable once with an operator ready to disable immediately. The first physical motion must be
   forward. The robot must not first seek x=`1.5 m`.
4. After enabling, require `PathPlanner/VisionTest/StartAccepted=true`, `AbortReason=NONE`,
   measured robot-center start x=`1.2..2.6 m`, y=`1.5..2.5 m`, and normalized start yaw=`0 degrees`.
   From the observed camera X `2.399 m`, expected robot-center X is near `2.247 m`.
5. Graph `Drive/Pose`, `PathPlanner/ActivePath`, `DriveToPose/TargetPose`, module target/measured speeds,
   `DriveToPose/Controller/Active`, `AtGoal`, and `Finished`. Confirm the coarse path advances toward
   x=`3.6 m`, hands off after x=`3.3 m`, and finishes near robot-center x=`4.25 m`. This leaves the
   camera lenses about `1.60 m` in X from the x=`6.0 m` tag plane, with both tags visible.
   Also require `PathPlanner/VisionTest/CoarseGoalEndVelocityMetersPerSecond=1.4` for spatial handoff.
   The module target speed should no longer fall toward zero and then rise again around the handoff.
6. If `StartAccepted=false`, do not bypass the gate. Read `AbortReason`, restore a fresh two-tag view
   or correct the physical start, and retry only after the cause is understood.

For the first post-change A/B, run one spatial-handoff test from the same squared start and capture the
wpilog plus ruler distances at both front frame corners. Compare the minimum module target/measured
speed in the final `0.4 s` of PathPlanner with the first `0.4 s` of precision control, command duration,
final translation/yaw error, settle duration, and timeout. No controller gains, handoff/target geometry,
vision weights, constraints, or direct 1 m/2 m behavior changed in this correction.

The three test motions share the precision target `(4.25, 2.0, 0 degrees)`, so end-pose numbers compare
1:1. M1 and the straight M2 now begin at the measured robot pose; only the curved M3 retains its
legacy fixed start:

- **M1 — Straight precision-only**: no path; `DriveToPosePrecisionCommand` drives from the current pose
  to `(4.25, 2.0)`. Chooser: `Precision To Tag Board` / `AB: ... (TrigSolve)`.
- **M2 — Straight path + handoff**: a fresh MultiTag robot start generates a straight PathPlanner path
  to `(3.6, 2.0)`, with spatial handoff at x > 3.3 and precision finish. Chooser: `VisionTest (spatial
  handoff)` + AB variants.
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

## Real-robot WPILOG finalization

The robot records continuously to `/home/lvuser/logs`. At the end of a test batch:

1. Disable the robot.
2. Press SmartDashboard `Close Current Log And Start New (Disabled Only)` exactly once.
3. Wait for `RealOutputs/Logging/RotationPending = false`, verify `RotationCount` incremented, and
   confirm `LastRotationError` is empty. `ActiveLogPath` identifies the new active file.
4. Download the previous (closed) `.wpilog`, not the small new active file.
5. Open the downloaded file and confirm its final timestamp is later than the last recorded run before
   leaving the test area.

Filesystem open/close operations run on background threads; the AdvantageKit receiver only swaps the
already-open writer between complete tables. Do not power-cycle or delete the active file as a substitute
for this procedure.

After downloading and validating every log that must be retained, the disabled-only SmartDashboard
command `Delete Stored Logs And Start Fresh (Disabled Only)` can reclaim the roboRIO log storage. It
briefly detaches file logging, closes the prior writer, recursively deletes every file/subdirectory in
`/home/lvuser/logs`, and then opens a fresh active WPILOG. This works even when there is no space to open
a replacement first; live NT4 remains active during the brief file-log gap. Wait for
`RealOutputs/Logging/PurgePending = false`, confirm
`PurgeCount` incremented, and require an empty `LastRotationError`. The new active log is intentionally
retained because AdvantageKit continues recording while robot code runs.

## 2026-08-24 selectable-yaw validation

The straight current-position 1 m/2 m autos now use `YawPrecision.RELAXED` at 1.8 degrees. Final
tag-board and coarse-to-precise handoff alignment continue to use `YawPrecision.PRECISE` at 1.5
degrees. This is a command-level selection, not a global loosening of heading accuracy.

For the first controlled check, manually build/deploy and run one `Forward 1m - PnP + Iso` test. Before
motion, open `C:\MechaRAMS\Temp\AdvantageScope 8-24-2026 - Selectable Yaw Precision.json`. During the
run require:

- `DriveToPose/Controller/YawPrecisionMode = RELAXED`;
- `DriveToPose/Controller/ConfiguredRotationToleranceDegrees = 1.8`;
- no timeout and no settling-hold exit;
- tape-measured left/right distance plus visible wheel-stop time as in the preceding settling tests.

Compare command time, first pose+velocity qualification, `ErrorThetaSignedDegrees`, module-speed tail,
and physical endpoint with `ef00a32a`/`20011a08`. Do not change gains or constraints in the same run.
Then run one `Precision To Tag Board` only to verify the mode returns to `PRECISE` and the numeric gate
returns to 1.5 degrees. A multipart auto may mark a nonterminal precision segment `RELAXED`, but its
final placement/aiming segment should remain `PRECISE` unless the game task explicitly permits a loose
terminal heading.

Result `85e18116`: relaxed yaw deployed correctly and physical endpoint was 1.01875 m average, but
active time was 2.327 s with three pose-tolerance entries. The final hold itself was clean; competing
active corrections remained. Before further tolerance/gain changes, run this exact camera-isolation
pair with no other change:

1. Cover the front-left camera; run `Forward 1m - PnP + Iso`; record a fresh log and physical left/right
   endpoints.
2. Return to the identical starting placement, uncover front-left, cover front-right, and repeat.
3. For each log compare `PoseToleranceEntryCount`, `AtGoalEntryCount`,
   `SettlingHoldExitCount`, requested/measured omega, mean/max measured-versus-target module speed,
   `WheelsStopped`, each active camera's innovation, and command duration.

Keep tag visibility, seeding procedure, battery state, speed constraints, gains, and 1.8 degree mode
identical. A single-camera run with substantially fewer pose entries/omega reversals identifies
camera-extrinsic/fusion disagreement; similar oscillation in both isolates low-level drivetrain
velocity/rotation tracking as the primary next characterization target.

Isolated result: front-right-only (`84e33292`, left camera physically covered) was substantially worse
than front-left-only (`0d086421`, right camera covered). Before another drive test, use the stationary
dual-camera view to validate a one-variable front-right yaw-transform correction. Current signed
same-position evidence is left-pose yaw minus right-pose yaw = -2.054 degrees, so the candidate
robot-to-front-right yaw is +15.74 degrees rather than +13.69 degrees. Leave measured XYZ and pitch
unchanged. After an approved code change:

1. Keep the robot disabled and squared to the board; collect at least 100 accepted samples from both
   cameras. Compare camera0-camera1 X, Y, and yaw. Require the yaw mean to move materially toward zero
   without increasing its spread.
2. If static yaw converges, seed once with both tags and run one dual-camera `Forward 1m - PnP + Iso`.
3. Compare against `85e18116`, `84e33292`, and `0d086421`; do not change yaw tolerance, drivetrain
   gains, constraints, camera XYZ, or pitch in the same test.
4. If the right camera still causes yaw motion, keep its XY observation but disable its theta fusion
   until the full right-camera extrinsic/solve calibration is repeated.

## 2026-08-31 camera-jitter capture and validity decisions

After manually deploying the corrected front-right yaw (+15.74 degrees), open
`C:\MechaRAMS\Temp\AdvantageScope 8-31-2026 - Camera Jitter Calibration.json` and perform this before
another trajectory:

1. Charge the battery, square the disabled robot to the surveyed tag board, and show both tags to both
   cameras. Do not seed or move after positioning.
2. Press `Start Camera Jitter Capture (Disabled Only)` once. Hold everything still until Camera0 and
   Camera1 `Jitter/Ready` are true (100 accepted MultiTag samples each).
3. Save the log and record, for each camera: `MeanPose`, `StdDevXMeters`, `StdDevYMeters`,
   `StdDevTranslationMeters`, `StdDevYawDegrees`, and all three `PeakToPeak*` fields. Also record the
   `Vision/JitterCapture/Camera0MinusCamera1*` fields. The signed X and Y fields show the
   direction of the systematic disagreement; the pose-distance field shows its magnitude.
4. Repeat from a second surveyed distance and a modest left/right viewing angle. One pose can hide an
   intrinsic or extrinsic error that changes across the image.

Interpretation and validity adjustment:

- A stable nonzero **mean-pose difference** is systematic bias: fix robot-to-camera extrinsics,
  intrinsic calibration, or tag layout. Do not disguise bias by merely increasing covariance.
- Similar means but larger **random XY scatter** on one camera supports increasing that camera's
  `CAMERA_STD_DEV_FACTORS` by a measured sigma ratio, so its XY contributes less.
- Similar XY but larger **yaw scatter** supports increasing its
  `CAMERA_ANGULAR_STD_DEV_FACTORS` independently.
- A camera whose corrected MultiTag heading remains biased or produces trajectory rotation can set
  `CAMERA_ROTATION_TRUST_ENABLED[index]=false`; theta becomes infinite-uncertainty while its accepted
  XY remains fused.
- Do not derive runtime jitter from a moving trajectory. Motion, latency, drivetrain error, and camera
  error are inseparable there; the button intentionally captures only while disabled.

All trust factors remain 1.0 and both headings remain enabled for the first corrected-yaw capture. Make
only one factor/trust change per subsequent A/B run and preserve the raw log that justified it.

### First valid capture result (`1788218509562_cce4a8da`)

Both cameras completed 100 samples while disabled. Front-left minus front-right mean was `+1.07 cm X`,
`-4.22 cm Y`, `4.35 cm translation`, and `+1.54 degrees yaw`. Front-left translation/yaw sigma was
`2.143 cm / 0.273 degrees`; front-right was `0.995 cm / 0.129 degrees`, a consistent approximately
2.15x ratio. Peak-to-peak Y was `12.13 cm` left versus `4.59 cm` right. Because the two yaw means were
approximately symmetric around zero (`+0.747` and `-0.796 degrees`), do not down-weight one camera or
change another transform from this one placement. Repeat at the identical pose, then at a second
surveyed pose, before separating systematic extrinsic correction from covariance weighting.

### Two-distance result and selected validity policy (`160c1546`, `cb76b12e`)

Both logs completed 100 MultiTag samples per camera while disabled. The tag distances were about
3.90/3.87 m in the farther capture and 2.94/2.88 m in the closer capture. Front-left versus
front-right translation/yaw sigma ratios repeated at `2.38/2.35` far and `1.93/1.87` close. This
supports front-left XY/angular factors of `2.15/2.10`.

The closer-minus-farther mean-pose deltas were `(1.0245, -0.1332, -0.052 degrees)` from front-left and
`(1.0120, -0.1007, -0.472 degrees)` from front-right. Both cameras reproduce the approximate 1 m X
move closely, but front-right yaw is substantially more position-dependent. Therefore disable only
front-right theta while retaining its lower-noise XY. Keep front-left theta enabled at its measured
angular factor. Do not change either camera transform in this same validation step.

Next validation after manual deployment:

1. Repeat one disabled 100-sample capture and confirm the logged factors are Camera0 XY/theta
   `2.15/2.10`, Camera1 `1.0/1.0`, with Camera1 rotation trust false.
2. Run one dual-camera 1 m trajectory from the standard test position.
3. Measure left/right bumper travel and save the log. Compare settle duration, yaw reversals, and
   post-tolerance module motion against `85e18116`, `84e33292`, and `0d086421`.

### First deployed validity run (`1c14fe31`, `657ac75a`)

The stationary log confirms all per-camera factors and Camera1 rotation eligibility. The 1 m command
completed successfully in 1.806 s with 1.98 cm fused translation error and 1.27 degrees yaw error.
Post-pose-tolerance measured omega had zero sign reversals above 2 deg/s. At command end, measured
translation/omega/max-module speed was only `0.0049 m/s / 0.98 deg/s / 0.0266 m/s`, so the later
`WheelsStopped` transition is mostly its stricter debounce rather than one-third second of large wheel
motion.

The command still missed the desired approximately 1.3 s profile time because fused yaw, not X,
delayed completion. Fused yaw changed `2.71 degrees` beyond integrated measured omega, with the largest
residual during final deceleration. For the next isolated A/B, set enabled camera rotation fusion false
while preserving disabled/manual front-left MultiTag seeding. Leave gains, constraints, tolerance,
settle hold, transforms, and per-camera XY factors unchanged.

After deployment, run one 1 m test with both cameras open. Confirm during enabled motion:

- `Vision/Modes/FuseRotationWhileEnabled = false`;
- both cameras continue logging accepted frames;
- both `Vision/Camera*/LastTrustedRotation` outputs are false;
- manual seed still succeeds while disabled before the run.

Measure both bumper endpoints and save the log. Primary pass criteria are first pose-tolerance entry
near the translation profile end, no large fused-yaw residual versus integrated omega, command duration
closer to 1.3 s, and no meaningful wheel motion after command end.

### Enabled XY-only follow-up (`a0331c79`) and next controlled run

The deployed mode was correct (`FuseRotationWhileEnabled=false`). Physical endpoint measurements were
left `1.010 m` and right `0.985 m`: center travel `0.9975 m`, only `0.25 cm` short. The frame ruler
points are `0.6072 m` apart (`0.4572 m` wheel track plus `0.075 m` per side), so their `2.5 cm`
difference corresponds to approximately `-2.36 degrees` clockwise yaw. The log began at a camera-seeded
`+1.55 degrees` and targeted zero, so it commanded most of that unwanted rotation.

The command took `2.242 s`. It did not show fused longitudinal overshoot; maximum along-track progress
was the final `0.9846 m`. The dominant timing fault was scheduler starvation: controller sample gaps
reached `0.218 s` and `0.200 s`, with user-code times of `171 ms` and `181 ms`. Those cycles coincided
with camera bursts of 18 and 16 observations across both cameras. Do not tune PID from this run.

After manual deployment, perform exactly one `Forward 1m - PnP + Iso` run with both cameras open:

1. Square both frame edges to the board, seed vision once while disabled, and rotate/start a fresh log.
2. Verify `DriveToPose/Calibration/HeadingNormalized=true`; the controller's measured start yaw should
   be near zero without an initial rotation maneuver.
3. Graph `/Vision/Camera0/SupersededPoseObservationCount` and Camera1's matching field beside both
   `UnreadResultCount` fields. Occasional nonzero superseded counts are allowed; no pose is carried to
   another loop.
4. Graph `/RealOutputs/Vision/Timing/PeriodicMs`, both `Camera*FusionMs`, and
   `/RealOutputs/LoggedRobot/UserCodeMS` plus `FullCycleMS`. Require no cycle above `40 ms`; record the
   largest value even if the run otherwise looks good.
5. Retain the existing DriveToPose pose/velocity/tolerance, module-speed, and wheel-stop fields. Measure
   both frame-edge endpoint distances again.

Pass target for this isolated run: center distance within `+/- 2 cm`, left-right difference no more than
`1 cm` (about `0.94 degrees` across the frame), command time materially below `2.0 s`, and no 100+ ms
scheduler gap. Keep controller gains, tolerances, camera transforms,
and covariance factors unchanged until this timing/heading experiment is evaluated.

### FIFO regression (`716d7881`) and required replacement validation

The first pacing implementation is rejected. It normalized start yaw successfully and reduced the
<=6 cm tail from `0.530 s` to `0.279 s`, but it carried old poses across loops. At command end Camera0
still had 119 pending poses and Camera1 had 52; almost no current vision reached the estimator. Physical
center travel was `1.1425 m`, while fused progress claimed only `0.9660 m`. This run is invalid for
controller tuning and demonstrates why a persistent pose FIFO must not be restored.

The replacement drains the complete unread-result queue and fuses only the newest solvable pose per
camera per loop. After manual deployment, repeat exactly one squared, vision-seeded
`Forward 1m - PnP + Iso` with both cameras open. Require `HeadingNormalized=true`, physical center travel within 2 cm,
corner difference within 1 cm, both cameras accepting current frames, no 100+ ms control gap, and
`SupersededPoseObservationCount` visible for both cameras. Stop testing and revert to commit `c84b53e`
if physical travel exceeds 1.05 m again.

## 2026-09-02 `4844` 2 m result and gyro-rate A/B

The unchanged 2 m confirmation traveled `2.050/2.040 m` at the frame corners (`2.045 m` center) and
finished about `0.94 degrees` clockwise. Translation entered 4 cm at +`1.672 s`, but heading reached
`-4.97 degrees`; the 1.8-degree combined gate did not pass until +`2.555 s`, and total command time was
`2.808 s`. The configured hold was only `0.061 s`. This assigns the observed terminal movement to yaw
recovery, not translation settling.

The next run validates only the angular-rate source change:

1. Manually build and deploy; keep both cameras open and all existing gains, constraints, tolerances,
   covariance, and vision modes unchanged.
2. Load `C:\MechaRAMS\Temp\AdvantageScope 9-2-2026 - Gyro Rate Validation.json`.
3. While disabled, require `Drive/GyroYawRateSignalOK=true` and an applied update frequency of at least
   `100 Hz`. A `250 Hz` value is expected/valid when the CTRE odometry signal sharing raises the
   applied frame rate.
4. Seed as usual, start a fresh log, and run `Forward 2m - PnP + Iso` once from the same squared setup.
5. Record maximum/final left and right frame-corner distances, lateral displacement if measurable,
   visible settling time, and log suffix. Stop for visible divergence.
6. Compare peak `ErrorThetaSignedDegrees`, time of first 4 cm pose entry, AtGoal time, command duration,
   Pigeon rate, kinematic rate, requested omega, and their integrated heading changes against `4844`.

Pass direction is a materially smaller approximately 5-degree deceleration yaw excursion and a command
time moving toward the theoretical approximately 1.9 s translation profile. Do not widen RELAXED yaw
or tune theta gains in the same run. If the Pigeon and pose agree but the chassis still yaws, inspect
per-module speed/steer tracking next. The physical-versus-fused distance difference (`2.045 m` physical
versus `2.0079 m` fused progress) is a separate possible scale issue and requires a repeat before any
wheel-radius change.

### `0caf` validation result

The corrected deployment passed. `GyroYawRateSignalOK` stayed true at a shared-frame applied rate of
250 Hz. The Pigeon rate integrated `-0.90 degrees` versus `-1.06 degrees` from gyro-owned pose;
kinematic omega still predicted the wrong net direction at `+7.32 degrees`. Peak yaw was only
`-2.27 degrees`, compared with `-4.97 degrees` in `4844`.

Physical travel was `2.015/1.995 m` (`2.005 m` center). Command time was `1.941 s`; it entered combined
pose tolerance at +`1.837 s`, qualified pose+velocity once at +`1.877 s`, never escaped the hold, and
ended after `0.064 s` of hold. The post-pose-entry tail was `5.36%`, meeting the <10% requirement.

Do not tune direct-drive gains, damping, profile constraints, vision weighting, or yaw tolerance from
this passing result. Run one unchanged 1 m regression with the same deployment and both cameras open.
If command time remains near 1.27 s with no visible terminal correction, close this direct-distance
tuning stage and proceed to a real PathPlanner spatial-handoff test. Keep the timing fields visible;
`0caf` still had one `89.2 ms` controller gap even though final profile lag was only `1.26 ms`.

### `0b06` regression result and velocity-escape validation

`0b06` completed the 1 m command in `1.214 s`, but it is not a valid pass. Physical left/right travel
was `1.000/1.045 m`, implying approximately `+4.24 degrees` counterclockwise yaw. The command first
qualified at +`1.152 s` with Pigeon rate `7.73 deg/s`; during the latched hold the rate then rose to
`14.35`, `21.39`, and ended at `19.11 deg/s`. Max module speed at command end was `0.409 m/s`, fused
heading was already `+2.24 degrees`, and heading continued past `+3.1 degrees` afterward. The old
pose-only hold escape therefore hid renewed motion.

After manually building/deploying the velocity-escape change, run exactly one `Forward 1m - PnP +
Iso` with both cameras open and a fresh log. Do not alter gains, profile constraints, yaw mode, or
vision settings. Capture:

- `DriveToPose/SettlingHoldActive`, `AtGoalEntryCount`, and `SettlingHoldExitCount`;
- `DriveToPose/OutsideSettlingEscapePoseTolerance` and
  `OutsideSettlingEscapeVelocityTolerance`;
- measured translation/rotation speed and configured entry/escape speed thresholds;
- error X/Y/theta, Pigeon yaw rate, requested omega, max module measured/target speed, and
  `Drive/WheelsStopped`;
- command active/finished, settle seconds, loop/profile timing, and final left/right ruler distances.

Pass only if the command does not finish during renewed motion, visible terminal correction remains
small, the center distance remains close to 1 m, and the corner difference returns near the accepted
approximately 2 degree range. One or more hold exits are acceptable in this diagnostic run. If hold
exits make the command slow, tune rotational response from the log rather than removing the safety
escape.

### `1bd6` velocity-escape validation result

The repeat passed with a documented yaw transient. Physical left/right travel was `1.005/1.027 m`,
or `1.016 m` center and approximately `+2.08 degrees` counterclockwise. Command duration was
`1.4637 s`. The first complete pose entry occurred at +`1.0802 s`, but yaw then briefly left tolerance;
the final stable pose entry was at +`1.3552 s`, followed by `0.1085 s` to command end (`7.41%`). The
hold qualified once at +`1.3954 s`, remained latched for `0.0683 s`, and never escaped. Final fused
error was `0.01784 m / 0.851 degrees`; translation/rotation speeds were `0.0605 m/s / 2.17 deg/s`.

This confirms that the command no longer finishes during the high-rate condition seen in `0b06`.
Retain the new velocity escape and freeze direct-drive constants. The approximately `0.38 s` from the
first brief pose entry to command end records a remaining yaw excursion, so do not describe the run
as zero-settling. The next test should use the actual PathPlanner spatial-handoff auto with the same
vision and precision-controller settings. Measure coarse-to-precision handoff time, total command
time, final left/right distances, and lateral offset, and retain the same settling/yaw/module fields.
