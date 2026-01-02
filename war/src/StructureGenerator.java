import java.util.Random;

public class StructureGenerator {
    private Random random;
    private boolean houseGenerated = false;
    private int houseX, houseY;
    
    public StructureGenerator() {
        random = new Random();
    }
    
    // Основной метод для генерации структур на карте
    public void generateStructures(char[][] map, long worldSeed, int playerX, int playerY) {
        random.setSeed(worldSeed);
        
        // Генерируем только один дом рядом с игроком
        if (!houseGenerated) {
            generateHouseNearPlayer(map, playerX, playerY);
            houseGenerated = true;
        }
        
        System.out.println("🏠 Сгенерирован 1 дом рядом с игроком");
    }
    
    private void generateHouseNearPlayer(char[][] map, int playerX, int playerY) {
        int attempts = 0;
        int maxAttempts = 50;
        
        // Пытаемся найти место для дома в радиусе 10-20 тайлов от игрока
        while (attempts < maxAttempts) {
            // Случайное направление и расстояние от игрока
            double angle = random.nextDouble() * 2 * Math.PI;
            int distance = 10 + random.nextInt(15); // 10-25 тайлов от игрока
            
            int houseX = playerX + (int)(Math.cos(angle) * distance);
            int houseY = playerY + (int)(Math.sin(angle) * distance);
            
            // Проверяем границы карты
            houseX = Math.max(10, Math.min(houseX, GameConstants.MAP_WIDTH - 10));
            houseY = Math.max(10, Math.min(houseY, GameConstants.MAP_HEIGHT - 10));
            
            if (isValidHouseLocation(map, houseX, houseY)) {
                this.houseX = houseX;
                this.houseY = houseY;
                generateHouse(map, houseX, houseY);
                System.out.println("✅ Дом построен рядом с игроком в позиции: " + houseX + ", " + houseY);
                return;
            }
            attempts++;
        }
        
        // Если не нашли идеальное место, строим дом на фиксированном расстоянии
        System.out.println("⚠️ Не удалось найти идеальное место для дома, строим вплотную");
        
        // Пробуем 4 направления вокруг игрока
        int[][] directions = {{5, 0}, {-5, 0}, {0, 5}, {0, -5}};
        for (int[] dir : directions) {
            int x = playerX + dir[0];
            int y = playerY + dir[1];
            
            x = Math.max(10, Math.min(x, GameConstants.MAP_WIDTH - 10));
            y = Math.max(10, Math.min(y, GameConstants.MAP_HEIGHT - 10));
            
            if (isValidHouseLocation(map, x, y)) {
                this.houseX = x;
                this.houseY = y;
                generateHouse(map, x, y);
                System.out.println("✅ Дом построен вплотную к игроку: " + x + ", " + y);
                return;
            }
        }
        
        // Последняя попытка - прямо рядом с игроком
        houseX = playerX + 3;
        houseY = playerY + 3;
        houseX = Math.max(10, Math.min(houseX, GameConstants.MAP_WIDTH - 10));
        houseY = Math.max(10, Math.min(houseY, GameConstants.MAP_HEIGHT - 10));
        
        // Принудительно очищаем область для дома
        clearAreaForHouse(map, houseX, houseY);
        generateHouse(map, houseX, houseY);
        System.out.println("⚠️ Дом построен принудительно: " + houseX + ", " + houseY);
    }
    
    private void clearAreaForHouse(char[][] map, int x, int y) {
        // Очищаем область 7x7 для дома
        for (int houseY = y - 3; houseY <= y + 3; houseY++) {
            for (int houseX = x - 3; houseX <= x + 3; houseX++) {
                if (houseX >= 0 && houseX < GameConstants.MAP_WIDTH && 
                    houseY >= 0 && houseY < GameConstants.MAP_HEIGHT) {
                    map[houseY][houseX] = GameConstants.GRASS;
                }
            }
        }
    }
    
