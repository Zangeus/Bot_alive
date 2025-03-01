package End;

import Waiters.FindButtonAndPress;
import Waiters.TelegramBotSender;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static Waiters.Extractor.extractResource;

public class EndIsNear {

    private static Boolean successResponse;
    private static Boolean failureResponse;

    static {
        Optional<Boolean> success = checkFileExists("bot_sources/success.txt");
        Optional<Boolean> failure = checkFileExists("bot_sources/fail.txt");

        if (success.isPresent()) successResponse = success.get();
        else System.out.println("Файл не существует или не является целевым");

        if (failure.isPresent()) failureResponse = failure.get();
        else System.out.println("Файл не существует или не является целевым");

    }

    public static void end() {
        try {
            showSRC();
            LocalDateTime startTime = LocalDateTime.now();
            DayOfWeek dayOfWeek = startTime.getDayOfWeek();

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            Runnable checkTask = getRunnableToCheck(scheduler);

            if (dayOfWeek == DayOfWeek.MONDAY) {
                scheduler.scheduleAtFixedRate(checkTask, 0, 5, TimeUnit.MINUTES);
            } else {
                scheduler.schedule(() -> {
                        if (!FindButtonAndPress.findAndClick(extractResource("/checking.png")))
                            getResponse(false);
                }, 0, TimeUnit.SECONDS);
            }

            scheduler.shutdown();
            if (!scheduler.awaitTermination(1, TimeUnit.DAYS)) {
                System.err.println("Тайм-аут! Принудительно завершаем пул.");
                scheduler.shutdownNow();
            }

            if (!FindButtonAndPress.findAndClick(extractResource("/stop.png"))) {
                getResponse(false);
            }

            getResponse(FindButtonAndPress.findAndClick(extractResource("/tasks_done.png")));

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private static Runnable getRunnableToCheck(ScheduledExecutorService scheduler) {
        AtomicInteger attempts = new AtomicInteger(0);
        return () -> {
            try {
                boolean isFound = FindButtonAndPress.findAndClick(extractResource("/checking.png"));
                if (isFound) {
                    scheduler.shutdown();
                } else if (attempts.incrementAndGet() >= 5) {
                    scheduler.shutdown();
                    getResponse(false);
                }
            } catch (Exception e) {
                System.err.println("Ошибка в задаче: " + e.getMessage());
                scheduler.shutdownNow();
            }
        };
    }

    public static void turnOff() {
        try {
            Runtime.getRuntime().exec("shutdown -s -f -t 100");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<Boolean> checkFileExists(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return Optional.empty();
        }

        String absolutePath = file.getAbsolutePath();
        String successPath = new File("bot_sources/success.txt").getAbsolutePath();
        String failPath = new File("bot_sources/fail.txt").getAbsolutePath();

        if (absolutePath.equals(successPath)) {
            return Optional.of(true);
        } else if (absolutePath.equals(failPath)) {
            return Optional.of(false);
        } else {
            return Optional.empty();
        }
    }

    public static void getResponse(boolean success) {
        TelegramBotSender.sendMessage(success ? successResponse : failureResponse);
        turnOff();
        CloseProcess.close("MuMuPlayer.exe");
//        CloseProcess.close("MuMuVMMHeadless.exe");
//        CloseProcess.close("src");
        System.exit(0);
    }

    public static void showSRC() {
        String windowTitle = "src";
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
        if (hwnd == null) {
            System.out.println("Окно с заголовком '" + windowTitle + "' не найдено.");
            return;
        }

        User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_RESTORE);
        User32.INSTANCE.SetForegroundWindow(hwnd);

        CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS)
                .execute(() -> User32.INSTANCE.SetForegroundWindow(hwnd));
    }
}
