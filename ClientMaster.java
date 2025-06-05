import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**     
        The ClientMaster serves as a client side game state manager.
        It containes, player, room, enemy, and item data. Game logic
        is delegated to ServerMaster. However, the client still needs
        to know if game state data for rendering.

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

public class ClientMaster {
    private Player userPlayer;
    private HashMap <Integer, Room> allRooms;
    private Room currentRoom;
    private CopyOnWriteArrayList<Entity> entities; 
    private int xpBarPercent;
    private int userLvl;
    private String heldItemIdentifier;
    private boolean isGameOver;
    private int bossHPPercent;
    private int currentStage;
    private MiniMap minimap;
    private final Map<Integer, Boolean> tooltipStates;
    private Entity cachedUIItem;  // Add this field to cache the UI item
    private Item activeTooltipItem;
    
    /**
     * Creates a ClientMaster instance with the following fields as null:
     * userPlayer, allRooms, and currentRoom. The entities ArrayList
     * is initialized here.
     */
    public ClientMaster(){
        userPlayer = null; // All rendering is respective to client's player noted as "userPlayer"
        allRooms = null;
        currentRoom = null;
        entities = new CopyOnWriteArrayList<>();
        minimap = new MiniMap();
        tooltipStates = new HashMap<>();
        activeTooltipItem = null;
    }

    /**
     * Sets the userPlayer's XPBarPercentage
     * @param percent the XPBarPercent value to be set
     */
    public void setXPBarPercent(int percent){
        xpBarPercent = percent;
    }

    /**
     * Gets the value of XPBarPercent
     * @return XPBarPercent field value
     */
    public int getXPBarPercent(){
        return xpBarPercent;
    }

    /**
     * Gets the value of bossHpPercent 
     * @return bossHpPercent field value
     */
    public int getBossHPPercent(){
        return bossHPPercent;
    }

    /**
     * Sets BossHpBarPercent value to the passed argument
     * @param i value to set BossHpBarPercent to
     */
    public void setBossHPBarPercent(int i){
        bossHPPercent = i;
    }

    /**
     * Sets the userLvl value to the passed argument
     * @param lvl the userLvl value to be set to
     */
    public void setUserLvl(int lvl){
        userLvl = lvl;
    }

    /**
     * Gets the userLvl value
     * @return the userLvl field value
     */
    public int getUserLvl(){
        return userLvl;
    }

    /**
     * Sets the value of the allRooms HashMap to the argument passed
     * @param allRooms a HashMap mapping an integer id to a room
     */
    public void setAllRooms(HashMap<Integer, Room> allRooms) {
        this.allRooms = allRooms;
    }

    /**
     * Returns the userPlayer reference
     * @return a reference to the userPlayer 
     */
    public Player getUserPlayer() {
        return userPlayer;
    }

    /**
     * Sets the client's userPlayer field to the provided argument
     * @param userPlayer a Player to set the client's userPlayer field to
     */
    public void setUserPlayer(Player userPlayer) {
        this.userPlayer = userPlayer;
    }

