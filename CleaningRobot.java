import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class CleaningRobot extends Enemy{
    private static BufferedImage[] sprites;

    static {
        setSprites();
    }

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
        if (distanceSquared <= AGGRO_DISTANCE) {
            if (now - lastAttackTime > attackCDDuration) {
                activateDefense(gsm);
                lastAttackTime = now;
            }
        } else {
            runFromPlayer(pursued);
        }
        

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

