import java.util.*;

public class MultiplayerManager {
    private NetworkManager network;
    private Map<Integer, MultiplayerPlayer> remotePlayers;
    private int localPlayerId;
    private boolean isMultiplayer = false;
    private GamePanel gamePanel;
 
    public MultiplayerManager() {
        remotePlayers = new HashMap<>();
        network = new NetworkManager(this);
    }
    
    // Установка ссылки на игровую панель
    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        network.setGamePanel(gamePanel);
    }
    
    // Получение ссылки на игровую панель
    public GamePanel getGamePanel() {
        return gamePanel;
    }
    // Получение сетевого менеджера
    public NetworkManager getNetworkManager() {
        return network;
    }
    
    public boolean createGame() {
        if (network.startServer()) {
            isMultiplayer = true;
            localPlayerId = 1; // Хост - игрок 1
            
            // Устанавливаем и отправляем сид мира
            if (gamePanel != null) {
                long worldSeed = System.currentTimeMillis();
                gamePanel.setWorldSeed(worldSeed);
                sendWorldSeed(worldSeed);
                System.out.println("🎮 Создана мультиплеерная игра с сидом: " + worldSeed);
            } else {
                System.out.println("🎮 Создана мультиплеерная игра (GamePanel не установлен)");
            }
            
            return true;
        }
        return false;
    }
    
    public boolean joinGame(String ip) {
        if (network.connectToServer(ip)) {
            isMultiplayer = true;
            localPlayerId = 2; // Клиент - игрок 2
            System.out.println("🎮 Присоединились к игре " + ip);
            return true;
        }
        return false;
    }
    
    public void updatePlayerPosition(double x, double y, int direction) {
        if (isMultiplayer) {
            // Используем Locale.US для гарантии точки как разделителя
            String message = String.format(java.util.Locale.US, "PLAYER_UPDATE:%d:%.2f:%.2f:%d", 
                localPlayerId, x, y, direction);
            network.broadcastMessage(message);
            System.out.println("📤 Отправлена позиция: " + message);
        }
    }
    
    public void sendWorldSeed(long worldSeed) {
    if (isMultiplayer) {
        String message = "WORLD_SEED:" + worldSeed;
        network.broadcastMessage(message);
        System.out.println("🌍 Отправлен сид мира: " + worldSeed);
    }
}

// Отправка сида мира конкретному клиенту
public void sendWorldSeedToClient(ClientHandler client, long worldSeed) {
    if (isMultiplayer && client != null) {
        String message = "WORLD_SEED:" + worldSeed;
        network.sendToClient(client, message);
        System.out.println("🌍 Отправлен сид мира клиенту " + client.getPlayerId() + ": " + worldSeed);
    }
}
    
    public void addRemotePlayer(int playerId, double x, double y) {
    if (!remotePlayers.containsKey(playerId)) {
        remotePlayers.put(playerId, new MultiplayerPlayer(playerId, x, y));
        System.out.println("🎮 Добавлен удаленный игрок ID: " + playerId + " на позиции: " + x + ", " + y);
        
        // Отправляем сид мира новому игроку, если мы хост
        if (isServer() && gamePanel != null) {
            long worldSeed = gamePanel.getWorldSeed();
            // Находим клиента по ID и отправляем ему сид
            for (ClientHandler client : network.getClients()) {
                if (client.getPlayerId() == playerId) {
                    sendWorldSeedToClient(client, worldSeed);
                    break;
                }
            }
        }
    }
}
    
    public void updateRemotePlayer(int playerId, double x, double y, int direction) {
        MultiplayerPlayer player = remotePlayers.get(playerId);
        if (player != null) {
            player.updatePosition(x, y, direction);
        } else {
            // Если игрок не найден, создаем нового
            System.out.println("🎮 Игрок не найден, создаем нового: ID=" + playerId);
            addRemotePlayer(playerId, x, y);
            updateRemotePlayer(playerId, x, y, direction);
        }
    }
    
    public void removeRemotePlayer(int playerId) {
        MultiplayerPlayer removedPlayer = remotePlayers.remove(playerId);
        if (removedPlayer != null) {
            System.out.println("🎮 Игрок отключился ID: " + playerId);
        }
    }
    
    public Collection<MultiplayerPlayer> getRemotePlayers() {
        return remotePlayers.values();
    }
    
    public boolean isMultiplayer() {
        return isMultiplayer;
    }
    
    public boolean isServer() {
        return network.isServer();
    }
    
    public int getLocalPlayerId() {
        return localPlayerId;
    }
    
    public void disconnect() {
        network.disconnect();
        remotePlayers.clear();
        isMultiplayer = false;
        System.out.println("🔌 Мультиплеер отключен");
    }
    
    // Метод для проверки связи
    public void sendPing() {
        if (isMultiplayer) {
            network.sendPing();
        }
    }
    
    // Метод для проверки статуса подключения
    public void checkConnectionStatus() {
        if (isMultiplayer) {
            System.out.println("📡 Статус подключения:");
            System.out.println("   - Режим: " + (isServer() ? "ХОСТ" : "КЛИЕНТ"));
            System.out.println("   - Локальный ID: " + localPlayerId);
            System.out.println("   - Удаленных игроков: " + remotePlayers.size());
            
            // Выводим информацию о каждом удаленном игроке
            for (MultiplayerPlayer player : remotePlayers.values()) {
                System.out.println("   - Игрок " + player.getPlayerId() + " (" + player.getName() + "): " + 
                                 String.format("%.1f", player.getX()) + ", " + String.format("%.1f", player.getY()));
            }
            
            // Информация о клиентах
            System.out.println("   - Подключенных клиентов: " + network.getClients().size());
        } else {
            System.out.println("📡 Мультиплеер не активен");
        }
    }
}

class MultiplayerPlayer {
    private int playerId;
    private double x, y;
    private int direction;
    private String name;
    
    public MultiplayerPlayer(int playerId, double x, double y) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.name = "Игрок " + playerId;
        this.direction = GameConstants.DIRECTION_DOWN;
    }
    
    public void updatePosition(double x, double y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }
    
    // Геттеры
    public int getPlayerId() { 
        return playerId; 
    }
    
    public double getX() { 
        return x; 
    }
    
    public double getY() { 
        return y; 
    }
    
    public int getDirection() { 
        return direction; 
    }
    
    public String getName() { 
        return name; 
    }
    
    // Сеттеры
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDirection(int direction) {
        this.direction = direction;
    }
}