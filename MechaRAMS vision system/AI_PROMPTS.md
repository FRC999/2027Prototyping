# AI Prompt Log

This file records mentor prompts that affect the design process so students can study the AI-aided workflow.

## 2026-06-29 Initial Project Prompt

The initial full prompt was provided in:

`S:\MechaRAMS\Vision\MechaRAMS vision system\prompt.docx`

Reorganized meaning-preserving summary:

- Team 999 is building a 2027 vision/localization/trajectory prototype using the 2025 robot chassis.
- Final Java project goes in `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration`.
- Documentation, prompts, status, and strategy files go in `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system`.
- Review prior research documents and incorporate strong ideas from public 2025/2026 FRC teams and process templates.
- Prefer truth over guessing. Stop and ask if something is ambiguous.
- Use the 2026 robot code style where useful, but use better current CTR/Phoenix patterns if appropriate.
- Use 2025 robot code for CAN IDs and relevant chassis constants.
- Build for one Xbox controller.
- Include drivetrain characterization routines and click-by-click instructions if characterization is needed.
- Support simulation and AdvantageScope/PhotonVision simulation when practical.
- Create AI skills/configuration/session-state files so Claude and Codex can continue work efficiently.
- Create a detailed camera/tag/trajectory test plan.
- Keep documentation in markdown when possible.
- Record this and future prompts for student analysis.
- Document final robot controls.

## 2026-06-30 Clarifications

Mentor answers:

- Use PhotonVision.
- Use one Orange Pi and two USB2 cameras.
- Cameras are Arducam 100fps global-shutter color USB camera boards, 1MP OV9782 UVC, low-distortion M12 lens.
- Use 2025 CAN bus name, motor CAN IDs, and Pigeon ID because the code runs on the 2025 chassis.
- The 2025 chassis uses SDS MK4 L3 modules.
- Use 6.5 inch AprilTags and prepare a field layout.
- Use AdvantageKit.
- Overwrite the empty command template.
- Include all prompts in markdown for future study.

Design impact:

- Start with two cameras and one Orange Pi.
- Use PhotonVision as the pilot pipeline.
- Put camera placement recommendations in docs and provisional transforms in code.
- Add AdvantageKit vendordep and logging.
- Use 2025 drivetrain hardware constants.

## 2026-06-30 Best-In-Breed Follow-Up

Mentor reminder:

- Incorporate good ideas from other teams, not only basic PhotonVision.
- Northstar navigation looked promising.
- Interrupting or finishing PathPlanner trajectory and switching to position/tolerance final control looked promising.
- Goal is best-in-breed plus appropriate optimization.

Design impact:

- Vision code consumes all unread frames from every camera and sorts observations by timestamp.
- Vision acceptance/rejection is logged.
- Single-tag observations do not contribute trusted heading.
- Multi-tag observations get tighter heading covariance.
- The robot has a separate `DriveToPosePrecisionCommand` for final tolerance/settle motion after coarse trajectory motion.

## 2026-06-30 AI Template Follow-Up

Mentor reminder:

- The project should incorporate AI skills and templates for future modifications.
- The useful ideas are not limited to Team 6328.

Design impact:

- Added project `AGENTS.md` and `CLAUDE.md`.
- Added this prompt log and `SESSION_STATE.md`.
- Future AI sessions must update state before and after substantial tasks.

## Exact Conversation Prompts

### User Prompt - 2026-06-29

```text
The document S:\MechaRAMS\Vision\MechaRAMS vision system\prompt.docx contains the full initial prompt for this project. Review. Ask questions as needed. When there are no questions or ambiguities, do the tasks according to the prompt.
```

### User Clarification - 2026-06-30

