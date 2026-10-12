"""Read full-runtime BLine/H desktop WPILOGs; prints portable JSON evidence.

Usage: python audit_bline_desktop.py LOG_FOLDER --pattern 'akit_rotated_179184*.wpilog'
No robot execution/writes. Timing is from saved logs, not runner polling.
Raw logs stay external; file fingerprints make the report traceable.
"""
import argparse
import hashlib
import json
import math
from pathlib import Path
from audit_h4_history import Log, angle_difference


def audit(path):
    log = Log(path)
    rows = []
    for prefix, begin, end in [
        ("BLine/Test/", "BLINE_TRAVEL", "ENDED"),
        ("PathPlanner/HolonomicTest/", "RESET_TO_TRUSTED_START", "COMPLETE"),
    ]:
        phases = log.data.get(prefix + "CurrentPhase", [])
        # The initial snapshot can carry a previous run's values. Only real
        # transitions later in this file constitute a newly executed trial.
        for start, phase in phases[1:]:
            if phase != begin:
                continue
            finish = next((t for t, v in phases if t > start and v in
                           (end, "INTERRUPTED", "ABORTED")), None)
            if finish is None:
                continue
            precise = next((t for t, v in phases if start < t <= finish
                            and v == "FINAL_PRECISION"), None)
            initialized = precise is not None and any(v for t, v in log.between(
                "DriveToPose/Controller/Active", precise, finish))
            post_end = min(finish + 3, log.end)
            post_end = min(post_end, next((t for t, v in log.between(
                "/DriverStation/Enabled", finish, post_end) if not v), post_end))
            pose = log.at("Drive/Pose", finish)
            target = log.at(prefix + "FinalTargetPose", finish)
            if target is None:
                target = log.at("DriveToPose/TargetPose", finish)
            wheel_times = [t for t, _ in log.between("Drive/Pose", start, post_end)
                           if log.at("Drive/MaxAbsModuleSpeedMetersPerSecond", t, 0) > .05]
            movement = max(wheel_times) - min(wheel_times) if wheel_times else 0
            radius = log.at("DriveToPose/Controller/ConfiguredTranslationToleranceMeters", finish)
            first_tight = next((t for t, v in log.between(
                "DriveToPose/TranslationErrorMeters", precise, finish) if radius is not None and v <= radius), None
            ) if precise is not None else None
            last_motion = max(wheel_times) if wheel_times else None
            settling = max(0, last_motion - first_tight) if first_tight and last_motion else None
            baseline = log.at("Drive/ModuleTargets", finish)
            jumps = [max(abs(angle_difference(v[i], baseline[i])) for i in (1, 3, 5, 7))
                     for t, v in log.between("Drive/ModuleTargets", finish, post_end)
                     if baseline is not None]
            reason = log.at(prefix + "CompletionReason", finish,
                            "PRECISION_TIMEOUT" if log.at("DriveToPose/TimedOut", finish)
                            else log.at(prefix + "CurrentPhase", finish))
            rows.append(dict(
                file=path.name, sha256=hashlib.sha256(path.read_bytes()).hexdigest(),
                bytes=path.stat().st_size, family="BLine" if prefix.startswith("BLine") else "PathPlanner",
                mode=log.at(prefix + "Mode", start), start=start, finish=finish,
                command_seconds=finish-start, precision_seconds=finish-precise if precise else None,
                precision_initialized=initialized,
                phase_events=[(t-start, v) for t, v in phases if start <= t <= finish],
                completed=log.at(prefix + "Completed", finish), reason=reason,
                finish_qualified=log.at("DriveToPose/FinishQualified", finish) if initialized else None,
                precision_timeout=log.at("DriveToPose/TimedOut", finish) if initialized else None,
                handoff_triggered=log.at(prefix + "FinalHandoff/Triggered", finish),
                travel_outcome=log.at("BLine/Travel/Outcome", finish) if prefix.startswith("BLine") else None,
                final_pose=pose, target_pose=target,
                estimated_translation_error_m=math.hypot(pose[0]-target[0], pose[1]-target[1]),
                estimated_yaw_error_deg=angle_difference(pose[2], target[2]),
                translation_tolerance_m=radius,
                movement_seconds_at_005_m_s=movement,
                first_final_translation_entry=first_tight,
                final_settling_seconds_at_005_m_s=settling,
                settling_share=settling/movement if settling is not None and movement else None,
                post_seconds=post_end-finish,
                post_max_wheel_m_s=max((v for _, v in log.between(
                    "Drive/MaxAbsModuleSpeedMetersPerSecond", finish, post_end)),
                    default=log.at("Drive/MaxAbsModuleSpeedMetersPerSecond", finish)),
                post_max_target_angle_jump_deg=max(jumps, default=0),
                post_owner=log.at("Drive/CommandOwner", post_end),
                post_angle_hold=log.at("Drive/PrecisionAngleHoldActive", post_end),
                gyro_valid_throughout=all(log.at("Drive/GyroYawRateSignalOK", t, False)
                    for t, _ in log.between("Drive/Pose", start, post_end)),
                physical_measurements=None,
                caveat="Configured CTRE/PV simulation, not physical or independent optical truth."
            ))
    # Rejection can finish inside one scheduler cycle; an ABORTED value may
    # remain unchanged from a previous log. Identify new enabled-auto episodes,
    # not an inherited phase snapshot, and inspect the recorded reason.
    for start, enabled in log.data.get("/DriverStation/Enabled", [])[1:]:
        if not enabled or not log.at("/DriverStation/Autonomous", start, False):
            continue
        end = next((t for t, v in log.data["/DriverStation/Enabled"]
                    if t > start and not v), None)
        if end is None or log.at("BLine/Test/CurrentPhase", end) != "ABORTED":
            continue
        if log.at("BLine/Test/StartAccepted", end, False):
            continue
        rows.append(dict(file=path.name, sha256=hashlib.sha256(path.read_bytes()).hexdigest(),
            bytes=path.stat().st_size, family="BLine", mode="REJECTED_START",
            start=start, finish=end, reason=log.at("BLine/Test/AbortReason", end),
            completed=log.at("BLine/Test/Completed", end),
            max_requested_wheel_m_s=max([log.at("Drive/MaxAbsModuleTargetSpeedMetersPerSecond", start, 0)]
                + [v for t, v in log.between("Drive/MaxAbsModuleTargetSpeedMetersPerSecond", start, end)]),
            physical_measurements=None, caveat="Desktop start rejection; not a hardware boot test."))
    return rows


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("folder", type=Path)
    parser.add_argument("--pattern", default="*.wpilog")
    parser.add_argument("--source-revision", default="UNRECORDED")
    args = parser.parse_args()
    rows = []
    for file in sorted(args.folder.glob(args.pattern)):
        rows.extend(audit(file))
    print(json.dumps(dict(source_revision=args.source_revision,
                         motion_threshold_m_s=.05, records=rows), indent=2, allow_nan=False))
