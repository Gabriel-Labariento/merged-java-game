import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The LoudBell class extends Item. It allows the Player holder
        to decrease attack cooldown and increase damage.

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

public class LoudBell extends Item {
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(LoudBell.class.getResourceAsStream("resources/Sprites/Items/loudbell.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }
    
    /**
     * Creates a LoudBell instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public LoudBell(int x, int y){
        identifier = NetworkProtocol.LOUDBELL;
        worldX = x;
        worldY = y;
        currentRoom = null;

        matchHitBoxBounds();
        initTooltip("Loud Bell", "Reduces defense by 100 but increases damage by 2x and attack cooldown by 25%", false);    
    }

    @Override
    public void applyEffects(){
        initialDefense = owner.getDefense();
        owner.setDefense(initialDefense-100);

        initialCDDuration = owner.getAttackCDDuration();
        owner.setAttackCDDuration((int) Math.round(initialCDDuration*0.75));

        initialAttackFrameDuration = owner.getAttackFrameDuration();
        owner.setAttackFrameDuration((int) Math.round(initialAttackFrameDuration*0.75));

        initialDamage = owner.getDamage();
        owner.setDamage((int) Math.round(initialDamage*2.0));
    }

    @Override
    public void removeEffects(){
        owner.setDefense(initialDefense);
        owner.setAttackCDDuration(initialCDDuration);
        owner.setDamage(initialDamage);
        owner.setAttackFrameDuration(initialAttackFrameDuration);
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}