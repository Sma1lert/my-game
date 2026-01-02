import javax.swing.*;
import java.awt.*;

public class MultiplayerLobbyPanel extends JPanel {
    private JButton hostButton;
    private JButton joinButton;
    private JButton backButton;
    private JTextField ipField;
    private LobbyListener listener;
    private boolean isHostMode = false;
    
    public interface LobbyListener {
        void onStartGame();
        void onJoinGame(String ipAddress);
        void onBackToMenu();
    }
    
    public MultiplayerLobbyPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 5, 20));
        initializeLobby();
    }
    
    public void setListener(LobbyListener listener) {
        this.listener = listener;
    }
    
    public void setHostMode(boolean isHost) {
        this.isHostMode = isHost;
        updateUIForMode();
    }
    
    private void initializeLobby() {
        // Заголовок
        JLabel titleLabel = new JLabel("МУЛЬТИПЛЕЕР", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Garamond", Font.BOLD, 36));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // Панель управления
        JPanel controlPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        controlPanel.setBackground(new Color(10, 5, 20));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));
        
        // Кнопка создания игры
        hostButton = createLobbyButton("🎮 СОЗДАТЬ ИГРУ");
        hostButton.addActionListener(e -> {
            if (listener != null) listener.onStartGame();
        });
        controlPanel.add(hostButton);
        
        // Поле для ввода IP
        JPanel ipPanel = new JPanel(new FlowLayout());
        ipPanel.setBackground(new Color(10, 5, 20));
        
        JLabel ipLabel = new JLabel("IP адрес:");
        ipLabel.setFont(new Font("Garamond", Font.PLAIN, 16));
        ipLabel.setForeground(Color.WHITE);
        ipPanel.add(ipLabel);
        
        ipField = new JTextField("localhost", 15);
        ipField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        ipField.setBackground(new Color(30, 15, 40));
        ipField.setForeground(Color.WHITE);
        ipField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 150), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        ipPanel.add(ipField);
        
        // Кнопка присоединения
        joinButton = createLobbyButton("🔗 ПРИСОЕДИНИТЬСЯ");
        joinButton.addActionListener(e -> {
            if (listener != null && !ipField.getText().trim().isEmpty()) {
                listener.onJoinGame(ipField.getText().trim());
            }
        });
        ipPanel.add(joinButton);
        
        controlPanel.add(ipPanel);
        
        // Информация для хоста
        JLabel infoLabel = new JLabel(
            "<html><div style='text-align: center; color: #AAAAFF;'>" +
            "Для создания игры нажмите 'СОЗДАТЬ ИГРУ'<br>" +
            "IP адрес будет показан в консоли<br>" +
            "Для присоединения введите IP хоста и нажмите 'ПРИСОЕДИНИТЬСЯ'" +
            "</div></html>", 
            SwingConstants.CENTER
        );
        infoLabel.setFont(new Font("Garamond", Font.PLAIN, 14));
        controlPanel.add(infoLabel);
        
        add(controlPanel, BorderLayout.CENTER);
        
        // Кнопка назад
        backButton = createLobbyButton("⬅ НАЗАД В МЕНЮ");
        backButton.addActionListener(e -> {
            if (listener != null) listener.onBackToMenu();
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(10, 5, 20));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        updateUIForMode();
    }
    
    private JButton createLobbyButton(String text) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;
            
            {
                setFont(new Font("Garamond", Font.BOLD, 18));
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
    
    private void updateUIForMode() {
        if (isHostMode) {
            hostButton.setText("🎮 ЗАПУСТИТЬ ИГРУ");
            joinButton.setEnabled(false);
            ipField.setEnabled(false);
        } else {
            hostButton.setText("🎮 СОЗДАТЬ ИГРУ");
            joinButton.setEnabled(true);
            ipField.setEnabled(true);
        }
    }
}