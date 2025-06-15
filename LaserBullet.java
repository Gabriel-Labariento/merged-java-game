import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The LaserBullet class extends Attack. It is a fast moving
        bullet that travels from an Entity origin outward using vectors.

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

 public class LaserBullet extends Attack {
        public static final int HEIGHT = 16;
        public static final int WIDTH = 16;
        double normalizedX, normalizedY;
        private static BufferedImage sprite;

        // Sets sprite
        static {
            try {
                BufferedImage img = ImageIO.read(LaserBullet.class.getResourceAsStream("resources/Sprites/Attacks/laserbullet.png"));
                sprite = img;
            } catch (IOException e) {
                System.out.println("Exception in setSprites()" + e);
            }
        }
        
        /**
         * Creates a LaserBullet instance with appropriate fields
         * @param owner the Entity spawning the Attack
         * @param x the spawn x-coordinate
         * @param y the spawn y-coordinate
         * @param nX the normalized x coordinate of the direction to travel to
         * @param nY the normalized y coordinate of the direction to travel to
         */
        public LaserBullet(Entity owner, int x, int y, double nX, double nY){
            attackNum++;
            id = attackNum;
            identifier = NetworkProtocol.LASERBULLET;
            this.owner = owner;
            isFriendly = false;
            damage = 1;
            width = 16;
            height = 16;
            worldX = x;
            worldY = y;
            speed = 4; // Note: Do not make equal to 1. When multiplied with floats, becomes 0.
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
        public void updateEntity(ServerMaster gsm) {
            moveBullet();
            if (!hasPlayedSound){
                SoundManager.getInstance().playPooledSound("laserBullet.wav");
                hasPlayedSound = true;
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

        /**
         * Moves the bullet along a trajectory based on the speed and the direction
         * provided by normalizedX and normalizedY
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
