# Optional diagnostic: measured static X translation

NOT a required next classroom test. The mentor clarified that current manual
measurements are rough checks and camera-based localization is the primary tuning
reference. Retain this card for a later repeatable discrepancy investigation or
for teaching the competition-chassis calibration method. It requires sufficiently
accurate independent displacement measurements to assess scale, not rough estimates.

Purpose: compare each camera's reported distance change with a ruler, independently
of PathPlanner, final-pose control and wheel-distance calibration. This follows the
H5/H4 baseline; it does not require new robot code, a new layout or more cameras.
Do not adjust focus, camera transforms, weights, wheel radius or gains during it.

## Setup

- Both cameras open, tags and cameras unmoved, normal lighting/settings.
- Use the familiar clear straight corridor from the normal starting marks. Do not
  extend into a blocked area or move closer than the already tested safe positions.
- Identify BOTH rear frame-corner floor marks, matching the mentor's measurement
  convention. Use a square or equal left/right
  distances to keep the robot perpendicular to the tag board at each placement.
- Measure along the fixed field X axis, not diagonally between final/initial marks.
- `Static Localization - PnP + Iso (Disabled Only)` selects the common mode.
- Use `Start Camera Jitter Capture (Disabled Only)`, not NEXT 1A/1B: those buttons
  label a different Y-shift experiment.

## One log containing three stationary captures

1. Place the disabled robot at normal start A. Rotate to a new log with
   `Close Current Log And Start New (Disabled Only)`; wait for RotationPending=false
   and note the new ActiveLogPath.
2. Press `Start Camera Jitter Capture (Disabled Only)`. Keep the robot stationary
   until NEW sample counts reach100/100, ComparisonReady=true and Active=false.
   Record A's floor references and capture order.
3. Use slow teleop to move approximately 1 m forward in the existing clear straight
   corridor, then DISABLE. Never approach for measuring or repositioning while
   enabled. Keep the heading square and Y unchanged as closely as practical.
4. Measure actual displacement of both rear corners along X, plus Y change of the
   rear-right corner. Keep the same references throughout; do not substitute a
   different corner. It need not be exactly1.000m; an accurate measured distance is
   more useful than an unverified command to move1m. Record any heading difference.
5. At this near position B, run another disabled stationary100/100 capture to
   completion. Do not move while capture is active.
6. With the route clear, use slow teleop to return near A, disable, and check the
   original physical marks. Record any residual left-X/right-X/named-corner-Y
   displacement rather than pretending the replacement is exact. If necessary,
   adjust safely under the team's normal positioning procedure, then remeasure.
7. Take a third stationary100/100 capture at A2. Finalize the log by rotating again.
   Download the file noted in step1, containing A -> manual movement -> B -> manual
   return -> A2. Save in `C:\MechaRAMS\temp` and send its suffix and measurements.

This is NOT an autonomous trajectory trial; do not select H5 to make the return.
Jitter capture must be off during both manual moves. Normal logging stays on.
No three-second enabled finish wait is needed here because there is no autonomous
finish to audit; wait for each disabled capture's actual100/100 completion.

## Report

| Placement | Rear-left X from A | Rear-right X from A | Rear-right Y from A | Heading/squareness notes |
|---|---|---|---|---|
| A | 0 | 0 | 0; identify reference | |
| B, about1m forward | | | | |
| A2, replaced at start | | | | |

Use signed meters or centimeters and state units. No AdvantageScope values need
manual transcription: the camera means, scatter, counts and fused poses are
already in the WPILOG. The robot cannot record the ruler values for you.

## How we will use it

- Compare camera0 and camera1 B-minus-A X with the actual measured translation.
- Compare A2 with A, accounting for imperfect physical replacement, to distinguish
  repeatability/noise from distance-dependent discrepancy.
- Compare Y/heading too, so a diagonal placement or rotation is not called a scale error.
- If both camera distance changes disagree similarly with the ruler, inspect shared
  tag size/layout and intrinsic calibration/reference geometry before tuning motors.
- If one differs, inspect that camera's calibration/mount/image data.
- If both match across the placements, proceed to an explicitly wheel-only distance
  diagnostic before changing wheel radius. Existing vision-fused `Drive/Pose` is
  not wheel-only odometry; the dedicated diagnostic is still a tooling gap.

One three-capture set is a screening test, not a final calibration certificate.
There is no need to repeat the H4/H5 driving baseline just to complete this test.
