# BLine optional autos: validation test plan

October 12, 2026. Source integration is implemented; desktop GUI and physical
qualification remain pending. BLine-Lib is pinned to v0.9.2. No robot deployment
was performed by the assistant. See [maturity review](BLINE_MATURITY_EVALUATION_2026-10-12.md)
for why the main follower is being compared and the final controller is retained.

## What the new option does

Fresh trusted MultiTag start -> existing safe-start/target checks -> one explicit
start reset with normalized yaw -> BLine polyline travel -> guarded geometric
handoff -> unchanged DriveToPosePrecisionCommand -> preserved wheel-angle hold.

The robot must physically face +X/square to the board before these current-start
tests. They support varying start X/Y, not arbitrary start heading or obstacles.
BLine does not depend on PathPlanner warmup. There are no mechanism events,
alliance flipping, automatic obstacle replanning or minimum-speed floors.

| Autonomous Mode selection | Existing comparison | Final displacement from accepted start |
|---|---|---|
| `BLine 1 - Forward Entry + Precision` | `Holonomic 1 - Forward Entry` | X +1.50 m, Y unchanged, yaw 0 degrees |
| `BLine 2 - Forward Then Strafe Left + Precision` | `Holonomic 2 - Forward Then Strafe Left` | X +1.50 m, Y +0.75 m, yaw 0 degrees |
| `BLine 3 - Forward Then Diagonal Left + Precision` | `Holonomic 3 - Forward Then Diagonal Left` | X +2.25 m, Y +0.75 m, yaw 0 degrees |
| `BLine 4 - Diagonal With Camera-Facing Yaw + Precision` | `Holonomic 4 - Diagonal With Camera-Facing Yaw` | X +2.25 m, Y +0.75 m, yaw -20 degrees |

H5 is deliberately not ported: a reliable intermediate stop/reversal requires
more than BLine's native pose-only completion. Keep the existing
`Holonomic 5 - Out And Return To Start` as the regression, including the known 7461
late-yaw case. Native BLine-only final placement is not an installed option.

## Limits and finish rules

BLine translation/rotation/cross-track kP = 5/7/2; kI/kD = 0. These are provisional
experimental gains. Global ceilings match H tests: 1.2 m/s, 1.2 m/s^2, 60 deg/s,
120 deg/s^2. Corner-target travel is additionally capped at 0.60 m/s and the final
segment at 0.80 m/s. For BLine1, the entire straight is that final segment.
Intermediate handoff radius is 0.10 m, projected handoffs enabled. Travel timeout is 8 s.

The final handoff needs ALL: armed after being >= 1.00 m from the goal, final BLine
translation target active, distance <= 0.30 m for BLine2 or <= 0.55 m otherwise,
inside the final straight, cross-track <= 0.05 m, direction within 30 degrees when
measured translation speed exceeds 0.10 m/s. Invalid pose/speeds, timeout or native
completion without that geometry aborts travel. It must not start precision.

Last-leg finish uses the current shared 4 cm translation/1.5 degree yaw policy,
pose and motion gates, continuous 50 ms qualification, recovery, measured-angle
hold and 4 s precision timeout. Rotating-finish classification and stricter
wheel/gyro checks remain unchanged. These are estimated-pose requirements;
physical accuracy is a separate question.

Compare geometry and actuation honestly: BLine follows polylines through the
shared anchors; PathPlanner rounds splines through them. Both main stages use
the CTRE ApplyRobotSpeeds request family with its existing OpenLoopVoltage default,
verified against the installed Phoenix 26.3.0 constructor. PathPlanner populates
modeled wheel-force arrays; BLine explicitly supplies zeros. Do not assume that
provides closed-loop wheel tracking during main travel. The common final controller
continues using its separate closed-loop Velocity request. The additional corner/final-segment
caps mean this first screening is not an equal-constraint controller-only timing
benchmark. Match verified limits in a separate performance experiment later.

## 1. Automated checks

Original-laptop continuation: local toolchain checks and automated full configured
CTRE/PV desktop screening are now complete. [Results](BLINE_DESKTOP_SCREENING_2026-10-12.md)
record25 qualified stops, start rejection/interruption, timing limitations and
remaining GUI/physical/recovery work. This is distinct from the ideal-plant tests.

