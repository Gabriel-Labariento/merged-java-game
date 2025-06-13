import java.util.ArrayList;
import java.util.Iterator;

/**     
        The Player class is an abstract class that extends Entity and
        implements Effectable. It is extended by FastCat, HeavyCat, and
        GunCat. This class handles player movement, status effects,
        level progression, room transitions, and various player states.

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

public abstract class Player extends Entity implements Effectable{
    public static final int INVINCIBILITY_DURATION = 1000;
    public static final int REVIVAL_DURATION = 5000;
    public long invincibilityEnd;
    public long coolDownEnd;
    public int screenX;
    public int screenY;
    public boolean isDown;
    public boolean isReviving;
    public long revivalTime;
    public int currentXP;
    public int currentLvl;
    public int currentXPCap;
    public int pastXPCap;
    protected int baseSpeed;
    public Item heldItem;
    private final ArrayList<StatusEffect> statusEffects;
    private static final long BASE_STEP_SOUND_COOLDOWN = 300; // Base cooldown of 300ms
    private static final float MIN_SPEED_MULTIPLIER = 0.5f; // Slowest speed will double the cooldown
    private static final float MAX_SPEED_MULTIPLIER = 2.0f; // Fastest speed will halve the cooldown
    private long lastStepSoundTime = 0;
    protected boolean hasPlayedLevelUpSound = false;

    //  TUTORIAL VARIABLES
    protected int movementCount = 0;
    private static final int SIGNIFICANT_MOVEMENT = 50;

    protected int attackCount = 0;
    private static final int ATTACK_THRESHOLD = 2;

    /**
     * Each Player instance starts at level 1, holds no item, and has 
     * an empty statusEffects ArrayList
     */
    public Player(){
        currentLvl = 1;
        currentXPCap = 100;
        heldItem = null;
        statusEffects = new ArrayList<>();
    }

    /**
     * Applies experience points to the player and handles leveling logic.
     * @param xp
     */
    public void applyXP(int xp){
        currentXP += xp;
        hasPlayedLevelUpSound = false;
        while (currentXP >= currentXPCap) { 
            currentLvl++; 
            
            if (!hasPlayedLevelUpSound) {
                SoundManager.getInstance().playPooledSound("levelUp");
                hasPlayedLevelUpSound = true;
            }
            
            //Increase stats and heal
            levelUpStats();
            hitPoints = maxHealth;

            //Reset item effects
            if(heldItem != null){
                heldItem.removeEffects();
                heldItem.applyEffects();    
            }

            //Exponential function for scaling required experience points properly
            pastXPCap = currentXPCap;
            double exponentForScaling = 1.5;
            int baseXP = 100;
            currentXPCap = (int) Math.floor(baseXP * (Math.pow(currentLvl, exponentForScaling)));
        }
    }

    /**
     * Gets the value of the pastXPCap field
     * @return the int value of pastXPCap
     */
    public int getPastXPCap(){
        return pastXPCap;
    }

    /**
     * Gets the value of currentXPCap 
     * @return the int value of currentXPCap
     */
    public int getCurrentXPCap(){
        return currentXPCap;
    }

    /**
     * Sets the Player's held item to the passed Item argument
     * @param item the item to set heldItem to
     */
    public void setHeldItem(Item item){
        heldItem = item;
    }

    /**
     * Gets the Item stored in heldItem
     * @return the Item in heldItem
     */
    public Item getHeldItem(){
        return heldItem;
    }

    @Override
    public void setHitPoints(int hP){
        if(hP > maxHealth && !isMaxHealthSet) hP = maxHealth; 
        if (hitPoints < 18) hitPoints = hP;
    }

    @Override
    public void setMaxHealth(int hP){
        if (maxHealth < 18) maxHealth = hP;
    }
    
    /**
     * Adds 1 to hitpoints and maxHealth if it is below 18.
     * Adds 1 to damage as well.
     */
    public void levelUpStats(){
        if (hitPoints < 18 || maxHealth < 18){
            hitPoints += 1;
            maxHealth += 1;
        }
        damage += 1;
    }

    /**
     * Gets the value of the currentLvl field
     * @return the int value of currentLvl
     */
    public int getCurrentLvl(){
        return currentLvl;
    }

    /**
     * Calculates the percentage of progression towards the next level
     * @return an integer from 0 to 100 representing the progress in the current level
     */
    public int getXPBarPercent(){    
        return (int) Math.floor(100*((double)(currentXP-pastXPCap)/(currentXPCap-pastXPCap)));
    }

    /**
     * Sets the value of isDown to the passed boolean
     * @param b the boolean value to set isDown to
     */
    public void setIsDown(boolean b){
        isDown = b;
    }

    /**
     * Gets the value of the isDown field
     * @return true if isDown is true, false otherwise
     */
    public boolean getIsDown(){
        return isDown;
    }   

    /**
     * Sets the value of coolDownEnd for Player attacks based on the time of invocation
     * plus the value of attackCDDuration
     */
    public void triggerCoolDown(){
        coolDownEnd = System.currentTimeMillis() + attackCDDuration;
    }

    /**
     * Check's if the Player's attack is on cool down
     * @return true if the attack is on cooldown, false otherwise
     */
    public boolean getIsOnCoolDown(){
        return System.currentTimeMillis() < coolDownEnd;
    }

    /**
     * Sets the value of revival time to the time of invocation plus
     * the value of REVIVAL_DURATION
     */
    public void triggerRevival(){
       revivalTime = System.currentTimeMillis() + REVIVAL_DURATION;
    }

    /**
     *  Sets the value of isReviving to the passed boolean
     * @param b the boolean value to set isReviving to
     */
    public void setIsReviving(boolean b){
        isReviving = b;
    }

    /**
     * Gets the value of the isReviving field
     * @return true if the player is being revived, false otherwise
     */
    public boolean getIsReviving(){
        return isReviving;
    }

    /**
     * Checks if the Player has finished the revival process
     * @return
     */
    public boolean getIsRevived(){
        return System.currentTimeMillis() >= revivalTime;
    }

    /**
     * Updates the player's sprite and position based on the input
     * @param input the character 'Q' 'W' 'A' 'S' or 'D' corresponding to an update action
     */
    public void update(char input){
        prevWorldX = worldX;
        prevWorldY = worldY;

        int changeX = 0;
        int changeY = 0;

        switch (input){
            case 'Q':
                heldItem = null;
                break;
            case 'W':
                if (isMoveInbound(0, -1 * speed)) changeY -= speed;
                break;
            case 'A':
                if (isMoveInbound(-1 * speed, 0)) changeX -= speed;
                break;
            case 'S':
                if (isMoveInbound(0, speed)) changeY += speed;
                break;
            case 'D':
                if (isMoveInbound(speed, 0)) changeX += speed;
                break;
        }

        long now = System.currentTimeMillis();
        if (now - lastSpriteUpdate > SPRITE_FRAME_DURATION && !isAttacking) {
            if (changeX < 0) {
                currSprite++;
                if (currSprite > 2) currSprite = 0;
            } else {
                currSprite++;
                if (currSprite < 3 || currSprite > 5) currSprite = 3;
            }
            lastSpriteUpdate = now;
        }

        worldX += changeX;
        worldY += changeY;

        if (changeX != 0 || changeY != 0){
            playStepSound();
            movementCount++;
        }

        matchHitBoxBounds();
    }

    private void playStepSound(){
        long currentTime = System.currentTimeMillis();
        
        // Calculate cooldown based on current speed relative to base speed
        float speedMultiplier = (float) speed / baseSpeed;
        // Clamp the multiplier between MIN and MAX values
        speedMultiplier = Math.max(MIN_SPEED_MULTIPLIER, Math.min(MAX_SPEED_MULTIPLIER, speedMultiplier));
        // Invert the multiplier since we want faster speed = shorter cooldown
        float cooldownMultiplier = 1.0f / speedMultiplier;
        
        long currentCooldown = (long)(BASE_STEP_SOUND_COOLDOWN * cooldownMultiplier);
        
        if (currentTime - lastStepSoundTime < currentCooldown) {
            return;
        }

        switch (ServerMaster.getInstance().getGameLevel()) {
            case 2:
            case 4:
                // WOODEN STEP
                SoundManager.getInstance().playPooledSound("woodWalk");
                break;
            case 6:
                // WATER STEP
                SoundManager.getInstance().playPooledSound("waterWalk");
                break;
            default:
                // NORMAL STEP
                SoundManager.getInstance().playPooledSound("normalWalk");
                break;
        }
        lastStepSoundTime = currentTime;
    }

    /**
     * Sets the value of invincibilityEnd to the current time of invocation
     * plus the value of INVINCIBILITY_DURATION
     */
    public void triggerInvincibility(){
        invincibilityEnd = System.currentTimeMillis() + INVINCIBILITY_DURATION;
    }

    /**
     * Checks if the Player is currently invincible
     * @return true if the player is in an invincible state, false otherwise
     */
    public boolean getIsInvincible(){
        return System.currentTimeMillis() < invincibilityEnd;
    }

    /**
     * Builds a String storing room transition data in the form RC:clientId,newX,newY,hp,destinationRoomId
     * @param room the room to transition to
     */
    private String getRoomTransitionData(Door origin, Room next) {
        StringBuilder sb = new StringBuilder();
        
        int[] newCoors = getNewPositionAfterRoomTransition(origin, next);
        int newX = newCoors[0];
        int newY = newCoors[1];

        sb.append(NetworkProtocol.ROOM_CHANGE)
        .append(identifier).append(NetworkProtocol.SUB_DELIMITER)
        .append(clientId).append(NetworkProtocol.SUB_DELIMITER)
        .append(newX).append(NetworkProtocol.SUB_DELIMITER)
        .append(newY).append(NetworkProtocol.SUB_DELIMITER)
        .append(hitPoints).append(NetworkProtocol.SUB_DELIMITER)
        .append(next.getRoomId()).append(NetworkProtocol.SUB_DELIMITER)
        .append(currSprite).append(NetworkProtocol.SUB_DELIMITER)
        .append(getZIndex());

        return sb.toString();
    }

    /**
     * Gets the new position of the player in the new room after using a door.
     * @param origin the door in the previous room where the player came from
     * @param next the room the player is going to
     * @return an int array with the new position of the player in the next room, index 0 as the x position and index 1 as the y position
     */
    private int[] getNewPositionAfterRoomTransition(Door origin, Room next){
        int[] newCoordinates = new int[2];


        // Added null safety for next room
        if (next == null) {
            // System.out.println("Next room is null on getNewPositionAfterRoomTransition");
            newCoordinates[0] = (Room.WIDTH_TILES / 2) * GameCanvas.TILESIZE;
            newCoordinates[1] = (Room.HEIGHT_TILES / 2) * (GameCanvas.TILESIZE);
            return newCoordinates;
        }

        String otherDoorDirection = getOppositeDirection(origin.getDirection());

        Door otherDoor = null;

        for (Door d : next.getDoorsArrayList()) {
            if (d.getDirection().equals(otherDoorDirection)) otherDoor = d;    
        }

        if (otherDoor == null) {
            System.out.println("Could not find corresponding door in getNewPositionAfterRoomTransition()");
            return null;
        }

        int offset = height;

        switch (otherDoorDirection) {
            case "T":
                newCoordinates[0] = next.getWorldX() + (next.getWidth() / 2) - origin.getHeight();
                newCoordinates[1] = next.getWorldY() + origin.getHeight() + offset;
                break;
            case "B":
                newCoordinates[0] = next.getWorldX() + (next.getWidth() / 2) - origin.getWidth();
                newCoordinates[1] = next.getWorldY() + next.getHeight() - (origin.getHeight() + offset);
                break;
            case "L":
                newCoordinates[0] = next.getWorldX() + origin.getWidth() + offset;
                newCoordinates[1] = next.getWorldY() + (next.getHeight() / 2) - origin.getHeight();
                break;
            case "R":
                newCoordinates[0] = next.getWorldX() + next.getWidth() - (origin.getWidth() + offset);
                newCoordinates[1] = next.getWorldY() + (next.getHeight() / 2) - origin.getHeight();
                break;
            default:
                throw new AssertionError("Assertion in getNewPositionAfterRoomTransition");
        }

        return newCoordinates;
    }

    /**
     * Checks if a player is colliding with any door in the currentRoom and returns that door if the door is open
     * @return the door the player is colliding with
     */
    private Door getCollidingDoor(){
        for (Door d : currentRoom.getDoorsArrayList()) {
            if (isColliding(d) && (d.isOpen())) return d;
        }
        return null;
    }

    /**
     * {@inheritDoc }
     * @param isUserPlayer true if the calling player is the user player, false otherwise
     * @return a string in the possible forms: RC:clifntId,newX,newY,destinationRoomId or clientId,newX,newY,currentRoomId 
     */
    @Override
    public String getAssetData(boolean isUserPlayer){
        StringBuilder sb = new StringBuilder();

        Door d = getCollidingDoor();
        if ( isUserPlayer && d != null) { // Only send room transition data for the user player
            if (d.isExitToNewDungeon()) {
                ServerMaster.getInstance().triggerLevelTransition();
                return null;
            }
            else return getRoomTransitionData(d, d.getOtherRoom(currentRoom)); // return a different string upon room change
        } else {
            // String format: identifier, clientId,x,y,hp,roomId,currsprite,zindex
            sb.append(identifier).append(NetworkProtocol.SUB_DELIMITER)
            .append(clientId).append(NetworkProtocol.SUB_DELIMITER)
            .append(worldX).append(NetworkProtocol.SUB_DELIMITER)
            .append(worldY).append(NetworkProtocol.SUB_DELIMITER)
            .append(hitPoints).append(NetworkProtocol.SUB_DELIMITER)
            .append(currentRoom.getRoomId()).append(NetworkProtocol.SUB_DELIMITER)
            .append(currSprite).append(NetworkProtocol.SUB_DELIMITER)
            .append(getZIndex()).append(NetworkProtocol.DELIMITER);
        }

        return sb.toString();
    };
    
    @Override
    public void updateEntity(ServerMaster gsm) {
        //Do regen mechanics if player is holding a thick sweater
        if(heldItem instanceof ThickSweater ts && !isDown){
            ts.triggerRegenSystem();
        } 
        updateStatusEffects();
    }

    /**
     * Cycles through the attack sprites to simulate attack animation
     */
    public void runAttackFrames(){
        attackCount++;
        int frameCount = 0;
        while(frameCount < 4){
            long now = System.currentTimeMillis();
        
            if (now - lastSpriteUpdate > attackFrameDuration) {
                setIsAttacking(true);
                currSprite++;
                if (currSprite < 6) currSprite = 6;
                if (currSprite > 8) currSprite = 3;
                lastSpriteUpdate = now;
                frameCount++;
            }
        }
        setIsAttacking(false);
    }

    @Override
        public void updateStatusEffects(){
            if (statusEffects.isEmpty()) return;
            Iterator<StatusEffect> iter = statusEffects.iterator();
            
            while(iter.hasNext()){
                StatusEffect currEffect = iter.next();
                currEffect.tick(this);
                if (currEffect.isExpired()) {
                    currEffect.removeStatusEffect(this);
                    iter.remove();
                }
            }
        }

    @Override
    public void addStatusffect(StatusEffect se) {
        statusEffects.add(se);
    };

    /**
     * Gets the value of the baseSpeed field
     * @return the int value of baseSpeed
     */
    public int getBaseSpeed() {
        return baseSpeed;
    }

    /**
     * Gets the statusEffects ArrayList of the Player
     * @return an ArrayList statusEffects, containing all the StatusEffects currently affecting the Player
     */
    public ArrayList<StatusEffect> getStatusEffects() {
        return statusEffects;
    }

    public boolean hasMovedSignificantly(){
        return movementCount > SIGNIFICANT_MOVEMENT;
    }

    public boolean hasReachedAttackThreshold() {
        return attackCount > ATTACK_THRESHOLD;
    }
}
