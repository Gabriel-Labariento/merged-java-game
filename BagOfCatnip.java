import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BagOfCatnip extends Item {
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(BagOfCatnip.class.getResourceAsStream("resources/Sprites/Items/bagofcatnip.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }

    public BagOfCatnip(int x, int y){
        identifier = NetworkProtocol.BAGOFCATNIP;
        worldX = x;
        worldY = y;
        currentRoom = null;

        matchHitBoxBounds();
    }

    @Override
    public void applyEffects(){
        initialCDDuration = owner.getAttackCDDuration();
        owner.setAttackCDDuration((int) Math.round(initialCDDuration*1.25));

        initialDamage = owner.getDamage();
        owner.setDamage((int) Math.round(initialDamage*2.0));
    }

    @Override
    public void removeEffects(){
        owner.setAttackCDDuration(initialCDDuration);
        owner.setDamage(initialDamage);
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}
