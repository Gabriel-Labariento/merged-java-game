import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Snakelet extends Enemy{
    private static final int BULLET_COOLDOWN = 5000;
    private static final int ATTACK_DISTANCE = GameCanvas.TILESIZE * 4;
    private long lastBulletSend = 0;
    private static BufferedImage[] sprites;

    static {
        setSprites();
    }

    public Snakelet(int x, int y) {
        id = enemyCount++;
        identifier = NetworkProtocol.SNAKELET;
        speed = 2;
        height = 16;
        width = 16;
        worldX = x;
        worldY = y;
        maxHealth = 15;
        hitPoints = maxHealth;
        damage = 1;
        rewardXP = 75;
        currentRoom = null;
        currSprite = 0;
        
    }

    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(Snakelet.class.getResourceAsStream("resources/Sprites/Snakelet/snakelet_left0.png"));
            BufferedImage left1 = ImageIO.read(Snakelet.class.getResourceAsStream("resources/Sprites/Snakelet/snakelet_left1.png"));
            BufferedImage left2 = ImageIO.read(Snakelet.class.getResourceAsStream("resources/Sprites/Snakelet/snakelet_left2.png"));
            BufferedImage right0 = ImageIO.read(Snakelet.class.getResourceAsStream("resources/Sprites/Snakelet/snakelet_right0.png"));
            BufferedImage right1 = ImageIO.read(Snakelet.class.getResourceAsStream("resources/Sprites/Snakelet/snakelet_right1.png"));
            BufferedImage right2 = ImageIO.read(Snakelet.class.getResourceAsStream("resources/Sprites/Snakelet/snakelet_right2.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2};

        } catch (IOException e) {
            System.out.println("Exception in snakelet setSprites()" + e);
        }
    }

    @Override
    public void matchHitBoxBounds() {
        hitBoxBounds = new int[4];
        hitBoxBounds[0]= worldY + 3;
        hitBoxBounds[1] = worldY + height;
        hitBoxBounds[2]= worldX;
        hitBoxBounds[3] = worldX + width;
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
    }

    @Override
    public void updateEntity(ServerMaster gsm){
        now = System.currentTimeMillis();

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        if (getSquaredDistanceBetween(this, pursued) < ATTACK_DISTANCE * ATTACK_DISTANCE) {
            if (now - lastBulletSend > BULLET_COOLDOWN) {
                sendProjectile(gsm, pursued);
                lastBulletSend = now;
            }
        }
        else pursuePlayer(pursued);

        // Sprite walk update
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            if (worldX > pursued.getWorldX()) {
                currSprite++;
                if (currSprite > 2) currSprite = 0;
            } else {
                currSprite++;
                if (currSprite < 3 || currSprite > 5) currSprite = 3;
            }
            lastSpriteUpdate = now;
        }

        matchHitBoxBounds();
    }

    private void sendProjectile(ServerMaster gsm, Player target){
        int vectorX = target.getCenterX() - getCenterX();
        int vectorY = target.getCenterY() - getCenterY(); 
        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

        //Avoids 0/0 division edge case
        if (normalizedVector == 0) normalizedVector = 1; 
        double normalizedX = vectorX / normalizedVector;
        double normalizedY = vectorY / normalizedVector;

        double spreadAngle = Math.toRadians(15);

        double x1 = normalizedX * Math.cos(spreadAngle) - normalizedY * Math.sin(spreadAngle);
        double y1 = normalizedX * Math.sin(spreadAngle) + normalizedY * Math.cos(spreadAngle);

        double x2 = normalizedX * Math.cos(-spreadAngle) - normalizedY * Math.sin(-spreadAngle);
        double y2 = normalizedX * Math.sin(-spreadAngle) + normalizedY * Math.cos(-spreadAngle);

        // Bullet Spread Rotation Reference: https://stackoverflow.com/questions/31225062/rotating-a-vector-by-angle-and-axis-in-java

        SnakeBullet sb0 = new SnakeBullet(this, getCenterX()-SnakeBullet.WIDTH/2, getCenterY()-SnakeBullet.HEIGHT/2, normalizedX, normalizedY);
        SnakeBullet sb1 = new SnakeBullet(this, getCenterX()-SnakeBullet.WIDTH/2, getCenterY()-SnakeBullet.HEIGHT/2, x1, y1);
        SnakeBullet sb2 = new SnakeBullet(this, getCenterX()-SnakeBullet.WIDTH/2, getCenterY()-SnakeBullet.HEIGHT/2, x2, y2);

        sb0.addAttackEffect(new SlowEffect());
        sb0.addAttackEffect(new PoisonEffect());
        sb1.addAttackEffect(new SlowEffect());
        sb1.addAttackEffect(new PoisonEffect());
        sb2.addAttackEffect(new SlowEffect());
        sb2.addAttackEffect(new PoisonEffect());

        gsm.addEntity(sb0);
        gsm.addEntity(sb1);
        gsm.addEntity(sb2);
    }
}