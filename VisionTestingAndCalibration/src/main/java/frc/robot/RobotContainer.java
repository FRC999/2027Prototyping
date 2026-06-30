package frc.robot;

import com.ctre.phoenix6.Utils;
import com.pathplanner.lib.auto.AutoBuilder;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.VisionConstants;
import frc.robot.commands.AimAtGoalCommand;
import frc.robot.commands.DriveAndAimCommand;
import frc.robot.commands.DriveManuallyCommand;
import frc.robot.commands.DriveToPosePrecisionCommand;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.vision.VisionIO;
import frc.robot.subsystems.vision.VisionIOPhotonVision;
import frc.robot.subsystems.vision.VisionIOPhotonVisionSim;

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
  // Registered automatically with the CommandScheduler via SubsystemBase; held for lifetime + future
  // boresight aiming hooks (vision.getTargetX). Constructed for its periodic fusion side effect.
  @SuppressWarnings("unused")
  private final Vision vision = createVision();
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

    /*
     * Aiming (no turret/mechanism -- whole-chassis aim at the configurable virtual goal):
     *  - Right trigger held: drive normally (left stick) while the robot auto-faces the goal and leads
     *    its own motion (shoot-on-move). Idea: 1768 joystickDriveAtAngle / 6995 pose-fed aiming.
     *  - Right stick press: stationary "square up to the goal" with settle. Idea: 6995 atSetpoint gate.
     */
    driverController.rightTrigger().whileTrue(
        new DriveAndAimCommand(
            drive,
            () -> driverController.getLeftY(),
            () -> driverController.getLeftX(),
            () -> driverController.rightBumper().getAsBoolean()));
    driverController.rightStick().onTrue(new AimAtGoalCommand(drive));

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
    SmartDashboard.putData("Aim At Goal - Stationary", new AimAtGoalCommand(drive));
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
    /*
     * Coarse-then-precise handoff demo: run the timed PathPlanner path to get close, then finish on the
     * position-tolerance DriveToPosePrecisionCommand. Idea: 6328 DriveTrajectory.andThen(DriveToPose) --
     * a time-based path should never be what *finishes* a precise move.
     */
    autoChooser.addOption("VisionTest + Precision Handoff", Commands.defer(
        () -> {
          try {
            return AutoBuilder.buildAuto("VisionTest")
                .andThen(new DriveToPosePrecisionCommand(drive, TAG_BOARD_TEST_POSE));
          } catch (Exception ex) {
            return Commands.print("VisionTest handoff unavailable: " + ex.getMessage());
          }
        },
        java.util.Set.of(drive)));
    SmartDashboard.putData("Autonomous Mode", autoChooser);
  }

  public Command getAutonomousCommand() {
    return autoChooser.getSelected();
  }

  /**
   * Builds the vision subsystem with the correct IO layer for the current environment, and connects its
   * accepted observations to CTRE's pose estimator.
   *
   * <p>Idea traceability:
   *
   * <p>- AdvantageKit IO-layer pattern (6328 / 1768 template): pick {@link VisionIOPhotonVisionSim} in
   * simulation and {@link VisionIOPhotonVision} on the real robot, behind one {@link VisionIO} interface.
   * In simulation the sim IO is fed the true drivetrain pose so PhotonVision renders synthetic frames.
   *
   * <p>- BUG FIX (this review): PhotonVision timestamps are in the WPILib FPGA time base, but CTRE's
   * {@code SwerveDrivetrain} odometry buffer is on the Phoenix time base. They must be converted with
   * {@link Utils#fpgaToCurrentTime(double)} or every vision sample fuses against the wrong odometry
   * sample and latency compensation is silently broken. Codex's first pass passed the raw timestamp.
   */
  private Vision createVision() {
    Vision.VisionConsumer consumer =
        (pose, timestampSeconds, stdDevs) ->
            drive.addVisionMeasurement(pose, Utils.fpgaToCurrentTime(timestampSeconds), stdDevs);

    /*
     * ACTIVE CONFIG: 2 cameras on 1 Orange Pi -- the recommended starting point for the pilot. It is the
     * established-safe OPi5 budget and the simplest thing that proves the localization + precision + sim
     * pipeline. The fusion code is camera-count-agnostic (varargs), so scaling to 4 cameras / 2 Orange
     * Pis is just uncommenting the two BACK cameras below (their transforms + std-dev factors already
     * exist in VisionConstants). Camera index here must match VisionConstants.CAMERA_STD_DEV_FACTORS.
     */
    if (RobotBase.isSimulation()) {
      return new Vision(
          consumer,
          drive::getPose,
          new VisionIOPhotonVisionSim(
              VisionConstants.FRONT_LEFT_CAMERA_NAME,
              VisionConstants.ROBOT_TO_FRONT_LEFT_CAMERA,
              drive::getPose),
          new VisionIOPhotonVisionSim(
              VisionConstants.FRONT_RIGHT_CAMERA_NAME,
              VisionConstants.ROBOT_TO_FRONT_RIGHT_CAMERA,
              drive::getPose)
          // Scale to 4 cameras / 2 Orange Pis -- uncomment:
          // , new VisionIOPhotonVisionSim(
          //     VisionConstants.BACK_LEFT_CAMERA_NAME,
          //     VisionConstants.ROBOT_TO_BACK_LEFT_CAMERA, drive::getPose)
          // , new VisionIOPhotonVisionSim(
          //     VisionConstants.BACK_RIGHT_CAMERA_NAME,
          //     VisionConstants.ROBOT_TO_BACK_RIGHT_CAMERA, drive::getPose)
          );
    }

    return new Vision(
        consumer,
        drive::getPose,
        new VisionIOPhotonVision(
            VisionConstants.FRONT_LEFT_CAMERA_NAME, VisionConstants.ROBOT_TO_FRONT_LEFT_CAMERA),
        new VisionIOPhotonVision(
            VisionConstants.FRONT_RIGHT_CAMERA_NAME, VisionConstants.ROBOT_TO_FRONT_RIGHT_CAMERA)
        // Scale to 4 cameras / 2 Orange Pis -- uncomment:
        // , new VisionIOPhotonVision(
        //     VisionConstants.BACK_LEFT_CAMERA_NAME, VisionConstants.ROBOT_TO_BACK_LEFT_CAMERA)
        // , new VisionIOPhotonVision(
        //     VisionConstants.BACK_RIGHT_CAMERA_NAME, VisionConstants.ROBOT_TO_BACK_RIGHT_CAMERA)
        );
  }
}
