# Aiming Review

Review chassis-aiming code for Team 999. The prototype aims the whole chassis at a field goal — there is
NO turret or game-piece mechanism.

Check:

- `AimingCalculator` is pure (no subsystem deps) and unit-tested (`AimingCalculatorTest` passes).
- Aim heading = `atan2(goal - robot)` + aim-face offset; goal is `AimConstants.GOAL_POSITION`
  (configurable so it serves a future shooting or placing game).
- Shoot-on-move lookahead iterates a velocity-led future pose with a modeled time-of-flight; lead is
  opposite the motion direction.
- `AimAtGoalCommand` (stationary) has settle + safety timeout; `DriveAndAimCommand` keeps driver
  translation while auto-facing the goal.
- Logs target heading (the pose-derived bearing to the goal, `Aim/TargetHeadingDegrees`), heading error,
  distance, and lead/goal poses. The camera-relative residual (`poseBearing - cameraBearing`) is future
  boresight-hardware work — do not claim it is logged until a boresight camera exists.
- NO shooter/hood/flywheel/turret/GPM code is introduced. `Vision.getTargetX` is left available for a
  future boresight loop.

Output findings first, with file and line references.
