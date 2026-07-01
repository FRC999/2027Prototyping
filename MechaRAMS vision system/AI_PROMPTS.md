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
