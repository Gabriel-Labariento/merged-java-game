import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class SpawnIndicator extends Entity {
    private static final int INDICATOR_DURATION = 2000; // 2 seconds
    private static final int PULSE_DURATION = 500; // 0.5 seconds
    private static BufferedImage sprite;
    private static int entityCount = 0;
    private long startTime;
    private float alpha;
    private boolean isPulsing;
    private long lastPulseTime;
    
    static {
        try {
            sprite = ImageIO.read(SpawnIndicator.class.getResourceAsStream("resources/Sprites/SharedEnemy/spawn_indicator.png"));
        } catch (IOException e) {
            System.out.println("Exception in SpawnIndicator setSprite()" + e);
        }
    }
    
    public SpawnIndicator(int x, int y) {
        id = entityCount++;
        identifier = NetworkProtocol.SPAWN_INDICATOR;
        width = GameCanvas.TILESIZE * 2;
        height = GameCanvas.TILESIZE * 2;
        worldX = x;
        worldY = y;
        startTime = System.currentTimeMillis();
        lastPulseTime = startTime;
        alpha = 0.7f;
        isPulsing = true;
        matchHitBoxBounds();
    }
    
    @Override
    public void updateEntity(ServerMaster gsm) {
        long now = System.currentTimeMillis();
        
        // Handle pulsing effect
        if (isPulsing) {
            if (now - lastPulseTime > PULSE_DURATION) {
                alpha = alpha == 0.7f ? 0.3f : 0.7f;
                lastPulseTime = now;
            }
        }
        
        // Check if indicator should be removed
        if (now - startTime > INDICATOR_DURATION) {
            gsm.removeEntity(this);
        }
    }
    
    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset) {
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Set composite for transparency
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // Draw the indicator
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
        
        // Reset composite
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
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