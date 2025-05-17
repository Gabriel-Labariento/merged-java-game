import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Frog extends Enemy{
    private static final int ATTACK_RANGE = GameCanvas.TILESIZE * 3;
    private static final int IDLE_DURATION = 400;
    private static final int ATTACK_DURATION = 300;
    private boolean smashPerformed;
    private long lastStateChangeTime = 0;
    private static BufferedImage[] sprites;
    private enum State {IDLE, PURSUE, ATTACK, SMASH}; 
    private State currentState;
    
    static {
        setSprites();
    }

    public Frog(int x, int y) {
        id = enemyCount++;
        identifier = NetworkProtocol.FROG;
        speed = 5;
        height = 16;
        width = 16;
        worldX = x;
        worldY = y;
        maxHealth = 20;
        hitPoints = maxHealth;
        damage = 1;
        rewardXP = 50;
        currentRoom = null;
        currSprite = 0;
        currentState = State.IDLE;
        smashPerformed = false;
    }

     private static void setSprites() {
        try {
            BufferedImage down0 = ImageIO.read(Frog.class.getResourceAsStream("resources/Sprites/Frog/frog_down0.png"));
            BufferedImage down1 = ImageIO.read(Frog.class.getResourceAsStream("resources/Sprites/Frog/frog_down1.png"));

            sprites = new BufferedImage[] {down0, down1};

        } catch (IOException e) {
            System.out.println("Exception in Frog setSprites()" + e);
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
        long now = System.currentTimeMillis();

        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;

        double distanceSquared = getSquaredDistanceBetween(this, pursued);

        switch (currentState) {
            case IDLE:
                if (now - lastStateChangeTime > IDLE_DURATION) {
                    currSprite = 0;
                    currentState = (distanceSquared <= ATTACK_RANGE * ATTACK_RANGE) ? State.ATTACK : State.PURSUE;
                    lastStateChangeTime = now;
                }
                break;

            case PURSUE:
                currSprite = 1;
                initiateJump(pursued);
                currentState = State.IDLE;
                lastStateChangeTime = now;
                break;

            case ATTACK:
                if (!smashPerformed) {
                    currentState = State.SMASH;
                    smashPerformed = true;
                    lastStateChangeTime = now;
                    performSmashAttack(gsm);
                }
                if (now - lastStateChangeTime > ATTACK_DURATION) { 
                    initiateJump(pursued);
                    currentState = State.IDLE;
                    lastStateChangeTime = now;
                    smashPerformed = false;
                } 
                break;

            case SMASH:
                if (now - lastStateChangeTime > ATTACK_DURATION) {
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
