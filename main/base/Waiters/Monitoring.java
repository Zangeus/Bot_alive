package Waiters;

import End.CloseProcess;
import Start.StartIsHere;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static Waiters.ClickByCoords.activateAndClick;
import static Waiters.ClickByCoords.activateWindow;
import static Waiters.FindButtonAndPress.*;

public class Monitoring {
    private static final String MuMu = "MuMu Player 12";
    private static final String src = "src";
    private static final Point[] CLICK_POINTS = {
            new Point(970, 444),
            new Point(940, 666),
            new Point(915, 520),
            new Point(780, 675),
    };

    private static final int minutesBetweenIterations = 10;
    private static boolean picToSend = false;

    public static void monitorStart() {
        while (true) {
            activateWindow(src);
            if (findAndClickScreenless("critical.png")) {
                refresh();
                sleep(minutesBetweenIterations);
                continue;
            }

            if (findAndClickScreenless("critical_2.png")) {
                restart();
                sleep(minutesBetweenIterations);
                continue;
            }

            if (check("su_button.png") &&
                    check("elites_farm.png")) {
                check(("overview.png"));
                if (picToSend) sendPhoto();
                break;
            }

            picToSend = true;
            check(("overview.png"));
            sleep(minutesBetweenIterations);
        }
    }

    public static boolean monitorAfterMain() {
        monitorStart();
        return true;
    }

    public static void monitor() {
        monitorStart();
        executeEmergencyProtocol();
    }

    private static boolean check(String image) {
        return findAndClickScreenless(image);
    }

    private static void executeEmergencyProtocol() {
        CloseProcess.terminateProcesses();
        performEmergencyShutdown();
    }

    public static void sendPhoto() {
        String imagePath = "bot_sources/SU.png";
        TelegramBotSender.sendLocalPhoto(imagePath);

        TelegramBotSender.sendNoteMessage("Легендарный квест 1001-ночи был завершен");
    }


    private static void refresh() {
        activateAndClick(MuMu, CLICK_POINTS, 2000);

        activateWindow(src);
        findAndClickWithOneMessage("start_button.png", "Не удалось найти кнопку запуска");

    }

    private static void sleep(int minutes) {
        try {
            TimeUnit.MINUTES.sleep(minutes);
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
}
