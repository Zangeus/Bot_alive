package Start;

import Config.ConfigManager;
import Config.LauncherConfig;
import Waiters.FindButtonAndPress;
import Waiters.TelegramBotSender;

import java.awt.*;
import java.awt.event.KeyEvent;

import static Waiters.FindButtonAndPress.*;
import static java.lang.Thread.sleep;

public class StartIsHere {
    private static final LauncherConfig config = ConfigManager.loadConfig();

    public static boolean start() throws InterruptedException {
        activateWindows();

        if (!findAndClickWithOneMessageAndDelay("start.png"
                , "Кнопка для старта не была найдена", 2000))
            return false;

        return findAndClickWithOneMessage("start_button.png", "Не удалось найти кнопку запуска");
    }

    private static void activateWindows() {
        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_WINDOWS);
            robot.keyRelease(KeyEvent.VK_WINDOWS);
            sleep(500);
        } catch (InterruptedException | AWTException e) {
            Thread.currentThread().interrupt();
            System.err.println("Прервана активация меню Windows: " + e.getMessage());
        }
    }

    private static void handleFailure(String message) {
        System.err.println(message);

    }
}