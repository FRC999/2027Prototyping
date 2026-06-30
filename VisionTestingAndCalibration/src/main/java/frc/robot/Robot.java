package frc.robot;

import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

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

  public Robot() {
    Logger.recordMetadata("ProjectName", "VisionTestingAndCalibration");
    Logger.recordMetadata("Team", "999 MechaRAMS");
    Logger.recordMetadata("Architecture", "2025 CTRE swerve + PhotonVision + AdvantageKit");

    /*
     * Real robot logs go to the roboRIO filesystem. Simulation logs stay inside the project so
     * desktop tests can be replayed without pulling files from the robot.
     */
    if (isReal()) {
      Logger.addDataReceiver(new WPILOGWriter("/home/lvuser/logs"));
      Logger.addDataReceiver(new NT4Publisher());
    } else {
      Logger.addDataReceiver(new WPILOGWriter("logs/sim"));
      Logger.addDataReceiver(new NT4Publisher());
    }

    Logger.start();
    robotContainer = new RobotContainer();
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
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
