# Vision and Trajectory Test Plan

> **2026-06-30 update.** This plan covers the on-robot bring-up specifics (camera placement, tag board,
> precision/aiming tests). The full **staged, simulation-first** process — toolchain, sim validation,
> camera intrinsic calibration, extrinsic measurement, localization accuracy, characterization — now
> lives in `CALIBRATION_AND_TEST_PROCESS.md`. Do **Stage 1 (simulation)** there before any hardware.
> Log channels were renamed in the vision rebuild: cameras are `Vision/Camera0`, `Vision/Camera1`, …
> (index order in `RobotContainer`), with `Vision/Summary/*` aggregates.

## Hardware Baseline

- Coprocessor: one Orange Pi to start (scalable to two Pis for four cameras).
- Cameras: two Arducam OV9782 global-shutter color USB cameras to start.
- Pipeline: PhotonVision AprilTag.
- Robot controller: roboRIO runs final fusion and drivetrain control.
- Logging: AdvantageKit WPILOG plus NT4 for live AdvantageScope.

## Camera Placement

Mount the two cameras near the front-left and front-right corners, above or just inboard of the front swerve modules.

Recommended starting transforms from robot center:

| Camera | X forward | Y left | Z up | Roll | Pitch | Yaw |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| front-left | +0.23 m | +0.24 m | +0.43 m | 0 deg | -18 deg | +18 deg |
| front-right | +0.23 m | -0.24 m | +0.43 m | 0 deg | -18 deg | -18 deg |

Notes:

- Keep both cameras rigidly mounted. Flex will look like vision noise.
- Avoid bumper occlusion at low pitch angles.
- Measure final transforms from the robot coordinate origin after mounting; update `Constants.VisionConstants`.
- The Arducam color camera should work for a pilot because it is global shutter and USB UVC. It may need more lighting/exposure discipline than a monochrome AprilTag camera.

## Tag Board Layout

Use two 6.5 inch AprilTags on one flat board.

Code/deploy layout:

- Field size: 8.0 m by 4.0 m.
- Tag 1 pose: `(6.0 m, 1.75 m, 1.05 m)`, facing toward the robot start area.
- Tag 2 pose: `(6.0 m, 2.25 m, 1.05 m)`, facing toward the robot start area.
- Tag center spacing: 0.50 m horizontally.

Physical setup:

1. Put the board vertical and flat.
2. Put the two tag centers at the same height.
3. Space the tag centers 0.50 m apart.
4. Set tag center height to 1.05 m from the floor.
5. Mark the floor coordinate of the board so the field layout can be reproduced.

## PhotonVision Setup

1. Connect both cameras to the Orange Pi.
2. In PhotonVision, name the cameras exactly:
   - `front-left`
   - `front-right`
3. Set each camera to MJPEG and start around 1280x800 at 50 fps.
4. Calibrate each camera in PhotonVision using the exact resolution used for testing.
5. Load the custom AprilTag layout from:
   `src/main/deploy/apriltags/mecharams-two-tag-layout.json`
6. Set each camera's robot-to-camera transform to match the measured mount.
7. Start with exposure low enough that tag borders are crisp and not washed out.

## First Bring-Up

1. Build/deploy after Java 17 compile verification succeeds.
2. Put the robot at `(1.5 m, 2.0 m, 0 deg)`.
3. Press `A` to reset pose to the test start.
4. Open AdvantageScope and watch:
   - `Vision/Camera0/AcceptedFrames`, `Vision/Camera0/RejectedFrames`, `Vision/Camera0/LastRejectionReason`
   - `Vision/Camera1/AcceptedFrames`, `Vision/Camera1/RejectedFrames`, `Vision/Camera1/LastRejectionReason`
   - `Vision/Camera0/LastInnovationMeters` (how far each accepted frame pulls the estimate)
   - `Vision/Summary/AcceptedPoses`, `Vision/Summary/RejectedPoses`, `Vision/Summary/TagPoses`
   - `Drive/Pose` (the fused robot pose)
5. Rotate the robot slowly and verify each camera sees both tags over a useful range.

## Precision Test

1. Place the robot at the marked start.
2. Press `A`.
3. Press `X` to run precision drive to `(4.25 m, 2.0 m, 0 deg)`.
4. Measure final robot position with a tape measure.
5. Save the AdvantageKit log.
6. Repeat with vision enabled and with tags blocked.
7. Compare final error, rejected frame reasons, and tag distances.

## Coarse Path Plus Precision

PathPlanner should be used for coarse motion. The final 0.5-1.0 m should be handled by the precision command.

1. Open PathPlanner for this project.
2. Create an auto named `VisionTest`.
3. Start at `(1.5 m, 2.0 m, 0 deg)`.
4. End the coarse path around `(3.6 m, 2.0 m, 0 deg)`.
5. Keep constraints conservative: 1.6 m/s, 1.2 m/s^2, 120 deg/s, 180 deg/s^2.
6. Run the auto and then run `Precision Drive To Tag Board`.
7. If the coarse path drifts, fix drivetrain characterization before tuning vision.

## Chassis Aiming Test (no turret/mechanism)

Aims the whole robot at `AimConstants.GOAL_POSITION` (a configurable virtual goal).

1. Set `AimConstants.GOAL_POSITION` to the target you want to face.
2. Press `A` to reset pose.
3. Stationary: press the right stick (`Aim At Goal - Stationary`). Watch `Aim/HeadingErrorDegrees`
   settle below the tolerance and `Aim/Aimed` go true.
4. Moving: hold the right trigger and translate with the left stick. Confirm the robot keeps facing the
   goal and `Aim/LeadPose` shifts ahead of the robot when moving (shoot-on-move lead).
5. Save the log. When a boresight/turret camera + real target exist later, also log
   `poseBearing - cameraBearing` as the independent aim check.

## Characterization

Run SysId in this order:

1. Translation quasistatic forward/reverse.
2. Translation dynamic forward/reverse.
3. Steer tests only when modules are safely supported.
4. Rotation tests in a clear area.

Controls are documented in `ROBOT_CONTROLS.md`.

After characterization:

- Update wheel radius if distance is biased.
- Update drive feedforward/gains.
- Rerun the precision test and compare logs.
