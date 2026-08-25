package frc.robot.commands;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.AutoConstants;
import frc.robot.subsystems.DriveSubsystem;

/**
 * Final-pose controller used after manual reset, a coarse PathPlanner/Choreo move, or directly as a
 * positioning test. Drives in field coordinates until the robot holds a translation/rotation tolerance
 * for a continuous settle time, with a hard safety timeout.
 *
 * <p>This is a substantial upgrade over Codex's first pass (plain unprofiled PID, no feedforward, no
 * logging, no timeout). Idea traceability:
 *
 * <ul>
 *   <li><b>Profiled control on x/y/theta with velocity feedforward</b> -- the trapezoid profile
 *       decelerates to zero velocity exactly at the goal, eliminating the end-of-move overshoot a raw
 *       PID produces. Idea: 1768 Nashoba {@code driveToPose} (profiled PID x/y/theta); the same
 *       deceleration behavior 6328 {@code DriveToPose} gets from an explicit profile + FF fade.
 *   <li><b>Settle gate</b> -- success requires staying inside tolerance for a continuous settle time,
 *       not just one instantaneous touch. Idea: 1768 {@code cmdWithAccuracy} settle stopwatch.
 *   <li><b>Safety timeout</b> -- ends (unsuccessfully) after a hard cap so a bad target can never hang
 *       the command. Idea: 1768 wraps accuracy commands in {@code .withTimeout(totalTime + slack)}.
 *   <li><b>Full logging</b> of target / measured / errors / settle, satisfying the AGENTS.md rule that
 *       "every precision test must log target pose, measured final pose, translation error, rotation
 *       error, and settle time." Idea: 6328 {@code DriveToPose} logs measured/setpoint/goal every loop.
 * </ul>
 *
 * <p>The coarse-trajectory -> precision handoff (6328 {@code DriveTrajectory.until(spatial).andThen(
 * DriveToPose)}) is provided by {@link #handoffFrom(edu.wpi.first.wpilibj2.command.Command,
 * java.util.function.BooleanSupplier)}.
 */
public class DriveToPosePrecisionCommand extends Command {
  private final DriveSubsystem drive;
  private final Pose2d targetPose;

  private final ProfiledPIDController xController =
      new ProfiledPIDController(
          AutoConstants.PRECISION_DRIVE_KP,
          0.0,
          AutoConstants.PRECISION_DRIVE_KD,
          new TrapezoidProfile.Constraints(
              AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND,
              AutoConstants.PRECISION_MAX_ACCEL_METERS_PER_SECOND_SQUARED));
  private final ProfiledPIDController yController =
      new ProfiledPIDController(
          AutoConstants.PRECISION_DRIVE_KP,
          0.0,
          AutoConstants.PRECISION_DRIVE_KD,
          new TrapezoidProfile.Constraints(
              AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND,
              AutoConstants.PRECISION_MAX_ACCEL_METERS_PER_SECOND_SQUARED));
  private final ProfiledPIDController thetaController =
      new ProfiledPIDController(
          AutoConstants.PRECISION_THETA_KP,
          0.0,
          AutoConstants.PRECISION_THETA_KD,
          new TrapezoidProfile.Constraints(
              AutoConstants.PRECISION_MAX_OMEGA_RADIANS_PER_SECOND,
              AutoConstants.PRECISION_MAX_ANGULAR_ACCEL_RAD_PER_SECOND_SQUARED));

  private final Timer settleTimer = new Timer();
  private final Timer safetyTimer = new Timer();
  private boolean wasWithinPoseTolerance;
  private boolean settlingHoldLatched;
  private int poseToleranceEntryCount;
  private int atGoalEntryCount;
  private int settlingHoldExitCount;

  public DriveToPosePrecisionCommand(DriveSubsystem drive, Pose2d targetPose) {
    this.drive = drive;
    this.targetPose = targetPose;
    thetaController.enableContinuousInput(-Math.PI, Math.PI);
    xController.setTolerance(AutoConstants.PRECISION_TRANSLATION_TOLERANCE_METERS);
    yController.setTolerance(AutoConstants.PRECISION_TRANSLATION_TOLERANCE_METERS);
    thetaController.setTolerance(Math.toRadians(AutoConstants.PRECISION_ROTATION_TOLERANCE_DEGREES));
    addRequirements(drive);
  }

