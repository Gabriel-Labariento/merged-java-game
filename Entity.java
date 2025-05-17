import java.awt.Graphics2D;

public abstract class Entity extends GameObject {
    protected int id;
    protected String identifier;
    protected int prevWorldX;
    protected int prevWorldY;
    protected int speed;
    protected int maxHealth;
    protected int hitPoints;
    protected int defense;
    protected int clientId;
    public boolean isMaxHealthSet;
    protected int currSprite;
    protected int damage;
    protected boolean isAttacking;
    protected int attackFrameDuration;
    public Room currentRoom;
    protected static final int SPRITE_FRAME_DURATION = 200;
    protected long lastSpriteUpdate;
    protected long lastAttackTime;
    protected int attackCDDuration;

    public void draw(Graphics2D g2d, int xOffset, int yOffset){}

    public abstract void updateEntity(ServerMaster gsm);
    
    public int getClientId(){
        return clientId;
    }

    public String getIdentifier(){
        return identifier;
    }

    public int getDefense(){
        return defense;
    }

    public void setDefense(int d){
        defense = d;
    }

    public int getAttackCDDuration(){
        return attackCDDuration;
    }

    public void setAttackCDDuration(int duration){
        attackCDDuration = duration;
    }

    public int getAttackFrameDuration(){
        return attackFrameDuration;
    }

    public void setAttackFrameDuration(int a){
        attackFrameDuration = a;
    }

    public boolean getIsAttacking(){
        return isAttacking;
    }

    public void setIsAttacking(boolean b){
        isAttacking = b;
    }
    
    public String getAssetData(boolean isUserPlayer){
        StringBuilder sb = new StringBuilder();
        sb.append(identifier).append(NetworkProtocol.SUB_DELIMITER)
        .append(id).append(NetworkProtocol.SUB_DELIMITER)
        .append(worldX).append(NetworkProtocol.SUB_DELIMITER)
        .append(worldY).append(NetworkProtocol.SUB_DELIMITER)
        .append(currentRoom.getRoomId()).append(NetworkProtocol.SUB_DELIMITER)
        .append(currSprite).append(NetworkProtocol.SUB_DELIMITER)
        .append(getZIndex()).append(NetworkProtocol.DELIMITER);
        
        return sb.toString();
    };

    /**
     * Checks if the move an entity will make will keep them inside the room they are currently in
     * @param dx the change in x.
     * @param dy the change in y
     * @return a boolean that is true when the move is inbound, false when not.
     */
    public boolean isMoveInbound(int dx, int dy) {
        return !((worldX + dx < currentRoom.getWorldX()) ||
                ( (worldX + width) + dx > currentRoom.getWorldX() + currentRoom.getWidth()) ||
                (worldY + dy < currentRoom.getWorldY()) ||
                ((worldY + height) + dy > currentRoom.getWorldY() + currentRoom.getHeight())
             );
    }

    public void setPosition(int x, int y) {
        int[] roomBounds = currentRoom.getHitBoxBounds();

        if (y < roomBounds[0]) y = roomBounds[0];           // Top Boundary
        if (y + height > roomBounds[1]) y = roomBounds[1] - height;  // Bottom Boundary
        if (x < roomBounds[2]) x = roomBounds[2];           // Left Boundary
        if (x + width > roomBounds[3]) x = roomBounds[3] - width;   // Right Boundary
    
        worldX = x;
        worldY = y;

        matchHitBoxBounds();
    }

    /**
     * Checks whether an entity is dead or alive based on its health.
     * @return true if the entity is dead, false otherwise.
     */
    public boolean isDead(){
        return (hitPoints <= 0);
    }


    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public void setWorldX(int worldX) {
        this.worldX = worldX;
    }

    public void setWorldY(int worldY) {
        this.worldY = worldY;
    }

    public int getPrevWorldX() {
        return prevWorldX;
    }

    public int getPrevWorldY() {
        return prevWorldY;
    }

    public int getSpeed() {
        return speed;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public int getMaxHealth(){
        return maxHealth;
    }

    public void setMaxHealth(int mh){
        maxHealth = mh;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage){
        this.damage = damage;
    }

    public void setHitPoints(int hP) {
        //Limit new hp to maxhealth-unless entity is being parsed clientside (since maxhealth isnt communicated to client)
        if(hP > maxHealth && !isMaxHealthSet) hP = maxHealth; 
        hitPoints = hP;
    }

    public void setIsMaxHealthSet(boolean b){
        isMaxHealthSet = b;
    }

    public int getCurrSprite() {
        return currSprite;
    }

    public void setCurrSprite(int currSprite) {
        this.currSprite = currSprite;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSpeed(int s){
        speed = s;
    }

    @Override
    public int getZIndex(){
        return 1;
    }
}
    
    
   
