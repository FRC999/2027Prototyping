package frc.robot.commands;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.util.BLineTravelPolicy.Outcome;
import org.junit.jupiter.api.Test;

class GuardedBLineTravelCommandTest {
  private static class Follower extends Command {
    int initialized;
    int executed;
    int ended;
    boolean finished;
    boolean interrupted;

    Follower(Subsystem drive) { addRequirements(drive); }
    @Override public void initialize() { initialized++; }
    @Override public void execute() { executed++; }
    @Override public boolean isFinished() { return finished; }
    @Override public void end(boolean interrupted) { ended++; this.interrupted = interrupted; }
  }

  private static class Rig {
    Subsystem drive = new Subsystem() {};
    Follower follower = new Follower(drive);
    double now;
    boolean valid = true;
    boolean handoff;
    int stopped;
    GuardedBLineTravelCommand guard = new GuardedBLineTravelCommand(
        follower, () -> valid, () -> handoff, 8, () -> now, () -> stopped++);
  }

  @Test
  void successfulTransferKeepsRequirementsAndEndsCoarseExactlyOnce() {
    Rig rig = new Rig();
    assertTrue(rig.guard.getRequirements().contains(rig.drive));
    rig.guard.initialize();
    rig.now = 1;
    rig.guard.execute();
    assertFalse(rig.guard.isFinished());
    rig.handoff = true;
    rig.guard.execute();
    assertTrue(rig.guard.canHandoff());
    rig.guard.end(false);
    assertEquals(1, rig.follower.initialized);
    assertEquals(2, rig.follower.executed);
    assertEquals(1, rig.follower.ended);
    assertTrue(rig.follower.interrupted);
    assertEquals(1, rig.stopped);
  }

  @Test
  void invalidStateAndTimeoutStopBeforeSendingAnotherDriveRequest() {
    Rig rig = new Rig();
    rig.guard.initialize();
    rig.valid = false;
    rig.guard.execute();
    assertEquals(Outcome.INVALID_STATE, rig.guard.getOutcome());
    assertEquals(0, rig.follower.executed);
    rig.guard.end(false);
    assertEquals(1, rig.stopped);
    rig.valid = true;
    rig.guard.initialize();
    rig.handoff = true;
    rig.now = 8;
    rig.guard.execute();
    assertEquals(Outcome.TRAVEL_TIMEOUT, rig.guard.getOutcome());
    assertFalse(rig.guard.canHandoff());
    assertEquals(0, rig.follower.executed);
    rig.guard.end(false);
    assertEquals(2, rig.stopped);
  }

  @Test
  void nativePoseOnlyCompletionCannotStartPrecisionOutsideHandoffGeometry() {
    Rig rig = new Rig();
    rig.follower.finished = true;
    rig.guard.initialize();
    rig.guard.execute();
    assertEquals(Outcome.UNSAFE_COARSE_END, rig.guard.getOutcome());
    assertFalse(rig.guard.canHandoff());
  }

  @Test
  void disableOrCancellationIsAnInterruptionAndReuseClearsStaleOutcome() {
    Rig rig = new Rig();
    rig.guard.initialize();
    rig.guard.end(true);
    assertEquals(Outcome.INTERRUPTED, rig.guard.getOutcome());
    assertFalse(rig.guard.canHandoff());
    rig.guard.initialize();
    assertEquals(Outcome.RUNNING, rig.guard.getOutcome());
    assertFalse(rig.guard.isFinished());
  }

  @Test
  void rejectsUnboundedTimeoutConfiguration() {
    Rig rig = new Rig();
    assertThrows(IllegalArgumentException.class, () -> new GuardedBLineTravelCommand(
        new Follower(rig.drive), () -> true, () -> true, Double.NaN, () -> 0, () -> {}));
    assertThrows(IllegalArgumentException.class, () -> new GuardedBLineTravelCommand(
        new Follower(rig.drive), () -> true, () -> true, 0, () -> 0, () -> {}));
  }
}
