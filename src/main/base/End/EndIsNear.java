package End;

import Config.ConfigManager;
import Config.LauncherConfig;
import Waiters.FindButtonAndPress;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

public class EndIsNear {
    private static final LauncherConfig config = ConfigManager.loadConfig();
    private static final String WINDOW_TITLE = "src";

    public static boolean end() {
        try {
            focusApplicationWindow();
            System.out.println("─── Starting main check ───");

            if (!findAndClickWithDelay(config.getPicsToStartPath() + "/checking.png", 5000))
                return false;

            return findAndClick("stop.png") && findAndClick("tasks_done.png");

        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            CloseProcess.terminateProcesses();
            return false;
        }
    }

    private static boolean findAndClick(String image) throws Exception {
        String path = config.getPicsToStartPath() + "/" + image;
        System.out.println("Проверка: " + path);
        boolean result = FindButtonAndPress.findAndClick(path);
        System.out.println(result ? "✓ Обнаружено" : "✗ Не найдено");
        return result;
    }

    private static boolean findAndClickWithDelay(String path, int delayMs) throws Exception {
        System.out.println("Поиск элемента: " + path);
        boolean result = FindButtonAndPress.findAndClick(path);
        if (result) {
            System.out.println("✓ Успешное обнаружение");
            Thread.sleep(delayMs);
        }
        return result;
    }

    private static void focusApplicationWindow() {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, WINDOW_TITLE);
        if (hwnd != null) {
            User32.INSTANCE.ShowWindow(hwnd, WinUser.SW_RESTORE);
            User32.INSTANCE.SetForegroundWindow(hwnd);
            System.out.println("Окно приложения активировано");
        }
    }
}