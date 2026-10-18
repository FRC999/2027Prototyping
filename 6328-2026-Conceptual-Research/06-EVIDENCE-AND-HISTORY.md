# Evidence, versions, and limits of the findings

Research date: **October 17, 2026, America/New_York**.

## 1. Source baselines

| Source | Exact revision | Reason for choosing it |
|---|---|---|
| Mechanical Advantage `RobotCode2026Public` | `b2923d6557d4c9bef62d26932cc8a66fea2e07a6` | Public HEAD retrieved for this review; published October 17, 2026 at 02:49 EDT |
| Earlier MA baseline | `bf32451ba4337d02d02078c06d689d863a2d9d83` | The source used by the team's prior vision strategy; published June 26, 2026 at 21:08 EDT |
| PhotonVision | tag `v2026.3.4`, commit `a5be3d062ce5c145f4a759b0207057f218cda6b3` | A concrete 2026-season release; commit dated April 10 UTC, April 9 EDT |
| MechaRAMS repository before this documentation | `07d72c0b628e8d51c843ebea0c1f7785331aedae` | Latest `main` after the required pull |

All MA and PhotonVision file references in these documents use commit-pinned URLs. Current web documentation is used only where identified. PhotonVision's live documentation can point at 2027 development versions, which is why the comparison uses a cloned 2026 release instead.

[source-manifest.json](source-manifest.json) records the reviewed file paths, repository commits, SHA-256 hashes of their Git blobs, and source links. It does not copy the upstream implementation into our robot project.

## 2. What changed during the public history?

| Public snapshot | Observable evidence | What it does not prove |
|---|---|---|
| January 9: `5ddc8c9` | Initial 2026 detector wrapper calls ordinary OpenCV ArUco | That Northstar itself was new in 2026 |
| April 10: `da3c1db` | Git history introduces the `powerBudget` flywheel calculation | First private implementation date or event usage |
| April 24: `5e19b15` | BatteryEstimator and BreakerModel appear publicly | That the final integrated manager was already running at a particular competition |
| April 28: `afd7248` | Those battery/breaker files are deleted from the public snapshot | Why they were removed |
| May 20: `153bc63` | BatteryEstimator and BreakerModel return; FinanceDepartment appears | Its first private deployment date |
| May 29: `bb88510` | Aruco Max files appear and the worker switches to that detector | That the same detector was used at earlier spring events |
| June 26 EDT: `bf32451` | Baseline used in previous MechaRAMS research | A permanent final version of the robot |
| October 17: `b2923d6` | Total budget cap changes from 200 A to 190 A | The measured benefit of that tuning change |

The public repository is a published view of 6328's development work. Its team-authored introduction explicitly says some stable releases may lag development/review. Publication and deployment are therefore different claims. [Public-release explanation][H-PUBLIC], [Aruco Max introduction][H-ARUCOCOMMIT], [manager introduction][H-FINCOMMIT].

Northstar was already described publicly in January 2023. That post identifies education and hardware-specific control/optimization as reasons to build it. It does not substantiate a blanket conclusion that PhotonVision was unqualified in 2026. [Historical explanation][H-NORTHSTAR].

## 3. Comparison with the earlier MechaRAMS study

The June snapshot already contained Aruco Max and the power-manager files. This study adds detail that the earlier high-level vision discussion did not cover; these features were not all newly published in October.

The following scoped diff was inspected:

```powershell
git diff bf32451ba4337d02d02078c06d689d863a2d9d83 b2923d6557d4c9bef62d26932cc8a66fea2e07a6 -- northstar src/main/java/org/littletonrobotics/frc2026/energy
```

Within those two paths, Northstar has no changes and the only energy change is `maxBudgetAmps` from 200.0 to 190.0. This statement is deliberately limited to those paths; it is not a claim that the entire robot repository was unchanged.

The previous [vision strategy](../MechaRAMS%20vision%20system/Vision-Strategy-2027-v2.md) remains useful architectural background. Its broad “all accepted cameras” description needs the camera-1 fallback and first-two-autonomous-seconds exceptions explained in the new [vision walkthrough](01-VISION-WALKTHROUGH.md). The existing [research handoff](../MechaRAMS%20vision%20system/RESEARCH_HANDOFF.md) remains the entry point for prototype commissioning.

## 4. Confidence and claim boundaries

| Statement | Status |
|---|---|
| The checked Northstar worker uses Aruco Max | Verified in source |
| Aruco Max derives from ArUco Nano and uses Metal thresholding | Verified in wrapper, license, detector, and GPU code |
| PhotonVision v2026.3.4 has ArUco, multi-tag, and constrained estimation capabilities | Verified in pinned source |
| Aruco Max is faster or more accurate on MechaRAMS hardware | Not measured; no such claim is made |
| A specific PhotonVision defect motivated the meeting statement | Not established |
| The power manager combines battery and breaker models and limits drive | Verified through model, manager, Java output, and C++ configuration path |
| It estimates wire/circuit temperatures in Celsius | Not supported by these classes |
| It dynamically increases the shooter's hardware current limit | Not supported by the reviewed manager; shooter current influences the remaining drive allowance |
| It protects the adaptive drive budget in autonomous | False for the checked `getDriveLimit()`; autonomous overrides it |
| It guarantees no brownouts or breaker trips | Not established, and the implementation has documented limitations |
| It improved a specific number of points or prevented a measured number of failures | Not established |
| The mathematical building blocks were invented by 6328 | Not claimed; the contribution studied is customization and integration |

