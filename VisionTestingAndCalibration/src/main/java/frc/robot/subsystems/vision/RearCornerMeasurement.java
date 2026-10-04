package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

/** Pure planar geometry. Displacements use axes aligned with the robot at the recorded start. */
public final class RearCornerMeasurement {
  private RearCornerMeasurement() {}

  /** Assumes a centered rectangular frame and a known turn within +/-60 degrees, not a full spin. */
  public static Pose2d centerPose(
      double leftX, double rightX, double rightY, double width, double centerToRear) {
    validate(width, centerToRear, leftX, rightX, rightY);
    double sine = (rightX - leftX) / width;
    if (Math.abs(sine) > Math.sin(Math.toRadians(60.0))) {
      throw new IllegalArgumentException("Measurements exceed the +/-60 degree small-turn range.");
    }
    double yaw = Math.asin(sine);
    double cosineMinusOne = Math.cos(yaw) - 1.0;
    return new Pose2d(
        (leftX + rightX) / 2.0 + centerToRear * cosineMinusOne,
        rightY + centerToRear * sine + width / 2.0 * cosineMinusOne,
        Rotation2d.fromRadians(yaw));
  }

  /** Predicted [rear-left X, rear-right X, rear-right Y] offsets, each from that corner's own mark. */
  public static double[] cornerOffsets(Pose2d relativeCenter, double width, double centerToRear) {
    validate(width, centerToRear, relativeCenter.getX(), relativeCenter.getY(),
        relativeCenter.getRotation().getRadians());
    double s = relativeCenter.getRotation().getSin();
    double c = relativeCenter.getRotation().getCos() - 1.0;
    return new double[] {
      relativeCenter.getX() - centerToRear * c - width / 2.0 * s,
      relativeCenter.getX() - centerToRear * c + width / 2.0 * s,
      relativeCenter.getY() - centerToRear * s - width / 2.0 * c
    };
  }

  private static void validate(double width, double rear, double... values) {
    if (!Double.isFinite(width) || !Double.isFinite(rear) || width <= 0.0 || rear < 0.0) {
      throw new IllegalArgumentException("Frame width must be positive and center-to-rear nonnegative.");
    }
    for (double value : values) {
      if (!Double.isFinite(value)) {
        throw new IllegalArgumentException("Enter all three finite corner offsets in meters.");
      }
    }
  }
}
