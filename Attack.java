import java.util.ArrayList;

/**     
        The Attack class extends the Entity class and is extended
        by other attacks in the game from both Player and Enemy.
        The methods listed here handle owner attachment, attack
        effects handling, and render order setting.

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

public abstract class Attack extends Entity{
    public static int attackNum = Integer.MIN_VALUE;
    public int duration;
    public int xOffset;
    public int yOffset;
    public long expirationTime;
    public boolean isFriendly;
    public boolean isOffsetInitialized;
    public Entity owner;
    private final ArrayList<StatusEffect> attackEffects;
    protected boolean hasPlayedSound = false;

    /**
     * At the instantiation of an object that extends Attack,
     * a new attackEffects ArrayList is added where the StatusEffect
     * abilities of the attack are added.
     */
    public Attack(){
        attackEffects = new ArrayList<>();
    }

    /**
     * Sets the expritation time of the Attack instance to
     * the time of calling + the passed argument in Milliseconds
     * @param duration how long from creation does the attack expire
     */
    public void setExpirationTime(int duration){
        expirationTime = System.currentTimeMillis() + duration;
    }

    /**
     * Entity to whom the Attack is attached to
     * @return a reference to the Attack's owner
     */
    public Entity getOwner(){
        return owner;
    }

    /**
     * Makes an Attack object follow its owner amidst owner movement
     * Updates the hitbox of the Attack as well to match its position
     */
    public void attachToOwner(){    
        if (owner != null){
            int ownerX = owner.getWorldX();
            int ownerY = owner.getWorldY();
            int prevOwnerX = owner.getPrevWorldX();
            int prevOwnerY = owner.getPrevWorldY();

            // Initialize attack-owner offset
            if (!isOffsetInitialized){
                isOffsetInitialized = true;
                xOffset = worldX - ownerX;
                yOffset = worldY - ownerY;
            }

            // //If owner has moved, only then should you move attack
            if (prevOwnerX != ownerX || prevOwnerY != ownerY){
                worldX = ownerX + xOffset;
                worldY = ownerY + yOffset;
            }

            matchHitBoxBounds();
        }
    }

    /**
     * Checks if at the time of calling, the current time is past the
     * set expiration time in setExpirationTime
     * @return true if the attack is expired, false otherwise
     */
    public boolean getIsExpired(){
        return System.currentTimeMillis() >= expirationTime;
    }

    /**
     * Gets the Attack's isFriendly field value.
     * Attacks from Players are marked isFriendly = true
     * Attacks from Enemies are marked isFriendly = false
     * @return true if the Attack isFriendly, false otherwise
     */
    public boolean getIsFriendly(){
        return isFriendly;
    }

    /**
     * Adds a StatusEffect to the object's attackEffects ArrayList
     * @param se the StatusEffect to be added
     */
    public void addAttackEffect(StatusEffect se){
        attackEffects.add(se);
    }

    /**
     * Gets the attackEffects ArrayList of the Attack instance
     * @return the attackEffects ArrayList of the object
     */
    public ArrayList<StatusEffect> getAttackEffects() {
        return attackEffects;
    }

    @Override
    public int getZIndex(){
        return 0;
    }

}