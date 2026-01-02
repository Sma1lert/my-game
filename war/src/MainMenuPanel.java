
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class MainMenuPanel extends JPanel {
    private JButton singlePlayerButton;
    private JButton createMultiplayerButton;
    private JButton joinMultiplayerButton;
    private JButton saveButton;
    private JButton loadButton;
    private JButton settingsButton;
    private JButton exitButton;
    private MainMenuListener listener;
    
    public interface MainMenuListener {
        void onSinglePlayer();
        void onCreateMultiplayer();
        void onJoinMultiplayer();
        void onSaveGame();
        void onLoadGame();
        void onSettings();
        void onExit();
    }
    
    public MainMenuPanel() {
        setLayout(new GridLayout(7, 1, 10, 10));
        setBackground(new Color(10, 5, 20));
        setBorder(BorderFactory.createEmptyBorder(80, 100, 80, 100));
        initializeMenu();
    }
    
    public void setListener(MainMenuListener listener) {
        this.listener = listener;
    }
    
    private void initializeMenu() {
        // Кнопка одиночной игры
        singlePlayerButton = createMenuButton("ОДИНОЧНАЯ ИГРА");
        singlePlayerButton.addActionListener(e -> {
            if (listener != null) listener.onSinglePlayer();
        });
        add(singlePlayerButton);
        
        // Кнопка создания мультиплеерной игры
        createMultiplayerButton = createMenuButton("СОЗДАТЬ ИГРУ");
        createMultiplayerButton.addActionListener(e -> {
            if (listener != null) listener.onCreateMultiplayer();
        });
        add(createMultiplayerButton);
        
        // Кнопка присоединения к игре
        joinMultiplayerButton = createMenuButton("ПРИСОЕДИНИТЬСЯ");
        joinMultiplayerButton.addActionListener(e -> {
            if (listener != null) listener.onJoinMultiplayer();
        });
        add(joinMultiplayerButton);
        
        // Кнопка сохранения
        saveButton = createMenuButton("СОХРАНИТЬ ИГРУ");
        saveButton.addActionListener(e -> {
            if (listener != null) listener.onSaveGame();
        });
        add(saveButton);
        
        // Кнопка загрузки
        loadButton = createMenuButton("ЗАГРУЗИТЬ ИГРУ");
        loadButton.addActionListener(e -> {
            if (listener != null) listener.onLoadGame();
        });
        add(loadButton);
        
        // Кнопка настроек
        settingsButton = createMenuButton("НАСТРОЙКИ");
        settingsButton.addActionListener(e -> {
            if (listener != null) listener.onSettings();
        });
        add(settingsButton);
        
        // Кнопка выхода
        exitButton = createMenuButton("ВЫХОД");
        exitButton.addActionListener(e -> {
            if (listener != null) listener.onExit();
        });
        add(exitButton);
    }
    
    private JButton createMenuButton(String text) {
        GothicMenuButton button = new GothicMenuButton(text);
        button.setFont(new Font("Garamond", Font.BOLD, 20));
        button.setForeground(new Color(255, 255, 255));
        button.setBackground(new Color(40, 20, 60));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(300, 60));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(160, 80, 200), 3),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        return button;
    }
    
    // Стилизованная кнопка для готического меню
    private class GothicMenuButton extends JButton {
        private boolean isHovered = false;
        
        public GothicMenuButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(true);
            setOpaque(false);
            
            // Добавляем обработчики для эффекта наведения
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
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            int width = getWidth();
            int height = getHeight();
            
            // Фон кнопки с градиентом
            if (isHovered) {
                // Эффект при наведении - более яркий градиент
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(80, 40, 120),
                    0, height, new Color(120, 60, 180)
                );
                g2d.setPaint(gradient);
            } else {
                // Обычное состояние
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(50, 25, 75),
                    0, height, new Color(70, 35, 105)
                );
                g2d.setPaint(gradient);
            }
            g2d.fillRoundRect(0, 0, width, height, 15, 15);
            
            // Эффект свечения при наведении
            if (isHovered) {
                g2d.setColor(new Color(200, 100, 255, 80));
                g2d.fillRoundRect(2, 2, width - 4, height - 4, 13, 13);
            }
            
            // Текст с эффектами
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
            g2d.drawRoundRect(0, 0, width - 1, height - 1, 15, 15);
            
            // Внутренняя обводка
            g2d.setColor(new Color(180, 120, 220, 100));
            g2d.drawRoundRect(1, 1, width - 3, height - 3, 13, 13);
            
            g2d.dispose();
        }
        
        @Override
        protected void paintBorder(Graphics g) {
            // Граница рисуется в paintComponent
        }
    }
}
