import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The EnemySmash extends Attack. It is an ultra wide 
        attack attached to its owner that damages Players.

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

public class EnemySmash extends Attack {
    public static final int SMASH_RADIUS = GameCanvas.TILESIZE * 2;
    private static BufferedImage sprite;

    /**
     * Set sprites to the class, not the instances
     */
    static {
        try {
            BufferedImage img = ImageIO.read(EnemySmash.class.getResourceAsStream("resources/Sprites/SharedEnemy/enemysmash.png"));
            sprite = img;
        } catch (IOException e) {
            System.out.println("Exception in EnemySmash setSprite()" + e);
        }
    }

    /**
     * Creates an EnemySmash instance with appropriate fields
     * @param owner the Entity creating the attack, to which it is attached to
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public EnemySmash(Entity owner, int x, int y){
        attackNum++;
        id = attackNum;
        identifier = NetworkProtocol.ENEMYSMASH;
        this.owner = owner;
        isFriendly = false;
        damage = 1;
        width = 2 * SMASH_RADIUS;
        height = 2 * SMASH_RADIUS;
        worldX = x;
        worldY = y;

        //For checking attack duration
        duration = 800;
        setExpirationTime(duration);

        matchHitBoxBounds();
    }

    
    @Override
    public void updateEntity(ServerMaster gsm) {
        if (!hasPlayedSound){
            SoundManager.getInstance().playPooledSound("playerSmash.wav");
            hasPlayedSound = true;
        }
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
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
    public void updateCarousel() {
        throw new UnsupportedOperationException("Unimplemented method 'updateCarousel'");
    }
    
}