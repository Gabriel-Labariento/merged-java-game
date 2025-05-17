public abstract class StatusEffect {
    protected int duration;
    protected long expireTime;

    public abstract StatusEffect copy();

    public abstract void applyStatusEffect(Player player);

    public abstract void removeStatusEffect(Player player);

    public boolean isExpired(){
        return System.currentTimeMillis() >= expireTime;
    }

    public abstract void tick(Player player);
}