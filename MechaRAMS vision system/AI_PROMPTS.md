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
