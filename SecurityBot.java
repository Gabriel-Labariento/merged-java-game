import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The SecurityBot class extends Enemy. It appears in level five of the game.
        If chases Players until it gets to within attack distance. Within attack
        distance, it fires a projectile to targeted Players. When Players get close
        and try to chase SecurityBot, it runs away.

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

public class SecurityBot extends Enemy{
    private static final int SOUND_COOLDOWN = 3000;
    private long lastSoundTime = 0;
    public static int ratCount = 0;
    private static BufferedImage[] sprites;

    // Call the static setSprites() method
    static {
        setSprites();
    }

    /**
     * Creates a SecurityBot instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public SecurityBot(int x, int y) {
        identifier = NetworkProtocol.SECURITYBOT;
        speed = 1;
        height = 24;
        width = 24;
        worldX = x;
        worldY = y;
        maxHealth = 10;
        hitPoints = maxHealth;
        damage = 1;
        rewardXP = 50;
        currentRoom = null;
        currSprite = 0;
        attackCDDuration = 2000;
    }

    /**
     * Set the sprite images to the class and not the instances
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/SecurityBot/left0.png"));
            BufferedImage left1 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/SecurityBot/left1.png"));
            BufferedImage left2 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/SecurityBot/left2.png"));
            BufferedImage right0 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/SecurityBot/right0.png"));
            BufferedImage right1 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/SecurityBot/right1.png"));
            BufferedImage right2 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/SecurityBot/right2.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2};

        } catch (IOException e) {
            System.out.println("Exception in SecurityBot setSprites()" + e);
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
        final double ACTION_DISTANCE = (GameCanvas.TILESIZE * 8) *(GameCanvas.TILESIZE * 8);

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        if (getSquaredDistanceBetween(this, pursued) > ACTION_DISTANCE)
            pursuePlayer(pursued);
        else {
            //If in aggro range trigger attacks, and run if chased
            if (getSquaredDistanceBetween(this, pursued) < ACTION_DISTANCE){
                runFromPlayer(pursued);
            }
            
            if (now - lastAttackTime > attackCDDuration) {
                sendProjectile(gsm, pursued);
                lastAttackTime = now;
            }
        }
            
        
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

        if (now - lastSoundTime > SOUND_COOLDOWN){
            SoundManager.getInstance().playPooledSound("enemyDetected");
            lastSoundTime = now;
        }
    }

    /**
     * Creates a fast travelling projectile (LaserBullet) that moves towards the target Player
     * @param gsm the ServerMaster instance containing the entities ArrayList
     * @param target the attack's target Player
     */
    private void sendProjectile(ServerMaster gsm, Player target){
        int vectorX = target.getCenterX() - getCenterX();
        int vectorY = target.getCenterY() - getCenterY(); 
        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

        //Avoids 0/0 division edge case
        if (normalizedVector == 0) normalizedVector = 1; 
        double normalizedX = vectorX / normalizedVector;
        double normalizedY = vectorY / normalizedVector;

        LaserBullet bullet = new LaserBullet(this, this.worldX-LaserBullet.WIDTH/2, this.worldY-LaserBullet.HEIGHT/2, normalizedX, normalizedY);
        gsm.addEntity(bullet);
    }
}