    /**
     * Contains a list of all renderable entities in the game. It returns
     * a new instance of that entity based on the identifier.
     * @param identifier the specific String in NetworkProtocol corresponding to an entity
     * @param id the entity's id
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return a new instance of the entity corresponding to the provided identifier.
     */
    public Entity getEntity(String identifier, int id, int x, int y){
        //Player entities
        if (identifier.equals( NetworkProtocol.HEAVYCAT)) return new HeavyCat(id, x, y);
        else if (identifier.equals( NetworkProtocol.FASTCAT)) return new FastCat(id, x, y);
        else if (identifier.equals( NetworkProtocol.GUNCAT)) return new GunCat(id, x, y);

        //Item entities
        else if (identifier.equals( NetworkProtocol.REDFISH)) return new RedFish(x, y);
        else if (identifier.equals( NetworkProtocol.CATTREAT)) return new CatTreat(x, y);
        else if (identifier.equals( NetworkProtocol.MILK)) return new Milk(x, y);
        else if (identifier.equals( NetworkProtocol.PREMIUMCATFOOD)) return new PremiumCatFood(x, y);
        else if (identifier.equals( NetworkProtocol.GOLDFISH)) return new Goldfish(x, y);
        else if (identifier.equals( NetworkProtocol.LIGHTSCARF)) return new LightScarf(x, y);
        else if (identifier.equals( NetworkProtocol.THICKSWEATER)) return new ThickSweater(x, y);
        else if (identifier.equals( NetworkProtocol.BAGOFCATNIP)) return new BagOfCatnip(x, y);
        else if (identifier.equals( NetworkProtocol.LOUDBELL)) return new LoudBell(x, y);
        else if (identifier.equals( NetworkProtocol.PRINGLESCAN)) return new PringlesCan(x,y);

        //Enemy entities
        else if (identifier.equals(NetworkProtocol.SPAWN_INDICATOR))
             return new SpawnIndicator(x, y);
             
        //Normal 
        else if (identifier.equals( NetworkProtocol.SPIDER)) return new Spider(x, y);
        else if (identifier.equals( NetworkProtocol.COCKROACH)) return new Cockroach(x, y);
        else if (identifier.equals( NetworkProtocol.RAT)) return new Rat(x, y);
        else if (identifier.equals( NetworkProtocol.SMALLDOG)) return new SmallDog(x, y);
        else if (identifier.equals( NetworkProtocol.BUNNY)) return new Bunny(x, y);
        else if (identifier.equals( NetworkProtocol.FROG)) return new Frog(x,y);
        else if (identifier.equals( NetworkProtocol.BEE)) return new Bee(x, y);
        else if (identifier.equals( NetworkProtocol.SNAKELET)) return new Snakelet(x, y);
        else if (identifier.equals( NetworkProtocol.CLEANINGBOT)) return new CleaningRobot(x, y);
        else if (identifier.equals( NetworkProtocol.SECURITYBOT)) return new SecurityBot(x, y);
        else if (identifier.equals( NetworkProtocol.FERALRAT)) return new FeralRat(x,y);
        else if (identifier.equals( NetworkProtocol.SCREAMERRAT)) return new ScreamerRat(x, y);
        else if (identifier.equals( NetworkProtocol.MUTATEDANCHOVY)) return new MutatedAnchovy(x, y);
        else if (identifier.equals( NetworkProtocol.MUTATEDARCHERFISH)) return new MutatedArcherfish(x, y);
        else if (identifier.equals( NetworkProtocol.MUTATEDPUFFERFISH)) return new MutatedPufferfish(x, y);

        //Bosses
        else if (identifier.equals( NetworkProtocol.RATKING)) return new RatKing(x,y);
        else if (identifier.equals( NetworkProtocol.FERALDOG)) return new FeralDog(x, y);
        else if (identifier.equals( NetworkProtocol.TURTLE)) return new Turtle(x, y);
        else if (identifier.equals( NetworkProtocol.SNAKE)) return new Snake(x, y);
        else if (identifier.equals( NetworkProtocol.ADULTCAT)) return new AdultCat(x,y);
        else if (identifier.equals( NetworkProtocol.CONJOINEDRATS)) return new ConjoinedRats(x, y);
        else if (identifier.equals( NetworkProtocol.FISHMONSTER)) return new FishMonster(x, y);

        //Attack entities
        else if (identifier.equals(NetworkProtocol.PLAYERSMASH)) 
            return new PlayerSmash(id, null, x, y, 0, false);
        else if (identifier.equals( NetworkProtocol.PLAYERSLASH)) 
            return new PlayerSlash(id, null, x, y, 0, false);
        else if (identifier.equals( NetworkProtocol.PLAYERBULLET)) 
            return new PlayerBullet(id, null, x, y, 0, 0, 0, false);
        else if (identifier.equals(NetworkProtocol.SPIDERBULLET))
            return new SpiderBullet(null, x, y, 0, 0);
        else if (identifier.equals(NetworkProtocol.SNAKEBULLET))
            return new SnakeBullet(null, x, y, 0, 0);
        else if (identifier.equals(NetworkProtocol.LASERBULLET))
            return new LaserBullet(null, x, y, 0, 0);
        else if (identifier.equals(NetworkProtocol.ENEMYBITE))
            return new EnemyBite(null, x, y);
        else if (identifier.equals(NetworkProtocol.ENEMYSLASH))
            return new EnemySlash(null, x, y);
        else if (identifier.equals(NetworkProtocol.ENEMYSMASH))
            return new EnemySmash(null, x, y);
        else return null;
    }

