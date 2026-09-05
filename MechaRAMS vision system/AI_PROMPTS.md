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

# 2026-08-31 - Approve yaw correction and add per-camera jitter validity testing

```text
I agree with the recommendation, so change things. Should we have testing to determine camera jitter
and perhaps adjust the validity of results from each camera?
```

Design impact: apply only the measured front-right yaw correction (+15.74 degrees). Add a
disabled-only SmartDashboard capture that freezes 100 accepted MultiTag poses per camera and logs mean
pose, X/Y/translation/yaw population standard deviation, peak-to-peak ranges, and camera-to-camera mean
offset. Add independent per-camera XY covariance factor, angular factor, and rotation-trust enable
controls, all neutral for the first capture. Use systematic mean differences to correct calibration;
use measured random-scatter ratios to change covariance; disable only a camera's theta if corrected
MultiTag heading remains unsafe. Never automatically learn validity from a moving trajectory.

# 2026-08-31 - First camera-jitter log downloaded

```text
log downloaded last 4 digits b267
```

Analysis impact: `cbe4b267` contains the deployed instrumentation but is not a completed capture.
`Active` never became true and both sample counts stayed at zero, although both cameras supplied
accepted MultiTag observations. Require an observed Active transition and increasing counts on the
next disabled attempt; do not derive camera weights from this log.

# 2026-08-31 - First valid camera-jitter capture

```text
new log capture is 9562
```

Analysis impact: `1788218509562_cce4a8da` completed 100 samples per camera while disabled. Front-left
showed about 2.15x the translation scatter and 2.12x the yaw scatter of front-right, but their mean
yaws straddled expected zero and remained 1.54 degrees apart. Require a same-pose repeat and a second
viewing pose before changing transforms or camera-specific covariance.

# 2026-08-31 - Closer and farther stationary captures

```text
We have two logs when the robot was stationary. The one ending with b12e is from a closer position,
while the one ending 1546 is the farther one. The linear distance between the two positions is about
1 meter on the X axis.
```

Design impact: both captures completed successfully and repeated front-left's approximately 2x larger
random scatter. Apply measured Camera0 XY/angular factors `2.15/2.10`. Camera1/front-right was quieter
but its mean yaw changed `0.47 degrees` across the move versus `0.05 degrees` for front-left, so disable
only Camera1 theta while retaining its XY. Do not alter transforms or drivetrain/trajectory controls in
the same change.

# 2026-08-31 - Deployed validity stationary and 1 m logs

```text
The log that captures the 1 meter trajectory move ends with c75a. The stationary log captured before
the move ends with fe31.
```

Design impact: `fe31` confirms the measured per-camera settings. `c75a` is calmer after entering pose
tolerance, but fused yaw changed 2.71 degrees beyond integrated measured omega and delayed completion
to 1.806 seconds. Preserve front-left MultiTag heading for disabled/manual seed, make all enabled
camera fusion XY-only, and leave controller gains/tolerances unchanged for the next isolated test.

# 2026-08-31 - Enabled XY-only follow-up endpoint and frame geometry

```text
The log ends with 1c79. The left side of the frame is 1.01 meters from the starting point. The right
side is 0.985 meters. Each wheel center is 7.5 cm in both X and Y from the frame edge.
```

Design impact: center travel is `0.9975 m`; the frame measurement spacing is `0.6072 m`, making the
endpoint difference approximately `-2.36 degrees` clockwise. The deployed XY-only enabled-vision mode
was present, but the camera-seeded start yaw was `+1.55 degrees`, so targeting zero commanded an
unwanted turn. Normalize heading to zero only for the physically squared relative-forward calibration
autos while preserving current X/Y. The 2.242 s run also contained 0.218/0.200 s scheduler gaps that
coincided with 18/16 queued observations. Preserve every unread frame in a FIFO, pace each camera to
one estimator observation per robot loop, and log backlog plus IO/fusion/periodic timing. Do not tune
PID, tolerances, transforms, or covariance in the same test.

