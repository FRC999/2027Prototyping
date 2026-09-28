# H4 history and trajectory calibration status — September 28, 2026

## Bottom line

We have a useful low-speed, front-camera trajectory baseline, not a finished calibration
for every trajectory or competition speed. H3 and H5 have recently become much more
consistent. H4 still has variable final turning/settling. The mentor is correct that
some earlier H4 runs were faster.

The historical audit also found a specific command-transition bug: **after the final
controller completed, the default joystick command undid its measured-wheel-angle hold.**
This can restart wheel motion after a successful stop. The current change fixes that
transition only; it does not retune the controllers or promise shorter pre-completion
settling.

## What the historical logs actually show

The audit scanned the 101 WPILOG files present in C:\MechaRAMS\temp and found 13 completed
DIAGONAL_WITH_YAW (H4) attempts. Times below come from recorded phases, not visual estimates.
All these attempts reported no precision timeout.

- Total: RESET_TO_TRUSTED_START through COMPLETE, including route setup/alignment.
- Final controller: last DriveToPose activation through COMPLETE. This includes useful
  approach travel as well as settling; it is not all wasted time.
- Hold tail: first zero-hold entry through COMPLETE, including any release/re-entry.
  It excludes earlier corrections and motion after completion. It is **not** a complete
  measurement of visible jitter.
- Yaw is the logged fused robot heading. It is not an independent physical measurement.

| H4 log | Total s | Final controller s | Hold tail s | Hold releases | Yaw at finish / 1 s later |
|---|---:|---:|---:|---:|---|
| 54ea | 7.754 | 0.102 | — | 0 | -19.85° / -19.84° |
| 6106 | 5.937 | 0.104 | — | 0 | -19.84° / -19.84° |
| a06b | 4.272 | 1.109 | 0.480 | 1 | -19.98° / -16.42° |
| 0e7b | 3.045 | 0.656 | 0.060 | 0 | -20.27° / -21.00° |
| 6ffd | 4.317 | 1.946 | 0.415 | 2 | -18.14° / -18.22° |
| 5a1b | 4.959 | 2.638 | 1.906 | 5 | -20.02° / -20.04° |
| 12b2 | 3.259 | 0.707 | 0.080 | 0 | -19.83° / -18.59° |
| 0b40 | 3.506 | 1.041 | 0.052 | 0 | -20.78° / -20.78° |
| 6e71 | 3.651 | 1.363 | 0.069 | 0 | -19.77° / -13.80° |
| a6ab | 4.887 | 2.404 | 1.423 | 2 | -18.84° / -18.79° |
| be88 | 3.541 | 1.171 | 0.552 | 0 | -19.20° / -19.09° |
| b2ab | 4.296 | 1.711 | 0.521 | 0 | -19.63° / -19.82° |
| b228 | 3.895 | 1.540 | 0.628 | 0 | -20.74° / -19.92° |

54ea/6106 used the earlier segmented route: PathPlanner had already nearly stopped
before the final controller. Their short final-controller times are not evidence of a
better continuous approach.

0e7b, 12b2 and especially 0b40 demonstrate genuinely faster finishes. 0b40's enabled
post-finish yaw changed by only about 0.09°. 0e7b and 12b2 changed by up to about
1.29° and 1.26° respectively, so their short command times did not mean perfect rest.
The same broad controller generation also produced slow 6ffd/5a1b runs.

The underlying final algorithm remained profiled position/heading feedback with
feedforward, velocity damping and a zero-hold stop. The continuous-path versions did
not alternate between fundamentally different final algorithms. Important later changes
were continuous finish qualification, hold recovery, measured steering-angle hold and
conditional stricter motion checks. A wholesale rollback would remove useful fixes too.

## The latest two runs: two different problems

| Component | b2ab | b228 |
|---|---:|---:|
| Setup/alignment before PathPlanner | 0.372 s | 0.164 s |
| PathPlanner driving | 2.213 s | 2.192 s |
| Final controller | 1.711 s | 1.540 s |
| First hold to command finish | 0.521 s | 0.628 s |
| Lowest logged battery voltage during auto | 10.71 V | 10.78 V |
| Largest final-controller loop interval | 76.7 ms | 128.2 ms |

**Before completion:** both handed off with about 16.6–16.8° of turning still to do
and approximately 43°/s of clockwise rotation. Both then showed angular overshoot
or reversal during the last approach. Neither released its final zero hold. The
last 0.52/0.63 s was spent waiting for completion checks, not repeatedly releasing
and re-entering the hold.

