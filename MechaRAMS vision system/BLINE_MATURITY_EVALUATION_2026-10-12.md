# BLine and position-based autonomous maturity evaluation

Subsequent October 12 implementation: four optional BLine1-H4 + Precision
chooser entries now exist. The research-only review below records the evaluation
before implementation. See [validation plan](BLINE_VALIDATION_TEST_PLAN.md) for
implemented guards, current limits, local checks and pending GUI/hardware work.
No robot deployment or physical performance improvement is claimed.

Reviewed October 12, 2026. Research only: no robot source, dependency, control
binding, calibration or deployment changes. No local build, test or simulation
was run. Upstream CI status is evidence about the upstream project, not validation
on MechaRAMS hardware.

## Recommendation

BLine is mature enough to justify an optional, controlled comparison as a main
travel follower. Keep the existing PathPlanner baseline and our precision finish.
The proposed additional pattern is BLine travel -> our geometry-aware handoff ->
DriveToPosePrecisionCommand. Do not replace our final controller simply to acquire
position-based behavior: it already corrects toward a fixed destination and checks
motion, continuous qualification, recovery and hold ownership.

This is an evaluation recommendation, not an installed chooser option. Nothing
here resolves the localization discrepancies or H5 late-yaw case described in
[RESEARCH_HANDOFF.md](RESEARCH_HANDOFF.md).

## Identity and reproducibility

