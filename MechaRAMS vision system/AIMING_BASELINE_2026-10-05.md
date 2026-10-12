# A0 stationary aiming: dc76 and dd6b

Classroom tests captured October5; rechecked offline October12,2026. Mentor reports
smooth turns and no visible sustained jitter; the counterclockwise-start trial
appeared to overshoot farther. Labels describe the initial displacement from aim,
not the direction of the commanded correction.

## Evidence

| Item | Clockwise start: dc76 | Counterclockwise start: dd6b |
|---|---:|---:|
| Original filename | `akit_rotated_1791247101169_fa28dc76.wpilog` | `akit_rotated_1791247184970_3700dd6b.wpilog` |
| File bytes | 4,130,133 | 3,967,786 |
| Aim ownership starts, FPGA s | 1383.476084 | 1467.052404 |
| Default ownership resumes, FPGA s | 1386.484709 | 1468.339589 |
| Ownership duration | 3.008625 s | 1.287185 s |
| Initial estimated heading-error magnitude | 6.036 degrees | 13.480 degrees |
| Peak overshoot past estimated target | 2.824 degrees | 4.833 degrees |
| Estimated heading-error magnitude at end | 2.159 degrees | 1.107 degrees |
| Peak gyro yaw-rate magnitude | 34.160 deg/s | 74.250 deg/s |
| Minimum roboRIO battery voltage during command | 11.801 V | 11.543 V |
| Interpretation | Safety timeout; did not qualify | Qualified heading finish |

SHA256 fingerprints:

- dc76: `fc0b2fdc06c63f560426cc91ec97f810912adf6b004ea4f43a92c8f60bbdd425`
- dd6b: `3287321528872c4b2fa79e516292bab2159f28ab20bf913dc1f9f5ae40861f83`

Both files are in the original PC's `C:\MechaRAMS\temp`, outside Git. Results are
preserved here; copy raw files separately for reanalysis.

## Method and interpretation

Use the existing `tools/audit_h4_history.py` **Log** reader, not its H4-specific
report. Bound each trial using Drive/CommandOwner=AimAtGoalCommand through the
next owner. At each Drive/Pose time, compute wrapped signed yaw-minus-current-
Aim/TargetHeadingDegrees error. Overshoot is the largest error on the opposite
side from the initial error. Read gyro and voltage in that window. Changed-only
records are held until their next update; do not extrapolate past log end.

This source version does not explicitly log an aim finish-reason enum. Timeout
versus qualification is inferred from duration, Aim/Aimed transitions and the
command's 3s timeout / 0.2s in-tolerance logic. The robot remained enabled until
well after ownership returned. Do not invent a deployed Aim/TimedOut field.

dc76 entered tolerance at +0.251s and left at +0.381s, before the required0.2s
continuous qualification. Angular rate was mostly below2deg/s after~0.76s.
Small wheel targets persisted while measured wheels were mostly stopped; it
stayed~2.2degrees off until timeout. No visible jitter does not equal a pass.

dd6b entered tolerance at +0.290s and left at +0.334s, then reentered at+1.056s
and completed. It corrected after larger overshoot. Starting errors and battery
conditions differ, so this is not an equal-angle direction comparison.

All errors refer to the robot's estimated goal bearing; physical boresight was
not surveyed. Fused pose changed~3cm during each nominal rotation, not independent
proof that the chassis physically translated that amount.

## Current implementation versus the next experiment

AimAtGoalCommand profiles heading with kP5/kD0, adds profile velocity and calls
generic driveRobotRelative using OpenLoopVoltage. It does not use the existing
driveRobotRelativeVelocity path used by precision drive. Goal=(7.5,2.0)m, front
face; tolerance1.5degrees, settle0.2s, timeout3s. Small open-loop correction against
friction is a plausible contributor to dc76's residual, not a uniquely proven cause.

**Recommended, NOT implemented:** switch stationary aim to the existing velocity
request, keeping gain/profile/target/tolerance unchanged initially; repeat A0.
Compare timeout, overshoot, angular response, small-request wheel response and
after-stop motion. Do not change kD simultaneously. No code change or deployment
of that recommendation has happened.

A later fixed-point moving test needs an explicit board-midpoint target(6,2),
shooting lead disabled, conservative limits and a clear route. Current moving
aim has modeled lead enabled and a virtual target beyond the board; it is not
that test. These are preparation requirements, not a newly installed mode.

## Repeating A0

1. Both cameras open, settings unchanged; clear the swept frame area.
2. Disabled: explicitly seed from fresh trusted vision and confirm success.
3. Fresh log; legacy jitter collection off. Enable teleop, turn a small angle
   manually, release sticks and let it stop.
4. Click the right stick once; keep controls neutral during stationary aiming.
5. Record3s after completion, disable, finalize/rotate and download.
6. Repeat from the opposite side; inspect actual initial logged error rather
   than assume the approximate manual turn was exactly10degrees.

Existing outputs: Aim/TargetHeadingDegrees, HeadingErrorDegrees, Aimed,
DistanceMeters, GoalPose; Drive/Pose, GyroYawRateDegreesPerSecond, module
states/targets, CommandOwner; DriverStation enabled state and battery voltage.
No additional aiming signal or deployment is claimed by this report.
