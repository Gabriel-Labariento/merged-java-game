import java.util.ArrayList;

/**     
        The Enemy class extends Entity. It is then extended by classes
        that are adversarial, those who attack the player. Listed in 
        this class are shared methods that enable enemy behavior:
        pursuing, creating shared attacks, scanning for, and running
        away from Player.

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

public abstract class Enemy extends Entity {

    public ArrayList<Integer> attacksTakenById;
    public static int enemyCount = Integer.MIN_VALUE;
    public int rewardXP;
    public boolean isBoss;
    public long now;
    protected boolean isBuffed;
    
    /**
     * All Enemy instances have default values: an empty 
     * attacksTakenById ArrayList to prevent unintended
     * attack stacks, isBoss is set to false, and isBuffed
     * is set to false
     */
    public Enemy(){
        attacksTakenById = new ArrayList<>();
        isBoss = false;
        isBuffed = false;
    }

    /**
     * Gets the value of the rewardXP field
     * @return the rewardXP field value
     */
    public int getRewardXP(){
        return rewardXP;
    }

    /**
     * Gets the value of the isBoss field
     * @return boolean true if the Enemy is a boss, false otherwise
     */
    public boolean getIsBoss(){
        return isBoss;
    }

    /**
     * Sets the rewardXP field value to the provided argument
     * @param i the value to set rewardXP to
     */
    public void setRewardXP(int i){
        rewardXP = i;
    }
    
    /**
     * Adds the id of an attack to the attacksTakenById ArrayList
     * @param id the attack Id to add
     */
    public void loadAttack(int id){
        attacksTakenById.add(id);
    }

    /**
     * Checks if the attack Id is in the attacksTakenById ArrayList
     * @param id the attack Id
     * @return boolean true if the attack is not in the attacksTakenById ArrayList, false if it is
     */
    public boolean validateAttack(int id){
        return !attacksTakenById.contains(id);
    }

    /**
     * Gets the Id of the last attack added to the attacksTakenById ArrayList
     * @return the Id of the last attack added to the attacksTakenById ArrayList
     */
    public int getLastAttackID(){
        return attacksTakenById.get(attacksTakenById.size()-1);
    }

    
    /**
     * Looks through the entities ArrayList from ServerMaster to see if
     * there are any players within a certain radius to it. If there are multiple,
     * return the closest.
     * @param gsm the ServerMaster instance containing the ArrayList of entities
     * @return the closestPlayer to the enemy within the scanRadius
     */
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

    /**
     * Moves the Enemy towards the Player
     * @param player the Player to move towards
     */
    public void pursuePlayer(Player player) {
       int newX = worldX;
        int newY = worldY;

        if (player.getCenterX() > getCenterX()) newX += speed;
        else if (player.getCenterX() < getCenterX()) newX -= speed;

        if (player.getCenterY() > getCenterY()) newY += speed;
        else if (player.getCenterY() < getCenterY()) newY -= speed;

        setPosition(newX, newY);
    }

    /**
     * Performs the opposite of pursuePlayer(). Moves away from the Player argument
     * @param player the player to move away from
     */
    public void runFromPlayer(Player player){
        int newX = worldX;
        int newY = worldY;
        
        if (player.getCenterX() > getCenterX()) newX -= speed;
        else if (player.getCenterX() < getCenterX()) newX += speed;

        if (player.getCenterY() > getCenterY()) newY -= speed;
        else if (player.getCenterY() < getCenterY()) newY += speed;

        setPosition(newX, newY);
    }    

    /**
     * Creates a bite (small) attack towards the target Player with the given status effect
     * @param gsm the ServerMaster instance containing the ArrayList of entities
     * @param target the target of the attack
     * @param effect the status effect to be added to the attack
     */
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

    /**
     * Creates a slash (wide) attack towards the target Player with the given status effect
     * @param gsm the ServerMaster instance containing the ArrayList of entities
     * @param target the target of the attack
     * @param effect the status effect to be added to the attack
     */
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

    /**
     * Creates a slash (wide) attack towards the target player but at a random offset
     * @param gsm the ServerMaster instance containing the entities ArrayList
     * @param target the Player target of the attack
     */
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

    /**
     * Creates a random bullet headed towards the target player but with a random offset
     * @param gsm the ServerMaster instance containing the entities ArrayList
     * @param target the Player target of the bullet attack
     */
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

    /**
     * Creates a smash (ultra wide) attack towards the player but with a random offset
     * @param gsm the ServerMaster instance containing the entities ArrayList
     * @param target the Player target of the attack
     */
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

    /**
     * Creates a smash (ultra wide) attack around the calling Enemy
     * @param gsm the ServerMaster instance containing the entities ArrayList
     */
    public void performSmashAttack(ServerMaster gsm){
        int smashX = worldX - EnemySmash.SMASH_RADIUS;
        int smashY = worldY - EnemySmash.SMASH_RADIUS;

        EnemySmash fs = new EnemySmash(this, smashX, smashY);
        fs.addAttackEffect(new SlowEffect());
        gsm.addEntity(fs);
    }

    /**
     * Causes the calling Enemy to move suddenly, simulating a jump
     * towards the target Player
     * @param target the Player to jump towards
     */
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

    /**
     * Allows the calling Enemy to spawn more enemies in the currentRoom
     */
    public void spawnMinions(){
        if (currentRoom != null && currentRoom.getMobSpawner() != null) {
        Enemy newSpawn = currentRoom.getMobSpawner().createNormalEnemy(currentRoom.getGameLevel());
        currentRoom.getMobSpawner().spawnEnemy(newSpawn);
        }
    }

    /**
     * Increases the speed and damage of the calling Entity by 1.
     * The buff can only be applied once.
     */
    public void applyBuff(){
        if (!isBuffed){
            speed += 1;
            damage += 1;
        }
        isBuffed = true;
    }
}
