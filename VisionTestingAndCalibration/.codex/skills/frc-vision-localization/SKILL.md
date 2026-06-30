# FRC Vision Localization Skill

Use this skill when modifying Team 999 AprilTag localization, PhotonVision, camera transforms, pose fusion, or AdvantageKit replay behavior.

## Required Reads

Before editing, read:

- `S:\MechaRAMS\2027Prototyping\VisionTestingAndCalibration\AGENTS.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\SESSION_STATE.md`
- `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\VISION_AND_TRAJECTORY_TEST_PLAN.md`

## Design Rules

- Consume all unread camera frames.
- Sort vision observations by timestamp.
- Reject physically impossible poses.
- Single-tag observations may correct translation but should not correct heading.
- Multi-tag observations may correct heading with distance/tag-count weighted covariance.
- Log accepted frames, rejected frames, rejection reasons, tag count, average distance, and accepted pose.
- Keep final fusion on the roboRIO unless timing logs justify moving it.

## Verification

- Run `.\gradlew.bat compileJava` with Java 17 or newer.
- If Java 17 is missing, record that blocker in `SESSION_STATE.md`.
- Update camera/tag docs when transforms or layouts change.
