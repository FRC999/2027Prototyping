package frc.robot;

import com.ctre.phoenix6.Utils;
import com.pathplanner.lib.auto.AutoBuilder;

import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
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
import frc.robot.commands.DriveToPosePrecisionCommand.YawPrecision;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.vision.VisionPolicy.CovarianceModel;
import frc.robot.subsystems.vision.VisionPolicy.SingleTagStrategy;
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
  // Registered automatically with the CommandScheduler via SubsystemBase. Referenced by the A/B autos
  // (single-tag strategy + covariance-model toggles) and by future boresight hooks (vision.getTargetX).
  private final Vision vision = createVision();
  // LoggedDashboardChooser (not SendableChooser) so the SELECTED auto name is written to the log every
  // loop -- the 2026-07-01 sim log could not tell which chooser option produced each auto period.
  private final LoggedDashboardChooser<Command> autoChooser =
      new LoggedDashboardChooser<>("Autonomous Mode");

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
    /*
     * Pose reset (A) and operator-perspective seed (B) are gated to NON-autonomous. The 2026-07-01 sim
     * log showed an A press during an enabled auto reset the pose mid-run and invalidated the trajectory;
     * these must only be usable in teleop/disabled.
     */
    driverController.a().and(() -> !DriverStation.isAutonomousEnabled())
        .onTrue(Commands.runOnce(() -> drive.resetPose(START_POSE), drive));
    driverController.b().and(() -> !DriverStation.isAutonomousEnabled())
        .onTrue(Commands.runOnce(drive::seedFieldRelativeBlueForward, drive));
    driverController.leftStick().and(() -> !DriverStation.isAutonomousEnabled())
        .onTrue(Commands.runOnce(this::seedPoseFromVision, drive).ignoringDisable(true));
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
    SmartDashboard.putData(
        "Seed Pose From Vision",
        Commands.runOnce(this::seedPoseFromVision, drive).ignoringDisable(true));
    SmartDashboard.putData("Precision Drive To Tag Board", new DriveToPosePrecisionCommand(drive, TAG_BOARD_TEST_POSE));
    SmartDashboard.putData("Aim At Goal - Stationary", new AimAtGoalCommand(drive));
    SmartDashboard.putData(
        "Start Camera Jitter Capture (Disabled Only)",
        Commands.runOnce(vision::startCameraJitterCapture).ignoringDisable(true));
    SmartDashboard.putData(
        "Stop Camera Jitter Capture (Disabled Only)",
        Commands.runOnce(vision::stopCameraJitterCapture).ignoringDisable(true));
    SmartDashboard.putData("SysId Select Translation", drive.selectTranslationSysId());
    SmartDashboard.putData("SysId Select Steer", drive.selectSteerSysId());
    SmartDashboard.putData("SysId Select Rotation", drive.selectRotationSysId());
  }

  /**
   * Prefixes a command with an explicit vision-mode configuration. EVERY chooser option (baseline and
   * experiment) goes through this, so each auto run states its own configuration and a mode left over
   * from a previous test can never contaminate a comparison run. The modes are also logged every loop
   * ({@code Vision/Modes/*}), so each log names the configuration that produced it.
   */
  private Command withVisionModes(
      SingleTagStrategy strategy, CovarianceModel covariance, Command command) {
    return Commands.runOnce(
            () -> {
              vision.setSingleTagStrategy(strategy);
              vision.setCovarianceModel(covariance);
            })
        .andThen(command);
  }

  /** Baseline modes: the validated 2026-06-30 behavior (PnP single-tag, isotropic covariance). */
  private Command withBaselineVisionModes(Command command) {
    return withVisionModes(SingleTagStrategy.PNP, CovarianceModel.ISOTROPIC, command);
  }

  private void configureAutos() {
    /*
     * The PathPlanner auto is built lazily. A missing VisionTest auto should not crash robot
     * startup; it should produce an obvious dashboard/console message while the rest of the
     * prototype remains usable.
     */
    autoChooser.addDefaultOption("No Auto", Commands.none());

    /*
     * CURRENT-POSE FORWARD TESTS: each deferred command snapshots the fused drivetrain pose only when
     * autonomous starts. It does not reset odometry and does not assume the robot was placed at
     * START_POSE. All variants move in field +X, preserve the captured Y coordinate, and correct the
     * final heading to 0 deg. The identical motions across the four vision configurations make the
     * resulting DriveToPose/* and Vision/* logs directly comparable.
     */
    addRelativeForwardAutos(1.0, "1m");
    addRelativeForwardAutos(2.0, "2m");

    autoChooser.addOption("Precision To Tag Board",
        withBaselineVisionModes(new DriveToPosePrecisionCommand(drive, TAG_BOARD_TEST_POSE)));
    autoChooser.addOption("PathPlanner Auto: VisionTest", withBaselineVisionModes(Commands.defer(
        () -> {
          try {
            return AutoBuilder.buildAuto("VisionTest");
          } catch (Exception ex) {
            return Commands.print("PathPlanner auto VisionTest is not available: " + ex.getMessage());
          }
        },
        java.util.Set.of(drive))));
    /*
     * Sequential: run the FULL timed path, then finish precisely. Simple, but the path still runs to its
     * time-based end before precision starts.
     */
    autoChooser.addOption("VisionTest then Precision (sequential)", withBaselineVisionModes(Commands.defer(
        () -> {
          try {
            return AutoBuilder.buildAuto("VisionTest")
                .andThen(new DriveToPosePrecisionCommand(drive, TAG_BOARD_TEST_POSE));
          } catch (Exception ex) {
            return Commands.print("VisionTest sequential unavailable: " + ex.getMessage());
          }
        },
        java.util.Set.of(drive))));
    /*
     * Interrupting spatial handoff -- the actual 6328 pattern: bail out of the timed path as soon as the
     * robot crosses x = 3.3 m (the path ends near 3.6 m), then finish on the position-tolerance
     * controller. Exercises DriveToPosePrecisionCommand.handoffFrom(path, spatialCondition). A time-based
     * path should never be what *finishes* a precise move.
     */
    autoChooser.addOption("VisionTest (spatial handoff)",
        withBaselineVisionModes(spatialHandoffAuto("VisionTest")));

    /*
     * A/B EXPERIMENT AUTOS (2026-07-16 survey). Same motions as the baselines above; only the vision
     * configuration differs, so end-pose error and Vision/Summary/* channels compare directly:
     *
     *  - "TrigSolve": single-tag XY from camera-to-tag translation + odometry-buffer heading
     *    (SingleTagTrigSolver; idea 6328 / PhotonVision PNP_DISTANCE_TRIG_SOLVE / 1678 production).
     *  - "AnisoCov": 5940-style ray-aligned anisotropic covariance (PROVISIONAL coefficients until
     *    fitted from robot logs -- test plan stage R2).
     *
     * Run order + pass criteria: VISION_AND_TRAJECTORY_TEST_PLAN.md ("2026-07-16 A/B validation").
     */
    autoChooser.addOption("AB: Precision To Tag Board (TrigSolve)",
        withVisionModes(SingleTagStrategy.TRIG_SOLVE, CovarianceModel.ISOTROPIC,
            new DriveToPosePrecisionCommand(drive, TAG_BOARD_TEST_POSE)));
    autoChooser.addOption("AB: VisionTest spatial handoff (TrigSolve)",
        withVisionModes(SingleTagStrategy.TRIG_SOLVE, CovarianceModel.ISOTROPIC,
            spatialHandoffAuto("VisionTest")));
    autoChooser.addOption("AB: VisionTest spatial handoff (AnisoCov)",
        withVisionModes(SingleTagStrategy.PNP, CovarianceModel.ANISOTROPIC,
            spatialHandoffAuto("VisionTest")));
    autoChooser.addOption("AB: VisionTest spatial handoff (TrigSolve+AnisoCov)",
        withVisionModes(SingleTagStrategy.TRIG_SOLVE, CovarianceModel.ANISOTROPIC,
            spatialHandoffAuto("VisionTest")));

    /*
     * CURVED-TRAJECTORY variants (2026-07-16): "VisionTestCurved" is an S-curve (dips to y=1.25 with a
     * 25 deg mid-path rotation sweep, same start/end as the straight path) -- it exercises vision during
     * lateral motion + rotation, where camera views change and single-tag stretches appear. Same spatial
     * handoff (x > 3.3) and the same precision finish, so results compare 1:1 with the straight runs.
     * Exact run order: VISION_AND_TRAJECTORY_TEST_PLAN.md "Execution checklist".
     */
    autoChooser.addOption("VisionTestCurved (spatial handoff)",
        withBaselineVisionModes(spatialHandoffAuto("VisionTestCurved")));
    autoChooser.addOption("AB: Curved handoff (TrigSolve)",
        withVisionModes(SingleTagStrategy.TRIG_SOLVE, CovarianceModel.ISOTROPIC,
            spatialHandoffAuto("VisionTestCurved")));
    autoChooser.addOption("AB: Curved handoff (TrigSolve+AnisoCov)",
        withVisionModes(SingleTagStrategy.TRIG_SOLVE, CovarianceModel.ANISOTROPIC,
            spatialHandoffAuto("VisionTestCurved")));
    // LoggedDashboardChooser publishes itself to SmartDashboard/NT ("Autonomous Mode") and logs the
    // selected option name -- no separate SmartDashboard.putData needed.
  }

  /** Adds the four vision-algorithm variants for one current-pose-relative forward distance. */
  private void addRelativeForwardAutos(double distanceMeters, String distanceLabel) {
    autoChooser.addOption("Forward " + distanceLabel + " - PnP + Iso",
        withVisionModes(SingleTagStrategy.PNP, CovarianceModel.ISOTROPIC,
            relativeForwardPrecisionAuto(distanceMeters)));
    autoChooser.addOption("Forward " + distanceLabel + " - TrigSolve + Iso",
        withVisionModes(SingleTagStrategy.TRIG_SOLVE, CovarianceModel.ISOTROPIC,
            relativeForwardPrecisionAuto(distanceMeters)));
    autoChooser.addOption("Forward " + distanceLabel + " - PnP + Aniso",
        withVisionModes(SingleTagStrategy.PNP, CovarianceModel.ANISOTROPIC,
            relativeForwardPrecisionAuto(distanceMeters)));
    autoChooser.addOption("Forward " + distanceLabel + " - TrigSolve + Aniso",
        withVisionModes(SingleTagStrategy.TRIG_SOLVE, CovarianceModel.ANISOTROPIC,
            relativeForwardPrecisionAuto(distanceMeters)));
  }

  /** Resets the drivetrain pose from a fresh, accepted MultiTag camera estimate. */
  private void seedPoseFromVision() {
    var seedPose = vision.getFreshTrustedSeedPose();
    boolean succeeded = seedPose.isPresent();
    Logger.recordOutput("Vision/ManualSeed/Succeeded", succeeded);
    if (succeeded) {
      drive.resetPose(seedPose.get());
      Logger.recordOutput("Vision/ManualSeed/Pose", seedPose.get());
      DriverStation.reportWarning("Seeded drivetrain pose from fresh MultiTag vision.", false);
    } else {
      DriverStation.reportWarning(
          "Vision pose seed rejected: no fresh accepted MultiTag observation.", false);
    }
  }

  /**
   * Captures the current fused pose at command initialization and drives to a target exactly
   * {@code distanceMeters} farther along field +X. The deferred construction is the key: creating the
   * target during robot startup would silently turn this back into a predefined-start test.
   */
  private Command relativeForwardPrecisionAuto(double distanceMeters) {
    return Commands.defer(
        () -> {
          Pose2d start = drive.getPose();
          Pose2d target =
              new Pose2d(start.getX() + distanceMeters, start.getY(), Rotation2d.kZero);
          return new DriveToPosePrecisionCommand(drive, target, YawPrecision.RELAXED);
        },
        java.util.Set.of(drive));
  }

  /**
   * The primary competition pattern (decided 2026-07-01): coarse PathPlanner path, interrupted
   * spatially at x > 3.3 m, finished by the position-tolerance precision controller. Built lazily and
   * fault-tolerantly, shared by the baseline and all A/B chooser options. Parametrized by auto name
   * (2026-07-16) so the straight "VisionTest" and the curved "VisionTestCurved" transit paths share
   * the identical handoff + precision finish -- only the coarse trajectory differs.
   */
  private Command spatialHandoffAuto(String autoName) {
    return Commands.defer(
        () -> {
          try {
            Command path = AutoBuilder.buildAuto(autoName);
            return new DriveToPosePrecisionCommand(drive, TAG_BOARD_TEST_POSE)
                .handoffFrom(path, () -> drive.getPose().getX() > 3.3);
          } catch (Exception ex) {
            return Commands.print(autoName + " spatial handoff unavailable: " + ex.getMessage());
          }
        },
        java.util.Set.of(drive));
  }

  public Command getAutonomousCommand() {
    return autoChooser.get();
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
          drive::getLastResetTimeSeconds,
          drive::sampleHeadingAt,
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
        drive::getLastResetTimeSeconds,
        drive::sampleHeadingAt,
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
