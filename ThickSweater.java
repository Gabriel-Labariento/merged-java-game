import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ThickSweater extends Item {
    private long regenTime;
    private static final int REGENCDDURATION = 3000;
    private boolean isFirstTimeUse;
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(ThickSweater.class.getResourceAsStream("resources/Sprites/Items/thicksweater.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }

    
    public ThickSweater(int x, int y){
        identifier = NetworkProtocol.THICKSWEATER;
        worldX = x;
        worldY = y;
        currentRoom = null;
        isFirstTimeUse = true;
        regenTime = 0;

        matchHitBoxBounds();

    }

    @Override
    public void applyEffects(){
        initialSpeed = owner.getSpeed();
        owner.setSpeed(1);
    }

    @Override
    public void removeEffects(){
        owner.setSpeed(initialSpeed);
    }

    public void triggerRegenSystem(){
        if(regenTime < System.currentTimeMillis()){     
            triggerRegenTimer();

            //Stop healspam when dropping and picking up item
            if (!isFirstTimeUse) owner.setHitPoints(owner.getHitPoints() + 1);
            isFirstTimeUse = false;
        }
    }

    private void triggerRegenTimer(){
        regenTime = System.currentTimeMillis() + REGENCDDURATION;
    }

    public void setIsFirstTimeUse(boolean b){
        isFirstTimeUse = b;
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}