# FRC AI Automation Source Investigation

Prepared for Team 999 / MechaRAMS programming planning. Public-source verification date: June 27, 2026.

## Bottom line

- I did not find evidence that most 2025-2026 Einstein/top teams have publicly released explicit AI coding contracts such as AGENTS.md, CLAUDE.md, Codex skills, or Claude commands.
- The strongest directly reusable public FRC AI templates found are from FRCTeam360/RainMaker26, Team4050/2026-rebuilt, the Shenzhen Robotics Alliance MapleSim template, and several FRC-specific MCP/RAG tools.
- Top teams such as 2910, 254, and 6328 remain extremely useful for architecture, IO layers, simulation, logging, and autonomous design, even when they do not publish AI-specific files.
- For CAD, the practical recommendation is to stay with Onshape and automate around it. Onshape AI is not yet a generative FRC CAD designer; useful automation today is FeatureScript, Onshape API, FRCDesignLib/FRCDesignApp, Onshape4FRC, checklists, and repeatable CAD standards.
- No login or private download was required for this pass. If you want the next phase, the useful private step is to clone selected repos and create a Team 999 starter AI environment in your actual repo/VM.

## Directly useful AI agent / AI contract files
- **FRCTeam360/RainMaker26 repository** - Verified open. Most useful public FRC robot-code AI environment found. README explicitly tells AI assistants to use CLAUDE.md and AGENTS.md.
  - URL: https://github.com/FRCTeam360/RainMaker26
- **FRCTeam360 RainMaker26 AGENTS.md** - Verified open. Strong FRC Java agent contract: project overview, docs/vendor links, command-based references, setup/test/format/static-analysis commands, IO layer conventions.
  - URL: https://github.com/FRCTeam360/RainMaker26/blob/main/AGENTS.md
- **FRCTeam360 RainMaker26 CLAUDE.md** - Verified open. Claude-specific project context with build/test/sim/deploy commands and FRC-specific architecture patterns.
  - URL: https://github.com/FRCTeam360/RainMaker26/blob/main/CLAUDE.md
- **FRCTeam360 .claude/commands/fsm.md** - Verified open. A reusable Claude command/skill for extracting finite state machines and generating Mermaid diagrams. Useful model for Team 999 superstructure/FSM review.
  - URL: https://github.com/FRCTeam360/RainMaker26/blob/main/.claude/commands/fsm.md
- **Team4050/2026-rebuilt repository** - Verified open. Public 2026 FRC Java repo with AGENTS.md, CLAUDE.md, devcontainer, docs, scripts, AdvantageScope layout. Good structure example.
  - URL: https://github.com/Team4050/2026-rebuilt
- **Team4050/2026-rebuilt AGENTS.md** - Verified open. FRC Java guidance for Claude Code and other agents: build/sim/Spotless, lifecycle, subsystem conventions, simulation and key rules.
  - URL: https://github.com/Team4050/2026-rebuilt/blob/main/AGENTS.md
- **Team4050/2026-rebuilt CLAUDE.md** - Verified open. Confirmed to point at AGENTS.md; useful pattern to keep one canonical agent guide and avoid contradictory instructions.
  - URL: https://github.com/Team4050/2026-rebuilt/blob/main/CLAUDE.md
- **SRA AdvantageKit TalonSwerve Template with MapleSim repository** - Verified open. Very relevant for full-device simulation. AdvantageKit + CTRE swerve + MapleSim integration, including sim startup workflow.
  - URL: https://github.com/Shenzhen-Robotics-Alliance/AdvantageKit-TalonSwerveTemplate-MapleSim
- **SRA template AGENTS.md** - Verified open. Explicit warning that agents usually lack FRC framework knowledge, then lists required docs and IO/simulation architecture.
  - URL: https://github.com/Shenzhen-Robotics-Alliance/AdvantageKit-TalonSwerveTemplate-MapleSim/blob/main/AGENTS.md
- **MapleSim repository** - Verified open. Advanced FRC Java simulation library. Useful for Team 999 if WPILib/CTRE device sim is not enough.
  - URL: https://github.com/Shenzhen-Robotics-Alliance/maple-sim
