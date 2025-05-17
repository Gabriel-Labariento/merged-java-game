import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class CatTreat extends Item {
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(CatTreat.class.getResourceAsStream("resources/Sprites/Items/cattreat.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }

    public CatTreat(int x, int y){
        identifier = NetworkProtocol.CATTREAT;
        worldX = x;
        worldY = y;
        currentRoom = null;
        isConsumable = true;

        matchHitBoxBounds();
    }

    @Override
    public void applyEffects(){
        double addedXP = Math.round((owner.getCurrentXPCap() - owner.getPastXPCap())*0.15);
        owner.applyXP((int) addedXP);
    }

    @Override
    public void removeEffects(){
        
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }

    

    
    
}