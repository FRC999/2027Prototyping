# How the Robot Decides What to Do

Plain-English guide to localization, autonomous driving, handoffs, and final positioning

Audience: management, build team, drive team, students, and new programmers

Last checked against the robot code: September 4, 2026

## Purpose of this document

The robot does not use one giant algorithm. It uses several smaller algorithms, each with a specific
job. Some run together as layers. Others take turns controlling the drivetrain.

This guide explains those decisions as functional **IF/THEN rules**. It intentionally uses less math
than the engineering documents. The exact code and constants remain the final source of truth.

Update this document whenever we add an algorithm, change a handoff, change a tolerance, or promote an
experimental mode to the normal competition mode.

Visual versions of this guide:

- [`ALGORITHM_DECISION_MAP.md`](ALGORITHM_DECISION_MAP.md) renders as a static, color-coded decision
  tree on GitHub.
- [`algorithm-decision-map.html`](algorithm-decision-map.html) is the interactive local version. Open
  it in a browser and select any box for details, terminology, and related log signals.

## Generic conditions and behavior-changing mechanisms

The primary explanation is now the [generic decision map](ALGORITHM_DECISION_MAP.md) and its
[interactive version](algorithm-decision-map.html). Each route supplies a destination, heading,
starting requirements, and handoff condition. Motion limits and finish requirements are settings;
many are currently shared constants rather than independently configurable per-route values.

Every condition that changes requested motion deserves a visible decision: profile timing,
translation-distance bands, speed caps, each of the four settle checks, hold escape, and timeout.
Calculations that cooperate continuously, such as feedback and angular-rate damping, appear as
components connected to those decisions rather than fictitious ownership handoffs.

Translation damping opposes measured translation with increasing strength through the final approach
band. Rotation damping opposes measured turning throughout active correction. Damping does not
replace the position controller or decide by itself that the robot should stop. Planned motion,
feedback, and damping are combined; speed limits and the zero-hold override then determine output.

The generic map describes a destination requiring a stop. Automatic braking-distance handoff selection
and passing-waypoint completion would need further implementation. The current command composition
can also transfer to the final controller if the coarse command finishes before the spatial predicate.

## Current VisionTest example

The numerical rules in the remainder of this guide document the present test configuration. They are
examples of the generic conditions, not fixed requirements for every trajectory. In particular, the
field-X handoff, board target, fresh MultiTag start, and yaw normalization belong to this test setup.

### The 30-second explanation

The complete autonomous stack works like this:

1. Wheel sensors and the gyro continuously estimate where the robot is.
2. PhotonVision camera measurements correct that estimate when the measurements pass safety checks.
3. Before the straight VisionTest starts, the robot requires a fresh two-tag camera position in a safe
   starting area.
4. PathPlanner controls the fast, coarse part of the trip.
5. When the robot crosses the handoff line, PathPlanner stops controlling the robot.
6. The DriveToPose last-leg controller takes over from the robot's current position and speed.
7. DriveToPose slows the robot, corrects position and angle, and then commands zero motion.
8. The command finishes only after position, angle, forward/sideways speed, and turning speed are all
   acceptable for a short continuous time.
9. CTRE's motor controllers are the bottom layer. They make each wheel follow the speed requested by
   whichever high-level drive command currently owns the drivetrain.

Vision helps answer **where the robot is**. PathPlanner and DriveToPose decide **how the robot should
move**. Vision does not directly command the motors.

## One-page decision sequence

For a quick management or build-team explanation, read the routine as this chain:

1. **IF** the cameras do not provide a recent, believable two-tag starting position,
   **THEN** do not start the measured-start test.
2. **IF** the start is valid, **THEN** use that measured X/Y position and define the robot's test
   heading as straight ahead.
3. **IF** the robot has not crossed x=`3.3 m`, **THEN** PathPlanner owns the drivetrain and performs
   the fast, smooth part of the move.
4. **IF** the robot crosses x=`3.3 m`, **THEN** interrupt PathPlanner and immediately give control to
   DriveToPose.
5. **IF** the robot is still far from the final point, **THEN** DriveToPose keeps approaching at useful
   speed.
6. **IF** the robot gets near the final point, **THEN** DriveToPose fades out planned forward motion
   and increases electronic braking.
7. **IF** position, heading, forward/sideways speed, and turning speed are all acceptable,
   **THEN** command zero motion and begin the short settling hold.
8. **IF** the robot stays inside the wider escape limits for `0.05 seconds`, **THEN** finish the
   command.
