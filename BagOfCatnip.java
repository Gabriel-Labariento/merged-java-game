import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The BagOfCatnip class extends the Item class.
        It multiplies the owner's damage by 2 but increases
        attack cooldown time by 1.5.

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

public class BagOfCatnip extends Item {
    public static BufferedImage sprite;

    /**
     * Sets the object's sprite
     */
    static {
        try {
            sprite = ImageIO.read(BagOfCatnip.class.getResourceAsStream("resources/Sprites/Items/bagofcatnip.png"));
        } catch (IOException e) {
            System.out.println("Exception in BagOfCatnip setSprites()" + e);
        }
    }

    /**
     * Creates an instance of BagOfCatnip with appropriate hitbox bounds
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public BagOfCatnip(int x, int y){
        identifier = NetworkProtocol.BAGOFCATNIP;
        worldX = x;
        worldY = y;
        currentRoom = null;

        matchHitBoxBounds();
        initTooltip("Bag of Catnip", "Multiplies damage by 2 but increases attack cooldown time by 1.5", true);
    }

    @Override
    public void applyEffects(){
        initialCDDuration = owner.getAttackCDDuration();
        owner.setAttackCDDuration((int) Math.round(initialCDDuration*1.25));

        initialDamage = owner.getDamage();
        owner.setDamage((int) Math.round(initialDamage*2.0));
    }

    @Override
    public void removeEffects(){
        owner.setAttackCDDuration(initialCDDuration);
        owner.setDamage(initialDamage);
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}
