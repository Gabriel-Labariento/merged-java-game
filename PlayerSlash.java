import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The PlayerSlash class extends Attack. It is a melee attack attached
        to the Entity who created it.

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


public class PlayerSlash extends Attack{
    public static final int WIDTH = 40;
    public static final int HEIGHT = 40;

    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(PlayerSlash.class.getResourceAsStream("resources/Sprites/Attacks/playerslash.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }

    /**
     * Creates a new PlayerSlash instance with fields set to the arguments
     * @param cid the clientId of the creating Entity
     * @param entity the Entity creating the attack
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param d the damage
     * @param isFriendly whether the attack is friendly to players or not
     */
    public PlayerSlash(int cid, Entity entity, int x, int y, int d, boolean isFriendly){
        attackNum++;
        id = attackNum;
        clientId = cid;
        identifier = NetworkProtocol.PLAYERSLASH;
        owner = entity;
        this.isFriendly = isFriendly;
        damage = d;
        
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
        // Bounds array is formatted as such: top, bottom, left, right; SIZES SUBJECT TO CHANGE PER ENTITY
        hitBoxBounds = new int[4];
        hitBoxBounds[0]= worldY;
        hitBoxBounds[1] = worldY + height;
        hitBoxBounds[2]= worldX;
        hitBoxBounds[3] = worldX + width;
    }

    @Override
    public void updateEntity(ServerMaster gsm) {
        attachToOwner();

    }
}
