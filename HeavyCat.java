import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The HeavyCat extends the Player class. It is one of three
        playable Player classes. This Player was designed for a 
        melee attack with more health but slower movement.

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

public class HeavyCat extends Player{
    private static BufferedImage[] sprites;

    /**
     * Calls the static setSprites method
     */
    static {
        setSprites();
    }

    /**
     * Creates a new HeavyCat instance with appropriate fields
     * @param cid the client ID of the Player creating this class
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public HeavyCat(int cid, int x, int y){
        this.clientId = cid;
        identifier = NetworkProtocol.HEAVYCAT;
        baseSpeed = 2;
        speed = baseSpeed;
        height = 16;
        width = 16;
        screenX = 800/2 - width/2;
        screenY = 600/2 - height/2;
        worldX = x;
        worldY = y;
        maxHealth = 6;
        hitPoints = maxHealth;
        damage = 8;
        isDown = false;
        attackCDDuration = 1200;
        currSprite = 0;
        attackFrameDuration = 200;

        matchHitBoxBounds();
    }

    /**
     * Set the sprite images to the class and not the instance
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/left0.png"));
            BufferedImage left1 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/left1.png"));
            BufferedImage left2 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/left2.png"));
            BufferedImage right0 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/right0.png"));
            BufferedImage right1 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/right1.png"));
            BufferedImage right2 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/right2.png"));
            BufferedImage attack0 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/attack0.png"));
            BufferedImage attack1 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/attack1.png"));
            BufferedImage attack2 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/attack2.png"));
            BufferedImage death = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/death.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2, attack0, attack1, attack2, death};

        } catch (IOException e) {
            System.out.println("Exception in HeavyCat setSprites()" + e);
        }
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        BufferedImage spriteImage = sprites[currSprite];

        if (isSpriteWhite){
            BufferedImage temp = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tempG2d = temp.createGraphics();
            
            tempG2d.drawImage(spriteImage, 0, 0, null);
            tempG2d.setComposite(AlphaComposite.SrcIn);
            tempG2d.setColor(Color.WHITE);
            tempG2d.fillRect(0, 0, width, height);
            tempG2d.dispose();

            g2d.drawImage(temp, xOffset, yOffset, null);
        } 
        else g2d.drawImage(spriteImage, xOffset, yOffset, width, height, null);
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