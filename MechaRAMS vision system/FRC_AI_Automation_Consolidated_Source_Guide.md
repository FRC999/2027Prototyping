# FRC AI Automation Consolidated Source Guide

Prepared for Team 999 / MechaRAMS programming and CAD automation planning.  
Consolidates the two prior documents supplied by Team 999 and adds direct actionable links and per-source analysis.  
Public-source verification/update date: June 27, 2026.

## Executive summary

- The best public FRC AI-agent material I found is **not** mostly from the 2026 world-champion teams. The most reusable AI environment sources are Team 360, Team 1868, Team 6238, Team 6391, Team 4050, Team 7660, Team 8092, and FRC-specific tooling repositories.
- The best elite performance/code/CAD references are different: Team 2910, Team 254, Team 4414, Team 1323, Team 6328, Team 6329, Team 1768, Team 125, and Team 4206. These should shape what Team 999 asks AI to produce, even where no AI contract files are public.
- For HighTide specifically: I found direct links to the GitHub org, 2026 technical binder, Chief Delphi binder/Q&A, reveal/Behind-the-Bumpers/interview videos, TBA, and FIRST event page. I did **not** find a current public 2025/2026 robot-code repo or AGENTS/CLAUDE file. So HighTide is a strategy/design/process benchmark, not a copyable robot-code source.
- CAD automation should stay centered on **Onshape**, FeatureScript, REST API, FRCDesignLib/FRCDesignApp, Onshape4FRC, and deterministic geometry calculators. Onshape AI Advisor can help with guidance, but it should not be treated as an autonomous FRC CAD designer.

## Recommended mining order

1. **Team 360 RainMaker26** - copy/adapt AI workflow, AGENTS.md, CLAUDE.md, and Claude commands.
2. **Team 1868 frc-claude-config** - copy/adapt the multi-agent Claude structure: reviewer, safety auditor, subsystem architect, vision reviewer, skills, commands.
3. **Team 6238 and Team 6391** - mine serious AdvantageKit/sim/replay Claude guidance.
4. **SRA MapleSim template + AdvantageKit + wpilog-mcp/wpilib-agent-tools** - build the safe evidence loop: AI change -> build -> sim -> log/replay analysis -> human review.
5. **Team 6328 + 2910 + 254** - use as high-quality code/sim/autonomous/CAD-release benchmarks.
6. **4414 + 1323 + 1768 + 6329 + 125 + WildStang** - use as performance, design, manufacturing, mechanism, and CAD-process benchmarks.

## Source-profile format

Each profile below answers: what links exist, what type of material it is, what Team 999 can extract from it, and what I did **not** find. This is intentionally more direct than the earlier “checked / benchmark” wording.

## Detailed team and source profiles

### Team 360 - The Revolution

**Priority:** Highest priority for direct AI-agent workflow  
**Competitive/source context:** Strong 2026 team, Pacific Northwest district rank #6 in the prior ranking pass; not an Einstein-style performance benchmark, but the most useful public FRC AI workflow example found.  
**Team 999 takeaway:** Mine this first for Team 999 AGENTS.md, CLAUDE.md, .claude commands, safe AI code-review rules, Gradle/sim/test commands, and mentor-controlled review practices.

**Direct links:**

- Robot code repo - RainMaker26 (Code / AI environment): https://github.com/FRCTeam360/RainMaker26
- AGENTS.md (Codex/agent contract): https://github.com/FRCTeam360/RainMaker26/blob/main/AGENTS.md
- CLAUDE.md (Claude project memory): https://github.com/FRCTeam360/RainMaker26/blob/main/CLAUDE.md
- .claude commands folder (Reusable Claude commands): https://github.com/FRCTeam360/RainMaker26/tree/main/.claude/commands
- FSM command (FSM extraction / Mermaid diagrams): https://github.com/FRCTeam360/RainMaker26/blob/main/.claude/commands/fsm.md
- 2026 Open Alliance build thread (Discussion / AI use notes): https://www.chiefdelphi.com/t/frc-360-the-revolution-2026-build-thread-open-alliance/510290

**What Team 999 can use:**

- Use their AGENTS.md as the closest public model for a strict FRC Java robot-code agent contract.
- Copy the idea of a canonical agent document plus Claude-specific memory, rather than putting conflicting rules in multiple places.
- Adapt their command list into Team 999 commands: sim smoke test, FSM map, vision-pose review, trajectory-auto review, and safety audit.
- Use their OA discussion as evidence that AI works best as first-pass reviewer and pair programmer, not as an unchecked merger/deployer.

**Limitations / caution:** The team is not a direct proxy for 4414/1323/2910 level on-field performance, so use it for AI process, not final architecture/performance expectations.

### Team 1868 - Space Cookies / FRC Claude Code Config

**Priority:** Highest priority for reusable Claude/Codex environment structure  
**Competitive/source context:** Strong team with a public Claude Code configuration package; this is more important as an AI-environment artifact than as a robot-code benchmark.  
**Team 999 takeaway:** Use this as the starting point for Team 999-specific Claude agents, commands, contexts, and skills.

**Direct links:**

- FRC Claude Code config repo (AI environment package): https://github.com/Feramirr/frc-claude-config
- Rules directory (Rules / policy): https://github.com/Feramirr/frc-claude-config/tree/main/rules
- Agents directory (Specialized reviewers): https://github.com/Feramirr/frc-claude-config/tree/main/agents
- Commands directory (Claude slash commands): https://github.com/Feramirr/frc-claude-config/tree/main/commands
- Skills directory (FRC development skills): https://github.com/Feramirr/frc-claude-config/tree/main/skills

**What Team 999 can use:**

- Create Team 999 versions of safety reviewer, code reviewer, subsystem architect, vision reviewer, and pre-merge reviewer agents.
- Use their directory structure to avoid a single giant prompt file.
- Create separate contexts for WPILib, CTRE Phoenix 6, Limelight/QuestNav, PathPlanner, AdvantageKit/replay, and CAD/Onshape.

**Limitations / caution:** This is not a top-team robot-code release. It is valuable because it is a clean AI configuration model.

### Team 6238 - Popcorn Penguins

**Priority:** High priority direct AI + serious code architecture  
**Competitive/source context:** 2026 California district rank #16 in the prior ranking pass; stronger competitive context than many direct AI-template examples.  
**Team 999 takeaway:** Good bridge between Claude guidance and AdvantageKit-style robot architecture, including simulation/replay workflow. Also useful for CAD/manufacturing automation through PenguinCAM.

**Direct links:**

- 2026-Robot repo (Robot code): https://github.com/6238/2026-Robot
- CLAUDE.md (Claude project memory): https://github.com/6238/2026-Robot/blob/main/CLAUDE.md
- PenguinCAM (Onshape-to-CNC / manufacturing automation): https://github.com/6238/PenguinCAM
- PenguinCAM live app (Manufacturing workflow app): https://penguincam.popcornpenguins.com

