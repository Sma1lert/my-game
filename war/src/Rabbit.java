import java.util.Random;

public class Rabbit {
    private int x;
    private int y;
    private int health;
    private int moveCooldown;
    private Random random;
    public void setHealth(int health) {
    this.health = health;
}
    
    public Rabbit(int x, int y) {
        this.x = x;
        this.y = y;
        this.health = 4;
        this.random = new Random();
        this.moveCooldown = 0;
    }
    
    public void update(char[][] map) {
        if (moveCooldown > 0) {
            moveCooldown--;
            return;
        }
        
        // Случайное движение с меньшей вероятностью (20% вместо 30%)
        if (random.nextDouble() < 0.2) {
            int dx = random.nextInt(3) - 1;
            int dy = random.nextInt(3) - 1;
            
            int newX = x + dx;
            int newY = y + dy;
            
            if (newX >= 0 && newX < GameConstants.MAP_WIDTH && 
                newY >= 0 && newY < GameConstants.MAP_HEIGHT) {
                
                char terrain = map[newY][newX];
                if (terrain == GameConstants.GRASS) {
                    x = newX;
                    y = newY;
                }
            }
        }
        
        // Увеличиваем задержку перед следующим движением (20-40 кадров вместо 10-30)
        moveCooldown = 20 + random.nextInt(20);
    }
    
    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }
    
    public boolean isAlive() {
        return health > 0;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getHealth() {
        return health;
    }
}
