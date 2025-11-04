import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== War Game ===");
        System.out.println("Запуск игры...");
        
        // Запускаем в EDT для Swing
        SwingUtilities.invokeLater(() -> {
            try {
                new GameWindow();
                System.out.println("✅ Игра успешно запущена!");
            } catch (Exception e) {
                System.err.println("❌ Ошибка запуска игры: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Ошибка запуска игры: " + e.getMessage(), 
                    "War Game - Ошибка", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}