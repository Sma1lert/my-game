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
    private SaveLoadPanel savePanel;
    private SaveLoadPanel loadPanel;
    
    // Добавляем панель паузы
    private PauseMenuPanel pausePanel;
    private boolean isPaused = false;
    
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
        pausePanel = new PauseMenuPanel(); // Новая панель паузы
        
        // Настраиваем слушателей
        setupListeners();
        
        // Добавляем экраны в основную панель
        mainPanel.add(menuPanel, "Menu");
        mainPanel.add(gamePanel, "Game");
        mainPanel.add(settingsPanel, "Settings");
        mainPanel.add(lobbyPanel, "Lobby");
        mainPanel.add(pausePanel, "Pause"); // Добавляем панель паузы
        
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
            public void onSaveGame() {
                showSavePanel();
            }
            
            @Override
            public void onLoadGame() {
                showLoadPanel();
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
            
            @Override
            public void onExportSave() {
                // Реализация экспорта сохранения
                showSavePanel();
            }
            
            @Override
            public void onImportSave() {
                // Реализация импорта сохранения
                showLoadPanel();
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
        
        // Добавляем слушатель для панели паузы
        pausePanel.setListener(new PauseMenuPanel.PauseMenuListener() {
            @Override
            public void onResume() {
                resumeGame();
            }
            
            @Override
            public void onSave() {
                showSavePanelFromPause();
            }
            
            @Override
            public void onLoad() {
                showLoadPanelFromPause();
            }
            
            @Override
            public void onMainMenu() {
                returnToMenuFromPause();
            }
            
            @Override
            public void onExit() {
                exitGameFromPause();
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
    
    // ============ СИСТЕМА ПАУЗЫ ============
    
    public void togglePause() {
        if (!isPaused) {
            pauseGame();
        } else {
            resumeGame();
        }
    }
    
    public void pauseGame() {
        if (!isPaused && isGameRunning()) {
            isPaused = true;
            
            // Останавливаем игровой таймер
            if (gamePanel != null) {
                gamePanel.stopGame();
            }
            
            // Переключаемся на экран паузы
            cardLayout.show(mainPanel, "Pause");
            
            System.out.println("⏸ Игра поставлена на паузу");
        }
    }
    
    public void resumeGame() {
        if (isPaused) {
            isPaused = false;
            
            // Возвращаемся к игре
            cardLayout.show(mainPanel, "Game");
            
            // Перезапускаем игровой таймер
            if (gamePanel != null) {
                gamePanel.startGame();
                gamePanel.requestFocusInWindow(); // Возвращаем фокус на игровую панель
            }
            
            System.out.println("▶ Игра возобновлена");
        }
    }
    
    private void showSavePanelFromPause() {
    // Показываем панель сохранения без снятия паузы
    showSavePanel();
    System.out.println("💾 Переход к сохранению из паузы");
}

private void showLoadPanelFromPause() {
    // Показываем панель загрузки без снятия паузы
    showLoadPanel();
    System.out.println("📂 Переход к загрузке из паузы");
}
    
    private void returnToMenuFromPause() {
        int result = JOptionPane.showConfirmDialog(frame,
            "Вернуться в главное меню?\nТекущий прогресс будет потерян.",
            "Подтверждение",
            JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            isPaused = false;
            showMenu();
        }
    }
    
    private void exitGameFromPause() {
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
    
    private boolean isGameRunning() {
        return frame.isVisible() && cardLayout != null;
    }
    
    // ============ МЕТОД ДЛЯ ВОЗВРАТА В МЕНЮ ИЗ ИГРЫ ============
    
    public void returnToMenu() {
        // Вместо прямого возврата в меню, показываем меню паузы
        pauseGame();
    }
    
    // ============ МЕТОДЫ ДЛЯ ПАНЕЛИ СОХРАНЕНИЯ/ЗАГРУЗКИ ============
    
    public void showSavePanel() {
        // Проверяем, есть ли активная игра
        if (gamePanel == null) {
            JOptionPane.showMessageDialog(frame,
                "Для сохранения необходимо начать игру!",
                "Внимание",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (savePanel == null) {
            savePanel = new SaveLoadPanel(this, true);
            mainPanel.add(savePanel, "Save");
        } else {
            savePanel.refreshSaveList();
        }
        cardLayout.show(mainPanel, "Save");
    }
    
    public void showLoadPanel() {
        if (loadPanel == null) {
            loadPanel = new SaveLoadPanel(this, false);
            mainPanel.add(loadPanel, "Load");
        } else {
            loadPanel.refreshSaveList();
        }
        cardLayout.show(mainPanel, "Load");
    }
    
    public void showGamePanel() {
        cardLayout.show(mainPanel, "Game");
        gamePanel.requestFocusInWindow();
    }
    
    // ============ ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ============
    
    public void setTitle(String title) {
        frame.setTitle(title);
    }
    
    public void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(frame, message, title, messageType);
    }
    
    public GamePanel getGamePanel() {
        return gamePanel;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public void shutdown() {
        if (gamePanel != null) {
            gamePanel.stopGame();
        }
        frame.dispose();
        }


}
