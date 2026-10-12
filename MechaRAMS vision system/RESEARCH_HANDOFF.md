# Continue the vision and driving research on another computer

Updated October 12, 2026. This is the current entry point for the research and
prototype commissioning work. It preserves conclusions and uncertainties without
requiring this chat or the original PC. Robot source remains authoritative for
implemented behavior; a recommendation is not a deployed change.

**Latest continuation:** [BLine laptop handoff](BLINE_LAPTOP_HANDOFF.md) includes
the implemented optional modes, verification state, checkout/build steps and
next-assistant message. [BLine validation](BLINE_VALIDATION_TEST_PLAN.md) is the
ordered desktop/physical comparison plan. Pull main; no manual code/doc copying.

The original laptop subsequently completed local verification and automated
configured CTRE/PV runtime screening:25 normal qualified stops plus rejection/
interruption checks. [Measured results and limits](BLINE_DESKTOP_SCREENING_2026-10-12.md)
include cases missing the settling goal and the simulator's estimator-fed camera
limitation. GUI inspection, physical qualification and full-physics recovery
remain pending; source behavior was not changed by this verification.

## Where the knowledge lives

Both parts are tracked in **FRC999/2027Prototyping**:

- `VisionTestingAndCalibration/`: robot code exercised on the 2025 prototype
  chassis; [its README](../VisionTestingAndCalibration/README.md) links back here.
- `MechaRAMS vision system/`: algorithms, research, calibration procedures,
  decisions, evidence reports, offline analysis tools and portable viewer layouts.

The source project is named VisionTestingAndCalibration, even when the team refers
to the C2025 chassis/code. This handoff does not modify a separate competition
repository. Reuse the method on the next chassis; remeasure its constants.

On the other PC, clone https://github.com/FRC999/2027Prototyping or pull `main` in
an existing clean checkout. Read this file, the playbook and session state before
making new changes. Preserve that PC's local work when updating. The documentation
and tools use repository-relative links; the checkout need not be `C:\MechaRAMS`.

## Read these in order

| Purpose | Maintained record |
|---|---|
| Continue the newly implemented optional BLine experiment | [BLine laptop handoff](BLINE_LAPTOP_HANDOFF.md), [validation plan](BLINE_VALIDATION_TEST_PLAN.md) |
| What to calibrate, test order, data and acceptance criteria | [Competition chassis calibration playbook](COMPETITION_CHASSIS_CALIBRATION_PLAYBOOK.md) |
| Plain-English IF/THEN control and handoff rules | [Functional algorithm handoffs](FUNCTIONAL_ALGORITHM_HANDOFFS.md) |
| Driving decisions and selectable explanations | [Driving map](ALGORITHM_DECISION_MAP.md), [interactive HTML](algorithm-decision-map.html) |
| Pose construction, gates, clocks, fusion and diagnostics | [Localization map](LOCALIZATION_DECISION_MAP.md), [interactive HTML](localization-decision-map.html) |
| Relative start/point measurements and their results | [Relative localization test card](RELATIVE_LOCALIZATION_TEST_CARD.md) |
| Camera X/Y/Z and roll/pitch/yaw on another chassis | [Camera mounting calibration manual](CAMERA_MOUNT_CALIBRATION_MANUAL.md), [offline calculator](tools/calculate_camera_mount.py) |
| Trajectory experiments and remaining qualification | [September 28 H4 history](H4_HISTORY_AND_CALIBRATION_STATUS_2026-09-28.md), [October 3 H5](H5_CLASSROOM_BASELINE_2026-10-03.md), [October 3 H4](H4_DEB8_CLASSROOM_BASELINE_2026-10-03.md) |
| Most recent aiming experiment | [A0 aiming baseline](AIMING_BASELINE_2026-10-05.md) |
| Detailed chronology and mentor requests | [Session state](SESSION_STATE.md), [prompt record](AI_PROMPTS.md) |
| Missing tools and future tests | [Generic commissioning next tests](GENERIC_COMMISSIONING_NEXT_TESTS_2026-10-03.md) |

Historical reviews and strategy documents describe their own dates. For example,
early gap analyses say aiming or simulation is absent; those are not current
implementation claims. Consult the current source and maintained guides first.

## What we learned about trajectory algorithms

1. **Separate planning, localization and actuator control.** A controller reaching
   its estimated target does not establish physical accuracy. If wheel motion is
   over but the estimated pose changes, investigate sensing before raising PID.
2. **Generate from the actual accepted start.** An old fixed start can send the
   robot backward toward that coordinate. Today's H routes use fresh start X/Y,
   but normalize yaw to zero and use board-aligned axes; they are not arbitrary
   robot-heading-relative templates. Check the route/clearance before enabling.
3. **Keep intermediate corners in a continuous PathPlanner path.** Multiple
   stop-and-start segments caused visible pauses. Final pose control belongs to
   the last approach to the entire destination. H5 needs an intentional far-end
   stop/reversal; that is not final settling at the returned start.
4. **Handoff depends on state and geometry.** Finish the last direction change,
   approach the final straight, and check direction/cross-track alignment before
   handing over. A universal 80% point is not implemented or appropriate for all
   routes. Seed the final profile from measured velocity to avoid a speed dip.
