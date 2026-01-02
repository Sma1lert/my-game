import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PauseMenuPanel extends JPanel {
    private JButton resumeButton;
    private JButton saveButton;
    private JButton loadButton;
    private JButton mainMenuButton;
    private JButton exitButton;
    private PauseMenuListener listener;
    
    public interface PauseMenuListener {
        void onResume();
        void onSave();
        void onLoad();
        void onMainMenu();
        void onExit();
    }
    
    public PauseMenuPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 5, 20, 230)); // Полупрозрачный темный фон
        setOpaque(true);
        initializeMenu();
    }
    
    public void setListener(PauseMenuListener listener) {
        this.listener = listener;
    }
    
    private void initializeMenu() {
        // Заголовок
        JLabel titleLabel = new JLabel("ПАУЗА", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Garamond", Font.BOLD, 48));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // Панель кнопок
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.setBackground(new Color(0, 0, 0, 0)); // Прозрачный
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 0, 100));
        
        // Кнопка "Продолжить"
        resumeButton = createPauseButton("▶ ПРОДОЛЖИТЬ");
        resumeButton.addActionListener(e -> {
            if (listener != null) listener.onResume();
        });
        buttonPanel.add(resumeButton);
        
        // Кнопка "Сохранить"
        saveButton = createPauseButton("💾 СОХРАНИТЬ");
        saveButton.addActionListener(e -> {
            if (listener != null) listener.onSave();
        });
        buttonPanel.add(saveButton);
        
        // Кнопка "Загрузить"
        loadButton = createPauseButton("📂 ЗАГРУЗИТЬ");
        loadButton.addActionListener(e -> {
            if (listener != null) listener.onLoad();
        });
        buttonPanel.add(loadButton);
        
        // Кнопка "Главное меню"
        mainMenuButton = createPauseButton("🏠 ГЛАВНОЕ МЕНЮ");
        mainMenuButton.addActionListener(e -> {
            if (listener != null) listener.onMainMenu();
        });
        buttonPanel.add(mainMenuButton);
        
        // Кнопка "Выход"
        exitButton = createPauseButton("🚪 ВЫХОД");
        exitButton.addActionListener(e -> {
            if (listener != null) listener.onExit();
        });
        buttonPanel.add(exitButton);
        
        add(buttonPanel, BorderLayout.CENTER);
        
        // Подсказка внизу
        JLabel hintLabel = new JLabel("ESC - скрыть меню", SwingConstants.CENTER);
        hintLabel.setFont(new Font("Garamond", Font.ITALIC, 14));
        hintLabel.setForeground(new Color(180, 180, 220));
        hintLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(hintLabel, BorderLayout.SOUTH);
    }
    
    private JButton createPauseButton(String text) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            
            {
                setFont(new Font("Garamond", Font.BOLD, 20));
                setForeground(Color.WHITE);
                setFocusPainted(false);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(160, 80, 200), 3),
                    BorderFactory.createEmptyBorder(12, 25, 12, 25)
                ));
                setContentAreaFilled(false);
                
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        isHovered = true;
                        repaint();
                    }
                    
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        isHovered = false;
                        repaint();
                    }
                });
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth();
                int height = getHeight();
                
                // Фон кнопки
                if (isHovered) {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(80, 40, 120, 200),
                        0, height, new Color(120, 60, 180, 200)
                    );
                    g2d.setPaint(gradient);
                } else {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(50, 25, 75, 180),
                        0, height, new Color(70, 35, 105, 180)
                    );
                    g2d.setPaint(gradient);
                }
                g2d.fillRoundRect(0, 0, width, height, 20, 20);
                
                // Эффект свечения при наведении
                if (isHovered) {
                    g2d.setColor(new Color(200, 100, 255, 80));
                    g2d.fillRoundRect(2, 2, width - 4, height - 4, 18, 18);
                }
                
                // Текст
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int x = (width - textWidth) / 2;
                int y = (height - fm.getHeight()) / 2 + fm.getAscent();
                
                // Тень текста
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.drawString(text, x + 2, y + 2);
                
                // Основной текст с градиентом
                if (isHovered) {
                    GradientPaint textGradient = new GradientPaint(
                        x, y - 10, new Color(255, 255, 200),
                        x, y + 10, new Color(255, 215, 0)
                    );
                    g2d.setPaint(textGradient);
                } else {
                    GradientPaint textGradient = new GradientPaint(
                        x, y - 10, new Color(255, 255, 255),
                        x, y + 10, new Color(200, 180, 255)
                    );
                    g2d.setPaint(textGradient);
                }
                g2d.drawString(text, x, y);
                
                // Внешняя обводка
                g2d.setColor(new Color(120, 60, 180));
                g2d.drawRoundRect(0, 0, width - 1, height - 1, 20, 20);
                
                // Внутренняя обводка
                g2d.setColor(new Color(180, 120, 220, 100));
                g2d.drawRoundRect(1, 1, width - 3, height - 3, 18, 18);
                
                g2d.dispose();
            }
        };
        
        return button;
    }
}