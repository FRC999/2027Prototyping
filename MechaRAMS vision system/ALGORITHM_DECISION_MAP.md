<a name="top"></a>

# MechaRAMS • How the robot decides to drive

**Team 999 · A visual guide for students, mentors, and the build team**

[Who owns driving?](#who-owns-driving) · [Small control mechanisms](#small-mechanisms-inside-drivetopose) · [When do we stop?](#four-distinct-settle-entry-decisions) · [Explain the terms](#click-to-explore) · [What varies?](#what-stays-shared-and-what-varies)

> **The short version:** Localization tells us where we are. PathPlanner handles the main route.
> DriveToPose handles the final approach. Smaller mechanisms slow motion, limit corrections,
> and decide when to stop correcting so the robot does not keep twitching.

| Purple | Green | Blue | Yellow | Orange | Red |
|---|---|---|---|---|---|
| Locate the robot | Follow the route | Final corrections | Make a decision | Dampen motion | Block or stop |

Read the arrows in order; diamonds ask questions. Dashed arrows provide information rather than
transfer control. **Localization continues while either driving controller owns the wheels.**

<details>
<summary><strong>Scope: generic rules, not one fixed test trajectory</strong></summary>

This describes our reusable **drive-to-stop pattern**. Coordinates, motion limits, handoff conditions,
distance bands, tolerances, and timeout are configuration values, not universal numbers.
Fixed practice-field values are kept outside both generic maps, in a separate
[test configuration reference](VISIONTEST_CONFIGURATION_EXAMPLE.md). They are not navigation defaults.

A handoff predicate is supplied by the route. The current test uses a field-X boundary.
Automatically choosing the handoff from braking distance and speed would require additional code.
Passing waypoints are a different completion pattern: this diagram's settle gate applies to a
destination where the robot must stop.

</details>

<details>
<summary><strong>Want the clickable-box version?</strong></summary>

Download [algorithm-decision-map.html](algorithm-decision-map.html) using GitHub's download button,
then open the downloaded file in your browser. It is self-contained and works offline.
The repository file preview displays its source, not the interactive application.
This Markdown page is the version to share directly on GitHub: diagrams plus expandable explanations.

</details>

## Who owns driving?

```mermaid
flowchart TD
  Sensors["Wheel odometry and gyro"] --> Pose["Fused robot pose"]
  Vision["Accepted camera measurements"] --> Pose
  Start{"Required localization available?"} -- No --> Block["Block start"]
  Start -- Yes --> Valid{"Start valid for selected route?"}
  Valid -- No --> Block
  Valid -- Yes --> Plan["Use actual starting pose and intended heading"]
  Plan --> PP["PathPlanner follows selected route"]
  Pose -. Feedback .-> PP
  PP --> H{"Route handoff condition reached?"}
  H -- No --> PP
  H -- Yes --> Transfer["Initialize final controller from current pose and speed"]
  H -- "Coarse command ends first" --> Transfer
  Transfer --> Last["DriveToPose: calculate motion request"]
  Pose -. Feedback .-> Last
  Last --> Latched{"Already in zero hold?"}
  Latched -- No --> Entry{"Position, heading AND both speeds acceptable?"}
  Entry -- No --> Apply["Apply limited correction request"]
  Apply --> Last
  Entry -- Yes --> Zero["Command zero and start hold timer"]
  Latched -- Yes --> Escape{"Any wider escape limit exceeded?"}
  Escape -- Yes --> Release["Release hold and reset timer"]
  Release --> Entry
  Escape -- No --> ZeroKeep["Keep commanding zero"]
  Zero --> Time{"Required hold time elapsed?"}
  ZeroKeep --> Time
  Time -- No --> Last
  Time -- Yes --> Done["Finish successfully"]
  Last -- "Configured timeout reached" --> Stop["Stop and report timeout"]
  classDef loc fill:#eee3fa,stroke:#7b4bb7,color:#251534;
  classDef path fill:#dcf4e9,stroke:#16835e,color:#10382b;
  classDef precise fill:#deedfb,stroke:#176fc1,color:#102f4d;
  classDef decision fill:#fff0cb,stroke:#b66b00,color:#4b3108;
  classDef safety fill:#fce5e8,stroke:#be3f4e,color:#4c1820;
  class Sensors,Vision,Pose,Plan loc;
  class PP path;
  class Transfer,Last,Apply,Release precise;
  class Start,Valid,H,Latched,Entry,Escape,Time decision;
  class Block,Stop safety;
```

The coarse-command completion branch reflects the current command composition: the final controller
starts when the coarse command ends naturally or the handoff predicate interrupts it.
Localization keeps running throughout.

## Small mechanisms inside DriveToPose

These cooperate on each update; they do not take turns owning the drivetrain.

```mermaid
flowchart TD
  Gyro{"Gyro rate signal valid?"} -- Yes --> UseGyro["Use Pigeon turning rate"]
  Gyro -- No --> UseWheels["Use wheel-kinematic turning-rate fallback"]
  UseGyro --> Timing
  UseWheels --> Timing
  Timing{"Profile step due?"}
  Timing -- Yes --> Advance["Advance profile; cap catch-up work"]
  Timing -- No --> Hold["Keep profile; recalculate feedback"]
  Advance --> Distance{"Which distance band?"}
  Hold --> Distance
  Distance -- "Outside outer boundary" --> Far["Full translation feedforward; no added XY damping"]
  Distance -- "Between boundaries" --> Near["Fade feedforward; increase XY damping smoothly"]
  Distance -- "Inside inner boundary" --> Inner["Zero translation feedforward; full configured XY damping"]
  Far --> Sum["Combine profile, feedback and damping"]
  Near --> Sum
  Inner --> Sum
  Rate{"Nonzero measured turn rate?"} -- Yes --> Damp["Oppose measured turning"]
  Rate -- No --> NoDamp["Angular damping contributes zero"]
  Damp --> Sum
  NoDamp --> Sum
  Sum --> XY{"Translation request exceeds limit?"}
  XY -- Yes --> Scale["Scale X and Y together; preserve direction"]
  XY -- No --> KeepXY["Keep translation request"]
  Scale --> R{"Turn request exceeds limit?"}
  KeepXY --> R
  R -- Yes --> Cap["Cap angular speed magnitude"]
  R -- No --> KeepR["Keep angular request"]
  Cap --> Candidate["Candidate motion request"]
  KeepR --> Candidate
  Candidate --> Override["Hold state decides: apply candidate OR command zero"]
  classDef decision fill:#fff0cb,stroke:#b66b00,color:#4b3108;
  classDef damping fill:#ffe2ce,stroke:#ab580d,color:#492408;
  classDef correction fill:#deedfb,stroke:#176fc1,color:#102f4d;
  class Timing,Distance,Rate,XY,R decision;
  class Far,Near,Inner,Damp,NoDamp damping;
  class Advance,Hold,Sum,Scale,KeepXY,Cap,KeepR,Candidate,Override correction;
```

**Damping** is an opposing contribution based on measured velocity, like a shock absorber.
Translation damping grows as the robot enters its final approach band.
Rotation damping is applied throughout active correction and scales with measured turn rate.
These contributions may reduce the net request without making the robot move backward or reverse its
turn. Other components are added before the request reaches the motors.

**Feedforward fading** reduces the planned translation-speed contribution near the goal.
**Feedback** still corrects position and heading errors.
**Hysteresis** uses different entry and escape limits to prevent repeated hold/release toggling.
These are separate mechanisms.

## Four distinct settle-entry decisions

| Decision | If yes | If no |
|---|---|---|
| Position error within configured tolerance? | Position qualifies | Entry cannot qualify |
| Heading error within selected tolerance? | Heading qualifies | Entry cannot qualify |
| Measured translation slow enough? | Translation speed qualifies | Entry cannot qualify |
| Measured turning slow enough? | Turn rate qualifies | Entry cannot qualify |

All four must pass together to **enter** the hold. Once holding, small failures of these tight entry
checks do not restart correction. Any wider position, heading, translation-speed, or turn-rate escape
violation releases the hold. A timeout or interruption also ends the command with zero requested motion.

## Click to explore

<details>
<summary><strong>Why do we use two driving controllers?</strong></summary>

**PathPlanner** follows the main route and its planned speeds. **DriveToPose** works toward the final
position and heading. IF the route's handoff condition is met, THEN the final controller takes over
using the robot's current pose and speed. In the current composition it also takes over if the
coarse command finishes first. This is not a universal "80 percent complete" rule.

</details>

<details>
<summary><strong>Damping: the electronic shock absorber</strong></summary>

IF the robot still moves, THEN damping adds a contribution opposing that measured motion.
Translation damping grows through the final approach band; rotational damping opposes measured
turning throughout active correction. It does not own the wheels or decide whether the command
has finished. Too much damping can make the approach slow.

</details>

<details>
<summary><strong>Feedforward versus feedback: planned motion versus correction</strong></summary>

**Feedforward** contributes the speed planned by the motion profile. **Feedback** corrects the
difference between the target and the measured state. IF the robot enters the final distance band,
THEN the planned translation contribution fades while translation damping grows. Position and
heading feedback remain available. The resulting combined request is then limited.

</details>

<details>
<summary><strong>Why not finish as soon as position looks correct?</strong></summary>

The robot can pass through the right position while still moving quickly or turning. IF position,
heading, translation speed, AND turn rate qualify together, THEN it enters zero hold. IF the hold
lasts long enough without an escape, THEN the command succeeds. A timeout stops the command but
does not mean the target was reached.

</details>

<details>
<summary><strong>Hysteresis: how we avoid stop-correct-stop chatter</strong></summary>

Entering zero hold requires the tighter limits. Leaving it requires a larger error or speed that
crosses a wider escape limit. IF a small fluctuation only crosses an entry limit, THEN keep holding
zero. IF any escape limit is exceeded, THEN resume correction and reset the hold timer.
This gap prevents small measurement fluctuations from constantly restarting corrections.

</details>

<details>
<summary><strong>Sensor fallback, timing, and speed limits</strong></summary>

IF the gyro rate signal is invalid, THEN use the turning rate estimated from wheel motion.
IF a profile step is due, THEN advance it with bounded catch-up work; otherwise keep the profile
while recalculating feedback. IF the combined translation request is too large, THEN scale X and Y
together to preserve direction. IF the turn request is too large, THEN cap it separately.
These mechanisms shape the request; they do not replace the controller.

</details>

## What stays shared and what varies?

- **Shared mechanisms:** profile, feedback, damping, speed limiting, settle/escape logic.
- **Route geometry:** start, target, heading, and handoff predicate.
- **Selected settings:** speed and acceleration limits, distance bands, damping strengths,
  entry/escape tolerances, hold duration, and timeout.
- **Current implementation limit:** many settings are shared constants today. Showing them as
  configurable conditions does not imply a per-route runtime settings interface already exists.
- **Test-only convention:** resetting heading to zero assumes a physically aligned test robot;
  a general route uses its intended starting heading.

The purple boxes estimate position; green follows the main route; blue calculates final corrections;
yellow changes behavior through a decision; orange shows damping-related contributions; red stops or
blocks motion. Colors supplement the labels.

Last source review: September 4, 2026. Keep this map and its interactive counterpart synchronized with
[the functional guide](FUNCTIONAL_ALGORITHM_HANDOFFS.md).

---

[Back to top](#top) · [Documentation index](README.md) · [Detailed IF/THEN guide](FUNCTIONAL_ALGORITHM_HANDOFFS.md)

Maintainers: update this page, the interactive map, and the functional guide whenever behavior changes.
This page uses GitHub-supported [Mermaid diagrams](https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/creating-diagrams)
and [expandable sections](https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/organizing-information-with-collapsed-sections).
