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

    public static boolean end() {
        try {
            focusApplicationWindow();
            scheduler = Executors.newScheduledThreadPool(2);

            // Создаем главную задачу с таймаутом
            Future<Boolean> mainTask = scheduler.submit(() -> {
                scheduleTasks(LocalDateTime.now().getDayOfWeek());
                return executeFullWorkflow();
            });

            try {
                // Ожидаем результат с таймаутом
                return mainTask.get(10, TimeUnit.MINUTES);
            } catch (TimeoutException e) {
                System.err.println("Превышено время выполнения!");
                mainTask.cancel(true);
                return false;
            }
        } catch (Exception e) {
            handleCriticalError(e);
            return false;
        } finally {
            shutdownScheduler();
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
            System.out.println("[Режим понедельника] Фоновые проверки активированы");
        } else {
            scheduler.schedule(
                    createSingleCheckTask(),
                    0,
                    TimeUnit.SECONDS
            );
            System.out.println("[Обычный режим] Стандартная проверка");
        }
    }

    private static Runnable createMonitoringTask() {
        AtomicInteger attempts = new AtomicInteger(0);
        return () -> {
            if (Thread.currentThread().isInterrupted()) return;

            try {
                System.out.println("\nПроверка #" + attempts.incrementAndGet());
                if (performFullCheck() || attempts.get() >= MAX_ATTEMPTS) {
                    shutdownScheduler();
                }
            } catch (Exception e) {
                handleTaskError(e);
            }
        };
    }

    private static Runnable createSingleCheckTask() {
        return () -> {
            try {
                if (!performFullCheck()) {
                    handleFailure("Основная проверка не пройдена");
                }
            } catch (Exception e) {
                handleTaskError(e);
            }
        };
    }

    private static boolean performFullCheck() throws Exception {
        System.out.println("─── Основная проверка ───");
        boolean checkResult = performCheck();
        return checkResult && executePostCheckActions();
    }

    private static boolean performCheck() throws Exception {
        String checkingPath = config.getPicsToStartPath() + "/checking.png";
        System.out.println("Поиск элемента: " + checkingPath);

        boolean result = FindButtonAndPress.findAndClick(checkingPath);
        if (result) {
            System.out.println("✓ Успешное обнаружение");
            TimeUnit.SECONDS.sleep(5); // Увеличенная задержка
        }
        return result;
    }

    private static boolean executePostCheckActions() throws Exception {
        System.out.println("\n─── Дополнительные проверки ───");
        return verifyComponent("stop.png") && verifyComponent("tasks_done.png");
    }

    private static boolean verifyComponent(String image) {
        String path = config.getPicsToStartPath() + "/" + image;
        System.out.println("Проверка: " + path);
        boolean result = FindButtonAndPress.findAndClick(path);
        System.out.println(result ? "✓ Обнаружено" : "✗ Не найдено");
        return result;
    }

    private static void handleFailure(String message) {
        System.err.println("Ошибка: " + message);
    }

    private static void handleCriticalError(Exception e) {
        System.err.println("Критическая ошибка: " + e.getMessage());
        performCleanup();
    }

    private static void handleTaskError(Exception e) {
        System.err.println("Ошибка выполнения: " + e.getMessage());
    }

    private static void performCleanup() {
        try {
            System.out.println("Завершение процессов...");
            CloseProcess.terminate("MuMuPlayer.exe");
            CloseProcess.terminate("src");
        } catch (Exception e) {
            System.err.println("Ошибка очистки: " + e.getMessage());
        }
    }

    private static void shutdownScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            System.out.println("Остановка планировщика...");
            scheduler.shutdownNow(); // Принудительный останов
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.err.println("Не все задачи завершились корректно");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void focusApplicationWindow() {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, WINDOW_TITLE);
        if (hwnd != null) {
            User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_RESTORE);
            User32.INSTANCE.SetForegroundWindow(hwnd);
            System.out.println("Окно приложения активировано");
        }
    }

    private static boolean executeFullWorkflow() {
        try {
            return performFullCheck();
        } catch (Exception e) {
            handleCriticalError(e);
            return false;
        }
    }
}