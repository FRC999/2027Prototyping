# Continue the BLine comparison on another laptop

Updated October 12, 2026. Handoff marker: `BLINE_HANDOFF_2026-10-12`.

This file, the implementation, dependencies and test plan are versioned together
in [FRC999/2027Prototyping](https://github.com/FRC999/2027Prototyping), branch `main`.
No manual copying of code, prompts, guides or viewer layouts is required. The
third-party research clones and prior raw WPILOG files are not needed to build
or begin these new tests: Gradle fetches the pinned BLine dependency.

## Retrieve and identify the new version

In an existing checkout, inspect local changes before updating:

```powershell
git status --short --branch
git pull --ff-only origin main
git log -1 --oneline -- "MechaRAMS vision system/BLINE_LAPTOP_HANDOFF.md"
```

If the other laptop has local changes or diverged commits, preserve them and
reconcile deliberately; do not reset or overwrite them to make a pull succeed.
For a new checkout:

```powershell
git clone https://github.com/FRC999/2027Prototyping.git
cd 2027Prototyping
```

The checkout location is arbitrary. `S:\MechaRAMS` and `C:\MechaRAMS` in historical
examples identify the earlier machines; substitute this laptop's checkout and
WPILib installation paths. Open `VisionTestingAndCalibration` in WPILib VS Code,
not the sibling documentation folder. The Gradle wrapper JAR and scripts are
tracked. Install/use WPILib2026, Java17+, AdvantageScope and internet access for
dependencies; do not migrate this experiment to WPILib2027 beta while testing it.

## What is implemented

Stable BLine-Lib v0.9.2, with explicit JSON Simple runtime dependency. Four
additional Autonomous Mode entries coexist with the existing PathPlanner/H routes
and the `No Auto` default:

1. `BLine 1 - Forward Entry + Precision`
2. `BLine 2 - Forward Then Strafe Left + Precision`
3. `BLine 3 - Forward Then Diagonal Left + Precision`
4. `BLine 4 - Diagonal With Camera-Facing Yaw + Precision`

Each takes a fresh trusted MultiTag start, applies the existing safe-start and
generated-target checks, normalizes yaw once for the physically squared setup,
then runs BLine polyline travel. Its guarded final-segment/distance/line/direction
handoff starts our unchanged DriveToPosePrecisionCommand. The final stop retains
estimated4 cm/1.5degree limits, motion checks, continuous qualification, recovery,
4 s timeout and measured-wheel-angle hold. Native BLine pose-only completion is
not the final authority. Travel timeout/invalid state/unsafe coarse completion
stops without starting the last leg or reporting successful placement.

BLine gains5/7/2 and intermediate radius0.10m are provisional. The corner-target
cap is0.60m/s, final-segment cap0.80m/s (all of BLine1's straight), global ceiling
1.2m/s and travel timeout8s. The initial experiment compares validity/recovery;
its geometry/limits differ from the PathPlanner spline, so don't infer which is
universally faster from unequal settings.

Both main travel branches use the existing CTRE ApplyRobotSpeeds OpenLoopVoltage
mode (verified in Phoenix26.3.0). Final precision uses its separate Velocity
request. BLine zeros path-force arrays. Do not silently switch main actuator mode
or retune vision while comparing the route followers.

No BLine H5 is installed: its mandatory far-end reversal requires a separately
qualified intermediate stop. Existing H5 is a regression, including known late-yaw
case7461. No arbitrary-heading paths, obstacle replanning, mechanism events or
alliance flips were added. Stationary aiming remains open-loop; its previously
recommended change has still not been implemented. Camera recalibration remains
unconfirmed, and the flexible board limits physical-accuracy claims.

## What has actually been verified

- `compileJava`: pass.
- JUnit:95/95 pass.
- `jacocoTestCoverageVerification`: >=90% line coverage on each gated pure class.
- `jar`: pass; packaged BLine FollowPath, JSONParser and guarded travel classes.
- Actual pinned BLine follower with an ideal kinematic plant:1 s delay,15 cm lateral
  displacement, blocked-route timeout. Also geometry/yaw/constraints and lifecycle
  failure/interruption/requirements tests.
- Maintained document links and interactive HTML script/detail-node consistency.

These are local toolchain and automated checks. Full CTRE/PhotonVision GUI
simulation, physical driving and deployment have NOT been performed for BLine.
No conclusion of better physical precision or faster settling is established.
Two existing vision test expectations were corrected for the already configured
left-camera2.15 factor; actual vision policy/weights and final-controller gains
were not changed.

## Next work, in order

Read [RESEARCH_HANDOFF.md](RESEARCH_HANDOFF.md), the top of
[SESSION_STATE.md](SESSION_STATE.md), then
[BLINE_VALIDATION_TEST_PLAN.md](BLINE_VALIDATION_TEST_PLAN.md). The latter contains
exact options, click-by-click setup, signals, thresholds and failure tests.

1. Verify the pulled checkout and its toolchain on this laptop. From the robot
   project run `./gradlew.bat compileJava test jacocoTestCoverageVerification`.
   If Java reports PKIX certificate errors, the validation plan has the temporary
   Windows-ROOT trust-store workaround. Keep certificate validation enabled.
2. Complete normal desktop simulation first. Establish the SIM start in enabled
   teleop with A, then disable and require BLine/Test/Preflight/ReadyToEnable.
   Pair H1 with BLine1 first, three valid repeats each. Reset the SIM start before
   each run; a prior endpoint is outside the allowed start box.
3. Progress to paired H2/BLine2, H3/BLine3 and H4/BLine4 only after earlier checks
   pass. Run unchanged H5 as a regression. Preserve >=3s after completion, disable,
   close/rotate the log, and verify its endpoint before analysis.
4. Check start rejection, interruption during travel/final control, qualified
   handoff, timeout without precision and preserved default-command stop ownership.
   Physical-disturbance GUI buttons were not installed; the plan distinguishes
   existing automated tests from later physics-harness extensions.
5. After desktop checks, the team performs manual hardware deployment and bounded
   tests using the same camera/configuration state and physical start marks.
   Do not use the SIM fixed A reset as a real-robot measurement reference.
6. Analyze per-run logs before changing gains. Separate command/movement/settling
   times, estimated and physical endpoint, yaw, handoff speed, failures and trial
   range. Keep optical/reference accuracy separate from follower recovery.

AdvantageScope viewer exports in `advantage-scope-layouts/` are tracked. Live
outputs use `AdvantageKit/RealOutputs/`; saved logs use `/RealOutputs/`. New signals
are `BLine/Test/*`, `BLine/Travel/*`, `BLine/FollowPath/*`, plus existing Drive and
DriveToPose channels. SUCCESS plus Completed=true is required; phase ENDED alone
does not prove successful placement. Exact channel table is in the validation plan.

New logs on the other laptop can be analyzed there. Old raw logs listed in
`evidence/local-log-inventory-2026-10-12.json` remain outside Git; their findings
are preserved in reports. Retrieve the originals separately only for historical
reanalysis, not as a prerequisite for the new BLine experiment.

## Handoff message for Codex or Claude

This message is already in the repository; the next assistant can read it here.

> Continue the MechaRAMS optional BLine evaluation in FRC999/2027Prototyping on
> main. Read this BLINE_LAPTOP_HANDOFF.md, RESEARCH_HANDOFF.md, the latest
> SESSION_STATE.md and BLINE_VALIDATION_TEST_PLAN.md before acting. Preserve this
> laptop's local work and resolve historical absolute paths against its actual
> checkout. Four BLine1-H4 + Precision options are implemented and locally
> compiled/tested/packaged, but GUI and hardware qualification are pending. Begin
> with the plan's paired H1/BLine1 desktop checks; do not claim deployment or
> physical precision from automated ideal-kinematic tests. Keep the shared final
> controller, vision configuration and physical-reference limitations intact.
> H5 remains a PathPlanner regression; no BLine reversal exists. Do not implement
> the unrelated stationary-aim proposal or change gains to conceal camera/board
> bias. Record the actual code/calibration state, exact new logs, results and
> failures; update maintained guides and session/prompt records. Ask the mentor
> before hardware deployment. Commit/push authorized completed work so the next
> laptop receives the durable record.
