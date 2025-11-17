import javax.swing.*;
import java.awt.*;

public class GameWindow {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private GamePanel gamePanel;
    private MainMenuPanel menuPanel;
    private SettingsPanel settingsPanel;
    private MultiplayerLobbyPanel lobbyPanel;
    
    public GameWindow() {
        initialize();
    }
    
    private void initialize() {
        frame = new JFrame("Genesis Mundi - Living World Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        // Создаем основную панель с CardLayout для переключения между экранами
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Создаем экраны
        menuPanel = new MainMenuPanel();
        gamePanel = new GamePanel(this);
        settingsPanel = new SettingsPanel();
        lobbyPanel = new MultiplayerLobbyPanel();
        
        // Настраиваем слушателей
        setupListeners();
        
        // Добавляем экраны в основную панель
        mainPanel.add(menuPanel, "Menu");
        mainPanel.add(gamePanel, "Game");
        mainPanel.add(settingsPanel, "Settings");
        mainPanel.add(lobbyPanel, "Lobby");
        
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Показываем меню при запуске
        showMenu();
        
        System.out.println("✅ GameWindow инициализирован");
    }
    
    private void setupListeners() {
        menuPanel.setListener(new MainMenuPanel.MainMenuListener() {
            @Override
            public void onSinglePlayer() {
                startSinglePlayerGame();
            }
            
            @Override
            public void onCreateMultiplayer() {
                showMultiplayerLobby(true);
            }
            
            @Override
            public void onJoinMultiplayer() {
                showMultiplayerLobby(false);
            }
            
            @Override
            public void onSettings() {
                showSettings();
            }
            
            @Override
            public void onExit() {
                exitGame();
            }
        });
        
        settingsPanel.setListener(new SettingsPanel.SettingsListener() {
            @Override
            public void onBackToMenu() {
                showMenu();
            }
        });
        
        lobbyPanel.setListener(new MultiplayerLobbyPanel.LobbyListener() {
            @Override
            public void onStartGame() {
                startMultiplayerGame(true, null);
            }
            
            @Override
            public void onJoinGame(String ipAddress) {
                startMultiplayerGame(false, ipAddress);
            }
            
            @Override
            public void onBackToMenu() {
                showMenu();
            }
        });
    }
    
    private void startSinglePlayerGame() {
        System.out.println("🎮 Запуск одиночной игры...");
        
        // Останавливаем предыдущую игру если была
        if (gamePanel != null) {
            gamePanel.stopGame();
        }
        
        // Устанавливаем случайный сид для одиночной игры
        long worldSeed = System.currentTimeMillis();
        gamePanel.setWorldSeed(worldSeed);
        System.out.println("🌍 Установлен сид одиночной игры: " + worldSeed);
        
        cardLayout.show(mainPanel, "Game");
        gamePanel.startGame();
        gamePanel.requestFocusInWindow();
        
        System.out.println("✅ Одиночная игра запущена");
    }
    
    private void showMultiplayerLobby(boolean isHost) {
        lobbyPanel.setHostMode(isHost);
        cardLayout.show(mainPanel, "Lobby");
        
        if (isHost) {
            System.out.println("🎮 Режим создания мультиплеерной игры");
        } else {
            System.out.println("🎮 Режим присоединения к игре");
        }
    }
    
    private void startMultiplayerGame(boolean createGame, String ipAddress) {
        System.out.println("🎮 Запуск мультиплеерной игры: " + (createGame ? "ХОСТ" : "КЛИЕНТ"));
        
        // Останавливаем предыдущую игру если была
        if (gamePanel != null) {
            gamePanel.stopGame();
        }
        
        boolean success;
        if (createGame) {
            // Для хоста: устанавливаем сид и создаем игру
            long worldSeed = System.currentTimeMillis();
            gamePanel.setWorldSeed(worldSeed);
            System.out.println("🌍 Хост установил сид мира: " + worldSeed);
            
            success = gamePanel.startMultiplayerGame(true);
        } else {
            // Для клиента: подключаемся к серверу (сид получим от хоста)
            success = gamePanel.startMultiplayerGame(false, ipAddress);
        }
        
        if (success) {
            cardLayout.show(mainPanel, "Game");
            gamePanel.requestFocusInWindow();
            
            if (createGame) {
                System.out.println("🎮 Создана мультиплеерная игра (Хост)");
            } else {
                System.out.println("🎮 Присоединились к игре: " + ipAddress);
            }
        } else {
            String errorMessage = createGame ? 
                "Не удалось создать мультиплеерную игру!\nПроверьте настройки сети и брандмауэр." :
                "Не удалось подключиться к серверу!\nПроверьте IP адрес и настройки сети.";
                
            JOptionPane.showMessageDialog(frame,
                errorMessage,
                "Ошибка мультиплеера",
                JOptionPane.ERROR_MESSAGE);
            
            // Возвращаемся в лобби при ошибке
            showMultiplayerLobby(createGame);
            
            System.out.println("❌ Ошибка мультиплеера: " + errorMessage);
        }
    }
    
    private void showSettings() {
        cardLayout.show(mainPanel, "Settings");
        System.out.println("⚙️ Открыты настройки");
    }
    
    public void showMenu() {
        cardLayout.show(mainPanel, "Menu");
        if (gamePanel != null) {
            gamePanel.stopGame();
        }
        System.out.println("📋 Открыто главное меню");
    }
    
    private void exitGame() {
        int result = JOptionPane.showConfirmDialog(frame,
            "Вы действительно хотите выйти из игры?",
            "Подтверждение выхода",
            JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            // Корректно останавливаем игру перед выходом
            if (gamePanel != null) {
                gamePanel.stopGame();
            }
            System.out.println("🛑 Выход из игры...");
            System.exit(0);
        }
    }
    
    // Метод для возврата в меню из игры
    public void returnToMenu() {
        int result = JOptionPane.showConfirmDialog(frame,
            "Вернуться в главное меню?\nТекущий прогресс будет потерян.",
            "Подтверждение",
            JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            showMenu();
        }
    }
    
    // Дополнительные методы для управления окном
    public void setTitle(String title) {
        frame.setTitle(title);
    }
    
    public void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(frame, message, title, messageType);
    }
    
