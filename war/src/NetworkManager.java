
import java.io.*;
import java.net.*;
import java.util.*;

public class NetworkManager {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private boolean isServer = false;
    private int port = 27333;
    
    private MultiplayerManager multiplayerManager;
    private GamePanel gamePanel;
    
    // Для приема больших сохранений
    private Map<Integer, StringBuilder> saveDataBuffers;
    private Map<Integer, Integer> expectedChunks;
    
    public NetworkManager(MultiplayerManager multiplayerManager) {
        this.multiplayerManager = multiplayerManager;
        clients = new ArrayList<>();
        saveDataBuffers = new HashMap<>();
        expectedChunks = new HashMap<>();
    }
    
    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }
    
    public boolean startServer() {
        try {
            serverSocket = new ServerSocket(port);
            isServer = true;
            System.out.println("✅ Сервер запущен на порту " + port);
            System.out.println("📡 IP адрес для подключения: " + getLocalIP());
            
            new Thread(this::acceptConnections).start();
            return true;
        } catch (IOException e) {
            System.out.println("❌ Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean connectToServer(String ip) {
        try {
            System.out.println("🔄 Попытка подключения к " + ip + ":" + port);
            Socket socket = new Socket(ip, port);
            ClientHandler client = new ClientHandler(socket, false, multiplayerManager);
            clients.add(client);
            new Thread(client).start();
            System.out.println("✅ Подключено к серверу " + ip);
            return true;
        } catch (IOException e) {
            System.out.println("❌ Ошибка подключения к " + ip + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void acceptConnections() {
        while (!serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, true, multiplayerManager);
                clients.add(client);
                new Thread(client).start();
                System.out.println("🎮 Новый игрок подключился!");
                
                if (multiplayerManager != null) {
                    int newPlayerId = clients.size() + 1;
                    
                    // ВАЖНО: Получаем реальную позицию хоста
                    double hostX = 0, hostY = 0;
                    if (gamePanel != null) {
                        hostX = gamePanel.getPlayerX();
                        hostY = gamePanel.getPlayerY();
                        System.out.println("🎯 Позиция хоста: " + hostX + ", " + hostY);
                    }
                    
                    // Генерируем позицию рядом с хостом
                    int[] spawnPos = findSpawnPositionNearHost(hostX, hostY);
                    int newPlayerX = spawnPos[0];
                    int newPlayerY = spawnPos[1];
                    
                    multiplayerManager.addRemotePlayer(newPlayerId, newPlayerX, newPlayerY);
                    
                    // Отправляем новому игроку его ID, позицию и СИД МИРА
                    long worldSeed = gamePanel != null ? gamePanel.getWorldSeed() : System.currentTimeMillis();
                    client.sendMessage("PLAYER_ASSIGN:" + newPlayerId + ":" + newPlayerX + ":" + newPlayerY + ":" + worldSeed);
                    System.out.println("🎮 Создан удаленный игрок ID: " + newPlayerId + 
                                     " на позиции: " + newPlayerX + ", " + newPlayerY +
                                     " с сидом: " + worldSeed);
                    
                    // Также отдельно отправляем сид мира (на случай если PLAYER_ASSIGN не обработается)
                    client.sendMessage("WORLD_SEED:" + worldSeed);
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) {
                    System.out.println("❌ Ошибка принятия подключения: " + e.getMessage());
                }
            }
        }
    }
    
    private int[] findSpawnPositionNearHost(double hostX, double hostY) {
        int attempts = 0;
        int maxAttempts = 50;
        
        while (attempts < maxAttempts) {
            // Генерируем случайное смещение в радиусе 5-20 блоков
            double angle = Math.random() * 2 * Math.PI;
            double distance = 5 + Math.random() * 15;
            
            int spawnX = (int)(hostX + Math.cos(angle) * distance);
            int spawnY = (int)(hostY + Math.sin(angle) * distance);
            
            // Проверяем границы карты
            spawnX = Math.max(0, Math.min(spawnX, GameConstants.MAP_WIDTH - 1));
            spawnY = Math.max(0, Math.min(spawnY, GameConstants.MAP_HEIGHT - 1));
            
            // Проверяем, что позиция валидна (не вода, не дерево)
            if (gamePanel != null && gamePanel.isValidSpawnPosition(spawnX, spawnY)) {
                System.out.println("✅ Найдена валидная позиция для спавна: " + spawnX + ", " + spawnY);
                return new int[]{spawnX, spawnY};
            }
            
            attempts++;
        }
        
        // Если не нашли идеальное место, возвращаем позицию рядом без проверки
        System.out.println("⚠️ Не удалось найти идеальное место за " + maxAttempts + " попыток");
        int fallbackX = (int)hostX + 3;
        int fallbackY = (int)hostY + 3;
        fallbackX = Math.max(0, Math.min(fallbackX, GameConstants.MAP_WIDTH - 1));
        fallbackY = Math.max(0, Math.min(fallbackY, GameConstants.MAP_HEIGHT - 1));
        
        return new int[]{fallbackX, fallbackY};
    }
    
    public void broadcastMessage(String message) {
        Iterator<ClientHandler> iterator = clients.iterator();
        while (iterator.hasNext()) {
            ClientHandler client = iterator.next();
            if (client.isConnected()) {
                client.sendMessage(message);
            } else {
                iterator.remove();
                System.out.println("🔌 Удален отключившийся клиент");
            }
        }
    }
    
    public void sendToClient(ClientHandler targetClient, String message) {
        if (targetClient != null && targetClient.isConnected()) {
            targetClient.sendMessage(message);
        }
    }
    
    public boolean isServer() {
        return isServer;
    }
    
    public void disconnect() {
        try {
            for (ClientHandler client : clients) {
                client.disconnect();
            }
            clients.clear();
            
            if (serverSocket != null) {
                serverSocket.close();
            }
            System.out.println("🔌 Сетевое соединение закрыто");
        } catch (IOException e) {
            System.out.println("❌ Ошибка отключения: " + e.getMessage());
        }
    }
    
    public List<ClientHandler> getClients() {
        return clients;
    }
    
    private String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "неизвестен";
        }
    }
    
    public void sendPing() {
        broadcastMessage("PING");
        System.out.println("🏓 Отправлен ping для проверки связи");
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isServerSide;
    private boolean connected = true;
    private MultiplayerManager multiplayerManager;
    private int playerId;
    
    // Для сборки больших сохранений
    private StringBuilder saveDataBuffer;
    private int expectedSaveChunks = 0;
    private int receivedSaveChunks = 0;
    
    public ClientHandler(Socket socket, boolean isServerSide, MultiplayerManager multiplayerManager) {
        this.socket = socket;
        this.isServerSide = isServerSide;
        this.multiplayerManager = multiplayerManager;
        
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("❌ Ошибка создания клиента: " + e.getMessage());
        }
    }
    
    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null && connected) {
                System.out.println("📨 Получено сообщение: " + message);
                handleGameMessage(message);
            }
        } catch (IOException e) {
            System.out.println("❌ Ошибка чтения сообщения: " + e.getMessage());
        } finally {
            disconnect();
        }
    }
    
    private void handleGameMessage(String message) {
        if (multiplayerManager == null) return;
        
        if (message.startsWith("PLAYER_UPDATE:")) {
            handlePlayerUpdate(message);
        } else if (message.startsWith("PLAYER_ASSIGN:")) {
            handlePlayerAssign(message);
        } else if (message.startsWith("WORLD_SEED:")) {
            handleWorldSeed(message);
        } else if (message.startsWith("WORLD_SAVE_START:")) {
            handleWorldSaveStart(message);
        } else if (message.startsWith("WORLD_SAVE_CHUNK:")) {
            handleWorldSaveChunk(message);
        } else if (message.equals("WORLD_SAVE_END")) {
            handleWorldSaveEnd();
        } else if (message.equals("PING")) {
            sendMessage("PONG");
            System.out.println("🏓 Ответ на ping");
        } else if (message.equals("PONG")) {
            System.out.println("🏓 Получен pong от игрока " + playerId);
        }
    }
    
    private void handlePlayerUpdate(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 5) {
            try {
                int playerId = Integer.parseInt(parts[1]);
                
                // ИСПРАВЛЕНИЕ: Заменяем запятые на точки для корректного парсинга
                String xStr = parts[2].replace(',', '.');
                String yStr = parts[3].replace(',', '.');
                
                double x = Double.parseDouble(xStr);
                double y = Double.parseDouble(yStr);
                int direction = Integer.parseInt(parts[4]);
                
                multiplayerManager.updateRemotePlayer(playerId, x, y, direction);
                System.out.println("🔄 Обработка позиции игрока " + playerId + ": " + x + ", " + y);
            } catch (NumberFormatException e) {
                System.out.println("❌ Ошибка парсинга PLAYER_UPDATE: " + e.getMessage());
            }
        }
    }
    
    private void handlePlayerAssign(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 5) {
            try {
                this.playerId = Integer.parseInt(parts[1]);
                String xStr = parts[2].replace(',', '.');
                String yStr = parts[3].replace(',', '.');
                String seedStr = parts[4].replace(',', '.');
                
                double spawnX = Double.parseDouble(xStr);
                double spawnY = Double.parseDouble(yStr);
                long worldSeed = Long.parseLong(seedStr);
                
                System.out.println("🎮 Назначен ID игрока: " + playerId + 
                                 " с позицией спавна: " + spawnX + ", " + spawnY +
                                 " и сидом мира: " + worldSeed);
                
                // Устанавливаем позицию спавна и СИД МИРА для этого игрока
                if (multiplayerManager.getGamePanel() != null) {
                    multiplayerManager.getGamePanel().setPlayerSpawnPosition(spawnX, spawnY);
                    multiplayerManager.getGamePanel().setWorldSeed(worldSeed);
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Ошибка парсинга PLAYER_ASSIGN: " + e.getMessage());
            }
        }
    }
    
    private void handleWorldSeed(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            try {
                long worldSeed = Long.parseLong(parts[1]);
                System.out.println("🌍 Получен сид мира от хоста: " + worldSeed);
                
                // Устанавливаем сид мира на клиенте
                if (multiplayerManager.getGamePanel() != null) {
                    multiplayerManager.getGamePanel().setWorldSeed(worldSeed);
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Ошибка парсинга WORLD_SEED: " + e.getMessage());
            }
        }
    }
    
    private void handleWorldSaveStart(String message) {
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            try {
                expectedSaveChunks = Integer.parseInt(parts[1]);
                receivedSaveChunks = 0;
                saveDataBuffer = new StringBuilder();
                System.out.println("📥 Начало приема сохранения мира (частей: " + expectedSaveChunks + ")");
            } catch (NumberFormatException e) {
                System.out.println("❌ Ошибка парсинга WORLD_SAVE_START: " + e.getMessage());
            }
        }
    }
    
    private void handleWorldSaveChunk(String message) {
        String[] parts = message.split(":", 3);
        if (parts.length >= 3) {
            try {
                int chunkIndex = Integer.parseInt(parts[1]);
                String chunkData = parts[2];
                
                if (saveDataBuffer != null) {
                    saveDataBuffer.append(chunkData);
                    receivedSaveChunks++;
                    System.out.println("📥 Получена часть сохранения " + (chunkIndex + 1) + "/" + expectedSaveChunks);
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Ошибка парсинга WORLD_SAVE_CHUNK: " + e.getMessage());
            }
        }
    }
    
    private void handleWorldSaveEnd() {
        if (saveDataBuffer != null && receivedSaveChunks == expectedSaveChunks) {
            System.out.println("✅ Получено все сохранение мира (" + saveDataBuffer.length() + " байт)");
            
            // Импортируем сохранение
            if (multiplayerManager.getGamePanel() != null) {
                multiplayerManager.getGamePanel().importWorldState(saveDataBuffer.toString());
            }
            
            // Очищаем буфер
            saveDataBuffer = null;
            expectedSaveChunks = 0;
            receivedSaveChunks = 0;
        } else {
            System.out.println("❌ Не все части сохранения получены (" + receivedSaveChunks + "/" + expectedSaveChunks + ")");
        }
    }
    
    public void sendMessage(String message) {
        if (connected && out != null) {
            out.println(message);
        }
    }
    
    public void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            
            if (multiplayerManager != null && playerId > 0) {
                multiplayerManager.removeRemotePlayer(playerId);
            }
            System.out.println("🔌 Игрок " + playerId + " отключился");
        } catch (IOException e) {
            System.out.println("❌ Ошибка отключения клиента: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }
}
