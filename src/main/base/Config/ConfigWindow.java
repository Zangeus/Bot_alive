package Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import Waiters.Main;

public class ConfigWindow extends JFrame {
    private final LauncherConfig config;

    // Компоненты интерфейса
    private JSpinner attemptsSpinner;
    private JCheckBox successCheck;
    private JCheckBox failureCheck;
    private JCheckBox reportCheck;
    private JTextField botTokenField;
    private JTextField chatIdField;
    private JTextField picsPathField;
    private JTextArea successMessagesArea;
    private JTextArea failureMessagesArea;
    private JTextArea reportMessagesArea;

    public ConfigWindow() {
        config = ConfigManager.loadConfig();
        initUI();
    }

    private void initUI() {
        setTitle("Настройки приложения");
        setSize(600, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (!Main.isRunning()) {
                    System.exit(0);
                }
            }
        });
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Основные", createGeneralPanel());
        tabbedPane.addTab("Telegram", createTelegramPanel());
        tabbedPane.addTab("Пути", createPathsPanel());
        tabbedPane.addTab("Сообщения", createMessagesPanel()); // Новая вкладка

        add(tabbedPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        JTabbedPane messagesTabbedPane = new JTabbedPane();

        // Success Messages
        JPanel successPanel = new JPanel(new BorderLayout());
        successMessagesArea = new JTextArea(10, 40);
        successMessagesArea.setText(String.join("\n", config.getSuccessMessages()));
        successPanel.add(new JLabel("Сообщения об успехе (каждое с новой строки):"), BorderLayout.NORTH);
        successPanel.add(new JScrollPane(successMessagesArea), BorderLayout.CENTER);

        // Failure Messages
        JPanel failurePanel = new JPanel(new BorderLayout());
        failureMessagesArea = new JTextArea(10, 40);
        failureMessagesArea.setText(String.join("\n", config.getFailureMessages()));
        failurePanel.add(new JLabel("Сообщения об ошибках (каждое с новой строки):"), BorderLayout.NORTH);
        failurePanel.add(new JScrollPane(failureMessagesArea), BorderLayout.CENTER);

        // Report Messages
        JPanel reportPanel = new JPanel(new BorderLayout());
        reportMessagesArea = new JTextArea(10, 40);
        reportMessagesArea.setText(String.join("\n", config.getReportMessages()));
        reportPanel.add(new JLabel("Сообщения для отчетов (каждое с новой строки):"), BorderLayout.NORTH);
        reportPanel.add(new JScrollPane(reportMessagesArea), BorderLayout.CENTER);

        messagesTabbedPane.addTab("Успех", successPanel);
        messagesTabbedPane.addTab("Ошибки", failurePanel);
        messagesTabbedPane.addTab("Отчеты", reportPanel);

        panel.add(messagesTabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

        attemptsSpinner = new JSpinner(
                new SpinnerNumberModel(config.getAttemptsAmount(), 1, 100, 1));

        successCheck = new JCheckBox("Уведомлять об успехе", config.isSuccessNotification());
        failureCheck = new JCheckBox("Уведомлять о неудаче", config.isFailureNotification());
        reportCheck = new JCheckBox("Отправлять отчет", config.isReportNotification());

        panel.add(new JLabel("Количество попыток:"));
        panel.add(attemptsSpinner);
        panel.add(successCheck);
        panel.add(failureCheck);
        panel.add(reportCheck);

        return panel;
    }

    private JPanel createTelegramPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

        botTokenField = new JTextField(config.getBotToken());
        chatIdField = new JTextField(config.getChatId());

        panel.add(new JLabel("Bot Token:"));
        panel.add(botTokenField);
        panel.add(new JLabel("Chat ID:"));
        panel.add(chatIdField);

        return panel;
    }

    private JPanel createPathsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        picsPathField = new JTextField(config.getPicsToStartPath());
        JButton browseBtn = new JButton("Обзор...");
        browseBtn.addActionListener(e -> choosePicsDirectory());

        JButton openReadmeBtn = new JButton("Открыть README");
        openReadmeBtn.addActionListener(e -> openReadme());

        panel.add(new JLabel("Путь к изображениям:"), BorderLayout.NORTH);
        panel.add(picsPathField, BorderLayout.CENTER);
        panel.add(browseBtn, BorderLayout.EAST);
        panel.add(openReadmeBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        JButton saveBtn = new JButton("Сохранить");
        saveBtn.addActionListener(this::saveConfig);
        panel.add(saveBtn);
        return panel;
    }

    private void saveConfig(ActionEvent e) {
        // Сохраняем значения напрямую из компонентов
        config.setAttemptsAmount((Integer) attemptsSpinner.getValue());
        config.setSuccessNotification(successCheck.isSelected());
        config.setFailureNotification(failureCheck.isSelected());
        config.setReportNotification(reportCheck.isSelected());
        config.setBotToken(botTokenField.getText());
        config.setChatId(chatIdField.getText());
        config.setPicsToStartPath(picsPathField.getText());
        config.setSuccessMessages(Arrays.asList(successMessagesArea.getText().split("\n")));
        config.setFailureMessages(Arrays.asList(failureMessagesArea.getText().split("\n")));
        config.setReportMessages(Arrays.asList(reportMessagesArea.getText().split("\n")));

        ConfigManager.saveConfig(config);
        JOptionPane.showMessageDialog(this, "Настройки сохранены!");
        dispose();
    }

    private void choosePicsDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            picsPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void openReadme() {
        try {
            File readmeFile = new File(config.getReadmePath());
            if (!readmeFile.exists()) {
                createDefaultReadme(readmeFile);
            }
            Desktop.getDesktop().open(readmeFile);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при открытии README: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createDefaultReadme(File readmeFile) throws IOException {
        try (FileWriter writer = new FileWriter(readmeFile)) {
            writer.write("# Руководство пользователя\n\n");
            writer.write("Добро пожаловать в приложение!\n");
            writer.write("Здесь будет основная документация...\n");
        }
    }

    public static void showConfigWindow() {
        SwingUtilities.invokeLater(() -> {
            ConfigWindow window = new ConfigWindow();
            window.setVisible(true);
        });
    }
}