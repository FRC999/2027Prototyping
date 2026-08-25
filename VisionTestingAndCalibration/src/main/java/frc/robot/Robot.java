package frc.robot;

import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.util.RotatingWPILOGWriter;

/**
 * Main robot lifecycle for the vision/localization prototype.
 *
 * <p>Idea traceability:
 *
 * <p>- 6328 AdvantageKit ecosystem: extend {@link LoggedRobot}, start the logger before creating
 * subsystems, write WPILOG files, and publish NT4 data so AdvantageScope can inspect live and
 * replayed behavior.
 *
 * <p>- WPILib 2026 command framework: schedule and cancel autonomous commands through
 * {@link CommandScheduler} rather than deprecated command instance helpers.
 */
public class Robot extends LoggedRobot {
  private Command autonomousCommand;
  private RobotContainer robotContainer;
  private final RotatingWPILOGWriter logWriter;

  public Robot() {
    Logger.recordMetadata("ProjectName", "VisionTestingAndCalibration");
    Logger.recordMetadata("Team", "999 MechaRAMS");
    Logger.recordMetadata("Architecture", "2025 CTRE swerve + PhotonVision + AdvantageKit");

    /*
     * Real robot logs go to the roboRIO filesystem. Simulation logs stay inside the project so
     * desktop tests can be replayed without pulling files from the robot.
     */
    logWriter = new RotatingWPILOGWriter(isReal() ? "/home/lvuser/logs" : "logs/sim");
    Logger.addDataReceiver(logWriter);
    Logger.addDataReceiver(new NT4Publisher());

    Logger.start();
    robotContainer = new RobotContainer();
    SmartDashboard.putData(
        "Close Current Log And Start New (Disabled Only)",
        Commands.runOnce(this::requestLogRotation).ignoringDisable(true));
    SmartDashboard.putData(
        "Delete Stored Logs And Start Fresh (Disabled Only)",
        Commands.runOnce(this::requestLogPurge).ignoringDisable(true));
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    Logger.recordOutput("Logging/RotationPending", logWriter.isRotationPending());
    Logger.recordOutput("Logging/PurgePending", logWriter.isPurgePending());
    Logger.recordOutput("Logging/RotationCount", logWriter.getRotationCount());
    Logger.recordOutput("Logging/PurgeCount", logWriter.getPurgeCount());
    Logger.recordOutput(
        "Logging/LastRotationTimestampSeconds",
        logWriter.getLastRotationTimestampMicros() / 1_000_000.0);
    Logger.recordOutput(
        "Logging/LastPurgeTimestampSeconds",
        logWriter.getLastPurgeTimestampMicros() / 1_000_000.0);
    Logger.recordOutput("Logging/LastRotationError", logWriter.getLastError());
    Logger.recordOutput("Logging/ActiveLogPath", logWriter.getActiveLogPath());
  }

  private void requestLogRotation() {
    if (!DriverStation.isDisabled()) {
      DriverStation.reportWarning("Log rotation rejected: disable the robot first.", false);
      return;
    }

    if (logWriter.requestRotation()) {
      DriverStation.reportWarning(
          "Starting a new WPILOG and closing the previous one in the background. "
              + "Wait for Logging/RotationPending=false.",
          false);
    } else {
      DriverStation.reportWarning("Log rotation is already pending.", false);
    }
  }

  private void requestLogPurge() {
    if (!DriverStation.isDisabled()) {
      DriverStation.reportWarning("Log purge rejected: disable the robot first.", false);
      return;
    }

    if (logWriter.requestPurge()) {
      DriverStation.reportWarning(
          "Temporarily detaching file logging, deleting all files and subdirectories in the log "
              + "folder, then starting a fresh WPILOG in the background. "
              + "Wait for Logging/PurgePending=false.",
          false);
    } else {
      DriverStation.reportWarning("A log rotation or purge is already pending.", false);
    }
  }

  @Override
  public void autonomousInit() {
    autonomousCommand = robotContainer.getAutonomousCommand();
    if (autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(autonomousCommand);
    }
  }

  @Override
  public void teleopInit() {
    if (autonomousCommand != null) {
      CommandScheduler.getInstance().cancel(autonomousCommand);
    }
  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }
}
