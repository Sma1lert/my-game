import java.io.Serializable;

public class RabbitSaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int x;
    private int y;
    private int health;
    
    public RabbitSaveData() {}
    
    public RabbitSaveData(Rabbit rabbit) {
        this.x = rabbit.getX();
        this.y = rabbit.getY();
        this.health = rabbit.getHealth();
    }
    
    // Геттеры и сеттеры
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }
}