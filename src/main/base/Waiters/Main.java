package Waiters;

import End.EndIsNear;
import Start.StartIsHere;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class Main {
    private static final String LOCK_FILE = "app.lock";
    public static int ATTEMPTS = 1;

    public static void main(String[] args) {
        if (!acquireLock()) {
            System.err.println("Программа уже запущена! Выход...");
            return;
        }
        while (ATTEMPTS <= 3) {
            ATTEMPTS++;
            {
                StartIsHere.start();
                sleep(10);
                EndIsNear.end();
            }
        }
    }

    private static void sleep(long minutes) {
        try {
            Thread.sleep(minutes * 60 * 1000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
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
}
