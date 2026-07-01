package frc.robot.subsystems.vision;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.VisionConstants;

/**
 * AprilTag localization front end. Owns frame ingestion (via {@link VisionIO}), pose validation,
 * covariance selection, timestamped fusion, and structured logging. The drivetrain only receives
 * accepted, weighted, timestamped observations through a {@link VisionConsumer}.
 *
 * <p>This rewrite is based on the official AdvantageKit PhotonVision template (also shipped by 1768
 * Nashoba) for the IO-layer + accepted/rejected logging shape, with several deliberate upgrades that
 * encode this project's whole thesis -- "fix the measurement discipline that hurt us in 2025/2026":
 *
 * <ul>
 *   <li><b>Single-tag heading is never trusted</b>: angular std-dev is {@code +Infinity} for one-tag
 *       solves. Idea: 6328 {@code Vision.java} (uses {@code Double.POSITIVE_INFINITY}) and the v2 strategy
 *       doc rule 7.4. The stock template trusts angular even for single tags -- that is exactly the
 *       circular-feedback bug the research blamed for our 2026 aiming drift.
 *   <li><b>NaN / non-finite rejection</b>: a degenerate PnP solve can emit NaN/Inf and poison CTRE's
 *       estimator. Idea: v2 strategy doc rule 7 ("NaN/infinite values"). Codex's first pass omitted this.
 *   <li><b>Per-camera std-dev factors</b>: a worse-calibrated camera counts less. Idea: 6328
 *       {@code cameras[i].stdDevFactor()}.
 *   <li><b>Innovation logging</b>: each accepted observation's distance from the current estimate is
 *       logged. Idea: a pragmatic version of 3467's n-sigma gate -- CTRE's estimator does not expose its
 *       covariance, so we log the raw innovation magnitude as the tunable signal instead of computing a
 *       true n-sigma.
 *   <li><b>Structured rejection reasons</b>: every discarded frame logs a {@link RejectionReason} enum,
 *       so log filters can count categories over a match. Idea: 3467 rejection-reason enums.
 *   <li><b>Ignore vision early in autonomous</b>: protects the known auto start pose from a bad first
 *       frame. Idea: 6328 {@code autoTimer}/{@code autoIgnoreTimeSecs}.
 * </ul>
 */
public class Vision extends SubsystemBase {
  /** Sink for accepted observations. {@code RobotContainer} wires this to CTRE's estimator. */
  @FunctionalInterface
  public static interface VisionConsumer {
    void accept(Pose2d visionRobotPose, double timestampSeconds, Matrix<N3, N1> stdDevs);
  }

  /** Stable, log-filterable categories for why a frame was discarded. Idea: 3467 reason enums. */
  public enum RejectionReason {
    ACCEPTED,
    NO_TAGS,
    NON_FINITE,
    BAD_Z,
    OUTSIDE_FIELD,
    TOO_FAR,
    SINGLE_TAG_AMBIGUOUS
  }

  private final VisionConsumer consumer;
  private final Supplier<Pose2d> robotPoseSupplier;
  private final DoubleSupplier lastResetTimeSupplier;
  private final VisionIO[] io;
  private final VisionIOInputsAutoLogged[] inputs;
  private final Alert[] disconnectedAlerts;

  // All tag poses in the layout, precomputed. Logged every loop so AdvantageScope can always draw the
  // whole board, not just the tags a camera happens to see this loop (Vision/Summary/TagPoses).
  private final Pose3d[] layoutTagPoses;

  // Restarted whenever we are NOT in enabled autonomous, so it measures "seconds since auto start".
  private final Timer autoTimer = new Timer();

  public Vision(
      VisionConsumer consumer,
      Supplier<Pose2d> robotPoseSupplier,
      DoubleSupplier lastResetTimeSupplier,
      VisionIO... io) {
    this.consumer = consumer;
    this.robotPoseSupplier = robotPoseSupplier;
    this.lastResetTimeSupplier = lastResetTimeSupplier;
    this.io = io;
    layoutTagPoses =
        VisionConstants.CUSTOM_FIELD_LAYOUT.getTags().stream()
            .map(tag -> tag.pose)
            .toArray(Pose3d[]::new);

    inputs = new VisionIOInputsAutoLogged[io.length];
    disconnectedAlerts = new Alert[io.length];
    for (int i = 0; i < io.length; i++) {
      inputs[i] = new VisionIOInputsAutoLogged();
      disconnectedAlerts[i] =
          new Alert("Vision camera " + i + " is disconnected.", AlertType.kWarning);
    }
    autoTimer.start();
  }

