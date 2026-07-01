package frc.robot;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Inches;
import static edu.wpi.first.units.Units.KilogramSquareMeters;
import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.Volts;

import java.util.List;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.Pigeon2Configuration;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.signals.StaticFeedforwardSignValue;
import com.ctre.phoenix6.swerve.SwerveDrivetrainConstants;
import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType;
import com.ctre.phoenix6.swerve.SwerveModuleConstants.ClosedLoopOutputType;
import com.ctre.phoenix6.swerve.SwerveModuleConstants.DriveMotorArrangement;
import com.ctre.phoenix6.swerve.SwerveModuleConstants.SteerFeedbackType;
import com.ctre.phoenix6.swerve.SwerveModuleConstants.SteerMotorArrangement;
import com.ctre.phoenix6.swerve.SwerveModuleConstantsFactory;
import com.ctre.phoenix6.swerve.SwerveRequest;
import com.pathplanner.lib.path.PathConstraints;

import edu.wpi.first.apriltag.AprilTag;
import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.units.measure.Current;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.LinearVelocity;
import edu.wpi.first.units.measure.MomentOfInertia;
import edu.wpi.first.units.measure.Voltage;

/**
 * Central configuration for the 2027 vision/localization prototype.
 *
 * <p>Idea traceability:
 *
 * <p>- Team 999 2025 robot code: The CAN bus name, Pigeon ID, motor IDs, CANCoder IDs, module
 * offsets, and drive inversion flags come from the 2025 chassis because this prototype is intended
 * to run on that robot.
 *
 * <p>- CTRE Phoenix 6 examples: The swerve constants are expressed through CTRE's
 * {@link SwerveModuleConstantsFactory} and {@link SwerveDrivetrainConstants}, matching the Phoenix
 * 6 generated-project shape instead of hand-writing a separate kinematics stack.
 *
 * <p>- 6328/Northstar-style localization work: Vision constants are separated from drivetrain
 * constants, and every uncertainty/rejection threshold is explicit so log review can tune the
 * estimator policy without hunting through control code.
 */
public final class Constants {
  private Constants() {}

  public static final class OperatorConstants {
    private OperatorConstants() {}

    public static final int DRIVER_CONTROLLER_PORT = 0;
    public static final double STICK_DEADBAND = 0.08;
    public static final double SLOW_MODE_SCALE = 0.35;
  }

  public static final class SwerveConstants {
    private SwerveConstants() {}

    /**
     * CTRE odometry update rate for the drivetrain estimator. The project keeps this higher than
     * the normal 50 Hz robot loop so time-aligned vision samples have a dense odometry history to
     * fuse against.
     *
     * <p>Idea traceability: mirrors the "odometry first, vision corrects pose history" principle
     * used by 6328/Northstar and other high-performing localization stacks.
     */
    /**
     * roboRIO/CANivore odometry sampling rate -- this is the rate CTRE's background thread reads the
     * Pigeon + drive/steer encoders over the CAN FD bus. It is NOT a camera or Orange Pi rate.
     *
     * <p>Important hardware note (Team 999 has Orange Pis, not a 6328-style Mac mini): the cameras /
     * PhotonVision run at their own ~30-50 fps on the Orange Pis and publish over NetworkTables. Each
     * (much slower) camera frame is fused by timestamp against this dense 250 Hz odometry history. So
     * raising this to 250 Hz asks nothing of the Orange Pi -- it is pure roboRIO + CANivore work, which
     * is exactly what 6328/3467/5687 do on the roboRIO. Codex used 100 Hz.
     */
    public static final double ODOMETRY_UPDATE_FREQUENCY_HZ = 250.0;
    public static final double POSE_HISTORY_SECONDS = 0.6;

    /**
     * 2025 chassis geometry. These values are intentionally conservative placeholders until the
     * actual center-to-center module spacing is remeasured on the robot.
     */
    public static final double TRACK_WIDTH_METERS = 0.4572;
    public static final double WHEEL_BASE_METERS = 0.4572;
    public static final double MAX_SPEED_METERS_PER_SECOND = 5.21;
    public static final double MAX_ANGULAR_RATE_RADIANS_PER_SECOND = 2.0 * Math.PI;
    public static final double DRIVE_DEADBAND_RATIO = 0.04;
    public static final double ROTATION_DEADBAND_RATIO = 0.05;

