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
        loadPlayerTexture("player_down", "plaer_go_on_down.png");
        loadPlayerTexture("player_up", "plaer_go_on_up.png");
        loadPlayerTexture("player_left", "plaer_go_on_left.png");
        loadPlayerTexture("player_right", "plaer_go_on_right.png");
        
        // Создаем простую текстуру воды
        textures.put("water", createWaterTexture(32, 32));
        
        System.out.println("✅ Все текстуры загружены: " + textures.size());
        
        // Проверяем что текстуры персонажа доступны
        checkPlayerTextures();
    }
    
    private void loadPlayerTexture(String textureName, String fileName) {
    String[] possiblePaths = {
        "textures/" + fileName,
        fileName,
        "src/textures/" + fileName,
        "../textures/" + fileName,
        "build/textures/" + fileName
    };
    
    for (String path : possiblePaths) {
        try {
            File textureFile = new File(path);
            System.out.println("🔍 Проверяем путь: " + textureFile.getAbsolutePath());
            
            if (textureFile.exists()) {
                System.out.println("✅ Файл найден: " + path);
                BufferedImage originalTexture = ImageIO.read(textureFile);
                
                // Пробуем оба метода удаления фона
                BufferedImage texture = removeBackground(originalTexture);
                
                textures.put(textureName, texture);
                System.out.println("✅ Текстура " + textureName + " загружена: " + 
                    texture.getWidth() + "x" + texture.getHeight() + 
                    " (фон удален)");
                return;
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка загрузки " + path + ": " + e.getMessage());
        }
    }
    
    // Если файл не найден, создаем текстуру программно
    System.out.println("❌ Файл не найден: " + fileName);
    System.out.println("Создаем текстуру " + textureName + " программно...");
    textures.put(textureName, createPlayerTexture(32, 32, textureName));
}
    
    private BufferedImage removeBackground(BufferedImage image) {
    BufferedImage newImage = new BufferedImage(
        image.getWidth(), 
        image.getHeight(), 
        BufferedImage.TYPE_INT_ARGB
    );
    
    // Анализируем углы изображения чтобы определить цвет фона
    Color[] cornerColors = {
        new Color(image.getRGB(0, 0), true),
        new Color(image.getRGB(image.getWidth()-1, 0), true),
        new Color(image.getRGB(0, image.getHeight()-1), true),
        new Color(image.getRGB(image.getWidth()-1, image.getHeight()-1), true)
    };
    
    // Находим наиболее вероятный цвет фона
    Color backgroundColor = findMostCommonColor(cornerColors);
    
    // Удаляем пиксели похожие на фон
    for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
            int pixel = image.getRGB(x, y);
            Color color = new Color(pixel, true);
            
            // Если пиксель похож на фон, делаем прозрачным
            if (isSimilarColor(color, backgroundColor, 30)) {
                newImage.setRGB(x, y, 0x00000000);
            } else {
                newImage.setRGB(x, y, pixel);
            }
        }
    }
    
    return newImage;
}

private Color findMostCommonColor(Color[] colors) {
    // Простая реализация - берем первый непрозрачный цвет
    for (Color color : colors) {
        if (color.getAlpha() > 200) {
            return color;
        }
    }
    return colors[0];
}

