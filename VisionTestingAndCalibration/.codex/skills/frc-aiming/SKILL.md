# FRC Chassis Aiming Skill

Use this skill when modifying chassis aiming (pointing the whole robot at a field goal). Team 999 is
**not** building a turret or game-piece mechanism in this prototype — aiming is chassis-only.

## Required Reads

- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\AGENTS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\DESIGN_DECISIONS_AND_REJECTED_IDEAS.md`

## Architecture

```
util/AimingCalculator.java   pure, stateless, unit-tested: heading-to-goal + shoot-on-move lookahead
commands/AimAtGoalCommand    stationary "square up to the goal" with settle + safety timeout
commands/DriveAndAimCommand  driver translates, robot auto-faces goal and leads its motion
Constants.AimConstants       GOAL_POSITION (configurable), aim-face offset, heading gains, TOF model
```

## Design Rules

- Aim heading = `atan2(goal − robot)` + aim-face offset. Idea: 6995 `aimAtFieldPose`, 1768 `ShootingUtil`.
- Shoot-on-move: iterate a velocity-led "future pose" using a modeled time-of-flight, then aim from there.
  Idea: 6328 `LaunchCalculator` (20 iters), 1768 `ShootingUtil` (25 iters). Lead is *opposite* the motion
  direction (verified in `AimingCalculatorTest`).
- Keep `AimingCalculator` pure (no subsystem deps) so it stays headless-unit-testable.
- The "goal" is a configurable field point — works for a future shooting OR placing game; only
  `GOAL_POSITION` changes when the 2027 target is known.
- Log target heading, heading error, distance, lead pose, goal pose.
- NO turret, NO shooter/hood/flywheel maps, NO game-piece code. Keep `Vision.getTargetX` available so a
  boresight/turret loop can be added later.

## Verification

- `./gradlew.bat compileJava` and `./gradlew.bat test`. Keep `AimingCalculatorTest` passing.
- Validate stationary + moving aim in simulation (`CALIBRATION_AND_TEST_PROCESS.md` Stage 7 / Stage 1.3).
