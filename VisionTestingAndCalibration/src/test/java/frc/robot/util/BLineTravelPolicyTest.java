package frc.robot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import frc.robot.util.BLineTravelPolicy.Outcome;

class BLineTravelPolicyTest {
  @Test
  void waitsForGeometryAndDoesNotTreatNativeFinishAsHandoff() {
    assertEquals(Outcome.RUNNING, BLineTravelPolicy.evaluate(1, 8, true, false, false));
    assertEquals(Outcome.UNSAFE_COARSE_END, BLineTravelPolicy.evaluate(1, 8, true, false, true));
    assertEquals(Outcome.HANDOFF, BLineTravelPolicy.evaluate(1, 8, true, true, false));
    assertEquals(Outcome.HANDOFF, BLineTravelPolicy.evaluate(1, 8, true, true, true));
  }

  @Test
  void timeoutCannotFallThroughIntoPrecisionEvenWithSimultaneousHandoff() {
    assertEquals(Outcome.TRAVEL_TIMEOUT, BLineTravelPolicy.evaluate(8, 8, true, true, true));
    assertEquals(Outcome.TRAVEL_TIMEOUT, BLineTravelPolicy.evaluate(9, 8, true, false, false));
  }

  @Test
  void invalidLocalizationOrClockBlocksHandoff() {
    assertEquals(Outcome.INVALID_STATE, BLineTravelPolicy.evaluate(1, 8, false, true, true));
    assertEquals(Outcome.INVALID_STATE, BLineTravelPolicy.evaluate(Double.NaN, 8, true, true, false));
    assertEquals(Outcome.INVALID_STATE, BLineTravelPolicy.evaluate(-1, 8, true, false, false));
    assertEquals(Outcome.INVALID_STATE,
        BLineTravelPolicy.evaluate(Double.POSITIVE_INFINITY, 8, true, false, false));
  }
}
