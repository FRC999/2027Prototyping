# FRC AI Automation Master Source Guide

Prepared for Team 999 / MechaRAMS programming, vision, trajectory, and CAD automation planning.  
Version: consolidated portrait edition, June 27, 2026.  
This document consolidates the original AI source investigation, the expanded ranking/CAD document, the verified source guide, and the supplied vision/localization/trajectory research documents.

## Executive summary
- The best public FRC AI-agent materials are not necessarily from the top world-champion teams. The most directly reusable AI environment sources remain Team 360, Team 1868, Team 6238, Team 6391, Team 4050, Team 7660, and Team 8092.
- The best elite code, logging, replay, vision, trajectory, and CAD/process references are a different set: 6328, 1768, 5687, 3467, 125, 2910, 254, 1678, 4414, 1323, 6329, 4206, and 111/112.
- This version restores the large ranking table with 2026/recent strength and Championship/division context, keeps the detailed per-team analysis, adds non-team/community resource tables, and keeps the 2025/2026 Championship top-10 appendices.
- The earlier Nashoba issue is corrected: 1768 has a public 2026 repository and vision package, and it is now treated as a high-value New England source.
- For CAD, Onshape remains the recommended base. AI should be used as a parameter/check/review assistant wrapped around deterministic geometry calculators, FeatureScript, Onshape API, and human design review, not as an autonomous robot designer.

## Re-check / correction notes

- **Nashoba 1768 correction:** public repo: https://github.com/Nashoba-Robotics/2026NashobaRobotics; vision package: https://github.com/Nashoba-Robotics/2026NashobaRobotics/tree/main/src/main/java/frc/robot/subsystems/vision.
- **HighTide/4414:** the useful public material is binder/video/process/design; I still did not verify a current public 2025/2026 robot-code repo or AGENTS/CLAUDE file.
- **195 CyberKnights:** use the release thread and browser/manual access for self-hosted GitLab; this may require authentication or a normal browser session.

## How to read the tiers
- **A / A- Direct AI**: public AGENTS.md, CLAUDE.md, Claude commands, skills, or agent workflow materials worth adapting.
- **A Code / Infrastructure / Vision**: high-value robot code, logging, replay, localization, trajectory control, or CAD infrastructure.
- **A Performance / Design**: elite strategy/design/process material, often binder/videos rather than code.
- **B / Watch**: useful but lower priority, less complete, non-Java/different stack, or needs local clone/manual verification.

## Ranked source teams used or added

| Team | Name / Source | Category | 2026/recent strength | Championship/division context | Material / useful links | What Team 999 should mine | Tier |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 360 | The Revolution | AGENTS/CLAUDE + commands + robot code | 2026 official 40-20; Pacific Northwest district rank #6. | Attended Houston; not in the 2026 division top-10 qualification list checked. | RainMaker26 robot repo; AGENTS.md, CLAUDE.md, .claude commands, dev workflow, sim/test/static-analysis commands.<br>RainMaker26 repo; AGENTS.md; CLAUDE.md (+3 more in profile) | Team 999 AGENTS.md, CLAUDE.md, Claude commands, Gradle build/sim/test/static-analysis workflow, mentor-reviewed AI code process. | A - Direct AI |
| 1868 | Space Cookies | Claude rules, agents, commands, contexts, skills | 2026 official 30-29; California district rank #36; Engineering Inspiration at Silicon Valley. | Attended Archimedes; not in top-10 qualification list. | Shared Claude Code config: rules, agents, commands, contexts, skills for FRC robot development.<br>frc-claude-config repo; rules; agents (+4 more in profile) | Multi-agent Claude setup: code reviewer, safety auditor, subsystem architect, vision reviewer, FRC skills, install scripts. | A - Direct AI environment |
| 6238 | Popcorn Penguins | CLAUDE.md + AdvantageKit + sim/replay + CAD/manufacturing tool | 2026 official 38-21; California district rank #16; California Northern District Championship winner. | Attended Houston; not in the 2026 division top-10 qualification lists checked. | 2026-Robot CLAUDE.md and .claude-style guidance; AdvantageKit IO/sim/replay pattern.<br>2026-Robot repo; CLAUDE.md; PenguinCAM repo (+1 more in profile) | AdvantageKit IO/replay guidance, test commands, MapleSim-style sim ideas, PenguinCAM Onshape-to-CNC workflow. | A- - Direct AI + serious architecture |
| 6391 | Bearbotics | CLAUDE.md + rebuilt robot code | 2026 official 26-14; Regional Pool rank #185; St. Louis Regional rank #2, Regional Finalist, Autonomous Award. | Attended Houston; not in the 2026 top-10 qualification lists checked. | 2026 rebuilt robot repo with CLAUDE.md; built on AdvantageKit-style architecture.<br>2026-6391-Rebuilt repo; CLAUDE.md | AdvantageKit IO pattern, real/sim/replay implementations, Phoenix 6 conventions, log-analysis thresholds. | A- - Direct Claude + AdvantageKit |
| 4050 | Biohazard | AGENTS.md + CLAUDE.md + devcontainer + docs | 2026 official 4-9; Regional Pool rank #683. | Did not appear in 2026 Championship top-10 division lists. | 2026-rebuilt and frc-java-template style source; AI/developer-environment materials surfaced in prior pass.<br>2026-rebuilt repo; AGENTS.md; CLAUDE.md (+1 more in profile) | Repository skeleton, devcontainer/docs/scripts, Epilogue/DataLog notes, command/subsystem conventions. | B+ - Template/process |
| 7660 | Byting Irish | AGENTS.md + robot code | 2026 official 15-15; FIM rank #240. | Did not appear in 2026 Championship top-10 division lists. | 2026 robot repo with AGENTS.md and simulation/replay notes.<br>robot-2026 repo; AGENTS.md | Compact agent instructions, WPILib sanity checks, build/deploy/log workflow. | B+ - Direct AI example |
| 8092 | GOAT | AGENTS.md, CLAUDE.md, ROBOT.md | 2026 official 3-6; Regional Pool rank #936; Avrasya Regional Judges Award. | Did not appear in 2026 Championship top-10 division lists. | Robot-2026 / 8092-2026 repos with CLAUDE.md and Claude Code usage notes.<br>8092-2026 repo; AGENTS.md; CLAUDE.md (+1 more in profile) | Robot summary file, Claude conventions, possible multilingual organization ideas. | B - Direct AI-use example |
| 846 | Funky Monkeys | Robot code / possible AGENTS.md | 2026 public robot-code candidate found during re-check; competition ranking/context was not verified in this pass. | No Championship top-10 context verified; clone locally before treating as a primary source. | Robot code / possible AGENTS.md<br>gibbon repo; AGENTS.md candidate | Additional codebase to clone locally and inspect for AI instructions and architecture patterns. | B - Additional code/agent candidate |
| 6328 | Mechanical Advantage | RobotCode2026Public + AdvantageKit + AdvantageScope + Onshape4FRC | 2026 official 51-13; overall 75-20; New England district rank #5; Minuteman and Waterbury winners; Impact Award at Waterbury and New England DCMP. | 2026 Hopper rank #20, not top-10; 2025 top-10 not found in checked division lists. | RobotCode2026Public, AdvantageKit, Onshape4FRC resources.<br>RobotCode2026Public; Vision folder; RobotState.java (+6 more in profile) | Logging/replay architecture, IO layers, vision fusion, RobotState estimator, DriveToPose, trajectory handoff, CAD education. | A - Infrastructure/code/CAD |
| 1768 | Nashoba Robotics | 2026 robot code + PhotonVision + AdvantageKit + Choreo/accuracy gating | 2026 official 70-17; overall 96-27; New England district rank #1; URI, WPI, Burns, and New England DCMP winners. | 2026 Galileo qualification rank #5; 2025 Hopper qualification rank #1. | Public 2026 repo with PhotonVision/AdvantageKit-style vision package; Choreo/trajectory accuracy patterns; correction to earlier miss.<br>2026NashobaRobotics repo; Vision folder; Vision.java (+7 more in profile) | PhotonVision with 3 cameras, AdvantageKit vision template, Vision.java/VisionConstants.java, Choreo/AutoFactory, settle-gated trajectory ideas. | A - New England code/vision/trajectory |
| 5687 | The Outliers | C++ robot code, Limelight/custom fusion, PhotonVision sim references | Supplied research identifies 5687 as 2026 New England #2; public C++ repo found for vision/fusion reference. | Important New England benchmark from supplied research; no top-10 Championship appendix entry carried over here. | Public C++ repo; Limelight/custom fusion and PhotonVision sim references from supplied research.<br>2026-Robot repo; src/main; include folder (+2 more in profile) | OdometryThread/PoseEstimator architecture, Limelight measurements, custom fusion on roboRIO. | A- - New England vision/fusion |
| 3467 | Windham Windup | ThriftyCam/c2 custom coprocessor + AdvantageKit + custom pose estimator | Supplied research identifies 3467 as 2026 New England #3 and a 51-0 record source; public code and OA build blog found. | Important New England benchmark from supplied research; no top-10 Championship appendix entry carried over here. | Public code/OA source; ThriftyCam/c2 coprocessor, custom pose estimator, DriveToPose, intrinsics-in-code.<br>Skip-5.16-Perry repo; Vision folder; posestimator library (+2 more in profile) | Per-camera intrinsics in code, custom posestimator, skid rejection, n-sigma innovation gate, reusable DriveToPose tolerance pattern. | A- - Vision/fusion/DriveToPose |
|  | 125 NUTRONs | Simple Limelight MT2 + AdvantageKit + CAD/design releases | Top NE program; 2025 nu25 code reviewed in uploaded docs; 2026 reveal/materials found. | No Championship context verified in this pass. | Simple Limelight MT2 + AdvantageKit + CAD/design releases<br>nu25 GitLab repo; 2024 code/CAD release; 2026 reveal (+2 more in profile) | A deliberately simple but winning vision pattern: MT2 translation with effectively ignored theta; dist² weighting; strong logging. | A- - NE code/design reference |
| 2910 | Jack in the Bot | Elite robot code, CAD/tech binder, 2024 turret hybrid reference | 2026 official 53-23; PNW rank #3; PNW DCMP winner; Newton Division winner. | 2026 Newton qualification rank #1; 2025 Newton qualification rank #3. | 2026/2025 public robot-code repos and 2024 turret/control references.<br>2026 robot code; 2024 robot code; Team 2910 GitHub org (+4 more in profile) | Code quality benchmark, turret/global+local aiming ideas from 2024, CAD/tech binder release process. | A - Elite code/CAD/performance |
| 254 | The Cheesy Poofs | Robot code + technical binder + Q&A | 2026 official 65-6; California district rank #2; Silicon Valley, Central Valley, CA Northern, and Curie Division winners. | 2026 Curie qualification rank #4; 2025 Milstein qualification rank #4. | FRC-2025-Public and elite robot-code references.<br>Team254 GitHub org; FRC-2025-Public; FRC-2024-Public (+3 more in profile) | Elite code structure, simulation/autonomous/controls ideas, binder quality, code Q&A. | A - Elite code/binder benchmark |
| 1678 | Citrus Circuits | Public robot code, goal tracker, global/local split | 2026 Milstein qualification rank #2; 2025 Daly qualification rank #3; elite Citrus public code history is useful for global/local aiming patterns. | 2026 Milstein qualification rank #2; 2025 Daly qualification rank #3. | 2026 public code + 2024 goal tracker/global pose source for local/global split.<br>C2026-Public; C2024-Public; frc1678 GitHub org (+1 more in profile) | Separate goal tracker vs global pose architecture; 2026 CTRE GeneratedDrivetrain + vision as a modern reference. | A- - Elite historical code/local tracker |
| 6995 | NOMAD | 2026 robot code, Choreo AutoFactory, turret from global pose | Useful 2026 code-pattern source; competitive benchmark lower priority than 6328/1768/2910, but valuable Choreo/turret example. | Not in the Championship top-10 appendices used here; valuable as a live 2026 Choreo AutoFactory/alliance-flip and pose-fed turret code example. | 2026 robot code; Choreo AutoFactory/alliance flip proof; pose-fed turret.<br>Robot-2026 repo; TBA 6995 | Real 2026 proof of Choreo alliance flipping, trigger-based autos, and turret aimed from pose supplier. | B+ - Turret/Choreo example |
| 7407 | Choate Wired Boars | RobotPy, PhotonVision, Open Alliance | 2025 Milstein qualification rank #3; CT/nearby public RobotPy/PhotonVision source and Open Alliance reference. | 2025 Milstein qualification rank #3; 2026 Championship context not separately verified in this pass. | RobotPy, PhotonVision, Open Alliance<br>7407-DriveCode-Rebuilt repo; Choate Robotics GitHub org; 2026 OA build thread (+1 more in profile) | Python/RobotPy PhotonVision approach, OA design updates, CAD/code links from thread. | B+ - RobotPy/PhotonVision/OA |
| 195 | CyberKnights | CAD/code/documentation release, self-hosted GitLab, ROS/Mac Mini stack | Connecticut powerhouse; supplied research notes custom ROS/Mac Mini stack and self-hosted GitLab/release-thread access limitations. | Manual/browser check item; self-hosted GitLab may require auth or browser JS. Use release thread to enumerate exact code/CAD assets. | Release thread and self-hosted GitLab/manual source; custom ROS/Mac Mini stack.<br>2026 CAD, code, documentation release; GitHub org pointing to GitLab; Self-hosted GitLab group (+3 more in profile) | High-performance CT process, CAD/release materials, custom ROS/coproc ideas if accessible. | A- - CT performance/CAD/code release, manual access |
| 4414 | HighTide | Technical binder, videos, discussion, design/process | 2026 official 70-2; California district rank #1; Ventura, Orange County, CA Southern, Daly Division, and Houston Championship winners. | 2026 Daly qualification rank #1; 2025 Johnson qualification rank #6. | World-champion performance benchmark; public source/org and Chief Delphi references checked.<br>Team4414 GitHub org; 2026 tech binder CD thread; 2026 technical binder site (+5 more in profile) | World-champion strategy, design, CAD/process presentation, manufacturing tooling ideas, technical binder quality. | A - World champion design/process benchmark |
| 1323 | MadTown Robotics | Technical binder/Q&A, website/wiki, reveal/discussion | 2026 official 69-4; California district rank #4; San Francisco, Central Valley, CA Northern, Daly Division, and Houston Championship winners. | 2026 Daly qualification rank #2; 2025 Newton qualification rank #1. | World-champion performance benchmark and public-org/source check.<br>team1323 GitHub org; Team website; Team wiki (+4 more in profile) | Elite strategy, mechanism packaging, printed/manufactured part practices, technical-binder expectations. | A - World champion performance/mechanism benchmark |
| 6329 | Bucks' Wrath | Robot reveal and mechanism discussion | 2026 official 61-15-1; New England district rank #4; Pine Tree, Newsom, and Hopper Division winners. | 2026 Hopper qualification rank #3; 2025 Curie qualification rank #8. | New England top-team benchmarking and division-finalist/top-10 coverage.<br>2026 robot reveal: ROMAN; Team website; TBA 6329 (+1 more in profile) | Spindexter/drum/feed mechanism packaging ideas, local NE performance benchmark. | A- - NE performance/mechanism benchmark |
| 4206 | Robo Vikes | GitHub org, code/org resources | 2026 official 47-28; Texas district rank #11; Newton Division winner. | Newton Division winner, but not in 2026 Newton top-10 qualification ranks. | Public GitHub/org reference from expanded top-team pass.<br>frc4206 GitHub org; 26-c-0002 repo; battleaid repo (+1 more in profile) | Top-playoff reference, battleaid utilities/guides, possible 2026 second robot code. | A- - Einstein finalist / org to watch |
| 111/112 | WildStang Robotics Program | Open Alliance mechanism deep dive | Team 111 2026 official 30-15; Regional Pool rank #115; Midwest Regional finalist and Autonomous Award; Illinois State Championship winner. | Team 111 attended Galileo; not top-10 qualification list. | 2026 Open Alliance dye-rotor deep dive and mechanism math/risks.<br>WildStang 2026 build blog page 4; Build blog main thread; TBA 111 2026 (+1 more in profile) | Dye rotor geometry, overfeed/jam risks, design tradeoffs, swerve-module clearance considerations. | A - Mechanism/CAD concept source |
| 2363 | Triple Helix | FRC AI/MCP tooling source | 2026 official 15-15; FIRST Chesapeake rank #59. | Did not appear in 2026 Championship top-10 division lists. | wpilog-mcp: MCP server for WPILib / AdvantageKit / WPILOG log querying.<br>https://github.com/TripleHelixProgramming/wpilog-mcp; https://www.thebluealliance.com/team/2363/2026 | Very useful for AI debugging/replay evidence loops even though it is tooling, not a full robot AI environment. | A for tooling |
| 488 | XBot | Reusable robot-code / documentation source | Most recent verified prior page: 2025 official 26-20; PNW rank #28; Sammamish finalist. 2026 page appeared incomplete/stale in prior pass. | No 2026 top-10 Championship division result verified. | SeriouslyCommonLib, Programming-Docs AGENTS.md, docs/style patterns.<br>https://github.com/Team488/SeriouslyCommonLib; https://github.com/Team488/Programming-Docs/blob/main/AGENTS.md; https://www.thebluealliance.com/team/488/2026 | Good Java/WPILib library and documentation pattern source; not an elite 2026 competitive benchmark. | B+ |
| 834 | SparTechs | AI/strategy-board source / watch item | 2026 official 11-18-1; Mid-Atlantic rank #94. | No 2026 top-10 Championship division result verified. | StrategyBoard2026-style AI guidance surfaced in expanded search; exact repo should be locally rechecked before use.<br>https://github.com/team834; https://www.thebluealliance.com/team/834/2026 | Potentially useful for non-robot-code AI workflow ideas; lower priority than 360/6238/1868. | C+ |

