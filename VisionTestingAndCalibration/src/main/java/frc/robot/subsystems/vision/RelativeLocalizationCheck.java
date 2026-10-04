package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.SwerveConstants;
import frc.robot.Constants.VisionConstants;
import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import org.littletonrobotics.junction.Logger;

/** Disabled-only measurement notebook. Never commands motion, resets pose, or changes fusion. */
public final class RelativeLocalizationCheck extends SubsystemBase {
  private static final String LOG = "Calibration/RelativePose/";
  private static final String UI = "Localization Check/";
  private static final int SAMPLES = 100;
  private static final double TIMEOUT_SECONDS = 15.0;
  private final Vision vision;
  private final Supplier<Pose2d> fusedPose;
  private final DoubleSupplier lastResetTime;
  private final BooleanSupplier stationary;
  private final boolean[] included;
  private StationaryPose3dAccumulator[] cameraSamples;
  private StationaryPose3dAccumulator[] rawCameraSamples;
  private PoseJitterAccumulator fusedSamples;
  private Pose3d[] referenceCameras;
  private Pose2d referenceFused;
  private Pose2d[] pointRelativeCameras;
  private Pose2d pointRelativeFused;
  private double[] lastFrameTime;
  private double[] firstFrameTime;
  private int[] rejectedByNormalGate;
  private boolean active;
  private boolean recordingReference;
  private boolean referenceReady;
  private boolean measurementReady;
  private double started;
  private double resetAtReference;
  private double nextProgress;
  private double width;
  private double centerToRear;
  private long referenceId;
  private int pointIndex;

  public RelativeLocalizationCheck(Vision vision, Supplier<Pose2d> fusedPose,
      DoubleSupplier lastResetTime, BooleanSupplier stationary) {
    this.vision = vision;
    this.fusedPose = fusedPose;
    this.lastResetTime = lastResetTime;
    this.stationary = stationary;
    included = new boolean[vision.getCameraCount()];
    // Measured prototype edge-to-wheel-center = 0.075m on both sides (mentor).
    // Editable diagnostic geometry; never changes drivetrain/module geometry.
    SmartDashboard.putNumber(UI + "RearFrameWidthMeters", SwerveConstants.TRACK_WIDTH_METERS + 0.150);
    SmartDashboard.putNumber(UI + "CenterToRearFrameMeters", SwerveConstants.WHEEL_BASE_METERS / 2 + 0.075);
    for (int i = 0; i < included.length; i++) {
      SmartDashboard.putBoolean(UI + "IncludeCamera" + i, true);
    }
    SmartDashboard.putData("Localization Check - Record Start (Disabled Only)",
        Commands.runOnce(() -> start(true)).ignoringDisable(true));
    SmartDashboard.putData("Localization Check - Capture Point (Disabled Only)",
        Commands.runOnce(() -> start(false)).ignoringDisable(true));
    SmartDashboard.putData("Localization Check - Save Tape Measurements (Disabled Only)",
        Commands.runOnce(this::saveManual).ignoringDisable(true));
    SmartDashboard.putData("Localization Check - Cancel Capture (Disabled Only)",
        Commands.runOnce(() -> {
          if (DriverStation.isDisabled() && active) cancel("CANCELLED; any completed reference is retained");
        }).ignoringDisable(true));
    clearManual();
    publishState("NOT_STARTED");
    Logger.recordOutput(LOG + "SchemaVersion", "relative-pose-v1");
    publishPose("Fused", Pose2d.kZero);
    for (int i = 0; i < included.length; i++) publishPose("Camera" + i, Pose2d.kZero);
  }

  private void clearManual() {
    SmartDashboard.putNumber(UI + "RearLeftFrameX_m", Double.NaN);
    SmartDashboard.putNumber(UI + "RearRightFrameX_m", Double.NaN);
    SmartDashboard.putNumber(UI + "RearRightFrameY_m", Double.NaN);
    SmartDashboard.putBoolean(UI + "TurnWithin60DegreesConfirmed", false);
    SmartDashboard.putBoolean(UI + "ManualComparisonValid", false);
    Logger.recordOutput(LOG + "ManualComparisonValid", false);
  }