```text
1. Yes, PhotonVision, one OrangePi, two cameras
2. Yes, two USB cameras. Will need to know where to put them (probably over the drive modules on the front) and their roll/pitch/yaw recommendation
3. Use the CAN bus name and CAN IDs for motors and Pigeon from 2025. Basically the code will run on 2025 chassis. In 2025 we ran SDS MK4 modules with L3 gear. https://www.swervedrivespecialties.com/collections/mk4-module-kits/products/mk4-swerve-module
4. The cameras will be "Arducam 100fps Global Shutter Color USB Camera Board, 1MP OV9782 UVC Webcam Module with Low Distortion M12 Lens Without Microphones" - [https://www.amazon.com/Arducam-Shutter-Distortion-Without-Microphones/dp/B0CLXZ29F9](https://www.amazon.com/Arducam-Shutter-Distortion-Without-Microphones/dp/B0CLXZ29F9) if you think that's sufficient for our purposes.
5. Yes, 6.5" tags. I will attach them as you see fit, as long as you prepare a field layout file for me.
6. Yes, definitely want AdvantageKit, especially for emulation, if you think this will work well.
7. Yes, feel free to overwrite anything there. It's an empty template with some standard example things there right now.

Do not forget to include all prompts including this conversation into some .md file for future study.
```

### User Follow-Up - 2026-06-30

```text
And do not forget - I want to incorporate good ideas from other teams. NorthStar navigation looked promising. INterrupting PathPlanner trajectory and switching to the position/tolerance one for precision is promising etc. The goal is to get the "best in breed" plus whatever optimization you think will be appropriate.
```

### User Follow-Up - 2026-06-30

```text
Also it does not need to be 6328 specifically - you saw a lot of good ideas and AI templates in other teams. I definitely want to incorporate AI skills and templates for further modifications as needed.
```

### User Follow-Up - 2026-06-30

```text
Create step-by-step click by click instructions how to run all that in simulation. Also create architectural documents - how does it all work, which pieces from which teams' code did you use and how, where should it run and how it should be deployed.
```

### User Compile Feedback - 2026-06-30

```text
Also compile discovered a few deprecated items:

> Task :compileJava
S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\Robot.java:41: warning: [removal] schedule() in Command has been deprecated and marked for removal
      autonomousCommand.schedule();
                       ^
S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\subsystems\VisionSubsystem.java:63: warning: [removal] PhotonPoseEstimator(AprilTagFieldLayout,PoseStrategy,Transform3d) in PhotonPoseEstimator has been deprecated and marked for removal
      estimator = new PhotonPoseEstimator(
                  ^
S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\subsystems\VisionSubsystem.java:67: warning: [removal] setMultiTagFallbackStrategy(PoseStrategy) in PhotonPoseEstimator has been deprecated and marked for removal
      estimator.setMultiTagFallbackStrategy(PoseStrategy.LOWEST_AMBIGUITY);
               ^
S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\src\main\java\frc\robot\subsystems\VisionSubsystem.java:79: warning: [removal] update(PhotonPipelineResult) in PhotonPoseEstimator has been deprecated and marked for removal
        Optional<EstimatedRobotPose> estimate = estimator.update(result);
                                                         ^
4 warnings
[Incubating] Problems report is available at: file:///S:/MechaRAMS/2027Prototyping/VisionTestingAndCalibration/build/reports/problems/problems-report.html

BUILD SUCCESSFUL in 18s
4 actionable tasks: 4 executed
 *  Terminal will be reused by tasks, press any key to close it. 
```

### User Dependency Update - 2026-06-30

```text
I also updated CTRE and AdvantageKit libraries to the latest version. The compile still worked.
```

### User Code Documentation Request - 2026-06-30

```text
I'd like you to document the code with great details. More importantly, indicated in relevant parts which idea was used and where, like you have in Team Ideas adapted, but in relevant parts of the code. That will be easier to track and explain.
```

## 2026-06-29/30 Claude (Opus 4.8) Session — Best-In-Breed Review and Implementation

This block records the mentor prompts that drove the second working session (Claude), where the
Codex output was reviewed against real top-team code and then substantially rebuilt. Recorded verbatim
(lightly trimmed only for typos) so students can follow exactly what was asked of the AI.

### Orientation - 2026-06-29

```text
I started working with Codex on the project. Check the AI_PROMPTS.md and follow the references and
descriptions from there to get a full scope. The SESSION_STATE.md contains current state of things.
Make sure to look at the Claude-specific files generated by Codex in the
S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration folder and its subfolders. Before I ask you
to do anything, first get familiar with all that and ask questions if needed.
```

