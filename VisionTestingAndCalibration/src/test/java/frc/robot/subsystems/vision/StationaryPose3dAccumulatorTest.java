package frc.robot.subsystems.vision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import org.junit.jupiter.api.Test;

class StationaryPose3dAccumulatorTest {
  @Test
  void validatesTargetAndRejectsEmptyMean() {
    assertThrows(IllegalArgumentException.class, () -> new StationaryPose3dAccumulator(0));
    assertThrows(IllegalStateException.class, () -> new StationaryPose3dAccumulator(2).mean());
  }

  @Test
  void retainsRollPitchAndMeanTranslationThenFreezes() {
    var stats = new StationaryPose3dAccumulator(2);
    Rotation3d orientation = new Rotation3d(0.02, -0.3, 0.4);
    assertFalse(stats.isReady());
    stats.add(new Pose3d(1, 2, 3, orientation));
    stats.add(new Pose3d(3, 4, 5, orientation));
    stats.add(new Pose3d(100, 100, 100, new Rotation3d()));
    assertTrue(stats.isReady());
    assertEquals(2, stats.count());
    assertEquals(2, stats.samples().length);
    assertEquals(2, stats.mean().getX(), 1e-12);
    assertEquals(3, stats.mean().getY(), 1e-12);
    assertEquals(4, stats.mean().getZ(), 1e-12);
    assertEquals(0.02, stats.mean().getRotation().getX(), 1e-12);
    assertEquals(-0.3, stats.mean().getRotation().getY(), 1e-12);
    assertEquals(0.4, stats.mean().getRotation().getZ(), 1e-12);
  }

  @Test
  void handlesYawWrapAndIgnoresNonFiniteTranslation() {
    var stats = new StationaryPose3dAccumulator(2);
    Pose3d invalid = new Pose3d(Double.NaN, 0, 0, new Rotation3d());
    assertFalse(StationaryPose3dAccumulator.isFinite(invalid));
    stats.add(invalid);
    assertEquals(0, stats.count());
    stats.add(new Pose3d(0, 0, 0, new Rotation3d(0, 0, Math.toRadians(179))));
    stats.add(new Pose3d(0, 0, 0, new Rotation3d(0, 0, Math.toRadians(-179))));
    assertEquals(Math.PI, Math.abs(stats.mean().getRotation().getZ()), 1e-12);
  }
}
