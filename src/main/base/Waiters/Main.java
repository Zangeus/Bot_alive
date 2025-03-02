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
    private static int ATTEMPTS = 0;
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
            while (ATTEMPTS < 3 && !success) {
                System.out.println("\n=== Попытка #" + (ATTEMPTS + 1) + " ===");
                StartIsHere.start();

                sleep(config.getSleepDurationMinutes() * 60);

                boolean attemptResult = false;
                try {
                    attemptResult = EndIsNear.end();
                } catch (Exception e) {
                    System.err.println("Критическая ошибка в EndIsNear: " + e.getMessage());
                }

                ATTEMPTS++; // Увеличиваем счетчик ПОСЛЕ попытки

                if (attemptResult) {
                    success = true;
                    System.out.println("УСПЕХ! Основной цикл завершен.");
                    break;
                } else if (ATTEMPTS < 3) {
                    System.out.println("Повтор через 15 секунд...");
                    sleep(15); // Пауза между попытками
                }
            }
        } finally {
            isRunning = false;
            System.out.println("\n=== ЗАВЕРШЕНИЕ РАБОТЫ ===");
            performFinalCleanup();

            if (!success || forceShutdown) {
                sendEmergencyMessage();
                if (forceShutdown) {
                    performEmergencyShutdown();
                }
            }
            System.exit(0);
        }
    }

    private static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void requestForceShutdown() {
        forceShutdown = true;
    }

    private static void sendEmergencyMessage() {
        if (config.isNotificationsEnabled()) {
            TelegramBotSender.sendMessages(List.of("Самый ужасный заход эвер"));
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
