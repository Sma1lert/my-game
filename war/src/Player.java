public class Player {
    private double x;
    private double y;
    private int health;
    private int hunger;
    private int experience;
    private int level;
    private int direction;
    
    private int moveCooldown = 0;
    private static final int MOVE_DELAY = 15;
    private static final int RUN_MOVE_DELAY = 8;
    private boolean isRunning = false;
    
    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.health = 100;
        this.hunger = GameConstants.MAX_HUNGER;
        this.experience = GameConstants.STARTING_XP;
        this.level = GameConstants.STARTING_LEVEL;
        this.direction = GameConstants.DIRECTION_DOWN;
    }
    
    // Существующие методы...
    
    // СЕТТЕРЫ ДЛЯ СОХРАНЕНИЙ
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setHealth(int health) { this.health = health; }
    public void setHunger(int hunger) { this.hunger = hunger; }
    public void setExperience(int experience) { this.experience = experience; }
    public void setLevel(int level) { this.level = level; }
    public void setDirection(int direction) { this.direction = direction; }
    
    
    // Геттеры для целочисленных координат (для карты)
    public int getX() { return (int) Math.floor(x); }
    public int getY() { return (int) Math.floor(y); }
    
    // Геттеры для дробных координат
    public double getExactX() { return x; }
    public double getExactY() { return y; }
    
    public int getHealth() { return health; }
    public int getHunger() { return hunger; }
    public int getExperience() { return experience; }
    public int getLevel() { return level; }
    public int getDirection() { return direction; }
    
    public boolean canMove() {
        return moveCooldown <= 0;
    }
    
    // Обновленный метод для движения с дробными шагами
    public void move(double dx, double dy, int newDirection, boolean running) {
        this.x += dx;
        this.y += dy;
        this.direction = newDirection;
        this.isRunning = running;
        
        if (running) {
            this.moveCooldown = RUN_MOVE_DELAY;
            // При беге тратим в 2 раза больше голода
            if (Math.random() < 0.3) { // 30% шанс потратить голод при беге
                decreaseHunger(2);
            }
        } else {
            this.moveCooldown = MOVE_DELAY;
            // При обычной ходьбе тратим обычное количество голода
            if (Math.random() < 0.15) { // 15% шанс потратить голод при ходьбе
                decreaseHunger(1);
            }
        }
    }
    
    // Методы для изменения состояния
    public void takeDamage(int damage) {
        this.health = Math.max(0, this.health - damage);
    }
    
    public void heal(int amount) {
        this.health = Math.min(100, this.health + amount);
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
    
    // Получить координаты атаки в зависимости от направления
    public int getAttackX() {
        switch (direction) {
            case GameConstants.DIRECTION_LEFT: return (int) Math.floor(x - 1);
            case GameConstants.DIRECTION_RIGHT: return (int) Math.floor(x + 1);
            default: return (int) Math.floor(x);
        }
    }
    
    public int getAttackY() {
        switch (direction) {
            case GameConstants.DIRECTION_UP: return (int) Math.floor(y - 1);
            case GameConstants.DIRECTION_DOWN: return (int) Math.floor(y + 1);
            default: return (int) Math.floor(y);
        }
    }
    // В класс Player добавьте:
public boolean canAttackTo(double targetX, double targetY) {
    double distance = Math.sqrt(Math.pow(targetX - x, 2) + Math.pow(targetY - y, 2));
    return distance <= GameConstants.ATTACK_RANGE;
}

public void setDirectionTowards(double targetX, double targetY) {
    double dx = targetX - x;
    double dy = targetY - y;
    
    // Определяем основное направление по большей компоненте
    if (Math.abs(dx) > Math.abs(dy)) {
        this.direction = (dx > 0) ? GameConstants.DIRECTION_RIGHT : GameConstants.DIRECTION_LEFT;
    } else {
        this.direction = (dy > 0) ? GameConstants.DIRECTION_DOWN : GameConstants.DIRECTION_UP;
    }
}

public int getAttackTargetX(double targetX) {
    double dx = targetX - x;
    double distance = Math.sqrt(dx * dx + 0); // Просто для нормализации
    if (distance > GameConstants.ATTACK_RANGE) {
        dx = dx / distance * GameConstants.ATTACK_RANGE;
    }
    return (int) Math.floor(x + dx);
}

public int getAttackTargetY(double targetY) {
    double dy = targetY - y;
    double distance = Math.sqrt(0 + dy * dy); // Просто для нормализации
    if (distance > GameConstants.ATTACK_RANGE) {
        dy = dy / distance * GameConstants.ATTACK_RANGE;
    }
    return (int) Math.floor(y + dy);
}
    // Обновление состояния (вызывается каждый кадр)
    public void update() {
        // Уменьшаем задержку движения
        if (moveCooldown > 0) {
            moveCooldown--;
        }
        
        // Голод уменьшается со временем (реже)
        if (Math.random() < 0.003) {
            decreaseHunger(1);
        }
    }
}