import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The ThickSweater class extends Item. It speeds up the Player and
        adds 1 to its current health through a regeneration phase.

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

public class ThickSweater extends Item {
    private long regenTime;
    private static final int REGENCDDURATION = 3000;
    private boolean isFirstTimeUse;
    public static BufferedImage sprite;

    // Set the sprite to the class and not the instance
    static {
        try {
            sprite = ImageIO.read(ThickSweater.class.getResourceAsStream("resources/Sprites/Items/thicksweater.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }

    /**
     * Creates a ThickSweater instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public ThickSweater(int x, int y) {
        super();
        worldX = x;
        worldY = y;
        currentRoom = null;
        identifier = NetworkProtocol.THICKSWEATER;
        initialCDDuration = 0;
        initialDamage = 0;
        initialMaxHealth = 0;
        initialHitPoints = 0;
        initialSpeed = -1;
        initialDefense = 2;
        initialAttackFrameDuration = 0;
        isFirstTimeUse = true;
        regenTime = 0;

        initTooltip("Thick Sweater", 
                   "A cozy sweater that provides defense and health regeneration. Reduces movement speed but grants passive healing.", 
                   false);

        matchHitBoxBounds();
    }

    @Override
    public void applyEffects(){
        initialSpeed = owner.getSpeed();
        owner.setSpeed(1);
    }

    @Override
    public void removeEffects(){
        owner.setSpeed(initialSpeed);
    }

    public void triggerRegenSystem(){
        if(regenTime < System.currentTimeMillis()){     
            triggerRegenTimer();

            //Stop healspam when dropping and picking up item
            if (!isFirstTimeUse) owner.setHitPoints(owner.getHitPoints() + 1);
            isFirstTimeUse = false;
        }
    }

    /**
     * Sets the regenTime field value to the current time of invocation
     * plus the value of REGENCDDURATION
     */
    private void triggerRegenTimer(){
        regenTime = System.currentTimeMillis() + REGENCDDURATION;
    }

    /**
     * Sets the value of isFirstTimeUse to the passed boolean
     * @param b the boolean value to set isFirstTimeUse to
     */
    public void setIsFirstTimeUse(boolean b){
        isFirstTimeUse = b;
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}