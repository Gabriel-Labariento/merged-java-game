/**     
        The SlowEffect extends StatusEffect. It lasts for three seconds.
        It slows down the player by two movement units during the time 
        of application.

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

public class SlowEffect extends StatusEffect{
    /**
     * Creates a SlowEffect instance that lasts for 3 seconds
     */
    public SlowEffect(){
        duration = 3000;
        expireTime = System.currentTimeMillis() + duration;
    }

    @Override
    public void applyStatusEffect(Player player) {
        player.setSpeed(1);        
    }

    @Override
    public void tick(Player player){
        if (isExpired()) {
            removeStatusEffect(player);
        } else applyStatusEffect(player);
    }

    @Override
    public void removeStatusEffect(Player player) {
        player.setSpeed(player.getBaseSpeed());
    }

    @Override
    public StatusEffect copy(){
        return new SlowEffect();
    }
}