5. **A stop has several checks.** Position, yaw, chassis/wheel speed, continuous
   finish qualification and hold-release/recovery are different decisions.
   Crossing a 2-4 cm position boundary once does not guarantee immediate success.
6. **Preserve wheel angles across completion.** Hold measured steering angles at
   zero drive; the default command must retain that hold when sticks are neutral
   or teleop is not enabled. Post-command steering changes can restart motion even
   after a valid stop. Inspect command ownership, not only controller gains.
7. **Use generic conditions for rotating finishes.** The stricter stop gate is
   selected from remaining heading error/angular motion when precision begins,
   rather than a route called H4. Recheck H3 and H5 after any rotating-finish change.
8. **Actuator behavior matters.** Closed-loop wheel velocity helped precision
   travel/braking. Do not assume every command uses it: current aiming still uses
   the generic open-loop requests. Stationary aiming's switch is only proposed.

Details and exact current thresholds are in the functional guide and source.
Prototype gains, wheel radius, mount angles, speed limits and geometry are examples,
not constants to carry unmeasured onto a competition chassis. The historical 5%
distance discrepancy motivated scale investigation; vision-fused trajectory
endpoints do not independently establish encoder scale.

## What we learned about localization and calibration

- Drain unread results and keep the newest solvable candidate per camera/burst;
  never carry a growing stale-pose FIFO into later loops. Convert PhotonVision's
  FPGA timestamps with `Utils.fpgaToCurrentTime` before CTRE fusion.
- Single-tag ambiguity, impossible Z and other rejection reasons must be examined
  before loosening gates. Detecting two individual tags does not alone prove a
  MultiTag solve is enabled or being consumed. Single-tag heading is not trusted.
- Lens calibration and mounting calibration are separate. Set/secure focus first,
  calibrate the individual camera at its actual processing image mode, then survey
  its mount. Small focus changes still invalidate previous intrinsic calibration.
- Raw PV MultiTag is field-to-camera; robot code applies the inverse configured
  robot-to-camera transform. Camera-center rows are not robot-center coordinates.
- Smooth, low-scatter estimates can be biased. Compare individual cameras, fused
  pose, repeat captures, near/far positions and a return to the original marks.
  Do not calibrate one camera by simply declaring the other or fused pose correct.
- Use +X forward, +Y left, +Z up; positive yaw is counterclockwise. For the usual
  forward-facing zero-roll mount, tilt up means negative pitch; left toe-in means
  negative yaw, right toe-in positive yaw. Roll=0 is a hypothesis to verify.
- A small error in the estimated initial heading can give good forward distance
  but wrong sideways displacement: 3 degrees over 2 m is about 10.5 cm lateral
  and only 2.7 mm forward projection error. This illustrates sensitivity; it is
  not a measured camera-angle correction.
- Constant XYZ mount error mostly cancels in unchanged-heading relative moves;
  turns expose lever-arm errors. A common absolute translation of the entire tag
  layout likewise mostly cancels. Wrong tag spacing, relative depth/orientation,
  lens calibration or a warped tag can distort the solve with distance and view.
- All recent manual references are **rear FRAME corners**, with the rear bumper
  removed. Compare the same corner in fixed ORIGINAL floor axes, not the rotated
  robot axes. Two X displacements plus one explicitly identified corner Y can
  infer center/yaw under the documented rectangular-frame/small-turn assumptions.
  Use actual new-chassis geometry and record measurement uncertainty.

### Recent evidence and its limits

The October 5 sequence was captured while disabled with 100 fresh samples per
selected camera and zero normal geometry-gate rejections. In `29d0`, `6541` and
`612b`, X was relatively close while Y discrepancy grew. `e102` ended during a
capture; `f917` completed it. Old retained values in rotated logs are not a new
measurement. The test card holds the full tables and file identities.

At `612b`, original-axis rear X was 2.022 m left and 1.960 m right. The updated
measurement was RIGHT rear Y +0.005 m (positive sign assumed). Camera-derived
RIGHT rear Y was -0.1309 m left camera, -0.0891 m right camera, -0.1415 m fused:
approximately 13.6/9.4/14.6 cm disagreement with that rough physical check.
Geometry accounts for the robot's rotation. Do not convert this into a fitted
mount correction or treat uncertain tape readings as an exact survey.

For a long stationary `f917` window, left mean Y changed about -1.81 cm and right
about +0.66 cm between early/late windows, assuming nothing was disturbed. This
does not prove mount movement or thermal drift; some intermediate windows had
low MultiTag throughput. Short capture spread is not the entire error budget.

The later board inspection found tag1 center **1.49 m**, tag2 **1.50 m**, center
spacing **0.50 m**, black squares approximately **0.165 m**, and a hump near tag1
in a flexible whiteboard. The actual PV **Settings / AprilTag Field Layout**
screenshot matched the robot layout: tag1 (6,2.25,1.5), tag2 (6,1.75,1.5), both
yaw180 degrees. Earlier dashboard camera-pose screenshots were incorrectly
interpreted as a layout mismatch; that conclusion is superseded.