### Goal Clarification - 2026-06-29

```text
Calibration is probably a wrong name for what I am trying to do. I want to figure out how should I get
the vision working for very high precision with navigation and trajectories, much better precision than
we had in prior two years, as our performance and aiming was very far from working well. I also want
everything to work in simulation as well. The idea was to analyze the code of very successful teams,
see what code, strategies, testing and calibration we can use in our code, whether we need to update
hardware, or can we proceed with what we have to get decent performance ... create "best in breed" code
that incorporates progressive ideas from many different top teams that published the code.
```

### Ambition / Process Answer - 2026-06-29

```text
The idea is to really get the code working the best way possible. This is a summer project and we have
plenty of time. I want, however, to run everything in simulation first. As long as everything is
properly documented, I am OK with complexity. I'd rather learn what other teams did now instead of
finding out things piecemeal. Note that you will also need to modify documentation on how all this
should be done, make sure to include prompts, update task list and session status etc. We're shooting
for an ambitious project. But we also want to accomplish it with somewhat limited hardware (we're not
buying a MAC MINI like 6328 does). I can use a few Orange Pi computers with the cameras I already
indicated (Arducam OV9782). Also make sure to comment out everything you do, indicating exactly where
parts came from (e.g. from ... idea of 2910...). Consider this final code a teaching exercise. Note
also that the ultimate goal for this year is to have ALL of the robot code AI-generated. So, we supply
skills and prompts and get the code. After everything is working, we will need to convert things to
skills and AI config files and prompts.
```

### Review Scope - 2026-06-29

```text
So for you - I wanted you to take a look at what Codex did and see if something may have been missed,
maybe something could have been done better or improved.

The idea was to evaluate the code that was discovered by prior research for all this. The links to the
repos discovered so far were in various documents in the folder that contains all of the documentation.
```

### Unattended Implementation Authorization - 2026-06-30

```text
After the summaries and documentation feel free to implement all of that unattended - as long as you
understand everything and do not have questions and do not see ambiguities. Otherwise definitely stop
and ask. I want you to be 100% sure in what you're doing.
```

### Aiming + Autonomy Answers - 2026-06-30

```text
For aiming - we do not know if the new season will be a shooting game or a placing one. So, if aiming
part is important for your implementation, configurable goal is OK. Remember - we're not doing the
Turret or any kind of GPM for now. We're just trying to see what can we do so we drive and position the
robot precisely.

For autonomy I am OK with doing everything - as long as you design a clear testing and calibration
process and documentation. It's a summer project and we have plenty of time to understand it.
```

### AdvantageScope Model Request - 2026-06-30

```text
Also for AdvantageScope - maybe have in documentation to point us to some robot model so we see how it
behaves on a field when we do simulation as well as real robot driving.
```

### Rejected-Ideas Document Request - 2026-06-30

```text
I also wanted you to indicate in some document which teams' interesting ideas you did not implement, and
why (e.g. maybe you have seen better ideas from a different team). That will help us to see what works
better and maybe choose a different path if something would not prove to be fruitful. Do not forget to
update prompt document so the team can follow what exactly did I ask AI to do.
```

### Hardware Cognizance - 2026-06-30

```text
Remember that we're not using MAC MINI like 6328 does. Just Orange Pi. So, I do not know if we can
really process data on it at 250 Hz with PhotonVision. Maybe we can. So be cognizant about our hardware
- 4 cameras, two Orange Pi.

But if you think it's enough to start with two cameras and one Orange Pi, that would be even better.
Nevertheless if you think putting two Pi and four cameras is important, I will do it.
```

Design impact of this session:

- Cloned and read the actual top-team repos (6328, 3467, 1768, 6995) instead of relying on summaries;
  results in `CODEX_CODE_REVIEW_AND_GAP_ANALYSIS.md` and `DESIGN_DECISIONS_AND_REJECTED_IDEAS.md`.
- Fixed real bugs (vision timestamp time base, no NaN gate, precision command had no timeout/logging).
- Rebuilt vision on the AdvantageKit IO-layer with a working PhotonVision simulation.
- Upgraded the precision controller (profiled + feedforward + settle + safety timeout + logging).
- Added chassis aim-at-configurable-goal (no turret/GPM) with shoot-on-move lookahead as a teaching
  artifact.
