import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class SaveLoadPanel extends JPanel {
    private GameWindow gameWindow;
    private boolean isSaveMode;
    private JList<String> saveList;
    private DefaultListModel<String> listModel;
    private JButton actionButton;
    private JButton deleteButton;
    private JButton backButton;
    private JTextField saveNameField;
    
    public SaveLoadPanel(GameWindow gameWindow, boolean isSaveMode) {
        this.gameWindow = gameWindow;
        this.isSaveMode = isSaveMode;
        
        setLayout(new BorderLayout());
        setBackground(new Color(10, 5, 20));
        initializePanel();
        refreshSaveList();
    }
    
    private void initializePanel() {
        // Заголовок
        String title = isSaveMode ? "СОХРАНЕНИЕ ИГРЫ" : "ЗАГРУЗКА ИГРЫ";
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Garamond", Font.BOLD, 36));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // Основная панель
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(10, 5, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        
        // Список сохранений
        listModel = new DefaultListModel<>();
        saveList = new JList<>(listModel);
        saveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        saveList.setBackground(new Color(30, 15, 40));
        saveList.setForeground(Color.WHITE);
        saveList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        saveList.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 150), 1));
        
        JScrollPane scrollPane = new JScrollPane(saveList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(160, 80, 200), 2),
            "Список сохранений",
            0, 0,
            new Font("Garamond", Font.BOLD, 16),
            new Color(200, 180, 255)
        ));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Панель управления
        JPanel controlPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        controlPanel.setBackground(new Color(10, 5, 20));
        
        if (isSaveMode) {
            // Поле для ввода имени сохранения
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            namePanel.setBackground(new Color(10, 5, 20));
            
            JLabel nameLabel = new JLabel("Имя сохранения:");
            nameLabel.setFont(new Font("Garamond", Font.PLAIN, 16));
            nameLabel.setForeground(Color.WHITE);
            namePanel.add(nameLabel);
            
            saveNameField = new JTextField(20);
            saveNameField.setFont(new Font("Monospaced", Font.PLAIN, 14));
            saveNameField.setBackground(new Color(30, 15, 40));
            saveNameField.setForeground(Color.WHITE);
            saveNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 150), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            namePanel.add(saveNameField);
            
            controlPanel.add(namePanel);
        }
        
        // Кнопка действия (Сохранить/Загрузить)
        actionButton = createControlButton(isSaveMode ? "💾 СОХРАНИТЬ" : "📂 ЗАГРУЗИТЬ");
        actionButton.addActionListener(e -> performAction());
        controlPanel.add(actionButton);
        
        // Кнопка удаления
        deleteButton = createControlButton("🗑️ УДАЛИТЬ");
        deleteButton.addActionListener(e -> deleteSelectedSave());
        controlPanel.add(deleteButton);
        
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
        
        // Кнопка назад
        backButton = createControlButton("⬅ НАЗАД");
        backButton.addActionListener(e -> {
            if (isSaveMode) {
                gameWindow.showMenu();
            } else {
                gameWindow.showGamePanel();
            }
        });
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(10, 5, 20));
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JButton createControlButton(String text) {
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
    
    public void refreshSaveList() {
        listModel.clear();
        GamePanel gamePanel = gameWindow.getGamePanel();
        if (gamePanel != null) {
            SaveLoadManager saveManager = gamePanel.getSaveManager();
            if (saveManager != null) {
                List<String> saves = saveManager.getSaveList();
                for (String save : saves) {
                    listModel.addElement(save);
                }
            } else {
                System.out.println("❌ SaveManager не инициализирован");
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Нет активной игры для сохранения/загрузки", 
                "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performAction() {
        if (isSaveMode) {
            saveGame();
        } else {
            loadGame();
        }
    }
    
    private void saveGame() {
        String saveName = saveNameField.getText().trim();
        if (saveName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Введите имя сохранения", 
                "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        GamePanel gamePanel = gameWindow.getGamePanel();
        if (gamePanel != null) {
            boolean success = gamePanel.saveGame(saveName);
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Игра успешно сохранена: " + saveName, 
                    "Успех", 
                    JOptionPane.INFORMATION_MESSAGE);
                refreshSaveList();
                saveNameField.setText("");
            }
        }
    }
    
    private void loadGame() {
        String selectedSave = saveList.getSelectedValue();
        if (selectedSave == null) {
            JOptionPane.showMessageDialog(this, 
                "Выберите сохранение для загрузки", 
                "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        GamePanel gamePanel = gameWindow.getGamePanel();
        if (gamePanel != null) {
            boolean success = gamePanel.loadGame(selectedSave);
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Игра успешно загружена: " + selectedSave, 
                    "Успех", 
                    JOptionPane.INFORMATION_MESSAGE);
                gameWindow.showGamePanel();
            }
        }
    }
    
    private void deleteSelectedSave() {
        String selectedSave = saveList.getSelectedValue();
        if (selectedSave == null) {
            JOptionPane.showMessageDialog(this, 
                "Выберите сохранение для удаления", 
                "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Вы действительно хотите удалить сохранение: " + selectedSave + "?",
            "Подтверждение удаления",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            GamePanel gamePanel = gameWindow.getGamePanel();
            if (gamePanel != null) {
                SaveLoadManager saveManager = gamePanel.getSaveManager();
                if (saveManager != null) {
                    boolean success = saveManager.deleteSave(selectedSave);
                    if (success) {
                        JOptionPane.showMessageDialog(this, 
                            "Сохранение удалено: " + selectedSave, 
                            "Успех", 
                            JOptionPane.INFORMATION_MESSAGE);
                        refreshSaveList();
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Ошибка удаления сохранения", 
                            "Ошибка", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
}