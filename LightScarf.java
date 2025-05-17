import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class LightScarf extends Item {
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(LightScarf.class.getResourceAsStream("resources/Sprites/Items/lightscarf.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }

    public LightScarf(int x, int y){
        identifier = NetworkProtocol.LIGHTSCARF;
        worldX = x;
        worldY = y;
        currentRoom = null;

        matchHitBoxBounds();
    }

    @Override
    public void applyEffects(){
        initialCDDuration = owner.getAttackCDDuration();
        owner.setAttackCDDuration((int) Math.round(initialCDDuration*0.5));

        initialAttackFrameDuration = owner.getAttackFrameDuration();
        owner.setAttackFrameDuration((int) Math.round(initialAttackFrameDuration*0.5));

        initialMaxHealth = owner.getMaxHealth();
        owner.setMaxHealth((int) Math.round(initialMaxHealth*0.25));

    }

    @Override
    public void removeEffects(){
        owner.setAttackCDDuration(initialCDDuration);
        owner.setMaxHealth(initialMaxHealth);
        owner.setAttackFrameDuration(initialAttackFrameDuration);
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}