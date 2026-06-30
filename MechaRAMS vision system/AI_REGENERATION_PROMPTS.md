# AI Regeneration Prompts

Author: Claude (Opus 4.8) session, 2026-06-30
Purpose: Toward the goal of **AI-generated robot code** — these prompt templates, paired with the skills
in `VisionTestingAndCalibration/.codex/skills/` and `.claude/commands/`, let an AI agent regenerate or
extend each module to the best-in-breed spec. Each prompt assumes the agent first reads the skill and the
required docs it names.

## How to use

1. Open the agent in the `VisionTestingAndCalibration` project.
2. Paste the relevant prompt below.
3. The agent should read the named skill + docs, implement, then run
   `./gradlew.bat compileJava` and `./gradlew.bat test` before reporting.

Each module has a **skill** (the rules, in `.codex/skills/`) and a **prompt** (the task, below). The
skills are: `frc-project-bootstrap`, `frc-swerve-drivetrain`, `frc-vision-localization`,
`frc-trajectory-precision`, `frc-aiming`, `frc-simulation-and-testing`.

---

## Master playbook — regenerate the whole project from scratch (ordered)

Run these in order; each must compile + test before the next. This is the path toward fully
AI-generated robot code: supply the skills + these prompts, get the project.

```text
0. Read SESSION_STATE.md, ARCHITECTURE_AND_DEPLOYMENT.md, AGENTS.md, and INITIAL_PROMPT_REORGANIZED.md.
1. Use frc-project-bootstrap: create the WPILib 2026 GradleRIO project with Phoenix6 + AdvantageKit
   (+ @AutoLog processor) + PhotonLib + PathPlannerLib, Constants (2025 hardware + vision + auto + aim),
   Robot (LoggedRobot + AdvantageKit), and a RobotContainer skeleton. Wire JUnit. Compile.
2. Use frc-swerve-drivetrain: implement DriveSubsystem (CTRE SwerveDrivetrain, 250 Hz odometry,
   PathPlanner AutoBuilder, SysId, 5 ms sim thread, pose logging) and the manual drive command. Compile.
3. Use frc-vision-localization: implement the IO-layer vision (VisionIO/@AutoLog, VisionIOPhotonVision,
   VisionIOPhotonVisionSim, Vision) with the fpgaToCurrentTime consumer. Compile + test.
4. Use frc-trajectory-precision: implement DriveToPosePrecisionCommand (profiled + FF + settle + timeout
   + logging + handoff) and the VisionTest path/auto. Compile + test.
5. Use frc-aiming: implement AimingCalculator + AimAtGoalCommand + DriveAndAimCommand (configurable goal,
   shoot-on-move, no mechanism). Compile + test.
6. Use frc-simulation-and-testing: confirm the PhotonVision sim produces frames and all unit tests pass;
   build the AdvantageScope layout per ADVANTAGESCOPE_SETUP.md.
7. Bind everything in RobotContainer per ROBOT_CONTROLS.md; update SESSION_STATE.md.
Stop and ask if any hardware constant is unknown; mark unmeasured values provisional.
```

---

## Prompt: Vision localization (regenerate the IO-layer fusion)

```text
Use the frc-vision-localization skill. Build/maintain the AprilTag localization on the AdvantageKit
IO-layer: VisionIO (@AutoLog) + VisionIOPhotonVision + VisionIOPhotonVisionSim + a Vision subsystem that
validates, weights, timestamp-orders, and fuses observations via a VisionConsumer into CTRE's estimator.
Enforce every rule in the skill: fuse all unread frames; convert timestamps with
Utils.fpgaToCurrentTime; single-tag heading std dev = +Infinity; reject NaN/Inf/bad-Z/off-field/too-far/
ambiguous with logged RejectionReason enums; covariance = baseline*dist^2/tagCount*cameraFactor; ignore
vision early in auto; log accepted/rejected/tag poses + innovation. Keep the proven AdvantageKit
PhotonVision template shape (6328/1768). Add nothing game-specific. Compile and run the unit tests.
```

## Prompt: Precision driving + handoff

```text
Use the frc-trajectory-precision skill. Maintain DriveToPosePrecisionCommand as a profiled x/y/theta
controller with velocity feedforward, a settle gate, a hard safety timeout, and full logging (target /
measured / errors / settle / finished / timedOut). Provide handoffFrom(coarse, spatialCondition) that
runs a PathPlanner/Choreo path until the condition then finishes on this controller (6328 pattern). Keep
constraints conservative. A time-based path must never finish a precise move. Compile and run tests.
```