private boolean isSimilarColor(Color c1, Color c2, int tolerance) {
    return Math.abs(c1.getRed() - c2.getRed()) < tolerance &&
           Math.abs(c1.getGreen() - c2.getGreen()) < tolerance &&
           Math.abs(c1.getBlue() - c2.getBlue()) < tolerance &&
           Math.abs(c1.getAlpha() - c2.getAlpha()) < tolerance;
}
    
    private void loadTextureFromFile(String textureName, String filePath) {
        try {
            File textureFile = new File(filePath);
            
            // Пробуем разные пути
            if (!textureFile.exists()) {
                textureFile = new File("textures/" + new File(filePath).getName());
            }
            if (!textureFile.exists()) {
                textureFile = new File("build/textures/" + new File(filePath).getName());
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
    
    private void checkPlayerTextures() {
        System.out.println("=== ПРОВЕРКА ТЕКСТУР ПЕРСОНАЖА ===");
        String[] playerTextures = {"player_down", "player_up", "player_left", "player_right"};
        
        for (String textureName : playerTextures) {
            if (textures.containsKey(textureName)) {
                BufferedImage texture = textures.get(textureName);
                System.out.println("🎯 " + textureName + ": " + 
                    texture.getWidth() + "x" + texture.getHeight() + " (загружена)");
            } else {
                System.out.println("❌ " + textureName + ": НЕ ЗАГРУЖЕНА!");
            }
        }
    }
    
    private BufferedImage createPlayerTexture(int width, int height, String direction) {
        BufferedImage texture = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = texture.createGraphics();
        
        // Включаем сглаживание
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Фон - полностью прозрачный
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, width, height);
        g2d.setComposite(AlphaComposite.SrcOver);
        
        // Тело персонажа (синий)
        g2d.setColor(new Color(0, 100, 255, 255));
        g2d.fillRoundRect(8, 12, 16, 20, 8, 8);
        
        // Голова (светло-желтая)
        g2d.setColor(new Color(255, 220, 150, 255));
        g2d.fillOval(10, 4, 12, 12);
        
        // Глаза и направление взгляда
        g2d.setColor(Color.BLACK);
        switch (direction) {
            case "player_down":
                // Глаза смотрят вниз
                g2d.fillRect(12, 10, 2, 2);
                g2d.fillRect(18, 10, 2, 2);
                // Тело смотрит вниз
                g2d.fillRect(14, 22, 4, 6);
                break;
            case "player_up":
                // Глаза смотрят вверх
                g2d.fillRect(12, 8, 2, 2);
                g2d.fillRect(18, 8, 2, 2);
                // Тело смотрит вверх
                g2d.fillRect(14, 16, 4, 6);
                break;
            case "player_left":
                // Глаза смотрят влево
                g2d.fillRect(10, 8, 2, 2);
                g2d.fillRect(10, 12, 2, 2);
                // Тело смотрит влево
                g2d.fillRect(10, 16, 6, 4);
                break;
            case "player_right":
                // Глаза смотрят вправо
                g2d.fillRect(20, 8, 2, 2);
                g2d.fillRect(20, 12, 2, 2);
                // Тело смотрит вправо
                g2d.fillRect(16, 16, 6, 4);
                break;
        }
        
        // Текст для отладки (полупрозрачный)
        g2d.setColor(new Color(255, 255, 255, 128));
        g2d.setFont(new Font("Arial", Font.BOLD, 8));
        String dirText = direction.replace("player_", "").toUpperCase();
        g2d.drawString(dirText, 4, 30);
        
        g2d.dispose();
        System.out.println("✅ Текстура " + direction + " создана программно: " + width + "x" + height);
        return texture;
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
            
            // Травинки
            g2d.setColor(new Color(50, 205, 50));
            for (int i = 0; i < 5; i++) {
                int x = i * 6 + 2;
                g2d.drawLine(x, height, x, height - 8 - (i % 2) * 4);
            }
        } else if (name.equals("tree")) {
            // Дерево - коричневое с зеленой кроной
            g2d.setColor(new Color(101, 67, 33));
            g2d.fillRect(12, 16, 8, 16); // ствол
            
            g2d.setColor(new Color(34, 139, 34));
            g2d.fillOval(4, 4, 24, 16); // крона
            
            // Детали кроны
            g2d.setColor(new Color(50, 205, 50));
            for (int i = 0; i < 3; i++) {
                int x = 8 + i * 8;
                g2d.fillOval(x, 6, 4, 4);
            }
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
        
        // Вода - синяя с прозрачностью
        g2d.setColor(new Color(30, 144, 255, 180));
        g2d.fillRect(0, 0, width, height);
        
        // Волны
        g2d.setColor(new Color(70, 130, 255, 120));
        for (int i = 0; i < 3; i++) {
            int y = 8 + i * 8;
            g2d.drawArc(0, y, width, 8, 0, 180);
        }
        
        // Блики
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.fillOval(20, 6, 8, 4);
        g2d.fillOval(8, 18, 6, 3);
        
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
        
        // Красный фон с шашечками
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width, height);
        
        g2d.setColor(Color.WHITE);
        for (int y = 0; y < height; y += 8) {
            for (int x = 0; x < width; x += 8) {
                if ((x / 8 + y / 8) % 2 == 0) {
                    g2d.fillRect(x, y, 4, 4);
                }
            }
        }
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString("ERR", 8, 18);
        
        g2d.dispose();
        return texture;
    }
    
    public boolean hasTexture(String textureName) {
        return textures.containsKey(textureName);
    }
}