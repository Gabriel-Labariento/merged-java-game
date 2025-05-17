import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Turtle extends Enemy {
    private static final int SPRITE_FRAME_DURATION = 300;
    private static final int SPAWN_COOLDOWN = 4000;
    private static final int IDLE_DURATION = 2000;
    private static final int ATTACK_COOLDOWN = 6000;
    private static final int SHELL_DURATION = 6000;
    private static final int OUT_OF_SHELL_DURATION = 5000;
    private final int KILLER_DAMAGE = 10;
    private static final int ATTACK_RANGE = GameCanvas.TILESIZE * 3;

    private long lastAttackTime = 0;
    private long lastIdleTime = 0;
    private long lastSpawnTime = 0;
    private long lastShellEnterTime = 0;
    private long lastShellExitTime = 0;
    private long lastSpriteUpdate = 0;

    private static BufferedImage[] sprites;
    private enum Phase {KILLER, SHELL};
    private enum State {IDLE, PURSUE, ATTACK, HIDDEN};

    private State currentState;
    private Phase currentPhase;

    private int realHealth;

    static {
        setSprites();
    }

    public Turtle(int x, int y) {
        id = -1; // Only one instance, doesn't really matter the value but there has to be one
        identifier = NetworkProtocol.TURTLE;
        speed = 1;
        height = 48;
        width = 48;
        worldX = x;
        worldY = y;
        maxHealth = 90;
        hitPoints = maxHealth;
        realHealth = maxHealth;
        damage = KILLER_DAMAGE;
        rewardXP = 200;
        currentRoom = null;
        currSprite = 0;
        currentPhase = Phase.KILLER;
        currentState = State.IDLE;
    }

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
        long now = System.currentTimeMillis();
        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        
        switch (currentPhase) {
            case KILLER:
                handleKillerPhase(now, pursued, gsm);
                break;
            case SHELL:
                handleShellPhase(now, pursued, gsm);
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
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            if (worldY < pursued.getWorldY()) {
                currSprite = (currSprite == 0 || currSprite == 1) ? (currSprite == 0 ? 1 : 0) : 0; // Ensure shuffling between sprites 0 and 1
            } else {
                currSprite = (currSprite == 4 || currSprite == 5) ? (currSprite == 4 ? 5 : 4) : 4; // Ensure shuffling between sprites 4 and 5
            } lastSpriteUpdate = now;
        }
        matchHitBoxBounds();
    }

    private void handleShellPhase(long now, Player pursued, ServerMaster gsm){
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
