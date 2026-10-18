# What Mechanical Advantage changed in vision and power management

Research date: **October 17, 2026**. Audience: MechaRAMS mentors and students. This is a source-based conceptual study, not a robot software deployment or a hardware benchmark.

## The answers

**Vision: yes, there is a real custom detector.** The reviewed Northstar worker uses **Aruco Max**, a C++ detector based on ArUco Nano, with Apple Metal accelerating adaptive thresholding. It reads the normal AprilTag 36h11 family. Northstar then uses OpenCV pose solvers and 6328's robot-side filtering and fusion. It is a custom implementation and integration, not a new tag format or a wholly new pose-estimation theory. [Detector selection][M-WORKER], [detector implementation][M-DETECT], [provenance][M-WRAP].

**That does not establish that PhotonVision is inadequate.** PhotonVision v2026.3.4 already has AprilTag and OpenCV ArUco pipelines, multi-tag solving, queued results, and constrained pose estimation. The evidence establishes different engineering choices; it does not establish a universal performance advantage. The team's historical explanation cites training and control over hardware-specific optimization. The particular PhotonVision limitation discussed in your meeting remains unverified. [PhotonVision pipelines][P-ARUCO], [pose strategies][P-POSE], [6328's explanation][H-NORTHSTAR].

**Power: yes, they built a model-based manager.** `FinanceDepartment` combines a battery voltage-sag model and a main-breaker stress model, calculates a total current budget, subtracts other loads, and limits the four drive motors. When the shooter draws more, the drive allowance shrinks. That can preserve voltage for scoring, but there is no general “give the shooter another 20 amps” allocator in this implementation. [Manager][M-FIN], [battery model][M-BAT], [breaker model][M-BREAK].

**The temperature description needs qualification.** The breaker model tracks a dimensionless accumulated stress value with cooling. It does not calculate degrees Celsius throughout the wiring, individual branch breakers, or battery. Separately, motor temperatures are logged. [Breaker model][M-BREAK], [motor IO][M-MODULECPP].

## Read in this order

| Reader / purpose | Document |
|---|---|
| Students and first introduction | [Student guide](04-STUDENT-GUIDE.md) |
| Mentor briefing and graphical overview | [Architecture diagrams](03-ARCHITECTURE.md) |
| How the detector, pose solving, and fusion work | [Vision walkthrough and PhotonVision comparison](01-VISION-WALKTHROUGH.md) |
| Battery, breaker, current budgeting, and shooter algorithms | [Power walkthrough](02-POWER-WALKTHROUGH.md) |
| What MechaRAMS could adopt and how to evaluate it | [Adoption and validation roadmap](05-MECHARAMS-ROADMAP.md) |
| Exact versions, publication history, limits of evidence | [Evidence and history](06-EVIDENCE-AND-HISTORY.md) |

The diagrams use fenced `mermaid` blocks, which GitHub renders graphically in the normal Markdown view.

## Qualifications that change the engineering decision

1. **Autonomous bypasses the adaptive drive allocation.** It uses 50 A per drive motor. Outside autonomous, the calculated limit is clamped to 5–35 A per motor and rounded down in 0.5 A steps. The 5 A floor can also exceed a very small remaining budget. [Allocation code][M-FIN], [constants][M-LIMITS].
2. **The 190 A total budget is a model target, not a guaranteed instantaneous robot-current ceiling.** Other loads, telemetry delay, hardware application delay, the minimum drive allowance, and the autonomous override matter.
3. **Aruco Max is tied to Apple Metal in the published build.** It is not a drop-in Orange Pi detector. A port would require replacing the platform-specific implementation and measuring its performance. [Build configuration][M-SETUP].
4. **“2026 public code” does not mean “verified at every 2026 event.”** Aruco Max first appears publicly on May 29; the integrated manager first appears on May 20. An earlier battery/breaker implementation appeared April 24, was removed April 28, and returned May 20. Public publication dates do not identify private development or deployment dates.
5. **The prior June source snapshot is still useful.** Between the June 26 EDT snapshot used by the previous research and this October 17 snapshot, Northstar is unchanged, and the only change within `energy/` is the total budget cap from 200 A to 190 A. See the reproducible comparison in [Evidence and history](06-EVIDENCE-AND-HISTORY.md).

## Scope and relationship to our earlier work

This supplements the [existing research handoff](../MechaRAMS%20vision%20system/RESEARCH_HANDOFF.md), [vision strategy](../MechaRAMS%20vision%20system/Vision-Strategy-2027-v2.md), and [research log](../MechaRAMS%20vision%20system/research-log.md). Those contain the team's broader localization, calibration, aiming, and trajectory work. This study concentrates on the detector internals and electrical power management that prompted the meeting questions.

The practical recommendation is to retain the existing vision baseline while testing whether a detector change solves a measured problem, and to start power-management work with trustworthy current/voltage logging and an advisory model. The models are small enough to study independently of 6328's Mac mini/Idun control architecture; their published constants are specific to their robot and batteries.

<!-- Source references: generated from pinned research baselines. -->

[H-NORTHSTAR]: https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2023-build-thread/420691/36
[M-BAT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/BatteryEstimator.java
[M-BREAK]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/BreakerModel.java
[M-DETECT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/aruco_max.h
[M-FIN]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/FinanceDepartment.java
[M-LIMITS]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/energy/CurrentLimits.java
[M-MODULECPP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/cpp/subsystems/ModuleIOTalonFX.cpp
[M-SETUP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/setup.py
[M-WORKER]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/apriltag_worker.py
[M-WRAP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/wrapper.cpp
[P-ARUCO]: https://github.com/PhotonVision/photonvision/blob/a5be3d062ce5c145f4a759b0207057f218cda6b3/photon-core/src/main/java/org/photonvision/vision/pipeline/ArucoPipeline.java
[P-POSE]: https://github.com/PhotonVision/photonvision/blob/a5be3d062ce5c145f4a759b0207057f218cda6b3/photon-lib/src/main/java/org/photonvision/PhotonPoseEstimator.java
