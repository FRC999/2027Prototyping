# Robot Safety Audit

Review robot-affecting changes before deploy.

Check:

- CAN IDs and bus names match the intended chassis.
- Default commands stop safely when interrupted.
- SysId controls are documented and require deliberate button combinations.
- No high-speed autonomous command is the default.
- No high-frequency console or SmartDashboard spam was added.
- Simulation-only code is guarded.
- Any unverified hardware constants are documented as provisional.

Output critical safety issues first.
