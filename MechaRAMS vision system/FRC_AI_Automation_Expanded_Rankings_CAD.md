# FRC AI Automation Source Ranking & CAD Automation Expansion

Prepared for Team 999 / MechaRAMS programming and design planning.  
Verification date: June 27, 2026.  
This is a companion/update to the first FRC AI automation source investigation. It adds team ranking context, expands the Championship division scope, and adds a practical CAD/AI-aided design plan.

## Executive summary

- The most reusable **direct AI-agent materials** are not primarily from the absolute top 2026 world-champion teams. The best public AI environment examples found are Team 360, Team 6238, Team 6391, Team 1868, Team 4050, Team 7660, Team 8092, plus community tools such as `wpilib-agent-tools` and `wpilog-mcp`.
- The strongest **elite-code/performance references** remain Team 4414, Team 1323, Team 2910, Team 254, Team 6328, Team 6329, Team 1768, Team 125, and Team 4206. Most of these elite teams did **not** expose public `AGENTS.md`, `CLAUDE.md`, Codex skills, or Claude command packs in the materials checked.
- The expanded Championship scan now includes top-10 qualification rankings for all 2025 and 2026 Championship divisions, not only Einstein/world-finalist teams.
- CAD conclusion: staying with **Onshape** is still the best operational choice for an FRC team because of collaboration, FRC libraries, FeatureScript, REST API support, and public FRC training resources. However, today’s practical AI CAD workflow should be a **parameter/check/review assistant** rather than an autonomous CAD designer.
- For a turret/dye-rotor decision, AI can absolutely help build parametric trade studies, perimeter checks, candidate turret-center placement, game-piece compression calculations, ring sizing ranges, and mechanism risk scoring. It should not be trusted to generate final geometry without deterministic checks, prototypes, and mentor review.

## How to read the ranking tiers

- **A / A- direct AI**: public FRC-specific AI guidance, agent contracts, or Claude/Codex-ready environment files worth copying/adapting.
- **A for code/performance/infrastructure**: elite robot code, logging, replay, simulation, strategy, or CAD resources, but not necessarily direct AI-agent files.
- **B / C**: useful templates or examples, but either the team was not competitively elite that season or the AI material is narrower.

## Ranked source teams used or added

