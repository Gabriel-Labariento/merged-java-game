import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class RatKing extends Enemy {
    private static final int SPAWN_COOLDOWN = 4000;
    private static final int ATTACK_COOLDOWN = 5000;
    private static final int SMASH_COOLDOWN = 3000;
    private static final int ATTACK_RANGE = GameCanvas.TILESIZE * 3;
    private long lastSpawnTime = 0;
    private long lastSmashTime = 0;
    private static BufferedImage[] sprites;
    private enum Phase {MOVE_AWAY, CHASE}
    private Phase currentPhase;

    static {
        setSprites();
    }

    public RatKing(int x, int y) {
        isBoss = true;
        enemyCount++;
        identifier = NetworkProtocol.RATKING;
        speed = 1;
        height = 48;
        width = 48;
        worldX = x;
        worldY = y;
        maxHealth = 150;
        hitPoints = maxHealth;
        damage = 2;
        rewardXP = 200;
        currentRoom = null;
        currSprite = 0;
        currentPhase = Phase.MOVE_AWAY;
        isBoss = true;
    }

    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(RatKing.class.getResourceAsStream("resources/Sprites/RatKing/ratking_left_0.png"));
            BufferedImage left1 = ImageIO.read(RatKing.class.getResourceAsStream("resources/Sprites/RatKing/ratking_left_1.png"));
            BufferedImage left2 = ImageIO.read(RatKing.class.getResourceAsStream("resources/Sprites/RatKing/ratking_left_blur.png"));
            BufferedImage right0 = ImageIO.read(RatKing.class.getResourceAsStream("resources/Sprites/RatKing/ratking_right_0.png"));
            BufferedImage right1 = ImageIO.read(RatKing.class.getResourceAsStream("resources/Sprites/RatKing/ratking_right_1.png"));
            BufferedImage right2 = ImageIO.read(RatKing.class.getResourceAsStream("resources/Sprites/RatKing/ratking_right_blur.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2};

        } catch (IOException e) {
            System.out.println("Exception in RatKing setSprites()" + e);
        }
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
    }

    @Override
    public void matchHitBoxBounds() {
        hitBoxBounds = new int[4];
        hitBoxBounds[0]= worldY + 3;
        hitBoxBounds[1] = worldY + height - 3;
        hitBoxBounds[2]= worldX + 5;
        hitBoxBounds[3] = worldX + width - 5 ;
    }
    
    @Override
    public void updateEntity(ServerMaster gsm){
        long now = System.currentTimeMillis();
        Player pursued = scanForPlayer(gsm);

        switch (currentPhase) {
            case MOVE_AWAY:
                if (hitPoints <= maxHealth / 2) currentPhase = Phase.CHASE;
                else {
                    if (pursued == null) return;

                    // Moving away
                    runFromPlayer(pursued);

                    // Spawning new enemies
                    if (now - lastSpawnTime > SPAWN_COOLDOWN) {
                        if (currentRoom != null && currentRoom.getMobSpawner() != null) {
                            Enemy newSpawn = currentRoom.getMobSpawner().createNormalEnemy(currentRoom.getGameLevel());
                            currentRoom.getMobSpawner().spawnEnemy(newSpawn);
                            lastSpawnTime = now;
                        }
                    }

                    // Attack player in range
                    if (getSquaredDistanceBetween(this, pursued) <= ATTACK_RANGE * ATTACK_RANGE) {
                        if (now - lastAttackTime > ATTACK_COOLDOWN) {
                            createSlashAttack(gsm, pursued, null);
                            lastAttackTime = now;
                        } 
                    }
                }
                break;

            case CHASE:
                if (pursued == null) return;    

                double squaredDistance = getSquaredDistanceBetween(this, pursued);

                // Smash attack
                if ( squaredDistance <= ATTACK_RANGE * ATTACK_RANGE ){
                    if (now - lastSmashTime > SMASH_COOLDOWN) {
                        performSmashAttack(gsm);
                        lastSmashTime = now;
                    }
                } 
                
                // Wide attack
                if ( squaredDistance <= ATTACK_RANGE * ATTACK_RANGE) {
                    if (now - lastAttackTime > ATTACK_COOLDOWN){
                        createSlashAttack(gsm, pursued, null);
                        lastAttackTime = now;
                    }
                } 
                
                pursuePlayer(pursued);
                break;
            default:
                throw new AssertionError();
        }

        // Sprite walk update
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            if (worldX > pursued.getWorldX()) {
                currSprite++;
                if (currSprite > 2) currSprite = 0;
            } else {
                currSprite++;
                if (currSprite < 3 || currSprite > 5) currSprite = 3;
            } lastSpriteUpdate = now;
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
