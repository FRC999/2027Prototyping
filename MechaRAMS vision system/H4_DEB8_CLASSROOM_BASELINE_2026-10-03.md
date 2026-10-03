# H4 deb8 — classroom baseline and frame-corner geometry

File: `C:\MechaRAMS\temp\akit_rotated_1791069008844_861ddeb8.wpilog`.
One completed DIAGONAL_WITH_YAW; paired disabled camera captures are present and
valid. No robot-code changes, build, tests, simulation or deployment performed.

Measurement policy clarified by mentor: manual distances are approximate sanity
checks, not precision survey data. Use camera-based localization as the primary
operational reference for prototype tuning, with gyro/module data for actual motion.
The corner calculations below explain geometry only; do not tune constants or
claim a precise physical error from their decimal places. Camera estimates are
still not independently verified absolute ground truth.

## Motion result

| Metric | Result |
|---|---:|
| Command start / finish | 1256.106907 / 1259.484305 s |
| Whole command | 3.377 s |
| PathPlanner travel phase | 2.196 s |
| Final-controller phase, including final travel/turn | 1.077 s |
| First tight-pose entry | 1259.009666 s |
| First hold entry | 1259.193330 s |
| First tight-pose entry to command end | 0.475 s |
| Hold entry to command end | 0.291 s |
| Hold releases / timeout | 0 / false |
| Strict finish-motion classification / finish qualified | true / true |
| Logged yaw at finish / at +1 s | -18.591 / -18.573 degrees |
| Target yaw | -20 degrees |
| Controller-estimated position / yaw error at finish | 2.22 cm / 1.40 degrees |
| Battery minimum | 10.45 V |
| Final-loop median / maximum | 23.9 / 62.4 ms |

The controller's final sampled pose and the periodic Drive/Pose have small
within-loop differences; use controller error/qualification for the finish gate.
H4's configured translation window is 4 cm, yaw window 1.5 degrees. Both are met
in its estimated pose. This is not independent proof of physical X accuracy.

At the >0.05 m/s module-speed threshold, first-to-last motion spans 2.881 s and
first tight-pose entry to last motion is 0.184 s (6.4%). There is a further quiet
hold/qualification interval before command completion. Thus the nearly jitter-free
observation is supported, but the full first-arrival-to-command-end interval is
still 0.475 s; do not present that as 0.184 s.

Low-level encoder velocities below that motion threshold persist intermittently:
after completion max module speed is 0.016 m/s, and the 0.01 m/s threshold would
produce a much longer tail. Threshold choice changes the metric; do not label
either threshold as absolute mechanical rest without considering gyro, module
angles and physical observation.

The default command preserves the measured-angle hold. Steering-target jump after
completion is zero; heading changes by at most 0.027 degrees in the enabled post
window. There is no repeated recovery correction like H5 7461. Post-finish enabled
recording is 2.832 s, slightly below the requested three. Gyro status valid; no
brownout. Battery voltage alone does not prove whether the battery was replaced.

## Correct interpretation of the physical measurements

Mentor supplies left X2.405 m, right X2.200 m, right Y0.870 m and confirms the Y
reference is the **rear-right frame corner**. Mentor further confirms ALL reported
displacements use rear corners: left X is rear-left, right X is rear-right. No
front-corner interpretation should be used for these measurements.

Geometry used: current module-center wheelbase/trackwidth 0.4572 m plus the
mentor's previously measured 0.075 m wheel-center-to-frame offset on both sides.
That implies a 0.6072 m square frame, centered on the drivetrain, half-length and
half-width 0.3036 m. Recheck if the actual measuring references differ. Assume
initial frame square to field +X and displacements measured along fixed X/Y axes
from each corner's own starting mark, not along the rotated frame.

The left/right X difference of 0.205 m implies approximately **-19.73 degrees**
of clockwise rotation. That agrees closely with the requested -20 degrees; logged
normalized yaw is about one degree less clockwise. Camera mean heading changes
are another estimate, not the physical reference.

At -19.73 degrees the rear-right corner moves approximately +0.1203 m in Y solely
from rotation about the center. Subtracting that from measured +0.870 m gives
**robot-center Y displacement about +0.7497 m**, essentially the +0.750 m target.
Had the same reading been from the front-right corner, the answer would instead
be about +0.9547 m. This is why the corner identity matters.

With the confirmed rear-corner references:

| Quantity | Calculated robot-center displacement | Difference from target |
|---|---:|---:|
| X | +2.2847 m | +3.47 cm |
| Y | +0.7497 m | -0.03 cm |
| Yaw | -19.73 degrees | 0.27 degrees less clockwise |

The average of the two X readings, 2.3025 m, is the rear-edge midpoint, not the
robot center. Rotation moves that midpoint about +1.78 cm in X, leaving center
displacement about2.2847m. These approximate checks do not contradict the current
H4 4cm translation/1.5degree yaw limits, but are not a precision acceptance survey.
Do not call the inferred tenths of a millimeter meaningful measurement precision.
Do not infer a wheel-radius correction from the3.47cm difference: vision is fused
during the route and distance scale needs an independent diagnostic.

## Paired static captures

Both new captures reached 100 samples per camera, disabled, with reported wheel
speed zero. Ignore inherited Ready statistics at the beginning of the rotated log.

| Capture | Time interval | Left XY sigma | Right XY sigma | Mean camera-to-camera XY separation |
|---|---|---:|---:|---:|
| Before | 1247.377–1250.325 s | 1.57 cm | 0.82 cm | 2.47 cm |
| After | 1267.648–1270.222 s | 0.156 cm | 0.138 cm | 1.51 cm |

Paired camera-inferred ROBOT displacement: left +2.2506 X/+0.7249 Y/-18.143 degrees;
right +2.2567 X/+0.7164 Y/-18.141 degrees. These are after-minus-before camera
means, not raw camera-center poses. Lower endpoint scatter does not prove absence
of systematic bias. Both cameras agree well on X change, but that still needs the
independent physical reference.

## Baseline lesson and next diagnostic

The three planned driving runs are complete:

- H5 78cd: good stopping, physical versus estimated Y discrepancy.
- H5 7461: closer physical return, real late yaw escape/recovery and long settling.
- H4 deb8: stable final hold, physical centerY about+.75m, centerX about+2.285m
  and rotation about-19.73degrees using the confirmed rear-corner references.

Keep the current controller settings for this analysis. No extra H4 repeat or
mandatory ruler-intensive localization test is needed now. The next tuning
investigation is the log-confirmed late yaw escape/recovery in H5 7461; preserve
the successful H4 hold/stop behavior. This is an investigation target, not a
claim that a new controller change has already been implemented.

A [static X-distance test card](STATIC_X_DISTANCE_TEST_CARD_2026-10-03.md) is
available as an OPTIONAL later diagnostic if a repeatable large discrepancy needs
checking, or when teaching the new-chassis calibration procedure. It is not the
current required classroom sequence under the mentor's measurement policy.
