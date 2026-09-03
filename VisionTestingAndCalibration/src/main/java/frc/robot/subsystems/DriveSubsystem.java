package frc.robot.subsystems;

import static edu.wpi.first.units.Units.DegreesPerSecond;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.RadiansPerSecond;
import static edu.wpi.first.units.Units.Rotations;
import static edu.wpi.first.units.Units.Second;
import static edu.wpi.first.units.Units.Volts;

import java.util.Optional;
import java.util.function.Supplier;

import com.ctre.phoenix6.SignalLogger;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.Utils;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.Pigeon2;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.swerve.SwerveDrivetrain;
import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveModuleConstants;
import com.ctre.phoenix6.swerve.SwerveRequest;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.util.PathPlannerLogging;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;

import org.littletonrobotics.junction.Logger;

import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.SwerveConstants;
import frc.robot.Constants.SwerveConstants.ModuleConfig;

/**
 * CTRE Phoenix 6 swerve drivetrain wrapper for the 2025 chassis.
 *
 * <p>This class intentionally extends CTRE's {@link SwerveDrivetrain} instead of building an
 * independent odometry/kinematics stack. That keeps hardware bring-up close to the vendor examples,
 * which is valuable for a prototype where the team needs to diagnose CAN, motor controller, and sim
 * behavior quickly.
 *
 * <p>Idea traceability:
 *
 * <p>- CTRE Phoenix 6 examples: module constants, sim update loop, SysId requests, and
 * {@code SwerveDrivetrain} inheritance are all CTRE-style.
 *
 * <p>- PathPlanner best practice: {@link AutoBuilder} is configured once here, close to the
 * drivetrain methods it needs: pose supplier, pose reset, current speeds, and robot-relative output.
 *
 * <p>- 6328/Northstar-style localization: timestamped vision observations are accepted through a
 * single {@link #addVisionMeasurement(Pose2d, double, Matrix)} entry point so every fused pose
 * measurement can be logged and audited upstream before reaching the estimator.
 */
public class DriveSubsystem extends SwerveDrivetrain<TalonFX, TalonFX, CANcoder> implements Subsystem {
  private static final double MODULE_STOPPED_SPEED_METERS_PER_SECOND = 0.02;
  private final Pigeon2 pigeon;
  private final StatusSignal<AngularVelocity> pigeonYawRateSignal;
  private final SwerveRequest.FieldCentric fieldCentric = SwerveConstants.FIELD_CENTRIC_REQUEST;
  private final SwerveRequest.RobotCentric robotCentric = new SwerveRequest.RobotCentric();
  // Keep the precision request separate from the generic robot-relative request. CTRE RobotCentric
  // defaults to OpenLoopVoltage; the six-run 2026-08-19 baseline showed that actual speed remained
  // 0.76-0.85 m/s after the profile had requested only 0.42-0.48 m/s near the target. Velocity mode
  // engages the configured TalonFX closed-loop drive gains without silently changing aiming or other
  // callers that still use driveRobotRelative().
  private final SwerveRequest.RobotCentric precisionRobotCentric =
      new SwerveRequest.RobotCentric().withDriveRequestType(DriveRequestType.Velocity);
  private final SwerveRequest.ApplyRobotSpeeds pathApplyRobotSpeeds = new SwerveRequest.ApplyRobotSpeeds();
  private final SwerveRequest.SysIdSwerveTranslation translationCharacterization =
      new SwerveRequest.SysIdSwerveTranslation();
  private final SwerveRequest.SysIdSwerveSteerGains steerCharacterization =
      new SwerveRequest.SysIdSwerveSteerGains();
  private final SwerveRequest.SysIdSwerveRotation rotationCharacterization =
      new SwerveRequest.SysIdSwerveRotation();

