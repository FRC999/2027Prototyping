# VisionTestingAndCalibration prototype

Java WPILib/CTRE/PhotonVision prototype exercised on the Team999 2025 chassis.
The sibling research area in this same repository is the durable source of
calibration methodology, experiment results and algorithm rationale.

**Continuing on another computer:** start with
[Research handoff](../MechaRAMS%20vision%20system/RESEARCH_HANDOFF.md).

- [Commissioning playbook](../MechaRAMS%20vision%20system/COMPETITION_CHASSIS_CALIBRATION_PLAYBOOK.md): what to calibrate and how to test it on a new chassis.
- [Functional algorithm handoffs](../MechaRAMS%20vision%20system/FUNCTIONAL_ALGORITHM_HANDOFFS.md): current IF/THEN driving rules.
- [Relative localization test card](../MechaRAMS%20vision%20system/RELATIVE_LOCALIZATION_TEST_CARD.md): reference capture, rear-frame measurements and evidence.
- [Camera mount manual](../MechaRAMS%20vision%20system/CAMERA_MOUNT_CALIBRATION_MANUAL.md): intrinsics versus mounting, six-axis measurements and validation.
- [Latest aiming baseline](../MechaRAMS%20vision%20system/AIMING_BASELINE_2026-10-05.md): dc76/dd6b, timeout versus success, next isolated experiment.
- [Session state](../MechaRAMS%20vision%20system/SESSION_STATE.md): detailed history and pending work.
- [Portable AdvantageScope layouts](../MechaRAMS%20vision%20system/advantage-scope-layouts/README.md): live/saved viewer configurations.

Current code still uses open-loop stationary aiming. The recommended closed-loop
aim change has not been implemented. Lens recalibration was planned but no result
was supplied. Do not assume a documented proposal is deployed, or copy prototype
geometry/gains onto another chassis without measurement.

The October12 portability update changes documentation/assets only. Per standing
mentor instructions the assistant does not build, compile, simulate or deploy
this robot. The team performs manual verification/deployment when behavior changes.
