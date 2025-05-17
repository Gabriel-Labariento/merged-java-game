public class SlowEffect extends StatusEffect{

    int initialPlayerSpeed;

    public SlowEffect(){
        duration = 3000;
        expireTime = System.currentTimeMillis() + duration;
    }

    @Override
    public void applyStatusEffect(Player player) {
        initialPlayerSpeed = player.getBaseSpeed();
        player.setSpeed(player.getBaseSpeed() - 2);        
    }

    @Override
    public void tick(Player player){
        if (isExpired()) {
            removeStatusEffect(player);
        } else applyStatusEffect(player);
    }

    @Override
    public void removeStatusEffect(Player player) {
        player.setSpeed(initialPlayerSpeed);
        System.out.println("Removed slow");
        System.out.println("Initial Player Speed: " + initialPlayerSpeed);
        System.out.println("Player speed: " + player.getSpeed());
    }

    @Override
    public StatusEffect copy(){
        return new SlowEffect();
    }
}