  /*
   * SysId is wired directly into CTRE's swerve characterization requests. Keeping translation,
   * steering, and rotation separate follows CTRE's generated-project guidance and makes it possible
   * to characterize the 2025 chassis before trusting autonomous tuning.
   */
  private final SysIdRoutine sysIdTranslation = new SysIdRoutine(
      new SysIdRoutine.Config(null, Volts.of(4), null,
          state -> SignalLogger.writeString("SysIdTranslation_State", state.toString())),
      new SysIdRoutine.Mechanism(volts -> setControl(translationCharacterization.withVolts(volts)), null, this));

  private final SysIdRoutine sysIdSteer = new SysIdRoutine(
      new SysIdRoutine.Config(null, Volts.of(7), null,
          state -> SignalLogger.writeString("SysIdSteer_State", state.toString())),
      new SysIdRoutine.Mechanism(volts -> setControl(steerCharacterization.withVolts(volts)), null, this));

  private final SysIdRoutine sysIdRotation = new SysIdRoutine(
      new SysIdRoutine.Config(Volts.of(Math.PI / 6.0).per(Second), Volts.of(Math.PI), null,
          state -> SignalLogger.writeString("SysIdRotation_State", state.toString())),
      new SysIdRoutine.Mechanism(
          output -> setControl(rotationCharacterization.withRotationalRate(output.in(Volts))), null, this));

  private SysIdRoutine selectedSysIdRoutine = sysIdTranslation;
  // FPGA time of the last pose reset. Vision uses this to discard in-flight camera frames captured
  // before the reset, which would otherwise yank the freshly-reset estimate back toward the old pose.
  private double lastResetTimeSeconds = 0.0;
  private Notifier simNotifier;
  private double lastSimTimeSeconds;

  /**
   * Builds the four CTRE module constants from the 2025 module map in {@link SwerveConstants}.
   *
   * <p>Idea traceability: this is intentionally boring and explicit. The 2025 chassis already has
   * known CAN IDs and offsets; hiding them behind loops or generated arrays would make pit-side
   * verification harder.
   */
  public static DriveSubsystem create() {
    return new DriveSubsystem(
        SwerveConstants.CONSTANT_CREATOR.createModuleConstants(
            SwerveConstants.FRONT_LEFT.steerMotorId(),
            SwerveConstants.FRONT_LEFT.driveMotorId(),
            SwerveConstants.FRONT_LEFT.cancoderId(),
            Rotations.of(SwerveConstants.FRONT_LEFT.angleOffsetRotations()),
            Meters.of(SwerveConstants.FRONT_LEFT.xMeters()),
            Meters.of(SwerveConstants.FRONT_LEFT.yMeters()),
            SwerveConstants.FRONT_LEFT.driveMotorInverted(),
            SwerveConstants.FRONT_LEFT.steerMotorInverted(),
            SwerveConstants.FRONT_LEFT.cancoderInverted()),
        SwerveConstants.CONSTANT_CREATOR.createModuleConstants(
            SwerveConstants.FRONT_RIGHT.steerMotorId(),
            SwerveConstants.FRONT_RIGHT.driveMotorId(),
            SwerveConstants.FRONT_RIGHT.cancoderId(),
            Rotations.of(SwerveConstants.FRONT_RIGHT.angleOffsetRotations()),
            Meters.of(SwerveConstants.FRONT_RIGHT.xMeters()),
            Meters.of(SwerveConstants.FRONT_RIGHT.yMeters()),
            SwerveConstants.FRONT_RIGHT.driveMotorInverted(),
            SwerveConstants.FRONT_RIGHT.steerMotorInverted(),
            SwerveConstants.FRONT_RIGHT.cancoderInverted()),
        SwerveConstants.CONSTANT_CREATOR.createModuleConstants(
            SwerveConstants.BACK_LEFT.steerMotorId(),
            SwerveConstants.BACK_LEFT.driveMotorId(),
            SwerveConstants.BACK_LEFT.cancoderId(),
            Rotations.of(SwerveConstants.BACK_LEFT.angleOffsetRotations()),
            Meters.of(SwerveConstants.BACK_LEFT.xMeters()),
            Meters.of(SwerveConstants.BACK_LEFT.yMeters()),
            SwerveConstants.BACK_LEFT.driveMotorInverted(),
            SwerveConstants.BACK_LEFT.steerMotorInverted(),
            SwerveConstants.BACK_LEFT.cancoderInverted()),
        SwerveConstants.CONSTANT_CREATOR.createModuleConstants(
            SwerveConstants.BACK_RIGHT.steerMotorId(),
            SwerveConstants.BACK_RIGHT.driveMotorId(),
            SwerveConstants.BACK_RIGHT.cancoderId(),
            Rotations.of(SwerveConstants.BACK_RIGHT.angleOffsetRotations()),
            Meters.of(SwerveConstants.BACK_RIGHT.xMeters()),
            Meters.of(SwerveConstants.BACK_RIGHT.yMeters()),
            SwerveConstants.BACK_RIGHT.driveMotorInverted(),
            SwerveConstants.BACK_RIGHT.steerMotorInverted(),
            SwerveConstants.BACK_RIGHT.cancoderInverted()));
  }