**What Team 999 can use:**

- Use their CLAUDE.md for AdvantageKit IO/sim/replay instructions and command checklist patterns.
- Study their MapleSim/AdvantageKit integration ideas when building Team 999 full-device simulation.
- Study PenguinCAM as an example of moving from Onshape CAD data to manufacturing planning, which is very relevant to Team 999 CAD weakness.

**Limitations / caution:** The public source is very useful, but do not assume their hardware constants or architecture match Team 999 swerve/Limelight/QuestNav stack.

### Team 6391 - Bearbotics

**Priority:** High priority direct Claude + AdvantageKit-style architecture  
**Competitive/source context:** 2026 regional finalist/autonomous-award context from prior pass; not a world finalist, but useful public AI guidance.  
**Team 999 takeaway:** Use for detailed Claude guidance around AdvantageKit IO separation, real/sim/replay implementations, Phoenix 6 conventions, and log-analysis thresholds.

**Direct links:**

- 2026-6391-Rebuilt repo (Robot code): https://github.com/6391-Ursuline-Bearbotics/2026-6391-Rebuilt
- CLAUDE.md (Claude project memory): https://github.com/6391-Ursuline-Bearbotics/2026-6391-Rebuilt/blob/main/CLAUDE.md

**What Team 999 can use:**

- Adapt their AdvantageKit IO pattern notes into Team 999 robot-contract.md and simulation-contract.md.
- Review their vision/localization and log-analysis guidance for creating AI review checks.
- Use as an example for students because it is closer to an attainable public repo than an opaque world champion codebase.

**Limitations / caution:** Good AI/code pattern source, but not an elite strategic benchmark.

### Team 4050 - Biohazard

**Priority:** Medium-high priority template / repo-structure example  
**Competitive/source context:** Lower 2026 competitive strength in prior pass, but useful because the repo contains practical AI/developer-environment artifacts.  
**Team 999 takeaway:** Use for repo skeleton ideas: devcontainer, docs folder, scripts, AGENTS.md, CLAUDE.md, wiring-diagram.md, AdvantageScope layout.

**Direct links:**

- 2026-rebuilt repo (Robot code / template): https://github.com/Team4050/2026-rebuilt
- AGENTS.md (Agent contract): https://github.com/Team4050/2026-rebuilt/blob/main/AGENTS.md
- CLAUDE.md (Claude memory): https://github.com/Team4050/2026-rebuilt/blob/main/CLAUDE.md
- frc-java-template (Reusable Java template): https://github.com/Team4050/frc-java-template

**What Team 999 can use:**

- Use as a starter layout reference, especially for devcontainer and documentation structure.
- Good example of keeping CLAUDE.md short and pointing agents to a more canonical rules file.

**Limitations / caution:** Template value is higher than competitive-benchmark value.

### Team 7660 - Byting Irish

**Priority:** Medium priority AI/robot-code example  
**Competitive/source context:** Moderate competitive result in prior pass, but directly useful because it has public agent instructions and autonomous/vision notes.  
**Team 999 takeaway:** Mine for small, practical AGENTS.md patterns around build/deploy checks and robot-log handling.

**Direct links:**

- robot-2026 repo (Robot code): https://github.com/FRC7660/robot-2026
- AGENTS.md (Agent contract): https://github.com/FRC7660/robot-2026/blob/main/AGENTS.md

**What Team 999 can use:**

- Study their hybrid autonomous discussion and AdvantageKit replay notes.
- Use their AGENTS.md as an example of a compact, operational agent guide.

**Limitations / caution:** Treat as a pattern sample, not a top-performance architecture standard.

### Team 8092 - GOAT

**Priority:** Medium-low direct AI-use example  
**Competitive/source context:** Lower competitive benchmark in prior pass, but valuable as public evidence of Claude/AGENTS-style FRC use.  
**Team 999 takeaway:** Useful to scan for how another team organizes AGENTS.md, CLAUDE.md, ROBOT.md, and .claude materials.

**Direct links:**

- 8092-2026 repo (Robot code / AI files): https://github.com/GOAT-8092/8092-2026
- AGENTS.md (Agent contract): https://github.com/GOAT-8092/8092-2026/blob/main/AGENTS.md
- CLAUDE.md (Claude memory): https://github.com/GOAT-8092/8092-2026/blob/main/CLAUDE.md
- ROBOT.md (Robot description): https://github.com/GOAT-8092/8092-2026/blob/main/ROBOT.md

**What Team 999 can use:**

- Use for naming/organization ideas only.
- Check whether their ROBOT.md-style summary would help Team 999 give AI the robot hardware truth in one place.

**Limitations / caution:** Some content is Turkish; lower priority than 360/1868/6238/6391.

### Team 2910 - Jack in the Bot

**Priority:** Highest priority elite code/CAD/performance benchmark  
**Competitive/source context:** 2026 Newton Division winner and Einstein finalist; 2026 Newton qualification rank #1 in prior ranking table; also strong 2025 division ranking.  
**Team 999 takeaway:** Use for elite code, controls, autonomous, CAD/technical binder, and design benchmark. No public AGENTS.md/CLAUDE.md was found in the checked public repo, so this is a performance/architecture source rather than an AI-contract source.

**Direct links:**

- 2026 robot code repo (Robot code): https://github.com/FRCTeam2910/2026CompetitionRobot-Public
- Team 2910 GitHub org (GitHub org): https://github.com/FRCTeam2910
- 2026 code release discussion (Chief Delphi / Q&A): https://www.chiefdelphi.com/t/team-2910-code-release-2026/521778
- 2026 CAD and Tech Binder release (CAD / technical binder / reveal links): https://www.chiefdelphi.com/t/2910-cad-and-tech-binder-release-2026/521705
- Team 2910 resources page (Team resources): https://frcteam2910.org/resources/
- Team 2910 TBA page (Results context): https://www.thebluealliance.com/team/2910

**What Team 999 can use:**

- Study their current public robot code as an elite target architecture for Team 999 AI-generated code quality.
- Study the CAD/tech binder release thread for mechanism packaging, CAD organization, and what a high-quality public release looks like.
- Use their code-release Q&A to identify subtle autonomous/controls ideas, not just file structure.
- Treat their code as an example for AI review prompts: “compare this Team 999 subsystem against high-level patterns seen in elite robot code.”

**Limitations / caution:** Public AI contract files were not found in the checked 2026 repo. Some CAD/tech-binder links are embedded in Chief Delphi/Google Drive/Onshape and may require a browser login or clicking through the CD post.

### Team 254 - The Cheesy Poofs

**Priority:** Highest priority elite code/technical-binder benchmark  
**Competitive/source context:** Perennial elite team; 2026 Curie qualification rank #4 in prior table and 2025 Milstein qualification rank #4. Public 2025 code and technical binder are very useful.  
**Team 999 takeaway:** Use for elite software architecture, simulation/logging, autonomous design, and technical-binder quality. No current 2026 AGENTS.md/CLAUDE.md found in this pass.