9. **IF** the robot drifts outside any escape limit during that hold, **THEN** resume active
   correction.
10. **IF** the last-leg controller still cannot settle after `4 seconds`, **THEN** stop on timeout and
    report that the run did not finish normally.

The camera system, wheel odometry, and gyro continue estimating position throughout this sequence.
The handoff changes which algorithm commands the wheels; it does not switch localization off and on.

## Who controls what?

| Layer | Main job | Runs when |
| --- | --- | --- |
| Wheel odometry + Pigeon gyro | Maintain the robot's continuous position and heading estimate | Always |
| PhotonVision + vision policy | Correct the position estimate with accepted AprilTag observations | Whenever usable frames arrive |
| PathPlanner | Follow the fast, coarse route | First part of a handoff autonomous routine |
| DriveToPose | Finish at the requested position and heading | Last part of the routine, or the entire direct-distance test |
| CTRE wheel velocity control | Make the individual modules follow requested wheel speeds | Under every closed-loop drive command |

Only one scheduled command owns the drivetrain at a time. PathPlanner and DriveToPose do not fight
each other. The command scheduler ends or interrupts one before the other takes control.

## Main straight spatial-handoff routine

This is the current `VisionTest (spatial handoff)` decision sequence.

### 1. Disabled preflight

- **IF** there is a recently accepted MultiTag pose no more than `0.25 seconds` old,
  **THEN** a camera-based starting position is available.
- **IF** that robot-center pose is inside x=`1.2 to 2.6 m`, y=`1.5 to 2.5 m`, and within `15 degrees`
  of straight ahead, **THEN** the start is considered safe.
- **IF** both checks pass while the robot is disabled, **THEN**
  `PathPlanner/VisionTest/Preflight/ReadyToEnable` becomes `true`.
- **IF** either check fails, **THEN** the operator should not enable the test.

### 2. Autonomous start

- **IF** the fresh MultiTag start has disappeared or moved outside the safe area,
  **THEN** the autonomous command stops without moving and records the reason.
- **IF** the start is accepted, **THEN** the code uses the measured robot-center X and Y.
- **THEN** it sets the test heading to `0 degrees`, because the chassis is physically squared to the
  board for this test.
- **THEN** it generates a straight PathPlanner route from that measured starting translation toward
  the coarse endpoint at `(3.6 m, 2.0 m)`.

This avoids the old behavior that reset the robot to a made-up x=`1.5 m` start and could make the
robot drive backward before moving forward.

### 3. PathPlanner owns the coarse move

- **WHILE** robot X is at or below `3.3 m`, **THEN** PathPlanner controls the drivetrain.
- PathPlanner handles the smooth acceleration and keeps the robot on the planned line.
- The current test limits PathPlanner to `1.6 m/s` and `1.2 m/s²`.
- The spatial path is told that its endpoint speed is `1.4 m/s`.
- **WHY:** this path will be interrupted before its x=`3.6 m` endpoint. Asking it to stop at that
  unused endpoint made the robot slow down before the handoff and then speed up again afterward.

The switch is based on a position, not a timer and not a fixed percentage. From our usual starting
position near x=`2.25 m`, x=`3.3 m` is about `78%` of the way through PathPlanner's coarse segment.
If the starting position changes, the percentage changes, but the x=`3.3 m` handoff line does not.

### 4. Control passes to DriveToPose

- **IF** robot X becomes greater than `3.3 m`, **THEN** the PathPlanner command is interrupted.
- **THEN** DriveToPose immediately reads the current robot position and current speed.
- **THEN** DriveToPose starts its own smooth motion plan from those current values.
- **THEN** DriveToPose owns the drivetrain until it succeeds, times out, or is interrupted.

The final target for this test is robot-center pose `(4.25 m, 2.0 m, 0 degrees)`.

The DriveToPose logging channels are registered during safe robot startup. This prevents the first
handoff from pausing while AdvantageKit creates dozens of new log publishers. That pause caused the
large `0586` overrun even though the driving profile itself was smooth.

## What the last-leg DriveToPose controller does

DriveToPose combines three functional ideas:

- A smooth motion plan says how quickly the robot should approach the target.
- Position feedback pushes toward the target when the robot is behind and back toward it when the
  robot is ahead.
- Velocity damping acts like electronic braking near the target so the robot does not coast through.

### Distance behavior

- **IF** the robot is more than `35 cm` from the target, **THEN** the planned forward-speed portion is
  fully active.
- **IF** the robot is between `35 cm` and `4 cm` from the target, **THEN** the planned forward-speed
  portion gradually fades and velocity damping gradually increases.
