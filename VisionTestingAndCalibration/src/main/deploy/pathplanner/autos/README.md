# PathPlanner Autos

A starter `VisionTest` auto ships here (and `paths/VisionTestPath.path`): a straight move from
`(1.5 m, 2.0 m, 0 deg)` to `(3.6 m, 2.0 m, 0 deg)` using the cautious constraints in `settings.json`.

The robot code exposes it three ways in the auto chooser:

- **PathPlanner Auto: VisionTest** — the coarse path alone.
- **VisionTest then Precision (sequential)** — the full path, then `DriveToPosePrecisionCommand` finishes
  precisely at `(4.25 m, 2.0 m, 0 deg)`.
- **VisionTest (spatial handoff)** — interrupts the path at x > 3.3 m and finishes on the precision
  controller (the genuine 6328 coarse->precise pattern).

A missing/edited auto is non-fatal: the chooser catches it and prints to the console. Edit in the
PathPlanner GUI (`Open Project` -> this folder).
