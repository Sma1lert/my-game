import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class SplashScreen extends JWindow {
    private double rotationAngle = 0;
    private Timer animationTimer;
    private static final int SIZE = 300;
    private String currentMessage = "Genesis Mundi - Загрузка...";
    private int currentProgress = 0;
    
    public SplashScreen() {
        // Создаем окно без рамки
        setSize(SIZE, SIZE);
        setLocationRelativeTo(null); // Центрируем на экране
        setBackground(new Color(0, 0, 0, 0)); // Прозрачный фон
        
        // Панель для рисования мандалы
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Включение сглаживания
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                
                // Темный фон с градиентом
                GradientPaint bgGradient = new GradientPaint(
                    0, 0, new Color(10, 5, 20),
                    getWidth(), getHeight(), new Color(30, 15, 40)
                );
                g2d.setPaint(bgGradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Сохраняем трансформацию
                AffineTransform originalTransform = g2d.getTransform();
                
                // Применяем вращение
                g2d.rotate(rotationAngle, centerX, centerY);
                
                // Рисуем упрощенную мандалу для прелоадера
                drawSplashMandala(g2d, centerX, centerY, 120);
                
                // Восстанавливаем трансформацию
                g2d.setTransform(originalTransform);
                
                // Текст загрузки
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(currentMessage)) / 2;
                int textY = getHeight() - 50;
                g2d.drawString(currentMessage, textX, textY);
                
                // Прогресс-бар
                int progressWidth = 200;
                int progressHeight = 8;
                int progressX = (getWidth() - progressWidth) / 2;
                int progressY = getHeight() - 30;
                
                // Фон прогресс-бара
                g2d.setColor(new Color(50, 50, 70));
                g2d.fillRect(progressX, progressY, progressWidth, progressHeight);
                
                // Заполненная часть
                int filledWidth = (int)(currentProgress / 100.0 * progressWidth);
                GradientPaint progressGradient = new GradientPaint(
                    progressX, progressY, new Color(100, 65, 165),
                    progressX + filledWidth, progressY, new Color(150, 100, 255)
                );
                g2d.setPaint(progressGradient);
                g2d.fillRect(progressX, progressY, filledWidth, progressHeight);
                
                // Анимация пульсации для незаполненной части (ИСПРАВЛЕНО)
                if (currentProgress < 100) {
                    double pulse = (System.currentTimeMillis() % 1000) / 1000.0;
                    int alpha = (int)(100 + 155 * Math.abs(Math.sin(pulse * Math.PI * 2))); // Используем abs чтобы избежать отрицательных значений
                    alpha = Math.min(255, Math.max(0, alpha)); // Гарантируем, что alpha в пределах 0-255
                    g2d.setColor(new Color(80, 80, 100, alpha));
                    g2d.fillRect(progressX + filledWidth, progressY, progressWidth - filledWidth, progressHeight);
                }
                
                // Рамка прогресс-бара
                g2d.setColor(new Color(150, 150, 180));
                g2d.drawRect(progressX, progressY, progressWidth, progressHeight);
                
                // Процент завершения
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                String percentText = currentProgress + "%";
                int percentX = progressX + progressWidth + 10;
                int percentY = progressY + progressHeight;
                g2d.drawString(percentText, percentX, percentY);
            }
        };
        
        panel.setOpaque(false);
        add(panel);
        
        // Таймер анимации (60 FPS)
        animationTimer = new Timer(16, e -> {
            rotationAngle += 0.04;
            panel.repaint();
        });
    }
    
    private void drawSplashMandala(Graphics2D g2d, int centerX, int centerY, int radius) {
        // Упрощенная версия мандалы для быстрой отрисовки
        
        // 1. Внешний круг
        g2d.setColor(new Color(120, 80, 180, 150));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // 2. Вращающиеся лучи
        g2d.setColor(new Color(200, 180, 255, 100));
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI / 6 * i;
            int x1 = centerX + (int)(Math.cos(angle) * (radius - 20));
            int y1 = centerY + (int)(Math.sin(angle) * (radius - 20));
            int x2 = centerX + (int)(Math.cos(angle) * radius);
            int y2 = centerY + (int)(Math.sin(angle) * radius);
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // 3. Среднее кольцо - готические арки
        g2d.setColor(new Color(160, 120, 220, 120));
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 4 * i;
            int x = centerX + (int)(Math.cos(angle) * (radius - 40));
            int y = centerY + (int)(Math.sin(angle) * (radius - 40));
            
            // Простая готическая арка
            int[] archX = {x - 8, x, x + 8};
            int[] archY = {y, y - 15, y};
            g2d.drawPolyline(archX, archY, 3);
        }
        
        // 4. Внутренний круг с узором
        g2d.setColor(new Color(180, 140, 240, 80));
        g2d.drawOval(centerX - (radius - 80), centerY - (radius - 80), (radius - 80) * 2, (radius - 80) * 2);
        
        // 5. Центральная готическая роза
        drawSimpleGothicRose(g2d, centerX, centerY, radius - 100);
    }
    
    private void drawSimpleGothicRose(Graphics2D g2d, int centerX, int centerY, int size) {
        // Упрощенная готическая роза
        
        // Внешний круг розы
        g2d.setColor(new Color(200, 160, 255));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(centerX - size, centerY - size, size * 2, size * 2);
        
        // Лепестки
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 4 * i;
            int petalX = centerX + (int)(Math.cos(angle) * size);
            int petalY = centerY + (int)(Math.sin(angle) * size);
            
            // Острые лепестки
            int[] petalPointsX = {
                centerX,
                petalX,
                centerX + (int)(Math.cos(angle + Math.PI/8) * size * 0.7)
            };
            int[] petalPointsY = {
                centerY,
                petalY,
                centerY + (int)(Math.sin(angle + Math.PI/8) * size * 0.7)
            };
            g2d.drawPolyline(petalPointsX, petalPointsY, 3);
        }
        
        // Внутренний круг
        g2d.drawOval(centerX - size/2, centerY - size/2, size, size);
        
        // Самый центр - светящаяся точка
        g2d.setColor(new Color(255, 255, 200));
        g2d.fillOval(centerX - size/4, centerY - size/4, size/2, size/2);
        
        // Добавляем простой градиент вручную (ИСПРАВЛЕНО: убрана проблема с альфа-каналом)
        g2d.setColor(new Color(255, 200, 100));
        g2d.fillOval(centerX - size/8, centerY - size/8, size/4, size/4);
    }
    
    public void showSplash() {
        setVisible(true);
        animationTimer.start();
    }
    
    public void hideSplash() {
        animationTimer.stop();
        setVisible(false);
        dispose();
    }
    
    public void updateProgress(String message, int progress) {
        this.currentMessage = message;
        this.currentProgress = Math.min(100, Math.max(0, progress));
        repaint();
    }
    
    // Метод для тестирования
    public static void main(String[] args) {
        SplashScreen splash = new SplashScreen();
        splash.showSplash();
        
        // Имитация прогресса загрузки
        Timer progressTimer = new Timer(100, e -> {
            int progress = splash.currentProgress + 5;
            if (progress <= 100) {
                splash.updateProgress("Загрузка... " + progress + "%", progress);
            } else {
                ((Timer)e.getSource()).stop();
                splash.hideSplash();
                System.exit(0);
            }
        });
        progressTimer.start();
    }
}