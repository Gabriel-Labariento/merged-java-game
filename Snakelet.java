import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The Snakelet class extends Enemy. It appears in level four of the game.
        It follows the player at a certain distance and sends three projectiles
        at varied angles towards it.

        @author Niles Tristan Cabrera (240828)
        @author Gabriel Matthew Labariento (242425)
        @version 20 May 2025

        We have not discussed the Java language code in our program
        with anyone other than my instructor or the teaching assistants
        assigned to this course.
        We have not used Java language code obtained from another student,
        or any other unauthorized source, either modified or unmodified.
        If any Java language code or documentation used in our program
        was obtained from another source, such as a textbook or website,
        that has been clearly noted with a proper citation in the comments
        of our program.
**/

public class Snakelet extends Enemy{
    private static final int BULLET_COOLDOWN = 5000;
    private static final int ATTACK_DISTANCE = GameCanvas.TILESIZE * 4;
    private long lastBulletSend = 0;
    private static BufferedImage[] sprites;

    // Calls the static setSprites() method
    static {
        setSprites();
    }

    /**
     * Creates a Snakelet instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
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

    /**
     * Sets the sprite images to the class and not the instances
     */
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

    /**
     * Sends three projectiles at varied angles towards the target Player
     * @param gsm the ServerMaster instance containing the entities ArrayList
     * @param target the Player to send the projectiles towards
     */
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