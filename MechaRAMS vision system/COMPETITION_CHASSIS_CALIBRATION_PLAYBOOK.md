# Competition chassis calibration playbook

Updated October 3, 2026. This is a reusable commissioning procedure, not a claim
that the prototype's constants are ready for competition.

## What we are trying to learn

The prototype is a teaching and test platform. Success means that another student
can identify a problem, choose the right test, collect usable evidence, and know
which setting to change. It does not mean spending every class removing the last
small wiggle from a chassis we will replace.

The deliverables are a measured-hardware sheet, a repeatable set of tests, and a
report showing accuracy, repeatability, travel time, settling, and failure handling.
The previous [classroom baseline](CLASSROOM_TEST_CARD_2026-10-03.md) is complete.
Next use [the generic commissioning sequence](GENERIC_COMMISSIONING_NEXT_TESTS_2026-10-03.md),
starting with [relative start/point measurements](RELATIVE_LOCALIZATION_TEST_CARD.md).
The [six-axis camera-mount manual](CAMERA_MOUNT_CALIBRATION_MANUAL.md) and its offline
calculator are prepared for the later surveyed/new-chassis session. Wheel-only and
arbitrary-heading autonomous diagnostics remain tooling gaps. The relative notebook
needs the team to build/deploy before use; it commands no robot motion.

## What carries over, and what does not

| Keep as reusable software/process | Measure or validate again on the competition robot |
|---|---|
| Coordinate conventions, timestamps, logging, replay and diagnostics | CAN IDs/buses, motor directions, encoder zeros, module ordering and geometry |
| Current-start route generation and safe-start checks | Field layout, robot dimensions and obstacle clearances |
| PathPlanner for travel, final-pose controller for the final approach | Wheel radius, ratios, feedforward, motor gains, acceleration and braking limits |
| Continuous paths; spatial handoff after the last direction change | Handoff distance/speed appropriate to the new braking response |
| Final hold ownership and qualified finish/recovery logic | Translation/yaw tolerances and motion thresholds appropriate to the task |
| Camera IO, rejection reasons, timestamped fusion and static captures | Intrinsics, camera mounts, coverage, per-camera noise and fusion weights |

Even reusable code needs regression testing on new hardware. A nominally identical
camera can retain a saved intrinsic calibration only if the actual camera, lens,
focus, and image mode are unchanged and the calibration checks out. A new mounting
requires a new robot-to-camera transform.

Accuracy requirements are not tuning knobs. Decide what the task needs before
tuning. A relaxed transit heading and precise scoring heading can be different
modes; increasing a tolerance solely to hide an error is not calibration.

## Ordered calibration stages

Complete the measurements and basic sensing before tuning a controller to chase
those measurements. Camera and mechanical preparation can happen in parallel.

### 0. Establish a safe, traceable baseline

1. Record chassis/version, code commit actually deployed, battery identity, load,
   wheel condition, camera names/resolutions/focus condition, and tag-layout file.
2. Verify normal stop/disable, hardware communication, valid gyro rate, log creation,
   available disk space, and a downloaded log that includes the end of a test.
3. On the new chassis, explicitly test cold boot -> disabled readiness -> auto,
   WITHOUT teleop first. If it fails, record abort/preflight conditions rather than
   adding a hidden "drive teleop first" dependency. The prototype boot investigation
   is deferred, not declared solved by this document.
4. Check real robot-loop timing and sensor validity. An applied 250 Hz status setting
   is not proof that readings are fresh or valid, nor that the main loop runs at 250 Hz.

**Advance when:** a short, supervised low-speed test has a complete readable log,
correct sensor directions, and no unexplained reset/communication fault.

### 1. Measure geometry and verify signs

Record robot origin, frame/bumper dimensions, each wheel center X/Y, actual drive
and steer ratios, absolute encoder zero, motor inversion, gyro mounting, camera
optical-center X/Y/Z and mount angles, mass/load, and field tag poses/sizes.