BLine was published by Team 2638 Rebel Robotics, not Team 2056. Its broad approach
is shared with the custom point-to-point systems used by 2056 and 2910. The author's
[announcement](https://www.chiefdelphi.com/t/introducing-bline-a-new-rapid-polyline-autonomous-path-planning-suite/509778)
establishes the attribution. The supplied
[self-correcting-auto discussion](https://www.chiefdelphi.com/t/how-do-self-correcting-autos-work/525111)
was read through the public Discourse JSON because normal page retrieval failed;
post 5 reports 2910 participation in BLine development/testing after Worlds.
Those statements are participant reports, not controlled benchmarks.

New local research clones, outside the robot repository:

| Repository | Local folder under `S:\MechaRAMS\_research_clones` | Reviewed revision |
|---|---|---|
| [BLine-Lib](https://github.com/edanliahovetsky/BLine-Lib) | `BLine-Lib` | `961a76538f0c91043cec2f0d70d910c43acb1b90` |
| [BLine-Web](https://github.com/edanliahovetsky/BLine-Web) | `BLine-Web` | `ab79fa3cfe0a50a49192e8c6ad65256a715dc3cf` |
| [2056 public code](https://github.com/Team2056/PublicCodeBank) | `2056-PublicCodeBank` | `70a74d08142de5b859af78b04d7f6e1a7cf0f6cf` |
| [2910 public 2026 code](https://github.com/FRCTeam2910/2026CompetitionRobot-Public) | Existing `2910-2026` clone | `921c85bf725fbd9c488eb7d557e0e065640da095` |

Our comparison baseline is `2027Prototyping` commit `d2e9a13`. BLine's stable
vendordep selects v0.9.2, tag commit `c88adfd95f1006be9c19d4e3d89a326db8ee6012`.
The later main commit changes README wording. The stable library targets Java 17 /
WPILib 2026; its separate WPILib 2027 branch is beta. No dependency-resolution or
binary-compatibility test in our project has been performed.

## Maturity assessment

| Candidate | Evidence and limits | Decision for MechaRAMS |
|---|---|---|
| Position-driven point/segment progression | Published 2056/2910 implementations and participant field reports; gains and completion policies differ | Established approach worth testing across the whole route |
| BLine-Lib stable v0.9.2 | Versioned releases, documented API, JUnit tests, CI, reported competition use; comparatively young library | Suitable experimental dependency; qualify on our chassis before promotion |
| BLine-Web editor | Browser/desktop authoring, JSON export, tests and parity tooling; README calls it beta | Optional authoring tool; initial comparison can use Java-built paths |
| BLine's native final completion | Pose tolerances, without measured-speed/continuous-settle qualification | Keep as an optional baseline, not our precise-stop authority |
| Our current final controller | Direct physical stop/ownership evidence, including successful H4/H5 cases and known H5 regression | Retain; broader reliability and physical accuracy remain unqualified |
| A*/raycast recovery described by team 2791 in the discussion | Established search concept, but that team's implementation was not linked or audited here | Defer integration; requires obstacle map, footprint and recovery validation |

The latest BLine main [Test and Package run](https://github.com/edanliahovetsky/BLine-Lib/actions/runs/37883272331)
reported success for `961a765`. Its workflow runs build/tests/package on pushes
and pull requests. Reviewed tests cover geometry, traversal, rotation continuity,
cross-track behavior, completion, events, transforms and command composition.
They are useful evidence, but not our collision/traction/latency qualification.

The September 28 v0.9.2 release specifically fixes cross-track geometry and rotation continuity
across early handoffs. Active repair and regression coverage support maintenance
maturity; the recent behavior fixes also argue for pinning a release and repeating
our route matrix, rather than treating every version as already qualified.

The library and editor state BSD 3-Clause licensing. Use a versioned dependency
for a trial rather than copying and maintaining a private follower fork. Preserve
attribution/license requirements if source is ever adapted.

## What BLine adds

Reviewed [FollowPath source](https://github.com/edanliahovetsky/BLine-Lib/blob/961a76538f0c91043cec2f0d70d910c43acb1b90/src/main/java/frc/robot/lib/BLine/FollowPath.java),
especially initialize/execute, target advancement, distance calculation and finish.
Each cycle derives travel from live pose, active polyline targets, remaining
distance and cross-track error. Heading can follow geometric progress separately.
The output limiter starts from measured speed, then limits changes in commanded
speed. Intermediate targets can advance by radius or optional projected progress.

If a robot is delayed before reaching its current target, a geometric follower
continues pursuing that geometry instead of advancing a trajectory clock. That
addresses a real limitation of the time-indexed main travel phase in our hybrid.
Our existing final controller helps only after it takes ownership; it cannot make
an earlier PathPlanner phase position-parameterized.

However, traversal is not arbitrary replanning. Translation cursors advance
forward, rotation progress retains forward progress, and events fire once. A
backward shove after a handoff does not automatically rewind prior route elements
or repeat a mechanism event. Test displacement before AND after corners/events.

The distinction between a passed route anchor and an accomplished mechanism action
must remain explicit. Position-based events can trigger from projection even with
lateral error. Scoring/intake success needs its own sensor/state qualification.

## Why our final controller should remain

BLine's `isFinished()` at line 1232 checks final traversal and pose tolerances;
its source explicitly leaves final velocity tolerance as a TODO. `end()` commands
zero speed. The official [completion guide](https://bline-docs.pages.dev/concepts/key-parameters/)
also describes this limit.

Our [precision controller](../VisionTestingAndCalibration/src/main/java/frc/robot/commands/DriveToPosePrecisionCommand.java)
adds pose AND motion qualification, continuous finish confirmation, hold recovery,
timeout reporting, measured steering-angle hold and preserved ownership afterward.
These respond to observed robot behavior, not just extra theoretical safeguards.
Replacing them with pose-only completion would abandon useful test-backed behavior.

The public 2056 `DriveToPoint` (OPRDriveBase.cpp line 273) supports continuous transit
or distance PID, but its returned `inPosition` is translation-distance only.
2910's AutoFactory waits for `isAtDriveToPointSetpoint`; that method checks position
distance, while heading qualification is separate. Their full team systems may
provide additional sequencing. Neither primitive alone demonstrates our required
precision-stop contract; do not infer that from team reputation.

## Integration conditions for a later experiment

1. Keep the same fused pose, vision policy and physical/calibration state. BLine
   cannot repair bias, stale localization, or wheel slip hidden from the estimator.
2. Use robot-relative speed input/output and explicitly choose the CTRE motor
   request. Avoid accidentally comparing different actuator modes as though only
   the path-following algorithm changed. Resolve identical travel request modes
   for the primary comparison; keep precision velocity control shared.
3. Build from an accepted live start. BLine's default pose-reset consumer is a
   no-op; preserve that for chained travel. The README's optional reset example
   must not reset localization at every stage or overwrite our trusted start.
4. Reuse our final-straight, cross-track and direction checks before handing to
   precision. BLine projected handoffs alone have no corresponding lateral
   corridor check; large radii/projections can cut a constrained corner.
5. Keep local limits conservative around turns. A polyline corner is not
   automatically a dynamically feasible turn. Acceleration limiting constrains
   commands, not actual traction or module response. Minimum-speed floors start
   at zero; do not add them to conceal an uncharacterized low-speed response.
6. Add a travel timeout/stall policy with explicit failure status. A timeout must
   abort or select a validated recovery; it must not masquerade as successful
   coarse completion and blindly start precision through an unfinished corner.
7. Inspect the zero-speed `end()` request at handoff and actual next-owner timing.
   Our precision initialization inherits measured velocity; do not assume that
   automatically eliminates every transition dip under a different follower.
8. Explicitly configure practice-field dimensions and flipping. BLine's defaults
   are 16.54 x 8.07 m; do not apply alliance transforms blindly to our custom board area.
9. Wire the four BLine logging consumers to AdvantageKit. They default to no-ops;
   record progress, remaining distance, handoffs, cross-track/heading error and
   constrained requests alongside our measured speeds and final-controller data.
10. Keep one drivetrain owner. Defer mechanism-event integration until basic travel
    works; BLine's proxy-based command helpers change composition/requirement
    semantics and need a deliberate scheduling review.

## Comparison that would justify adoption

Primary candidates: current PathPlanner travel + our final controller versus
BLine travel + that same final controller. BLine-only completion is a separate
optional reference, not the primary experiment. Common destinations, corridor,
speed/acceleration bounds, actuator mode and localization must be recorded; curved
and polyline geometry cannot be assumed identical merely because endpoints match.

Start with straight travel, then an H3-like translation route, H4-like rotation
and H5-like return. Preserve the known late-yaw 7461 case. Include a stop/post-owner
window of at least 3 s and separate command time, movement time, settling and
estimated versus measured endpoint error. Use at least three valid repeats per
condition for screening and report ranges, not one favorable run.

The distinctive comparison is controlled disturbance recovery. In robot-physics
simulation, test a brief loss of physical progress while keeping sensing truthful,
a lateral displacement, delay before/after a corner, and displacement after an
event. Also test refusal to finish while blocked. A pose reset alone is an
estimator discontinuity experiment, not a simulated collision. Initial physical
tests should be ordinary low-speed routes; contact/disturbance testing needs its
own validated setup, not someone restraining a powered chassis.

Promote BLine for an applicable route family only if it improves recovery or
reduces useful tuning/authoring work without unacceptable corner cutting, settle
time, failure behavior or loss of final-stop quality. Faster generation and public
performance percentages do not establish that outcome on our robot. Reliable
completion after disruption can be valuable even when an undisturbed run takes
slightly longer.

## Status after this review

Research clones acquired and source, tests, release history, documentation and
upstream CI inspected. No dependency installed and no local performance claim.
Optional BLine travel integration is recommended for a later implementation task.
Optical/reference validation and independent wheel/gyro commissioning remain
necessary in parallel; this follower comparison does not replace those layers.
