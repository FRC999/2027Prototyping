# BLine desktop screening on the original laptop

October12,2026. Pulled main189d02a, preserving the laptop's unrelated files, and
executed the handoff's local verification and staged desktop checks. Robot source,
vision configuration, gains, route limits and aiming behavior were unchanged.
No hardware deployment or physical test occurred.

## What ran and what passed

- WPILib2026 bundled Java17; compileJava,95/95 JUnit, JaCoCo gate and jar passed.
  All eight gated pure classes report100% line coverage in this run.
- Actual Robot lifecycle, scheduler, CTRE desktop swerve update thread, simulated
  PhotonVision IO/fusion, live NT outputs and the rotating WPILOG writer.
- Three valid repeats of each H1-H4 and BLine1-BLine4 counterpart:24 trials.
- One unchanged H5 return regression:25 qualified stops total, no precision timeout.
- Each driving file includes a full3s enabled neutral post-completion window.
  Wheel-angle hold remained active under DriveManuallyCommand ownership; maximum
  steering-TARGET jump was0degrees. Largest logged post-finish wheel speed was
  0.023m/s, below the report's0.05m/s motion threshold. Gyro validity stayed true
  in the audited route/post-stop intervals.
- Missing-MultiTag and out-of-area starts rejected with Completed=false and zero
  requested wheel speed. Disable during travel and active final precision logged
  interruption, stopped and did not report success.

The desktop runner replaces manual DS/dashboard clicks with DriverStationSim,
an A-button reset and local NetworkTables chooser/rotation actions. It uses the
same configured Robot and CTRE/PV runtime, not a replacement ideal plant. It runs
without the GUI; visual GUI inspection is still a separate pending check.

## Timing and estimated endpoint results

Each cell shows median command duration and min-max, seconds; three trials per
one-way family. Durations come from WPILOG phase boundaries, excluding setup and
the post-stop recording window. The runner's printed wall-clock polling time is
slightly longer and is not used in this table.

| Route | PathPlanner + precision | BLine + precision |
|---|---:|---:|
| H1 / forward1.50m | 3.700 (2.840-4.554) | 3.660 (3.580-4.421) |
| H2 / forward then left | 3.944 (3.779-4.480) | 5.501 (5.000-6.099) |
| H3 / forward then diagonal | 3.700 (3.420-3.937) | 6.339 (6.160-6.619) |
| H4 / diagonal with yaw | 3.620 (3.539-3.900) | 6.087 (5.940-6.106) |

H5's single run took9.660s, including its intentional turnaround and return.
Its final precision phase took3.821s and estimated translation error was1.61cm,
inside its2cm return tolerance. One-way endpoint error was within4cm; maximum
absolute estimated yaw error across all25 runs was0.645degrees.

**This does not demonstrate that BLine is faster.** The corner/final-segment caps,
polyline versus spline geometry and wheel-force feedforward differ. Stochastic
vision and CPU timing also differ between trials. H2-H4 are slower under the
current conservative BLine setup. Shared qualified endpoint control works in both.

The desired final-settling share under10% was NOT met universally:9 of25 trials
exceeded it under the saved-log motion proxy. Settling is first final translation-
window entry during precision through the last wheel sample above0.05m/s; the
window is the logged route tolerance (4cm one-way,2cm H5), without waiting for yaw
to be correct before starting the settling clock. All commanded corrections and
post-finish wheel motion in that interval count; H5's middle reversal does not.
Steering motion and below-threshold motion are not represented by that one number.
Do not label these trials universally performance-qualified or absolutely at rest.

## Failure evidence

| File suffix | Check | Result |
|---|---|---|
| 14f1 | No fresh MultiTag at initial SIM pose | NO_FRESH_MULTITAG_START, rejected, requested wheel speed0 |
| 3dd9 | Fresh vision but simulated position outside safe start area | GENERATED_TARGET_OUTSIDE_TEST_AREA, rejected, requested wheel speed0 |
| c6ae | Disable during BLINE_TRAVEL | INTERRUPTED, no precision initialization or success, stopped |
| 792f | Disable at FINAL_PRECISION phase transition | Interrupted before precision actually initialized; not counted as active-controller coverage |
| 3db8 | Repeat after Controller/Active=true and five correction samples | Active final controller interrupted, Completed=false, stopped |

In3db8, final phase began6.125486s, controller became active6.144800s and disabled
interruption occurred6.264727s. This distinction prevents a phase label alone from
being mistaken for execution of that controller. The runner now waits for actual
initialization and correction before testing that interruption.

Blocked travel, delayed progress,15cm lateral displacement, invalid-state and
unsafe-native-completion checks passed in the existing automated test suite. Its
follower disturbance tests use an ideal kinematic plant, not this full CTRE/PV
runtime. Full-physics disturbance injection/recovery qualification remains pending.
No new GUI physical-disturbance control was installed.

