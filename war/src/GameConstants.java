import java.awt.Color;

public class GameConstants {
    // Размеры всей карты
    public static final int MAP_WIDTH = 1000;
    public static final int MAP_HEIGHT = 1000;

    // В класс GameConstants добавьте:
    public static final double ATTACK_RANGE = 3.0; // Дальность атаки в тайлах
    public static final int ATTACK_DAMAGE = 5;     // Урон атаки
    
    // Система атаки
    public static final int PLAYER_MAX_HEALTH = 100;
    public static final int RABBIT_HEALTH = 4;
    public static final int SWORD_DAMAGE = 5;
    public static final int RABBIT_EXPERIENCE = 10;
    
    // Размеры окна
    public static final int SCREEN_WIDTH = 1200;
    public static final int SCREEN_HEIGHT = 800;
    
    // Размер тайла в пикселях
    public static final int TILE_SIZE = 32;
    
    // Символы для отрисовки карты
    public static final char GRASS = '.';
    public static final char PLAYER = '@';
    public static final char TREE = 'T';
    public static final char WATER = '~';
    public static final char SOIL = '#';
    public static final char EMPTY = ' ';
    public static final char CAVE_ENTRANCE = 'C';
    public static final char ENEMY = 'E';
    
    // Новые типы terrain для структур
    public static final char STONE = 'S';
    public static final char WOOD_PLANK = 'P';
    public static final char GLASS = 'G';
    public static final char ROOFED = 'R'; // Тайлы с крышей
    
    public static final Color UI_BACKGROUND = new Color(20, 20, 30, 220);
    public static final Color UI_BORDER = new Color(80, 80, 120);
    public static final Color HEALTH_COLOR = new Color(220, 60, 60);
    public static final Color HEALTH_BG_COLOR = new Color(100, 30, 30);
    public static final Color HUNGER_COLOR = new Color(255, 165, 0);
    public static final Color HUNGER_BG_COLOR = new Color(100, 70, 0);
    public static final Color EXP_COLOR = new Color(100, 200, 255);
    public static final Color EXP_BG_COLOR = new Color(30, 80, 120);
    public static final Color TEXT_COLOR = new Color(240, 240, 240);
    public static final Color LABEL_COLOR = new Color(180, 180, 220);

    public static final int UI_PANEL_HEIGHT = 150;
    public static final int UI_MARGIN = 15;
    public static final int UI_ELEMENT_HEIGHT = 30;
    public static final int UI_BAR_WIDTH = 200;
    public static final int UI_BAR_HEIGHT = 20;

    // Настройки игрока
    public static final int MAX_HEALTH = 20;
    public static final int MAX_HUNGER = 20;
    public static final int STARTING_XP = 0;
    public static final int STARTING_LEVEL = 1;
    public static final int STARTING_HP = 20;
    
    // Направления персонажа
    public static final int DIRECTION_DOWN = 0;
    public static final int DIRECTION_UP = 1;
    public static final int DIRECTION_LEFT = 2;
    public static final int DIRECTION_RIGHT = 3;
    
    // Названия файлов текстур для направлений
    public static final String PLAYER_TEXTURE_DOWN = "player_down";
    public static final String PLAYER_TEXTURE_UP = "player_up";
    public static final String PLAYER_TEXTURE_LEFT = "player_left";
    public static final String PLAYER_TEXTURE_RIGHT = "player_right";
    
    // Текстуры terrain
    public static final String TEXTURE_GRASS = "grass";
    public static final String TEXTURE_TREE = "tree";
    public static final String TEXTURE_WATER = "water";
    
    // Текстуры для структур
    public static final String TEXTURE_STONE = "stone";
    public static final String TEXTURE_WOOD_PLANK = "wood_plank";
    public static final String TEXTURE_GLASS = "glass";
    public static final String TEXTURE_ROOF = "roof"; // Текстура крыши
    
    // ID предметов (только меч)
    public static final int ITEM_SWORD = 1;
    
    // Предметы в игре (только меч)
    public static final String[] ITEM_NAMES = {
        "Меч"
    };
    
    // Текстуры для предметов (только меч)
    public static final String[] ITEM_TEXTURES = {
        "sword"
    };
    
    // Размер инвентаря
    public static final int INVENTORY_ROWS = 4;
    public static final int INVENTORY_COLS = 9;
    public static final int INVENTORY_SLOT_SIZE = 40;
    
    // Цвета
    public static final Color BACKGROUND_COLOR = new Color(60, 60, 60);
    
    // Получить название текстуры по ID предмета
    public static String getTextureName(int itemId) {
        if (itemId > 0 && itemId <= ITEM_TEXTURES.length) {
            return ITEM_TEXTURES[itemId - 1];
        }
        return null;
    }
    
    // Получить текстуру для terrain
    public static String getTerrainTexture(char terrain) {
        switch (terrain) {
            case GRASS: return TEXTURE_GRASS;
            case TREE: return TEXTURE_TREE;
            case WATER: return TEXTURE_WATER;
            case STONE: return TEXTURE_STONE;
            case WOOD_PLANK: return TEXTURE_WOOD_PLANK;
            case GLASS: return TEXTURE_GLASS;
            case ROOFED: return TEXTURE_ROOF; // Крыша для тайлов с крышей
            default: return null;
        }
    }
}