- **MapleSim CLAUDE.md** - Verified open. Claude guidance for the MapleSim library itself: build/test/publish, API constraints, simulation timing and safety notes.
  - URL: https://github.com/Shenzhen-Robotics-Alliance/maple-sim/blob/main/CLAUDE.md
- **FRC7660/robot-2026 repository** - Verified open. Additional 2026 FRC repo with AGENTS.md and autonomous/vision strategy notes. Useful as a small, direct example.
  - URL: https://github.com/FRC7660/robot-2026
- **FRC7660/robot-2026 AGENTS.md** - Verified open. Short agent file covering robot log workflow, build/deploy commands, and WPILib JDK sanity check.
  - URL: https://github.com/FRC7660/robot-2026/blob/main/AGENTS.md
- **6391-Ursuline-Bearbotics/2026-6391-Rebuilt repository** - Verified open. Additional 2026 FRC robot-code repo built on 6328 AdvantageKit template; includes hardware, simulation and log-analysis material.
  - URL: https://github.com/6391-Ursuline-Bearbotics/2026-6391-Rebuilt
- **6391 CLAUDE.md** - Verified open. Detailed Claude guidance: AdvantageKit IO pattern, real/sim/replay instantiation, Phoenix 6 conventions, log analysis scripts and thresholds.
  - URL: https://github.com/6391-Ursuline-Bearbotics/2026-6391-Rebuilt/blob/main/CLAUDE.md
- **Team488 Programming-Docs AGENTS.md** - Verified open. Not robot code, but a useful example of agent guidance for a team programming documentation/curriculum site.
  - URL: https://github.com/Team488/Programming-Docs/blob/main/AGENTS.md

## FRC-specific MCP / RAG / log-analysis tools
- **frc-rag-mcpserver** - Verified open. FRC-specific RAG/MCP idea: force AI assistant to query version-specific WPILib docs before answering robot-code questions. Recommend self-hosting/indexing rather than depending on an unknown public endpoint.
  - URL: https://github.com/ramalamadingdong/frc-rag-mcpserver
- **agentic-csa** - Verified open. MCP server that aggregates FRC docs: WPILib, REV, CTRE Phoenix, Redux, PhotonVision. Includes a copilot-instructions.md pattern.
  - URL: https://github.com/ramalamadingdong/agentic-csa
- **agentic-csa CLAUDE.md** - Verified open. Good reference for documenting an MCP server architecture and dev commands for agents.
  - URL: https://github.com/ramalamadingdong/agentic-csa/blob/main/CLAUDE.md
- **TripleHelixProgramming/wpilog-mcp** - Verified open. MCP server for asking natural-language questions about WPILib/AdvantageKit logs. Built by FRC Team 2363. Good match for post-sim/post-match analysis.
  - URL: https://github.com/TripleHelixProgramming/wpilog-mcp
- **withinfocus/tba-mcp-server** - Verified open. MCP server for The Blue Alliance data. Useful for scouting, event data, match schedule/ranking context, and autonomous strategy research.
  - URL: https://github.com/withinfocus/tba-mcp-server

## Top Worlds / New England sources and code references
- **2025 Championship Einstein awards - The Blue Alliance** - Verified open. Source used to identify 2025 Einstein winners/finalists: winners 1323/2910/4272/5026; finalists 1690/4414/2073/5166.
  - URL: https://www.thebluealliance.com/event/2025cmptx#awards
- **2026 Championship Einstein awards - The Blue Alliance** - Verified open. Source used to identify 2026 Einstein winners/finalists: winners 4414/1323/4065/1538; finalists 2910/2046/868/4206.
  - URL: https://www.thebluealliance.com/event/2026cmptx#awards
- **2026 Championship playoff results - FIRST** - Verified open. Official playoff source used to cross-check 2026 Championship division/Einstein results.
  - URL: https://frc-events.firstinspires.org/2026/CMPTX/playoffs