# 2026-08-31 - Prioritize the visible final 5-6 cm correction tail

```text
What we really want to address is drive time. Driving to approximately the correct position takes
1.2-1.5 seconds; the rest is settle-down time. Reduce the almost-in-place jitter that only moves the
bot about 5-6 cm.
```

Analysis impact: in `1c79`, first <=6 cm error occurred at +1.7125 s and finish at +2.2424 s. The
0.5299 s tail accumulated 21.7 cm fused path for 4.6 cm net progress, four vx reversals, and eight omega
reversals. The real 4 cm pose gate was first reached at +2.0998 s and the command finished only 0.1426 s
later, so shortening the 0.05 s hold or widening tolerance does not address the visible behavior.
Feedforward was nearly gone and damping nearly full throughout the tail. First validate the pending
heading-normalization and camera-FIFO change; if reversals remain, A/B one terminal damping/feedforward
adjustment with physical overshoot as the safety gate.

# 2026-08-31 - FIFO validation overshot by 14 cm

```text
The log ends with 7881. Drive distance was 1.14 m at the left frame corner and 1.145 m at the right,
so the bot overshot by about 14 cm.
```

Design impact: reject the persistent FIFO immediately. `716d7881` ended with 119/52 camera poses still
pending and almost no enabled vision accepted after the heading reset. Fused progress reported only
0.966 m while physical center travel was 1.1425 m. Preserve the successful heading normalization
(corner delta improved to 5 mm, about 0.47 degrees) and timing logs, but drain each camera completely
and fuse only the newest solvable pose per robot-loop burst. Log older same-burst poses as superseded.
Do not tune PID/damping/tolerance from this invalid-localization run.

# 2026-08-31 - Newest-frame validation and short endpoint

```text
The log is 7b6d. The drive distance was 0.985 m left frame corner and 0.96 m on right frame corner.
```

Design impact: physical center travel is `0.9725 m`, while fused along-track travel is `0.9661 m`, so
newest-only vision removed the prior estimator/physical distance divergence. The command still lasted
`2.275 s`; the final <=6 cm consumed `0.572 s`. Robot-loop executions averaged `34.5 ms`, yet each
profiled controller advanced only 20 ms. The stale profile placed the robot ahead of its internal
setpoint and produced negative X feedback before reaching the physical target, followed by a forward
correction as the profile caught up. Synchronize the profile to accumulated wall time with a five-step
catch-up cap and explicit timing logs. Keep all gains, damping, tolerances, transforms, and camera
validity settings unchanged for one controlled 1 m A/B run.

# 2026-08-31 - Wall-clock profile validation succeeds

```text
The log is c346. The drive distance was 1.025 m left frame corner and 0.995 m on right frame corner.
This was much better timing. There was almost no observable settling down once the bot arrived to
where it stopped.
```

Analysis impact: the command completed in `1.274 s`; its final <=6 cm took `0.114 s` or `8.95%`, so
commit `39ab164` meets the requested <10% settle-tail goal. Fused final error was 3.1 mm, with one hold
entry and no exit. Keep all controller and vision constants unchanged. The remaining 3 cm ruler-corner
difference implies about 2.83 degrees clockwise physical rotation even though fused gyro heading ended
about 0.19 degrees the other way. Run one unchanged 2 m test and measure both endpoints plus lateral
motion; if the signed difference repeats, add raw-Pigeon-versus-wheel-only yaw logging before any
rotation tuning. Separately, do not raise motion constraints until the two early 180/117 ms user-code
gaps are diagnosed.

# 2026-09-02 - Two-meter confirmation and acceptable per-command yaw precision

```text
The log is 4844. The drive distance was 2.05 m left frame corner and 2.04 m on right frame corner. We
observed the settling time around 0.5 seconds. Even the angular difference—while I would have liked to
get it under 1.6 degrees or so—the 2.2 degrees is not that huge, especially if we can have a special
command for yaw precision if it is really required in a specific case.
```

