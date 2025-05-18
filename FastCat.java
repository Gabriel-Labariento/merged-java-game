import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class FastCat extends Player{
    private static BufferedImage[] sprites;

    static {
        setSprites();
    }

    public FastCat(int cid, int x, int y){
        this.clientId = cid;
        identifier = NetworkProtocol.FASTCAT;
        baseSpeed = 5;
        speed = baseSpeed;
        height = 16;
        width = 16;
        screenX = 800/2 - width/2;
        screenY = 600/2 - height/2;
        worldX = x;
        worldY = y;
        maxHealth = 2000;
        hitPoints = maxHealth;
        damage = 20;
        isDown = false;
        attackCDDuration = 600;
        attackFrameDuration = 125;

        matchHitBoxBounds();
    }

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
    }

    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/left0.png"));
            BufferedImage left1 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/left1.png"));
            BufferedImage left2 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/left2.png"));
            BufferedImage right0 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/right0.png"));
            BufferedImage right1 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/right1.png"));
            BufferedImage right2 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/right2.png"));
            BufferedImage attack0 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/attack0.png"));
            BufferedImage attack1 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/attack1.png"));
            BufferedImage attack2 = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/attack2.png"));
            BufferedImage death = ImageIO.read(FastCat.class.getResourceAsStream("resources/Sprites/FastCat/death.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2, attack0, attack1, attack2, death};

        } catch (IOException e) {
            System.out.println("Exception in Rat setSprites()" + e);
        }
    }

    @Override
    public void matchHitBoxBounds() {
        hitBoxBounds = new int[4];
        hitBoxBounds[0]= worldY;
        hitBoxBounds[1] = worldY + height;
        hitBoxBounds[2]= worldX;
        hitBoxBounds[3] = worldX + width;
    }
}