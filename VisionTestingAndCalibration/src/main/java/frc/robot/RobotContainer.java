package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.Constants.OperatorConstants;
import frc.robot.commands.DriveManuallyCommand;
import frc.robot.commands.DriveToPosePrecisionCommand;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.VisionSubsystem;

/**
 * Robot wiring: subsystems, operator controls, dashboard commands, and autonomous chooser.
 *
 * <p>Idea traceability:
 *
 * <p>- WPILib command-template pattern: keep construction and bindings in one place so a new
 * student can find "what button does what" without tracing through subsystem constructors.
 *
 * <p>- Precision-handoff autonomous pattern: expose both a direct final-pose command and a
 * PathPlanner auto entry. This lets the team test the final tolerance controller by itself before
 * embedding it after a generated path.
 *
 * <p>- AI/process templates from multiple teams: dashboard commands are intentionally named with
 * test intent so future AI sessions and human reviewers can connect a log file to the exact action
 * that created it.
 */
public class RobotContainer {
  private static final Pose2d START_POSE = new Pose2d(1.5, 2.0, Rotation2d.kZero);
  private static final Pose2d TAG_BOARD_TEST_POSE = new Pose2d(4.25, 2.0, Rotation2d.kZero);

  private final CommandXboxController driverController =
      new CommandXboxController(OperatorConstants.DRIVER_CONTROLLER_PORT);
  private final DriveSubsystem drive = DriveSubsystem.create();
  @SuppressWarnings("unused")
  private final VisionSubsystem vision = new VisionSubsystem(drive);
  private final SendableChooser<Command> autoChooser = new SendableChooser<>();

  public RobotContainer() {
    /*
     * Manual drive is the default command so every simulation/real run has an immediate safe
     * fallback: if no autonomous or test command owns the drivetrain, the driver controls it.
     */
    drive.setDefaultCommand(new DriveManuallyCommand(
        drive,
        () -> driverController.getLeftY(),
        () -> driverController.getLeftX(),
        () -> driverController.getRightX(),
        () -> driverController.rightBumper().getAsBoolean()));

    configureBindings();
    configureDashboard();
    configureAutos();
  }

  private void configureBindings() {
    /*
     * Bindings are arranged from simple bring-up controls to characterization controls:
     * pose reset/orientation seeding, precision target drive, hard stop, SysId selection, then SysId
     * execution. This matches the order used in ROBOT_CONTROLS.md.
     */
    driverController.a().onTrue(Commands.runOnce(() -> drive.resetPose(START_POSE), drive));
    driverController.b().onTrue(Commands.runOnce(drive::seedFieldRelativeBlueForward, drive));
    driverController.x().onTrue(new DriveToPosePrecisionCommand(drive, TAG_BOARD_TEST_POSE));
    driverController.y().whileTrue(Commands.run(drive::stop, drive));

    driverController.povUp().onTrue(drive.selectTranslationSysId());
    driverController.povRight().onTrue(drive.selectSteerSysId());
    driverController.povDown().onTrue(drive.selectRotationSysId());

    driverController.leftBumper().and(driverController.back())
        .whileTrue(drive.sysIdQuasistatic(SysIdRoutine.Direction.kReverse));
    driverController.leftBumper().and(driverController.start())
        .whileTrue(drive.sysIdQuasistatic(SysIdRoutine.Direction.kForward));
    driverController.leftTrigger().and(driverController.back())
        .whileTrue(drive.sysIdDynamic(SysIdRoutine.Direction.kReverse));
    driverController.leftTrigger().and(driverController.start())
        .whileTrue(drive.sysIdDynamic(SysIdRoutine.Direction.kForward));
  }

  private void configureDashboard() {
    /*
     * Dashboard commands duplicate the critical controller actions so the same tests can be run
     * from simulation, a driver laptop, or an AI-assisted checklist without needing an Xbox
     * controller connected.
     */
    SmartDashboard.putData("Reset Pose - Test Start", Commands.runOnce(() -> drive.resetPose(START_POSE), drive));
    SmartDashboard.putData("Precision Drive To Tag Board", new DriveToPosePrecisionCommand(drive, TAG_BOARD_TEST_POSE));
    SmartDashboard.putData("SysId Select Translation", drive.selectTranslationSysId());
    SmartDashboard.putData("SysId Select Steer", drive.selectSteerSysId());
    SmartDashboard.putData("SysId Select Rotation", drive.selectRotationSysId());
  }

  private void configureAutos() {
    /*
     * The PathPlanner auto is built lazily. A missing VisionTest auto should not crash robot
     * startup; it should produce an obvious dashboard/console message while the rest of the
     * prototype remains usable.
     */
    autoChooser.setDefaultOption("No Auto", Commands.none());
    autoChooser.addOption("Precision To Tag Board", new DriveToPosePrecisionCommand(drive, TAG_BOARD_TEST_POSE));
    autoChooser.addOption("PathPlanner Auto: VisionTest", Commands.defer(
        () -> {
          try {
            return AutoBuilder.buildAuto("VisionTest");
          } catch (Exception ex) {
            return Commands.print("PathPlanner auto VisionTest is not available: " + ex.getMessage());
          }
        },
        java.util.Set.of(drive)));
    SmartDashboard.putData("Autonomous Mode", autoChooser);
  }

  public Command getAutonomousCommand() {
    return autoChooser.getSelected();
  }
}
