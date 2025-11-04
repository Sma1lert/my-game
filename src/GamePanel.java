import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class GamePanel extends JPanel implements KeyListener {
    private char[][] map;
    private Player player;
    
    private int cameraX;
    private int cameraY;
    private int visibleTilesX;
    private int visibleTilesY;
    
    private InventoryPanel inventoryPanel;
    private boolean inventoryVisible = false;
    private Timer gameTimer;
    
    public GamePanel() {
        setPreferredSize(new Dimension(
            GameConstants.SCREEN_WIDTH,
            GameConstants.SCREEN_HEIGHT
        ));
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        
        initializeGame();
    }
    
    private void initializeGame() {
        visibleTilesX = GameConstants.SCREEN_WIDTH / GameConstants.TILE_SIZE;
        visibleTilesY = (GameConstants.SCREEN_HEIGHT - 100) / GameConstants.TILE_SIZE;
        
        map = new char[GameConstants.MAP_HEIGHT][GameConstants.MAP_WIDTH];
        generateWorld();
        
        player = new Player(GameConstants.MAP_WIDTH / 2, GameConstants.MAP_HEIGHT / 2);
        centerCameraOnPlayer();
        
        // Инициализируем менеджер текстур
        TextureManager textureManager = TextureManager.getInstance();
        System.out.println("✅ TextureManager инициализирован");
        
        inventoryPanel = new InventoryPanel();
        System.out.println("✅ InventoryPanel инициализирован");
    }
    
    private void generateWorld() {
        for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConstants.MAP_WIDTH; x++) {
                double chance = Math.random();
                if (chance < 0.7) {
                    map[y][x] = GameConstants.GRASS; // 70% трава
                } else if (chance < 0.85) {
                    map[y][x] = GameConstants.TREE;  // 15% деревья
                } else {
                    map[y][x] = GameConstants.WATER; // 15% вода
                }
            }
        }
    }
    
    private void centerCameraOnPlayer() {
        cameraX = player.getX() - visibleTilesX / 2;
        cameraY = player.getY() - visibleTilesY / 2;
        cameraX = Math.max(0, Math.min(cameraX, GameConstants.MAP_WIDTH - visibleTilesX));
        cameraY = Math.max(0, Math.min(cameraY, GameConstants.MAP_HEIGHT - visibleTilesY));
    }
    
    public void startGame() {
        gameTimer = new Timer(100, e -> {
            player.update();
            repaint();
        });
        gameTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        
        if (inventoryVisible) {
            drawInventoryScreen(g);
        } else {
            drawMap(g);
            drawStatusPanel(g);
        }
    }
    
    private void drawMap(Graphics g) {
        for (int screenY = 0; screenY < visibleTilesY; screenY++) {
            int worldY = cameraY + screenY;
            if (worldY < 0 || worldY >= GameConstants.MAP_HEIGHT) continue;
            
            for (int screenX = 0; screenX < visibleTilesX; screenX++) {
                int worldX = cameraX + screenX;
                if (worldX < 0 || worldX >= GameConstants.MAP_WIDTH) continue;
                
                int pixelX = screenX * GameConstants.TILE_SIZE;
                int pixelY = screenY * GameConstants.TILE_SIZE;
                
                char terrain = map[worldY][worldX];
                
                // Пропускаем отрисовку если это позиция игрока (игрок рисуется отдельно)
                if (worldX == player.getX() && worldY == player.getY()) {
                    continue;
                }
                
                // Рисуем текстуру terrain
                drawTerrain(g, pixelX, pixelY, terrain);
            }
        }
        
        // Рисуем персонажа поверх карты
        drawPlayer(g);
    }
    
    private void drawTerrain(Graphics g, int x, int y, char terrain) {
        String textureName = GameConstants.getTerrainTexture(terrain);
        
        if (textureName != null) {
            try {
                BufferedImage texture = TextureManager.getInstance().getTexture(textureName);
                if (texture != null) {
                    g.drawImage(texture, x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, null);
                    return;
                }
            } catch (Exception e) {
                System.out.println("Ошибка рисования текстуры " + textureName + ": " + e.getMessage());
            }
        }
        
        // Если текстура не загрузилась, рисуем символ
        Color color = Color.WHITE;
        switch (terrain) {
            case GameConstants.GRASS: color = Color.GREEN; break;
            case GameConstants.TREE: color = new Color(101, 67, 33); break;
            case GameConstants.WATER: color = Color.BLUE; break;
        }
        
        drawTerrainSymbol(g, x, y, terrain, color);
    }
    
    private void drawTerrainSymbol(Graphics g, int x, int y, char symbol, Color color) {
        g.setColor(color);
        g.setFont(new Font("Monospaced", Font.PLAIN, GameConstants.TILE_SIZE - 2));
        g.drawString(String.valueOf(symbol), x, y + GameConstants.TILE_SIZE - 2);
    }
    
    private void drawPlayer(Graphics g) {
        int playerScreenX = (player.getX() - cameraX) * GameConstants.TILE_SIZE;
        int playerScreenY = (player.getY() - cameraY) * GameConstants.TILE_SIZE;
        
        // Получаем текстуру персонажа в зависимости от направления
        String playerTextureName = getPlayerTextureByDirection();
        
        try {
            BufferedImage playerTexture = TextureManager.getInstance().getTexture(playerTextureName);
            if (playerTexture != null) {
                g.drawImage(playerTexture, playerScreenX, playerScreenY, 
                           GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, null);
            } else {
                // Если текстура не загрузилась, рисуем символ
                drawTerrainSymbol(g, playerScreenX, playerScreenY, GameConstants.PLAYER, Color.YELLOW);
            }
        } catch (Exception e) {
            // В случае ошибки рисуем символ
            drawTerrainSymbol(g, playerScreenX, playerScreenY, GameConstants.PLAYER, Color.YELLOW);
        }
    }
    
    private String getPlayerTextureByDirection() {
        switch (player.getDirection()) {
            case GameConstants.DIRECTION_UP:
                return GameConstants.PLAYER_TEXTURE_UP;
            case GameConstants.DIRECTION_LEFT:
                return GameConstants.PLAYER_TEXTURE_LEFT;
            case GameConstants.DIRECTION_RIGHT:
                return GameConstants.PLAYER_TEXTURE_RIGHT;
            case GameConstants.DIRECTION_DOWN:
            default:
                return GameConstants.PLAYER_TEXTURE_DOWN;
        }
    }
    
    private void drawStatusPanel(Graphics g) {
        int panelY = GameConstants.SCREEN_HEIGHT - 100;
        
        // Фон панели статуса
        g.setColor(new Color(30, 30, 30, 200));
        g.fillRect(0, panelY, GameConstants.SCREEN_WIDTH, 100);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        // Здоровье и голод
        drawHealthBar(g, 20, panelY + 20);
        drawHungerBar(g, 20, panelY + 45);
        
        // Позиция и уровень
        g.drawString("Позиция: X=" + player.getX() + " Y=" + player.getY(), 200, panelY + 25);
        g.drawString("Уровень: " + player.getLevel(), 200, panelY + 45);
        g.drawString("Опыт: " + player.getExperience(), 200, panelY + 65);
        
        // Выбранный предмет с текстурой
        drawSelectedItem(g, 400, panelY + 20);
        
        // Управление
        g.drawString("WASD - движение", 400, panelY + 65);
        g.drawString("E - инвентарь", 550, panelY + 65);
        
        // Горячая панель
        drawHotbar(g);
    }
    
    private void drawHealthBar(Graphics g, int x, int y) {
        g.setColor(Color.WHITE);
        g.drawString("Здоровье:", x, y);
        
        int heartSize = 10;
        int spacing = 2;
        int startX = x + 80;
        
        for (int i = 0; i < GameConstants.MAX_HEALTH / 2; i++) {
            int heartX = startX + i * (heartSize + spacing);
            
            if (player.getHealth() >= (i + 1) * 2) {
                g.setColor(GameConstants.HEALTH_COLOR);
                g.fillRect(heartX, y - heartSize, heartSize, heartSize);
            } else if (player.getHealth() >= i * 2 + 1) {
                g.setColor(GameConstants.HEALTH_COLOR);
                g.fillRect(heartX, y - heartSize, heartSize / 2, heartSize);
                g.setColor(Color.GRAY);
                g.fillRect(heartX + heartSize / 2, y - heartSize, heartSize / 2, heartSize);
            } else {
                g.setColor(Color.GRAY);
                g.fillRect(heartX, y - heartSize, heartSize, heartSize);
            }
            
            g.setColor(Color.BLACK);
            g.drawRect(heartX, y - heartSize, heartSize, heartSize);
        }
    }
    
    private void drawHungerBar(Graphics g, int x, int y) {
        g.setColor(Color.WHITE);
        g.drawString("Голод:", x, y);
        
        int foodSize = 10;
        int spacing = 2;
        int startX = x + 80;
        
        for (int i = 0; i < GameConstants.MAX_HUNGER / 2; i++) {
            int foodX = startX + i * (foodSize + spacing);
            
            if (player.getHunger() >= (i + 1) * 2) {
                g.setColor(GameConstants.HUNGER_COLOR);
                g.fillRect(foodX, y - foodSize, foodSize, foodSize);
            } else if (player.getHunger() >= i * 2 + 1) {
                g.setColor(GameConstants.HUNGER_COLOR);
                g.fillRect(foodX, y - foodSize, foodSize / 2, foodSize);
                g.setColor(Color.GRAY);
                g.fillRect(foodX + foodSize / 2, y - foodSize, foodSize / 2, foodSize);
            } else {
                g.setColor(Color.GRAY);
                g.fillRect(foodX, y - foodSize, foodSize, foodSize);
            }
            
            g.setColor(Color.BLACK);
            g.drawRect(foodX, y - foodSize, foodSize, foodSize);
        }
    }
    
    private void drawHotbar(Graphics g) {
        int hotbarWidth = GameConstants.INVENTORY_COLS * GameConstants.INVENTORY_SLOT_SIZE;
        int startX = (GameConstants.SCREEN_WIDTH - hotbarWidth) / 2;
        int y = GameConstants.SCREEN_HEIGHT - 100 - 40;
        
        // Фон хотбара
        g.setColor(new Color(30, 30, 30, 200));
        g.fillRect(startX - 5, y - 5, hotbarWidth + 10, GameConstants.INVENTORY_SLOT_SIZE + 10);
        
        for (int i = 0; i < GameConstants.INVENTORY_COLS; i++) {
            int slotX = startX + i * GameConstants.INVENTORY_SLOT_SIZE;
            
            // Фон слота
            g.setColor(new Color(100, 100, 100));
            g.fillRect(slotX, y, GameConstants.INVENTORY_SLOT_SIZE, GameConstants.INVENTORY_SLOT_SIZE);
            
            // Рамка слота
            if (i == inventoryPanel.getSelectedSlot()) {
                g.setColor(Color.YELLOW);
            } else {
                g.setColor(Color.GRAY);
            }
            g.drawRect(slotX, y, GameConstants.INVENTORY_SLOT_SIZE, GameConstants.INVENTORY_SLOT_SIZE);
            
            // Рисуем предмет в слоте
            drawHotbarItem(g, slotX, y, i);
            
            // Номер клавиши
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(String.valueOf(i + 1), slotX + 5, y + 15);
        }
    }
    
    private void drawHotbarItem(Graphics g, int slotX, int slotY, int slotIndex) {
        int row = slotIndex / GameConstants.INVENTORY_COLS;
        int col = slotIndex % GameConstants.INVENTORY_COLS;
        int itemId = inventoryPanel.inventory[row][col];
        
        if (itemId > 0 && itemId <= GameConstants.ITEM_NAMES.length) {
            String textureName = GameConstants.getTextureName(itemId);
            
            if (textureName != null) {
                try {
                    BufferedImage texture = TextureManager.getInstance().getTexture(textureName);
                    if (texture != null) {
                        int textureSize = GameConstants.INVENTORY_SLOT_SIZE - 8;
                        int textureX = slotX + (GameConstants.INVENTORY_SLOT_SIZE - textureSize) / 2;
                        int textureY = slotY + (GameConstants.INVENTORY_SLOT_SIZE - textureSize) / 2;
                        
                        g.drawImage(texture, textureX, textureY, textureSize, textureSize, null);
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка рисования текстуры в хотбаре: " + e.getMessage());
                }
            }
            
            // Если текстура не загрузилась, рисуем цветной квадрат
            g.setColor(Color.BLUE);
            int itemSize = GameConstants.INVENTORY_SLOT_SIZE - 8;
            int itemX = slotX + (GameConstants.INVENTORY_SLOT_SIZE - itemSize) / 2;
            int itemY = slotY + (GameConstants.INVENTORY_SLOT_SIZE - itemSize) / 2;
            g.fillRect(itemX, itemY, itemSize, itemSize);
        }
    }
    
    private void drawSelectedItem(Graphics g, int x, int y) {
        g.setColor(Color.WHITE);
        g.drawString("Выбран:", x, y);
        
        int selectedSlot = inventoryPanel.getSelectedSlot();
        int row = selectedSlot / GameConstants.INVENTORY_COLS;
        int col = selectedSlot % GameConstants.INVENTORY_COLS;
        int itemId = inventoryPanel.inventory[row][col];
        
        if (itemId > 0 && itemId <= GameConstants.ITEM_NAMES.length) {
            String itemName = GameConstants.ITEM_NAMES[itemId - 1];
            String textureName = GameConstants.getTextureName(itemId);
            
            int textureSize = 32;
            int textureX = x + 80;
            int textureY = y - 25;
            
            if (textureName != null) {
                try {
                    BufferedImage texture = TextureManager.getInstance().getTexture(textureName);
                    if (texture != null) {
                        g.drawImage(texture, textureX, textureY, textureSize, textureSize, null);
                        
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.BOLD, 12));
                        g.drawString(itemName, textureX, textureY + textureSize + 15);
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка рисования текстуры выбранного предмета: " + e.getMessage());
                }
            }
            
            g.setColor(Color.BLUE);
            g.fillRect(textureX, textureY, textureSize, textureSize);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString(itemName, textureX, textureY + textureSize + 15);
        } else {
            g.setColor(Color.GRAY);
            g.drawString("Пусто", x + 80, y);
        }
    }
    
    private void drawInventoryScreen(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
        
        int invX = (GameConstants.SCREEN_WIDTH - inventoryPanel.getPreferredSize().width) / 2;
        int invY = (GameConstants.SCREEN_HEIGHT - inventoryPanel.getPreferredSize().height) / 2;
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(invX, invY);
        inventoryPanel.paint(g2d);
        g2d.dispose();
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Нажми E для закрытия инвентаря", 
                    GameConstants.SCREEN_WIDTH / 2 - 150, 
                    GameConstants.SCREEN_HEIGHT - 30);
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        if (inventoryVisible) {
            if (keyCode == KeyEvent.VK_E) {
                inventoryVisible = false;
            }
        } else {
            switch (keyCode) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    movePlayer(0, -1);
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    movePlayer(0, 1);
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    movePlayer(-1, 0);
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    movePlayer(1, 0);
                    break;
                case KeyEvent.VK_E:
                    inventoryVisible = true;
                    break;
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);
                    break;
                case KeyEvent.VK_1: inventoryPanel.setSelectedSlot(0); break;
                case KeyEvent.VK_2: inventoryPanel.setSelectedSlot(1); break;
                case KeyEvent.VK_3: inventoryPanel.setSelectedSlot(2); break;
                case KeyEvent.VK_4: inventoryPanel.setSelectedSlot(3); break;
                case KeyEvent.VK_5: inventoryPanel.setSelectedSlot(4); break;
                case KeyEvent.VK_6: inventoryPanel.setSelectedSlot(5); break;
                case KeyEvent.VK_7: inventoryPanel.setSelectedSlot(6); break;
                case KeyEvent.VK_8: inventoryPanel.setSelectedSlot(7); break;
                case KeyEvent.VK_9: inventoryPanel.setSelectedSlot(8); break;
            }
        }
        
        repaint();
    }
    
    private void movePlayer(int dx, int dy) {
        int newX = player.getX() + dx;
        int newY = player.getY() + dy;
        
        // Определяем направление взгляда по движению
        int newDirection = player.getDirection();
        if (dy < 0) newDirection = GameConstants.DIRECTION_UP;
        else if (dy > 0) newDirection = GameConstants.DIRECTION_DOWN;
        else if (dx < 0) newDirection = GameConstants.DIRECTION_LEFT;
        else if (dx > 0) newDirection = GameConstants.DIRECTION_RIGHT;
        
        if (newX >= 0 && newX < GameConstants.MAP_WIDTH && 
            newY >= 0 && newY < GameConstants.MAP_HEIGHT) {
            
            char terrain = map[newY][newX];
            if (terrain != GameConstants.WATER) {
                player.setPosition(newX, newY, newDirection);
                updateCamera();
            }
        }
    }
    
    private void updateCamera() {
        int targetCameraX = player.getX() - visibleTilesX / 2;
        int targetCameraY = player.getY() - visibleTilesY / 2;
        
        cameraX = targetCameraX;
        cameraY = targetCameraY;
        
        cameraX = Math.max(0, Math.min(cameraX, GameConstants.MAP_WIDTH - visibleTilesX));
        cameraY = Math.max(0, Math.min(cameraY, GameConstants.MAP_HEIGHT - visibleTilesY));
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
}