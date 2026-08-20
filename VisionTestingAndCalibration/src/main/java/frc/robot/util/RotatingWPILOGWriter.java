package frc.robot.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.littletonrobotics.junction.LogDataReceiver;
import org.littletonrobotics.junction.LogTable;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

/**
 * AdvantageKit data receiver that can close the current WPILOG and immediately start a new one.
 *
 * <p>Opening and closing files can block on roboRIO storage, so neither operation is allowed on
 * AdvantageKit's receiver thread. A background thread opens the replacement writer first. The receiver
 * then performs only an in-memory reference swap between complete tables and immediately continues
 * logging to the replacement. A second background thread closes the old file. A destructive purge
 * briefly detaches this one file receiver, closes/deletes on a background thread, and prepares a fresh
 * writer; NT4 and the rest of AdvantageKit continue running during that short file-log gap.
 */
public final class RotatingWPILOGWriter implements LogDataReceiver {
  private record PreparedWriter(WPILOGWriter writer, String path, boolean completesPurge) {}

  private final String logFolder;
  private final AtomicBoolean rotationPending = new AtomicBoolean(false);
  private final AtomicInteger rotationCount = new AtomicInteger(0);
  private final AtomicBoolean purgePending = new AtomicBoolean(false);
  private final AtomicInteger purgeCount = new AtomicInteger(0);
  private final AtomicLong lastRotationTimestampMicros = new AtomicLong(0);
  private final AtomicLong lastPurgeTimestampMicros = new AtomicLong(0);
  private final AtomicBoolean detachForPurgeRequested = new AtomicBoolean(false);
  private final AtomicReference<PreparedWriter> readyWriter = new AtomicReference<>();
  private final AtomicReference<String> lastError = new AtomicReference<>("");
  private final AtomicReference<String> activeLogPath = new AtomicReference<>("");

  private volatile WPILOGWriter writer;

  public RotatingWPILOGWriter(String logFolder) {
    this.logFolder = logFolder;
  }

  @Override
  public void start() {
    writer = new WPILOGWriter(logFolder);
    writer.start();
    activeLogPath.set(logFolder + "/(automatic AdvantageKit filename)");
  }

  @Override
  public void end() {
    PreparedWriter unopenedReplacement = readyWriter.getAndSet(null);
    if (unopenedReplacement != null) {
      closeOnBackgroundThread(unopenedReplacement.writer(), false);
    }
    if (writer != null) {
      writer.end();
    }
  }

  @Override
  public void putTable(LogTable table) {
    if (detachForPurgeRequested.compareAndSet(true, false)) {
      WPILOGWriter previous = writer;
      writer = null;
      activeLogPath.set("(purge in progress; file logging temporarily detached)");
      closePurgeAndPrepareOnBackgroundThread(previous);
      return;
    }

    PreparedWriter replacement = readyWriter.getAndSet(null);
    if (replacement != null) {
      WPILOGWriter previous = writer;
      writer = replacement.writer();
      activeLogPath.set(replacement.path());
      rotationCount.incrementAndGet();
      lastRotationTimestampMicros.set(table.getTimestamp());
      writer.putTable(table);
      if (previous != null) {
        closeOnBackgroundThread(previous, true);
      } else if (replacement.completesPurge()) {
        purgeCount.incrementAndGet();
        lastPurgeTimestampMicros.set(table.getTimestamp());
        purgePending.set(false);
        rotationPending.set(false);
      } else {
        rotationPending.set(false);
      }
      return;
    }

    WPILOGWriter activeWriter = writer;
    if (activeWriter != null) {
      activeWriter.putTable(table);
    }
  }

  /**
   * Requests one nonblocking close/open operation. Returns false when another rotation is pending.
   */
  public boolean requestRotation() {
    return requestReplacement();
  }

  /**
   * Requests recursive deletion of all log-folder contents followed by a fresh active log.
   *
   * <p>Between complete tables, this receiver detaches the active writer. A background thread closes it,
   * purges the guarded folder, and opens a fresh explicit file. This ordering works even at 100% disk
   * usage and never unlinks a file while it is still being written. Returns false when another
   * rotate/purge operation is pending.
   */
  public boolean requestPurge() {
    if (!rotationPending.compareAndSet(false, true)) {
      return false;
    }
    purgePending.set(true);
    lastError.set("");
    detachForPurgeRequested.set(true);
    return true;
  }

