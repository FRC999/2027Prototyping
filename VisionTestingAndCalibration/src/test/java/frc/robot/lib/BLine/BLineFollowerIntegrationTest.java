package frc.robot.lib.BLine;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.commands.GuardedBLineTravelCommand;
import frc.robot.util.BLineRouteFactory;
import frc.robot.util.BLineTravelPolicy.Outcome;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Runs the actual pinned follower with an ideal kinematic plant and deterministic
 * clock. This tests geometric progress and our transit lifecycle, not CTRE traction,
 * PhotonVision, physical endpoint accuracy, or the final controller's dynamics.
 * The package grants access to upstream's package-private test clock; production
 * continues using its WPILib clock. An upstream upgrade must revalidate this seam.
 */
class BLineFollowerIntegrationTest {
  @AfterEach
  void restoreClock() { FollowPath.setTimestampSupplier(null); }

  private static class Plant {
    final Pose2d start = new Pose2d(2.2, 2, Rotation2d.kZero);
    Pose2d pose = start;
    ChassisSpeeds requested = new ChassisSpeeds();
    ChassisSpeeds measured = new ChassisSpeeds();
    double now;
    boolean blocked;
    int stopped;
    final FollowPath follower;
    final GuardedBLineTravelCommand travel;

    Plant() {
      FollowPath.setTimestampSupplier(() -> now);
      Path path = BLineRouteFactory.create(new Pose2d[] {
          start, new Pose2d(3.7, 2, Rotation2d.kZero)
      }, Rotation2d.kZero);
      follower = new FollowPath.Builder(new Subsystem() {}, () -> pose, () -> measured,
          speeds -> requested = speeds, new PIDController(5, 0, 0),
          new PIDController(7, 0, 0), new PIDController(2, 0, 0))
          .withTRatioBasedTranslationHandoffs(true).build(path);
      travel = new GuardedBLineTravelCommand(follower, () -> true,
          () -> follower.getCurrentTranslationElementIndex() == BLineRouteFactory.finalTranslationIndex(path)
              && pose.getX() >= 3.15 && pose.getX() <= 3.7 && Math.abs(pose.getY() - 2) <= .05,
          8, () -> now, () -> stopped++);
      travel.initialize();
    }

    void tick() {
      now += .02;
      travel.execute();
      assertTrue(Double.isFinite(requested.vxMetersPerSecond));
      assertTrue(Double.isFinite(requested.vyMetersPerSecond));
      assertTrue(Double.isFinite(requested.omegaRadiansPerSecond));
      assertTrue(Math.hypot(requested.vxMetersPerSecond, requested.vyMetersPerSecond) <= 1.2 + 1e-9);
      measured = blocked ? new ChassisSpeeds() : requested;
      ChassisSpeeds field = ChassisSpeeds.fromRobotRelativeSpeeds(measured, pose.getRotation());
      pose = new Pose2d(pose.getTranslation().plus(new Translation2d(
          field.vxMetersPerSecond * .02, field.vyMetersPerSecond * .02)),
          pose.getRotation().plus(Rotation2d.fromRadians(field.omegaRadiansPerSecond * .02)));
    }

    void runToEnd() {
      for (int i = 0; i < 450 && !travel.isFinished(); i++) tick();
      assertTrue(travel.isFinished());
      travel.end(false);
    }
  }

  @Test
  void delayedPhysicalProgressKeepsGeometricTargetAndThenReachesHandoff() {
    Plant plant = new Plant();
    plant.blocked = true;
    for (int i = 0; i < 50; i++) plant.tick();
    assertEquals(plant.start, plant.pose);
    assertEquals(1.5, plant.follower.getRemainingPathDistanceMeters(), 1e-9);
    assertFalse(plant.travel.isFinished());
    plant.blocked = false;
    plant.runToEnd();
    assertEquals(Outcome.HANDOFF, plant.travel.getOutcome());
    assertEquals(1, plant.stopped);
  }

  @Test
  void lateralDisplacementRecoversBeforeQualifiedHandoff() {
    Plant plant = new Plant();
    for (int i = 0; i < 30; i++) plant.tick();
    plant.pose = new Pose2d(plant.pose.getX(), plant.pose.getY() + .15, plant.pose.getRotation());
    plant.runToEnd();
    assertEquals(Outcome.HANDOFF, plant.travel.getOutcome());
    assertTrue(Math.abs(plant.pose.getY() - 2) <= .05);
  }

  @Test
  void continuouslyBlockedRouteTimesOutWithoutCallingTheLastLegSuccessful() {
    Plant plant = new Plant();
    plant.blocked = true;
    plant.runToEnd();
    assertEquals(Outcome.TRAVEL_TIMEOUT, plant.travel.getOutcome());
    assertFalse(plant.travel.canHandoff());
    assertEquals(plant.start, plant.pose);
    assertEquals(0, plant.requested.vxMetersPerSecond, 1e-9);
  }
}
