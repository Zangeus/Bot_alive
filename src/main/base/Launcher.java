import Config.ConfigManager;
import Config.ConfigWindow;
import Config.LauncherConfig;

import javax.swing.*;

public class Launcher {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--config")) {
            SwingUtilities.invokeLater(() -> {
                ConfigWindow window = new ConfigWindow();
                window.setVisible(true);
            });
        } else {
            LauncherConfig config = ConfigManager.loadConfig();
            startMainApplication(config);
        }
    }

    private static void startMainApplication(LauncherConfig config) {
        // Здесь инициализация основного приложения
        System.out.println("Запуск основного приложения...");
    }
}