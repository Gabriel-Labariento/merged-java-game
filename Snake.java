import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Snake extends Enemy {
    private static final int SPRITE_FRAME_DURATION = 200;
    private static final int BULLET_COOLDOWN = 5000;
    private static final int DASH_COOLDOWN = 1000;
    private int SPAWN_COOLDOWN = 5000;
    private static final int DASH_DISTANCE = GameCanvas.TILESIZE * 4;
    private static final int ATTACK_DISTANCE = GameCanvas.TILESIZE * 4;
     private static final int BURST_INTERVAL = 200;
    private static final int TOTAL_BURSTS = 5;

    private long lastSpriteUpdate = 0;
    private long lastBulletSend = 0;

    private int burstCount = 0;
    private boolean inBurst;
    private long lastBurstSend = 0;

    private long lastDashTime = 0;
    
    private long lastSpawnTime = 0;

    private static BufferedImage[] sprites;
    private enum Phase {BULLETS, CONSTRICT};
    private Phase currentPhase;

    private enum State {IDLE, PURSUE, DASH};
    private State currentState;

    static {
        setSprites();
    }

    public Snake(int x, int y) {
        id = -1; // Only one instance, doesn't really matter the value but there has to be one
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
         long now = System.currentTimeMillis();
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
    }

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

    @Override
    public Player scanForPlayer(ServerMaster gsm){
        final int scanRadius = GameCanvas.TILESIZE * 10; // Larger scan radius
        Player closestPlayer = null;
        double minDistance = Integer.MAX_VALUE;

        for (Entity e : gsm.getEntities()) {
            if (e instanceof Player player) {
                if (this.getCurrentRoom() != player.getCurrentRoom()) continue; 
                // Get the center distance between the player and the entity
                double distanceSquared = 
                    (Math.pow(getCenterX() - e.getCenterX(), 2) + Math.pow(getCenterY() - e.getCenterY(), 2));
                
                if ( (distanceSquared <= scanRadius * scanRadius) && (distanceSquared < minDistance)) {
                    closestPlayer = player;
                    minDistance = distanceSquared;
                }
            }
        }
        return closestPlayer;
    }
}
