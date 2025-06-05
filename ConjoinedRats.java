import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The ConjoinedRats class extends Enemy. It is one 
        of the bosses in the game. It appears in level six.
        This boss summons minions to fight for it and pursues
        the player at a slower pace when limping.

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

public class ConjoinedRats extends Enemy{
    private static BufferedImage[] sprites;
    private long lastLimpTime;
    private static final int LIMP_CD_DURATION = 200;
    private static final int NOISE_COOLDOWN = 4000;
    private long lastNoiseTime = 0;

    /**
     * Calls the static setSprites() method
     */
    static {
        setSprites();
    }

    /**
     * Creates ConjoinedRats instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public ConjoinedRats(int x, int y) {
        isBoss = true;
        lastSpriteUpdate = 0;
        lastAttackTime = 0;
        id = enemyCount++;
        identifier = NetworkProtocol.CONJOINEDRATS;
        speed = 1;
        height = 120;
        width = 120;
        worldX = x;
        worldY = y;
        maxHealth = 600;
        hitPoints = maxHealth;
        damage = 3;
        rewardXP = 50;
        currentRoom = null;
        currSprite = 0;
        attackCDDuration = 1200;
        
    }

    /**
     * Sets the sprite images to the class and not individual images
     */
    private static void setSprites() {
        try {
            BufferedImage walk0 = ImageIO.read(ConjoinedRats.class.getResourceAsStream("resources/Sprites/ConjoinedRats/walk0.png"));
            BufferedImage walk1 = ImageIO.read(ConjoinedRats.class.getResourceAsStream("resources/Sprites/ConjoinedRats/walk1.png"));
            BufferedImage walk2 = ImageIO.read(ConjoinedRats.class.getResourceAsStream("resources/Sprites/ConjoinedRats/walk2.png"));
            sprites = new BufferedImage[] {walk0, walk1, walk2};

        } catch (IOException e) {
            System.out.println("Exception in ConjoinedRat setSprites()" + e);
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
        final double ACTION_DISTANCE = (GameCanvas.TILESIZE * 10) * (GameCanvas.TILESIZE * 10);

         // Sprite update
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            currSprite++;
            if (currSprite > 2) currSprite = 0;
            lastSpriteUpdate = now;
        }

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        
        double distanceSquared = getSquaredDistanceBetween(this, pursued);

        // Slow the boss beyond lowest speed
        if (now - lastLimpTime > LIMP_CD_DURATION){
            pursuePlayer(pursued);
            lastLimpTime = now;
        }

        // Summon minions if within action distance and attack has cooled down
        if (distanceSquared <= ACTION_DISTANCE && now - lastAttackTime > attackCDDuration) {
            summonMinion(gsm);
            lastAttackTime = now;
        } 
        
        // Update hitbox
        matchHitBoxBounds();

        if (now - lastNoiseTime > NOISE_COOLDOWN){
            SoundManager.getInstance().playPooledSound("ratMonsterNoise");
            lastNoiseTime = now;
        }
    }

    /**
     * Summons a random minion rat: Rat, FeralRat, or ScreamerRat
     * @param gsm ServerMaster instance containing the entities arraylist to which the minions are added
     */
    private void summonMinion(ServerMaster gsm){
        int centerX = getCenterX();
        int centerY = getCenterY();

        //Roll a random summon
        double summonRoll = Math.random();
        Enemy summon;
        if (summonRoll < 0.75) summon = new Rat(centerX, centerY);
        else if (summonRoll < 0.95) summon = new FeralRat(centerX, centerY);
        else summon = new ScreamerRat(centerX, centerY);

        //Make sure players cant farm
        summon.setRewardXP(0);
        gsm.addEntity(summon);
    }

    /**
     * Upon death, summons 10 minions.
     * @param gsm ServerMaster instance containing the entities arraylist to which the minions are added
     */
    public void handleDeath(ServerMaster gsm){
        for (int i = 0; i < 10; i++) summonMinion(gsm);
    }
}