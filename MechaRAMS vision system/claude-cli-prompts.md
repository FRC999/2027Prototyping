# Claude Code (CLI) prompts for the localization project

I was able to access everything needed for the gap analysis (your repo, nu25 on GitLab, RobotCode2026Public on GitHub). 254/2910/1690/2056 have simply not released 2026 code yet — that's not an access problem. These prompts are for executing the plan in `localization-gap-analysis-2026.md` with Claude Code, which can build and iterate directly in the repo.

Run from `T:\Projects\MechaRAMS\2026Competition\2026Competition`.

---

## Prompt 1 — Phase 1: add vision logging

```
Read localization-gap-analysis-2026.md in S:\MechaRAMS\Vision\MechaRAMS vision system (finding F3).
Add WPILib DataLog-based structured logging to the vision/odometry path without changing behavior:
- In OdometryUpdatesSubsystem and LLAprilTagSubsystem, log per camera per loop: pose estimate (MT1 and MT2), tag count, avgTagDist, max ambiguity across rawFiducials, latency, accepted/rejected and the rejection reason, std devs used, MT1-vs-MT2 selection, FSM state, and the fused drivetrain pose.
- Log QuestNav frames (pose, timestamp, freshness).
- Use NetworkTables-backed DataLog entries so AdvantageScope can view them live and from .wpilog files.
- Build with ./gradlew build and fix any errors.
```

## Prompt 2 — Phase 3: fusion fixes (one at a time)

```
Read localization-gap-analysis-2026.md (findings F1, F2, F5, F6, F7).
Implement in this order, building after each step:
1. F1: In LimelightHelpers.llStdDev / fusePoseEstimate, set yaw std dev to Double.POSITIVE_INFINITY whenever the estimate is MegaTag2; keep finite yaw std dev only for MegaTag1 multi-tag (tagCount >= 2).
2. F2: Replace getBestPoseEstimateFromAllLL single-winner selection in CALIBRATED_NO_Q with fusing every camera's estimate that passes rejection, each with its own std devs.
3. F6: Replace the std dev formula with xy = kXY * avgTagDist^2 / tagCount^2 * cameraFactor (start kXY = 0.02), theta analogous with kTheta = 0.06 for MT1 multi-tag only. Use max ambiguity across all rawFiducials, not rawFiducials[0].
4. F7: Remove the 3.0 m kMaxCameraToTargetDistance rejection for multi-tag estimates; add rejection for poses outside the field boundary (+/- 0.5 m margin, use FieldConstants-equivalent from AprilTagFieldLayout getFieldLength/getFieldWidth).
5. F5: Make gateMeasurement time-bounded: after 25 consecutive rejections, accept the next physically-plausible measurement with inflated std devs instead of relying on re-anchor resets.
Do not remove the FSM or QuestNav logic. Keep each change in a separate commit.
```

## Prompt 3 — re-audit when top teams release 2026 code

```
Check https://github.com/flamingchickens1540/frc-software-releases and the GitHub orgs
Team254, FRCTeam2910, team1690, frc1678 for newly released 2026 robot code.
For each found repo: clone it, locate the AprilTag/vision/localization code, and summarize how they
(a) compute vision std devs, (b) reject bad measurements, (c) handle multi-camera fusion,
(d) handle rotation trust, (e) any pose-reset strategy.
Compare against the findings in localization-gap-analysis-2026.md and note anything that changes our plan.
```

## Prompt 4 — Phase 2/5: ground-truth analysis of logged data

```
I have .wpilog files from our ground-truth test sessions in <folder>. For each surveyed position
(coordinates in ground-truth.csv): extract the per-camera vision pose estimates while the robot was
stationary at that position, compute bias and standard deviation per camera vs the surveyed pose,
plot error vs avgTagDist and vs tag count, and fit the kXY coefficient for the dist^2/tagCount^2
std dev model. Output a calibration report and recommended per-camera stdDevFactor values.
```