**Direct links:**

- Team 254 GitHub org (GitHub org): https://github.com/Team254
- FRC-2025-Public repo (Robot code): https://github.com/Team254/FRC-2025-Public
- FRC-2024-Public repo (Robot code): https://github.com/Team254/FRC-2024-Public
- 2025 technical binder/code/Q&A thread (Technical binder / code / Q&A): https://www.chiefdelphi.com/t/team-254-presents-2025-undertow-technical-binder-code-q-a/506115
- 2025 technical binder PDF (Technical binder PDF): https://media.team254.com/2025/09/985607eb-2025-Tech-Binder-254.pdf
- Team 254 TBA page (Results context): https://www.thebluealliance.com/team/254

**What Team 999 can use:**

- Use as a code quality and systems-integration benchmark, especially IO separation, simulation/replay, autonomous workflow, and pose/path handling.
- Use their technical binder as an example of how to document design goals, alpha-bot learning, autonomous strategy, and simulation decisions.
- For Team 999 AI prompts, use 254 as a “what good looks like” reference, not as a file to blindly clone.

**Limitations / caution:** The best directly verified public code is 2025, not a current 2026 AI contract.

### Team 4414 - HighTide

**Priority:** Highest priority elite performance/CAD-process benchmark, not code template  
**Competitive/source context:** 2026 Houston Championship winner with Team 1323; 2026 Daly qualification rank #1 and California district rank #1 in the prior table.  
**Team 999 takeaway:** This is exactly where the earlier document was too vague. For HighTide, I found strong public technical-binder/discussion/video material, but I did not find a current public 2025/2026 robot-code repo or AGENTS/CLAUDE contract in the checked public sources.

**Direct links:**

- Team 4414 GitHub org (GitHub org; appears older/not current robot code): https://github.com/Team4414
- Alternate GitHub org casing (GitHub org; checked as public org): https://github.com/team4414
- 2026 Tech Binder discussion - RIPCURRENT (Chief Delphi / technical binder Q&A): https://www.chiefdelphi.com/t/team-4414-hightide-2026-tech-binder-ripcurrent/519602
- 2026 technical binder site (Technical binder): https://2026.team4414.com/
- 2026 robot reveal video (Video): https://www.youtube.com/watch?v=9VpVZiApRFw
- Behind the Bumpers - 4414 HighTide REBUILT Champions (Video): https://www.youtube.com/watch?v=XM7l9Z1HiDs
- Championship Winner Interview (Video): https://www.youtube.com/watch?v=Xnf7X2Bf8BQ
- Team website (Team site): https://www.team4414.com/
- Team 4414 TBA page (Results context): https://www.thebluealliance.com/team/4414
- Team 4414 FIRST event page (Official results): https://frc-events.firstinspires.org/team/4414

**What Team 999 can use:**

- Use the binder and videos for robot architecture, iteration process, manufacturing/process tooling ideas, and high-level strategy.
- The CD discussion contains useful comments about their web/front-end/binder tooling and manufacturing process tooling such as TideParts; this is directly relevant to CAD/process automation for Team 999.
- Use their public material to set expectations for what an elite release looks like: visual overlays, iteration history, technical explanation, and clear presentation.

**Limitations / caution:** No current public robot code repo or direct AI contract was found. Do not list HighTide as a code source unless a new public repo appears or someone from the team shares it.

### Team 1323 - MadTown Robotics

**Priority:** Highest priority elite performance/mechanism benchmark, limited public code  
**Competitive/source context:** 2026 Houston Championship winner with 4414; 2026 Daly qualification rank #2 and 2025 Newton qualification rank #1 in prior table.  
**Team 999 takeaway:** Use for performance, packaging, mechanism ideas, and technical-binder discussions. Public current robot code appears intentionally limited/unreleased based on checked sources.

**Direct links:**

- Team 1323 GitHub org (GitHub org; mostly older public repos visible): https://github.com/team1323
- Team website (Team site): https://team1323.com/
- Knowledge base / wiki (Team resource site): https://team1323.com/wiki/
- 2025 Tech Binder discussion (Technical binder Q&A): https://www.chiefdelphi.com/t/1323-2025-tech-binder/500435
- 2025 Tech Binder discussion - code release comment (Public explanation about not releasing code): https://www.chiefdelphi.com/t/1323-2025-tech-binder/500435/38
- 2025 robot reveal discussion (Robot reveal / discussion): https://www.chiefdelphi.com/t/1323-madtown-robot-reveal/495568
- Team 1323 TBA 2026 page (Results context): https://www.thebluealliance.com/team/1323/2026

**What Team 999 can use:**

- Study mechanism packaging, CAD/weight/process discussion, and high-level design decisions.
- Use as a high-performance reference for “what did they prioritize?” rather than as a software template.
- Their technical-binder thread has useful discussion about manufacturing, printed parts, weight optimization, and iteration timing.

**Limitations / caution:** No current 2025/2026 robot code or AI contract located. Existing public discussion suggests they may not consider code release valuable currently.

### Team 6328 - Mechanical Advantage

**Priority:** Highest priority New England code/sim/logging/CAD resource  
**Competitive/source context:** New England elite; 2026 public robot code and Open Alliance thread; top source for AdvantageKit/AdvantageScope and Onshape4FRC.  
**Team 999 takeaway:** Use 6328 as the foundation for Team 999 evidence-based AI workflow: logging, replay, sim, AdvantageScope review, and Onshape/FRC CAD training.

**Direct links:**

- RobotCode2026Public (Robot code): https://github.com/Mechanical-Advantage/RobotCode2026Public
- Mechanical Advantage GitHub org (GitHub org): https://github.com/Mechanical-Advantage
- 2026 Open Alliance build thread (Open Alliance / development thread): https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2026-build-thread/509595
- AdvantageKit (Logging/replay framework): https://github.com/Mechanical-Advantage/AdvantageKit
- AdvantageScope (Telemetry viewer): https://github.com/Mechanical-Advantage/AdvantageScope
- Onshape4FRC (FRC Onshape training): https://onshape4frc.com/
- 2026 reveal video (Video): https://www.youtube.com/watch?v=uy4nd5wp0PU
- Team resources page (Team resources): https://www.littletonrobotics.org/about-6328/resources-2/
- Team 6328 TBA 2026 page (Results context): https://www.thebluealliance.com/team/6328/2026

**What Team 999 can use:**

- Use AdvantageKit and AdvantageScope as the basis for AI-verifiable sim/replay workflows.
- Use RobotCode2026Public and earlier RobotCode repos to teach AI what a clean IO/sim/replay architecture looks like.
- Use Onshape4FRC as the best FRC-specific CAD training/reference base for students.
- Use the OA thread to see how design/code decisions evolve, not just the final snapshot.

