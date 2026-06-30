package frc.robot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import frc.robot.Constants.AimConstants;
import frc.robot.util.AimingCalculator.AimingSolution;
import org.junit.jupiter.api.Test;

/**
 * Headless verification of the chassis-aiming geometry. Runs with {@code ./gradlew test} -- no robot,
 * no display. This is the kind of test our 2026 robot lacked, where a couple of degrees of aim error
 * decided the match.
 */
class AimingCalculatorTest {
  private static final double EPS = 1e-6;

  @Test
  void stationaryAimPointsStraightAtGoalWhenColinear() {
    // Robot directly "below" the goal in +x: goal is at GOAL_POSITION, robot 2 m toward -x at same y.
    var goal = AimConstants.GOAL_POSITION;
    var robot = new Pose2d(goal.getX() - 2.0, goal.getY(), Rotation2d.fromDegrees(123.0));
    AimingSolution s = AimingCalculator.solveStationary(robot);
    // Heading should point along +x (0 deg) toward the goal, plus the configured aim-face offset.
    assertEquals(
        AimConstants.ROBOT_AIM_OFFSET.getRadians(), s.driveHeading().getRadians(), EPS);
    assertEquals(2.0, s.distanceMeters(), EPS);
  }

  @Test
  void stationaryAim90DegreesWhenGoalIsToTheSide() {
    var goal = AimConstants.GOAL_POSITION;
    // Robot at same x, 1.5 m toward -y: goal is in the +y direction => 90 deg.
    var robot = new Pose2d(goal.getX(), goal.getY() - 1.5, Rotation2d.kZero);
    AimingSolution s = AimingCalculator.solveStationary(robot);
    assertEquals(
        90.0 + AimConstants.ROBOT_AIM_OFFSET.getDegrees(), s.driveHeading().getDegrees(), 1e-4);
  }

  @Test
  void movingLeadShiftsAimWhenStrafing() {
    var goal = AimConstants.GOAL_POSITION;
    var robot = new Pose2d(goal.getX() - 3.0, goal.getY(), Rotation2d.kZero);

    AimingSolution still = AimingCalculator.solveMoving(robot, new ChassisSpeeds());
    // Strafe in +y while aiming: the lead point moves +y, so the required heading rotates off 0 deg.
    AimingSolution moving = AimingCalculator.solveMoving(robot, new ChassisSpeeds(0.0, 2.0, 0.0));

    if (AimConstants.SHOOT_ON_MOVE_ENABLED) {
      assertEquals(0.0, still.driveHeading().getDegrees(), 1e-4);
      // Shoot-on-move aims from the velocity-led future position toward the goal (6328 model). Strafing
      // +y pushes the lead point +y, so the heading from there to the goal rotates NEGATIVE -- i.e. you
      // aim "behind" your motion to compensate, which is the physically correct lead direction.
      assertTrue(
          moving.driveHeading().getDegrees() < -0.5,
          "Strafing +y should rotate the aim heading negative (lead compensation), got "
              + moving.driveHeading().getDegrees());
      assertTrue(moving.leadPoint().getY() > robot.getY(), "Lead point should move +y when strafing +y");
    }
  }

  @Test
  void timeOfFlightModelIsPositiveAndMonotonic() {
    assertTrue(AimingCalculator.modelTimeOfFlight(0.0) > 0.0);
    assertTrue(
        AimingCalculator.modelTimeOfFlight(5.0) > AimingCalculator.modelTimeOfFlight(1.0),
        "Modeled TOF must grow with distance");
  }
}