    private boolean isValidHouseLocation(char[][] map, int startX, int startY) {
        // Проверяем, что область 7x7 свободна (с запасом вокруг дома)
        for (int y = startY - 3; y <= startY + 3; y++) {
            for (int x = startX - 3; x <= startX + 3; x++) {
                if (x < 0 || x >= GameConstants.MAP_WIDTH || y < 0 || y >= GameConstants.MAP_HEIGHT) {
                    return false;
                }
                // Дом можно строить только на траве
                if (map[y][x] != GameConstants.GRASS) {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean isHouseGenerated() {
    return houseGenerated;
}

public void setHouseGenerated(boolean houseGenerated) {
    this.houseGenerated = houseGenerated;
}

public void setHousePosition(int x, int y) {
    this.houseX = x;
    this.houseY = y;
}
    // Генерация дома 5x5 из камня с деревянным полом
    private void generateHouse(char[][] map, int startX, int startY) {
        // Фундамент и пол из деревянных досок
        for (int y = startY; y < startY + 5; y++) {
            for (int x = startX; x < startX + 5; x++) {
                if (y >= 0 && y < GameConstants.MAP_HEIGHT && x >= 0 && x < GameConstants.MAP_WIDTH) {
                    map[y][x] = GameConstants.WOOD_PLANK;
                }
            }
        }
        
        // Стены из камня (только по периметру)
        for (int y = startY; y < startY + 5; y++) {
            for (int x = startX; x < startX + 5; x++) {
                if (y >= 0 && y < GameConstants.MAP_HEIGHT && x >= 0 && x < GameConstants.MAP_WIDTH) {
                    // Стены только по краям (кроме углов для лучшего вида)
                    boolean isWall = (y == startY || y == startY + 4) && (x >= startX && x <= startX + 4) ||
                                    (x == startX || x == startX + 4) && (y >= startY && y <= startY + 4);
                    
                    if (isWall) {
                        map[y][x] = GameConstants.STONE;
                    }
                }
            }
        }
        
        // Дверь (в центре нижней стены)
        if (startY + 4 < GameConstants.MAP_HEIGHT && startX + 2 < GameConstants.MAP_WIDTH) {
            map[startY + 4][startX + 2] = GameConstants.WOOD_PLANK;
        }
        
        // Окна (в боковых стенах)
        if (startY + 1 < GameConstants.MAP_HEIGHT && startX < GameConstants.MAP_WIDTH) {
            map[startY + 1][startX] = GameConstants.GLASS;
        }
        if (startY + 3 < GameConstants.MAP_HEIGHT && startX < GameConstants.MAP_WIDTH) {
            map[startY + 3][startX] = GameConstants.GLASS;
        }
        if (startY + 1 < GameConstants.MAP_HEIGHT && startX + 4 < GameConstants.MAP_WIDTH) {
            map[startY + 1][startX + 4] = GameConstants.GLASS;
        }
        if (startY + 3 < GameConstants.MAP_HEIGHT && startX + 4 < GameConstants.MAP_WIDTH) {
            map[startY + 3][startX + 4] = GameConstants.GLASS;
        }
        
        // Крыша (только над внутренним пространством, не над стенами)
        for (int y = startY + 1; y < startY + 4; y++) {
            for (int x = startX + 1; x < startX + 4; x++) {
                if (y >= 0 && y < GameConstants.MAP_HEIGHT && x >= 0 && x < GameConstants.MAP_WIDTH) {
                    // Помечаем внутренние тайлы как имеющие крышу
                    map[y][x] = GameConstants.ROOFED;
                }
            }
        }
    }
    
    // Метод для получения позиции дома (может пригодиться)
    public int[] getHousePosition() {
        return new int[]{houseX, houseY};
    }
    
    // Метод для сброса состояния (при регенерации мира)
    public void reset() {
        houseGenerated = false;
        houseX = 0;
        houseY = 0;
    }
    
    // Метод для получения типа структуры в указанной позиции
    public static String getStructureTypeAt(char terrain) {
        switch (terrain) {
            case GameConstants.STONE:
                return "Каменная стена";
            case GameConstants.WOOD_PLANK:
                return "Деревянный пол";
            case GameConstants.GLASS:
                return "Стеклянное окно";
            case GameConstants.ROOFED:
                return "Крыша";
            default:
                return null;
        }
    }
    
    // Метод для проверки, можно ли ходить по этому типу структуры
    public static boolean isStructurePassable(char terrain) {
        // По полу и пространству под крышей можно ходить, по стенам - нет
        return terrain == GameConstants.WOOD_PLANK || terrain == GameConstants.ROOFED;
    }
}