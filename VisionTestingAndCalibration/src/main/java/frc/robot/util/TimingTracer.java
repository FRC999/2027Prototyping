package frc.robot.util;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.wpilibj.Timer;

public final class TimingTracer {
  private final String name;
  private double startTime;

  public TimingTracer(String name) {
    this.name = name;
  }

  public void start() {
    startTime = Timer.getFPGATimestamp();
  }

  public void stopAndLog() {
    Logger.recordOutput("Timing/" + name + "Ms", (Timer.getFPGATimestamp() - startTime) * 1000.0);
  }
}
