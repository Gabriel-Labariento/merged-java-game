import java.util.ArrayList;
import java.util.Iterator;
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

    public Player(){
        currentLvl = 1;
        currentXPCap = 100;
        heldItem = null;
        statusEffects = new ArrayList<>();
    }

    public void applyXP(int xp){
        currentXP += xp;

        while (currentXP >= currentXPCap) { 
            currentLvl++; 
            System.out.println("New level: " + currentLvl);


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

    public int getPastXPCap(){
        return pastXPCap;
    }

    public int getCurrentXPCap(){
        return currentXPCap;
    }

    public void setHeldItem(Item item){
        heldItem = item;
    }

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
    
    public void levelUpStats(){
        if (hitPoints < 18 || maxHealth < 18){
            hitPoints += 1;
            maxHealth += 1;
        }
        damage += 1;
    }

    public int getCurrentLvl(){
        return currentLvl;
    }

    public int getXPBarPercent(){    
        return (int) Math.floor(100*((double)(currentXP-pastXPCap)/(currentXPCap-pastXPCap)));
    }

    public void setIsDown(boolean b){
        isDown = b;
    }


    public boolean getIsDown(){
        return isDown;
    }

    public void triggerCoolDown(){
        coolDownEnd = System.currentTimeMillis() + attackCDDuration;
    }

    public boolean getIsOnCoolDown(){
        return System.currentTimeMillis() < coolDownEnd;
    }

    public void triggerRevival(){
       revivalTime = System.currentTimeMillis() + REVIVAL_DURATION;  
    }

    public void setIsReviving(boolean b){
        isReviving = b;
    }

    public boolean getIsReviving(){
        return isReviving;
    }

    public boolean getIsRevived(){
        return System.currentTimeMillis() >= revivalTime;
    }

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
        matchHitBoxBounds();
    }

    /**
     * 
     */
    public void triggerInvincibility(){
        invincibilityEnd = System.currentTimeMillis() + INVINCIBILITY_DURATION;
    }

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

    public int[] getScreenPos() {
        int[] screenPos = {screenX, screenY};
        return screenPos;
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

    public void runAttackFrames(){
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

    public int getBaseSpeed() {
        return baseSpeed;
    }

    public ArrayList<StatusEffect> getStatusEffects() {
        return statusEffects;
    }
}