  @SafeVarargs
  private DriveSubsystem(
      SwerveModuleConstants<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration>... modules) {
    super(
        TalonFX::new,
        TalonFX::new,
        CANcoder::new,
        SwerveConstants.DRIVETRAIN_CONSTANTS,
        SwerveConstants.ODOMETRY_UPDATE_FREQUENCY_HZ,
        modules);
    pigeon = getPigeon2();
    // Use the refreshing getter once so the cached StatusSignal starts with a real status/value.
    // Subsequent control reads refresh the same object non-blockingly.
    pigeonYawRateSignal = pigeon.getAngularVelocityZWorld();
    pigeonYawRateSignal.setUpdateFrequency(
        SwerveConstants.PIGEON_YAW_RATE_UPDATE_FREQUENCY_HZ, 0.1);
    if (Utils.isSimulation()) {
      startSimThread();
    }
    configurePathPlanner();
  }

  /**
   * Connects PathPlanner's autonomous output to CTRE's {@code ApplyRobotSpeeds} request.
   *
   * <p>Idea traceability:
   *
   * <p>- PathPlanner/CTRE integration examples: use {@code AutoBuilder.configure} and pass CTRE's
   * force feedforwards into {@code withWheelForceFeedforwardsX/Y} instead of dropping them.
   *
   * <p>- Precision-handoff pattern from teams such as 2910/254/1678: PathPlanner is responsible for
   * getting near the target efficiently; {@code DriveToPosePrecisionCommand} can then finish inside
   * a smaller tolerance window.
   */
  private void configurePathPlanner() {
    try {
      RobotConfig config = RobotConfig.fromGUISettings();
      AutoBuilder.configure(
          this::getPose,
          this::resetPose,
          () -> getState().Speeds,
          (speeds, feedforwards) -> setControl(
              pathApplyRobotSpeeds
                  .withSpeeds(speeds)
                  .withWheelForceFeedforwardsX(feedforwards.robotRelativeForcesXNewtons())
                  .withWheelForceFeedforwardsY(feedforwards.robotRelativeForcesYNewtons())),
          new PPHolonomicDriveController(new PIDConstants(5.0, 0.0, 0.0), new PIDConstants(7.0, 0.0, 0.0)),
          config,
          () -> false,
          this);
      PathPlannerLogging.setLogCurrentPoseCallback(
          pose -> Logger.recordOutput("PathPlanner/CurrentPose", pose));
      PathPlannerLogging.setLogTargetPoseCallback(
          pose -> Logger.recordOutput("PathPlanner/TargetPose", pose));
      PathPlannerLogging.setLogActivePathCallback(
          poses ->
              Logger.recordOutput(
                  "PathPlanner/ActivePath", poses.toArray(Pose2d[]::new)));
    } catch (Exception ex) {
      DriverStation.reportWarning("PathPlanner AutoBuilder not configured: " + ex.getMessage(), false);
    }
  }

