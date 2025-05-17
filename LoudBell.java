import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class LoudBell extends Item {
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(LoudBell.class.getResourceAsStream("resources/Sprites/Items/loudbell.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }
    
    public LoudBell(int x, int y){
        identifier = NetworkProtocol.LOUDBELL;
        worldX = x;
        worldY = y;
        currentRoom = null;

        matchHitBoxBounds();
    }

    @Override
    public void applyEffects(){
        initialDefense = owner.getDefense();
        owner.setDefense(initialDefense-100);

        initialCDDuration = owner.getAttackCDDuration();
        owner.setAttackCDDuration((int) Math.round(initialCDDuration*0.75));

        initialAttackFrameDuration = owner.getAttackFrameDuration();
        owner.setAttackFrameDuration((int) Math.round(initialAttackFrameDuration*0.75));

        initialDamage = owner.getDamage();
        owner.setDamage((int) Math.round(initialDamage*2.0));
    }

    @Override
    public void removeEffects(){
        owner.setDefense(initialDefense);
        owner.setAttackCDDuration(initialCDDuration);
        owner.setDamage(initialDamage);
        owner.setAttackFrameDuration(initialAttackFrameDuration);
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}