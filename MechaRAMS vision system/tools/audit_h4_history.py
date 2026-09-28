r"""Read archived WPILOGs without loading robot code; compare H4 timing and post-stop motion.

Usage: python audit_h4_history.py C:\MechaRAMS\temp --output audit.json
The output is telemetry evidence, not independent physical ground truth or a simulation.
Changed-only records are sampled with zero-order hold; no extrapolation beyond log end.
"""

import argparse
import bisect
import json
import math
from pathlib import Path
import statistics
import struct


class Log:
    def __init__(self, path):
        raw = path.read_bytes()
        if raw[:6] != b"WPILOG":
            raise ValueError(f"Not a WPILOG: {path}")
        pos = 12 + int.from_bytes(raw[8:12], "little")
        entries, self.data = {}, {}
        while pos < len(raw):
            header = raw[pos]
            pos += 1
            values = []
            for width in [(header & 3) + 1, ((header >> 2) & 3) + 1, ((header >> 4) & 7) + 1]:
                values.append(int.from_bytes(raw[pos:pos + width], "little"))
                pos += width
            entry, length, micros = values
            payload = raw[pos:pos + length]
            pos += length
            if len(payload) != length:
                raise ValueError(f"Truncated record: {path}")
            if entry == 0:
                if payload and payload[0] == 0:
                    entry_id = int.from_bytes(payload[1:5], "little")
                    offset, strings = 5, []
                    for _ in range(2):
                        size = int.from_bytes(payload[offset:offset + 4], "little")
                        offset += 4
                        strings.append(payload[offset:offset + size].decode())
                        offset += size
                    entries[entry_id] = strings
                continue
            if entry not in entries:
                continue
            name, kind = entries[entry]
            name = name.removeprefix("/RealOutputs/")
            if kind == "double":
                value = struct.unpack("<d", payload)[0]
            elif kind == "boolean":
                value = bool(payload[0])
            elif kind == "int64":
                value = int.from_bytes(payload, "little", signed=True)
            elif kind == "string":
                value = payload.decode(errors="replace")
            elif kind in ("struct:Pose2d", "struct:ChassisSpeeds", "struct:SwerveModuleState[]"):
                value = struct.unpack("<" + "d" * (length // 8), payload)
            else:
                continue
            self.data.setdefault(name, []).append((micros / 1e6, value))
        self.times = {key: [t for t, _ in samples] for key, samples in self.data.items()}
        self.end = max(times[-1] for times in self.times.values())

    def at(self, key, time, default=None):
        index = bisect.bisect_right(self.times.get(key, []), time) - 1
        return self.data[key][index][1] if index >= 0 else default

    def between(self, key, start, end):
        return [(t, v) for t, v in self.data.get(key, []) if start <= t <= end]


def angle_difference(a, b):
    return math.degrees(math.atan2(math.sin(a - b), math.cos(a - b)))


def audit(path, mode="DIAGONAL_WITH_YAW"):
    log = Log(path)
    phases = log.data.get("PathPlanner/HolonomicTest/CurrentPhase", [])
    rows = []
    for start, phase in phases:
        if phase != "RESET_TO_TRUSTED_START":
            continue
        if mode is not None and log.at("PathPlanner/HolonomicTest/Mode", start) != mode:
            continue
        finish = next((t for t, v in phases if t > start and v in ("COMPLETE", "INTERRUPTED")), None)
        if finish is None:
            continue
        starts = [t for t, v in log.between("DriveToPose/Controller/Active", start, finish) if v]
        if not starts:
            continue
        active = starts[-1]
        target = log.at("DriveToPose/TargetPose", finish)
        pose = log.at("Drive/Pose", finish)
        handoff_pose = log.at("Drive/Pose", active)
        holds = [(t, v) for t, v in log.between("DriveToPose/SettlingHoldActive", active, finish) if t > active + .001]
        first_hold = next((t for t, v in holds if v), None)
        configured = {k.split("Configured", 1)[1]: log.at(k, finish) for k in log.data if k.startswith("DriveToPose/Controller/Configured")}
        # Terminate the post-finish window at disable, mode switch, log end, or next auto start.
        enabled_end = min(finish + 3., log.end)
        for key in ("/DriverStation/Enabled", "/DriverStation/Autonomous"):
            if not log.at(key, finish, False):
                enabled_end = finish
            enabled_end = min(enabled_end, next((t for t, v in log.between(key, finish + 1e-6, enabled_end) if not v), enabled_end))
        enabled_end = min(enabled_end, next((t for t, v in phases if t > finish and v == "RESET_TO_TRUSTED_START"), enabled_end))
        yaw_after = {}
        for delay in (.3, 1., 2., 3.):
            q = finish + delay
            if q <= enabled_end and log.at("Drive/Pose", q) is not None:
                yaw_after[str(delay)] = math.degrees(log.at("Drive/Pose", q)[2])
        post_poses = log.between("Drive/Pose", finish, enabled_end)
        gyro = log.between("Drive/GyroYawRateDegreesPerSecond", active, enabled_end)
        wheel = log.between("Drive/MaxAbsModuleSpeedMetersPerSecond", active, enabled_end)
        battery = [v for _, v in log.between("/SystemStats/BatteryVoltage", start, finish)]
        loops = [v for _, v in log.between("DriveToPose/Controller/LoopDtMilliseconds", active, finish)]
        post_moving = [t for t, v in wheel if t >= finish and abs(v) > .05]
        target_at_finish = log.at("Drive/ModuleTargets", finish)
        post_targets = log.between("Drive/ModuleTargets", finish + 1e-6, enabled_end)
        target_changes = [(t, max(abs(angle_difference(v[j], target_at_finish[j]))
                                 for j in (1, 3, 5, 7))) for t, v in post_targets] if target_at_finish else []
        target_jump = next(((t, delta) for t, delta in target_changes if delta > 1.), None)
        post_pose_samples = {}
        for delay in (0., .25, .5, 1., 2., 3.):
            q = finish + delay
            if q <= enabled_end:
                p = log.at("Drive/Pose", q)
                post_pose_samples[str(delay)] = {
                    "pose": p,
                    "yaw_degrees": math.degrees(p[2]),
                    "gyro_deg_s": log.at("Drive/GyroYawRateDegreesPerSecond", q),
                    "max_wheel_m_s": log.at("Drive/MaxAbsModuleSpeedMetersPerSecond", q),
                    "owner": log.at("Drive/CommandOwner", q),
                    "angle_hold": log.at("Drive/PrecisionAngleHoldActive", q),
                    "manual_allowed": log.at("Drive/ManualDriveAllowed", q),
                }
        row = dict(
            file=path.name, suffix=path.stem[-4:], start=start, finish=finish,
            mode=log.at("PathPlanner/HolonomicTest/Mode", start),
            end_phase=log.at("PathPlanner/HolonomicTest/CurrentPhase", finish),
            start_pose=log.at("PathPlanner/HolonomicTest/NormalizedStartPose", active),
            phase_events=[(t-start, v) for t,v in phases if start<=t<=finish],
            total=finish-start, precision=finish-active, final_controller_start=active,
            first_hold=(first_hold-active if first_hold else None),
            hold_tail=(finish-first_hold if first_hold else None),
            hold_events=[(t-active, v) for t, v in holds],
            hold_exits=log.at("DriveToPose/SettlingHoldExitCount", finish),
            strict=log.at("DriveToPose/Controller/StrictFinishMotionRequired", finish),
            continuous_finish="DriveToPose/FinishQualificationSeconds" in log.data,
            measured_angle_hold=log.at("DriveToPose/Controller/DriveRequestType", finish)=="VelocityAngleHold",
            timed_out=log.at("DriveToPose/TimedOut", finish),
            finish_qualified=log.at("DriveToPose/FinishQualified", finish),
            target=target, finish_pose=pose,
            finish_yaw=math.degrees(pose[2]),
            translation_error=math.hypot(pose[0]-target[0], pose[1]-target[1]),
            handoff_yaw_error=angle_difference(target[2], handoff_pose[2]),
            handoff_gyro=log.at("Drive/GyroYawRateDegreesPerSecond", active),
            finish_gyro=log.at("Drive/GyroYawRateDegreesPerSecond", finish),
            finish_max_wheel=log.at("Drive/MaxAbsModuleSpeedMetersPerSecond", finish),
            enabled_post_seconds=enabled_end-finish, yaw_after=yaw_after,
            max_post_yaw_change=max((abs(angle_difference(v[2], pose[2])) for _,v in post_poses),default=None),
            last_post_wheel_motion=(max(post_moving)-finish if post_moving else None),
            target_jump_after_finish_s=(target_jump[0]-finish if target_jump else None),
            target_jump_degrees=(target_jump[1] if target_jump else None),
            post_max_wheel=max((abs(v) for t,v in wheel if t>=finish), default=None),
            post_max_abs_gyro=max((abs(v) for t,v in gyro if t>=finish), default=None),
            post_max_target_angle_change=max((v for _,v in target_changes),default=0. if target_at_finish else None),
            post_samples=post_pose_samples,
            ownership_telemetry_present="Drive/CommandOwner" in log.data,
            post_ownership_events={k: log.between(k,finish,enabled_end) for k in (
                "Drive/CommandOwner","Drive/PrecisionAngleHoldActive","Drive/ManualDriveAllowed")},
            battery_min=min(battery) if battery else None,
            loop_max=max(loops) if loops else None,
            loop_median=statistics.median(loops) if loops else None,
            config=configured,
            holonomic_config={k.removeprefix("PathPlanner/HolonomicTest/"): log.at(k,finish) for k in log.data if k.startswith("PathPlanner/HolonomicTest/") and ("Max" in k or "Constraint" in k or "Distance" in k)},
        )
        rows.append(row)
    return rows


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("folder", type=Path)
    parser.add_argument("--output", type=Path)
    parser.add_argument("--suffix", nargs="+", help="Only logs ending in these suffixes")
    parser.add_argument("--all-modes", action="store_true", help="Include H3/H5 and other holonomic modes")
    args = parser.parse_args()
    rows = []
    for path in sorted(args.folder.glob("*.wpilog")):
        if args.suffix and not any(path.stem.endswith(s) for s in args.suffix):
            continue
        rows.extend(audit(path, mode=None if args.all_modes else "DIAGONAL_WITH_YAW"))
    for row in rows:
        print(row["suffix"], "total", round(row["total"],3), "precision", round(row["precision"],3),
              "tail", round(row["hold_tail"],3) if row["hold_tail"] is not None else None,
              "strict", row["strict"], "exits",row["hold_exits"],
              "yaw", round(row["finish_yaw"],2),
              "yaw+1", round(row["yaw_after"].get("1.0",float("nan")),2),
              "angle_hold",row["measured_angle_hold"])
    if args.output:
        args.output.write_text(json.dumps(rows, indent=2, allow_nan=False), encoding="utf-8")
