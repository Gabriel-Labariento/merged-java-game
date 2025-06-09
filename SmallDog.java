import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The SmallDog class extends Enemy. It appears in level two of the game.
        It creates two attacks: a bark (wide) attack if the player is far and a
        bite (narrow) attack if a player is near. It also chases the player around
        the room.

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

public class SmallDog extends Enemy{
    private static final int ATTACK_COOLDOWN = 1500;
    private static BufferedImage[] sprites;

    // Call the static setSprites() method
    static {
        setSprites();
    }

    /**
     * Creates a SmallDog instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public SmallDog(int x, int y) {
        id = enemyCount++;
        identifier = NetworkProtocol.SMALLDOG;
        speed = 1;
        height = 16;
        width = 20;
        worldX = x;
        worldY = y;
        maxHealth = 20;
        hitPoints = maxHealth;
        damage = 2;
        rewardXP = 75;
        currentRoom = null;
        currSprite = 0;
        
    }

    /**
     * Set the sprite images to the class and not the instance
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_left0.png"));
            BufferedImage left1 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_left1.png"));
            BufferedImage left2 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_left2.png"));
            BufferedImage right0 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_right0.png"));
            BufferedImage right1 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_right1.png"));
            BufferedImage right2 = ImageIO.read(SmallDog.class.getResourceAsStream("resources/Sprites/SmallDog/dog_right2.png"));
            sprites = new BufferedImage[] {left0, left1, left2, right0, right1, right2};

        } catch (IOException e) {
            System.out.println("Exception in SmallDog setSprites()" + e);
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
        final double BITE_DISTANCE = GameCanvas.TILESIZE * 2.5;
        final double BARK_DISTANCE = GameCanvas.TILESIZE * 3.5;

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;

        double distanceSquared = getSquaredDistanceBetween(this, pursued);
        // If within biting distance, create a bite attack
        if ( distanceSquared <= BITE_DISTANCE * BITE_DISTANCE) {
            if (now - lastAttackTime > ATTACK_COOLDOWN ) {
                createBiteAttack(gsm, pursued, null);
                lastAttackTime = now;
            }
        // If farther than biting distance but within barking distance, create bark attack
        } else if (distanceSquared <  BARK_DISTANCE * BARK_DISTANCE) {
            SoundManager.getInstance().playPooledSound("dogBark");
            if (now - lastAttackTime > ATTACK_COOLDOWN) {
                createSlashAttack(gsm, pursued, null);
                lastAttackTime = now;
            }
        // If too far to attack, pursue
        } else {
            pursuePlayer(pursued);
        }
        

        // Sprite walk update
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            if (worldX > pursued.getWorldX()) {
                currSprite++;
                if (currSprite > 2) currSprite = 0;
            } else {
                currSprite++;
                if (currSprite < 3 || currSprite > 5) currSprite = 3;
            }
            lastSpriteUpdate = now;
        }
        matchHitBoxBounds();
    }
}
