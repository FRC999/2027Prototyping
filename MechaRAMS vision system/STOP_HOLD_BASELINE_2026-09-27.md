# September 27 stationary and holonomic baseline

## Stationary pair

`fef2` (1A) and `e3b0` (1B) each completed a disabled 100-sample capture per camera.
Camera0 mean displacement was (-0.02581, +0.75056) m with +0.561 degree yaw change.
Camera1 mean displacement was (-0.00652, +0.70985) m with +1.057 degree yaw change.
Actual ruler displacement between these captures has not been separately confirmed by the user.
The requested displacement was +0.750 m Y. Do not treat the requested value as measured truth.
No camera transform, lateral scale, or vision weight adjustment is justified yet.

## Moving baseline

| Test / log | Total command | Continuous PathPlanner | Final controller | First hold to command finish | Hold releases |
| --- | ---: | ---: | ---: | ---: | ---: |
| H3 / 83fd | 4.355 s | 2.181 s | 1.806 s | 1.054 s | 7 |
| H4 / 5a1b | 4.959 s | 2.194 s | 2.638 s | 1.906 s | 5 |
| H5 / 76a2 | 7.624 s | 5.738 s outbound plus return | 1.622 s | 0.941 s | 2 |

Totals also include startup/reset overhead. First hold to finish is a controller measure, not an
independent measurement of visually observed jitter or time until every wheel stops.
All three runs completed without timeout, with valid gyro-rate status at the recorded hold exits.

| Test | Physical left/right front-corner X | Physical Y | Fused center displacement X/Y | Fused final yaw |
| --- | --- | --- | --- | --- |
| H3 | 2.260 / 2.285 m | approximately +0.810 m | +2.23796 / +0.77859 m | +1.146 deg |
| H4 | 2.380 / 2.165 m | not supplied | +2.24065 / +0.73331 m | -20.009 deg |
| H5 | 0 / 0 m | -0.015 m | -0.00744 / +0.01042 m | -0.655 deg |

H4 front-corner displacements include rotation of the measurement points. Their average is the
front-edge midpoint movement, not automatically the robot-center movement. H3 Y was explicitly
very approximate, so it cannot calibrate lateral scale.

## Evidence for the next isolated change

Six H3 hold releases were yaw-speed driven and one translation-speed driven. Four H4 releases
were yaw-speed driven and one translation-speed driven. Both H5 releases involved excessive
yaw speed; one also exceeded translation speed. After first hold, raw speed escape intervals
included many 20..70 ms excursions and some 100..233 ms excursions. H4/H5 also subsequently
crossed the wider pose envelope. These logs do not establish that all motion was sensor noise.

The proposed 80 ms continuous speed confirmation keeps zero commanded through short excursions,
preserves immediate pose escape and correction for sustained motion, and blocks success while
speed confirmation is pending. Success rechecks current tight pose/speed qualification. Physical
retest is required because changed motor commands change future measurements; an old-log policy
comparison cannot predict the new robot trajectory.

Retest H4 twice, H3 once, H5 once, each from the 1A marks in a separate finalized log. No speed,
PID gain, route, camera transform, or localization weight changes are part of this experiment.
