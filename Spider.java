import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The Spider class extends Enemy. It appears in the first level of the game.
        It follows the player at a certain distance and sends a SpiderBullet
        projectile towards the player.

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

public class Spider extends Enemy{
    public static int spiderCount = 0;
    private static final int BULLET_COOLDOWN = 5000;
    private static final int ATTACK_DISTANCE = GameCanvas.TILESIZE * 4;
    private long lastBulletSend = 0;
    private static BufferedImage[] sprites;

    // Call the static setSprites() method
    static {
        setSprites();
    }

    /**
     * Creates a Spider instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Spider(int x, int y) {
        id = spiderCount++;
        identifier = NetworkProtocol.SPIDER;
        speed = 1;
        height = 16;
        width = 16;
        worldX = x;
        worldY = y;
        maxHealth = 10;
        hitPoints = maxHealth;
        damage = 1;
        rewardXP = 50;
        currentRoom = null;
        currSprite = 0;
        
    }

    /**
     * Set the sprites to the class and not the instance
     */
    private static void setSprites() {
        try {
            BufferedImage up0 = ImageIO.read(Spider.class.getResourceAsStream("resources/Sprites/Spider/spider_up0.png"));
            BufferedImage up1 = ImageIO.read(Spider.class.getResourceAsStream("resources/Sprites/Spider/spider_up1.png"));
            BufferedImage down0 = ImageIO.read(Spider.class.getResourceAsStream("resources/Sprites/Spider/spider_down0.png"));
            BufferedImage down1 = ImageIO.read(Spider.class.getResourceAsStream("resources/Sprites/Spider/spider_down1.png"));
            sprites = new BufferedImage[] {up0, up1, down0, down1};

        } catch (IOException e) {
            System.out.println("Exception in Spider setSprites()" + e);
        }
    }

    @Override
    public void matchHitBoxBounds() {
        hitBoxBounds = new int[4];
        hitBoxBounds[0]= worldY;
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
            if (worldY > pursued.getWorldY()) {
                currSprite = (currSprite == 1) ? 0 : 1;
            } else {
                currSprite = (currSprite == 2) ? 3 : 2;
            }
            lastSpriteUpdate = now;
        }


        matchHitBoxBounds();
    }

    /**
     * Creates a SpiderBullet projectile that travels towards the target Player
     * @param gsm the ServerMaster instance containing the entities ArrayList
     * @param target the Player to whom the projectile is moving towards
     */
    private void sendProjectile(ServerMaster gsm, Player target){
        int vectorX = target.getCenterX() - getCenterX();
        int vectorY = target.getCenterY() - getCenterY(); 
        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

        //Avoids 0/0 division edge case
        if (normalizedVector == 0) normalizedVector = 1; 
        double normalizedX = vectorX / normalizedVector;
        double normalizedY = vectorY / normalizedVector;

        SpiderBullet sb = new SpiderBullet(this, this.worldX-SpiderBullet.WIDTH/2, this.worldY-SpiderBullet.HEIGHT/2, normalizedX, normalizedY);
        gsm.addEntity(sb);
    }
}
