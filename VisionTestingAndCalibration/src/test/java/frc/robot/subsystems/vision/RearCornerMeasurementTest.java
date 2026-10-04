package frc.robot.subsystems.vision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import org.junit.jupiter.api.Test;

class RearCornerMeasurementTest {
  @Test
  void translationAndBothRotationSignsRoundTripWithRectangularFrame() {
    for (double degrees : new double[] {-45, -20, 0, 20, 45}) {
      Pose2d expected = new Pose2d(1.25, -0.3, Rotation2d.fromDegrees(degrees));
      double[] readings = RearCornerMeasurement.cornerOffsets(expected, 0.7, 0.4);
      Pose2d result = RearCornerMeasurement.centerPose(readings[0], readings[1], readings[2], 0.7, 0.4);
      assertEquals(expected.getX(), result.getX(), 1e-12);
      assertEquals(expected.getY(), result.getY(), 1e-12);
      assertEquals(degrees, result.getRotation().getDegrees(), 1e-12);
    }
  }

  @Test
  void independentRearCornerExampleAndSigns() {
    Pose2d result = RearCornerMeasurement.centerPose(2.405, 2.2, 0.87, 0.6072, 0.3036);
    assertEquals(2.284673879, result.getX(), 1e-8);
    assertEquals(0.749673879, result.getY(), 1e-8);
    assertEquals(-19.73165, result.getRotation().getDegrees(), 1e-5);
    Pose2d straight = RearCornerMeasurement.centerPose(1, 1, 0.2, 0.6072, 0.3036);
    assertEquals(1, straight.getX(), 1e-12);
    assertEquals(0.2, straight.getY(), 1e-12);
    assertEquals(0, straight.getRotation().getDegrees(), 1e-12);
  }

  @Test
  void rejectsMissingGeometryNonFiniteAndOutOfRangeMeasurements() {
    for (double bad : new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
      assertThrows(IllegalArgumentException.class, () -> RearCornerMeasurement.centerPose(0, 0, 0, bad, 0.3));
    }
    for (double bad : new double[] {-1, Double.NaN, Double.POSITIVE_INFINITY}) {
      assertThrows(IllegalArgumentException.class, () -> RearCornerMeasurement.centerPose(0, 0, 0, 0.6, bad));
    }
    assertThrows(IllegalArgumentException.class, () -> RearCornerMeasurement.centerPose(Double.NaN, 0, 0, 0.6, 0.3));
    assertThrows(IllegalArgumentException.class, () -> RearCornerMeasurement.centerPose(0, 0, Double.POSITIVE_INFINITY, 0.6, 0.3));
    assertThrows(IllegalArgumentException.class, () -> RearCornerMeasurement.centerPose(0, 0.6, 0, 0.6, 0.3));
  }
}
