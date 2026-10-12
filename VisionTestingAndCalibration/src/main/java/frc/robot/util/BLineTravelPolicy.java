package frc.robot.util;

/**
 * Pure decisions for the optional BLine transit stage. Idea traceability: BLine
 * supplies position-driven progress; MechaRAMS supplies a bounded, geometry-qualified
 * transfer to its tested final-pose controller. Coarse completion alone is not success.
 */
public final class BLineTravelPolicy {
  private BLineTravelPolicy() {}

  public enum Outcome {
    RUNNING,
    HANDOFF,
    INVALID_STATE,
    TRAVEL_TIMEOUT,
    UNSAFE_COARSE_END,
    INTERRUPTED
  }

  public static Outcome evaluate(
      double elapsedSeconds,
      double timeoutSeconds,
      boolean localizationValid,
      boolean safeHandoff,
      boolean followerFinished) {
    if (!localizationValid || !Double.isFinite(elapsedSeconds) || elapsedSeconds < 0.0) {
      return Outcome.INVALID_STATE;
    }
    if (elapsedSeconds >= timeoutSeconds) {
      return Outcome.TRAVEL_TIMEOUT;
    }
    if (safeHandoff) {
      return Outcome.HANDOFF;
    }
    return followerFinished ? Outcome.UNSAFE_COARSE_END : Outcome.RUNNING;
  }
}