From `VisionTestingAndCalibration`, using WPILib Java 17+:

```powershell
.\gradlew.bat compileJava test jacocoTestCoverageVerification
```

Completed locally: 95/95 tests pass and the >= 90% pure-class line-coverage gate
passes. New checks exercise native BLine path construction, yaw versus spline
tangents, corner constraint ordinals, guarded command ownership/lifecycle,
qualified handoff, interruption, invalid state and timeout. Actual pinned-follower
tests use a deterministic ideal kinematic plant for a 1 s physical-progress delay,
15 cm lateral displacement and continuously blocked travel. They do not model
CTRE traction, camera fusion or last-leg physical dynamics.

Two pre-existing VisionPolicy assertions were corrected to include the already
configured left-camera 2.15 trust factor. Vision code, camera weights and precision
gains were not changed for this integration.

If this PC reports Java certificate errors while downloading JitPack, use its
Windows trust store without disabling certificate validation, in the current
PowerShell session:

```powershell
$env:JAVA_HOME='C:\Users\Public\wpilib\2026\jdk'
$env:JAVA_TOOL_OPTIONS='-Djavax.net.ssl.trustStoreType=Windows-ROOT -Djavax.net.ssl.trustStore=NONE'
.\gradlew.bat compileJava test jacocoTestCoverageVerification --no-daemon
```

## 2. Desktop simulation, click by click

1. Open the project in WPILib VS Code. Press Ctrl+Shift+P, choose **WPILib:
   Simulate Robot Code**, select Sim GUI and Driver Station when prompted.
2. Open AdvantageScope, connect to the simulator at localhost. Use the existing
   field/Auto Finish Ownership layout; add the channels below.
3. In Driver Station select **Teleoperated**, enable, release the sticks and press
   A once to establish the SIM start (1.5,2.0,0 degrees). Then disable. This fixed
   reset is for SIM setup only, not real current-start measurements.
4. Wait for fresh vision and `BLine/Test/Preflight/ReadyToEnable=true`.
5. In Sim GUI open **NetworkTables** -> **SmartDashboard** -> **Autonomous Mode**.
   Find the chooser's `selected` string and set it to the exact BLine1 name above
   (or use a connected dashboard chooser). Confirm the logged selected name.
6. Select **Autonomous** in Driver Station and enable. Watch BLINE_TRAVEL become
   FINAL_PRECISION; then expect Completed=true and CompletionReason=SUCCESS.
7. Keep enabled, sticks neutral, for at least3 s after completion. Confirm wheel
   targets do not jump and the hold survives default-command ownership. Disable.
8. While disabled, open SmartDashboard's **Close Current Log And Start New
   (Disabled Only)** command and set its `running` value true (or click it in a
   connected dashboard). Use this close/rotate command, not the delete-logs one.
   Verify the closed file includes completion and the post-stop window before analysis.
   See [controls](ROBOT_CONTROLS.md) and [runbook](SIMULATION_RUNBOOK.md).
9. Return to the SIM start with step 3 BEFORE each next run. Otherwise a current-
   start auto may correctly reject an endpoint as outside its starting box.
10. Run paired H1/BLine1 at least three times each. Then H2/BLine2, H3/BLine3 and
    H4/BLine4, returning to the start each time. Run existing H5 as a regression.

The equivalent DS/dashboard sequence was automated through the actual desktop
Robot/CTRE/PV runtime with25 valid route stops and failure checks. GUI visual
inspection has not been performed. See the [report and runner](BLINE_DESKTOP_SCREENING_2026-10-12.md)
before interpreting this stage or its remaining performance/recovery limitations.

## 3. Signals to inspect

The repository now includes [BLine Comparison.json](advantage-scope-layouts/BLine%20Comparison.json)
with original finish graphs plus BLine live/saved tables. All27 added keys were
verified against a finalized runtime log; GUI import/visual review is still pending.

Live outputs are under `AdvantageKit/RealOutputs/`; saved WPILOG outputs are under
`/RealOutputs/`. BLine internal callbacks are namespaced `BLine/FollowPath/`.

