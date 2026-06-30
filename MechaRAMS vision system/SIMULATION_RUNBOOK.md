# Simulation Runbook

This document explains how to run the current `VisionTestingAndCalibration` project in simulation and what the simulation can and cannot prove today.

## Current Simulation Status

Implemented now:

- WPILib desktop simulation entry point.
- CTRE swerve simulation update loop in `DriveSubsystem`.
- Xbox/Driver Station controls in simulation.
- AdvantageKit logging to `logs/sim`.
- NT4 publishing for live AdvantageScope.
- PathPlanner auto chooser fallback that does not crash if `VisionTest` is missing.

Implemented as of the 2026-06-30 Claude rebuild (previously missing):

- **Synthetic PhotonVision AprilTag frames in simulation** via `VisionIOPhotonVisionSim`
  (`VisionSystemSim` + `PhotonCameraSim`), fed the true sim pose each loop and modeled on the Arducam
  OV9782 (resolution/FOV/FPS/latency). Vision now produces accepted/rejected frames in pure sim.
- The full localization, precision-drive, and chassis-aiming pipeline is now desktop-testable.

Still not implemented (intentionally):

- Simulated raw camera *images* (we simulate detections/poses, which is what fusion needs).
- A real shooter/turret mechanism (out of scope — chassis aiming only).

Meaning: you can now validate AprilTag fusion quality, precision driving, and aiming entirely in
simulation. See `CALIBRATION_AND_TEST_PROCESS.md` Stage 1 for the exact sim test steps and
`ADVANTAGESCOPE_SETUP.md` for visualizing the robot + tags + vision ghosts on a field.

## One-Time Setup

### Use Java 17 Or Newer

The normal shell on this machine may still resolve Java 11:

`T:\Program Files\Amazon Corretto\jdk11.0.20_8`

WPILib 2026 requires Java 17 or newer. The WPILib 2026 bundled JDK is installed here:

`C:\Users\Public\wpilib\2026\jdk`

Click-by-click:

1. Open Windows Start.
2. Type `Environment Variables`.
3. Click `Edit the system environment variables`.
4. Click `Environment Variables`.
5. Under user variables, select `JAVA_HOME`.
6. Click `Edit`.
7. Set it to:
   `C:\Users\Public\wpilib\2026\jdk`
8. Click `OK`.
9. Click `OK`.
10. Close all terminals and VS Code windows.
11. Reopen a terminal.
12. Run:

```powershell
java -version
```

Expected result: version 17 or newer.

Temporary PowerShell option without changing Windows settings:

