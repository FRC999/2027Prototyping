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
import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
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
    public static final double ODOMETRY_UPDATE_FREQUENCY_HZ = 100.0;
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

    public static final String FRONT_LEFT_CAMERA_NAME = "front-left";
    public static final String FRONT_RIGHT_CAMERA_NAME = "front-right";

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
    public static final double SINGLE_TAG_THETA_STD_DEV = 999999.0;
    public static final double BASE_XY_STD_DEV = 0.05;
    public static final double BASE_THETA_STD_DEV = 0.12;

    public static final Matrix<N3, N1> FALLBACK_STD_DEVS =
        VecBuilder.fill(1.0, 1.0, SINGLE_TAG_THETA_STD_DEV);

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
    public static final double PRECISION_TRANSLATION_TOLERANCE_METERS = 0.035;
    public static final double PRECISION_ROTATION_TOLERANCE_DEGREES = 2.0;
    public static final double PRECISION_SETTLE_SECONDS = 0.25;
    public static final double PRECISION_MAX_SPEED_METERS_PER_SECOND = 1.1;
    public static final double PRECISION_MAX_OMEGA_RADIANS_PER_SECOND = Math.toRadians(130.0);
  }
}
