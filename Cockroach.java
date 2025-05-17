import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Cockroach extends Enemy{
    private static final int IDLE_DURATION = 400;
    private static final int ATTACK_DURATION = 300;
    private long lastStateChangeTime = 0;
    private static final int ATTACK_RANGE = GameCanvas.TILESIZE * 2;
    private static BufferedImage[] sprites;
    private enum State {IDLE, PURSUE, ATTACK}; 
    private State currentState;
    
    static {
        setSprites();
    }

    public Cockroach(int x, int y) {
        id = enemyCount++;
        identifier = NetworkProtocol.COCKROACH;
        speed = 1;
        height = 16;
        width = 16;
        worldX = x;
        worldY = y;
        maxHealth = 10;
        hitPoints = maxHealth;
        damage = 1;
        rewardXP = 50;
        currentRoom = null;
        currSprite = 0;
        currentState = State.IDLE;
    }

     private static void setSprites() {
        try {
            BufferedImage up0 = ImageIO.read(Cockroach.class.getResourceAsStream("resources/Sprites/Cockroach/cockroach_up0.png"));
            BufferedImage up1 = ImageIO.read(Cockroach.class.getResourceAsStream("resources/Sprites/Cockroach/cockroach_up1.png"));
            BufferedImage down0 = ImageIO.read(Cockroach.class.getResourceAsStream("resources/Sprites/Cockroach/cockroach_down0.png"));
            BufferedImage down1 = ImageIO.read(Cockroach.class.getResourceAsStream("resources/Sprites/Cockroach/cockroach_down1.png"));

            sprites = new BufferedImage[] {up0, up1, down0, down1};

        } catch (IOException e) {
            System.out.println("Exception in Cockroach setSprites()" + e);
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

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;

        // Sprite walk update
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION) {
            if (worldY > pursued.getWorldY()) {
                currSprite = (currSprite == 1) ? 0 : 1;
            } else if (worldY < pursued.getWorldY()) {
                currSprite = (currSprite == 2) ? 3 : 2;
            }
            lastSpriteUpdate = now;
        }

        double distanceSquared = getSquaredDistanceBetween(this, pursued);

        switch (currentState) {
            case IDLE:
                if (now - lastStateChangeTime > IDLE_DURATION) {
                    currentState = State.PURSUE;
                    lastStateChangeTime = now;
                }
                break;

            case PURSUE:
                pursuePlayer(pursued);
                if (distanceSquared <= ATTACK_RANGE * ATTACK_RANGE) {
                    currentState = State.ATTACK;
                    lastStateChangeTime = now;
                }
                break;

            case ATTACK:
                if (now - lastStateChangeTime > ATTACK_DURATION) { 
                    initiateJump(pursued);
                    currentState = State.IDLE;
                    lastStateChangeTime = now;
                } 
                break;

            default:
                throw new AssertionError();
        }

        matchHitBoxBounds();
    }
}