- Clarified that 250 Hz is roboRIO/CANivore odometry, not an Orange Pi rate.
- Chose 2 cameras / 1 Orange Pi as the recommended start, code scalable to 4 cameras / 2 Pis.
- Added headless JUnit tests so the math is verifiable without a robot.

### Trajectory Method Selection (to Codex) - 2026-07-01

```text
I think the idea was to see whether some trajectory navigation method is faster or better than the
other? Wouldn't the last results simply indicate that the pure PathPlanner navigation is inferior to the
trajectory methods that were designed for me? And if so, which one should we use?
```

Design impact:

- Confirmed from the sim log that pure PathPlanner is transit-only (0.066 m at its own endpoint) while
  the precision handoffs land 0.027–0.034 m from the tag-board target.
- **Spatial handoff (`handoffFrom`, x > 3.3) adopted as the primary competition pattern**; sequential
  handoff retained as debug/reference; pure PathPlanner retained as baseline test only.

### Technique Refresh Directive - 2026-07-16

```text
Check if there is newer version of any software published by other teams that we used, and check if
any new top-15 teams either in worlds or respective districts published or updated something relevant
to us. [...] Besides just updating algorithms I want you to see if there is newer or better way to
improve precision of both localization and trajectory driving than what we're doing or investigating.
Note that we decided really not to use QuestNav for now due to its size, though for localization it
worked fine. So, right now we focus on camera-based features. Primarily - with PhotonVision.
```

Design impact:

- QuestNav formally excluded (size), camera-based/PhotonVision confirmed as the platform.
- All dependencies verified current (2026-07-16); no upgrades available.
- New study repos: 2910-2026, 1678-2026, 5940-2026 in `_research_clones`.
- Adoption candidates recorded in SESSION_STATE: PhotonVision trig-solve + constrained SolvePnP
  (single-tag precision), 5940-style anisotropic log-fitted covariance, PathPlanner
  SwerveSetpointGenerator (anti-slip odometry), 1678 lookahead pose input.

### Implementation Authorization + Test Plan Request - 2026-07-16

```text
If you can improve the code, feel free to do it. Commit when needed. [...] If there is a new
algorithm you want us to test separately, add it to the autos that we can run. If there are
improvements to the existing algorithms you want us to test, write a test plan in .md or modify
existing one. So far we only tested in simulation, but we will have time to test on a real robot.
So, tell me what to test and in what sequence. Feel free to add additional autos that we should be
testing with. Make sure to implement proper code coverage (I think you already have some of that).
My goal is preferably stay 90% or above.
```

Design impact:

- Implemented trig-solve single-tag strategy + anisotropic covariance as runtime-toggleable A/B
  options (validated baseline stays default); four "AB:" autos added to the chooser.
- `VisionPolicy` extracted from `Vision` so all fusion decisions are pure + fully coverable.
- JaCoCo added with a >= 90% line-coverage gate on the pure-logic classes (VisionPolicy,
  SingleTagTrigSolver, AimingCalculator — all currently 100%); hardware-bound classes are validated
  by the sim/robot test plan instead.
- Sim (S1–S5) and real-robot (R0–R5) test sequences written into VISION_AND_TRAJECTORY_TEST_PLAN.md,
  including the R2 procedure that fits the anisotropic coefficients from logs.

### Algorithm Comparison Documentation Request - 2026-07-16

```text
Can you make sure that the logical differences between algorithms are described in documentation,
and may be do some comparative analysis and preliminary expectations from testing? I want to make
sure that all that is reflected in the documentation and the code walkthrough.
```

Design impact:

- New ALGORITHM_COMPARISON_AND_EXPECTATIONS.md (comparison tables, error budgets, failure-mode
  duality, per-test prediction table with deviation diagnoses; R5 decision rule = best worst-case).
- Test plan R2 now requires fitting anisotropic coefficients under the winning single-tag strategy.
- Walkthrough A7 opens with the "lever arm" logical framing and points to the comparison doc.

### Exact Step-by-Step Test Plan Request - 2026-07-16