Design impact: retain the existing explicit `PRECISE`/`RELAXED` command modes and do not globally make
all segments wait for precision heading. In `4844`, however, the final physical corner difference was
only about `0.94 degrees`; the delay came from a transient `-4.97 degree` yaw excursion. Translation
was within 4 cm by +1.672 s, but the combined gate waited until +2.555 s. More importantly,
module-kinematic omega integrated to `+8.09 degrees` while gyro-owned pose changed `-0.67 degrees`.
Use the Pigeon Z-world rate for theta damping and angular settling, retain module omega as diagnostic
and fallback, log both at an explicit 100 Hz Pigeon signal rate, and validate this isolated change with
one repeated 2 m run before changing tolerance or theta gains.

# 2026-09-02 - First gyro-rate deployment health check

```text
Drive/GyroYawRateSignalOK is false; Drive/GyroYawRateAppliedUpdateFrequencyHz is 250 Hz.
```

Design impact: stop before running the A/B trajectory. `getAngularVelocityZWorld(false)` created the
cached signal without refreshing it, so status remained uninitialized and the controller correctly
used its kinematic fallback. Initialize through the refreshing getter and call nonblocking
`refresh(false)` before consuming the cached rate. Do not force the applied rate down: Phoenix can
report 250 Hz when the drivetrain's 250 Hz odometry signal shares the same status frame and requests a
higher rate. Validation requires `SignalOK=true`, a live nonzero response when the chassis is rotated,
and any applied rate at or above 100 Hz.

# 2026-09-02 - Corrected gyro-rate 2 m validation

```text
The log is 0caf. The drive distance was 2.015 m left frame corner and 1.995 m on right frame corner.
There was almost no settling time.
```

Analysis impact: accept the Pigeon-rate change. Physical center travel was `2.005 m`; command time
fell from 2.808 s to 1.941 s, peak yaw fell from 4.97 to 2.27 degrees, and the post-pose-entry tail was
0.104 s or 5.36%. The Pigeon rate integrated to -0.90 degrees versus -1.06 degrees from gyro-owned
pose, while module-kinematic omega still predicted +7.32 degrees. Freeze direct-drive control and
vision constants. Run one unchanged 1 m regression, then proceed to PathPlanner-to-precision handoff
testing if that remains near the prior 1.274 s result without visible settling.

# 2026-09-02 - One-meter regression exposed unsafe settle-latch completion

```text
The log is 0b06. The drive distance was 1.0 m left frame corner and 1.045 m on right frame corner.
There was almost no settling time.
```

Design impact: do not accept the visually short tail as a pass. The 4.5 cm corner difference implies
about +4.24 degrees of physical yaw. The command qualified once at +1.152 s, but Pigeon yaw rate then
rose from 7.73 deg/s to 21.39 deg/s during the latched hold and was still 19.11 deg/s at command end.
The pose-only escape envelope allowed completion because heading error had not yet exceeded 2.5
degrees. Preserve the existing entry limits and pose hysteresis, add a wider 0.18 m/s / 12 deg/s
measured-motion escape envelope, log pose and velocity escape causes separately, and repeat one 1 m
validation before any gain or profile change.

# 2026-09-02 - Velocity-safe one-meter validation

```text
The log is 1bd6. The drive distance was 1.005 m left frame corner and 1.027 m on right frame corner.
There was a little more settling than last time but not a lot.
```

Analysis impact: accept the velocity-escape correction and make no additional controller change.
Center travel was 1.016 m and physical yaw was approximately +2.08 degrees. The command took 1.4637 s;
its final stable pose-entry tail was 0.1085 s (7.41%), with one AtGoal entry, no hold exit, and a
0.0683 s final hold. It ended at 0.0605 m/s and 2.17 deg/s instead of the unsafe 19.1 deg/s in `0b06`.
Preserve the earlier brief yaw excursion and 0.3835 s first-entry-to-end interval in the record. Move
to PathPlanner-to-precision spatial-handoff testing rather than retuning from this single run.

