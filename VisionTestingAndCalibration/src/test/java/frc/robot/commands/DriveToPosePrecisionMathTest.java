package frc.robot.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Headless verification of the precision command's pure math. Pins the vector-clamp fix (per-axis
 * clamping would let a diagonal command exceed the configured max by up to sqrt(2)).
 */
class DriveToPosePrecisionMathTest {
  @Test
  void clampLeavesSubMaxVectorUnchanged() {
    double[] r = DriveToPosePrecisionCommand.clampTranslationToMax(0.3, 0.4, 1.0); // norm 0.5 < 1.0
    assertEquals(0.3, r[0], 1e-9);
    assertEquals(0.4, r[1], 1e-9);
  }

  @Test
  void clampScalesOversizedVectorToMaxNorm() {
    double[] r = DriveToPosePrecisionCommand.clampTranslationToMax(3.0, 4.0, 2.5); // norm 5 -> 2.5
    assertEquals(2.5, Math.hypot(r[0], r[1]), 1e-9);
  }

  @Test
  void clampBoundsDiagonalThatPerAxisWouldLeak() {
    // Per-axis clamping to max=1 would leave (1,1) -> norm sqrt(2)=1.414. Vector clamp must bound to 1.
    double[] r = DriveToPosePrecisionCommand.clampTranslationToMax(1.0, 1.0, 1.0);
    assertEquals(1.0, Math.hypot(r[0], r[1]), 1e-9);
  }
}
