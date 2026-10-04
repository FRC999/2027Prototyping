# Next prototype work: prove the commissioning process

October 3, 2026. This replaces the earlier recommendation to make another H5
controller adjustment immediately. The objective is transferable calibration,
not perfect motion from this particular chassis.

## Decision

**Latest mentor clarification:** absolute mount calibration is deferred to the new
chassis/surveyed setup. The immediate experiment is the
[relative localization test card](RELATIVE_LOCALIZATION_TEST_CARD.md): a button
records the start; manually reposition, capture numbered points, and compare
rear-frame corner displacements. This needs the new diagnostic code deployed by
the team, not a controller retune. It supersedes the original next-class steps
below; those surveyed mount steps remain a future procedure.

Continue using this robot while the next robot's mounts are being built. Keep
the current controller baseline. We have exercised forward motion, forward-to-
strafe transitions, diagonals, motion with heading change, return/reversal,
spatial handoff after the last turn, and post-command hold. Do not repeat all
those routes indefinitely just to improve a few centimeters or one wiggle.

We have NOT demonstrated universal accuracy or a reliable <10% settling result
in every run. Keep H5 log 7461 as the known late-yaw recovery regression, alongside
good-stop cases 78cd and H4 deb8. A reusable process should detect that difference.

The next deliverables are: a checked camera-mount worksheet/calculator, independent
wheel/gyro calibration evidence, general trajectory templates, and an automated
report that separates localization error from controller behavior.

## Ordered work

| Order | Test/tool | What we learn | Available now? |
|---|---|---|---|
| 1 | Relative multi-pose localization check | Do cameras/fused estimates report the actual change from a recorded start? | New disabled-only dashboard tool implemented; team must build/deploy |
| 2 | Wheel-only distance and independent heading diagnostic | Can we calibrate wheel scale/geometry without vision hiding errors? | Needs diagnostic logging/tooling; not the existing vision-seeded autos |
| 3 | Generic route and handoff matrix | Do the same rules work in both directions and at different start headings? | Existing H routes cover part; new generic templates needed |
| 4 | Controlled speed, braking and load comparison | Can we choose constraints/gains systematically rather than per route? | Current telemetry useful; validated actuator characterization/step tools still needed |
| 5 | Startup, camera coverage and recovery | Can we tell readiness/failure from poor calibration? | Some existing readiness logs; automated qualification incomplete |

No extra camera is required for steps 1-2. Add cameras when measured coverage
shows that the intended routes cannot reliably see tags, not to mask a bad mount.

## Deferred absolute mount-calibration session

Follow [Camera mounting calibration](CAMERA_MOUNT_CALIBRATION_MANUAL.md).

1. Keep both cameras open, current constants unchanged, and robot disabled for
   recording. Do not run an autonomous route from a turned starting pose.
2. Check the tag board and robot reference plane with a level. Record robot-origin
   geometry and camera X/Y/Z from that origin. Confirm physical camera identities.
3. Establish a square pose A and a square pose B about 0.75-1 m apart along the
   known clear X corridor. Measure their robot-center locations independently.
   Start with these two if class time is limited; label the result preliminary.
4. At each pose, capture each camera's six-number **Multi-tag pose, field-to-camera**
   row at least five times over 5-10 s. Include name/position labels. Optional
   existing 100-sample jitter captures provide extra X/Y/yaw noise evidence.
5. Complete a surveyed small-turn pose C and an unused validation pose D, only
   where the swept frame is clear and both tags remain visible. Otherwise defer
   these poses rather than inventing coordinates or driving outside the clear area.
6. Send the measurements/captures and logs. Use the calculator to compare inferred
   mounts, measured XYZ, roll=0 versus measured roll, and unused-pose errors.

For a final accuracy-qualified result, the refocused left camera needs its intrinsic
calibration repeated first. Without that, we can still practice the procedure and
expose inconsistency, but should not fit compensating mount angles as final values.
This is a calibration-method experiment, not another H5 repeat.

## Step 2: independent drivetrain measurement

Prepare a safe wheel-only diagnostic and logging of raw module travel, steering
angles, and independent gyro heading/change. Keep the normal fused estimate logged
alongside it, but exclude camera corrections from the calibration reference.
Covering cameras is not a substitute: current autos may reject a no-vision start.

Once prepared, demonstrate measured straight travel at two distances in both
directions; repeat enough to see measurement scatter. Also check a measured
lateral displacement and a small known rotation when the clear area permits.
On the competition robot use at least three valid repetitions per condition.

- A consistent forward/reverse scale error points to effective radius/units.
- Direction-dependent errors point toward steering zeros, slip or mechanics.
- Incorrect yaw for known motion points toward gyro reference/module geometry.
- Correct wheel-only scale but shifting fused endpoints points back to localization.

