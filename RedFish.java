import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The RedFish class extends Item. It adds one-fourth of the Player's
        max health to its currentHealth.

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


public class RedFish extends Item {
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(RedFish.class.getResourceAsStream("resources/Sprites/Items/redfish.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }
    
    /**
     * Creates a RedFish instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public RedFish(int x, int y){
        identifier = NetworkProtocol.REDFISH;
        worldX = x;
        worldY = y;
        currentRoom = null;
        isConsumable = true;

        matchHitBoxBounds();
        initTooltip("Red Fish", "Restores 25% of the player's max health", true);
    }

    @Override
    public void applyEffects(){
        double restoredHP = Math.round(owner.getMaxHealth()*0.25);
        owner.setHitPoints(owner.getHitPoints() + (int) restoredHP);
    }

    @Override
    public void removeEffects(){}

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }

}
