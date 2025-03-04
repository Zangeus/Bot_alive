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
import java.util.Objects;
import java.util.List;

import Waiters.Main;

public class ConfigWindow extends JFrame {
    private final LauncherConfig config;

    // Компоненты интерфейса
    private JTextField botTokenField;
    private JTextField chatIdField;
    private JTextField picsPathField;

    private final JTextArea successMessagesArea;
    private final JTextArea failureMessagesArea;
    private final JTextArea reportMessagesArea;
    private JSpinner attemptsSpinner;
    private JCheckBox successCheck;
    private JCheckBox failureCheck;
    private JCheckBox reportCheck;
    private JCheckBox mondayCheck;
    private JSpinner sleepDurationSpinner;

    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font SMALLER_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public ConfigWindow() {
        config = ConfigManager.loadConfig();

        // Инициализируем компоненты перед использованием
        botTokenField = new JTextField();
        chatIdField = new JTextField();
        picsPathField = new JTextField();
        successMessagesArea = new JTextArea();
        failureMessagesArea = new JTextArea();
        reportMessagesArea = new JTextArea();

        initUI();
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            UIManager.put("Button.margin", new Insets(5, 10, 5, 10));

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.font", BASE_FONT);
            UIManager.put("Label.font", BASE_FONT);
            UIManager.put("TextField.font", BASE_FONT);
            UIManager.put("TextArea.font", BASE_FONT);
            UIManager.put("Spinner.font", BASE_FONT);
            UIManager.put("CheckBox.font", BASE_FONT);
            UIManager.put("TabbedPane.font", HEADER_FONT);
        } catch (Exception ignored) {}

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("Настройки");
        setSize(800, 600);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon.png")));
        if (icon.getImage() != null) {
            setIconImage(icon.getImage());
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (!Main.isRunning()) {
                    System.exit(0);
                }
            }
        });
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        tabbedPane.addTab("Основные", createGeneralPanel());
        tabbedPane.addTab("Telegram", createTelegramPanel());
        tabbedPane.addTab("Пути", createPathsPanel());
        tabbedPane.addTab("Сообщения", createMessagesPanel());

        add(tabbedPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        botTokenField.setText(config.getBotToken());
        chatIdField.setText(config.getChatId());
        picsPathField.setText(config.getPicsToStartPath());

        successMessagesArea.setText(String.join("\n", config.getSuccessMessages()));
        failureMessagesArea.setText(String.join("\n", config.getFailureMessages()));
        reportMessagesArea.setText(String.join("\n", config.getReportMessages()));
    }

    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JTabbedPane messagesTabbedPane = new JTabbedPane();
        messagesTabbedPane.setFont(HEADER_FONT.deriveFont(16f));
        messagesTabbedPane.setBackground(new Color(245, 245, 245));

        // Общие настройки для всех подвкладок
        Font areaFont = BASE_FONT.deriveFont(14f);
        Color borderColor = new Color(200, 200, 200);

        // Вкладка "Успех"
        JPanel successPanel = createMessageSubPanel(
                "Успех",
                successMessagesArea,
                config.getSuccessMessages(),
                areaFont,
                borderColor
        );

        // Вкладка "Ошибки"
        JPanel failurePanel = createMessageSubPanel(
                "Ошибки",
                failureMessagesArea,
                config.getFailureMessages(),
                areaFont,
                borderColor
        );

        // Вкладка "Отчеты"
        JPanel reportPanel = createMessageSubPanel(
                "Отчеты",
                reportMessagesArea,
                config.getReportMessages(),
                areaFont,
                borderColor
        );

        messagesTabbedPane.addTab("Успех", successPanel);
        messagesTabbedPane.addTab("Ошибки", failurePanel);
        messagesTabbedPane.addTab("Отчеты", reportPanel);

        panel.add(messagesTabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMessageSubPanel(String title, JTextArea area, List<String> messages,
                                         Font areaFont, Color borderColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel label = new JLabel(title);
        label.setFont(HEADER_FONT.deriveFont(Font.BOLD, 16f));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(label, BorderLayout.NORTH);

        // Текстовая область
        area = new JTextArea(15, 50);
        area.setFont(areaFont);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setText(String.join("\n", messages));

        // Скралл-панель с кастомным оформлением
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(borderColor),
                        "Каждое сообщение с новой строки"
                ),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Подсказка
        area.setToolTipText("<html><body style='width: 300px'>"
                + "Вводите сообщения, каждое с новой строки.<br>"
                + "Пример:<br>"
                + "Операция успешно завершена!<br>"
                + "Все системы работают нормально"
                + "</body></html>");

        panel.add(scrollPane, BorderLayout.CENTER);

        // Нижняя панель с примером
        JPanel examplePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        examplePanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 5));
        JLabel exampleLabel = new JLabel("Пример: \"Операция выполнена успешно в %time%\"");
        exampleLabel.setFont(SMALLER_FONT);
        exampleLabel.setForeground(new Color(100, 100, 100));
        examplePanel.add(exampleLabel);

        panel.add(examplePanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createGeneralPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 20, 5, 5); // Отступы: верх, лево, низ, право
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Инициализация компонентов
        attemptsSpinner = new JSpinner(new SpinnerNumberModel(
                config.getAttemptsAmount(), 1, 100, 1));

        sleepDurationSpinner = new JSpinner(new SpinnerNumberModel(
                config.getSleepDurationMinutes(), 1, 1440, 1));

        // Инициализация чекбоксов
        successCheck = new JCheckBox("Уведомлять об успехе", config.isSuccessNotification());
        failureCheck = new JCheckBox("Уведомлять о неудаче", config.isFailureNotification());
        reportCheck = new JCheckBox("Отправлять отчет", config.isReportNotification());
        mondayCheck = new JCheckBox("Активировать проверки в понедельник", config.isMondayCheckEnabled());

        // Добавление компонентов
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Количество попыток:"), gbc);

        gbc.gridx = 1;
        panel.add(attemptsSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Длительность сна (минут):"), gbc);

        gbc.gridx = 1;
        panel.add(sleepDurationSpinner, gbc);

        // Настройки для чекбоксов
        gbc.gridx = 0;
        gbc.gridwidth = 2; // Занимаем 2 колонки

        gbc.gridy = 2;
        panel.add(successCheck, gbc);

        gbc.gridy = 3;
        panel.add(failureCheck, gbc);

        gbc.gridy = 4;
        panel.add(reportCheck, gbc);

        gbc.gridy = 5;
        panel.add(mondayCheck, gbc);

        // Пустое пространство внизу
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        panel.add(Box.createGlue(), gbc);

        // Общий отступ панели
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        return panel;
    }
    private JPanel createTelegramPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30)); // Отступы вокруг всей панели
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Отступы между элементами
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Заголовок
        JLabel header = new JLabel("Настройки Telegram");
        header.setFont(HEADER_FONT.deriveFont(18f));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(header, gbc);

        // Bot Token
        JLabel tokenLabel = new JLabel("Bot Token:");
        tokenLabel.setFont(BASE_FONT);
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(tokenLabel, gbc);

        botTokenField = new JTextField(config.getBotToken(), 25);
        botTokenField.setFont(BASE_FONT);
        botTokenField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1;
        panel.add(botTokenField, gbc);

        // Chat ID
        JLabel chatIdLabel = new JLabel("Chat ID:");
        chatIdLabel.setFont(BASE_FONT);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(chatIdLabel, gbc);

        chatIdField = new JTextField(config.getChatId(), 25);
        chatIdField.setFont(BASE_FONT);
        chatIdField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1;
        panel.add(chatIdField, gbc);

        // Подсказки
        botTokenField.setToolTipText("<html>Токен вашего Telegram бота<br>Пример: 123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11</html>");
        chatIdField.setToolTipText("<html>ID чата для отправки уведомлений<br>Пример: -1001234567890</html>");

        // Добавим пустое пространство внизу
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JPanel createPathsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Заголовок
        JLabel header = new JLabel("Настройки путей");
        header.setFont(HEADER_FONT.deriveFont(18f));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(header, gbc);

        // Поле пути
        JLabel pathLabel = new JLabel("Путь к изображениям:");
        pathLabel.setFont(BASE_FONT);
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(pathLabel, gbc);

        picsPathField = new JTextField(config.getPicsToStartPath(), 25);
        picsPathField.setFont(BASE_FONT);
        picsPathField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        gbc.gridx = 1;
        panel.add(picsPathField, gbc);

        // Кнопка обзора
        JButton browseBtn = new JButton("Обзор...");
        browseBtn.addActionListener(e -> choosePicsDirectory()); // ВОССТАНАВЛИВАЕМ ОБРАБОТЧИК
        styleButton(browseBtn, new Color(66, 133, 244), Color.BLACK);
        browseBtn.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(browseBtn, gbc);


        // Кнопка README
        JButton openReadmeBtn = new JButton("Открыть руководство");
        openReadmeBtn.addActionListener(e -> openReadme());
        styleButton(openReadmeBtn, new Color(234, 67, 53), Color.BLACK);
        openReadmeBtn.setPreferredSize(new Dimension(180, 30));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(openReadmeBtn, gbc);

        // Разделитель
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(220, 220, 220));
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 0, 10, 0);
        panel.add(separator, gbc);

        // Подсказки
        picsPathField.setToolTipText("<html>Укажите полный путь к папке с изображениями<br>Пример: C:\\Users\\User\\Pictures\\BotImages</html>");
        browseBtn.setToolTipText("Выбрать папку через проводник");
        openReadmeBtn.setToolTipText("Открыть файл с инструкциями в формате PDF");

        // Пустое пространство
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        panel.add(Box.createGlue(), gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

        JButton saveBtn = new JButton("Сохранить");
        JButton cancelBtn = new JButton("Отмена");

        styleButton(saveBtn, new Color(76, 175, 80), Color.BLACK);
        styleButton(cancelBtn, new Color(244, 67, 54), Color.BLACK);

        saveBtn.addActionListener(this::saveConfig);
        cancelBtn.addActionListener(e -> dispose()); // Добавляем обработчик

        panel.add(saveBtn);
        panel.add(cancelBtn);

        return panel;
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setFont(HEADER_FONT);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // Дополнительная страховка - явное указание отступов
        button.setMargin(new Insets(5, 10, 5, 10));
    }

    private void saveConfig(ActionEvent e) {
        // Сохраняем значения напрямую из компонентов
        config.setAttemptsAmount((Integer) attemptsSpinner.getValue());
        config.setSuccessNotification(successCheck.isSelected());
        config.setFailureNotification(failureCheck.isSelected());
        config.setReportNotification(reportCheck.isSelected());
        config.setMondayCheckEnabled(mondayCheck.isSelected());

        config.setBotToken(botTokenField.getText());
        config.setChatId(chatIdField.getText());
        config.setPicsToStartPath(picsPathField.getText());

        config.setSuccessMessages(Arrays.asList(successMessagesArea.getText().split("\n")));
        config.setFailureMessages(Arrays.asList(failureMessagesArea.getText().split("\n")));
        config.setReportMessages(Arrays.asList(reportMessagesArea.getText().split("\n")));
        config.setSleepDurationMinutes((Integer) sleepDurationSpinner.getValue());

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
            writer.write("Основные файлы конфигурации должны находиться в папке `bot_sources`\n");
            writer.write("Полная документация доступна по [ссылке](google.com)");
        }
    }

    public static void showConfigWindow() {
        SwingUtilities.invokeLater(() -> {
            ConfigWindow window = new ConfigWindow();
            window.setVisible(true);
        });
    }

}