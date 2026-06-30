# FRC Vision Localization Skill

Use this skill when modifying Team 999 AprilTag localization, PhotonVision, camera transforms, pose
fusion, AdvantageKit replay, or the vision simulation.

## Required Reads

- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\AGENTS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\SESSION_STATE.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\CODEX_CODE_REVIEW_AND_GAP_ANALYSIS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\CALIBRATION_AND_TEST_PROCESS.md`

## Architecture (AdvantageKit IO-layer — keep this shape)

```
subsystems/vision/
  VisionIO.java                 @AutoLog inputs + PoseObservation/TargetObservation records
  VisionIOPhotonVision.java     real camera: getAllUnreadResults -> multi-tag + single-tag pose solves
  VisionIOPhotonVisionSim.java  VisionSystemSim + PhotonCameraSim (fed true sim pose each loop)
  Vision.java                   validation + covariance + timestamped fusion via VisionConsumer
```
Pattern source: official AdvantageKit PhotonVision template (6328-authored, shipped by 1768). Select the
sim IO in `RobotBase.isSimulation()`, real IO otherwise (see `RobotContainer.createVision`).

## Design Rules (non-negotiable — these are our 2025/2026 bug fixes)

- Consume ALL unread frames per camera (`getAllUnreadResults`). Fuse each observation **at its own
  PhotonVision timestamp** — CTRE's odometry pose-history buffer aligns it in time, so no explicit
  cross-frame sort is needed (insertion order within a loop does not change the result). Only refactor to
  an explicit merge/sort if you replace CTRE's estimator with a sequential custom one (6328-style).
- **Convert timestamps to the CTRE time base** with `Utils.fpgaToCurrentTime(...)` before
  `addVisionMeasurement` (PhotonVision uses FPGA time; CTRE's estimator uses Phoenix time). Idea: CTRE
  swerve+vision integration requirement; getting this wrong silently breaks latency compensation.
- **Single-tag heading is never trusted** → angular std dev = `Double.POSITIVE_INFINITY`. Idea: 6328.
- Reject NaN/Inf, impossible Z, off-field, too-far, ambiguous single-tag — log a `RejectionReason` enum.
- Covariance = `baseline * dist² / tagCount² * cameraFactor` (per-camera factors; tag count squared). Idea: 6328/6995.
- Ignore vision for the first `AUTO_VISION_IGNORE_SECONDS` of auto. Idea: 6328.
- Log accepted/rejected poses, tag poses, innovation distance, per-camera frame counts.
- Keep final fusion on the roboRIO (CTRE estimator). Do not move fusion to the Orange Pi.

## Hardware Notes

- Orange Pi only (no Mac mini). Cameras run ~30–50 fps; the 250 Hz figure is the roboRIO/CANivore
  odometry thread, not a camera/Pi rate.
- Start with 2 cameras / 1 Orange Pi; code scales to 4 cameras / 2 Pis (uncomment in `RobotContainer`,
  transforms already in `VisionConstants`).

## Verification

- `./gradlew.bat compileJava` and `./gradlew.bat test` (Java 17). The vision policy is unit-tested in
  `VisionPolicyTest` — keep those tests passing (single-tag ∞, dist²/tagCount scaling, all reject gates).
- Validate in simulation first (`CALIBRATION_AND_TEST_PROCESS.md` Stage 1) before touching transforms.
