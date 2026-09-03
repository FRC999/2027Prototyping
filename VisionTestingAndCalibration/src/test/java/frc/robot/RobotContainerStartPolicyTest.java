package frc.robot;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
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
}
