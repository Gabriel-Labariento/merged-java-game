public class PoisonEffect extends StatusEffect{

    /**     
        The PoisonEffect class extends StatusEffect. It damages the Player
        for by subtracting 1 from its hitPoints every second for three
        seconds.

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

    public static final int DPS = 1;
    private static final int DAMAGE_INTERVAL = 1000;
    private long lastAppliedTime = 0;

    /**
     * Creates a new PoisonEffect instance  with a duration of 3 seconds.
     */
    public PoisonEffect(){
        duration = 3000;
        expireTime = System.currentTimeMillis() + duration;
        lastAppliedTime = System.currentTimeMillis();
    }

    @Override
    public void applyStatusEffect(Player player) {
        if (!player.getStatusEffects().contains(this)) player.setHitPoints(player.getHitPoints() - DPS);
    }

    @Override
    public void tick(Player player){
        long now = System.currentTimeMillis();
        if (isExpired()) {
            removeStatusEffect(player);
            return;
        }

        if (now - lastAppliedTime > DAMAGE_INTERVAL) {
            applyStatusEffect(player);
            lastAppliedTime = now;
        }
        
    }

    @Override
    public void removeStatusEffect(Player player) {
    }

    @Override
    public StatusEffect copy(){
        return new PoisonEffect();
    }
}