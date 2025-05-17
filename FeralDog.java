import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class FeralDog extends Enemy {
    private int SPAWN_COOLDOWN = 5000;
    private int ATTACK_COOLDOWN = 4000;
    private static final int ATTACK_RANGE = GameCanvas.TILESIZE * 3;
    private long lastSpawnTime = 0;
    private static BufferedImage[] sprites;
    private enum Phase {NORMAL, BUFFED}
    private enum State {IDLE, PURSUE, ATTACK};
    private Phase currentPhase;
    private State currentState;

    static {
        setSprites();
    }

    public FeralDog(int x, int y) {
        id = -1; // Only one instance, doesn't really matter the value but there has to be one
        identifier = NetworkProtocol.FERALDOG;
        speed = 1;
        height = 30;
        width = 46;
        worldX = x;
        worldY = y;
        maxHealth = 100;
        hitPoints = maxHealth;
        damage = 2;
        rewardXP = 200;
        currentRoom = null;
        currSprite = 0;
        currentPhase = Phase.NORMAL;
        currentState = State.IDLE;
    }

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
                if (hitPoints <= maxHealth / 2) currentPhase = Phase.BUFFED;
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

    private void handleStateBehavior(long now, Player pursued, ServerMaster gsm){
        switch (currentState) {
            case IDLE:
                if (now - lastAttackTime > ATTACK_COOLDOWN){
                    currentState = State.ATTACK;
                    lastAttackTime = now;
                } else currentState = State.PURSUE;
                break;

            case PURSUE:
                if (getSquaredDistanceBetween(this, pursued) <= ATTACK_RANGE * ATTACK_RANGE) {
                    currentState = State.IDLE;
                } else pursuePlayer(pursued);
                break;

            case ATTACK:
                createSlashAttack(gsm, pursued, null);
                currSprite = (pursued.getWorldX() > worldX) ? 5 : 2;
                lastAttackTime = now;
                currentState = State.PURSUE;
                break;
        }
    }

    private void updateWalkFrame(long now, Player pursued){
        // Sprite walk update
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            if (currentPhase == Phase.NORMAL) {
                if (worldX > pursued.getWorldX()) { // LEFT
                currSprite++;
                if (currSprite > 2) currSprite = 0;
                } else if (worldX < pursued.getWorldX()) { // RIGHT
                    currSprite++;
                    if (currSprite < 3 || currSprite > 5) currSprite = 3;
                } lastSpriteUpdate = now;
            } else { // BUFFED SPRITES
                if (worldX > pursued.getWorldX()) { // LEFT
                    if (currSprite < 6 || currSprite > 8) currSprite = 6;
                    else currSprite = (currSprite + 1 > 8) ? 6 : currSprite + 1;
                } else if (worldX < pursued.getWorldX()) { // RIGHT
                    if (currSprite < 9 || currSprite > 11) currSprite = 9;
                    else currSprite = (currSprite + 1 > 11) ? 9 : currSprite + 1;
                } lastSpriteUpdate = now;
            }            
        }
    }
}
