public class SlowEffect extends StatusEffect{

    public SlowEffect(){
        duration = 3000;
        expireTime = System.currentTimeMillis() + duration;
    }

    @Override
    public void applyStatusEffect(Player player) {
        int slowedSpeed = player.getBaseSpeed() - 2;
        player.setSpeed(slowedSpeed);        
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
        System.out.println("Removed slow");
        System.out.println("Player speed: " + player.getSpeed());
    }

    @Override
    public StatusEffect copy(){
        return new SlowEffect();
    }
}