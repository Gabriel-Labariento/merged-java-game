import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The ScreamerRat class extends Enemy. It appears in level 6 of the game.
        Although not a boss, it has the ability to spawn other Rat instances like
        Rat and FeralRat.

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

public class ScreamerRat extends Enemy{
    private static final int SCREAM_COOLDOWN = 3000;
    private long lastScreamTime = 0;
    private static BufferedImage[] sprites;
    public static final int MAXSUMMONCOUNT = 4;

    // Call the static setSprites() method
    static {
        setSprites();
    }

    /**
     * Creates a ScreamerRat instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public ScreamerRat(int x, int y) {
        lastSpriteUpdate = 0;
        lastAttackTime = 0;
        id = enemyCount++;
        identifier = NetworkProtocol.SCREAMERRAT;
        speed = 3;
        height = 32;
        width = 32;
        worldX = x;
        worldY = y;
        maxHealth = 10;
        hitPoints = maxHealth;
        damage = 1;
        rewardXP = 50;
        currentRoom = null;
        currSprite = 1;
        attackCDDuration = 10000;
        
    }

    /**
     * Sets the sprite images to the class and not the instances
     */
    private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(ScreamerRat.class.getResourceAsStream("resources/Sprites/ScreamerRat/screaming.png"));
            BufferedImage right0 = ImageIO.read(ScreamerRat.class.getResourceAsStream("resources/Sprites/ScreamerRat/hidden.png"));
            sprites = new BufferedImage[] {left0, right0};

        } catch (IOException e) {
            System.out.println("Exception in ScreamerRat setSprites()" + e);
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
        final double ACTION_DISTANCE = (GameCanvas.TILESIZE * 5) * (GameCanvas.TILESIZE * 5);

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        double distanceSquared = getSquaredDistanceBetween(this, pursued);
        if (distanceSquared <= ACTION_DISTANCE) {
            if (now - lastAttackTime > attackCDDuration) {
                if (now - lastScreamTime > SCREAM_COOLDOWN) {
                    SoundManager.getInstance().playPooledSound("screamerRat");
                    lastScreamTime = now;
                }
                screamSummon(gsm);
                lastAttackTime = now;
            }
            currSprite = 0;
        } else {
            currSprite = 1;
        }
    
        matchHitBoxBounds();
    }

    /**
     * Creates a Rat or FeralRat enemy summon with no experience points allocation
     * @param gsm the ServerMaster instance containing the entities ArrayList where summons will be added.
     */
    private void screamSummon(ServerMaster gsm){
        int summonedRatCount = (int) (Math.random() * MAXSUMMONCOUNT);
        int centerX = getCenterX();
        int centerY = getCenterY();

        for (int i = 0; i< summonedRatCount; i++){
            double summonRoll = Math.random();
            Enemy summon;
            if (summonRoll <= 0.6) summon = new FeralRat(centerX, centerY);
            else summon = new Rat(centerX, centerY);

            summon.setRewardXP(0);
            gsm.addEntity(summon);
            
        }
        
    }
}