**Limitations / caution:** No direct AGENTS.md/CLAUDE.md was found in the checked public repo. Their value is infrastructure and architecture, not AI prompt contracts.

### Team 6329 - Bucks' Wrath

**Priority:** High priority New England performance/mechanism benchmark  
**Competitive/source context:** 2026 Hopper Division winner; 2026 Hopper qualification rank #3 and 2025 Curie rank #8 in prior table.  
**Team 999 takeaway:** Use for local performance expectations and mechanism inspiration, especially spindexter/drum/dye-rotor style packaging discussions. Public robot code was not found in this pass.

**Direct links:**

- 2026 robot reveal discussion (Reveal / mechanism discussion): https://www.chiefdelphi.com/t/6329-bucks-wrath-robot-reveal-2026-roman/515342
- Team website (Team site): https://bucksporths.wixsite.com/frc6329
- Team 6329 TBA page (Results context): https://www.thebluealliance.com/team/6329

**What Team 999 can use:**

- Study robot reveal/mechanism discussion for feed/index/shooter packaging ideas.
- Use as a local New England benchmark when setting Team 999 performance goals.

**Limitations / caution:** No current public code or AI contract located.

### Team 1768 - Nashoba Robotics

**Priority:** High priority New England performance/mechanism benchmark  
**Competitive/source context:** 2026 New England district rank #1 in prior pass; 2026 Galileo qualification rank #5 and 2025 Hopper rank #1.  
**Team 999 takeaway:** Use for local elite robot architecture and mechanism strategy; public current code/AI contract not found.

**Direct links:**

- Nashoba Robotics GitHub org (GitHub org; current 2026 code not found): https://github.com/Nashoba-Robotics
- 2026 robot reveal discussion (Reveal / video links): https://www.chiefdelphi.com/t/team-1768-nashoba-robotics-2026-robot-reveal-nightshift/519386
- Team website (Team site): https://nashobarobotics.com/
- Team 1768 TBA page (Results context): https://www.thebluealliance.com/team/1768

**What Team 999 can use:**

- Study mechanism layout from the reveal thread: drum shooter, intake/hopper concepts, and packaging tradeoffs.
- Use as a local benchmark for what a high-performing New England robot looks like.

**Limitations / caution:** The GitHub org has older/currently limited repos; no current AI contract found.

### Team 125 - NUTRONS

**Priority:** High priority New England design/mechanism benchmark  
**Competitive/source context:** 2026 Curie qualification rank #10 and New England rank #7 in prior table; historically strong design/prototyping culture.  
**Team 999 takeaway:** Use for mechanism discussion, dye-rotor/bopper packaging clues, and prior code/CAD release patterns.

**Direct links:**

- FRC125 GitHub org (GitHub org; older repos visible): https://github.com/FRC125
- Team website (Team site): https://www.nutrons.com/
- Team YouTube channel (Videos): https://www.youtube.com/nutronsfrc125
- 2026 reveal discussion (Reveal / dye-rotor discussion): https://www.chiefdelphi.com/t/frc125-the-nutrons-2026-reveal/516273
- 2024 code and CAD release (Code/CAD release example): https://www.chiefdelphi.com/t/frc125-the-nutrons-2024-code-and-cad-release/467670
- Team 125 TBA page (Results context): https://www.thebluealliance.com/team/125

**What Team 999 can use:**

- Study the 2026 reveal discussion for dye-rotor packaging, modified swerve/module-space tradeoffs, and bumper hopper ideas.
- Study the 2024 release as a model for how to publish CAD/code with Onshape, STEP, and source links.
- Use as a local design benchmark and as inspiration for Team 999 CAD automation checks around geometry/perimeter clearance.

**Limitations / caution:** No current 2026 code/AI contract located.

### Team 4206 - Robo Vikes

**Priority:** Medium-high elite playoff/source watchlist  
**Competitive/source context:** 2026 Newton Division winner with Einstein finalist alliance; Texas district rank #11 in prior pass.  
**Team 999 takeaway:** Useful as a top-playoff benchmark and for general guides/utilities through Battleaid; no high-confidence current AI contract found.

**Direct links:**

- FRC 4206 GitHub org (GitHub org): https://github.com/frc4206
- Battleaid (Utilities/guides/lessons): https://github.com/frc4206/battleaid
- Team 4206 TBA page (Results context): https://www.thebluealliance.com/team/4206

**What Team 999 can use:**

- Monitor for current code releases and use Battleaid as a general teaching/tooling reference.
- Use their 2026 result as another example that elite playoff performance does not necessarily imply a public AI setup.

**Limitations / caution:** No current robot AI contract found in public pass.

### WildStang Robotics Program - Teams 111/112

**Priority:** Highest priority mechanism/CAD concept source for dye rotor  
**Competitive/source context:** Team 111 was a strong 2026 team but the main value here is the detailed Open Alliance mechanism discussion, not ranking.  
**Team 999 takeaway:** Use as a primary reference for dye-rotor geometry, risks, current draw, packaging, and why a design-assistant must do deterministic math instead of just “inventing CAD.”

**Direct links:**

- 2026 WildStang build blog - dye rotor geometry page (Open Alliance / mechanism math): https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853?page=4
- Dye rotor current draw discussion (Open Alliance / test results): https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853/216
- Dye rotor installed/manual test (Open Alliance / prototype): https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853?page=5
- Turret wiring / limited rotation discussion (Open Alliance / turret packaging caution): https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853?page=8

**What Team 999 can use:**

- Use their geometry discussion to seed Team 999 turret/dye-rotor calculators.
- Use their current-draw and feed-ratio testing as an example of what AI must not guess: prototype data matters.
- Use their turret wiring/rotation limitation as a concrete CAD-check case: even if geometry works, wires/motors/climb can limit rotation.

**Limitations / caution:** Mechanism-specific source, not an AI coding environment source.

### Shenzhen Robotics Alliance / MapleSim / AdvantageKit TalonSwerve Template

**Priority:** Highest priority simulation source  
**Competitive/source context:** Community simulation infrastructure; not an FRC team ranking item.  
**Team 999 takeaway:** Use for full-device sim, especially if Team 999 wants AI-generated code to be tested in a VM/container before deployment.

**Direct links:**

- AdvantageKit TalonSwerve Template with MapleSim (Simulation template): https://github.com/Shenzhen-Robotics-Alliance/AdvantageKit-TalonSwerveTemplate-MapleSim
- Template AGENTS.md (Agent contract): https://github.com/Shenzhen-Robotics-Alliance/AdvantageKit-TalonSwerveTemplate-MapleSim/blob/main/AGENTS.md
- MapleSim repo (Simulation library): https://github.com/Shenzhen-Robotics-Alliance/maple-sim
- MapleSim CLAUDE.md (Claude guidance): https://github.com/Shenzhen-Robotics-Alliance/maple-sim/blob/main/CLAUDE.md

