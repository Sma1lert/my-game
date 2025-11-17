import javax.swing.*;

public class Main {
    private static SplashScreen splashScreen;
    
    public static void main(String[] args) {
        System.out.println("=== Genesis Mundi ===");
        System.out.println("Запуск игры...");
        
        // Устанавливаем нативный look and feel для лучшего отображения
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("⚠️ Не удалось установить системный стиль: " + e.getMessage());
        }
        
        // Создаем и показываем прелоадер в EDT
        SwingUtilities.invokeLater(() -> {
            splashScreen = new SplashScreen();
            splashScreen.showSplash();
        });
        
        // Имитируем начальную загрузку
        simulateInitialLoading();
        
        // Запускаем игру в отдельном потоке
        new Thread(() -> {
            try {
                // Загрузка основных ресурсов
                loadGameResources();
                
                // Создаем главное окно в EDT
                SwingUtilities.invokeLater(() -> {
                    try {
                        createAndShowGame();
                    } catch (Exception e) {
                        handleGameError(e);
                    }
                });
                
            } catch (Exception e) {
                handleGameError(e);
            }
        }).start();
    }
    
    private static void simulateInitialLoading() {
        try {
            // Имитация начальной загрузки ядра игры
            System.out.println("🔧 Инициализация ядра игры...");
            Thread.sleep(800);
            
            System.out.println("🔧 Проверка системных требований...");
            Thread.sleep(400);
            
            System.out.println("🔧 Загрузка конфигурации...");
            Thread.sleep(600);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void loadGameResources() {
        try {
            updateSplashProgress("🔄 Загрузка текстур...", 20);
            Thread.sleep(800);
            
            updateSplashProgress("🔄 Инициализация мира...", 40);
            Thread.sleep(700);
            
            updateSplashProgress("🔄 Загрузка игровых механик...", 60);
            Thread.sleep(600);
            
            updateSplashProgress("🔄 Подготовка интерфейса...", 80);
            Thread.sleep(500);
            
            updateSplashProgress("🔄 Финальная настройка...", 95);
            Thread.sleep(300);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private static void createAndShowGame() {
        try {
            System.out.println("🎮 Создание игрового окна...");
            
            // Создаем главное окно
            GameWindow gameWindow = new GameWindow();
            
            // Небольшая задержка для плавности перед скрытием прелоадера
            Thread.sleep(500);
            
            // Скрываем прелоадер
            if (splashScreen != null) {
                splashScreen.hideSplash();
            }
            
            System.out.println("✅ Игра успешно запущена!");
            System.out.println("=================================");
            
            // Дополнительная информация о системе
            printSystemInfo();
            
        } catch (Exception e) {
            handleGameError(e);
        }
    }
    
    private static void updateSplashProgress(String message, int progress) {
        System.out.println(message);
        
        // Если бы у нашего SplashScreen были методы для обновления прогресса
        // мы бы вызвали их здесь. Например:
        // splashScreen.updateProgress(message, progress);
    }
    
    private static void handleGameError(Exception e) {
        System.err.println("❌ Критическая ошибка запуска игры: " + e.getMessage());
        e.printStackTrace();
        
        // Скрываем прелоадер при ошибке
        SwingUtilities.invokeLater(() -> {
            if (splashScreen != null) {
                splashScreen.hideSplash();
            }
            
            // Показываем сообщение об ошибке
            JOptionPane.showMessageDialog(null,
                "<html><body style='width: 300px;'>" +
                "<h3>Genesis Mundi - Ошибка запуска</h3>" +
                "<p>Не удалось запустить игру:</p>" +
                "<pre>" + e.getMessage() + "</pre>" +
                "<p>Проверьте что:</p>" +
                "<ul>" +
                "<li>Установлена Java 8 или выше</li>" +
                "<li>Достаточно оперативной памяти</li>" +
                "<li>Файлы игры не повреждены</li>" +
                "</ul>" +
                "</body></html>",
                "Genesis Mundi - Ошибка",
                JOptionPane.ERROR_MESSAGE);
        });
    }
    
    private static void printSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        
        System.out.println("💻 Системная информация:");
        System.out.println("   OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("   Java: " + System.getProperty("java.version"));
        System.out.println("   Память: " + totalMemory + "MB / " + maxMemory + "MB (свободно: " + freeMemory + "MB)");
        System.out.println("   Процессоров: " + runtime.availableProcessors());
        System.out.println("=================================");
    }
    
    // Метод для принудительного закрытия игры (может быть вызван из других классов)
    public static void shutdownGame() {
        System.out.println("🛑 Завершение работы Genesis Mundi...");
        
        // Скрываем прелоадер если он еще visible
        if (splashScreen != null) {
            splashScreen.hideSplash();
        }
        
        // Даем время на завершение операций
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("✅ Игра завершена");
        System.exit(0);
    }
    
    // Метод для перезапуска игры (может быть полезен для смены режимов)
    public static void restartGame() {
        System.out.println("🔄 Перезапуск игры...");
        
        // Скрываем прелоадер
        if (splashScreen != null) {
            splashScreen.hideSplash();
        }
        
        // Даем время на завершение операций
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Перезапускаем JVM (это сложно, поэтому просто выходим)
        System.out.println("⚠️ Для смены режима перезапустите игру вручную");
        System.exit(0);
    }
}