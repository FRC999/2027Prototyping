package frc.robot.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.robot.Constants.AutoConstants;
import frc.robot.lib.BLine.Path;
import org.junit.jupiter.api.Test;

class BLineRouteFactoryTest {
  @Test
  void straightPathUsesSharedConstraintsAndHasAnExplicitFinalTranslationIndex() {
    Path path = BLineRouteFactory.create(new Pose2d[] {
        new Pose2d(2.2, 2, Rotation2d.kZero), new Pose2d(3.7, 2, Rotation2d.kZero)
    }, Rotation2d.kZero);
    assertTrue(path.isValid());
    assertEquals(1.2, path.getDefaultGlobalConstraints().getMaxVelocityMetersPerSec(), 1e-9);
    assertEquals(AutoConstants.PRECISION_TRANSLATION_TOLERANCE_METERS,
        path.getEndTranslationToleranceMeters(), 1e-9);
    assertEquals(3, BLineRouteFactory.finalTranslationIndex(path));
    var finalTarget = (Path.TranslationTarget)
        path.getPathElementsWithConstraintsNoWaypoints().get(3).getFirst();
    assertEquals(3.7, finalTarget.translation().getX(), 1e-9);
    assertEquals(0.8, ((Path.TranslationTargetConstraint)
        path.getPathElementsWithConstraintsNoWaypoints().get(3).getSecond())
        .maxVelocityMetersPerSec(), 1e-9);
  }

  @Test
  void splineTangentsNeverBecomeChassisHeadingAndCornerLimitsUseTargetOrdinals() {
    Path path = BLineRouteFactory.create(new Pose2d[] {
        new Pose2d(2.2, 2, Rotation2d.kZero),
        new Pose2d(3.4, 2, Rotation2d.kZero),
        new Pose2d(3.9, 2.2, Rotation2d.fromDegrees(45)),
        new Pose2d(4.45, 2.75, Rotation2d.fromDegrees(45))
    }, Rotation2d.fromDegrees(-20));
    var elements = path.getPathElementsWithConstraintsNoWaypoints();
    List<Path.RotationTarget> rotations = elements.stream()
        .map(pair -> pair.getFirst()).filter(Path.RotationTarget.class::isInstance)
        .map(Path.RotationTarget.class::cast).toList();
    assertEquals(3, rotations.size());
    assertEquals(0, rotations.get(0).rotation().getDegrees(), 1e-9);
    assertEquals(0, rotations.get(1).rotation().getDegrees(), 1e-9);
    assertEquals(-20, rotations.get(2).rotation().getDegrees(), 1e-9);
    int ordinal = 0;
    for (var pair : elements) {
      if (pair.getFirst() instanceof Path.TranslationTarget) {
        var limit = (Path.TranslationTargetConstraint) pair.getSecond();
        assertEquals(ordinal == 1 || ordinal == 2 ? 0.6 : ordinal == 3 ? 0.8 : 1.2,
            limit.maxVelocityMetersPerSec(), 1e-9);
        assertEquals(0, limit.minVelocityMetersPerSec(), 1e-9);
        ordinal++;
      }
    }
    assertEquals(4, ordinal);
    assertTrue(elements.get(BLineRouteFactory.finalTranslationIndex(path)).getFirst()
        instanceof Path.TranslationTarget);
  }

  @Test
  void rejectsRoutesWithoutStartOrTranslationTargets() {
    assertThrows(IllegalArgumentException.class, () -> BLineRouteFactory.create(null, Rotation2d.kZero));
    assertThrows(IllegalArgumentException.class,
        () -> BLineRouteFactory.create(new Pose2d[] {Pose2d.kZero}, Rotation2d.kZero));
    Path rotationOnly = new Path(List.of(new Path.RotationTarget(Rotation2d.kZero, 1)),
        null, new Path.DefaultGlobalConstraints(1.2, 1.2, 60, 120, .04, 1.5, .1));
    assertThrows(IllegalArgumentException.class, () -> BLineRouteFactory.finalTranslationIndex(rotationOnly));
  }
}
