package frc.robot.subsystems;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.littletonrobotics.junction.Logger;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.VisionConstants;
import frc.robot.util.TimingTracer;

/**
 * PhotonVision AprilTag localization front end.
 *
 * <p>The subsystem owns camera polling, pose-estimator selection, observation validation,
 * covariance selection, timestamp ordering, and AdvantageKit logging. The drivetrain only receives
 * accepted, timestamped observations.
 *
 * <p>Idea traceability:
 *
 * <p>- 6328/Northstar: consume every unread camera frame, preserve original timestamps, sort all
 * accepted observations before fusion, and reject physically impossible poses before they affect
 * odometry.
 *
 * <p>- 125 and other conservative single-tag approaches: single-tag frames may contribute
 * translation, but heading is effectively ignored by assigning a huge theta standard deviation.
 *
 * <p>- 3467/5687-style estimator review: every camera logs raw pose, tag count, average distance,
 * accepted/rejected frame counts, and a human-readable rejection reason.
 *
 * <p>- 1768/PhotonVision pattern: PhotonVision does offboard target detection and robot-pose
 * solving; the roboRIO remains responsible for final estimator fusion and command decisions.
 */
public class VisionSubsystem extends SubsystemBase {
  private final DriveSubsystem drive;
  private final CameraPipeline[] cameras;
  private final TimingTracer timingTracer = new TimingTracer("Vision");

  public VisionSubsystem(DriveSubsystem drive) {
    this.drive = drive;
    cameras = new CameraPipeline[] {
        new CameraPipeline(VisionConstants.FRONT_LEFT_CAMERA_NAME, VisionConstants.ROBOT_TO_FRONT_LEFT_CAMERA),
        new CameraPipeline(VisionConstants.FRONT_RIGHT_CAMERA_NAME, VisionConstants.ROBOT_TO_FRONT_RIGHT_CAMERA)
    };
  }

  @Override
  public void periodic() {
    timingTracer.start();
    List<VisionObservation> observations = new ArrayList<>();
    for (CameraPipeline camera : cameras) {
      camera.collectObservations(observations);
    }
    /*
     * PhotonVision can deliver multiple unread frames per camera in a single robot loop. Sorting the
     * merged list by capture timestamp makes fusion deterministic and avoids applying a newer camera
     * frame before an older frame from the other camera.
     */
    observations.sort(Comparator.comparingDouble(VisionObservation::timestampSeconds));
    for (VisionObservation observation : observations) {
      drive.addVisionMeasurement(observation.pose(), observation.timestampSeconds(), observation.standardDeviations());
    }
    Logger.recordOutput("Vision/AcceptedObservationCount", observations.size());
    timingTracer.stopAndLog();
  }

  /**
   * Per-camera PhotonVision pipeline.
   *
   * <p>Each physical camera owns one PhotonCamera and one PhotonPoseEstimator because the
   * robot-to-camera transform is part of pose solving. Keeping this state per camera avoids
   * accidentally reusing one camera's transform for the other camera.
   */
  private static final class CameraPipeline {
    private final String name;
    private final PhotonCamera camera;
    private final PhotonPoseEstimator estimator;

    CameraPipeline(String name, edu.wpi.first.math.geometry.Transform3d robotToCamera) {
      this.name = name;
      camera = new PhotonCamera(name);
      estimator = new PhotonPoseEstimator(
          VisionConstants.CUSTOM_FIELD_LAYOUT,
          robotToCamera);
    }

    /**
     * Pulls every unread PhotonVision result from this camera, converts valid results into
     * observations, and appends accepted observations to the caller-owned list.
     *
     * <p>Rejected frames still update logs. This is deliberate: a high rejection count can mean the
     * camera is useful but the mount/calibration/layout is wrong, which is a different problem than
     * no frames arriving at all.
     */
    void collectObservations(List<VisionObservation> acceptedObservations) {
      int accepted = 0;
      int rejected = 0;

      for (PhotonPipelineResult result : camera.getAllUnreadResults()) {
        if (!result.hasTargets()) {
          continue;
        }

        Optional<EstimatedRobotPose> estimate = estimateRobotPose(result);
        if (estimate.isEmpty()) {
          rejected++;
          continue;
        }

        EstimatedRobotPose robotPose = estimate.get();
        Pose3d estimatedPose3d = robotPose.estimatedPose;
        Pose2d estimatedPose = estimatedPose3d.toPose2d();
        List<PhotonTrackedTarget> targets = robotPose.targetsUsed;
        int tagCount = targets.size();
        double averageDistance = averageTagDistanceMeters(targets);
        boolean trustedRotation = tagCount >= 2;
        String rejectionReason = rejectionReason(estimatedPose3d, estimatedPose, tagCount, averageDistance, targets);

        /*
         * Raw pose logging happens before rejection so AdvantageScope can show what the camera
         * wanted to contribute even when the fusion policy correctly discards it.
         */
        Logger.recordOutput("Vision/" + name + "/LastRawPose", estimatedPose);
        Logger.recordOutput("Vision/" + name + "/LastTagCount", tagCount);
        Logger.recordOutput("Vision/" + name + "/LastAverageDistanceMeters", averageDistance);
        Logger.recordOutput("Vision/" + name + "/LastTrustedRotation", trustedRotation);

        if (!rejectionReason.isEmpty()) {
          Logger.recordOutput("Vision/" + name + "/LastRejectedReason", rejectionReason);
          rejected++;
          continue;
        }

        Matrix<N3, N1> stdDevs = standardDeviations(tagCount, averageDistance, trustedRotation);
        acceptedObservations.add(new VisionObservation(estimatedPose, robotPose.timestampSeconds, stdDevs));
        Logger.recordOutput("Vision/" + name + "/LastAcceptedPose", estimatedPose);
        Logger.recordOutput("Vision/" + name + "/LastRejectedReason", "");
        accepted++;
      }

      Logger.recordOutput("Vision/" + name + "/AcceptedFrames", accepted);
      Logger.recordOutput("Vision/" + name + "/RejectedFrames", rejected);
      Logger.recordOutput("Vision/" + name + "/Connected", RobotBase.isSimulation() || camera.isConnected());
    }