  private void start(boolean reference) {
    if (!DriverStation.isDisabled() || active || !stationary.getAsBoolean()) {
      publishState("REQUEST_REJECTED: must be disabled, stationary and not already capturing");
      return;
    }
    if (!reference && (!referenceReady || lastResetTime.getAsDouble() != resetAtReference)) {
      publishState("REQUEST_REJECTED: record a new start (missing reference or estimator was reset)");
      return;
    }
    if (reference) {
      double requestedWidth = SmartDashboard.getNumber(UI + "RearFrameWidthMeters", Double.NaN);
      double requestedRear = SmartDashboard.getNumber(UI + "CenterToRearFrameMeters", Double.NaN);
      try {
        RearCornerMeasurement.centerPose(0, 0, 0, requestedWidth, requestedRear);
      } catch (IllegalArgumentException ex) {
        publishState("REQUEST_REJECTED: " + ex.getMessage());
        return;
      }
      boolean[] requested = new boolean[included.length];
      boolean any = false;
      for (int i = 0; i < requested.length; i++) {
        requested[i] = SmartDashboard.getBoolean(UI + "IncludeCamera" + i, true);
        if (requested[i] && (i >= VisionConstants.ROBOT_TO_CAMERA_TRANSFORMS.length
            || i >= VisionConstants.CAMERA_NAMES.length)) {
          publishState("REQUEST_REJECTED: camera has no configured transform");
          return;
        }
        any |= requested[i];
      }
      if (!any) {
        publishState("REQUEST_REJECTED: select at least one camera");
        return;
      }
      System.arraycopy(requested, 0, included, 0, requested.length);
      width = requestedWidth;
      centerToRear = requestedRear;
      referenceReady = false;
      referenceId = (long) (Timer.getFPGATimestamp() * 1_000_000);
      pointIndex = 0;
      resetAtReference = lastResetTime.getAsDouble();
    } else {
      pointIndex++;
    }
    recordingReference = reference;
    measurementReady = false;
    clearManual();
    cameraSamples = new StationaryPose3dAccumulator[included.length];
    rawCameraSamples = new StationaryPose3dAccumulator[included.length];
    lastFrameTime = new double[included.length];
    firstFrameTime = new double[included.length];
    rejectedByNormalGate = new int[included.length];
    Arrays.fill(lastFrameTime, Double.NEGATIVE_INFINITY);
    Arrays.fill(firstFrameTime, Double.NaN);
    for (int i = 0; i < included.length; i++) {
      cameraSamples[i] = new StationaryPose3dAccumulator(SAMPLES);
      rawCameraSamples[i] = new StationaryPose3dAccumulator(SAMPLES);
    }
    fusedSamples = new PoseJitterAccumulator(10000); // bounded by the 15s timeout, normally <750
    started = Timer.getFPGATimestamp();
    nextProgress = started;
    active = true;
    publishState(reference ? "CAPTURING_START: keep still" : "CAPTURING_POINT: keep still");
    Logger.recordOutput(LOG + "CaptureStartTimeSeconds", started);
  }

  @Override
  public void periodic() {
    if (referenceReady && lastResetTime.getAsDouble() != resetAtReference) {
      referenceReady = false;
      measurementReady = false;
      clearManual();
      publishState("REFERENCE_INVALID: estimator reset; record a new start");
    }
    if (!active) return; // No image-rate dashboard work or added sensor polling during driving.
    double now = Timer.getFPGATimestamp();
    if (!DriverStation.isDisabled() || !stationary.getAsBoolean()
        || lastResetTime.getAsDouble() != resetAtReference) {
      cancel("CAPTURE_ABORTED: enabled, wheel/gyro motion, or estimator reset; retry while still");
      return;
    }
    if (now - started > TIMEOUT_SECONDS) {
      cancel("CAPTURE_TIMEOUT: need 100 fresh MultiTag frames from each selected camera");
      return;
    }
    // A half-second hands-off interval prevents old pre-button frames entering the window.
    if (now - started < 0.5) return;
    Pose2d fused = fusedPose.get();
    if (!Double.isFinite(fused.getX()) || !Double.isFinite(fused.getY())
        || !Double.isFinite(fused.getRotation().getRadians())) {
      cancel("CAPTURE_ABORTED: fused pose is not finite");
      return;
    }
    fusedSamples.add(fused);
    boolean ready = true;
    for (int i = 0; i < included.length; i++) {
      if (!included[i]) continue;
      var candidate = vision.getDiagnosticMultiTag(i);
      if (!cameraSamples[i].isReady() && candidate.isPresent()) {
        var obs = candidate.get();
        if (Double.isFinite(obs.timestamp()) && obs.timestamp() >= started + 0.5
            && obs.timestamp() <= now && now - obs.timestamp() <= 0.25
            && obs.timestamp() > lastFrameTime[i] && StationaryPose3dAccumulator.isFinite(obs.pose())) {
          Pose3d rawCamera = obs.pose().transformBy(VisionConstants.ROBOT_TO_CAMERA_TRANSFORMS[i]);
          if (StationaryPose3dAccumulator.isFinite(rawCamera)) {
            cameraSamples[i].add(obs.pose());
            rawCameraSamples[i].add(rawCamera);
            if (cameraSamples[i].count() == 1) firstFrameTime[i] = obs.timestamp();
            lastFrameTime[i] = obs.timestamp();
            if (VisionPolicy.rejectionReason(obs) != VisionPolicy.RejectionReason.ACCEPTED) {
              rejectedByNormalGate[i]++;
            }
          }
        }
      }
      ready &= cameraSamples[i].isReady();
    }
    if (now >= nextProgress) {
      for (int i = 0; i < included.length; i++) {
        SmartDashboard.putNumber(UI + "Camera" + i + "/Samples", cameraSamples[i].count());
        Logger.recordOutput(LOG + "Camera" + i + "/Samples", cameraSamples[i].count());
      }
      nextProgress = now + 0.1;
    }
    if (ready) complete(now);
  }

