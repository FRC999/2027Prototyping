# PathPlanner Autos

A legacy `VisionTest` auto ships here (and `paths/VisionTestPath.path`) as an editor/reference asset: a
straight move from `(1.5 m, 2.0 m, 0 deg)` to `(3.6 m, 2.0 m, 0 deg)`. Do not execute this fixed-start
auto directly on the robot. The robot chooser's straight entries instead generate a fresh path at auto
initialization from a trusted MultiTag robot-center pose, preserving measured X/Y and normalizing yaw.

The robot code exposes it three ways in the auto chooser:

- **PathPlanner Auto: VisionTest** — the coarse path alone.
- **VisionTest then Precision (sequential)** — the full path, then `DriveToPosePrecisionCommand` finishes
  precisely at `(4.25 m, 2.0 m, 0 deg)`.
- **VisionTest (spatial handoff)** — interrupts the path at x > 3.3 m and finishes on the precision
  controller (the genuine 6328 coarse->precise pattern). Its runtime path requests a `1.4 m/s`
  goal-end speed so it carries speed into the interruption instead of braking for the unused endpoint.
  The coarse-only and sequential runtime paths still request zero endpoint speed.

The generated straight path rejects a missing/stale MultiTag start or a start outside the configured
test area without moving. The `VisionTestCurved` stress test remains file-based and absolute. Edit the
reference assets in the PathPlanner GUI (`Open Project` -> this folder).
