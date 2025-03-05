package Waiters;

import End.CloseProcess;
import Start.StartIsHere;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static Waiters.FindButtonAndPress.findAndClickScreenless;

public class Monitoring {
    private static final String WINDOW_TITLE = "src";
    private static final int minutesBetweenIterations = 10;
    private static final int secondsAfterMessages = 5;


    public static void monitor() {
        do focusApplicationWindow();
        while (!handleCriticalSituation());
    }

    private static boolean handleCriticalSituation() {
        if (findAndClickScreenless("critical.png")) {
            restart();
            return false;
        }

        if (!checkAndHandle("checking.png")) return false;
        if (!checkAndHandle("su_button.png")) return false;
        if (!checkAndHandle("elites_farm.png")) return false;

        executeEmergencyProtocol();
        return true;
    }

    private static boolean checkAndHandle(String image) {
        if (findAndClickScreenless(image)) return true;
        restart();
        return false;
    }

    private static void executeEmergencyProtocol() {

        TelegramBotSender.sendNoteMessage("Легендарный квест 1001-ночи был завершен");
        sleep(secondsAfterMessages);

        CloseProcess.terminateProcesses();
        performEmergencyShutdown();
    }

    private static void restart() {
        CloseProcess.terminateProcesses();
        for (int i = 0; i <= 3; i++) {
            try {
                if (StartIsHere.start()) break;
                else if (i == 3) TelegramBotSender
                        .sendNoteMessage("Не удалось запустить бота");
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void performEmergencyShutdown() {
        try {
            Runtime.getRuntime().exec("shutdown -s -f -t 100");
            System.out.println("Выключение было запущенно");
        } catch (IOException e) {
            System.err.println("Выключение было прервано: " + e.getMessage());
        }
    }

    private static void focusApplicationWindow() {
        sleep(60 * minutesBetweenIterations);

        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, WINDOW_TITLE);
        if (hwnd != null) {
            User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_RESTORE);
            User32.INSTANCE.SetForegroundWindow(hwnd);
            System.out.println("Окно приложения активировано");
        }
    }
}