```powershell
$env:JAVA_HOME='C:\Users\Public\wpilib\2026\jdk'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

## Compile First

1. Open PowerShell.
2. Run:

```powershell
cd 'S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration'
.\gradlew.bat compileJava
```

3. If compile fails because of Java version, fix Java first.
4. If compile fails because of API mismatches, give the errors to Codex/Claude and ask it to repair against WPILib 2026, Phoenix 6, PhotonLib, PathPlanner, and AdvantageKit APIs.

Do not deploy or trust simulation behavior until `compileJava` succeeds.

## Run Robot Simulation From VS Code

Click-by-click:

1. Open WPILib VS Code.
2. Click `File`.
3. Click `Open Folder...`.
4. Select:
   `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration`
5. Click `Select Folder`.
6. Wait for Gradle/WPILib indexing to finish.
7. Press `Ctrl+Shift+P`.
8. Type `WPILib: Simulate Robot Code`.
9. Click `WPILib: Simulate Robot Code`.
10. If prompted for extension options, choose `Sim GUI` and `Driver Station`.
11. Wait for the simulator and simulated Driver Station to open.

Expected:

- The robot program starts.
- AdvantageKit creates logs under:
  `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\logs\sim`
- NT4 data is available to AdvantageScope.

## Run Robot Simulation From Terminal

1. Open PowerShell.
2. Run:

```powershell
cd 'S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration'
.\gradlew.bat simulateJava
```

3. If simulation GUI does not open, run from WPILib VS Code instead.

## Driver Station Simulation

Click-by-click:

1. In the simulated Driver Station window, confirm robot code is connected.
2. Select `Teleoperated`.
3. Click `Enable`.
4. Use an Xbox controller on USB, or use simulation joystick controls if configured.
5. Test controls:
   - Left stick Y: forward/back.
   - Left stick X: strafe.
   - Right stick X: rotate.
   - Right bumper: slow mode.
   - `A`: reset pose to `(1.5 m, 2.0 m, 0 deg)`.
   - `X`: run precision drive to `(4.25 m, 2.0 m, 0 deg)`.
   - `Y`: stop drivetrain while held.
   - Right stick press: stationary aim at the goal.
   - Right trigger held: drive (left stick) while auto-facing the goal (shoot-on-move).

If no controller is detected:

1. Click the Driver Station joystick tab.
2. Verify an Xbox controller appears.
3. If it does not, unplug/replug the controller.
4. Restart simulation if needed.

## AdvantageScope Live View

Click-by-click:

1. Open AdvantageScope.
2. Click `File`.
3. Click `Connect to Robot`.
4. Select NetworkTables/NT4.
5. Use the local simulation server, usually `localhost`.
6. Add a `3D Field` tab; set `Drive/Pose` as the robot, add `Vision/Summary/TagPoses` (tags) and
   `Vision/Summary/AcceptedPoses` (ghost). See `ADVANTAGESCOPE_SETUP.md` for the robot model.
7. Add line graphs for:
   - `Vision/Camera0/AcceptedFrames`, `Vision/Camera0/RejectedFrames`
   - `Vision/Camera1/AcceptedFrames`, `Vision/Camera1/RejectedFrames`
   - `Vision/Camera0/LastInnovationMeters`
   - `DriveToPose/TranslationErrorMeters`, `Aim/HeadingErrorDegrees`

Vision now produces synthetic frames in simulation (`VisionIOPhotonVisionSim`), so accepted frames
should climb once the robot faces the tag board — you no longer need hardware to see fusion working.

## AdvantageScope Log Replay

Click-by-click:

1. Run a short simulation session.
2. Disable the robot.
3. Close simulation.
4. Open AdvantageScope.
5. Click `File`.
6. Click `Open Log...`.
7. Browse to:
   `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\logs\sim`
8. Select the newest `.wpilog`.
9. Add plots/field views.
10. Check command behavior and timing.

## Simulated Precision Test

This tests the final-pose command logic without real vision corrections.

1. Start simulation.
2. Enable Teleop.
3. Press `A` to reset pose.
4. Press `X` to run precision drive.
5. Watch the robot pose in AdvantageScope.
6. Verify the command stops near `(4.25 m, 2.0 m, 0 deg)`.
7. Save the log.

What this proves:

- Command scheduling works.
- CTRE simulated drivetrain updates pose.
- Precision controller drives toward the target.
- AdvantageKit logs are produced.

What this does not prove:

- Camera calibration.
- PhotonVision target detection.
- Timestamp correctness for real camera observations.
- Real drivetrain characterization.

## Simulated PathPlanner Auto

A starter `VisionTest` path + auto now ships in `src/main/deploy/pathplanner/` (straight move from
`(1.5, 2.0)` to `(3.6, 2.0)`), and the chooser also offers "VisionTest then Precision (sequential)" and
"VisionTest (spatial handoff)", both finishing on `DriveToPosePrecisionCommand`. To edit/recreate in the GUI:

Click-by-click:

1. Open PathPlanner.
2. Click `Open Project`.
3. Select:
   `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration`
4. Confirm robot settings load from:
   `src/main/deploy/pathplanner/settings.json`
5. Create a new path from `(1.5, 2.0, 0 deg)` to roughly `(3.6, 2.0, 0 deg)`.
6. Use conservative constraints:
   - Max velocity: `1.6 m/s`
   - Max acceleration: `1.2 m/s^2`
   - Max angular velocity: `120 deg/s`
   - Max angular acceleration: `180 deg/s^2`
7. Create a new auto named exactly:
   `VisionTest`
8. Add the path to the auto.
9. Save.
10. Return to VS Code.
11. Run simulation.
12. In SmartDashboard/Shuffleboard, select `PathPlanner Auto: VisionTest` if available.
13. Enable Autonomous.

After the coarse auto, test the final precision command separately with `X` or the dashboard command.

## PhotonVision Simulation (implemented)

AprilTag simulation is now implemented in `VisionIOPhotonVisionSim` (selected automatically in sim):

1. A shared `VisionSystemSim` is loaded with `VisionConstants.CUSTOM_FIELD_LAYOUT`.
2. Each camera adds a `PhotonCameraSim` with its transform and an OV9782-modeled `SimCameraProperties`.
3. Each loop the simulator is fed the true drivetrain pose (`visionSim.update(poseSupplier.get())`).
4. The real ingestion path (`VisionIOPhotonVision.updateInputs`) then runs unchanged, so the `Vision`
   subsystem fuses simulated results exactly as it would real ones.
5. AdvantageScope shows accepted/rejected simulated observations (`Vision/Summary/*`).

This is compile- and unit-tested (`VisionPolicyTest`). See `CALIBRATION_AND_TEST_PROCESS.md` Stage 1 for
the exact sim checklist.

## Troubleshooting

| Symptom | Likely Cause | Fix |
| --- | --- | --- |
| Gradle says Java 11 is active | `JAVA_HOME` points at old JDK | Set `JAVA_HOME` to `C:\Users\Public\wpilib\2026\jdk` |
| `VisionTest` auto missing | Deploy files not synced | A starter auto ships in `deploy/pathplanner`; or choose `No Auto` |
| No vision frames in simulation | Robot not facing the tag board, or transforms wrong | Rotate toward the tags; check `ROBOT_TO_*_CAMERA` |
| AdvantageScope cannot connect | NT4 sim server not running | Start robot sim first |
| No controller input | Xbox not detected by Driver Station | Replug controller and restart sim |
