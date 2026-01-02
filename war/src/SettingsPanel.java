import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class SettingsPanel extends JPanel {
    private JButton backButton;
    private JButton exportButton;
    private JButton importButton;
    private SettingsListener listener;
    
    public interface SettingsListener {
        void onBackToMenu();
        void onExportSave();
        void onImportSave();
    }
    
    public SettingsPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 5, 20));
        initializeSettings();
    }
    
    public void setListener(SettingsListener listener) {
        this.listener = listener;
    }
    
    private void initializeSettings() {
        // Заголовок
        JLabel titleLabel = new JLabel("НАСТРОЙКИ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Garamond", Font.BOLD, 36));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // Панель настроек
        JPanel settingsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        settingsPanel.setBackground(new Color(10, 5, 20));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // Кнопка экспорта сохранений
        exportButton = createSettingsButton("📤 ЭКСПОРТ СОХРАНЕНИЙ");
        exportButton.addActionListener(e -> {
            if (listener != null) listener.onExportSave();
        });
        settingsPanel.add(exportButton);
        
        // Кнопка импорта сохранений
        importButton = createSettingsButton("📥 ИМПОРТ СОХРАНЕНИЙ");
        importButton.addActionListener(e -> {
            if (listener != null) listener.onImportSave();
        });
        settingsPanel.add(importButton);
        
        // Сообщение
        JLabel messageLabel = new JLabel("Другие настройки появятся в будущих обновлениях", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Garamond", Font.ITALIC, 16));
        messageLabel.setForeground(new Color(200, 180, 255));
        settingsPanel.add(messageLabel);
        
        add(settingsPanel, BorderLayout.CENTER);
        
        // Кнопка назад с улучшенным стилем
        backButton = new JButton("НАЗАД В МЕНЮ") {
            private boolean isHovered = false;
            
            {
                setFont(new Font("Garamond", Font.BOLD, 18));
                setForeground(Color.WHITE);
                setBackground(new Color(40, 20, 60));
                setFocusPainted(false);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(160, 80, 200), 2),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
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
                
                // Фон
                if (isHovered) {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(70, 35, 105),
                        0, height, new Color(90, 45, 135)
                    );
                    g2d.setPaint(gradient);
                } else {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(50, 25, 75),
                        0, height, new Color(70, 35, 105)
                    );
                    g2d.setPaint(gradient);
                }
                g2d.fillRoundRect(0, 0, width, height, 10, 10);
                
                // Текст
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int x = (width - textWidth) / 2;
                int y = (height - fm.getHeight()) / 2 + fm.getAscent();
                
                // Тень текста
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(text, x + 1, y + 1);
                
                // Основной текст
                if (isHovered) {
                    g2d.setColor(new Color(255, 215, 0));
                } else {
                    g2d.setColor(new Color(200, 180, 255));
                }
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        
        backButton.addActionListener(e -> {
            if (listener != null) listener.onBackToMenu();
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(10, 5, 20));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JButton createSettingsButton(String text) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            
            {
                setFont(new Font("Garamond", Font.BOLD, 16));
                setForeground(Color.WHITE);
                setFocusPainted(false);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(160, 80, 200), 2),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
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
                
                // Фон
                if (isHovered) {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(70, 35, 105),
                        0, height, new Color(90, 45, 135)
                    );
                    g2d.setPaint(gradient);
                } else {
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(50, 25, 75),
                        0, height, new Color(70, 35, 105)
                    );
                    g2d.setPaint(gradient);
                }
                g2d.fillRoundRect(0, 0, width, height, 10, 10);
                
                // Текст
                FontMetrics fm = g2d.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int x = (width - textWidth) / 2;
                int y = (height - fm.getHeight()) / 2 + fm.getAscent();
                
                // Тень текста
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(text, x + 1, y + 1);
                
                // Основной текст
                if (isHovered) {
                    g2d.setColor(new Color(255, 215, 0));
                } else {
                    g2d.setColor(new Color(200, 180, 255));
                }
                g2d.drawString(text, x, y);
                
                g2d.dispose();
            }
        };
        
        return button;
    }
}