**What Team 999 can use:**

- Use as a simulation target for Team 999 swerve, drivetrain, and autonomous changes.
- Use the AGENTS.md warnings about FRC framework knowledge as a template for Team 999 agent rules.
- Pair this with AdvantageKit logs and replay so the AI has evidence from sim/logs before it proposes robot changes.

**Limitations / caution:** Needs careful integration with Team 999 hardware conventions; not a drop-in solution.

## FRC-specific AI / MCP / log-analysis tooling

These are not team-performance benchmarks, but they may be essential for making AI-generated code safe and evidence-driven.

- **wpilib-agent-tools**: Sandbox-first Codex/Claude/Cursor harness for WPILib simulation, NT4 recording, WPILOG analysis, and patch review. Use as a design pattern for a VM/container-based Team 999 AI test harness.  URL: https://github.com/edanliahovetsky/wpilib-agent-tools
- **wpilog-mcp - Team 2363 Triple Helix**: MCP server for asking natural-language questions about WPILib/AdvantageKit logs. Useful for post-sim and post-match analysis.  URL: https://github.com/TripleHelixProgramming/wpilog-mcp
- **frc-rag-mcpserver**: FRC-specific RAG/MCP idea to force assistants to query version-specific docs before answering WPILib questions.  URL: https://github.com/ramalamadingdong/frc-rag-mcpserver
- **agentic-csa**: MCP server aggregating WPILib, REV, CTRE Phoenix, Redux, and PhotonVision docs. Good model for Team 999 local docs assistant.  URL: https://github.com/ramalamadingdong/agentic-csa
- **agentic-csa CLAUDE.md**: Example of documenting MCP server architecture and dev commands for Claude.  URL: https://github.com/ramalamadingdong/agentic-csa/blob/main/CLAUDE.md
- **tba-mcp-server**: MCP server for The Blue Alliance data; useful for scouting, schedule/ranking context, and autonomous/strategy research.  URL: https://github.com/withinfocus/tba-mcp-server

## Official AI, WPILib, vendor, and framework documentation

These links should be referenced directly from Team 999 AGENTS.md / CLAUDE.md so the AI assistant does not rely on stale internal memory.

- **AGENTS.md open format**: General AGENTS.md convention used by coding agents.  URL: https://agents.md/
- **OpenAI Codex AGENTS.md docs**: Official Codex project-instruction behavior.  URL: https://developers.openai.com/codex/agents-md/
- **OpenAI Codex Skills docs**: Official skill packaging and invocation guidance.  URL: https://developers.openai.com/codex/skills/
- **Claude Code memory / CLAUDE.md**: Official Claude project-memory guidance.  URL: https://docs.claude.com/en/docs/claude-code/memory
- **Claude Code best practices**: Official advice on check commands, iteration, and review.  URL: https://www.anthropic.com/engineering/claude-code-best-practices
- **WPILib robot-code CI**: Official GitHub Actions / WPILib Docker CI guidance.  URL: https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/robot-code-ci.html
- **WPILib robot simulation**: Official desktop simulation docs.  URL: https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html
- **CTRE Phoenix 6 simulation**: Official CTRE SimState/device simulation docs.  URL: https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/simulation/simulation-intro.html
- **PhotonVision Java simulation**: Official PhotonVision simulation docs.  URL: https://docs.photonvision.org/en/latest/docs/simulation/simulation-java.html
- **PathPlanner Build an Auto**: AutoBuilder and autonomous command patterns.  URL: https://pathplanner.dev/pplib-build-an-auto.html
- **PathPlanner pathfinding**: Pathfinding caveats and navgrid/preplanned-path patterns.  URL: https://pathplanner.dev/pplib-pathfinding.html
- **AdvantageKit IO docs**: Core IO separation pattern for sim/replay.  URL: https://docs.advantagekit.org/data-flow/recording-inputs/io-interfaces/
- **AdvantageScope live sources**: Connect to simulator/NT4/log sources.  URL: https://docs.advantagescope.org/more-features/live-sources/
- **Limelight NetworkTables / MegaTag2 / LL4 API**: Important for Team 999 Limelight orientation/pose-source rules.  URL: https://docs.limelightvision.io/docs/docs-limelight/apis/complete-networktables-api

## CAD / Onshape automation links

- **Onshape AI Advisor**: Native Onshape AI help/Q&A. Useful for guidance, not autonomous FRC robot design.  URL: https://www.onshape.com/en/resource-center/tech-tips/onshape-ai-advisor
- **Onshape AI in CAD roadmap article**: Onshape AI roadmap: FeatureScript autocomplete, AI search, rendering, future agent directions.  URL: https://www.onshape.com/en/blog/how-onshape-is-bringing-artificial-intelligence-into-cad
- **Onshape REST API overview**: Practical path for scripts to read/write metadata, export, and integrate design automation.  URL: https://onshape-public.github.io/docs/api-intro/
- **Onshape API advanced intro**: More API guidance.  URL: https://onshape-public.github.io/docs/api-adv/intro/
- **FeatureScript documentation**: Custom features, reusable geometry helpers, and parametric automation.  URL: https://cad.onshape.com/FsDoc/
- **FRCDesignLib**: FRC COTS component library / design resources for Onshape.  URL: https://frcdesign.org/resources/frcdesignlib/
- **FRCDesignApp Chief Delphi thread**: FRC Onshape app/plugin discussion.  URL: https://www.chiefdelphi.com/t/frcdesignapp/474775
- **Introducing the new FRCDesignApp**: Updated FRCDesignApp discussion.  URL: https://www.chiefdelphi.com/t/introducing-the-new-frcdesignapp/507335
- **Onshape4FRC**: FRC-specific Onshape curriculum/resources.  URL: https://onshape4frc.com/
- **Onshape4FRC Chief Delphi thread**: Original community discussion.  URL: https://www.chiefdelphi.com/t/onshape4frc/391957

## CAD automation analysis for Team 999

### What is realistic

A useful CAD assistant can take a prompt such as “game piece is a ball of diameter d; compare turret, fixed shooter, and dye rotor; suggest ring diameter, turret center location, motor keepouts, and safe rotation limits.” But the LLM should be a front end for a deterministic engineering workflow, not a black-box designer.

Recommended architecture:

1. AI converts the design request into a structured mechanism specification.
2. A calculator/checker computes geometry, perimeter violations, ball compression, clearances, motor keepouts, and safe rotation ranges.
3. Onshape variables/configurations and FeatureScript generate/update simple controlled geometry.
4. The assistant produces a review report with assumptions, risks, required prototypes, and pass/fail checks.
5. Students and mentors review before anything becomes final CAD or build documentation.

### Turret / dye-rotor geometry checks Team 999 should automate

```text
C = turret center = (cx, cy)
p_i = any point on rotating turret assembly expressed relative to C
q_i(theta) = C + R(theta) * p_i
For every theta in the allowed rotation range, q_i(theta) must remain inside the legal boundary
or inside the current game's allowed extension envelope.
```

