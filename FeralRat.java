import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The FeralRat extends Enemy. It is a simple Enemy that
        creates a bite attack toward the player when close, and
        chases the player if they are far.

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

public class FeralRat extends Enemy{
    
    private static BufferedImage[] sprites;
    private static final int NOISE_DURATION = 3500;
    private long lastNoiseTime = 0;

    /**
     * Calls the static setSprites() method
     */
    static {
        setSprites();
    }

    /**
     * Creates a FeralRat instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public FeralRat(int x, int y) {
        lastSpriteUpdate = 0;
        lastAttackTime = 0;
        id = enemyCount++;
        identifier = NetworkProtocol.FERALRAT;
        speed = 3;
        height = 32;
        width = 32;
        worldX = x;
        worldY = y;
        maxHealth = 10;
        hitPoints = maxHealth;
        damage = 1;
        rewardXP = 50;
        currentRoom = null;
        currSprite = 0;
        attackCDDuration = 500;
        
    }

    /**
     * Set the sprite images to the class and not the instances
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(FeralRat.class.getResourceAsStream("resources/Sprites/FeralRat/left0.png"));
            BufferedImage right0 = ImageIO.read(FeralRat.class.getResourceAsStream("resources/Sprites/FeralRat/right0.png"));
            sprites = new BufferedImage[] {left0, right0};

        } catch (IOException e) {
            System.out.println("Exception in FeralRat setSprites()" + e);
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
        final double ACTION_DISTANCE = (GameCanvas.TILESIZE) * (GameCanvas.TILESIZE);

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        double distanceSquared = getSquaredDistanceBetween(this, pursued);

        // If within distance
        if (distanceSquared <= ACTION_DISTANCE) {
            if (now - lastAttackTime > attackCDDuration) {
                // Check if cool down has finished 
                createBiteAttack(gsm, pursued, null);
                lastAttackTime = now;
            }
        } else {
            pursuePlayer(pursued);
        }
        
        // Sprite walk update
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            if (worldX > pursued.getWorldX()) {
                currSprite = 0;
            } else {
                currSprite = 1;
            }
            lastSpriteUpdate = now;
        }

        matchHitBoxBounds();

        if (now - lastNoiseTime > NOISE_DURATION){
            SoundManager.getInstance().playPooledSound("feralRatNoise");
            lastNoiseTime = now;
        }
    }
}