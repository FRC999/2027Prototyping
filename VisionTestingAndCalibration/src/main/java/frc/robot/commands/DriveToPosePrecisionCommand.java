package frc.robot.commands;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.AutoConstants;
import frc.robot.subsystems.DriveSubsystem;

/**
 * Final-pose controller used after manual reset, test commands, or a coarse PathPlanner move.
 *
 * <p>The command drives in field coordinates until the robot is inside a translation/rotation
 * tolerance for a continuous settle time. The settle timer prevents "instant success" when the
 * robot briefly crosses the target while still moving.
 *
 * <p>Idea traceability:
 *
 * <p>- 2910/254/1678-style autonomous architecture: let the trajectory planner handle the long
 * move, then switch to a simpler local controller that can be tuned for final scoring tolerance.
 *
 * <p>- PathPlanner interruption/handoff idea from the prompt: this command is the target for that
 * handoff. A future autonomous sequence can run a PathPlanner path until near the endpoint, cancel
 * or finish it, then schedule this command for the last few inches/degrees.
 */
public class DriveToPosePrecisionCommand extends Command {
  private final DriveSubsystem drive;
  private final Pose2d targetPose;
  private final PIDController xController = new PIDController(2.4, 0.0, 0.0);
  private final PIDController yController = new PIDController(2.4, 0.0, 0.0);
  private final PIDController thetaController = new PIDController(4.2, 0.0, 0.0);
  private final Timer settleTimer = new Timer();

  public DriveToPosePrecisionCommand(DriveSubsystem drive, Pose2d targetPose) {
    this.drive = drive;
    this.targetPose = targetPose;
    thetaController.enableContinuousInput(-Math.PI, Math.PI);
    addRequirements(drive);
  }

  @Override
  public void initialize() {
    settleTimer.reset();
    settleTimer.stop();
  }

  @Override
  public void execute() {
    Pose2d pose = drive.getPose();
    /*
     * Controllers operate in field X/Y/theta so the target pose is easy to reason about in logs.
     * The output is converted to robot-relative chassis speeds immediately before commanding CTRE.
     */
    double xSpeed = xController.calculate(pose.getX(), targetPose.getX());
    double ySpeed = yController.calculate(pose.getY(), targetPose.getY());
    double omega = thetaController.calculate(pose.getRotation().getRadians(), targetPose.getRotation().getRadians());

    xSpeed = MathUtil.clamp(xSpeed, -AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND,
        AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND);
    ySpeed = MathUtil.clamp(ySpeed, -AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND,
        AutoConstants.PRECISION_MAX_SPEED_METERS_PER_SECOND);
    omega = MathUtil.clamp(omega, -AutoConstants.PRECISION_MAX_OMEGA_RADIANS_PER_SECOND,
        AutoConstants.PRECISION_MAX_OMEGA_RADIANS_PER_SECOND);

    drive.driveRobotRelative(ChassisSpeeds.fromFieldRelativeSpeeds(xSpeed, ySpeed, omega, pose.getRotation()));

    /*
     * Require the robot to remain inside tolerance for the full settle time. This makes the command
     * more useful as an autonomous building block than a single at-goal check.
     */
    if (atGoal()) {
      if (!settleTimer.isRunning()) {
        settleTimer.restart();
      }
    } else {
      settleTimer.stop();
      settleTimer.reset();
    }
  }

  @Override
  public boolean isFinished() {
    return settleTimer.hasElapsed(AutoConstants.PRECISION_SETTLE_SECONDS);
  }

  @Override
  public void end(boolean interrupted) {
    drive.stop();
  }

  private boolean atGoal() {
    Pose2d pose = drive.getPose();
    double translationError = pose.getTranslation().getDistance(targetPose.getTranslation());
    double rotationError = Math.abs(pose.getRotation().minus(targetPose.getRotation()).getDegrees());
    return translationError <= AutoConstants.PRECISION_TRANSLATION_TOLERANCE_METERS
        && rotationError <= AutoConstants.PRECISION_ROTATION_TOLERANCE_DEGREES;
  }
}
