package frc.robot.subsystems.vision;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.photonvision.PhotonCamera;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import frc.robot.Constants.VisionConstants;

/**
 * Real-hardware PhotonVision camera IO.
 *
 * <p>Idea traceability: this is the official AdvantageKit PhotonVision template implementation (shipped
 * by 1768 Nashoba as {@code VisionIOPhotonVision}), adapted to Team 999's custom two-tag layout.
 *
 * <p>Key points the template gets right and Codex's first pass also did:
 *
 * <p>- {@code getAllUnreadResults()} drains <em>every</em> queued frame, not just the latest. PhotonVision
 * can buffer several frames between 50 Hz robot loops; dropping them throws away corrections. A local
 * FIFO preserves every pose while pacing estimator delivery so a burst cannot block one control loop.
 *
 * <p>- Multi-tag uses the coprocessor's combined PnP solve directly; single-tag is reconstructed from the
 * known tag pose. The robot pose is obtained by composing the field->camera transform with the inverse of
 * the robot->camera transform.
 *
 * <p>What was intentionally removed vs the 1768 template: 1768 hard-codes a blocklist of game-specific
 * REEFSCAPE tag IDs to ignore for single-tag solves. We have no such game tags, so the blocklist is gone;
 * single-tag quality is instead governed by the ambiguity gate in {@link Vision}.
 */
public class VisionIOPhotonVision implements VisionIO {
  /**
   * Bound expensive timestamped estimator rewinds without dropping camera frames. One observation
   * per camera per 20 ms loop provides 50 observations/second/camera, above the measured pipeline
   * delivery rate, while spreading a burst across several control cycles instead of processing nine
   * old poses in one cycle.
   */
  private static final int MAX_POSE_OBSERVATIONS_PER_UPDATE = 1;

  protected final PhotonCamera camera;
  protected final Transform3d robotToCamera;
  private final ArrayDeque<PoseObservation> pendingPoseObservations = new ArrayDeque<>();

  public VisionIOPhotonVision(String name, Transform3d robotToCamera) {
    camera = new PhotonCamera(name);
    this.robotToCamera = robotToCamera;
  }

  @Override
  public void updateInputs(VisionIOInputs inputs) {
    inputs.connected = camera.isConnected();

    Set<Short> tagIds = new HashSet<>();
    var unreadResults = camera.getAllUnreadResults();
    inputs.unreadResultCount = unreadResults.size();

    for (var result : unreadResults) {
      // Latest simple target bearing (for future boresight aiming, not for fusion).
      if (result.hasTargets()) {
        inputs.latestTargetObservation =
            new TargetObservation(
                Rotation2d.fromDegrees(result.getBestTarget().getYaw()),
                Rotation2d.fromDegrees(result.getBestTarget().getPitch()),
                true,
                result.getTimestampSeconds());
      } else {
        inputs.latestTargetObservation =
            new TargetObservation(Rotation2d.kZero, Rotation2d.kZero, false, result.getTimestampSeconds());
      }

      if (result.multitagResult.isPresent() && !result.targets.isEmpty()) {
        // Multi-tag: coprocessor solved a combined field->camera transform (!isEmpty guards the divide).
        var multitagResult = result.multitagResult.get();
        Transform3d fieldToCamera = multitagResult.estimatedPose.best;
        Transform3d fieldToRobot = fieldToCamera.plus(robotToCamera.inverse());
        Pose3d robotPose = new Pose3d(fieldToRobot.getTranslation(), fieldToRobot.getRotation());

        double totalTagDistance = 0.0;
        for (var target : result.targets) {
          totalTagDistance += target.bestCameraToTarget.getTranslation().getNorm();
        }
        tagIds.addAll(multitagResult.fiducialIDsUsed);

        pendingPoseObservations.addLast(
            new PoseObservation(
                result.getTimestampSeconds(),
                robotPose,
                multitagResult.estimatedPose.ambiguity,
                multitagResult.fiducialIDsUsed.size(),
                totalTagDistance / result.targets.size(),
                // Primary tag = first tag of the combined solve (anchors the anisotropic-covariance
                // ray angle; on the 0.5 m two-tag board the per-tag ray angles are nearly identical).
                multitagResult.fiducialIDsUsed.isEmpty()
                    ? -1
                    : multitagResult.fiducialIDsUsed.get(0)));

      } else if (!result.targets.isEmpty()) {
        // Single-tag: reconstruct robot pose from the known tag pose in our custom layout.
        var target = result.targets.get(0);
        var tagPose = VisionConstants.CUSTOM_FIELD_LAYOUT.getTagPose(target.fiducialId);
        if (tagPose.isPresent()) {
          Transform3d fieldToTarget =
              new Transform3d(tagPose.get().getTranslation(), tagPose.get().getRotation());
          Transform3d cameraToTarget = target.bestCameraToTarget;
          Transform3d fieldToCamera = fieldToTarget.plus(cameraToTarget.inverse());
          Transform3d fieldToRobot = fieldToCamera.plus(robotToCamera.inverse());
          Pose3d robotPose = new Pose3d(fieldToRobot.getTranslation(), fieldToRobot.getRotation());

          tagIds.add((short) target.fiducialId);
          pendingPoseObservations.addLast(
              new PoseObservation(
                  result.getTimestampSeconds(),
                  robotPose,
                  target.poseAmbiguity,
                  1,
                  cameraToTarget.getTranslation().getNorm(),
                  // Primary tag = THE tag: lets Vision reconstruct cameraToTarget for the trig-solve
                  // strategy without logging a whole extra Transform3d per observation.
                  target.fiducialId));
        }
      }
    }

    List<PoseObservation> poseObservations =
        new ArrayList<>(MAX_POSE_OBSERVATIONS_PER_UPDATE);
    while (poseObservations.size() < MAX_POSE_OBSERVATIONS_PER_UPDATE
        && !pendingPoseObservations.isEmpty()) {
      poseObservations.add(pendingPoseObservations.removeFirst());
    }
    inputs.poseObservations = poseObservations.toArray(new PoseObservation[0]);
    inputs.pendingPoseObservationCount = pendingPoseObservations.size();

    inputs.tagIds = new int[tagIds.size()];
    int i = 0;
    for (int id : tagIds) {
      inputs.tagIds[i++] = id;
    }
  }
}