| Team | Name | Category | 2026/Recent strength | Championship/division context | Material used | Why it matters | Tier |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 360 | The Revolution | Direct AI robot-code source | 2026 official 40-20; Pacific Northwest district rank #6. | Attended Houston; not in the 2026 division top-10 qualification list checked. | RainMaker26 robot repo; AGENTS.md, CLAUDE.md, .claude commands, dev workflow, sim/test/static-analysis commands. | One of the best directly reusable public FRC AI-agent setups found. | A |
| 6238 | Popcorn Penguins | Direct AI robot-code source | 2026 official 38-21; California district rank #16; California Northern District Championship winner. | Attended Houston; not in the 2026 division top-10 qualification lists checked. | 2026-Robot CLAUDE.md and .claude-style guidance; AdvantageKit IO/sim/replay pattern. | Stronger competition team than most direct AI-template repos; good bridge between agent guidance and serious robot code. | A- |
| 6391 | Bearbotics | Direct AI robot-code source | 2026 official 26-14; Regional Pool rank #185; St. Louis Regional rank #2, Regional Finalist, Autonomous Award. | Attended Houston; not in the 2026 top-10 qualification lists checked. | 2026 rebuilt robot repo with CLAUDE.md; built on AdvantageKit-style architecture. | Useful direct AI/code architecture source; especially good for autonomous/robot-architecture review. | A- |
| 1868 | Space Cookies | Direct Claude configuration source | 2026 official 30-29; California district rank #36; Engineering Inspiration at Silicon Valley. | Attended Archimedes; not in top-10 qualification list. | Shared Claude Code config: rules, agents, commands, contexts, skills for FRC robot development. | Most explicit “AI environment” repo found: reviewer agents, safety auditor, subsystem architect, vision reviewer, commands, install scripts. | A |
| 4050 | Biohazard | Direct AI template / rebuilt robot source | 2026 official 4-9; Regional Pool rank #683. | Did not appear in 2026 Championship top-10 division lists. | 2026-rebuilt and frc-java-template style source; AI/developer-environment materials surfaced in prior pass. | Good reusable template value even though competitive strength was not elite in 2026. | B+ |
| 7660 | Byting Irish | Direct AI robot-code source | 2026 official 15-15; FIM rank #240. | Did not appear in 2026 Championship top-10 division lists. | 2026 robot repo with AGENTS.md and simulation/replay notes. | Useful for agent instructions around robot code; competition result moderate. | B+ |
| 8092 | GOAT | Direct AI-use robot source | 2026 official 3-6; Regional Pool rank #936; Avrasya Regional Judges Award. | Did not appear in 2026 Championship top-10 division lists. | Robot-2026 / 8092-2026 repos with CLAUDE.md and Claude Code usage notes. | Direct evidence of Claude Code use; not a competitive benchmark. | B |
| 2363 | Triple Helix | FRC AI/MCP tooling source | 2026 official 15-15; FIRST Chesapeake rank #59. | Did not appear in 2026 Championship top-10 division lists. | wpilog-mcp: MCP server for WPILib logs and AdvantageKit/WPILOG workflows. | Very useful for AI debugging/replay evidence loop even if it is tooling, not a full robot AI environment. | A for tooling |
| 488 | XBot | Reusable robot-code / documentation source | Most recent verified page used: 2025 official 26-20; PNW rank #28; Sammamish finalist. 2026 page appeared incomplete/stale at check time. | No 2026 top-10 Championship division result verified. | SeriouslyCommonLib and docs/style patterns; useful for Java/WPILib command architecture and reusable mechanisms. | Good library/design reference, not an elite 2026 competitive benchmark and not a direct AI contract source. | B+ |
| 834 | SparTechs | AI/strategy-board source found in expanded pass | 2026 official 11-18-1; Mid-Atlantic rank #94. | No 2026 top-10 Championship division result verified. | StrategyBoard2026-style AI guidance surfaced in expanded search. | Potentially useful for non-robot-code AI workflow ideas; lower priority than 360/6238/1868. | C+ |
| 2910 | Jack in the Bot | Elite robot-code / simulation benchmark | 2026 official 53-23; PNW rank #3; PNW DCMP winner; Newton Division winner. | 2026 Newton qualification rank #1; 2025 Newton qualification rank #3. | 2026/2025 public robot-code repos and 2024 turret/control references. | Study for elite architecture, controls, autonomous strategy, and sim/replay practices; no public AI contract found in checked repos. | A for code/performance |
| 254 | The Cheesy Poofs | Elite robot-code benchmark | 2026 official 65-6; California district rank #2; Silicon Valley, Central Valley, CA Northern, and Curie Division winners. | 2026 Curie qualification rank #4; 2025 Milstein qualification rank #4. | FRC-2025-Public and elite robot-code references. | Excellent code/performance benchmark; no direct AI contract found in checked public source. | A for code/performance |
| 1323 | MadTown Robotics | Elite performance benchmark | 2026 official 69-4; California district rank #4; San Francisco, Central Valley, CA Northern, Daly Division, and Houston Championship winners. | 2026 Daly qualification rank #2; 2025 Newton qualification rank #1. | World-champion performance benchmark and public-org/source check. | Elite strategy/performance benchmark; no direct AI contract found in checked public source. | A for performance |
| 4414 | HighTide | Elite performance benchmark | 2026 official 70-2; California district rank #1; Ventura, Orange County, CA Southern, Daly Division, and Houston Championship winners. | 2026 Daly qualification rank #1; 2025 Johnson qualification rank #6. | World-champion performance benchmark; public source/org and Chief Delphi references checked. | Elite strategy/performance benchmark; no direct public AI contract found in checked materials. | A for performance |
| 6328 | Mechanical Advantage | Elite code/sim/tooling benchmark + CAD resource | 2026 official 51-13; overall 75-20; New England district rank #5; Minuteman and Waterbury winners; Impact Award at Waterbury and New England DCMP. | 2026 Hopper rank #20, not top-10; 2025 top-10 not found in checked division lists. | RobotCode2026Public, AdvantageKit, Onshape4FRC resources. | Top source for logging/replay architecture and FRC Onshape teaching resources; not direct AI contract source. | A for infrastructure |
| 6329 | Bucks' Wrath | New England elite benchmark | 2026 official 61-15-1; New England district rank #4; Pine Tree, Newsom, and Hopper Division winners. | 2026 Hopper qualification rank #3; 2025 Curie qualification rank #8. | New England top-team benchmarking and division-finalist/top-10 coverage. | Important local benchmark for Team 999 even if no direct AI contract was found. | A for NE performance |
| 1768 | Nashoba Robotics | New England elite benchmark | 2026 official 70-17; overall 96-27; New England district rank #1; URI, WPI, Burns, and New England DCMP winners. | 2026 Galileo qualification rank #5; 2025 Hopper qualification rank #1. | New England top-team benchmark and division top-10 coverage. | Very important local strategy/performance benchmark; no direct public AI contract found. | A for NE performance |
| 125 | NUTRONS | New England elite / design benchmark | 2026 official 68-17-1; New England district rank #7; North Shore winner. | 2026 Curie qualification rank #10. | NE benchmark and historical design/prototyping references around dye-rotor lineage. | Useful local benchmark and mechanism-design inspiration; not direct AI contract source. | A- for NE performance/design |
| 4206 | Robo Vikes | Elite division-winner robot-code/reference source | 2026 official 47-28; Texas district rank #11; Newton Division winner. | Newton Division winner, but not in 2026 Newton top-10 qualification ranks. | Public GitHub/org reference from expanded top-team pass. | Useful top-playoff benchmark; not a direct AI contract source. | A- for performance |
| 111/112 | WildStang Robotics Program | Mechanism/CAD concept source | Team 111 2026 official 30-15; Regional Pool rank #115; Midwest Regional finalist and Autonomous Award; Illinois State Championship winner. | Team 111 attended Galileo; not top-10 qualification list. | 2026 Open Alliance dye-rotor deep dive and mechanism math/risks. | Best found source for dye-rotor tradeoff and geometry discussion; useful for AI-aided CAD/mechanism prompts. | A for mechanism insight |

