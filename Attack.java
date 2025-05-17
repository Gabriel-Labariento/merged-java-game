
import java.util.ArrayList;

public abstract class Attack extends Entity{
    public static int attackNum = Integer.MIN_VALUE;
    public int duration;
    public int xOffset;
    public int yOffset;
    public long expirationTime;
    public boolean isFriendly;
    public boolean isOffsetInitialized;
    public Entity owner;
    private ArrayList<StatusEffect> attackEffects;

    public Attack(){
        attackEffects = new ArrayList<>();
    }

    
    public void setExpirationTime(int duration){
        expirationTime = System.currentTimeMillis() + duration;
    }

    public Entity getOwner(){
        return owner;
    }

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

    public boolean getIsExpired(){
        return System.currentTimeMillis() >= expirationTime;
    }

    public boolean getIsFriendly(){
        return isFriendly;
    }

    public void addAttackEffect(StatusEffect se){
        attackEffects.add(se);
    }

    public ArrayList<StatusEffect> getAttackEffects() {
        return attackEffects;
    }

    @Override
    public int getZIndex(){
        return 0;
    }


}