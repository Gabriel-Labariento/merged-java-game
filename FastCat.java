import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The FastCat extends the Player class. It is one of three
        playable Player classes. This Player was designed
        for a melee attack with faster movement.

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

public class FastCat extends Player{
    private static BufferedImage[] sprites;

    /**
     * Calls the static setSprites() method
     */
    static {
        setSprites();
    }

    /**
     * Creates a FastCat instance with appropriate fields
     * @param cid the clientId of the Player creating the instance
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public FastCat(int cid, int x, int y){
        this.clientId = cid;
        identifier = NetworkProtocol.FASTCAT;
        baseSpeed = 5;
        speed = baseSpeed;
        height = 16;
        width = 16;
        screenX = 800/2 - width/2;
        screenY = 600/2 - height/2;
        worldX = x;
        worldY = y;
        maxHealth = 4;
        hitPoints = maxHealth;
        damage = 30; // TODO: REVERT DAMAGE BACK TO 5
        isDown = false;
        attackCDDuration = 600;
        attackFrameDuration = 125;

        matchHitBoxBounds();
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
    }

    /**
     * Set the sprite images to the class
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/left0.png"));
            BufferedImage left1 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/left1.png"));
            BufferedImage left2 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/left2.png"));
            BufferedImage right0 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/right0.png"));
            BufferedImage right1 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/right1.png"));
            BufferedImage right2 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/right2.png"));
            BufferedImage attack0 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/attack0.png"));
            BufferedImage attack1 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/attack1.png"));
            BufferedImage attack2 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/attack2.png"));
            BufferedImage death = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/death.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2, attack0, attack1, attack2, death};

        } catch (IOException e) {
            System.out.println("Exception in FastCat setSprites()" + e);
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
}