## Non-team / community resources used

| Resource | Type | What it provides | Ranking note | Links |
| --- | --- | --- | --- | --- |
| Shenzhen Robotics Alliance / MapleSim | Simulation library | maple-sim FRC Java simulation with physics engines and CLAUDE.md guidance. | Not an FRC team ranking item. | https://github.com/Shenzhen-Robotics-Alliance/maple-sim |
| wpilib-agent-tools | AI/robotics harness | Sandbox-first Codex/Claude/Cursor harness for WPILib simulation, NT4 recording, WPILOG analysis, and patch review. | Individual/community tool, not a team. | https://github.com/edanliahovetsky/wpilib-agent-tools |
| FRCDesignApp / FRCDesignLib | Onshape/FRC CAD library tooling | Plugin/app and component library for inserting COTS FRC parts in Onshape. | Community CAD infrastructure, not a team. | https://frcdesign.org/resources/frcdesignlib/<br>https://www.chiefdelphi.com/t/introducing-the-new-frcdesignapp/507335 |
| Onshape4FRC | FRC Onshape learning/resource site | FRC CAD teaching resources, calculators, COTS library notes, FeatureScript walkthroughs. | Built by FRC 6328 mentors, but the site itself is a resource, not a competition result. | https://onshape4frc.com/ |
| Onshape AI Advisor | Native Onshape AI help | AI documentation/best-practice assistant inside the Onshape workflow. | CAD-product feature, not a team. | https://www.onshape.com/en/features/ai-advisor |

## Detailed source profiles

### 360 The Revolution

**Priority:** A - Direct AI  
**Category:** AGENTS/CLAUDE + commands + robot code  
**Context:** Strong PNW team; most directly reusable public FRC AI-agent robot repo found.  
**Main Team 999 use:** Team 999 AGENTS.md, CLAUDE.md, Claude commands, Gradle build/sim/test/static-analysis workflow, mentor-reviewed AI code process.  
**Caution:** Use as AI-process source, not as world-champion performance benchmark.

