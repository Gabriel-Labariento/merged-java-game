import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Milk extends Item {
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(Milk.class.getResourceAsStream("resources/Sprites/Items/milk.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }
    
    public Milk(int x, int y){
        identifier = NetworkProtocol.MILK;
        worldX = x;
        worldY = y;
        currentRoom = null;
        isConsumable = true;

        matchHitBoxBounds();
    }

    @Override
    public void applyEffects(){
        owner.setMaxHealth(owner.getMaxHealth() + 1);
        owner.setHitPoints(owner.getHitPoints() + 1);
    }

    @Override
    public void removeEffects(){
        
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}
