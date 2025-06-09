import java.awt.Graphics2D;

/**     
        The Entity class extends GameObject and is extended directly
        by Player, Enemy, Attack, and Item classes. The methods in this 
        class are mostly related to getting and setting fields, getting
        Entity data, and handling Entity movement bounds.

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



    /**
     * Draws the object relative to the userPlayer with offsets defined in the
     * GameCanvas class
     * @param g2d the Graphics2D object used for drawing 
     * @param xOffset the xOffset defined in GameCanvas
     * @param yOffset the yOffset defined in GameCanvas
     */
    public abstract void draw(Graphics2D g2d, int xOffset, int yOffset);

    /**
     * Calls update on Entity behavior that relies on game tick intervals:
     * movement, attack, sprite, and status effect updates
     * @param gsm the ServerMaster instance containing the Entities ArrayList
     */
    public abstract void updateEntity(ServerMaster gsm);
    
    /**
     * Builds a String that contains all the Entity data that the client needs.
     * @param isUserPlayer boolean value: true if pertaining to the userPlayer, false otherwise
     * @return a String in the format: identifier,id,x,y,roomId,sprite,zIndex|
     */
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

    /**
     * Sets the position of an Entity while respecting the bounds of the Room.
     * If the position of the Entity to be set exceeds the Room bounds, it snaps
     * the position to the edge of the Room.
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
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
     * Gets the clientId of the Entity
     * @return the value of clientId
     */
    public int getClientId(){
        return clientId;
    }

    /**
     * Gets the identifier of the Entity
     * @return the String identifier assigned to the Entity
     */
    public String getIdentifier(){
        return identifier;
    }

    /**
     * Gets the value of the defense field
     * @return the int value of the defense field
     */
    public int getDefense(){
        return defense;
    }

    /**
     * Sets the value of the defense field to the passed argument
     * @param d the value to set the defense field to
     */
    public void setDefense(int d){
        defense = d;
    }

    /**
     * Gets the value of attackCDDuration
     * @return the int value of attackCDDuration
     */
    public int getAttackCDDuration(){
        return attackCDDuration;
    }

    /**
     * Sets the value of attackCDDuration to the passed argument
     * @param duration the value to set attackCDDuration to
     */
    public void setAttackCDDuration(int duration){
        attackCDDuration = duration;
    }

    /**
     * Gets the value of attackFrameDuration
     * @return the int value of attackFrameDuration
     */
    public int getAttackFrameDuration(){
        return attackFrameDuration;
    }

    /**
     * Sets the value of attackFrameDuration to the passed argument
     * @param a the int value to set attackFrameDutation to
     */
    public void setAttackFrameDuration(int a){
        attackFrameDuration = a;
    }

    /**
     * Gets the value of the isAttacking field
     * @return the boolean value of isAttacking
     */
    public boolean getIsAttacking(){
        return isAttacking;
    }

    /**
     * Sets the value of the isAttacking field to the passed argument
     * @param b the boolean value to set isAttacking to
     */
    public void setIsAttacking(boolean b){
        isAttacking = b;
    }   
    

    /**
     * Checks whether an entity is dead or alive based on its health.
     * @return true if the entity is dead, false otherwise.
     */
    public boolean isDead(){
        return (hitPoints <= 0);
    }

    /**
     * Gets a reference to the currentRoom field value
     * @return a Room object pertaining to the currentRoom
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Sets the currentRoom field value to the passed argument
     * @param currentRoom the Room to set currentRoom to
     */
    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    /**
     * Sets the worldX field value to the provided argument
     * @param worldX the value to set worldX to
     */
    public void setWorldX(int worldX) {
        this.worldX = worldX;
    }

    /**
     * Sets the woldY field value to the provided argument
     * @param worldY the value to set worldY to
     */
    public void setWorldY(int worldY) {
        this.worldY = worldY;
    }

    /**
     * Gets the value of the prevWorldX field
     * @return the int value of prevWorldX
     */
    public int getPrevWorldX() {
        return prevWorldX;
    }

    /**
     * Gets the value of prevWorldY field
     * @return the int value of prevWorldY
     */
    public int getPrevWorldY() {
        return prevWorldY;
    }

    /**
     * Gets the value of the speed field
     * @return the int value of the speed field
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Gets the value of the hitPoints field
     * @return the int value of the hitPoints field
     */
    public int getHitPoints() {
        return hitPoints;
    }

    /**
     * Gets the value of the maxHealth field
     * @return the int value of the maxHealth field
     */
    public int getMaxHealth(){
        return maxHealth;
    }

    /**
     * Sets the value of the maxHealth field to the passed argument
     * @param mh the int value to set maxHealth to
     */
    public void setMaxHealth(int mh){
        maxHealth = mh;
    }

    /**
     * Gets the value of the damage field
     * @return the int value of the damage field
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Sets the value of damage to the passed argument
     * @param damage the int value to set damage to
     */
    public void setDamage(int damage){
        this.damage = damage;
    }

    /**
     * Set the value of the hitPoints field to the passed argument
     * Limits new hp to maxhealth-unless entity is being parsed 
     * clientside (since maxhealth isn't communicated to client)
     * @param hP the int value to set hitPoints to
     */
    public void setHitPoints(int hP) {
        if(hP > maxHealth && !isMaxHealthSet) hP = maxHealth; 
        hitPoints = hP;
    }

    /**
     * Sets the value of isMaxHealthSet to the provided argument
     * @param b the boolean value to set isMaxHealthSet to
     */
    public void setIsMaxHealthSet(boolean b){
        isMaxHealthSet = b;
    }

    /**
     * Gets the value of the currSprite field
     * @return the int value of currSprite
     */
    public int getCurrSprite() {
        return currSprite;
    }

    /**
     * Sets the currSprite field value to the passed argument
     * @param currSprite the int value to set currSprite to
     */
    public void setCurrSprite(int currSprite) {
        this.currSprite = currSprite;
    }
    
    /**
     * Gets the value of the Id field
     * @return the int value of Id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the value of the Id field to the passed argument
     * @param id the int value to set Id to
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the value of the speed field to the passed argument
     * @param s the int value to set speed to
     */
    public void setSpeed(int s){
        speed = s;
    }

    @Override
    public int getZIndex(){
        return 1;
    }
}
    
    
   
