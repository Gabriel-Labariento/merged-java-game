import java.io.*;

public class SaveState implements Serializable{
    
    public int gameLevel;
    public boolean isGameOver;
    public boolean isFinalBossKilled;
    public boolean hasChosenScene5End;
    public int playerNum;

    public int clientId;
    public String playerType; // FASTCAT, HEAVYCAT, GUNCAT
    public int hitPoints, maxHealth;
    public int damage;
    public int defense;
    public int currentXP;
    public int currentLvl;
    public int currentXPCap;
    public boolean isDown;
    public String heldItemIdentifier; // null if no item
    
    // public static class PlayerSaveData implements Serializable {
    //     public int clientId;
    //     public String playerType;
    //     public int worldX, worldY;
    //     public int hitPoints, maxHealth;
    //     public int currentLvl;
    //     public int xp;
    //     public boolean isDown;
    //     public String heldItemType; // null if no item
        
    //     public PlayerSaveData(Player player) {
    //         this.clientId = player.getClientId();
    //         this.playerType = player.getIdentifier();
    //         this.worldX = player.getWorldX();
    //         this.worldY = player.getWorldY();
    //         this.hitPoints = player.getHitPoints();
    //         this.maxHealth = player.getMaxHealth();
    //         this.currentLvl = player.getCurrentLvl();
    //         this.xp = player.getCurrentXP();
    //         this.isDown = player.getIsDown();
            
    //         Item heldItem = player.getHeldItem();
    //         this.heldItemType = (heldItem != null) ? heldItem.getIdentifier() : null;
    //     }
    // }
    

}