  /**
   * Camera-relative yaw to the best target on the given camera, or empty when the index is invalid, the
   * camera sees no target, or the bearing is stale (no fresh frame within
   * {@code TARGET_OBSERVATION_MAX_STALENESS_SECONDS}). The hook a future boresight loop would servo on.
   *
   * <p>Returns {@link Optional} (not a bare angle) so a caller cannot mistake "no/stale target" for
   * "target dead ahead (0 deg)"; {@code hasTarget} + the frame timestamp carry that distinction.
   */
  public Optional<Rotation2d> getTargetX(int cameraIndex) {
    if (cameraIndex < 0 || cameraIndex >= inputs.length) {
      return Optional.empty();
    }
    var obs = inputs[cameraIndex].latestTargetObservation;
    return freshTargetX(obs, Timer.getTimestamp());
  }

  /**
   * Whether validated vision should be fused right now, given the autonomous state and how long auto has
   * been running. Returns false only during the first {@code AUTO_VISION_IGNORE_SECONDS} of enabled
   * autonomous. Idea: 6328 early-auto vision ignore. Pure + static so it is unit-testable headlessly.
   */
  static boolean shouldAcceptDuringAuto(boolean autonomousEnabled, double secondsSinceAutoStart) {
    return !autonomousEnabled || secondsSinceAutoStart >= VisionConstants.AUTO_VISION_IGNORE_SECONDS;
  }

  /**
   * True when a vision frame's capture timestamp predates the last pose reset, so it must not be fused
   * (an in-flight frame still sees the pre-reset pose). Pure + static for headless unit testing.
   */
  static boolean isPreResetFrame(double obsTimestampSeconds, double lastResetTimeSeconds) {
    return obsTimestampSeconds < lastResetTimeSeconds;
  }

  /**
   * Whether a vision frame must be withheld because of a recent pose reset: true if its capture timestamp
   * predates the reset ({@link #isPreResetFrame}) OR we are still within {@code quarantineSeconds} of the
   * reset. The time window catches queued/latency-delayed frames whose timestamp slipped past the reset.
   * Pure + static for headless unit testing.
   */
  static boolean isResetSuppressed(
      double obsTimestampSeconds,
      double lastResetTimeSeconds,
      double nowSeconds,
      double quarantineSeconds) {
    return isPreResetFrame(obsTimestampSeconds, lastResetTimeSeconds)
        || (nowSeconds - lastResetTimeSeconds) < quarantineSeconds;
  }

