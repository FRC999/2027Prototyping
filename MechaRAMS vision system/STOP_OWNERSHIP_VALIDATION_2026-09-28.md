# Stop-ownership retest: 3a29 / be40 / e73d / 14c2

## Conclusion

The mentor subsequently confirmed that the code had not been deployed and will
repeat the tests after deployment. Treat this batch as pre-fix results.

These files do not demonstrate deployment of the stop-ownership fix d06e5b4.
All four completely lack Drive/CommandOwner, Drive/PrecisionAngleHoldActive and
Drive/ManualDriveAllowed, including the raw WPILOG field names. These are unconditional
subsystem-periodic outputs in that revision; selecting fields in AdvantageScope is
not required to record them. All four also show the old post-completion steering-target
jump. The local built VisionTestingAndCalibration.jar likewise lacks all three strings
in DriveSubsystem.class. The logs have no Git SHA metadata, so the exact older deployed
revision cannot be identified.

**No H5-specific path, gain, speed, tolerance or target was changed in d06e5b4.**
That change applies to every precision completion: preserve the measured steering-angle
hold when the default joystick command resumes in autonomous or with neutral teleop sticks.
The present logs are consistent with the earlier behavior, not a regression caused by
that fix. No robot behavior is changed in this analysis revision.

## Timing and heading

All four report successful completion, FinishQualified=true and TimedOut=false.
Headings below are logged fused estimates, not independent physical measurements.
Positive yaw is counterclockwise from the normalized zero-degree starting direction.

| Test / log | Total command s | Final controller s | First hold to finish s | Hold releases | Yaw at finish | Yaw 1 s later |
|---|---:|---:|---:|---:|---:|---:|
| H4-1 / 3a29 | 3.358 | 0.980 | 0.385 | 0 | -19.80° | -19.37° |
| H4-2 / be40 | 3.791 | 1.458 | 0.844 | 1 | -20.49° | -20.71° |
| H3 / e73d | 3.580 | 1.079 | 0.567 | 1 | +1.22° | +1.14° |
| H5 / 14c2 | 7.152 | 1.113 | 0.286 | 0 | +1.05° | +2.37° |

Total includes route setup/alignment. Final-controller time includes useful approach
travel; it is not entirely jitter. The first-hold tail excludes earlier corrections
and post-completion motion, so it is not a complete visual-settling measurement.

H4-2 left its first hold at precision +0.902 s after heading error remained outside
the tight limit for the 200 ms requalification window (error about 2.02°). H3 similarly
released at +0.814 s with heading error about 1.65°. These explain extra corrections
before completion; they are separate from the later default-command disturbance.

H4 still selects strict finish; H3/H5 select the normal finish at their calm handoffs.
Logged H5 translation tolerance remains 0.02 m and yaw tolerance remains 1.5°.
Its target is the sampled starting X/Y with normalized 0° yaw.

## Why H5 was not straight after stopping

14c2's completion is at log time 501.864844 s:

| Relative to completion | Logged yaw | Gyro rate | Maximum absolute module speed |
|---|---:|---:|---:|
| 0.000 s | +1.046° | +0.801°/s | 0.002 m/s |
| +0.034 s | +1.057° | -0.105°/s | 0.001 m/s |
| +0.068 s | +1.066° | +1.961°/s | 0.393 m/s |
| +0.108 s | +1.463° | +18.414°/s | 0.232 m/s |
| +0.411 s | +2.492° | +0.152°/s | 0.011 m/s |
| +1.000 s | +2.372° | -0.172°/s | 0.000 m/s |

At +0.068 s the steering targets change by up to 67.65° from the captured hold.
The maximum wheel speed reaches 0.463 m/s at +0.088 s. This is the same signature
that motivated d06e5b4. Wheel-encoder speed can include steering/coupling effects;
the gyro independently confirms real renewed rotation.

The controller finished within its existing 1.5° estimated heading tolerance.
It does not keep correcting pose after completion, and a different subsequent steering
request disturbed that accepted stop. Do not tighten H5's yaw tolerance or change its
PID based on this run before deploying and testing the already-written transition fix.

Post-finish target changes also occur in 3a29 (69.26° at +40 ms), be40 (37.15° at
+54 ms), and e73d (31.53° at +41 ms). The smaller visible jitter is not proof that
the new hold-preservation code ran.

Post-analysis windows are capped at disable, the next auto, log end, or 3 s.
Actual available enabled windows: H4-1 3 s, H4-2 2.957 s, H3 1.252 s, H5 1.754 s.
The H3/H5 windows establish the initial post-finish effect but are shorter than
the requested three seconds.

## Next action: deployment check, then H5 first

1. Manually build and deploy the current source from
   C:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration.
   Do not merely resend the existing older JAR. Codex did not build or deploy.
2. While disabled, check live AdvantageScope under
   NT:/AdvantageKit/RealOutputs/Drive. CommandOwner, PrecisionAngleHoldActive and
   ManualDriveAllowed must all exist before moving. ManualDriveAllowed should be
   false while disabled/in autonomous; PrecisionAngleHoldActive may initially be false.
   If the fields are still absent, stop the retest and resolve the deployed project/build.
3. Use the existing layout:
   C:\MechaRAMS\temp\AdvantageScope 9-28-2026 - Auto Finish Ownership.json.
   No additional layout or logging fields are needed.
4. Run H5 once first, normal marked/squared starting position, both cameras open,
   jitter capture OFF, return route clear. Observe at least 3 s still enabled after
   completion, controls untouched and disable ready. Disable immediately for unsafe
   motion; otherwise disable after the observation and rotate the log.
5. Verify PrecisionAngleHoldActive stays true after successful completion and
   ManualDriveAllowed stays false. CommandOwner may switch to DriveManuallyCommand;
   the steering targets should not jump. Save the log and measure physical X/Y/yaw.
6. If that check passes, run H4 twice and H3 once with the same recording procedure.
   Do not change gains or routes between these comparisons.

The unchanged H5 finish allows some angular error (up to 1.5° in the estimate), not
mathematical zero. Assess any remaining physical-heading error after the post-finish
disturbance is removed. Independent yaw/X/Y measurements were not provided for this batch.

## Reproduce

Run tools/audit_h4_history.py with --all-modes --suffix 3a29 be40 e73d 14c2.
Derived data: C:\MechaRAMS\temp\stop-ownership-validation-2026-09-28.json.
The reader uses zero-order hold for changed-only entries and examines only enabled
autonomous post-windows. No robot build, tests, simulation, or deployment were performed.
