# Trajectory Review

Review trajectory and precision-driving changes for Team 999.

Check:

- PathPlanner/Choreo is used for coarse motion, not final tolerance.
- `DriveToPosePrecisionCommand` keeps: profiled x/y/theta + velocity feedforward (decelerates to 0 at the
  goal), a settle gate, a safety timeout, and full logging (target/measured/errors/settle/finished).
- A time-based path never *finishes* a precise move; coarse->precise uses `handoffFrom(path, condition)`.
- Controller gains, max speed/accel, and max omega are conservative for testing.
- The path can be interrupted safely; the robot stops at command end.
- Autonomous selection cannot crash if a path file is missing (chooser catches + prints).
- `./gradlew.bat test` still passes.

Output findings first, with file and line references.
