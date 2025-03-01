package Start;

import Waiters.FindButtonAndPress;

import java.awt.*;
import java.awt.event.KeyEvent;

import static Waiters.Extractor.extractResource;
import static java.lang.Thread.sleep;

public class StartIsHere {

    public static void start() {
        try {
            Robot robot = new Robot();

            robot.keyPress(KeyEvent.VK_WINDOWS);
            robot.keyRelease(KeyEvent.VK_WINDOWS);
            sleep(300);

            FindButtonAndPress.findAndClick(extractResource("/start.png"));

            sleep(2000);

            FindButtonAndPress.findAndClick(extractResource("/start_button.png"));

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}
