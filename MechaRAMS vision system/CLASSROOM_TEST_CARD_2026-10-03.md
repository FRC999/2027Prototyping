# Classroom test card — October 3, 2026

Progress: H5-1 **78cd**, H5-2 **7461** and H4 **deb8** are received and analyzed.
[Comparison](H5_CLASSROOM_BASELINE_2026-10-03.md): good stopping in 78cd but a
physical Y discrepancy; 7461 returns closer but has real angular correction.
[H4 comparison](H4_DEB8_CLASSROOM_BASELINE_2026-10-03.md): good stop, physical
rear-corner geometry consistent with the intended Y/yaw and about3.5cm extra centerX.
This driving baseline is COMPLETE. No additional repeat or mandatory static-distance
test is requested now. The next control investigation is the log-confirmed H5
late yaw recovery; no robot-code or layout change has been made. The
[static X-distance comparison](STATIC_X_DISTANCE_TEST_CARD_2026-10-03.md) is
an optional future calibration-method test, not today's required next step.

Measurement clarification: ALL reported measurements use REAR frame corners;
leftX=rear-left, rightX/Y=rear-right. The instructions below have been corrected
to preserve that convention. Do not apply prior front-corner assumptions.

Manual measurements are approximate sanity checks. Camera-based robot pose is the
primary reference for prototype trajectory tuning, checked against both cameras,
gyro/module motion and gross physical discrepancies. Do not tune to calculated
millimeter/tenth-degree differences from rough ruler data.

## Purpose and scope

Learn a repeatable calibration procedure, not perfect this prototype's constants.
Today separates physical return accuracy, camera-estimate movement, and angular
settling. No new controller settings, camera weights, speed, or deployment are
required if the version used for 4a06 remains installed. Do not change focus today.
Both cameras stay open. Two cameras are enough for these existing board-facing routes.

The left camera was refocused without recalibration. We can study motion and
compare with rulers, but must label its absolute calibration provisional. This is
not competition-chassis accuracy qualification. Recalibration remains part of the
new-chassis procedure, not a blocker to this bounded classroom exercise.

This card supersedes earlier "next test" instructions, including the older
SmartDashboard `NEXT TESTS - Order` reminder. That reminder has not been changed.
The deferred cold-boot/auto-without-teleop investigation is not today's experiment.

## Test order

Select these exact entries in SmartDashboard's **Autonomous Mode** chooser.
All displacements are from that run's start, in the board-aligned field axes.

| Order | Chooser entry | Intended final robot-center displacement | Main question |
|---|---|---|---|
| 1 | Holonomic 5 - Out And Return To Start | X=0 m, Y=0 m, yaw=0 degrees | Does the physical return agree with localization? |
| 2 | Holonomic 5 - Out And Return To Start | X=0 m, Y=0 m, yaw=0 degrees | Is that result repeatable? |
| 3 | Holonomic 4 - Diagonal With Camera-Facing Yaw | X=+2.25 m, Y=+0.75 m, yaw=-20 degrees (clockwise) | Does a turning finish spend extra time correcting yaw? |
| Optional, if time | Holonomic 3 - Forward Then Diagonal Left | X=+2.25 m, Y=+0.75 m, yaw=0 degrees | Same outbound endpoint without the intended final rotation |

H5 travels outward through the left-side area before returning; zero net travel
does NOT mean a stationary test. Its turnaround must slow/reverse. Do not count
that necessary intermediate reversal as final settling.

## Before the first run

1. Clear the entire previously used outbound/return corridor, including floor
   wires, the space behind the start, and the robot's swept corners. Keep a driver
   ready to disable immediately. Software start checks do not detect obstacles.
2. Use a charged battery. Record any battery replacement between runs; do not mix
   it silently into a tuning comparison.
3. Place the robot at the normal marks, perpendicular to the tag board, facing
   +X; +Y is left. For this comparison, use the same marks for every run. The code
   samples current vision X/Y, so an exact absolute board distance is not required,
   but the position must preserve the whole route's clearance and pass preflight.
   These tests normalize starting yaw to zero: they are NOT arbitrary-heading
   robot-relative routes. Do not start H3/H4 from H5's outward/left endpoint.
4. Mark the floor directly under both REAR FRAME corners. Use the rear-right
   corner for Y. Record the distance between the two frame references
   if it differs from the previous 0.6072 m. Do not alternate frame and bumper.
5. Use the existing layout:
   `C:\MechaRAMS\temp\AdvantageScope 9-28-2026 - Auto Finish Ownership.json`.
   It has live and saved-log views. No additional logging code or new layout is
   needed. The sidebar can show readiness fields even if a table does not list them.
6. While disabled, check `PathPlanner/HolonomicTest/Preflight/ReadyToEnable=true`
   and `Drive/GyroYawRateSignalOK=true`. Confirm `Drive/CommandOwner`,
   `Drive/PrecisionAngleHoldActive`, and `Drive/ManualDriveAllowed` exist. Hold may
   be false before the first run. If readiness stays false, save the status/log;
   do not force the start, reset to the fixed test pose, or assume teleop is required.

## Repeat this procedure for EACH driving run

1. With the robot disabled and positioned, press
   **Close Current Log And Start New (Disabled Only)**. Wait for
   `Logging/RotationPending=false`, a changed `Logging/ActiveLogPath`, and no
   rotation error. Write down this NEW path: it is the file for this run.
