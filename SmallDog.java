import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SmallDog extends Enemy{
    private static final int ATTACK_COOLDOWN = 1500;
    private static BufferedImage[] sprites;

    static {
        setSprites();
    }

    public SmallDog(int x, int y) {
        id = enemyCount++;
        identifier = NetworkProtocol.SMALLDOG;
        speed = 1;
        height = 16;
        width = 20;
        worldX = x;
        worldY = y;
        maxHealth = 20;
        hitPoints = maxHealth;
        damage = 2;
        rewardXP = 75;
        currentRoom = null;
        currSprite = 0;
        
    }

     private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_left0.png"));
            BufferedImage left1 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_left1.png"));
            BufferedImage left2 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_left2.png"));
            BufferedImage right0 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_right0.png"));
            BufferedImage right1 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_right1.png"));
            BufferedImage right2 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_right2.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2};

        } catch (IOException e) {
            System.out.println("Exception in SmallDog setSprites()" + e);
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
        final double BITE_DISTANCE = GameCanvas.TILESIZE * 2.5;
        final double BARK_DISTANCE = GameCanvas.TILESIZE * 3.5;

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;

        double distanceSquared = getSquaredDistanceBetween(this, pursued);
        // If within biting distance, create a bite attack
        if ( distanceSquared <= BITE_DISTANCE * BITE_DISTANCE) {
            if (now - lastAttackTime > ATTACK_COOLDOWN ) {
                createBiteAttack(gsm, pursued, null);
                lastAttackTime = now;
            }
        // If farther than biting distance but within barking distance, create bark attack
        } else if (distanceSquared <  BARK_DISTANCE * BARK_DISTANCE) {
            if (now - lastAttackTime > ATTACK_COOLDOWN) {
                createSlashAttack(gsm, pursued, null);
                lastAttackTime = now;
            }
        // If too far to attack, pursue
        } else {
            pursuePlayer(pursued);
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
}
