# VisionTestingAndCalibration prototype

Java WPILib/CTRE/PhotonVision prototype exercised on the Team999 2025 chassis.
The sibling research area in this same repository is the durable source of
calibration methodology, experiment results and algorithm rationale.

**Continuing on another computer:** start with
[Research handoff](../MechaRAMS%20vision%20system/RESEARCH_HANDOFF.md).

For the new optional follower, read the
[BLine laptop handoff](../MechaRAMS%20vision%20system/BLINE_LAPTOP_HANDOFF.md) and
[validation plan](../MechaRAMS%20vision%20system/BLINE_VALIDATION_TEST_PLAN.md).
Four BLine + Precision entries coexist with the original routes. The source is
compiled/tested/packaged; GUI and physical qualification remain pending.

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

The original October12 portability audit changed documentation/assets only.
The subsequent authorized BLine implementation includes locally verified code,
95 passing tests, coverage and packaging. The team performs simulator/physical
qualification and manual hardware deployment; no BLine deployment is claimed.
