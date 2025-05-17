import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GunCat extends Player{
    private static BufferedImage[] sprites;

    static {
        setSprites();
    }
    

    public GunCat(int cid, int x, int y){
        this.clientId = cid;
        identifier = NetworkProtocol.GUNCAT;
        baseSpeed = 2;
        speed = baseSpeed;
        height = 16;
        width = 16;
        screenX = 800/2 - width/2;
        screenY = 600/2 - height/2;
        worldX = x;
        worldY = y;
        maxHealth = 2;
        hitPoints = maxHealth;
        damage = 5;
        isDown = false;
        attackCDDuration = 800;
        attackFrameDuration = 75;
        matchHitBoxBounds();
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
    }

    
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/left0.png"));
            BufferedImage left1 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/left1.png"));
            BufferedImage left2 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/left2.png"));
            BufferedImage right0 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/right0.png"));
            BufferedImage right1 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/right1.png"));
            BufferedImage right2 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/right2.png"));
            BufferedImage attack0 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/attack0.png"));
            BufferedImage attack1 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/attack1.png"));
            BufferedImage attack2 = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/attack2.png"));
            BufferedImage death = ImageIO.read(GunCat.class.getResourceAsStream("resources/Sprites/GunCat/death.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2, attack0, attack1, attack2, death};

        } catch (IOException e) {
            System.out.println("Exception in Rat setSprites()" + e);
        }
    }s

    @Override
    public void matchHitBoxBounds() {
        hitBoxBounds = new int[4];
        hitBoxBounds[0]= worldY;
        hitBoxBounds[1] = worldY + height;
        hitBoxBounds[2]= worldX;
        hitBoxBounds[3] = worldX + width;
    }
}