```text
Make sure you note the exact step-by-step test plan (e.g. 1. Test each method in Sim using simple
2 m forward trajectory, 2. Test each method on real both the same way, 3. Test each method on curved
trajectory - indicate which kind etc)
```

Design impact:

- Added the curved test trajectory (VisionTestCurved: S-curve with 25 deg mid-path rotation sweep)
  because the existing paths were all straight — a curved+rotating transit is what actually stresses
  vision (changing camera->tag views, single-tag stretches).
- Test plan now has a numbered 19-step execution checklist (sim steps 1-12, robot steps 13-19) over
  three shared motions (M1 straight precision-only 2.75 m, M2 straight handoff, M3 curved handoff),
  with a fill-in results table and a best-worst-case decision rule.

### Student-Level Algorithm Explanation Request - 2026-07-16

```text
Besides great mathematical explanation that you provided I also need the one suitable for
high-schoolers (remember - we're high school team), so something suitable for 15-years-old that
would describe each of the 6 or whatever number of algorithms we will be testing, what makes each
of them different, and comparative analysis between them, including their usability in each case.
```

Design impact:

- New ALGORITHMS_FOR_STUDENTS.md: 10 algorithms, analogy-based explanations, comparison table,
  when-to-use cheat sheet. Team documentation now has three levels: student guide -> engineering
  comparison (ALGORITHM_COMPARISON_AND_EXPECTATIONS.md) -> line-by-line code walkthrough.

### Adoption Policy + Camera Placement Question - 2026-07-16

```text
I'd like to see you adding to the explanation and documentation what do we plan to do with all
these algorithms - do we use only some of them? Do we switch between them depending on the game
situation or how we're driving? In other words - what will we do with the results of our
evaluation? Will it also help us to determine what's the best camera placement and their
orientation?
```

Design impact:

- Adoption policy documented: layered algorithms, one fixed competition configuration chosen at
  step 19, no driver-switched modes, per-frame automatic selection is the real "situational
  switching"; conditional heading-health fallback named with its data trigger.
- Test plan step 20 added: camera-placement analysis from existing logs (coverage map + per-camera
  error curves) feeding cross-eye yaw, 2-vs-4 cameras, and per-camera trust factors.
# 2026-08-09 — Current-pose forward trajectory comparison

Mentor request: add selectable 1-meter and 2-meter forward autonomous motions using the current fused
robot position as the start rather than a predefined/reset pose. Provide comparable chooser entries for
the existing vision algorithms and allow the motion to correct final yaw to zero.
# 2026-08-09 — Seed pose from cameras

Mentor request: add an Xbox controller button that seeds both drivetrain position and direction from
the current camera-derived pose. Preserve the project's rule that single-tag heading is untrusted.

# 2026-08-10 - Measured distance bias, overshoot, and camera mount angles

Mentor measured 1.05 m actual travel for a 1.00 m trajectory and 2.10 m for a 2.00 m trajectory, and
authorized correcting the linear drivetrain scale. A one-camera test reduced endpoint jerking from about
four seconds to about two seconds, implicating the rigid camera mounts' actual yaw rather than intrinsic
Charuco calibration. Determine a repeatable method to solve robot-to-camera yaw/pitch (programmatically
from MultiTag field-to-camera transforms or with a physical laser check), then address true controller
overshoot only after wheel scale and camera extrinsics are corrected.

Implementation decision: retain the manually measured translations because the physical distance
measurements are more reliable than the cameras' mutually inconsistent absolute X/Z estimates. Use
left `(0.152, +0.266, 0.420)` m at pitch/yaw `-18.88/-14.80` degrees and right
`(0.152, -0.266, 0.435)` m at pitch/yaw `-17.10/+13.69` degrees, with roll fixed at zero.

# 2026-08-16 - Close and rotate the active AdvantageKit log

Mentor request: verify whether the uploaded log reaches the final trajectory timestamp at 2024 seconds;
if it does not, add a button that closes the active log and starts a new one so a completed file can be
downloaded reliably. The uploaded file ended at 14.475459 readable seconds. Implement a disabled-only
dashboard action, preserve complete-table boundaries without blocking the logger receiver thread, and
expose rotation status in AdvantageKit rather than trying to stop and restart the global `Logger`.

