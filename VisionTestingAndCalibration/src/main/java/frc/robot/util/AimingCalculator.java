package frc.robot.util;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import frc.robot.Constants.AimConstants;

/**
 * Pure (stateless, side-effect-free) chassis-aiming math: given the robot pose and its field-relative
 * velocity, returns the heading that points the chosen robot face at the configured field goal,
 * optionally leading a moving robot (shoot-on-move).
 *
 * <p>Kept as a static util with no subsystem dependencies specifically so it can be unit-tested headless
 * (see {@code AimingCalculatorTest}) -- the geometry is where aiming actually succeeds or fails, and our
 * 2026 robot had no way to test it.
 *
 * <p>Idea traceability:
 *
 * <ul>
 *   <li>Heading-to-target = {@code atan2(goal - robot)} -- 6995 {@code TurretS.aimAtFieldPose}, 1768
 *       {@code ShootingUtil}, 6328 {@code LaunchCalculator.getDriveAngleWithLauncherOffset}.
 *   <li>Velocity-compensated "future pose" lookahead loop -- 6328 {@code LaunchCalculator} (20 iters)
 *       and 1768 {@code ShootingUtil} (25 iters). With a constant time-of-flight the loop converges in
 *       one step; because real TOF grows with distance, the loop matters. We model TOF as
 *       {@code base + perMeter * distance} so the demonstration is honest and testable.
 * </ul>
 */
public final class AimingCalculator {
  private AimingCalculator() {}

  /**
   * @param driveHeading field-relative heading to command so the robot's aim face points at the goal
   * @param aimPoint the goal point used (field coordinates)
   * @param leadPoint the velocity-compensated future robot position the solution aimed from
   * @param distanceMeters distance from {@code leadPoint} to the goal
   * @param timeOfFlightSeconds modeled projectile time used for the lead
   */
  public record AimingSolution(
      Rotation2d driveHeading,
      Translation2d aimPoint,
      Translation2d leadPoint,
      double distanceMeters,
      double timeOfFlightSeconds) {}

  /** Aims at the static goal with no motion lead (placing game / stationary shot). */
  public static AimingSolution solveStationary(Pose2d robotPose) {
    Translation2d goal = AimConstants.GOAL_POSITION;
    Translation2d robot = robotPose.getTranslation();
    double distance = goal.getDistance(robot);
    Rotation2d heading = goal.minus(robot).getAngle().plus(AimConstants.ROBOT_AIM_OFFSET);
    return new AimingSolution(heading, goal, robot, distance, modelTimeOfFlight(distance));
  }

  /**
   * Aims at the goal, leading the robot's current field velocity by the modeled time-of-flight so the
   * shot/placement lands correctly while moving. Falls back to the stationary solution when
   * shoot-on-move is disabled.
   */
  public static AimingSolution solveMoving(Pose2d robotPose, ChassisSpeeds fieldRelativeSpeeds) {
    if (!AimConstants.SHOOT_ON_MOVE_ENABLED) {
      return solveStationary(robotPose);
    }
    Translation2d goal = AimConstants.GOAL_POSITION;
    Translation2d robot = robotPose.getTranslation();

    // Converge a "future pose" lead: where the robot will be after the projectile's flight, where flight
    // time itself depends on (future) distance. Idea: 6328/1768 iterate this fixed-point.
    Translation2d leadPoint = robot;
    double distance = goal.getDistance(robot);
    double tof = modelTimeOfFlight(distance);
    for (int i = 0; i < AimConstants.SHOOT_ON_MOVE_ITERATIONS; i++) {
      tof = modelTimeOfFlight(distance);
      leadPoint =
          new Translation2d(
              robot.getX() + fieldRelativeSpeeds.vxMetersPerSecond * tof,
              robot.getY() + fieldRelativeSpeeds.vyMetersPerSecond * tof);
      distance = goal.getDistance(leadPoint);
    }
    Rotation2d heading = goal.minus(leadPoint).getAngle().plus(AimConstants.ROBOT_AIM_OFFSET);
    return new AimingSolution(heading, goal, leadPoint, distance, tof);
  }

  /** Teaching TOF model: linear in distance. A real shooter would interpolate a measured map. */
  public static double modelTimeOfFlight(double distanceMeters) {
    return AimConstants.SHOOT_ON_MOVE_BASE_TOF_SECONDS
        + AimConstants.SHOOT_ON_MOVE_TOF_PER_METER * distanceMeters;
  }
}
