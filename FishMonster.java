import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class FishMonster extends Enemy{
    private static BufferedImage[] sprites;
    private enum State {PHASE1, PHASE2, PHASE3}; 
    private int summonCDDuration;
    public State currentForm;
    private double actionDistance;
    private long lastLimpTime;
    private long lastSummonTime;
    private static final int LIMP_CD_DURATION = 50;

    static {
        setSprites();
    }

    public FishMonster(int x, int y) {
        isBoss = true;
        now = System.currentTimeMillis();
        lastSpriteUpdate = now;
        lastAttackTime = now;
        lastSummonTime = now;
        id = enemyCount++;
        identifier = NetworkProtocol.FISHMONSTER;
        speed = 1;
        height = 150;
        width = 150;
        worldX = x;
        worldY = y;
        maxHealth = 700;
        hitPoints = maxHealth;
        damage = 2;
        rewardXP = 50;
        currentRoom = null;
        currSprite = 0;
        currentForm = State.PHASE1;

        matchHitBoxBounds();
        
    }

     private static void setSprites() {
        try {
            BufferedImage phaseA0 = ImageIO.read(FishMonster.class.getResourceAsStream("resources/Sprites/FishMonster/phaseA0.png"));
            BufferedImage phaseA1 = ImageIO.read(FishMonster.class.getResourceAsStream("resources/Sprites/FishMonster/phaseA1.png"));
            BufferedImage phaseA2 = ImageIO.read(FishMonster.class.getResourceAsStream("resources/Sprites/FishMonster/phaseA2.png"));
            BufferedImage phaseB0 = ImageIO.read(FishMonster.class.getResourceAsStream("resources/Sprites/FishMonster/phaseB0.png"));
            BufferedImage phaseB1 = ImageIO.read(FishMonster.class.getResourceAsStream("resources/Sprites/FishMonster/phaseB1.png"));
            BufferedImage phaseB2 = ImageIO.read(FishMonster.class.getResourceAsStream("resources/Sprites/FishMonster/phaseB2.png"));
            BufferedImage phaseC0 = ImageIO.read(FishMonster.class.getResourceAsStream("resources/Sprites/FishMonster/phaseC0.png"));
            BufferedImage phaseC1 = ImageIO.read(FishMonster.class.getResourceAsStream("resources/Sprites/FishMonster/phaseC1.png"));
            BufferedImage phaseC2 = ImageIO.read(FishMonster.class.getResourceAsStream("resources/Sprites/FishMonster/phaseC2.png"));
            sprites = new BufferedImage[] {phaseA0, phaseA1, phaseA2, phaseB0, phaseB1, phaseB2, phaseC0, phaseC1, phaseC2};

        } catch (IOException e) {
            System.out.println("Exception in Rat setSprites()" + e);
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
        
        updateFrame();
        Player pursued = scanForPlayer(gsm);
        if (pursued == null) return;
        double distanceSquared = getSquaredDistanceBetween(this, pursued);
        
        switch (currentForm) {
            case PHASE1:
                //Slow the boss beyond lowest speed
                if (now - lastLimpTime > LIMP_CD_DURATION){
                    pursuePlayer(pursued);
                    lastLimpTime = now;
                }

                //Configure stats
                actionDistance = (GameCanvas.TILESIZE*8) * (GameCanvas.TILESIZE*8);
                damage = 3;
                summonCDDuration = 6000;

                if (now - lastSummonTime > summonCDDuration){
                    summonMinion(gsm);
                    lastSummonTime = now;
                }

                // if (now - lastAttackTime > attackCDDuration && distanceSquared <= actionDistance){
                //     createBiteAttack(gsm, pursued, null);
                //     lastAttackTime = now;
                // }

                //Set triger for phase 2
                if(hitPoints <= (maxHealth*0.5)) currentForm = State.PHASE2;
                break;
            case PHASE2:
                damage = 1;
                summonCDDuration = 12000;
                speed = 1;
                
                pursuePlayer(pursued);
                if (now - lastSummonTime > summonCDDuration){
                    summonMinion(gsm);
                    lastSummonTime = now;
                }
                break;
            case PHASE3:
                attackCDDuration = 400;
                actionDistance = (GameCanvas.TILESIZE*16) * (GameCanvas.TILESIZE*16);
                   
                if (now - lastAttackTime > attackCDDuration && distanceSquared <= actionDistance){
                    double attackRoll = Math.random();
                    lastAttackTime = now;

                    if (attackRoll > 0.3) createRandomBullet(gsm, pursued);
                    else createBulletCircle(gsm);
                }
                    
                break;
     
            
            default:
                throw new AssertionError();
        }

        matchHitBoxBounds();
    }

    public void triggerPhase3(){
        currentForm = State.PHASE3;
        hitPoints = maxHealth;
    }

    public boolean isPhase3(){
        return (currentForm == State.PHASE3);
    }

    private void updateFrame(){
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION){
            currSprite++;
            lastSpriteUpdate = now;
            switch (currentForm) {
                case PHASE1:
                    if (currSprite > 2) currSprite = 0;
                    break;
                case PHASE2:
                    if (currSprite > 5 || currSprite < 3) currSprite = 3;
                    break;
                case PHASE3:
                    if (currSprite > 8 || currSprite < 6) currSprite = 6;
                    break;
            }
        }
    }

    private void summonMinion(ServerMaster gsm){
        int centerX = getCenterX();
        int centerY = getCenterY();

        //Roll a random summon
        double summonRoll = Math.random();
        Enemy summon;
        if (summonRoll < 0.33) summon = new MutatedAnchovy(centerX, centerY);
        else if (summonRoll < 0.67) summon = new MutatedArcherfish(centerX, centerY);
        else summon = new MutatedPufferfish(centerX, centerY);

        //Make sure players cant farm
        summon.setRewardXP(0);
        gsm.addEntity(summon);
    }

    private void createBulletCircle(ServerMaster gsm){
        double radius = GameCanvas.TILESIZE;

        int centerX = getCenterX();
        int centerY = getCenterY();
        
        // Create 8 bullets around robot in a circle
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            
            // Calculate normalized direction vector
            double dirX = Math.cos(angle);
            double dirY = Math.sin(angle);
            
            // Calculate the spawn point on circle
            int spawnX = (int) (Math.round((centerX + (dirX * radius)) -  PlayerBullet.WIDTH / 2.0));
            int spawnY = (int) (Math.round((centerY + (dirY * radius)) - PlayerBullet.HEIGHT / 2.0));
            
            PlayerBullet bullet = new PlayerBullet(id, null, spawnX, spawnY, dirX, dirY, damage, false);
            bullet.setSpeed(3);
            gsm.addEntity(bullet);

        }
    }
    
}