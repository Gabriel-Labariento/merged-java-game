import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The SpiderBullet class extends Attack. It is a projectile sent by
        Spider that uses vectors to travel towards Players.

        @author Niles Tristan Cabrera (240828)
        @author Gabriel Matthew Labariento (242425)
        @version 20 May 2025

        We have not discussed the Java language code in our program
        with anyone other than my instructor or the teaching assistants
        assigned to this course.
        We have not used Java language code obtained from another student,
        or any other unauthorized source, either modified or unmodified.
        If any Java language code or documentation used in our program
        was obtained from another source, such as a textbook or website,
        that has been clearly noted with a proper citation in the comments
        of our program.
**/

 public class SpiderBullet extends Attack {
        public static final int HEIGHT = 16;
        public static final int WIDTH = 16;
        double normalizedX, normalizedY;
        private static BufferedImage sprite;

        // Set the sprite image to the class
        static {
            try {
                BufferedImage img = ImageIO.read(SpiderBullet.class.getResourceAsStream("resources/Sprites/Spider/spiderbullet.png"));
                sprite = img;
            } catch (IOException e) {
                System.out.println("Exception in SpiderBullet setSprites()" + e);
            }
        }

        /**
         * Creates a SpiderBullet instance with fields set to the parameters
         * @param owner the Entity (Spider) creating the attack
         * @param x the x-coordinate
         * @param y the y-coordinate
         * @param nX the x-component of the normalized vector to dictate projectile movement
         * @param nY the y-component of the normalized vector to dictate projectile movement
         */
        public SpiderBullet(Entity owner, int x, int y, double nX, double nY){
            attackNum++;
            id = attackNum;
            identifier = NetworkProtocol.SPIDERBULLET;
            this.owner = owner;
            isFriendly = false;
            damage = 1;
            width = 16;
            height = 16;
            worldX = x;
            worldY = y;
            speed = 2; // Note: Do not make equal to 1. When multiplied with floats, becomes 0.
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
            if (!hasPlayedSound){
                SoundManager.getInstance().playPooledSound("spiderBullet.wav");
                hasPlayedSound = true;
            }
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

        /**
         * Moves the bullet towards the player with the components of the
         * normalized vector
         */
        private void moveBullet(){
            worldX += speed * normalizedX;
            worldY += speed * normalizedY;
            matchHitBoxBounds();
        }

        @Override
        public void updateCarousel() {
            throw new UnsupportedOperationException("Unimplemented method 'updateCarousel'");
        }
        
    }