# Vision Review

Review PhotonVision/localization code for Team 999 (AdvantageKit IO-layer architecture).

Check:

- IO-layer shape kept: `VisionIO` (`@AutoLog`) + `VisionIOPhotonVision` + `VisionIOPhotonVisionSim` +
  `Vision`; inputs captured with `Logger.processInputs` (replayable).
- Every unread frame from every camera is consumed (`getAllUnreadResults`), fused timestamp-ordered.
- **Timestamps converted with `Utils.fpgaToCurrentTime` before `addVisionMeasurement`** (FPGA -> Phoenix
  time base). This is the #1 thing to verify — getting it wrong silently breaks fusion.
- Single-tag heading std dev = `Double.POSITIVE_INFINITY`; theta trusted only for multi-tag.
- Covariance = `baseline * dist^2 / tagCount * cameraFactor`.
- Rejection uses physical impossibility (NaN/Inf, Z, off-field, too far, ambiguity), logged as a
  `RejectionReason` enum; not disagreement with stale odometry.
- Accepted/rejected counts + poses, tag poses, and innovation distance are logged.
- Robot-to-camera transforms marked provisional unless measured; camera names match PhotonVision exactly.
- Simulation path (`VisionIOPhotonVisionSim`) selected in sim and fed the true pose.
- `VisionPolicyTest` still passes (`./gradlew.bat test`).

Output findings first, with file and line references.