    /**
     * 2025 CANivore bus. The hoot log path enables CTRE low-level signal capture during bring-up.
     *
     * <p>Idea traceability: CTRE Phoenix 6 examples use CANBus objects and hoot logging as the
     * vendor-supported path for debugging device traffic and sim replay.
     */
    public static final CANBus CAN_BUS = new CANBus("canivore1", "./logs/vision-test.hoot");
    public static final int PIGEON_ID = 40;
    public static final Pigeon2Configuration PIGEON_CONFIGS = null;

    /**
     * Initial closed-loop gains for SDS MK4 L3 steering. These are startup values, not final
     * characterization output.
     *
     * <p>Idea traceability: CTRE generated swerve projects keep gains in the module factory so each
     * module is created consistently.
     */
    public static final Slot0Configs STEER_GAINS = new Slot0Configs()
        .withKP(100.0)
        .withKI(0.0)
        .withKD(0.5)
        .withKS(0.1)
        .withKV(1.59)
        .withKA(0.0)
        .withStaticFeedforwardSign(StaticFeedforwardSignValue.UseClosedLoopSign);

    public static final Slot0Configs DRIVE_GAINS = new Slot0Configs()
        .withKP(0.1)
        .withKI(0.0)
        .withKD(0.0)
        .withKS(0.0)
        .withKV(0.124);

    public static final TalonFXConfiguration DRIVE_INITIAL_CONFIGS = new TalonFXConfiguration();
    public static final TalonFXConfiguration STEER_INITIAL_CONFIGS = new TalonFXConfiguration()
        .withCurrentLimits(
            new CurrentLimitsConfigs()
                .withStatorCurrentLimit(Amps.of(60))
                .withStatorCurrentLimitEnable(true));
    public static final CANcoderConfiguration CANCODER_INITIAL_CONFIGS = new CANcoderConfiguration();

    /** SDS MK4 L3 drive ratio. Confirm against final installed gearing before competition use. */
    public static final double DRIVE_GEAR_RATIO = 6.12;
    public static final double STEER_GEAR_RATIO = 12.8;
    public static final double COUPLE_RATIO = 3.5714285714285716;
    public static final Distance WHEEL_RADIUS = Inches.of(2.0);
    public static final Current SLIP_CURRENT = Amps.of(80.0);
    public static final LinearVelocity SPEED_AT_12_VOLTS = MetersPerSecond.of(MAX_SPEED_METERS_PER_SECOND);
    public static final MomentOfInertia STEER_INERTIA = KilogramSquareMeters.of(0.01);
    public static final MomentOfInertia DRIVE_INERTIA = KilogramSquareMeters.of(0.01);
    public static final Voltage STEER_FRICTION_VOLTAGE = Volts.of(0.2);
    public static final Voltage DRIVE_FRICTION_VOLTAGE = Volts.of(0.2);

    public static final SwerveModuleConstantsFactory<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration>
        CONSTANT_CREATOR =
            new SwerveModuleConstantsFactory<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration>()
                .withDriveMotorGearRatio(DRIVE_GEAR_RATIO)
                .withSteerMotorGearRatio(STEER_GEAR_RATIO)
                .withCouplingGearRatio(COUPLE_RATIO)
                .withWheelRadius(WHEEL_RADIUS)
                .withSteerMotorGains(STEER_GAINS)
                .withDriveMotorGains(DRIVE_GAINS)
                .withSteerMotorClosedLoopOutput(ClosedLoopOutputType.Voltage)
                .withDriveMotorClosedLoopOutput(ClosedLoopOutputType.Voltage)
                .withSlipCurrent(SLIP_CURRENT)
                .withSpeedAt12Volts(SPEED_AT_12_VOLTS)
                .withDriveMotorType(DriveMotorArrangement.TalonFX_Integrated)
                .withSteerMotorType(SteerMotorArrangement.TalonFX_Integrated)
                .withFeedbackSource(SteerFeedbackType.FusedCANcoder)
                .withDriveMotorInitialConfigs(DRIVE_INITIAL_CONFIGS)
                .withSteerMotorInitialConfigs(STEER_INITIAL_CONFIGS)
                .withEncoderInitialConfigs(CANCODER_INITIAL_CONFIGS)
                .withSteerInertia(STEER_INERTIA)
                .withDriveInertia(DRIVE_INERTIA)
                .withSteerFrictionVoltage(STEER_FRICTION_VOLTAGE)
                .withDriveFrictionVoltage(DRIVE_FRICTION_VOLTAGE);

