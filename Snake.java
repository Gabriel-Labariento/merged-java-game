import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The Snake class extends enemy. It is a boss that appears in level four.
        It has the ability to spawn minions. In its first phase, it sends multiple
        bullets towards the player in a fan shape. In its second phase after half
        health, it follows the player and dashes towards the player simulating
        an attack.

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

public class Snake extends Enemy {
    private static final int BULLET_COOLDOWN = 5000;
    private static final int DASH_COOLDOWN = 1000;
    private final int SPAWN_COOLDOWN = 5000;
    private static final int HISS_COOLDOWN = 3000;

    private static final int DASH_DISTANCE = GameCanvas.TILESIZE * 3;
    private static final int ATTACK_DISTANCE = GameCanvas.TILESIZE * 3;

    private static final int BURST_INTERVAL = 200;
    private static final int TOTAL_BURSTS = 5;
    private long lastBulletSend = 0;
    private int burstCount = 0;
    private boolean inBurst;
    private long lastBurstSend = 0;
    private long lastHissTime = 0;

    private long lastDashTime = 0;
    private long lastSpawnTime = 0;

    private static BufferedImage[] sprites;
    private enum Phase {BULLETS, CONSTRICT};
    private Phase currentPhase;

    private enum State {IDLE, PURSUE, DASH};
    private State currentState;
    
    // Calls the static setSprites method
    static {
        setSprites();
    }

    /**
     * Creates a Snake instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Snake(int x, int y) {
        isBoss = true;
        enemyCount++;
        identifier = NetworkProtocol.SNAKE;
        speed = 1;
        height = 48;
        width = 48;
        worldX = x;
        worldY = y;
        maxHealth = 500;
        hitPoints = maxHealth;
        damage = 2;
        rewardXP = 200;
        currentRoom = null;
        currSprite = 0;
        currentPhase = Phase.BULLETS;
        currentState = State.IDLE;
        isBoss= true;
    }

    /**
     * Set the sprite images to the class and not the instance
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(Snake.class.getResourceAsStream("resources/Sprites/Snake/snake_left0.png"));
            BufferedImage left1 = ImageIO.read(Snake.class.getResourceAsStream("resources/Sprites/Snake/snake_left1.png"));
            BufferedImage left2 = ImageIO.read(Snake.class.getResourceAsStream("resources/Sprites/Snake/snake_left2.png"));
            BufferedImage right0 = ImageIO.read(Snake.class.getResourceAsStream("resources/Sprites/Snake/snake_right0.png"));
            BufferedImage right1 = ImageIO.read(Snake.class.getResourceAsStream("resources/Sprites/Snake/snake_right1.png"));
            BufferedImage right2 = ImageIO.read(Snake.class.getResourceAsStream("resources/Sprites/Snake/snake_right2.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2};

        } catch (IOException e) {
            System.out.println("Exception in Snake setSprites()" + e);
        }
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
    }

    @Override
    public void matchHitBoxBounds() {
        hitBoxBounds = new int[4];
        hitBoxBounds[0]= worldY + 5;
        hitBoxBounds[1] = worldY + height - 3;
        hitBoxBounds[2]= worldX + 1;
        hitBoxBounds[3] = worldX + width ;
    }
    
    @Override
    public void updateEntity(ServerMaster gsm){
        now = System.currentTimeMillis();
        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        switch (currentPhase) {
            case BULLETS:
                // Burst Attack
                if (getSquaredDistanceBetween(this, pursued) < ATTACK_DISTANCE * ATTACK_DISTANCE) {
                    handleBurstAttack(gsm, pursued, now);
                    runFromPlayer(pursued);
                } else pursuePlayer(pursued);
                if (hitPoints <= maxHealth / 2) currentPhase = Phase.CONSTRICT;
                break;
            case CONSTRICT:
                handleConstrictBehavior(pursued, now);
                break;
            default:
                throw new AssertionError();
        }
        
        // Spawning new enemies
        if (now - lastSpawnTime > SPAWN_COOLDOWN) {
            spawnMinions();
            lastSpawnTime = now;
        }

        // Sprite walk update
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            if (worldX > pursued.getWorldX()) {
                currSprite++;
                if (currSprite > 2) currSprite = 0;
            } else {
                currSprite++;
                if (currSprite < 3 || currSprite > 5) currSprite = 3;
            }
            lastSpriteUpdate = now;
        }

        matchHitBoxBounds();

        if (now - lastHissTime > HISS_COOLDOWN){
            SoundManager.getInstance().playPooledSound("snakeHiss");
            lastHissTime = now;
        }
    }

    /**
     * Sends three SnakBullet instances towards the Player at slightly varied angles
     * @param gsm the ServerMaster instance containing the entities ArrayList
     * @param target the Player to send the projectiles to
     */
    private void sendProjectile(ServerMaster gsm, Player target){
        int vectorX = target.getCenterX() - getCenterX();
        int vectorY = target.getCenterY() - getCenterY(); 
        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

        //Avoids 0/0 division edge case
        if (normalizedVector == 0) normalizedVector = 1; 
        double normalizedX = vectorX / normalizedVector;
        double normalizedY = vectorY / normalizedVector;

        double spreadAngle = Math.toRadians(15);

        double x1 = normalizedX * Math.cos(spreadAngle) - normalizedY * Math.sin(spreadAngle);
        double y1 = normalizedX * Math.sin(spreadAngle) + normalizedY * Math.cos(spreadAngle);

        double x2 = normalizedX * Math.cos(-spreadAngle) - normalizedY * Math.sin(-spreadAngle);
        double y2 = normalizedX * Math.sin(-spreadAngle) + normalizedY * Math.cos(-spreadAngle);

        // Bullet Spread Rotation Reference: https://stackoverflow.com/questions/31225062/rotating-a-vector-by-angle-and-axis-in-java

        SnakeBullet sb0 = new SnakeBullet(this, getCenterX()-SnakeBullet.WIDTH/2, getCenterY()-SnakeBullet.HEIGHT/2, normalizedX, normalizedY);
        SnakeBullet sb1 = new SnakeBullet(this, getCenterX()-SnakeBullet.WIDTH/2, getCenterY()-SnakeBullet.HEIGHT/2, x1, y1);
        SnakeBullet sb2 = new SnakeBullet(this, getCenterX()-SnakeBullet.WIDTH/2, getCenterY()-SnakeBullet.HEIGHT/2, x2, y2);

        sb0.addAttackEffect(new SlowEffect());
        sb0.addAttackEffect(new PoisonEffect());
        sb1.addAttackEffect(new SlowEffect());
        sb1.addAttackEffect(new PoisonEffect());
        sb2.addAttackEffect(new SlowEffect());
        sb2.addAttackEffect(new PoisonEffect());

        gsm.addEntity(sb0);
        gsm.addEntity(sb1);
        gsm.addEntity(sb2);
    }