Real-roboRIO correction: synchronous file close/open inside the receiver callback exhausted the
AdvantageKit receiver queue. The final design must never perform filesystem open/close on that callback:
open the uniquely named replacement on a background thread, atomically swap the ready writer between
complete tables, and close the previous writer on a background thread.

# 2026-08-19 - Analyze six separate 1 m / 2 m precision logs

Mentor supplied three 1 m and three 2 m rotated WPILOGs plus tape measurements, reporting that every
run overshot and returned and most showed slight clockwise drift. Correlate each file with the physical
endpoint, quantify peak overshoot, settling, yaw, vision disagreement, voltage, and requested-vs-measured
speed. Decide whether wheel scale, motion constraints, low-level drive control, pose PID, or camera
fusion should be changed first; do not tune from final distance alone.

# 2026-08-19 - Implement the controlled closed-loop follow-up

```text
I accept the recommendations. Do all changes in the code as needed. If you need to monitor additional
items, let me know which ones.
```

Design impact: isolate the low-level response by giving only `DriveToPosePrecisionCommand` a CTRE
closed-loop `Velocity` request; preserve the validated wheel radius, motion constraints, and pose gains.
Log profile feedforward, pose feedback, clamped request, measured chassis motion, and tracking errors as
both structs and graph-friendly scalars. Initialize AdvantageKit power-distribution logging without
guessing a PDH CAN ID, and document the exact six-run comparison and required physical measurements.

# 2026-08-19 - Prepare the AdvantageScope validation layout

```text
It is saved in the folder c:\MechaRams\Temp. Create a new configuration file for me and do not override
the existing one. Also make sure that the new items actually exist with the correct path.
```

Design impact: preserve the mentor's original AdvantageScope JSON and create a validated copy with a
dedicated precision velocity-tracking graph and all new controller fields added to the Table. Verify
every configured leaf against the exact logger key in source before handoff.

# 2026-08-20 - Add a dashboard log-purge command

```text
In fact, for future changes - make a button on a smartdashboard that deletes all logs captured in that
folder. Do not need to preserve content of that directory; files and subfolders can be deleted.
```

Design impact: make the destructive command disabled-only and nonblocking. Detach file logging between
complete tables, close the active writer, recursively purge the guarded log folder, and then open a fresh
active log; this ordering works even at 100% disk usage. Live NT4 continues while a few file-log tables
may be dropped. Expose pending/count/timestamp/error status for verification.

# 2026-08-20 - Correct remaining precision overshoot and PDH logging

```text
We ran a single one meter test; it jittered less but still overshot about 2 cm. The 2 m trajectory
overshot about 20 cm. Recommendations accepted: modify code as needed, give the ordered test sequence,
and make sure PDH data uses the proper API.
```

Design impact: keep the controlled CTRE velocity-mode comparison and existing gains/constraints, but
fade translational profile feedforward from 1.0 at 0.35 m to 0.0 at the 0.04 m tolerance based on
measured remaining distance. Do not fade pose feedback. Log raw versus faded feedforward and the scale.
Use AdvantageKit's explicit REV-PDH registration on documented default CAN ID 1, then validate one 1 m
and one 2 m safety run before collecting repeats or running translation SysId.

# 2026-08-24 - Eliminate the long overshoot-and-return tail

```text
The main issue is that it is still overshooting, going back, and taking about one second to return to
the 1 m position. Evaluate why, correct what is needed, and add whatever data is needed.
```

Design impact: the fade fixed most peak X error but exposed an undamped stop/settle state. Add
near-target measured-velocity damping for translation, measured-rate damping for theta, velocity limits
to the goal condition, and a closed-loop zero request during a shorter qualified hold. Log pose-only vs
velocity-qualified entries and controller-request vs applied-request. Mirror voltage/current from the
existing AdvantageKit-owned PDH Conduit snapshot into graph-friendly outputs; never allocate a second
WPILib PDH object for the same CAN ID.

# 2026-08-24 - Fix duplicate PDH allocation at startup

