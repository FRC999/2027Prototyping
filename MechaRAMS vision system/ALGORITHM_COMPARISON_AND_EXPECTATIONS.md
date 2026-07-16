# Algorithm Comparison and Preliminary Expectations

Author: Claude (Fable 5) session, 2026-07-16
Purpose: the *logical* differences between the switchable vision algorithms, what should happen when
we A/B them, and what a deviation from those expectations would mean. Read this BEFORE running the
"2026-07-16 A/B validation plan" in `VISION_AND_TRAJECTORY_TEST_PLAN.md`; the walkthrough
(`CODE_WALKTHROUGH_VISION_AND_TRAJECTORY.md` A7) covers the same code line-by-line.

All numeric expectations below are order-of-magnitude engineering estimates, clearly labeled — the
whole point of the A/B plan is to replace them with measured values.

---

## 1. Single-tag pose: PnP (baseline) vs TrigSolve

### What each algorithm actually computes

| | `PNP` (baseline) | `TRIG_SOLVE` |
| --- | --- | --- |
| Inputs from the camera | All 4 tag corners → full 6-DOF camera-to-tag transform (translation AND rotation) | Only the camera-to-tag **translation** (range + bearing); the PnP rotation is discarded |
| Inputs from the robot | None (pure exteroception) | Robot **heading at the frame timestamp** (CTRE odometry buffer) |
| Assumptions | Tag geometry known | Tag geometry known + drivebase flat on the floor + heading is accurate |
| Robot XY comes from | `fieldToTag ∘ cameraToTag⁻¹ ∘ robotToCamera⁻¹` — the PnP **rotation participates** in locating the robot | Tag's known field position minus the field-frame robot→tag vector, rotated by the **gyro heading** |
| Robot θ output | Discarded by policy (σθ = +∞) | The heading we were given — discarded by policy too (σθ = +∞; fusing it back would be circular) |

### The one-sentence logical difference

In single-tag PnP, an error in the *solved rotation* swings the computed camera position **around the
tag** like a lever arm — so the notorious two-solution ambiguity (and plain rotation noise) corrupts
XY even though we never fuse the heading. TrigSolve cuts that lever arm by substituting the known
heading; the (possibly flipped) PnP rotation drops out of the math entirely
(`SingleTagTrigSolverTest.corruptedPnpRotationDoesNotMoveTheSolution` pins this).

### Error budget (estimates, single tag at d ≈ 2 m, 1280x800 OV9782)

| Error source | PNP | TRIG_SOLVE |
| --- | --- | --- |
| Range (along the camera→tag ray) | ~1–2% of d (tag apparent size) → ~2–4 cm | Same ~2–4 cm (same measurement) |
| Bearing (across the ray) | ~0.05–0.1° → millimeters | Same |
| Rotation coupling | Degrees-level rotation noise × d → **several cm, and bimodal** (ambiguity flip → decimeters, gated at 0.20 but a gate is not a fix) | **Zero from PnP**; instead heading error × d: with a Pigeon 2 kept honest by multi-tag θ corrections (≤ ~0.5°) → ≤ ~1.7 cm at 2 m, unimodal |
| Camera mount / intrinsics error | Affects both | Affects both (R0 exists for a reason) |

Net expectation: **TrigSolve's single-tag XY scatter should be visibly tighter and unimodal**,
mostly in the across-ray direction, and should degrade *gracefully* (linearly with heading error and
distance) instead of *catastrophically* (ambiguity flip).

### Failure-mode duality (what each is immune to)

- Ambiguity flip / rotation noise: PnP **vulnerable**, TrigSolve **immune**.
- Gyro/heading drift: PnP **immune**, TrigSolve **vulnerable** (error ≈ Δθ·d, so ~3.5 cm per degree
  at 2 m). Our mitigations: multi-tag frames keep correcting θ; the reset quarantine covers the
  heading-buffer race after a pose reset; frames fall back to PnP when the buffer can't answer.
- Tilted robot (ramp/collision): PnP tolerates it; TrigSolve's flat-floor assumption breaks — same
  caveat as PhotonVision's `CONSTRAINED_SOLVEPNP`. Irrelevant on the bench, revisit for a real field.
- The subtle one — circularity: TrigSolve consumes our own heading estimate. This is SAFE for θ
  (we never fuse θ from single tags) but means single-tag XY quality is now *conditioned on* multi-tag
  frames appearing often enough to keep θ honest. A long single-tag-only stretch is the scenario to
  watch in R1 (static angled positions).

### When PnP would still win

Only when the heading is wrong and the PnP solve is clean: right after a gyro glitch, with a badly
measured camera yaw, or at very close range where PnP is well-conditioned anyway. If the A/B shows
PnP winning anywhere else, suspect the camera mount transforms (R0) before suspecting the algorithm.

