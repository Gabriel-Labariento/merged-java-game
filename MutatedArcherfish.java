import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The MutatedArcherfish class extends Enemy. It appears in level
        7 of the game. It attacks through bullets and runs from players
        when chased.

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


public class MutatedArcherfish extends Enemy{
    
    private static BufferedImage[] sprites;
    private static final int MOAN_DURATION = 3000;
    private long lastMoanTime = 0;

    // Calls the static setSprites() method
    static {
        setSprites();
    }   

    /**
     * Creates a MutatedArcherfish instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public MutatedArcherfish(int x, int y) {
        lastSpriteUpdate = 0;
        lastAttackTime = 0;
        id = enemyCount++;
        identifier = NetworkProtocol.MUTATEDARCHERFISH;
        speed = 1;
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
        attackCDDuration = 600;
        
    }

    /**
     * Sets the sprite images to the class and not the instances
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(MutatedArcherfish.class.getResourceAsStream("resources/Sprites/MutatedArcherfish/left0.png"));
            BufferedImage right0 = ImageIO.read(MutatedArcherfish.class.getResourceAsStream("resources/Sprites/MutatedArcherfish/right0.png"));
            sprites = new BufferedImage[] {left0, right0};

        } catch (IOException e) {
            System.out.println("Exception in MutatedArcherfish setSprites()" + e);
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
        final double ACTION_DISTANCE = (GameCanvas.TILESIZE*7) * (GameCanvas.TILESIZE *7);

        
        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        if (getSquaredDistanceBetween(this, pursued) > ACTION_DISTANCE)
            pursuePlayer(pursued);
        else {
            if (now - lastAttackTime > attackCDDuration) {
                createRandomBullet(gsm, pursued);
                lastAttackTime = now;
            }

            //If in aggro range trigger attacks, and run if chased
            if (getSquaredDistanceBetween(this, pursued) < ACTION_DISTANCE){
                runFromPlayer(pursued);
            }
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

        if (now - lastMoanTime > MOAN_DURATION){
            SoundManager.getInstance().playPooledSound("mutatedArcherfish");
            lastMoanTime = now;
        }
    }
}