No robot code was deployed or modified for this research. No physical camera benchmark, battery discharge characterization, breaker test, or robot power test was performed. Documentation review and numerical examples should not be reported as those tests.

## 5. Primary-source index

### Vision

| Reference | What it establishes |
|---|---|
| [Northstar worker][M-WORKER] | Selected detector and pipeline wiring |
| [FiducialDetector.py][M-FID] | Ordinary ArUco versus native Aruco Max wrappers |
| [aruco_max.h][M-DETECT] | Detection stages, retry policy, defaults, corner refinement |
| [Metal implementation][M-METAL] | GPU kernels and buffer management |
| [Native wrapper][M-WRAP] and [ArUco Nano license][M-NANOLICENSE] | Implementation provenance |
| [Native build configuration][M-SETUP] | Apple framework and Homebrew dependencies |
| [CameraPoseEstimator.py][M-PNP] | Single/multi-tag OpenCV solving |
| [Northstar main][M-NSMAIN] and [publisher][M-PUBLISH] | Timestamp placement, queues, published results |
| [VisionIONorthstar][M-IO] | Settings/layout publication and observation queue consumption |
| [Vision][M-VISION] and [VisionConstants][M-VCONST] | Gates, policies, weighting, camera transforms |
| [RobotState][M-STATE] | Odometry history and vision correction |

### Power and actuator integration

| Reference | What it establishes |
|---|---|
| [BatteryEstimator][M-BAT] | One-RC model, Peukert counting, polarization correction |
| [BreakerModel][M-BREAK] | Stress accumulation, cooling, inverse trip-time budget, edge branches |
| [FinanceDepartment][M-FIN] | Budget composition, recovery, drive limits, autonomous override |
| [BatteryLogger][M-LOGGER] | Supply-current/power/energy accounting and auxiliary estimates |
| [CurrentLimits][M-LIMITS] | Published mechanism and drive limit values |
| [Module][M-MODULE] and [ModuleIO][M-MODULEIO] | Drive/steering classification and requested output |
| [ModuleIOTalonFX][M-MODULECPP] | Native drive supply-current configuration |
| [TalonFXCurrentConfigurator][M-CONFIGURATOR] | Background application, coalescing, retry behavior |
| [Flywheel][M-FLY] and [FlywheelIOTalonFX][M-FLYCPP] | Power-aware ramp versus hardware current limits |
| [Robot][M-ROBOT], [FullSubsystem][M-FULL], [Constants][M-CONSTANTS] | Scheduling, resets, and nominal 5 ms loop |

### PhotonVision and historical context

