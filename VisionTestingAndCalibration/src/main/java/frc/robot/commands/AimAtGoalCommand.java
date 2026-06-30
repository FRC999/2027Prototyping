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
import frc.robot.Constants.AimConstants;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.util.AimingCalculator;
import frc.robot.util.AimingCalculator.AimingSolution;

/**
 * Rotates the stationary chassis to face the configured field goal and holds within tolerance for a
 * settle time (with a safety timeout). No translation, no mechanism -- this is the "square up to a known
 * field point" primitive a placing game needs and a shooting game uses for a stationary shot.
 *
 * <p>Idea traceability: 6995 {@code TurretS.setAngleFieldRelative} / 1768 {@code atShootingSetpoint}
 * gate the shot on being at the aim heading. The {@code poseBearing - aimHeading} residual logged here is
 * the independent aiming check the v2 strategy doc (section 10) says our 2026 robot lacked.
 */
public class AimAtGoalCommand extends Command {
  private final DriveSubsystem drive;
  private final ProfiledPIDController headingController =
      new ProfiledPIDController(
          AimConstants.AIM_HEADING_KP,
          0.0,
          AimConstants.AIM_HEADING_KD,
          new TrapezoidProfile.Constraints(
              AimConstants.AIM_MAX_OMEGA_RAD_PER_SEC,
              AimConstants.AIM_MAX_ANGULAR_ACCEL_RAD_PER_SEC_SQUARED));
  private final Timer settleTimer = new Timer();
  private final Timer safetyTimer = new Timer();

  public AimAtGoalCommand(DriveSubsystem drive) {
    this.drive = drive;
    headingController.enableContinuousInput(-Math.PI, Math.PI);
    headingController.setTolerance(Math.toRadians(AimConstants.AIM_TOLERANCE_DEGREES));
    addRequirements(drive);
  }

  @Override
  public void initialize() {
    Pose2d pose = drive.getPose();
    headingController.reset(
        pose.getRotation().getRadians(), drive.getFieldRelativeSpeeds().omegaRadiansPerSecond);
    settleTimer.stop();
    settleTimer.reset();
    safetyTimer.restart();
  }

  @Override
  public void execute() {
    Pose2d pose = drive.getPose();
    AimingSolution solution = AimingCalculator.solveStationary(pose);
    Rotation2d target = solution.driveHeading();

    double omega =
        headingController.calculate(pose.getRotation().getRadians(), target.getRadians())
            + headingController.getSetpoint().velocity;
    omega = MathUtil.clamp(omega, -AimConstants.AIM_MAX_OMEGA_RAD_PER_SEC, AimConstants.AIM_MAX_OMEGA_RAD_PER_SEC);
    drive.driveRobotRelative(new ChassisSpeeds(0.0, 0.0, omega));

    double headingErrorDeg = Math.abs(pose.getRotation().minus(target).getDegrees());
    boolean aimed = headingErrorDeg <= AimConstants.AIM_TOLERANCE_DEGREES;
    if (aimed) {
      if (!settleTimer.isRunning()) {
        settleTimer.restart();
      }
    } else {
      settleTimer.stop();
      settleTimer.reset();
    }

    Logger.recordOutput("Aim/Mode", "Stationary");
    Logger.recordOutput("Aim/TargetHeadingDegrees", target.getDegrees());
    Logger.recordOutput("Aim/HeadingErrorDegrees", headingErrorDeg);
    Logger.recordOutput("Aim/DistanceMeters", solution.distanceMeters());
    Logger.recordOutput("Aim/Aimed", aimed);
    Logger.recordOutput("Aim/GoalPose", new Pose2d(solution.aimPoint(), Rotation2d.kZero));
  }

  @Override
  public boolean isFinished() {
    return settleTimer.hasElapsed(AimConstants.AIM_SETTLE_SECONDS)
        || safetyTimer.hasElapsed(AimConstants.AIM_SAFETY_TIMEOUT_SECONDS);
  }

  @Override
  public void end(boolean interrupted) {
    drive.stop();
  }
}