- **2026 New England District rankings - The Blue Alliance** - Verified open. Source used to identify top New England teams for this pass: 1768, 5687, 3467, 6329, 6328, 195, 125, 5000, etc.
  - URL: https://www.thebluealliance.com/district/2026ne/rankings
- **FRCTeam2910/2026CompetitionRobot-Public** - Verified open. Worlds finalist/top team code. Strong CTRE swerve and project structure reference. I did not find an explicit AGENTS.md/CLAUDE.md in the visible public repo listing.
  - URL: https://github.com/FRCTeam2910/2026CompetitionRobot-Public/
- **Team254/FRC-2025-Public** - Verified open. Not a confirmed 2026 Einstein source, but an elite-team public codebase with simulation/logging/autonomous architecture worth studying.
  - URL: https://github.com/Team254/FRC-2025-Public
- **Mechanical-Advantage/RobotCode2026Public** - Verified open. Top New England team 6328 public 2026 code. Updated from stable internal code; excellent AdvantageKit/IO/sim/logging reference. No explicit AGENTS.md/CLAUDE.md found in visible listing.
  - URL: https://github.com/Mechanical-Advantage/RobotCode2026Public
- **FRC 6328 Mechanical Advantage 2026 Build Thread** - Verified open. Open Alliance thread with public development context for 6328.
  - URL: https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2026-build-thread/509595
- **AdvantageKit repository** - Verified open. Logging, telemetry and replay framework by Team 6328. Strong foundation for AI-checkable sim/replay workflows.
  - URL: https://github.com/Mechanical-Advantage/AdvantageKit
- **Team1323 GitHub org** - Verified open. I found only older public pinned repositories in the visible org page during this pass. No current 2025/2026 robot-code AI contract located.
  - URL: https://github.com/team1323
- **Team4414 GitHub org** - Verified open. Visible public GitHub was not a current 2025/2026 AI robot-code template source during this pass.
  - URL: https://github.com/Team4414
- **Team 4414 2026 Technical Binder** - Verified loads. The binder loads and contains technical content. I saw community/binder references to AI/front-end work, but not a directly reusable robot-code AI contract.
  - URL: https://1339storage.blob.core.windows.net/2026files/frc4414/binder/index.html
- **FRC 4206 GitHub org** - Verified open. 2026 finalist team/org search result. Useful org to watch, but no high-confidence public AI agent template found in this pass.
  - URL: https://github.com/frc4206

## Community discussion and Open Alliance monitoring
- **Chief Delphi: How is your team using AI/LLMs for Robot Code in 2026?** - Verified open. Best public FRC discussion found on AI/LLM robot-code use. Includes use cases and cautions about outdated APIs and review/simulation.
  - URL: https://www.chiefdelphi.com/t/how-is-your-team-using-ai-llms-for-robot-code-in-2026/513448
- **FRC Open Alliance category** - Verified open. Ongoing public build threads. Good place to monitor whether more teams publish AI workflows during kickoff/build season.
  - URL: https://www.chiefdelphi.com/c/first/open-alliance/89
- **FRC 360 The Revolution 2026 Open Alliance thread** - Verified search/open result. Context thread for the RainMaker26 repo and AI-guided code review practices.
  - URL: https://www.chiefdelphi.com/t/frc-360-the-revolution-2026-build-thread-open-alliance/510290

## Official AI assistant and FRC framework documentation
- **OpenAI Codex AGENTS.md documentation** - Verified open. Official Codex behavior for AGENTS.md: scope, precedence, project instructions and limits.
  - URL: https://developers.openai.com/codex/agents-md/
- **OpenAI Codex Skills documentation** - Verified open. Official Codex skill packaging and invocation guidance. Useful for Team 999 .agents/skills.
  - URL: https://developers.openai.com/codex/skills/
- **Anthropic Claude Code memory / CLAUDE.md** - Verified open. Official Claude guidance for CLAUDE.md: project memory, what to include, and keeping it concise.
  - URL: https://docs.claude.com/en/docs/claude-code/memory
- **Anthropic Claude Code best practices** - Verified open. Official advice to give Claude a check command such as tests/build/lint.
  - URL: https://www.anthropic.com/engineering/claude-code-best-practices