---

## 2. Covariance: Isotropic (baseline) vs Anisotropic

### The logical difference

Both models answer "how much should the estimator trust this frame?" The isotropic baseline
(`baseline · d²/tagCount² · cameraFactor`) says **the same in every direction**. Physically that is
false: a camera measures a tag's *bearing* (sub-pixel corner centroid) far better than its *range*
(apparent size). The real error ellipse is elongated **along the camera→tag ray**, typically 2–4x.
The anisotropic model states exactly that: two power laws `σ = C·dᴱ` (parallel/perpendicular),
rotated into field axes through the ray angle α.

### What a mismatched model does to the estimator

With isotropic σ the Kalman gain is direction-blind: the estimator **over-trusts the noisy along-ray
component** (pose "breathes" toward/away from the board with each frame) and **under-trusts the
clean across-ray component** (slower lateral convergence). On our board (tags at x=6 facing −x,
approach along +x) the ray is nearly field-X at the target, so: expect X-axis jitter/pulling from
range noise under ISOTROPIC, and cleaner X with unchanged-or-better Y under ANISOTROPIC. This is the
prime suspect for the 2026-07-01 post-command drift watch item (pose moved ~8 cm between command
finish and disable while the robot sat still — accepted frames kept dragging the estimate).

### Honest caveats

- The shipped `ANISO_*` coefficients are PROVISIONAL — same order of magnitude as the isotropic
  baseline at 2 m with a 2:1 parallel:perp ratio. Until stage R2 fits them from real logs, the model
  has the right *shape* but made-up *magnitudes*. **In sim expect little to no visible difference**
  (S4 is a regression check, not a victory lap).
- θ deliberately keeps the baseline model (no unfitted second power law); 5940's θ-blending is a
  documented follow-up once we have fitted data.
- The two experiments interact: TrigSolve *changes the shape* of single-tag error (across-ray error
  becomes heading-driven). Therefore **R2 must fit coefficients with the single-tag strategy you
  intend to compete with** (fit under TrigSolve if S2/S3 promote it). Fitting under PNP and running
  under TRIG_SOLVE would bake the wrong ellipse in.

---

## 3. Preliminary expectations per test (prediction → what a deviation means)

| Test | Prediction | If it deviates |
| --- | --- | --- |
| S1 regression | Identical to 2026-07-01 (~0.03–0.05 m end-pose, no timeouts); `TrigSolvedPoses` empty | Any change = the refactor broke the baseline path — stop, bisect (VisionPolicy extraction is the suspect) |
| S2 TrigSolve precision-only | End-pose error equal or slightly better; `LastUsedTrigSolve` true on single-tag frames; tighter `AcceptedPoses` scatter near the board | Worse error or oscillation → heading sampling suspect (`sampleHeadingAt` empty/laggy?) — check fallback rate first |
| S3 TrigSolve handoff | Clearest sim win: tighter scatter in the x>3.3 region (single-tag views dominate); equal-or-better end-pose | No difference at all is ACCEPTABLE in sim (sim ambiguity flips are rare); the decisive test is R1 |
| S4 AnisoCov (provisional) | No visible change; no new timeouts; drift no worse | A regression here = the rotation-into-field-axes math or ray-angle anchor is wrong — the unit tests bound this, so check `primaryTagId` plumbing |
| S5 combined + reset | No regression vs best of S3/S4; pose snaps and stays after A-press in TrigSolve mode | Bounce-back after reset = quarantine/heading-buffer interplay bug — grab the log, it is exactly what `ResetSuppressedPoses` diagnoses |
| R1 static grid | THE decisive TrigSolve test: at angled single-tag positions, TrigSolve scatter ≤ PnP everywhere, biggest gap at long range/high ambiguity | PnP wins anywhere → re-verify camera mounts + intrinsics (R0) before judging; TrigSolve slow bias growing over minutes → θ is drifting, check multi-tag correction rate |
| R2 fit | Fitted parallel σ noticeably > perp σ (that ratio IS the anisotropy); exponents near 2 | Parallel ≈ perp → anisotropy is not worth its complexity on our board; keep ISOTROPIC and record that honestly |
| R3/R4 driving A/B | TrigSolve: equal-or-better mean end-pose, fewer outliers. AnisoCov (post-R2): reduced post-command drift, cleaner along-ray behavior | Drift persists under fitted AnisoCov → the drift is NOT covariance-shaped; next suspects are mount flex or a systematic range bias (calibration) |

Decision rule (R5): adopt whichever configuration has the best *worst-case* end-pose error over 5
runs — competition cares about the worst cycle, not the average one.
