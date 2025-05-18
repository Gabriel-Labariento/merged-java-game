/**     
        The Effectable interface is implemented by an Entity
        who can be affected by StatusEffects. It holds methods
        for adding a status effect to the entity and updating
        the status effect.

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

public interface Effectable {
    /**
     * Adds a Status Effect to the Entity
     * @param se the statusEffect to be added
     */
    public void addStatusffect(StatusEffect se);

    /**
     * Updates the status effects on the implementing Entity
     */
    public void updateStatusEffects();
}