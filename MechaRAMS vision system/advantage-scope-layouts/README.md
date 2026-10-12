# Portable AdvantageScope layouts

October12 addition: [BLine Comparison](BLine%20Comparison.json) preserves the existing
Auto Finish graphs/tables and adds live/saved BLine outcome, progress and handoff
tables. All27 added field keys were checked against a finalized desktop WPILOG.
The version26.0.0 wrapper/controller schema is inherited from the working export;
GUI import itself has not been performed in this automation session.

Repository copies of the previously working classroom exports. Both declare
layout version26.0.0 with the recognized hubs/satellites/version wrapper and
separate live-NT and saved-log tabs.

- [Relative Localization Check](Relative%20Localization%20Check.json): reference,
  capture progress and camera/fused comparisons. Original filename:
  `AdvantageScope 10-03-2026 - Relative Localization Check.json`.
- [Auto Finish Ownership](Auto%20Finish%20Ownership.json): end motion, holds,
  qualification and ownership. Original filename:
  `AdvantageScope 9-28-2026 - Auto Finish Ownership.json`.

Import from this folder on any PC. Saved-log fields use /RealOutputs/...; live
fields use NT:/AdvantageKit/RealOutputs/.... Use tabs for your data source.
Some fields appear only after the associated command/capture records them.
Importing a viewer layout does not enable or stop robot recording.

The source exports are copied without JSON-content changes. Offline checks verify
structure and prefix conventions; this task did not reopen AdvantageScope or run
the robot. No dedicated A0 layout is installed here; its existing signals are in
the [aiming report](../AIMING_BASELINE_2026-10-05.md).
