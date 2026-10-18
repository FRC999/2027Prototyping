# Northstar, Aruco Max, and the PhotonVision comparison

Review date: October 17, 2026. Source baseline: Mechanical Advantage `b2923d6`; comparison: PhotonVision **v2026.3.4**, `a5be3d0`. Exact revisions and publication limits are in [Evidence and history](06-EVIDENCE-AND-HISTORY.md).

## 1. Separate the three problems

An AprilTag localization system does three different jobs:

| Stage | Question answered | Typical output |
|---|---|---|
| Detection | Where are the tag's corners, and which tag is it? | Tag ID and four image coordinates |
| Pose solving | What camera position and orientation could produce those corners? | One or more 3D poses and reprojection errors |
| Robot localization | How should this delayed measurement change our position estimate? | A corrected robot pose combined with wheel and gyro history |

A better detector can find more tags or produce better corners. It cannot compensate for a wrong field layout, incorrect camera mounting measurements, or faulty fusion timing. A good pose solver also cannot recover information that an image never captured.

In 6328's implementation, **Aruco Max performs detection; OpenCV performs the core pose solve; `Vision` and `RobotState` perform robot-side selection and fusion**. [Worker][M-WORKER], [camera pose solver][M-PNP], [vision processing][M-VISION], [robot state][M-STATE].

## 2. What is actually custom in the detector?

`apriltag_worker.py` explicitly constructs `ArucoMaxFiducialDetector` with the AprilTag 36h11 dictionary. The ordinary OpenCV detector class still exists in `FiducialDetector.py`, but it is not the class selected by this worker. The Python wrapper converts color images to grayscale and invokes the native extension. [Worker][M-WORKER], [detector wrapper][M-FID].

The extension describes itself as an optimized ArUco detector based on ArUco Nano and includes an ArUco Nano license. This establishes reuse and customization, not an invention from a blank page. “ArUco” describes the detection implementation here; it can decode the standard AprilTag dictionary used by the field. [Native wrapper][M-WRAP], [upstream notice][M-NANOLICENSE].

The executable pipeline in `aruco_max.h` is:

1. **Grayscale preparation.** Work on brightness values rather than three color channels.
2. **Adaptive thresholding.** Compare each pixel against a local brightness average to produce a binary image. Defaults use a 15-pixel box window and threshold difference of 3. This makes dark borders detectable under varying illumination.
3. **Contour tracing.** Trace connected boundaries using a custom routine that tracks revisits. Reject contours too small to be useful.
4. **Quadrilateral filtering.** Approximate each contour as a polygon; require four corners, convexity, and minimum side size. The default minimum side length is 10 pixels.
5. **Bit sampling and identification.** Use a perspective transform to sample the candidate's cell centers from the grayscale image. Threshold the sampled cells and compare the interior bits to the dictionary after checking the border.
6. **Retry uncertain candidates.** Make up to five attempts. Subsequent attempts perturb sampling corners with Gaussian noise of 0.75 pixels; the third attempt uses a different adaptive threshold on the sampled grid, while the others use Otsu thresholding. These retries affect decoding; final geometric refinement is a separate step.
7. **Duplicate removal.** Remove nested same-ID detections associated with inner/outer boundaries. This is not a general prohibition on seeing the same ID twice in an image.
8. **Subpixel corner refinement.** Refine accepted corners with OpenCV `cornerSubPix`. Its search half-window scales with the marker perimeter and is clamped between 1 and 5 pixels.

The defaults allow no dictionary error correction and no erroneous border bits. This is an explicit detection tradeoff: strict acceptance can suppress false detections while reducing tolerance to damaged or poorly resolved tags. The code does not prove how large either effect is in matches. [Implementation and defaults][M-DETECT].

## 3. Exactly what does the GPU do?

Apple Metal accelerates the **adaptive-threshold stage**. One GPU kernel computes horizontal brightness sums; a second computes vertical sums and the threshold comparison. Buffers are reused when image dimensions stay the same. Contour extraction, decoding, and corner refinement remain in the C++/OpenCV path visible in the source. It would be inaccurate to call the entire recognition and pose pipeline GPU-based. [Metal implementation][M-METAL], [detector stages][M-DETECT].

The extension links Metal and Foundation and uses Homebrew OpenCV paths. Although the header contains a CPU threshold branch, the shown detector supplies the address of its own Metal context, and the build still requires Apple frameworks. That branch is not evidence of a working Linux fallback. **Using this version on Orange Pi would be a porting project.** [Build][M-SETUP], [detector context][M-DETECT].

