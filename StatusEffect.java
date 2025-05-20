/**     
        The StatusEffect class is an abstract class. It is extended
        by PoisonEffect and SlowEffect. It supports the creation of
        a copy of the StatusEffect, the application and removal of the
        effect on an Effectable, and the update of StatusEffects across
        time.

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

public abstract class StatusEffect {
    protected int duration;
    protected long expireTime;

    /**
     * Creates a copy of the specific StatusEffect with a set duration.
     * This ensures that upon application, a new copy is applied to the 
     * Player with full duration.
     * @return a copy of the StatusEffect being applied
     */
    public abstract StatusEffect copy();

    /**
     * Applies the effect to the player 
     * @param player the player to apply the StatusEffect to
     */
    public abstract void applyStatusEffect(Player player);

    /**
     * Removes the StatusEffect from the player's statusEffects ArrayList
     * and undoes any effects brought on by applyStatusEffects
     * @param player the player being affected by the StatusEffect
     */
    public abstract void removeStatusEffect(Player player);

    /**
     * Checks whether the current time is greater than or equal to the
     * StatusEffect's expireTime
     * @return true if the StatusEffect is expired, false otherwise
     */
    public boolean isExpired(){
        return System.currentTimeMillis() >= expireTime;
    }

    /**
     * The main update method of the StatusEffect. Calls applyStatusEffect
     * and checks if the StatusEffect is expired for removal every game tick.
     * @param player
     */
    public abstract void tick(Player player);
}