## Non-team / community resources used

| Resource | Type | What it provides | Ranking note | Links |
| --- | --- | --- | --- | --- |
| Shenzhen Robotics Alliance / MapleSim | Simulation library | maple-sim FRC Java simulation with physics engines and CLAUDE.md guidance. | Not an FRC team ranking item. | https://github.com/Shenzhen-Robotics-Alliance/maple-sim |
| wpilib-agent-tools | AI/robotics harness | Sandbox-first Codex/Claude/Cursor harness for WPILib simulation, NT4 recording, WPILOG analysis, and patch review. | Individual/community tool, not a team. | https://github.com/edanliahovetsky/wpilib-agent-tools |
| FRCDesignApp / FRCDesignLib | Onshape/FRC CAD library tooling | Plugin/app and component library for inserting COTS FRC parts in Onshape. | Community CAD infrastructure, not a team. | https://frcdesign.org/resources/frcdesignlib/<br>https://www.chiefdelphi.com/t/introducing-the-new-frcdesignapp/507335 |
| Onshape4FRC | FRC Onshape learning/resource site | FRC CAD teaching resources, calculators, COTS library notes, FeatureScript walkthroughs. | Built by FRC 6328 mentors, but the site itself is a resource, not a competition result. | https://onshape4frc.com/ |
| Onshape AI Advisor | Native Onshape AI help | AI documentation/best-practice assistant inside the Onshape workflow. | CAD-product feature, not a team. | https://www.onshape.com/en/features/ai-advisor |

## What changed from the first investigation