- **IF** the robot is within `4 cm`, **THEN** the planned forward-speed portion is zero.
- Position correction and braking can still operate until the full stop conditions are satisfied.
- The total requested translation speed is limited to `1.6 m/s`.

### Angle behavior

DriveToPose does **not** stop angle correction just because the angular speed is small.

- **IF** the heading is still outside the selected angle tolerance, **THEN** it continues commanding
  an angle correction, even if the robot is currently turning slowly or not turning at all.
- **IF** the run uses precise yaw, **THEN** the normal heading tolerance is `1.5 degrees`.
- **IF** the run is a straight 1 m or 2 m ruler-calibration test, **THEN** it uses the relaxed
  `1.8-degree` heading tolerance.
- **IF** the robot is still turning, **THEN** gyro-rate damping requests motion against that turn,
  like electronic shock absorption. The retained value is `0.70`; it was increased from `0.35`
  after `e974` showed repeated angular ringing and validated by the low-yaw `107d` run.
- The controller limits commanded turning speed to `180 degrees/second`, or about `3.14 rad/s`.

### When it first stops correcting

DriveToPose starts a zero-motion settling hold only when **all four** statements are true at the same
time:

1. The total position error is no more than `4 cm`.
2. The heading error is no more than `1.5 degrees`, or `1.8 degrees` in relaxed-yaw mode.
3. The robot's translation speed is no more than `0.12 m/s`.
4. The robot's turning speed is no more than `8 degrees/second`, or about `0.14 rad/s`.

- **IF** all four qualify, **THEN** the controller commands zero translation and zero rotation.
- **IF** the robot remains inside the wider escape window described below for `0.05 seconds`,
  **THEN** the command succeeds and ends.

This means being physically near the target is not enough. Passing through the target quickly does
not count as being settled.

### When it starts correcting again

The settling hold uses a wider escape window so ordinary sensor noise does not make the robot jitter.

- **IF** position error grows beyond `6 cm`, **THEN** active correction resumes.
- **OR IF** heading error grows beyond `2.5 degrees`, **THEN** active correction resumes.
- **OR IF** translation speed rises above `0.18 m/s`, **THEN** active correction resumes.
- **OR IF** turning speed rises above `12 degrees/second`, about `0.21 rad/s`, **THEN** active
  correction resumes.
- **ELSE** small changes inside that wider window are ignored while zero motion is commanded.

This is called hysteresis: it uses a tight rule to enter the stopped state and a wider rule to leave
it. The goal is to prevent repeated tiny corrections after the robot is already acceptably parked.

### Safety timeout

- **IF** DriveToPose cannot settle within `4 seconds`, **THEN** it stops and reports a timeout.
- **IF** the command is canceled or another mode takes over, **THEN** it commands the drivetrain to
  stop.

## Vision decision rules

Vision continuously offers position measurements to the drivetrain estimator. The estimator combines
those measurements with wheel and gyro tracking.

### Which camera frames are processed?

- **IF** a camera delivers several unread solvable frames in one robot loop, **THEN** only the newest
  solvable pose is used and the older poses are counted as superseded.
- **WHY:** carrying an old frame backlog across loops can make the estimator correct toward where the
  robot used to be.

### Which measurements are rejected?

- **IF** any important number is missing, infinite, or not a number, **THEN** reject the frame.
- **IF** the calculated robot height is more than `25 cm` above or below the floor, **THEN** reject it
  as `BAD_Z`.
- **IF** the calculated pose is more than `50 cm` outside the known field border, **THEN** reject it.
- **IF** average tag distance is greater than `5 m`, **THEN** reject it.
- **IF** a single-tag result has unknown ambiguity or ambiguity greater than `0.20`, **THEN** reject it.
- **IF** the result passes every check, **THEN** it may be fused into the robot position estimate.

### Reset and autonomous protection

- **IF** a camera frame was captured before the most recent pose reset, **THEN** reject it.
- **IF** the reset happened less than `0.35 seconds` ago, **THEN** temporarily suppress vision so an
  in-flight old frame cannot undo the reset.
- **IF** autonomous has been enabled for less than `0.30 seconds`, **THEN** validated vision frames are
  logged but not fused.
- **AFTER** those protection windows expire, **THEN** accepted vision can correct the estimate again.

### MultiTag versus single-tag position

- **IF** PhotonVision sees two or more tags in one frame, **THEN** use its MultiTag position solution.
- **IF** only one tag is usable, **THEN** the selected single-tag algorithm determines the X/Y result.

