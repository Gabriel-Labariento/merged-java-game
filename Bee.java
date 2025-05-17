import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Bee extends Enemy{
    private static final int DASH_COOLDOWN = 1000;
    private static final int DASH_DISTANCE = GameCanvas.TILESIZE * 4;
    private long lastDashTime = 0;
    private static BufferedImage[] sprites;
    private enum State {IDLE, PURSUE, DASH};
    private State currentState;

    static {
        setSprites();
    }

    public Bee(int x, int y) {
        id = enemyCount++;
        identifier = NetworkProtocol.BEE;
        speed = 1;
        height = 16;
        width = 20;
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
            BufferedImage left0 = ImageIO.read(Bee.class.getResourceAsStream("resources/Sprites/Bee/bee_left0.png"));
            BufferedImage left1 = ImageIO.read(Bee.class.getResourceAsStream("resources/Sprites/Bee/bee_left1.png"));
            BufferedImage leftBlurred = ImageIO.read(Bee.class.getResourceAsStream("resources/Sprites/Bee/bee_left_blur.png"));
            BufferedImage right0 = ImageIO.read(Bee.class.getResourceAsStream("resources/Sprites/Bee/bee_right0.png"));
            BufferedImage right1 = ImageIO.read(Bee.class.getResourceAsStream("resources/Sprites/Bee/bee_right1.png"));
            BufferedImage rightBlurred = ImageIO.read(Bee.class.getResourceAsStream("resources/Sprites/Bee/bee_right_blur.png"));
            sprites = new BufferedImage[] {left0, left1, leftBlurred, right0, right1, rightBlurred};

        } catch (IOException e) {
            System.out.println("Exception in Bee setSprites()" + e);
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
            if (worldX < pursued.getWorldX()) {
                currSprite = (currSprite == 3) ? 4 : 3;
            } else {
                currSprite = (currSprite == 0) ? 1 : 0;
            }
            lastSpriteUpdate = now;
        }

        switch (currentState) {
            case IDLE:
                speed = 1;
                runFromPlayer(pursued);
                if (now - lastDashTime > DASH_COOLDOWN){
                    currentState = State.DASH;
                    lastDashTime = now;
                } else currentState = State.PURSUE;
                break;

            case PURSUE:
                if (getSquaredDistanceBetween(this, pursued) <= DASH_DISTANCE * DASH_DISTANCE) {
                    currentState = State.IDLE;
                } else pursuePlayer(pursued);
                break;

            case DASH:
                speed = DASH_DISTANCE;
                currSprite = (pursued.getWorldX() > worldX) ? 5 : 2;
                pursuePlayer(pursued);
                lastDashTime = now;
                currentState = State.PURSUE;
                break;

            default:
                throw new AssertionError();
        }
        matchHitBoxBounds();
    }


}
