import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements KeyListener {
    private char[][] map;
    private int[][] biomes;
    private Player player;
    private List<Rabbit> rabbits;
    private long worldSeed;
    private double cameraX;
    private double cameraY;
    private int visibleTilesX;
    private int visibleTilesY;
    
    private InventoryPanel inventoryPanel;
    private boolean inventoryVisible = false;
    private Timer gameTimer;
    private GameWindow gameWindow;
    
    private boolean isAttacking = false;
    private int attackAnimationFrame = 0;
    private Timer attackTimer;
    
    // Для обработки зажатых клавиш
    private boolean shiftPressed = false;
    
    // Для системы атаки по курсору
    private double mouseWorldX = 0;
    private double mouseWorldY = 0;
    private boolean showAttackRange = false;
    
    // Мультиплеер
    private MultiplayerManager multiplayerManager;
    private boolean isMultiplayer = false;
    
    // Генератор структур
    private StructureGenerator structureGenerator;
    
    // Система сохранений
    private SaveLoadManager saveManager;
    
    // Временное сообщение в игре
    private String gameMessage = "";
    private long messageDisplayTime = 0;
    
    public GamePanel(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.multiplayerManager = new MultiplayerManager();
        this.saveManager = new SaveLoadManager();
        
        setPreferredSize(new Dimension(
            GameConstants.SCREEN_WIDTH,
            GameConstants.SCREEN_HEIGHT
        ));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        this.multiplayerManager.setGamePanel(this);
        
        // Добавляем обработчики мыши
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateMouseWorldPosition(e.getX(), e.getY());
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && !inventoryVisible) {
                    attackAtCursor();
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                showAttackRange = true;
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                showAttackRange = false;
                repaint();
            }
        });
        
        initializeGame();
    }
    
    private void initializeGame() {
        visibleTilesX = GameConstants.SCREEN_WIDTH / GameConstants.TILE_SIZE;
        visibleTilesY = (GameConstants.SCREEN_HEIGHT - GameConstants.UI_PANEL_HEIGHT) / GameConstants.TILE_SIZE;
        
        map = new char[GameConstants.MAP_HEIGHT][GameConstants.MAP_WIDTH];
        biomes = new int[GameConstants.MAP_HEIGHT][GameConstants.MAP_WIDTH];
        rabbits = new ArrayList<>();
        
        // Инициализируем сид (будет переопределен при мультиплеере)
        this.worldSeed = System.currentTimeMillis();
        
        // Инициализируем генератор структур
        structureGenerator = new StructureGenerator();
        
        // Генерируем мир после инициализации генератора структур
        generateWorld();
        generateRabbits();
        
        int startX, startY;
        do {
            startX = (int)(Math.random() * (GameConstants.MAP_WIDTH - 50)) + 25;
            startY = (int)(Math.random() * (GameConstants.MAP_HEIGHT - 50)) + 25;
        } while (map[startY][startX] == GameConstants.WATER || map[startY][startX] == GameConstants.TREE);
        
        player = new Player(startX, startY);
        centerCameraOnPlayer();
        
        // Генерируем структуры (только один дом рядом с игроком) после создания игрока
        structureGenerator.generateStructures(map, worldSeed, player.getX(), player.getY());
        
        TextureManager textureManager = TextureManager.getInstance();
        System.out.println("✅ TextureManager инициализирован");
        
        inventoryPanel = new InventoryPanel();
        System.out.println("✅ InventoryPanel инициализирован");
        
        attackTimer = new Timer(50, e -> {
            if (isAttacking) {
                attackAnimationFrame++;
                if (attackAnimationFrame >= 6) {
                    isAttacking = false;
                    attackAnimationFrame = 0;
                    attackTimer.stop();
                }
                repaint();
            }
        });
        
        System.out.println("🎮 Игрок создан в позиции: X=" + startX + " Y=" + startY);
        System.out.println("🏠 Дом сгенерирован рядом с игроком");
        System.out.println("💾 Менеджер сохранений инициализирован");
    }
    
    // ============ СИСТЕМА СОХРАНЕНИЙ ============
    
    public void quickSave() {
        String saveName = "quicksave_" + System.currentTimeMillis();
        if (saveGame(saveName)) {
            showGameMessage("Быстрое сохранение создано: " + saveName, 2000);
        } else {
            showGameMessage("Ошибка сохранения!", 2000);
        }
    }
    
    public void quickLoad() {
        // Загружаем последнее быстрое сохранение
        List<String> saves = saveManager.getSaveList();
        String lastQuickSave = null;
        long latestTime = 0;
        
        for (String save : saves) {
            if (save.startsWith("quicksave_")) {
                try {
                    long saveTime = Long.parseLong(save.substring(9));
                    if (saveTime > latestTime) {
                        latestTime = saveTime;
                        lastQuickSave = save;
                    }
                } catch (NumberFormatException e) {
                    // Пропускаем некорректные имена
                }
            }
        }
        
        if (lastQuickSave != null) {
            if (loadGame(lastQuickSave)) {
                showGameMessage("Быстрое сохранение загружено", 2000);
            }
        } else {
            showGameMessage("Быстрое сохранение не найдено!", 2000);
        }
    }
    
    public boolean saveGame(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            System.out.println("❌ Имя сохранения не может быть пустым");
            return false;
        }
        
        try {
            GameSaveData saveData = new GameSaveData();
            
            // Данные игрока
            saveData.setPlayerX(player.getExactX());
            saveData.setPlayerY(player.getExactY());
            saveData.setPlayerHealth(player.getHealth());
            saveData.setPlayerHunger(player.getHunger());
            saveData.setPlayerExperience(player.getExperience());
            saveData.setPlayerLevel(player.getLevel());
            saveData.setPlayerDirection(player.getDirection());
            
            // Данные мира
            saveData.setWorldSeed(worldSeed);
            
            // Копируем данные карты
            char[][] mapCopy = new char[GameConstants.MAP_HEIGHT][GameConstants.MAP_WIDTH];
            for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
                System.arraycopy(map[y], 0, mapCopy[y], 0, GameConstants.MAP_WIDTH);
            }
            saveData.setMapData(mapCopy);
            
            // Копируем данные биомов
            int[][] biomeCopy = new int[GameConstants.MAP_HEIGHT][GameConstants.MAP_WIDTH];
            for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
                System.arraycopy(biomes[y], 0, biomeCopy[y], 0, GameConstants.MAP_WIDTH);
            }
            saveData.setBiomeData(biomeCopy);
            
            // Данные кроликов
            List<RabbitSaveData> rabbitsData = new ArrayList<>();
            for (Rabbit rabbit : rabbits) {
                if (rabbit.isAlive()) {
                    RabbitSaveData rabbitData = new RabbitSaveData();
                    rabbitData.setX(rabbit.getX());
                    rabbitData.setY(rabbit.getY());
                    rabbitData.setHealth(rabbit.getHealth());
                    rabbitsData.add(rabbitData);
                }
            }
            saveData.setRabbitsData(rabbitsData);
            
            // Данные структур
            if (structureGenerator != null) {
                saveData.setHouseGenerated(structureGenerator.isHouseGenerated());
                int[] housePos = structureGenerator.getHousePosition();
                saveData.setHouseX(housePos[0]);
                saveData.setHouseY(housePos[1]);
            }
            
            // Мета-данные
            saveData.setSaveName(saveName);
            saveData.setSaveTimestamp(System.currentTimeMillis());
            
            // Сохраняем через SaveLoadManager
            boolean success = saveManager.saveGame(saveData);
            if (success) {
                showGameMessage("Игра сохранена: " + saveName, 2000);
            }
            return success;
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка сохранения игры: " + e.getMessage());
            e.printStackTrace();
            showGameMessage("Ошибка сохранения!", 2000);
            return false;
        }
    }
    
    public boolean loadGame(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            System.out.println("❌ Имя сохранения не может быть пустым");
            showGameMessage("Ошибка загрузки: имя не указано", 2000);
            return false;
        }
        
        try {
            GameSaveData saveData = saveManager.loadGame(saveName);
            if (saveData == null) {
                System.out.println("❌ Сохранение не найдено: " + saveName);
                showGameMessage("Сохранение не найдено: " + saveName, 2000);
                return false;
            }
            
            // Останавливаем текущую игру
            stopGame();
            
            // Восстанавливаем мир
            this.worldSeed = saveData.getWorldSeed();
            
            // Восстанавливаем карту
            char[][] loadedMap = saveData.getMapData();
            if (loadedMap != null) {
                for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
                    System.arraycopy(loadedMap[y], 0, map[y], 0, GameConstants.MAP_WIDTH);
                }
            }
            
            // Восстанавливаем биомы
            int[][] loadedBiomes = saveData.getBiomeData();
            if (loadedBiomes != null) {
                for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
                    System.arraycopy(loadedBiomes[y], 0, biomes[y], 0, GameConstants.MAP_WIDTH);
                }
            }
            
            // Восстанавливаем игрока
            double playerX = saveData.getPlayerX();
            double playerY = saveData.getPlayerY();
            this.player = new Player((int)playerX, (int)playerY);
            player.setHealth(saveData.getPlayerHealth());
            player.setHunger(saveData.getPlayerHunger());
            player.setExperience(saveData.getPlayerExperience());
            player.setLevel(saveData.getPlayerLevel());
            player.setDirection(saveData.getPlayerDirection());
            
            // Восстанавливаем кроликов
            this.rabbits.clear();
            List<RabbitSaveData> rabbitsData = saveData.getRabbitsData();
            if (rabbitsData != null) {
                for (RabbitSaveData rabbitData : rabbitsData) {
                    Rabbit rabbit = new Rabbit(rabbitData.getX(), rabbitData.getY());
                    rabbit.setHealth(rabbitData.getHealth());
                    rabbits.add(rabbit);
                }
            }
            
            // Восстанавливаем структуры
            if (structureGenerator != null) {
                structureGenerator.setHouseGenerated(saveData.isHouseGenerated());
                structureGenerator.setHousePosition(saveData.getHouseX(), saveData.getHouseY());
            }
            
            // Обновляем камеру
            centerCameraOnPlayer();
            
            // Перезапускаем игру
            startGame();
            
            System.out.println("✅ Игра загружена: " + saveName);
            showGameMessage("Игра загружена: " + saveName, 2000);
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки игры: " + e.getMessage());
            e.printStackTrace();
            showGameMessage("Ошибка загрузки игры!", 2000);
            return false;
        }
    }
    
    // Метод для загрузки из объекта GameSaveData
   public void loadFromSave(GameSaveData saveData) {
    if (saveData == null) return;
    
    try {
        // Восстанавливаем мир
        this.worldSeed = saveData.getWorldSeed();
        
        // Восстанавливаем карту
        char[][] loadedMap = saveData.getMapData();
        if (loadedMap != null) {
            for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
                System.arraycopy(loadedMap[y], 0, map[y], 0, GameConstants.MAP_WIDTH);
            }
        }
        
        // Восстанавливаем биомы
        int[][] loadedBiomes = saveData.getBiomeData();
        if (loadedBiomes != null) {
            for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
                System.arraycopy(loadedBiomes[y], 0, biomes[y], 0, GameConstants.MAP_WIDTH);
            }
        }
        
        // Восстанавливаем игрока
        double playerX = saveData.getPlayerX();
        double playerY = saveData.getPlayerY();
        
        // Создаем нового игрока
        this.player = new Player((int)playerX, (int)playerY);
        player.setX(playerX);
        player.setY(playerY);
        player.setHealth(saveData.getPlayerHealth());
        player.setHunger(saveData.getPlayerHunger());
        player.setExperience(saveData.getPlayerExperience());
        player.setLevel(saveData.getPlayerLevel());
        player.setDirection(saveData.getPlayerDirection());
        
        // Восстанавливаем кроликов
        this.rabbits.clear();
        List<RabbitSaveData> rabbitsData = saveData.getRabbitsData();
        if (rabbitsData != null) {
            for (RabbitSaveData rabbitData : rabbitsData) {
                Rabbit rabbit = new Rabbit(rabbitData.getX(), rabbitData.getY());
                rabbit.setHealth(rabbitData.getHealth());
                rabbits.add(rabbit);
            }
        }
        
        // Восстанавливаем структуры
        if (structureGenerator != null) {
            structureGenerator.setHouseGenerated(saveData.isHouseGenerated());
            structureGenerator.setHousePosition(saveData.getHouseX(), saveData.getHouseY());
        }
        
        // Обновляем камеру
        centerCameraOnPlayer();
        
        System.out.println("✅ Игра загружена из объекта: " + saveData.getSaveName());
        showGameMessage("Игра загружена: " + saveData.getSaveName(), 2000);
        
        // Если игра была запущена, перезапускаем
        if (gameTimer != null && !gameTimer.isRunning()) {
            startGame();
        }
        
    } catch (Exception e) {
        System.err.println("❌ Ошибка загрузки из объекта: " + e.getMessage());
        e.printStackTrace();
        showGameMessage("Ошибка загрузки игры!", 2000);
    }
}
    
    // Временное сообщение в игре
    private void showGameMessage(String message, int displayTimeMs) {
        this.gameMessage = message;
        this.messageDisplayTime = System.currentTimeMillis() + displayTimeMs;
    }
    
    // ============ ГЕНЕРАЦИЯ МИРА ============
    
    private void generateWorld() {
        generateBiomes();
        generateTerrainFromBiomes();
        generateWaterBodies();
        
        System.out.println("✅ Мир сгенерирован: биомы, озера, реки созданы");
    }
    
    private void generateBiomes() {
        double[][] noise = new double[GameConstants.MAP_HEIGHT][GameConstants.MAP_WIDTH];
        
        for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConstants.MAP_WIDTH; x++) {
                double value = 0;
                value += perlinNoise(x * 0.01, y * 0.01) * 0.5;
                value += perlinNoise(x * 0.02, y * 0.02) * 0.25;
                value += perlinNoise(x * 0.04, y * 0.04) * 0.125;
                value += perlinNoise(x * 0.08, y * 0.08) * 0.0625;
                
                noise[y][x] = value;
            }
        }
        
        for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConstants.MAP_WIDTH; x++) {
                if (noise[y][x] > 0.1) {
                    biomes[y][x] = 1; // Лес
                } else {
                    biomes[y][x] = 0; // Луг
                }
            }
        }
        
        System.out.println("🌳 Биомы сгенерированы");
    }
    
    private void generateTerrainFromBiomes() {
        for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
            for (int x = 0; x < GameConstants.MAP_WIDTH; x++) {
                if (biomes[y][x] == 0) {
                    if (Math.random() < 0.95) {
                        map[y][x] = GameConstants.GRASS;
                    } else {
                        map[y][x] = GameConstants.TREE;
                    }
                } else {
                    if (Math.random() < 0.7) {
                        map[y][x] = GameConstants.TREE;
                    } else {
                        map[y][x] = GameConstants.GRASS;
                    }
                }
            }
        }
    }
    
    private void generateWaterBodies() {
        generateLakes();
        generateRivers();
        System.out.println("🌊 Водоемы сгенерированы");
    }
    
    private void generateLakes() {
        Random deterministicRandom = new Random(worldSeed);
        int lakeCount = 15 + (int)(deterministicRandom.nextDouble() * 10);
        
        for (int i = 0; i < lakeCount; i++) {
            int centerX = (int)(deterministicRandom.nextDouble() * GameConstants.MAP_WIDTH);
            int centerY = (int)(deterministicRandom.nextDouble() * GameConstants.MAP_HEIGHT);
            int lakeSize = 8 + (int)(deterministicRandom.nextDouble() * 20);
            
            createLake(centerX, centerY, lakeSize, deterministicRandom);
        }
        
        System.out.println("🏞️ Создано " + lakeCount + " озер с сидом: " + worldSeed);
    }
    
    private void createLake(int centerX, int centerY, int size, Random random) {
        int radiusX = size;
        int radiusY = (int)(size * (0.6 + random.nextDouble() * 0.8));
        
        for (int y = centerY - radiusY; y <= centerY + radiusY; y++) {
            for (int x = centerX - radiusX; x <= centerX + radiusX; x++) {
                if (x >= 0 && x < GameConstants.MAP_WIDTH && y >= 0 && y < GameConstants.MAP_HEIGHT) {
                    double normalizedX = (double)(x - centerX) / radiusX;
                    double normalizedY = (double)(y - centerY) / radiusY;
                    double ellipseValue = normalizedX * normalizedX + normalizedY * normalizedY;
                    
                    double waterProbability = 1.0 - ellipseValue;
                    waterProbability += (random.nextDouble() * 0.4) - 0.2;
                    
                    if (ellipseValue < 0.3) {
                        map[y][x] = GameConstants.WATER;
                    } else if (ellipseValue < 1.0 && waterProbability > 0.3) {
                        map[y][x] = GameConstants.WATER;
                    }
                }
            }
        }
        
        addLakeBays(centerX, centerY, radiusX, radiusY, random);
    }
    
    private void addLakeBays(int centerX, int centerY, int radiusX, int radiusY, Random random) {
        int bayCount = 3 + (int)(random.nextDouble() * 4);
        
        for (int i = 0; i < bayCount; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            int bayStartX = centerX + (int)(Math.cos(angle) * radiusX * 0.8);
            int bayStartY = centerY + (int)(Math.sin(angle) * radiusY * 0.8);
            createBay(bayStartX, bayStartY, angle, random);
        }
    }
    
    private void createBay(int startX, int startY, double direction, Random random) {
        int bayLength = 3 + (int)(random.nextDouble() * 8);
        int bayWidth = 1 + (int)(random.nextDouble() * 3);
        
        int x = startX;
        int y = startY;
        
        for (int i = 0; i < bayLength; i++) {
            x += (int)(Math.cos(direction) * 1.2);
            y += (int)(Math.sin(direction) * 1.2);
            
            for (int wy = -bayWidth; wy <= bayWidth; wy++) {
                for (int wx = -bayWidth; wx <= bayWidth; wx++) {
                    int worldX = x + wx;
                    int worldY = y + wy;
                    
                    if (worldX >= 0 && worldX < GameConstants.MAP_WIDTH && 
                        worldY >= 0 && worldY < GameConstants.MAP_HEIGHT) {
                        
                        double distance = Math.sqrt(wx * wx + wy * wy);
                        if (distance <= bayWidth && random.nextDouble() < 0.7) {
                            map[worldY][worldX] = GameConstants.WATER;
                        }
                    }
                }
            }
            
            direction += (random.nextDouble() * 0.5) - 0.25;
        }
    }
    
    private void generateRivers() {
        Random deterministicRandom = new Random(worldSeed);
        int riverCount = 5 + (int)(deterministicRandom.nextDouble() * 3);
        
        for (int i = 0; i < riverCount; i++) {
            int startX, startY;
            if (deterministicRandom.nextDouble() < 0.5) {
                startX = (int)(deterministicRandom.nextDouble() * GameConstants.MAP_WIDTH);
                startY = deterministicRandom.nextDouble() < 0.5 ? 0 : GameConstants.MAP_HEIGHT - 1;
            } else {
                startX = deterministicRandom.nextDouble() < 0.5 ? 0 : GameConstants.MAP_WIDTH - 1;
                startY = (int)(deterministicRandom.nextDouble() * GameConstants.MAP_HEIGHT);
            }
            
            createRiver(startX, startY, deterministicRandom);
        }
        
        System.out.println("🌊 Создано " + riverCount + " рек с сидом: " + worldSeed);
    }
    
    private void createRiver(int startX, int startY, Random random) {
        int x = startX;
        int y = startY;
        int riverLength = 100 + (int)(random.nextDouble() * 200);
        
        int dirX = startX == 0 ? 1 : (startX == GameConstants.MAP_WIDTH - 1 ? -1 : 0);
        int dirY = startY == 0 ? 1 : (startY == GameConstants.MAP_HEIGHT - 1 ? -1 : 0);
        
        if (dirX == 0 && dirY == 0) {
            if (random.nextDouble() < 0.5) {
                dirX = random.nextDouble() < 0.5 ? -1 : 1;
            } else {
                dirY = random.nextDouble() < 0.5 ? -1 : 1;
            }
        }
        
        for (int i = 0; i < riverLength; i++) {
            if (x < 0 || x >= GameConstants.MAP_WIDTH || y < 0 || y >= GameConstants.MAP_HEIGHT) {
                break;
            }
            
            int riverWidth = 1 + (int)(random.nextDouble() * 2);
            for (int wy = -riverWidth; wy <= riverWidth; wy++) {
                for (int wx = -riverWidth; wx <= riverWidth; wx++) {
                    int nx = x + wx;
                    int ny = y + wy;
                    
                    if (nx >= 0 && nx < GameConstants.MAP_WIDTH && ny >= 0 && ny < GameConstants.MAP_HEIGHT) {
                        double distance = Math.sqrt(wx * wx + wy * wy);
                        if (distance <= riverWidth) {
                            map[ny][nx] = GameConstants.WATER;
                        }
                    }
                }
            }
            
            x += dirX;
            y += dirY;
            
            if (random.nextDouble() < 0.3) {
                if (random.nextDouble() < 0.5) {
                    dirX += random.nextDouble() < 0.5 ? -1 : 1;
                    dirX = Math.max(-1, Math.min(1, dirX));
                } else {
                    dirY += random.nextDouble() < 0.5 ? -1 : 1;
                    dirY = Math.max(-1, Math.min(1, dirY));
                }
            }
        }
    }
    
    private void generateRabbits() {
        Random deterministicRandom = new Random(worldSeed);
        int rabbitCount = 30 + (int)(deterministicRandom.nextDouble() * 20);
        
        for (int i = 0; i < rabbitCount; i++) {
            int x, y;
            int attempts = 0;
            
            do {
                x = (int)(deterministicRandom.nextDouble() * GameConstants.MAP_WIDTH);
                y = (int)(deterministicRandom.nextDouble() * GameConstants.MAP_HEIGHT);
                attempts++;
            } while (attempts < 100 && 
                    (biomes[y][x] != 0 || 
                     map[y][x] != GameConstants.GRASS || 
                     map[y][x] == GameConstants.WATER));
            
            if (attempts < 100) {
                rabbits.add(new Rabbit(x, y));
            }
        }
        
        System.out.println("🐇 Сгенерировано " + rabbits.size() + " кроликов с сидом: " + worldSeed);
    }
    
    // ============ PERLIN NOISE ============
    
    private double perlinNoise(double x, double y) {
        int X = (int)Math.floor(x) & 255;
        int Y = (int)Math.floor(y) & 255;
        
        x -= Math.floor(x);
        y -= Math.floor(y);
        
        double u = fade(x);
        double v = fade(y);
        
        int A = p[X] + Y;
        int B = p[X + 1] + Y;
        
        return lerp(v, lerp(u, grad(p[A], x, y), grad(p[B], x - 1, y)),
                      lerp(u, grad(p[A + 1], x, y - 1), grad(p[B + 1], x - 1, y - 1)));
    }
    
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }
    
    private double grad(int hash, double x, double y) {
        int h = hash & 7;
        double u = h < 4 ? x : y;
        double v = h < 4 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
    
    private static final int[] p = new int[512];
    static {
        int[] permutation = {
            151,160,137,91,90,15,131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,
            8,99,37,240,21,10,23,190,6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,
            35,11,32,57,177,33,88,237,149,56,87,174,20,125,136,171,168,68,175,74,165,71,
            134,139,48,27,166,77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,
            55,46,245,40,244,102,143,54,65,25,63,161,1,216,80,73,209,76,132,187,208,89,
            18,169,200,196,135,130,116,188,159,86,164,100,109,198,173,186,3,64,52,217,226,
            250,124,123,5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,
            189,28,42,223,183,170,213,119,248,152,2,44,154,163,70,221,153,101,155,167,43,
            172,9,129,22,39,253,19,98,108,110,79,113,224,232,178,185,112,104,218,246,97,
            228,251,34,242,193,238,210,144,12,191,191,179,162,241,81,51,145,235,249,14,239,
            107,49,192,214,31,181,199,106,157,184,84,204,176,115,121,50,45,127,4,150,254,
            138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
        };
        
        for (int i = 0; i < 256; i++) {
            p[256 + i] = p[i] = permutation[i];
        }
    }
    
    // ============ ОСНОВНЫЕ МЕТОДЫ ИГРЫ ============
    
    private void centerCameraOnPlayer() {
        cameraX = player.getExactX() - visibleTilesX / 2.0;
        cameraY = player.getExactY() - visibleTilesY / 2.0;
        cameraX = Math.max(0, Math.min(cameraX, GameConstants.MAP_WIDTH - visibleTilesX));
        cameraY = Math.max(0, Math.min(cameraY, GameConstants.MAP_HEIGHT - visibleTilesY));
    }
    
    public boolean startMultiplayerGame(boolean createGame, String ip) {
        if (createGame) {
            if (multiplayerManager.createGame()) {
                isMultiplayer = true;
                
                // Если мы сервер, добавляем второго игрока рядом с первым
                if (multiplayerManager.isServer()) {
                    int player2X, player2Y;
                    int attempts = 0;
                    
                    do {
                        double angle = Math.random() * 2 * Math.PI;
                        double distance = 5 + Math.random() * 15;
                        
                        player2X = (int)(player.getX() + Math.cos(angle) * distance);
                        player2Y = (int)(player.getY() + Math.sin(angle) * distance);
                        
                        player2X = Math.max(0, Math.min(player2X, GameConstants.MAP_WIDTH - 1));
                        player2Y = Math.max(0, Math.min(player2Y, GameConstants.MAP_HEIGHT - 1));
                        
                        attempts++;
                    } while (attempts < 50 && 
                            (map[player2Y][player2X] == GameConstants.WATER || 
                             map[player2Y][player2X] == GameConstants.TREE));
                    
                    if (attempts >= 50) {
                        player2X = (int)player.getX() + 5;
                        player2Y = (int)player.getY() + 5;
                        
                        player2X = Math.max(0, Math.min(player2X, GameConstants.MAP_WIDTH - 1));
                        player2Y = Math.max(0, Math.min(player2Y, GameConstants.MAP_HEIGHT - 1));
                        
                        System.out.println("⚠️ Не удалось найти идеальное место, спавним рядом: " + 
                                         player2X + ", " + player2Y);
                    }
                    
                    multiplayerManager.addRemotePlayer(2, player2X, player2Y);
                    System.out.println("🎮 Второй игрок создан рядом: X=" + player2X + " Y=" + player2Y);
                }
                
                startGame();
                return true;
            }
        } else {
            if (multiplayerManager.joinGame(ip)) {
                isMultiplayer = true;
                startGame();
                return true;
            }
        }
        return false;
    }
    
    public void startGame() {
        // Убедимся, что игра не на паузе при старте
        if (gameWindow != null) {
            gameWindow.resumeGame();
        }
        
        gameTimer = new Timer(50, e -> {
            player.update();
            updateRabbits();
            updateCamera();
            
            // Обновляем отображение сообщения
            if (System.currentTimeMillis() > messageDisplayTime) {
                gameMessage = "";
            }
            
            if (isMultiplayer) {
                sendPlayerUpdate();
            }
            
            repaint();
        });
        gameTimer.start();
        requestFocusInWindow(); // Получаем фокус для обработки клавиш
        System.out.println("🎮 Игра запущена" + (isMultiplayer ? " (Мультиплеер)" : " (Одиночная)"));
    }
    
    public void stopGame() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
            System.out.println("🎮 Игра остановлена");
        }
        if (attackTimer != null && attackTimer.isRunning()) {
            attackTimer.stop();
        }
        if (isMultiplayer) {
            multiplayerManager.disconnect();
            isMultiplayer = false;
            System.out.println("🔌 Отключено от мультиплеера");
        }
    }
    
    private void sendPlayerUpdate() {
        if (isMultiplayer) {
            multiplayerManager.updatePlayerPosition(
                player.getExactX(), 
                player.getExactY(), 
                player.getDirection()
            );
        }
    }
    
    private void updateRabbits() {
        Iterator<Rabbit> iterator = rabbits.iterator();
        while (iterator.hasNext()) {
            Rabbit rabbit = iterator.next();
            if (rabbit.isAlive()) {
                rabbit.update(map);
            } else {
                iterator.remove();
            }
        }
    }
    
    private void updateCamera() {
        double targetCameraX = player.getExactX() - visibleTilesX / 2.0;
        double targetCameraY = player.getExactY() - visibleTilesY / 2.0;
        
        cameraX += (targetCameraX - cameraX) * 0.1;
        cameraY += (targetCameraY - cameraY) * 0.1;
        
        cameraX = Math.max(0, Math.min(cameraX, GameConstants.MAP_WIDTH - visibleTilesX));
        cameraY = Math.max(0, Math.min(cameraY, GameConstants.MAP_HEIGHT - visibleTilesY));
    }
    
    private void updateMouseWorldPosition(int screenX, int screenY) {
        mouseWorldX = (screenX / (double) GameConstants.TILE_SIZE) + cameraX;
        mouseWorldY = (screenY / (double) GameConstants.TILE_SIZE) + cameraY;
        
        mouseWorldX = Math.max(0, Math.min(mouseWorldX, GameConstants.MAP_WIDTH - 1));
        mouseWorldY = Math.max(0, Math.min(mouseWorldY, GameConstants.MAP_HEIGHT - 1));
        
        if (showAttackRange) {
            repaint();
        }
    }
    
    private void attackAtCursor() {
        if (isAttacking) return;
        
        if (!player.canAttackTo(mouseWorldX, mouseWorldY)) {
            System.out.println("❌ Слишком далеко для атаки!");
            return;
        }
        
        isAttacking = true;
        attackAnimationFrame = 0;
        attackTimer.start();
        
        player.setDirectionTowards(mouseWorldX, mouseWorldY);
        
        int attackX = player.getAttackTargetX(mouseWorldX);
        int attackY = player.getAttackTargetY(mouseWorldY);
        
        System.out.println("⚔️ Атака по курсору: X=" + attackX + " Y=" + attackY);
        
        for (Rabbit rabbit : rabbits) {
            if (rabbit.getX() == attackX && rabbit.getY() == attackY && rabbit.isAlive()) {
                rabbit.takeDamage(GameConstants.ATTACK_DAMAGE);
                System.out.println("🎯 Попадание по кролику! У кролика осталось: " + rabbit.getHealth() + " HP");
                
                if (!rabbit.isAlive()) {
                    System.out.println("🐇 Кролик побежден!");
                    player.addExperience(10);
                }
                break;
            }
        }
    }
    
    // ============ ОТРИСОВКА ============
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (inventoryVisible) {
            drawInventoryScreen(g);
        } else {
            drawMap(g);
            drawGameUI(g);
            
            // Рисуем временное сообщение
            if (!gameMessage.isEmpty()) {
                drawGameMessage(g);
            }
            
            // Рисуем индикатор паузы если игра на паузе
            if (gameWindow != null && gameWindow.isPaused()) {
                drawPauseIndicator(g);
            }
        }
    }
    
    private void drawGameMessage(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Font font = new Font("Arial", Font.BOLD, 16);
        FontMetrics fm = g2d.getFontMetrics(font);
        int messageWidth = fm.stringWidth(gameMessage);
        int messageHeight = fm.getHeight();
        
        int x = (getWidth() - messageWidth) / 2;
        int y = 50;
        
        // Фон сообщения
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(x - 10, y - messageHeight + 5, messageWidth + 20, messageHeight + 10, 15, 15);
        
        // Текст сообщения
        g2d.setFont(font);
        g2d.setColor(Color.YELLOW);
        g2d.drawString(gameMessage, x, y);
    }
    
    private void drawPauseIndicator(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        String pauseText = "ПАУЗА";
        Font font = new Font("Arial", Font.BOLD, 48);
        FontMetrics fm = g2d.getFontMetrics(font);
        int textWidth = fm.stringWidth(pauseText);
        int textHeight = fm.getHeight();
        
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2;
        
        // Тень текста
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.setFont(font);
        g2d.drawString(pauseText, x + 4, y + 4);
        
        // Основной текст
        g2d.setColor(new Color(255, 215, 0, 200));
        g2d.drawString(pauseText, x, y);
    }
    
    private void drawMap(Graphics g) {
        // Сначала рисуем основную карту
        for (int screenY = 0; screenY < visibleTilesY; screenY++) {
            int worldY = (int) Math.floor(cameraY) + screenY;
            if (worldY < 0 || worldY >= GameConstants.MAP_HEIGHT) continue;
            
            for (int screenX = 0; screenX < visibleTilesX; screenX++) {
                int worldX = (int) Math.floor(cameraX) + screenX;
                if (worldX < 0 || worldX >= GameConstants.MAP_WIDTH) continue;
                
                double offsetX = (cameraX - Math.floor(cameraX)) * GameConstants.TILE_SIZE;
                double offsetY = (cameraY - Math.floor(cameraY)) * GameConstants.TILE_SIZE;
                
                int pixelX = (int) (screenX * GameConstants.TILE_SIZE - offsetX);
                int pixelY = (int) (screenY * GameConstants.TILE_SIZE - offsetY);
                
                char terrain = map[worldY][worldX];
                drawTerrain(g, pixelX, pixelY, terrain);
            }
        }
        
        // Затем рисуем крыши поверх (если нужно)
        drawRoofs(g);
        
        // Остальная отрисовка (игроки, враги, эффекты)...
        if (showAttackRange && !inventoryVisible) {
            drawAttackRange(g);
        }
        
        drawRabbits(g);
    
        if (isAttacking) {
            drawAttack(g);
        }
        
        drawPlayer(g);
        
        // Отрисовка удаленных игроков
        if (isMultiplayer) {
            drawRemotePlayers(g);
        }
    }
    
    private void drawRoofs(Graphics g) {
        for (int screenY = 0; screenY < visibleTilesY; screenY++) {
            int worldY = (int) Math.floor(cameraY) + screenY;
            if (worldY < 0 || worldY >= GameConstants.MAP_HEIGHT) continue;
            
            for (int screenX = 0; screenX < visibleTilesX; screenX++) {
                int worldX = (int) Math.floor(cameraX) + screenX;
                if (worldX < 0 || worldX >= GameConstants.MAP_WIDTH) continue;
                
                double offsetX = (cameraX - Math.floor(cameraX)) * GameConstants.TILE_SIZE;
                double offsetY = (cameraY - Math.floor(cameraY)) * GameConstants.TILE_SIZE;
                
                int pixelX = (int) (screenX * GameConstants.TILE_SIZE - offsetX);
                int pixelY = (int) (screenY * GameConstants.TILE_SIZE - offsetY);
                
                char terrain = map[worldY][worldX];
                
                // Рисуем крышу только для тайлов с крышей
                if (terrain == GameConstants.ROOFED) {
                    drawRoof(g, pixelX, pixelY);
                }
            }
        }
    }
    
    private void drawRoof(Graphics g, int x, int y) {
        String textureName = GameConstants.TEXTURE_ROOF;
        
        try {
            BufferedImage texture = TextureManager.getInstance().getTexture(textureName);
            if (texture != null) {
                // Рисуем крышу с полупрозрачностью для видимости интерьера
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2d.drawImage(texture, x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, null);
                g2d.dispose();
                return;
            }
        } catch (Exception e) {
            System.out.println("Ошибка рисования текстуры крыши: " + e.getMessage());
        }
        
        // Запасной вариант - полупрозрачный серый квадрат
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        g2d.setColor(new Color(80, 80, 80));
        g2d.fillRect(x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
        g2d.dispose();
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
        
        Color color = Color.WHITE;
        switch (terrain) {
            case GameConstants.GRASS: 
                color = new Color(34, 139, 34);
                break;
            case GameConstants.TREE: 
                color = new Color(101, 67, 33);
                break;
            case GameConstants.WATER: 
                color = new Color(30, 144, 255);
                break;
            case GameConstants.STONE:
                color = new Color(120, 120, 120);
                break;
            case GameConstants.WOOD_PLANK:
                color = new Color(160, 120, 80);
                break;
            case GameConstants.GLASS:
                color = new Color(200, 220, 255, 150);
                break;
            case GameConstants.ROOFED:
                // Крыша уже отрисована в drawRoofs, поэтому здесь ничего не делаем
                return;
        }
        
        drawTerrainSymbol(g, x, y, terrain, color);
    }
    
    private void drawTerrainSymbol(Graphics g, int x, int y, char symbol, Color color) {
        g.setColor(color);
        g.fillRect(x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
        
        g.setColor(color.darker());
        g.drawRect(x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, GameConstants.TILE_SIZE - 10));
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (GameConstants.TILE_SIZE - fm.charWidth(symbol)) / 2;
        int textY = y + (GameConstants.TILE_SIZE + fm.getAscent()) / 2 - 2;
        g.drawString(String.valueOf(symbol), textX, textY);
    }
    
    private void drawRabbits(Graphics g) {
        for (Rabbit rabbit : rabbits) {
            if (!rabbit.isAlive()) continue;
            
            double rabbitScreenX = (rabbit.getX() - cameraX) * GameConstants.TILE_SIZE;
            double rabbitScreenY = (rabbit.getY() - cameraY) * GameConstants.TILE_SIZE;
            
            if (rabbitScreenX >= -GameConstants.TILE_SIZE && 
                rabbitScreenX < GameConstants.SCREEN_WIDTH &&
                rabbitScreenY >= -GameConstants.TILE_SIZE && 
                rabbitScreenY < GameConstants.SCREEN_HEIGHT - GameConstants.UI_PANEL_HEIGHT) {
                
                drawRabbit(g, (int) rabbitScreenX, (int) rabbitScreenY, rabbit);
            }
        }
    }
    
    private void drawRabbit(Graphics g, int x, int y, Rabbit rabbit) {
        String textureName = "rabbit";
        
        try {
            BufferedImage texture = TextureManager.getInstance().getTexture(textureName);
            if (texture != null) {
                g.drawImage(texture, x, y, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, null);
                drawRabbitHealthBar(g, x, y, rabbit.getHealth());
                return;
            }
        } catch (Exception e) {
            System.out.println("Ошибка рисования текстуры кролика: " + e.getMessage());
        }
        
        drawRabbitSymbol(g, x, y, rabbit);
    }
    
    private void drawRabbitSymbol(Graphics g, int x, int y, Rabbit rabbit) {
        g.setColor(Color.WHITE);
        g.fillOval(x + 4, y + 4, GameConstants.TILE_SIZE - 8, GameConstants.TILE_SIZE - 8);
        
        g.setColor(Color.PINK);
        g.fillOval(x + 6, y + 2, 6, 8);
        g.fillOval(x + GameConstants.TILE_SIZE - 12, y + 2, 6, 8);
        
        g.setColor(Color.BLACK);
        g.fillOval(x + 8, y + 10, 4, 4);
        g.fillOval(x + GameConstants.TILE_SIZE - 12, y + 10, 4, 4);
        
        g.setColor(Color.PINK);
        g.fillOval(x + GameConstants.TILE_SIZE/2 - 2, y + 15, 4, 3);
        
        drawRabbitHealthBar(g, x, y, rabbit.getHealth());
    }
    
    private void drawRabbitHealthBar(Graphics g, int x, int y, int health) {
        int barWidth = GameConstants.TILE_SIZE - 4;
        int barHeight = 4;
        int barX = x + 2;
        int barY = y - 6;
        
        g.setColor(Color.RED);
        g.fillRect(barX, barY, barWidth, barHeight);
        
        g.setColor(Color.GREEN);
        int healthWidth = (int)((health / 4.0) * barWidth);
        g.fillRect(barX, barY, healthWidth, barHeight);
        
        g.setColor(Color.BLACK);
        g.drawRect(barX, barY, barWidth, barHeight);
    }
    
    private void drawAttack(Graphics g) {
        int attackX = (int)((player.getAttackTargetX(mouseWorldX) - cameraX) * GameConstants.TILE_SIZE);
        int attackY = (int)((player.getAttackTargetY(mouseWorldY) - cameraY) * GameConstants.TILE_SIZE);
        
        int alpha = 150 - (attackAnimationFrame * 25);
        alpha = Math.max(50, alpha);
        
        g.setColor(new Color(255, 0, 0, alpha));
        g.fillRect(attackX, attackY, GameConstants.TILE_SIZE, GameConstants.TILE_SIZE);
        
        drawSwordEffect(g, attackX, attackY);
    }
    
    private void drawSwordEffect(Graphics g, int x, int y) {
        g.setColor(Color.YELLOW);
        
        switch (player.getDirection()) {
            case GameConstants.DIRECTION_UP:
                g.fillRect(x + GameConstants.TILE_SIZE/2 - 1, y, 2, GameConstants.TILE_SIZE/2);
                break;
            case GameConstants.DIRECTION_DOWN:
                g.fillRect(x + GameConstants.TILE_SIZE/2 - 1, y + GameConstants.TILE_SIZE/2, 2, GameConstants.TILE_SIZE/2);
                break;
            case GameConstants.DIRECTION_LEFT:
                g.fillRect(x, y + GameConstants.TILE_SIZE/2 - 1, GameConstants.TILE_SIZE/2, 2);
                break;
            case GameConstants.DIRECTION_RIGHT:
                g.fillRect(x + GameConstants.TILE_SIZE/2, y + GameConstants.TILE_SIZE/2 - 1, GameConstants.TILE_SIZE/2, 2);
                break;
        }
    }
    
    private void drawAttackRange(Graphics g) {
        int playerScreenX = (int)((player.getExactX() - cameraX) * GameConstants.TILE_SIZE);
        int playerScreenY = (int)((player.getExactY() - cameraY) * GameConstants.TILE_SIZE);
        
        int rangePixels = (int)(GameConstants.ATTACK_RANGE * GameConstants.TILE_SIZE);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(255, 255, 0, 50));
        g2d.fillOval(
            playerScreenX - rangePixels + GameConstants.TILE_SIZE / 2,
            playerScreenY - rangePixels + GameConstants.TILE_SIZE / 2,
            rangePixels * 2,
            rangePixels * 2
        );
        
        int mouseScreenX = (int)((mouseWorldX - cameraX) * GameConstants.TILE_SIZE);
        int mouseScreenY = (int)((mouseWorldY - cameraY) * GameConstants.TILE_SIZE);
        
        g2d.setColor(new Color(255, 255, 0, 150));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawLine(
            playerScreenX + GameConstants.TILE_SIZE / 2,
            playerScreenY + GameConstants.TILE_SIZE / 2,
            mouseScreenX + GameConstants.TILE_SIZE / 2,
            mouseScreenY + GameConstants.TILE_SIZE / 2
        );
        
        if (!player.canAttackTo(mouseWorldX, mouseWorldY)) {
            g2d.setColor(new Color(255, 0, 0, 150));
            
            double angle = Math.atan2(mouseWorldY - player.getExactY(), mouseWorldX - player.getExactX());
            int boundaryX = (int)((player.getExactX() + Math.cos(angle) * GameConstants.ATTACK_RANGE - cameraX) * GameConstants.TILE_SIZE);
            int boundaryY = (int)((player.getExactY() + Math.sin(angle) * GameConstants.ATTACK_RANGE - cameraY) * GameConstants.TILE_SIZE);
            
            g2d.drawLine(
                playerScreenX + GameConstants.TILE_SIZE / 2,
                playerScreenY + GameConstants.TILE_SIZE / 2,
                boundaryX + GameConstants.TILE_SIZE / 2,
                boundaryY + GameConstants.TILE_SIZE / 2
            );
        }
    }
    
    private void drawPlayer(Graphics g) {
        double playerScreenX = (player.getExactX() - cameraX) * GameConstants.TILE_SIZE;
        double playerScreenY = (player.getExactY() - cameraY) * GameConstants.TILE_SIZE;
        
        String playerTextureName = getPlayerTextureByDirection();
        
        try {
            BufferedImage playerTexture = TextureManager.getInstance().getTexture(playerTextureName);
            if (playerTexture != null) {
                g.drawImage(playerTexture, (int) playerScreenX, (int) playerScreenY, 
                           GameConstants.TILE_SIZE, GameConstants.TILE_SIZE, null);
            } else {
                drawPlayerSymbol(g, (int) playerScreenX, (int) playerScreenY);
            }
        } catch (Exception e) {
            drawPlayerSymbol(g, (int) playerScreenX, (int) playerScreenY);
        }
    }
    
    private void drawRemotePlayers(Graphics g) {
        for (MultiplayerPlayer remotePlayer : multiplayerManager.getRemotePlayers()) {
            double remoteScreenX = (remotePlayer.getX() - cameraX) * GameConstants.TILE_SIZE;
            double remoteScreenY = (remotePlayer.getY() - cameraY) * GameConstants.TILE_SIZE;
            
            if (remoteScreenX >= -GameConstants.TILE_SIZE && 
                remoteScreenX < GameConstants.SCREEN_WIDTH &&
                remoteScreenY >= -GameConstants.TILE_SIZE && 
                remoteScreenY < GameConstants.SCREEN_HEIGHT - GameConstants.UI_PANEL_HEIGHT) {
                
                drawRemotePlayer(g, (int)remoteScreenX, (int)remoteScreenY, remotePlayer);
            }
        }
    }
    
    private void drawRemotePlayer(Graphics g, int x, int y, MultiplayerPlayer remotePlayer) {
        g.setColor(Color.CYAN);
        g.fillOval(x + 2, y + 2, GameConstants.TILE_SIZE - 4, GameConstants.TILE_SIZE - 4);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString(remotePlayer.getName(), x, y - 5);
        
        g.setColor(Color.BLACK);
        switch (remotePlayer.getDirection()) {
            case GameConstants.DIRECTION_UP:
                g.fillRect(x + GameConstants.TILE_SIZE/2 - 2, y + 4, 4, 8);
                break;
            case GameConstants.DIRECTION_DOWN:
                g.fillRect(x + GameConstants.TILE_SIZE/2 - 2, y + GameConstants.TILE_SIZE - 12, 4, 8);
                break;
            case GameConstants.DIRECTION_LEFT:
                g.fillRect(x + 4, y + GameConstants.TILE_SIZE/2 - 2, 8, 4);
                break;
            case GameConstants.DIRECTION_RIGHT:
                g.fillRect(x + GameConstants.TILE_SIZE - 12, y + GameConstants.TILE_SIZE/2 - 2, 8, 4);
                break;
        }
    }
    
    private void drawPlayerSymbol(Graphics g, int x, int y) {
        g.setColor(Color.YELLOW);
        g.fillOval(x + 2, y + 2, GameConstants.TILE_SIZE - 4, GameConstants.TILE_SIZE - 4);
        
        g.setColor(Color.BLACK);
        switch (player.getDirection()) {
            case GameConstants.DIRECTION_UP:
                g.fillRect(x + GameConstants.TILE_SIZE/2 - 2, y + 4, 4, 8);
                break;
            case GameConstants.DIRECTION_DOWN:
                g.fillRect(x + GameConstants.TILE_SIZE/2 - 2, y + GameConstants.TILE_SIZE - 12, 4, 8);
                break;
            case GameConstants.DIRECTION_LEFT:
                g.fillRect(x + 4, y + GameConstants.TILE_SIZE/2 - 2, 8, 4);
                break;
            case GameConstants.DIRECTION_RIGHT:
                g.fillRect(x + GameConstants.TILE_SIZE - 12, y + GameConstants.TILE_SIZE/2 - 2, 8, 4);
                break;
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
    
    // ============ УЛУЧШЕННЫЙ ИНТЕРФЕЙС ============
    
    private void drawGameUI(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Включаем сглаживание для красивого интерфейса
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int panelY = GameConstants.SCREEN_HEIGHT - GameConstants.UI_PANEL_HEIGHT;
        
        // Фон панели интерфейса
        drawUIPanelBackground(g2d, panelY);
        
        // Разделяем интерфейс на три колонки
        int col1X = GameConstants.UI_MARGIN;
        int col2X = GameConstants.SCREEN_WIDTH / 3;
        int col3X = (GameConstants.SCREEN_WIDTH * 2) / 3;
        
        int currentY = panelY + GameConstants.UI_MARGIN;
        
        // Колонка 1: Основные показатели (здоровье, голод, опыт)
        drawPlayerStats(g2d, col1X, currentY);
        
        // Колонка 2: Информация о мире и позиции
        drawWorldInfo(g2d, col2X, currentY);
        
        // Колонка 3: Инвентарь и мультиплеер
        drawInventoryAndMultiplayer(g2d, col3X, currentY);
        
        // Горячие клавиши (хотбар) - рисуем над панелью интерфейса
        drawModernHotbar(g2d, panelY - 50);
    }
    
    private void drawUIPanelBackground(Graphics2D g2d, int panelY) {
        // Основной фон панели
        g2d.setColor(GameConstants.UI_BACKGROUND);
        g2d.fillRoundRect(0, panelY, 
                         GameConstants.SCREEN_WIDTH, GameConstants.UI_PANEL_HEIGHT, 
                         20, 20);
        
        // Граница панели
        g2d.setColor(GameConstants.UI_BORDER);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(2, panelY + 2, 
                         GameConstants.SCREEN_WIDTH - 4, GameConstants.UI_PANEL_HEIGHT - 4, 
                         18, 18);
        
        // Разделительные линии между колонками
        g2d.setColor(new Color(80, 80, 120, 100));
        g2d.drawLine(GameConstants.SCREEN_WIDTH / 3, panelY + 10, 
                    GameConstants.SCREEN_WIDTH / 3, panelY + GameConstants.UI_PANEL_HEIGHT - 10);
        g2d.drawLine((GameConstants.SCREEN_WIDTH * 2) / 3, panelY + 10, 
                    (GameConstants.SCREEN_WIDTH * 2) / 3, panelY + GameConstants.UI_PANEL_HEIGHT - 10);
    }
    
    private void drawPlayerStats(Graphics2D g2d, int x, int y) {
        // Заголовок колонки
        drawSectionHeader(g2d, x, y, "СТАТУС ПЕРСОНАЖА");
        y += 25;
        
        // Здоровье
        drawStatBar(g2d, x, y, "❤️ ЗДОРОВЬЕ", 
                   player.getHealth(), GameConstants.PLAYER_MAX_HEALTH,
                   GameConstants.HEALTH_COLOR, GameConstants.HEALTH_BG_COLOR);
        y += GameConstants.UI_ELEMENT_HEIGHT + 5;
        
        // Голод
        drawStatBar(g2d, x, y, "🍖 СЫТОСТЬ", 
                   player.getHunger(), GameConstants.MAX_HUNGER,
                   GameConstants.HUNGER_COLOR, GameConstants.HUNGER_BG_COLOR);
        y += GameConstants.UI_ELEMENT_HEIGHT + 5;
        
        // Уровень и опыт
        drawLevelAndExp(g2d, x, y);
    }
    
    private void drawWorldInfo(Graphics2D g2d, int x, int y) {
        // Заголовок колонки
        drawSectionHeader(g2d, x, y, "ИНФОРМАЦИЯ О МИРЕ");
        y += 25;
        
        // Координаты
        drawInfoText(g2d, x, y, "📍 КООРДИНАТЫ:", 
                    String.format("X: %.1f, Y: %.1f", player.getExactX(), player.getExactY()));
        y += GameConstants.UI_ELEMENT_HEIGHT;
        
        // Биом
        String biomeName = getCurrentBiomeName();
        drawInfoText(g2d, x, y, "🌿 БИОМ:", biomeName);
        y += GameConstants.UI_ELEMENT_HEIGHT;
        
        // Кролики поблизости
        int visibleRabbits = countVisibleRabbits();
        drawInfoText(g2d, x, y, "🐇 КРОЛИКИ РЯДОМ:", String.valueOf(visibleRabbits));
    }
    
    private void drawInventoryAndMultiplayer(Graphics2D g2d, int x, int y) {
        // Заголовок колонки
        drawSectionHeader(g2d, x, y, "ИНВЕНТАРЬ И СЕТЬ");
        y += 25;
        
        // Выбранный предмет
        String selectedItem = inventoryPanel.getSelectedItemName();
        drawInfoText(g2d, x, y, "🎒 ВЫБРАНО:", selectedItem);
        y += GameConstants.UI_ELEMENT_HEIGHT;
        
        // Слот инвентаря
        drawInfoText(g2d, x, y, "🔢 СЛОТ:", 
                    String.valueOf(inventoryPanel.getSelectedSlot() + 1) + "/" + 
                    (GameConstants.INVENTORY_ROWS * GameConstants.INVENTORY_COLS));
        y += GameConstants.UI_ELEMENT_HEIGHT;
        
        // Мультиплеер
        String multiplayerStatus = isMultiplayer ? 
            (multiplayerManager.isServer() ? "⚡ ХОСТ" : "🔗 КЛИЕНТ") : "🔌 ОДИНОЧНАЯ";
        drawInfoText(g2d, x, y, "🌐 РЕЖИМ:", multiplayerStatus);
        y += GameConstants.UI_ELEMENT_HEIGHT;
        
        // Кол-во игроков в мультиплеере
        if (isMultiplayer) {
            int playerCount = multiplayerManager.getRemotePlayers().size() + 1;
            drawInfoText(g2d, x, y, "👥 ИГРОКОВ:", playerCount + " в сети");
        }
    }
    
    private void drawModernHotbar(Graphics2D g2d, int y) {
        int hotbarWidth = GameConstants.INVENTORY_COLS * 50;
        int startX = (GameConstants.SCREEN_WIDTH - hotbarWidth) / 2;
        
        // Фон хотбара
        g2d.setColor(new Color(40, 40, 60, 220));
        g2d.fillRoundRect(startX - 10, y - 10, hotbarWidth + 20, 60, 15, 15);
        
        // Граница хотбара
        g2d.setColor(new Color(100, 100, 150));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(startX - 10, y - 10, hotbarWidth + 20, 60, 15, 15);
        
        // Слоты хотбара
        for (int i = 0; i < GameConstants.INVENTORY_COLS; i++) {
            int slotX = startX + i * 50;
            
            // Фон слота
            if (i == inventoryPanel.getSelectedSlot()) {
                g2d.setColor(new Color(100, 150, 255, 150));
                g2d.fillRoundRect(slotX, y, 45, 45, 10, 10);
                g2d.setColor(new Color(200, 220, 255));
            } else {
                g2d.setColor(new Color(80, 80, 100));
                g2d.fillRoundRect(slotX, y, 45, 45, 10, 10);
                g2d.setColor(new Color(140, 140, 160));
            }
            
            g2d.drawRoundRect(slotX, y, 45, 45, 10, 10);
            
            // Предмет в слоте
            drawHotbarItem(g2d, slotX, y, i);
            
            // Номер слота
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.drawString(String.valueOf(i + 1), slotX + 5, y + 15);
        }
    }
    
    // Вспомогательные методы отрисовки интерфейса
    private void drawSectionHeader(Graphics2D g2d, int x, int y, String text) {
        g2d.setColor(GameConstants.LABEL_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(text, x, y);
    }
    
    private void drawStatBar(Graphics2D g2d, int x, int y, String label, 
                           int current, int max, Color fillColor, Color bgColor) {
        // Метка
        g2d.setColor(GameConstants.LABEL_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(label, x, y + 15);
        
        // Фон бара
        g2d.setColor(bgColor);
        g2d.fillRoundRect(x + 120, y, GameConstants.UI_BAR_WIDTH, GameConstants.UI_BAR_HEIGHT, 10, 10);
        
        // Заполненная часть
        double percentage = (double) current / max;
        int filledWidth = (int) (GameConstants.UI_BAR_WIDTH * percentage);
        
        if (filledWidth > 0) {
            g2d.setColor(fillColor);
            g2d.fillRoundRect(x + 120, y, filledWidth, GameConstants.UI_BAR_HEIGHT, 10, 10);
        }
        
        // Граница
        g2d.setColor(new Color(40, 40, 40));
        g2d.drawRoundRect(x + 120, y, GameConstants.UI_BAR_WIDTH, GameConstants.UI_BAR_HEIGHT, 10, 10);
        
        // Текст значения
        g2d.setColor(GameConstants.TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        String valueText = current + " / " + max;
        int textWidth = g2d.getFontMetrics().stringWidth(valueText);
        g2d.drawString(valueText, x + 120 + (GameConstants.UI_BAR_WIDTH - textWidth) / 2, y + 14);
    }
    
    private void drawLevelAndExp(Graphics2D g2d, int x, int y) {
        g2d.setColor(GameConstants.LABEL_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("⭐ УРОВЕНЬ " + player.getLevel(), x, y + 15);
        
        g2d.setColor(GameConstants.TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.drawString("Опыт: " + player.getExperience(), x + 120, y + 15);
    }
    
    private void drawInfoText(Graphics2D g2d, int x, int y, String label, String value) {
        g2d.setColor(GameConstants.LABEL_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(label, x, y + 15);
        
        g2d.setColor(GameConstants.TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString(value, x + 120, y + 15);
    }
    
    private String getCurrentBiomeName() {
        int playerX = player.getX();
        int playerY = player.getY();
        
        if (playerY >= 0 && playerY < GameConstants.MAP_HEIGHT && 
            playerX >= 0 && playerX < GameConstants.MAP_WIDTH) {
            int biome = biomes[playerY][playerX];
            return (biome == 0) ? "Луг" : "Лес";
        }
        return "Неизвестно";
    }
    
    private int countVisibleRabbits() {
        int count = 0;
        for (Rabbit rabbit : rabbits) {
            if (!rabbit.isAlive()) continue;
            
            double rabbitScreenX = rabbit.getX() - cameraX;
            double rabbitScreenY = rabbit.getY() - cameraY;
            
            if (rabbitScreenX >= 0 && rabbitScreenX < visibleTilesX &&
                rabbitScreenY >= 0 && rabbitScreenY < visibleTilesY) {
                count++;
            }
        }
        return count;
    }
    
    private void drawHotbarItem(Graphics2D g2d, int slotX, int slotY, int slotIndex) {
        int row = slotIndex / GameConstants.INVENTORY_COLS;
        int col = slotIndex % GameConstants.INVENTORY_COLS;
        int itemId = inventoryPanel.inventory[row][col];
        
        if (itemId > 0 && itemId <= GameConstants.ITEM_NAMES.length) {
            String textureName = GameConstants.getTextureName(itemId);
            
            if (textureName != null) {
                try {
                    BufferedImage texture = TextureManager.getInstance().getTexture(textureName);
                    if (texture != null) {
                        int textureSize = 35;
                        int textureX = slotX + (45 - textureSize) / 2;
                        int textureY = slotY + (45 - textureSize) / 2;
                        
                        g2d.drawImage(texture, textureX, textureY, textureSize, textureSize, null);
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка рисования текстуры в хотбаре: " + e.getMessage());
                }
            }
            
            g2d.setColor(Color.BLUE);
            int itemSize = 35;
            int itemX = slotX + (45 - itemSize) / 2;
            int itemY = slotY + (45 - itemSize) / 2;
            g2d.fillRect(itemX, itemY, itemSize, itemSize);
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
    
    // ============ УПРАВЛЕНИЕ ============
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        if (inventoryVisible) {
            if (keyCode == KeyEvent.VK_E || keyCode == KeyEvent.VK_ESCAPE) {
                inventoryVisible = false;
                repaint();
            }
        } else {
            switch (keyCode) {
                case KeyEvent.VK_SHIFT:
                    shiftPressed = true;
                    break;
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    if (player.canMove()) {
                        movePlayer(0, -0.5, GameConstants.DIRECTION_UP);
                    }
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    if (player.canMove()) {
                        movePlayer(0, 0.5, GameConstants.DIRECTION_DOWN);
                    }
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    if (player.canMove()) {
                        movePlayer(-0.5, 0, GameConstants.DIRECTION_LEFT);
                    }
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    if (player.canMove()) {
                        movePlayer(0.5, 0, GameConstants.DIRECTION_RIGHT);
                    }
                    break;
                case KeyEvent.VK_E:
                    inventoryVisible = true;
                    break;
                case KeyEvent.VK_SPACE:
                    attackAtCursor();
                    break;
                case KeyEvent.VK_ESCAPE:  // МЕНЮ ПАУЗЫ
                    if (gameWindow != null) {
                        gameWindow.togglePause();
                    }
                    break;
                case KeyEvent.VK_P:  // Альтернативная клавиша паузы
                    if (gameWindow != null) {
                        gameWindow.togglePause();
                    }
                    break;
                case KeyEvent.VK_F5:
                    quickSave();
                    break;
                case KeyEvent.VK_F9:
                    quickLoad();
                    break;
                case KeyEvent.VK_1: 
                    inventoryPanel.setSelectedSlot(0); 
                    repaint();
                    break;
                case KeyEvent.VK_2: 
                    inventoryPanel.setSelectedSlot(1); 
                    repaint();
                    break;
                case KeyEvent.VK_3: 
                    inventoryPanel.setSelectedSlot(2); 
                    repaint();
                    break;
                case KeyEvent.VK_4: 
                    inventoryPanel.setSelectedSlot(3); 
                    repaint();
                    break;
                case KeyEvent.VK_5: 
                    inventoryPanel.setSelectedSlot(4); 
                    repaint();
                    break;
                case KeyEvent.VK_6: 
                    inventoryPanel.setSelectedSlot(5); 
                    repaint();
                    break;
                case KeyEvent.VK_7: 
                    inventoryPanel.setSelectedSlot(6); 
                    repaint();
                    break;
                case KeyEvent.VK_8: 
                    inventoryPanel.setSelectedSlot(7); 
                    repaint();
                    break;
                case KeyEvent.VK_9: 
                    inventoryPanel.setSelectedSlot(8); 
                    repaint();
                    break;
            }
        }
    }
    
    private void movePlayer(double dx, double dy, int newDirection) {
        double newX = player.getExactX() + dx;
        double newY = player.getExactY() + dy;
        
        int checkX = (int) Math.floor(newX);
        int checkY = (int) Math.floor(newY);
        
        if (checkX >= 0 && checkX < GameConstants.MAP_WIDTH && 
            checkY >= 0 && checkY < GameConstants.MAP_HEIGHT) {
            
            char terrain = map[checkY][checkX];
            boolean canPass = true;
            
            // Проверяем проходимость для структур
            if (terrain == GameConstants.STONE || terrain == GameConstants.GLASS) {
                canPass = false; // Стены и стекло непроходимы
            } else if (terrain == GameConstants.WATER) {
                canPass = false; // Вода непроходима
            }
            
            if (canPass) {
                player.move(dx, dy, newDirection, shiftPressed);
            }
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        if (keyCode == KeyEvent.VK_SHIFT) {
            shiftPressed = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    // ============ ГЕТТЕРЫ И СЕТТЕРЫ ============
    
    public void setWorldSeed(long seed) {
        this.worldSeed = seed;
        System.out.println("🌍 Установлен сид мира: " + seed);
        regenerateWorld();
    }
    
    public long getWorldSeed() {
        return worldSeed;
    }
    
    private void regenerateWorld() {
        System.out.println("🔄 Перегенерация мира с сидом: " + worldSeed);
        
        double oldX = player.getExactX();
        double oldY = player.getExactY();
        
        // Сбрасываем генератор структур
        structureGenerator.reset();
        
        generateWorld();
        generateRabbits();
        
        player = new Player((int)oldX, (int)oldY);
        centerCameraOnPlayer();
        
        // Генерируем структуры снова
        structureGenerator.generateStructures(map, worldSeed, player.getX(), player.getY());
        
        System.out.println("✅ Мир перегенерирован, игрок на позиции: " + oldX + ", " + oldY);
    }
   public SaveLoadManager getSaveManager() {
    return saveManager;
}
    public void regenerateWorldWithSeed(long seed) {
        this.worldSeed = seed;
        regenerateWorld();
    }
    
    public void setPlayerSpawnPosition(double x, double y) {
        System.out.println("🎯 Установка позиции спавна: " + x + ", " + y);
        
        this.player = new Player((int)x, (int)y);
        centerCameraOnPlayer();
        
        System.out.println("✅ Игрок перемещен на позицию: " + x + ", " + y);
    }
    
    public boolean isValidSpawnPosition(int x, int y) {
        if (x < 0 || x >= GameConstants.MAP_WIDTH || y < 0 || y >= GameConstants.MAP_HEIGHT) {
            return false;
        }
        
        char terrain = map[y][x];
        return terrain != GameConstants.WATER && terrain != GameConstants.TREE;
    }
    
    public double getPlayerX() {
        return player.getExactX();
    }
    
    public double getPlayerY() {
        return player.getExactY();
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public char[][] getMapData() {
        return map;
    }
    
    public int[][] getBiomeData() {
        return biomes;
    }
    
    public List<Rabbit> getRabbits() {
        return rabbits;
    }
    
    public StructureGenerator getStructureGenerator() {
        return structureGenerator;
    }
    
    public MultiplayerManager getMultiplayerManager() {
        return multiplayerManager;
    }
    
    public boolean isMultiplayer() {
        return isMultiplayer;
    }
    
    public boolean startMultiplayerGame(boolean createGame) {
        return startMultiplayerGame(createGame, "localhost");
    }
    
    // Метод для экспорта состояния мира в строку Base64
    public String exportWorldState() {
        try {
            GameSaveData saveData = new GameSaveData();
            
            // Заполняем данные как в saveGame
            saveData.setPlayerX(player.getExactX());
            saveData.setPlayerY(player.getExactY());
            saveData.setPlayerHealth(player.getHealth());
            saveData.setPlayerHunger(player.getHunger());
            saveData.setPlayerExperience(player.getExperience());
            saveData.setPlayerLevel(player.getLevel());
            saveData.setPlayerDirection(player.getDirection());
            
            saveData.setWorldSeed(worldSeed);
            
            char[][] mapCopy = new char[GameConstants.MAP_HEIGHT][GameConstants.MAP_WIDTH];
            for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
                System.arraycopy(map[y], 0, mapCopy[y], 0, GameConstants.MAP_WIDTH);
            }
            saveData.setMapData(mapCopy);
            
            int[][] biomeCopy = new int[GameConstants.MAP_HEIGHT][GameConstants.MAP_WIDTH];
            for (int y = 0; y < GameConstants.MAP_HEIGHT; y++) {
                System.arraycopy(biomes[y], 0, biomeCopy[y], 0, GameConstants.MAP_WIDTH);
            }
            saveData.setBiomeData(biomeCopy);
            
            List<RabbitSaveData> rabbitsData = new ArrayList<>();
            for (Rabbit rabbit : rabbits) {
                if (rabbit.isAlive()) {
                    RabbitSaveData rabbitData = new RabbitSaveData();
                    rabbitData.setX(rabbit.getX());
                    rabbitData.setY(rabbit.getY());
                    rabbitData.setHealth(rabbit.getHealth());
                    rabbitsData.add(rabbitData);
                }
            }
            saveData.setRabbitsData(rabbitsData);
            
            if (structureGenerator != null) {
                saveData.setHouseGenerated(structureGenerator.isHouseGenerated());
                int[] housePos = structureGenerator.getHousePosition();
                saveData.setHouseX(housePos[0]);
                saveData.setHouseY(housePos[1]);
            }
            
            saveData.setSaveName("multiplayer_sync_" + System.currentTimeMillis());
            saveData.setSaveTimestamp(System.currentTimeMillis());
            
            return SaveSerializer.serializeSaveData(saveData);
        } catch (Exception e) {
            System.err.println("❌ Ошибка экспорта состояния мира: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Метод для импорта состояния мира из строки Base64
    public void importWorldState(String serializedData) {
        try {
            GameSaveData saveData = SaveSerializer.deserializeSaveData(serializedData);
            if (saveData != null) {
                loadFromSave(saveData);
                System.out.println("✅ Состояние мира синхронизировано");
                showGameMessage("Мир синхронизирован с хостом", 2000);
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка импорта состояния мира: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Метод для синхронизации в мультиплеере
    public void syncWorldInMultiplayer() {
        if (isMultiplayer && multiplayerManager.isServer()) {
            String worldState = exportWorldState();
            if (worldState != null) {
                // Отправляем всем клиентам
                for (ClientHandler client : multiplayerManager.getNetworkManager().getClients()) {
                    multiplayerManager.sendWorldSave(client, worldState);
                }
            }
        }
    }
    
    public void updateGameState() {
        // Метод для обновления состояния игры (может быть вызван при выходе из паузы)
        centerCameraOnPlayer();
        repaint();
    }
}