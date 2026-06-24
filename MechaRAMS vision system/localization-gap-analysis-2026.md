# MechaRAMS Localization Gap Analysis — Summer 2026

Comparison of our 2026 competition vision/localization stack against FRC 6328 Mechanical Advantage (RobotCode2026Public, "Darwin") and FRC 125 NUTRONs (nu25). Goal: identify precision weaknesses and define a summer improvement plan.

---

## 1. Our current architecture (as implemented in 2026Competition)

- **Pose estimator:** CTRE `SwerveDrivetrain` built-in Kalman estimator; vision via `addVisionMeasurement`.
- **Cameras:** 2 Limelights (`limelight-middle`, `limelight-left`) of the 4 LL4s owned.
- **State machine** (`OdometryUpdatesSubsystem`): INITIALIZE → SEEKING_TAGS_(Q/NO_Q) → CALIBRATED_(Q/NO_Q). LL anchors the field pose; QuestNav becomes primary when healthy; LL is fallback.
- **Per-camera selection:** MT1 vs MT2 chosen per camera (MT1 failsafe when yaw diff > 90°); then **one** "best" camera estimate is selected per loop and fused.
- **Quality gates:** single-tag ambiguity ≤ 0.20, camera-to-tag distance ≤ 3.0 m (4.0 m seed), innovation gate vs odometry (0.6–0.8 m + 0.5·speed, 15–20°).
- **Std devs:** LL — linear in distance, small tag-count and ambiguity terms, clamped 0.05–0.60 m; yaw 1–10°. Quest — 5–15 cm by speed.
- **Re-anchoring:** hard `resetPose` + IMU reset on yaw reset, sustained tag loss, delayed-MT1-recal (5 s), and Quest↔LL transitions.
- **Consumer:** turret auto-aim computes hub distance/angle from the fused pose → localization error translates directly into shot error.

This is a thoughtful design — the MT1/MT2 selection, IMU mode management, and Quest fallback FSM are more sophisticated than most teams'. The precision losses are mostly in *how measurements are weighted and discarded*, and in the *absence of measurement infrastructure*.

## 2. What the top teams do differently

**6328 (2026, Darwin):** 4 cameras on a custom Northstar (coprocessor SolvePnP) pipeline. **Every valid observation from every camera and every frame** is fused each loop, sorted by timestamp, into a custom RobotState estimator with a pose-history buffer. Std devs scale with `0.01 · dist² / tagCount²` (XY) and `0.03 · dist² / tagCount²` (theta), times a per-camera trust factor. Rotation is only trusted for multi-tag solves; single-tag ambiguity is resolved by comparing both PnP solutions against current gyro heading. Rejection is physical: outside field border (±0.5 m) or Z outside [−0.5, 1.0] m. No innovation gating, no mid-match pose resets. Camera extrinsics include live-tunable pitch "fudge" values and even a time-varying pose function for a camera mounted on a moving mechanism. Everything runs on AdvantageKit with full log replay, and Northstar records video when FMS attaches.

**125 (nu25, 2025 robot):** the opposite lesson — a deliberately *simple* Limelight MT2-only pipeline: fuse MT2 with `1.5 · dist²` XY std dev and **999 (i.e., ignore) theta**, reject when |ω| > 100°/s, distance > 3 m, or jump > 20 ft; MT1 only used for seeding while disabled and close to a tag. Fully logged with AdvantageKit. They won with this. Precision came from good odometry + correct weighting + logging-driven tuning, not complexity.

## 3. Findings — ranked by expected precision impact

### F1. MT2 yaw is fused back into the estimator (circular feedback) — likely our biggest accuracy bug
MT2 derives its pose *from the yaw we feed it* via `SetRobotOrientation`. Its rotation output carries no independent information, yet `llStdDev()` assigns it a finite yaw std dev (1–10°), so the estimator "corrects" heading with its own heading. This couples gyro drift into translation (MT2 translation error grows with yaw error ≈ distance · sin(yaw error)) and can lock in a slightly wrong heading. Both 6328 and 125 set theta std dev to **infinity** for MT2/yaw-seeded observations and only trust rotation from genuine multi-tag MT1 solves.

**Fix:** `thetaStdDev = Double.POSITIVE_INFINITY` for MT2 estimates; finite theta only for MT1 multi-tag.