For 360-degree rotation, the simple first-pass clearance check is:

```text
r_max = max(||p_i||) over all rotating points: motors, belts, shooter plates, sensors, wire loops
cx must be at least r_max from left/right legal limits
cy must be at least r_max from front/back legal limits
If impossible, restrict rotation range and enforce hard stops/software limits.
```

First-pass turret ring sizing:

```text
ring_ID >= max(game_piece_path_width + 2*clearance, shooter_throat_envelope, wire_passage_requirement)
ring_OD = ring_ID + 2*(bearing/ring width + fastener edge distance + manufacturing margin)
```

### Dye rotor vs. conventional turret

| Architecture | When it may help | Risk flags | AI output should include |
| --- | --- | --- | --- |
| Conventional fixed feeder + turreted shooter | Simpler than dye rotor; good if limited-angle turret is enough. | Motors/shooter plates can swing outside perimeter; cable wrap; off-center feed jams. | Turret-center candidates, allowed rotation range, ring size, hard stops, cable routing, motor-envelope map. |
| Fixed shooter + rotating feeder/indexer | Useful if shooter is large/heavy and feeder can aim/phase balls. | More controls complexity; still may collide with frame/perimeter. | Feed timing, collision checks, compression, sensors. |
| Dye rotor / drum feed with center exit | Can centralize exit and simplify shooter/turret feed packaging. | High mechanical complexity; power transfer, jams, current draw, overfeed tuning, serviceability. | Drum diameter range, feed roller radius, up-feed ratio, current/jam risk, prototype plan. |
| No turret / swerve-aimed robot | Often simplest if drivetrain aim is good enough. | Requires strong localization/control and may be less tolerant while moving. | Vision/localization requirements, alignment time, shooting-map requirements. |

The WildStang links above are the best public dye-rotor geometry/process source found. Their discussion is valuable because it shows both the geometry and the prototype/testing risks; this is the kind of mechanism where AI must be forced to expose assumptions and run deterministic checks.

## Team 999 implementation map

| Team 999 artifact | Seed from | Purpose |
| --- | --- | --- |
| `AGENTS.md` | Team 360, Team 4050, SRA template, official AGENTS docs | Canonical Codex/agent contract. |
| `CLAUDE.md` | Team 360, Team 6238, Team 6391, Claude docs | Claude-specific project memory; should point to AGENTS.md and docs/ai. |
| `.claude/agents` / `.claude/commands` | Team 1868, Team 360 | Reviewers, safety audits, sim smoke tests, FSM mapping, vision review. |
| `docs/ai/robot-contract.md` | Team 360/6238/6391 patterns + Team 999 hardware truth | CAN IDs, bus names, frames, coordinate systems, mechanism inventory. |
| `docs/ai/simulation-contract.md` | SRA MapleSim, 6328/AdvantageKit | What is simulated, what is replayable, known fidelity limits. |
| `docs/ai/vision-contract.md` | Team 999 practice + Limelight docs + 6391/6238 patterns | Limelight orientation, QuestNav frames, MegaTag2 rules, rejection thresholds. |
| CI workflow | WPILib CI docs, Team 360/6238 commands | Build/test/static check before AI changes are accepted. |
| Log/replay assistant | AdvantageKit, AdvantageScope, wpilog-mcp, wpilib-agent-tools | AI must inspect logs/sim evidence before recommending changes. |
| CAD assistant | Onshape API, FeatureScript, Onshape4FRC, FRCDesignLib, PenguinCAM, WildStang | Parametric layout, turret/dye-rotor checks, COTS insertion, manufacturing workflow. |

## Authentication / downloads you may need later

- **GitHub public repos** above did not require authentication for this pass, but GitHub login may be useful for heavy code search, cloning, or viewing large file trees without rate limits.
- **2910 CAD / tech binder links** are embedded in the Chief Delphi release thread. The Onshape CAD and Google Drive binder links may require normal browser login or accepting Google/Onshape prompts. Start here: https://www.chiefdelphi.com/t/2910-cad-and-tech-binder-release-2026/521705
- **125 2024 code/CAD release** has embedded Onshape, STEP, and GitLab links. Start here: https://www.chiefdelphi.com/t/frc125-the-nutrons-2024-code-and-cad-release/467670
- **4414 2026 binder** should be directly available at https://2026.team4414.com/ and via the CD thread. If the binder site has rendering/login issues, use the CD thread to reach the current link: https://www.chiefdelphi.com/t/team-4414-hightide-2026-tech-binder-ripcurrent/519602
- **Team 999 Onshape automation** will require your own Onshape document access and API/OAuth credentials only when we start automating Team 999 CAD. Do not share private keys or secrets in chat; export/download documents or provide read-only public links when possible.

## Watch list / lower-priority sources

- **Team488 Programming-Docs AGENTS.md**: Useful team-programming documentation agent file, but not robot code.  URL: https://github.com/Team488/Programming-Docs/blob/main/AGENTS.md
- **Team488 SeriouslyCommonLib**: Reusable FRC Java framework/HAL separation ideas.  URL: https://github.com/Team488/SeriouslyCommonLib
- **Team846/gibbon**: Additional 2026 robot-code repo with AGENTS.md; not primary until deeper analysis.  URL: https://github.com/Team846/gibbon
- **frc6377/rebuilt_2026**: README referenced copilot instructions, but direct file check previously failed; caution source.  URL: https://github.com/frc6377/rebuilt_2026
- **frc-6045/2026-naan-REALEST**: Additional 2026 repo with CLAUDE.md; lower priority.  URL: https://github.com/frc-6045/2026-naan-REALEST
- **Team 834 / StrategyBoard2026 note**: Potential strategy-board AI idea from earlier pass; needs re-check before relying on it.  URL: https://github.com/search?q=FRC+834+StrategyBoard2026&type=repositories

## Negative-result notes

These are not proof that files do not exist. They mean I did not find the artifact in this public GitHub/Chief Delphi/web pass.

- I did not find current public AGENTS.md / CLAUDE.md / Codex Skills for 4414, 1323, 2910, 254, 6328, 6329, 1768, 125, or 4206 in the checked materials.
- I did not find a current public 2025/2026 robot-code repo for HighTide/4414; the public value is binder, videos, discussion, and process/strategy references.
- I did not find current public robot code from 1323; public discussion indicates they may not currently find code release valuable. Their public value is binder/discussion/mechanism/process rather than code.
- Several top teams publish CAD/tech binders/videos but not code. That is still useful for Team 999 CAD/strategy automation, but it should be labeled correctly.

## Appendix A - 2026 Championship top-10 qualification rankings by division

Source table consolidated from the earlier ranking document. Use these rankings as a competitive context filter, not as proof that a team published code or AI files.

