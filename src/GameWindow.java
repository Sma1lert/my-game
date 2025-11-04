import javax.swing.*;

public class GameWindow {
    private JFrame frame;
    private GamePanel gamePanel;
    
    public GameWindow() {
        initialize();
    }
    
    private void initialize() {
        frame = new JFrame("War Game - Survival Adventure");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        gamePanel = new GamePanel();
        frame.add(gamePanel);
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        gamePanel.startGame();
    }
}