The two single-tag choices are:

- **PnP, current baseline:** infer camera position from how the square tag appears in the image.
- **TrigSolve, A/B experiment:** use the tag direction and distance from the camera plus the gyro
  heading from the time the picture was captured.

- **IF** TrigSolve is selected but the tag layout or historical gyro heading is unavailable,
  **THEN** it automatically falls back to PnP for that frame.
- **IF** the frame contains only one tag, **THEN** camera-reported heading is never trusted; only X/Y
  can be fused.

### Who controls heading while moving?

- **IF** the robot is enabled, **THEN** the Pigeon gyro owns estimator heading. Camera yaw is not fused,
  even from a MultiTag frame.
- **IF** the robot is disabled or the operator requests a manual camera seed, **THEN** an eligible
  fresh MultiTag solution may supply heading.
- The front-left camera is currently eligible to provide that seed heading.
- The front-right camera still provides useful X/Y, but its rotation contribution is disabled.

This means a noisy camera angle should not directly steer the enabled robot. It can still affect X/Y
and therefore the position correction.

### How strongly is each camera trusted?

- **IF** tags are closer, **THEN** the camera measurement receives more weight.
- **IF** more tags are in the solve, **THEN** the measurement receives much more weight.
- **IF** the front-left camera reports a measurement, **THEN** its X/Y influence is reduced compared
  with the more stable front-right camera. The current front-left uncertainty multiplier is `2.15`.
- **IF** isotropic trust is selected, **THEN** the same uncertainty is assumed in every direction.
- **IF** anisotropic trust is selected, **THEN** the code trusts side-to-side bearing more than distance
  toward or away from the tag.

Anisotropic trust is still an experiment because its exact coefficients have not yet been fitted from
enough real-robot data.

## What each autonomous chooser option actually does

| Chooser type | IF selected, THEN |
| --- | --- |
| `Forward 1m` or `Forward 2m` | Skip PathPlanner. Capture the current fused X/Y, normalize test yaw to zero, and use DriveToPose for the entire distance with relaxed yaw tolerance. |
| `Precision To Tag Board` | Skip PathPlanner and use DriveToPose for the entire move to `(4.25, 2.0, 0°)`. |
| `PathPlanner Auto: VisionTest` | Use the generated current-start PathPlanner route and stop after the coarse route. No precision finish. |
| `VisionTest then Precision (sequential)` | Finish the complete PathPlanner route at x=`3.6 m`, then start DriveToPose. PathPlanner's endpoint speed is zero. |
| `VisionTest (spatial handoff)` | Interrupt PathPlanner after x=`3.3 m`, using a nonzero `1.4 m/s` coarse endpoint target so it does not plan an early stop, then let DriveToPose finish at x=`4.25 m`. Actual speed at the handoff depends on the generated profile. |
| `AB: ... TrigSolve` | Run the same motion but use TrigSolve for accepted single-tag X/Y measurements. |
| `AB: ... AnisoCov` | Run the same motion but use directional camera trust instead of equal trust in every direction. |
| `VisionTestCurved ...` | Follow the fixed curved stress-test route, then use the same x=`3.3 m` spatial handoff to DriveToPose. |

The `AB:` entries are controlled comparisons. They should not be called the new normal mode until logs
show they perform better or at least no worse over repeated runs.

## Aiming algorithms are a separate branch

The aiming commands do not participate in the PathPlanner-to-DriveToPose autonomous handoff.

- **IF** stationary aiming is requested, **THEN** the robot stops translating and turns the chassis
  toward the configured field target.
- **IF** heading remains within `1.5 degrees` for `0.2 seconds`, **THEN** stationary aiming succeeds.
- **IF** it cannot settle within `3 seconds`, **THEN** it stops on timeout.
- **IF** drive-and-aim is held, **THEN** the driver continues controlling translation while the
  software controls rotation toward the target.
- **IF** shoot-on-move lead is enabled, **THEN** the aim point is adjusted for where the moving robot is
  expected to be after a modeled flight time.

The shoot-on-move calculation is currently a teaching and chassis-control feature. This prototype does
not contain a real shooter, hood, turret, or measured projectile table.

## Normal modes versus experiments