2. Select **Static Localization - PnP + Iso (Disabled Only)**. Then press
   **Start Camera Jitter Capture (Disabled Only)** without moving the robot.
   Confirm a NEW capture: sample counts restart, then both reach 100;
   `Vision/JitterCapture/ComparisonReady` becomes true and `Active` becomes false.
   It stops automatically. Do not press Stop after an arbitrary three seconds.
   An already-true Ready value from an older capture does not count.
3. Select the appropriate Autonomous Mode from the table. Recheck disabled
   preflight. Jitter capture must be OFF. Normal WPILOG recording stays ON.
   Do not press A / Reset Pose - Test Start or the fixed Precision Drive button.
   This auto samples and seeds its own fresh vision start.
4. Enable Autonomous and let this ONE route finish. Do not touch the controls.
   Disable immediately for unsafe/unexpected motion; safety overrides this card.
5. When `PathPlanner/HolonomicTest/Completed=true`, also verify
   `DriveToPose/TimedOut=false` and `DriveToPose/FinishQualified=true` for this run.
   Completed alone does not prove a precision success. If safe, leave Autonomous
   enabled, supervised and untouched for at least THREE more seconds after
   completion, and longer if the wheels have not stopped. This records coasting,
   steering motion, and the handoff to the default command. Then disable.
6. WITHOUT repositioning, measure both rear-frame X displacements and the rear-right
   corner's Y displacement from their starting marks. For H5: positive X means it
   stopped forward of its original position; negative means behind. Positive Y
   means left. Use signed centimeters or meters, with units explicitly stated.
7. Still disabled and stationary, press **Start Camera Jitter Capture (Disabled Only)**
   again. Wait for the NEW 100/100 samples, ComparisonReady=true, Active=false.
   Keep both pre-run and post-run captures in this SAME driving log.
8. Press **Close Current Log And Start New (Disabled Only)** to finalize the run.
   Wait for rotation to finish. Download the file you wrote down in step 1,
   NOT the new mostly empty active file. Do not use the Delete Stored Logs button.
   Open the downloaded file and confirm it contains the auto, post-finish interval,
   and endpoint capture. A layout save is not a log save.
9. Save it in `C:\MechaRAMS\temp`, report its filename/suffix and measurements,
   then reposition to the original marks for the next run. Each driving run gets
   a separate finalized file. Static captures belong to their associated run.

If capture cannot reach Ready or the precision move times out, stop the sequence
and send that log. If only H4 endpoint measuring is impractical, run it as a timing
and angular-response test and label physical accuracy UNMEASURED. Do not guess.

## What to report

| Run | Log suffix | Left frame X | Right frame X | Y and exact reference | Observations / battery change |
|---|---|---|---|---|---|
| H5-1 | | | | | |
| H5-2 | | | | | |
| H4-1 | | Optional | Optional | Optional | |
| Optional H3 | | | | | |

For rotated H4, corner displacements are not the robot-center displacement. In
particular, averaging the two rear X values tracks the rear-edge midpoint, which
moves relative to the center when yaw changes. We will not compare those numbers
directly with the +2.25 m center target. Record a floor outline/independent heading
if convenient; no precise H4 physical survey is required for today's timing test.

## Useful AdvantageScope table fields — ALREADY recorded

These exact output paths were checked in source and the saved 4a06 WPILOG.
In a live connection, prefix each below with `NT:/AdvantageKit/RealOutputs/`.
In a saved WPILOG, prefix it with `/RealOutputs/`.

- `PathPlanner/HolonomicTest/Preflight/ReadyToEnable`
- `PathPlanner/HolonomicTest/CurrentPhase`
- `PathPlanner/HolonomicTest/Completed`
- `DriveToPose/TranslationErrorMeters`
- `DriveToPose/RotationErrorDegrees`
- `DriveToPose/WithinPoseTolerance`
- `DriveToPose/SettlingHoldActive`
- `DriveToPose/SettlingHoldExitCount`
- `DriveToPose/FinishQualified`
- `DriveToPose/TimedOut`
- `Drive/PrecisionAngleHoldActive`
- `Drive/MaxAbsModuleSpeedMetersPerSecond`
- `Drive/GyroYawRateDegreesPerSecond`
- `Drive/GyroYawRateSignalOK`
- `Vision/JitterCapture/Active`
- `Vision/JitterCapture/ComparisonReady`
- `Vision/Camera0/Jitter/SampleCount` and `Vision/Camera1/Jitter/SampleCount`
- `Logging/ActiveLogPath`
- `Logging/RotationPending`
- `Logging/LastRotationError`

Battery is an INPUT, with a different prefix:
live `NT:/AdvantageKit/SystemStats/BatteryVoltage`, saved `/SystemStats/BatteryVoltage`.
It is roboRIO supply voltage, not PDH total current. The recent logs' PDH voltage
was zero/unusable; do not use it as the battery measurement.

No items need to be added to robot logging for today's tests. Displaying a field
in a table is optional and does not enable or disable its recording. Full logs
also contain controller requests, measured speeds, module angles, camera poses,
and configuration used for the deeper analysis.

## What happens after these runs

We report physical error separately from estimated error, command duration,
wheel/steering stop time, final-settling fraction, camera noise/bias comparison,
and any loop-delay correlation. We do not tune gains just because an estimate
moved while the wheels were stopped.

If H5 is physically repeatable, keep that baseline. If H4 alone shows real yaw
oscillation, the next controlled experiment changes only the rotating final-
approach behavior and includes an H5 regression run. If physical and estimated
endpoints disagree, investigate localization before that controller experiment.
The broader transfer procedure is in
[Competition Chassis Calibration Playbook](COMPETITION_CHASSIS_CALIBRATION_PLAYBOOK.md).
