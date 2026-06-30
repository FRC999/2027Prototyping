package frc.robot.subsystems.vision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import frc.robot.Constants.VisionConstants;
import frc.robot.subsystems.vision.Vision.RejectionReason;
import frc.robot.subsystems.vision.VisionIO.PoseObservation;
import org.junit.jupiter.api.Test;

/**
 * Headless verification of the vision fusion policy (the rejection gates and adaptive covariance). These
 * encode this project's whole thesis -- the measurement discipline our 2026 robot got wrong -- so they
 * are worth pinning with tests.
 */
class VisionPolicyTest {
  private static PoseObservation obs(double x, double y, double z, double ambiguity, int tagCount, double dist) {
    return new PoseObservation(0.0, new Pose3d(x, y, z, new Rotation3d()), ambiguity, tagCount, dist);
  }

  @Test
  void acceptsGoodMultiTagPose() {
    assertEquals(RejectionReason.ACCEPTED, Vision.rejectionReason(obs(4.0, 2.0, 0.0, 0.0, 2, 2.0)));
  }

  @Test
  void rejectsNoTags() {
    assertEquals(RejectionReason.NO_TAGS, Vision.rejectionReason(obs(4.0, 2.0, 0.0, 0.0, 0, 2.0)));
  }

  @Test
  void rejectsNonFinite() {
    assertEquals(
        RejectionReason.NON_FINITE,
        Vision.rejectionReason(obs(Double.NaN, 2.0, 0.0, 0.0, 2, 2.0)));
  }

  @Test
  void rejectsImpossibleZ() {
    assertEquals(
        RejectionReason.BAD_Z,
        Vision.rejectionReason(obs(4.0, 2.0, VisionConstants.MAX_ACCEPTED_Z_METERS + 0.5, 0.0, 2, 2.0)));
  }

  @Test
  void rejectsOutsideField() {
    assertEquals(
        RejectionReason.OUTSIDE_FIELD, Vision.rejectionReason(obs(-5.0, 2.0, 0.0, 0.0, 2, 2.0)));
  }

  @Test
  void rejectsTooFar() {
    assertEquals(
        RejectionReason.TOO_FAR,
        Vision.rejectionReason(
            obs(4.0, 2.0, 0.0, 0.0, 2, VisionConstants.MAX_AVERAGE_TAG_DISTANCE_METERS + 1.0)));
  }

  @Test
  void rejectsAmbiguousSingleTag() {
    assertEquals(
        RejectionReason.SINGLE_TAG_AMBIGUOUS,
        Vision.rejectionReason(
            obs(4.0, 2.0, 0.0, VisionConstants.MAX_SINGLE_TAG_AMBIGUITY + 0.1, 1, 2.0)));
  }

  @Test
  void singleTagHeadingIsNeverTrusted() {
    // tagCount 1 -> trustRotation false -> theta std dev must be +Infinity (6328 discipline).
    var stdDevs = Vision.standardDeviations(0, 2.0, 1, false);
    assertTrue(Double.isInfinite(stdDevs.get(2, 0)), "single-tag theta std dev must be infinite");
    assertTrue(Double.isFinite(stdDevs.get(0, 0)), "single-tag xy std dev must stay finite");
  }

  @Test
  void covarianceScalesWithDistanceSquaredOverTagCount() {
    // dist=2, tags=2 -> factor 4/2 = 2.0; baseline 0.06 -> xy 0.12; theta baseline 0.08 -> 0.16.
    var stdDevs = Vision.standardDeviations(0, 2.0, 2, true);
    assertEquals(VisionConstants.LINEAR_STD_DEV_BASELINE * 2.0, stdDevs.get(0, 0), 1e-9);
    assertEquals(VisionConstants.ANGULAR_STD_DEV_BASELINE * 2.0, stdDevs.get(2, 0), 1e-9);
  }

  @Test
  void moreTagsMeansTighterCovariance() {
    double twoTag = Vision.standardDeviations(0, 3.0, 2, true).get(0, 0);
    double threeTag = Vision.standardDeviations(0, 3.0, 3, true).get(0, 0);
    assertTrue(threeTag < twoTag, "more tags should reduce the std dev");
  }

  @Test
  void teleopAlwaysFusesVision() {
    // Not autonomous -> always accept, regardless of the timer value.
    assertTrue(Vision.shouldAcceptDuringAuto(false, 0.0));
    assertTrue(Vision.shouldAcceptDuringAuto(false, 100.0));
  }

  @Test
  void earlyAutoSuppressesThenResumes() {
    double ignore = VisionConstants.AUTO_VISION_IGNORE_SECONDS;
    assertTrue(!Vision.shouldAcceptDuringAuto(true, ignore - 0.05), "early auto must suppress fusion");
    assertTrue(Vision.shouldAcceptDuringAuto(true, ignore + 0.05), "fusion resumes after the window");
  }
}
