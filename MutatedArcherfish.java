import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class MutatedArcherfish extends Enemy{
    
    private static BufferedImage[] sprites;

    static {
        setSprites();
    }

    public MutatedArcherfish(int x, int y) {
        lastSpriteUpdate = 0;
        lastAttackTime = 0;
        id = enemyCount++;
        identifier = NetworkProtocol.MUTATEDARCHERFISH;
        speed = 1;
        height = 32;
        width = 32;
        worldX = x;
        worldY = y;
        maxHealth = 10;
        hitPoints = maxHealth;
        damage = 1;
        rewardXP = 50;
        currentRoom = null;
        currSprite = 0;
        attackCDDuration = 600;
        
    }

     private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(MutatedArcherfish.class.getResourceAsStream("resources/Sprites/MutatedArcherfish/left0.png"));
            BufferedImage right0 = ImageIO.read(MutatedArcherfish.class.getResourceAsStream("resources/Sprites/MutatedArcherfish/right0.png"));
            sprites = new BufferedImage[] {left0, right0};

        } catch (IOException e) {
            System.out.println("Exception in Rat setSprites()" + e);
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
        final double ACTION_DISTANCE = (GameCanvas.TILESIZE*7) * (GameCanvas.TILESIZE *7);

        
        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        if (getSquaredDistanceBetween(this, pursued) > ACTION_DISTANCE)
            pursuePlayer(pursued);
        else {
            if (now - lastAttackTime > attackCDDuration) {
                createRandomBullet(gsm, pursued);
                lastAttackTime = now;
            }

            //If in aggro range trigger attacks, and run if chased
            if (getSquaredDistanceBetween(this, pursued) < ACTION_DISTANCE){
                runFromPlayer(pursued);
            }
        }
        
        // Sprite walk update
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            if (worldX > pursued.getWorldX()) {
                currSprite = 0;
            } else {
                currSprite = 1;
            }
            lastSpriteUpdate = now;
        }

        matchHitBoxBounds();
    }
}