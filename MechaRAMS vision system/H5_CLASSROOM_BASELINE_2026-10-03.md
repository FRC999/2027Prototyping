# H5 classroom baseline: 78cd and 7461

## Conclusion

Both logs contain one successful H5 and valid before/after stationary camera
captures. Settings match each other and 4a06. No robot-code change is made.

78cd stops well but has a measured lateral return discrepancy. 7461 physically
returns closer but really does resume angular correction near the end. These are
two separate calibration questions; neither justifies changing all PID gains.
Complete the planned unchanged-code H4 comparison next, using a charged battery.

## Files and physical measurements

- `C:\MechaRAMS\temp\akit_rotated_1791068430194_b08d78cd.wpilog`
- `C:\MechaRAMS\temp\akit_rotated_1791068794176_61027461.wpilog`

| Measurement | 78cd | 7461 |
|---|---:|---:|
| Left front-frame X displacement | 0 cm | 0 cm |
| Right front-frame X displacement | +1.0 cm | +0.15 cm |
| Right frame-corner Y displacement | -5.0 cm | -1.0 cm |
| Mentor observation | Almost no jitter | Angular jitter, probably >1 s |

The second right-X input was explicitly **0.0015 m**, not 0.015 m. Do not silently
turn 1.5 mm into 1.5 cm. These are frame-corner measurements, not surveyed robot-
center poses. Using the historical 0.6072 m reference width and an initially square
frame, the X differences imply about +0.94 and +0.14 degrees, respectively. That
inference depends on the reference width, squareness and measurement precision.
It does not agree exactly with logged yaw, so do not certify sub-degree accuracy.

## Timing and stopping

| Metric | 78cd | 7461 |
|---|---:|---:|
| Auto start timestamp | 770.560620 s | 1046.791081 s |
| Auto complete timestamp | 777.574542 s | 1055.133795 s |
| Whole command | 7.014 s | 8.343 s |
| Outbound PathPlanner phase | 3.137 s | 3.138 s |
| Return PathPlanner phase before precision | 2.617 s | 2.612 s |
| Final-controller phase | 0.868 s | 2.422 s |
| First tight-pose entry to command end | 0.172 s | 1.636 s |
| First hold entry to command end | 0.172 s | 1.437 s |
| Hold releases | 0 | 1 |
| Finish qualified / timed out | true / false | true / false |
| First-to-last module speed >0.05 m/s | 6.473 s | 7.883 s |
| First tight-pose entry to last module speed >0.05 m/s | 0.102 s | 1.438 s |
| That wheel-motion settling fraction | 1.6% | 18.2% |
| Final-controller loop median / maximum | 22.8 / 80.1 ms | 23.8 / 83.6 ms |
| Minimum roboRIO battery voltage during auto | 10.35 V | 9.55 V |

The >0.05 m/s measure is a thresholded encoder proxy, not proof of absolute rest.
For 78cd, a stricter >0.01 m/s proxy extends the arrival-to-stop tail to 0.258 s
(3.9% of corresponding movement time). Its observed stopping is consistent with
the <10% objective. For 7461, even the 0.05 m/s proxy exceeds 10%; command timing
also shows the long finish. Do not hide it by starting the settling clock only at
the final successful hold.

Whole-command timing includes 0.392/0.171 s before the outbound phase. Actual
PathPlanner travel phases differ by only milliseconds. The extra time is mainly
in final control, not a newly slower path. Neither run logged a brownout. The lower
battery in 7461 is a comparison variable, not proof it caused the oscillation.

Post-completion angle hold is preserved in BOTH runs when DriveManuallyCommand
takes ownership. Steering targets have zero post-finish angle jump. No module
exceeds 0.05 m/s after completion in either run. This is not the old default-command
bug restarting wheel motion. For 78cd, measured module-angle spreads after finish
are at most 0.53 degrees, not another large steering correction.

## What happened during 7461's long finish

1. Precision starts at 1052.711462 s with about 0.25 degrees heading error and
   very low gyro rate. The existing initialization-time classification therefore
   selects the normal, not strict, finishing-motion gate.
2. First tight-pose entry is at 1053.498112 s, but the robot is not yet continuously
   qualified to finish.
3. First hold starts at 1053.697246 s: estimated XY error 1.06 cm, yaw error
   0.81 degrees, measured controller yaw speed 2.27 deg/s. Maximum module speed
   is still about 0.216 m/s. Low chassis speed is not the same as every wheel stopped.
4. During that zero-request hold, yaw continues increasing. At 1053.986125 s it
   reaches 2.658 degrees, exceeding the immediate 2.5-degree pose escape threshold.
   XY error is only 1.71 cm. The controller exits hold and requests angular correction.
   This is not the 200 ms pose-requalification or 80 ms velocity-escape path.
