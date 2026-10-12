package frc.robot.util;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import frc.robot.Constants.AutoConstants;
import frc.robot.lib.BLine.Path;

/**
 * Programmatic BLine paths sharing the H1-H4 geometric anchors and destinations.
 *
 * <p>2638's BLine separates translation anchors from holonomic heading. The angles
 * on our PathPlanner anchors are spline tangents, NOT requested chassis headings.
 * Keep zero yaw until the last straight, then interpolate to the final robot yaw.
 * A polyline through these anchors is intentionally not the same spline as PathPlanner.
 */
public final class BLineRouteFactory {
  private BLineRouteFactory() {}

  public static Path create(Pose2d[] anchors, Rotation2d finalHeading) {
    if (anchors == null || anchors.length < 2) {
      throw new IllegalArgumentException("BLine route needs at least a start and endpoint");
    }
    List<Path.PathElement> elements = new ArrayList<>();
    elements.add(new Path.Waypoint(anchors[0].getTranslation(), Rotation2d.kZero));
    for (int i = 1; i < anchors.length - 1; i++) {
      if (i == anchors.length - 2) {
        elements.add(new Path.Waypoint(anchors[i].getTranslation(), Rotation2d.kZero));
      } else {
        elements.add(new Path.TranslationTarget(anchors[i].getTranslation()));
      }
    }
    elements.add(new Path.Waypoint(anchors[anchors.length - 1].getTranslation(), finalHeading));

    Path.RangedConstraint finalApproachLimit = new Path.RangedConstraint(
        AutoConstants.HOLONOMIC_FINAL_HANDOFF_END_SPEED_METERS_PER_SECOND,
        anchors.length - 1, anchors.length - 1);
    Path.PathConstraints constraints = new Path.PathConstraints()
        .setMaxVelocityMetersPerSec(finalApproachLimit);
    if (anchors.length > 2) {
      // BLine ranged limits use translation-target ordinals, not expanded runtime indices.
      // Slow the approach to both anchors bounding the corner. Cap the final straight
      // at the existing H approach speed rather than entering the 0.55 m handoff at
      // full cruise speed. For H1 this caps the whole straight; compare limits honestly.
      constraints.setMaxVelocityMetersPerSec(
          new Path.RangedConstraint(
              AutoConstants.BLINE_CORNER_MAX_SPEED_METERS_PER_SECOND, 1, anchors.length - 2),
          finalApproachLimit);
    }
    return new Path(elements, constraints, new Path.DefaultGlobalConstraints(
        AutoConstants.HOLONOMIC_TEST_CONSTRAINTS.maxVelocityMPS(),
        AutoConstants.HOLONOMIC_TEST_CONSTRAINTS.maxAccelerationMPSSq(),
        Math.toDegrees(AutoConstants.HOLONOMIC_TEST_CONSTRAINTS.maxAngularVelocityRadPerSec()),
        Math.toDegrees(AutoConstants.HOLONOMIC_TEST_CONSTRAINTS.maxAngularAccelerationRadPerSecSq()),
        AutoConstants.PRECISION_TRANSLATION_TOLERANCE_METERS,
        AutoConstants.PRECISION_ROTATION_TOLERANCE_DEGREES,
        AutoConstants.BLINE_INTERMEDIATE_HANDOFF_RADIUS_METERS));
  }

  /** Final expanded translation index used to prevent handoff before the last leg. */
  public static int finalTranslationIndex(Path path) {
    var elements = path.getPathElementsWithConstraintsNoWaypoints();
    for (int i = elements.size() - 1; i >= 0; i--) {
      if (elements.get(i).getFirst() instanceof Path.TranslationTarget) {
        return i;
      }
    }
    throw new IllegalArgumentException("BLine route has no translation target");
  }
}
