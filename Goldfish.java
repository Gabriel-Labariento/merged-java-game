import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Goldfish extends Item {
    public static BufferedImage sprite;
        static {
            try {
                sprite = ImageIO.read(Goldfish.class.getResourceAsStream("resources/Sprites/Items/goldfish.png"));
            } catch (IOException e) {
                System.out.println("Exception in setSprites()" + e);
            }
        }

    public Goldfish(int x, int y){
        identifier = NetworkProtocol.GOLDFISH;
        worldX = x;
        worldY = y;
        currentRoom = null;

        matchHitBoxBounds();
    }

    @Override
    public void applyEffects(){
    }

    @Override
    public void removeEffects(){
        
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }
}