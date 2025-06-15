import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The PringlesCan class extends Item. It buffs up the Player's defense
        but increases its attack cooldown time and decreases the owner's
        damage.

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


public class PringlesCan extends Item {
    public static BufferedImage sprite;

    // Set sprites to the class
    static {
        try {
            sprite = ImageIO.read(PringlesCan.class.getResourceAsStream("resources/Sprites/Items/pringlescan.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }
    
    /**
     * Creates a PringlesCan instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public PringlesCan(int x, int y){
        identifier = NetworkProtocol.PRINGLESCAN;
        worldX = x;
        worldY = y;
        currentRoom = null;

        matchHitBoxBounds();
        initTooltip("Pringles Can", "Increases defense by 50 but increases attack cooldown time by 25% and decreases damage by 25%", false);
    }

    @Override
    public void applyEffects(){
        initialDefense = owner.getDefense();
        owner.setDefense(initialDefense+50);

        initialCDDuration = owner.getAttackCDDuration();
        owner.setAttackCDDuration((int) Math.round(initialCDDuration*1.25));

        initialDamage = owner.getDamage();
        owner.setDamage((int) Math.round(initialDamage*0.75));
    }

    @Override
    public void removeEffects(){
        owner.setDefense(initialDefense);
        owner.setAttackCDDuration(initialCDDuration);
        owner.setDamage(initialDamage);
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}