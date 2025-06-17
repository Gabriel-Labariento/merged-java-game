import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;

public class DamageIndicator extends Entity {
    private static final int INDICATOR_DURATION = 1200; // 2 seconds
    private static final int SPRITE_DURATION = 200;
    private long lastMoveTime;
    private static final int MOVE_DELAY_DURATION = 40;
    private static int entityCount = 0;
    private long startTime = 0;
    private long lastSpriteChange = 0;
    private static Font gameFont;
    private static Font indicatorFont;

    static {
        try {
            gameFont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/Fonts/PressStart2P-Regular.ttf"));
            indicatorFont = gameFont.deriveFont(8f);
        } catch (IOException | FontFormatException e) {
            System.out.println("Exception in SpawnIndicator setSprite(): " + e);
        }
    }
    
    public DamageIndicator(int x, int y) {
        id = entityCount++;
        identifier = NetworkProtocol.DAMAGE_INDICATOR;
        width = 8;
        height = 8;
        worldX = x;
        worldY = y;
        currSprite = 0;
        zIndex = 2;
        maxHealth = Integer.MAX_VALUE;
        startTime = System.currentTimeMillis();
        currentRoom = null;
    }
    
    @Override
    public void updateEntity(ServerMaster gsm) {
        // if (!hasPlayedSound){
        //     SoundManager.getInstance().playPooledSound("monsterSpawn.wav");
        //     hasPlayedSound = true;
        // }

        long now = System.currentTimeMillis();
        
        if (now - lastSpriteChange > SPRITE_DURATION) {
            currSprite = (currSprite == 0) ? 1 : 0;
            lastSpriteChange = now;
        }

        // Check if indicator should be removed
        if (now - startTime > INDICATOR_DURATION) {
            gsm.removeEntity(this);
        }

        if (now - lastMoveTime > MOVE_DELAY_DURATION){
            // Slowly glide up
            worldY--;
            lastMoveTime = now;
        }
        

        matchHitBoxBounds();
    }
    
    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset) {
        // Switch colors
        if(currSprite == 0) g2d.setColor(Color.WHITE);
        else g2d.setColor(Color.BLACK);

        //Center text
        FontMetrics metrics = g2d.getFontMetrics(indicatorFont);
        String damageDealt = String.valueOf(hitPoints);
        int textWidth = metrics.stringWidth(damageDealt);
        int drawX = (int) (xOffset- textWidth / 2.0);
        g2d.setFont(indicatorFont);  
        g2d.drawString(String.valueOf(hitPoints), drawX, yOffset);
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