## Prompt: Chassis aiming (no mechanism)

```text
Use the frc-aiming skill. Maintain util/AimingCalculator (pure, unit-tested) computing the heading that
points the configured aim face at AimConstants.GOAL_POSITION, with a velocity-compensated shoot-on-move
lookahead (iterate a future pose with a modeled time-of-flight; lead is opposite the motion). Provide
AimAtGoalCommand (stationary, settle + timeout) and DriveAndAimCommand (driver translates, robot
auto-faces goal). Log target heading / error / distance / lead pose. NO turret, shooter, or game-piece
code. Compile and run AimingCalculatorTest.
```

## Prompt: Project bootstrap (scaffolding)

```text
Use the frc-project-bootstrap skill. Create/maintain the WPILib 2026 GradleRIO scaffolding: build.gradle
with Phoenix6 + AdvantageKit (including the akit-autolog annotation processor via the JsonSlurper version
block) + PhotonLib + PathPlannerLib + JUnit 5; Constants.java with OperatorConstants, SwerveConstants
(2025 CAN IDs/offsets/bus/Pigeon), VisionConstants, AutoConstants, AimConstants; Robot.java as a
LoggedRobot that starts AdvantageKit; and a RobotContainer that selects real/sim vision IO and wires the
fpgaToCurrentTime VisionConsumer. Confirm @AutoLog generates *AutoLogged. Compile and run tests.
```

## Prompt: Swerve drivetrain

```text
Use the frc-swerve-drivetrain skill. Implement DriveSubsystem extending CTRE SwerveDrivetrain from the
2025 module map, with 250 Hz odometry, PathPlanner AutoBuilder (passing CTRE wheel-force feedforwards),
the three SysId requests, a 5 ms simulation Notifier, and periodic logging of Drive/Pose + speeds +
module states. Expose getPose/getFieldRelativeSpeeds/driveFieldRelative/driveRobotRelative/stop. Add the
field-relative DriveManuallyCommand as the default command. Do not guess hardware constants. Compile/test.
```

## Prompt: Simulation + tests

```text
Use the frc-simulation-and-testing skill. Ensure VisionIOPhotonVisionSim renders frames via VisionSystemSim
+ PhotonCameraSim with OV9782-modeled properties, fed the true pose, selected in RobotBase.isSimulation().
Keep/extend headless tests: VisionPolicyTest (gates + dist^2/tagCount covariance + single-tag theta=Inf)
and AimingCalculatorTest (heading geometry + lead direction). ./gradlew.bat test must pass.
```

## Prompt: Add a new module to the best-in-breed spec (general)

```text
Read SESSION_STATE.md, CODEX_CODE_REVIEW_AND_GAP_ANALYSIS.md, and DESIGN_DECISIONS_AND_REJECTED_IDEAS.md.
Implement <MODULE>. Follow AGENTS.md. For every non-obvious choice, add an inline comment naming the
team and idea it came from (e.g. "Idea: 6328 LaunchCalculator ..."). Prefer measured constants + logged,
replayable behavior over tuning by impression. Update the relevant docs and SESSION_STATE.md. Verify with
./gradlew.bat compileJava and ./gradlew.bat test before reporting. If anything is ambiguous, stop and ask.
```

## Prompt: Hardware/architecture guardrails (always in effect)

```text
Constraints for all robot code: Orange Pi coprocessors only (no Mac mini). Image processing stays on the
Pi; final fusion + drivetrain control stay on the roboRIO. The 250 Hz figure is the roboRIO/CANivore
odometry thread, NOT a camera/Pi rate. Start with 2 cameras / 1 Pi; keep the code scalable to 4 cameras /
2 Pis. Do not guess CAN IDs, module offsets, camera transforms, gear ratios, or tag coordinates — mark
unmeasured values provisional.
```

---

## Notes for evolving this toward "all code AI-generated"

- Each skill encodes the *rules*; each prompt here is the *task*; the docs provide the *context*. As more
  modules are added, add a skill + a prompt template here so the module is regenerable.
- Keep the unit tests (`src/test/...`) — they are the machine-checkable definition of "correct" that lets
  an AI verify its own regeneration without a robot.
