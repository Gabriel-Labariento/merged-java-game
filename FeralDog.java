import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The FeralDog extends Enemy. It is one of the bosses in the game.
        It appears at the end of level 2. Its primary behavior is follow
        and then slash at the Player when near. It can create minions and
        apply buff to them.

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

public class FeralDog extends Enemy {
    private int SPAWN_COOLDOWN = 5000;
    private int ATTACK_COOLDOWN = 4000;
    private static final int SNARL_COOLDOWN = 4000;
    private static final int ATTACK_RANGE = GameCanvas.TILESIZE * 3;
    private long lastSpawnTime = 0;
    private long lastSnarlTime = 0;
    private static BufferedImage[] sprites;
    private enum Phase {NORMAL, BUFFED}
    private enum State {IDLE, PURSUE, ATTACK};
    private Phase currentPhase;
    private State currentState;
    private boolean hasPlayedBuffSound = false;

    /**
     * Call the static setSprites() method
     */
    static {
        setSprites();
    }

    /**
     * Creates a FeralDog instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public FeralDog(int x, int y) {
        isBoss = true;
        id = enemyCount++;
        identifier = NetworkProtocol.FERALDOG;
        speed = 1;
        height = 30;
        width = 46;
        worldX = x;
        worldY = y;
        maxHealth = 200;
        hitPoints = maxHealth;
        damage = 2;
        rewardXP = 200;
        currentRoom = null;
        currSprite = 0;
        currentPhase = Phase.NORMAL;
        currentState = State.IDLE;
        isBoss = true;
    }

    /**
     * Set the sprite images to the class and not the instance
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_left0.png"));
            BufferedImage left1 = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_left1.png"));
            BufferedImage leftAttack = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_left_attack.png"));
            BufferedImage right0 = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_right0.png"));
            BufferedImage right1 = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_right1.png"));
            BufferedImage rightAttack = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_right_attack.png"));

            BufferedImage leftBuffed0 = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_left0_buffed.png"));
            BufferedImage leftBuffed1 = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_left1_buffed.png"));
            BufferedImage leftBuffedAttack = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_left_attack_buffed.png"));
            BufferedImage rightBuffed0 = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_right0_buffed.png"));
            BufferedImage rightBuffed1 = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_right1_buffed.png"));
            BufferedImage rightBuffedAttack = ImageIO.read(FeralDog.class.getResourceAsStream("resources/Sprites/FeralDog/feralDog_right_attack_buffed.png"));
            sprites = new BufferedImage[] {left0, left1, leftAttack, right0, right1, rightAttack,
                                        leftBuffed0, leftBuffed1, leftBuffedAttack,
                                        rightBuffed0, rightBuffed1, rightBuffedAttack};

        } catch (IOException e) {
            System.out.println("Exception in FeralDog setSprites()" + e);
        }
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
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
    public void updateEntity(ServerMaster gsm){
        now = System.currentTimeMillis();
        Player pursued = scanForPlayer(gsm);

        switch (currentPhase) {

            case NORMAL:
                if (hitPoints <= maxHealth / 2) currentPhase = Phase.BUFFED; // If health is below half, switch phase
                else {
                    if (pursued == null) return;

                    // Spawning new enemies
                    if (now - lastSpawnTime > SPAWN_COOLDOWN) {
                        spawnMinions();
                        lastSpawnTime = now;
                    }

                    handleStateBehavior(now, pursued, gsm);
                }
                break;

            case BUFFED:
                if (!hasPlayedBuffSound) {
                    SoundManager.getInstance().playPooledSound("buffHowl");
                    hasPlayedBuffSound = true;
                }
                if (pursued == null) return;    
                
                // Shorten cooldowns
                ATTACK_COOLDOWN = 2000;
                SPAWN_COOLDOWN = 3000;
                    
                // Spawning new enemies more frequently
                if (now - lastSpawnTime > SPAWN_COOLDOWN){
                    spawnMinions();
                    lastSpawnTime = now;
                }

                handleStateBehavior(now, pursued, gsm);
                
                // Loop through created enemies and apply buff to them
                for (Entity e : gsm.getEntities()) {
                    if (e instanceof Enemy enemy && enemy != this) {
                        enemy.applyBuff();
                    }
                }

                break;
            default:
                throw new AssertionError();
        }

        updateWalkFrame(now, pursued);
        matchHitBoxBounds();

        if (now - lastSnarlTime > SNARL_COOLDOWN){
            SoundManager.getInstance().playPooledSound("dogSnarl");
            lastSnarlTime = now;
        }
    }

    /**
     * Handles the behavior of the instance based on its currentState
     * @param now the time when the method was invoked
     * @param pursued the target Player
     * @param gsm the ServerMaster instance containing the entities ArrayList 
     */
    private void handleStateBehavior(long now, Player pursued, ServerMaster gsm){
        switch (currentState) {
            case IDLE:
                // Switch to attack state after cool down
                if (now - lastAttackTime > ATTACK_COOLDOWN){
                    currentState = State.ATTACK;
                    lastAttackTime = now;
                } else currentState = State.PURSUE;
                break;
            
            case PURSUE:
                // If within range, telegraph before attack
                if (getSquaredDistanceBetween(this, pursued) <= ATTACK_RANGE * ATTACK_RANGE) {
                    currentState = State.IDLE;
                } else pursuePlayer(pursued);
                break;

            case ATTACK:
                createSlashAttack(gsm, pursued, null);
                currSprite = (pursued.getWorldX() > worldX) ? 5 : 2; // Attack Sprites
                lastAttackTime = now;
                currentState = State.PURSUE;
                break;
        }
    }

    /**
     * Handles the sprite to be shown based on Phase (Normal / Buffed)
     * and position in relation to target Player
     * @param now the time when the method was invoked
     * @param pursued the targeted Player
     */
    private void updateWalkFrame(long now, Player pursued){
        // Sprite walk update
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            if (currentPhase == Phase.NORMAL) {
                if (worldX > pursued.getWorldX()) {                     // LEFT
                currSprite++;
                if (currSprite > 2) currSprite = 0;
                } else if (worldX < pursued.getWorldX()) {              // RIGHT
                    currSprite++;
                    if (currSprite < 3 || currSprite > 5) currSprite = 3;
                } lastSpriteUpdate = now;
            } else { // BUFFED SPRITES
                if (worldX > pursued.getWorldX()) {                     // LEFT
                    if (currSprite < 6 || currSprite > 8) currSprite = 6;
                    else currSprite = (currSprite + 1 > 8) ? 6 : currSprite + 1;
                } else if (worldX < pursued.getWorldX()) {              // RIGHT
                    if (currSprite < 9 || currSprite > 11) currSprite = 9;
                    else currSprite = (currSprite + 1 > 11) ? 9 : currSprite + 1;
                } lastSpriteUpdate = now;
            }            
        }
    }
}