**After completion:** steering targets changed sharply and measured wheel motion
increased again, despite remaining in autonomous with zero drive-speed targets:

| Log | First >1° target change after finish | Largest wheel-angle change at that sample | Maximum measured wheel speed in enabled post-window |
|---|---:|---:|---:|
| 6e71 | 0.040 s | 68.37° | 0.327 m/s |
| b2ab | 0.042 s | 81.95° | 0.454 m/s |
| b228 | 0.148 s | 87.84° | 0.471 m/s |

These are wheel-encoder speeds; steering/coupling can contribute, so they are not
equal to robot-center translation. Gyro motion provides separate evidence that
the robot can rotate too. The post-window stops at disable, a new autonomous run,
log end, or 3 seconds.

### Why the targets changed

1. DriveToPose captured the actual steering angles and held zero drive velocity.
2. The auto finished and released the drivetrain.
3. The scheduler started its default command, DriveManuallyCommand.
4. That command sent a FieldCentric request even outside teleop and with zero joystick.
5. driveFieldRelative cleared the precision angle hold; native steering targets
   replaced the captured targets.

Default commands are not inherently teleop-only: they are scheduled when the
subsystem becomes free. See the [WPILib scheduler sequence](https://docs.wpilib.org/en/stable/docs/software/commandbased/command-scheduler.html#step-4-schedule-default-commands).

This corrects the earlier interpretation of 6e71: its approximately 6° post-finish
rotation was not adequately explained as coasting after an early finish. The
command transition is a concrete disturbance. The strict finish check alone cannot
prevent a later command from changing the wheel angles.

## Code change in this revision

- Outside enabled teleop, the default command calls drive.stop(), preserving any
  existing precision wheel-angle hold.
- During teleop with all sticks inside the deadband, it also preserves that hold.
- Actual joystick input resumes normal field-relative driving and clears the hold.
- A subsequent autonomous/driving command still takes ownership normally.
- Added three continuously logged fields: Drive/CommandOwner,
  Drive/PrecisionAngleHoldActive and Drive/ManualDriveAllowed.
  The last field means enabled teleop, not that the default command currently owns drive.

Unchanged: path shape, speed/acceleration, PID/feedforward/damping gains, camera
weights/transforms, finish tolerances, conditional strict finish, recovery and timeout.
This is a shared command-lifecycle fix, not an H4-specific branch. It requires
manual robot validation. No robot build, tests, simulation or deployment were run.

## Where calibration stands

| Area | Status | What is still needed |
|---|---|---|
| Camera intrinsics/tag layout/mount geometry | Working front-facing baseline | Independent accuracy across distance and heading; low noise does not prove correct absolute geometry |
| Forward distance scale | Strong local evidence | 31ea/62ff: camera X separation 1.0001/1.0039 m, fused 1.0020 m for a measured 1 m; repeat in broader conditions before another scale change |
| Lateral localization | Not independently closed | fef2/e3b0 camera Y changes differed: 0.7506 vs 0.7099 m; precise physical displacement was not supplied |
| Relative starting pose and continuous paths | Implemented and exercised | Preserve regression tests; arbitrary starts must still satisfy clearance and tag visibility |
| PathPlanner to final-controller handoff | Functional, follows final straight segment | Validate incoming speed and heading on more route shapes, especially late rotation |
| H3/H5 endpoint behavior | Promising recent tests | Repeatable physical X/Y/yaw measurements; not just fused error or one good run |
| H4 late-rotation endpoint | Still being tuned | Validate stop ownership first; then reduce pre-hold angular ringing without weakening accuracy |
| Drive/steer motor response | Working, not exhaustively characterized | Diagnose requested versus measured steering/drive response if ringing remains; do not guess new CTRE gains |
| Timing on roboRIO 1 | Still has loop outliers | Profile whether repeatable stalls coincide with control trouble; a large maximum alone does not prove cause |
| Full-speed/general-field operation | Not validated | More clearance, speed ladder, varied battery/heading/route coverage |
| Auto directly after boot | Open, deliberately deferred | Eventually verify without requiring teleop first |

We should not claim that all remaining error is PID tuning. A controller can finish
correctly in its estimated coordinate system while the physical robot is several
centimeters away. Nor should we declare every final-controller second to be jitter.

The requested settling-under-10%-of-total goal is not yet reliably demonstrated
for H4. Its latest first-hold tails alone were about 12% and 16% of total time,
before adding any post-command disturbance. That metric misses pre-hold jitter too.

## Next test — isolate the stop-ownership fix

1. Manually build and deploy this revision. Load the new layout:
   C:\MechaRAMS\temp\AdvantageScope 9-28-2026 - Auto Finish Ownership.json.
   The previous layout is preserved. New fields do not exist in older robot binaries/logs.
2. Use the usual safe marked start, squared to the tag board, both cameras open,
   and camera-jitter capture OFF. Clear the entire route, including H5's return.
   The paths use the sampled starting position; matching floor marks is for comparison,
   not a requirement to drive to an old fixed start.
3. Run H4 twice, H3 once, H5 once, with one finalized log per run.
   Targets relative to the accepted start: H4 +2.25 m X / +0.75 m Y / -20°;
   H3 +2.25 m X / +0.75 m Y / 0°; H5 0 / 0 / 0°.
4. After each command completes, leave the robot enabled and controls untouched
   for at least 3 seconds, supervised with Driver Station disable ready.
   Disable immediately if unexpected motion is unsafe. Then disable and rotate the log.
5. Measure final left/right front-corner X, a clearly identified Y reference,
   and independent yaw after the wheels stop. For H4, measure a robot-center reference
   if possible: rotated front-corner X values alone do not equal center displacement.
   Record the reference points used; do not move the robot before measuring.
6. In the layout, verify that after a successful finish:
   - Drive/PrecisionAngleHoldActive stays true.
   - Drive/ManualDriveAllowed stays false while autonomous is enabled.
   - CommandOwner may legitimately change to DriveManuallyCommand.
   - ModuleTargets steering angles do not jump; measured wheel speeds and gyro settle.
   - TimedOut is false. H4 normally selects strict finish; the logged calm H3/H5
     handoffs normally do not.
7. Separately verify normal teleop: small deliberate input moves the robot,
   releasing the stick stops it. Keep this out of the auto's 3-second post-window.

The layout plots continuously updated Drive gyro telemetry. DriveToPose's last
measured gyro value stops updating when that command ends and cannot diagnose later motion.
The layout displays data; the robot's WPILOG writer records it independently.

### What follows that test

**First, close H4's angular approach.** If the post-finish target jump disappears
but pre-finish yaw ringing remains, examine target versus actual steering/gyro
response. A candidate generic improvement is to schedule most turning earlier
in PathPlanner so the last straight approach begins closer to final heading.
Any such change should use final-heading/turning needs, not the string "H4".
Keep precision control only on the last part of the entire route. Do not combine
that experiment with the stop-ownership fix.

**Second, validate accuracy and repeatability.** Freeze a candidate revision and
run at least three measured H3/H4/H5 attempts plus forward/lateral reference moves.
Measure X, Y and yaw, save individual logs, and report typical and worst errors,
timeouts, command time and wheel/gyro rest after completion. Use independent
physical measurements to separate localization error from controller error.
This is where a precise lateral reference and stationary camera comparisons help.
Use jitter capture only for a separate, stationary capture, not while driving.

**Third, increase speed in controlled steps.** Once endpoint behavior is repeatable,
increase one motion limit at a time and recheck braking/steering tracking and final
accuracy. Full-speed runs need a larger, clear area with braking margin. Do not
raise all limits in the current restricted corner.

**Fourth, broaden coverage.** Test other headings, opposite-direction strafes,
curves and multi-stop sequences, then camera occlusion and direct-from-boot auto.
Add rear/side-facing cameras when those routes leave the two front cameras without
reliable tags. More cameras will not fix this command-transition bug, and are not
needed for the immediate four-run retest.

## Reproducibility and limitations

Reader: tools/audit_h4_history.py (standard Python, reads WPILOG directly).
Derived report: C:\MechaRAMS\temp\h4-history-audit-2026-09-28.json.
Changed-only signals are sampled with zero-order hold; telemetry sources may have
one-loop timestamp skew. Post-finish windows exclude disabled time. No physical
ground truth is inferred from the camera/fused estimates. Older settings are not
randomized controlled experiments, and the same version can perform differently
with battery, starting state, steering angles and scheduler timing.

Standing safety: final command timeout is not success; disable for unsafe motion.
Do not add wheel toe-in/pizza braking, new camera transforms or guessed PID gains
during this isolated regression test.
