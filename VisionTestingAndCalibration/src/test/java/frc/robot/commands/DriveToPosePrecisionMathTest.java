package frc.robot.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Headless verification of the precision command's pure math. Pins the vector-clamp fix (per-axis
 * clamping would let a diagonal command exceed the configured max by up to sqrt(2)).
 */
class DriveToPosePrecisionMathTest {
  @Test
  void persistentLossOfTightPoseResumesCorrectionButBriefNoiseDoesNot() {
    var confirmation = new DriveToPosePrecisionCommand.SettleVelocityEscape(0.20);
    assertFalse(confirmation.update(true, true, 1.0));
    assertFalse(confirmation.update(true, true, 1.19));
    assertFalse(confirmation.update(true, false, 1.195));
    assertFalse(confirmation.isPending());
    assertFalse(confirmation.update(true, true, 2.0));
    assertTrue(confirmation.update(true, true, 2.21));
    assertFalse(confirmation.update(false, true, 2.22));
    assertFalse(confirmation.isPending());
    assertFalse(confirmation.update(true, true, 3.0));
    confirmation.reset();
    assertFalse(confirmation.isPending());
  }

  @Test
  void agedHoldCannotFinishWithPendingMotionOrFailedCurrentQualification() {
    assertFalse(DriveToPosePrecisionCommand.canFinishHold(true, true, true, true));
    assertFalse(DriveToPosePrecisionCommand.canFinishHold(true, false, false, true));
    assertFalse(DriveToPosePrecisionCommand.canFinishHold(false, true, false, true));
    assertFalse(DriveToPosePrecisionCommand.canFinishHold(true, true, false, false));
    assertTrue(DriveToPosePrecisionCommand.canFinishHold(true, true, false, true));
  }

  @Test
  void briefSpeedExcursionStaysPendingThenClearsWithoutReleasingHold() {
    var escape = new DriveToPosePrecisionCommand.SettleVelocityEscape(0.08);
    assertFalse(escape.update(true, true, 1.0));
    assertTrue(escape.isPending());
    assertFalse(escape.update(true, true, 1.06));
    assertFalse(escape.update(true, false, 1.07));
    assertFalse(escape.isPending());
    assertEquals(0.0, escape.elapsedSeconds(1.07));
  }

  @Test
  void sustainedSpeedExcursionReleasesHoldUsingActualElapsedTime() {
    var escape = new DriveToPosePrecisionCommand.SettleVelocityEscape(0.08);
    assertFalse(escape.update(true, true, 1.0));
    assertTrue(escape.update(true, true, 1.09));
    assertEquals(0.09, escape.elapsedSeconds(1.09), 1e-9);
  }

  @Test
  void separatedSpikesDoNotAccumulateAndInactiveHoldClearsConfirmation() {
    var escape = new DriveToPosePrecisionCommand.SettleVelocityEscape(0.08);
    assertFalse(escape.update(true, true, 1.0));
    assertFalse(escape.update(true, false, 1.05));
    assertFalse(escape.update(true, true, 1.06));
    assertFalse(escape.update(true, true, 1.12));
    assertFalse(escape.update(false, true, 1.20));
    assertFalse(escape.isPending());
    assertFalse(escape.update(true, true, 1.21));
    escape.reset();
    assertFalse(escape.isPending());
  }

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

  @Test
  void feedforwardFadeIsZeroAtAndInsideTargetRadius() {
    assertEquals(0.0, DriveToPosePrecisionCommand.feedforwardScaleForDistance(0.0, 0.04, 0.35), 1e-9);
    assertEquals(0.0, DriveToPosePrecisionCommand.feedforwardScaleForDistance(0.04, 0.04, 0.35), 1e-9);
  }

  @Test
  void feedforwardFadeIsLinearBetweenRadii() {
    assertEquals(0.5, DriveToPosePrecisionCommand.feedforwardScaleForDistance(0.195, 0.04, 0.35), 1e-9);
  }

  @Test
  void feedforwardFadeIsOneOutsideMaxRadius() {
    assertEquals(1.0, DriveToPosePrecisionCommand.feedforwardScaleForDistance(0.35, 0.04, 0.35), 1e-9);
    assertEquals(1.0, DriveToPosePrecisionCommand.feedforwardScaleForDistance(2.0, 0.04, 0.35), 1e-9);
  }

  @Test
  void velocityDampingOpposesMotionAndRespectsScale() {
    assertEquals(-0.225, DriveToPosePrecisionCommand.velocityDamping(1.0, 0.45, 0.5), 1e-9);
    assertEquals(0.225, DriveToPosePrecisionCommand.velocityDamping(-1.0, 0.45, 0.5), 1e-9);
    assertEquals(0.0, DriveToPosePrecisionCommand.velocityDamping(1.0, 0.45, 0.0), 1e-9);
  }

  @Test
  void settleEscapeIgnoresOrdinaryToleranceNoise() {
    assertFalse(DriveToPosePrecisionCommand.exceedsSettleEscapeTolerance(0.05, 2.0, 0.06, 2.5));
  }

  @Test
  void settleEscapeReleasesForLargeTranslationOrRotationError() {
    assertTrue(DriveToPosePrecisionCommand.exceedsSettleEscapeTolerance(0.061, 0.0, 0.06, 2.5));
    assertTrue(DriveToPosePrecisionCommand.exceedsSettleEscapeTolerance(0.0, 2.51, 0.06, 2.5));
  }

  @Test
  void settleVelocityEscapeIgnoresMotionInsideWiderEnvelope() {
    assertFalse(
        DriveToPosePrecisionCommand.exceedsSettleEscapeVelocityTolerance(
            0.18, 12.0, 0.18, 12.0));
  }

  @Test
  void settleVelocityEscapeReleasesForRenewedTranslationOrRotation() {
    assertTrue(
        DriveToPosePrecisionCommand.exceedsSettleEscapeVelocityTolerance(
            0.181, 0.0, 0.18, 12.0));
    assertTrue(
        DriveToPosePrecisionCommand.exceedsSettleEscapeVelocityTolerance(
            0.0, -12.01, 0.18, 12.0));
  }
}
