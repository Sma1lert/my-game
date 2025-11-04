public class Player {
    private int x;
    private int y;
    private int health;
    private int hunger;
    private int experience;
    private int level;
    private int direction; // направление взгляда
    
    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.health = GameConstants.MAX_HEALTH;
        this.hunger = GameConstants.MAX_HUNGER;
        this.experience = GameConstants.STARTING_XP;
        this.level = GameConstants.STARTING_LEVEL;
        this.direction = GameConstants.DIRECTION_DOWN; // по умолчанию смотрит вниз
    }
    
    // Геттеры
    public int getX() { return x; }
    public int getY() { return y; }
    public int getHealth() { return health; }
    public int getHunger() { return hunger; }
    public int getExperience() { return experience; }
    public int getLevel() { return level; }
    public int getDirection() { return direction; }
    
    // Сеттеры для движения с изменением направления
    public void setPosition(int newX, int newY, int newDirection) {
        this.x = newX;
        this.y = newY;
        this.direction = newDirection;
    }
    
    public void setDirection(int newDirection) {
        this.direction = newDirection;
    }
    
    // Методы для изменения состояния
    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }
    
    public void heal(int amount) {
        this.health = Math.min(GameConstants.MAX_HEALTH, this.health + amount);
    }
    
    public void decreaseHunger(int amount) {
        this.hunger = Math.max(0, this.hunger - amount);
        
        // Если голод равен 0, наносим урон
        if (this.hunger == 0 && this.health > 0) {
            takeDamage(1);
        }
    }
    
    public void increaseHunger(int amount) {
        this.hunger = Math.min(GameConstants.MAX_HUNGER, this.hunger + amount);
    }
    
    public void addExperience(int xp) {
        this.experience += xp;
    }
    
    public boolean isAlive() {
        return health > 0;
    }
    
    // Обновление состояния (вызывается каждый кадр)
    public void update() {
        // Голод уменьшается со временем
        if (Math.random() < 0.01) {
            decreaseHunger(1);
        }
    }
}