These few ruler measurements must be careful enough to calibrate a scale. Routine
trajectory rear-corner measurements can remain rough sanity checks as requested.

## Step 3: test families, not named-route patches

Keep PathPlanner responsible for the continuous route, including intermediate
turns. Use the final controller only on the last aligned approach to the final
destination. H5's required reversal is an intentional slowdown, not final settling.

New reusable templates should support:

1. Straight and lateral translations in both signs, with unchanged yaw.
2. Diagonals in both lateral directions.
3. Translation while yaw changes clockwise AND counterclockwise.
4. Out-and-return with the same start/end pose, plus a chained route ending elsewhere.
5. The same path-relative motion starting at a different actual robot heading.
6. A final approach that starts with little yaw error versus one with active yaw
   motion, while retaining the same generic finish/recovery policy.

Do not run all combinations immediately. Start with one representative of each
previously missing behavior, then repeat the cases that reveal a problem. Compare
against the existing no-yaw/moving-yaw/return regression set after algorithm changes.
There is no need to validate every alternate trajectory library or experimental
fusion model before the selected baseline can be transferred to another chassis.

**Implementation gap:** current holonomic routes use fresh start X/Y but normalize
yaw to zero and use board-aligned axes. They are not arbitrary-start-heading,
robot-relative templates. Add proper start-pose transforms and safe endpoint/route
previews before trying item 5; do not merely turn the robot and run today's H3.

**Space constraint:** our left-side room starts only after about 1.5 m of forward
travel. Mirrored right paths, large spins and faster motion are not authorized by
the existing space description. Use the known forward-entry area for small tests;
larger/symmetric tests need verified space. No new chooser entries are installed
by this document.

## Step 4: useful speed experiments

After sensing is checked, run one identical geometric route at conservative and
moderately increased constraints. Compare planned travel, actual module tracking,
handoff speed/direction, stopping distance, final error and settling. Do not change
the speed, camera transform and PID together.

Use a larger clear area for high-speed characterization. Validate the SysId motor
voltage/position/velocity recordings and units before fitting feedforward; the
existing rotation SysId selector requests angular rate, not physical motor voltage.
Recalibrate this layer on the new chassis with representative mass, wheels and load.

Do not introduce deliberate wheel scrubbing/pizza braking in this commissioning
sequence. First establish ordinary motor braking and predictable steering; the
extra skid would make localization and repeatable distance harder to diagnose.

## Step 5: operation and added cameras

Include one cold-boot -> disabled readiness -> auto check WITHOUT teleop first
once ready to investigate the previously reported startup issue. If readiness or
starting fails, preserve the boot log before using teleop or rebooting again. Do
not claim this historical issue is solved without a cold-boot demonstration.

Check one-camera loss/recovery while disabled first, then define a supervised
low-speed motion test with an explicit safe fallback. Do not assume current
no-vision autos are permitted to start, or disconnect wiring during a moving run.
Map fresh-tag coverage over intended headings before choosing more camera positions.

For additional cameras the core fusion architecture can stay. Update roster,
measured transforms, trust settings and time/health checks. The new relative tool
supports selected cameras and 3D samples; the OLD jitter tool still has a two-camera
completion rule. Benchmark a third stream on one Pi; two cameras per Pi
for four cameras is a starting architecture, not a guaranteed FPS specification.

## What to save / when to stop prototype development

The report should identify deployed code, hardware/calibration versions, test
parameters and exact log. Compare per-camera pose, fused pose, wheel-only/gyro
references when available, requested/measured motion, handoff, finish reason,
post-finish motion, battery and timing. Keep physical corner references explicit.
For moving runs record at least 3 s after completion before disabling when safe;
one run per finalized WPILOG remains convenient. Static calibration may use one
clearly labeled log for multiple stationary positions.

Use the existing AdvantageScope layout for currently logged controller metrics:
`C:\MechaRAMS\temp\AdvantageScope 9-28-2026 - Auto Finish Ownership.json`.
It is a viewer configuration, not a recording switch or a full six-axis mount tool.
New diagnostic tools will need a separately named layout with verified paths.

The new relative-check layout is
`C:\MechaRAMS\temp\AdvantageScope 10-03-2026 - Relative Localization Check.json`;
its source paths are verified against the new diagnostic code, not a deployed log
(the team has not yet built/deployed this change).

We are ready to transfer the process when students can independently:

- Fill out geometry and camera sheets and validate them at unused reference poses.
- Separate wheel scale, camera bias/noise, motor response and controller errors.
- Use a small representative trajectory matrix and obtain a complete timing report.
- Diagnose readiness/coverage faults without blindly changing PID or tolerances.
- Repeat those steps on the next chassis using its own constants.

That does not require every prototype run to have zero jitter. Preserve unresolved
cases and their logs as diagnostic examples; do not present them as passed tests.