### 2026 Archimedes

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 5940 | 4.80 | 572.50 | 10-0-0 |
| 2 | 9470 | 4.70 | 643.10 | 9-1-0 |
| 3 | 1114 | 4.60 | 592.10 | 10-0-0 |
| 4 | 8608 | 4.40 | 544.10 | 9-1-0 |
| 5 | 1241 | 4.20 | 535.20 | 9-1-0 |
| 6 | 3045 | 4.10 | 544.40 | 8-2-0 |
| 7 | 27 | 4.00 | 543.10 | 8-2-0 |
| 8 | 10021 | 3.90 | 529.60 | 8-2-0 |
| 9 | 2106 | 3.90 | 496.60 | 8-1-1 |
| 10 | 5468 | 3.80 | 501.40 | 8-2-0 |

### 2026 Curie

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 5907 | 4.50 | 575.30 | 10-0-0 |
| 2 | 6800 | 4.50 | 574.60 | 9-1-0 |
| 3 | 5199 | 4.40 | 526.60 | 10-0-0 |
| 4 | 254 | 4.20 | 563.10 | 9-1-0 |
| 5 | 1987 | 4.20 | 552.50 | 9-1-0 |
| 6 | 7028 | 4.10 | 538.60 | 9-1-0 |
| 7 | 9023 | 3.90 | 538.90 | 8-2-0 |
| 8 | 3128 | 3.90 | 511.50 | 9-1-0 |
| 9 | 6369 | 3.70 | 516.10 | 7-3-0 |
| 10 | 125 | 3.50 | 543.40 | 7-3-0 |

### 2026 Daly

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 4414 | 4.90 | 693.40 | 10-0-0 |
| 2 | 1323 | 4.90 | 587.30 | 10-0-0 |
| 3 | 4028 | 4.00 | 468.60 | 9-1-0 |
| 4 | 1325 | 3.90 | 527.80 | 8-2-0 |
| 5 | 316 | 3.80 | 457.30 | 9-1-0 |
| 6 | 3538 | 3.70 | 490.50 | 8-2-0 |
| 7 | 340 | 3.60 | 452.00 | 8-2-0 |
| 8 | 10213 | 3.50 | 442.60 | 8-2-0 |
| 9 | 2073 | 3.30 | 451.80 | 7-3-0 |
| 10 | 4678 | 3.20 | 487.00 | 7-3-0 |

### 2026 Galileo

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 7769 | 4.40 | 598.50 | 9-1-0 |
| 2 | 1690 | 4.00 | 597.70 | 8-2-0 |
| 3 | 5913 | 3.90 | 488.30 | 9-1-0 |
| 4 | 1506 | 3.90 | 475.10 | 9-1-0 |
| 5 | 1768 | 3.80 | 562.00 | 7-3-0 |
| 6 | 9496 | 3.80 | 533.90 | 8-2-0 |
| 7 | 449 | 3.80 | 467.20 | 8-2-0 |
| 8 | 4469 | 3.60 | 479.50 | 8-2-0 |
| 9 | 2468 | 3.50 | 471.40 | 7-3-0 |
| 10 | 6528 | 3.50 | 459.50 | 8-2-0 |

### 2026 Hopper

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 2056 | 4.60 | 632.30 | 9-1-0 |
| 2 | 1706 | 4.60 | 602.00 | 10-0-0 |
| 3 | 6329 | 4.30 | 595.00 | 9-1-0 |
| 4 | 9785 | 4.30 | 545.10 | 9-1-0 |
| 5 | 5000 | 4.00 | 555.10 | 8-2-0 |
| 6 | 1732 | 3.90 | 555.20 | 7-3-0 |
| 7 | 8513 | 3.90 | 547.30 | 8-2-0 |
| 8 | 8044 | 3.90 | 546.60 | 7-3-0 |
| 9 | 581 | 3.80 | 616.60 | 7-3-0 |
| 10 | 1701 | 3.80 | 524.10 | 8-2-0 |

### 2026 Johnson

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 4522 | 4.70 | 565.90 | 10-0-0 |
| 2 | 1792 | 4.00 | 460.50 | 9-1-0 |
| 3 | 6324 | 3.90 | 458.10 | 9-1-0 |
| 4 | 7558 | 3.80 | 532.90 | 8-2-0 |
| 5 | 117 | 3.60 | 529.00 | 7-3-0 |
| 6 | 190 | 3.60 | 476.10 | 8-2-0 |
| 7 | 67 | 3.50 | 512.10 | 7-3-0 |
| 8 | 836 | 3.50 | 475.50 | 8-2-0 |
| 9 | 4391 | 3.50 | 465.20 | 8-2-0 |
| 10 | 2337 | 3.50 | 443.80 | 8-2-0 |

### 2026 Milstein

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 7457 | 4.40 | 510.10 | 10-0-0 |
| 2 | 1678 | 4.20 | 561.90 | 9-1-0 |
| 3 | 2481 | 4.00 | 563.50 | 8-2-0 |
| 4 | 694 | 4.00 | 432.70 | 10-0-0 |
| 5 | 4499 | 3.60 | 485.60 | 8-2-0 |
| 6 | 686 | 3.60 | 448.40 | 8-2-0 |
| 7 | 2122 | 3.50 | 510.60 | 7-3-0 |
| 8 | 1771 | 3.50 | 464.90 | 8-2-0 |
| 9 | 4089 | 3.40 | 493.60 | 7-3-0 |
| 10 | 1640 | 3.40 | 475.00 | 7-3-0 |

### 2026 Newton

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 2910 | 4.60 | 706.70 | 9-1-0 |
| 2 | 9128 | 4.30 | 536.10 | 9-1-0 |
| 3 | 5549 | 4.30 | 515.10 | 9-1-0 |
| 4 | 604 | 4.10 | 564.60 | 8-2-0 |
| 5 | 973 | 4.10 | 559.60 | 8-2-0 |
| 6 | 1833 | 3.90 | 480.60 | 8-2-0 |
| 7 | 6036 | 3.80 | 522.00 | 8-2-0 |
| 8 | 2052 | 3.80 | 500.40 | 8-2-0 |
| 9 | 1796 | 3.70 | 534.20 | 7-3-0 |
| 10 | 695 | 3.70 | 522.90 | 7-3-0 |

## Appendix B - 2025 Championship top-10 qualification rankings by division

### 2025 Archimedes

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 8044 | 5.50 | 208.60 | 9-1-0 |
| 2 | 1987 | 5.40 | 217.00 | 9-1-0 |
| 3 | 1197 | 5.30 | 219.60 | 8-2-0 |
| 4 | 1756 | 5.30 | 209.40 | 8-2-0 |
| 5 | 4391 | 5.10 | 219.50 | 8-2-0 |
| 6 | 1114 | 5.00 | 202.50 | 8-2-0 |
| 7 | 341 | 5.00 | 219.10 | 7-3-0 |
| 8 | 2481 | 5.00 | 218.10 | 8-2-0 |
| 9 | 604 | 4.90 | 216.80 | 7-3-0 |
| 10 | 2106 | 4.90 | 199.00 | 8-2-0 |