# 2026-09-02 - Fixed-start spatial handoff reversed into an obstacle

```text
The bot went backwards first and hit an obstacle, and then went forward. We always start the bot at
4.13 distance from the board looking forward. Driver Station is blue. PhotonVision reports starting
field-to-camera X about 2.399 m, so starting robot X is not 1.5 m.
```

Design impact: alliance flipping was not involved (`shouldFlipPath` is false and DS was blue). The
fixed auto reset the estimator to x=`1.5 m`; when fresh vision restored the actual robot pose, the
time-based path saw the robot roughly 0.75 m ahead and commanded backward. Replace every straight
VisionTest chooser variant with a runtime path from a fresh accepted MultiTag robot pose. Preserve its
translation, normalize yaw to zero, retain the x=`3.3 m` handoff and `(4.25, 2.0)` precision target,
and reject missing or implausible starts without motion. Treat PhotonVision's displayed pose as camera
pose, not robot-center pose. At the final target the front lenses remain about `1.60 m` in X from the
tag plane, preserving the two-tag view. Keep the legacy fixed auto only as an editor reference; do not
change the validated direct-distance controller or curved test in this safety correction.

# 2026-09-02 - Spatial-handoff start status absent before enable

```text
We do not see PathPlanner/VisionTest/StartAccepted.
```

Design impact: the deferred command did not create that output until autonomous initialization, so
its absence in a disabled live view was expected but poor safety UX. Initialize it at robot startup as
false with `AbortReason=NOT_RUN`, and continuously log a disabled preflight group containing fresh
MultiTag availability, safe start, ready-to-enable, robot pose, expected travel, and board distance.
Keep the actual `StartAccepted` decision at auto initialization. Correct the AdvantageScope live graph
to use the `/AdvantageKit/RealOutputs/...` namespace; saved logs continue to use `/RealOutputs/...`.

# 2026-09-02 - First measured-start spatial handoff run

```text
The log is cb20. The drive distance was 1.995 m left frame corner and 2.008 m on right frame corner.
There was a slow down in the middle of drive which was probably a hand over from the PP to the last
leg algorithm. The settling time was fairly low. In the middle of drive it slowed down and then speed
picked up again.
```

Analysis impact: the measured-start safety correction worked and achieved `2.0015 m` physical center
travel against `2.00836 m` expected. The slowdown is a real planned velocity valley: the zero-speed
coarse endpoint at x=`3.6 m` made PathPlanner reduce module target from `1.379` to `0.746 m/s` before
the x=`3.3 m` handoff, then the precision controller accelerated back to `1.606 m/s` with `0.905 m`
remaining. The final stable hold was only `0.061 s`. Recommend one controlled follow-up: spatial mode
alone uses a nonzero coarse goal-end velocity near the observed `1.4 m/s` cruise speed; do not alter
handoff location, final target, gains, tolerances, or vision. Separately preserve the two startup loop
gaps (`296/174 ms`) and mid-path `55-61 ms` gaps for later runtime-performance diagnosis.

# 2026-09-02 - Implement accepted nonzero spatial-handoff end velocity

```text
You did not modify the code. Can you modify the code to accommodate the changes so I can re-test?
```

Design impact: implement the previously accepted `cb20` recommendation. The generated straight path
uses a `1.4 m/s` goal-end velocity only for `SPATIAL_HANDOFF`; coarse-only and sequential paths remain
at zero. Log the selected value so the deployed artifact is directly verifiable. Do not alter the
handoff position, endpoint, final target, constraints, PID/damping, tolerances, vision, or direct
distance autos. Update the test layout and documentation, but do not build, test, simulate, or deploy.

