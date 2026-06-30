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

Not fully implemented yet:

- Synthetic PhotonVision camera/AprilTag target generation inside robot simulation.
- PhotonVision SimVisionSystem or VisionSystemSim wiring.
- Simulated camera images.

Meaning: you can currently simulate drivetrain code, command scheduling, controls, SysId command selection, AdvantageKit logging, and precision-drive command behavior. You cannot yet prove AprilTag measurement quality in pure simulation without adding PhotonVision simulation hooks.

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
6. Add a `Field2d` or `3D Field` tab.
7. Add robot pose signals if visible.
8. Add line graphs for:
   - `Timing/VisionMs`
   - `Vision/AcceptedObservationCount`
   - `Vision/front-left/AcceptedFrames`
   - `Vision/front-left/RejectedFrames`
   - `Vision/front-right/AcceptedFrames`
   - `Vision/front-right/RejectedFrames`

In current pure simulation, vision accepted frames will likely stay zero until PhotonVision simulation hooks are added.

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

The code currently exposes a `VisionTest` PathPlanner auto name, but the auto file has not been created.

Click-by-click to create it:

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

## PhotonVision Simulation Next Step

To fully simulate AprilTags, the robot project needs a follow-up implementation using PhotonLib simulation APIs.

Expected design:

1. Create simulated AprilTags from `VisionConstants.CUSTOM_FIELD_LAYOUT`.
2. Create a simulated vision system with both camera transforms.
3. Feed the current drivetrain pose to the vision simulator each periodic loop.
4. Let the normal `VisionSubsystem` consume simulated PhotonVision results.
5. Verify AdvantageScope shows accepted/rejected simulated observations.

Do not treat camera simulation as complete until this is implemented and compile-tested.

## Troubleshooting

| Symptom | Likely Cause | Fix |
| --- | --- | --- |
| Gradle says Java 11 is active | `JAVA_HOME` points at old JDK | Set `JAVA_HOME` to `C:\Users\Public\wpilib\2026\jdk` |
| `VisionTest` auto missing | PathPlanner auto file not created | Create the auto or choose `No Auto` |
| No vision frames in simulation | PhotonVision simulation hooks not implemented yet | Add PhotonLib sim wiring |
| AdvantageScope cannot connect | NT4 sim server not running | Start robot sim first |
| No controller input | Xbox not detected by Driver Station | Replug controller and restart sim |
