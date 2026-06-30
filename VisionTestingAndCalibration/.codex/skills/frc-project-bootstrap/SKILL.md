# FRC Project Bootstrap Skill

Use this skill when creating/maintaining the project scaffolding: `build.gradle`, vendordeps,
`Constants.java`, `Robot.java` (AdvantageKit lifecycle), `RobotContainer.java` wiring, and the deploy
layout. This is the "frame" the other skills (vision, drivetrain, trajectory, aiming) plug into.

## Required Reads

- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\AGENTS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\ARCHITECTURE_AND_DEPLOYMENT.md`

## Build setup

- WPILib 2026 GradleRIO, Java 17. Vendordeps: Phoenix6, AdvantageKit, PhotonLib, PathPlannerLib,
  WPILibNewCommands.
- **AdvantageKit `@AutoLog`** requires the annotation processor in `build.gradle`:
  `def akitJson = new groovy.json.JsonSlurper().parseText(new File(... "/vendordeps/AdvantageKit.json").text)`
  and `annotationProcessor "org.littletonrobotics.akit:akit-autolog:$akitJson.version"`.
- Keep JUnit 5 wired (`test { useJUnitPlatform() }`) so headless math tests run with `./gradlew.bat test`.

## Constants

One `Constants.java` with nested classes: `OperatorConstants`, `SwerveConstants` (2025 hardware),
`VisionConstants` (layout, camera names/transforms, gates, covariance baselines + per-camera factors,
sim-camera model), `AutoConstants` (precision gains/profile/tolerances/timeout), `AimConstants`
(configurable goal, heading gains, shoot-on-move model). Comment provenance of each adopted idea.

## Robot lifecycle

`Robot extends LoggedRobot`: record metadata, start AdvantageKit (`WPILOGWriter` to `/home/lvuser/logs`
real / `logs/sim` sim, plus `NT4Publisher`), `Logger.start()`, then build `RobotContainer`. Schedule/cancel
autonomous via `CommandScheduler`.

## RobotContainer wiring

Construct subsystems; select **real vs sim vision IO** by `RobotBase.isSimulation()`; wire the
`VisionConsumer` to `drive.addVisionMeasurement(pose, Utils.fpgaToCurrentTime(t), stdDevs)`; bind one Xbox
controller (see `ROBOT_CONTROLS.md`); expose dashboard test commands and a fault-tolerant auto chooser.

## Verification

- `./gradlew.bat compileJava` and `./gradlew.bat test` must pass before declaring bootstrap done.
- Confirm `@AutoLog` generated `*AutoLogged` under `build/generated/...`.
