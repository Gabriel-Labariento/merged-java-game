import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The GunCat extends the Player class. It is one of three
        playable Player classes. This Player supports was designed
        for ranged attacks.

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


public class GunCat extends Player{
    private static BufferedImage[] sprites;

    /**
     * Calls the static setSprites() method
     */
    static {
        setSprites();
    }
    
    /**
     * Creates a new GunCat instance with appropriate fields
     * @param cid the client Id of the Player creating this class
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public GunCat(int cid, int x, int y){
        this.clientId = cid;
        identifier = NetworkProtocol.GUNCAT;
        baseSpeed = 2;
        speed = baseSpeed;
        height = 16;
        width = 16;
        screenX = 800/2 - width/2;
        screenY = 600/2 - height/2;
        worldX = x;
        worldY = y;
        maxHealth = 2;
        hitPoints = maxHealth;
        damage = 500;
        isDown = false;
        attackCDDuration = 800;
        attackFrameDuration = 75;
        matchHitBoxBounds();
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        BufferedImage spriteImage = sprites[currSprite];
        long now = System.currentTimeMillis();
        
        boolean isSpriteWhite = false;

        if (getIsInvincible() && (now - lastSpriteUpdate > (SPRITE_FRAME_DURATION*5))){
            isSpriteWhite = !(isSpriteWhite);
            lastSpriteUpdate = now;
        } 

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


    /**
     * Set the sprite images to the class and not the instance
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/left0.png"));
            BufferedImage left1 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/left1.png"));
            BufferedImage left2 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/left2.png"));
            BufferedImage right0 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/right0.png"));
            BufferedImage right1 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/right1.png"));
            BufferedImage right2 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/right2.png"));
            BufferedImage attack0 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/attack0.png"));
            BufferedImage attack1 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/attack1.png"));
            BufferedImage attack2 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/attack2.png"));
            BufferedImage death = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/death.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2, attack0, attack1, attack2, death};

        } catch (IOException e) {
            System.out.println("Exception in GunCat setSprites()" + e);
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