package Waiters;

import Config.ConfigManager;
import Config.LauncherConfig;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class FindButtonAndPress {
    private static final LauncherConfig config = ConfigManager.loadConfig();
    public static final Robot robot;
    private static double MATCH_THRESHOLD = 0.8;

    static {
        try {
            robot = new Robot();
            nu.pattern.OpenCV.loadLocally();
        } catch (AWTException e) {
            throw new RuntimeException("Ошибка инициализации Robot: " + e.getMessage());
        }
    }

    public static boolean findAndClick(String imagePath) {
        int attempts = 0;
        final int maxAttempts = config.getAttemptsAmount();
        final int delayMs = config.getSearchDelayMs();

        while (attempts < maxAttempts) {
            Point buttonLocation = findButton(imagePath);
            if (buttonLocation != null) {
                performClick(buttonLocation);
                return true;
            }

            sleepSafe(delayMs);
            attempts++;
        }

        handleFailure(maxAttempts);
        return false;
    }

    private static Point findButton(String buttonImagePath) {
        Mat buttonImage = Imgcodecs.imread(buttonImagePath);
        if (buttonImage.empty()) {
            System.err.println("Изображение не найдено: " + buttonImagePath);
            return null;
        }

        try {
            Mat screen = captureScreen();
            Mat result = new Mat();
            Imgproc.matchTemplate(screen, buttonImage, result, Imgproc.TM_CCOEFF_NORMED);

            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
            return mmr.maxVal > MATCH_THRESHOLD ? new Point(mmr.maxLoc.x, mmr.maxLoc.y) : null;
        } catch (Exception e) {
            System.err.println("Ошибка поиска: " + e.getMessage());
            return null;
        }
    }

    private static Mat captureScreen() {
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenImage = robot.createScreenCapture(screenRect);
        return convertToMat(screenImage);
    }

    private static Mat convertToMat(BufferedImage image) {
        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                mat.put(y, x, new byte[] {
                        (byte) ((pixel) & 0xFF),   // Blue
                        (byte) ((pixel >> 8) & 0xFF),  // Green
                        (byte) ((pixel >> 16) & 0xFF)  // Red
                });
            }
        }
        return mat;
    }

    private static void performClick(Point location) {
        robot.mouseMove((int) location.x, (int) location.y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        System.out.println("Успешный клик в координатах: " + location);
    }

    private static void sleepSafe(int delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Прервано ожидание: " + e.getMessage());
        }
    }

    private static void handleFailure(int maxAttempts) {
        System.err.println("Кнопка не найдена после " + maxAttempts + " попыток");
        // Отправляем уведомление со скриншотом
        if (config.isFailureNotification() && config.isNotificationsEnabled()) {
            try {
                byte[] screenshot = Extractor.captureScreenshot();
                TelegramBotSender.sendPhoto(
                        screenshot,
                        "Не удалось найти кнопку после " + maxAttempts + " попыток!"
                );
            } catch (Exception e) {
                System.err.println("Ошибка отправки скриншота: " + e.getMessage());
                TelegramBotSender.sendMessages(List.of("Ошибка! Не удалось сделать скриншот"));
            }
        }
    }

    public static void calibrateDetection(double newThreshold) {
        System.out.println("Калибровка порога обнаружения: " + newThreshold);
        MATCH_THRESHOLD = Math.min(Math.max(newThreshold, 0.5), 1.0);
    }
}