package frc.robot.commands;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.AimConstants;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.SwerveConstants;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.util.AimingCalculator;
import frc.robot.util.AimingCalculator.AimingSolution;

/**
 * Driver keeps field-relative translation control while the robot automatically rotates to keep the aim
 * face pointed at the goal, leading its own motion (shoot-on-move). This is the "drive and stay aimed"
 * teleop primitive -- useful for a placing game (square up while approaching) or a shooting game (aim
 * while repositioning), with no turret/mechanism.
 *
 * <p>Idea traceability: 1768 {@code joystickDriveAtAngle(rotationSupplier = ShootingUtil)} and 6995
 * pose-fed aiming -- driver owns x/y, the rotation target is computed from the global pose toward the
 * goal. The velocity-compensated lead comes from {@link AimingCalculator#solveMoving}, mirroring 6328
 * {@code LaunchCalculator} / 1768 {@code ShootingUtil} shoot-on-move.
 */
public class DriveAndAimCommand extends Command {
  private final DriveSubsystem drive;
  private final DoubleSupplier xSupplier;
  private final DoubleSupplier ySupplier;
  private final BooleanSupplier slowModeSupplier;

  private final ProfiledPIDController headingController =
      new ProfiledPIDController(
          AimConstants.AIM_HEADING_KP,
          0.0,
          AimConstants.AIM_HEADING_KD,
          new TrapezoidProfile.Constraints(
              AimConstants.AIM_MAX_OMEGA_RAD_PER_SEC,
              AimConstants.AIM_MAX_ANGULAR_ACCEL_RAD_PER_SEC_SQUARED));

  public DriveAndAimCommand(
      DriveSubsystem drive,
      DoubleSupplier xSupplier,
      DoubleSupplier ySupplier,
      BooleanSupplier slowModeSupplier) {
    this.drive = drive;
    this.xSupplier = xSupplier;
    this.ySupplier = ySupplier;
    this.slowModeSupplier = slowModeSupplier;
    headingController.enableContinuousInput(-Math.PI, Math.PI);
    headingController.setTolerance(Math.toRadians(AimConstants.AIM_TOLERANCE_DEGREES));
    addRequirements(drive);
  }

  @Override
  public void initialize() {
    Pose2d pose = drive.getPose();
    headingController.reset(
        pose.getRotation().getRadians(), drive.getFieldRelativeSpeeds().omegaRadiansPerSecond);
  }

  @Override
  public void execute() {
    Pose2d pose = drive.getPose();
    double scale = slowModeSupplier.getAsBoolean() ? OperatorConstants.SLOW_MODE_SCALE : 1.0;

    // Same Xbox sign convention as DriveManuallyCommand: stick forward -> +field X.
    double vx = -MathUtil.applyDeadband(xSupplier.getAsDouble(), OperatorConstants.STICK_DEADBAND)
        * SwerveConstants.MAX_SPEED_METERS_PER_SECOND * scale;
    double vy = -MathUtil.applyDeadband(ySupplier.getAsDouble(), OperatorConstants.STICK_DEADBAND)
        * SwerveConstants.MAX_SPEED_METERS_PER_SECOND * scale;

    AimingSolution solution = AimingCalculator.solveMoving(pose, drive.getFieldRelativeSpeeds());
    Rotation2d target = solution.driveHeading();
    double omega =
        headingController.calculate(pose.getRotation().getRadians(), target.getRadians())
            + headingController.getSetpoint().velocity;
    omega = MathUtil.clamp(omega, -AimConstants.AIM_MAX_OMEGA_RAD_PER_SEC, AimConstants.AIM_MAX_OMEGA_RAD_PER_SEC);

    drive.driveFieldRelative(vx, vy, omega);

    double headingErrorDeg = Math.abs(pose.getRotation().minus(target).getDegrees());
    Logger.recordOutput("Aim/Mode", "DriveAndAim");
    Logger.recordOutput("Aim/TargetHeadingDegrees", target.getDegrees());
    Logger.recordOutput("Aim/HeadingErrorDegrees", headingErrorDeg);
    Logger.recordOutput("Aim/DistanceMeters", solution.distanceMeters());
    Logger.recordOutput("Aim/Aimed", headingErrorDeg <= AimConstants.AIM_TOLERANCE_DEGREES);
    Logger.recordOutput("Aim/LeadPose", new Pose2d(solution.leadPoint(), target));
    Logger.recordOutput("Aim/GoalPose", new Pose2d(solution.aimPoint(), Rotation2d.kZero));
  }

  @Override
  public void end(boolean interrupted) {
    drive.stop();
  }
}