**Direct links:**
- [RainMaker26 repo](https://github.com/FRCTeam360/RainMaker26) - https://github.com/FRCTeam360/RainMaker26
- [AGENTS.md](https://github.com/FRCTeam360/RainMaker26/blob/main/AGENTS.md) - https://github.com/FRCTeam360/RainMaker26/blob/main/AGENTS.md
- [CLAUDE.md](https://github.com/FRCTeam360/RainMaker26/blob/main/CLAUDE.md) - https://github.com/FRCTeam360/RainMaker26/blob/main/CLAUDE.md
- [.claude commands](https://github.com/FRCTeam360/RainMaker26/tree/main/.claude/commands) - https://github.com/FRCTeam360/RainMaker26/tree/main/.claude/commands
- [FSM command](https://github.com/FRCTeam360/RainMaker26/blob/main/.claude/commands/fsm.md) - https://github.com/FRCTeam360/RainMaker26/blob/main/.claude/commands/fsm.md
- [Open Alliance thread](https://www.chiefdelphi.com/t/frc-360-the-revolution-2026-build-thread-open-alliance/510290) - https://www.chiefdelphi.com/t/frc-360-the-revolution-2026-build-thread-open-alliance/510290

**Analysis / what this gives us:**
- This is the cleanest direct public example of a robot-code repo telling AI assistants what to do. The README points assistants to AGENTS.md and CLAUDE.md, and the repo includes a .claude command structure.
- Best use for Team 999: copy the pattern, not the hardware constants. Use one canonical agent contract, then make CLAUDE.md either summarize or point to it.
- Create Team 999 commands modeled on this idea: fsm-map, sim-smoke-test, vision-pose-review, trajectory-auto-review, and safety-audit.

### 1868 Space Cookies / FRC Claude Config

**Priority:** A - Direct AI environment  
**Category:** Claude rules, agents, commands, contexts, skills  
**Context:** Public Claude Code configuration package; more important as AI environment artifact than as robot code.  
**Main Team 999 use:** Multi-agent Claude setup: code reviewer, safety auditor, subsystem architect, vision reviewer, FRC skills, install scripts.  
**Caution:** Not a robot-code release; use for AI workspace structure.

**Direct links:**
- [frc-claude-config repo](https://github.com/Feramirr/frc-claude-config) - https://github.com/Feramirr/frc-claude-config
- [rules](https://github.com/Feramirr/frc-claude-config/tree/main/rules) - https://github.com/Feramirr/frc-claude-config/tree/main/rules
- [agents](https://github.com/Feramirr/frc-claude-config/tree/main/agents) - https://github.com/Feramirr/frc-claude-config/tree/main/agents
- [commands](https://github.com/Feramirr/frc-claude-config/tree/main/commands) - https://github.com/Feramirr/frc-claude-config/tree/main/commands
- [skills](https://github.com/Feramirr/frc-claude-config/tree/main/skills) - https://github.com/Feramirr/frc-claude-config/tree/main/skills
- [install.ps1](https://github.com/Feramirr/frc-claude-config/blob/main/install.ps1) - https://github.com/Feramirr/frc-claude-config/blob/main/install.ps1
- [install.sh](https://github.com/Feramirr/frc-claude-config/blob/main/install.sh) - https://github.com/Feramirr/frc-claude-config/blob/main/install.sh

**Analysis / what this gives us:**
- This is the best public example of packaging Claude Code as a repeatable FRC development environment rather than as a one-off prompt.
- Team 999 should adapt the directory structure and create its own agents for WPILib/CTRE review, FRC safety review, subsystem design, vision review, and pre-merge checking.
- Use the install scripts only as a model. Team 999 should own a local copy because Claude/Codex tools, plugin names, and paths change.

### 6238 Popcorn Penguins

**Priority:** A- - Direct AI + serious architecture  
**Category:** CLAUDE.md + AdvantageKit + sim/replay + CAD/manufacturing tool  
**Context:** 2026 CA district #16 in prior pass; stronger competitive context than many direct AI-template repos.  
**Main Team 999 use:** AdvantageKit IO/replay guidance, test commands, MapleSim-style sim ideas, PenguinCAM Onshape-to-CNC workflow.  
**Caution:** Good architecture source; do not copy hardware assumptions.

**Direct links:**
- [2026-Robot repo](https://github.com/6238/2026-Robot) - https://github.com/6238/2026-Robot
- [CLAUDE.md](https://github.com/6238/2026-Robot/blob/main/CLAUDE.md) - https://github.com/6238/2026-Robot/blob/main/CLAUDE.md
- [PenguinCAM repo](https://github.com/6238/PenguinCAM) - https://github.com/6238/PenguinCAM
- [PenguinCAM app](https://penguincam.popcornpenguins.com) - https://penguincam.popcornpenguins.com

**Analysis / what this gives us:**
- CLAUDE.md is useful because it tells the assistant how to build, test, replay AdvantageKit logs, and respect subsystem/IO architecture.
- PenguinCAM is especially relevant to the CAD weakness: it shows a path from Onshape CAD data toward manufacturing workflow automation.
- Use this as a bridge between programming and CAD/manufacturing automation.

### 6391 Bearbotics

**Priority:** A- - Direct Claude + AdvantageKit  
**Category:** CLAUDE.md + rebuilt robot code  
**Context:** Useful public FRC Claude guidance around AdvantageKit and Phoenix 6; not an elite performance benchmark.  
**Main Team 999 use:** AdvantageKit IO pattern, real/sim/replay implementations, Phoenix 6 conventions, log-analysis thresholds.  
**Caution:** Use as implementation/review reference; not as final strategy/performance standard.

**Direct links:**
- [2026-6391-Rebuilt repo](https://github.com/6391-Ursuline-Bearbotics/2026-6391-Rebuilt) - https://github.com/6391-Ursuline-Bearbotics/2026-6391-Rebuilt
- [CLAUDE.md](https://github.com/6391-Ursuline-Bearbotics/2026-6391-Rebuilt/blob/main/CLAUDE.md) - https://github.com/6391-Ursuline-Bearbotics/2026-6391-Rebuilt/blob/main/CLAUDE.md

**Analysis / what this gives us:**
- Good example of a more detailed Claude memory for AdvantageKit-style robot code.
- Use to create Team 999 docs/ai/simulation-contract.md and docs/ai/robot-contract.md rules around real/sim/replay split.
- Useful for students because it is detailed but more approachable than opaque elite codebases.

### 4050 Biohazard

**Priority:** B+ - Template/process  
**Category:** AGENTS.md + CLAUDE.md + devcontainer + docs  
**Context:** Lower competitive benchmark, but public AGENTS.md is substantial and practical.  
**Main Team 999 use:** Repository skeleton, devcontainer/docs/scripts, Epilogue/DataLog notes, command/subsystem conventions.  
**Caution:** Template value higher than performance value.

**Direct links:**
- [2026-rebuilt repo](https://github.com/Team4050/2026-rebuilt) - https://github.com/Team4050/2026-rebuilt
- [AGENTS.md](https://github.com/Team4050/2026-rebuilt/blob/main/AGENTS.md) - https://github.com/Team4050/2026-rebuilt/blob/main/AGENTS.md
- [CLAUDE.md](https://github.com/Team4050/2026-rebuilt/blob/main/CLAUDE.md) - https://github.com/Team4050/2026-rebuilt/blob/main/CLAUDE.md
- [frc-java-template](https://github.com/Team4050/frc-java-template) - https://github.com/Team4050/frc-java-template

**Analysis / what this gives us:**
- AGENTS.md has practical build, simulation, lifecycle, subsystem, command, telemetry, constants, and CAN-ID guidance.
- Useful for Team 999 as a “what should an agent know about this repo?” checklist.
- Good source for student-friendly rules, especially around command factories, units, periodic telemetry, and not overloading mode callbacks.

### 7660 Byting Irish

**Priority:** B+ - Direct AI example  
**Category:** AGENTS.md + robot code  
**Context:** Moderate competitive result; useful because it has public AGENTS.md.  
**Main Team 999 use:** Compact agent instructions, WPILib sanity checks, build/deploy/log workflow.  
**Caution:** Pattern sample, not elite architecture standard.

**Direct links:**
- [robot-2026 repo](https://github.com/FRC7660/robot-2026) - https://github.com/FRC7660/robot-2026
- [AGENTS.md](https://github.com/FRC7660/robot-2026/blob/main/AGENTS.md) - https://github.com/FRC7660/robot-2026/blob/main/AGENTS.md

**Analysis / what this gives us:**
- Keep as a compact example of how small AGENTS.md files can still help.
- Useful for simple build/deploy and log instructions; lower priority than Team 360/6238/6391.

### 8092 GOAT

**Priority:** B - Direct AI-use example  
**Category:** AGENTS.md, CLAUDE.md, ROBOT.md  
**Context:** Lower competitive benchmark but public Claude/AGENTS-style materials.  
**Main Team 999 use:** Robot summary file, Claude conventions, possible multilingual organization ideas.  
**Caution:** Some content is Turkish; use for organization ideas only.

**Direct links:**
- [8092-2026 repo](https://github.com/GOAT-8092/8092-2026) - https://github.com/GOAT-8092/8092-2026
- [AGENTS.md](https://github.com/GOAT-8092/8092-2026/blob/main/AGENTS.md) - https://github.com/GOAT-8092/8092-2026/blob/main/AGENTS.md
- [CLAUDE.md](https://github.com/GOAT-8092/8092-2026/blob/main/CLAUDE.md) - https://github.com/GOAT-8092/8092-2026/blob/main/CLAUDE.md
- [ROBOT.md](https://github.com/GOAT-8092/8092-2026/blob/main/ROBOT.md) - https://github.com/GOAT-8092/8092-2026/blob/main/ROBOT.md

**Analysis / what this gives us:**
- Useful mostly as an example that ROBOT.md or similar robot-truth files can help AI understand mechanisms and hardware.
- Lower priority than the other direct AI sources.

### 846 Funky Monkeys / Gibbon

**Priority:** B - Additional code/agent candidate  
**Category:** Robot code / possible AGENTS.md  
**Context:** 2026 robot codebase found during re-check; AGENTS link was not fully opened due a GitHub/web error, so treat cautiously.  
**Main Team 999 use:** Additional codebase to clone locally and inspect for AI instructions and architecture patterns.  
**Caution:** Do not rely on as primary until locally cloned/inspected.

**Direct links:**
- [gibbon repo](https://github.com/Team846/gibbon) - https://github.com/Team846/gibbon
- [AGENTS.md candidate](https://github.com/Team846/gibbon/blob/main/AGENTS.md) - https://github.com/Team846/gibbon/blob/main/AGENTS.md

**Analysis / what this gives us:**
- The repo is public and worth cloning in a later local pass. I would not make it a primary source until direct raw AGENTS.md content is verified.

### 6328 Mechanical Advantage

**Priority:** A - Infrastructure/code/CAD  
**Category:** RobotCode2026Public + AdvantageKit + AdvantageScope + Onshape4FRC  
**Context:** New England elite; public 2026 code; top AdvantageKit/AdvantageScope/Onshape4FRC source.  
**Main Team 999 use:** Logging/replay architecture, IO layers, vision fusion, RobotState estimator, DriveToPose, trajectory handoff, CAD education.  
**Caution:** No public AGENTS.md/CLAUDE.md found; value is infrastructure/architecture, not AI contract.

**Direct links:**
- [RobotCode2026Public](https://github.com/Mechanical-Advantage/RobotCode2026Public) - https://github.com/Mechanical-Advantage/RobotCode2026Public
- [Vision folder](https://github.com/Mechanical-Advantage/RobotCode2026Public/tree/main/src/main/java/org/littletonrobotics/frc2026/subsystems/vision) - https://github.com/Mechanical-Advantage/RobotCode2026Public/tree/main/src/main/java/org/littletonrobotics/frc2026/subsystems/vision
- [RobotState.java](https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/RobotState.java) - https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/RobotState.java
- [DriveToPose.java](https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/commands/DriveToPose.java) - https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/commands/DriveToPose.java
- [DriveTrajectory.java](https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/commands/DriveTrajectory.java) - https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/commands/DriveTrajectory.java
- [2026 OA build thread](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2026-build-thread/509595) - https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2026-build-thread/509595
- [AdvantageKit](https://github.com/Mechanical-Advantage/AdvantageKit) - https://github.com/Mechanical-Advantage/AdvantageKit
- [AdvantageScope](https://github.com/Mechanical-Advantage/AdvantageScope) - https://github.com/Mechanical-Advantage/AdvantageScope
- [Onshape4FRC](https://onshape4frc.com/) - https://onshape4frc.com/

**Analysis / what this gives us:**
- Use this as the center of Team 999’s evidence loop: log every input, replay it, and make AI prove changes against logs/simulation.
- Their RobotCode2026Public README explicitly points to robot code, technical binder, OA thread, setup, and build/deploy/sim commands.
- The attached localization/trajectory docs identify 6328 as the key source for multi-camera fusion, global-pose aiming, and DriveToPose handoff patterns.

### 1768 Nashoba Robotics

**Priority:** A - New England code/vision/trajectory  
**Category:** 2026 robot code + PhotonVision + AdvantageKit + Choreo/accuracy gating  
**Context:** 2026 New England district #1; public 2026 repo verified; this corrects the previous document.  
**Main Team 999 use:** PhotonVision with 3 cameras, AdvantageKit vision template, Vision.java/VisionConstants.java, Choreo/AutoFactory, settle-gated trajectory ideas.  
**Caution:** No public AGENTS.md/CLAUDE.md found; use for vision/trajectory architecture, not AI contract.

**Direct links:**
- [2026NashobaRobotics repo](https://github.com/Nashoba-Robotics/2026NashobaRobotics) - https://github.com/Nashoba-Robotics/2026NashobaRobotics
- [Vision folder](https://github.com/Nashoba-Robotics/2026NashobaRobotics/tree/main/src/main/java/frc/robot/subsystems/vision) - https://github.com/Nashoba-Robotics/2026NashobaRobotics/tree/main/src/main/java/frc/robot/subsystems/vision
- [Vision.java](https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/Vision.java) - https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/Vision.java
- [VisionConstants.java](https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/VisionConstants.java) - https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/VisionConstants.java
- [VisionIOPhotonVision.java](https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/VisionIOPhotonVision.java) - https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/VisionIOPhotonVision.java
- [VisionIOPhotonVisionSim.java](https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/VisionIOPhotonVisionSim.java) - https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/VisionIOPhotonVisionSim.java
- [RobotContainer.java](https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/RobotContainer.java) - https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/RobotContainer.java
- [Nashoba GitHub org](https://github.com/Nashoba-Robotics) - https://github.com/Nashoba-Robotics
- [TBA 1768 2026](https://www.thebluealliance.com/team/1768/2026) - https://www.thebluealliance.com/team/1768/2026
- [Reveal thread](https://www.chiefdelphi.com/t/team-1768-nashoba-robotics-2026-robot-reveal-nightshift/519386) - https://www.chiefdelphi.com/t/team-1768-nashoba-robotics-2026-robot-reveal-nightshift/519386

**Analysis / what this gives us:**
- Correction: the 2026 repo exists and is directly useful. It includes a vision package with Vision.java, VisionConstants.java, VisionIO, real PhotonVision IO, and PhotonVision simulation IO.
- The vision code follows the AdvantageKit template pattern: process inputs per camera, reject bad observations, compute std devs from distance/tag count and per-camera factors, and log accepted/rejected poses.
- The uploaded trajectory analysis calls out Nashoba’s position-and-settle-gated trajectory completion as one of the cleanest “do not finish just because time elapsed” patterns.

### 5687 The Outliers

**Priority:** A- - New England vision/fusion  
**Category:** C++ robot code, Limelight/custom fusion, PhotonVision sim references  
**Context:** 2026 New England #2 in uploaded research; public C++ repo verified.  
**Main Team 999 use:** OdometryThread/PoseEstimator architecture, Limelight measurements, custom fusion on roboRIO.  
**Caution:** C++ stack; deeper local clone needed for precise file-by-file extraction.

**Direct links:**
- [2026-Robot repo](https://github.com/frc5687/2026-Robot) - https://github.com/frc5687/2026-Robot
- [src/main](https://github.com/frc5687/2026-Robot/tree/main/src/main) - https://github.com/frc5687/2026-Robot/tree/main/src/main
- [include folder](https://github.com/frc5687/2026-Robot/tree/main/src/main/include) - https://github.com/frc5687/2026-Robot/tree/main/src/main/include
- [cpp folder](https://github.com/frc5687/2026-Robot/tree/main/src/main/cpp) - https://github.com/frc5687/2026-Robot/tree/main/src/main/cpp
- [TBA 5687](https://www.thebluealliance.com/team/5687/2026) - https://www.thebluealliance.com/team/5687/2026

**Analysis / what this gives us:**
- Useful because it shows that Limelight hardware can still perform at a top NE level when fusion and code architecture are good.
- The uploaded research log says 5687 uses its own OdometryThread/PoseEstimator classes feeding Limelight measurements; image processing stays on coprocessor and fusion runs on roboRIO.

### 3467 Windham Windup

**Priority:** A- - Vision/fusion/DriveToPose  
**Category:** ThriftyCam/c2 custom coprocessor + AdvantageKit + custom pose estimator  
**Context:** 2026 New England #3 in uploaded research; public code archived; strong custom vision/fusion patterns.  
**Main Team 999 use:** Per-camera intrinsics in code, custom posestimator, skid rejection, n-sigma innovation gate, reusable DriveToPose tolerance pattern.  
**Caution:** GPLv3 code/license considerations; use ideas carefully and respect license.

**Direct links:**
- [Skip-5.16-Perry repo](https://github.com/WHS-FRC-3467/Skip-5.16-Perry) - https://github.com/WHS-FRC-3467/Skip-5.16-Perry
- [Vision folder](https://github.com/WHS-FRC-3467/Skip-5.16-Perry/tree/master/src/main/java/frc/robot/subsystems/vision) - https://github.com/WHS-FRC-3467/Skip-5.16-Perry/tree/master/src/main/java/frc/robot/subsystems/vision
- [posestimator library](https://github.com/WHS-FRC-3467/Skip-5.16-Perry/tree/master/src/main/java/frc/lib/posestimator) - https://github.com/WHS-FRC-3467/Skip-5.16-Perry/tree/master/src/main/java/frc/lib/posestimator
- [Open Alliance build blog](https://www.chiefdelphi.com/t/3467-windham-windup-2026-build-blog/508474) - https://www.chiefdelphi.com/t/3467-windham-windup-2026-build-blog/508474
- [TBA 3467 2026](https://www.thebluealliance.com/team/3467/2026) - https://www.thebluealliance.com/team/3467/2026

**Analysis / what this gives us:**
- The uploaded research log highlights 3467 as one of the best custom-estimator references: per-wheel skid rejection, uncertainty inflation, statistical innovation gating, and logged rejection reasons.
- Use as a more advanced source after Team 999 has basic AdvantageKit/logging/replay working.
- Also useful for camera hardware/intrinsics-in-code and “do not trust generic factory calibration blindly” discipline.

### 125 NUTRONs

**Priority:** A- - NE code/design reference  
**Category:** Simple Limelight MT2 + AdvantageKit + CAD/design releases  
**Context:** Top NE program; 2025 nu25 code reviewed in uploaded docs; 2026 reveal/materials found.  
**Main Team 999 use:** A deliberately simple but winning vision pattern: MT2 translation with effectively ignored theta; dist² weighting; strong logging.  
**Caution:** nu25 is 2025 code; check 2026 code if/when public.

**Direct links:**
- [nu25 GitLab repo](https://gitlab.com/nutrons125/nu25) - https://gitlab.com/nutrons125/nu25
- [2024 code/CAD release](https://www.chiefdelphi.com/t/frc125-the-nutrons-2024-code-and-cad-release/467670) - https://www.chiefdelphi.com/t/frc125-the-nutrons-2024-code-and-cad-release/467670
- [2026 reveal](https://www.chiefdelphi.com/t/frc125-the-nutrons-2026-reveal/516273) - https://www.chiefdelphi.com/t/frc125-the-nutrons-2026-reveal/516273
- [NUTRONs website](https://www.nutrons.com/) - https://www.nutrons.com/
- [TBA 125](https://www.thebluealliance.com/team/125) - https://www.thebluealliance.com/team/125

**Analysis / what this gives us:**
- The localization gap analysis explicitly compares Team 999 against 125 and notes the value of correct weighting and logging over complexity.
- Use as a sanity check: Team 999 should not over-build a complex Quest/Limelight/PhotonVision fusion if a simple properly weighted/logged approach performs well.
- Team 125 also matters for dye-rotor/design lineage through their historical prototyping and references from WildStang.

### 2910 Jack in the Bot

**Priority:** A - Elite code/CAD/performance  
**Category:** Elite robot code, CAD/tech binder, 2024 turret hybrid reference  
**Context:** 2026 Newton #1 and Einstein finalist; public 2026 code and 2026 CAD/tech-binder release found.  
**Main Team 999 use:** Code quality benchmark, turret/global+local aiming ideas from 2024, CAD/tech binder release process.  
**Caution:** 2026 code release appears early/incomplete in uploaded re-check; no public AI contract found.

**Direct links:**
- [2026 robot code](https://github.com/FRCTeam2910/2026CompetitionRobot-Public) - https://github.com/FRCTeam2910/2026CompetitionRobot-Public
- [2024 robot code](https://github.com/FRCTeam2910/2024CompetitionRobot-Public) - https://github.com/FRCTeam2910/2024CompetitionRobot-Public
- [Team 2910 GitHub org](https://github.com/FRCTeam2910) - https://github.com/FRCTeam2910
- [2026 code release discussion](https://www.chiefdelphi.com/t/team-2910-code-release-2026/521778) - https://www.chiefdelphi.com/t/team-2910-code-release-2026/521778
- [2026 CAD and tech binder release](https://www.chiefdelphi.com/t/2910-cad-and-tech-binder-release-2026/521705) - https://www.chiefdelphi.com/t/2910-cad-and-tech-binder-release-2026/521705
- [Team resources](https://frcteam2910.org/resources/) - https://frcteam2910.org/resources/
- [TBA 2910](https://www.thebluealliance.com/team/2910) - https://www.thebluealliance.com/team/2910

**Analysis / what this gives us:**
- Use 2910 as a “what good looks like” source for code, CAD release quality, controls, and elite design documentation.
- The uploaded trajectory research identifies 2910 2024 as the cleanest turret hybrid: direct tag angle when visible, global pose fallback otherwise.
- No direct AGENTS.md/CLAUDE.md was found; use them to shape Team 999 expectations for AI output, not to seed the AI environment itself.

### 254 Cheesy Poofs

**Priority:** A - Elite code/binder benchmark  
**Category:** Robot code + technical binder + Q&A  
**Context:** Perennial elite; public 2025 code and technical binder; 2026 public code not verified in this pass.  
**Main Team 999 use:** Elite code structure, simulation/autonomous/controls ideas, binder quality, code Q&A.  
**Caution:** Best public code is 2025; no public AI contract found.

**Direct links:**
- [Team254 GitHub org](https://github.com/Team254) - https://github.com/Team254
- [FRC-2025-Public](https://github.com/Team254/FRC-2025-Public) - https://github.com/Team254/FRC-2025-Public
- [FRC-2024-Public](https://github.com/Team254/FRC-2024-Public) - https://github.com/Team254/FRC-2024-Public
- [2025 binder/code/Q&A thread](https://www.chiefdelphi.com/t/team-254-presents-2025-undertow-technical-binder-code-q-a/506115) - https://www.chiefdelphi.com/t/team-254-presents-2025-undertow-technical-binder-code-q-a/506115
- [2025 technical binder PDF](https://media.team254.com/2025/09/985607eb-2025-Tech-Binder-254.pdf) - https://media.team254.com/2025/09/985607eb-2025-Tech-Binder-254.pdf
- [TBA 254](https://www.thebluealliance.com/team/254) - https://www.thebluealliance.com/team/254

**Analysis / what this gives us:**
- Use as an elite code quality reference and a technical-binder reference.
- Especially useful for prompts that ask AI to review whether Team 999 code is architecturally mature, testable, and maintainable.

### 1678 Citrus Circuits

**Priority:** A- - Elite historical code/local tracker  
**Category:** Public robot code, goal tracker, global/local split  
**Context:** 2026 public code exists; 2024 code has older 254-lineage separate GoalTracker/global pose ideas.  
**Main Team 999 use:** Separate goal tracker vs global pose architecture; 2026 CTRE GeneratedDrivetrain + vision as a modern reference.  
**Caution:** 2026 code may be more vanilla/early-season; 2024 is stronger for local/global architecture pattern.

**Direct links:**
- [C2026-Public](https://github.com/frc1678/C2026-Public) - https://github.com/frc1678/C2026-Public
- [C2024-Public](https://github.com/frc1678/C2024-Public) - https://github.com/frc1678/C2024-Public
- [frc1678 GitHub org](https://github.com/frc1678) - https://github.com/frc1678
- [TBA 1678](https://www.thebluealliance.com/team/1678) - https://www.thebluealliance.com/team/1678

**Analysis / what this gives us:**
- The uploaded trajectory research identifies 1678 2024 as a useful example of keeping a local GoalTracker separate from global pose estimation.
- Use as a source for “local target signal plus global pose” thinking, especially if Team 999 adds shooter-boresight vision.

### 6995 NOMAD

**Priority:** B+ - Turret/Choreo example  
**Category:** 2026 robot code, Choreo AutoFactory, turret from global pose  
**Context:** Not a primary elite benchmark, but the uploaded research found concrete 2026 Choreo AutoFactory and pose-fed turret usage.  
**Main Team 999 use:** Real 2026 proof of Choreo alliance flipping, trigger-based autos, and turret aimed from pose supplier.  
**Caution:** Use for specific architecture examples, not overall team-performance standard.

**Direct links:**
- [Robot-2026 repo](https://github.com/frc6995/Robot-2026) - https://github.com/frc6995/Robot-2026
- [TBA 6995](https://www.thebluealliance.com/team/6995) - https://www.thebluealliance.com/team/6995

**Analysis / what this gives us:**
- Useful because it backs up two Team 999 decisions: Choreo can alliance-flip, and a turret can be driven from global pose even if you later add boresight correction.

### 7407 Choate Wired Boars

**Priority:** B+ - RobotPy/PhotonVision/OA  
**Category:** RobotPy, PhotonVision, Open Alliance  
**Context:** CT/nearby team with public RobotPy code and Open Alliance build thread.  
**Main Team 999 use:** Python/RobotPy PhotonVision approach, OA design updates, CAD/code links from thread.  
**Caution:** Different language stack; use for vision ideas and local CT comparison, not Java template.

**Direct links:**
- [7407-DriveCode-Rebuilt repo](https://github.com/Choate-Robotics/7407-DriveCode-Rebuilt) - https://github.com/Choate-Robotics/7407-DriveCode-Rebuilt
- [Choate Robotics GitHub org](https://github.com/choate-robotics) - https://github.com/choate-robotics
- [2026 OA build thread](https://www.chiefdelphi.com/t/frc-7407-wired-boars-2026-build-thread-open-alliance/507852) - https://www.chiefdelphi.com/t/frc-7407-wired-boars-2026-build-thread-open-alliance/507852
- [TBA 7407](https://www.thebluealliance.com/team/7407) - https://www.thebluealliance.com/team/7407

**Analysis / what this gives us:**
- The uploaded research log found RobotPy/PhotonVision files worth reviewing. Use as local ecosystem evidence that PhotonVision can be used successfully outside the Java/AdvantageKit path.
- Because Team 999 is Java, use the ideas but not the code directly.

### 195 CyberKnights

**Priority:** A- - CT performance/CAD/code release, manual access  
**Category:** CAD/code/documentation release, self-hosted GitLab, ROS/Mac Mini stack  
**Context:** CT powerhouse; 2026 release thread found; self-hosted GitLab may require browser/auth.  
**Main Team 999 use:** High-performance CT process, CAD/release materials, custom ROS/coproc ideas if accessible.  
**Caution:** GitLab group may need authentication/JavaScript; user may need to open/download.

**Direct links:**
- [2026 CAD, code, documentation release](https://www.chiefdelphi.com/t/team-195-the-cyberknights-2026-cad-code-and-documentation-release/521390) - https://www.chiefdelphi.com/t/team-195-the-cyberknights-2026-cad-code-and-documentation-release/521390
- [GitHub org pointing to GitLab](https://github.com/frcteam195) - https://github.com/frcteam195
- [Self-hosted GitLab group](https://gitlab.team195.com/cyberknights) - https://gitlab.team195.com/cyberknights
- [Team website](https://www.team195.com/) - https://www.team195.com/
- [TBA 195 2026](https://www.thebluealliance.com/team/195/2026) - https://www.thebluealliance.com/team/195/2026
- [YouTube channel](https://www.youtube.com/@FRCTeam195/videos) - https://www.youtube.com/@FRCTeam195/videos

**Analysis / what this gives us:**
- This is a place where your offer to authenticate/download may matter. The public CD release says CAD, judging materials, and code are linked, but the GitLab side may need a browser or account.
- Use for CT-level benchmark and custom stack ideas only after you can access/download the release materials.

### 4414 HighTide

**Priority:** A - World champion design/process benchmark  
**Category:** Technical binder, videos, discussion, design/process  
**Context:** 2026 Houston champion, Daly #1, CA district #1; public code/AI contract not found.  
**Main Team 999 use:** World-champion strategy, design, CAD/process presentation, manufacturing tooling ideas, technical binder quality.  
**Caution:** Do not list as a code source unless a current public repo appears.

**Direct links:**
- [Team4414 GitHub org](https://github.com/Team4414) - https://github.com/Team4414
- [2026 tech binder CD thread](https://www.chiefdelphi.com/t/team-4414-hightide-2026-tech-binder-ripcurrent/519602) - https://www.chiefdelphi.com/t/team-4414-hightide-2026-tech-binder-ripcurrent/519602
- [2026 technical binder site](https://2026.team4414.com/) - https://2026.team4414.com/
- [Robot reveal video](https://www.youtube.com/watch?v=9VpVZiApRFw) - https://www.youtube.com/watch?v=9VpVZiApRFw
- [Behind the Bumpers](https://www.youtube.com/watch?v=XM7l9Z1HiDs) - https://www.youtube.com/watch?v=XM7l9Z1HiDs
- [Championship interview](https://www.youtube.com/watch?v=Xnf7X2Bf8BQ) - https://www.youtube.com/watch?v=Xnf7X2Bf8BQ
- [Team website](https://www.team4414.com/) - https://www.team4414.com/
- [TBA 4414 2026](https://www.thebluealliance.com/team/4414/2026) - https://www.thebluealliance.com/team/4414/2026

**Analysis / what this gives us:**
- This fixes the previous vague entry: HighTide is useful because of binder/video/process material, not because of copyable current code.
- Use their public material for design review rubrics, strategic priorities, and the kind of technical release Team 999 should eventually produce.
- I did not find a current 2025/2026 public robot-code repo or AI contract files.

### 1323 MadTown Robotics

**Priority:** A - World champion performance/mechanism benchmark  
**Category:** Technical binder/Q&A, website/wiki, reveal/discussion  
**Context:** 2026 Houston champion with 4414; public current robot code appears limited/unreleased.  
**Main Team 999 use:** Elite strategy, mechanism packaging, printed/manufactured part practices, technical-binder expectations.  
**Caution:** No current public robot-code/AI contract found.

**Direct links:**
- [team1323 GitHub org](https://github.com/team1323) - https://github.com/team1323
- [Team website](https://team1323.com/) - https://team1323.com/
- [Team wiki](https://team1323.com/wiki/) - https://team1323.com/wiki/
- [2025 tech binder thread](https://www.chiefdelphi.com/t/1323-2025-tech-binder/500435) - https://www.chiefdelphi.com/t/1323-2025-tech-binder/500435
- [2025 code-release comment](https://www.chiefdelphi.com/t/1323-2025-tech-binder/500435/38) - https://www.chiefdelphi.com/t/1323-2025-tech-binder/500435/38
- [2025 robot reveal thread](https://www.chiefdelphi.com/t/1323-madtown-robot-reveal/495568) - https://www.chiefdelphi.com/t/1323-madtown-robot-reveal/495568
- [TBA 1323 2026](https://www.thebluealliance.com/team/1323/2026) - https://www.thebluealliance.com/team/1323/2026

**Analysis / what this gives us:**
- Use as an elite mechanism/process benchmark. Public value is binder/discussion, not current code.
- Do not overstate it as an AI or code source.

### 6329 Bucks' Wrath

**Priority:** A- - NE performance/mechanism benchmark  
**Category:** Robot reveal and mechanism discussion  
**Context:** 2026 Hopper Division winner; NE #4; no public code found in re-check.  
**Main Team 999 use:** Spindexter/drum/feed mechanism packaging ideas, local NE performance benchmark.  
**Caution:** No current public code/AI contract found.

**Direct links:**
- [2026 robot reveal: ROMAN](https://www.chiefdelphi.com/t/6329-bucks-wrath-robot-reveal-2026-roman/515342) - https://www.chiefdelphi.com/t/6329-bucks-wrath-robot-reveal-2026-roman/515342
- [Team website](https://bucksporths.wixsite.com/frc6329) - https://bucksporths.wixsite.com/frc6329
- [TBA 6329](https://www.thebluealliance.com/team/6329) - https://www.thebluealliance.com/team/6329
- [FIRST 6329 2026](https://frc-events.firstinspires.org/2026/team/6329) - https://frc-events.firstinspires.org/2026/team/6329

**Analysis / what this gives us:**
- The reveal thread includes mechanism details such as spindexter dimensions and compression comments; useful for CAD/mechanism prompts.
- Use as a local benchmark; no public code source currently verified.

### 4206 Robo Vikes

**Priority:** A- - Einstein finalist / org to watch  
**Category:** GitHub org, code/org resources  
**Context:** 2026 Newton Division winner and Einstein finalist; public GitHub org has repos including 26-c-0002 and battleaid.  
**Main Team 999 use:** Top-playoff reference, battleaid utilities/guides, possible 2026 second robot code.  
**Caution:** No current public AI contract found; verify exact robot-code relevance before mining.

**Direct links:**
- [frc4206 GitHub org](https://github.com/frc4206) - https://github.com/frc4206
- [26-c-0002 repo](https://github.com/frc4206/26-c-0002) - https://github.com/frc4206/26-c-0002
- [battleaid repo](https://github.com/frc4206/battleaid) - https://github.com/frc4206/battleaid
- [TBA 4206 2026](https://www.thebluealliance.com/team/4206/2026) - https://www.thebluealliance.com/team/4206/2026

**Analysis / what this gives us:**
- Useful to watch because of 2026 Einstein finalist context and public org resources.
- Current public material did not surface AGENTS/CLAUDE-style AI files.

### 111/112 WildStang

**Priority:** A - Mechanism/CAD concept source  
**Category:** Open Alliance mechanism deep dive  
**Context:** Public 2026 Open Alliance build blog with dye-rotor geometry/math discussion.  
**Main Team 999 use:** Dye rotor geometry, overfeed/jam risks, design tradeoffs, swerve-module clearance considerations.  
**Caution:** Mechanism concept source, not robot-code AI source.

**Direct links:**
- [WildStang 2026 build blog page 4](https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853?page=4) - https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853?page=4
- [Build blog main thread](https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853) - https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853
- [TBA 111 2026](https://www.thebluealliance.com/team/111/2026) - https://www.thebluealliance.com/team/111/2026
- [TBA 112 2026](https://www.thebluealliance.com/team/112/2026) - https://www.thebluealliance.com/team/112/2026

**Analysis / what this gives us:**
- Best found public source for dye rotor design thinking. The post discusses adapting geometry to the 2026 game piece, clearing modern swerve modules, and replacing slip rings.
- Use it to seed Team 999 CAD-agent prompts for turret/dye-rotor trade studies and perimeter checks.

### 2363 Triple Helix / wpilog-mcp

**Priority:** A for tooling  
**Category:** FRC AI/MCP log-analysis tooling  
**Context:** Team-built public tooling source; competition strength is secondary to its role in the AI evidence loop.  
**Main Team 999 use:** MCP server for natural-language analysis of WPILib/AdvantageKit logs; useful for simulation, practice log, and match review workflows.  
**Caution:** Tooling source, not a robot architecture benchmark.

**Direct links:**
- [wpilog-mcp](https://github.com/TripleHelixProgramming/wpilog-mcp) - https://github.com/TripleHelixProgramming/wpilog-mcp
- [TBA 2363 2026](https://www.thebluealliance.com/team/2363/2026) - https://www.thebluealliance.com/team/2363/2026

**Analysis / what this gives us:**
- This should be considered for the “AI can only modify code after reading logs/simulation output” workflow.
- Useful for turning AdvantageKit/WPILOG evidence into assistant-readable queries and post-change review notes.

### 488 XBot / SeriouslyCommonLib / Programming Docs

**Priority:** B+  
**Category:** Reusable Java/WPILib library and documentation pattern  
**Context:** Prior pass found 2025 competitive context and useful docs/library materials; 2026 context was not a primary benchmark.  
**Main Team 999 use:** Reusable command/subsystem patterns, HAL separation/unit testing ideas, and Programming-Docs AGENTS.md as a documentation-agent example.  
**Caution:** Not a direct elite 2026 benchmark; use as library/docs pattern source.

**Direct links:**
- [SeriouslyCommonLib](https://github.com/Team488/SeriouslyCommonLib) - https://github.com/Team488/SeriouslyCommonLib
- [Programming-Docs AGENTS.md](https://github.com/Team488/Programming-Docs/blob/main/AGENTS.md) - https://github.com/Team488/Programming-Docs/blob/main/AGENTS.md
- [TBA 488 2026](https://www.thebluealliance.com/team/488/2026) - https://www.thebluealliance.com/team/488/2026

**Analysis / what this gives us:**
- The Programming-Docs AGENTS.md is useful even though it is not robot code because Team 999 may need AI guidance for student training docs.
- SeriouslyCommonLib can be mined for reusable command architecture and testing patterns.

### 834 SparTechs

**Priority:** C+ / Watch  
**Category:** Potential AI/strategy-board workflow source  
**Context:** Lower priority source from the expanded search; exact StrategyBoard-style repo should be locally rechecked.  
**Main Team 999 use:** Potential non-robot-code AI workflow ideas, especially around strategy boards or scouting/analysis.  
**Caution:** Do not treat as a verified primary source until the exact repo/files are cloned and inspected.

**Direct links:**
- [team834 GitHub](https://github.com/team834) - https://github.com/team834
- [TBA 834 2026](https://www.thebluealliance.com/team/834/2026) - https://www.thebluealliance.com/team/834/2026

**Analysis / what this gives us:**
- Keep as a watch item, not a foundation. The main Team 999 AI environment should be built from 360/1868/6238/6391 plus 6328/1768/3467 for technical architecture.

## FRC-specific AI / MCP / log-analysis tooling
- **wpilib-agent-tools:** Sandbox-first Codex/Claude/Cursor harness for WPILib sim, NT4 recording, WPILOG analysis, and patch review. URL: https://github.com/edanliahovetsky/wpilib-agent-tools
- **wpilog-mcp:** MCP server for asking natural-language questions about WPILib/AdvantageKit logs; built by FRC 2363. URL: https://github.com/TripleHelixProgramming/wpilog-mcp
- **frc-rag-mcpserver:** FRC RAG/MCP concept for forcing assistants to query version-specific docs before answering robot-code questions. URL: https://github.com/ramalamadingdong/frc-rag-mcpserver
- **agentic-csa:** MCP server aggregating WPILib, REV, CTRE Phoenix, Redux, PhotonVision docs. URL: https://github.com/ramalamadingdong/agentic-csa
- **agentic-csa CLAUDE.md:** Example of MCP architecture documentation for Claude. URL: https://github.com/ramalamadingdong/agentic-csa/blob/main/CLAUDE.md
- **tba-mcp-server:** TBA data MCP server for scouting/event/ranking context. URL: https://github.com/withinfocus/tba-mcp-server
- **AdvantageKit:** Logging/replay framework; central to AI-verifiable robot changes. URL: https://github.com/Mechanical-Advantage/AdvantageKit
- **AdvantageScope:** Telemetry/log viewer for live and replay analysis. URL: https://github.com/Mechanical-Advantage/AdvantageScope
- **Shenzhen Robotics Alliance MapleSim:** Advanced FRC Java simulation library. URL: https://github.com/Shenzhen-Robotics-Alliance/maple-sim
- **SRA AdvantageKit TalonSwerve Template MapleSim:** AdvantageKit + CTRE swerve + MapleSim template with AGENTS.md. URL: https://github.com/Shenzhen-Robotics-Alliance/AdvantageKit-TalonSwerveTemplate-MapleSim

## Official AI, WPILib, vendor, and framework documentation
- **OpenAI Codex AGENTS.md:** Official Codex project-instruction behavior. URL: https://developers.openai.com/codex/agents-md/
- **OpenAI Codex Skills:** Official skill packaging guidance. URL: https://developers.openai.com/codex/skills/
- **Claude Code memory / CLAUDE.md:** Official Claude project-memory guidance. URL: https://docs.claude.com/en/docs/claude-code/memory
- **Claude Code best practices:** Check commands, iterative workflows, and review advice. URL: https://www.anthropic.com/engineering/claude-code-best-practices
- **WPILib CI:** Robot-code GitHub Actions / WPILib Docker CI. URL: https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/robot-code-ci.html
- **WPILib simulation:** Desktop robot simulation. URL: https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html
- **CTRE Phoenix 6 simulation:** Phoenix 6 SimState/device simulation. URL: https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/simulation/simulation-intro.html
- **PhotonVision Java simulation:** PhotonVision simulation. URL: https://docs.photonvision.org/en/latest/docs/simulation/simulation-java.html
- **PathPlanner Build an Auto:** AutoBuilder and autonomous commands. URL: https://pathplanner.dev/pplib-build-an-auto.html
- **PathPlanner Choreo interop:** Choreo trajectories in PathPlannerLib. URL: https://pathplanner.dev/pplib-choreo.html
- **Choreo docs:** Choreo trajectory generation and docs. URL: https://choreo.autos/
- **AdvantageKit IO docs:** IO interfaces for logging/replay. URL: https://docs.advantagekit.org/data-flow/recording-inputs/io-interfaces/
- **Limelight complete NT API:** MegaTag2/SetRobotOrientation/LL4 integration reference. URL: https://docs.limelightvision.io/docs/docs-limelight/apis/complete-networktables-api

## Vision, localization, trajectory, and PhotonVision additions

### Key findings from supplied research
- Team 999’s 2026 localization stack was sophisticated, but the biggest precision weaknesses were fusing MT2 yaw with finite standard deviation, selecting only one camera/one frame per loop, missing structured logs/replay, hard mid-operation resets, and hard innovation gates that can lock in odometry drift.
- The top-team localization pattern is not a single camera vendor. The shared pattern is multiple compact global-shutter cameras or strong Limelight fusion, fuse every valid observation, distance-squared/tag-count weighting, rotation only when independent/trustworthy, physical rejection on field/Z/ambiguity, no enabled hard resets, and AdvantageKit-style logging/replay.
- Nashoba 1768 is important because it gives a public 2026 PhotonVision/AdvantageKit-style reference implementation from the #1 New England team.
- The recommended PhotonVision pilot is 2 OV9281-class global-shutter cameras on one Orange Pi 5 for the pilot, later 3-4 cameras across 1-2 Orange Pi 5s if the pilot passes ground-truth tests.
- For trajectory precision, switching PathPlanner to Choreo does not by itself fix endpoint error. Both styles are time-parameterized. The fix is timed path for transit, then DriveToPose or a settle-gated endpoint controller for precise arrival.
- For aiming, the recommended Team 999 approach is global-pose coarse aim plus a shooter-boresight/global-shutter camera for fine correction and a logged poseBearing - cameraBearing check.

### Vision/trajectory source links
| Source | URL | What to use |
| --- | --- | --- |
| Nashoba 1768 2026 repo | https://github.com/Nashoba-Robotics/2026NashobaRobotics | PhotonVision/AdvantageKit reference implementation and Choreo/trajectory source. |
| Nashoba vision folder | https://github.com/Nashoba-Robotics/2026NashobaRobotics/tree/main/src/main/java/frc/robot/subsystems/vision | Vision.java, constants, IO, PhotonVision, and sim files. |
| 6328 RobotCode2026Public | https://github.com/Mechanical-Advantage/RobotCode2026Public | Northstar/multi-camera vision, RobotState, trajectory handoff, logging/replay. |
| 6328 RobotState.java | https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/RobotState.java | Custom estimator/replay architecture. |
| 6328 DriveToPose.java | https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/commands/DriveToPose.java | Position-tolerance endpoint controller. |
| 3467 posestimator library | https://github.com/WHS-FRC-3467/Skip-5.16-Perry/tree/master/src/main/java/frc/lib/posestimator | Skid rejection, n-sigma gating, uncertainty-aware estimator ideas. |
| 5687 2026 repo | https://github.com/frc5687/2026-Robot | C++ Limelight/custom fusion reference. |
| 125 nu25 | https://gitlab.com/nutrons125/nu25 | Simple Limelight MT2/AdvantageKit vision reference. |
| 2910 2024 robot code | https://github.com/FRCTeam2910/2024CompetitionRobot-Public | Turret target-relative + global fallback aiming example. |
| 1678 C2024-Public | https://github.com/frc1678/C2024-Public | Separate GoalTracker/global pose historical architecture. |
| 6995 Robot-2026 | https://github.com/frc6995/Robot-2026 | Choreo AutoFactory/alliance flipping and pose-fed turret example. |
| PhotonVision docs | https://docs.photonvision.org/ | PhotonVision setup/calibration/simulation. |

## CAD / Onshape automation sources and plan

- **Onshape AI Advisor:** Native Onshape AI help/Q&A; guidance, not autonomous FRC design. URL: https://www.onshape.com/en/resource-center/tech-tips/onshape-ai-advisor
- **Onshape AI roadmap:** FeatureScript autocomplete/search/rendering/future agent direction. URL: https://www.onshape.com/en/blog/how-onshape-is-bringing-artificial-intelligence-into-cad
- **Onshape REST API overview:** Practical script/API integration path. URL: https://onshape-public.github.io/docs/api-intro/
- **Onshape advanced API intro:** More API guidance. URL: https://onshape-public.github.io/docs/api-adv/intro/
- **FeatureScript docs:** Custom features and parametric automation. URL: https://cad.onshape.com/FsDoc/
- **FRCDesignLib:** FRC COTS component library/design resources. URL: https://frcdesign.org/resources/frcdesignlib/
- **FRCDesignApp CD thread:** Community app/plugin discussion. URL: https://www.chiefdelphi.com/t/frcdesignapp/474775
- **New FRCDesignApp thread:** Updated app discussion. URL: https://www.chiefdelphi.com/t/introducing-the-new-frcdesignapp/507335
- **Onshape4FRC:** FRC Onshape curriculum/resources. URL: https://onshape4frc.com/
- **Onshape4FRC CD thread:** Original community discussion. URL: https://www.chiefdelphi.com/t/onshape4frc/391957

### Turret / dye-rotor / mechanism AI workflow
- Use AI to convert strategy intent into structured mechanism_spec.yaml inputs.
- Run deterministic geometry checks for turret center, rotation envelope, perimeter/extension legality, motor/sensor/wire keepouts, ball path compression, ring ID/OD, and manufacturing constraints.
- Use Onshape variables, configurations, FeatureScript helper features, and the REST API to generate controlled construction geometry and reports.
- Require human design review and prototype validation before any AI-suggested mechanism is treated as buildable.

```text
C = turret center = (cx, cy)
p_i = rotating point relative to C
q_i(theta) = C + R(theta) * p_i
For every theta in the allowed range, q_i(theta) must remain inside the legal boundary or allowed extension envelope.

r_max = max(||p_i||) over motors, belts, shooter plates, sensors, wire loops
For full 360 degree rotation, cx/cy must be at least r_max from legal limits; otherwise restrict theta and add hard stops/software limits.

ring_ID >= max(game_piece_path_width + 2*clearance, shooter_throat_envelope, wire_passage_requirement)
ring_OD = ring_ID + 2*(bearing/ring width + fastener edge distance + manufacturing margin)
```

| Architecture | When it helps | Risk flags | AI output should include |
| --- | --- | --- | --- |
| Conventional feeder + turreted shooter | Limited-angle turret is enough and packaging is manageable | Motors/shooter plates leave perimeter; cable wrap; off-center feed jams | Center candidates, rotation range, ring ID/OD, cable plan, motor envelope |
| Fixed shooter + rotating feeder | Shooter is too heavy/large to rotate | Complex feed timing/control | Feed timing, compression, sensor plan, collision map |
| Dye rotor / drum feed | Center exit helps shooter/turret packaging | High complexity, power transfer, jams, overfeed tuning | Drum diameter, feed roller radius, overfeed %, jam-risk score, prototype plan |
| Swerve-aimed no turret | Simplest if localization/heading are strong | Requires excellent pose/controls; less robust during motion | Localization budget, alignment time, shot map, DriveToPose/fine aim requirements |

## Appendix A - 2026 Championship top-10 qualification rankings by division
| Year | Division | Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- | --- | --- |
| 2026 | Archimedes | 1 | 5940 | 4.80 | 572.50 | 10-0-0 |
| 2026 | Archimedes | 2 | 9470 | 4.70 | 643.10 | 9-1-0 |
| 2026 | Archimedes | 3 | 1114 | 4.60 | 592.10 | 10-0-0 |
| 2026 | Archimedes | 4 | 8608 | 4.40 | 544.10 | 9-1-0 |
| 2026 | Archimedes | 5 | 1241 | 4.20 | 535.20 | 9-1-0 |
| 2026 | Archimedes | 6 | 3045 | 4.10 | 544.40 | 8-2-0 |
| 2026 | Archimedes | 7 | 27 | 4.00 | 543.10 | 8-2-0 |
| 2026 | Archimedes | 8 | 10021 | 3.90 | 529.60 | 8-2-0 |
| 2026 | Archimedes | 9 | 2106 | 3.90 | 496.60 | 8-1-1 |
| 2026 | Archimedes | 10 | 5468 | 3.80 | 501.40 | 8-2-0 |
| 2026 | Curie | 1 | 5907 | 4.50 | 575.30 | 10-0-0 |
| 2026 | Curie | 2 | 6800 | 4.50 | 574.60 | 9-1-0 |
| 2026 | Curie | 3 | 5199 | 4.40 | 526.60 | 10-0-0 |
| 2026 | Curie | 4 | 254 | 4.20 | 563.10 | 9-1-0 |
| 2026 | Curie | 5 | 1987 | 4.20 | 552.50 | 9-1-0 |
| 2026 | Curie | 6 | 7028 | 4.10 | 538.60 | 9-1-0 |
| 2026 | Curie | 7 | 9023 | 3.90 | 538.90 | 8-2-0 |
| 2026 | Curie | 8 | 3128 | 3.90 | 511.50 | 9-1-0 |
| 2026 | Curie | 9 | 6369 | 3.70 | 516.10 | 7-3-0 |
| 2026 | Curie | 10 | 125 | 3.50 | 543.40 | 7-3-0 |
| 2026 | Daly | 1 | 4414 | 4.90 | 693.40 | 10-0-0 |
| 2026 | Daly | 2 | 1323 | 4.90 | 587.30 | 10-0-0 |
| 2026 | Daly | 3 | 4028 | 4.00 | 468.60 | 9-1-0 |
| 2026 | Daly | 4 | 1325 | 3.90 | 527.80 | 8-2-0 |
| 2026 | Daly | 5 | 316 | 3.80 | 457.30 | 9-1-0 |
| 2026 | Daly | 6 | 3538 | 3.70 | 490.50 | 8-2-0 |
| 2026 | Daly | 7 | 340 | 3.60 | 452.00 | 8-2-0 |
| 2026 | Daly | 8 | 10213 | 3.50 | 442.60 | 8-2-0 |
| 2026 | Daly | 9 | 2073 | 3.30 | 451.80 | 7-3-0 |
| 2026 | Daly | 10 | 4678 | 3.20 | 487.00 | 7-3-0 |
| 2026 | Galileo | 1 | 7769 | 4.40 | 598.50 | 9-1-0 |
| 2026 | Galileo | 2 | 1690 | 4.00 | 597.70 | 8-2-0 |
| 2026 | Galileo | 3 | 5913 | 3.90 | 488.30 | 9-1-0 |
| 2026 | Galileo | 4 | 1506 | 3.90 | 475.10 | 9-1-0 |
| 2026 | Galileo | 5 | 1768 | 3.80 | 562.00 | 7-3-0 |
| 2026 | Galileo | 6 | 9496 | 3.80 | 533.90 | 8-2-0 |
| 2026 | Galileo | 7 | 449 | 3.80 | 467.20 | 8-2-0 |
| 2026 | Galileo | 8 | 4469 | 3.60 | 479.50 | 8-2-0 |
| 2026 | Galileo | 9 | 2468 | 3.50 | 471.40 | 7-3-0 |
| 2026 | Galileo | 10 | 6528 | 3.50 | 459.50 | 8-2-0 |
| 2026 | Hopper | 1 | 2056 | 4.60 | 632.30 | 9-1-0 |
| 2026 | Hopper | 2 | 1706 | 4.60 | 602.00 | 10-0-0 |
| 2026 | Hopper | 3 | 6329 | 4.30 | 595.00 | 9-1-0 |
| 2026 | Hopper | 4 | 9785 | 4.30 | 545.10 | 9-1-0 |
| 2026 | Hopper | 5 | 5000 | 4.00 | 555.10 | 8-2-0 |
| 2026 | Hopper | 6 | 1732 | 3.90 | 555.20 | 7-3-0 |
| 2026 | Hopper | 7 | 8513 | 3.90 | 547.30 | 8-2-0 |
| 2026 | Hopper | 8 | 8044 | 3.90 | 546.60 | 7-3-0 |
| 2026 | Hopper | 9 | 581 | 3.80 | 616.60 | 7-3-0 |
| 2026 | Hopper | 10 | 1701 | 3.80 | 524.10 | 8-2-0 |
| 2026 | Johnson | 1 | 4522 | 4.70 | 565.90 | 10-0-0 |
| 2026 | Johnson | 2 | 1792 | 4.00 | 460.50 | 9-1-0 |
| 2026 | Johnson | 3 | 6324 | 3.90 | 458.10 | 9-1-0 |
| 2026 | Johnson | 4 | 7558 | 3.80 | 532.90 | 8-2-0 |
| 2026 | Johnson | 5 | 117 | 3.60 | 529.00 | 7-3-0 |
| 2026 | Johnson | 6 | 190 | 3.60 | 476.10 | 8-2-0 |
| 2026 | Johnson | 7 | 67 | 3.50 | 512.10 | 7-3-0 |
| 2026 | Johnson | 8 | 836 | 3.50 | 475.50 | 8-2-0 |
| 2026 | Johnson | 9 | 4391 | 3.50 | 465.20 | 8-2-0 |
| 2026 | Johnson | 10 | 2337 | 3.50 | 443.80 | 8-2-0 |
| 2026 | Milstein | 1 | 7457 | 4.40 | 510.10 | 10-0-0 |
| 2026 | Milstein | 2 | 1678 | 4.20 | 561.90 | 9-1-0 |
| 2026 | Milstein | 3 | 2481 | 4.00 | 563.50 | 8-2-0 |
| 2026 | Milstein | 4 | 694 | 4.00 | 432.70 | 10-0-0 |
| 2026 | Milstein | 5 | 4499 | 3.60 | 485.60 | 8-2-0 |
| 2026 | Milstein | 6 | 686 | 3.60 | 448.40 | 8-2-0 |
| 2026 | Milstein | 7 | 2122 | 3.50 | 510.60 | 7-3-0 |
| 2026 | Milstein | 8 | 1771 | 3.50 | 464.90 | 8-2-0 |
| 2026 | Milstein | 9 | 4089 | 3.40 | 493.60 | 7-3-0 |
| 2026 | Milstein | 10 | 1640 | 3.40 | 475.00 | 7-3-0 |
| 2026 | Newton | 1 | 2910 | 4.60 | 706.70 | 9-1-0 |
| 2026 | Newton | 2 | 9128 | 4.30 | 536.10 | 9-1-0 |
| 2026 | Newton | 3 | 5549 | 4.30 | 515.10 | 9-1-0 |
| 2026 | Newton | 4 | 604 | 4.10 | 564.60 | 8-2-0 |
| 2026 | Newton | 5 | 973 | 4.10 | 559.60 | 8-2-0 |
| 2026 | Newton | 6 | 1833 | 3.90 | 480.60 | 8-2-0 |
| 2026 | Newton | 7 | 6036 | 3.80 | 522.00 | 8-2-0 |
| 2026 | Newton | 8 | 2052 | 3.80 | 500.40 | 8-2-0 |
| 2026 | Newton | 9 | 1796 | 3.70 | 534.20 | 7-3-0 |
| 2026 | Newton | 10 | 695 | 3.70 | 522.90 | 7-3-0 |

## Appendix B - 2025 Championship top-10 qualification rankings by division
| Year | Division | Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- | --- | --- |
| 2025 | Archimedes | 1 | 8044 | 5.50 | 208.60 | 9-1-0 |
| 2025 | Archimedes | 2 | 1987 | 5.40 | 217.00 | 9-1-0 |
| 2025 | Archimedes | 3 | 1197 | 5.30 | 219.60 | 8-2-0 |
| 2025 | Archimedes | 4 | 1756 | 5.30 | 209.40 | 8-2-0 |
| 2025 | Archimedes | 5 | 4391 | 5.10 | 219.50 | 8-2-0 |
| 2025 | Archimedes | 6 | 1114 | 5.00 | 202.50 | 8-2-0 |
| 2025 | Archimedes | 7 | 341 | 5.00 | 219.10 | 7-3-0 |
| 2025 | Archimedes | 8 | 2481 | 5.00 | 218.10 | 8-2-0 |
| 2025 | Archimedes | 9 | 604 | 4.90 | 216.80 | 7-3-0 |
| 2025 | Archimedes | 10 | 2106 | 4.90 | 199.00 | 8-2-0 |
| 2025 | Curie | 1 | 7457 | 5.80 | 224.80 | 10-0-0 |
| 2025 | Curie | 2 | 3663 | 5.50 | 234.30 | 9-1-0 |
| 2025 | Curie | 3 | 7028 | 5.40 | 215.40 | 9-1-0 |
| 2025 | Curie | 4 | 4646 | 5.30 | 218.50 | 8-2-0 |
| 2025 | Curie | 5 | 1771 | 5.30 | 228.60 | 9-1-0 |
| 2025 | Curie | 6 | 4611 | 5.30 | 207.80 | 9-1-0 |
| 2025 | Curie | 7 | 340 | 5.20 | 202.50 | 8-2-0 |
| 2025 | Curie | 8 | 6329 | 5.20 | 217.00 | 9-1-0 |
| 2025 | Curie | 9 | 8884 | 5.10 | 219.50 | 8-2-0 |
| 2025 | Curie | 10 | 359 | 5.00 | 221.80 | 7-2-1 |
| 2025 | Daly | 1 | 4415 | 5.80 | 214.10 | 10-0-0 |
| 2025 | Daly | 2 | 1796 | 5.60 | 223.30 | 9-1-0 |
| 2025 | Daly | 3 | 1678 | 5.40 | 221.20 | 9-1-0 |
| 2025 | Daly | 4 | 6424 | 5.30 | 212.50 | 9-1-0 |
| 2025 | Daly | 5 | 179 | 5.20 | 200.30 | 8-2-0 |
| 2025 | Daly | 6 | 2056 | 5.10 | 239.20 | 7-3-0 |
| 2025 | Daly | 7 | 4655 | 4.90 | 205.20 | 7-3-0 |
| 2025 | Daly | 8 | 2630 | 4.90 | 213.40 | 8-2-0 |
| 2025 | Daly | 9 | 694 | 4.80 | 217.20 | 6-4-0 |
| 2025 | Daly | 10 | 3792 | 4.80 | 193.60 | 7-3-0 |
| 2025 | Galileo | 1 | 4946 | 5.80 | 213.70 | 10-0-0 |
| 2025 | Galileo | 2 | 9483 | 5.30 | 209.40 | 9-1-0 |
| 2025 | Galileo | 3 | 4522 | 5.20 | 209.50 | 9-1-0 |
| 2025 | Galileo | 4 | 190 | 5.00 | 211.00 | 8-2-0 |
| 2025 | Galileo | 5 | 59 | 4.80 | 195.40 | 8-2-0 |
| 2025 | Galileo | 6 | 3035 | 4.80 | 199.50 | 7-3-0 |
| 2025 | Galileo | 7 | 8214 | 4.70 | 200.40 | 9-1-0 |
| 2025 | Galileo | 8 | 180 | 4.60 | 198.30 | 8-2-0 |
| 2025 | Galileo | 9 | 5847 | 4.50 | 199.60 | 6-4-0 |
| 2025 | Galileo | 10 | 7558 | 4.50 | 196.10 | 6-4-0 |
| 2025 | Hopper | 1 | 1768 | 5.00 | 214.90 | 8-2-0 |
| 2025 | Hopper | 2 | 2200 | 5.00 | 208.20 | 8-2-0 |
| 2025 | Hopper | 3 | 4728 | 5.00 | 205.20 | 8-2-0 |
| 2025 | Hopper | 4 | 3339 | 4.80 | 196.40 | 9-1-0 |
| 2025 | Hopper | 5 | 6621 | 4.80 | 192.00 | 8-2-0 |
| 2025 | Hopper | 6 | 9245 | 4.80 | 190.70 | 8-1-1 |
| 2025 | Hopper | 7 | 4907 | 4.70 | 216.00 | 7-3-0 |
| 2025 | Hopper | 8 | 9496 | 4.60 | 210.90 | 7-3-0 |
| 2025 | Hopper | 9 | 7632 | 4.60 | 190.10 | 8-2-0 |
| 2025 | Hopper | 10 | 2075 | 4.50 | 209.10 | 6-4-0 |
| 2025 | Johnson | 1 | 1690 | 5.60 | 228.70 | 9-1-0 |
| 2025 | Johnson | 2 | 1619 | 5.60 | 216.70 | 9-1-0 |
| 2025 | Johnson | 3 | 6800 | 5.50 | 209.90 | 9-1-0 |
| 2025 | Johnson | 4 | 111 | 5.40 | 224.60 | 9-1-0 |
| 2025 | Johnson | 5 | 2607 | 5.20 | 201.70 | 8-2-0 |
| 2025 | Johnson | 6 | 4414 | 4.80 | 215.40 | 7-3-0 |
| 2025 | Johnson | 7 | 1325 | 4.80 | 206.00 | 8-2-0 |
| 2025 | Johnson | 8 | 2714 | 4.80 | 203.80 | 7-2-1 |
| 2025 | Johnson | 9 | 2930 | 4.80 | 192.10 | 7-3-0 |
| 2025 | Johnson | 10 | 45 | 4.70 | 194.30 | 7-3-0 |
| 2025 | Milstein | 1 | 5940 | 5.50 | 221.70 | 9-1-0 |
| 2025 | Milstein | 2 | 118 | 5.50 | 229.00 | 9-1-0 |
| 2025 | Milstein | 3 | 7407 | 5.40 | 210.90 | 9-1-0 |
| 2025 | Milstein | 4 | 254 | 5.20 | 224.30 | 8-2-0 |
| 2025 | Milstein | 5 | 910 | 5.10 | 202.60 | 9-1-0 |
| 2025 | Milstein | 6 | 3006 | 5.10 | 179.30 | 9-1-0 |
| 2025 | Milstein | 7 | 503 | 4.90 | 193.60 | 7-3-0 |
| 2025 | Milstein | 8 | 3674 | 4.90 | 195.00 | 7-2-1 |
| 2025 | Milstein | 9 | 5895 | 4.80 | 202.40 | 8-2-0 |
| 2025 | Milstein | 10 | 2137 | 4.80 | 197.50 | 8-2-0 |
| 2025 | Newton | 1 | 1323 | 5.90 | 239.50 | 10-0-0 |
| 2025 | Newton | 2 | 2832 | 5.50 | 202.10 | 10-0-0 |
| 2025 | Newton | 3 | 2910 | 5.40 | 243.00 | 8-2-0 |
| 2025 | Newton | 4 | 422 | 5.30 | 222.10 | 8-2-0 |
| 2025 | Newton | 5 | 148 | 5.00 | 208.50 | 8-2-0 |
| 2025 | Newton | 6 | 9072 | 5.00 | 208.90 | 8-2-0 |
| 2025 | Newton | 7 | 3538 | 5.00 | 204.50 | 8-2-0 |
| 2025 | Newton | 8 | 7426 | 4.90 | 206.40 | 8-2-0 |
| 2025 | Newton | 9 | 3937 | 4.90 | 198.40 | 8-2-0 |
| 2025 | Newton | 10 | 449 | 4.90 | 199.10 | 8-2-0 |

## Appendix C - Authentication / manual-download items
- Team 195 CyberKnights: public release thread exists, but self-hosted GitLab may need a browser/authenticated access to enumerate exact repos and CAD/code assets.
- Private Onshape documents: export or share read-only links; automation requires Onshape API/OAuth credentials that should not be shared in chat.
- Chief Delphi pages are public, but embedded Google Drive/Onshape/binder links can require a browser session.
- GitHub public pages generally worked, but heavy code search or rate-limited access may require logging in.

## Appendix D - Recommended Team 999 starter repository skeleton
```text
repo-root/
  AGENTS.md
  CLAUDE.md
  .github/workflows/ci.yml
  .github/copilot-instructions.md
  .agents/skills/
    frc-subsystem/SKILL.md
    frc-sim/SKILL.md
    vision-localization/SKILL.md
    pathplanner-auto/SKILL.md
    cad-onshape/SKILL.md
  .claude/agents/
    frc-code-reviewer.md
    frc-safety-reviewer.md
    frc-subsystem-architect.md
    frc-vision-reviewer.md
  .claude/commands/
    review-safety.md
    sim-smoke-test.md
    fsm-map.md
    vision-pose-review.md
    trajectory-auto-review.md
    cad-review.md
  docs/ai/
    robot-contract.md
    simulation-contract.md
    vision-contract.md
    deployment-safety.md
    cad-contract.md
    source-map.md
  cad/
    mechanism_spec.yaml
    cad_rules.md
    turret_layout.py
    onshape_variables.md
```

## Appendix E - Verified source link index
- **The Blue Alliance 2026 Championship divisions:** https://www.thebluealliance.com/event/2026cmptx
- **TBA 2026 Newton rankings:** https://www.thebluealliance.com/event/2026new#rankings
- **TBA 2026 Daly rankings:** https://www.thebluealliance.com/event/2026dal#rankings
- **TBA 2026 Curie rankings:** https://www.thebluealliance.com/event/2026cur#rankings
- **TBA 2026 Hopper rankings:** https://www.thebluealliance.com/event/2026hop#rankings
- **TBA 2026 Galileo rankings:** https://www.thebluealliance.com/event/2026gal#rankings
- **TBA 2026 Archimedes rankings:** https://www.thebluealliance.com/event/2026arc#rankings
- **TBA 2026 Johnson rankings:** https://www.thebluealliance.com/event/2026joh#rankings
- **TBA 2026 Milstein rankings:** https://www.thebluealliance.com/event/2026mil#rankings
- **TBA 2025 Championship divisions:** https://www.thebluealliance.com/event/2025cmptx
- **FRCTeam360 RainMaker26:** https://github.com/FRCTeam360/RainMaker26
- **6238 2026-Robot CLAUDE.md:** https://github.com/6238/2026-Robot/blob/main/CLAUDE.md
- **FRC Team 1868 Claude Code config:** https://github.com/Feramirr/frc-claude-config
- **wpilib-agent-tools:** https://github.com/edanliahovetsky/wpilib-agent-tools
- **Mechanical Advantage AdvantageKit:** https://github.com/Mechanical-Advantage/AdvantageKit
- **Mechanical Advantage RobotCode2026Public:** https://github.com/Mechanical-Advantage/RobotCode2026Public
- **Onshape AI Advisor:** https://www.onshape.com/en/features/ai-advisor
- **Onshape FeatureScript documentation:** https://cad.onshape.com/FsDoc/
- **Onshape REST API documentation:** https://onshape-public.github.io/docs/api-intro/
- **FRCDesignLib / FRCDesignApp:** https://frcdesign.org/resources/frcdesignlib/
- **Onshape4FRC:** https://onshape4frc.com/
- **WildStang dye rotor Chief Delphi post:** https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853?page=4
- **360 The Revolution - RainMaker26 repo:** https://github.com/FRCTeam360/RainMaker26
- **360 The Revolution - AGENTS.md:** https://github.com/FRCTeam360/RainMaker26/blob/main/AGENTS.md
- **360 The Revolution - CLAUDE.md:** https://github.com/FRCTeam360/RainMaker26/blob/main/CLAUDE.md
- **360 The Revolution - .claude commands:** https://github.com/FRCTeam360/RainMaker26/tree/main/.claude/commands
- **360 The Revolution - FSM command:** https://github.com/FRCTeam360/RainMaker26/blob/main/.claude/commands/fsm.md
- **360 The Revolution - Open Alliance thread:** https://www.chiefdelphi.com/t/frc-360-the-revolution-2026-build-thread-open-alliance/510290
- **1868 Space Cookies / FRC Claude Config - frc-claude-config repo:** https://github.com/Feramirr/frc-claude-config
- **1868 Space Cookies / FRC Claude Config - rules:** https://github.com/Feramirr/frc-claude-config/tree/main/rules
- **1868 Space Cookies / FRC Claude Config - agents:** https://github.com/Feramirr/frc-claude-config/tree/main/agents
- **1868 Space Cookies / FRC Claude Config - commands:** https://github.com/Feramirr/frc-claude-config/tree/main/commands
- **1868 Space Cookies / FRC Claude Config - skills:** https://github.com/Feramirr/frc-claude-config/tree/main/skills
- **1868 Space Cookies / FRC Claude Config - install.ps1:** https://github.com/Feramirr/frc-claude-config/blob/main/install.ps1
- **1868 Space Cookies / FRC Claude Config - install.sh:** https://github.com/Feramirr/frc-claude-config/blob/main/install.sh
- **6238 Popcorn Penguins - 2026-Robot repo:** https://github.com/6238/2026-Robot
- **6238 Popcorn Penguins - CLAUDE.md:** https://github.com/6238/2026-Robot/blob/main/CLAUDE.md
- **6238 Popcorn Penguins - PenguinCAM repo:** https://github.com/6238/PenguinCAM
- **6238 Popcorn Penguins - PenguinCAM app:** https://penguincam.popcornpenguins.com
- **6391 Bearbotics - 2026-6391-Rebuilt repo:** https://github.com/6391-Ursuline-Bearbotics/2026-6391-Rebuilt
- **6391 Bearbotics - CLAUDE.md:** https://github.com/6391-Ursuline-Bearbotics/2026-6391-Rebuilt/blob/main/CLAUDE.md
- **4050 Biohazard - 2026-rebuilt repo:** https://github.com/Team4050/2026-rebuilt
- **4050 Biohazard - AGENTS.md:** https://github.com/Team4050/2026-rebuilt/blob/main/AGENTS.md
- **4050 Biohazard - CLAUDE.md:** https://github.com/Team4050/2026-rebuilt/blob/main/CLAUDE.md
- **4050 Biohazard - frc-java-template:** https://github.com/Team4050/frc-java-template
- **7660 Byting Irish - robot-2026 repo:** https://github.com/FRC7660/robot-2026
- **7660 Byting Irish - AGENTS.md:** https://github.com/FRC7660/robot-2026/blob/main/AGENTS.md
- **8092 GOAT - 8092-2026 repo:** https://github.com/GOAT-8092/8092-2026
- **8092 GOAT - AGENTS.md:** https://github.com/GOAT-8092/8092-2026/blob/main/AGENTS.md
- **8092 GOAT - CLAUDE.md:** https://github.com/GOAT-8092/8092-2026/blob/main/CLAUDE.md
- **8092 GOAT - ROBOT.md:** https://github.com/GOAT-8092/8092-2026/blob/main/ROBOT.md
- **846 Funky Monkeys / Gibbon - gibbon repo:** https://github.com/Team846/gibbon
- **846 Funky Monkeys / Gibbon - AGENTS.md candidate:** https://github.com/Team846/gibbon/blob/main/AGENTS.md
- **6328 Mechanical Advantage - RobotCode2026Public:** https://github.com/Mechanical-Advantage/RobotCode2026Public
- **6328 Mechanical Advantage - Vision folder:** https://github.com/Mechanical-Advantage/RobotCode2026Public/tree/main/src/main/java/org/littletonrobotics/frc2026/subsystems/vision
- **6328 Mechanical Advantage - RobotState.java:** https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/RobotState.java
- **6328 Mechanical Advantage - DriveToPose.java:** https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/commands/DriveToPose.java
- **6328 Mechanical Advantage - DriveTrajectory.java:** https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/commands/DriveTrajectory.java
- **6328 Mechanical Advantage - 2026 OA build thread:** https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2026-build-thread/509595
- **6328 Mechanical Advantage - AdvantageKit:** https://github.com/Mechanical-Advantage/AdvantageKit
- **6328 Mechanical Advantage - AdvantageScope:** https://github.com/Mechanical-Advantage/AdvantageScope
- **6328 Mechanical Advantage - Onshape4FRC:** https://onshape4frc.com/
- **1768 Nashoba Robotics - 2026NashobaRobotics repo:** https://github.com/Nashoba-Robotics/2026NashobaRobotics
- **1768 Nashoba Robotics - Vision folder:** https://github.com/Nashoba-Robotics/2026NashobaRobotics/tree/main/src/main/java/frc/robot/subsystems/vision
- **1768 Nashoba Robotics - Vision.java:** https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/Vision.java
- **1768 Nashoba Robotics - VisionConstants.java:** https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/VisionConstants.java
- **1768 Nashoba Robotics - VisionIOPhotonVision.java:** https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/VisionIOPhotonVision.java
- **1768 Nashoba Robotics - VisionIOPhotonVisionSim.java:** https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/subsystems/vision/VisionIOPhotonVisionSim.java
- **1768 Nashoba Robotics - RobotContainer.java:** https://github.com/Nashoba-Robotics/2026NashobaRobotics/blob/main/src/main/java/frc/robot/RobotContainer.java
- **1768 Nashoba Robotics - Nashoba GitHub org:** https://github.com/Nashoba-Robotics
- **1768 Nashoba Robotics - TBA 1768 2026:** https://www.thebluealliance.com/team/1768/2026
- **1768 Nashoba Robotics - Reveal thread:** https://www.chiefdelphi.com/t/team-1768-nashoba-robotics-2026-robot-reveal-nightshift/519386
- **5687 The Outliers - 2026-Robot repo:** https://github.com/frc5687/2026-Robot
- **5687 The Outliers - src/main:** https://github.com/frc5687/2026-Robot/tree/main/src/main
- **5687 The Outliers - include folder:** https://github.com/frc5687/2026-Robot/tree/main/src/main/include
- **5687 The Outliers - cpp folder:** https://github.com/frc5687/2026-Robot/tree/main/src/main/cpp
- **5687 The Outliers - TBA 5687:** https://www.thebluealliance.com/team/5687/2026
- **3467 Windham Windup - Skip-5.16-Perry repo:** https://github.com/WHS-FRC-3467/Skip-5.16-Perry
- **3467 Windham Windup - Vision folder:** https://github.com/WHS-FRC-3467/Skip-5.16-Perry/tree/master/src/main/java/frc/robot/subsystems/vision
- **3467 Windham Windup - posestimator library:** https://github.com/WHS-FRC-3467/Skip-5.16-Perry/tree/master/src/main/java/frc/lib/posestimator
- **3467 Windham Windup - Open Alliance build blog:** https://www.chiefdelphi.com/t/3467-windham-windup-2026-build-blog/508474
- **3467 Windham Windup - TBA 3467 2026:** https://www.thebluealliance.com/team/3467/2026
- **125 NUTRONs - nu25 GitLab repo:** https://gitlab.com/nutrons125/nu25
- **125 NUTRONs - 2024 code/CAD release:** https://www.chiefdelphi.com/t/frc125-the-nutrons-2024-code-and-cad-release/467670
- **125 NUTRONs - 2026 reveal:** https://www.chiefdelphi.com/t/frc125-the-nutrons-2026-reveal/516273
- **125 NUTRONs - NUTRONs website:** https://www.nutrons.com/
- **125 NUTRONs - TBA 125:** https://www.thebluealliance.com/team/125
- **2910 Jack in the Bot - 2026 robot code:** https://github.com/FRCTeam2910/2026CompetitionRobot-Public
- **2910 Jack in the Bot - 2024 robot code:** https://github.com/FRCTeam2910/2024CompetitionRobot-Public
- **2910 Jack in the Bot - Team 2910 GitHub org:** https://github.com/FRCTeam2910
- **2910 Jack in the Bot - 2026 code release discussion:** https://www.chiefdelphi.com/t/team-2910-code-release-2026/521778
- **2910 Jack in the Bot - 2026 CAD and tech binder release:** https://www.chiefdelphi.com/t/2910-cad-and-tech-binder-release-2026/521705
- **2910 Jack in the Bot - Team resources:** https://frcteam2910.org/resources/
- **2910 Jack in the Bot - TBA 2910:** https://www.thebluealliance.com/team/2910
- **254 Cheesy Poofs - Team254 GitHub org:** https://github.com/Team254
- **254 Cheesy Poofs - FRC-2025-Public:** https://github.com/Team254/FRC-2025-Public
- **254 Cheesy Poofs - FRC-2024-Public:** https://github.com/Team254/FRC-2024-Public
- **254 Cheesy Poofs - 2025 binder/code/Q&A thread:** https://www.chiefdelphi.com/t/team-254-presents-2025-undertow-technical-binder-code-q-a/506115
- **254 Cheesy Poofs - 2025 technical binder PDF:** https://media.team254.com/2025/09/985607eb-2025-Tech-Binder-254.pdf
- **254 Cheesy Poofs - TBA 254:** https://www.thebluealliance.com/team/254
- **1678 Citrus Circuits - C2026-Public:** https://github.com/frc1678/C2026-Public
- **1678 Citrus Circuits - C2024-Public:** https://github.com/frc1678/C2024-Public
- **1678 Citrus Circuits - frc1678 GitHub org:** https://github.com/frc1678
- **1678 Citrus Circuits - TBA 1678:** https://www.thebluealliance.com/team/1678
- **6995 NOMAD - Robot-2026 repo:** https://github.com/frc6995/Robot-2026
- **6995 NOMAD - TBA 6995:** https://www.thebluealliance.com/team/6995
- **7407 Choate Wired Boars - 7407-DriveCode-Rebuilt repo:** https://github.com/Choate-Robotics/7407-DriveCode-Rebuilt
- **7407 Choate Wired Boars - Choate Robotics GitHub org:** https://github.com/choate-robotics
- **7407 Choate Wired Boars - 2026 OA build thread:** https://www.chiefdelphi.com/t/frc-7407-wired-boars-2026-build-thread-open-alliance/507852
- **7407 Choate Wired Boars - TBA 7407:** https://www.thebluealliance.com/team/7407
- **195 CyberKnights - 2026 CAD, code, documentation release:** https://www.chiefdelphi.com/t/team-195-the-cyberknights-2026-cad-code-and-documentation-release/521390
- **195 CyberKnights - GitHub org pointing to GitLab:** https://github.com/frcteam195
- **195 CyberKnights - Self-hosted GitLab group:** https://gitlab.team195.com/cyberknights
- **195 CyberKnights - Team website:** https://www.team195.com/
- **195 CyberKnights - TBA 195 2026:** https://www.thebluealliance.com/team/195/2026
- **195 CyberKnights - YouTube channel:** https://www.youtube.com/@FRCTeam195/videos
- **4414 HighTide - Team4414 GitHub org:** https://github.com/Team4414
- **4414 HighTide - 2026 tech binder CD thread:** https://www.chiefdelphi.com/t/team-4414-hightide-2026-tech-binder-ripcurrent/519602
- **4414 HighTide - 2026 technical binder site:** https://2026.team4414.com/
- **4414 HighTide - Robot reveal video:** https://www.youtube.com/watch?v=9VpVZiApRFw
- **4414 HighTide - Behind the Bumpers:** https://www.youtube.com/watch?v=XM7l9Z1HiDs
- **4414 HighTide - Championship interview:** https://www.youtube.com/watch?v=Xnf7X2Bf8BQ
- **4414 HighTide - Team website:** https://www.team4414.com/
- **4414 HighTide - TBA 4414 2026:** https://www.thebluealliance.com/team/4414/2026
- **1323 MadTown Robotics - team1323 GitHub org:** https://github.com/team1323
- **1323 MadTown Robotics - Team website:** https://team1323.com/
- **1323 MadTown Robotics - Team wiki:** https://team1323.com/wiki/
- **1323 MadTown Robotics - 2025 tech binder thread:** https://www.chiefdelphi.com/t/1323-2025-tech-binder/500435
- **1323 MadTown Robotics - 2025 code-release comment:** https://www.chiefdelphi.com/t/1323-2025-tech-binder/500435/38
- **1323 MadTown Robotics - 2025 robot reveal thread:** https://www.chiefdelphi.com/t/1323-madtown-robot-reveal/495568
- **1323 MadTown Robotics - TBA 1323 2026:** https://www.thebluealliance.com/team/1323/2026
- **6329 Bucks' Wrath - 2026 robot reveal: ROMAN:** https://www.chiefdelphi.com/t/6329-bucks-wrath-robot-reveal-2026-roman/515342
- **6329 Bucks' Wrath - Team website:** https://bucksporths.wixsite.com/frc6329
- **6329 Bucks' Wrath - TBA 6329:** https://www.thebluealliance.com/team/6329
- **6329 Bucks' Wrath - FIRST 6329 2026:** https://frc-events.firstinspires.org/2026/team/6329
- **4206 Robo Vikes - frc4206 GitHub org:** https://github.com/frc4206
- **4206 Robo Vikes - 26-c-0002 repo:** https://github.com/frc4206/26-c-0002
- **4206 Robo Vikes - battleaid repo:** https://github.com/frc4206/battleaid
- **4206 Robo Vikes - TBA 4206 2026:** https://www.thebluealliance.com/team/4206/2026
- **111/112 WildStang - WildStang 2026 build blog page 4:** https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853?page=4
- **111/112 WildStang - Build blog main thread:** https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853
- **111/112 WildStang - TBA 111 2026:** https://www.thebluealliance.com/team/111/2026
- **111/112 WildStang - TBA 112 2026:** https://www.thebluealliance.com/team/112/2026
- **2363 Triple Helix / wpilog-mcp - wpilog-mcp:** https://github.com/TripleHelixProgramming/wpilog-mcp
- **2363 Triple Helix / wpilog-mcp - TBA 2363 2026:** https://www.thebluealliance.com/team/2363/2026
- **488 XBot / SeriouslyCommonLib / Programming Docs - SeriouslyCommonLib:** https://github.com/Team488/SeriouslyCommonLib
- **488 XBot / SeriouslyCommonLib / Programming Docs - Programming-Docs AGENTS.md:** https://github.com/Team488/Programming-Docs/blob/main/AGENTS.md
- **488 XBot / SeriouslyCommonLib / Programming Docs - TBA 488 2026:** https://www.thebluealliance.com/team/488/2026
- **834 SparTechs - team834 GitHub:** https://github.com/team834
- **834 SparTechs - TBA 834 2026:** https://www.thebluealliance.com/team/834/2026
- **wpilib-agent-tools:** https://github.com/edanliahovetsky/wpilib-agent-tools
- **wpilog-mcp:** https://github.com/TripleHelixProgramming/wpilog-mcp
- **frc-rag-mcpserver:** https://github.com/ramalamadingdong/frc-rag-mcpserver
- **agentic-csa:** https://github.com/ramalamadingdong/agentic-csa
- **agentic-csa CLAUDE.md:** https://github.com/ramalamadingdong/agentic-csa/blob/main/CLAUDE.md
- **tba-mcp-server:** https://github.com/withinfocus/tba-mcp-server
- **AdvantageKit:** https://github.com/Mechanical-Advantage/AdvantageKit
- **AdvantageScope:** https://github.com/Mechanical-Advantage/AdvantageScope
- **Shenzhen Robotics Alliance MapleSim:** https://github.com/Shenzhen-Robotics-Alliance/maple-sim
- **SRA AdvantageKit TalonSwerve Template MapleSim:** https://github.com/Shenzhen-Robotics-Alliance/AdvantageKit-TalonSwerveTemplate-MapleSim
- **OpenAI Codex AGENTS.md:** https://developers.openai.com/codex/agents-md/
- **OpenAI Codex Skills:** https://developers.openai.com/codex/skills/
- **Claude Code memory / CLAUDE.md:** https://docs.claude.com/en/docs/claude-code/memory
- **Claude Code best practices:** https://www.anthropic.com/engineering/claude-code-best-practices
- **WPILib CI:** https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/robot-code-ci.html
- **WPILib simulation:** https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html
- **CTRE Phoenix 6 simulation:** https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/simulation/simulation-intro.html
- **PhotonVision Java simulation:** https://docs.photonvision.org/en/latest/docs/simulation/simulation-java.html
- **PathPlanner Build an Auto:** https://pathplanner.dev/pplib-build-an-auto.html
- **PathPlanner Choreo interop:** https://pathplanner.dev/pplib-choreo.html
- **Choreo docs:** https://choreo.autos/
- **AdvantageKit IO docs:** https://docs.advantagekit.org/data-flow/recording-inputs/io-interfaces/
- **Limelight complete NT API:** https://docs.limelightvision.io/docs/docs-limelight/apis/complete-networktables-api
- **Onshape AI Advisor:** https://www.onshape.com/en/resource-center/tech-tips/onshape-ai-advisor
- **Onshape AI roadmap:** https://www.onshape.com/en/blog/how-onshape-is-bringing-artificial-intelligence-into-cad
- **Onshape REST API overview:** https://onshape-public.github.io/docs/api-intro/
- **Onshape advanced API intro:** https://onshape-public.github.io/docs/api-adv/intro/
- **FeatureScript docs:** https://cad.onshape.com/FsDoc/
- **FRCDesignLib:** https://frcdesign.org/resources/frcdesignlib/
- **FRCDesignApp CD thread:** https://www.chiefdelphi.com/t/frcdesignapp/474775
- **New FRCDesignApp thread:** https://www.chiefdelphi.com/t/introducing-the-new-frcdesignapp/507335
- **Onshape4FRC:** https://onshape4frc.com/
- **Onshape4FRC CD thread:** https://www.chiefdelphi.com/t/onshape4frc/391957
- **Nashoba 1768 2026 repo:** https://github.com/Nashoba-Robotics/2026NashobaRobotics
- **Nashoba vision folder:** https://github.com/Nashoba-Robotics/2026NashobaRobotics/tree/main/src/main/java/frc/robot/subsystems/vision
- **6328 RobotCode2026Public:** https://github.com/Mechanical-Advantage/RobotCode2026Public
- **6328 RobotState.java:** https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/RobotState.java
- **6328 DriveToPose.java:** https://github.com/Mechanical-Advantage/RobotCode2026Public/blob/main/src/main/java/org/littletonrobotics/frc2026/commands/DriveToPose.java
- **3467 posestimator library:** https://github.com/WHS-FRC-3467/Skip-5.16-Perry/tree/master/src/main/java/frc/lib/posestimator
- **5687 2026 repo:** https://github.com/frc5687/2026-Robot
- **125 nu25:** https://gitlab.com/nutrons125/nu25
- **2910 2024 robot code:** https://github.com/FRCTeam2910/2024CompetitionRobot-Public
- **1678 C2024-Public:** https://github.com/frc1678/C2024-Public
- **6995 Robot-2026:** https://github.com/frc6995/Robot-2026
- **PhotonVision docs:** https://docs.photonvision.org/