package frc.robot.commands;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.SwerveConstants;
import frc.robot.subsystems.DriveSubsystem;

/**
 * Default field-relative teleop drive command.
 *
 * <p>Idea traceability:
 *
 * <p>- Keep the driver loop simple during localization bring-up. If vision corrupts the estimated
 * heading or pose, field-relative drive will make the problem visible immediately in driver feel and
 * AdvantageKit logs.
 *
 * <p>- Slow mode is included for camera/tag-board testing, where small controlled motions are more
 * useful than full-speed chassis response.
 */
public class DriveManuallyCommand extends Command {
  private final DriveSubsystem drive;
  private final DoubleSupplier xSupplier;
  private final DoubleSupplier ySupplier;
  private final DoubleSupplier omegaSupplier;
  private final BooleanSupplier slowModeSupplier;

  public DriveManuallyCommand(
      DriveSubsystem drive,
      DoubleSupplier xSupplier,
      DoubleSupplier ySupplier,
      DoubleSupplier omegaSupplier,
      BooleanSupplier slowModeSupplier) {
    this.drive = drive;
    this.xSupplier = xSupplier;
    this.ySupplier = ySupplier;
    this.omegaSupplier = omegaSupplier;
    this.slowModeSupplier = slowModeSupplier;
    addRequirements(drive);
  }

  @Override
  public void execute() {
    // Default commands also run after an autonomous command releases its subsystem.
    // Preserve the precision controller's measured wheel-angle hold in that case;
    // sending a zero FieldCentric request restores its older native steering targets.
    // b2ab/b228 show a large target-angle jump and renewed wheel motion after finish.
    if (!DriverStation.isTeleopEnabled()) {
      drive.stop();
      return;
    }
    double scale = slowModeSupplier.getAsBoolean() ? OperatorConstants.SLOW_MODE_SCALE : 1.0;
    /*
     * Xbox forward is negative Y in WPILib, so the signs are inverted here to make pushing the stick
     * forward command positive field X.
     */
    double x = -applyDeadband(xSupplier.getAsDouble()) * SwerveConstants.MAX_SPEED_METERS_PER_SECOND * scale;
    double y = -applyDeadband(ySupplier.getAsDouble()) * SwerveConstants.MAX_SPEED_METERS_PER_SECOND * scale;
    double omega = -applyDeadband(omegaSupplier.getAsDouble())
        * SwerveConstants.MAX_ANGULAR_RATE_RADIANS_PER_SECOND
        * scale;
    // A released joystick must not undo a precision stop started from a teleop button either.
    // A real driver request clears the held angles through driveFieldRelative as before.
    if (x == 0.0 && y == 0.0 && omega == 0.0) {
      drive.stop();
    } else {
      drive.driveFieldRelative(x, y, omega);
    }
  }

  @Override
  public void end(boolean interrupted) {
    drive.stop();
  }

  private static double applyDeadband(double value) {
    return MathUtil.applyDeadband(value, OperatorConstants.STICK_DEADBAND);
  }
}