```text
Robot startup fails with AllocationException -1029: REV PDH 1 previously allocated. The first
allocation is LoggedPowerDistribution/Conduit and the second is Robot's WPILib PowerDistribution.
```

Design impact: retain `LoggedPowerDistribution` as the only HAL owner. Remove the second WPILib device
object and populate `PowerDistributionDirect/*` from `ConduitApi`, which exposes the already-captured
PDH snapshot without allocating the device again.

Follow-up correction: the installed device was not verified at ID 1. Existing-handle JNI reads returned
zeros and then emitted `CAN: Message not found`, causing robot-loop overruns. Remove explicit registration
and all direct polling; use `SystemStats/BatteryVoltage` until hardware configuration supplies the real
PDH ID/bus.

# 2026-08-24 - Decide whether to tune handoff, loop rate, or PID derivative

```text
The damped 1 m run still takes about the same time to stabilize. Do we need to tune when PathPlanner
switches to closed-loop position, run the closed loop faster than 20 ms, or adjust PID kD?
```

Design impact: the relative-forward autos are direct precision-control moves and contain no PathPlanner
handoff. The log shows a 0.316 m/s signed velocity mismatch at target crossing and five separate goal
entries, so keep the 20 ms outer loop and correct the settling state machine plus the uncharacterized
TalonFX velocity loop. Explicit measured-velocity damping is already derivative-like; do not stack PID
kD on top until translation SysId supplies drive feedforward and the low-level response is validated.

# 2026-08-24 - Make settling a bounded fraction of the move

```text
Do not just rerun unchanged code. The goal is to get settling time under 10% of total trajectory time.
Use PathPlanner for most real autonomous motion and precision control for the end.
```

Design impact: retain direct 1 m/2 m moves as diagnostic controller baselines, and use coarse-to-precise
handoff for final autos after the final controller passes. Define settling time as first target crossing
until pose and chassis speed enter their limits and remain there; require it below 10% of controller
active time. Latch the qualified zero-velocity hold with a wider pose escape envelope, then characterize
the low-level velocity loop before changing outer-loop kD, loop rate, or PathPlanner handoff distance.

# 2026-08-24 - Define jitter by physical wheel motion

```text
Jitter is the interval from when the robot appears to reach the settled destination until the wheels
actually stop. It should be visible from wheel encoders. The X error appears below the threshold while
the jitter still occurs; can it be eliminated?
```

Design impact: add mean/max measured and target module-speed scalars plus a strict wheel-stopped flag.
Evaluate the full radial X/Y and heading tolerance rather than X alone. The `cca9ab7b` log shows a
0.719 s wheel-motion tail (32.6% of active time) and 3.4-7.1 cm inter-camera position disagreement near
the target. Preserve current control gains for a right-camera-only versus left-camera-only A/B before
widening tolerance, increasing loop rate, or adding another derivative term.

# 2026-08-24 - Reduce the final visible 0.2 second tail

```text
The ef00a32a run was really good and jitter was perhaps close to 0.2 seconds. Is there anything that
can cut the jitter down to pretty much zero?
```

Design impact: distinguish visible stop (~0.05 m/s maximum module speed) from a strict encoder-zero
threshold. The latch already entered once and held; the remaining delay follows a large module velocity
tracking mismatch during braking. Characterize the TalonFX translation loop before changing pose
tolerance or outer-loop timing, then fit `kS/kV/kA` and tune velocity-loop `kP` from a controlled step
response. Do not claim mathematically zero settling; target no visible correction and <10% active time.

# 2026-08-24 - Authorize CTRE tuning and shorten command lifetime

```text
If you need to try changing CTRE constants, we can try those. I need command time close to 1.2 seconds,
which is the visible drive time. Why does the command not end when we are there? If speed is low enough,
the wheels will stop in brake mode.
```

Design impact: raise only drive velocity `kP` from 0.10 to 0.20 V/rps and shorten the already-zero
post-qualification confirmation from 0.15 to 0.05 s. Keep pose/velocity qualification intact; at first
X-band arrival in `ef00a32a`, modules and yaw were still moving too quickly to safely declare completion.
The unchanged 2.5 m/s^2 constraint has an ideal 1 m profile time of 1.265 s, so validate braking and a
roughly 1.3-1.5 s command before considering faster constraints.

