package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import org.junit.jupiter.api.Test;

class RobotContainerStartPolicyTest {
  @Test
  void acceptsExpectedBoardTestStartArea() {
    assertTrue(
        RobotContainer.isSafeVisionTestStart(
            new Pose2d(1.6, 2.0, Rotation2d.fromDegrees(5.0))));
    assertTrue(
        RobotContainer.isSafeVisionTestStart(
            new Pose2d(2.25, 2.0, Rotation2d.fromDegrees(-5.0))));
  }

  @Test
  void rejectsUnseededWrongSideAndBadHeadingStarts() {
    assertFalse(RobotContainer.isSafeVisionTestStart(Pose2d.kZero));
    assertFalse(
        RobotContainer.isSafeVisionTestStart(
            new Pose2d(2.8, 2.0, Rotation2d.kZero)));
    assertFalse(
        RobotContainer.isSafeVisionTestStart(
            new Pose2d(1.6, 3.0, Rotation2d.kZero)));
    assertFalse(
        RobotContainer.isSafeVisionTestStart(
            new Pose2d(1.6, 2.0, Rotation2d.fromDegrees(20.0))));
  }

  @Test
  void createsHolonomicTargetsRelativeToMeasuredTranslationAndNormalizesYaw() {
    RobotContainer.HolonomicTestTargets targets =
        RobotContainer.createHolonomicTestTargets(
            new Pose2d(2.2, 2.0, Rotation2d.fromDegrees(4.0)));

    assertEquals(2.2, targets.start().getX(), 1e-9);
    assertEquals(2.0, targets.start().getY(), 1e-9);
    assertEquals(0.0, targets.start().getRotation().getDegrees(), 1e-9);
    assertEquals(3.7, targets.entry().getX(), 1e-9);
    assertEquals(2.0, targets.entry().getY(), 1e-9);
    assertEquals(3.7, targets.left().getX(), 1e-9);
    assertEquals(2.75, targets.left().getY(), 1e-9);
    assertEquals(4.45, targets.diagonal().getX(), 1e-9);
    assertEquals(2.75, targets.diagonal().getY(), 1e-9);
    assertTrue(RobotContainer.isSafeHolonomicTestPlan(targets));
    assertFalse(
        RobotContainer.isSafeHolonomicTestStart(
            new Pose2d(2.2, 2.0, Rotation2d.fromDegrees(20.0))));
  }

  @Test
  void rejectsHolonomicPlanTooCloseToBoardOrFieldEdge() {
    Pose2d validStart = new Pose2d(2.2, 2.0, Rotation2d.kZero);
    RobotContainer.HolonomicTestTargets tooCloseToBoard =
        new RobotContainer.HolonomicTestTargets(
            validStart,
            new Pose2d(3.7, 2.0, Rotation2d.kZero),
            new Pose2d(3.7, 2.75, Rotation2d.kZero),
            new Pose2d(5.0, 2.75, Rotation2d.kZero));
    RobotContainer.HolonomicTestTargets tooCloseToFieldEdge =
        new RobotContainer.HolonomicTestTargets(
            validStart,
            new Pose2d(3.7, 2.0, Rotation2d.kZero),
            new Pose2d(3.7, 3.7, Rotation2d.kZero),
            new Pose2d(4.45, 3.7, Rotation2d.kZero));

    assertFalse(RobotContainer.isSafeHolonomicTestPlan(tooCloseToBoard));
    assertFalse(RobotContainer.isSafeHolonomicTestPlan(tooCloseToFieldEdge));
  }

  @Test
  void finalHandoffRequiresTheFinalStraightAndAlignedTravel() {
    Pose2d finalApproachStart = new Pose2d(3.7, 2.30, Rotation2d.kZero);
    Pose2d finalTarget = new Pose2d(3.7, 2.75, Rotation2d.kZero);

    assertFalse(
        RobotContainer.isReadyForFinalApproachHandoff(
            new Pose2d(3.68, 2.25, Rotation2d.kZero),
            new ChassisSpeeds(0.40, 0.40, 0.0),
            finalApproachStart,
            finalTarget));
    assertFalse(
        RobotContainer.isReadyForFinalApproachHandoff(
            new Pose2d(3.79, 2.50, Rotation2d.kZero),
            new ChassisSpeeds(0.0, 0.80, 0.0),
            finalApproachStart,
            finalTarget));
    assertFalse(
        RobotContainer.isReadyForFinalApproachHandoff(
            new Pose2d(3.70, 2.50, Rotation2d.kZero),
            new ChassisSpeeds(0.0, -0.50, 0.0),
            finalApproachStart,
            finalTarget));
    assertTrue(
        RobotContainer.isReadyForFinalApproachHandoff(
            new Pose2d(3.72, 2.50, Rotation2d.kZero),
            new ChassisSpeeds(0.05, 0.80, 0.0),
            finalApproachStart,
            finalTarget));
  }
}
