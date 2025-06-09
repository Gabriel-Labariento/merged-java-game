import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The MutatedPufferfish class extends Enemy. It appears in level
        7 of the game. It follows players, smashes when available,
        and runs from players between smashes to refresh cooldown.

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


public class MutatedPufferfish extends Enemy{
    
    private static BufferedImage[] sprites;
    private static final int MOAN_DURATION = 3000;
    private long lastMoanTime = 0;

    // Calls the static setSprites() method
    static {
        setSprites();
    }

    /**
     * Creates a MutatedPufferfish instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public MutatedPufferfish(int x, int y) {
        lastSpriteUpdate = 0;
        lastAttackTime = 0;
        id = enemyCount++;
        identifier = NetworkProtocol.MUTATEDPUFFERFISH;
        speed = 1;
        height = 56;
        width = 56;
        worldX = x;
        worldY = y;
        maxHealth = 10;
        hitPoints = maxHealth;
        damage = 2;
        rewardXP = 100;
        currentRoom = null;
        currSprite = 0;
        attackCDDuration = 3500;
        
    }

    /**
     * Set the sprite images to the class and not the instances
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(MutatedPufferfish.class.getResourceAsStream("resources/Sprites/MutatedPufferfish/left0.png"));
            BufferedImage right0 = ImageIO.read(MutatedPufferfish.class.getResourceAsStream("resources/Sprites/MutatedPufferfish/right0.png"));
            sprites = new BufferedImage[] {left0, right0};

        } catch (IOException e) {
            System.out.println("Exception in MutatedPufferfish setSprites()" + e);
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
        final double ACTION_DISTANCE = (GameCanvas.TILESIZE * 5) * (GameCanvas.TILESIZE * 5);

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        double distanceSquared = getSquaredDistanceBetween(this, pursued);
        if (distanceSquared <= ACTION_DISTANCE) {
            if (now - lastAttackTime > attackCDDuration) {
                createRandomSmash(gsm, pursued);
                lastAttackTime = now;
            }
            runFromPlayer(pursued);
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

        if (now - lastMoanTime > MOAN_DURATION){
            SoundManager.getInstance().playPooledSound("mutatedPufferfish");
            lastMoanTime = now;
        }
    }
}