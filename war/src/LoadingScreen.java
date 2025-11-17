import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoadingScreen extends JWindow {
    private GothicMandalaPanel mandalaPanel;
    private JProgressBar progressBar;
    private Timer progressTimer;
    private int progressValue = 0;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    
    public LoadingScreen() {
        initializeLoadingScreen();
    }
    
    private void initializeLoadingScreen() {
        // Создаем основной контейнер
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(5, 5, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 40, 20));
        
        // Панель с мандалой
        mandalaPanel = new GothicMandalaPanel();
        
        // Заголовок
        titleLabel = new JLabel("GENESIS MUNDI", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Garamond", Font.BOLD, 48));
        titleLabel.setForeground(new Color(200, 180, 255));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        
        // Подзаголовок
        subtitleLabel = new JLabel("Mundi crescit, vita floret", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Garamond", Font.ITALIC, 18));
        subtitleLabel.setForeground(new Color(150, 150, 200));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Прогресс-бар
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.PLAIN, 12));
        progressBar.setForeground(new Color(160, 80, 200));
        progressBar.setBackground(new Color(30, 10, 50));
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 150), 1));
        progressBar.setString("Инициализация мира...");
        
        // Собираем интерфейс
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(mandalaPanel, BorderLayout.CENTER);
        mainPanel.add(subtitleLabel, BorderLayout.CENTER);
        mainPanel.add(progressBar, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        setSize(600, 700);
        setLocationRelativeTo(null); // Центрируем окно
        
        // Настраиваем таймер прогресса
        setupProgressTimer();
    }
    
    private void setupProgressTimer() {
        progressTimer = new Timer(100, new ActionListener() { // Обновление каждые 100мс
            @Override
            public void actionPerformed(ActionEvent e) {
                progressValue += 1; // 100мс * 100 шагов = 10 секунд
                progressBar.setValue(progressValue);
                
                // Меняем текст в зависимости от прогресса
                updateProgressText();
                
                if (progressValue >= 100) {
                    progressTimer.stop();
                    closeLoadingScreen();
                }
            }
        });
    }
    
    private void updateProgressText() {
        String text = "";
        if (progressValue < 20) {
            text = "Инициализация мира... " + progressValue + "%";
        } else if (progressValue < 40) {
            text = "Создание биомов... " + progressValue + "%";
        } else if (progressValue < 60) {
            text = "Генерация ландшафта... " + progressValue + "%";
        } else if (progressValue < 80) {
            text = "Заселение существами... " + progressValue + "%";
        } else {
            text = "Запуск симуляции... " + progressValue + "%";
        }
        progressBar.setString(text);
        
        // Плавное изменение цвета заголовка
        float hue = (float) progressValue / 100f;
        Color titleColor = Color.getHSBColor(0.75f + hue * 0.15f, 0.8f, 0.9f);
        titleLabel.setForeground(titleColor);
    }
    
    public void showLoadingScreen() {
        setVisible(true);
        mandalaPanel.startAnimation();
        progressTimer.start();
    }
    
    private void closeLoadingScreen() {
        mandalaPanel.stopAnimation();
        setVisible(false);
        dispose();
        
        // Запускаем главное меню игры
        SwingUtilities.invokeLater(() -> {
            try {
                new GameWindow();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Ошибка запуска игры: " + ex.getMessage(),
                    "Genesis Mundi - Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    // Метод для быстрого тестирования загрузочного экрана
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoadingScreen loadingScreen = new LoadingScreen();
            loadingScreen.showLoadingScreen();
        });
    }
}