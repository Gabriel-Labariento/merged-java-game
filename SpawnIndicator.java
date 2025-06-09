import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SpawnIndicator extends Entity {
    private static final int INDICATOR_DURATION = 2000; // 2 seconds
    private static final int SPRITE_DURATION = 20;
    
    private static BufferedImage sprites[];
    private static int entityCount = 0;
    private long startTime = 0;
    private long lastSpriteChange = 0;
    private boolean hasPlayedSound;

    static {
        try {
            BufferedImage redSpawnIndicator = ImageIO.read(SpawnIndicator.class.getResourceAsStream("/resources/Sprites/SharedEnemy/spawnindicator.png"));
            BufferedImage whiteSpawnIndicator = ImageIO.read(SpawnIndicator.class.getResourceAsStream("/resources/Sprites/SharedEnemy/spawnindicatorwhite.png"));
            BufferedImage blackSpawnIndicator = ImageIO.read(SpawnIndicator.class.getResourceAsStream("/resources/Sprites/SharedEnemy/spawnindicatorblack.png"));
            sprites = new BufferedImage[] {redSpawnIndicator, whiteSpawnIndicator, blackSpawnIndicator};
        } catch (IOException e) {
            System.out.println("Exception in SpawnIndicator setSprite(): " + e);
            e.printStackTrace();
        }
    }
    
    public SpawnIndicator(int x, int y) {
        id = entityCount++;
        identifier = NetworkProtocol.SPAWN_INDICATOR;
        width = 32;
        height = 32;
        worldX = x;
        worldY = y;
        currSprite = 0;
        startTime = System.currentTimeMillis();
        currentRoom = null;
        hasPlayedSound = false;
    }
    
    @Override
    public void updateEntity(ServerMaster gsm) {
        if (!hasPlayedSound){
            SoundManager.getInstance().playPooledSound("monsterSpawn.wav");
            hasPlayedSound = true;
        }

        long now = System.currentTimeMillis();
        
        if (now - lastSpriteChange > SPRITE_DURATION) {
            currSprite = (currSprite + 1) % 3;
            lastSpriteChange = now;
        }

        // Check if indicator should be removed
        if (now - startTime > INDICATOR_DURATION) {
            gsm.removeEntity(this);
        }

        matchHitBoxBounds();
    }
    
    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset) {
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the indicator at the correct screen position
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
    }
    
    @Override
    public void matchHitBoxBounds() {
        hitBoxBounds = new int[4];
        hitBoxBounds[0] = worldY;
        hitBoxBounds[1] = worldY + height;
        hitBoxBounds[2] = worldX;
        hitBoxBounds[3] = worldX + width;
    }
} 