### F2. Only one camera, one frame, fused per loop
`getBestPoseEstimateFromAllLL()` picks a single winner; the other camera's valid estimate is discarded, and only the latest frame per camera is sampled (LL4 produces frames faster than the 20 ms loop). 6328 fuses *all* observations from *all* cameras and *all* frames, timestamp-sorted. With correct std devs, more measurements always help — averaging across cameras cancels per-camera bias.

**Fix:** fuse every camera's estimate that passes rejection each loop (the Kalman filter does the weighting — "best pose" selection becomes unnecessary). Optionally use NT queues (`getBotPoseEstimate` history) to catch in-between frames.

### F3. No logging/replay — we cannot measure what we're trying to improve
We have SmartDashboard debug values but no persistent structured logs. 6328 and 125 both run AdvantageKit: every vision observation, std dev, rejection reason, and the resulting pose are logged and replayable in simulation, so a bad match can be re-run with modified fusion code against real data. This is the single highest-leverage summer investment — every other finding becomes testable instead of guesswork.

**Fix:** adopt AdvantageKit (IO-layer refactor of vision + odometry only is enough to start), or minimally WPILib `DataLog`/Epilogue logging of: per-camera pose, tag count, avg distance, ambiguity, accepted/rejected + reason, std devs used, fused pose, Quest pose. View in AdvantageScope.

### F4. Hard mid-operation pose resets
Re-anchor paths call `resetCTREPose` + IMU reset while the robot may be moving (delayed MT1 recal fires 5 s after anchoring; tag-loss return; Quest↔LL transitions). Each reset discards filter history, snaps the pose (visible as aim jumps), and trusts one single measurement completely. Top teams reset only at startup/disabled; during operation the filter converges to vision via std-dev weighting.

**Fix:** keep hard resets for disabled/pre-match seeding only. For recovery after tag loss, temporarily *lower* vision std devs (or use `gatePassOverride`-style acceptance with normal fusion) instead of resetting.

### F5. Innovation gating can lock in drift
`gateMeasurement` rejects vision > 0.6–0.8 m from odometry. If odometry drifts past the gate (collision, wheel slip in defense), every correct vision measurement is rejected until the tag-loss re-anchor logic happens to fire. 6328 has no odometry-innovation gate at all — rejection is physical-plausibility only (field bounds, Z, ambiguity); weighting handles the rest.

**Fix:** replace the hard gate with (a) physical rejection (F7) and (b) distance-scaled std devs. If you keep a gate, make it time-bounded: after N consecutive rejections, accept (the filter, not a reset, pulls the pose back).

### F6. Std dev model is mis-shaped and uncalibrated
Ours is linear in distance with a floor of 5 cm; tag-count credit is small and linear. Real PnP error grows ~quadratically with distance, and multi-tag improves accuracy dramatically. 6328: `coeff · dist²/tagCount²` (XY 0.01, theta 0.03) with per-camera factors; 125: `1.5 · dist²`. Also: we read `rawFiducials[0].ambiguity` — the *first* tag, not the worst or average — both in ranking and rejection.

**Fix:** switch to `k · avgDist² / tagCount²` per axis, per-camera factor; calibrate `k` from logged data (F3) by comparing stationary vision scatter at known distances. Use max ambiguity across fiducials.

### F7. Hard 3 m distance cutoff + missing physical rejection
Discarding everything beyond 3 m throws away multi-tag estimates that distance² weighting would handle gracefully — significant on the 2026 field when crossing the neutral zone. Meanwhile, the only sanity check is |coord| < 100 m; nothing rejects poses outside the actual field or with implausible Z (we use 2D, but MT1 3D pose Z is available in LL botpose and is a strong garbage detector).

**Fix:** drop (or greatly extend) the distance cutoff for multi-tag; reject instead on field border ±0.5 m and Z ∈ [−0.5, 1.0] m.

### F8. Field layout fixed at compile time — welded vs AndyMark
We hardcode `k2026RebuiltAndymark`. The two official 2026 layouts differ by enough to introduce systematic cm-level error if a venue uses the welded field. 6328 passes an `aprilTagLayoutSupplier` and switches per event. Note: the layout in the *robot code* must also match what each Limelight has uploaded as its fgc/field map — a mismatch between LL-internal map (used for MT) and robot-side layout is a silent systematic error.

**Fix:** make layout selectable (dashboard chooser / event flag), and add a pre-match checklist item verifying the uploaded LL field map matches.

