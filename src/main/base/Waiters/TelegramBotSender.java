package Waiters;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static Waiters.Extractor.captureScreenshot;
import static Waiters.Extractor.sendPhotoWithCaption;

public class TelegramBotSender {

    public static final String BOT_TOKEN;
    public static final String CHAT_ID;

    static {
        try {
            String[] telegramData =
                    readBotConfig("bot_sources/botAccess.txt");

            BOT_TOKEN = telegramData[0];
            CHAT_ID = telegramData[1];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final List<String> successMessages;
    private static final List<String> failureMessages;
    private static final List<String> reportMessages;

    static {
        try {
            successMessages = readMessagesFromFile("bot_sources/success_messages.txt");
            failureMessages = readMessagesFromFile("bot_sources/failure_messages.txt");
            reportMessages = readMessagesFromFile("bot_sources/report_messages.txt");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Failed to load messages", e);
        }
    }

    public static void sendMessage(Boolean response) {
        if (response == null) return;

        try {
            String message;
            if (response) {
                message = getRandomMessage(successMessages);
                sendTextMessage(message);
            } else if (Main.ATTEMPTS < 3) {
                message = getRandomMessage(reportMessages);
                handleReportMessage(message);
            } else {
                message = getRandomMessage(failureMessages);
                sendTextMessage(message);
            }
        } catch (Exception e) {
            System.out.println("Ошибка отправки: " + e.getMessage());
        }
    }

    private static void handleReportMessage(String message) {
        try {
            byte[] screenshot = captureScreenshot();
            sendPhotoWithCaption(screenshot, message);
        } catch (Exception e) {
            System.out.println("Ошибка скриншота: " + e.getMessage());
            sendTextMessage(message);
        }
    }

    private static void sendTextMessage(String message) {
        if (isInvalidMessage(message)) return;

        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String postData = "chat_id=" + CHAT_ID + "&text=" + encodedMessage;

        try {
            executePostRequest("sendMessage", postData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void executePostRequest(String method, String postData) throws IOException {
        String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/" + method;
        URL url = new URL(urlString);

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            handleResponse(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static void handleResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        String method = connection.getURL().getPath().contains("sendPhoto") ? "Фото" : "Сообщение";

        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println(method + " успешно отправлено!");
        } else {
            System.out.println("Ошибка отправки " + method + ". Код: " + responseCode);
        }
    }

    private static boolean isInvalidMessage(String message) {
        if (message == null || message.isEmpty()) {
            System.out.println("Пустое сообщение - отправка отменена");
            return true;
        }
        return false;
    }

    private static String getRandomMessage(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return "Нет доступных сообщений";
        }
        Random random = new Random();
        return messages.get(random.nextInt(messages.size()));
    }

    private static List<String> readMessagesFromFile(String fileName) throws Exception {
        List<String> messages = new ArrayList<>();
        try (InputStream is = TelegramBotSender.class.getResourceAsStream(fileName)) {
            if (is == null) {
                throw new FileNotFoundException("Файл не найден: " + fileName);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    messages.add(line.trim());
                }
            }
        }
        return messages;
    }

    public static String[] readBotConfig(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new IOException("Конфигурационный файл бота не найден: " + fileName);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String botToken = reader.readLine(); // Первая строка — токен бота
            String chatId = reader.readLine();   // Вторая строка — ID чата
            if (botToken == null || chatId == null) {
                throw new IOException("Конфигурационный файл бота неполный: требуется токен и идентификатор чата.");
            }
            return new String[]{botToken, chatId};
        }
    }
}