## Simulation limitations found

The current PV simulator receives drive::getPose, the estimator output; it does
not receive a separately logged independent physical ground-truth pose. Synthetic
pixel/calibration noise can feed back into subsequent frames and accepted starts.
Thus these runs test the configured software interactions, not independent
localization accuracy, real camera calibration, board flatness or measured travel.
Do not tune physical camera mounts or motor gains from these simulated endpoints.

There were startup/intermittent CTRE stale-frame console warnings and robot-loop
overruns, especially simulated camera work (some console epochs around70-150ms).
Runtime route gyro-valid flags were true, but this is not a uniform20ms timing
benchmark. The first runner deliberately ended competition and printed a generic
WPILib unexpected-return warning; later runners suppress that expected shutdown
message. No uncaught runtime fault or false success was observed in the audited
driving cases. Do not infer hardware CAN wiring faults from desktop startup text.

No GUI observation or actual physical measurements exist for these runs. Physical
reference/camera recalibration still requires the previously documented checks;
the flexible-board limitation is not fixed by a new route follower.

## Reproduce or inspect

The [portable JSON evidence](evidence/BLINE_DESKTOP_RESULTS_2026-10-12.json) retains
all30 route/failure records, exact source revision, filenames, SHA256 fingerprints,
phase events, endpoint/hold/timing statistics and caveats. Actual files are in
`VisionTestingAndCalibration/logs/sim/` on this PC, outside Git. All recorded
trial files were closed with the normal disabled-only rotation command. Copy raw
files separately for replay; the evidence report itself transfers with Git.

Reusable tools:

- [BLineDesktopValidation.java](tools/BLineDesktopValidation.java): runs the Robot
  lifecycle in a desktop-only JVM with simulated DS controls; refuses real HAL.
- [audit_bline_desktop.py](tools/audit_bline_desktop.py): reads finalized WPILOGs,
  ignores inherited initial phase snapshots, prints traceable JSON, and uses the
  logged route tolerance. It does not connect to or modify a robot.

From the robot project, with JAVA_HOME pointing to WPILib2026 JDK:

```powershell
.\gradlew.bat compileJava test jacocoTestCoverageVerification jar
& "$env:JAVA_HOME\bin\javac.exe" -cp 'build\libs\VisionTestingAndCalibration.jar' -d 'build\desktop-validation' '..\MechaRAMS vision system\tools\BLineDesktopValidation.java'
$taskNativePath = Join-Path (Get-Location) 'build\jni\release'
$env:PATH = "$taskNativePath;$env:PATH"
$env:HALSIM_EXTENSIONS = ''
& "$env:JAVA_HOME\bin\java.exe" "-Djava.library.path=$taskNativePath" -cp 'build\desktop-validation;build\libs\VisionTestingAndCalibration.jar' BLineDesktopValidation 1
```

Argument1 runs the three paired H1/BLine1 repeats. After they pass, `4 1` runs
families2-4 plus H5 (first-family index is zero-based). `4` runs the whole matrix
from family1. `checks` runs preflight rejection and both interruption cases;
`checks-precision` repeats active-precision interruption only. The runner stops
progression at a failed normal route. Close other simulator runtimes using the
same local NT ports before launching it. No external Driver Station is used.

Analyze this dataset without writing raw logs:

```powershell
python -B '..\MechaRAMS vision system\tools\audit_bline_desktop.py' logs\sim --pattern 'akit_rotated_179184*.wpilog' --source-revision 189d02a
```

Change the pattern/source revision for future experiments. JSON contains physical
measurements=null because estimated accuracy does not provide physical truth.

## Next team work

New layout: [BLine Comparison.json](advantage-scope-layouts/BLine%20Comparison.json)
in the repository's research/advantage-scope-layouts folder. It retains Auto Finish
graphs and adds live/saved BLine tables;27 new keys verified in actual saved logs.
Use this copy on either laptop. Its JSON schema is checked; GUI import remains
part of the team's visual review.

Review a representative BLine1 and H1 log in AdvantageScope using the validation
plan's channels. Then the team can manually deploy the chosen version and begin
the bounded H1/BLine1 physical screening: three repeats each, actual squared start
marks/fresh preflight, both cameras open, identical camera state, measured rear
corners,3s enabled post-stop evidence and one closed log per run. Confirm the
camera calibration state before making physical-accuracy claims. GUI inspection
and physical deployment/driving are not claimed completed by this report.

Keep BLine optional, preserve H5 regression and existing gains. A matched-constraint
performance experiment and full-physics recovery tests should precede adoption
claims; the current validity screening does not justify silently replacing the
proven PathPlanner family or retuning the shared final controller.