  /**
   * Applies an arbitrary CTRE request as a command. This keeps dashboard/test commands concise while
   * preserving CTRE's request-based control model.
   */
  public Command applyRequest(Supplier<SwerveRequest> requestSupplier) {
    return run(() -> setControl(requestSupplier.get()));
  }

  /**
   * Human-driver field-relative control.
   *
   * <p>Idea traceability: keep manual drive simple and explicit during vision bring-up. If the pose
   * estimate is wrong, the driver and logs should show that immediately rather than masking it with
   * extra driver-assist behavior.
   */
  public void driveFieldRelative(double xMetersPerSecond, double yMetersPerSecond, double omegaRadiansPerSecond) {
    setControl(fieldCentric
        .withVelocityX(xMetersPerSecond)
        .withVelocityY(yMetersPerSecond)
        .withRotationalRate(omegaRadiansPerSecond));
  }

  /**
   * Robot-relative velocity control used by the precision final-pose command and stop logic.
   */
  public void driveRobotRelative(ChassisSpeeds speeds) {
    setControl(robotCentric
        .withVelocityX(speeds.vxMetersPerSecond)
        .withVelocityY(speeds.vyMetersPerSecond)
        .withRotationalRate(speeds.omegaRadiansPerSecond));
  }

  /**
   * Robot-relative closed-loop velocity control used only by the final-pose controller.
   *
   * <p>This deliberately does not replace {@link #driveRobotRelative(ChassisSpeeds)} globally. It
   * makes the first post-baseline test a single-variable experiment: the motion profile, pose gains,
   * constraints, wheel radius, vision policy, and unrelated robot-relative commands remain unchanged.
   */
  public void driveRobotRelativeVelocity(ChassisSpeeds speeds) {
    setControl(precisionRobotCentric
        .withVelocityX(speeds.vxMetersPerSecond)
        .withVelocityY(speeds.vyMetersPerSecond)
        .withRotationalRate(speeds.omegaRadiansPerSecond));
  }

  public void stop() {
    driveRobotRelative(new ChassisSpeeds());
  }

  public Pose2d getPose() {
    return getState().Pose;
  }

  /**
   * Heading the robot had at a given WPILib FPGA timestamp, sampled from CTRE's odometry pose-history
   * buffer (the same buffer {@code addVisionMeasurement} fuses against, so the trig-solve strategy and
   * the estimator agree about "where was I then"). Added 2026-07-16 for the trig-solve single-tag
   * strategy (idea: 6328 / PhotonVision {@code PNP_DISTANCE_TRIG_SOLVE} require heading-at-timestamp
   * via {@code addHeadingData}; CTRE's buffer already stores it, so we sample instead of duplicating).
   *
   * <p>The FPGA->Phoenix time-base conversion mirrors the vision consumer in {@code RobotContainer} --
   * same correctness requirement, same {@link Utils#fpgaToCurrentTime} fix.
   */
  public Optional<Rotation2d> sampleHeadingAt(double fpgaTimestampSeconds) {
    return samplePoseAt(Utils.fpgaToCurrentTime(fpgaTimestampSeconds)).map(Pose2d::getRotation);
  }

  /** Robot-relative chassis speeds from CTRE's state (used by aiming/precision feedforward seeding). */
  public ChassisSpeeds getRobotRelativeSpeeds() {
    return getState().Speeds;
  }

  /** Field-relative chassis speeds, for seeding profiled controllers and shoot-on-move lookahead. */
  public ChassisSpeeds getFieldRelativeSpeeds() {
    return ChassisSpeeds.fromRobotRelativeSpeeds(getState().Speeds, getPose().getRotation());
  }

