# Calibration and Test Process

Author: Claude (Opus 4.8) session, 2026-06-30
Purpose: A clear, repeatable process to calibrate the cameras and validate localization, precision
driving, and aiming — **simulation first**, then on the real robot. Follow top to bottom.

A guiding principle from the research (6328 "Beyond the Coprocessor"): **measure, don't guess.** Every
gate and gain in the code is a starting point to be tuned from AdvantageKit logs, not a final value.

---

## Stage 0 — Toolchain

1. Java 17+ (`JAVA_HOME=C:\Users\Public\wpilib\2026\jdk`).
2. From `VisionTestingAndCalibration`:
   - `./gradlew.bat compileJava` — must succeed.
   - `./gradlew.bat test` — headless math checks (vision policy + aiming geometry) must pass.
3. AdvantageScope and PhotonVision (web UI) installed on the laptop. See `ADVANTAGESCOPE_SETUP.md`.

---

## Stage 1 — Simulation validation (no hardware needed)

The PhotonVision simulator now produces real synthetic frames (`VisionIOPhotonVisionSim`), so the entire
localization + precision + aiming pipeline is testable on a desktop.

### 1.1 Run sim and confirm vision frames

1. `./gradlew.bat simulateJava` (or VS Code `WPILib: Simulate Robot Code`).
2. In AdvantageScope, connect to the sim (NT4 / localhost).
3. Enable Teleop. Press `A` to reset pose to `(1.5, 2.0, 0°)`.
4. Drive toward the tags (toward +X). Watch these channels become non-zero/non-empty:
   - `Vision/Camera0/AcceptedFrames`, `Vision/Camera1/AcceptedFrames` > 0
   - `Vision/Summary/AcceptedPoses` (should cluster near the true pose)
   - `Vision/Summary/RejectedPoses` (sanity: off-field/too-far solves land here)
   - `Drive/Pose` tracks the commanded motion

   If accepted frames stay 0, the cameras are not seeing tags — rotate to face the board, or check the
   camera transforms.

### 1.2 Confirm the fusion discipline (the 2026 fixes)

- Block one tag (drive so only one is visible). Confirm `Vision/CameraX/LastTrustedRotation = false`
  and that heading does not get yanked (single-tag theta = ∞).
- Drive far from the board and confirm frames flip to `RejectedFrames` with
  `LastRejectionReason = TOO_FAR`.

### 1.3 Precision + aiming in sim

- Press `X` → `DriveToPosePrecisionCommand` to `(4.25, 2.0, 0°)`. In AdvantageScope watch
  `DriveToPose/TranslationErrorMeters` settle below `0.03` and `DriveToPose/AtGoal = true`, then
  `DriveToPose/Finished`. Confirm it does **not** time out (`DriveToPose/TimedOut = false`).
- Press right-stick (`Aim At Goal - Stationary`) → watch `Aim/HeadingErrorDegrees` settle below `1.5`.
- Hold right trigger and drive → `DriveAndAimCommand`: `Aim/TargetHeadingDegrees` should track the goal
  and `Aim/LeadPose` should shift ahead of the robot when moving (shoot-on-move lead).

Acceptance for Stage 1: all of the above behave as described and a log is saved under `logs/sim`.

---

## Stage 2 — Camera intrinsic calibration (PhotonVision, per camera)

Intrinsics (focal length, principal point, distortion) are unique to each camera+lens and **must** be
calibrated at the exact resolution used in matches. Idea: every top team (3467 keeps per-camera matrices;
6328 refines extrinsics against ground truth) treats calibration as first-class.

1. In PhotonVision, select the camera and open the **Cameras / Calibration** tab.
2. Choose the resolution you will run (start 1280×800).
3. Print a calibration target (ChArUco recommended) at exact scale on a rigid flat board.
4. Capture the recommended number of snapshots covering the whole field of view and a range of angles
   and distances. Keep the board flat and well lit (the OV9782 is global-shutter but color — give it
   light).
5. Run the calibration. Record the reported **mean reprojection error** (aim for well under 1 px).
6. Repeat for every camera. Save/export each calibration.

Record per camera: serial/name, resolution, reprojection error, date. Re-calibrate if the lens is
touched or refocused.

---

## Stage 3 — Extrinsic (robot-to-camera) measurement