### F9. Camera extrinsics are unverified config, not calibrated values
Robot-to-camera transforms live only in the LL web UI. A 1° pitch error ≈ 5+ cm of pose error at 3 m. 6328 measures transforms from CAD, then adds per-camera live-tunable pitch fudge values tuned against ground truth (theirs run −4.5° to −1.0° — even a CNC'd robot needs them).

**Fix:** summer calibration procedure — place robot at surveyed field positions, log per-camera pose error vs distance/angle, adjust extrinsics until per-camera bias is zero. Add the per-camera stdDevFactor from the residual scatter.

### F10. LL fusion stops when Quest is primary
In CALIBRATED_Q only Quest frames are fused; LL is used solely for re-anchoring. Quest drifts (slowly) and has no absolute reference; LL has absolute accuracy but noise. Fusing both continuously — Quest with tight std devs, LL with distance²-weighted std devs and infinite theta — gives drift-free *and* smooth output, and removes the need for most re-anchor machinery (F4).

### F11. Hardware under-use
Only 2 of 4 LL4s mounted. 6328 runs 4 cameras specifically so multiple tags stay in view in the chaotic neutral zone. More simultaneous tags → multi-tag solves → trusted rotation + better translation. The Hailo LL4s could simultaneously run object detection (fuel/robot detection like 6328's Northstar objdetect pipeline) without sacrificing tag pipelines.

## 4. Summer plan

**Phase 1 — Instrument (2–3 weeks).** Add AdvantageKit (or DataLog) logging of all vision inputs/decisions (F3). No behavior changes. Outcome: replayable logs from driver-practice sessions.

**Phase 2 — Measure baseline (1 week).** Ground-truth protocol: tape-survey 8–10 field positions (include long-distance and oblique-angle views); log stationary scatter and bias per camera per position; drive repeatable paths and measure end-pose error; capture defense-style hits to characterize odometry drift. This dataset is the before/after yardstick.

**Phase 3 — Fusion fixes (3–4 weeks), in order:** F1 (MT2 theta = ∞), F2 (fuse all cameras), F6 (dist²/tagCount² std devs, calibrated from Phase 2 data), F7 (physical rejection replaces distance cutoff), F5 (remove/soften innovation gate), F4 (eliminate mid-operation resets), F10 (continuous LL+Quest fusion). Each change validated by replaying Phase 2 logs (this is why Phase 1 comes first).

**Phase 4 — Hardware & calibration (parallel).** Mount remaining LL4s; run F9 extrinsics calibration for all cameras; decide Quest's future role (with F1–F10 done, LL-only may match Quest precision with far less complexity — evaluate with data).

**Phase 5 — Validate.** Re-run Phase 2 protocol; target: < 3 cm stationary bias at 4 m, no pose jumps > 5 cm during normal driving, shot-relevant distance error < 2% at hub range.

## 5. Reference repos and resources

- 6328 2026 (updated daily): https://github.com/Mechanical-Advantage/RobotCode2026Public — see `subsystems/vision/Vision.java`, `RobotState.addVisionObservation`, `northstar/`
- 125 nu25: https://gitlab.com/nutrons125/nu25 — see `subsystems/vision/`, `Robot.java` vision fusion block
- 254, 2910, 1690, 2056 have **not published 2026 code yet** (as of June 2026; they typically release post-season). Watch: https://github.com/flamingchickens1540/frc-software-releases and each team's GitHub org.
- 6328's 2026 Championship conference talk: "Beyond the Coprocessor: Lessons in FRC Vision & Localization" (slides usually posted in their Chief Delphi build thread: https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2026-build-thread/509595)
- AdvantageKit vision template (the basis of nu25's code): https://docs.advantagekit.org/getting-started/template-projects/

## 6. Sources

- Our code: `T:\Projects\MechaRAMS\2026Competition\2026Competition` (OdometryUpdates/*, lib/LimelightHelpers.java, lib/QuestHelpers.java, subsystems/DriveSubsystem.java)
- [Mechanical-Advantage/RobotCode2026Public](https://github.com/Mechanical-Advantage/RobotCode2026Public)
- [nutrons125/nu25](https://gitlab.com/nutrons125/nu25)
- [FRC software releases list](https://github.com/flamingchickens1540/frc-software-releases)
- [FRC 6328 2026 build thread](https://www.chiefdelphi.com/t/frc-6328-mechanical-advantage-2026-build-thread/509595)
