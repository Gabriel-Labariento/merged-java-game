public class PoisonEffect extends StatusEffect{

    public static final int DPS = 1;
    private static final int DAMAGE_INTERVAL = 1000;
    private long lastAppliedTime = 0;

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
        System.out.println("Removed poison effect");
    }

    @Override
    public StatusEffect copy(){
        return new PoisonEffect();
    }
}