package frc.robot.subsystems.vision;

import java.util.function.Supplier;

import org.photonvision.simulation.PhotonCameraSim;
import org.photonvision.simulation.SimCameraProperties;
import org.photonvision.simulation.VisionSystemSim;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;
import frc.robot.Constants.VisionConstants;

/**
 * Simulated PhotonVision camera IO. THIS IS THE KEYSTONE the prototype was missing.
 *
 * <p>Codex's {@code SIMULATION_RUNBOOK.md} admitted vision produced zero frames in simulation, so no
 * localization or aiming improvement could be validated on a desktop. PhotonLib ships a full physics
 * simulator for exactly this, and the wiring is tiny.
 *
 * <p>Idea traceability: 1768 Nashoba {@code VisionIOPhotonVisionSim} (the official AdvantageKit template)
 * for the {@link VisionSystemSim}/{@link PhotonCameraSim} pattern, plus 3467 Windham
 * {@code VisionIOPhotonVisionSim} for the practice of giving the sim camera <em>real</em> properties
 * (resolution, FOV, FPS, latency, calibration error) so simulated frames behave like the actual
 * Arducam OV9782 instead of a perfect camera.
 *
 * <p>How it works: one shared {@link VisionSystemSim} holds the AprilTag layout. Each loop we feed it the
 * true simulated robot pose; it renders what each {@link PhotonCameraSim} would see and pushes synthetic
 * results back through the very same {@link PhotonCamera} NetworkTables path the real pipeline uses. The
 * production {@link VisionIOPhotonVision#updateInputs} code then runs unchanged -- so simulation exercises
 * the real ingestion and fusion path, not a mock.
 */
public class VisionIOPhotonVisionSim extends VisionIOPhotonVision {
  // One simulated world shared by all cameras (static so multiple cameras populate the same field).
  private static VisionSystemSim visionSim;

  private final Supplier<Pose2d> poseSupplier;

  public VisionIOPhotonVisionSim(
      String name, Transform3d robotToCamera, Supplier<Pose2d> poseSupplier) {
    super(name, robotToCamera);
    this.poseSupplier = poseSupplier;

    if (visionSim == null) {
      visionSim = new VisionSystemSim("main");
      visionSim.addAprilTags(VisionConstants.CUSTOM_FIELD_LAYOUT);
    }

    // Model the Arducam OV9782: 1280x800 global shutter, low-distortion M12 lens.
    // Idea: 3467 configures SimCameraProperties to match the real sensor so the sim is honest about
    // resolution, frame rate, latency, and calibration noise instead of assuming a perfect camera.
    var props = new SimCameraProperties();
    props.setCalibration(
        VisionConstants.SIM_CAMERA_WIDTH_PX,
        VisionConstants.SIM_CAMERA_HEIGHT_PX,
        Rotation2d.fromDegrees(VisionConstants.SIM_CAMERA_DIAGONAL_FOV_DEGREES));
    props.setCalibError(
        VisionConstants.SIM_CAMERA_AVG_PX_ERROR, VisionConstants.SIM_CAMERA_PX_ERROR_STD_DEV);
    props.setFPS(VisionConstants.SIM_CAMERA_FPS);
    props.setAvgLatencyMs(VisionConstants.SIM_CAMERA_AVG_LATENCY_MS);
    props.setLatencyStdDevMs(VisionConstants.SIM_CAMERA_LATENCY_STD_DEV_MS);

    var cameraSim = new PhotonCameraSim(camera, props, VisionConstants.CUSTOM_FIELD_LAYOUT);
    visionSim.addCamera(cameraSim, robotToCamera);
  }

  @Override
  public void updateInputs(VisionIOInputs inputs) {
    // Render the world from the current true robot pose, then let the real ingestion code read it.
    visionSim.update(poseSupplier.get());
    super.updateInputs(inputs);
  }
}