# 2026-09-04 - `0586` smooth handoff but large overrun

```text
The log is 0586. The drive distance was 2.213 m left frame corner and 2.31 m on right frame corner.
It was running smooth but it seems it overran. There was almost no settling time.
```

Design impact: `0586` confirms `CoarseGoalEndVelocityMetersPerSecond=1.4` and removes the velocity
valley, but exposes a `468 ms` handoff scheduler cycle (`461 ms` user code; `407 ms` inside the
sequential group). The robot coasted from about x=`3.315` to x=`4.154` before the first precision
update. Twenty-two DriveToPose outputs were first registered in that handoff cycle and 69 more during
the first execute, causing further `88-96 ms` loops. Prime those exact existing output types during
safe startup without issuing a drive command. Keep the speed/profile/controller/vision experiment
otherwise unchanged, log priming status/duration, and use a separate timing-focused layout.

# 2026-09-04 - Plain-English functional algorithm and handoff guide

```text
Create an MD document that explains in plain english on high school level what different algorithms
we use do, and how the control is passed from one to another. For instance - we start driving with
PathPlanner, and then when we reach something like 80% of the trajectory we switch to the last-leg
algorithm that at some point stops correcting for angle if the angular speed does not exceed X radians
per second, but if it does it will do more corrections. In other words, I'd like an explanation that
would be less math and more functional one, for instance for management or build team. Also keep
modifying/adjusting this as we are using more algorithms. Basically I am looking for if/then type of
logic.
```

Documentation impact: add `FUNCTIONAL_ALGORITHM_HANDOFFS.md` as the living operational explanation.
Cover the entire active stack, but make IF/THEN behavior primary: vision acceptance and fallback,
measured-start safety gate, PathPlanner ownership, x=`3.3 m` spatial handoff, DriveToPose approach,
four-part settle entry, wider hold escape, timeout, chooser variants, and aiming as a separate branch.
Explicitly correct the example assumption: angle correction does not stop based on low angular speed
alone. Record current thresholds in both common and engineering units, distinguish baseline modes from
A/B experiments, add maintenance questions/change history, and link the guide from the docs index and
conceptual student guide. Do not change robot behavior.

# 2026-09-04 - `e974` spatial-handoff settling analysis and angular damping A/B

```text
The log is e974. The drive distance was 2.0 m left frame corner and 1.975 m on right frame corner. We
saw a settling time at least 0.5 seconds. Continue robot testing while documentation continues in a
parallel thread.
```

Implementation decision: parse the uploaded WPILOG directly and distinguish translation arrival from
full pose-and-velocity settlement. The run reached the 4 cm translation window 0.672 s after the
spatial handoff but took another 1.547 s to finish. Post-arrival yaw reached 3.46 degrees and
27.48 deg/s; the hold entered twice and exited once on the 12 deg/s angular escape. The Pigeon signal
was valid. Change only `PRECISION_ROTATION_VELOCITY_DAMPING` from 0.35 to 0.70 for the next controlled
A/B run. Preserve theta Kp, all tolerances, translation control, path constraints, handoff geometry,
and vision. Do not build, compile, simulate, or deploy.

# 2026-09-04 - `107d` rotation-damping validation

```text
The log is 107d. The drive distance was 2.045 m left frame corner and 2.045 m on right frame corner.
We saw almost no settling time.
```

Analysis decision: accept rotation damping `0.70`. The log confirmed only `0.45 deg / 3.68 deg/s`
post-arrival yaw versus `3.46 deg / 27.48 deg/s` in `e974`, and reduced DriveToPose time from 2.219 s
to 0.803 s. Keep the 0.18 m/s escape gate even though one 0.204 m/s translation sample caused a
one-cycle hold exit; it immediately re-entered and did not create visible settling. Request one
unchanged confirmation before closing this tuning stage. Do not change code for this result and do
not build, compile, simulate, or deploy.
