import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class RedFish extends Item {
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(RedFish.class.getResourceAsStream("resources/Sprites/Items/redfish.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }
    
    public RedFish(int x, int y){
        identifier = NetworkProtocol.REDFISH;
        worldX = x;
        worldY = y;
        currentRoom = null;
        isConsumable = true;

        matchHitBoxBounds();
    }

    @Override
    public void applyEffects(){
        double restoredHP = Math.round(owner.getMaxHealth()*0.25);
        owner.setHitPoints(owner.getHitPoints() + (int) restoredHP);
    }

    @Override
    public void removeEffects(){}

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }

}
