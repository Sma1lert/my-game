import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private static TextureManager instance;
    private Map<String, BufferedImage> textures;
    
    private TextureManager() {
        textures = new HashMap<>();
        loadTextures();
    }
    
    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }
    
    private void loadTextures() {
        System.out.println("=== ЗАГРУЗКА ТЕКСТУР ===");
        
        // Загружаем текстуру меча
        loadTextureFromFile("sword", "textures/sword_test.png");
        
        // Загружаем текстуру травы
        loadTextureFromFile("grass", "textures/grass_test.png");
        
        // Загружаем текстуру дерева
        loadTextureFromFile("tree", "textures/wood.png");
        
        // Загружаем текстуры персонажа
        loadTextureFromFile("player_down", "textures/plaer_go_on_down.png");
        loadTextureFromFile("player_up", "textures/plaer_go_on_up.png");
        loadTextureFromFile("player_left", "textures/plaer_go_on_left.png");
        loadTextureFromFile("player_right", "textures/plaer_go_on_right.png");

        //животные
        loadTextureFromFile("rabbit", "textures/rabbit_test.png");
        
        // Создаем простую текстуру воды
        textures.put("water", createWaterTexture(32, 32));
        
        // Создаем текстуры для интерфейса
        createUITextures();
        
        System.out.println("✅ Все текстуры загружены: " + textures.size());
        
        // Проверяем что текстуры доступны
        for (String textureName : textures.keySet()) {
            BufferedImage texture = textures.get(textureName);
            System.out.println("🎯 " + textureName + ": " + 
                texture.getWidth() + "x" + texture.getHeight());
        }
    }
    
    private void createUITextures() {
        // Текстуры для иконок интерфейса
        textures.put("ui_health", createHealthIcon());
        textures.put("ui_hunger", createHungerIcon());
        textures.put("ui_level", createLevelIcon());
        // ... другие иконки
    }
    
    private BufferedImage createHealthIcon() {
        BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        
        g2d.setColor(new Color(220, 60, 60));
        g2d.fillRect(4, 2, 8, 12);
        g2d.setColor(new Color(150, 30, 30));
        g2d.drawRect(4, 2, 8, 12);
        
        g2d.dispose();
        return icon;
    }
    
    private BufferedImage createHungerIcon() {
        BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        
        // Иконка еды (яблоко)
        g2d.setColor(new Color(200, 40, 40));
        g2d.fillOval(4, 3, 8, 8);
        g2d.setColor(new Color(50, 150, 50));
        g2d.fillRect(7, 1, 2, 4);
        
        g2d.dispose();
        return icon;
    }
    
    private BufferedImage createLevelIcon() {
        BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        
        // Иконка уровня (звезда)
        g2d.setColor(new Color(255, 215, 0));
        int[] xPoints = {8, 10, 14, 11, 12, 8, 4, 5, 2, 6};
        int[] yPoints = {2, 6, 7, 9, 13, 11, 13, 9, 7, 6};
        g2d.fillPolygon(xPoints, yPoints, 10);
        
        g2d.dispose();
        return icon;
    }
    
    private void loadTextureFromFile(String textureName, String filePath) {
        try {
            File textureFile = new File(filePath);
            
            // Пробуем разные пути
            if (!textureFile.exists()) {
                textureFile = new File("textures/" + new File(filePath).getName());
            }
            
            if (textureFile.exists()) {
                System.out.println("✅ Найден файл текстуры: " + textureFile.getAbsolutePath());
                BufferedImage texture = ImageIO.read(textureFile);
                textures.put(textureName, texture);
                System.out.println("✅ Текстура " + textureName + " загружена: " + 
                    texture.getWidth() + "x" + texture.getHeight());
            } else {
                System.out.println("❌ Файл не найден: " + filePath);
                System.out.println("Создаем текстуру " + textureName + " программно...");
                textures.put(textureName, createDefaultTexture(32, 32, textureName));
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка загрузки файла " + textureName + ": " + e.getMessage());
            System.out.println("Создаем текстуру " + textureName + " программно...");
            textures.put(textureName, createDefaultTexture(32, 32, textureName));
        }
    }
    
    private BufferedImage createDefaultTexture(int width, int height, String name) {
        BufferedImage texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = texture.createGraphics();
        
        // Фон - прозрачный
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, width, height);
        
        if (name.equals("sword")) {
            // Ручка меча - коричневая
            g2d.setColor(new Color(139, 69, 19));
            g2d.fillRect(10, 10, 4, 20);
            
            // Клинок меча - серый
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(14, 5, 8, 25);
            
            // Острие меча
            g2d.setColor(Color.WHITE);
            g2d.fillRect(16, 0, 4, 5);
        } else if (name.equals("grass")) {
            // Трава - зеленый
            g2d.setColor(new Color(34, 139, 34));
            g2d.fillRect(0, 0, width, height);
        } else if (name.equals("tree")) {
            // Дерево - коричневое с зеленой кроной
            g2d.setColor(new Color(101, 67, 33));
            g2d.fillRect(12, 16, 8, 16); // ствол
            
            g2d.setColor(new Color(34, 139, 34));
            g2d.fillOval(4, 4, 24, 16); // крона
        }
        
        // Текст для отладки
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 8));
        g2d.drawString(name.toUpperCase(), 2, 12);
        
        g2d.dispose();
        System.out.println("✅ Текстура " + name + " создана программно: " + width + "x" + height);
        return texture;
    }
    
    private BufferedImage createWaterTexture(int width, int height) {
        BufferedImage texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = texture.createGraphics();
        
        // Вода - синяя с волнами
        g2d.setColor(new Color(30, 144, 255, 180));
        g2d.fillRect(0, 0, width, height);
        
        // Волны
        g2d.setColor(new Color(70, 130, 255, 120));
        for (int i = 0; i < 3; i++) {
            int y = 8 + i * 8;
            g2d.drawArc(0, y, width, 8, 0, 180);
        }
        
        g2d.dispose();
        return texture;
    }
    
    public BufferedImage getTexture(String textureName) {
        BufferedImage texture = textures.get(textureName);
        if (texture == null) {
            System.out.println("❌ Текстура не найдена: " + textureName);
            return createErrorTexture(32, 32);
        }
        return texture;
    }
    
    private BufferedImage createErrorTexture(int width, int height) {
        BufferedImage texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = texture.createGraphics();
        
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width, height);
        
        g2d.setColor(Color.WHITE);
        g2d.drawString("ERR", 5, 15);
        
        g2d.dispose();
        return texture;
    }
    
    public boolean hasTexture(String textureName) {
        return textures.containsKey(textureName);
    }
}