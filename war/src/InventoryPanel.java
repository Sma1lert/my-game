import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class InventoryPanel extends JPanel {
    public int[][] inventory;
    private int selectedSlot = 0;
    
    public InventoryPanel() {
        setPreferredSize(new Dimension(
            GameConstants.INVENTORY_COLS * GameConstants.INVENTORY_SLOT_SIZE,
            GameConstants.INVENTORY_ROWS * GameConstants.INVENTORY_SLOT_SIZE + 30
        ));
        setBackground(new Color(30, 30, 30));
        setOpaque(true);
        
        initializeInventory();
    }
    
    private void initializeInventory() {
        inventory = new int[GameConstants.INVENTORY_ROWS][GameConstants.INVENTORY_COLS];
        
        // Только меч в первом слоте
        inventory[0][0] = GameConstants.ITEM_SWORD;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawInventory(g);
    }
    
    private void drawInventory(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        int startX = 10;
        int startY = 10;
        
        // Заголовок
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("ИНВЕНТАРЬ", startX, startY);
        startY += 25;
        
        // Рисуем все слоты инвентаря
        for (int row = 0; row < GameConstants.INVENTORY_ROWS; row++) {
            for (int col = 0; col < GameConstants.INVENTORY_COLS; col++) {
                drawSlot(g2d, startX, startY, row, col);
            }
        }
    }
    
    private void drawSlot(Graphics2D g2d, int startX, int startY, int row, int col) {
        int slotX = startX + col * GameConstants.INVENTORY_SLOT_SIZE;
        int slotY = startY + row * GameConstants.INVENTORY_SLOT_SIZE;
        int slotIndex = row * GameConstants.INVENTORY_COLS + col;
        
        // Фон слота
        g2d.setColor(new Color(100, 100, 100));
        g2d.fillRect(slotX, slotY, GameConstants.INVENTORY_SLOT_SIZE, GameConstants.INVENTORY_SLOT_SIZE);
        
        // Рамка слота
        if (slotIndex == selectedSlot) {
            g2d.setColor(Color.YELLOW);
        } else {
            g2d.setColor(Color.WHITE);
        }
        g2d.drawRect(slotX, slotY, GameConstants.INVENTORY_SLOT_SIZE, GameConstants.INVENTORY_SLOT_SIZE);
        
        // Рисуем предмет если есть
        int itemId = inventory[row][col];
        if (itemId > 0 && itemId <= GameConstants.ITEM_NAMES.length) {
            drawItemInSlot(g2d, slotX, slotY, itemId);
        }
        
        // Номер слота
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.drawString(String.valueOf(slotIndex + 1), slotX + 3, slotY + 12);
    }
    
    private void drawItemInSlot(Graphics2D g2d, int slotX, int slotY, int itemId) {
        String textureName = GameConstants.getTextureName(itemId);
        String itemName = GameConstants.ITEM_NAMES[itemId - 1];
        
        // Пробуем нарисовать текстуру
        if (textureName != null) {
            try {
                BufferedImage texture = TextureManager.getInstance().getTexture(textureName);
                if (texture != null) {
                    int textureSize = GameConstants.INVENTORY_SLOT_SIZE - 8;
                    int textureX = slotX + (GameConstants.INVENTORY_SLOT_SIZE - textureSize) / 2;
                    int textureY = slotY + (GameConstants.INVENTORY_SLOT_SIZE - textureSize) / 2;
                    
                    g2d.drawImage(texture, textureX, textureY, textureSize, textureSize, null);
                    
                    // Подписываем название
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 9));
                    g2d.drawString(itemName, slotX + 2, slotY + GameConstants.INVENTORY_SLOT_SIZE - 2);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Ошибка рисования текстуры: " + e.getMessage());
            }
        }
        
        // Если текстура не загрузилась, рисуем цветной квадрат с названием
        g2d.setColor(Color.BLUE);
        int itemSize = GameConstants.INVENTORY_SLOT_SIZE - 8;
        int itemX = slotX + (GameConstants.INVENTORY_SLOT_SIZE - itemSize) / 2;
        int itemY = slotY + (GameConstants.INVENTORY_SLOT_SIZE - itemSize) / 2;
        g2d.fillRect(itemX, itemY, itemSize, itemSize);
        
        // Название предмета
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 9));
        g2d.drawString(itemName, slotX + 5, slotY + GameConstants.INVENTORY_SLOT_SIZE - 5);
    }
    
    public void setSelectedSlot(int slot) {
        this.selectedSlot = slot;
        repaint();
    }
    
    public int getSelectedSlot() {
        return selectedSlot;
    }
    
    public void addItem(int itemId) {
        for (int row = 0; row < GameConstants.INVENTORY_ROWS; row++) {
            for (int col = 0; col < GameConstants.INVENTORY_COLS; col++) {
                if (inventory[row][col] == 0) {
                    inventory[row][col] = itemId;
                    repaint();
                    return;
                }
            }
        }
    }
    
    public String getSelectedItemName() {
        int row = selectedSlot / GameConstants.INVENTORY_COLS;
        int col = selectedSlot % GameConstants.INVENTORY_COLS;
        int itemId = inventory[row][col];
        
        if (itemId > 0 && itemId <= GameConstants.ITEM_NAMES.length) {
            return GameConstants.ITEM_NAMES[itemId - 1];
        }
        return "Пусто";
    }
}