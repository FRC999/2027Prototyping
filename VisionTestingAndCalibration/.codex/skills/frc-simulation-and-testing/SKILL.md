# FRC Simulation and Testing Skill

Use this skill when working on the PhotonVision simulation, AdvantageKit logging/replay, or headless unit
tests. The project rule is **validate in simulation before hardware**.

## Required Reads

- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\SIMULATION_RUNBOOK.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\CALIBRATION_AND_TEST_PROCESS.md` (Stage 1)
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\ADVANTAGESCOPE_SETUP.md`

## Vision simulation

`VisionIOPhotonVisionSim` extends the real IO and adds a shared `VisionSystemSim` + per-camera
`PhotonCameraSim` with OV9782-modeled `SimCameraProperties`. Each loop call `visionSim.update(truePose)`,
then the real ingestion runs unchanged. Selected automatically in `RobotBase.isSimulation()`. Do not mock
the fusion — exercise the real path.

## Headless tests (the machine-checkable spec)

- Live in `src/test/java/...`, run with `./gradlew.bat test` (no display/HAL needed for pure math).
- Keep/extend: `VisionPolicyTest` (rejection gates + dist^2/tagCount covariance + single-tag theta = Inf)
  and `AimingCalculatorTest` (heading geometry + shoot-on-move lead direction).
- Make pure math testable: keep `util/AimingCalculator` dependency-free; expose vision policy methods
  package-private for same-package tests.
- A new module is not "done" until it has a compile + a test and both pass.

## Logging / AdvantageScope

Log the fused `Drive/Pose`, vision `Vision/Summary/*` + per-camera channels, `DriveToPose/*`, `Aim/*` so
the same AdvantageScope layout works live and in replay. Build the layout once in sim, reuse on the robot.

## Verification

- `./gradlew.bat compileJava` and `./gradlew.bat test` green.
- Run `./gradlew.bat simulateJava`; confirm vision accepted frames climb when facing the tag board.
