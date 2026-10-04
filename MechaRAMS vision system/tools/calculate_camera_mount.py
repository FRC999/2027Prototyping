"""Offline mount arithmetic, not a robot command or a camera-intrinsics calibration.

Read the sibling ../CAMERA_MOUNT_CALIBRATION_MANUAL.md before collecting data.
Input: independently surveyed field-to-robot poses and raw PV field-to-camera poses.
All poses are [x_m, y_m, z_m, roll_deg, pitch_deg, yaw_deg], WPILib NWU convention.
Prints JSON only; never modifies input files, robot constants, or PhotonVision.
"""

import argparse
import json
import math
from pathlib import Path
import statistics


def multiply(a, b):
    w, x, y, z = a
    v, i, j, k = b
    return (w*v-x*i-y*j-z*k, w*i+x*v+y*k-z*j,
            w*j-x*k+y*v+z*i, w*k+x*j-y*i+z*v)


def conjugate(q):
    return (q[0], -q[1], -q[2], -q[3])


def rotate(q, v):
    return multiply(multiply(q, (0.0, *v)), conjugate(q))[1:]


def from_rpy(values):
    r, p, y = [math.radians(v)/2 for v in values]
    cr, sr, cp, sp, cy, sy = (math.cos(r), math.sin(r), math.cos(p),
                             math.sin(p), math.cos(y), math.sin(y))
    return (cr*cp*cy+sr*sp*sy, sr*cp*cy-cr*sp*sy,
            cr*sp*cy+sr*cp*sy, cr*cp*sy-sr*sp*cy)


def to_rpy(q):
    w, x, y, z = q
    pitch_sin = max(-1.0, min(1.0, 2*(w*y-z*x)))
    if abs(pitch_sin) > 0.999999:
        raise ValueError("Pitch near +/-90 degrees: Euler angles are ambiguous; use quaternion tooling.")
    return [math.degrees(math.atan2(2*(w*x+y*z), 1-2*(x*x+y*y))),
            math.degrees(math.asin(pitch_sin)),
            math.degrees(math.atan2(2*(w*z+x*y), 1-2*(y*y+z*z)))]


def vector(value, size, label):
    if (not isinstance(value, list) or len(value) != size or
            any(isinstance(x, bool) or not isinstance(x, (int, float)) or
                not math.isfinite(x) for x in value)):
        raise ValueError(f"{label} must contain {size} finite numbers; fill every null in the template.")
    return tuple(value)


def pose(value, label):
    v = vector(value, 6, label)
    return v[:3], from_rpy(v[3:])


def compose(a, b):
    shift = rotate(a[1], b[0])
    return tuple(x+y for x, y in zip(a[0], shift)), multiply(a[1], b[1])


def inverse(a):
    q = conjugate(a[1])
    return rotate(q, tuple(-v for v in a[0])), q


def mean_pose(poses):
    # Hemisphere-aligned normalized quaternion mean; intended for a compact cluster
    # of repeated estimates of ONE rigid mount, not arbitrarily different rotations.
    reference = poses[0][1]
    quaternions = [tuple((-1 if sum(a*b for a, b in zip(q, reference)) < 0 else 1)*x
                         for x in q) for _, q in poses]
    sums = [sum(q[i] for q in quaternions) for i in range(4)]
    norm = math.sqrt(sum(x*x for x in sums))
    if norm < 1e-9:
        raise ValueError("Mount rotations are inconsistent; cannot average them.")
    return tuple(statistics.mean(p[0][i] for p in poses) for i in range(3)), tuple(x/norm for x in sums)


def angle_between(a, b):
    # q and -q represent the same rotation.
    dot = min(1.0, abs(sum(x*y for x, y in zip(a, b))))
    return math.degrees(2*math.acos(dot))


def unpack(p):
    return [*p[0], *to_rpy(p[1])]


def rms(values):
    return math.sqrt(statistics.mean(v*v for v in values))


def robot_errors(camera_samples, reference, mount):
    estimates = [compose(sample, inverse(mount)) for sample in camera_samples]
    delta = [tuple(x-y for x, y in zip(p[0], reference[0])) for p in estimates]
    return {
        "mean_field_xyz_error_m": [statistics.mean(d[i] for d in delta) for i in range(3)],
        "xy_error_rms_m": rms([math.hypot(d[0], d[1]) for d in delta]),
        "orientation_error_rms_deg": rms([angle_between(p[1], reference[1]) for p in estimates]),
    }


