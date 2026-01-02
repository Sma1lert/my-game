import java.io.*;

public class SaveSerializer {
    public static String serializeSaveData(GameSaveData saveData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(saveData);
        }
        return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
    }
    
    public static GameSaveData deserializeSaveData(String serializedData) throws IOException, ClassNotFoundException {
        byte[] data = java.util.Base64.getDecoder().decode(serializedData);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (GameSaveData) ois.readObject();
        }
    }
}