The code ships **provisional** transforms (`VisionConstants.ROBOT_TO_*_CAMERA`). After mounting, measure
the real ones — a 1–2° extrinsic error is exactly the kind of bias that ruined 2026 aiming.

1. Define the robot origin (center of the drivetrain, floor level, +X forward, +Y left, +Z up).
2. For each camera measure the lens position `(x, y, z)` from origin (meters).
3. Measure the camera orientation (roll, pitch, yaw). Pitch is usually slightly down (negative); yaw is
   the cross-eye/outward angle.
4. Update `VisionConstants.ROBOT_TO_<camera>_CAMERA` and set the **same** transform in PhotonVision.
5. Verify: place the robot at a surveyed pose, look at `Vision/CameraX/LastAcceptedPose`. A good
   extrinsic gives an accepted pose within a few cm of truth. If it is biased in a consistent direction,
   refine the transform (6328 uses small per-camera pitch "fudge" tunables for exactly this).

---

## Stage 4 — Localization accuracy test (real robot, ground truth)

Goal: quantify pose error vs. surveyed positions, like the LL4-vs-PhotonVision protocol in the strategy
doc.

1. Mark 4–6 surveyed floor positions with known `(x, y, θ)` relative to the tag board.
2. For each: place the robot exactly, read `Drive/Pose` from AdvantageScope, record error.
3. Repeat with the robot rotated so different cameras see the tags.
4. Repeat moving slowly through positions (dynamic error matters more than static).
5. Tune from the log:
   - If accepted poses are noisy → raise `LINEAR_STD_DEV_BASELINE` / per-camera factor.
   - If a camera is consistently worse → raise its `CAMERA_STD_DEV_FACTORS` entry.
   - If good frames are rejected → loosen the relevant gate (`MAX_AVERAGE_TAG_DISTANCE_METERS`, etc.).
   - Watch `Vision/CameraX/LastInnovationMeters`: large persistent innovations mean a calibration or
     extrinsic problem, not a fusion-weight problem.

Acceptance: static error within a few cm / couple degrees; dynamic error stable; rejection reasons make
sense.

---

## Stage 5 — Drivetrain characterization (SysId)

Provisional wheel radius / feedforward must be replaced with measured values or precision driving will
drift. Controls are in `ROBOT_CONTROLS.md`.

1. D-pad selects the routine (up = translation, right = steer, down = rotation).
2. Run quasistatic forward/reverse, then dynamic forward/reverse (robot safely supported as appropriate).
3. Analyze the hoot/SysId logs; update `SwerveConstants` gains and `WHEEL_RADIUS`.
4. Re-run the precision test (Stage 6) and compare.

---

## Stage 6 — Precision driving test (real robot)

1. Place the robot at the marked start, press `A`.
2. Press `X` (or dashboard "Precision Drive To Tag Board").
3. Measure the real final position with a tape; compare to `DriveToPose/MeasuredPose`.
4. Save the log. Repeat: vision enabled vs. tags blocked; coarse-path-then-precision (auto "VisionTest +
   Precision Handoff").
5. Tune: tighten `PRECISION_*_TOLERANCE`, adjust `PRECISION_DRIVE_KP` / accel if it overshoots or is slow.

Acceptance: repeatable final position within tolerance; command settles (does not hit the safety timeout).

---

## Stage 7 — Aiming test (real robot, no mechanism)

1. Set `AimConstants.GOAL_POSITION` to the chosen field target.
2. Stationary: right-stick `Aim At Goal`. Confirm the robot squares up; log `Aim/HeadingErrorDegrees`.
3. Moving: hold right trigger and strafe/translate; confirm the robot keeps facing the goal and
   `Aim/LeadPose` leads correctly.
4. When a boresight/turret camera + target exist later, log `poseBearing − cameraBearing` as the
   independent aim check the strategy doc recommends.

---

## What "done" looks like for the pilot

- Sim exercises the full pipeline (Stage 1) and a log is saved.
- Each camera calibrated (Stage 2) and extrinsics measured (Stage 3).
- Static localization error within a few cm at surveyed positions (Stage 4).
- Drivetrain characterized (Stage 5).
- Precision driving repeatable within tolerance (Stage 6).
- Chassis aiming holds the goal within tolerance stationary and moving (Stage 7).
