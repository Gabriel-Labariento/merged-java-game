import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ScreamerRat extends Enemy{
    private static BufferedImage[] sprites;
    public static final int MAXSUMMONCOUNT = 4;

    static {
        setSprites();
    }

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

     private static void setSprites() {
        try {
            BufferedImage left0 = ImageIO.read(ScreamerRat.class.getResourceAsStream("resources/Sprites/ScreamerRat/screaming.png"));
            BufferedImage right0 = ImageIO.read(ScreamerRat.class.getResourceAsStream("resources/Sprites/ScreamerRat/hidden.png"));
            sprites = new BufferedImage[] {left0, right0};

        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
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
                screamSummon(gsm);
                lastAttackTime = now;
            }
            currSprite = 0;
        } else {
            currSprite = 1;
        }
    
        matchHitBoxBounds();
    }

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