package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;

import org.littletonrobotics.junction.AutoLog;

/**
 * Hardware-abstraction layer for one AprilTag camera.
 *
 * <p>Idea traceability:
 *
 * <p>- 6328 AdvantageKit ecosystem + the official AdvantageKit PhotonVision template (also shipped by
 * 1768 Nashoba as {@code subsystems/vision/VisionIO.java}): the camera is hidden behind an IO
 * interface whose inputs are captured into an {@code @AutoLog} struct every loop. This is what makes
 * logs deterministically <em>replayable</em> -- the fusion policy in {@link Vision} re-runs identically
 * against recorded inputs, which is the single biggest debugging advantage of the AdvantageKit stack.
 *
 * <p>Codex's first pass read {@link org.photonvision.PhotonCamera} directly inside the subsystem, so
 * AdvantageKit was only a logger, not a replay harness. Splitting the IO restores replay and also makes
 * the simulation camera ({@link VisionIOPhotonVisionSim}) a drop-in swap.
 */
public interface VisionIO {
  @AutoLog
  public static class VisionIOInputs {
    /** True when the coprocessor camera is reachable. Drives the pit "disconnected" alert. */
    public boolean connected = false;

    /**
     * Angle to the single best target. Not used for pose fusion; kept so a future boresight/turret
     * aiming loop can servo directly on a tag bearing (the 2910/6328 "local" signal). Idea: 1768
     * template {@code getTargetX}.
     */
    public TargetObservation latestTargetObservation =
        new TargetObservation(Rotation2d.kZero, Rotation2d.kZero, false);

    /** Every full-robot pose solve produced since the last loop (multi-tag and single-tag). */
    public PoseObservation[] poseObservations = new PoseObservation[0];

    /** IDs of all tags seen this loop, for field visualization in AdvantageScope. */
    public int[] tagIds = new int[0];
  }

  /**
   * Bearing to the best visible target (camera-relative). {@code hasTarget} distinguishes "target dead
   * ahead (0,0)" from "no target this frame" so a future boresight loop does not act on a phantom zero.
   */
  public static record TargetObservation(Rotation2d tx, Rotation2d ty, boolean hasTarget) {}

  /**
   * One field-relative robot-pose estimate from one frame.
   *
   * @param timestamp capture time in the WPILib FPGA time base (converted to the CTRE time base by the
   *     consumer -- see {@link Vision} and {@code RobotContainer}).
   * @param pose field-relative robot pose solved by PhotonVision
   * @param ambiguity PnP ambiguity (single-tag only; ~0 for multi-tag)
   * @param tagCount number of tags used in the solve
   * @param averageTagDistance mean camera-to-tag distance, used for distance-squared covariance
   */
  public static record PoseObservation(
      double timestamp, Pose3d pose, double ambiguity, int tagCount, double averageTagDistance) {}

  public default void updateInputs(VisionIOInputs inputs) {}
}