1. Added source-team competitive ranking context so you can see whether a repo came from an elite team, a strong regional team, or a lower-ranked team with a useful template.
2. Added 2025 and 2026 Championship division top-10 qualification tables.
3. Added more direct AI-environment leads: Team 6238 `CLAUDE.md`, Team 1868 shared Claude Code config, `wpilib-agent-tools`, and additional Claude/AGENTS-style repos.
4. Clarified that the best public AI-agent examples and the best elite robot-code examples are not the same list.
5. Added a CAD/AI mechanism-design section focused on turret/dye-rotor design automation.

## 2026 Championship top-10 qualification rankings by division

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

## 2025 Championship top-10 qualification rankings by division

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

## Interpretation: where the most useful sources sit competitively

- **Direct AI winners for reuse:** Team 360, Team 6238, Team 1868, Team 6391. These are the first places I would mine for `AGENTS.md`, `CLAUDE.md`, commands, safety-review prompts, subsystem architecture rules, Gradle/test/replay instructions, and repo conventions.
- **Elite robot-code/performance winners for architecture:** Team 2910, Team 254, Team 6328, Team 4414, Team 1323, Team 1768, Team 6329, Team 125, Team 4206. These should inform what the AI is expected to produce, even if they do not provide public AI contracts.
- **AI tooling winners:** `wpilib-agent-tools`, `wpilog-mcp`, AdvantageKit/log replay, MapleSim, and FRC-specific MCP/RAG tools. These are the strongest direction for a safe automation loop because the AI can gather evidence from simulation/logs before touching real robot code.
- **New England benchmark:** 1768, 6329, 6328, 125 are the most relevant local high-performance references from the expanded list. Team 999 should use them for design/strategy expectations even if public AI materials are limited.

## CAD / AI-aided design: what is realistic now

### Direct answer

Yes, we can build an AI-aided CAD workflow that accepts a prompt such as: “We want to use a turret, the game element is a ball of diameter `d`, suggest turret ring diameter, turret center location, safe rotation limits, motor placement constraints, and whether a dye rotor is better than a conventional feeder/turret.”

But the right implementation is **not** simply asking an LLM to draw a complete robot. The safe implementation is a deterministic engineering workflow wrapped by AI:

1. AI converts the strategy/design request into a structured mechanism specification.
2. A calculator/checker computes geometry, constraints, perimeter violations, ball compression, speed ranges, and clearances.
3. Onshape variables/configurations and FeatureScript generate or update parametric sketches/plates/placeholders.
4. The assistant produces a review report: assumptions, dimensions, risks, required prototypes, and pass/fail checks.
5. Mentors/students review, prototype, and only then turn the concept into final CAD.

### Why Onshape should probably remain the team CAD base

- Onshape has native collaboration, branching/versions, FRC education support, public FRC resources, FeatureScript, and REST APIs.
- Onshape AI Advisor is useful for help, best practices, and documentation-backed guidance, but it does **not** currently generate full designs, predict all errors, or make complex engineering decisions by itself.
- FeatureScript can create reusable custom features and custom tables; the REST API can read/write document data, features, configurations, and exports. That is enough to support a Team 999 “design assistant” that computes parameters and updates controlled documents.
- FRCDesignApp/FRCDesignLib and Onshape4FRC are better FRC-specific resources than generic CAD AI tools because they directly support COTS parts, FRC design style, and student learning.

### Turret / dye-rotor AI workflow

Inputs the assistant should require:

- Game piece diameter `d`; allowable compression range; coefficient/friction estimate if available.
- Robot frame size, bumper rules, legal extension/perimeter constraints, swerve module geometry, and protected electronics volume.
- Desired shooter location, shooter throat width, flywheel diameter, wheel spacing, hood/pivot geometry, and cable/wire routing preference.
- Candidate turret center locations, allowable rotation range, hard-stop requirements, motor locations, mass estimate, and center-of-gravity targets.
- Manufacturing constraints: plate thickness, available bearings/rings/gears, 3D print volume, waterjet/router access, and serviceability requirements.

