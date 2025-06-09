import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The LightScard class extends Item. It reduces the Player's attack
        cooldown but also decreases their health.

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

public class LightScarf extends Item {
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(LightScarf.class.getResourceAsStream("resources/Sprites/Items/lightscarf.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }

    /**
     * Creates a LightScarf instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public LightScarf(int x, int y){
        identifier = NetworkProtocol.LIGHTSCARF;
        worldX = x;
        worldY = y;
        currentRoom = null;

        matchHitBoxBounds();
        initTooltip("Light Scarf", "Reduces attack cooldown by 50% but decreases health by 75%", false);
    }

    @Override
    public void applyEffects(){
        initialCDDuration = owner.getAttackCDDuration();
        owner.setAttackCDDuration((int) Math.round(initialCDDuration*0.5));

        initialAttackFrameDuration = owner.getAttackFrameDuration();
        owner.setAttackFrameDuration((int) Math.round(initialAttackFrameDuration*0.5));

        initialMaxHealth = owner.getMaxHealth();
        owner.setMaxHealth((int) Math.round(initialMaxHealth*0.25));

    }

    @Override
    public void removeEffects(){
        owner.setAttackCDDuration(initialCDDuration);
        owner.setMaxHealth(initialMaxHealth);
        owner.setAttackFrameDuration(initialAttackFrameDuration);
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}