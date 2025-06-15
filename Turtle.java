import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The Turtle class extends Enemy. It is a boss that appears in level three.
        It has two distinct phases: Killer and Shell. In the Killer phase, it 
        actively pursues Players and creates a smash attack with devastating damage.
        In the Shell phase, it becomes immobile but invincible. It swithces between
        these states periodically. It also has the ability to spawn minions.

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

public class Turtle extends Enemy {
    private static final int SLOWER_SPRITE_FRAME_DURATION = 300; // INTENTIONALLY SLOWER SPRITE FRAME DURATION
    private static final int SPAWN_COOLDOWN = 4000;
    private static final int IDLE_DURATION = 2000;
    private static final int ATTACK_COOLDOWN = 6000;
    private static final int SHELL_DURATION = 6000;
    private static final int OUT_OF_SHELL_DURATION = 5000;
    private final int KILLER_DAMAGE = 5;
    private static final int ATTACK_RANGE = GameCanvas.TILESIZE * 3;

    private long lastIdleTime = 0;
    private long lastSpawnTime = 0;
    private long lastShellEnterTime = 0;
    private long lastShellExitTime = 0;

    private static BufferedImage[] sprites;
    private enum Phase {KILLER, SHELL};
    private enum State {IDLE, PURSUE, ATTACK, HIDDEN};

    private State currentState;
    private Phase currentPhase;

    private int realHealth;

    // Call the static setSprites() method
    static {
        setSprites();
    }

    /**
     * Creates a Turtle instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Turtle(int x, int y) {
        isBoss = true;
        id = enemyCount++;
        identifier = NetworkProtocol.TURTLE;
        speed = 1;
        height = 90;
        width = 90;
        worldX = x;
        worldY = y;
        maxHealth = 400;
        hitPoints = maxHealth;
        realHealth = maxHealth;
        damage = KILLER_DAMAGE;
        rewardXP = 200;
        currentRoom = null;
        isBoss = true;
        currSprite = 0;
        currentPhase = Phase.KILLER;
        currentState = State.IDLE;
        isBoss = true;
    }

    /**
     * Set the sprite images to the class and not the instance
     */
    private static void setSprites() {
        try {
            BufferedImage down0 = ImageIO.read(Turtle.class.getResourceAsStream("resources/Sprites/Turtle/turtle_down0.png"));
            BufferedImage down1 = ImageIO.read(Turtle.class.getResourceAsStream("resources/Sprites/Turtle/turtle_down1.png"));
            BufferedImage downShelledOut = ImageIO.read(Turtle.class.getResourceAsStream("resources/Sprites/Turtle/turtle_down2.png"));
            BufferedImage downShelledIn = ImageIO.read(Turtle.class.getResourceAsStream("resources/Sprites/Turtle/turtle_down3.png"));

            BufferedImage up0 = ImageIO.read(Turtle.class.getResourceAsStream("resources/Sprites/Turtle/turtle_up0.png"));
            BufferedImage up1 = ImageIO.read(Turtle.class.getResourceAsStream("resources/Sprites/Turtle/turtle_up1.png"));
            BufferedImage upShelledOut = ImageIO.read(Turtle.class.getResourceAsStream("resources/Sprites/Turtle/turtle_up2.png"));
            BufferedImage upShelledIn = ImageIO.read(Turtle.class.getResourceAsStream("resources/Sprites/Turtle/turtle_up3.png"));
            sprites = new BufferedImage[] {down0, down1, downShelledOut, downShelledIn, up0, up1, upShelledOut, upShelledIn};

        } catch (IOException e) {
            System.out.println("Exception in Turtle setSprites()" + e);
        }
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
    }

    @Override
    public void matchHitBoxBounds() {
        hitBoxBounds = new int[4];
        if (currentPhase == Phase.SHELL){
            hitBoxBounds[0]= worldY + 7;
            hitBoxBounds[1] = worldY + height - 7;
            hitBoxBounds[2]= worldX + 7;
            hitBoxBounds[3] = worldX + width - 7;
        } else {
            hitBoxBounds[0]= worldY;
            hitBoxBounds[1] = worldY + height;
            hitBoxBounds[2]= worldX;
            hitBoxBounds[3] = worldX + width;
        }
        
    }
    