  /**
   * Mount-corrected Pigeon yaw rate for heading damping and the precision-command stop gate.
   *
   * <p>{@code SwerveDriveState.Speeds.omegaRadiansPerSecond} is reconstructed from module states.
   * In the 2026-09-02 2 m log it integrated to {@code +8.09 deg} while the gyro-owned pose changed
   * {@code -0.67 deg}, so it is retained for drivetrain diagnosis but is not a trustworthy heading
   * rate for closed-loop damping. If the Pigeon signal reports an error, fall back to the kinematic
   * rate rather than feeding an invalid value into the controller.
   */
  public double getGyroYawRateRadiansPerSecond() {
    pigeonYawRateSignal.refresh(false);
    return pigeonYawRateSignal.getStatus().isOK()
        ? pigeonYawRateSignal.getValue().in(RadiansPerSecond)
        : getState().Speeds.omegaRadiansPerSecond;
  }

  public void resetPose(Pose2d pose) {
    super.resetPose(pose);
    // Record when the estimate was force-set so Vision can reject older, in-flight camera frames.
    lastResetTimeSeconds = Timer.getTimestamp();
  }

  /** FPGA time of the most recent pose reset (0 before any reset). */
  public double getLastResetTimeSeconds() {
    return lastResetTimeSeconds;
  }

  /**
   * Fuses a vetted vision observation into CTRE's pose estimator.
   *
   * <p>Important: validation does not happen here. The {@code Vision} subsystem owns validation,
   * covariance selection, timestamp ordering, and logging, and the {@code VisionConsumer} in
   * {@code RobotContainer} converts the timestamp to the CTRE time base before this call. This
   * separation makes it clear whether a bad pose came from camera processing policy or drivetrain
   * estimator behavior.
   */
  public void addVisionMeasurement(Pose2d pose, double timestampSeconds, Matrix<N3, N1> standardDeviations) {
    super.addVisionMeasurement(pose, timestampSeconds, standardDeviations);
  }

  public void seedFieldRelativeBlueForward() {
    setOperatorPerspectiveForward(Rotation2d.kZero);
  }

  public Command selectTranslationSysId() {
    return runOnce(() -> selectedSysIdRoutine = sysIdTranslation);
  }

  public Command selectSteerSysId() {
    return runOnce(() -> selectedSysIdRoutine = sysIdSteer);
  }

  public Command selectRotationSysId() {
    return runOnce(() -> selectedSysIdRoutine = sysIdRotation);
  }

  public Command sysIdQuasistatic(SysIdRoutine.Direction direction) {
    return selectedSysIdRoutine.quasistatic(direction);
  }

  public Command sysIdDynamic(SysIdRoutine.Direction direction) {
    return selectedSysIdRoutine.dynamic(direction);
  }

  /**
   * Runs CTRE's drivetrain simulation at 5 ms. The normal robot periodic loop is too slow for smooth
   * motor-controller simulation, so CTRE examples use a Notifier-backed update loop.
   */
  private void startSimThread() {
    lastSimTimeSeconds = Utils.getCurrentTimeSeconds();
    simNotifier = new Notifier(() -> {
      double currentTimeSeconds = Utils.getCurrentTimeSeconds();
      double dt = Math.max(0.0, Math.min(currentTimeSeconds - lastSimTimeSeconds, 0.05));
      lastSimTimeSeconds = currentTimeSeconds;
      updateSimState(dt, RobotController.getBatteryVoltage());
    });
    simNotifier.startPeriodic(0.005);
  }

