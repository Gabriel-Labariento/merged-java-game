import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The EnemyBite extends the Attack class.
        It creates a small attack attached to its owner
        that damages Players.

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

public class EnemyBite extends Attack{
    public static final int HEIGHT = 16;
    public static final int WIDTH = 16;
    private static BufferedImage sprite;

    /**
     * Set the sprite to the class and not instances
     */
    static {
        try {
            BufferedImage img = ImageIO.read(EnemyBite.class.getResourceAsStream("resources/Sprites/SharedEnemy/enemyslash.png"));
            sprite = img;
        } catch (IOException e) {
            System.out.println("Exception in EnemyBite setSprites()" + e);
        }
    }

    /**
     * Creates an instance of EnemyBite with appropriate fields
     * @param owner the Entity to which the attack is attached
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public EnemyBite(Entity owner, int x, int y){
        attackNum++;
        id = attackNum;
        identifier = NetworkProtocol.ENEMYBITE;
        this.owner = owner;
        isFriendly = false;
        damage = 1;
        width = WIDTH;
        height = HEIGHT;
        worldX = x;
        worldY = y;

        //For checking attack duration
        duration = 400;
        setExpirationTime(duration);

        matchHitBoxBounds();
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }

    @Override
    public void matchHitBoxBounds(){
        hitBoxBounds = new int[4];
        hitBoxBounds[0]= worldY;
        hitBoxBounds[1] = worldY + height;
        hitBoxBounds[2]= worldX;
        hitBoxBounds[3] = worldX + width;
    }

    @Override
    public void updateEntity(ServerMaster gsm) {
        if (!hasPlayedSound){
            SoundManager.getInstance().playPooledSound("enemyBite.wav");
            hasPlayedSound = true;
        }
        attachToOwner();
    }

    @Override
    public void updateCarousel() {
        throw new UnsupportedOperationException("Unimplemented method 'updateCarousel'");
    }
}