    // Метод для получения ссылки на GamePanel (может пригодиться)
    public GamePanel getGamePanel() {
        return gamePanel;
    }
    
    // Метод для принудительного закрытия игры
    public void shutdown() {
        if (gamePanel != null) {
            gamePanel.stopGame();
        }
        frame.dispose();
    }
}

// Класс лобби для мультиплеера
class MultiplayerLobbyPanel extends JPanel {
    private JButton startButton;
    private JButton joinButton;
    private JButton backButton;
    private JTextField ipField;
    private JLabel statusLabel;
    private LobbyListener listener;
    private boolean isHost = false;
    
    public interface LobbyListener {
        void onStartGame();
        void onJoinGame(String ipAddress);
        void onBackToMenu();
    }
    
    public MultiplayerLobbyPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);
        initializeLobby();
    }
    
    public void setListener(LobbyListener listener) {
        this.listener = listener;
    }
    
    public void setHostMode(boolean isHost) {
        this.isHost = isHost;
        updateLobbyDisplay();
    }
    
    private void initializeLobby() {
        // Заголовок
        JLabel titleLabel = new JLabel("МУЛЬТИПЛЕЕР", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // Центральная панель с элементами управления
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.DARK_GRAY);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // Статус лобби
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Поле для ввода IP (для клиента)
        JPanel ipPanel = new JPanel(new FlowLayout());
        ipPanel.setBackground(Color.DARK_GRAY);
        
        JLabel ipLabel = new JLabel("IP адрес сервера:");
        ipLabel.setForeground(Color.WHITE);
        ipLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        ipField = new JTextField("localhost", 15);
        ipField.setFont(new Font("Arial", Font.PLAIN, 14));
        ipField.setMaximumSize(new Dimension(200, 30));
        
        // Кнопка для автоматического определения IP в Radmin VPN
        JButton detectIPButton = new JButton("Авто IP");
        detectIPButton.setFont(new Font("Arial", Font.PLAIN, 12));
        detectIPButton.addActionListener(e -> {
            String radminIP = detectRadminIP();
            if (radminIP != null) {
                ipField.setText(radminIP);
                statusLabel.setText("Найден IP: " + radminIP);
            } else {
                statusLabel.setText("IP не найден. Введите вручную");
            }
        });
        
        ipPanel.add(ipLabel);
        ipPanel.add(ipField);
        ipPanel.add(detectIPButton);
        ipPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Кнопка запуска/присоединения
        startButton = new JButton();
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.setPreferredSize(new Dimension(200, 40));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(e -> {
            if (isHost) {
                if (listener != null) {
                    System.out.println("🎮 Создание мультиплеерной игры...");
                    listener.onStartGame();
                }
            } else {
                String ip = ipField.getText().trim();
                if (!ip.isEmpty()) {
                    if (listener != null) {
                        System.out.println("🎮 Присоединение к игре: " + ip);
                        listener.onJoinGame(ip);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Введите IP адрес сервера!", 
                        "Ошибка", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        // Информационная панель
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.DARK_GRAY);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel infoLabel1 = new JLabel("Инструкция по мультиплееру:");
        infoLabel1.setFont(new Font("Arial", Font.BOLD, 14));
        infoLabel1.setForeground(Color.CYAN);
        infoLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel infoLabel2 = new JLabel("• Хост создает игру и становится сервером");
        JLabel infoLabel3 = new JLabel("• Клиенты подключаются по IP хоста");
        JLabel infoLabel4 = new JLabel("• Все игроки видят одинаковый мир");
        JLabel infoLabel5 = new JLabel("• Игроки появляются рядом друг с другом");
        JLabel infoLabel6 = new JLabel("• Порт: 27333");
        JLabel infoLabel7 = new JLabel("• Для Radmin VPN используйте 'Авто IP'");
        
        for (JLabel label : new JLabel[]{infoLabel2, infoLabel3, infoLabel4, infoLabel5, infoLabel6, infoLabel7}) {
            label.setFont(new Font("Arial", Font.PLAIN, 12));
            label.setForeground(Color.LIGHT_GRAY);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        
        infoPanel.add(infoLabel1);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(infoLabel2);
        infoPanel.add(infoLabel3);
        infoPanel.add(infoLabel4);
        infoPanel.add(infoLabel5);
        infoPanel.add(infoLabel6);
        infoPanel.add(infoLabel7);
        
        // Сборка центральной панели
        centerPanel.add(statusLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        
        if (!isHost) {
            centerPanel.add(ipPanel);
            centerPanel.add(Box.createVerticalStrut(10));
        }
        
        centerPanel.add(startButton);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(infoPanel);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Панель кнопки назад
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.DARK_GRAY);
        
        backButton = new JButton("НАЗАД В МЕНЮ");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.addActionListener(e -> {
            if (listener != null) listener.onBackToMenu();
        });
        
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        updateLobbyDisplay();
    }
    
    private void updateLobbyDisplay() {
        if (isHost) {
            statusLabel.setText("Режим: СОЗДАНИЕ ИГРЫ");
            startButton.setText("СОЗДАТЬ ИГРУ");
            ipField.setVisible(false);
        } else {
            statusLabel.setText("Режим: ПРИСОЕДИНЕНИЕ");
            startButton.setText("ПРИСОЕДИНИТЬСЯ");
            ipField.setVisible(true);
        }
        
        // Обновляем отображение
        revalidate();
        repaint();
    }
    
    // Метод для автоматического определения Radmin IP
    private String detectRadminIP() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                if (iface.isUp() && !iface.isLoopback()) {
                    java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        java.net.InetAddress addr = addresses.nextElement();
                        if (addr instanceof java.net.Inet4Address) {
                            String ip = addr.getHostAddress();
                            // Radmin VPN обычно использует диапазон 26.x.x.x или 25.x.x.x
                            if (ip.startsWith("26.") || ip.startsWith("25.")) {
                                System.out.println("🔍 Найден Radmin VPN IP: " + ip);
                                return ip;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка определения IP: " + e.getMessage());
        }
        System.out.println("❌ Radmin VPN IP не найден");
        return null;
    }
}