    /**
     * Runs the preferred PhotonVision pose solve for this prototype.
     *
     * <p>The first choice is coprocessor multi-tag PNP because the Orange Pi should do the expensive
     * target processing and because multi-tag solves provide much better heading information. If
     * PhotonVision did not produce a multi-tag result, the fallback is the lowest-ambiguity single
     * tag pose. The fallback is still filtered later, and if accepted it receives weak heading
     * covariance.
     *
     * <p>Idea traceability: this reproduces the old PhotonPoseEstimator
     * MULTI_TAG_PNP_ON_COPROCESSOR + LOWEST_AMBIGUITY fallback behavior using the non-deprecated
     * PhotonVision 2026 individual estimator methods.
     */
    private Optional<EstimatedRobotPose> estimateRobotPose(PhotonPipelineResult result) {
      Optional<EstimatedRobotPose> multiTagEstimate = estimator.estimateCoprocMultiTagPose(result);
      if (multiTagEstimate.isPresent()) {
        return multiTagEstimate;
      }
      return estimator.estimateLowestAmbiguityPose(result);
    }

    /**
     * Average distance from camera to all targets used in the pose estimate.
     *
     * <p>Distance is used as a simple quality proxy: far tags produce noisier corner measurements
     * and should therefore get looser covariance.
     */
    private static double averageTagDistanceMeters(List<PhotonTrackedTarget> targets) {
      if (targets.isEmpty()) {
        return Double.POSITIVE_INFINITY;
      }
      double total = 0.0;
      for (PhotonTrackedTarget target : targets) {
        total += target.getBestCameraToTarget().getTranslation().getNorm();
      }
      return total / targets.size();
    }

    /**
     * Returns an empty string when the pose is acceptable; otherwise returns the reason to log.
     *
     * <p>Idea traceability:
     *
     * <p>- 6328/Northstar: reject poses outside the plausible field volume.
     *
     * <p>- 3467/5687-style estimator diagnostics: use short stable reason strings so log filters
     * and dashboards can count rejection categories over time.
     *
     * <p>- Conservative single-tag handling: ambiguity is only checked for one-tag estimates because
     * multi-tag solves are geometrically better constrained.
     */
    private static String rejectionReason(
        Pose3d pose3d,
        Pose2d pose2d,
        int tagCount,
        double averageDistance,
        List<PhotonTrackedTarget> targets) {
      if (tagCount == 0) {
        return "no-tags-used";
      }
      if (Math.abs(pose3d.getZ()) > VisionConstants.MAX_ACCEPTED_Z_METERS) {
        return "bad-z";
      }
      if (pose2d.getX() < -VisionConstants.FIELD_BORDER_MARGIN_METERS
          || pose2d.getX() > VisionConstants.FIELD_LENGTH_METERS + VisionConstants.FIELD_BORDER_MARGIN_METERS
          || pose2d.getY() < -VisionConstants.FIELD_BORDER_MARGIN_METERS
          || pose2d.getY() > VisionConstants.FIELD_WIDTH_METERS + VisionConstants.FIELD_BORDER_MARGIN_METERS) {
        return "outside-field";
      }
      if (averageDistance > VisionConstants.MAX_AVERAGE_TAG_DISTANCE_METERS) {
        return "too-far";
      }
      if (tagCount == 1 && targets.get(0).getPoseAmbiguity() > VisionConstants.MAX_SINGLE_TAG_AMBIGUITY) {
        return "single-tag-ambiguous";
      }
      return "";
    }

    /**
     * Selects measurement standard deviations for CTRE's estimator.
     *
     * <p>The model is intentionally simple for the pilot:
     *
     * <p>- farther tags get less trust by scaling with distance squared;
     *
     * <p>- more tags get more trust by dividing by tag count squared;
     *
     * <p>- one-tag estimates do not get trusted heading, so theta is effectively ignored.
     *
     * <p>Idea traceability: this is the same style of adaptive covariance used by many
     * AdvantageKit-era vision examples, including 6328/Northstar-inspired pipelines.
     */
    private static Matrix<N3, N1> standardDeviations(
        int tagCount,
        double averageDistanceMeters,
        boolean trustedRotation) {
      double distanceSquared = averageDistanceMeters * averageDistanceMeters;
      double tagScale = tagCount * tagCount;
      double xy = VisionConstants.BASE_XY_STD_DEV * distanceSquared / tagScale;
      double theta = trustedRotation
          ? VisionConstants.BASE_THETA_STD_DEV * distanceSquared / tagScale
          : VisionConstants.SINGLE_TAG_THETA_STD_DEV;
      return VecBuilder.fill(xy, xy, theta);
    }
  }

  /**
   * Fully vetted vision sample ready for drivetrain fusion.
   *
   * @param pose field-relative robot pose
   * @param timestampSeconds PhotonVision capture timestamp, not the current roboRIO loop time
   * @param standardDeviations x/y/theta uncertainty used by the drivetrain estimator
   */
  private record VisionObservation(
      Pose2d pose,
      double timestampSeconds,
      Matrix<N3, N1> standardDeviations) {}
}