    public static final SwerveDrivetrainConstants DRIVETRAIN_CONSTANTS =
        new SwerveDrivetrainConstants()
            .withCANBusName(CAN_BUS.getName())
            .withPigeon2Id(PIGEON_ID)
            .withPigeon2Configs(PIGEON_CONFIGS);

    /**
     * One physical swerve module from the 2025 chassis.
     *
     * <p>The record keeps electrical identity, measured steering offset, motor inversions, and
     * module location together so the CTRE factory cannot accidentally mix constants from different
     * corners.
     */
    public record ModuleConfig(
        int driveMotorId,
        int steerMotorId,
        int cancoderId,
        double angleOffsetRotations,
        boolean driveMotorInverted,
        boolean steerMotorInverted,
        boolean cancoderInverted,
        double xMeters,
        double yMeters) {}

    public static final ModuleConfig FRONT_LEFT = new ModuleConfig(
        3, 4, 31, 0.022582890625, false, false, false,
        WHEEL_BASE_METERS / 2.0, TRACK_WIDTH_METERS / 2.0);
    public static final ModuleConfig FRONT_RIGHT = new ModuleConfig(
        1, 2, 30, -0.3797604921875, true, false, false,
        WHEEL_BASE_METERS / 2.0, -TRACK_WIDTH_METERS / 2.0);
    public static final ModuleConfig BACK_LEFT = new ModuleConfig(
        7, 8, 33, 0.421386796875, false, false, false,
        -WHEEL_BASE_METERS / 2.0, TRACK_WIDTH_METERS / 2.0);
    public static final ModuleConfig BACK_RIGHT = new ModuleConfig(
        5, 6, 32, 0.088256890625, true, false, false,
        -WHEEL_BASE_METERS / 2.0, -TRACK_WIDTH_METERS / 2.0);

    /**
     * Default teleop drive request. It is open-loop voltage for a forgiving first bring-up on the
     * 2025 chassis; precise trajectory control uses separate robot-speed requests in
     * {@code DriveSubsystem}.
     */
    public static final SwerveRequest.FieldCentric FIELD_CENTRIC_REQUEST =
        new SwerveRequest.FieldCentric()
            .withDeadband(MAX_SPEED_METERS_PER_SECOND * DRIVE_DEADBAND_RATIO)
            .withRotationalDeadband(MAX_ANGULAR_RATE_RADIANS_PER_SECOND * ROTATION_DEADBAND_RATIO)
            .withDriveRequestType(DriveRequestType.OpenLoopVoltage);
  }

  public static final class VisionConstants {
    private VisionConstants() {}

    /**
     * Custom two-tag layout for the offseason tag board.
     *
     * <p>Idea traceability: instead of relying on a season field layout, this prototype uses a
     * deployable layout file and an identical in-code layout. That follows the calibration-board
     * style used by many teams when they want a repeatable indoor localization target before the
     * real field exists.
     */
    public static final String APRILTAG_LAYOUT_RESOURCE = "apriltags/mecharams-two-tag-layout.json";
    public static final double FIELD_LENGTH_METERS = 8.0;
    public static final double FIELD_WIDTH_METERS = 4.0;
    public static final double TAG_SIZE_METERS = 0.1651;
    public static final int LEFT_BOARD_TAG_ID = 1;
    public static final int RIGHT_BOARD_TAG_ID = 2;

    /*
     * Camera roster. RECOMMENDED START: 2 cameras (front-left, front-right) on 1 Orange Pi -- simplest
     * thing that proves the pipeline, and the established-safe OPi5 budget. The back-left/back-right
     * entries exist so scaling to 4 cameras / 2 Orange Pis is a one-line uncomment in RobotContainer.
     *
     * PhotonVision camera names must be globally unique across all Pis; the robot connects to each by
     * name over NetworkTables and does not care which Pi hosts it, so adding/moving a camera is just a
     * name + transform change here.
     *
     * Hardware traceability: Team 999 runs Orange Pis (not a 6328-style Mac mini). The
     * Chief-Delphi-established safe budget for an Orange Pi 5 is ~2 AprilTag streams at 1280x800; nobody
     * documents 4 tag cameras on one Pi (research-log.md pt 4). So if/when 4 cameras are wanted (for
     * competition field coverage + failure isolation), use 2 Pis at 2 cams each:
     *   Orange Pi A -> front-left, front-right     Orange Pi B -> back-left, back-right
     */
    public static final String FRONT_LEFT_CAMERA_NAME = "front-left";
    public static final String FRONT_RIGHT_CAMERA_NAME = "front-right";
    public static final String BACK_LEFT_CAMERA_NAME = "back-left";
    public static final String BACK_RIGHT_CAMERA_NAME = "back-right";