Thus the physical tag1 height and surface geometry differ from the nominal model.
Their contribution is plausible, not quantified; a 1 cm height error alone is not
proven to explain all Y disagreement. A flexible board limits physical-accuracy
qualification. Controller-response testing can continue, but do not tune mounting
angles to compensate for unknown board deformation. Establish a new start
reference after changing board geometry, layout, focus/calibration or reference
axes. No board correction or completed recalibration has been reported.

## Latest aiming status: implemented versus recommended

Current `AimConstants`: virtual target (7.5,2.0) m, front face, kP5/kD0,
1.5-degree heading tolerance held 0.2 s, 3-second safety timeout. Stationary aim
uses generic open-loop robot-relative motor control. Moving aim uses generic
field-relative control and modeled shooting lead (`SHOOT_ON_MOVE_ENABLED=true`).
The point is 1.5 m beyond the board; current moving aim is not the proposed
board-midpoint fixed-point tracking test. Right-stick click runs stationary aim;
right-trigger hold runs drive-and-aim. See the controls guide for other bindings.

`dc76` was smooth but ended at the 3-second timeout ~2.2 degrees off its estimated
target. `dd6b` overshot more but finished successfully in ~1.29 s ~1.1 degrees off.
There was no visible sustained jitter in either. Initial estimated errors were
6.0 and 13.5 degrees, so this is not an equal-angle direction comparison.

**Recommended, NOT implemented:** change only stationary aim to the existing
closed-loop wheel-speed request first, keeping target/gains/tolerance the same;
repeat A0 before judging its effect. Small open-loop commands against friction
are a plausible cause of the first run's stalled correction, not a proven sole
cause. Do not silently describe this as deployed. A later fixed-point moving
test needs a deliberate target, shooting lead disabled and conservative limits.

On October 10 the mentor planned PV recalibration, at least for the left camera.
Focus instructions: Camera page -> select camera -> Focus Mode; use a detailed
stationary scene at a representative distance; maximize score under unchanged
lighting/scene; secure lens; exit focus mode; recalibrate ChArUco at the pipeline
resolution (last screenshots:800x600). No universal focus score applies.
Official [focus guidance](https://docs.photonvision.org/en/latest/docs/quick-start/camera-focusing.html).
Planning to recalibrate does not establish that it was completed.

## Remaining work and acceptance

October 12 subsequent research: [BLine maturity evaluation](BLINE_MATURITY_EVALUATION_2026-10-12.md)
reviews 2638's published library and 2056/2910 position-based control. Optional
BLine main travel with our existing precision finish was subsequently added as
four optional one-way chooser entries. Local compile/tests/coverage pass; GUI,
physical qualification and deployment remain pending. See the
[validation plan](BLINE_VALIDATION_TEST_PLAN.md). Keep optical/reference qualification and the
known H5 late-yaw regression independent of follower selection.

The prototype has demonstrated the route/handoff/capture procedures, not universal
physical precision or a <10% settling guarantee. 1.274 s was accepted for the
earlier 1 m drive; the broader goal is final settling under 10% of movement time,
with command duration, finish reason and physical error reported separately.
Keep known late-yaw cases such as H5 `7461` as regressions, not discarded outliers.

Next layers are optical/reference validation, the isolated aiming experiment,
independent wheel-only distance/gyro calibration tooling, validated actuator
characterization, a generic route matrix and coverage/startup/recovery checks.
Cold boot without prior teleop was problematic historically and remains deferred.
Full arbitrary-heading paths, high-speed qualification, universal braking-distance
handoff and game-specific alignment are not demonstrated by today's H routes.

Additional cameras use the same core architecture, but each needs its own name,
intrinsics, mount, trust/coverage/timing verification and measured Pi budget.
The selected-camera relative capture supports a roster; legacy jitter completion
still assumes the two front cameras. More cameras cannot repair a shared bad layout.

## Bring the evidence and viewing tools with you

Portable AdvantageScope layouts are versioned in [advantage-scope-layouts](advantage-scope-layouts/README.md).
Import the relevant live or saved-log tabs. Their viewer selection does not enable
recording: AdvantageKit on the robot records the configured signals independently.
Download/finalize and verify the actual end of each log; pause is only a viewer
action. A missing endpoint, timeout, or inherited capture is not a successful test.

Repository reports preserve results and original filenames. Raw WPILOGs still
live outside Git in `C:\MechaRAMS\temp`; Git clone alone does not copy them. The
[log inventory](evidence/local-log-inventory-2026-10-12.json) lists local files at
this handoff. Copy desired raw logs separately to the other PC to reanalyze them;
there is no requirement to reproduce the same absolute folder path. Invoke the
offline audit with your actual log folder. Keep archived code, PV calibration
exports and physical-measurement notes together when reproducing a run.

After every new experiment, record actual deployed version, hardware/calibration
state, exact log identity, coordinate/corner references, uncertainty, battery,
changed variable, outcome and regression result. Update the relevant evidence
report and these maintained guides, commit, and push. A chat recommendation alone
is neither a durable record nor an implementation.
