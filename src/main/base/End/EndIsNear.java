package End;

import Config.ConfigManager;
import Config.LauncherConfig;
import Waiters.FindButtonAndPress;
import Waiters.Main;
import Waiters.TelegramBotSender;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static Waiters.TelegramBotSender.sendNotifications;

public class EndIsNear {
    private static final LauncherConfig config = ConfigManager.loadConfig();
    private static ScheduledExecutorService scheduler;
    private static final String WINDOW_TITLE = "src";
    private static final int SHUTDOWN_DELAY_SEC = 100;
    private static final int MAX_ATTEMPTS = 5;
    private static final int SCHEDULE_INTERVAL_MIN = 5;

    public static boolean end() {
        try {
            focusApplicationWindow();
            LocalDateTime startTime = LocalDateTime.now();

            scheduler = Executors.newScheduledThreadPool(2);
            scheduleTasks(startTime.getDayOfWeek());

            monitorScheduler();
            executePostCheckActions();
            return true;
        } catch (Exception e) {
            handleCriticalError(e);
            return false; // Возвращаем false при ошибке
        }
    }

    private static void scheduleTasks(DayOfWeek day) {
        if (day == DayOfWeek.MONDAY) {
            scheduler.scheduleAtFixedRate(
                    createMonitoringTask(),
                    0,
                    SCHEDULE_INTERVAL_MIN,
                    TimeUnit.MINUTES
            );
            System.out.println("Monday mode: Periodic checks enabled");
        } else {
            scheduler.schedule(
                    createSingleCheckTask(),
                    0,
                    TimeUnit.SECONDS
            );
            System.out.println("Regular mode: Single check scheduled");
        }
    }

    private static Runnable createMonitoringTask() {
        AtomicInteger attempts = new AtomicInteger(0);
        return () -> {
            try {
                System.out.println("Monitoring attempt #" + attempts.get());
                if (performCheck() || attempts.incrementAndGet() >= MAX_ATTEMPTS) {
                    shutdownScheduler();
                    if (attempts.get() >= MAX_ATTEMPTS) {
                        handleFailure("Max monitoring attempts reached");
                    }
                }
            } catch (Exception e) {
                handleTaskError(e);
            }
        };
    }

    private static Runnable createSingleCheckTask() {
        return () -> {
            try {
                if (!performCheck()) {
                    handleFailure("Initial check failed");
                }
            } catch (Exception e) {
                handleTaskError(e);
            }
        };
    }

    private static boolean performCheck() throws Exception {
        String checkingPath = config.getPicsToStartPath() + "/checking.png";
        System.out.println("Searching for: " + checkingPath);

        boolean result = FindButtonAndPress.findAndClick(checkingPath);
        if (result) {
            System.out.println("Check successful! Proceeding to next step...");
            TimeUnit.SECONDS.sleep(2); // Wait for UI update
            executePostCheckActions();
        }
        return result;
    }

    private static void executePostCheckActions() {
        System.out.println("Executing post-check actions...");
        boolean stopFound = verifyComponent("stop.png");
        boolean tasksDoneFound = verifyComponent("tasks_done.png");

        if (!stopFound || !tasksDoneFound) {
            handleFailure("Post-check verification failed");
        }
        initiateShutdownSequence(tasksDoneFound);
    }

    private static boolean verifyComponent(String image) {
        String path = config.getPicsToStartPath() + "/" + image;
        System.out.println("Verifying component: " + path);
        return FindButtonAndPress.findAndClick(path);
    }

    private static void performSystemCleanup(boolean success) {
        try {
            System.out.println("Starting system cleanup...");

            System.out.println("Terminating MuMuPlayer...");
            boolean mumuClosed = CloseProcess.terminate("MuMuPlayer.exe");
            CloseProcess.terminate("src");
            System.out.println("MuMuPlayer terminated: " + mumuClosed);

            if (success) {
                System.out.println("Initiating system shutdown...");
                try {
                    Runtime.getRuntime().exec("shutdown -s -f -t 100");
                    System.out.println("Shutdown command executed");
                } catch (IOException e) {
                    System.err.println("Standard shutdown failed: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Cleanup error: " + e.getMessage());
        } finally {
            System.exit(0);
        }
    }


    private static void initiateShutdownSequence(boolean success) {
        System.out.println("Initiating shutdown sequence...");
        sendNotifications(success);
        performSystemCleanup(success);
        if (success) {
            Main.requestForceShutdown();
        }
    }

    private static void monitorScheduler() throws InterruptedException {
        if (!scheduler.awaitTermination(1, TimeUnit.DAYS)) {
            System.err.println("Scheduler timeout! Forcing shutdown.");
            scheduler.shutdownNow();
        }
    }

    private static void shutdownScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            System.out.println("Shutting down scheduler...");
            scheduler.shutdown();
        }
    }

    private static void handleFailure(String message) {
        System.err.println("Failure: " + message);
        initiateShutdownSequence(false);
    }

    private static void handleCriticalError(Exception e) {
        System.err.println("Critical error: " + e.getMessage());
        sendNotifications(false);
        performSystemCleanup(false);
    }

    private static void handleTaskError(Exception e) {
        System.err.println("Task error: " + e.getMessage());
        shutdownScheduler();
        performSystemCleanup(false); // Передаем false
        System.exit(3);
    }

    private static void focusApplicationWindow() {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, WINDOW_TITLE);
        if (hwnd != null) {
            User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_RESTORE);
            User32.INSTANCE.SetForegroundWindow(hwnd);
            System.out.println("Application window focused");
        } else {
            System.out.println("Application window not found");
        }
    }
}