    /**
     * Provisional front-left camera transform.
     *
     * <p>Mount recommendation: above or just inboard of the front-left module, looking forward and
     * slightly inward. The negative pitch points down toward short-range tags; positive yaw angles
     * the camera toward robot centerline so the pair has overlapping views.
     *
     * <p>Idea traceability: this is a two-camera, cross-eyed variant of Northstar-style coverage:
     * maximize frontal overlap for scoring approaches while keeping each camera's view unique enough
     * to preserve tags during rotation.
     */
    public static final Transform3d ROBOT_TO_FRONT_LEFT_CAMERA =
        new Transform3d(
            new Translation3d(0.23, 0.24, 0.43),
            new Rotation3d(0.0, Math.toRadians(-18.0), Math.toRadians(18.0)));
    /**
     * Provisional front-right camera transform. Same assumptions as the left camera, mirrored across
     * the robot centerline.
     */
    public static final Transform3d ROBOT_TO_FRONT_RIGHT_CAMERA =
        new Transform3d(
            new Translation3d(0.23, -0.24, 0.43),
            new Rotation3d(0.0, Math.toRadians(-18.0), Math.toRadians(-18.0)));

    /**
     * Provisional rear cameras, mirrored to look backward and slightly inward. Rear coverage keeps tags
     * in view while the robot rotates or approaches a target backward, which is the main reason to go
     * from 2 to 4 cameras. MEASURE these after mounting -- they are starting guesses, not surveyed.
     */
    public static final Transform3d ROBOT_TO_BACK_LEFT_CAMERA =
        new Transform3d(
            new Translation3d(-0.23, 0.24, 0.43),
            new Rotation3d(0.0, Math.toRadians(-18.0), Math.toRadians(180.0 - 18.0)));
    public static final Transform3d ROBOT_TO_BACK_RIGHT_CAMERA =
        new Transform3d(
            new Translation3d(-0.23, -0.24, 0.43),
            new Rotation3d(0.0, Math.toRadians(-18.0), Math.toRadians(180.0 + 18.0)));

    /**
     * Vision rejection gates. These should be tuned from AdvantageKit logs after the cameras are
     * mounted and calibrated.
     *
     * <p>Idea traceability:
     *
     * <p>- 6328/Northstar: reject poses that are physically impossible before they touch the drive
     * estimator.
     *
     * <p>- 3467/5687-style estimator review: log explicit rejection reasons so every discarded
     * frame can be explained later.
     *
     * <p>- 125 and other conservative single-tag systems: keep single-tag translation if it is
     * clean, but do not trust it for heading.
     */
    public static final double MAX_ACCEPTED_Z_METERS = 0.25;
    public static final double FIELD_BORDER_MARGIN_METERS = 0.50;
    public static final double MAX_SINGLE_TAG_AMBIGUITY = 0.20;
    public static final double MAX_AVERAGE_TAG_DISTANCE_METERS = 5.0;

    /**
     * Covariance baselines at 1 m / 1 tag. Effective std-dev = baseline * dist^2 / tagCount^2 *
     * cameraFactor. Single-tag heading is never trusted (theta = +Infinity in {@code Vision}).
     *
     * <p>Idea traceability: 6328/Northstar adaptive covariance shape. Tune these from AdvantageKit logs
     * after the cameras are mounted and calibrated; they are starting points, not measured values.
     */
    public static final double LINEAR_STD_DEV_BASELINE = 0.06;
    public static final double ANGULAR_STD_DEV_BASELINE = 0.08;

    /**
     * Per-camera trust multipliers (index matches camera order in {@code RobotContainer}). A
     * worse-calibrated or more flexibly-mounted camera should get a larger factor so it counts less.
     * Idea: 6328 {@code cameras[i].stdDevFactor()}.
     */
    public static final double[] CAMERA_STD_DEV_FACTORS = new double[] {1.0, 1.0, 1.0, 1.0};

