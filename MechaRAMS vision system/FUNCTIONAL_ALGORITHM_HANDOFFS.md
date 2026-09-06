# How the robot navigates a trajectory

**Generic functional guide · Team 999 MechaRAMS**

[Visual decision trees](ALGORITHM_DECISION_MAP.md) · [Interactive map](algorithm-decision-map.html) · [Separate test configuration](VISIONTEST_CONFIGURATION_EXAMPLE.md)

This explains the reusable control mechanism, not a particular route across the practice field.
Targets, handoff conditions, speeds, distance bands, tolerances, and timeout come from the selected
routine or shared configuration. They are not universal distances or a fixed percentage of a trip.

Update this guide and both visual maps whenever a decision, algorithm, fallback, tolerance policy,
or chooser behavior changes. Robot source remains authoritative.

## What is reusable — and what is not automatic yet?

The reusable pattern is **follow the main route, transfer control, then finish at a requested pose**.
A pose means position plus heading. DriveToPose can also handle an entire direct move without a
PathPlanner stage.

The existing handoff helper accepts a route-specific condition. It does not automatically calculate
a safe handoff point for arbitrary routes. Many tuning values are shared constants today, not
independent settings exposed for every trajectory.

This guide describes destinations where the robot must **stop**. The current holonomic practice
routes use PathPlanner for intermediate segments and qualify with DriveToPose only at the final
destination. PathPlanner still requests zero planned speed at each sharp direction change, but it
does not spend a separate precision-settling period there. This is a stopped-segment test sequence,
not a seamless competition path. Seamless moving segments, automatic braking-distance handoffs,
and route-wide obstacle avoidance are not supplied by this mechanism alone.

## What each route must define

| Route information | What it changes |
|---|---|
| Starting requirements | Whether localization and starting conditions are sufficient to begin |
| Main path and coarse endpoint | Where the main trajectory goes |
| Final destination and heading | What the final controller tries to reach |
| Handoff condition | When the main controller gives up drivetrain ownership |
| Speed and acceleration limits | How quickly the robot may travel and change speed |
| Approach bands and damping gains | How planned motion fades and measured motion is damped |
| Finish policy | Required position, heading, and speed accuracy; hold and escape limits |
| Timeout | How long the final controller may try before reporting failure |

These are configuration responsibilities, not a claim that every item already has a per-route
dashboard selector. Some routines currently share the same values.

## Who owns what?

| Layer | Responsibility | Relationship to the other layers |
|---|---|---|
| Wheel sensors and gyro | Continuously track robot motion | Supply localization |
| PhotonVision and vision policy | Supply accepted camera-based corrections | Improve localization, never command motors |
| Fused pose estimate | Combine the available motion and camera information | Read by the driving controllers |
| PathPlanner | Follow the planned main route | Owns the drivetrain during the coarse phase |
| DriveToPose | Approach and finish at the target pose | Takes ownership after handoff, or handles a direct move |
| CTRE motor control | Make each wheel follow its request | Operates underneath the active driving controller |

Only one scheduled driving command owns the drivetrain at a time. Localization keeps running
through the handoff; switching driving controllers does not switch localization off.

## Start and main-route decisions

- **IF** the selected routine's localization requirements are not met, **THEN** its start gate blocks
  the move. Requiring fresh MultiTag vision is a test-specific policy, not a rule for all navigation.
- **IF** the official PathPlanner no-output warmup has not completed, **THEN** current-start
  PathPlanner tests do not move. The roboRIO performs first-use path-follower work while disabled.
- **IF** its allowed starting conditions fail, **THEN** do not begin that route.
- **IF** the start qualifies, **THEN** use the routine's intended starting state. A current-start
  routine uses the measured robot pose, not a made-up coordinate.
- **IF** the route begins with PathPlanner, **THEN** it owns the main drive until the handoff.
- **IF** the supplied handoff condition becomes true, **THEN** interrupt the coarse command and
  initialize DriveToPose using the current pose and chassis speed.
- **IF** the coarse command finishes first, **THEN** the current command composition also starts
  DriveToPose. This is an alternate transfer path, not an automatic safe-handoff calculation.
- **IF** the routine is a direct final-pose move, **THEN** skip PathPlanner.

Do not generally reset heading to zero: that convention only makes sense for a physically aligned
test. A route may begin at another heading and must use a consistent coordinate frame.

## Small mechanisms inside the final controller

These cooperate during active correction; they do not take turns owning the wheels.

### Sensor validity

**IF** the gyro turning-rate signal is valid, **THEN** use it. **ELSE** use the wheel-kinematic
turning-rate fallback. This affects damping, speed qualification, and profile initialization.
The fallback is not a guarantee of equally good measurements.

### Profile timing

A motion profile supplies a controlled progression of position and speed.

**IF** a profile step is due, **THEN** advance the profile with bounded catch-up work.
**ELSE** keep the profile state while recalculating feedback. This limits the effect of irregular
robot-loop timing; it does not make the control loop infinitely fast.

### Translation approach bands

