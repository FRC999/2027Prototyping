# Custom pose estimators — annotated walkthrough

Source files copied here for study:

- `6328/RobotState.java` — Mechanical Advantage 2026 (MIT-style license — free to copy)
- `3467/PoseEstimator.java`, `3467/SwerveOdometry.java` — Windham Windup 2026 (GPLv3 — copy only if our code is GPL-published; otherwise reimplement the ideas)
- `5687/PoseEstimator.{h,cpp}`, `OdometryThread.{h,cpp}` — The Outliers 2026 (C++, same architecture)

## The surprise: the Kalman math is identical to addVisionMeasurement

Both custom estimators use the **exact same closed-form Kalman update** as WPILib's
`SwerveDrivePoseEstimator` (which CTRE's `addVisionMeasurement` wraps). 3467's code literally
links the WPILib source lines it copied; 6328 cites `wpimath/algorithms.md`. The update is:

```
K[row] = q / (q + sqrt(q * r[row]))        // q = odometry variance, r = vision variance
correction = K * (visionPose - estimateAtVisionTimestamp)
estimatedPose = (estimateAtTimestamp + correction) + odometrySinceTimestamp
```

So a custom estimator is **not better filter math**. Calling `addVisionMeasurement` with correct
std devs gets you the same blend. What the custom versions buy is control over everything
*around* that equation — five things the CTRE/WPILib black box cannot do.

## Difference 1 — They control the odometry input (the prediction step)

With CTRE, the odometry twist comes from the swerve kinematics, take it or leave it. The custom
estimators construct the twist themselves and intervene when wheels lie:

**6328 `addOdometryObservation` (RobotState.java ~line 133):** computes a tilt factor
`t = 1 − inverseInterpolate(0°, 25°, tiltAngle)` where tilt comes from
`acos(cos(pitch)·cos(roll))`, then scales the twist: `twist.dx*t, twist.dy*t`. A robot being
climbed/rammed (tilted) gets its wheel translation progressively zeroed out. Rotation is always
overridden by the gyro delta when available, with a second fallback gyro path.

**3467 `SwerveOdometry` + `badWheels[]`:** a module flagged as skidding is excluded from the
kinematics solve entirely, and each bad wheel inflates the odometry std dev used in the Kalman
gain (×0.75 linear per wheel, capped at ×4) — so during a skid, vision automatically gets more
authority. With `addVisionMeasurement` the odometry trust (`qStdDevs`) is fixed at construction.

## Difference 2 — Statistical (n-sigma) outlier rejection instead of blind acceptance

`addVisionMeasurement` accepts everything you give it; rejection is your problem, done outside
with ad-hoc thresholds (our 2026 fixed 0.6 m innovation gate — which locks in drift, finding F5).

3467 computes, for every observation (PoseEstimator.java ~line 309):

```
translationNSigma = |visionPose − estimate| / sqrt(visionVar + odometryVar)
```

i.e., the residual measured in units of *current combined uncertainty*. Because odometry variance
is inflated during skid and effectively grows while cameras are blind, a residual that would be
"too big" in meters becomes statistically plausible exactly when the robot is actually lost —
recovery is automatic, no re-anchor state machine needed. (In this snapshot of their code the
n-sigma value is computed and logged, with a `BELOW_MIN_N_SIGMA` rejection reason defined —
the gate can be enabled as a threshold on these values.)

## Difference 3 — Every observation gets a logged verdict

3467 logs each vision frame as ACCEPTED / RESET_LOCKED / MISSING_ODOMETRY_SAMPLE /
BELOW_MIN_N_SIGMA / POSE_VALIDATOR_REJECTED, plus the n-sigma values, plus the candidate pose.
With `addVisionMeasurement` you cannot see what the filter did with your measurement. This is
the observability that makes precision *debuggable* — directly serves our finding F3.

## Difference 4 — Pose validity is a pluggable predicate

`withPoseValidator(pose -> onField(pose))` applies to both odometry and vision-fused candidate
poses — the *result* of the fusion is validated, not just the raw measurement. The black box
will happily integrate into a wall.

## Difference 5 — Time travel for consumers

Both expose `getPoseAtTime()` / `getEstimatedPoseAtTimestamp()` (estimate shifted by buffered
odometry deltas). That is what aiming-while-moving and latency-compensated turret control
consume. CTRE's `samplePoseAt()` exists but only for the fused pose; the customs also expose
the raw odometry pose and rotation buffers separately (6328 keeps a `rotationBuffer` of
Rotation3d for its moving-camera transforms).

## The actual update flow (6328, addVisionObservation, RobotState.java ~line 198)

1. Discard if older than the 2 s pose buffer.
2. `sample = poseBuffer.getSample(obs.timestamp)` — odometry pose at capture time.
3. `estimateAtTime = estimatedPose + (odometryPose → sample)` — shift estimate back in time.
4. Compute K from `qStdDevs` (odometry trust) and `r` (this observation's std devs²).
5. `transform = estimateAtTime → visionPose`; scale by K.
6. `estimatedPose = (estimateAtTime + scaledTransform) + (sample → odometryPose)` — replay to now.

Step 4–6 is `addVisionMeasurement`. Steps 1–3 and the odometry twist construction are where the
customization lives.

## What this means for MechaRAMS

- **Summer (2026 bot):** keep CTRE `addVisionMeasurement`; fix what we feed it (std devs, MT2
  yaw=∞, all cameras). Same math, most of the gain.
- **2027:** custom estimator (~300 lines, this folder is the spec) fed by a 250 Hz odometry
  thread, with: gyro-delta rotation override, tilt scaling (6328), per-wheel skid handling +
  n-sigma gate + verdict logging (3467), pose validator, `getPoseAtTime` for the turret.
  Base it on 6328's MIT code; treat 3467's GPL code as a design reference.
