package Waiters;

import Config.ConfigManager;
import Config.LauncherConfig;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static java.lang.Thread.sleep;

public class TakeTheMail {
    private static final class MailConfig {
        static final String WINDOW_TITLE = "MuMu Player 12";
        static final int DELAY_MS = 2000;
        static final Point BACK_BUTTON = new Point(555, 666);

        static final Point[] CLICK_POINTS = {
                new Point(288, 192),    // Esc
                new Point(1250, 320),   // Mail
                BACK_BUTTON                   // Get Rewards
        };
    }

    private static final Robot robot;
    private static final LauncherConfig config = ConfigManager.loadConfig();

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException("Failed to initialize Robot", e);
        }
    }

    public static void take() {
        if (!config.isTakeTheMailEnabled()) return;
        try {
            if (!activateWindow()) return;
            sleep(MailConfig.DELAY_MS);

            for (Point p : MailConfig.CLICK_POINTS) {
                performClick(p.x, p.y);
            }
            performActions();
        } catch (Exception e) {
            logError("Critical error", e);
        }
    }

    static void pressEsc() throws InterruptedException {
        sleep(1000);
        robot.keyPress(KeyEvent.VK_ESCAPE);
        robot.keyRelease(KeyEvent.VK_ESCAPE);
    }

    private static boolean activateWindow() throws InterruptedException {
        final int MAX_ATTEMPTS = 3;
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, MailConfig.WINDOW_TITLE);
            if (hwnd != null) {
                User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_RESTORE);
                User32.INSTANCE.SetForegroundWindow(hwnd);
                return true;
            }
            sleep(1000);
        }
        logError("Window not found: " + MailConfig.WINDOW_TITLE, null);
        return false;
    }

    private static void logError(String message, Exception e) {
        System.err.println("[ERROR] " + message);
        if (e != null) System.out.println(e.getMessage());
    }

    private static void performClick(int x, int y) throws InterruptedException {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        sleep(MailConfig.DELAY_MS);
    }

    private static void performActions() throws InterruptedException {
        performClick(MailConfig.BACK_BUTTON.x, MailConfig.BACK_BUTTON.y);
        pressEsc();
        pressEsc();
        sleep(MailConfig.DELAY_MS);
    }
}



