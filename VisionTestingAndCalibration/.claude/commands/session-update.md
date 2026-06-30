# Session Update

Use this command before ending any substantial work session.

1. Read `S:\MechaRAMS\2027Prototyping\MechaRAMS vision system\SESSION_STATE.md`.
2. Update the date, current objective, files changed, decisions made, tests run, and blockers.
3. Add any mentor prompts that changed direction to `AI_PROMPTS.md`.
4. If controls changed, update `ROBOT_CONTROLS.md`.
5. If camera/tag/path/aiming procedures changed, update `VISION_AND_TRAJECTORY_TEST_PLAN.md` and
   `CALIBRATION_AND_TEST_PROCESS.md`.
6. If architecture changed, update `ARCHITECTURE_AND_DEPLOYMENT.md`; if a module was added, add/refresh
   its skill (`.codex/skills/`) and prompt (`AI_REGENERATION_PROMPTS.md`).
7. Run `./gradlew.bat compileJava` and `./gradlew.bat test`; record the result.

Do not mark work complete if compile/tests or robot testing is blocked; record the blocker exactly.
