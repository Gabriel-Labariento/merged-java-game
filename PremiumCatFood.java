import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PremiumCatFood extends Item {
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(PremiumCatFood.class.getResourceAsStream("resources/Sprites/Items/premiumcatfood.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }
    
    public PremiumCatFood(int x, int y){
        identifier = NetworkProtocol.PREMIUMCATFOOD;
        worldX = x;
        worldY = y;
        currentRoom = null;
        isConsumable = true;

        matchHitBoxBounds();
    }

    @Override
    public void applyEffects(){
        owner.setDamage(owner.getDamage() + 1);
    }

    @Override
    public void removeEffects(){
        
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}