    @Override
    public void updateEntity(ServerMaster gsm){
        now = System.currentTimeMillis();
        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        
        switch (currentPhase) {
            case KILLER:
                handleKillerPhase(now, pursued, gsm);
                break;
            case SHELL:
                handleShellPhase(now, pursued);
                break;
            default:
                throw new AssertionError();
        }

        // Spawning new enemies
        if (now - lastSpawnTime > SPAWN_COOLDOWN) {
            spawnMinions();
            lastSpawnTime = now;
        }
    }

    /**
     * Manages the behavior during the Killer Phase. It switches states between
     * IDLE, PURSUE, and ATTACK. It updates the sprite based on current state,
     * movement direction, and position relative to the pursued Player.
     * @param now the current time on invocation
     * @param pursued the Player being pursued or attacked
     * @param gsm the ServerMaster instance containing the entities ArrayList
     */
    private void handleKillerPhase(long now, Player pursued, ServerMaster gsm){
        if (now - lastShellExitTime > OUT_OF_SHELL_DURATION) {
            currentPhase = Phase.SHELL;
            currentState = State.HIDDEN;
            realHealth = hitPoints;
            hitPoints = Integer.MAX_VALUE;
            lastShellEnterTime = now;
            return;
        } 
        switch (currentState) {
            case IDLE:
                if (now - lastIdleTime > IDLE_DURATION) currentState = State.PURSUE;
                else pursuePlayer(pursued); 
                break;
            case PURSUE:
                pursuePlayer(pursued);
                if (getSquaredDistanceBetween(this, pursued) <= ATTACK_RANGE * ATTACK_RANGE){
                    currentState = State.ATTACK;
                }
                break;
            case ATTACK:
                if (getSquaredDistanceBetween(this, pursued) <= ATTACK_RANGE * ATTACK_RANGE){
                    if (now - lastAttackTime > ATTACK_COOLDOWN){
                    performAttack(pursued, gsm);
                    lastAttackTime = now;
                    currentState = State.IDLE;
                    lastIdleTime = now;
                    }
                }
                break;
            default:
                throw new AssertionError();
        }

        // Sprite walk update
        if (now - lastSpriteUpdate > SLOWER_SPRITE_FRAME_DURATION) {
            if (worldY < pursued.getWorldY()) {
                currSprite = (currSprite == 0 || currSprite == 1) ? (currSprite == 0 ? 1 : 0) : 0; // Ensure shuffling between sprites 0 and 1
            } else {
                currSprite = (currSprite == 4 || currSprite == 5) ? (currSprite == 4 ? 5 : 4) : 4; // Ensure shuffling between sprites 4 and 5
            } lastSpriteUpdate = now;
        }
        matchHitBoxBounds();
    }

    /**
     * Manges the Turtle's behavior during the Shell phase. In this phase,
     * the Turtle hides in its shell. It does not move, but it becomes invulnerable.
     * The sprites are updated to show the Shelled State
     * @param now the current time on invocation
     * @param pursued the Player being hid from (affects the sprite image)
     */
    private void handleShellPhase(long now, Player pursued){
        if (now - lastShellEnterTime > SHELL_DURATION) {
            currentPhase = Phase.KILLER;
            currentState = State.IDLE;
            hitPoints = realHealth;
            lastShellExitTime = now;
            return;
        }
        // Set Sprite
        if (worldY > pursued.getWorldY()) currSprite = 7;
        else currSprite = 3;
    }

    /**
     * Creates an attack againsst the target Player by creating an
     * EnemySmash Attack at a position towards the Player
     * @param target the Player being targeted/attacked
     * @param gsm the ServerMaster instance containing the entities ArrayList
     */
    private void performAttack(Player target, ServerMaster gsm){
        int vectorX = target.getCenterX() - getCenterX();
        int vectorY = target.getCenterY() - getCenterY(); 
        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

        //Avoids 0/0 division edge case
        if (normalizedVector == 0) normalizedVector = 1; 
        double normalizedX = vectorX / normalizedVector;
        double normalizedY = vectorY / normalizedVector;

        int attackX = (int) (this.getCenterX() + normalizedX * ATTACK_RANGE);
        int attackY = (int) (this.getCenterY() + normalizedY * ATTACK_RANGE);

        attackX -= EnemySmash.SMASH_RADIUS;
        attackY -= EnemySmash.SMASH_RADIUS;

        EnemySmash enemySmash = new EnemySmash(this, attackX, attackY);
        enemySmash.setDamage(KILLER_DAMAGE);
        gsm.addEntity(enemySmash);
        SoundManager.getInstance().playPooledSound("waterSplash");
    }
}
