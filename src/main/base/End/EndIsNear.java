package End;

import Config.ConfigManager;
import Config.LauncherConfig;
import Waiters.TelegramBotSender;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static Waiters.FindButtonAndPress.*;

public class EndIsNear {
    private static final LauncherConfig config = ConfigManager.loadConfig();
    private static final String WINDOW_TITLE = "src";

    public static boolean end() {
        try {
            focusApplicationWindow();

            System.out.println("─── Starting main check ───");

            if (config.isMondayCheckEnabled() && LocalDateTime.now().getDayOfWeek() == DayOfWeek.MONDAY) {
                for (int i = 0; i < 8; i++) {
                    if (findAndClick("checking.png")) {
                        return findAndClickWithOneMessage("stop.png",
                                "Кнопка остановки не была найдена") &&
                                findAndClickForTasks("tasks_done.png");
                    } else sleep(3);
                }
                TelegramBotSender.sendNoteMessage("Очередной баг в виртуалке");
                return false;
            }
            else if (!findAndClickWithOneMessageAndDelay("checking.png",
                    "Кнопка завершения работы бота не была найдена", 2000))
                return false;

            return findAndClickWithOneMessage("stop.png",
                    "Кнопка остановки не была найдена") &&
                    findAndClickForTasks("tasks_done.png");

        } catch (Exception e) {
            System.err.println("Критическая ошибка: " + e.getMessage());
            CloseProcess.terminateProcesses();
            return false;
        }
    }

    private static void sleep(int minutes) {
        try {
            TimeUnit.MINUTES.sleep(minutes);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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