- **WPILib simulation docs** - Verified open. Official run/desktop simulation reference, including simulateJava and dashboards/AdvantageScope connection.
  - URL: https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/introduction.html
- **WPILib robot-code CI with GitHub Actions** - Verified open. Official CI setup for robot code using WPILib Docker container and Gradle build/test.
  - URL: https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/robot-code-ci.html
- **CTRE Phoenix 6 simulation docs** - Verified open. Official CTRE simulation reference: Phoenix 6 devices expose SimState and support simulation.
  - URL: https://v6.docs.ctr-electronics.com/en/stable/docs/api-reference/simulation/simulation-intro.html
- **PhotonVision simulation docs** - Verified open. Official PhotonVision Java simulation reference and its limitations.
  - URL: https://docs.photonvision.org/en/latest/docs/simulation/simulation-java.html
- **PathPlanner Build an Auto docs** - Verified open. AutoBuilder, autonomous command creation, and startup loading patterns.
  - URL: https://pathplanner.dev/pplib-build-an-auto.html
- **PathPlanner Pathfinding docs** - Verified open. Pathfinding caveats, navgrid, and chaining to preplanned paths for precision.
  - URL: https://pathplanner.dev/pplib-pathfinding.html
- **AdvantageKit record/replay and IO docs** - Verified open. Official IO layer pattern. Core to making AI-generated FRC code testable and sim/replay-friendly.
  - URL: https://docs.advantagekit.org/data-flow/recording-inputs/io-interfaces/
- **AdvantageScope live sources docs** - Verified open. Connect to live robot/simulator, NT4, AdvantageKit and Phoenix diagnostics.
  - URL: https://docs.advantagescope.org/more-features/live-sources/
- **Limelight MegaTag2 / LL4 IMU docs** - Verified open. Important for Team 999 vision guidance: SetRobotOrientation/IMUMode requirements and MegaTag2 pose use.
  - URL: https://docs.limelightvision.io/docs/docs-limelight/apis/complete-networktables-api

## CAD / Onshape automation sources
- **Onshape AI Advisor** - Verified open. Current Onshape AI help/Q&A feature. Important limitation: it helps with guidance but does not generate designs or make complex engineering decisions.
  - URL: https://www.onshape.com/en/resource-center/tech-tips/onshape-ai-advisor
- **Onshape AI roadmap / AI in CAD article** - Verified open. Onshape roadmap direction: LLM-powered FeatureScript autocomplete, AI search, quick rendering, future agents/MCP style work.
  - URL: https://www.onshape.com/en/blog/how-onshape-is-bringing-artificial-intelligence-into-cad
- **Onshape REST API overview** - Verified open. Best practical path for CAD automation now: query metadata/BOMs, export STEP/STL, manage configs/properties, integrate scripts.
  - URL: https://onshape-public.github.io/docs/api-adv/intro/
- **FRCDesignApp / FRCDesignLib Chief Delphi thread** - Verified open. FRC Onshape ecosystem for using COTS FRC parts and design resources; relevant because Team 999 already uses Onshape.
  - URL: https://www.chiefdelphi.com/t/frcdesignapp/474775
- **Onshape4FRC Chief Delphi thread** - Verified open. Created by 6328 to help FRC teams get started with Onshape; includes setup, CAD library/featurescripts, calculators and external resources.
  - URL: https://www.chiefdelphi.com/t/onshape4frc/391957

## Additional watch list / lower priority sources
- **Team846/gibbon** - Verified open. Additional 2026 robot-code repo with AGENTS.md in the listing; not analyzed deeply enough to make it a primary template.
  - URL: https://github.com/Team846/gibbon
- **frc6377/rebuilt_2026** - Verified open. README references .github/copilot-instructions.md, but the direct click returned 404 during verification. Treat as a caution, not a source.
  - URL: https://github.com/frc6377/rebuilt_2026