# 2026-08-24 - Reject an invalid CTRE comparison

```text
Log 7ddca884 finished at 1.05 m on both sides, but jitter was much longer.
```

Design impact: verify provenance from recorded configuration fields before attributing a run to a
constant change. This log lacks every field introduced with the `kP=0.20`/0.05 s experiment and retains
the old 0.15 s hold, so it is an old-code repeat rather than a failed new-gain test. Its delay was driven
by heading/pose qualification and 7.9 cm inter-camera disagreement before the hold, not a longer
post-hold brake tail.

# 2026-08-24 - Reject the confirmed `kP=0.20` experiment

```text
Log 20011a08: the robot is straight and finishes at 1.03 m, but jitter is bigger.
```

Design impact: the log confirms `kP=0.20` and 0.05 s confirmation. Revert only drive velocity `kP` to
0.10 because physical overshoot, total time, and velocity reversals worsened. Keep the 0.05 s
confirmation for one isolated comparison; its shorter duration did not cause the pre-hold overshoot.

# 2026-08-24 - Explain why the controller did not stop at 37.700 seconds

```text
Why did the bot not stop at 37.7? It seemed to be within tolerance.
```

Design impact: sample every gate at the exact cursor. Translation position/speed and angular speed all
passed, but yaw error was 1.579 degrees against a 1.5 degree limit, leaving the goal unqualified and
requesting -7.02 deg/s. Consider a separately controlled 2.0 degree yaw-capture test after reverting
the failed drive `kP`; do not hide this diagnosis by changing status-frame rates.

# 2026-08-24 - Make terminal yaw tolerance segment-specific

```text
Maybe make yaw tolerance close to 1.8 degrees for trajectory. Add a yaw-precision flag so some
trajectories remain very yaw-precise while others have a higher tolerance; yaw is less important for
an intermediate part of a multipart trajectory.
```

Design impact: add explicit `PRECISE` (1.5 degrees) and `RELAXED` (1.8 degrees) modes to
`DriveToPosePrecisionCommand`, defaulting all existing callers to `PRECISE`. Use `RELAXED` only for the
current-position 1 m/2 m straight-distance autos. Keep tag-board and final handoff segments precise;
future multipart groups can opt individual nonterminal precision segments into relaxed yaw. Log the
mode and effective numeric tolerance so a run cannot be misidentified.

# 2026-08-24 - Relaxed-yaw run still has substantial jitter

```text
Log 85e18116: left 1.02 m, right 1.0175 m, with lots of jitter.
```

Design impact: validate deployment first (`RELAXED`, 1.8 degrees) and separate active correction from
the final hold. The hold was clean and only 0.060 s; the robot repeatedly failed pose/velocity gates
before it. At the first pose entry, measured omega was +32.8 deg/s against a -18.2 deg/s request. Both
cameras were accepted but disagreed by 4.6 cm average/12.5 cm maximum and up to 2.7 degrees. Do not
widen tolerance or retune gains from this mixed-input run. Run right-camera-only then left-camera-only
1 m comparisons to isolate extrinsic/fusion disagreement from low-level velocity tracking.

# 2026-08-24 - Isolated camera comparison

```text
Left camera covered: log 84e33292, quite a bit of jitter. Right camera covered: log 0d086421, not
nearly as much jitter.
```

Design impact: map coverage to camera identity explicitly—left covered means front-right-only, and
right covered means front-left-only. The right-only run had 59.7 deg/s peak omega, 15 omega reversals,
0.715 m/s post-target module speed, and 0.160 s post-command wheel motion; left-only reduced those to
39.8 deg/s, nine reversals, 0.312 m/s, and 0.042 s. Same-position stationary dual-camera samples show
right estimates yaw about +2.05 degrees and Y about -7.8 cm relative to left. Recommend a controlled
right-transform yaw correction (+13.69 to approximately +15.74 degrees) with XYZ/pitch unchanged,
pending mentor approval, then repeat static dual-camera and 1 m checks. Do not alter gains/tolerance in
the same comparison.
