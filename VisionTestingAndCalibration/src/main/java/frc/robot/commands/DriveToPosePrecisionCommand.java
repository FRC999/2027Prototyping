package frc.robot.commands;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
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
  }

  @Override
  public void execute() {
    Pose2d pose = drive.getPose();

    // Profiled PID returns the control effort; getSetpoint().velocity is the profile's feedforward
    // velocity, which trapezoids to zero at the goal (this is what kills overshoot).
    double xSpeed = xController.calculate(pose.getX(), targetPose.getX()) + xController.getSetpoint().velocity;
    double ySpeed = yController.calculate(pose.getY(), targetPose.getY()) + yController.getSetpoint().velocity;
    double omega =
        thetaController.calculate(pose.getRotation().getRadians(), targetPose.getRotation().getRadians())
            + thetaController.getSetpoint().velocity;

    xSpeed = MathUtil.clamp(xSpeed, -AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND,
        AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND);
    ySpeed = MathUtil.clamp(ySpeed, -AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND,
        AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND);
    omega = MathUtil.clamp(omega, -AutoConstants.PRECISION_MAX_OMEGA_RADIANS_PER_SECOND,
        AutoConstants.PRECISION_MAX_OMEGA_RADIANS_PER_SECOND);

    drive.driveRobotRelative(ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, omega, pose.getRotation()));

    double translationError = pose.getTranslation().getDistance(targetPose.getTranslation());
    double rotationErrorDeg = Math.abs(pose.getRotation().minus(targetPose.getRotation()).getDegrees());
    boolean atGoal =
        translationError <= AutoConstants.PRECISION_TRANSLATION_TOLERANCE_METERS
            && rotationErrorDeg <= AutoConstants.PRECISION_ROTATION_TOLERANCE_DEGREES;

    // Require staying inside tolerance for the full settle time (1768 settle stopwatch).
    if (atGoal) {
      if (!settleTimer.isRunning()) {
        settleTimer.restart();
      }
    } else {
      settleTimer.stop();
      settleTimer.reset();
    }

    Logger.recordOutput("DriveToPose/TargetPose", targetPose);
    Logger.recordOutput("DriveToPose/MeasuredPose", pose);
    Logger.recordOutput("DriveToPose/TranslationErrorMeters", translationError);
    Logger.recordOutput("DriveToPose/RotationErrorDegrees", rotationErrorDeg);
    Logger.recordOutput("DriveToPose/AtGoal", atGoal);
    Logger.recordOutput("DriveToPose/SettleSeconds", settleTimer.get());
  }

  @Override
  public boolean isFinished() {
    return settleTimer.hasElapsed(AutoConstants.PRECISION_SETTLE_SECONDS)
        || safetyTimer.hasElapsed(AutoConstants.PRECISION_SAFETY_TIMEOUT_SECONDS);
  }

  @Override
  public void end(boolean interrupted) {
    drive.stop();
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
}
