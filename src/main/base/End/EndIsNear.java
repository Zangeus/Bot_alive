package End;

import Config.ConfigManager;
import Config.LauncherConfig;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import static Waiters.FindButtonAndPress.*;

public class EndIsNear {
    private static final LauncherConfig config = ConfigManager.loadConfig();
    private static final String WINDOW_TITLE = "src";

    public static boolean end() {
        try {
            focusApplicationWindow();

            System.out.println("─── Starting main check ───");

            if (!findAndClickWithMessageAndDelay("checking.png",
                    "Кнопка завершения бота не была найдена", 2000))
                return false;

            return findAndClickWithMessage("stop.png",
                    "Кнопка остановки не была найдена") &&
                    findAndClickWithMessage("tasks_done.png",
                            "Задания не были выполнены");

        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            CloseProcess.terminateProcesses();
            return false;
        }
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