Engineering inference: accelerating a per-pixel operation can reduce processing cost for large images and multiple cameras. The source establishes the mechanism, but this review did not measure a speedup, detection-range improvement, or localization-error improvement relative to PhotonVision.

## 4. Pose solving is recognizable, reusable geometry

The solver obtains the camera intrinsics and lens distortion from calibration, tag size from configuration, and tag coordinates from the selected field layout. It ignores IDs that are absent from that layout. [Camera pose solver][M-PNP].

For **one known tag**, it uses OpenCV `SOLVEPNP_IPPE_SQUARE`, producing two pose candidates and their reprojection errors. A flat square can produce ambiguous solutions, especially when the image does not strongly distinguish its orientation. The robot code receives both candidates.

For **multiple known tags in one camera image**, it transforms every tag corner into field coordinates and passes the collected image/field correspondences to `SOLVEPNP_SQPNP`. This produces a field-relative camera pose. It is a joint solve over corners, not an average of separately estimated tag poses. Each camera is solved separately; the later robot estimator combines observations across cameras. [Camera pose solver][M-PNP].

This is not a uniquely Northstar capability. PhotonVision's checked 2026 source also contains a multi-target PnP path using SQPnP, and its shared estimation utility uses a square solver for the single-target case. PhotonVision's AprilTag pipeline also has its own single-tag estimator, so it would be too broad to claim every single-tag path in both products is identical. [PhotonVision multi-target geometry][P-GEOMETRY], [AprilTag pipeline][P-APRILPIPE].

## 5. What happens to a delayed observation?

Northstar captures a frame, records `time.time()` after `get_frame()` returns, and sends that timestamp with the result. Its worker input queue has capacity one; a full input queue causes a new image to be skipped. This limits backlog. It does **not** process every image produced by the camera. NetworkTables publishers preserve individual completed observations, and `VisionIONorthstar` drains the observation queue on the robot-code side. [Main loop][M-NSMAIN], [publisher][M-PUBLISH], [receiver][M-IO].

That distinction matters: “process all queued vision results” is different from “process every captured frame.” Also, the shown timestamp is a host-side frame-retrieval timestamp, not proof of a hardware timestamp at the midpoint of exposure. End-to-end timing still needs validation.

The Java vision path then:

1. Looks up the camera-to-robot transform at the observation timestamp. One camera's transform depends on a historical mechanism-angle sample; without that sample its observation is skipped.
2. For a single tag, requires one reprojection error to be less than 0.4 times the other. Only then does it select the candidate whose robot heading is closer to the current estimated heading. It does not simply accept whichever candidate has the smallest reprojection error.
3. Converts the selected camera pose to robot pose.
4. Rejects positions beyond the field plus a 0.5 m margin, or outside the accepted Z range of -0.5 to +1.0 m.
5. Computes measurement standard deviations from distance, tag count, and a camera-specific multiplier.
6. Sorts the accepted observations by timestamp and sends them to `RobotState`.

Sources: [Vision][M-VISION], [configuration and moving-camera geometry][M-VCONST].

The weighting rules in the checked version are:

```text
sigmaXY = 0.01 * averageDistance^2 / tagCount^2 * cameraFactor
sigmaTheta = 0.03 * averageDistance^2 / tagCount^2 * cameraFactor
```

Distances are in meters; angular uncertainty is in radians. The second formula is used for multi-tag observations. For single-tag observations, `sigmaTheta` is infinity: heading helps choose the pose, but that observation is not used as an independent heading correction. These are tuned weighting rules, not a universal probabilistic accuracy guarantee. For example, 4 m with two tags and camera factor 1 gives `sigmaXY = 0.04 m`; that does not prove the actual error is four centimeters. [Vision weighting][M-VISION].

Two robot-specific exceptions qualify the “all cameras, all frames” description in earlier high-level discussions:

- Camera index 1 contributes pose observations only when camera index 0 is considered disconnected. Its detections can still be processed and logged.
- Vision fusion is withheld during the first 2 seconds of enabled autonomous.

These are observable policies, not general recommendations for MechaRAMS. Their full design rationale was not established by this review. [Vision][M-VISION], [constants][M-VCONST].

`RobotState` retains two seconds of odometry pose history. For a vision update, it shifts the current estimate back to the measurement time using odometry, applies a weighted pose correction, then moves forward again with the odometry transform. Its diagonal gain uses a closed-form filter expression associated with WPILib's estimator approach. It is not a camera-only pose reset, a joint image optimizer across all cameras, or evidence of a full evolving covariance EKF. [RobotState][M-STATE].

