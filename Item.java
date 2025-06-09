import java.awt.Graphics2D;

/**     
        The Item class is an abstract class that extends Entity.
        It supports item expiration handling, item to player interaction,
        and item ownership.

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

public abstract class Item extends Entity{
    public long despawnTime;
    public long pickUpCDTime;
    public static final int DROPDURATION = 60000;
    public static final int PICKUPCDDURATION = 5000;
    public Player owner;
    public boolean isConsumable;
    public boolean isHeld;
    public int initialCDDuration;
    public int initialDamage;
    public int initialMaxHealth;
    public int initialHitPoints;
    public int initialSpeed;
    public int initialDefense;
    public int initialAttackFrameDuration;
    protected String itemName;
    protected String itemDescription;
    protected ItemTooltip tooltip;
    protected int tooltipX, tooltipY;
    protected static int itemId  = 0;

    /**
     * Upon item creation, triggerDespawnTimer is called immediately
     * to signal when the item should despawn.
     */
    public Item() {
        triggerDespawnTimer();
        width = 16;
        height = 16;
        id = itemId++;
    }

    /**
     * Initializes the tooltip for this item
     * @param name the name of the item
     * @param description the description of the item
     * @param isConsumable whether the item is consumable
     */
    protected void initTooltip(String name, String description, boolean isConsumable) {
        this.itemName = name;
        this.itemDescription = description;
        this.isConsumable = isConsumable;
        this.tooltip = new ItemTooltip(name, description, isConsumable);
    }

    public ItemTooltip getTooltip() {
        return tooltip;
    }

    /**
     * Sets the value of the owner field to the passed argument
     * @param player the Player to set owner to
     */
    public void setOwner(Player player){
        owner = player;
    }

    /**
     * Sets despawnTime to the time of invoction + DROPDURATION
     */
    public void triggerDespawnTimer(){
        despawnTime = System.currentTimeMillis() + DROPDURATION;
    }

    /**
     * Sets the valud of the isHeld field
     * @param b the boolean value to set isHeld to
     */
    public void setIsHeld(boolean b){
        isHeld = b;
    }

    /**
     * Applies the item's effect to the owner
     */
    public abstract void applyEffects();

    /**
     * Removes the item's effect on the owner
     */
    public abstract void removeEffects();

    /**
     * Sets pickUpCDTime to the time of invocation + PICKUPCDDURATION
     */
    public void triggerPickUpCD(){
        pickUpCDTime = System.currentTimeMillis() + PICKUPCDDURATION;
    }

    /**
     * Checks whether an item is on pickup cooldown
     * @return true if the current time is less than the assigned pickUPCDTime, false otherwise
     */
    public boolean getIsOnPickUpCD(){
        return System.currentTimeMillis() < pickUpCDTime;
    }

    /**
     * Gets the value of the isConsumable field
     * @return true if the item is consumable, false otherwise
     */
    public boolean getIsConsumable(){
        return isConsumable;
    }

    /**
     * Gets the field value of isDespawned
     * @return false if the item is held or the current time is less than or equal to despawnTime, true otherwise
     */
    public boolean getIsDespawned(){
        if (isHeld) return false;
        return System.currentTimeMillis() >= despawnTime;
    }

    @Override
    public String getAssetData(boolean isUserPlayer) {
        StringBuilder sb = new StringBuilder();
        // System.out.println("In getAssetData of Rat, identifier is " + identifier);
        // String format: B,id,x,y,currentRoomId|
        sb.append(identifier).append(NetworkProtocol.SUB_DELIMITER)
        .append(id).append(NetworkProtocol.SUB_DELIMITER)
        .append(worldX).append(NetworkProtocol.SUB_DELIMITER)
        .append(worldY).append(NetworkProtocol.SUB_DELIMITER)
        .append(currentRoom.getRoomId()).append(NetworkProtocol.DELIMITER);

        return sb.toString();
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
    public void updateEntity(ServerMaster gsm){
        matchHitBoxBounds();
    }

    public void updateTooltip() {
        tooltipX = worldX;
        tooltipY = worldY;
        tooltip.setPosition(tooltipX, tooltipY);
        System.out.println("in updatetooltip");
    }
    
    public void drawTooltip(Graphics2D g2d) {
        tooltip.draw(g2d, tooltipX, tooltipY);
    }
}