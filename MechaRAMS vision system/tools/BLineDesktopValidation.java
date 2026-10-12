import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import edu.wpi.first.hal.AllianceStationID;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructSubscriber;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import frc.robot.Robot;

/** Desktop-only driver for the real Robot lifecycle, CTRE sim and PV sim.
 * Run as a separate JVM with the built robot jar and desktop native libraries.
 * It refuses a real HAL; sends no packets to a hardware Driver Station/roboRIO.
 * This is NOT an ideal kinematic substitute, GUI inspection or optical truth test.
 */
public final class BLineDesktopValidation {
  private static final NetworkTableInstance nt = NetworkTableInstance.getDefault();
  private static final String OUT = "/AdvantageKit/RealOutputs/";
  private static final AtomicReference<Robot> robot = new AtomicReference<>();
  private static StructSubscriber<Pose2d> pose;

  private static boolean bool(String key) { return nt.getEntry(OUT + key).getBoolean(false); }
  private static String str(String key) { return nt.getEntry(OUT + key).getString("MISSING"); }
  private static double number(String key) { return nt.getEntry(OUT + key).getDouble(Double.NaN); }
  private static void tick() throws InterruptedException {
    DriverStationSim.notifyNewData();
    Thread.sleep(20);
  }
  private static void waitFor(BooleanSupplier condition, double timeout, String description)
      throws InterruptedException {
    long deadline = System.nanoTime() + (long)(timeout * 1e9);
    while (!condition.getAsBoolean()) {
      if (System.nanoTime() > deadline) throw new IllegalStateException("TIMEOUT: " + description);
      tick();
    }
  }
  private static void pause(double seconds) throws InterruptedException {
    long deadline = System.nanoTime() + (long)(seconds * 1e9);
    while (System.nanoTime() < deadline) tick();
  }
  private static void mode(boolean enabled, boolean autonomous) {
    DriverStationSim.setAutonomous(autonomous);
    DriverStationSim.setTest(false);
    DriverStationSim.setEnabled(enabled);
  }
  private static void rotateLog() throws InterruptedException {
    nt.getEntry("/SmartDashboard/Close Current Log And Start New (Disabled Only)/running")
        .setBoolean(true);
    pause(.4);
    waitFor(() -> !bool("Logging/RotationPending"), 5, "log rotation");
    System.out.println("DESKTOP_LOG " + str("Logging/ActiveLogPath"));
  }
  private static void startPose() throws InterruptedException {
    mode(true, false);
    pause(.2);
    DriverStationSim.setJoystickButton(0, 1, true); // A, SIM fixed start only.
    pause(.15);
    DriverStationSim.setJoystickButton(0, 1, false);
    pause(.15);
    mode(false, false);
    pause(.4);
    waitFor(() -> bool("BLine/Test/Preflight/ReadyToEnable")
        && bool("PathPlanner/HolonomicTest/Preflight/ReadyToEnable"), 15, "fresh safe start");
    Pose2d p = pose.get();
    if (p.getTranslation().getDistance(new Pose2d(1.5, 2, new edu.wpi.first.math.geometry.Rotation2d())
        .getTranslation()) > .15) throw new IllegalStateException("SIM reset did not take effect: " + p);
  }
  private static void run(String selection, int repetition) throws InterruptedException {
    startPose();
    rotateLog();
    boolean bline = selection.startsWith("BLine");
    String prefix = bline ? "BLine/Test/" : "PathPlanner/HolonomicTest/";
    nt.getEntry("/SmartDashboard/Autonomous Mode/selected").setString(selection);
    pause(.2);
    long begin = System.nanoTime();
    System.out.println("DESKTOP_BEGIN " + repetition + " " + selection);
    mode(true, true);
    pause(.3);
    waitFor(() -> {
      String phase = str(prefix + "CurrentPhase");
      return phase.equals("ENDED") || phase.equals("COMPLETE") || phase.equals("ABORTED")
          || phase.equals("INTERRUPTED");
    }, 18, selection + " finish");
    double duration = (System.nanoTime() - begin) / 1e9;
    boolean passed = bool(prefix + "Completed") && !bool("DriveToPose/TimedOut")
        && bool("DriveToPose/FinishQualified")
        && (!bline || str(prefix + "CompletionReason").equals("SUCCESS"));
    System.out.printf("DESKTOP_RESULT selection=%s repeat=%d passed=%s observedSeconds=%.3f reason=%s"
        + " pose=%s translationError=%.5f rotationError=%.3f%n", selection, repetition,
        passed, duration, bline ? str(prefix + "CompletionReason") : str(prefix + "CurrentPhase"),
        pose.get(), number("DriveToPose/TranslationErrorMeters"), number("DriveToPose/RotationErrorDegrees"));
    pause(3.2); // Enabled neutral post-completion ownership evidence.
    System.out.println("DESKTOP_POST owner=" + str("Drive/CommandOwner") + " hold="
        + bool("Drive/PrecisionAngleHoldActive") + " maxWheel="
        + number("Drive/MaxAbsModuleSpeedMetersPerSecond"));
    mode(false, false);
    pause(.2);
    rotateLog();
    if (!passed) throw new IllegalStateException("Screening failed; do not progress: " + selection);
  }
  private static void rejectedStart(String expected) throws InterruptedException {
    rotateLog();
    nt.getEntry("/SmartDashboard/Autonomous Mode/selected")
        .setString("BLine 1 - Forward Entry + Precision");
    pause(.2);
    mode(true, true);
    pause(.2);
    waitFor(() -> str("BLine/Test/CurrentPhase").equals("ABORTED"), 3, "start rejection");
    if (!str("BLine/Test/AbortReason").equals(expected)
        || bool("BLine/Test/Completed") || bool("BLine/Test/StartAccepted"))
      throw new IllegalStateException("Wrong rejected-start outcome: " + str("BLine/Test/AbortReason"));
    System.out.println("DESKTOP_FAILURE_CHECK expected=" + expected + " passed=true");
    pause(3.2);
    mode(false, false);
    pause(.2);
    rotateLog();
  }
  private static void interrupted(String phase) throws InterruptedException {
    startPose();
    rotateLog();
    nt.getEntry("/SmartDashboard/Autonomous Mode/selected")
        .setString("BLine 1 - Forward Entry + Precision");
    pause(.2);
    mode(true, true);
    waitFor(() -> str("BLine/Test/CurrentPhase").equals(phase), 12, "interruption phase");
    if (phase.equals("BLINE_TRAVEL")) pause(.3);
    else {
      waitFor(() -> bool("DriveToPose/Controller/Active"), 3, "precision actually initialized");
      pause(.1); // Exercise active correction, not only its preceding phase marker.
    }
    mode(false, false);
    pause(.2);
    waitFor(() -> str("BLine/Test/CompletionReason").equals("INTERRUPTED"), 3, "interruption");
    if (bool("BLine/Test/Completed")) throw new IllegalStateException("Interruption reported success");
    pause(3.2);
    System.out.println("DESKTOP_FAILURE_CHECK interrupted=" + phase + " passed=true maxWheel="
        + number("Drive/MaxAbsModuleSpeedMetersPerSecond"));
    rotateLog();
  }
  private static void failureChecks() throws InterruptedException {
    if (bool("BLine/Test/Preflight/FreshMultiTagAvailable"))
      throw new IllegalStateException("Initial missing-vision setup unexpectedly has fresh MultiTag");
    rejectedStart("NO_FRESH_MULTITAG_START");
    startPose();
    mode(true, false);
    DriverStationSim.setJoystickAxis(0, 1, -.2);
    waitFor(() -> pose.get().getX() > 3.6, 12, "manual simulated out-of-area position");
    DriverStationSim.setJoystickAxis(0, 1, 0);
    pause(.6);
    mode(false, false);
    pause(.6);
    waitFor(() -> bool("BLine/Test/Preflight/FreshMultiTagAvailable"), 5, "out-of-area fresh vision");
    rejectedStart("GENERATED_TARGET_OUTSIDE_TEST_AREA");
    interrupted("BLINE_TRAVEL");
    interrupted("FINAL_PRECISION");
    System.out.println("DESKTOP_FAILURE_CHECKS_COMPLETE");
  }
  public static void main(String[] args) throws Exception {
    if (!RobotBase.isSimulation()) throw new IllegalStateException("Desktop HAL required");
    RobotBase.suppressExitWarning(true);
    Thread runtime = new Thread(() -> RobotBase.startRobot(() -> {
      Robot r = new Robot(); robot.set(r); return r;
    }), "real-robot-desktop-runtime");
    runtime.setDaemon(true);
    runtime.start();
    int exitCode = 0;
    try {
      pose = nt.getStructTopic(OUT + "Drive/Pose", Pose2d.struct).subscribe(new Pose2d());
      long deadline = System.nanoTime() + 20_000_000_000L;
      while (robot.get() == null) {
        if (System.nanoTime() > deadline) throw new IllegalStateException("Robot startup failed");
        Thread.sleep(20);
      }
      DriverStationSim.setDsAttached(true);
      DriverStationSim.setAllianceStationId(AllianceStationID.Blue1);
      DriverStationSim.setJoystickIsXbox(0, true);
      DriverStationSim.setJoystickAxisCount(0, 6);
      DriverStationSim.setJoystickButtonCount(0, 10);
      DriverStationSim.setJoystickPOVCount(0, 1);
      DriverStationSim.setJoystickPOV(0, 0, -1);
      mode(false, false);
      pause(1);
      String[] names = {
        "Holonomic 1 - Forward Entry", "BLine 1 - Forward Entry + Precision",
        "Holonomic 2 - Forward Then Strafe Left", "BLine 2 - Forward Then Strafe Left + Precision",
        "Holonomic 3 - Forward Then Diagonal Left", "BLine 3 - Forward Then Diagonal Left + Precision",
        "Holonomic 4 - Diagonal With Camera-Facing Yaw", "BLine 4 - Diagonal With Camera-Facing Yaw + Precision"
      };
      boolean checks = args.length > 0 && args[0].startsWith("checks");
      int families = checks ? 1 : args.length == 0 ? 1 : Integer.parseInt(args[0]);
      int firstFamily = args.length < 2 ? 0 : Integer.parseInt(args[1]);
      if (families < 1 || families > 4) throw new IllegalArgumentException("families=1..4");
      if (firstFamily < 0 || firstFamily >= families)
        throw new IllegalArgumentException("firstFamily=0..families-1; earlier families must already pass");
      if (checks) {
        if (args[0].equals("checks-precision")) interrupted("FINAL_PRECISION");
        else failureChecks();
      }
      for (int family = firstFamily; !checks && family < families; family++)
        for (int trial = 1; trial <= 3; trial++) {
          run(names[2 * family], trial);
          run(names[2 * family + 1], trial);
        }
      if (families == 4) run("Holonomic 5 - Out And Return To Start", 1);
      System.out.println(checks ? "DESKTOP_RUNTIME_COMPLETE mode=failure-checks"
          : "DESKTOP_SCREENING_COMPLETE families=" + families);
    } catch (Exception ex) {
      exitCode = 1;
      ex.printStackTrace();
    } finally {
      mode(false, false);
      pause(.3);
      if (robot.get() != null) robot.get().endCompetition();
      runtime.join(2000);
    }
    System.exit(exitCode);
  }
}
