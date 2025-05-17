import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PlayerBullet extends Attack{
    private double normalizedX;
    private double normalizedY;
    public static final int WIDTH = 16;
    public static final int HEIGHT = 16;
    public static BufferedImage sprite;
    static {
        try {
            sprite = ImageIO.read(PlayerBullet.class.getResourceAsStream("resources/Sprites/Attacks/playerbullet.png"));
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }

    
    public PlayerBullet(int cid, Entity entity, int x, int y, double nX, double nY, int d, boolean isFriendly){
        attackNum++;
        id = attackNum;
        clientId = cid;
        identifier = NetworkProtocol.PLAYERBULLET;
        owner = entity;
        this.isFriendly = isFriendly;
        damage = d;
        //Temporary hitPoints allocation
        width = WIDTH;
        height = HEIGHT;
        worldX = x;
        worldY = y;
        speed = 4;
        normalizedX = nX;
        normalizedY = nY;

        //For checking attack duration
        duration = 10000;
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

    private void moveBullet(){
        worldX += speed*normalizedX;
        worldY += speed*normalizedY;
        matchHitBoxBounds();
    }

    @Override
    public void updateEntity(ServerMaster gsm) {
        moveBullet();
    }

}