    /**
     * Initializes an entity and sets up its fields necessary for rendering appropriately
     * @param identifier the specific String in NetworkProtocol corresponding to an entity
     * @param id the entity's ID
     * @param x the x-coordinate   
     * @param y the y-coordinate
     * @param roomId the roomId where the entity is to be loaded
     * @param sprite the current sprite of the entity to render
     * @param zIndex a number that corresponds to which layer should the entity be rendered relative to other entities and gameObjects
     */
    public void loadEntity(String identifier, int id, int x, int y, int roomId, int sprite, int zIndex) {
        // First check if we already have this entity
        for (Entity e : entities) {
            if (e.getId() == id && e.getIdentifier().equals(identifier)) {
                // Update existing entity
                e.setPosition(x, y);
                e.setCurrSprite(sprite);
                e.setzIndex(zIndex);
                if (e.getCurrentRoom() == null) {
                    e.setCurrentRoom(getRoomById(roomId));
                }
                return;
            }
        }
        
        // If we don't have this entity, create a new one
        Entity entity = getEntity(identifier, id, x, y);
        entity.setCurrSprite(sprite);
        entity.setzIndex(zIndex);
        entity.setCurrentRoom(getRoomById(roomId));
        addEntity(entity);
    }

    /**
     * Gets an instance of an Item corresponding to the value of heldItemIdentifier
     * @return an instance of an Item corresponding to heldItemIdentifier
     */
    public Entity generateUIItem(){
        // Only create a new item if we don't have one cached or if the held item changed
        if (cachedUIItem == null || !cachedUIItem.getIdentifier().equals(heldItemIdentifier)) {
            cachedUIItem = getEntity(heldItemIdentifier, 0, 0, 0);
        }
        return cachedUIItem;
    }

    /**
     * Sets the heldItemIdentifier field value to the given argument
     * @param identifier the identifier to set the heldItemIdentifier to
     */
    public void setHeldItemIdentifier(String identifier){
        if (!identifier.equals(heldItemIdentifier)) {
            heldItemIdentifier = identifier;
            cachedUIItem = null;  // Clear cache when held item changes
        }
    }

    /**
     * Gets a reference to the client's currentRoom
     * @return a Room object pertaining to the client's currentRoom
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Sets the client's currentRoom to the provided argument
     * @param currentRoom a reference to the Room to set the client's currentRoom to
     */
    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    /**
     * Gets the list of entities the client holds
     * @return a CopyOnWriteArrayList that holds the entities the client knows about.
     */
    public CopyOnWriteArrayList<Entity> getEntities() {
        return entities;
    }

    /**
     * Sets the entities field of the client to the provided value
     * @param entities a CopyOnWriteArrayList to set the entities field to.
     */
    public void setEntities(CopyOnWriteArrayList<Entity> entities) {
        this.entities = entities;
    }
    
    /**
     * Adds an entity to the client's entities arrayList
     * @param e the entity to be added
     */
    public void addEntity(Entity e){
        entities.add(e);
    }

    /**
     * Returns a reference to the room to which the id argument corresponds with
     * @param id the id of a Room to look for
     * @return a reference to the Room object corresponding to the passed ID.
     */
    public Room getRoomById(int id) {
        return allRooms.get(id);
    }

    /**
     * Sets the value of the isGameOver boolean to the passed argument
     * @param b the boolean value to set isGameOver to
     */
    public void setIsGameOver(boolean b){
        isGameOver = b;
    }

    /**
     * Gets the value of isGameOver
     * @return true if isGameOver is true, false otherwise
     */
    public boolean getIsGameOver(){
        return isGameOver;
    }

    public int getCurrentStage(){
        return currentStage;
    }

    /**
     * Sets the value of the currentLevel boolean to the passed argument
     * @param i the integer value to set currentLevel to
     */
    public void setCurrentStage(int i){
        currentStage = i;
    }

    /**
     * Gets the HashMap of all rooms
     * @return the allRooms HashMap
     */
    public HashMap<Integer, Room> getAllRooms() {
        return allRooms;
    }

    /**
     * Gets the minimap instance
     * @return the minimap instance
     */
    public MiniMap getMiniMap() {
        return minimap;
    }

    /**
     * Gets the tooltip visibility state for an item
     * @param itemId the ID of the item
     * @return true if the tooltip should be shown, false otherwise
     */
    public boolean getTooltipState(int itemId) {
        return tooltipStates.getOrDefault(itemId, false);
    }

    /**
     * Toggles the tooltip visibility state for an item
     * @param itemId the ID of the item
     */
    public void toggleTooltipState(int itemId) {
        tooltipStates.put(itemId, !tooltipStates.getOrDefault(itemId, false));
    }

    public void setActiveTooltipItem(Item item) {
        this.activeTooltipItem = item;
    }
    
    public Item getActiveTooltipItem() {
        return this.activeTooltipItem;
    }
}
