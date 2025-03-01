package Waiters;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;

public class FindButtonAndPress {
    public static final Robot robot;
    private static final int MAX_ATTEMPTS = 40; // Максимальное количество попыток

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            throw new RuntimeException("Ошибка при создании Robot!", e);
        }
    }

    public static boolean findAndClick(String imagePath) {
        int attempts = 0;

        while (attempts < MAX_ATTEMPTS) {
            Point buttonLocation = findButton(imagePath);
            if (buttonLocation != null) {
                robot.mouseMove((int) buttonLocation.x, (int) buttonLocation.y);
                robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                System.out.println("Кнопка нажата!");
                return true;
            }

            attempts++;
            try {
                Thread.sleep(1000); // Подождать 1 секунду перед следующей попыткой
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Ошибка при ожидании: " + e.getMessage());
                return false;
            }
        }

        System.err.println("Не удалось найти кнопку за " + MAX_ATTEMPTS + " попыток.");
         // Завершаем программу с кодом ошибки
        return false;
    }

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    public static Point findButton(String buttonImagePath) {
        Mat buttonImage = Imgcodecs.imread(buttonImagePath);
        if (buttonImage.empty()) {
            System.err.println("Ошибка: изображение кнопки не найдено!");
            return null;
        }

        try {
            Robot robot = new Robot();
            BufferedImage screenImage = robot.createScreenCapture(
                    new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            Mat screen = bufferedImageToMat(screenImage);
            Mat result = new Mat();
            Imgproc.matchTemplate(screen, buttonImage, result, Imgproc.TM_CCOEFF_NORMED);
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

            return mmr.maxVal > 0.8 ? mmr.maxLoc : null;
        } catch (AWTException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static Mat bufferedImageToMat(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        Mat mat = new Mat(height, width, CvType.CV_8UC3);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bufferedImage.getRGB(x, y);
                byte blue = (byte) (pixel & 0xff);
                byte green = (byte) ((pixel >> 8) & 0xff);
                byte red = (byte) ((pixel >> 16) & 0xff);
                mat.put(y, x, new byte[]{blue, green, red});
            }
        }
        return mat;
    }
}
