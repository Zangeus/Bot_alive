package End;

import java.io.IOException;

public class CloseProcess {
    public static void close(String processName) {
        try {
            // Создаем процесс для выполнения команды task-list
            ProcessBuilder processBuilder = new ProcessBuilder("tasklist");
            Process process = processBuilder.start();

            // Читаем вывод команды task-list
            java.util.Scanner scanner = new java.util.Scanner(process.getInputStream());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // Проверяем, содержит ли строка имя процесса MuMu Player
                if (line.contains(processName)) {
                    // Извлекаем PID процесса
                    String[] parts = line.split("\\s+");
                    String pid = parts[1];

                    // Завершаем процесс по PID
                    new ProcessBuilder("taskkill", "/PID", pid, "/F").start();
                    System.out.println("Процесс " + processName + " с PID " + pid + " завершен.");
                }
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