  private boolean requestReplacement() {
    if (!rotationPending.compareAndSet(false, true)) {
      return false;
    }
    purgePending.set(false);
    lastError.set("");

    Thread opener = new Thread(() -> {
      try {
        String filename =
            "akit_rotated_"
                + System.currentTimeMillis()
                + "_"
                + UUID.randomUUID().toString().substring(0, 8)
                + ".wpilog";
        String replacementPath = Path.of(logFolder, filename).toString();
        WPILOGWriter replacement = new WPILOGWriter(replacementPath);
        replacement.start();
        if (!Files.exists(Path.of(replacementPath))) {
          throw new IllegalStateException("Replacement file was not created: " + replacementPath);
        }
        readyWriter.set(new PreparedWriter(replacement, replacementPath, false));
      } catch (RuntimeException exception) {
        lastError.set("Open failed: " + exception);
        rotationPending.set(false);
      }
    }, "AdvantageKit log rotation opener");
    opener.setDaemon(true);
    opener.start();
    return true;
  }

  private void closeOnBackgroundThread(WPILOGWriter writerToClose, boolean completesRotation) {
    Thread closer = new Thread(() -> {
      try {
        writerToClose.end();
      } catch (RuntimeException exception) {
        lastError.set("Close failed: " + exception);
      } finally {
        if (completesRotation) {
          rotationPending.set(false);
        }
      }
    }, "AdvantageKit log rotation closer");
    closer.setDaemon(true);
    closer.start();
  }

  private void closePurgeAndPrepareOnBackgroundThread(WPILOGWriter writerToClose) {
    Thread purgeThread = new Thread(() -> {
      String closeWarning = "";
      if (writerToClose != null) {
        try {
          writerToClose.end();
        } catch (RuntimeException exception) {
          // A completely full filesystem can make the final footer write fail. Continue with the
          // explicitly requested purge so the button can recover from that exact condition.
          closeWarning = "Previous log close warning: " + exception + "; ";
        }
      }

      try {
        purgeLogFolder();

        String filename =
            "akit_rotated_"
                + System.currentTimeMillis()
                + "_"
                + UUID.randomUUID().toString().substring(0, 8)
                + ".wpilog";
        String replacementPath = Path.of(logFolder, filename).toString();
        WPILOGWriter replacement = new WPILOGWriter(replacementPath);
        replacement.start();
        if (!Files.exists(Path.of(replacementPath))) {
          throw new IllegalStateException("Fresh log was not created after purge: " + replacementPath);
        }
        // A close warning is irrelevant after a successful requested purge because the old file was
        // deliberately discarded. Keep LastRotationError empty when cleanup and fresh-file creation
        // both succeed.
        lastError.set("");
        readyWriter.set(new PreparedWriter(replacement, replacementPath, true));
      } catch (RuntimeException exception) {
        lastError.set(closeWarning + "Purge failed: " + exception);
        purgePending.set(false);
        rotationPending.set(false);
      }
    }, "AdvantageKit log purge");
    purgeThread.setDaemon(true);
    purgeThread.start();
  }

  private void purgeLogFolder() {
    Path folder = Path.of(logFolder).toAbsolutePath().normalize();
    Path realRobotLogFolder = Path.of("/home/lvuser/logs").toAbsolutePath().normalize();
    boolean approvedFolder =
        folder.equals(realRobotLogFolder) || folder.endsWith(Path.of("logs", "sim"));
    if (!approvedFolder) {
      throw new IllegalArgumentException("Refusing to purge unexpected folder: " + folder);
    }

    try {
      List<Path> candidates;
      try (var paths = Files.walk(folder)) {
        candidates = paths.sorted(Comparator.reverseOrder()).toList();
      }
      for (Path candidate : candidates) {
        Path normalized = candidate.toAbsolutePath().normalize();
        if (!normalized.equals(folder)) {
          Files.deleteIfExists(normalized);
        }
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to purge " + folder, exception);
    }
  }

  public boolean isRotationPending() {
    return rotationPending.get();
  }

  public int getRotationCount() {
    return rotationCount.get();
  }

  public boolean isPurgePending() {
    return purgePending.get();
  }

  public int getPurgeCount() {
    return purgeCount.get();
  }

  public long getLastRotationTimestampMicros() {
    return lastRotationTimestampMicros.get();
  }

  public long getLastPurgeTimestampMicros() {
    return lastPurgeTimestampMicros.get();
  }

  public String getLastError() {
    return lastError.get();
  }

  public String getActiveLogPath() {
    return activeLogPath.get();
  }
}
