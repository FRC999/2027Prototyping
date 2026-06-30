# Robot Safety Audit

Review robot-affecting changes before deploy.

Check:

- CAN IDs and bus names match the intended chassis.
- Default commands stop safely when interrupted.
- SysId controls are documented and require deliberate button combinations.
- No high-speed autonomous command is the default.
- No high-frequency console or SmartDashboard spam was added.
- Simulation-only code is guarded (`RobotBase.isSimulation()` selects `VisionIOPhotonVisionSim`).
- Vision timestamps are converted with `Utils.fpgaToCurrentTime` before fusion.
- Precision/aiming commands have a safety timeout and stop on end; aiming adds no mechanism code.
- Any unverified hardware constants are documented as provisional.
- `./gradlew.bat compileJava` and `./gradlew.bat test` pass.

Output critical safety issues first.
