import java.io.*;
import java.util.*;

public class SaveLoadManager {
    private static final String SAVE_DIRECTORY = "saves";
    private static final String SAVE_EXTENSION = ".gmsave";
    
    public SaveLoadManager() {
        createSaveDirectory();
    }
    
    private void createSaveDirectory() {
        File saveDir = new File(SAVE_DIRECTORY);
        if (!saveDir.exists()) {
            if (saveDir.mkdirs()) {
                System.out.println("✅ Создана директория сохранений: " + saveDir.getAbsolutePath());
            } else {
                System.out.println("❌ Не удалось создать директорию сохранений");
            }
        }
    }
    
    // Сохранение игры (новая версия)
    public boolean saveGame(GameSaveData saveData) {
        if (saveData == null) {
            System.out.println("❌ Данные сохранения не инициализированы");
            return false;
        }
        
        String saveName = saveData.getSaveName();
        if (saveName == null || saveName.trim().isEmpty()) {
            saveName = "save_" + System.currentTimeMillis();
            saveData.setSaveName(saveName);
        }
        
        try {
            String filename = SAVE_DIRECTORY + File.separator + saveName + SAVE_EXTENSION;
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(saveData);
                System.out.println("✅ Игра сохранена: " + filename);
                return true;
            }
        } catch (IOException e) {
            System.out.println("❌ Ошибка сохранения игры: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Загрузка игры (новая версия)
    public GameSaveData loadGame(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            System.out.println("❌ Имя сохранения не может быть пустым");
            return null;
        }
        
        String filename = SAVE_DIRECTORY + File.separator + saveName + SAVE_EXTENSION;
        File saveFile = new File(filename);
        
        if (!saveFile.exists()) {
            System.out.println("❌ Файл сохранения не найден: " + filename);
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            GameSaveData saveData = (GameSaveData) ois.readObject();
            System.out.println("✅ Игра загружена: " + filename);
            return saveData;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("❌ Ошибка загрузки игры: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Получение списка сохранений
    public List<String> getSaveList() {
        List<String> saveList = new ArrayList<>();
        File saveDir = new File(SAVE_DIRECTORY);
        
        if (saveDir.exists() && saveDir.isDirectory()) {
            File[] saveFiles = saveDir.listFiles((dir, name) -> name.endsWith(SAVE_EXTENSION));
            
            if (saveFiles != null) {
                for (File saveFile : saveFiles) {
                    String name = saveFile.getName();
                    name = name.substring(0, name.length() - SAVE_EXTENSION.length());
                    saveList.add(name);
                }
            }
        }
        
        Collections.sort(saveList);
        return saveList;
    }
    
    // Удаление сохранения
    public boolean deleteSave(String saveName) {
    try {
        File saveFile = new File(SAVES_DIR + saveName + ".sav");
        return saveFile.delete();
    } catch (Exception e) {
        System.err.println("❌ Ошибка удаления сохранения: " + e.getMessage());
        return false;
    }
}
    
    // Проверка существования сохранения
    public boolean saveExists(String saveName) {
        String filename = SAVE_DIRECTORY + File.separator + saveName + SAVE_EXTENSION;
        return new File(filename).exists();
    }
}