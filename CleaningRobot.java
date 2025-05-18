import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The CleaningRobot class extends the Enemy class. It gives 
        appears in the game's fifth level. It runs away from the player
        when the player is far. When the player is close, it activates
        an defensive attack.

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

public class CleaningRobot extends Enemy{
    private static BufferedImage[] sprites;

    /**
     * Calls the static setSprites() method
     */
    static {
        setSprites();
    }

    /**
     * Creates a CleaningRobot instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public CleaningRobot(int x, int y) {
        id = enemyCount++;
        lastSpriteUpdate = 0;
        lastAttackTime = 0;
        identifier = NetworkProtocol.CLEANINGBOT;
        speed = 1;
        height = 24;
        width = 24;
        worldX = x;
        worldY = y;
        maxHealth = 10;
        hitPoints = maxHealth;
        damage = 1;
        rewardXP = 50;
        currentRoom = null;
        currSprite = 0;
        attackCDDuration = 1300;
        
    }

    /**
     * Sets the sprites to the class instead of the individual instances
     */
     private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(CleaningRobot.class.getResourceAsStream("resources/Sprites/CleaningBot/left0.png"));
            BufferedImage right0 = ImageIO.read(CleaningRobot.class.getResourceAsStream("resources/Sprites/CleaningBot/right0.png"));
            sprites = new BufferedImage[] {left0, right0};

        } catch (IOException e) {
            System.out.println("Exception in CleaningRobot setSprites()" + e);
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

    @Override
    public void draw(Graphics2D g2d, int xOffset, int yOffset){
        g2d.drawImage(sprites[currSprite], xOffset, yOffset, width, height, null);
    }


    @Override
    public void updateEntity(ServerMaster gsm){

        now = System.currentTimeMillis();
        final double AGGRO_DISTANCE = (GameCanvas.TILESIZE * 3) * (GameCanvas.TILESIZE * 3);

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        double distanceSquared = getSquaredDistanceBetween(this, pursued);

        // Check if the player is within aggro distance
        if (distanceSquared <= AGGRO_DISTANCE) {
            // If the attack has cooled down, attack.
            if (now - lastAttackTime > attackCDDuration) {
                activateDefense(gsm);
                lastAttackTime = now;
            }
        } else runFromPlayer(pursued);
        
        // Sprite walk update
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            if (worldX > pursued.getWorldX()) {
                currSprite = 0;
            } else {
                currSprite = 1;
            }
            lastSpriteUpdate = now;
        }

        matchHitBoxBounds();
    }

    /**
     * Creates and fires 8 LaserBullet objects around the CleaningRobot instance
     * @param gsm the ServerMaster containing the entities ArrayList where LaserBullets are added 
     */
    private void activateDefense(ServerMaster gsm){
        double radius = GameCanvas.TILESIZE;

        int centerX = getCenterX();
        int centerY = getCenterY();
        
        // Create 8 bullets around robot in a circle
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            
            // Calculate normalized direction vector
            double dirX = Math.cos(angle);
            double dirY = Math.sin(angle);
            
            // Calculate the spawn point on circle
            double spawnX = Math.round((centerX + (dirX * radius)) -  LaserBullet.WIDTH / 2.0);
            double spawnY = Math.round((centerY + (dirY * radius)) - LaserBullet.HEIGHT / 2.0);
            
            LaserBullet bullet = new LaserBullet(this, (int)spawnX, (int)spawnY, dirX, dirY);
            gsm.addEntity(bullet);

        }
    }
}