  private void complete(double now) {
    active = false;
    Pose2d fused = fusedSamples.getMeanPose();
    if (recordingReference) {
      referenceCameras = new Pose3d[included.length];
      referenceFused = fused;
      for (int i = 0; i < included.length; i++) {
        if (included[i]) referenceCameras[i] = cameraSamples[i].mean();
      }
      referenceReady = true;
    }
    pointRelativeFused = fused.relativeTo(referenceFused);
    pointRelativeCameras = new Pose2d[included.length];
    Logger.recordOutput(LOG + "CaptureEndTimeSeconds", now);
    Logger.recordOutput(LOG + "ReferenceFusedPose", referenceFused);
    Logger.recordOutput(LOG + "FusedPose", fused);
    publishPose("Fused", pointRelativeFused);
    for (int i = 0; i < included.length; i++) {
      Logger.recordOutput(LOG + "Camera" + i + "/Included", included[i]);
      if (!included[i]) continue;
      String key = LOG + "Camera" + i + "/";
      Pose3d mean = cameraSamples[i].mean();
      pointRelativeCameras[i] = mean.toPose2d().relativeTo(referenceCameras[i].toPose2d());
      publishPose("Camera" + i, pointRelativeCameras[i]);
      Logger.recordOutput(key + "RobotPoseMean3d", mean);
      Logger.recordOutput(key + "ReferenceRobotPose3d", referenceCameras[i]);
      Logger.recordOutput(key + "FieldToCameraMean3d", rawCameraSamples[i].mean());
      Logger.recordOutput(key + "FieldToCameraSamples", rawCameraSamples[i].samples());
      Logger.recordOutput(key + "RobotPoseSamples", cameraSamples[i].samples());
      Logger.recordOutput(key + "ConfiguredRobotToCamera", VisionConstants.ROBOT_TO_CAMERA_TRANSFORMS[i]);
      Logger.recordOutput(key + "Name", VisionConstants.CAMERA_NAMES[i]);
      Logger.recordOutput(key + "FirstFrameTimeSeconds", firstFrameTime[i]);
      Logger.recordOutput(key + "LastFrameTimeSeconds", lastFrameTime[i]);
      Logger.recordOutput(key + "NormalGateRejectedSamples", rejectedByNormalGate[i]);
      Logger.recordOutput(key + "Samples", cameraSamples[i].count());
      SmartDashboard.putNumber(UI + "Camera" + i + "/Samples", cameraSamples[i].count());
      SmartDashboard.putNumber(UI + "Camera" + i + "/NormalGateRejectedSamples", rejectedByNormalGate[i]);
    }
    measurementReady = !recordingReference;
    publishState(recordingReference ? "START_RECORDED: mark rear corners; then move manually"
        : "POINT_RECORDED: enter tape offsets or send reference ID + point + log");
  }