Use +X forward, +Y left, +Z up; counterclockwise yaw is positive. With the usual
zero-roll WPILib robot-to-camera rotation, a camera tilted UP has negative pitch.
An inward-facing LEFT camera has negative yaw, the RIGHT has positive yaw.
These signs follow the [WPILib coordinate convention](https://docs.wpilib.org/en/stable/docs/software/basic-programming/coordinate-system.html).

First check one module at a time at low power with appropriate support and a clear
area. Command forward/left/rotation and verify direction and reported angle, not
just whether the robot moves. Check known heading changes independently on the
floor. Then verify loaded driving on the floor. A lifted robot does not reproduce
loaded traction or braking.

**If/then:** if module directions, yaw sign, or geometry are wrong, fix those first.
Do not compensate with a trajectory gain, camera yaw, or alliance setting.

### 2. Calibrate wheel distance independently of vision

**Dedicated diagnostic still needed:** the existing vision-seeded autos and
`Drive/Pose` are not an independent wheel-distance calibration tool. Covering both
cameras may prevent those autos from starting and does not create this diagnostic.

Prepare a low-speed diagnostic that logs raw module travel and wheel-only odometry,
with vision fusion explicitly excluded from the comparison. Keep normal safety
limits. Once available:

1. Mark exact start/end references. Drive measured straight distances in both
   directions; start with 1 m and 2 m if the clear area permits.
2. Repeat each condition at least three times. Check lateral drift and heading,
   not only final X. Longer distances improve scale measurement when space allows.
3. Compare ruler distance with wheel-only reported distance, not the commanded
   target or the vision-corrected pose.
4. If wheel-only distance is consistently low by a scale factor, increase effective
   radius by physical distance / reported distance; if high, decrease it. For
   example, 2.10 m physical versus 2.00 m wheel-only suggests radius x1.05, assuming
   ratios are correct and there is no significant slip. Repeat to verify.
5. If forward/reverse or different distances disagree, investigate slip, steering
   zero, mechanics and measurement before fitting one scale factor.

**Output:** measured effective radius, trial scatter, and proof the distance scale
is correct. SysId does not replace this independent geometric measurement.

### 3. Calibrate each camera, then its mount

Detailed operator steps, a measurement template, roll verification and an offline
rigid-transform calculator: [Camera mounting calibration](CAMERA_MOUNT_CALIBRATION_MANUAL.md).

1. Set and secure focus before intrinsic calibration. Use the intended processing
   resolution, not just the preview stream resolution. Repeat for each physical
   camera. PhotonVision says refocusing requires recalibration; its focus score is
   useful within the same scene/lighting, not as a universal cross-camera score.
   [Focus guidance](https://docs.photonvision.org/en/latest/docs/quick-start/camera-focusing.html).
2. Use a rigid, flat, correctly sized ChArUco board. Measure printed square/marker
   dimensions. Collect varied distances, tilts and image positions rather than
   many nearly identical pictures. Save calibration and its reported error.
   A low reprojection error alone does not establish field-position accuracy.
   [Calibration procedure](https://docs.photonvision.org/en/latest/docs/calibration/calibration.html).
3. Survey tag centers, orientations and size. Measure camera position from the
   same robot origin as the drivetrain. Confirm camera-name identity by covering
   one lens while stationary, then uncover both.
4. In this implementation, PhotonVision's MultiTag output is field-to-CAMERA;
   robot code applies robot-to-camera geometry to obtain robot pose. Do not apply
   a second robot offset to that data or confuse the two poses when comparing X.
5. At several independently measured robot poses, compare each camera's inferred
   ROBOT pose with physical truth. Include at least a near/far pair and more than
   one heading. Estimate mount-angle corrections from multiple known poses;
   do not assume one camera or the fused pose is ground truth for the other.

**If/then:** repeatable bias means inspect geometry/intrinsics/layout; scatter means
inspect image quality and noise. Do not change measured camera height to conceal
bad pitch, tag height or optical calibration. Recheck after mount/focus changes.

### 4. Measure localization noise and dynamic behavior

Use the existing disabled-only 100-sample camera captures at known stationary
poses. Record each camera's mean pose, X/Y/yaw scatter and camera-to-camera mean
separation. Repeat at several distances/headings and repeat captures at the same
pose. One short capture is not enough to fit a reliable weight model.

Then run slow, independently measured forward/lateral moves and returns. Compare
physical endpoints, individual camera estimates, wheel motion, and fused pose.
Keep frame timestamps in the proper clock domain. Current code drains unread
frames and uses the newest valid solve per camera/loop; do not introduce a stale
pose queue to process later.

**If/then:**

- If both cameras agree but disagree with the ruler, inspect shared tag layout,
  scale/reference errors and common calibration assumptions.
- If one camera is consistently biased, fix that cause before changing weights.
- If one is noisier across repeated poses, fit less trust to its measured noise.
- If fused pose moves while wheels and physical marks remain stationary, investigate
  localization. It is not automatically a physical overshoot or a need for more kD.
- If moving error grows with speed but stationary accuracy is good, inspect latency,
  timestamp alignment, motion blur and slip before retuning end control.
- If frames are rejected, inspect the reason and raw geometry; do not simply widen
  rejection gates until bad measurements get accepted.

**Output:** bias and noise versus distance/heading, usable camera coverage, and a
documented weighting policy. Enabled/disabled heading fusion differs in this code;
never treat those two headings as the same measurement condition.

### 5. Characterize drivetrain response, then tune motor control

Not today's classroom exercise: characterization can travel much farther than our
routes. Plan it in a sufficiently large clear area with a dedicated operator.

The current code has translation/steer/rotation SysId selectors, but selecting one
does not prove that the correct motor data is saved. Translation and steer use
voltage requests. The rotation routine maps the SysId numeric output to a requested
rotational rate: do NOT interpret that channel as physical motor voltage or blindly
copy fitted motor feedforward from it.

For voltage-based motor characterization, collect quasistatic forward/reverse and
dynamic forward/reverse, measured motor voltage, position and velocity, and state
labels. Keep each four-test characterization set in its own Phoenix SignalLogger
recording; confirm/export that data for SysId separately from our one-auto-per-log
rule. CTRE documents the data/recording workflow and warns about required travel
space. [CTRE SysId procedure](https://v6.docs.ctr-electronics.com/en/2025/docs/api-reference/wpilib-integration/sysid-integration/plumbing-and-running-sysid.html).

Fit feedforward in the actual actuator/sensor units. Then test modest velocity
steps and deceleration, comparing requested versus measured module speed and
steering. Tune motor control first, trajectory pose feedback second. Repeat with
representative load and battery conditions. Do not deliberately run into brownout.

**If/then:** persistent velocity error at constant speed suggests feedforward/load
or actuator limits; motor-speed oscillation suggests motor-loop/mechanical issues.
Chassis pose error alone cannot identify motor kP or kD. Increasing update frequency
without checking validity and CAN utilization is not a calibration method.

### 6. Validate travel, handoff and final placement as separate jobs

Use three types of route: translation without yaw change (H3-like), translation
with yaw change (H4-like), and out-and-return (H5-like). Also validate straight,
lateral and mirrored routes once the available field and tools support them.

1. Begin with conservative speed/acceleration. Record physical endpoints and full
   logs; at least three valid repetitions per candidate before calling it repeatable.
2. Examine the travel phase: target path versus actual motion, module tracking,
   cross-track error, angular rate, saturation and loop timing.
3. Examine the handoff: is the last turn complete, is the robot moving toward the
   final target, and do requested speed/direction jump when control changes?
4. Examine the final controller: separate position error, yaw error, actual motion,
   commanded motion, hold entry/release and finish qualification.
5. Examine AFTER completion: does the default command preserve the stopped wheel
   angles, or issue a new steering/drive request? Continue logging at least 3 s.
6. Change one subsystem of tuning at a time; re-run a no-yaw and return regression
   when changing rotating finishes. Prefer a rule based on motion/heading state,
   not a special case named "H4".

**If/then:**

- Smooth travel but a speed dip at handoff: inspect velocity initialization and
  incompatible constraints before raising every gain.
- Direction reversal immediately after handoff: inspect last-turn geometry and
  handoff alignment before blaming final kP.
- Real yaw oscillation only on rotating finishes: examine angular profile, measured
  gyro rate, feedback and damping. Select a controlled angular experiment; preserve
  the working translation/return behavior.
- Correct physical pose but repeated correction from shifting estimates: investigate
  localization and the finish/recovery policy before increasing feedback strength.
- Good pose but wheels move after command completion: inspect command ownership and
  angle hold. That is a software-transition problem, not necessarily PID.

The controller's tolerance means "eligible to finish if other requirements also
pass," not "never command a correction inside this distance." Actual speed, yaw,
continuous qualification, and recovery rules also matter. See the current
[functional IF/THEN guide](FUNCTIONAL_ALGORITHM_HANDOFFS.md).

### 7. Raise performance and validate realistic operation

Only after the preceding tests are understood: increase speed/acceleration in
controlled steps and repeat the same benchmark matrix. Use a larger field for
high-speed, long reverse, arbitrary headings and wide turns. Include normal load
changes, realistic battery variation, camera visibility transitions, interrupted
autos, restart, alliance/reference changes and a safe no-fresh-vision start rejection.

Add cameras if coverage measurements show unacceptable blind intervals for the
intended route/heading, not simply because two cameras have imperfect calibration.
Four cameras will not fix a shared layout error. Validate each added camera/Pi's
identity, calibration, latency, weighting and CPU/network cost.

After chassis/localization commissioning, move on to game-specific alignment,
chained actions and mechanism/aiming coordination. Reuse the same logs, physical
reference checks and regression routes when mechanism load changes.

## How to score a run fairly

Record these separately; one number cannot represent all of them:

- **Physical accuracy:** measured final center/reference displacement and heading.
  Label frame-corner readings as such. Rotated front-corner means are not center X;
  a single corner's Y is not center Y.
- **Estimated accuracy:** the controller's pose error at completion and the fused
  pose afterward. These are not independent ground truth.
- **Command time:** auto initialization to completion, including intended reversals.
- **Movement time:** first actual wheel/chassis motion to last motion, including
  coast after command completion. Report drive-wheel motion and steering separately.
- **Final settling:** from first arrival in the final translation window during the
  final approach until final wheel/steering rest. Include subsequent yaw corrections
  and departures; do not hide them by starting the clock only after yaw is correct.
  Exclude H5's intentional intermediate turnaround.

The desired final-settling share is under 10% of movement time, with command time
also reported. Use the same thresholds and time definition for before/after reports.
The existing audit uses 0.05 m/s module speed as a wheel-motion proxy, not proof of
absolutely zero movement. Inspect module angles and gyro too; sample interval limits
timing precision. State the physical-error requirement separately from these timing
targets. A fast bad endpoint is not a pass; a timeout is not a successful placement.

Theoretical time must use route length, planned speed/acceleration, turns and
required stops, not net displacement (H5 has zero net distance). Compare generated
path duration with actual travel and identify final-controller overhead separately.
A simple straight profile is only a lower-bound reference, not a universal 1.2 s goal.

Report individual runs plus median/range for small sets. Three good runs are a
screening check, not statistical proof of competition reliability.

## Existing data versus future test tools

**Already available:** full WPILOG recording; disabled rotation/finalization; per-
camera static capture; current-start holonomic autos; controller terms, target and
measured poses, gyro rate/status, module targets/states, holds/qualification,
ownership, loop interval, and roboRIO battery voltage. Exact classroom paths are
in the test card. AdvantageScope layouts display data; they do not decide what
the robot records. Today's sequence needs no extra logging.

**Before the full new-chassis commissioning program:** add/verify these tools;
they are NOT claimed to exist as finished dashboard actions today:

1. Deployed Git/build identifier and a run ID/configuration manifest in each log.
2. Explicit wheel-only distance diagnostic plus raw module positions/distances,
   independently checked units, and safe vision-fusion isolation.
3. Validated motor characterization capture/export and conservative velocity-step
   tests; confirm motor voltage/current/status channels rather than guessing paths.
4. Independent gyro heading/change logging for sensor calibration; a vision-fused
   `Drive/Pose` heading alone is not the reference for that test.
5. Reliable PDH configuration/validity if total robot/channel currents are needed.
   Recent zero PDH voltage is unusable. `/SystemStats/BatteryVoltage` already serves
   voltage analysis; roboRIO battery-current telemetry is not whole-robot PDH current.
   Do not allocate the same PDH twice through competing logger/WPILib owners.
6. A repeatable report combining parsed logs with a physical-measurement manifest.
   Existing `tools/audit_h4_history.py` parses archived runs, but does not certify
   physical accuracy or implement all commissioning stages automatically.

Do not increase logging volume indiscriminately on roboRIO 1. Add data for a
specific question, measure timing/queue impact, and use device-side logging where
appropriate. A faster roboRIO may improve timing headroom; it cannot fix transforms,
wheel scale, bad timestamps or incorrect control ownership.

## Record sheet for EVERY calibration change

Record: date, operator, chassis/load, deployed version, parameter before/after,
reason/hypothesis, one changed variable, test route, start marks, log filename,
battery change, physical X/Y/heading with reference and uncertainty, camera/focus
state, measured timing/error, pass/fail reason, and regression results.

Revisit wheel scale after wheel/ratio changes; geometry after frame/module changes;
intrinsics after focus/image-mode changes; extrinsics after remounting; response
tuning after mass/load/gearing changes; coverage/weights after camera changes.

**Prototype completion means the method is demonstrated and documented. Competition
completion means the new robot passes the method's measured acceptance tests.**
