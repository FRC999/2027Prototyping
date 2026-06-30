# Vision Review

Review PhotonVision/localization code for Team 999.

Check:

- Every unread frame from every camera is consumed.
- Observations are sorted by timestamp before fusion.
- Single-tag observations do not contribute trusted heading.
- Multi-tag observations get tighter heading covariance.
- Pose rejection uses physical impossibility, not disagreement with stale odometry.
- Rejection reasons and accepted counts are logged.
- Robot-to-camera transforms are marked provisional unless measured.
- Camera names match PhotonVision exactly.
- AdvantageKit logs contain enough evidence to replay/tune.

Output findings first, with file and line references.
