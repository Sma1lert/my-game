import java.util.Random;

public class WorldMap {
    private char[][] map;
    private Random random;
    
    public WorldMap() {
        map = new char[GameConstants.MAP_HEIGHT][GameConstants.MAP_WIDTH];
        random = new Random();
        generateWorld();
    }
    
    private void generateWorld() {
        for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConstants.MAP_WIDTH; x++) {
                double chance = random.nextDouble();
                
                if (y < 5) {
                    // Поверхность - трава, деревья, вода (камней нет)
                    if (chance < 0.7) map[y][x] = GameConstants.GRASS;
                    else if (chance < 0.85) map[y][x] = GameConstants.TREE;
                    else if (chance < 0.95) map[y][x] = GameConstants.SOIL;
                    else map[y][x] = GameConstants.WATER;
                } else if (y < 15) {
                    // Подземелье - почва и пещеры (камней нет)
                    if (chance < 0.8) map[y][x] = GameConstants.SOIL;
                    else map[y][x] = GameConstants.EMPTY;
                } else {
                    // Глубокие пещеры
                    if (chance < 0.6) map[y][x] = GameConstants.SOIL;
                    else map[y][x] = GameConstants.EMPTY;
                }
            }
        }
        
        // Добавляем вход в пещеру
        map[4][GameConstants.MAP_WIDTH/2] = GameConstants.CAVE_ENTRANCE;
        map[5][GameConstants.MAP_WIDTH/2] = GameConstants.EMPTY;
        map[6][GameConstants.MAP_WIDTH/2] = GameConstants.EMPTY;
        
        placeEnemies();
    }
    
    private void placeEnemies() {
        for (int i = 0; i < 10; i++) {
            int x = random.nextInt(GameConstants.MAP_WIDTH);
            int y = random.nextInt(GameConstants.MAP_HEIGHT - 10) + 5;
            if (map[y][x] == GameConstants.EMPTY || map[y][x] == GameConstants.SOIL) {
                map[y][x] = GameConstants.ENEMY;
            }
        }
    }
    
    public char getTerrainAt(int x, int y) {
        if (x < 0 || x >= GameConstants.MAP_WIDTH || y < 0 || y >= GameConstants.MAP_HEIGHT) {
            return GameConstants.SOIL; // Граница карты теперь почва
        }
        return map[y][x];
    }
    
    public boolean canMoveTo(int x, int y) {
        char terrain = getTerrainAt(x, y);
        // Теперь можно ходить везде, кроме воды
        return terrain != GameConstants.WATER;
    }
    
    public String getTerrainName(char terrain) {
        switch (terrain) {
            case GameConstants.GRASS: return "Трава";
            case GameConstants.SOIL: return "Земля";
            case GameConstants.WATER: return "Вода";
            case GameConstants.TREE: return "Лес";
            case GameConstants.EMPTY: return "Пещера";
            case GameConstants.CAVE_ENTRANCE: return "Вход в пещеру";
            case GameConstants.ENEMY: return "Враг";
            default: return "Неизвестно";
        }
    }
    
    public char[][] getMap() {
        return map;
    }
}