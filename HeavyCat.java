import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HeavyCat extends Player{
    private static BufferedImage[] sprites;

    static {
        setSprites();
    }

    public HeavyCat(int cid, int x, int y){
        this.clientId = cid;
        identifier = NetworkProtocol.HEAVYCAT;
        baseSpeed = 2;
        speed = baseSpeed;
        height = 16;
        width = 16;
        screenX = 800/2 - width/2;
        screenY = 600/2 - height/2;
        worldX = x;
        worldY = y;
        maxHealth = 6;
        hitPoints = maxHealth;
        damage = 5;
        isDown = false;
        attackCDDuration = 1200;
        currSprite = 0;
        attackFrameDuration = 200;

        matchHitBoxBounds();
    }

    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/left0.png"));
            BufferedImage left1 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/left1.png"));
            BufferedImage left2 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/left2.png"));
            BufferedImage right0 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/right0.png"));
            BufferedImage right1 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/right1.png"));
            BufferedImage right2 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/right2.png"));
            BufferedImage attack0 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/attack0.png"));
            BufferedImage attack1 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/attack1.png"));
            BufferedImage attack2 = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/attack2.png"));
            BufferedImage death = ImageIO.read(HeavyCat.class.getResourceAsStream("resources/Sprites/HeavyCat/death.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2, attack0, attack1, attack2, death};

        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }



    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
    }

    @Override
    public void levelUpStats(){
        hitPoints += 1;
        maxHealth += 1;
        damage += 1;
        speed += 0;
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