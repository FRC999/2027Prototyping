package frc.robot.commands;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.util.BLineTravelPolicy;
import frc.robot.util.BLineTravelPolicy.Outcome;

/**
 * Owns one BLine travel command until a qualified handoff or explicit failure.
 *
 * <p>BLine v0.9.2 natively finishes from pose alone and has no travel timeout.
 * This adapter retains our stop contract: only HANDOFF permits the final controller;
 * a stalled, invalid or geometrically unfinished route stops without claiming success.
 * Child lifecycle is delegated under this command's requirements, never separately
 * scheduled, so there is only one drivetrain owner during the transfer.
 */
public final class GuardedBLineTravelCommand extends Command {
  private final Command follower;
  private final BooleanSupplier localizationValid;
  private final BooleanSupplier safeHandoff;
  private final DoubleSupplier clock;
  private final Runnable stop;
  private final double timeoutSeconds;
  private double startSeconds;
  private Outcome outcome = Outcome.RUNNING;

  public GuardedBLineTravelCommand(
      Command follower,
      BooleanSupplier localizationValid,
      BooleanSupplier safeHandoff,
      double timeoutSeconds,
      DoubleSupplier clock,
      Runnable stop) {
    if (!Double.isFinite(timeoutSeconds) || timeoutSeconds <= 0.0) {
      throw new IllegalArgumentException("BLine timeout must be finite and positive");
    }
    this.follower = follower;
    this.localizationValid = localizationValid;
    this.safeHandoff = safeHandoff;
    this.timeoutSeconds = timeoutSeconds;
    this.clock = clock;
    this.stop = stop;
    addRequirements(follower.getRequirements().toArray(Subsystem[]::new));
    CommandScheduler.getInstance().registerComposedCommands(follower);
  }

  @Override
  public void initialize() {
    outcome = Outcome.RUNNING;
    startSeconds = clock.getAsDouble();
    follower.initialize();
    Logger.recordOutput("BLine/Travel/Outcome", outcome.name());
    Logger.recordOutput("BLine/Travel/Active", true);
    Logger.recordOutput("BLine/Travel/TimeoutSeconds", timeoutSeconds);
  }

  @Override
  public void execute() {
    double elapsed = clock.getAsDouble() - startSeconds;
    outcome = BLineTravelPolicy.evaluate(
        elapsed, timeoutSeconds, localizationValid.getAsBoolean(), false, false);
    if (outcome == Outcome.RUNNING) {
      follower.execute();
      outcome = BLineTravelPolicy.evaluate(
          clock.getAsDouble() - startSeconds, timeoutSeconds,
          localizationValid.getAsBoolean(), safeHandoff.getAsBoolean(), follower.isFinished());
    }
    Logger.recordOutput("BLine/Travel/ElapsedSeconds", elapsed);
    Logger.recordOutput("BLine/Travel/Outcome", outcome.name());
  }

  @Override
  public boolean isFinished() {
    return outcome != Outcome.RUNNING;
  }

  @Override
  public void end(boolean interrupted) {
    if (interrupted) {
      outcome = Outcome.INTERRUPTED;
    }
    // Even the successful transfer interrupts coarse travel early. BLine sends zero;
    // then retain measured angles. Precision seeds from measured speed on initialization.
    follower.end(true);
    stop.run();
    Logger.recordOutput("BLine/Travel/Active", false);
    Logger.recordOutput("BLine/Travel/Outcome", outcome.name());
  }

  public boolean canHandoff() {
    return outcome == Outcome.HANDOFF;
  }

  public Outcome getOutcome() {
    return outcome;
  }
}