def analyze(data):
    if data.get("units") != "meters_degrees":
        raise ValueError("units must be meters_degrees.")
    manual = vector(data.get("measured_robot_to_camera_xyz_m"), 3, "measured_robot_to_camera_xyz_m")
    positions = data.get("positions")
    if not isinstance(positions, list) or not positions:
        raise ValueError("positions must be a nonempty list.")
    groups, warnings = [], []
    names = set()
    for entry in positions:
        name, role = entry.get("id"), entry.get("role")
        if not isinstance(name, str) or not name or name in names:
            raise ValueError("Every position needs a unique nonempty id.")
        names.add(name)
        if role not in ("fit", "check"):
            raise ValueError(f"{name}: role must be fit or check.")
        robot = pose(entry.get("field_to_robot"), name + ".field_to_robot")
        raw = entry.get("field_to_camera_samples")
        if not isinstance(raw, list) or not raw:
            raise ValueError(f"{name}: capture at least one field_to_camera sample.")
        cameras = [pose(v, name + ".camera sample") for v in raw]
        mounts = [compose(inverse(robot), camera) for camera in cameras]
        groups.append((name, role, robot, cameras, mean_pose(mounts)))
        if len(cameras) < 5:
            warnings.append(f"{name}: fewer than five samples; treat this as a preliminary check.")
    fit = [g[4] for g in groups if g[1] == "fit"]
    if not fit:
        raise ValueError("At least one fit position is required.")
    if len(fit) < 3:
        warnings.append("Fewer than three fit poses: not a complete multi-pose calibration.")
    if not any(g[1] == "check" for g in groups):
        warnings.append("No independent check pose. Do not declare this mount validated.")
    # Equal weight per surveyed pose: a long capture must not drown out other views.
    inferred = mean_pose(fit)
    candidate = (manual, inferred[1])
    angles = to_rpy(candidate[1])
    zero_roll = (manual, from_rpy([0.0, angles[1], angles[2]]))
    if max(angle_between(p[1], inferred[1]) for p in fit) > 5:
        warnings.append("Fit-pose mount rotations differ by over 5 degrees: inspect data before using the average.")
    return {
        "camera_name": data.get("camera_name"),
        "units_and_order": "meters/degrees: X,Y,Z,roll,pitch,yaw",
        "status": "CANDIDATE ONLY; no robot/PV changes; no automatic pass/fail",
        "mount_from_vision_equal_pose_weight": unpack(inferred),
        "candidate_using_measured_xyz": unpack(candidate),
        "vision_minus_measured_xyz_m": [x-y for x, y in zip(inferred[0], manual)],
        "zero_roll_comparison_only": unpack(zero_roll),
        "positions": [{
            "id": name, "role": role, "samples": len(cameras),
            "inferred_mount": unpack(mean),
            "error_using_measured_xyz_and_fitted_angles": robot_errors(cameras, robot, candidate),
            "error_if_roll_is_forced_to_zero": robot_errors(cameras, robot, zero_roll),
        } for name, role, robot, cameras, mean in groups],
        "warnings": warnings + [
            "Scatter does not include survey, intrinsic, or field-layout systematic error.",
            "Zero-roll comparison holds pitch/yaw fixed; it is not a separately optimized constrained fit.",
            "Do not overwrite measured XYZ to hide a pose-dependent optical or layout error.",
        ],
    }


def self_test():
    # Synthetic geometry only. Nonzero robot roll/pitch/yaw, a backward-facing mount,
    # wraparound, held-out poses, and preservation of independently measured XYZ.
    for angles in ([2.3, -18, -15], [-1.2, -20, 179.8], [0, 0, 0]):
        mount = ((0.15, -0.26, 0.43), from_rpy(angles))
        groups = []
        for i, rpy in enumerate(([0, 0, 0], [0, 0, 90], [1, -2, -20], [-2, 3, 175])):
            robot = ((2.0+i*.2, 1.8, 0.02), from_rpy(rpy))
            camera = compose(robot, mount)
            groups.append({"id": str(i), "role": "check" if i == 3 else "fit",
                           "field_to_robot": unpack(robot),
                           "field_to_camera_samples": [unpack(camera)]*5})
        result = analyze({"units": "meters_degrees", "camera_name": "SYNTHETIC",
                          "measured_robot_to_camera_xyz_m": list(mount[0]), "positions": groups})
        recovered = pose(result["candidate_using_measured_xyz"], "result")
        assert math.dist(recovered[0], mount[0]) < 1e-12
        assert angle_between(recovered[1], mount[1]) < 1e-5
        for g in result["positions"]:
            assert g["error_using_measured_xyz_and_fitted_angles"]["xy_error_rms_m"] < 1e-10
    assert math.dist(rotate(from_rpy([0, 0, 90]), [1, 0, 0]), [0, 1, 0]) < 1e-12
    wrap = mean_pose([((0, 0, 0), from_rpy([0, 0, 179])),
                      ((0, 0, 0), from_rpy([0, 0, -179]))])
    assert abs(abs(to_rpy(wrap[1])[2])-180) < 1e-10
    try:
        vector([None]*3, 3, "unfinished template")
    except ValueError:
        pass
    else:
        raise AssertionError("Missing measurements must be rejected.")
    print("Offline synthetic rigid-transform checks passed. This does not validate hardware calibration.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", nargs="?", type=Path)
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    if args.self_test:
        self_test()
    elif args.input is None:
        parser.error("Provide a measured-data JSON file, or --self-test.")
    else:
        try:
            print(json.dumps(analyze(json.loads(args.input.read_text(encoding="utf-8-sig"))), indent=2, allow_nan=False))
        except (OSError, ValueError, TypeError, KeyError) as exc:
            parser.error(str(exc))
