# The Algorithms, Explained (Student Guide)

Author: Claude (Fable 5) session, 2026-07-16
For: Team 999 students. No math background needed — the full engineering version with error budgets
lives in `ALGORITHM_COMPARISON_AND_EXPECTATIONS.md`, and the line-by-line code tour is
`CODE_WALKTHROUGH_VISION_AND_TRAJECTORY.md`.

Our robot constantly answers two questions: **"Where am I?"** (localization) and **"How do I drive
to where I need to be?"** (trajectory). We are testing several algorithms for each. Here is what
each one does, in plain language, and when each one wins.

---

## Part 1 — "Where am I?" (localization)

Think of the AprilTags on our board as **street signs with known addresses**. The robot's cameras
read them and work backwards: "if that sign looks like THAT from here, I must be standing HERE."

### Algorithm 1: Wheel odometry + gyro (the always-on base guess)

The robot counts wheel rotations and uses its gyro (an electronic inner ear) to track how it has
moved — like walking across your room with your eyes closed, counting steps. It updates 250 times a
second and is very smooth, but errors pile up: every little wheel slip is a step you counted wrong.
After a while, eyes-closed walking gets you lost. That is why we need the cameras — they are the
moments we "open our eyes" and correct the guess.

### Algorithm 2: Multi-tag solve (see two signs at once)

When a camera sees **both** tags in one picture, geometry pins the robot down almost perfectly —
like seeing two landmarks and knowing you must be at the one spot where both look exactly like that.
This is our best measurement: we trust its position AND its direction. Nothing to A/B here — when
two tags are visible, every strategy we test uses this and agrees.

The interesting question is what to do when the camera sees only ONE tag. That happens a lot —
during turns, at angles, at the edge of camera view. Algorithms 3 and 4 are the two competing
answers.

### Algorithm 3: Single-tag PnP (the baseline)

With one tag, the camera guesses the tag's **position and tilt** from how its square shape looks
distorted in the picture, then works backwards to the robot's position.

The weakness: judging the **tilt** of a flat square from far away is genuinely hard. A poster
angled 20° left and one angled 20° right look almost identical from across a room. Computers have
the same problem — it is called **ambiguity**, and the algorithm sometimes picks the mirror-image
answer. And here is the nasty part: even though we never trust the *direction* this algorithm
reports (we learned that lesson in 2026), a wrong tilt guess **swings the computed position around
the tag like a lever** — wrong tilt, wrong position. We currently protect ourselves by throwing
away suspicious frames (an "ambiguity gate"), but throwing data away is a bandage, not a cure.

### Algorithm 4: Single-tag TrigSolve (the challenger)

Key insight: **the robot already knows which way it is facing** — the gyro tells it, and the
multi-tag frames keep the gyro honest. So why struggle to guess the tag's tilt from the picture?

TrigSolve uses only two things from the camera that are genuinely easy to measure: **which direction**
the tag is (very accurate — you can point at something across a room without thinking) and **how far**
it is (decent). Combine that with the known facing direction, and the tag's known address:
"the sign is 2 meters ahead-and-a-bit-left, I'm facing the board, so I must be standing HERE." The
tilt guess — the unreliable part — drops out of the math completely. The mirror-image problem
physically cannot affect it.

The trade: TrigSolve inherits the gyro's mistakes. If the robot's sense of direction drifts (say,
after a hard collision, or a long stretch without seeing two tags), TrigSolve's positions shift
with it. So the two algorithms fail in **opposite** ways — PnP is immune to gyro drift but
vulnerable to the mirror problem; TrigSolve is immune to the mirror problem but vulnerable to gyro
drift. That is exactly why we A/B them instead of arguing about them.

### Algorithms 5 & 6: The trust models (isotropic vs anisotropic)

Every camera measurement gets merged into the base guess (Algorithm 1) with a **confidence weight** —
like averaging opinions but listening more to the friend who is usually right. Algorithms 5 and 6
are two ways of stating that confidence.

Here is the physical fact they interpret differently. Look at a car's headlights at night: you know
**which direction** the car is with pinpoint accuracy, but **how far away** it is — much harder.
Cameras are the same: direction to the tag = precise; distance to the tag = fuzzy. So the region of
"places the robot might actually be" is not a circle around the estimate — it is a **stretched oval
pointing at the tag** (fuzzy along the line to the tag, sharp across it).

