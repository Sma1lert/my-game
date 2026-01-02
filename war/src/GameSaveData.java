
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameSaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Данные игрока
    private double playerX;
    private double playerY;
    private int playerHealth;
    private int playerHunger;
    private int playerExperience;
    private int playerLevel;
    private int playerDirection;
    
    // Данные мира
    private long worldSeed;
    private char[][] mapData;
    private int[][] biomeData;
    
    // Данные кроликов
    private List<RabbitSaveData> rabbitsData;
    
    // Данные структур
    private boolean houseGenerated;
    private int houseX;
    private int houseY;
    
    // Мета-данные
    private String saveName;
    private long saveTimestamp;
    private int gameVersion = 1;
    
    public GameSaveData() {
        this.rabbitsData = new ArrayList<>();
    }
    
    // Геттеры и сеттеры
    public double getPlayerX() { return playerX; }
    public void setPlayerX(double playerX) { this.playerX = playerX; }
    
    public double getPlayerY() { return playerY; }
    public void setPlayerY(double playerY) { this.playerY = playerY; }
    
    public int getPlayerHealth() { return playerHealth; }
    public void setPlayerHealth(int playerHealth) { this.playerHealth = playerHealth; }
    
    public int getPlayerHunger() { return playerHunger; }
    public void setPlayerHunger(int playerHunger) { this.playerHunger = playerHunger; }
    
    public int getPlayerExperience() { return playerExperience; }
    public void setPlayerExperience(int playerExperience) { this.playerExperience = playerExperience; }
    
    public int getPlayerLevel() { return playerLevel; }
    public void setPlayerLevel(int playerLevel) { this.playerLevel = playerLevel; }
    
    public int getPlayerDirection() { return playerDirection; }
    public void setPlayerDirection(int playerDirection) { this.playerDirection = playerDirection; }
    
    public long getWorldSeed() { return worldSeed; }
    public void setWorldSeed(long worldSeed) { this.worldSeed = worldSeed; }
    
    public char[][] getMapData() { return mapData; }
    public void setMapData(char[][] mapData) { this.mapData = mapData; }
    
    public int[][] getBiomeData() { return biomeData; }
    public void setBiomeData(int[][] biomeData) { this.biomeData = biomeData; }
    
    public List<RabbitSaveData> getRabbitsData() { return rabbitsData; }
    public void setRabbitsData(List<RabbitSaveData> rabbitsData) { this.rabbitsData = rabbitsData; }
    
    public boolean isHouseGenerated() { return houseGenerated; }
    public void setHouseGenerated(boolean houseGenerated) { this.houseGenerated = houseGenerated; }
    
    public int getHouseX() { return houseX; }
    public void setHouseX(int houseX) { this.houseX = houseX; }
    
    public int getHouseY() { return houseY; }
    public void setHouseY(int houseY) { this.houseY = houseY; }
    
    public String getSaveName() { return saveName; }
    public void setSaveName(String saveName) { this.saveName = saveName; }
    
    public long getSaveTimestamp() { return saveTimestamp; }
    public void setSaveTimestamp(long saveTimestamp) { this.saveTimestamp = saveTimestamp; }
    
    public int getGameVersion() { return gameVersion; }
    public void setGameVersion(int gameVersion) { this.gameVersion = gameVersion; }
}
