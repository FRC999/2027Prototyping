package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Quaternion;
import edu.wpi.first.math.geometry.Rotation3d;
import java.util.ArrayList;
import java.util.List;

/** Bounded, diagnostic-only 3D mean of a compact stationary cluster; not an estimator. */
public final class StationaryPose3dAccumulator {
  private final int target;
  private final List<Pose3d> samples = new ArrayList<>();
  private final double[] sum = new double[7];
  private Quaternion reference;

  public StationaryPose3dAccumulator(int target) {
    if (target <= 0) throw new IllegalArgumentException("Positive sample target required.");
    this.target = target;
  }

  public static boolean isFinite(Pose3d p) {
    Quaternion q = p.getRotation().getQuaternion();
    return Double.isFinite(p.getX()) && Double.isFinite(p.getY()) && Double.isFinite(p.getZ())
        && Double.isFinite(q.getW()) && Double.isFinite(q.getX())
        && Double.isFinite(q.getY()) && Double.isFinite(q.getZ());
  }

  public void add(Pose3d pose) {
    if (isReady() || !isFinite(pose)) return;
    Quaternion q = pose.getRotation().getQuaternion();
    if (reference == null) reference = q;
    double sign = q.getW() * reference.getW() + q.getX() * reference.getX()
        + q.getY() * reference.getY() + q.getZ() * reference.getZ() < 0 ? -1 : 1;
    sum[0] += pose.getX();
    sum[1] += pose.getY();
    sum[2] += pose.getZ();
    sum[3] += sign * q.getW();
    sum[4] += sign * q.getX();
    sum[5] += sign * q.getY();
    sum[6] += sign * q.getZ();
    samples.add(pose);
  }

  public int count() { return samples.size(); }
  public boolean isReady() { return count() == target; }
  public Pose3d[] samples() { return samples.toArray(Pose3d[]::new); }

  public Pose3d mean() {
    if (count() == 0) throw new IllegalStateException("No stationary samples.");
    return new Pose3d(sum[0] / count(), sum[1] / count(), sum[2] / count(),
        new Rotation3d(new Quaternion(sum[3], sum[4], sum[5], sum[6]).normalize()));
  }
}