### 2025 Curie

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 7457 | 5.80 | 224.80 | 10-0-0 |
| 2 | 3663 | 5.50 | 234.30 | 9-1-0 |
| 3 | 7028 | 5.40 | 215.40 | 9-1-0 |
| 4 | 4646 | 5.30 | 218.50 | 8-2-0 |
| 5 | 1771 | 5.30 | 228.60 | 9-1-0 |
| 6 | 4611 | 5.30 | 207.80 | 9-1-0 |
| 7 | 340 | 5.20 | 202.50 | 8-2-0 |
| 8 | 6329 | 5.20 | 217.00 | 9-1-0 |
| 9 | 8884 | 5.10 | 219.50 | 8-2-0 |
| 10 | 359 | 5.00 | 221.80 | 7-2-1 |

### 2025 Daly

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 4415 | 5.80 | 214.10 | 10-0-0 |
| 2 | 1796 | 5.60 | 223.30 | 9-1-0 |
| 3 | 1678 | 5.40 | 221.20 | 9-1-0 |
| 4 | 6424 | 5.30 | 212.50 | 9-1-0 |
| 5 | 179 | 5.20 | 200.30 | 8-2-0 |
| 6 | 2056 | 5.10 | 239.20 | 7-3-0 |
| 7 | 4655 | 4.90 | 205.20 | 7-3-0 |
| 8 | 2630 | 4.90 | 213.40 | 8-2-0 |
| 9 | 694 | 4.80 | 217.20 | 6-4-0 |
| 10 | 3792 | 4.80 | 193.60 | 7-3-0 |

### 2025 Galileo

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 4946 | 5.80 | 213.70 | 10-0-0 |
| 2 | 9483 | 5.30 | 209.40 | 9-1-0 |
| 3 | 4522 | 5.20 | 209.50 | 9-1-0 |
| 4 | 190 | 5.00 | 211.00 | 8-2-0 |
| 5 | 59 | 4.80 | 195.40 | 8-2-0 |
| 6 | 3035 | 4.80 | 199.50 | 7-3-0 |
| 7 | 8214 | 4.70 | 200.40 | 9-1-0 |
| 8 | 180 | 4.60 | 198.30 | 8-2-0 |
| 9 | 5847 | 4.50 | 199.60 | 6-4-0 |
| 10 | 7558 | 4.50 | 196.10 | 6-4-0 |

### 2025 Hopper

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 1768 | 5.00 | 214.90 | 8-2-0 |
| 2 | 2200 | 5.00 | 208.20 | 8-2-0 |
| 3 | 4728 | 5.00 | 205.20 | 8-2-0 |
| 4 | 3339 | 4.80 | 196.40 | 9-1-0 |
| 5 | 6621 | 4.80 | 192.00 | 8-2-0 |
| 6 | 9245 | 4.80 | 190.70 | 8-1-1 |
| 7 | 4907 | 4.70 | 216.00 | 7-3-0 |
| 8 | 9496 | 4.60 | 210.90 | 7-3-0 |
| 9 | 7632 | 4.60 | 190.10 | 8-2-0 |
| 10 | 2075 | 4.50 | 209.10 | 6-4-0 |

### 2025 Johnson

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 1690 | 5.60 | 228.70 | 9-1-0 |
| 2 | 1619 | 5.60 | 216.70 | 9-1-0 |
| 3 | 6800 | 5.50 | 209.90 | 9-1-0 |
| 4 | 111 | 5.40 | 224.60 | 9-1-0 |
| 5 | 2607 | 5.20 | 201.70 | 8-2-0 |
| 6 | 4414 | 4.80 | 215.40 | 7-3-0 |
| 7 | 1325 | 4.80 | 206.00 | 8-2-0 |
| 8 | 2714 | 4.80 | 203.80 | 7-2-1 |
| 9 | 2930 | 4.80 | 192.10 | 7-3-0 |
| 10 | 45 | 4.70 | 194.30 | 7-3-0 |

### 2025 Milstein

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 5940 | 5.50 | 221.70 | 9-1-0 |
| 2 | 118 | 5.50 | 229.00 | 9-1-0 |
| 3 | 7407 | 5.40 | 210.90 | 9-1-0 |
| 4 | 254 | 5.20 | 224.30 | 8-2-0 |
| 5 | 910 | 5.10 | 202.60 | 9-1-0 |
| 6 | 3006 | 5.10 | 179.30 | 9-1-0 |
| 7 | 503 | 4.90 | 193.60 | 7-3-0 |
| 8 | 3674 | 4.90 | 195.00 | 7-2-1 |
| 9 | 5895 | 4.80 | 202.40 | 8-2-0 |
| 10 | 2137 | 4.80 | 197.50 | 8-2-0 |

### 2025 Newton

| Rank | Team | Ranking Score | Avg Match | Record |
| --- | --- | --- | --- | --- |
| 1 | 1323 | 5.90 | 239.50 | 10-0-0 |
| 2 | 2832 | 5.50 | 202.10 | 10-0-0 |
| 3 | 2910 | 5.40 | 243.00 | 8-2-0 |
| 4 | 422 | 5.30 | 222.10 | 8-2-0 |
| 5 | 148 | 5.00 | 208.50 | 8-2-0 |
| 6 | 9072 | 5.00 | 208.90 | 8-2-0 |
| 7 | 3538 | 5.00 | 204.50 | 8-2-0 |
| 8 | 7426 | 4.90 | 206.40 | 8-2-0 |
| 9 | 3937 | 4.90 | 198.40 | 8-2-0 |
| 10 | 449 | 4.90 | 199.10 | 8-2-0 |

## Appendix C - Team 999 starter repository skeleton

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

## Appendix D - Non-negotiable AI safety rules

- AI must never deploy code, enable the robot, or change current limits/inversions/gearing without explicit mentor review.
- Do not edit generated files such as CTRE TunerConstants, BuildConstants, generated PathPlanner assets, or vendordeps unless explicitly instructed.
- Every non-trivial change must run build/test/format and a relevant sim smoke test before being considered complete.
- All subsystem hardware should sit behind IO interfaces with real, sim, and replay/no-op implementations where practical.
- All physical constants should use WPILib units or clearly named constants; no unexplained magic numbers.
- Cache CAN/status-signal reads once per loop; avoid repeated device queries inside command execute methods.
- Vision code must validate timestamps, robot orientation/IMUMode requirements, NaN/impossible poses, tag count/distance, and field-boundary constraints.
- PathPlanner/trajectory commands must declare requirements, handle alliance flipping intentionally, and terminate or fail safe.
- Simulation fidelity limitations must be documented directly next to sim code, not hidden in chat history.
- Student learning remains explicit: AI output must be explainable in code review by the student who merges it.
