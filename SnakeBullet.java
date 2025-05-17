
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

 public class SnakeBullet extends Attack {
        public static final int HEIGHT = 10;
        public static final int WIDTH = 10;
        double normalizedX, normalizedY;
        private static BufferedImage sprite;

        static {
            try {
                BufferedImage img = ImageIO.read(SnakeBullet.class.getResourceAsStream("resources/Sprites/Snakelet/snake_bullet.png"));
                sprite = img;
            } catch (IOException e) {
                System.out.println("Exception in SnakeBullet setSprites()" + e);
            }
        }

        public SnakeBullet(Entity owner, int x, int y, double nX, double nY){
            attackNum++;
            id = attackNum;
            identifier = NetworkProtocol.SNAKEBULLET;
            this.owner = owner;
            isFriendly = false;
            damage = 1;
            //Temporary hitPoints allocation
            width = WIDTH;
            height = HEIGHT;
            worldX = x;
            worldY = y;
            speed = 3; // Note: Do not make equal to 1. When multiplied with floats, becomes 0.
            normalizedX = nX;
            normalizedY = nY;

            //For checking attack duration
            duration = 2000;
            setExpirationTime(duration);

            matchHitBoxBounds();
        }

        @Override
        public void draw(Graphics2D g2d, int xOffset, int yOffset){
            g2d.drawImage(sprite, xOffset, yOffset, width, height, null);
        }

        @Override
        public void updateEntity(ServerMaster gsm) {
            moveBullet();
        }

        @Override
        public void matchHitBoxBounds() {
            hitBoxBounds = new int[4];
            hitBoxBounds[0]= worldY;
            hitBoxBounds[1] = worldY + height;
            hitBoxBounds[2]= worldX;
            hitBoxBounds[3] = worldX + width;
        }

        private void moveBullet(){
            worldX += speed * normalizedX;
            worldY += speed * normalizedY;
            matchHitBoxBounds();
        }
        
    }