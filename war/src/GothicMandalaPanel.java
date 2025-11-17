import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class GothicMandalaPanel extends JPanel {
    private double rotationAngle = 0;
    private Timer animationTimer;
    private static final int SIZE = 512;

    public GothicMandalaPanel() {
    setPreferredSize(new Dimension(SIZE, SIZE));
    setBackground(new Color(5, 5, 15));
    
    // Создаем таймер, но не запускаем его сразу
    animationTimer = new Timer(40, e -> {
        rotationAngle += 0.01;
        repaint();
    });
    // Убрали animationTimer.start() из конструктора
}
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Включение сглаживания
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int centerX = SIZE / 2;
        int centerY = SIZE / 2;
        
        AffineTransform originalTransform = g2d.getTransform();
        g2d.rotate(rotationAngle, centerX, centerY);
        
        drawGothicMandala(g2d, centerX, centerY);
        
        g2d.setTransform(originalTransform);
    }
    
    private void drawGothicMandala(Graphics2D g2d, int centerX, int centerY) {
        // 1. ВНЕШНИЙ КРУГ - Готические арки и шпили
        drawGothicOuterRing(g2d, centerX, centerY, 240);
        
        // 2. КРУГ РОЗ ВЕТРОВ - Готические розы и символы
        drawRoseWindowRing(g2d, centerX, centerY, 190);
        
        // 3. КРУГ ВИТРАЖЕЙ - Сложные витражные узоры
        drawStainedGlassRing(g2d, centerX, centerY, 150);
        
        // 4. КРУГ ГОТИЧЕСКОЙ АРХИТЕКТУРЫ - Соборы и замки
        drawGothicArchitectureRing(g2d, centerX, centerY, 110);
        
        // 5. КРУГ МИСТИЧЕСКИХ СУЩЕСТВ - Горгульи и химеры
        drawGargoyleRing(g2d, centerX, centerY, 70);
        
        // 6. ЦЕНТР - Большая готическая роза
        drawGothicRoseCenter(g2d, centerX, centerY, 35);
    }
    
    private void drawGothicOuterRing(Graphics2D g2d, int centerX, int centerY, int radius) {
        // Основной круг с готическим орнаментом
        g2d.setColor(new Color(30, 10, 50));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // 8 готических арок
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 4 * i;
            drawGothicArch(g2d, centerX, centerY, radius - 20, angle, i);
        }
        
        // Острые шпили между арками
        for (int i = 0; i < 16; i++) {
            double angle = Math.PI / 8 * i;
            if (i % 2 == 1) { // Только на нечетных позициях
                drawGothicSpire(g2d, centerX, centerY, radius - 10, angle);
            }
        }
    }
    
    private void drawGothicArch(Graphics2D g2d, int centerX, int centerY, int radius, double angle, int archType) {
        int x = centerX + (int)(Math.cos(angle) * radius);
        int y = centerY + (int)(Math.sin(angle) * radius);
        
        g2d.setColor(new Color(80, 40, 120));
        
        switch(archType % 4) {
            case 0: // Стрельчатая арка
                int[] archX = {x - 15, x, x + 15};
                int[] archY = {y, y - 25, y};
                g2d.drawPolyline(archX, archY, 3);
                // Колонны
                g2d.fillRect(x - 12, y, 4, 20);
                g2d.fillRect(x + 8, y, 4, 20);
                break;
                
            case 1: // Ланцетовидная арка
                int[] lancetX = {x - 12, x, x + 12};
                int[] lancetY = {y, y - 30, y};
                g2d.drawPolyline(lancetX, lancetY, 3);
                // Узкие колонны
                g2d.fillRect(x - 10, y, 2, 25);
                g2d.fillRect(x + 8, y, 2, 25);
                break;
                
            case 2: // Килевидная арка
                int[] ogeeX = {x - 15, x - 5, x, x + 5, x + 15};
                int[] ogeeY = {y, y - 20, y - 25, y - 20, y};
                g2d.drawPolyline(ogeeX, ogeeY, 5);
                break;
                
            case 3: // Многолопастная арка
                int[] multiX = {x - 15, x - 8, x, x + 8, x + 15};
                int[] multiY = {y, y - 15, y - 25, y - 15, y};
                g2d.drawPolyline(multiX, multiY, 5);
                // Дополнительные арки
                g2d.drawArc(x - 12, y - 10, 10, 20, 0, -180);
                g2d.drawArc(x + 2, y - 10, 10, 20, 0, -180);
                break;
        }
    }
    
    private void drawGothicSpire(Graphics2D g2d, int centerX, int centerY, int radius, double angle) {
        int x = centerX + (int)(Math.cos(angle) * radius);
        int y = centerY + (int)(Math.sin(angle) * radius);
        
        g2d.setColor(new Color(200, 200, 220));
        
        // Основание шпиля
        g2d.fillRect(x - 2, y, 4, 15);
        
        // Острый шпиль
        int[] spireX = {x - 3, x, x + 3};
        int[] spireY = {y + 15, y - 10, y + 15};
        g2d.fillPolygon(spireX, spireY, 3);
        
        // Крест на вершине
        g2d.drawLine(x, y - 10, x, y - 15);
        g2d.drawLine(x - 2, y - 12, x + 2, y - 12);
    }
    
    private void drawRoseWindowRing(Graphics2D g2d, int centerX, int centerY, int radius) {
        // Основной круг для розеток
        g2d.setColor(new Color(60, 20, 80));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // 8 готических роз
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 4 * i;
            drawGothicRose(g2d, centerX, centerY, radius - 15, angle, i);
        }
    }
    
    private void drawGothicRose(Graphics2D g2d, int centerX, int centerY, int radius, double angle, int roseType) {
        int x = centerX + (int)(Math.cos(angle) * radius);
        int y = centerY + (int)(Math.sin(angle) * radius);
        
        g2d.setColor(new Color(120, 40, 160));
        
        switch(roseType % 4) {
            case 0: // Простая готическая роза
                // Внешний круг
                g2d.drawOval(x - 12, y - 12, 24, 24);
                // Внутренний узор
                for (int i = 0; i < 8; i++) {
                    double petalAngle = Math.PI / 4 * i;
                    int px = x + (int)(Math.cos(petalAngle) * 8);
                    int py = y + (int)(Math.sin(petalAngle) * 8);
                    g2d.drawLine(x, y, px, py);
                }
                break;
                
            case 1: // Роза с трилистниками
                g2d.drawOval(x - 10, y - 10, 20, 20);
                // Трилистники по кругу
                for (int i = 0; i < 6; i++) {
                    double leafAngle = Math.PI / 3 * i;
                    int lx = x + (int)(Math.cos(leafAngle) * 12);
                    int ly = y + (int)(Math.sin(leafAngle) * 12);
                    drawTrefoil(g2d, lx, ly, 4);
                }
                break;
                
            case 2: // Сложная роза с ажурным узором
                g2d.drawOval(x - 15, y - 15, 30, 30);
                // Внешние лепестки
                for (int i = 0; i < 12; i++) {
                    double petalAngle = Math.PI / 6 * i;
                    int px1 = x + (int)(Math.cos(petalAngle) * 12);
                    int py1 = y + (int)(Math.sin(petalAngle) * 12);
                    int px2 = x + (int)(Math.cos(petalAngle) * 18);
                    int py2 = y + (int)(Math.sin(petalAngle) * 18);
                    g2d.drawLine(px1, py1, px2, py2);
                }
                // Внутренние соединения
                for (int i = 0; i < 6; i++) {
                    double connAngle = Math.PI / 3 * i;
                    int cx1 = x + (int)(Math.cos(connAngle) * 18);
                    int cy1 = y + (int)(Math.sin(connAngle) * 18);
                    int cx2 = x + (int)(Math.cos(connAngle + Math.PI/3) * 18);
                    int cy2 = y + (int)(Math.sin(connAngle + Math.PI/3) * 18);
                    g2d.drawLine(cx1, cy1, cx2, cy2);
                }
                break;
                
            case 3: // Роза с витражным узором
                g2d.setColor(new Color(160, 80, 200));
                g2d.fillOval(x - 14, y - 14, 28, 28);
                
                g2d.setColor(new Color(30, 10, 50));
                // Радиальные линии
                for (int i = 0; i < 8; i++) {
                    double lineAngle = Math.PI / 4 * i;
                    int lx = x + (int)(Math.cos(lineAngle) * 14);
                    int ly = y + (int)(Math.sin(lineAngle) * 14);
                    g2d.drawLine(x, y, lx, ly);
                }
                // Концентрические круги
                g2d.drawOval(x - 10, y - 10, 20, 20);
                g2d.drawOval(x - 6, y - 6, 12, 12);
                break;
        }
    }
    
    private void drawTrefoil(Graphics2D g2d, int x, int y, int size) {
        // Трилистник - символ готики
        for (int i = 0; i < 3; i++) {
            double leafAngle = Math.PI * 2 / 3 * i;
            int lx = x + (int)(Math.cos(leafAngle) * size);
            int ly = y + (int)(Math.sin(leafAngle) * size);
            g2d.fillOval(lx - size/2, ly - size/2, size, size);
        }
    }
    
    private void drawStainedGlassRing(Graphics2D g2d, int centerX, int centerY, int radius) {
        // Фон для витражного кольца
        g2d.setColor(new Color(20, 10, 40, 200));
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // 12 витражных сегментов
        for (int i = 0; i < 12; i++) {
            Color[] stainedColors = {
                new Color(150, 0, 0, 150),     // Бордовый
                new Color(0, 0, 150, 150),     // Синий
                new Color(0, 100, 0, 150),     // Зеленый
                new Color(120, 0, 120, 150),   // Фиолетовый
                new Color(200, 100, 0, 150),   // Золотой
                new Color(0, 120, 120, 150)    // Бирюзовый
            };
            
            g2d.setColor(stainedColors[i % 6]);
            
            Arc2D segment = new Arc2D.Double(
                centerX - radius, centerY - radius, 
                radius * 2, radius * 2,
                i * 30, 30, Arc2D.PIE
            );
            g2d.fill(segment);
        }
        
        // Свинцовые переплеты витража
        g2d.setColor(new Color(40, 40, 40));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Радиальные переплеты
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI / 6 * i;
            int x1 = centerX + (int)(Math.cos(angle) * radius);
            int y1 = centerY + (int)(Math.sin(angle) * radius);
            g2d.drawLine(centerX, centerY, x1, y1);
        }
        
        // Концентрические переплеты
        g2d.drawOval(centerX - radius/2, centerY - radius/2, radius, radius);
        g2d.drawOval(centerX - radius*3/4, centerY - radius*3/4, radius*3/2, radius*3/2);
    }
    
    private void drawGothicArchitectureRing(Graphics2D g2d, int centerX, int centerY, int radius) {
        g2d.setColor(new Color(50, 30, 70));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // 8 готических сооружений
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 4 * i;
            drawGothicStructure(g2d, centerX, centerY, radius - 15, angle, i);
        }
    }
    
    private void drawGothicStructure(Graphics2D g2d, int centerX, int centerY, int radius, double angle, int structureType) {
        int x = centerX + (int)(Math.cos(angle) * radius);
        int y = centerY + (int)(Math.sin(angle) * radius);
        
        g2d.setColor(new Color(100, 80, 140));
        
        switch(structureType % 4) {
            case 0: // Готический собор
                // Главный неф
                g2d.fillRect(x - 8, y - 20, 16, 20);
                // Башни
                g2d.fillRect(x - 12, y - 30, 4, 30);
                g2d.fillRect(x + 8, y - 30, 4, 30);
                // Шпили
                int[] spire1 = {x - 11, x - 10, x - 9};
                int[] spireY1 = {y - 30, y - 40, y - 30};
                int[] spire2 = {x + 9, x + 10, x + 11};
                int[] spireY2 = {y - 30, y - 40, y - 30};
                g2d.fillPolygon(spire1, spireY1, 3);
                g2d.fillPolygon(spire2, spireY2, 3);
                // Розовое окно
                g2d.setColor(new Color(200, 150, 255));
                g2d.fillOval(x - 5, y - 15, 10, 10);
                break;
                
            case 1: // Готический замок
                // Основная башня
                g2d.fillRect(x - 6, y - 25, 12, 25);
                // Зубцы
                for (int i = 0; i < 4; i++) {
                    g2d.fillRect(x - 6 + i * 4, y - 25, 2, 4);
                }
                // Боковые башенки
                g2d.fillRect(x - 15, y - 15, 5, 15);
                g2d.fillRect(x + 10, y - 15, 5, 15);
                // Окна-бойницы
                g2d.setColor(new Color(180, 180, 220));
                g2d.fillRect(x - 4, y - 15, 2, 8);
                g2d.fillRect(x + 2, y - 15, 2, 8);
                break;
                
            case 2: // Аббатство
                // Главное здание
                g2d.fillRect(x - 10, y - 15, 20, 15);
                // Колокольня
                g2d.fillRect(x + 5, y - 30, 8, 30);
                // Арочные окна
                g2d.setColor(new Color(150, 150, 200));
                for (int i = 0; i < 3; i++) {
                    g2d.fillArc(x - 8 + i * 8, y - 10, 4, 8, 0, -180);
                }
                // Крест на колокольне
                g2d.setColor(Color.WHITE);
                g2d.drawLine(x + 9, y - 30, x + 9, y - 35);
                g2d.drawLine(x + 7, y - 32, x + 11, y - 32);
                break;
                
            case 3: // Руины готического храма
                // Уцелевшие арки
                g2d.drawArc(x - 12, y - 8, 24, 16, 0, -180);
                g2d.drawArc(x - 8, y - 12, 16, 24, 90, 180);
                // Обломки колонн
                g2d.fillRect(x - 14, y, 3, 8);
                g2d.fillRect(x + 11, y, 3, 8);
                g2d.fillRect(x - 5, y + 5, 10, 3);
                // Плющ на руинах
                g2d.setColor(new Color(40, 120, 40));
                for (int i = 0; i < 4; i++) {
                    int ivyX = x - 10 + i * 7;
                    g2d.fillOval(ivyX, y - 5, 6, 6);
                }
                break;
        }
    }
    
    private void drawGargoyleRing(Graphics2D g2d, int centerX, int centerY, int radius) {
        g2d.setColor(new Color(70, 40, 90));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // 12 горгулий и химер
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI / 6 * i;
            drawGargoyle(g2d, centerX, centerY, radius - 10, angle, i);
        }
    }
    
    private void drawGargoyle(Graphics2D g2d, int centerX, int centerY, int radius, double angle, int gargoyleType) {
        int x = centerX + (int)(Math.cos(angle) * radius);
        int y = centerY + (int)(Math.sin(angle) * radius);
        
        g2d.setColor(new Color(80, 80, 100));
        
        switch(gargoyleType % 6) {
            case 0: // Классическая горгулья
                // Тело
                g2d.fillOval(x - 6, y - 4, 12, 8);
                // Крылья
                g2d.fillRect(x - 10, y - 6, 8, 3);
                g2d.fillRect(x + 2, y - 6, 8, 3);
                // Голова с рогами
                g2d.fillOval(x - 4, y - 8, 8, 6);
                // Рога
                g2d.drawLine(x - 3, y - 8, x - 5, y - 12);
                g2d.drawLine(x + 3, y - 8, x + 5, y - 12);
                break;
                
            case 1: // Драконоподобная химера
                // Длинное тело
                g2d.fillRect(x - 8, y - 3, 16, 6);
                // Крылья дракона
                int[] wing1X = {x - 8, x - 15, x - 8};
                int[] wing1Y = {y - 3, y - 8, y - 10};
                int[] wing2X = {x + 8, x + 15, x + 8};
                int[] wing2Y = {y - 3, y - 8, y - 10};
                g2d.fillPolygon(wing1X, wing1Y, 3);
                g2d.fillPolygon(wing2X, wing2Y, 3);
                // Голова дракона
                g2d.fillOval(x - 5, y - 10, 10, 8);
                // Пасть
                g2d.setColor(Color.RED);
                g2d.drawArc(x - 3, y - 8, 6, 4, 0, -180);
                break;
                
            case 2: // Демоническая фигура
                // Человекоподобное тело
                g2d.fillOval(x - 4, y, 8, 10);
                // Голова с рогами
                g2d.fillOval(x - 3, y - 6, 6, 6);
                // Рога
                for (int i = 0; i < 3; i++) {
                    g2d.drawLine(x - 2 + i, y - 6, x - 3 + i, y - 10);
                }
                // Крылья летучей мыши
                int[] batWing1 = {x - 4, x - 12, x - 4};
                int[] batWing1Y = {y, y - 8, y - 12};
                int[] batWing2 = {x + 4, x + 12, x + 4};
                int[] batWing2Y = {y, y - 8, y - 12};
                g2d.fillPolygon(batWing1, batWing1Y, 3);
                g2d.fillPolygon(batWing2, batWing2Y, 3);
                break;
                
            case 3: // Ворон-горгулья
                // Тело птицы
                g2d.fillOval(x - 5, y - 3, 10, 6);
                // Голова
                g2d.fillOval(x - 3, y - 8, 6, 6);
                // Клюв
                g2d.setColor(new Color(200, 150, 0));
                g2d.fillRect(x, y - 7, 4, 2);
                // Крылья
                g2d.setColor(new Color(60, 60, 80));
                for (int i = 0; i < 5; i++) {
                    g2d.drawLine(x - 5, y - 2, x - 10, y - 8 + i);
                    g2d.drawLine(x + 5, y - 2, x + 10, y - 8 + i);
                }
                break;
                
            case 4: // Змеевидная горгулья
                // Извивающееся тело
                for (int i = 0; i < 3; i++) {
                    g2d.fillOval(x - 8 + i * 4, y - 2 + (i % 2) * 2, 6, 4);
                }
                // Голова змеи
                g2d.fillOval(x - 10, y - 4, 8, 6);
                // Раздвоенный язык
                g2d.setColor(Color.RED);
                g2d.drawLine(x - 6, y - 1, x - 8, y - 3);
                g2d.drawLine(x - 6, y - 1, x - 8, y + 1);
                // Глаза
                g2d.setColor(Color.YELLOW);
                g2d.fillOval(x - 8, y - 3, 2, 2);
                break;
                
            case 5: // Козлоголовая химера
                // Тело
                g2d.fillRect(x - 6, y, 12, 8);
                // Голова козла
                g2d.fillOval(x - 4, y - 8, 8, 8);
                // Рога
                g2d.drawLine(x - 2, y - 8, x - 6, y - 15);
                g2d.drawLine(x - 2, y - 8, x - 8, y - 12);
                g2d.drawLine(x + 2, y - 8, x + 6, y - 15);
                g2d.drawLine(x + 2, y - 8, x + 8, y - 12);
                // Копыта
                g2d.fillRect(x - 8, y + 8, 4, 3);
                g2d.fillRect(x + 4, y + 8, 4, 3);
                break;
        }
    }
    
    private void drawGothicRoseCenter(Graphics2D g2d, int centerX, int centerY, int radius) {
        // Фон центральной розы
        RadialGradientPaint gradient = new RadialGradientPaint(
            centerX, centerY, radius,
            new float[]{0.0f, 0.7f, 1.0f},
            new Color[]{new Color(100, 0, 100), new Color(60, 0, 80), new Color(30, 0, 50)}
        );
        g2d.setPaint(gradient);
        g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Сложная готическая роза
        g2d.setColor(new Color(200, 180, 255));
        g2d.setStroke(new BasicStroke(2));
        
        // Внешний круг розы
        g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Лепестки розы (16 лепестков)
        for (int i = 0; i < 16; i++) {
            double angle = Math.PI / 8 * i;
            
            // Внешние лепестки
            int outerX = centerX + (int)(Math.cos(angle) * radius);
            int outerY = centerY + (int)(Math.sin(angle) * radius);
            
            // Средние лепестки
            int midX = centerX + (int)(Math.cos(angle) * (radius * 0.7));
            int midY = centerY + (int)(Math.sin(angle) * (radius * 0.7));
            
            // Внутренние лепестки
            int innerX = centerX + (int)(Math.cos(angle) * (radius * 0.4));
            int innerY = centerY + (int)(Math.sin(angle) * (radius * 0.4));
            
            // Острые лепестки
            if (i % 2 == 0) {
                // Длинные острые лепестки
                int[] petalX = {centerX, outerX, centerX + (int)(Math.cos(angle + Math.PI/16) * radius * 0.8)};
                int[] petalY = {centerY, outerY, centerY + (int)(Math.sin(angle + Math.PI/16) * radius * 0.8)};
                g2d.drawPolyline(petalX, petalY, 3);
                
                int[] petalX2 = {centerX, outerX, centerX + (int)(Math.cos(angle - Math.PI/16) * radius * 0.8)};
                int[] petalY2 = {centerY, outerY, centerY + (int)(Math.sin(angle - Math.PI/16) * radius * 0.8)};
                g2d.drawPolyline(petalX2, petalY2, 3);
            } else {
                // Короткие закругленные лепестки
                g2d.drawLine(centerX, centerY, midX, midY);
            }
        }
        
        // Внутренний ажурный узор
        g2d.setColor(new Color(255, 255, 255, 150));
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 4 * i;
            
            // Трилистники вокруг центра
            int trefoilX = centerX + (int)(Math.cos(angle) * (radius * 0.3));
            int trefoilY = centerY + (int)(Math.sin(angle) * (radius * 0.3));
            drawTrefoil(g2d, trefoilX, trefoilY, 6);
        }
        
        // Самый центр - пентаграмма
        g2d.setColor(new Color(255, 0, 0, 100));
        drawPentagram(g2d, centerX, centerY, radius / 3);
        
        // Золотая точка в самом центре
        g2d.setColor(new Color(255, 215, 0));
        g2d.fillOval(centerX - 3, centerY - 3, 6, 6);
    }
    
    private void drawPentagram(Graphics2D g2d, int centerX, int centerY, int size) {
        // Рисуем пентаграмму
        int[] xPoints = new int[5];
        int[] yPoints = new int[5];
        
        for (int i = 0; i < 5; i++) {
            double angle = Math.PI * 2 / 5 * i - Math.PI / 2;
            xPoints[i] = centerX + (int)(Math.cos(angle) * size);
            yPoints[i] = centerY + (int)(Math.sin(angle) * size);
        }
        
        // Рисуем линии пентаграммы
        for (int i = 0; i < 5; i++) {
            for (int j = i + 2; j < 5; j++) {
                if ((i + j) % 5 != 0) { // Исключаем соседние точки
                    g2d.drawLine(xPoints[i], yPoints[i], xPoints[j], yPoints[j]);
                }
            }
        }
    }
    
    public void startAnimation() {
    if (animationTimer != null && !animationTimer.isRunning()) {
        animationTimer.start();
    }
}

public void stopAnimation() {
    if (animationTimer != null) {
        animationTimer.stop();
    }
}
    
    public BufferedImage getMandalaImage() {
        BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        paintComponent(g2d);
        g2d.dispose();
        return image;
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Genesis Mundi - Готическая Мандала (512x512)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        GothicMandalaPanel mandala = new GothicMandalaPanel();
        frame.add(mandala);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}