- **GOAT-8092/8092-2026** - Verified search/open result. Additional 2026 FRC repo with AGENTS.md/CLAUDE.md, Turkish language. Useful to monitor, lower priority for Team 999.
  - URL: https://github.com/GOAT-8092/8092-2026
- **frc-6045/2026-naan-REALEST** - Verified search result. Additional 2026 repo with CLAUDE.md. Not analyzed deeply.
  - URL: https://github.com/frc-6045/2026-naan-REALEST
- **Team488/SeriouslyCommonLib** - Verified open. Reusable FRC Java framework with HAL separation/unit testing ideas; not primarily an AI contract source.
  - URL: https://github.com/Team488/SeriouslyCommonLib

## Negative-result notes

These are not proof that no files exist. They are the result of this public web/GitHub/Chief Delphi pass; teams may keep AI workflows private or publish under a different name.
- **2025 Einstein winners/finalists checked by public search:** 1323, 2910, 4272, 5026, 1690, 4414, 2073, 5166. Confirmed strong public code for 2910; current explicit AI contract not found for this set in this public pass.
- **2026 Einstein winners/finalists checked by public search:** 4414, 1323, 4065, 1538, 2910, 2046, 868, 4206. Confirmed public references for 2910, 4206 org, 4414 binder/org; no direct current robot-code AGENTS/CLAUDE template found for these teams in this public pass.
- **2026 New England top teams checked by public search:** 1768, 5687, 3467, 6329, 6328, 195, 125, 5000 and related Open Alliance sources. 6328 is the best public NE programming/simulation reference, but not an explicit AI agent-template source. Continue monitoring NE/OA during 2027 build season.

## Recommended Team 999 starter environment

```text
repo-root/
  AGENTS.md                         # canonical Codex/agent contract, short and strict
  CLAUDE.md                         # Claude-specific memory; can point to AGENTS.md
  .github/workflows/ci.yml          # WPILib Docker CI: build, test, format, static checks
  .github/copilot-instructions.md   # optional if using Copilot or MCP-aware editors
  .agents/skills/
    frc-subsystem/SKILL.md
    frc-sim/SKILL.md
    vision-localization/SKILL.md
    pathplanner-auto/SKILL.md
    cad-onshape/SKILL.md
  .claude/commands/
    review-safety.md
    sim-smoke-test.md
    fsm-map.md
    vision-pose-review.md
    trajectory-auto-review.md
    cad-review.md
  docs/ai/
    robot-contract.md               # CAN IDs, buses, mechanisms, coordinate frames
    simulation-contract.md          # what is simulated and known limitations
    vision-contract.md              # Limelight/QuestNav/AprilTag conventions
    deployment-safety.md            # mentor-controlled deploy/enable rules
    cad-contract.md                 # Onshape naming, part studio/assembly rules
```

## Recommended non-negotiable AI safety rules
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

## Suggested next steps
- Clone the primary sources locally: RainMaker26, Team4050/2026-rebuilt, SRA MapleSim template, RobotCode2026Public, 2910 public code, frc-rag-mcpserver, agentic-csa, and wpilog-mcp.
- Create Team 999 AGENTS.md and CLAUDE.md from the verified patterns, but keep Team 999-specific truth in docs/ai so it is maintainable.
- Add CI immediately: WPILib Docker build/test, Spotless/checkstyle/SpotBugs if desired, and a fast simulation smoke test.
- Add small Codex skills and Claude commands one at a time: subsystem generator first, then sim IO, then vision localization, then auto/path planning.
- For CAD, stay with Onshape for now; automate with FeatureScript, Onshape API, FRCDesignLib/FRCDesignApp, and CAD checklists rather than expecting AI to generate a legal FRC robot design end-to-end today.

## Truthfulness notes

- I treated top team claims conservatively. Where I did not find an explicit public AGENTS.md/CLAUDE.md/Codex-skill style artifact, I state that directly instead of implying absence as fact.
- I did not authenticate to private GitHub, Onshape, Discord, Slack, or team-only resources.
- Some GitHub and Chief Delphi pages are dynamic; for those, I used the public content returned by the verified page/search result and marked limitations where appropriate.
