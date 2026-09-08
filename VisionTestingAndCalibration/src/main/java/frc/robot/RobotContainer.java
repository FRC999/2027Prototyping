package frc.robot;

import com.ctre.phoenix6.Utils;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.FollowPathCommand;
import com.pathplanner.lib.path.ConstraintsZone;
import com.pathplanner.lib.path.GoalEndState;
import com.pathplanner.lib.path.IdealStartingState;
import com.pathplanner.lib.path.PathPlannerPath;
import com.pathplanner.lib.path.RotationTarget;

import java.util.ArrayList;
import java.util.List;

import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;
import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.robot.Constants.AutoConstants;
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

  private enum VisionTestFinishMode {
    COARSE_ONLY,
    SEQUENTIAL_PRECISION,
    SPATIAL_HANDOFF
  }

  private enum HolonomicTestMode {
    FORWARD_ENTRY,
    FORWARD_THEN_LEFT,
    DIAGONAL_OUTBOUND,
    DIAGONAL_WITH_YAW,
    OUT_AND_RETURN
  }

  /** Current-start poses shared by the pure safety checks and the deferred auto builder. */
  static record HolonomicTestTargets(
      Pose2d start, Pose2d entry, Pose2d left, Pose2d diagonal) {}

  /** One continuous PathPlanner leg and the robot pose expected at that leg's endpoint. */
  private static record HolonomicPathLeg(
      String phaseName, PathPlannerPath path, Pose2d endpoint) {}

  /** Complete generated route. Out-and-return has two legs because it must reverse at the far end. */
  private static record HolonomicRoutePlan(
      List<HolonomicPathLeg> legs, Pose2d finalTarget, Pose2d[] pathAnchors) {}

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
  private boolean pathPlannerWarmupComplete = false;

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
    initializeVisionTestStatus();
    DriveToPosePrecisionCommand.primeTelemetrySchema();
    schedulePathPlannerWarmup();
  }

  private void initializeVisionTestStatus() {
    Logger.recordOutput("PathPlanner/VisionTest/StartAccepted", false);
    Logger.recordOutput("PathPlanner/VisionTest/AbortReason", "NOT_RUN");
    Logger.recordOutput("PathPlanner/VisionTest/PathBuildError", "NONE");
    Logger.recordOutput("PathPlanner/Warmup/Complete", false);
    Logger.recordOutput("PathPlanner/Warmup/Status", "NOT_SCHEDULED");
    Logger.recordOutput("PathPlanner/HolonomicTest/StartAccepted", false);
    Logger.recordOutput("PathPlanner/HolonomicTest/AbortReason", "NOT_RUN");
    Logger.recordOutput("PathPlanner/HolonomicTest/PathBuildError", "NONE");
    Logger.recordOutput("PathPlanner/HolonomicTest/Mode", "NOT_RUN");
    Logger.recordOutput("PathPlanner/HolonomicTest/CurrentPhase", "NOT_RUNNING");
    Logger.recordOutput("PathPlanner/HolonomicTest/PhaseIndex", 0);
    Logger.recordOutput("PathPlanner/HolonomicTest/CurrentTargetPose", Pose2d.kZero);
    Logger.recordOutput("PathPlanner/HolonomicTest/PhaseStartPose", Pose2d.kZero);
    Logger.recordOutput("PathPlanner/HolonomicTest/PhaseEndPose", Pose2d.kZero);
    Logger.recordOutput("PathPlanner/HolonomicTest/MeasuredVisionStartPose", Pose2d.kZero);
    Logger.recordOutput("PathPlanner/HolonomicTest/NormalizedStartPose", Pose2d.kZero);
    Logger.recordOutput("PathPlanner/HolonomicTest/EntryPose", Pose2d.kZero);
    Logger.recordOutput("PathPlanner/HolonomicTest/LeftPose", Pose2d.kZero);
    Logger.recordOutput("PathPlanner/HolonomicTest/DiagonalPose", Pose2d.kZero);
    Logger.recordOutput("PathPlanner/HolonomicTest/FinalPose", Pose2d.kZero);
    Logger.recordOutput("PathPlanner/HolonomicTest/ExpectedPathLengthMeters", 0.0);
    Logger.recordOutput("PathPlanner/HolonomicTest/ExpectedNetDisplacementMeters", 0.0);
    Logger.recordOutput("PathPlanner/HolonomicTest/Completed", false);
    Logger.recordOutput("PathPlanner/HolonomicTest/Interrupted", false);
    Logger.recordOutput("PathPlanner/HolonomicTest/FinalPrecisionOnly", true);
    Logger.recordOutput("PathPlanner/HolonomicTest/ContinuousPathGeometry", true);
    Logger.recordOutput("PathPlanner/HolonomicTest/PathCount", 0);
    Logger.recordOutput("PathPlanner/HolonomicTest/ExpectedIntermediateStops", 0);
    Logger.recordOutput("PathPlanner/HolonomicTest/FinalHandoff/Armed", false);
    Logger.recordOutput("PathPlanner/HolonomicTest/FinalHandoff/Triggered", false);
    Logger.recordOutput("PathPlanner/HolonomicTest/FinalHandoff/DistanceMeters", 0.0);
    Logger.recordOutput("PathPlanner/HolonomicTest/PlannedRouteWaypoints", new Pose2d[] {});
    Logger.recordOutput("Vision/StaticTest/Selection", "PNP_ISOTROPIC");
    Logger.recordOutput("Vision/StaticTest/Accepted", false);
  }

  /**
   * Runs PathPlanner's official no-output warmup while disabled. This loads and exercises the path
   * follower before an autonomous command can move the robot, reducing first-use class/JIT stalls on
   * the roboRIO 1. The warmup command owns no subsystem and sends no drivetrain request.
   */
  private void schedulePathPlannerWarmup() {
    Logger.recordOutput("PathPlanner/Warmup/Status", "RUNNING");
    Command warmup =
        FollowPathCommand.warmupCommand()
            .finallyDo(
                interrupted -> {
                  pathPlannerWarmupComplete = !interrupted;
                  Logger.recordOutput("PathPlanner/Warmup/Complete", pathPlannerWarmupComplete);
                  Logger.recordOutput(
                      "PathPlanner/Warmup/Status", interrupted ? "INTERRUPTED" : "COMPLETE");
                });
    CommandScheduler.getInstance().schedule(warmup);
  }

  /** Publishes a no-motion start check so the operator can verify VisionTest before enabling. */
  public void logVisionTestStartPreflight() {
    var trustedStart = vision.getFreshTrustedSeedPose();
    boolean freshMultiTagAvailable = trustedStart.isPresent();
    boolean safeStart = freshMultiTagAvailable && isSafeVisionTestStart(trustedStart.get());
    boolean readyToEnable = DriverStation.isDisabled() && safeStart && pathPlannerWarmupComplete;
    String status;
    if (!DriverStation.isDisabled()) {
      status = "ROBOT_NOT_DISABLED";
    } else if (!pathPlannerWarmupComplete) {
      status = "PATHPLANNER_WARMUP_PENDING";
    } else if (!freshMultiTagAvailable) {
      status = "NO_FRESH_MULTITAG_START";
    } else if (!safeStart) {
      status = "START_OUTSIDE_TEST_AREA";
    } else {
      status = "READY";
    }

    Logger.recordOutput(
        "PathPlanner/VisionTest/Preflight/FreshMultiTagAvailable", freshMultiTagAvailable);
    Logger.recordOutput("PathPlanner/VisionTest/Preflight/SafeStart", safeStart);
    Logger.recordOutput("PathPlanner/VisionTest/Preflight/ReadyToEnable", readyToEnable);
    Logger.recordOutput("PathPlanner/VisionTest/Preflight/Status", status);
    Logger.recordOutput(
        "PathPlanner/VisionTest/Preflight/PathPlannerWarmupComplete",
        pathPlannerWarmupComplete);

    boolean safeHolonomicPlan = false;
    if (freshMultiTagAvailable) {
      safeHolonomicPlan =
          isSafeHolonomicTestStart(trustedStart.get());
    }
    Logger.recordOutput(
        "PathPlanner/HolonomicTest/Preflight/FreshMultiTagAvailable", freshMultiTagAvailable);
    Logger.recordOutput(
        "PathPlanner/HolonomicTest/Preflight/PathPlannerWarmupComplete",
        pathPlannerWarmupComplete);
    Logger.recordOutput(
        "PathPlanner/HolonomicTest/Preflight/SafeGeneratedTargets", safeHolonomicPlan);
    Logger.recordOutput(
        "PathPlanner/HolonomicTest/Preflight/ReadyToEnable",
        DriverStation.isDisabled()
            && freshMultiTagAvailable
            && safeHolonomicPlan
            && pathPlannerWarmupComplete);

    if (freshMultiTagAvailable) {
      Pose2d pose = trustedStart.get();
      Logger.recordOutput("PathPlanner/VisionTest/Preflight/RobotPose", pose);
      Logger.recordOutput(
          "PathPlanner/VisionTest/Preflight/ExpectedTotalTravelMeters",
          TAG_BOARD_TEST_POSE.getX() - pose.getX());
      Logger.recordOutput(
          "PathPlanner/VisionTest/Preflight/BoardDistanceFromRobotCenterMeters",
          VisionConstants.TAG_BOARD_X_METERS - pose.getX());
    }
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
    SmartDashboard.putData(
        "Static Localization - PnP + Iso (Disabled Only)",
        selectStaticVisionMode(SingleTagStrategy.PNP));
    SmartDashboard.putData(
        "Static Localization - TrigSolve + Iso (Disabled Only)",
        selectStaticVisionMode(SingleTagStrategy.TRIG_SOLVE));
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
    autoChooser.addOption(
        "PathPlanner Auto: VisionTest",
        withBaselineVisionModes(currentPoseVisionTestAuto(VisionTestFinishMode.COARSE_ONLY)));
    /*
     * Sequential: run the FULL timed path, then finish precisely. Simple, but the path still runs to its
     * time-based end before precision starts.
     */
    autoChooser.addOption(
        "VisionTest then Precision (sequential)",
        withBaselineVisionModes(
            currentPoseVisionTestAuto(VisionTestFinishMode.SEQUENTIAL_PRECISION)));
    /*
     * Interrupting spatial handoff -- the actual 6328 pattern: bail out of the timed path as soon as the
     * robot crosses x = 3.3 m (the path ends near 3.6 m), then finish on the position-tolerance
     * controller. Exercises DriveToPosePrecisionCommand.handoffFrom(path, spatialCondition). A time-based
     * path should never be what *finishes* a precise move.
     */
    autoChooser.addOption("VisionTest (spatial handoff)",
        withBaselineVisionModes(currentPoseVisionTestAuto(VisionTestFinishMode.SPATIAL_HANDOFF)));

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
            currentPoseVisionTestAuto(VisionTestFinishMode.SPATIAL_HANDOFF)));
    autoChooser.addOption("AB: VisionTest spatial handoff (AnisoCov)",
        withVisionModes(SingleTagStrategy.PNP, CovarianceModel.ANISOTROPIC,
            currentPoseVisionTestAuto(VisionTestFinishMode.SPATIAL_HANDOFF)));
    autoChooser.addOption("AB: VisionTest spatial handoff (TrigSolve+AnisoCov)",
        withVisionModes(SingleTagStrategy.TRIG_SOLVE, CovarianceModel.ANISOTROPIC,
            currentPoseVisionTestAuto(VisionTestFinishMode.SPATIAL_HANDOFF)));

    /*
     * CURRENT-START HOLONOMIC TESTS: these stay in the measured free area on robot-left (+field Y).
     * Every option gets a fresh trusted MultiTag start at autonomous initialization, generates all
     * targets relative to that start, and validates the complete route before moving. Each straight
     * segment stops at a zero-speed PathPlanner goal and then uses DriveToPose for the exact endpoint.
     * The old fixed-start VisionTestCurved options moved toward -Y and are intentionally no longer in
     * the chooser; their deploy files remain only as editing references.
     */
    autoChooser.addOption(
        "Holonomic 1 - Forward Entry",
        withBaselineVisionModes(currentPoseHolonomicTestAuto(HolonomicTestMode.FORWARD_ENTRY)));
    autoChooser.addOption(
        "Holonomic 2 - Forward Then Strafe Left",
        withBaselineVisionModes(
            currentPoseHolonomicTestAuto(HolonomicTestMode.FORWARD_THEN_LEFT)));
    autoChooser.addOption(
        "Holonomic 3 - Forward Then Diagonal Left",
        withBaselineVisionModes(
            currentPoseHolonomicTestAuto(HolonomicTestMode.DIAGONAL_OUTBOUND)));
    autoChooser.addOption(
        "Holonomic 4 - Diagonal With Camera-Facing Yaw",
        withBaselineVisionModes(
            currentPoseHolonomicTestAuto(HolonomicTestMode.DIAGONAL_WITH_YAW)));
    autoChooser.addOption(
        "Holonomic 5 - Out And Return To Start",
        withBaselineVisionModes(
            currentPoseHolonomicTestAuto(HolonomicTestMode.OUT_AND_RETURN)));
    // LoggedDashboardChooser publishes itself to SmartDashboard/NT ("Autonomous Mode") and logs the
    // selected option name -- no separate SmartDashboard.putData needed.
  }

  /** Selects a stationary localization mode without ever changing vision settings while enabled. */
  private Command selectStaticVisionMode(SingleTagStrategy strategy) {
    return Commands.runOnce(
            () -> {
              boolean accepted = DriverStation.isDisabled();
              Logger.recordOutput("Vision/StaticTest/Accepted", accepted);
              if (!accepted) {
                DriverStation.reportWarning(
                    "Static localization mode change rejected: disable the robot first.", false);
                return;
              }
              vision.setSingleTagStrategy(strategy);
              vision.setCovarianceModel(CovarianceModel.ISOTROPIC);
              Logger.recordOutput(
                  "Vision/StaticTest/Selection", strategy.name() + "_ISOTROPIC");
            })
        .ignoringDisable(true);
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
   * Captures the current fused XY at command initialization, normalizes the heading to zero, and
   * drives to a target exactly {@code distanceMeters} farther along field +X. These ruler calibration
   * runs begin with the chassis physically squared to the tag-board plane, so retaining a biased
   * camera-seeded yaw would command an artificial turn before driving straight. The deferred
   * construction is the key: creating the target during robot startup would silently turn this back
   * into a predefined-start test.
   */
  private Command relativeForwardPrecisionAuto(double distanceMeters) {
    return Commands.defer(
        () -> {
          Pose2d measuredStart = drive.getPose();
          Pose2d start = new Pose2d(measuredStart.getTranslation(), Rotation2d.kZero);
          Pose2d target =
              new Pose2d(start.getX() + distanceMeters, start.getY(), Rotation2d.kZero);
          Logger.recordOutput("DriveToPose/Calibration/MeasuredStartPose", measuredStart);
          Logger.recordOutput("DriveToPose/Calibration/NormalizedStartPose", start);
          Logger.recordOutput("DriveToPose/Calibration/HeadingNormalized", true);
          return Commands.runOnce(() -> drive.resetPose(start), drive)
              .andThen(new DriveToPosePrecisionCommand(drive, target, YawPrecision.RELAXED));
        },
        java.util.Set.of(drive));
  }

  /**
   * Builds the straight PathPlanner test from the fresh physical start instead of the legacy fixed
   * x=1.5 auto start. This prevents a vision correction from putting the estimator ahead of the
   * time-parameterized path and causing an initial reverse command.
   */
  private Command currentPoseVisionTestAuto(VisionTestFinishMode finishMode) {
    return Commands.defer(
        () -> {
          Logger.recordOutput("PathPlanner/VisionTest/FinishMode", finishMode.name());
          Logger.recordOutput("PathPlanner/VisionTest/StartAccepted", false);
          Logger.recordOutput("PathPlanner/VisionTest/AbortReason", "NONE");
          Logger.recordOutput("PathPlanner/VisionTest/PathBuildError", "NONE");

          if (!pathPlannerWarmupComplete) {
            return rejectedVisionTestStart("PATHPLANNER_WARMUP_INCOMPLETE");
          }

          var trustedStart = vision.getFreshTrustedSeedPose();
          if (trustedStart.isEmpty()) {
            return rejectedVisionTestStart("NO_FRESH_MULTITAG_START");
          }

          Pose2d measuredStart = trustedStart.get();
          Logger.recordOutput("PathPlanner/VisionTest/MeasuredVisionStartPose", measuredStart);
          Logger.recordOutput(
              "PathPlanner/VisionTest/BoardDistanceFromRobotCenterMeters",
              VisionConstants.TAG_BOARD_X_METERS - measuredStart.getX());
          if (!isSafeVisionTestStart(measuredStart)) {
            return rejectedVisionTestStart("START_OUTSIDE_TEST_AREA");
          }

          Pose2d normalizedStart =
              new Pose2d(measuredStart.getTranslation(), Rotation2d.kZero);
          Pose2d coarseEnd =
              new Pose2d(
                  AutoConstants.VISION_TEST_COARSE_END_X_METERS,
                  TAG_BOARD_TEST_POSE.getY(),
                  Rotation2d.kZero);
          double coarseGoalEndVelocityMetersPerSecond =
              finishMode == VisionTestFinishMode.SPATIAL_HANDOFF
                  ? AutoConstants.VISION_TEST_SPATIAL_HANDOFF_END_SPEED_METERS_PER_SECOND
                  : 0.0;
          try {
            PathPlannerPath path =
                new PathPlannerPath(
                    PathPlannerPath.waypointsFromPoses(normalizedStart, coarseEnd),
                    AutoConstants.CAUTIOUS_CONSTRAINTS,
                    new IdealStartingState(0.0, Rotation2d.kZero),
                    new GoalEndState(
                        coarseGoalEndVelocityMetersPerSecond, Rotation2d.kZero));
            path.preventFlipping = true;

            Logger.recordOutput("PathPlanner/VisionTest/NormalizedStartPose", normalizedStart);
            Logger.recordOutput("PathPlanner/VisionTest/CoarseEndPose", coarseEnd);
            Logger.recordOutput(
                "PathPlanner/VisionTest/CoarseGoalEndVelocityMetersPerSecond",
                coarseGoalEndVelocityMetersPerSecond);
            Logger.recordOutput("PathPlanner/VisionTest/PrecisionTargetPose", TAG_BOARD_TEST_POSE);
            Logger.recordOutput(
                "PathPlanner/VisionTest/ExpectedTotalTravelMeters",
                TAG_BOARD_TEST_POSE.getX() - normalizedStart.getX());
            Logger.recordOutput(
                "PathPlanner/VisionTest/ExpectedFinalCameraXDistanceToBoardMeters",
                VisionConstants.TAG_BOARD_X_METERS
                    - TAG_BOARD_TEST_POSE.getX()
                    - VisionConstants.ROBOT_TO_FRONT_LEFT_CAMERA.getX());
            Logger.recordOutput(
                "PathPlanner/VisionTest/GeneratedPath",
                path.getPathPoses().toArray(Pose2d[]::new));
            Logger.recordOutput("PathPlanner/VisionTest/StartAccepted", true);

            Command coarse =
                Commands.runOnce(() -> drive.resetPose(normalizedStart), drive)
                    .andThen(AutoBuilder.followPath(path));
            return switch (finishMode) {
              case COARSE_ONLY -> coarse;
              case SEQUENTIAL_PRECISION ->
                  coarse.andThen(new DriveToPosePrecisionCommand(drive, TAG_BOARD_TEST_POSE));
              case SPATIAL_HANDOFF ->
                  new DriveToPosePrecisionCommand(drive, TAG_BOARD_TEST_POSE)
                      .handoffFrom(
                          coarse,
                          () -> drive.getPose().getX()
                              > AutoConstants.VISION_TEST_HANDOFF_X_METERS);
            };
          } catch (Exception ex) {
            Logger.recordOutput("PathPlanner/VisionTest/PathBuildError", ex.toString());
            return rejectedVisionTestStart("PATH_BUILD_FAILED");
          }
        },
        java.util.Set.of(drive));
  }

  /**
   * Builds one of the measured-space holonomic routes from a fresh MultiTag pose. The path geometry
   * is relative to the robot's actual translation; only yaw is normalized because the physical test
   * setup squares the frame to the tag board before every run.
   */
  private Command currentPoseHolonomicTestAuto(HolonomicTestMode mode) {
    return Commands.defer(
        () -> {
          Logger.recordOutput("PathPlanner/HolonomicTest/Mode", mode.name());
          Logger.recordOutput("PathPlanner/HolonomicTest/StartAccepted", false);
          Logger.recordOutput("PathPlanner/HolonomicTest/AbortReason", "NONE");
          Logger.recordOutput("PathPlanner/HolonomicTest/PathBuildError", "NONE");
          Logger.recordOutput("PathPlanner/HolonomicTest/Completed", false);
          Logger.recordOutput("PathPlanner/HolonomicTest/Interrupted", false);
          Logger.recordOutput("PathPlanner/HolonomicTest/PathCount", 0);
          Logger.recordOutput("PathPlanner/HolonomicTest/ExpectedIntermediateStops", 0);
          Logger.recordOutput("PathPlanner/HolonomicTest/FinalHandoff/Armed", false);
          Logger.recordOutput("PathPlanner/HolonomicTest/FinalHandoff/Triggered", false);
          Logger.recordOutput("PathPlanner/HolonomicTest/FinalHandoff/DistanceMeters", 0.0);

          if (!pathPlannerWarmupComplete) {
            return rejectedHolonomicTestStart("PATHPLANNER_WARMUP_INCOMPLETE");
          }

          var trustedStart = vision.getFreshTrustedSeedPose();
          if (trustedStart.isEmpty()) {
            return rejectedHolonomicTestStart("NO_FRESH_MULTITAG_START");
          }

          Pose2d measuredStart = trustedStart.get();
          HolonomicTestTargets targets = createHolonomicTestTargets(measuredStart);
          Logger.recordOutput("PathPlanner/HolonomicTest/MeasuredVisionStartPose", measuredStart);
          logHolonomicTargets(targets);
          if (!isSafeHolonomicTestStart(measuredStart)) {
            return rejectedHolonomicTestStart("GENERATED_TARGET_OUTSIDE_TEST_AREA");
          }

          try {
            HolonomicRoutePlan routePlan = createHolonomicRoutePlan(mode, targets);
            List<Command> phases = new ArrayList<>();
            phases.add(
                loggedHolonomicPhase(
                    0,
                    "RESET_TO_TRUSTED_START",
                    targets.start(),
                    Commands.runOnce(() -> drive.resetPose(targets.start()), drive)));

            for (int legIndex = 0; legIndex < routePlan.legs().size(); legIndex++) {
              HolonomicPathLeg leg = routePlan.legs().get(legIndex);
              Command pathCommand =
                  loggedHolonomicPhase(
                      legIndex + 1,
                      leg.phaseName(),
                      leg.endpoint(),
                      AutoBuilder.followPath(leg.path()));
              boolean finalPathLeg = legIndex == routePlan.legs().size() - 1;
              phases.add(
                  finalPathLeg
                      ? createHolonomicFinalHandoff(
                          legIndex + 1,
                          pathCommand,
                          routePlan.finalTarget(),
                          mode == HolonomicTestMode.FORWARD_THEN_LEFT
                              ? AutoConstants.HOLONOMIC_FORWARD_THEN_LEFT_HANDOFF_DISTANCE_METERS
                              : AutoConstants.HOLONOMIC_FINAL_HANDOFF_DISTANCE_METERS)
                      : pathCommand);
            }

            double expectedPathLength =
                routePlan.legs().stream()
                    .mapToDouble(leg -> pathLengthMeters(leg.path()))
                    .sum();
            double expectedNetDisplacement =
                switch (mode) {
                  case FORWARD_ENTRY -> AutoConstants.HOLONOMIC_ENTRY_FORWARD_METERS;
                  case FORWARD_THEN_LEFT ->
                      Math.hypot(
                          AutoConstants.HOLONOMIC_ENTRY_FORWARD_METERS,
                          AutoConstants.HOLONOMIC_LEFT_SHIFT_METERS);
                  case DIAGONAL_OUTBOUND, DIAGONAL_WITH_YAW ->
                      targets.start().getTranslation().getDistance(targets.diagonal().getTranslation());
                  case OUT_AND_RETURN -> 0.0;
                };
            Logger.recordOutput(
                "PathPlanner/HolonomicTest/ExpectedPathLengthMeters", expectedPathLength);
            Logger.recordOutput(
                "PathPlanner/HolonomicTest/ExpectedNetDisplacementMeters",
                expectedNetDisplacement);
            Logger.recordOutput(
                "PathPlanner/HolonomicTest/PlannedRouteWaypoints",
                routePlan.pathAnchors());
            Logger.recordOutput(
                "PathPlanner/HolonomicTest/PathCount", routePlan.legs().size());
            Logger.recordOutput(
                "PathPlanner/HolonomicTest/ExpectedIntermediateStops",
                Math.max(0, routePlan.legs().size() - 1));
            Logger.recordOutput("PathPlanner/HolonomicTest/StartAccepted", true);

            Command route = Commands.sequence(phases.toArray(Command[]::new));
            return route.finallyDo(
                interrupted -> {
                  drive.stop();
                  Logger.recordOutput("PathPlanner/HolonomicTest/FinalPose", drive.getPose());
                  Logger.recordOutput("PathPlanner/HolonomicTest/PhaseEndPose", drive.getPose());
                  Logger.recordOutput("PathPlanner/HolonomicTest/Interrupted", interrupted);
                  Logger.recordOutput("PathPlanner/HolonomicTest/Completed", !interrupted);
                  Logger.recordOutput(
                      "PathPlanner/HolonomicTest/CurrentPhase",
                      interrupted ? "INTERRUPTED" : "COMPLETE");
                });
          } catch (Exception ex) {
            Logger.recordOutput("PathPlanner/HolonomicTest/PathBuildError", ex.toString());
            return rejectedHolonomicTestStart("PATH_BUILD_FAILED");
          }
        },
        java.util.Set.of(drive));
  }

  /**
   * Builds smooth PathPlanner geometry for the complete route. Forward/left and
   * forward/diagonal corners are rounded inside the tested free-space envelope instead of making
   * the robot stop and restart at the mathematical corner. Out-and-return is the only two-path
   * route because reversing direction at the far endpoint requires a real zero-speed turnaround.
   */
  private HolonomicRoutePlan createHolonomicRoutePlan(
      HolonomicTestMode mode, HolonomicTestTargets targets) {
    Pose2d[] outboundAnchors = outboundHolonomicPathAnchors(mode, targets);
    Pose2d finalTarget =
        switch (mode) {
          case FORWARD_ENTRY -> targets.entry();
          case FORWARD_THEN_LEFT -> targets.left();
          case DIAGONAL_OUTBOUND -> targets.diagonal();
          case DIAGONAL_WITH_YAW ->
              new Pose2d(
                  targets.diagonal().getTranslation(),
                  Rotation2d.fromDegrees(
                      AutoConstants.HOLONOMIC_CAMERA_FACING_END_YAW_DEGREES));
          case OUT_AND_RETURN -> targets.start();
        };

    if (mode != HolonomicTestMode.OUT_AND_RETURN) {
      List<RotationTarget> rotationTargets =
          mode == HolonomicTestMode.DIAGONAL_WITH_YAW
              ? List.of(new RotationTarget(outboundAnchors.length - 2.0, Rotation2d.kZero))
              : List.of();
      PathPlannerPath path =
          createContinuousHolonomicPath(
              outboundAnchors,
              rotationTargets,
              List.of(),
              finalTarget.getRotation(),
              AutoConstants.HOLONOMIC_FINAL_HANDOFF_END_SPEED_METERS_PER_SECOND);
      return new HolonomicRoutePlan(
          List.of(new HolonomicPathLeg("CONTINUOUS_PATH", path, finalTarget)),
          finalTarget,
          outboundAnchors);
    }

    PathPlannerPath outbound =
        createContinuousHolonomicPath(
            outboundAnchors, List.of(), List.of(), Rotation2d.kZero, 0.0);
    Pose2d[] returnAnchors = reversePathAnchors(outboundAnchors);
    PathPlannerPath returning =
        createContinuousHolonomicPath(
            returnAnchors,
            List.of(),
            List.of(
                new ConstraintsZone(
                    returnAnchors.length - 2.0,
                    returnAnchors.length - 1.0,
                    AutoConstants.HOLONOMIC_RETURN_APPROACH_CONSTRAINTS)),
            Rotation2d.kZero,
            AutoConstants.HOLONOMIC_RETURN_HANDOFF_END_SPEED_METERS_PER_SECOND);
    Pose2d[] allAnchors = new Pose2d[outboundAnchors.length + returnAnchors.length - 1];
    System.arraycopy(outboundAnchors, 0, allAnchors, 0, outboundAnchors.length);
    System.arraycopy(
        returnAnchors,
        1,
        allAnchors,
        outboundAnchors.length,
        returnAnchors.length - 1);
    return new HolonomicRoutePlan(
        List.of(
            new HolonomicPathLeg(
                "OUTBOUND_CONTINUOUS_PATH", outbound, targets.diagonal()),
            new HolonomicPathLeg("RETURN_CONTINUOUS_PATH", returning, targets.start())),
        targets.start(),
        allAnchors);
  }

  private PathPlannerPath createContinuousHolonomicPath(
      Pose2d[] pathAnchors,
      List<RotationTarget> rotationTargets,
      List<ConstraintsZone> constraintZones,
      Rotation2d finalRobotRotation,
      double goalEndVelocityMetersPerSecond) {
    PathPlannerPath path =
        new PathPlannerPath(
            PathPlannerPath.waypointsFromPoses(pathAnchors),
            rotationTargets,
            List.of(),
            constraintZones,
            List.of(),
            AutoConstants.HOLONOMIC_TEST_CONSTRAINTS,
            new IdealStartingState(0.0, Rotation2d.kZero),
            new GoalEndState(goalEndVelocityMetersPerSecond, finalRobotRotation),
            false);
    path.preventFlipping = true;
    return path;
  }

  private Command createHolonomicFinalHandoff(
      int pathPhaseIndex,
      Command pathCommand,
      Pose2d finalTarget,
      double handoffDistanceMeters) {
    boolean[] handoffArmed = {false};
    boolean[] handoffLogged = {false};
    java.util.function.BooleanSupplier handoffCondition =
        () -> {
          double distance =
              drive.getPose().getTranslation().getDistance(finalTarget.getTranslation());
          if (distance >= AutoConstants.HOLONOMIC_FINAL_HANDOFF_ARM_DISTANCE_METERS) {
            handoffArmed[0] = true;
          }
          boolean triggered =
              handoffArmed[0]
                  && distance <= handoffDistanceMeters;
          Logger.recordOutput("PathPlanner/HolonomicTest/FinalHandoff/Armed", handoffArmed[0]);
          Logger.recordOutput("PathPlanner/HolonomicTest/FinalHandoff/Triggered", triggered);
          Logger.recordOutput("PathPlanner/HolonomicTest/FinalHandoff/DistanceMeters", distance);
          if (triggered && !handoffLogged[0]) {
            handoffLogged[0] = true;
            Logger.recordOutput("PathPlanner/HolonomicTest/PhaseIndex", pathPhaseIndex + 1);
            Logger.recordOutput("PathPlanner/HolonomicTest/CurrentPhase", "FINAL_PRECISION");
            Logger.recordOutput("PathPlanner/HolonomicTest/CurrentTargetPose", finalTarget);
            Logger.recordOutput("PathPlanner/HolonomicTest/PhaseStartPose", drive.getPose());
          }
          return triggered;
        };

    DriveToPosePrecisionCommand precision =
        new DriveToPosePrecisionCommand(drive, finalTarget, YawPrecision.PRECISE);
    return precision.handoffFrom(pathCommand, handoffCondition);
  }

  private static Pose2d[] outboundHolonomicPathAnchors(
      HolonomicTestMode mode, HolonomicTestTargets targets) {
    if (mode == HolonomicTestMode.FORWARD_ENTRY) {
      return new Pose2d[] {
        pathAnchor(targets.start().getTranslation(), 0.0),
        pathAnchor(targets.entry().getTranslation(), 0.0)
      };
    }

    double radius = AutoConstants.HOLONOMIC_CORNER_RADIUS_METERS;
    Pose2d beforeCorner =
        pathAnchor(
            new Translation2d(targets.entry().getX() - radius, targets.entry().getY()),
            0.0);
    if (mode == HolonomicTestMode.FORWARD_THEN_LEFT) {
      Pose2d afterCorner =
          pathAnchor(
              new Translation2d(targets.entry().getX(), targets.entry().getY() + radius),
              90.0);
      return new Pose2d[] {
        pathAnchor(targets.start().getTranslation(), 0.0),
        beforeCorner,
        afterCorner,
        pathAnchor(targets.left().getTranslation(), 90.0)
      };
    }

    double diagonalOffset = radius / Math.sqrt(2.0);
    Pose2d afterCorner =
        pathAnchor(
            new Translation2d(
                targets.entry().getX() + diagonalOffset,
                targets.entry().getY() + diagonalOffset),
            45.0);
    return new Pose2d[] {
      pathAnchor(targets.start().getTranslation(), 0.0),
      beforeCorner,
      afterCorner,
      pathAnchor(targets.diagonal().getTranslation(), 45.0)
    };
  }

  private static Pose2d[] reversePathAnchors(Pose2d[] forwardAnchors) {
    Pose2d[] reversed = new Pose2d[forwardAnchors.length];
    for (int i = 0; i < forwardAnchors.length; i++) {
      Pose2d forward = forwardAnchors[forwardAnchors.length - 1 - i];
      reversed[i] =
          new Pose2d(
              forward.getTranslation(),
              forward.getRotation().plus(Rotation2d.fromDegrees(180.0)));
    }
    return reversed;
  }

  private static Pose2d pathAnchor(Translation2d translation, double headingDegrees) {
    return new Pose2d(translation, Rotation2d.fromDegrees(headingDegrees));
  }

  private static double pathLengthMeters(PathPlannerPath path) {
    var points = path.getAllPathPoints();
    return points.isEmpty() ? 0.0 : points.get(points.size() - 1).distanceAlongPath;
  }

  private Command loggedHolonomicPhase(
      int phaseIndex, String phaseName, Pose2d target, Command command) {
    return Commands.runOnce(
            () -> {
              Logger.recordOutput("PathPlanner/HolonomicTest/PhaseIndex", phaseIndex);
              Logger.recordOutput("PathPlanner/HolonomicTest/CurrentPhase", phaseName);
              Logger.recordOutput("PathPlanner/HolonomicTest/CurrentTargetPose", target);
              Logger.recordOutput("PathPlanner/HolonomicTest/PhaseStartPose", drive.getPose());
            })
        .andThen(command)
        .finallyDo(
            interrupted ->
                Logger.recordOutput("PathPlanner/HolonomicTest/PhaseEndPose", drive.getPose()));
  }

  static HolonomicTestTargets createHolonomicTestTargets(Pose2d measuredStart) {
    Pose2d start = new Pose2d(measuredStart.getTranslation(), Rotation2d.kZero);
    Pose2d entry =
        new Pose2d(
            start.getX() + AutoConstants.HOLONOMIC_ENTRY_FORWARD_METERS,
            start.getY(),
            Rotation2d.kZero);
    Pose2d left =
        new Pose2d(
            entry.getX(),
            entry.getY() + AutoConstants.HOLONOMIC_LEFT_SHIFT_METERS,
            Rotation2d.kZero);
    Pose2d diagonal =
        new Pose2d(
            entry.getX() + AutoConstants.HOLONOMIC_DIAGONAL_FORWARD_METERS,
            left.getY(),
            Rotation2d.kZero);
    return new HolonomicTestTargets(start, entry, left, diagonal);
  }

  private void logHolonomicTargets(HolonomicTestTargets targets) {
    Logger.recordOutput("PathPlanner/HolonomicTest/NormalizedStartPose", targets.start());
    Logger.recordOutput("PathPlanner/HolonomicTest/EntryPose", targets.entry());
    Logger.recordOutput("PathPlanner/HolonomicTest/LeftPose", targets.left());
    Logger.recordOutput("PathPlanner/HolonomicTest/DiagonalPose", targets.diagonal());
  }

  static boolean isSafeHolonomicTestPlan(HolonomicTestTargets targets) {
    return targets != null
        && isSafeVisionTestStart(targets.start())
        && isSafeHolonomicTarget(targets.entry())
        && isSafeHolonomicTarget(targets.left())
        && isSafeHolonomicTarget(targets.diagonal());
  }

  static boolean isSafeHolonomicTestStart(Pose2d measuredStart) {
    return isSafeVisionTestStart(measuredStart)
        && isSafeHolonomicTestPlan(createHolonomicTestTargets(measuredStart));
  }

  private static boolean isSafeHolonomicTarget(Pose2d pose) {
    return pose != null
        && Double.isFinite(pose.getX())
        && Double.isFinite(pose.getY())
        && Double.isFinite(pose.getRotation().getRadians())
        && pose.getX() >= AutoConstants.HOLONOMIC_MIN_TARGET_X_METERS
        && pose.getY() >= AutoConstants.HOLONOMIC_MIN_TARGET_Y_METERS
        && pose.getY() <= AutoConstants.HOLONOMIC_MAX_TARGET_Y_METERS
        && VisionConstants.TAG_BOARD_X_METERS - pose.getX()
            >= AutoConstants.HOLONOMIC_MIN_BOARD_CLEARANCE_FROM_ROBOT_CENTER_METERS;
  }

  private Command rejectedHolonomicTestStart(String reason) {
    return Commands.runOnce(
        () -> {
          drive.stop();
          Logger.recordOutput("PathPlanner/HolonomicTest/StartAccepted", false);
          Logger.recordOutput("PathPlanner/HolonomicTest/AbortReason", reason);
          Logger.recordOutput("PathPlanner/HolonomicTest/CurrentPhase", "ABORTED");
          DriverStation.reportError(
              "Holonomic test aborted without moving: " + reason, false);
        },
        drive);
  }

  private Command rejectedVisionTestStart(String reason) {
    return Commands.runOnce(
        () -> {
          drive.stop();
          Logger.recordOutput("PathPlanner/VisionTest/StartAccepted", false);
          Logger.recordOutput("PathPlanner/VisionTest/AbortReason", reason);
          DriverStation.reportError(
              "VisionTest aborted without moving: " + reason, false);
        },
        drive);
  }

  static boolean isSafeVisionTestStart(Pose2d pose) {
    return pose != null
        && Double.isFinite(pose.getX())
        && Double.isFinite(pose.getY())
        && Double.isFinite(pose.getRotation().getRadians())
        && pose.getX() >= AutoConstants.VISION_TEST_START_MIN_X_METERS
        && pose.getX() <= AutoConstants.VISION_TEST_START_MAX_X_METERS
        && pose.getY() >= AutoConstants.VISION_TEST_START_MIN_Y_METERS
        && pose.getY() <= AutoConstants.VISION_TEST_START_MAX_Y_METERS
        && Math.abs(pose.getRotation().getDegrees())
            <= AutoConstants.VISION_TEST_START_MAX_ABS_YAW_DEGREES;
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