| Reference | What it establishes |
|---|---|
| [AprilTagDetectionPipe][P-DETECT] and [AprilTagPipeline][P-APRILPIPE] | WPILib AprilTag detection and pipeline integration |
| [PhotonArucoDetector][P-ARUCODETECT] and [ArucoPipeline][P-ARUCO] | OpenCV ArUco pipeline, including AprilTag dictionaries |
| [VisionEstimation][P-GEOMETRY] | Shared PnP geometry |
| [PhotonPoseEstimator][P-POSE] | Strategies and robot-camera transform handling |
| [PhotonCamera][P-CAMERA] | Bounded unread-result queue API |
| [Pinned pose documentation][P-POSEDOC] | Strategy explanation and credit to 6328 for trig approach |
| [6328's 2023 Northstar post][H-NORTHSTAR] | Historical origin and stated motivation |
| [6328's public-release explanation][H-PUBLIC] | Public/private timing limitations |
| [CTRE current-limit guide][H-CTRE] | Supply/stator distinction and empirical tuning guidance; live documentation |

## 6. Reproducing and extending this review

Temporary research clones were placed under the requested `S:\MechaRAMS\Temp` area. They are not required to read the documents on another computer. To reproduce the source inspection elsewhere, clone the upstream repositories, check out the exact revisions above, and inspect the linked paths.

Useful history commands from the Mechanical Advantage clone:

```powershell
git log --diff-filter=A --name-only -- northstar/aruco_max_cpp
git log --name-status -- src/main/java/org/littletonrobotics/frc2026/energy
git log -S powerBudget -- src/main/java/org/littletonrobotics/frc2026/subsystems/launcher/flywheel/Flywheel.java
git show 5ddc8c947216495eef914f82b2353980442210b6:northstar/pipeline/FiducialDetector.py
```

Continue by resolving the [questions for 6328](05-MECHARAMS-ROADMAP.md#7-questions-to-take-back-to-6328) or running the proposed experiments. Preserve the distinction between source facts, engineering interpretation, proposed MechaRAMS changes, and measured results. If later code changes the conclusions, record the new commit rather than silently replacing the historical baseline.

## 7. Documentation checks completed

- All 37 referenced upstream source files were resolved against the pinned Git commits and their Git-blob hashes recorded.
- Local document links and source-reference definitions were checked.
- All eight Mermaid diagrams parsed and rendered successfully with Mermaid 11 in a local headless browser. The main architecture, fusion, budgeting, and mode-policy diagrams were visually inspected; wide layouts were adjusted for readability. This is local rendering verification, not a claim that GitHub's private renderer was executed locally.
- The worked battery allowance, breaker accumulation/cooling, drive-budget table, vision weighting, and brownout ramp arithmetic were recalculated independently.
- Git whitespace checks passed. Robot compilation and hardware testing were not applicable to this documentation-only change and were not performed.

<!-- Source references: generated from pinned research baselines. -->

[H-ARUCOCOMMIT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/commit/bb88510d233e96c9ec3179fff896c6b21ca2503d
[H-CTRE]: https://v6.docs.ctr-electronics.com/en/stable/docs/hardware-reference/talonfx/improving-performance-with-current-limits.html
[H-FINCOMMIT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/commit/153bc63509c7a9acbfe5037e339d6840979bd045
[H-NORTHSTAR]: https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2023-build-thread/420691/36
[H-PUBLIC]: https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2026-build-thread/509595/8
[M-BAT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/BatteryEstimator.java
[M-BREAK]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/BreakerModel.java
[M-CONFIGURATOR]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/cpp/util/TalonFXCurrentConfigurator.cpp
[M-CONSTANTS]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/Constants.java
[M-DETECT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/aruco_max.h
[M-FID]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/pipeline/FiducialDetector.py
[M-FIN]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/FinanceDepartment.java
[M-FLY]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/launcher/flywheel/Flywheel.java
[M-FLYCPP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/cpp/subsystems/FlywheelIOTalonFX.cpp
[M-FULL]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/util/FullSubsystem.java
[M-IO]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/vision/VisionIONorthstar.java
[M-LIMITS]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/CurrentLimits.java
[M-LOGGER]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/BatteryLogger.java
[M-METAL]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/metal_context.cpp
[M-MODULE]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/drive/Module.java
[M-MODULECPP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/cpp/subsystems/ModuleIOTalonFX.cpp
[M-MODULEIO]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/drive/ModuleIO.java
[M-NANOLICENSE]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/LICENSE-ArucoNano.txt
[M-NSMAIN]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/__init__.py
[M-PNP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/pipeline/CameraPoseEstimator.py
[M-PUBLISH]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/output/OutputPublisher.py
[M-ROBOT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/Robot.java
[M-SETUP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/setup.py
[M-STATE]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/RobotState.java
[M-VCONST]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/vision/VisionConstants.java
[M-VISION]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/vision/Vision.java
[M-WORKER]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/apriltag_worker.py
[M-WRAP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/wrapper.cpp
[P-APRILPIPE]: https://github.com/PhotonVision/photonvision/blob/a5be3d062ce5c145f4a759b0207057f218cda6b3/photon-core/src/main/java/org/photonvision/vision/pipeline/AprilTagPipeline.java
[P-ARUCO]: https://github.com/PhotonVision/photonvision/blob/a5be3d062ce5c145f4a759b0207057f218cda6b3/photon-core/src/main/java/org/photonvision/vision/pipeline/ArucoPipeline.java
[P-ARUCODETECT]: https://github.com/PhotonVision/photonvision/blob/a5be3d062ce5c145f4a759b0207057f218cda6b3/photon-core/src/main/java/org/photonvision/vision/aruco/PhotonArucoDetector.java
[P-CAMERA]: https://github.com/PhotonVision/photonvision/blob/a5be3d062ce5c145f4a759b0207057f218cda6b3/photon-lib/src/main/java/org/photonvision/PhotonCamera.java
[P-DETECT]: https://github.com/PhotonVision/photonvision/blob/a5be3d062ce5c145f4a759b0207057f218cda6b3/photon-core/src/main/java/org/photonvision/vision/pipe/impl/AprilTagDetectionPipe.java
[P-GEOMETRY]: https://github.com/PhotonVision/photonvision/blob/a5be3d062ce5c145f4a759b0207057f218cda6b3/photon-targeting/src/main/java/org/photonvision/estimation/VisionEstimation.java
[P-POSE]: https://github.com/PhotonVision/photonvision/blob/a5be3d062ce5c145f4a759b0207057f218cda6b3/photon-lib/src/main/java/org/photonvision/PhotonPoseEstimator.java
[P-POSEDOC]: https://github.com/PhotonVision/photonvision/blob/a5be3d062ce5c145f4a759b0207057f218cda6b3/docs/source/docs/programming/photonlib/robot-pose-estimator.md