| Area | Current baseline | Experimental or special-purpose choice |
| --- | --- | --- |
| MultiTag position | PhotonVision MultiTag | No competing mode; always preferred when available |
| Single-tag X/Y | PnP | TrigSolve |
| Camera trust shape | Isotropic | Anisotropic |
| Enabled heading source | Pigeon gyro | Camera heading deliberately disabled while enabled |
| Coarse autonomous motion | PathPlanner | Curved path is a stress test, not a different controller |
| Final positioning | DriveToPose | Precise or relaxed yaw changes only its finish tolerance |
| Handoff style | Spatial handoff is the intended fast/precise pattern | Sequential and coarse-only modes are comparison tools |

## Plain-English glossary

- **Pose:** the robot's position and the direction it is facing.
- **Yaw or heading:** which way the robot points when viewed from above.
- **Odometry:** estimating motion by tracking how the wheels and gyro move.
- **Fused pose:** the best combined estimate after wheel, gyro, and accepted camera information are
  considered together.
- **Path or motion profile:** a planned series of positions and speeds that creates a smooth move.
- **Feedforward:** an expected amount of motion requested because the plan says the robot should be
  moving now.
- **Feedback:** a correction based on the difference between where the robot is and where it should
  be.
- **Velocity damping:** extra braking based on how quickly the robot is still moving near the target.
- **Tolerance:** an error small enough to be accepted for this job; it does not mean the error is
  exactly zero.
- **Settling hold:** the short period when the controller commands zero and checks that the robot is
  truly staying stopped.
- **Escape limit:** the wider boundary that causes corrections to restart if the robot moves after
  entering the settling hold.
- **Handoff:** the controlled moment when one drivetrain command stops and another takes ownership.
- **Timeout:** a safety ending used when the normal success conditions take too long.
- **Baseline:** the normal mode used for comparison.
- **A/B experiment:** a controlled test where one algorithm choice changes and the rest of the run is
  kept as similar as possible.

## Useful log signals for seeing the handoff

- `PathPlanner/VisionTest/Preflight/ReadyToEnable`: safe to start the measured-start VisionTest.
- `PathPlanner/VisionTest/StartAccepted`: the start was accepted when autonomous actually began.
- `PathPlanner/VisionTest/CoarseGoalEndVelocityMetersPerSecond`: should be `1.4` for spatial handoff.
- `PathPlanner/TargetPose`: where PathPlanner currently wants the robot.
- `DriveToPose/Controller/Active`: `true` after the last-leg controller takes ownership.
- `DriveToPose/WithinPoseTolerance`: position and heading are in their tight windows.
- `DriveToPose/WithinVelocityTolerance`: translation and turning speeds are low enough.
- `DriveToPose/AtGoal`: the zero-motion settling hold is active.
- `DriveToPose/OutsideSettlingEscapeTolerance`: the hold must be released and correction resumed.
- `DriveToPose/Finished`: the last-leg command ended.
- `DriveToPose/TimedOut`: it ended because the safety limit was reached instead of settling.
- `Vision/Modes/SingleTagStrategy` and `Vision/Modes/CovarianceModel`: identify the vision experiment.
- `Vision/Camera0/LastRejectionReason` and `Vision/Camera1/LastRejectionReason`: explain discarded frames.

## Functional checklist for future algorithm changes

Whenever an algorithm is added or changed, add an entry here that answers:

1. What job does it perform: sensing, estimating position, planning motion, final correction, aiming,
   or low-level motor control?
2. Does it run at the same time as another algorithm, or does it take exclusive control?
3. Exactly what **IF** condition starts it?
4. Exactly what **THEN** action does it take?
5. What condition ends it or passes control onward?
6. What fallback is used if its required data is missing?
7. What safety condition stops motion?
8. Which chooser option enables it?
9. Which log field proves it was active?
10. Is it baseline, experimental, calibration-only, or retired?

Also update the `Last checked against the robot code` date at the top. This prevents a clear document
from becoming a confidently wrong document as the software evolves.

## Change history

- **2026-09-04:** Added a static GitHub decision tree and a self-contained interactive decision map.
  The visual map uses selectable boxes and a detail panel without changing robot behavior.
- **2026-09-04:** `107d` validated rotation damping `0.70`: both ruler corners matched, post-arrival
  yaw stayed small, and the final-controller time fell substantially. Retained the safety gates.
- **2026-09-04:** Recorded the `e974` one-variable rotation-damping comparison: `0.35` to `0.70`, with
  all pose tolerances, handoff settings, translation control, and safety gates held constant.
- **2026-09-04:** Added the one-page decision sequence and plain-English glossary for management,
  build-team, and student reviews. No behavior or threshold changed.
- **2026-09-04:** Initial functional guide created from the robot-code branch after the nonzero
  spatial handoff velocity and DriveToPose telemetry-startup priming changes.