  @Override
  public void periodic() {
    /*
     * Log the fused estimator output every loop. Idea traceability: 6328 logs odometryPose and
     * estimatedPose via @AutoLogOutput. Codex's first pass logged vision *inputs* but never the fused
     * drivetrain pose, so the estimator's actual output was invisible in replay. These channels are
     * what AdvantageScope's 2D/3D Field tab and a robot model render. (Review BUG/ISSUE 4.)
     */
    var state = getState();
    pigeonYawRateSignal.refresh(false);
    double gyroYawRateDegreesPerSecond =
        pigeonYawRateSignal.getValue().in(DegreesPerSecond);
    double kinematicOmegaDegreesPerSecond =
        Math.toDegrees(state.Speeds.omegaRadiansPerSecond);
    Logger.recordOutput("Drive/Pose", state.Pose);
    Logger.recordOutput("Drive/Speeds", state.Speeds);
    Logger.recordOutput("Drive/GyroYawRateDegreesPerSecond", gyroYawRateDegreesPerSecond);
    Logger.recordOutput(
        "Drive/KinematicOmegaDegreesPerSecond", kinematicOmegaDegreesPerSecond);
    Logger.recordOutput(
        "Drive/GyroMinusKinematicOmegaDegreesPerSecond",
        gyroYawRateDegreesPerSecond - kinematicOmegaDegreesPerSecond);
    Logger.recordOutput("Drive/GyroYawRateSignalOK", pigeonYawRateSignal.getStatus().isOK());
    Logger.recordOutput(
        "Drive/GyroYawRateAppliedUpdateFrequencyHz",
        pigeonYawRateSignal.getAppliedUpdateFrequency());
    Logger.recordOutput("Drive/ModuleStates", state.ModuleStates);
    Logger.recordOutput("Drive/ModuleTargets", state.ModuleTargets);
    Logger.recordOutput("Drive/ConfiguredDriveGains/KP", SwerveConstants.DRIVE_KP);
    Logger.recordOutput("Drive/ConfiguredDriveGains/KI", SwerveConstants.DRIVE_KI);
    Logger.recordOutput("Drive/ConfiguredDriveGains/KD", SwerveConstants.DRIVE_KD);
    Logger.recordOutput("Drive/ConfiguredDriveGains/KS", SwerveConstants.DRIVE_KS);
    Logger.recordOutput("Drive/ConfiguredDriveGains/KV", SwerveConstants.DRIVE_KV);
    Logger.recordOutput("Drive/ConfiguredDriveGains/KA", SwerveConstants.DRIVE_KA);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredSettleSeconds",
        AutoConstants.PRECISION_SETTLE_SECONDS);
    double moduleSpeedSum = 0.0;
    double moduleSpeedMax = 0.0;
    for (var moduleState : state.ModuleStates) {
      double speed = Math.abs(moduleState.speedMetersPerSecond);
      moduleSpeedSum += speed;
      moduleSpeedMax = Math.max(moduleSpeedMax, speed);
    }
    double moduleTargetSpeedSum = 0.0;
    double moduleTargetSpeedMax = 0.0;
    for (var moduleTarget : state.ModuleTargets) {
      double speed = Math.abs(moduleTarget.speedMetersPerSecond);
      moduleTargetSpeedSum += speed;
      moduleTargetSpeedMax = Math.max(moduleTargetSpeedMax, speed);
    }
    Logger.recordOutput(
        "Drive/MeanAbsModuleSpeedMetersPerSecond",
        state.ModuleStates.length == 0 ? 0.0 : moduleSpeedSum / state.ModuleStates.length);
    Logger.recordOutput("Drive/MaxAbsModuleSpeedMetersPerSecond", moduleSpeedMax);
    Logger.recordOutput(
        "Drive/MeanAbsModuleTargetSpeedMetersPerSecond",
        state.ModuleTargets.length == 0
            ? 0.0
            : moduleTargetSpeedSum / state.ModuleTargets.length);
    Logger.recordOutput("Drive/MaxAbsModuleTargetSpeedMetersPerSecond", moduleTargetSpeedMax);
    Logger.recordOutput(
        "Drive/WheelsStopped", moduleSpeedMax <= MODULE_STOPPED_SPEED_METERS_PER_SECOND);
  }

  public ModuleConfig[] getModuleConfigsForDocs() {
    return new ModuleConfig[] {
        SwerveConstants.FRONT_LEFT,
        SwerveConstants.FRONT_RIGHT,
        SwerveConstants.BACK_LEFT,
        SwerveConstants.BACK_RIGHT
    };
  }
}
