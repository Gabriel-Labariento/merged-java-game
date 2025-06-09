import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The Frog extends Enemy. It appears in level three. Its main 
        behavior is to follow Players through hopping and occassionally
        attack with smash attacks.

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

public class Frog extends Enemy{
    private static final int ATTACK_RANGE = GameCanvas.TILESIZE * 3;
    private static final int CROAK_COOLDOWN = 2000;
    private static final int IDLE_DURATION = 400;
    private static final int ATTACK_DURATION = 300;
    private boolean smashPerformed;
    private long lastStateChangeTime = 0;
    private long lastCroakTime = 0;
    private static BufferedImage[] sprites;
    private enum State {IDLE, PURSUE, ATTACK, SMASH}; 
    private State currentState;
    
    /**
     * Call the static setSprites method
     */
    static {
        setSprites();
    }

    /**
     * Creates a Frog instance with appropritate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Frog(int x, int y) {
        id = enemyCount++;
        identifier = NetworkProtocol.FROG;
        speed = 5;
        height = 16;
        width = 16;
        worldX = x;
        worldY = y;
        maxHealth = 20;
        hitPoints = maxHealth;
        damage = 1;
        rewardXP = 50;
        currentRoom = null;
        currSprite = 0;
        currentState = State.IDLE;
        smashPerformed = false;
    }

    /**
     * Set the sprite images to the class and not the instances
     */
    private static void setSprites() {
        try {
            BufferedImage down0 = ImageIO.read(Frog.class.getResourceAsStream("resources/Sprites/Frog/frog_down0.png"));
            BufferedImage down1 = ImageIO.read(Frog.class.getResourceAsStream("resources/Sprites/Frog/frog_down1.png"));

            sprites = new BufferedImage[] {down0, down1};

        } catch (IOException e) {
            System.out.println("Exception in Frog setSprites()" + e);
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

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
    }

    
    @Override
    public void updateEntity(ServerMaster gsm){
        now = System.currentTimeMillis();

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;

        double distanceSquared = getSquaredDistanceBetween(this, pursued);

        switch (currentState) {
            case IDLE:
                // Stay in idle for a bit after an action
                if (now - lastStateChangeTime > IDLE_DURATION) {
                    currSprite = 0;
                    currentState = (distanceSquared <= ATTACK_RANGE * ATTACK_RANGE) ? State.ATTACK : State.PURSUE;
                    lastStateChangeTime = now;
                }
                break;
            
            case PURSUE:
                currSprite = 1; // Jump Sprite
                initiateJump(pursued); 
                currentState = State.IDLE;
                lastStateChangeTime = now;
                break;

            case ATTACK:
                // Prioritize smash 
                if (!smashPerformed) {
                    currentState = State.SMASH;
                    smashPerformed = true;
                    lastStateChangeTime = now;
                    performSmashAttack(gsm);
                }
                
                // Perform jump if possible
                if (now - lastStateChangeTime > ATTACK_DURATION) { 
                    initiateJump(pursued);
                    currentState = State.IDLE;
                    lastStateChangeTime = now;
                    smashPerformed = false;
                } 
                break;

            case SMASH:
                if (now - lastStateChangeTime > ATTACK_DURATION) {
                    currentState = State.IDLE;
                    lastStateChangeTime = now;
                }
                break;

            default:
                throw new AssertionError();
        }
        matchHitBoxBounds();

        if (now - lastCroakTime > CROAK_COOLDOWN) {
            SoundManager.getInstance().playPooledSound("frogCroak");
            lastCroakTime = now;
        }
    }    
}