- **Algorithm 5 — Isotropic ("the circle", baseline)**: pretends the uncertainty is the same in
  every direction. Simple, works, but it over-trusts the fuzzy distance component — so the robot's
  belief "breathes" toward/away from the board with each frame. We suspect this is why the pose
  drifted ~8 cm AFTER the robot stopped in our July 1 test.
- **Algorithm 6 — Anisotropic ("the oval", challenger)**: tells the truth about the oval shape, so
  the merge distrusts the fuzzy direction and fully uses the sharp one. One honest caveat: the
  oval's exact size comes from numbers we have to **measure on the real robot first** (test step 15);
  until then we run it with placeholder numbers, and it should merely not-be-worse.

---

## Part 2 — "How do I drive there?" (trajectory)

### Algorithm 7: Path following (PathPlanner)

The robot follows a pre-planned route like a dancer performing a memorized routine to music: at
each beat, be at this spot. It is fast and smooth — but it ends **when the music ends, not when
you're on your mark**. Our July tests measured it stopping ~7 cm off. Great for covering distance,
wrong tool for the last centimeters.

### Algorithm 8: Drive-to-pose ("precision parking")

No route, no clock — just "get to THIS exact spot and settle": drive, measure, correct, repeat,
and only declare done after **staying** inside the tolerance for a moment (plus a safety timeout so
it can never get stuck forever). Our July tests: ~3 cm. Slower than path following, but it finishes
the job. Best final-approach tool; too slow to cross the whole field with.

### Algorithms 9 & 10: Combining them — sequential vs spatial handoff

Both use the dancer for the long move and the parker for the finish; they differ in WHEN they
switch.

- **Algorithm 9 — Sequential**: finish the entire routine, then park. Simple, predictable, but you
  wait for the music to end even when you're already close.
- **Algorithm 10 — Spatial handoff (our chosen competition pattern)**: the moment the robot crosses
  a line on the floor (x > 3.3 m for us), abandon the routine and start parking. Faster cycle, and
  the picky parker controls more of the final approach. Our July data: parked 2–3x more accurately
  than path-following alone, with no timeout.

---

## The comparison table

| # | Algorithm | One-liner | Strong when | Weak when |
| --- | --- | --- | --- | --- |
| 1 | Odometry+gyro | Eyes-closed step counting | Short intervals between camera fixes | Long stretches, wheel slip |
| 2 | Multi-tag solve | Two landmarks pin you down | Both tags visible | Only works with 2+ tags in frame |
| 3 | Single-tag PnP | Guess the sign's tilt, work backwards | Close range, head-on views; wrong gyro | Far/angled views (mirror problem) |
| 4 | Single-tag TrigSolve | Known facing + direction/distance to sign | Far or angled single-tag views, turns | Gyro drift (rare for us; multi-tag frames keep fixing it) |
| 5 | Isotropic trust | Uncertainty is a circle | Simplicity; already validated | Over-trusts fuzzy distance → belief "breathes", post-stop drift |
| 6 | Anisotropic trust | Uncertainty is an oval pointing at the tag | Sitting/parking near the board; long-range frames | Needs real-robot measurements first (step 15) |
| 7 | Path following | Memorized dance routine on a clock | Covering distance fast | Ends on time, not on target (~7 cm off) |
| 8 | Drive-to-pose | Precision parking with a settle check | The last half meter (~3 cm) | Slow over long distances |
| 9 | Sequential handoff | Full dance, then park | Debugging (predictable) | Wastes time when already close |
| 10 | Spatial handoff | Cross the line → start parking | Competition cycles (fast AND accurate) | Needs a sensible handoff line per path |

## Which to use when (the cheat sheet)

- **Both tags visible?** Doesn't matter — Algorithm 2 dominates and all configurations agree.
- **Single tag, angled or far view** (the common case during approaches): **TrigSolve** should win —
  the mirror problem can't touch it. This is what test steps 3, 5, and 14 measure.
- **Curved / rotating trajectory** (our new S-curve test): cameras sweep across the board, so
  single-tag moments multiply — the more the robot turns, the more **TrigSolve** matters
  (steps 8–10). The gyro heading it relies on stays valid through the turn because the odometry
  buffer remembers the heading *at the moment each picture was taken*.
