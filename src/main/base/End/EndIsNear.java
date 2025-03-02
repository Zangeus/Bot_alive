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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EndIsNear {
    private static final LauncherConfig config = ConfigManager.loadConfig();
    private static ScheduledExecutorService scheduler;
    private static final String WINDOW_TITLE = "src";
    private static final int MAX_ATTEMPTS = 5;
    private static final int SCHEDULE_INTERVAL_MIN = 5;
    private static volatile boolean globalSuccess = false;

    public static boolean end() {
        try {
            focusApplicationWindow();
            LocalDateTime startTime = LocalDateTime.now();

            scheduler = Executors.newScheduledThreadPool(2);
            scheduleTasks(startTime.getDayOfWeek());

            if (!monitorScheduler()) {
                return false;
            }
            return globalSuccess;
        } catch (Exception e) {
            handleCriticalError(e);
            return false;
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
                boolean result = performFullCheck();
                if (result || attempts.incrementAndGet() >= MAX_ATTEMPTS) {
                    if (result) {
                        globalSuccess = true;
                        shutdownScheduler();
                    } else {
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
                globalSuccess = performFullCheck();
                if (!globalSuccess) {
                    handleFailure("Initial check failed");
                }
            } catch (Exception e) {
                handleTaskError(e);
            }
        };
    }

    private static boolean performFullCheck() throws Exception {
        boolean checkResult = performCheck();
        if (checkResult) {
            return executePostCheckActions();
        }
        return false;
    }

    private static boolean performCheck() throws Exception {
        String checkingPath = config.getPicsToStartPath() + "/checking.png";
        System.out.println("Searching for: " + checkingPath);

        boolean result = FindButtonAndPress.findAndClick(checkingPath);
        if (result) {
            System.out.println("Check successful! Waiting for UI update...");
            TimeUnit.SECONDS.sleep(2);
        }
        return result;
    }

    private static boolean executePostCheckActions() {
        System.out.println("Executing post-check actions...");
        boolean stopFound = verifyComponent("stop.png");
        boolean tasksDoneFound = verifyComponent("tasks_done.png");

        if (!stopFound || !tasksDoneFound) {
            System.err.println("Post-check verification failed");
            return false;
        }
        return true;
    }

    private static boolean verifyComponent(String image) {
        String path = config.getPicsToStartPath() + "/" + image;
        System.out.println("Verifying component: " + path);
        return FindButtonAndPress.findAndClick(path);
    }

    private static void initiateShutdownSequence(boolean success) {
        System.out.println("Initiating shutdown sequence...");
        if (config.isReportNotification()) {
            sendNotifications(success);
        }
        performSystemCleanup(success);
        if (success) {
            Main.requestForceShutdown();
        }
    }

    private static boolean monitorScheduler() throws InterruptedException {
        return scheduler.awaitTermination(1, TimeUnit.DAYS);
    }

    private static void shutdownScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    private static void handleFailure(String message) {
        System.err.println("Сбой: " + message);
        sendNotifications(false);
        shutdownScheduler(); // Только останавливаем планировщик
    }

    private static void handleCriticalError(Exception e) {
        System.err.println("Критическая ошибка: " + e.getMessage());
        sendNotifications(false);
        performSystemCleanup(false);
    }

    private static void handleTaskError(Exception e) {
        System.err.println("Ошибка задачи: " + e.getMessage());
        shutdownScheduler();
        sendNotifications(false);
    }

    private static void sendNotifications(boolean success) {
        if ((success && config.isSuccessNotification()) || (!success && config.isFailureNotification())) {
            TelegramBotSender.sendNotifications(success);
        }
    }

    private static void performSystemCleanup(boolean success) {
        try {
            System.out.println("Выполнение очистки...");
            CloseProcess.terminate("MuMuPlayer.exe");
            CloseProcess.terminate("src");

            if (success) {
                System.out.println("Инициирование выключения системы...");
                Runtime.getRuntime().exec("shutdown -s -f -t 100");
            }
        } catch (Exception e) {
            System.err.println("Ошибка очистки: " + e.getMessage());
        }
    }

    private static void focusApplicationWindow() {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, WINDOW_TITLE);
        if (hwnd != null) {
            User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_RESTORE);
            User32.INSTANCE.SetForegroundWindow(hwnd);
        }
    }
}