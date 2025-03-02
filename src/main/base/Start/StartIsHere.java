package Start;

import Config.ConfigManager;
import Config.LauncherConfig;
import Waiters.FindButtonAndPress;
import Waiters.TelegramBotSender;

import java.awt.*;
import java.awt.event.KeyEvent;
import static java.lang.Thread.sleep;

public class StartIsHere {
    private static final LauncherConfig config = ConfigManager.loadConfig();

    public static void start() {
        try {
            Robot robot = new Robot();
            activateWindows(robot);

            String startImagePath = config.getPicsToStartPath() + "/start.png";
            if (!FindButtonAndPress.findAndClick(startImagePath)) {
                handleFailure("Не удалось найти стартовую кнопку");
            }

            sleep(2000); // Ожидание анимации

            String startButtonPath = config.getPicsToStartPath() + "/start_button.png";
            if (!FindButtonAndPress.findAndClick(startButtonPath)) {
                handleFailure("Не удалось найти кнопку запуска");
            }

        } catch (Exception e) {
            handleError(e);
        }
    }

    private static void activateWindows(Robot robot) {
        try {
            robot.keyPress(KeyEvent.VK_WINDOWS);
            robot.keyRelease(KeyEvent.VK_WINDOWS);
            sleep(300); // Короткая задержка для открытия меню
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Прервана активация меню Windows: " + e.getMessage());
        }
    }

    private static void handleFailure(String message) {
        System.err.println(message);
        if (config.isFailureNotification()) {
            TelegramBotSender.sendMessages(config.getFailureMessages());
        }
        System.exit(1);
    }

    private static void handleError(Exception e) {
        System.err.println("Критическая ошибка запуска: " + e.getMessage());
        if (config.isFailureNotification()) {
            TelegramBotSender.sendMessages(config.getFailureMessages());
        }
        System.exit(2);
    }
}