| Distance from final target | Planned translation contribution | Added translation damping |
|---|---|---|
| Outside the outer boundary | Full contribution | None |
| Between the boundaries | Fade smoothly | Increase smoothly |
| Inside the inner boundary | Zero contribution | Full configured strength |

The boundaries are distances **remaining to the destination**, not absolute field coordinates.
Position feedback continues correcting error through all three bands.

### Rotational damping

**IF** measured turning is nonzero, **THEN** add a contribution opposing it.
**IF** measured turning is zero, **THEN** the angular damping contribution is zero.

This is continuous damping during active correction, not a separate controller activated only after
a turn-rate threshold is crossed. The settle gate uses its own turn-rate threshold.
Damping does not disable heading feedback by itself.

### Combine and limit the requests

Combine planned motion, position/heading feedback, and damping.

- **IF** translation exceeds its speed limit, **THEN** scale the X and Y requests together,
  preserving their direction.
- **IF** turning exceeds its angular-speed limit, **THEN** cap that request separately.
- **IF** zero hold is active, **THEN** the hold overrides the candidate request with zero motion.

Opposing damping does not necessarily make the robot reverse: the combined request determines motion.
Too much damping can slow the approach.

## Four separate decisions to enter zero hold

Each asks about the measured state, not just what we requested from the wheels.

| Question | IF yes | IF no |
|---|---|---|
| Position error acceptable? | Position qualifies | Keep correcting |
| Heading error acceptable for the selected precision policy? | Heading qualifies | Keep correcting |
| Translation speed low enough? | Translation speed qualifies | Keep correcting |
| Turning speed low enough? | Turning speed qualifies | Keep correcting |

**IF all four qualify together, THEN** command zero motion and begin the hold timer.
A robot may pass through the correct position while still moving too fast, so position alone is
not sufficient.

## Holding, resuming correction, and finishing

- **IF** already holding zero and no wider escape limit is crossed, **THEN** keep commanding zero.
  A small failure of a tighter entry check does not restart correction.
- **IF** any wider position, heading, translation-speed, or turning-speed escape limit is crossed,
  **THEN** release the hold, reset its timer, and resume correction.
- **IF** zero hold survives for the required duration, **THEN** finish successfully.
- **IF** the final-controller timeout expires first, **THEN** stop and report timeout, not success.
- **IF** the command is interrupted, **THEN** end with a zero-motion request.

Different entry and escape limits are called **hysteresis**. Their job is to prevent noisy
measurements from repeatedly switching between stopping and correcting.

## Choosing the pattern

| Pattern | Functional behavior |
|---|---|
| Coarse trajectory only | Follow the main trajectory; no DriveToPose finish is added |
| Sequential coarse plus final | Let the coarse command finish, then run DriveToPose |
| Spatial/condition handoff | Transfer when the route's condition is reached, or coarse completion occurs first |
| Direct final-pose move | Use DriveToPose for the whole move |
| Precise versus relaxed heading | Change heading qualification according to the selected finish policy |
| Stopped multi-segment route | Follow each PathPlanner segment, continue after intermediate stops, and precisely qualify only the route's final endpoint |
| Out and return | Save the measured start, follow the outward segments, reverse the segment order, and precisely finish at that saved start |

The concrete dashboard chooser names and test coordinates live in the
[separate configuration reference](VISIONTEST_CONFIGURATION_EXAMPLE.md). These patterns do not
mean that every possible combination is already exposed in the chooser.

For the current holonomic practice sequence, **IF** a target would enter the configured field-edge or
tag-board clearance margin, **THEN** the complete route is rejected before any wheel is commanded.
**IF** the route changes only translation, **THEN** robot yaw stays at zero while the wheels create
forward, sideways, or diagonal motion. **IF** the camera-facing yaw route is selected, **THEN** the
robot rotates gradually to the configured negative yaw while translating diagonally. **IF** the
out-and-return route is selected, **THEN** it keeps zero yaw, reaches the outward diagonal point, and
retraces to the original measured start. Intermediate points remain PathPlanner-owned; DriveToPose
runs only after the last PathPlanner segment finishes.

An X-wheel or “ski-pizza” stance is not part of moving trajectory braking in this stage. It is useful
as a stationary parking stance, but inserting it into a moving baseline would mix a braking experiment
with the first holonomic geometry test. Test that separately only after the baseline passes.

## Small dictionary

- **Localization:** estimating where the robot is.
- **Pose:** position and heading together.
- **Feedforward:** a contribution based on the planned motion.
- **Feedback:** a correction based on measured error.
- **Damping:** a contribution opposing measured velocity, like a shock absorber.
- **Handoff:** changing which command owns the drivetrain.
- **Tolerance:** the allowed error or speed for a particular check.
- **Hysteresis:** tighter limits to enter a hold and wider limits to leave it.
- **Timeout:** stop trying after the allowed time; this is not proof of success.

Documentation scope: the reusable drive-to-stop structure reviewed September 4, 2026.
Changes to documentation do not change robot behavior or make a new route safe automatically.