5. Yaw subsequently peaks near 4.879 degrees at 1054.429545 s. Corrective motion
   continues until the second hold at 1054.850982 s, then qualification completes
   at 1055.133795 s.

Enabled vision heading fusion is false. Gyro/heading evidence supports real yaw
motion, not merely a camera yaw estimate changing. Camera XY changes can still
affect translation and module requests. Large loop intervals occur around hold
entry/release; that correlation alone does not prove the root cause.

The useful generic question for the next tuning experiment is whether hold entry
and recovery should account for motion that develops late in the final approach,
rather than relying only on a calm handoff classification. Do not simply widen
yaw tolerance or change all kD values based on this one run. First complete H4 to
compare a deliberately rotating finish under the same software.

## Position reported by the robot versus the ruler

| At command completion | 78cd | 7461 |
|---|---:|---:|
| Estimated X displacement from return target | +1.58 cm | +1.85 cm |
| Estimated Y displacement from return target | -0.24 cm | +0.70 cm |
| Estimated radial error | 1.59 cm | 1.97 cm |
| Normalized yaw | -0.247 degrees | +1.235 degrees |
| Yaw at +1 s, still enabled | -0.241 degrees | +1.093 degrees |

The controller successfully reached its estimated 2 cm window. That does not
establish a 2 cm physical return. In particular, 78cd's measured right-frame Y
is -5 cm; it should not be declared a physical-accuracy pass merely because the
fused Y at completion is near zero. Corner rotation must be accounted for, but
the small corner-X difference does not by itself explain a 5 cm lateral shift.

After completion, fused Y still changes with nearly stationary wheels. In 78cd,
wheel-kinematic integrated travel over the first 2.52 s after finish is about
0.62 mm, while estimated Y varies by centimeters. This is why post-stop pose
changes must not automatically be counted as physical motion.

## Static camera evidence

Each NEW capture below reached 100 samples from each camera while disabled, with
maximum reported wheel speed zero. Capture was off during both trajectories.
7461 initially inherits old Ready statistics; those are excluded.

| Run / capture | Capture interval | Camera0 XY sigma | Camera1 XY sigma | Camera0 minus Camera1 mean Y | Mean XY separation |
|---|---|---:|---:|---:|---:|
| 78cd before | 733.113–736.003 s | 1.17 cm | 1.09 cm | -3.45 cm | 4.41 cm |
| 78cd after | 852.113–854.997 s | 1.47 cm | 1.24 cm | -5.10 cm | 5.12 cm |
| 7461 before | 1034.798–1037.755 s | 1.63 cm | 1.00 cm | -6.02 cm | 6.49 cm |
| 7461 after | 1064.845–1067.746 s | 1.41 cm | 1.30 cm | -1.48 cm | 1.97 cm |

Camera0 is front-left, Camera1 front-right. These are each camera's inferred ROBOT
poses after applying its mount transform, not raw camera-center coordinates.

Paired mean displacement from the before to after capture:

| Run / camera | X change | Y change | Yaw change |
|---|---:|---:|---:|
| 78cd left | +1.19 cm | -3.35 cm | -0.55 degrees |
| 78cd right | -2.02 cm | -1.70 cm | -0.80 degrees |
| 7461 left | +2.35 cm | +1.72 cm | +1.10 degrees |
| 7461 right | +3.46 cm | -2.82 cm | +1.71 degrees |

Neither camera is independent ground truth. They do not even agree on the sign
of 7461's Y change. This is enough evidence to keep camera/physical consistency
as an open calibration item, not enough to fit a new camera transform or weight.

The auto takes its entire start pose from the trusted MultiTag seed. Current
configuration permits left-camera heading trust, but not right-camera heading
trust; normal XY fusion still uses both cameras and downweights left with factor
2.15 versus right 1.0. A difference between that start reference and the blended
estimate is a plausible contributor to return error. It is not proof of all 5 cm
of physical discrepancy, nor justification to treat one camera as unquestionably
correct. Focus calibration remains provisional under the agreed prototype scope.

## Data quality and next action

- Both files are usable and finalized, with one completed driving run each.
- 78cd has 2.520 s enabled post-finish, slightly short of the requested three.
  7461 has 4.125 s, enough for the full three-second audit.
- Gyro rate status is valid in the audited runs; applied update rate is 250 Hz.
- Existing controller settings match 4a06; no new layout or logging is needed.
- Keep the same deployment for one H4. Use a charged battery, record the swap,
  preserve >=3 s supervised enabled post-finish recording, then disable/capture
  and finalize. Physical H4 endpoint surveying is optional for this angular test.
- After H4, separate any generic late-motion/hold-entry experiment from camera
  calibration. Include a return-route regression if controller behavior changes.

This illustrates the commissioning method: good stopping and good localization
are separate pass/fail questions. Do not repair one by obscuring the other.