| Channel | Check |
|---|---|
| `BLine/Version` | v0.9.2 |
| `BLine/Test/Preflight/ReadyToEnable` | True while disabled at an accepted start |
| `BLine/Test/StartAccepted`, `AbortReason`, `PathBuildError` | Start/result labels for this run |
| `BLine/Test/Mode`, `CurrentPhase`, `CompletionReason`, `Completed` | Success requires SUCCESS and Completed=true; ENDED alone proves nothing |
| `BLine/Test/NormalizedStartPose`, `FinalTargetPose`, `PlannedRouteAnchors` | Accepted translation, destination and polyline anchors |
| `BLine/Test/FinalHandoff/Armed`, `Triggered`, `DistanceMeters` | Handoff only on the final aligned approach |
| `BLine/Travel/Outcome`, `Active`, `ElapsedSeconds` | HANDOFF or explicit failure; RUNNING ends by8 s |
| `BLine/FollowPath/translationElementIndex`, `currentSegmentProgress`, `remainingPathDistanceMeters` | Position-driven progress; no clock-driven target advance during a physical delay |
| `BLine/FollowPath/crossTrackError`, `translationControllerOutput`, `outputOmegaRadPerSec` | Correction and heading response |
| `BLine/Test/HandoffPose`, `HandoffSpeeds`, `FinalPose` | Transition speed/pose versus post-command estimate |
| `DriveToPose/FinishQualified`, `TimedOut`, translation/rotation errors | Qualified final placement rather than timeout |
| `Drive/ModuleStates`, `ModuleTargets`, gyro rate, CommandOwner, PrecisionAngleHoldActive | Actual motion and preserved stop after command completion |

Follower progress outputs can remain at their last value after travel ends. Use
Travel/Active and phase boundaries; retained values are not another executed run.

## 4. Failure and recovery verification

First verify rejection while disabled/no valid start: no-fresh-vision and an
out-of-area start must reject without commanded travel. Verify disable during
travel AND precision stops, logs interruption, and does not report success.

For controlled disturbance work, extend a desktop physics harness before physical
contact tests. Hold physical progress for1 s with truthful sensing, displace
laterally15 cm, and repeat before/after corners. A continuously blocked route must
end TRAVEL_TIMEOUT with Completed=false and never enter FINAL_PRECISION. Add a
case where native coarse completion occurs outside our final-line gate: it must
abort UNSAFE_COARSE_END. A pose reset tests estimator discontinuity, not collision
recovery. The GUI has no newly installed physical-disturbance button in this change.

Check recovery route clearance, target progression, handoff and finish reason.
Projected progress can advance intermediate targets with lateral error; the final
gate does not provide obstacle protection for earlier corners. Do not restrain a
powered chassis by hand to imitate a delay or invent an unverified recovery route.

## 5. Physical screening and adoption criteria

After desktop validation, the team manually deploys. Keep the same camera focus,
intrinsics/layout/mounts, vision modes, wheel configuration and final gains. Record
deployed source revision, battery, initial pose, route limits and exact log ID.
Use original physical start marks/square alignment, not the SIM A reset. Confirm
actual swept-frame clearance before every route; controller bounds are backstops.

Begin with BLine1 and H1, three valid repeats each. Add paired H2-H4 only when the
earlier stage passes. Preserve >=3 s enabled post-finish evidence and finalize
one driving log per run. Measure rear-frame corners in fixed ORIGINAL axes;
camera/board limitations remain in force. Repeat H5 on unchanged controller code.

Pass screening requires successful qualified finishes without timeout, correct
handoff geometry, no unacceptable overshoot/corner excursion, and unchanged stop
ownership. Report command time, movement time, settling share, measured physical
endpoint, estimated endpoint, yaw, handoff speed and trial range separately. Use
the same definitions in the [commissioning playbook](COMPETITION_CHASSIS_CALIBRATION_PLAYBOOK.md).
The desired settling share remains <10%, not a promise all current cases meet it.

Adopt BLine for a route family only after it demonstrates better recovery or
meaningfully simpler tuning/authoring without worse clearance, placement, settling
or failure behavior. Equal endpoint precision is plausible with the shared final
controller, but it is not established by this implementation or a single sim run.
