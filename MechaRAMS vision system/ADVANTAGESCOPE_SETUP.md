# AdvantageScope Setup — Robot Model + Field Visualization

Author: Claude (Opus 4.8) session, 2026-06-30
Purpose: See the robot move on a field (with a 3D robot model) during **both** simulation and real
driving. The robot code now logs everything AdvantageScope needs (this was review BUG/ISSUE 4 — Codex
never logged the fused pose).

AdvantageScope docs: https://docs.advantagescope.org — see "3D Field", "Configuration / Custom Assets",
and "Connecting" (NT4 / log files).

---

## What the robot publishes (works in sim and on the real robot)

`Robot.java` runs AdvantageKit with an `NT4Publisher` (live) and a `WPILOGWriter` (file), so the same
channels are available live and in replay:

| Channel | Type | Use in AdvantageScope |
| --- | --- | --- |
| `Drive/Pose` | `Pose2d` | The robot model's pose (fused estimate). |
| `Drive/ModuleStates`, `Drive/ModuleTargets` | `SwerveModuleState[]` | Swerve module arrows. |
| `Vision/Summary/TagPoses` | `Pose3d[]` | Where the AprilTags are (currently seen). |
| `Vision/Summary/AcceptedPoses` / `AutoSuppressedPoses` / `RejectedPoses` | `Pose3d[]` | Vision poses: actually **fused** / validated-but-withheld in early auto / rejected by a gate. |
| `DriveToPose/TargetPose`, `DriveToPose/MeasuredPose` | `Pose2d` | Precision target vs measured. |
| `Aim/GoalPose`, `Aim/LeadPose` | `Pose2d` | Aiming target and shoot-on-move lead point. |

---

## 1. Connect

- **Simulation:** start `simulateJava`, then in AdvantageScope: `File → Connect to Simulator` (NT4,
  `localhost`).
- **Real robot:** `File → Connect to Robot` (uses team number `999`). Same channels appear.
- **Replay a log:** `File → Open Log…` → newest `.wpilog` (sim: `logs/sim`; robot: pulled from
  `/home/lvuser/logs`).

---

## 2. Add a robot model (3D Field)

AdvantageScope ships several built-in robot models, and you can add a custom one.

### Quick start (built-in model)

1. Add a **3D Field** tab.
2. Drag `Drive/Pose` onto the tab; set its type to **Robot**.
3. In the pose's options, pick a built-in robot model (e.g. a generic/KitBot swerve chassis) so a 3D
   body follows the pose.

### Custom MechaRAMS model (recommended for the real look)

1. Export the 2025 chassis CAD to a `.glb` (or download a generic swerve `.glb`).
2. Put it in AdvantageScope's **user assets** folder (Help → "Show Assets Folder"):
   `userAssets/Robot_MechaRAMS/` containing `model.glb` and a `config.json` (see the docs "Custom
   Assets / Robots" for the exact `config.json` fields: name, rotations, position offset, and optional
   swerve module locations).
3. Restart AdvantageScope; select "MechaRAMS" as the model for `Drive/Pose`.

> The model is purely cosmetic — the **pose** comes from `Drive/Pose`, so the model behaves identically
> in sim and on the real robot.

---

## 3. Add the field and tags

Our test uses a **custom 8 m × 4 m two-tag field**, not an official FRC field.

- Easiest: keep the AdvantageScope field set to an evergreen/blank field and add
  `Vision/Summary/TagPoses` as **AprilTag** (or as 3D poses) so the two tags render at their true
  positions, and `Drive/Pose` as the robot. That alone shows the robot driving relative to the tags.
- Add `Vision/Summary/AcceptedPoses` as a second, semi-transparent **Ghost** robot to see what vision is
  proposing vs. where the fused pose is — the cleanest way to *see* the fusion working.
- Add `DriveToPose/TargetPose` and `Aim/GoalPose` as **target** poses to watch precision/aiming.

(If you later test on a real FRC field, select that season's field config and load the official AprilTag
layout instead.)

---

## 4. Suggested line-graph tab (tuning)

Add a **Line Graph** tab with:
`Vision/Camera0/AcceptedFrames`, `Vision/Camera0/RejectedFrames`, `Vision/Camera0/LastInnovationMeters`,
`DriveToPose/TranslationErrorMeters`, `DriveToPose/RotationErrorDegrees`, `Aim/HeadingErrorDegrees`,
`Timing/VisionMs` — this is the at-a-glance health + tuning view for every test in
`CALIBRATION_AND_TEST_PROCESS.md`.

---

## 5. Same workflow, real robot

Because the channels are identical, the exact tab layout you build in simulation works unchanged when
connected to the real robot — build and save the layout once in sim, reuse it at events.
