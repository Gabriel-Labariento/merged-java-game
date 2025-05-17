import java.util.ArrayList;

public abstract class Enemy extends Entity {
    public ArrayList<Integer> attacksTakenById;
    public static int enemyCount = Integer.MIN_VALUE;
    public int rewardXP;
    public boolean isBoss;
    public long now;
    protected boolean isBuffed; 
    public Enemy(){
        attacksTakenById = new ArrayList<>();
        isBoss = false;
        isBuffed = false;
    }

    public int getRewardXP(){
        return rewardXP;
    }

    public boolean getIsBoss(){
        return isBoss;
    }

    public void setRewardXP(int i){
        rewardXP = i;
    }
    
    public void loadAttack(int id){
        attacksTakenById.add(id);
    }

    public boolean validateAttack(int id){
        return !attacksTakenById.contains(id);
    }

    public int getLastAttackID(){
        return attacksTakenById.get(attacksTakenById.size()-1);
    }

    
    // Right now, simple logic that scans if the distance between the player and the entity is <= scanRadius.
    // Pursues if yes. Does not yet consider obstacles.
    public Player scanForPlayer(ServerMaster gsm){
        final int scanRadius = GameCanvas.TILESIZE * 16;
        Player closestPlayer = null;
        double minDistance = Integer.MAX_VALUE; // Random large number

        for (Entity e : gsm.getEntities()) {
            if (e instanceof Player player) {
                   if (this.getCurrentRoom() != player.getCurrentRoom()) continue; 
                // Get the center distance between the player and the entity
                double distanceSquared = 
                    (Math.pow(getCenterX() - e.getCenterX(), 2) + Math.pow(getCenterY() - e.getCenterY(), 2));
                
                if ( (distanceSquared <= scanRadius * scanRadius) && (distanceSquared < minDistance)) {
                    closestPlayer = player;
                    minDistance = distanceSquared;
                }
            }
        }
        return closestPlayer;
    }

    public void pursuePlayer(Player player) {
       int newX = worldX;
        int newY = worldY;

        if (player.getCenterX() > getCenterX()) newX += speed;
        else if (player.getCenterX() < getCenterX()) newX -= speed;

        if (player.getCenterY() > getCenterY()) newY += speed;
        else if (player.getCenterY() < getCenterY()) newY -= speed;

        setPosition(newX, newY);
    }

    public void runFromPlayer(Player player){
        int newX = worldX;
        int newY = worldY;
        
        if (player.getCenterX() > getCenterX()) newX -= speed;
        else if (player.getCenterX() < getCenterX()) newX += speed;

        if (player.getCenterY() > getCenterY()) newY -= speed;
        else if (player.getCenterY() < getCenterY()) newY += speed;

        setPosition(newX, newY);
    }    

    public void createBiteAttack(ServerMaster gsm, Player target, StatusEffect effect){
        int vectorX = target.getCenterX() - getCenterX();
        int vectorY = target.getCenterY() - getCenterY(); 
        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

        //Avoids 0/0 division edge case
        if (normalizedVector == 0) normalizedVector = 1; 
        double normalizedX = vectorX / normalizedVector;
        double normalizedY = vectorY / normalizedVector;

        int biteDistance = GameCanvas.TILESIZE;
        int biteX = (int) (this.getCenterX() + normalizedX * biteDistance);
        int biteY = (int) (this.getCenterY() + normalizedY * biteDistance);
        biteX -= EnemyBite.WIDTH / 2;
        biteY -= EnemyBite.HEIGHT / 2;

        EnemyBite eb = new EnemyBite(this, biteX, biteY);
        if (effect != null) eb.addAttackEffect(effect);
        gsm.addEntity(eb);
    }

