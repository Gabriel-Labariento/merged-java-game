import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The CatTreat class extends the Item class. It gives 
        additional experience points to the consuming player.

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

public class CatTreat extends Item {
    public static BufferedImage sprite;

    /**
     * Sets the object's image
     */
    static {
        try {
            sprite = ImageIO.read(CatTreat.class.getResourceAsStream("resources/Sprites/Items/cattreat.png"));
        } catch (IOException e) {
            System.out.println("Exception in CatTreat setSprites()" + e);
        }
    }

    /**
     * Creates a CatTreat instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public CatTreat(int x, int y){
        identifier = NetworkProtocol.CATTREAT;
        worldX = x;
        worldY = y;
        currentRoom = null;
        isConsumable = true;

        matchHitBoxBounds();
        initTooltip("Cat Treat", "Gives 15% of the player's current XP cap as XP", true);
    }

    @Override
    public void applyEffects(){
        double addedXP = Math.round((owner.getCurrentXPCap() - owner.getPastXPCap())*0.15);
        owner.applyXP((int) addedXP);
    }

    @Override
    public void removeEffects(){
        
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
    
}