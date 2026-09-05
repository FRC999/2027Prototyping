# Autonomous Algorithm Decision Map

This is the GitHub-friendly overview of the robot's current autonomous control flow. For clickable
boxes with explanations, terminology, and related AdvantageKit signals, open
[`algorithm-decision-map.html`](algorithm-decision-map.html) locally in a web browser.
For the complete written explanation, see
[`FUNCTIONAL_ALGORITHM_HANDOFFS.md`](FUNCTIONAL_ALGORITHM_HANDOFFS.md).

```mermaid
flowchart TD
    W["Wheel sensors + Pigeon gyro<br/>track motion continuously"]:::localization
    V["PhotonVision + vision policy<br/>accept or reject camera poses"]:::localization
    F["Fused robot pose<br/>best X, Y, and heading estimate"]:::localization
    W --> F
    V --> F

    P0["Disabled preflight"]:::output
    D1{"Fresh MultiTag pose?<br/>age ≤ 0.25 s"}:::decision
    X1["Do not start<br/>record rejection reason"]:::safety
    D2{"Inside safe start area?<br/>X, Y, and yaw bounds"}:::decision
    X2["Stop without moving"]:::safety
    S["Use measured X/Y start<br/>define test yaw as 0°"]:::localization
    PP["PathPlanner owns drive<br/>fast, smooth coarse route"]:::path
    D3{"Robot X > 3.3 m?"}:::decision
    H["Transfer ownership<br/>carry current pose and speed"]:::precision
    DP["DriveToPose owns drive<br/>approach, correct, and brake"]:::precision
    D4{"All four settle checks?<br/>position + heading + both speeds"}:::decision
    C["Continue active correction"]:::precision
    Z["Command zero motion<br/>begin settling hold"]:::output
    D5{"Outside an escape limit?"}:::decision
    R["Resume active correction"]:::precision
    D6{"Held for 0.05 s?"}:::decision
    OK["Finish successfully<br/>leave zero-motion request"]:::output
    TO["Stop: 4 s timeout"]:::safety

    P0 --> D1
    D1 -- No --> X1
    D1 -- Yes --> D2
    D2 -- No --> X2
    D2 -- Yes --> S
    S --> PP
    F -. "position estimate used throughout" .-> PP
    PP --> D3
    D3 -- No --> PP
    D3 -- Yes --> H
    H --> DP
    F -. "position and speed feedback" .-> DP
    DP --> D4
    DP -- "4 seconds without success" --> TO
    D4 -- No --> C
    C --> DP
    D4 -- Yes --> Z
    Z --> D5
    D5 -- Yes --> R
    R --> DP
    D5 -- No --> D6
    D6 -- No --> Z
    D6 -- Yes --> OK

    classDef localization fill:#eee3fa,stroke:#7b4bb7,color:#251534,stroke-width:2px;
    classDef path fill:#dcf4e9,stroke:#16835e,color:#10382b,stroke-width:2px;
    classDef precision fill:#deedfb,stroke:#176fc1,color:#102f4d,stroke-width:2px;
    classDef decision fill:#fff0cb,stroke:#b66b00,color:#4b3108,stroke-width:2px;
    classDef safety fill:#fce5e8,stroke:#be3f4e,color:#4c1820,stroke-width:2px;
    classDef output fill:#e6ebf2,stroke:#52627a,color:#1e2939,stroke-width:2px;
```

## Color key

- Purple: localization and pose information.
- Green: PathPlanner coarse driving.
- Blue: DriveToPose final positioning.
- Yellow: a decision, tolerance, or handoff boundary.
- Red: a blocked start or safety timeout.
- Gray: a control state or successful finish.

The dotted arrows show localization information being used by a driving algorithm. They are not
drivetrain handoffs. Only one scheduled driving command owns the wheels at a time.

Last checked against the robot code: September 4, 2026.