- **Higher speeds**: the picture the camera took is already ~50-100 ms old when we use it, and at
  2 m/s that is 10-20 cm of travel — so what matters most is **timestamping** (every algorithm we
  run matches each frame to where the robot was when the photo was taken — this is non-negotiable
  plumbing, not a choice) and **spatial handoff** (Algorithm 10), which stops wasting time at the
  end of the path. Faster driving also means fewer clean frames, which again favors TrigSolve —
  it extracts a good position from a single imperfect glimpse.
- **Sitting still / final parking near the board**: **anisotropic trust** (Algorithm 6, once fitted
  in step 15) should stop the estimate from creeping after the robot stops — watch the
  "drift at disable" column in the results table.
- **After a collision or gyro glitch**: the one scenario where plain **PnP** is safer than
  TrigSolve until a multi-tag view fixes the heading. This is why the losing algorithm stays in the
  chooser instead of being deleted.

## Vocabulary

- **PnP (Perspective-n-Point)**: the math that answers "given how these known points look in my
  photo, where was the camera?"
- **Ambiguity**: two mirror-image tilts of a flat tag that look nearly identical to the camera.
- **Odometry**: position tracking by counting your own movements (wheels + gyro).
- **Covariance / standard deviation**: the "how sure am I?" number attached to a measurement; the
  merge listens more to smaller numbers.
- **Fusion / estimator**: the running weighted average that combines odometry with camera fixes.
- **Handoff**: switching from the route-follower to the precision parker mid-drive.
- **Settle**: requiring the robot to STAY on target briefly before declaring success, so one lucky
  bounce through the tolerance doesn't count.

---

## What happens after the tests? (what we DO with all these algorithms)

A fair question: we have ten algorithms — do we use all of them? Switch between them during a match?
Here is the plan.

**Most of them are layers, not competitors.** Odometry (1) always runs. The multi-tag solve (2) is
always used whenever a camera sees two tags — no test changes that. Path following (7) and
drive-to-pose (8) both stay, doing different jobs, connected by the spatial handoff (10) we already
picked in July. The only real contests are: **PnP vs TrigSolve** for single-tag frames (3 vs 4), and
**circle vs oval** for trust (5 vs 6). The tests pick ONE winner in each contest.

**The robot then runs one fixed setup — nobody flips switches during a match.** A mode the drivers
never practiced with is how you lose a match, not win one. The "AB:" chooser entries stay after the
decision, but as practice and regression tools: whenever we change vision code later, we re-run the
checklist and confirm the winner still wins.

**But wait — doesn't the robot switch algorithms anyway? Yes — automatically, every frame.** Inside
the one fixed setup, every camera picture picks its own algorithm without any human: see two tags →
multi-tag solve; see one tag → the winning single-tag strategy; can't get a heading for that frame →
quietly fall back to PnP; picture looks impossible → throw it away. The game situation chooses the
algorithm for us, 50 times a second, faster and more reliably than any driver could. That is the
real answer to "do we switch depending on the situation": the switching is built in, per frame.

The one exception we might add later: if testing shows TrigSolve drifting during long stretches
without multi-tag views (its known weakness), we would add an automatic rule — "if the heading
hasn't been double-checked by a two-tag view recently, use PnP for now." Automatic, data-triggered,
and only if the logs prove we need it.

**The results also tune numbers, not just pick winners:** the oval's real size (step 15), each
camera's real trust factor (measured instead of the current "1.0, dunno"), and possibly loosening
the ambiguity gate — if TrigSolve wins, frames we currently throw away for tilt-suspicion become
safe to use, which means MORE measurements, which means better tracking.

**And yes — the same test logs will tell us where the cameras should point.** Camera placement is
really the question "what does each camera see, how often, and how well?", and every run records
exactly that. Three decisions come straight out of the data:

- **The cross-eye angle** (our cameras currently toe inward 18°, a guess): the curved-trajectory
  runs show when we drop to one tag or zero tags mid-turn. If there are blind moments, the map of
  them says whether to angle the cameras wider apart or closer together.
- **Two cameras or four**: if the blind moments happen mostly when the robot rotates away from the
  board, that is the evidence for adding the rear camera pair (the code is already wired for them).
- **Is one of our cameras mounted badly?** If one camera's measurements are consistently fuzzier
  than the other's at the same distance, its mount flexes or its calibration is off — the
  error-vs-distance curves from step 14 expose it immediately.

So the evaluation gives us four things: a winner per contest, real numbers instead of placeholders,
proof (or disproof) of our camera layout — and a repeatable checklist we can re-run any time we
change anything.
