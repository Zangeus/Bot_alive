package Config;

import java.util.List;
import java.util.ArrayList;

public class LauncherConfig {
    private int attemptsAmount = 40;
    private boolean successNotification = true;
    private boolean failureNotification = true;
    private boolean reportNotification = true;

    private List<String> successMessages = new ArrayList<>();
    private List<String> failureMessages = new ArrayList<>();
    private List<String> reportMessages = new ArrayList<>();

    private String botToken = "";
    private String chatId = "";
    private String picsToStartPath = "";
    private String readmePath = "README.md";

    private boolean enableAutoRetry = true;
    private int searchDelayMs = 1000;
    private boolean debugMode = false;

    // Новые поля
    private boolean notificationsEnabled = true;
    private boolean reportWithScreenshot = false;

    // Геттеры для новых полей
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public boolean isReportWithScreenshot() {
        return reportWithScreenshot;
    }

    // Сеттеры для новых полей (опционально)
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public void setReportWithScreenshot(boolean reportWithScreenshot) {
        this.reportWithScreenshot = reportWithScreenshot;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    // Геттеры и сеттеры
    public boolean isEnableAutoRetry() {
        return enableAutoRetry;
    }

    public void setEnableAutoRetry(boolean enableAutoRetry) {
        this.enableAutoRetry = enableAutoRetry;
    }

    public int getSearchDelayMs() {
        return searchDelayMs;
    }

    public void setSearchDelayMs(int searchDelayMs) {
        this.searchDelayMs = searchDelayMs;
    }

    // Геттеры и сеттеры
    public int getAttemptsAmount() {
        return attemptsAmount;
    }

    public void setAttemptsAmount(int attemptsAmount) {
        this.attemptsAmount = attemptsAmount;
    }

    public boolean isSuccessNotification() {
        return successNotification;
    }

    public void setSuccessNotification(boolean successNotification) {
        this.successNotification = successNotification;
    }

    public boolean isFailureNotification() {
        return failureNotification;
    }

    public void setFailureNotification(boolean failureNotification) {
        this.failureNotification = failureNotification;
    }

    public boolean isReportNotification() {
        return reportNotification;
    }

    public void setReportNotification(boolean reportNotification) {
        this.reportNotification = reportNotification;
    }

    public List<String> getSuccessMessages() {
        return successMessages;
    }

    public void setSuccessMessages(List<String> successMessages) {
        this.successMessages = successMessages;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public void setFailureMessages(List<String> failureMessages) {
        this.failureMessages = failureMessages;
    }

    public List<String> getReportMessages() {
        return reportMessages;
    }

    public void setReportMessages(List<String> reportMessages) {
        this.reportMessages = reportMessages;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getPicsToStartPath() {
        return picsToStartPath;
    }

    public void setPicsToStartPath(String picsToStartPath) {
        this.picsToStartPath = picsToStartPath;
    }

    public String getReadmePath() {
        return readmePath;
    }

    public void setReadmePath(String readmePath) {
        this.readmePath = readmePath;
    }
}