Core geometry check:

```text
C = turret center = (cx, cy)
p_i = any point on rotating turret assembly expressed relative to C
q_i(theta) = C + R(theta) * p_i
For every theta in the allowed rotation range, q_i(theta) must remain inside the legal boundary or within allowed extension rules.
```

For full 360-degree rotation, the simple first-pass clearance check is:

```text
r_max = max(||p_i||) over all rotating points, motors, belts, shooter plates, sensors, and wire loops
cx must be at least r_max from the left/right limits
cy must be at least r_max from the front/back limits
If that is impossible, restrict the rotation range and enforce hard stops/software limits.
```

Ring sizing first pass:

```text
ring_ID >= max(game_piece_path_width + 2*clearance, shooter_throat_envelope, wire_passage_requirement)
ring_OD = ring_ID + 2*(bearing/ring width + fastener edge distance + manufacturing margin)
```

This is intentionally not a single magic formula. A usable turret ring depends on the bearing style, plate thickness, fasteners, pulley/gear path, cable chain/slip-ring decision, and how the game piece enters/exits the turret.

### Dye rotor versus conventional feeder/turret

A dye rotor / “dyerotor” is a real FRC mechanism concept used in 2026 Open Alliance discussions. The main appeal is that a center-exit feed can make turret/shooter packaging easier. The major downside is complexity: game-piece path, drum size, feed-roller power transfer, overfeed tuning, jams, serviceability, and the risk that the entire robot must be designed around the feed system.

A useful AI design assistant should score at least these architectures:

| Architecture | When it may be good | Risk flags | AI output should include |
| --- | --- | --- | --- |
| Conventional fixed feeder + turreted shooter | Simpler than dye rotor; good if a limited-angle turret is enough. | Motors or shooter plates can swing outside perimeter; cable wrap; off-center feed jams. | Turret center candidates, allowed rotation range, ring size, hard stops, cable routing, motor envelope map. |
| Fixed shooter + rotating feeder/indexer | Useful if shooter is large/heavy and feeder can aim or phase balls. | More complex controls; may still have perimeter/collision issues. | Feed timing, collision checks, ball path compression, sensor plan. |
| Dye rotor / drum feed with center exit | Can simplify center shooter/turret packaging and keep exit centralized. | High mechanical complexity; power transfer to rotating feed rollers; tuning overfeed; may require designing the robot around it. | Drum diameter range, feed roller radius, overfeed target, gear-reduction candidates, jam-risk score, prototype plan. |
| No turret / swerve-aimed robot | Often simplest if drivetrain aim is sufficient. | Requires excellent localization/control; less tolerant if shooting while moving. | Trajectory/vision requirements, alignment time estimate, shooting-map requirements. |

WildStang’s dye-rotor writeup provides a useful example relationship for their geometry:

```text
Gear Reduction = 2 * (Drum Radius - Fuel Radius) / Radius Feed Roller
```

That formula should be treated as an inspiration for a specific geometry, not a universal design rule. Team 999 would still need a prototype and a rule/legal-envelope check for the current game.

### Proposed Team 999 CAD assistant components

1. `mechanism_spec.yaml` — structured input file for game piece, frame size, rules, mechanism constraints, available parts, and design goals.
2. `cad_rules.md` — design rules for chain/belt center distances, bearing spacing, fastener edge distances, service clearances, wire routing, bumper/perimeter restrictions, and FRC safety checks.
3. `turret_layout.py` — deterministic geometry calculator that sweeps rotation angles and outputs safe center/radius/rotation candidates.
4. `mechanism_trade_study.md` — AI-written report comparing turret, fixed shooter, dye rotor, and other architectures against scoring priorities.
5. Onshape variable/config templates — `#ball_diameter`, `#turret_center_x`, `#turret_center_y`, `#ring_ID`, `#ring_OD`, `#shooter_angle`, `#max_rotation_deg`, etc.
6. FeatureScript helper features — simple construction geometry, rotating envelope visualization, clearance rings, and hole-pattern/ring skeleton generation.
7. Human design review checklist — mentor/student signoff before the AI-generated concept is treated as buildable.

