package Waiters;

import Config.ConfigManager;
import Config.LauncherConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;

public class TelegramBotSender {
    private static final LauncherConfig config = ConfigManager.loadConfig();
    private static final Random random = new Random();
    private static final String API_URL = "https://api.telegram.org/bot";

    public static void sendMessages(List<String> messages) {
        if (!validateConfig()) return;

        String message = getRandomMessage(messages);
        if (isInvalidMessage(message)) {
            System.out.println("Пустое сообщение - отправка отменена");
            return;
        }

        sendRequest("sendMessage", "text", message);
    }

    public static void sendReportWithScreenshot(List<String> messages) {
        if (!validateConfig()) return;

        try {
            byte[] screenshot = Extractor.captureScreenshot();
            String caption = getRandomMessage(messages);
            Extractor.sendPhotoWithCaption(
                    screenshot,
                    caption,
                    config.getBotToken(),
                    config.getChatId()
            );
        } catch (Exception e) {
            System.err.println("Ошибка отправки отчета: " + e.getMessage());
            sendMessages(messages);
        }
    }

    public static void sendNotifications(boolean success) {
        // Проверка общего флага уведомлений
        if (!config.isNotificationsEnabled()) return;

        // Проверка конкретных флагов типа уведомления
        if (success && !config.isSuccessNotification()) return;
        if (!success && !config.isFailureNotification()) return;

        // Выбор сообщений
        List<String> messages = success ?
                config.getSuccessMessages() :
                config.getFailureMessages();

        // Проверка на пустые сообщения
        if (messages == null || messages.isEmpty()) return;

        // Выбор способа отправки
        if (config.isReportWithScreenshot()) {
            sendReportWithScreenshot(messages);
        } else {
            sendMessages(messages);
        }
    }

    private static boolean validateConfig() {
        if (config.getBotToken() == null || config.getBotToken().isEmpty()) {
            System.err.println("Токен бота не настроен!");
            return false;
        }

        if (config.getChatId() == null || config.getChatId().isEmpty()) {
            System.err.println("Chat ID не настроен!");
            return false;
        }

        return true;
    }

    private static String getRandomMessage(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return config.isDebugMode()
                    ? "Нет доступных сообщений в конфигурации"
                    : "";
        }
        return messages.get(random.nextInt(messages.size()));
    }

    private static boolean isInvalidMessage(String message) {
        return message == null || message.isEmpty() || message.equals("Нет доступных сообщений");
    }

    private static void sendRequest(String method, String paramType, String content) {
        try {
            String urlString = API_URL + config.getBotToken() + "/" + method;
            String postData = String.format(
                    "chat_id=%s&%s=%s",
                    URLEncoder.encode(config.getChatId(), StandardCharsets.UTF_8),
                    paramType,
                    URLEncoder.encode(content, StandardCharsets.UTF_8)
            );

            HttpURLConnection connection = createConnection(urlString);
            sendPostData(connection, postData);
            handleResponse(connection);
        } catch (Exception e) {
            System.err.println("Ошибка отправки сообщения: " + e.getMessage());
        }
    }

    private static HttpURLConnection createConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        return connection;
    }

    private static void sendPostData(HttpURLConnection connection, String postData) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = postData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }

    private static void handleResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.err.println("Ошибка Telegram API. Код: " + responseCode);
            readErrorResponse(connection);
        }
        connection.disconnect();
    }

    private static void readErrorResponse(HttpURLConnection connection) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8)
        )) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.err.println("Ответ сервера: " + response);
        } catch (IOException e) {
            System.err.println("Ошибка чтения ответа об ошибке: " + e.getMessage());
        }
    }

    // Метод для прямой отправки фото (используется в Extractor)
    public static void sendPhoto(byte[] imageBytes, String caption) {
        if (!config.shouldSendFailureReport()) return;

        try {
            Extractor.sendPhotoWithCaption(
                    imageBytes,
                    caption,
                    config.getBotToken(),
                    config.getChatId()
            );
        } catch (Exception e) {
            System.err.println("Ошибка отправки фото: " + e.getMessage());
        }
    }
}