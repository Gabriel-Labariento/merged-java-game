import java.util.*;
import java.util.concurrent.*;

/**     
        The Itemhandler class is an a class that allows for randomly choosing between 
        certain items whenever its rollItem() method is called

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

public class ItemsHandler{
    private static final ConcurrentHashMap<String, Integer> AVAILABLEITEMS = new ConcurrentHashMap<>();
    private static double dropChanceSum;

    static {
        //Use static initializer block to make hashmap of ITEM - DROPCHANCE
        AVAILABLEITEMS.put("NONE", 60);
        AVAILABLEITEMS.put(NetworkProtocol.REDFISH, 15);
        AVAILABLEITEMS.put(NetworkProtocol.CATTREAT, 15);
        AVAILABLEITEMS.put(NetworkProtocol.MILK, 4);
        AVAILABLEITEMS.put(NetworkProtocol.PREMIUMCATFOOD, 4);
        AVAILABLEITEMS.put(NetworkProtocol.GOLDFISH, 2);
        AVAILABLEITEMS.put(NetworkProtocol.LIGHTSCARF, 2);
        AVAILABLEITEMS.put(NetworkProtocol.THICKSWEATER, 2);
        AVAILABLEITEMS.put(NetworkProtocol.BAGOFCATNIP, 2);
        AVAILABLEITEMS.put(NetworkProtocol.LOUDBELL, 2);
        AVAILABLEITEMS.put(NetworkProtocol.PRINGLESCAN, 2);

        for (Integer dropChance: AVAILABLEITEMS.values()){
            dropChanceSum += dropChance;
        }
    }
    
    public static void updateItemDropChance(String itemName, int newChance) {
        Integer oldChance = AVAILABLEITEMS.get(itemName);
        if (oldChance != null) {
            AVAILABLEITEMS.put(itemName, newChance);
            dropChanceSum += newChance - oldChance;
        }
    }
    /**
     * Generates a random item when an enemy is killed
     * @param enemy the enemy defeated that will cause the drop
     * @return the Item to be dropped
     */
    public Item rollItem(Enemy enemy){
        //Get a random number between 0 to whatever drop chance is (in this case 100)
        double roll = Math.random() * dropChanceSum;
        double cumulativeChance = 0;

        for (Map.Entry<String, Integer> entry : AVAILABLEITEMS.entrySet()){
            double dropChance = entry.getValue();

            //Add to var to project all of the dropchances within their respective domains from the interval [0,100]
            cumulativeChance += dropChance;
            
            //Check to see if random number from 0-100 is within the selection interval for a specific item
            if (roll < cumulativeChance) {
                //Create a new item from the middle of the enemy
                // System.out.print("SELECTED FROM SERVER: ");
                String heldItemIdentifier = entry.getKey();
                if (heldItemIdentifier.equals("NONE")) return null;
                else return createItem(heldItemIdentifier, enemy.getWorldX(), enemy.getWorldY());

            }
        }
        return null;
    }

    public Item createItem(String identifier, int x, int y){
        if (identifier.equals( NetworkProtocol.REDFISH)) return new RedFish(x, y);
        else if (identifier.equals( NetworkProtocol.CATTREAT)) return new CatTreat(x, y);
        else if (identifier.equals( NetworkProtocol.MILK)) return new Milk(x, y);
        else if (identifier.equals( NetworkProtocol.PREMIUMCATFOOD)) return new PremiumCatFood(x, y);
        else if (identifier.equals( NetworkProtocol.GOLDFISH)) return new Goldfish(x, y);
        else if (identifier.equals( NetworkProtocol.LIGHTSCARF)) return new LightScarf(x, y);
        else if (identifier.equals( NetworkProtocol.THICKSWEATER)) return new ThickSweater(x, y);
        else if (identifier.equals( NetworkProtocol.BAGOFCATNIP)) return new BagOfCatnip(x, y);
        else if (identifier.equals( NetworkProtocol.LOUDBELL)) return new LoudBell(x, y);
        else if (identifier.equals( NetworkProtocol.PRINGLESCAN)) return new PringlesCan(x,y);
        else return null;
    }

}