    /**
     * Calls sendProjectile in succession every BURST_INTERVAL until TOTAL_BURSTS
     * is reached. Then, waits for BULLET_COOLDOWN to finish before initiating
     * the next attack
     * @param gsm the ServerMaster instance containing the entities ArrayList
     * @param target the attack's target Player
     * @param now the current time in milliseconds when the method is called
     */
    private void handleBurstAttack(ServerMaster gsm, Player target, long now){
        // Check if burst is available
        if (!inBurst && now - lastBulletSend > BULLET_COOLDOWN) {
            inBurst = true;
            burstCount = 0;
        }

        // Send projectiles at intervals
        if (inBurst && burstCount < TOTAL_BURSTS) {
            if (now - lastBurstSend > BURST_INTERVAL){
                sendProjectile(gsm, target);
                burstCount++;
                lastBurstSend = now;
            }
        }

        // Stop burst sending and update lastBulletSend to cooldown attack
        if (inBurst && burstCount >= TOTAL_BURSTS) {
            inBurst = false;
            lastBulletSend = now;
        }
    }

    /**
     * Handles the Phase 2 behavior of the Snake after health is halved. It
     * follows the Player and periodically dashes towards it
     * @param target the player being followed and dashed towards
     * @param now the current time in milliseconds when the method was called
     */
    private void handleConstrictBehavior(Player target, long now){
        switch (currentState) {
            case IDLE:
                speed = 1;
                runFromPlayer(target);
                if (now - lastDashTime > DASH_COOLDOWN){
                    currentState = State.DASH;
                    lastDashTime = now;
                } else currentState = State.PURSUE;
                break;

            case PURSUE:
                if (getSquaredDistanceBetween(this, target) <= DASH_DISTANCE * DASH_DISTANCE) {
                    currentState = State.IDLE;
                } else pursuePlayer(target);
                break;

            case DASH:
                speed = DASH_DISTANCE;
                currSprite = (target.getWorldX() > worldX) ? 5 : 2;
                pursuePlayer(target);
                lastDashTime = now;
                currentState = State.PURSUE;
                break;

            default:
                throw new AssertionError();
        }
        matchHitBoxBounds();
    }
}