    /**
     * Seconds at the start of autonomous during which vision is ignored, protecting the known auto
     * start pose from a bad first frame. Idea: 6328 {@code autoIgnoreTimeSecs}.
     */
    public static final double AUTO_VISION_IGNORE_SECONDS = 0.3;

    /**
     * Max age of a single-target bearing before {@code Vision.getTargetX} treats it as stale and returns
     * empty. Cameras publish slower than the robot loop, so on no-frame loops the last bearing lingers;
     * a future boresight loop must not servo on it. Roughly a few camera frames at ~30-50 fps.
     */
    public static final double TARGET_OBSERVATION_MAX_STALENESS_SECONDS = 0.25;

    /**
     * Fixed "quarantine" after a pose reset during which ALL vision is suppressed, on top of the
     * per-frame timestamp check. The sim log (2026-07-01) showed queued/sim-delayed frames whose
     * timestamp slipped just past the reset time still getting fused and bouncing the pose back; a short
     * time-based window catches those. ~camera latency + margin.
     */
    public static final double RESET_QUARANTINE_SECONDS = 0.35;

    /**
     * Simulated-camera model for {@code VisionIOPhotonVisionSim}, approximating the Arducam OV9782
     * (1280x800 global shutter, low-distortion M12 lens, USB2 UVC). These let the PhotonVision simulator
     * produce frames that behave like the real sensor (resolution, FOV, frame rate, latency, pixel
     * noise) rather than a perfect camera. Idea: 3467 configures real SimCameraProperties.
     */
    public static final int SIM_CAMERA_WIDTH_PX = 1280;
    public static final int SIM_CAMERA_HEIGHT_PX = 800;
    public static final double SIM_CAMERA_DIAGONAL_FOV_DEGREES = 84.0;
    public static final double SIM_CAMERA_AVG_PX_ERROR = 0.25;
    public static final double SIM_CAMERA_PX_ERROR_STD_DEV = 0.08;
    public static final double SIM_CAMERA_FPS = 50.0;
    public static final double SIM_CAMERA_AVG_LATENCY_MS = 30.0;
    public static final double SIM_CAMERA_LATENCY_STD_DEV_MS = 8.0;

    /**
     * In-code copy of the deploy layout. Keeping this here makes unit/sim use straightforward even
     * when the deploy directory is not mounted the same way as on the roboRIO.
     */
    public static final AprilTagFieldLayout CUSTOM_FIELD_LAYOUT = new AprilTagFieldLayout(
        List.of(
            new AprilTag(
                LEFT_BOARD_TAG_ID,
                new Pose3d(6.0, 1.75, 1.05, new Rotation3d(0.0, 0.0, Math.PI))),
            new AprilTag(
                RIGHT_BOARD_TAG_ID,
                new Pose3d(6.0, 2.25, 1.05, new Rotation3d(0.0, 0.0, Math.PI)))),
        FIELD_LENGTH_METERS,
        FIELD_WIDTH_METERS);
  }

  public static final class AutoConstants {
    private AutoConstants() {}

    /**
     * Conservative constraints for early path testing on the 2025 chassis.
     *
     * <p>Idea traceability: use PathPlanner for the coarse move, then hand off to
     * {@code DriveToPosePrecisionCommand} for the final tolerance/settle behavior. This mirrors the
     * "trajectory gets you close, local controller finishes the job" pattern seen in strong
     * autonomous stacks from teams such as 2910, 254, and 1678.
     */
    public static final PathConstraints CAUTIOUS_CONSTRAINTS =
        new PathConstraints(1.6, 1.2, Math.toRadians(120.0), Math.toRadians(180.0));
    // 0.04 m: the 2026-07-01 sim log showed runs landing at ~0.027 m but timing out because they couldn't
    // HOLD the tighter 0.03 m window for the settle time. Re-validate from a fresh log after the reset fix.
    public static final double PRECISION_TRANSLATION_TOLERANCE_METERS = 0.04;
    public static final double PRECISION_ROTATION_TOLERANCE_DEGREES = 1.5;
    public static final double PRECISION_SETTLE_SECONDS = 0.25;
    public static final double PRECISION_MAX_SPEED_METERS_PER_SECOND = 1.6;
    public static final double PRECISION_MAX_OMEGA_RADIANS_PER_SECOND = Math.toRadians(180.0);