### Example CAD-agent prompt

```text
You are Team 999 CAD/strategy assistant. Use only the provided rules, dimensions, and source templates. Do not invent legal limits. Given: ball diameter d = __, robot frame = __ x __, bumper/perimeter rule excerpt = __, desired shooter center height = __, swerve module keepout polygons = __, available bearing/ring options = __. Generate three mechanism candidates: conventional turret, fixed shooter with swerve aim, and dye rotor/drum feed. For each candidate provide dimensions, assumptions, turret center candidates, ring ID/OD range, motor/envelope collision risk, ball compression range, serviceability concerns, required prototypes, and pass/fail checks. Output a YAML parameter block and a design-review report. Stop if any required rule/dimension is missing.
```

## Authentication and downloads

For this public-source pass, I did not find a need for you to authenticate to read the main GitHub/TBA/Chief Delphi/Onshape documentation pages. GitHub may require login if you want deeper code search, high-rate cloning, or access to private/forked repos. Onshape automation will require your own Onshape document permissions and API/OAuth credentials only when we start automating your team documents.

If a repo or Onshape document you want checked is private or login-gated, send the link or export/download it and I can analyze it. Do not share private keys or secrets.

## Verified source links

- The Blue Alliance 2026 Championship divisions: https://www.thebluealliance.com/event/2026cmptx
- TBA 2026 Newton rankings: https://www.thebluealliance.com/event/2026new#rankings
- TBA 2026 Daly rankings: https://www.thebluealliance.com/event/2026dal#rankings
- TBA 2026 Curie rankings: https://www.thebluealliance.com/event/2026cur#rankings
- TBA 2026 Hopper rankings: https://www.thebluealliance.com/event/2026hop#rankings
- TBA 2026 Galileo rankings: https://www.thebluealliance.com/event/2026gal#rankings
- TBA 2026 Archimedes rankings: https://www.thebluealliance.com/event/2026arc#rankings
- TBA 2026 Johnson rankings: https://www.thebluealliance.com/event/2026joh#rankings
- TBA 2026 Milstein rankings: https://www.thebluealliance.com/event/2026mil#rankings
- TBA 2025 Championship divisions: https://www.thebluealliance.com/event/2025cmptx
- FRCTeam360 RainMaker26: https://github.com/FRCTeam360/RainMaker26
- 6238 2026-Robot CLAUDE.md: https://github.com/6238/2026-Robot/blob/main/CLAUDE.md
- FRC Team 1868 Claude Code config: https://github.com/Feramirr/frc-claude-config
- wpilib-agent-tools: https://github.com/edanliahovetsky/wpilib-agent-tools
- Mechanical Advantage AdvantageKit: https://github.com/Mechanical-Advantage/AdvantageKit
- Mechanical Advantage RobotCode2026Public: https://github.com/Mechanical-Advantage/RobotCode2026Public
- Onshape AI Advisor: https://www.onshape.com/en/features/ai-advisor
- Onshape FeatureScript documentation: https://cad.onshape.com/FsDoc/
- Onshape REST API documentation: https://onshape-public.github.io/docs/api-intro/
- FRCDesignLib / FRCDesignApp: https://frcdesign.org/resources/frcdesignlib/
- Onshape4FRC: https://onshape4frc.com/
- WildStang dye rotor Chief Delphi post: https://www.chiefdelphi.com/t/wildstang-robotics-program-team-111-and-112-build-blog-2026/509853?page=4

## Recommended next step

Create a Team 999 repository skeleton with: `AGENTS.md`, `CLAUDE.md`, `.claude/commands`, Codex prompt pack, `mechanism_spec.yaml`, `sim/`, AdvantageKit replay instructions, WPILib simulation harness, and CAD rules. Then seed it with adapted ideas from Team 360, Team 6238, Team 1868, `wpilib-agent-tools`, AdvantageKit/6328, and Onshape4FRC/FRCDesignLib.