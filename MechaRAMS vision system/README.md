# MechaRAMS Vision System — Localization & Trajectory Analysis

Research and design notes for the 2027 vision/localization effort, produced from a
review of top FRC teams' public code (6328, 3467, 1768, 2910, 1678, 5687).

## Contents

| File | What it is |
|------|------------|
| `Localization-and-Trajectory-Precision-2027.docx` | **Main analysis.** Local vs. global localization, aiming (turret/boresight vs. global-pose), trajectory precision (time-based vs. position/settle end-controllers), Choreo vs. PathPlanner, and a concrete 2027 plan with code skeletons. |
| `Vision-Strategy-2027.docx` | High-level vision strategy + NE/CT team survey; PhotonVision decision. |
| `PhotonVision-Pilot-Guide.docx` | Step-by-step camera placement and pilot procedure. |
| `localization-gap-analysis-2026.md` | Root-cause analysis of the 2026 localization issues. |
| `research-log.md` | Running log of every repo checked and what was found (so re-audits don't redo work). |
| `claude-cli-prompts.md` | Prompts used to drive the CLI re-audits. |
| `reference-estimators/` | Annotated copies of other teams' estimator source for study (see walkthrough). |

## reference-estimators/ — third-party code, for reference only

These are **unmodified excerpts from other teams' public repositories**, kept here for
study alongside `README-walkthrough.md`. They are **not** part of the MechaRAMS build.
Original copyright and licenses belong to their authors:

- `6328/RobotState.java` — Littleton Robotics (FRC 6328), MIT-style license.
- `3467/*` — Windham Windup (FRC 3467) `posestimator` library, **GPLv3**.
- `5687/*` — The Outliers (FRC 5687).

If any of this is ever adapted into MechaRAMS code, follow each file's license
(in particular, GPLv3 for 3467) or reimplement the idea independently.