## 6. Fair comparison with PhotonVision v2026.3.4

| Capability | 6328 implementation | PhotonVision baseline | Conclusion |
|---|---|---|---|
| AprilTag family | Standard 36h11 through Aruco Max | 36h11 supported | Same field markers |
| Native detection | ArUco Nano-derived custom C++ | WPILib AprilTag detector path; separate OpenCV ArUco path | Different implementations are available |
| GPU thresholding | Explicit Apple Metal kernels | The inspected detector paths use WPILib/OpenCV APIs | Concrete Northstar customization; no universal GPU claim about every possible PV platform |
| Multi-tag pose | SQPnP from one camera's visible tag corners | Coprocessor multi-target PnP | Not unique to Northstar |
| Alternative pose strategies | Team-specific filtering and heading selection | Multiple strategies, including constrained SolvePnP and PnP-distance trig solve | PV is not limited to a naive single-tag result |
| Result queues | NT observation queues | `getAllUnreadResults()`, bounded FIFO | Both can support all-unread-result processing |
| Camera extrinsics | Team functions, including a moving mount | Robot-to-camera transform can be updated through PhotonPoseEstimator | Mechanism history/integration is still robot code's responsibility |
| Robot fusion | Custom `RobotState`, explicit policies | PhotonLib pose output feeds the team's drivetrain estimator | Compare the complete integration, not just detector FPS |
| Configuration | Robot code publishes settings and layout; calibration stored locally | Product configuration/UI plus PhotonLib | Different ownership and workflow |

Sources: [PV AprilTag detector][P-DETECT], [PV ArUco detector][P-ARUCODETECT], [PV ArUco pipeline][P-ARUCO], [PV geometry][P-GEOMETRY], [PV pose estimator][P-POSE], [PV result queue][P-CAMERA], [Northstar receiver/configuration][M-IO].

PhotonVision also explicitly credits a 6328 reference implementation for its PnP-distance trig method. This is evidence of ideas moving into the common toolset, not evidence that teams must maintain a separate Northstar stack to benefit from all of 6328's work. [Pinned PhotonVision documentation][P-POSEDOC].

## 7. What is an innovation, and what is established practice?

**Concrete 2026 customization:** Aruco Max integration, its Metal threshold implementation, and the detector configuration/processing choices above. Its first public appearance in this repository is May 29.

**Older Northstar ideas retained in 2026:** hardware-specific optimization, robot-owned camera configuration, queued observations, and robot-side fusion. Northstar existed by 2023; these should not be presented as inventions made in 2026. The January 2026 repository snapshot still selected the ordinary OpenCV ArUco detector. [Historical explanation][H-NORTHSTAR], [initial detector][H-INITIALFID], [Aruco Max introduction][H-ARUCOCOMMIT].

**Established mathematical building blocks:** calibrated projective geometry, IPPE/SQPnP, subpixel corner refinement, gyro/odometry fusion, and uncertainty weighting. The useful engineering contribution is how those pieces are selected, optimized, and operated together.

No review of two implementations can establish superiority to “everybody else.” No matched-hardware 2026 benchmark was verified here. The appropriate MechaRAMS question is whether our present limiting factor is missed detections, poor corners, timing, calibration, or fusion. Only the first two directly motivate changing the detector. The [roadmap](05-MECHARAMS-ROADMAP.md) describes how to distinguish them.

<!-- Source references: generated from pinned research baselines. -->

[H-ARUCOCOMMIT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/commit/bb88510d233e96c9ec3179fff896c6b21ca2503d
[H-INITIALFID]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/5ddc8c947216495eef914f82b2353980442210b6/northstar/pipeline/FiducialDetector.py
[H-NORTHSTAR]: https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2023-build-thread/420691/36
[M-DETECT]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/aruco_max.h
[M-FID]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/pipeline/FiducialDetector.py
[M-IO]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/src/main/java/org/littletonrobotics/frc2026/subsystems/vision/VisionIONorthstar.java
[M-METAL]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/metal_context.cpp
[M-NANOLICENSE]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/aruco_max_cpp/LICENSE-ArucoNano.txt
[M-NSMAIN]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/__init__.py
[M-PNP]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/pipeline/CameraPoseEstimator.py
[M-PUBLISH]: https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/b2923d6557d4c9bef62d26932cc8a66fea2e07a6/northstar/output/OutputPublisher.py
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
