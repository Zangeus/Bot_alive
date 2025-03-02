package Waiters;

import Config.ConfigManager;
import Config.LauncherConfig;
import End.EndIsNear;
import Start.StartIsHere;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final String LOCK_FILE = "app.lock";
    private static boolean success = false;
    private static final LauncherConfig config = ConfigManager.loadConfig();
    private static boolean forceShutdown = false;
    public static volatile boolean isRunning = true;

    public static void main(String[] args) {
        if (!acquireLock()) {
            System.err.println("Программа уже запущена! Выход...");
            return;
        }
        isRunning = true;

        try {
            success = false;
            final int maxAttempts = 3;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                System.out.println("\n=== Попытка #" + attempt + " ===");
                StartIsHere.start();

                sleep(0 * 60);

                boolean attemptResult = false;
                try {
                    attemptResult = EndIsNear.end();
                } catch (Exception e) {
                    System.err.println("Критическая ошибка в EndIsNear: " + e.getMessage());
                }

                if (attemptResult) {
                    success = true;
                    System.out.println("УСПЕХ! Основной цикл завершен.");
                    sendMessages(config.getSuccessMessages());
                    break;
                } else {
                    if (attempt < maxAttempts) {
                        sendMessages(config.getReportMessages());
                        System.out.println("Повтор через 15 секунд...");
                        sleep(15);
                    } else {
                        sendMessages(config.getFailureMessages());
                    }
                }
            }
        } finally {
            isRunning = false;
            System.out.println("\n=== ЗАВЕРШЕНИЕ РАБОТЫ ===");
            performFinalCleanup();

            if (!success || forceShutdown) {
                if (forceShutdown) {
                    performEmergencyShutdown();
                }
            }

            System.exit(0);
        }
    }

    private static void sendMessages(List<String> messages) {
        if (messages != null && !messages.isEmpty()) {
            TelegramBotSender.sendMessages(messages);
        } else {
            System.err.println("Список сообщений пуст, отправка отменена");
        }
    }

    private static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void performEmergencyShutdown() {
        try {
            Runtime.getRuntime().exec("shutdown -s -f -t 100");
            System.out.println("Shutdown command sent successfully");
        } catch (IOException e) {
            System.err.println("Failed to execute shutdown command: " + e.getMessage());
        }
    }

    private static void performFinalCleanup() {
        try {
            File lockFile = new File(LOCK_FILE);
            if (lockFile.exists()) {
                lockFile.delete();
            }
        } catch (Exception e) {
            System.err.println("Final cleanup error: " + e.getMessage());
        }
    }

    private static boolean acquireLock() {
        try {
            File lockFile = new File(LOCK_FILE);
            FileChannel channel = new RandomAccessFile(lockFile, "rw").getChannel();
            FileLock lock = channel.tryLock();

            if (lock == null) {
                channel.close();
                return false;
            }

            // Добавляем shutdown hook для удаления блокировки при завершении работы
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    lock.release();
                    channel.close();
                    lockFile.delete();
                } catch (IOException ignored) {
                }
            }));

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean isRunning() {
        return isRunning;
    }
}