    public void createSlashAttack(ServerMaster gsm, Player target, StatusEffect effect){
        int vectorX = target.getCenterX() - getCenterX();
        int vectorY = target.getCenterY() - getCenterY(); 
        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

        //Avoids 0/0 division edge case
        if (normalizedVector == 0) normalizedVector = 1; 
        double normalizedX = vectorX / normalizedVector;
        double normalizedY = vectorY / normalizedVector;

        int spawnDistance = GameCanvas.TILESIZE * 3;
        int spawnX = (int) (this.getCenterX() + normalizedX * spawnDistance);
        int spawnY = (int) (this.getCenterY() + normalizedY * spawnDistance);
        spawnX -= EnemySlash.WIDTH / 2;
        spawnY -= EnemySlash.HEIGHT / 2;

        EnemySlash es = new EnemySlash(this, spawnX, spawnY);
        if (effect != null) es.addAttackEffect(effect);
        gsm.addEntity(es);
    }

    
    public void createRandomSlash(ServerMaster gsm, Player target){
        int vectorX = target.getCenterX() - getCenterX();
        int vectorY = target.getCenterY() - getCenterY(); 
        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

        //Avoids 0/0 division edge case
        if (normalizedVector == 0) normalizedVector = 1; 
        double normalizedX = vectorX / normalizedVector;
        double normalizedY = vectorY / normalizedVector;

        double randomDirX = (Math.random() * 2) * normalizedX;
        double randomDirY = (Math.random() * 2) * normalizedY;

        int spawnDistance = GameCanvas.TILESIZE * 2;
        int spawnX = ((int) (this.getCenterX() + randomDirX * spawnDistance)) - PlayerSlash.WIDTH / 2;
        int spawnY = ((int) (this.getCenterY() + randomDirY * spawnDistance)) - PlayerSlash.HEIGHT / 2;
        
        gsm.addEntity(new PlayerSlash(id, null, spawnX, spawnY, damage, false));
    }

    public void createRandomBullet(ServerMaster gsm, Player target){
        int vectorX = target.getCenterX() - getCenterX();
        int vectorY = target.getCenterY() - getCenterY(); 
        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

        if (normalizedVector == 0) normalizedVector = 1; 
        double normalizedX = vectorX / normalizedVector;
        double normalizedY = vectorY / normalizedVector;

        //Get random imprecise shot
        double randomizerX = (Math.random() * 2);
        double randomizerY = (Math.random() * 2);

        //Limit inaccuracy
        if (randomizerX < 0.25 || randomizerX > 1.75) randomizerX = 1;
        if (randomizerY < 0.25 || randomizerY > 1.75) randomizerY = 1;

        //Derive parameters
        double randomDirX = randomizerX * normalizedX;
        double randomDirY = randomizerY * normalizedY;
        int spawnX = getCenterX() - PlayerBullet.WIDTH/2;
        int spawnY = getCenterY() - PlayerBullet.HEIGHT/2;

        gsm.addEntity(new PlayerBullet(id, null, spawnX, spawnY, randomDirX, randomDirY, damage, false));
    }

    public void createRandomSmash(ServerMaster gsm, Player target){
        int vectorX = target.getCenterX() - getCenterX();
        int vectorY = target.getCenterY() - getCenterY(); 
        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

        //Avoids 0/0 division edge case
        if (normalizedVector == 0) normalizedVector = 1; 
        double normalizedX = vectorX / normalizedVector;
        double normalizedY = vectorY / normalizedVector;

        double randomDirX = (Math.random() * 2) * normalizedX;
        double randomDirY = (Math.random() * 2) * normalizedY;

        int spawnDistance = GameCanvas.TILESIZE * 5;
        int spawnX = ((int) (this.getCenterX() + randomDirX * spawnDistance)) - PlayerSlash.WIDTH / 2;
        int spawnY = ((int) (this.getCenterY() + randomDirY * spawnDistance)) - PlayerSlash.HEIGHT / 2;
        
        gsm.addEntity(new PlayerSmash(id, null, spawnX, spawnY, damage, false));
    }
    public void performSmashAttack(ServerMaster gsm){
        int smashX = worldX - EnemySmash.SMASH_RADIUS;
        int smashY = worldY - EnemySmash.SMASH_RADIUS;

        EnemySmash fs = new EnemySmash(this, smashX, smashY);
        fs.addAttackEffect(new SlowEffect());
        gsm.addEntity(fs);
    }

    public void initiateJump(Player target){
        int vectorX = target.getCenterX() - getCenterX();
        int vectorY = target.getCenterY() - getCenterY(); 
        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

        //Avoids 0/0 division edge case
        if (normalizedVector == 0) normalizedVector = 1; 
        double normalizedX = vectorX / normalizedVector;
        double normalizedY = vectorY / normalizedVector;

        int jumpDistance = GameCanvas.TILESIZE * 2;

        int newX = (int) (worldX + normalizedX * jumpDistance);
        int newY = (int) (worldY + normalizedY * jumpDistance);

        setPosition(newX, newY);
    }
    public void spawnMinions(){
        if (currentRoom != null && currentRoom.getMobSpawner() != null) {
        Enemy newSpawn = currentRoom.getMobSpawner().createNormalEnemy(currentRoom.getGameLevel());
        currentRoom.getMobSpawner().spawnEnemy(newSpawn);
        }
    }
    public void applyBuff(){
        if (!isBuffed){
            speed += 1;
            damage += 1;
        }
        isBuffed = true;
    }
}
