import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The GoldFish class extends the Item class. It allows the owner
        to be revived upon death.
        
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

public class Goldfish extends Item {
    public static BufferedImage sprite;
    
    // Sets the sprite
    static {
        try {
            sprite = ImageIO.read(Goldfish.class.getResourceAsStream("resources/Sprites/Items/goldfish.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }
    
    /**
     * Creates a GoldFish instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Goldfish(int x, int y){
        identifier = NetworkProtocol.GOLDFISH;
        worldX = x;
        worldY = y;
        currentRoom = null;

        matchHitBoxBounds();
        initTooltip("Goldfish", "Gives 10% of the player's current XP cap as XP", true);
    }

    @Override
    public void applyEffects(){
    }

    @Override
    public void removeEffects(){
        
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}