  @Override
  public void periodic() {
    for (int i = 0; i < io.length; i++) {
      io[i].updateInputs(inputs[i]);
      Logger.processInputs("Vision/Camera" + i, inputs[i]);
    }

    // Idea: 6328 -- restart the timer whenever not in enabled auto; suppress vision for the first
    // AUTO_VISION_IGNORE_SECONDS of autonomous so a stray frame cannot yank the known start pose.
    if (!DriverStation.isAutonomousEnabled()) {
      autoTimer.restart();
    }
    boolean acceptDuringAuto =
        shouldAcceptDuringAuto(DriverStation.isAutonomousEnabled(), autoTimer.get());

    List<Pose3d> allAccepted = new LinkedList<>();
    List<Pose3d> allSuppressed = new LinkedList<>();
    List<Pose3d> allResetSuppressed = new LinkedList<>();
    List<Pose3d> allRejected = new LinkedList<>();
    List<Pose3d> allTagPoses = new LinkedList<>();
    Pose2d currentEstimate = robotPoseSupplier.get();
    double lastResetTime = lastResetTimeSupplier.getAsDouble();
    double now = Timer.getTimestamp();

    for (int cam = 0; cam < io.length; cam++) {
      disconnectedAlerts[cam].set(!inputs[cam].connected);

      for (int tagId : inputs[cam].tagIds) {
        VisionConstants.CUSTOM_FIELD_LAYOUT.getTagPose(tagId).ifPresent(allTagPoses::add);
      }

      int accepted = 0;
      int rejected = 0;
      for (var obs : inputs[cam].poseObservations) {
        RejectionReason reason = rejectionReason(obs);
        if (reason != RejectionReason.ACCEPTED) {
          allRejected.add(obs.pose());
          rejected++;
          Logger.recordOutput("Vision/Camera" + cam + "/LastRejectionReason", reason.toString());
          continue;
        }

        // Reset gate: discard a frame captured before the last reset (timestamp check) OR any frame during
        // a short quarantine window right after a reset. The 2026-07-01 sim log showed queued/sim-delayed
        // frames whose timestamp slipped just past the reset still bouncing the fresh pose back; the
        // quarantine catches those. Both parts are self-limiting once the vision stream tracks the new pose.
        if (isResetSuppressed(
            obs.timestamp(), lastResetTime, now, VisionConstants.RESET_QUARANTINE_SECONDS)) {
          allResetSuppressed.add(obs.pose());
          continue;
        }

        // Early-auto gate (6328): a validated frame is NOT fused during the first
        // AUTO_VISION_IGNORE_SECONDS of autonomous, so a stray early frame cannot move the known start
        // pose. Suppressed poses are logged on their OWN channel (not AcceptedPoses) so a log reader can
        // tell "validated but withheld" from "actually fused."
        if (!acceptDuringAuto) {
          allSuppressed.add(obs.pose());
          continue;
        }

        boolean trustRotation = obs.tagCount() >= 2;
        Matrix<N3, N1> stdDevs = standardDeviations(cam, obs.averageTagDistance(), obs.tagCount(), trustRotation);

        consumer.accept(obs.pose().toPose2d(), obs.timestamp(), stdDevs);
        accepted++;
        // AcceptedPoses == frames actually fused (matches the AcceptedFrames count below).
        allAccepted.add(obs.pose());

        // Pragmatic 3467-style innovation signal: how far this accepted frame pulled us.
        double innovationMeters =
            obs.pose().toPose2d().getTranslation().getDistance(currentEstimate.getTranslation());
        Logger.recordOutput("Vision/Camera" + cam + "/LastInnovationMeters", innovationMeters);
        Logger.recordOutput("Vision/Camera" + cam + "/LastAcceptedPose", obs.pose().toPose2d());
        Logger.recordOutput("Vision/Camera" + cam + "/LastTrustedRotation", trustRotation);
      }

      Logger.recordOutput("Vision/Camera" + cam + "/AcceptedFrames", accepted);
      Logger.recordOutput("Vision/Camera" + cam + "/RejectedFrames", rejected);
      Logger.recordOutput("Vision/Camera" + cam + "/Connected", inputs[cam].connected);
    }

    Logger.recordOutput("Vision/Summary/AcceptedPoses", allAccepted.toArray(Pose3d[]::new));
    Logger.recordOutput("Vision/Summary/AutoSuppressedPoses", allSuppressed.toArray(Pose3d[]::new));
    Logger.recordOutput("Vision/Summary/ResetSuppressedPoses", allResetSuppressed.toArray(Pose3d[]::new));
    Logger.recordOutput("Vision/Summary/RejectedPoses", allRejected.toArray(Pose3d[]::new));
    Logger.recordOutput("Vision/Summary/TagPoses", allTagPoses.toArray(Pose3d[]::new));
    Logger.recordOutput("Vision/Summary/AcceptingDuringAuto", acceptDuringAuto);
    // Every tag in the layout, always -- so AdvantageScope can render the whole board even when no
    // camera currently sees a tag. Add /RealOutputs/Vision/Layout/TagPoses in file replay.
    Logger.recordOutput("Vision/Layout/TagPoses", layoutTagPoses);
  }