  @Override
  public void initialize() {
    Pose2d pose = drive.getPose();
    ChassisSpeeds speeds = drive.getFieldRelativeSpeeds();
    // Seed each profile with the current state so the first command is continuous (no jump). Idea:
    // 6328 DriveToPose resets controllers with current pose + current field velocity in initialize().
    xController.reset(pose.getX(), speeds.vxMetersPerSecond);
    yController.reset(pose.getY(), speeds.vyMetersPerSecond);
    thetaController.reset(pose.getRotation().getRadians(), speeds.omegaRadiansPerSecond);
    settleTimer.stop();
    settleTimer.reset();
    safetyTimer.restart();
    wasWithinPoseTolerance = false;
    settlingHoldLatched = false;
    poseToleranceEntryCount = 0;
    atGoalEntryCount = 0;
    settlingHoldExitCount = 0;
    // Clear stale end-of-run flags so the log reflects THIS run while it is in progress.
    Logger.recordOutput("DriveToPose/Finished", false);
    Logger.recordOutput("DriveToPose/TimedOut", false);
    Logger.recordOutput("DriveToPose/Controller/Active", true);
    Logger.recordOutput("DriveToPose/Controller/DriveRequestType", "Velocity");
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredMaxSpeedMetersPerSecond",
        AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredMaxAccelerationMetersPerSecondSquared",
        AutoConstants.PRECISION_MAX_ACCEL_METERS_PER_SECOND_SQUARED);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredTranslationKp",
        AutoConstants.PRECISION_DRIVE_KP);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredTranslationKd",
        AutoConstants.PRECISION_DRIVE_KD);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredTranslationFfMinRadiusMeters",
        AutoConstants.PRECISION_TRANSLATION_FF_MIN_RADIUS_METERS);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredTranslationFfMaxRadiusMeters",
        AutoConstants.PRECISION_TRANSLATION_FF_MAX_RADIUS_METERS);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredTranslationVelocityDamping",
        AutoConstants.PRECISION_TRANSLATION_VELOCITY_DAMPING);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredRotationVelocityDamping",
        AutoConstants.PRECISION_ROTATION_VELOCITY_DAMPING);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredSettleMaxTranslationSpeedMetersPerSecond",
        AutoConstants.PRECISION_SETTLE_MAX_TRANSLATION_SPEED_METERS_PER_SECOND);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredSettleMaxRotationSpeedDegreesPerSecond",
        AutoConstants.PRECISION_SETTLE_MAX_ROTATION_SPEED_DEGREES_PER_SECOND);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredSettleEscapeTranslationMeters",
        AutoConstants.PRECISION_SETTLE_ESCAPE_TRANSLATION_METERS);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredSettleEscapeRotationDegrees",
        AutoConstants.PRECISION_SETTLE_ESCAPE_ROTATION_DEGREES);
    Logger.recordOutput(
        "DriveToPose/Controller/ConfiguredSettleSeconds",
        AutoConstants.PRECISION_SETTLE_SECONDS);
  }

  @Override
  public void execute() {
    Pose2d pose = drive.getPose();
    ChassisSpeeds measuredVelocityRobot = drive.getRobotRelativeSpeeds();
    ChassisSpeeds measuredVelocityField = drive.getFieldRelativeSpeeds();

    // Profiled PID returns the control effort; getSetpoint().velocity is the profile's feedforward
    // velocity, which trapezoids to zero at the goal. Keep the terms separate so the next log can
    // distinguish profile demand, pose-feedback correction, clamping, and low-level velocity error.
    double xFeedback = xController.calculate(pose.getX(), targetPose.getX());
    double yFeedback = yController.calculate(pose.getY(), targetPose.getY());
    double thetaFeedback =
        thetaController.calculate(pose.getRotation().getRadians(), targetPose.getRotation().getRadians());
    var xSetpoint = xController.getSetpoint();
    var ySetpoint = yController.getSetpoint();
    var thetaSetpoint = thetaController.getSetpoint();

    double translationError = pose.getTranslation().getDistance(targetPose.getTranslation());
    double translationFeedforwardScale =
        feedforwardScaleForDistance(
            translationError,
            AutoConstants.PRECISION_TRANSLATION_FF_MIN_RADIUS_METERS,
            AutoConstants.PRECISION_TRANSLATION_FF_MAX_RADIUS_METERS);

    // Fade only translation feedforward based on the robot's measured remaining distance. The
    // profile remains responsible for the smooth acceleration ramp, while the fade prevents a
    // lagging internal profile from pushing the robot forward through the physical target. Pose
    // feedback remains at full authority so it can brake immediately when the robot is ahead.
    double fadedXProfileVelocity = xSetpoint.velocity * translationFeedforwardScale;
    double fadedYProfileVelocity = ySetpoint.velocity * translationFeedforwardScale;
    double translationDampingScale = 1.0 - translationFeedforwardScale;
    double xVelocityDamping =
        velocityDamping(
            measuredVelocityField.vxMetersPerSecond,
            AutoConstants.PRECISION_TRANSLATION_VELOCITY_DAMPING,
            translationDampingScale);
    double yVelocityDamping =
        velocityDamping(
            measuredVelocityField.vyMetersPerSecond,
            AutoConstants.PRECISION_TRANSLATION_VELOCITY_DAMPING,
            translationDampingScale);
    double omegaVelocityDamping =
        velocityDamping(
            measuredVelocityField.omegaRadiansPerSecond,
            AutoConstants.PRECISION_ROTATION_VELOCITY_DAMPING,
            1.0);
    double xSpeedUnclamped = xFeedback + fadedXProfileVelocity + xVelocityDamping;
    double ySpeedUnclamped = yFeedback + fadedYProfileVelocity + yVelocityDamping;
    double omegaUnclamped = thetaFeedback + thetaSetpoint.velocity + omegaVelocityDamping;

    // Clamp translational speed as a VECTOR (see clampTranslationToMax): per-axis clamping would let a
    // diagonal command reach sqrt(2) * max. Omega is bounded separately.
    double[] clamped =
        clampTranslationToMax(
            xSpeedUnclamped,
            ySpeedUnclamped,
            AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND);
    double xSpeed = clamped[0];
    double ySpeed = clamped[1];
    double omega = MathUtil.clamp(
        omegaUnclamped,
        -AutoConstants.PRECISION_MAX_OMEGA_RADIANS_PER_SECOND,
        AutoConstants.PRECISION_MAX_OMEGA_RADIANS_PER_SECOND);

    ChassisSpeeds rawProfileVelocityField =
        new ChassisSpeeds(xSetpoint.velocity, ySetpoint.velocity, thetaSetpoint.velocity);
    ChassisSpeeds profileVelocityField =
        new ChassisSpeeds(
            fadedXProfileVelocity, fadedYProfileVelocity, thetaSetpoint.velocity);
    ChassisSpeeds feedbackVelocityField = new ChassisSpeeds(xFeedback, yFeedback, thetaFeedback);
    ChassisSpeeds dampingVelocityField =
        new ChassisSpeeds(xVelocityDamping, yVelocityDamping, omegaVelocityDamping);
    ChassisSpeeds requestedVelocityFieldUnclamped =
        new ChassisSpeeds(xSpeedUnclamped, ySpeedUnclamped, omegaUnclamped);
    ChassisSpeeds controllerRequestedVelocityField = new ChassisSpeeds(xSpeed, ySpeed, omega);
    ChassisSpeeds controllerRequestedVelocityRobot =
        ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, omega, pose.getRotation());

    double rotationErrorDeg = Math.abs(pose.getRotation().minus(targetPose.getRotation()).getDegrees());
    double measuredTranslationSpeed =
        Math.hypot(
            measuredVelocityField.vxMetersPerSecond, measuredVelocityField.vyMetersPerSecond);
    double measuredRotationSpeedDeg =
        Math.abs(Math.toDegrees(measuredVelocityField.omegaRadiansPerSecond));
    boolean withinPoseTolerance =
        translationError <= AutoConstants.PRECISION_TRANSLATION_TOLERANCE_METERS
            && rotationErrorDeg <= AutoConstants.PRECISION_ROTATION_TOLERANCE_DEGREES;
    boolean withinVelocityTolerance =
        measuredTranslationSpeed
                <= AutoConstants.PRECISION_SETTLE_MAX_TRANSLATION_SPEED_METERS_PER_SECOND
            && measuredRotationSpeedDeg
                <= AutoConstants.PRECISION_SETTLE_MAX_ROTATION_SPEED_DEGREES_PER_SECOND;
    boolean goalQualifiedThisLoop =
        withinPoseTolerance && withinVelocityTolerance;
    boolean outsideSettlingEscapeTolerance =
        exceedsSettleEscapeTolerance(
            translationError,
            rotationErrorDeg,
            AutoConstants.PRECISION_SETTLE_ESCAPE_TRANSLATION_METERS,
            AutoConstants.PRECISION_SETTLE_ESCAPE_ROTATION_DEGREES);

    if (withinPoseTolerance && !wasWithinPoseTolerance) {
      poseToleranceEntryCount++;
    }
    wasWithinPoseTolerance = withinPoseTolerance;

    // Latch the zero-velocity hold after the first pose+velocity qualification. The 4b2a639a robot
    // log entered AtGoal five times because ordinary estimator/velocity noise released the hold and
    // restarted active correction. Ignore that small noise during the 0.15 s hold; only a pose error
    // outside the wider escape envelope is allowed to resume correction.
    if (settlingHoldLatched && outsideSettlingEscapeTolerance) {
      settlingHoldLatched = false;
      settlingHoldExitCount++;
      settleTimer.stop();
      settleTimer.reset();
    }
    if (!settlingHoldLatched && goalQualifiedThisLoop) {
      settlingHoldLatched = true;
      atGoalEntryCount++;
      settleTimer.restart();
    }
    boolean atGoal = settlingHoldLatched;

    // Once position and velocity are both acceptable, hold a closed-loop zero request instead of
    // continuing to chase sub-tolerance pose noise during the settle timer. Preserve the controller's
    // pre-hold request separately in the log for diagnosis.
    ChassisSpeeds requestedVelocityField =
        atGoal ? new ChassisSpeeds() : controllerRequestedVelocityField;
    ChassisSpeeds requestedVelocityRobot =
        atGoal ? new ChassisSpeeds() : controllerRequestedVelocityRobot;
    ChassisSpeeds trackingErrorRobot =
        new ChassisSpeeds(
            requestedVelocityRobot.vxMetersPerSecond - measuredVelocityRobot.vxMetersPerSecond,
            requestedVelocityRobot.vyMetersPerSecond - measuredVelocityRobot.vyMetersPerSecond,
            requestedVelocityRobot.omegaRadiansPerSecond - measuredVelocityRobot.omegaRadiansPerSecond);

    drive.driveRobotRelativeVelocity(requestedVelocityRobot);

    Logger.recordOutput("DriveToPose/TargetPose", targetPose);
    Logger.recordOutput("DriveToPose/MeasuredPose", pose);
    Logger.recordOutput("DriveToPose/TranslationErrorMeters", translationError);
    Logger.recordOutput("DriveToPose/RotationErrorDegrees", rotationErrorDeg);
    Logger.recordOutput("DriveToPose/WithinPoseTolerance", withinPoseTolerance);
    Logger.recordOutput("DriveToPose/WithinVelocityTolerance", withinVelocityTolerance);
    Logger.recordOutput("DriveToPose/GoalQualifiedThisLoop", goalQualifiedThisLoop);
    Logger.recordOutput(
        "DriveToPose/OutsideSettlingEscapeTolerance", outsideSettlingEscapeTolerance);
    Logger.recordOutput("DriveToPose/AtGoal", atGoal);
    Logger.recordOutput("DriveToPose/SettlingHoldActive", settlingHoldLatched);
    Logger.recordOutput("DriveToPose/PoseToleranceEntryCount", poseToleranceEntryCount);
    Logger.recordOutput("DriveToPose/AtGoalEntryCount", atGoalEntryCount);
    Logger.recordOutput("DriveToPose/SettlingHoldExitCount", settlingHoldExitCount);
    Logger.recordOutput(
        "DriveToPose/MeasuredTranslationSpeedMetersPerSecond", measuredTranslationSpeed);
    Logger.recordOutput(
        "DriveToPose/MeasuredRotationSpeedDegreesPerSecond", measuredRotationSpeedDeg);
    Logger.recordOutput("DriveToPose/SettleSeconds", settleTimer.get());
    Logger.recordOutput("DriveToPose/ErrorXFieldMeters", targetPose.getX() - pose.getX());
    Logger.recordOutput("DriveToPose/ErrorYFieldMeters", targetPose.getY() - pose.getY());
    Logger.recordOutput(
        "DriveToPose/ErrorThetaSignedDegrees",
        targetPose.getRotation().minus(pose.getRotation()).getDegrees());
    Logger.recordOutput(
        "DriveToPose/Controller/ProfileSetpointPose",
        new Pose2d(
            xSetpoint.position,
            ySetpoint.position,
            new Rotation2d(thetaSetpoint.position)));
    Logger.recordOutput("DriveToPose/Controller/RawProfileVelocityField", rawProfileVelocityField);
    Logger.recordOutput("DriveToPose/Controller/ProfileVelocityField", profileVelocityField);
    Logger.recordOutput("DriveToPose/Controller/FeedbackVelocityField", feedbackVelocityField);
    Logger.recordOutput("DriveToPose/Controller/DampingVelocityField", dampingVelocityField);
    Logger.recordOutput(
        "DriveToPose/Controller/RequestedVelocityFieldUnclamped",
        requestedVelocityFieldUnclamped);
    Logger.recordOutput(
        "DriveToPose/Controller/ControllerRequestedVelocityField",
        controllerRequestedVelocityField);
    Logger.recordOutput(
        "DriveToPose/Controller/ControllerRequestedVelocityRobot",
        controllerRequestedVelocityRobot);
    Logger.recordOutput("DriveToPose/Controller/RequestedVelocityField", requestedVelocityField);
    Logger.recordOutput("DriveToPose/Controller/RequestedVelocityRobot", requestedVelocityRobot);
    Logger.recordOutput("DriveToPose/Controller/MeasuredVelocityField", measuredVelocityField);
    Logger.recordOutput("DriveToPose/Controller/MeasuredVelocityRobot", measuredVelocityRobot);
    Logger.recordOutput("DriveToPose/Controller/VelocityTrackingErrorRobot", trackingErrorRobot);
    Logger.recordOutput(
        "DriveToPose/Controller/DistanceToTargetMeters", translationError);
    Logger.recordOutput(
        "DriveToPose/Controller/TranslationFeedforwardScale", translationFeedforwardScale);
    Logger.recordOutput(
        "DriveToPose/Controller/TranslationDampingScale", translationDampingScale);
    Logger.recordOutput(
        "DriveToPose/Controller/DampingVxFieldMetersPerSecond", xVelocityDamping);
    Logger.recordOutput(
        "DriveToPose/Controller/DampingVyFieldMetersPerSecond", yVelocityDamping);
    Logger.recordOutput(
        "DriveToPose/Controller/DampingOmegaDegreesPerSecond",
        Math.toDegrees(omegaVelocityDamping));
    Logger.recordOutput(
        "DriveToPose/Controller/ControllerRequestedVxRobotMetersPerSecond",
        controllerRequestedVelocityRobot.vxMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/ControllerRequestedOmegaDegreesPerSecond",
        Math.toDegrees(controllerRequestedVelocityRobot.omegaRadiansPerSecond));
    Logger.recordOutput(
        "DriveToPose/Controller/RawProfileVxFieldMetersPerSecond",
        rawProfileVelocityField.vxMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/RawProfileVyFieldMetersPerSecond",
        rawProfileVelocityField.vyMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/ProfileVxFieldMetersPerSecond",
        profileVelocityField.vxMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/FeedbackVxFieldMetersPerSecond",
        feedbackVelocityField.vxMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/ProfileVyFieldMetersPerSecond",
        profileVelocityField.vyMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/FeedbackVyFieldMetersPerSecond",
        feedbackVelocityField.vyMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/ProfileOmegaDegreesPerSecond",
        Math.toDegrees(profileVelocityField.omegaRadiansPerSecond));
    Logger.recordOutput(
        "DriveToPose/Controller/FeedbackOmegaDegreesPerSecond",
        Math.toDegrees(feedbackVelocityField.omegaRadiansPerSecond));
    Logger.recordOutput(
        "DriveToPose/Controller/RequestedVxRobotMetersPerSecond",
        requestedVelocityRobot.vxMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/MeasuredVxRobotMetersPerSecond",
        measuredVelocityRobot.vxMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/TrackingErrorVxRobotMetersPerSecond",
        trackingErrorRobot.vxMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/RequestedVyRobotMetersPerSecond",
        requestedVelocityRobot.vyMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/MeasuredVyRobotMetersPerSecond",
        measuredVelocityRobot.vyMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/TrackingErrorVyRobotMetersPerSecond",
        trackingErrorRobot.vyMetersPerSecond);
    Logger.recordOutput(
        "DriveToPose/Controller/RequestedOmegaDegreesPerSecond",
        Math.toDegrees(requestedVelocityRobot.omegaRadiansPerSecond));
    Logger.recordOutput(
        "DriveToPose/Controller/MeasuredOmegaDegreesPerSecond",
        Math.toDegrees(measuredVelocityRobot.omegaRadiansPerSecond));
    Logger.recordOutput(
        "DriveToPose/Controller/TrackingErrorOmegaDegreesPerSecond",
        Math.toDegrees(trackingErrorRobot.omegaRadiansPerSecond));
    Logger.recordOutput(
        "DriveToPose/Controller/TranslationVelocityTrackingErrorMetersPerSecond",
        Math.hypot(trackingErrorRobot.vxMetersPerSecond, trackingErrorRobot.vyMetersPerSecond));
    Logger.recordOutput(
        "DriveToPose/Controller/TranslationCommandClamped",
        Math.hypot(xSpeedUnclamped, ySpeedUnclamped)
            > AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND);
    Logger.recordOutput(
        "DriveToPose/Controller/RotationCommandClamped",
        Math.abs(omegaUnclamped) > AutoConstants.PRECISION_MAX_OMEGA_RADIANS_PER_SECOND);
  }

  @Override
  public boolean isFinished() {
    return settleTimer.hasElapsed(AutoConstants.PRECISION_SETTLE_SECONDS)
        || safetyTimer.hasElapsed(AutoConstants.PRECISION_SAFETY_TIMEOUT_SECONDS);
  }

  @Override
  public void end(boolean interrupted) {
    drive.stop();
    Logger.recordOutput("DriveToPose/Controller/Active", false);
    Logger.recordOutput("DriveToPose/Finished", true);
    Logger.recordOutput("DriveToPose/TimedOut",
        safetyTimer.hasElapsed(AutoConstants.PRECISION_SAFETY_TIMEOUT_SECONDS)
            && !settleTimer.hasElapsed(AutoConstants.PRECISION_SETTLE_SECONDS));
  }

  /**
   * Builds the coarse-then-precise handoff: run {@code coarse} (a PathPlanner/Choreo path) until
   * {@code handoffCondition} becomes true (e.g. crossing a spatial line near the endpoint), then bail
   * out of the timed path and finish on this position-tolerance controller.
   *
   * <p>Idea traceability: 6328 {@code AutoCommands}: {@code new DriveTrajectory(...).until(spatial)
   * .andThen(new DriveToPose(...).until(withinTolerance))}. A time-based path should never be what
   * *finishes* a precise move.
   */
  public Command handoffFrom(Command coarse, java.util.function.BooleanSupplier handoffCondition) {
    return coarse.until(handoffCondition).andThen(this);
  }

  /**
   * Scales a field translation velocity {@code (xSpeed, ySpeed)} so its magnitude never exceeds
   * {@code maxSpeed}, preserving direction; returns {@code {x, y}}. Clamping each axis independently
   * would allow {@code sqrt(2) * maxSpeed} on a diagonal. Static + pure so it is unit-testable.
   */
  static double[] clampTranslationToMax(double xSpeed, double ySpeed, double maxSpeed) {
    double norm = Math.hypot(xSpeed, ySpeed);
    if (norm > maxSpeed) {
      double scale = maxSpeed / norm;
      return new double[] {xSpeed * scale, ySpeed * scale};
    }
    return new double[] {xSpeed, ySpeed};
  }

  /**
   * Returns a linear 0..1 velocity-feedforward scale based on measured remaining distance. The scale
   * is zero inside {@code minRadius}, one beyond {@code maxRadius}, and linear between them. Static +
   * pure so the target-crossing behavior is pinned by unit tests.
   */
  static double feedforwardScaleForDistance(double distance, double minRadius, double maxRadius) {
    if (maxRadius <= minRadius) {
      throw new IllegalArgumentException("maxRadius must be greater than minRadius");
    }
    return MathUtil.clamp((Math.max(0.0, distance) - minRadius) / (maxRadius - minRadius), 0.0, 1.0);
  }

  /**
   * Opposes measured velocity by {@code gain * scale}. A scale of zero leaves cruise motion
   * unchanged; a scale of one applies full near-target damping. Static + pure for unit coverage.
   */
  static double velocityDamping(double measuredVelocity, double gain, double scale) {
    return -measuredVelocity * Math.max(0.0, gain) * MathUtil.clamp(scale, 0.0, 1.0);
  }

  /** Returns true only when a latched settling hold must be abandoned for active pose correction. */
  static boolean exceedsSettleEscapeTolerance(
      double translationError,
      double rotationErrorDegrees,
      double translationEscapeMeters,
      double rotationEscapeDegrees) {
    return translationError > translationEscapeMeters
        || rotationErrorDegrees > rotationEscapeDegrees;
  }
}