    /**
     * Motion-profile + gains for the precision final-pose controller.
     *
     * <p>Idea traceability: 1768 Nashoba's {@code driveToPose} uses profiled PID on x/y/theta (the
     * trapezoid profile decelerates to zero velocity exactly at the goal, which is the key behavior
     * 6328's {@code DriveToPose} achieves with an explicit profile + feedforward fade). 6328 defaults
     * are ~1 cm / 1 deg tolerance; we start a touch looser and tune from logs.
     */
    public static final double PRECISION_MAX_ACCEL_METERS_PER_SECOND_SQUARED = 2.5;
    public static final double PRECISION_MAX_ANGULAR_ACCEL_RAD_PER_SECOND_SQUARED = Math.toRadians(360.0);
    public static final double PRECISION_DRIVE_KP = 3.2;
    public static final double PRECISION_DRIVE_KD = 0.0;
    public static final double PRECISION_THETA_KP = 4.5;
    public static final double PRECISION_THETA_KD = 0.0;

    /**
     * Hard safety backstop: the precision command ends (unsuccessfully) after this long even if it never
     * settles, so a bad target / obstacle cannot hang the command forever. Idea: 1768 wraps trajectory
     * accuracy commands in {@code .withTimeout(totalTime + slack)}; the research flagged the absence of
     * any timeout in Codex's version as a real bug.
     */
    public static final double PRECISION_SAFETY_TIMEOUT_SECONDS = 4.0;
  }

  /**
   * Chassis-aiming configuration. Team 999 is NOT building a turret or game-piece mechanism in this
   * prototype -- the goal is to prove we can drive and orient the whole robot precisely toward a known
   * field point. The "goal" is a configurable virtual target so the same code serves a future shooting
   * game (aim a launcher) or a placing game (square up to a scoring location); only this point moves.
   *
   * <p>Idea traceability: 6995 {@code TurretS.aimAtFieldPose} / 1768 {@code ShootingUtil} compute an
   * aim heading as the angle from the robot to a field target; 6328 {@code LaunchCalculator} and 1768
   * add a velocity-compensated "future pose" lookahead for shoot-on-move. We keep the chassis-aim +
   * lookahead math (as a teaching artifact, fully logged) but attach no mechanism.
   */
  public static final class AimConstants {
    private AimConstants() {}

    /**
     * Virtual goal to aim at, in field coordinates. Placed beyond the two-tag board (tags are at x=6.0)
     * and centered between them. Move this one constant when the real target is known.
     */
    public static final Translation2d GOAL_POSITION = new Translation2d(7.5, 2.0);

    /**
     * Which robot face points at the goal. 0 deg = front (+x) faces the goal; set to 180 deg if the
     * scoring/launching side is the back of the robot.
     */
    public static final Rotation2d ROBOT_AIM_OFFSET = Rotation2d.kZero;

    // Heading controller for auto-facing the goal while driving (DriveAndAimCommand) and for the
    // stationary AimAtGoalCommand. Idea: 1768 joystickDriveAtAngle / 6995 setAngleFieldRelative.
    public static final double AIM_HEADING_KP = 5.0;
    public static final double AIM_HEADING_KD = 0.0;
    public static final double AIM_MAX_OMEGA_RAD_PER_SEC = Math.toRadians(360.0);
    public static final double AIM_MAX_ANGULAR_ACCEL_RAD_PER_SEC_SQUARED = Math.toRadians(720.0);
    public static final double AIM_TOLERANCE_DEGREES = 1.5;
    public static final double AIM_SETTLE_SECONDS = 0.2;
    public static final double AIM_SAFETY_TIMEOUT_SECONDS = 3.0;

    /**
     * Teaching model of projectile/lead time for the shoot-on-move lookahead. With a real shooter the
     * time-of-flight would come from a distance lookup map (6328/1768); here it is a simple
     * {@code base + perMeter*distance} so the convergence loop is meaningful and unit-testable. Set
     * SHOOT_ON_MOVE_ENABLED false to aim at the static goal (placing game) instead of leading it.
     */
    public static final boolean SHOOT_ON_MOVE_ENABLED = true;
    public static final int SHOOT_ON_MOVE_ITERATIONS = 15;
    public static final double SHOOT_ON_MOVE_BASE_TOF_SECONDS = 0.25;
    public static final double SHOOT_ON_MOVE_TOF_PER_METER = 0.08;
  }
}
