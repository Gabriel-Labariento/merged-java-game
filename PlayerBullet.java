import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The PlayerBullet class extends Attack and implements the movement
        and rendering of fired projectiles. It uses normalized vectors 
        to travel across a limited duration.

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

public class PlayerBullet extends Attack{
    private final double normalizedX;
    private final double normalizedY;
    public static final int WIDTH = 16;
    public static final int HEIGHT = 16;
    public static BufferedImage sprite;
    

    // Sets the sprite image to the class and not the instance
    static {
        try {
            sprite = ImageIO.read(PlayerBullet.class.getResourceAsStream("resources/Sprites/Attacks/playerbullet.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }

    /**
     * Creates a new PlayerBullet with the specified properties
     * @param cid the clientId of the sending Entty
     * @param entity the entity who owns the Attack or its source
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param nX the x component of the normalized vector that handles bullet direction
     * @param nY the y component of the normalized vector that handles bullet direction
     * @param d the damage of the bullet
     * @param isFriendly whether the bullet is friendly to players or not
     */
    public PlayerBullet(int cid, Entity entity, int x, int y, double nX, double nY, int d, boolean isFriendly){
        attackNum++;
        id = attackNum;
        clientId = cid;
        identifier = NetworkProtocol.PLAYERBULLET;
        owner = entity;
        this.isFriendly = isFriendly;
        damage = d;
        width = WIDTH;
        height = HEIGHT;
        worldX = x;
        worldY = y;
        speed = 4;
        normalizedX = nX;
        normalizedY = nY;

        //For checking attack duration
        duration = 10000;
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

    /**
     * Moves the bullet based on its speed and the value of the vector components
     * normalizedX and normalizedY
     */
    private void moveBullet(double scaleFactor){
        worldX += (speed*scaleFactor)*normalizedX;
        worldY += (speed*scaleFactor)*normalizedY;
        matchHitBoxBounds();
    }

    @Override
    public void updateEntity(ServerMaster gsm) {
        if (!hasPlayedSound) {
            SoundManager.getInstance().playPooledSound("playerBullet.wav");
            hasPlayedSound = true;
        }
        moveBullet(1);
    }

    @Override
    public void updateCarousel() {
        if (!hasPlayedSound) {
            SoundManager.getInstance().playPooledSound("playerBullet.wav");
            hasPlayedSound = true;
        }
        moveBullet(0.4);
    }

}