  private void saveManual() {
    if (!DriverStation.isDisabled() || active || !measurementReady || !referenceReady
        || lastResetTime.getAsDouble() != resetAtReference) {
      publishState("TAPE_REJECTED: first complete a point with a valid reference, while disabled");
      return;
    }
    try {
      if (!SmartDashboard.getBoolean(UI + "TurnWithin60DegreesConfirmed", false)) {
        throw new IllegalArgumentException("Confirm total turn from start is within +/-60 degrees.");
      }
      double leftX = SmartDashboard.getNumber(UI + "RearLeftFrameX_m", Double.NaN);
      double rightX = SmartDashboard.getNumber(UI + "RearRightFrameX_m", Double.NaN);
      double rightY = SmartDashboard.getNumber(UI + "RearRightFrameY_m", Double.NaN);
      Pose2d measured = RearCornerMeasurement.centerPose(leftX, rightX, rightY, width, centerToRear);
      Logger.recordOutput(LOG + "Manual/RearLeftFrameXMeters", leftX);
      Logger.recordOutput(LOG + "Manual/RearRightFrameXMeters", rightX);
      Logger.recordOutput(LOG + "Manual/RearRightFrameYMeters", rightY);
      publishPose("Manual", measured);
      publishError("Fused", pointRelativeFused, measured);
      for (int i = 0; i < included.length; i++) {
        if (included[i]) publishError("Camera" + i, pointRelativeCameras[i], measured);
      }
      Logger.recordOutput(LOG + "ManualComparisonValid", true);
      SmartDashboard.putBoolean(UI + "ManualComparisonValid", true);
      publishState("TAPE_SAVED_FOR_POINT_" + pointIndex + ": errors are estimate minus tape");
    } catch (IllegalArgumentException ex) {
      Logger.recordOutput(LOG + "ManualComparisonValid", false);
      SmartDashboard.putBoolean(UI + "ManualComparisonValid", false);
      publishState("TAPE_REJECTED: " + ex.getMessage());
    }
  }

  private void publishPose(String source, Pose2d pose) {
    publishNumber(source + "/RelativeXMeters", pose.getX());
    publishNumber(source + "/RelativeYMeters", pose.getY());
    publishNumber(source + "/RelativeYawDegrees", pose.getRotation().getDegrees());
    // Constructor primes pose keys before frame dimensions have been captured.
    if (width > 0) {
      double[] corners = RearCornerMeasurement.cornerOffsets(pose, width, centerToRear);
      publishNumber(source + "/RearLeftFrameXMeters", corners[0]);
      publishNumber(source + "/RearRightFrameXMeters", corners[1]);
      publishNumber(source + "/RearRightFrameYMeters", corners[2]);
    }
  }

  private void publishError(String source, Pose2d estimate, Pose2d manual) {
    publishNumber(source + "/ErrorXMeters", estimate.getX() - manual.getX());
    publishNumber(source + "/ErrorYMeters", estimate.getY() - manual.getY());
    publishNumber(source + "/ErrorYawDegrees",
        estimate.getRotation().minus(manual.getRotation()).getDegrees());
  }

  private void publishNumber(String suffix, double value) {
    Logger.recordOutput(LOG + suffix, value);
    SmartDashboard.putNumber(UI + suffix, value);
  }

  private void cancel(String reason) {
    active = false;
    measurementReady = false;
    clearManual();
    publishState(reason);
  }

  private void publishState(String status) {
    Logger.recordOutput(LOG + "Status", status);
    Logger.recordOutput(LOG + "CaptureActive", active);
    Logger.recordOutput(LOG + "ReferenceReady", referenceReady);
    Logger.recordOutput(LOG + "MeasurementReady", measurementReady);
    Logger.recordOutput(LOG + "ReferenceId", referenceId);
    Logger.recordOutput(LOG + "PointIndex", pointIndex);
    Logger.recordOutput(LOG + "RearFrameWidthMeters", width);
    Logger.recordOutput(LOG + "CenterToRearFrameMeters", centerToRear);
    SmartDashboard.putString(UI + "Status", status);
    SmartDashboard.putBoolean(UI + "CaptureActive", active);
    SmartDashboard.putBoolean(UI + "ReferenceReady", referenceReady);
    SmartDashboard.putBoolean(UI + "MeasurementReady", measurementReady);
    SmartDashboard.putNumber(UI + "ReferenceId", referenceId);
    SmartDashboard.putNumber(UI + "PointIndex", pointIndex);
  }
}
