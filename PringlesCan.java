import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PringlesCan extends Item {
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(PringlesCan.class.getResourceAsStream("resources/Sprites/Items/pringlescan.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }
    
    public PringlesCan(int x, int y){
        identifier = NetworkProtocol.PRINGLESCAN;
        worldX = x;
        worldY = y;
        currentRoom = null;

        matchHitBoxBounds();
    }

    @Override
    public void applyEffects(){
        initialDefense = owner.getDefense();
        owner.setDefense(initialDefense+50);

        initialCDDuration = owner.getAttackCDDuration();
        owner.setAttackCDDuration((int) Math.round(initialCDDuration*1.25));

        initialDamage = owner.getDamage();
        owner.setDamage((int) Math.round(initialDamage*0.75));
    }

    @Override
    public void removeEffects(){
        owner.setDefense(initialDefense);
        owner.setAttackCDDuration(initialCDDuration);
        owner.setDamage(initialDamage);
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}