  /** Returns {@link RejectionReason#ACCEPTED} when the observation is usable, else the failing gate. */
  static RejectionReason rejectionReason(VisionIO.PoseObservation obs) {
    if (obs.tagCount() == 0) {
      return RejectionReason.NO_TAGS;
    }
    Pose3d p = obs.pose();
    // Reject NaN/Inf in the pose AND the downstream scalars: a NaN distance would make
    // standardDeviations hand CTRE NaN std devs, and a NaN ambiguity would silently pass the ambiguity
    // gate below (any comparison with NaN is false). Idea: v2 strategy doc rule 7.
    if (!(Double.isFinite(p.getX())
        && Double.isFinite(p.getY())
        && Double.isFinite(p.getZ())
        && Double.isFinite(p.getRotation().getZ())
        && Double.isFinite(obs.averageTagDistance())
        && Double.isFinite(obs.ambiguity()))) {
      return RejectionReason.NON_FINITE;
    }
    if (Math.abs(p.getZ()) > VisionConstants.MAX_ACCEPTED_Z_METERS) {
      return RejectionReason.BAD_Z;
    }
    double x = p.getX();
    double y = p.getY();
    double m = VisionConstants.FIELD_BORDER_MARGIN_METERS;
    if (x < -m
        || x > VisionConstants.FIELD_LENGTH_METERS + m
        || y < -m
        || y > VisionConstants.FIELD_WIDTH_METERS + m) {
      return RejectionReason.OUTSIDE_FIELD;
    }
    if (obs.averageTagDistance() > VisionConstants.MAX_AVERAGE_TAG_DISTANCE_METERS) {
      return RejectionReason.TOO_FAR;
    }
    // PhotonVision ambiguity is [0,1], or -1 when it could not be computed. Reject a single-tag frame with unknown (negative) OR high ambiguity: we do not gyro-disambiguate, so an unverifiable single-tag pose could be the flipped PnP solution. (Multi-tag solves never reach this gate.)
    if (obs.tagCount() == 1 && (obs.ambiguity() < 0.0 || obs.ambiguity() > VisionConstants.MAX_SINGLE_TAG_AMBIGUITY)) {
      return RejectionReason.SINGLE_TAG_AMBIGUOUS;
    }
    return RejectionReason.ACCEPTED;
  }

  /**
   * Distance-squared / tag-count covariance with a per-camera trust factor; heading is ignored for
   * single-tag solves.
   *
   * <p>Idea traceability: 6328/Northstar and 6995 adaptive covariance ({@code k * dist^2 / tagCount^2 *
   * cameraFactor} -- tag count is SQUARED, so multi-tag solves are trusted much more); 125 NUTRONs
   * conservative single-tag heading (theta = +Infinity, never fuse one-tag rotation).
   */
  static Matrix<N3, N1> standardDeviations(
      int cameraIndex, double averageDistanceMeters, int tagCount, boolean trustRotation) {
    double distanceFactor =
        averageDistanceMeters * averageDistanceMeters / ((double) tagCount * tagCount);
    double cameraFactor =
        cameraIndex < VisionConstants.CAMERA_STD_DEV_FACTORS.length
            ? VisionConstants.CAMERA_STD_DEV_FACTORS[cameraIndex]
            : 1.0;
    double xy = VisionConstants.LINEAR_STD_DEV_BASELINE * distanceFactor * cameraFactor;
    double theta =
        trustRotation
            ? VisionConstants.ANGULAR_STD_DEV_BASELINE * distanceFactor * cameraFactor
            : Double.POSITIVE_INFINITY;
    return VecBuilder.fill(xy, xy, theta);
  }

  /**
   * Pure freshness check for {@link #getTargetX}: returns the target yaw only when a target is present
   * AND its frame timestamp is within {@code TARGET_OBSERVATION_MAX_STALENESS_SECONDS} of {@code
   * nowSeconds} in EITHER direction. Using the absolute difference also rejects a future-dated timestamp
   * (a clock glitch), so the check is fully bounded. Static (no HAL/Timer) so it is unit-testable.
   */
  static Optional<Rotation2d> freshTargetX(VisionIO.TargetObservation obs, double nowSeconds) {
    if (!obs.hasTarget()) {
      return Optional.empty();
    }
    if (Math.abs(nowSeconds - obs.timestampSeconds())
        > VisionConstants.TARGET_OBSERVATION_MAX_STALENESS_SECONDS) {
      return Optional.empty();
    }
    return Optional.of(obs.tx());
  }
}
