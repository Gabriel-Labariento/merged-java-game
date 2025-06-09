import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The Rat class extends Enemy. It appears in the second level of the game.
        It follows the Player and generates a bite attack when the attack
        is not on cool down and the Player is sufficiently close.

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

public class Rat extends Enemy{
    private static final int BITE_COOLDOWN = 1500;
    private long lastBiteAttack = 0;
    private static BufferedImage[] sprites;

    // Calls the static setSprites() method
    static {
        setSprites();
    }

    /**
     * Creates a Rat instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Rat(int x, int y) {
        id = enemyCount++;
        identifier = NetworkProtocol.RAT;
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
     * Sets the sprite images to the class and not the instances
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/Rat/rat_left0.png"));
            BufferedImage left1 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/Rat/rat_left1.png"));
            BufferedImage left2 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/Rat/rat_left2.png"));
            BufferedImage right0 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/Rat/rat_right0.png"));
            BufferedImage right1 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/Rat/rat_right1.png"));
            BufferedImage right2 = ImageIO.read(Rat.class.getResourceAsStream("resources/Sprites/Rat/rat_right2.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2};

        } catch (IOException e) {
            System.out.println("Exception in Rat setSprites()" + e);
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
        if (getSquaredDistanceBetween(this, pursued) < GameCanvas.TILESIZE * GameCanvas.TILESIZE) {
            if (now - lastBiteAttack > BITE_COOLDOWN ) {
                createBiteAttack(gsm, pursued, null);
                lastBiteAttack = now;
                SoundManager.getInstance().playPooledSound("rat");
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
}
