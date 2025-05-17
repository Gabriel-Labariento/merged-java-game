import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class EnemyBite extends Attack{
    public static final int HEIGHT = 16;
    public static final int WIDTH = 16;
    private static BufferedImage sprite;

    static {
        try {
            BufferedImage img = ImageIO.read(EnemyBite.class.getResourceAsStream("resources/Sprites/SharedEnemy/enemyslash.png"));
            sprite = img;
        } catch (IOException e) {
            System.out.println("Exception in EnemyBite setSprites()" + e);
        }
    }

    public EnemyBite(Entity owner, int x, int y){
        attackNum++;
        id = attackNum;
        identifier = NetworkProtocol.ENEMYBITE;
        this.owner = owner;
        isFriendly = false;
        damage = 1;
        width = WIDTH;
        height = HEIGHT;
        worldX = x;
        worldY = y;

        //For checking attack duration
        duration = 400;
        setExpirationTime(duration);

        matchHitBoxBounds();
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
    }

    @Override
    public void matchHitBoxBounds(){
        // Bounds array is formatted as such: top, bottom, left, right; SIZES SUBJECT TO CHANGE PER ENTITY
        hitBoxBounds = new int[4];
        hitBoxBounds[0]= worldY;
        hitBoxBounds[1] = worldY + height;
        hitBoxBounds[2]= worldX;
        hitBoxBounds[3] = worldX + width;
    }

    @Override
    public void updateEntity(ServerMaster gsm) {
        attachToOwner();
    }
}
