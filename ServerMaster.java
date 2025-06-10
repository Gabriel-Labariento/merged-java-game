import java.awt.Point;
import java.util.*;
import java.util.concurrent.*;

/**     
        The ServerMaster class serves as the central game state manager and logic
        handler for the game. It manages all game entities, all collisions, player
        inputs, and facilitates communication between server and client/s over the
        network.

        Only one instance of this class can exist through a Singleton pattern.
        See: https://www.geeksforgeeks.org/singleton-design-pattern/
        It controls the game update loop, tracks the states of players, allows
        room transitions, and facilitates level progress.

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

public class ServerMaster {
    private CopyOnWriteArrayList<Entity> entities;
    private DungeonMap dungeonMap;
    private ItemsHandler itemsHandler;
    private MiniMapManager miniMapManager;
    private int userPlayerIndex;
    private Room currentRoom;

    private static int gameLevel;
    private static final int MAX_LEVEL = 7;
    private boolean isTutorialComplete = false;
    private boolean isItemCollisionAllowed = true;

    private final ConcurrentHashMap<Character, Integer> keyInputQueue;
    private final ConcurrentHashMap<Integer, Integer> availableRevives;
    private ArrayList<GameServer.ConnectedPlayer> connectedPlayers;
    private final ArrayList<ClickInput> clickInputQueue;
    
    private int playerNum;
    private int downedPlayersNum;
    private int bossHPPercent;

    private static ServerMaster singleInstance = null;

    private boolean hasPlayedGameOverSound = false;
    private boolean hasPlayedRevivingSound = false;
    private boolean hasPlayedReviveSuccessSound = false;

    /**
     * Private constructor used in the Singleton pattern. This 
     * initializes all required game systems including entity tracking,
     * map initialization, input control, and item handling.
     */
    private ServerMaster(){
        // PLAYERS AND ENTITIES
        playerNum = 0;
        downedPlayersNum = 0;
        gameLevel = 0;
        entities = new CopyOnWriteArrayList<>();
        availableRevives = new ConcurrentHashMap<>();
        userPlayerIndex = -1;

        // MAP
        dungeonMap = new DungeonMap(gameLevel);
        dungeonMap.generateRooms();
        currentRoom = dungeonMap.getStartRoom();
        
        // INPUTS
        keyInputQueue = new ConcurrentHashMap<>();
        clickInputQueue = new ArrayList<>();

        // MINIMAP
        miniMapManager = new MiniMapManager();
        miniMapManager.initializeMap(currentRoom);

        // ITEMS
        itemsHandler = new ItemsHandler();

    }

    /**
     * Gets the singular instance of ServerMaster and creates one if no instance
     * yet exists.
     * @return the singleton instance of ServerMaster
     */
    public static synchronized ServerMaster getInstance() {
        if (singleInstance == null) {
            singleInstance = new ServerMaster();
            singleInstance.resetSoundFlags();  // Reset flags when creating new instance
        }
        return singleInstance;
    }

    /**
     * Main game update method called on each frame that handles all game logic.
     * It processes inputs, checks for collisions between entities, and updates
     * entities. It does not do anything when there are no entities yet.
     */
    public void update(){
        // Do not update the game at start of the gameserver (no entities yet)
        //  System.out.println("Entities array size: " + entities.size());
        if (entities.isEmpty()) return;

        // Update objects accordingly to the inputs
        processInputs();

        //Detect and resolve collisions
        checkCollisions();

        //Process the changes and update existing entities accordingly
        updateEntities();
    }

    /**
     * Updates all entities that exist in the game, handles their death, and 
     * handles special events like player revives and special enemy deaths.
     * It calls the handleDownsAndRevives method to trigger revival mechanics.
     * It handles the addition and removal of items dropped from enemies.
     * It removes expired attacks. It calls the updateEntity() method on individual
     * entities. And it checks if the currentRoom has been cleared.
     *  
     */
    public void updateEntities(){
        //Check on and resolve the end of life properties of each entity
        for (Entity entity:entities){
            if(entity instanceof Player player && player.getHitPoints() <=0){
                //Trigger goldfish effects on death if player is holding it
                if ((player.getHeldItem()) instanceof Goldfish){
                    player.setHitPoints(player.getMaxHealth());
                    player.setIsDown(false);
                    player.setHeldItem(null);
                    continue;
                }
                handleDownsAndRevives(player);
            }
            // Special deaths, xp addition, and item drops
            else if (entity instanceof Enemy enemy){
                if (enemy.getIsBoss()) bossHPPercent = (int) (((double)enemy.getHitPoints()/enemy.getMaxHealth())*100);
                if (enemy.getHitPoints() <= 0){
                    //Trigger death animation;
                    //Give reward xp to the player who took the last hit
                    for (Entity e:entities){
                        if(e instanceof Attack attack && attack.getId() == enemy.getLastAttackID()){
                            ((Player)attack.getOwner()).applyXP(enemy.getRewardXP());
                        }
                    }
                    
                    //Handle Special Deaths
                    if(enemy instanceof ConjoinedRats cr) cr.handleDeath(this);
                    if(enemy instanceof FishMonster fm && !(fm.isPhase3())) {
                        fm.triggerPhase3();
                        continue;    
                    }
                    
                    Entity rolledItem = itemsHandler.rollItem(enemy);
                    if (rolledItem != null) addEntity(rolledItem);
                    entities.remove(enemy);
                }   
            }
            else if (entity instanceof Attack attack && attack.getIsExpired()){
                entities.remove(attack);
            }
            else if (entity instanceof Item item && item.getIsDespawned()){
                entities.remove(item);
            }

            entity.updateEntity(this);
        }
        //Reset list to track available revives per frame
        availableRevives.clear();

        // Check for room clearing after all entity updates
        if (checkRoomCleared()) handleRoomCleared();
    }

    /**
     * Handles the player down and revive mechanics. When a player is downed,
     * that is their hitPoints <= 0, their isDown field is set to true and their
     * sprite is updated to show a downed state. However, they can be revived by
     * other players by standing next to them. If all players in the game are downed,
     * then the game is over.s
     */
    private void handleDownsAndRevives(Player player) {
        //DOWNING AND REVIVAL MECHANICS
            //Set death sprite
            player.setCurrSprite(9);

            //If the player has not yet been recorded as being downed, set them as such
            if (!player.getIsDown()){
                player.setIsDown(true);

                //If no players are left, activate end game sequence
                downedPlayersNum++;
                if (downedPlayersNum == playerNum){
                    StringBuilder sb = new StringBuilder();
                    
                    sb.append(NetworkProtocol.GAME_OVER);
                    sendMessageToClients(sb.toString());
                    if (!hasPlayedGameOverSound) {
                        SoundManager.getInstance().stopAllSounds();
                        SoundManager.getInstance().playSound("gameOver");
                        hasPlayedGameOverSound = true;
                    }
                    entities.clear(); // important
                }
            }

            //Search through list of available revives to see if the player can be revived
            boolean isInContact = false;
            for (int i:availableRevives.values()){
                
                if (i == player.getClientId()){
                    //If the player has not yet been recorded as reviving, set them as such
                    if (!player.getIsReviving()){
                        player.triggerRevival();
                        player.setIsReviving(true);
                        if (!hasPlayedRevivingSound){
                            SoundManager.getInstance().playPooledSound("reviving");
                            hasPlayedReviveSuccessSound = true;
                        }
                        

                    }
                    isInContact = true;
                    break;
                } 
            }

            //If the other player has moved away from the downed player
            if(!isInContact){
                player.setIsReviving(false);
                SoundManager.getInstance().stopSound("reviving");
                hasPlayedRevivingSound = false;  // Reset the reviving sound flag when no longer in contact
                
                //Insert UI indicators
            }

            //After the revivaltime and without the living player moving away, revive the downed player with one health
            if(player.getIsReviving() && player.getIsRevived()){
                player.setHitPoints(1);
                player.setIsDown(false);

                //Remove death sprite
                player.setCurrSprite(3);

                if (!hasPlayedReviveSuccessSound) {
                    SoundManager.getInstance().playPooledSound("reviveSuccess");
                    hasPlayedReviveSuccessSound = true;
                }
                
                // Reset all revive-related sound flags after successful revival
                hasPlayedRevivingSound = false;
                hasPlayedReviveSuccessSound = false;
            }
        }
    
    /**
     * Checks if the currentRoom has been cleared of enemies. Different room
     * types have different clear logics. Start Rooms are always cleared already.
     * End rooms are only cleared when the boss is defeated. Non-start and non-end
     * rooms are cleared when all the spawnable enemies from that Room's mobSpawner
     * have been defeated.
     * @return true if the room is cleared, false otherwise
     */
    private boolean checkRoomCleared(){
        if (currentRoom.isStartRoom() || currentRoom.isCleared()) return true;

        if (currentRoom.isEndRoom()) {
            if (currentRoom.getMobSpawner().isBossKilled()){
                currentRoom.setCleared(true);
                return true;
            }
        }

        if (!currentRoom.isEndRoom() && currentRoom.getMobSpawner().isAllKilled()) {
            currentRoom.setCleared(true);
            return true;
        } return false;
    }

    /**
     * Once a room is cleared, stops the mob spawner, opens its doors and marks it as cleared.
     * Calls a handleBossDefeat() if room is an end room
     */
    private void handleRoomCleared(){
        if (currentRoom.isStartRoom() || currentRoom.isClearedHandled()) return;

        if (!currentRoom.getMobSpawner().isSpawning()) return;

        currentRoom.getMobSpawner().stopSpawn();
        currentRoom.openDoors();

        if (currentRoom.isEndRoom()) {
            handleBossDefeat();
        }

        currentRoom.setIsClearedHandled(true);
    }
    
    /**
     * Increments the game level
     * and adds a new door to a random direction in the room. The 
     * data for this door is sent to the client for drawing
     */
    private void handleBossDefeat(){
        String doorData = addExitRoomGoingToNewDungeon();
        StringBuilder sb = new StringBuilder();
        sb.append(NetworkProtocol.BOSS_KILLED).append(doorData); //BK:D:doorId,x,y,direction,roomAId,roomBId
        sendMessageToClients(sb.toString());

        // Stop boss music and play defeat sound
        SoundManager.getInstance().stopSound("bossMusic");
        SoundManager.getInstance().playPooledSound("bossDefeat");
        
        // Ensure the room is marked as cleared
        currentRoom.setCleared(true);
    }

    /**
     * Creates a door on a random direction in the room where a door does not yet exist.
     * It then serializes this door.
     * @return a string in the format D:id,x,y,roomAID,roomBID where roomAID = roomBID = currentRoomID.
     */
    private String addExitRoomGoingToNewDungeon(){
        if (!currentRoom.isEndRoom()) return null;
        String direction = null;
        while (true) {
            direction = currentRoom.chooseRandomDirection();
            if (!currentRoom.getDoors().containsKey(direction)) break;
        }
        // System.out.println("Direction: " + direction);

        Door d = currentRoom.createDoorFromDirection(direction);
        d.setIsExitToNewDungeon(true);
        d.setIsOpen(true);
        d.setRoomB(currentRoom);
        currentRoom.addDoorToArrayList(d);
        
        return d.serialize();
    }
    
    /**
     * Scans through the entities ArrayList and returns a new ArrayList containing
     * all the Player instances.
     * @return an ArrayList with all Player instances in the entities ArrayList
     */
    private ArrayList<Player> getAllPlayers(){
        ArrayList<Player> players = new ArrayList<>();

        for (Entity e : entities) {
            if (e instanceof Player player) players.add(player);
        }
        return players;
    }

    /**
     * Creates a new dungeon, sends its data to the clients.
     * Gets the data of the players and sends it to the clients as well.
     * Sent String is in the format: LC:M:(See DungeonMap.serialize())|E:(Player Data)
     */
    public void triggerLevelTransition(){    
        incrementGameLevel();
        ArrayList<Player> players = getAllPlayers();
        entities.clear();

        DungeonMap newDungeon = generateNewDungeon();
        dungeonMap = newDungeon;
        currentRoom = newDungeon.getStartRoom();

        // Play the new level's ambient music
        SoundManager.getInstance().playLevelMusic(gameLevel);

        String newDungeonMapData = newDungeon.serialize();  
        StringBuilder mapData = new StringBuilder();

        mapData.append(NetworkProtocol.LEVEL_CHANGE).append(newDungeonMapData).append(NetworkProtocol.DELIMITER);
        sendMessageToClients(mapData.toString());

        for (Player player : players) {
            player.setCurrentRoom(currentRoom);
            player.setWorldX(currentRoom.getCenterX());
            player.setWorldY(currentRoom.getCenterY());
            entities.add(player);
        }

        // Clear and reinitialize the minimap
        for (GameServer.ConnectedPlayer player : connectedPlayers) {
            miniMapManager.clearDiscoveredRooms(player.getCid());
            miniMapManager.addNewPlayer(player.getCid(), currentRoom);
        }

        miniMapManager.initializeMap(dungeonMap.getStartRoom());
    }
    
    /**
     * Broadcasts a message to all connectd clients. It is used for sending
     * specific game state updates and events.
     * @param message the message String to send to all the clients
     */
    public void sendMessageToClients(String message){
        for (GameServer.ConnectedPlayer cp : connectedPlayers) {
            cp.promptAssetsThread(message);
        }
    }

    /**
     * Detects and handles collisions between the game entities. 
     * It uses a sort, sweep, and prune algorithm for more efficient
     * collision detection.
     * First, it sorts entities by their x-axis position.
     * Then, it checks for possible collisions between those that overlap on the x-axis.
     * Then, it confirms if a collision really is happening through the entities' hitBoxBounds.
     * If a collision is detected, it calls resolveCollision() to handle the collision.
     */
    public void checkCollisions(){
        //SORT, SWEEP, AND, PRUNE DETECTION
        try {
            //Make a new arraylist containing all of the elements of entities
            ArrayList<Entity> sortedEntities = new ArrayList<>(entities);
            // System.out.println("Entity count: " + sortedEntities.size());

            //Sort entities from the universal arraylist by their worldx values (left bounds)
            Collections.sort(sortedEntities, Comparator.comparingInt(e -> e.getHitBoxBounds()[2]));
            // System.out.println("Number of entities in sortedEntities " + sortedEntities.size());
            int size = sortedEntities.size();
            for(int i = 0; i < size; i++){
                Entity entity1 = sortedEntities.get(i);
                int[] b1 = entity1.getHitBoxBounds();

                // Get the entity at the next index
                for(int j = (i+1); j < size; j++){
                    Entity entity2 = sortedEntities.get(j);
                    if (entity2 == null) {
                        // System.out.println("entity 2 is null in sortedEntities");
                        continue;
                    }

                    int[] b2 = entity2.getHitBoxBounds();
 
                    //Skip detection if the second entity starts after the first ends on the x-axis
                    if(b2[2]>b1[3]) break;

                    //Check for collisions in the top, bottom, left, right of entity1 against entity2
                    if (b1[0] < b2[1] && b1[1] > b2[0] && b1[2] < b2[3] && b1[3] > b2[2]){
                        resolveCollision(entity1, entity2, b1, b2);
                        // System.out.println("Detected collision between " + entity1.getClass() + " and " + entity2.getClass());
                    }
                }
            }   
        } catch (Exception e) {
            System.err.println("Exception in check collisions: " + e);
        }
        
    }

    /**
     * Resolves a collision between two entities depending on their types.
     * Attack and Enemy/Player collision damages and knocks back the enemy/player
     * Players colliding with enemies damages the Player
     * Enemies colliding with each other pushes them away from each other
     * Players colliding with other players triggers revival mechanics if one of them is down
     * Players colliding with items allows for item pick up
     * @param e1 the first entity
     * @param e2 the second entity
     * @param b1 the hitboxBounds of the first entity
     * @param b2 the hitboxBounds of the second entity
     */
    public void resolveCollision(Entity e1, Entity e2, int[] b1, int[] b2){
        if (e1.getCurrentRoom() != e2.getCurrentRoom()) return;

        // ATTACK-PLAYER/ENEMY COLLISION HANDLING
        // If entity is an attack and is not friendly and if the second entity is a player, then the player takes damage.
        // If entity is an attack and is friendly and if the second entity is an enemy, then the enemy takes damage.
        
        if (e1 instanceof Attack attack && !attack.getIsFriendly() && e2 instanceof Player player){
            damagePlayer(player, attack);
        }

        else if (e2 instanceof Attack attack && !attack.getIsFriendly() && e1 instanceof Player player){
            damagePlayer(player, attack);
        }
            
        else if (e1 instanceof Attack attack && attack.getIsFriendly() && e2 instanceof Enemy enemy){
            damageEnemy(enemy, attack);
        }
            
        else if (e2 instanceof Attack attack && attack.getIsFriendly() && e1 instanceof Enemy enemy){
            damageEnemy(enemy, attack);
        }
            
        //PLAYER/ENEMY COLLISION HANDLING
        else if (e1 instanceof Player player && e2 instanceof Enemy enemy){
            preventOverlap(player, enemy, b1, b2);
            damagePlayer(player, enemy);
        }

        else if (e2 instanceof Player player && e1 instanceof Enemy enemy){
            preventOverlap(player, enemy, b1, b2);
            damagePlayer(player, enemy);
        }

        //Disable overlap resolution for enemies that summon other enemies
        else if (e1 instanceof Enemy && e2 instanceof Enemy 
        && !(e1 instanceof ConjoinedRats) && !(e2 instanceof ConjoinedRats)
        && !(e1 instanceof FishMonster) && !(e2 instanceof FishMonster))
            preventOverlap(e1, e2, b1, b2);

        else if (e1 instanceof Player p1 && e2 instanceof Player p2){
            //Collision detection for revival system
            int cid1 = p1.getClientId();
            int cid2 = p2.getClientId();

            //Check if living player is already in the availableRevives list to avoid duplicates
            if(!p1.getIsDown() && p2.getIsDown() && availableRevives.get(cid1) == null)
                availableRevives.put(cid1, cid2);
            else if (!p2.getIsDown() && p1.getIsDown() && availableRevives.get(cid2) == null)
                availableRevives.put(cid2, cid1);
        }

        // ITEM collision handling
        else if (e1 instanceof Item item && e2 instanceof Player player){
            applyItem(item, player);
        }
        else if (e2 instanceof Item item && e1 instanceof Player player){
            applyItem(item, player);
        }
    }

    /**
     * Applies the item's effect to the Player on collision. Consumable items
     * immediately applies its effects. Non-consumables are added to the 
     * inventory to be held, if no item is held by the player already.
     * @param item the item picked up
     * @param player the player picking the item up
     */
    private void applyItem(Item item, Player player){
        if (!isItemCollisionAllowed) return;

        item.setOwner(player);
        
        if (item.getIsConsumable()){
            item.applyEffects();
            entities.remove(item);
            SoundManager.getInstance().playPooledSound("equipItem"); // Play sound for consumable pickup
        }
        else {
            if(item.getIsOnPickUpCD()) return;

            if (player.getHeldItem() == null){
                //Reset first time use boolean in thick sweater to avoid immediate application of regen on pickup
                if(item instanceof ThickSweater ts) ts.setIsFirstTimeUse(true);
                player.setHeldItem(item);
                item.applyEffects();
                item.setIsHeld(true);
                entities.remove(item);
                SoundManager.getInstance().playPooledSound("equipItem"); // Play sound for non-consumable pickup
            } 
        }
        
    }

    /**
     * Damages a player when hit by an enemy or an enemy attack. It applies
     * knockback, invincibility frames, and the status effect from the attack
     * if it has any. The damage taken is reduced by the player's defense field
     * @param player the player taking damage
     * @param entity the entity dealing the damage
     */
    private void damagePlayer(Player player, Entity entity){
        //Debouncing condition
        if(player.getIsInvincible()){ // TODO: ADD NOT
            //Calculate damage taken: new health = current health - (damage*(1-(defense/100)))
            double dmgMitigationFactor = (1-(player.getDefense()/100.0));
            if(dmgMitigationFactor < 0) dmgMitigationFactor = 0;
            int dmgReceived = (int) (Math.round(entity.getDamage()*dmgMitigationFactor));
            player.setHitPoints(player.getHitPoints()-dmgReceived);
            applyKnockBack(player, entity);
            player.triggerInvincibility();
            
            if (entity instanceof Attack attack) {
                applyAttackEffectsToPlayer(player, attack);
            } 
        }
    }
    
    /**
     * Applies status effects from attacks to the Player by creating new copies
     * of the StatusEffect stored in Attack
     * @param player the player getting the StatusEffect
     * @param attack the attack giving the StatusEffect
     */
    private void applyAttackEffectsToPlayer(Player player, Attack attack){
        for (StatusEffect se : attack.getAttackEffects()) {
            StatusEffect effectCopy = (StatusEffect) se.copy();
            player.addStatusffect(effectCopy); 
        }
    }

    /**
     * Damages an enemy when hit by an attack. It validates the attack using its 
     * ID to prevent it from damaging the enemy multiple times too quickly. 
     * It applies a knockback to non boss enemies
     * @param enemy the enemy taking damage
     * @param attack the attack dealing damage
     */
    private void damageEnemy(Enemy enemy, Attack attack){
        int id = attack.getId();
        //Debouncing condition
        if(enemy.validateAttack(id)){
            enemy.setHitPoints(enemy.getHitPoints()-attack.getDamage());
            if (!enemy.isBoss) applyKnockBack(enemy, attack);
            enemy.loadAttack(id);
        }
    }

    /**
     * Prevents two entities from overlapping by pushing them apart. It uses
     * normal vectors to determine the direction by which to separate and how 
     * much to separate entities
     * @param e1 the first entity in the collision
     * @param e2 the second entity in the collision
     * @param b1 the hitBoxBounds of the first entity
     * @param b2 the hitBoxBounds of the second entity
     */
    private void preventOverlap(Entity e1, Entity e2, int[] b1, int[] b2){
        //Get the position vectors of both entities
        int[] positionVector1 = new int[2];
        positionVector1[0] = e1.getWorldX() + e1.getWidth()/2;
        positionVector1[1] = e1.getWorldY() + e1.getHeight()/2;

        int[] positionVector2 = new int[2];
        positionVector2[0] = e2.getWorldX() + e2.getWidth()/2;
        positionVector2[1] = e2.getWorldY() + e2.getHeight()/2;

        //Find the unit normal and unit tangent vectors
        int[] normalVector = new int[2];
        normalVector[0] = positionVector2[0] - positionVector1[0];
        normalVector[1] = positionVector2[1] - positionVector1[1];

        double normalVectorMagnitude = Math.sqrt((normalVector[0]*normalVector[0]) + (normalVector[1]*normalVector[1]));
        double[] unitNormal = new double[2];
        unitNormal[0] = normalVector[0]/normalVectorMagnitude;
        unitNormal[1] = normalVector[1]/normalVectorMagnitude;

        //Get the overlaps on both axes
        double overlapX = Math.min(b1[3], b2[3]) - Math.max(b1[2], b2[2]);
        double overlapY = Math.min(b1[1], b2[1]) - Math.max(b1[0], b2[0]);

        //Get the dot product of the overlaps and their respective unit normals
        double overlap = overlapX * Math.abs(unitNormal[0]) + overlapY * Math.abs(unitNormal[1]);

        //Add a minimum overlap threshold in order to minimize constant correction
        double overlapThreshold = 5;
        if (overlap > overlapThreshold){
            //Divide overlap by an arbitrary number to smoothen out the visual resolution of the collision
            double resolutionFactor = overlap / 8;
            int e1X = ((int)(e1.getWorldX() - unitNormal[0] * resolutionFactor));
            int e1Y = ((int)(e1.getWorldY() - unitNormal[1] * resolutionFactor));
            int e2X = ((int)(e2.getWorldX() + unitNormal[0] * resolutionFactor));
            int e2Y = ((int)(e2.getWorldY() + unitNormal[1] * resolutionFactor));

            e1.setPosition(e1X, e1Y);
            e2.setPosition(e2X, e2Y);
        }

        e1.matchHitBoxBounds();
        e2.matchHitBoxBounds();

    }

    /**
     * Applies a knockback to an entity when hit by another entity.
     * Calculates a direction based on the position of the two entities
     * and pushes the target away from the attacker.
     * @param target the entity being knocked back
     * @param attacker the entity causing the knockback
     */
    private void applyKnockBack(Entity target, Entity attacker) {

        int[] entityPosition = target.getPositionVector();
        int[] attackPosition = attacker.getPositionVector();

        int[] normalVector = getNormalVector(entityPosition, attackPosition);

        double normalVectorMagnitude = Math.sqrt((normalVector[0]*normalVector[0]) + (normalVector[1]*normalVector[1]));
        if (normalVectorMagnitude == 0) normalVectorMagnitude = 1;
        
        double[] unitNormal;

        if (normalVectorMagnitude == 0) {
            unitNormal = new double[] {0, -1};
        } else unitNormal = getUnitNormal(normalVector, normalVectorMagnitude);

        int knockBackStrength = 24;
        int newX = (int) (target. getWorldX() - unitNormal[0] * knockBackStrength);
        int newY = (int)(target.getWorldY() - unitNormal[1] * knockBackStrength);

        target.setPosition(newX, newY);
        target.matchHitBoxBounds();
    }

    /**
     * Processes all inputs from clients, keyboard and mouse. Keyboard inputs
     * are used for item pick up and movement. Mouse inputs are used for 
     * attacks.
     */
    private void processInputs(){
        keyInputQueue.forEach((key, cid) ->{
            // System.out.println("Processing input: " + key + "," + cid);
            Player player = (Player) getPlayerFromClientId(cid);

            //Restrain player if downed
            if (!player.getIsDown()) {
                if (key == 'Q'){
                    //Remove item effects and drop it on the ground
                    Item heldItem = player.getHeldItem();
                    if (heldItem != null){
                        //Call background state methods on heldItem
                        heldItem.removeEffects();
                        heldItem.setIsHeld(false);
                        heldItem.triggerPickUpCD();
                        heldItem.triggerDespawnTimer();
                        
                        //Visual drop mechanics
                        heldItem.setCurrentRoom(player.getCurrentRoom());
                        heldItem.setWorldX(player.getWorldX());
                        heldItem.setWorldY(player.getWorldY());
                        heldItem.matchHitBoxBounds();
                        addEntity(heldItem);
                    }
                }
                player.update(key);   
            }        
            
        });
        keyInputQueue.clear();

        clickInputQueue.forEach((clickInput) -> {processClickInput(clickInput.mouseButton, clickInput.x, clickInput.y, clickInput.cid);});
        clickInputQueue.clear();
    }

    /**
     * Adds a key input to the queue for processing
     * @param input the character of the pressed keyboard input
     * @param cid the client ID of the player sending the input
     */
    public void loadKeyInput(char input, int cid){
        keyInputQueue.put(input, cid);
    }

    /**
     * Adds a mouse click input to the queue for processing
     * @param mouseButton "L" for left-click and "R" for right-click
     * @param x the x-coordinate of the click
     * @param y the y-coordinate of the click
     * @param cid the client ID of the Player sending the click
     */
    public void loadClickInput(String mouseButton, int x, int y, int cid){
        clickInputQueue.add(new ClickInput(mouseButton, x, y, cid));
    }

    /**
     * Creates an Player Attack based on the direction of the click input
     * relative to the player and the type of player.
     * @param mouseButton "L" if a left-click, "R" if a right-click
     * @param clickX the x-coordinate of the click
     * @param clickY the y-coordinate of the click
     * @param cid the client ID of the player who sent the click
     */
    public void processClickInput(String mouseButton, int clickX, int clickY, int cid){
        if (mouseButton.equals("R")) return;

        Player originPlayer = (Player) getPlayerFromClientId(cid);

            //Debouncing constraints
            if(originPlayer.getIsOnCoolDown() || originPlayer.getIsDown()) return;
            
            // Only create a new attack if the player is not on cooldown
            if (!originPlayer.getIsOnCoolDown()) {
                Thread runAttack = new Thread(){
                    @Override
                    public void run(){
                        originPlayer.triggerCoolDown();
                        originPlayer.runAttackFrames();

                        int attackDamage = originPlayer.getDamage();
                        int frameWidth = 800;
                        int frameHeight = 600;
                        int centerX = frameWidth/2;
                        int centerY = frameHeight/2;
                        
                        //Get a point a set distance away from the center of the screen in the direction of the click
                        int vectorX = clickX - centerX;
                        int vectorY = clickY - centerY;  
                        int distance = 20;
                        double normalizedVector = Math.sqrt((vectorX*vectorX)+(vectorY*vectorY));

                        //Avoids 0/0 division edge case
                        if (normalizedVector == 0) normalizedVector = 1; 
                        double normalizedX = vectorX/normalizedVector;
                        double normalizedY = vectorY/normalizedVector;
                        int attackScreenX = (int) (centerX + distance*normalizedX);
                        int attackScreenY = (int) (centerY + distance*normalizedY);

                        int playerScreenX = frameWidth/2 - originPlayer.getWidth()/2;
                        int playerScreenY = frameHeight/2 - originPlayer.getHeight()/2;

                        int worldX = (originPlayer.getWorldX() - playerScreenX) + attackScreenX;
                        int worldY = (originPlayer.getWorldY() - playerScreenY) + attackScreenY;

                        Attack playerAttack = null;
                        int attackHeight;
                        int attackWidth;

                        if (originPlayer.getIdentifier().equals(NetworkProtocol.FASTCAT)){
                            attackWidth = 40;
                            attackHeight = 40;
                            playerAttack = new PlayerSlash(cid, originPlayer, worldX-attackWidth/2, worldY - attackHeight/2, 
                            attackDamage, true);
                        } 
                        else if (originPlayer.getIdentifier().equals(NetworkProtocol.HEAVYCAT)){
                            attackWidth = 80;
                            attackHeight = 80;
                            playerAttack = new PlayerSmash(cid, originPlayer, worldX-attackWidth/2, worldY - attackHeight/2, 
                            attackDamage, true);
                        }
                        else if (originPlayer.getIdentifier().equals(NetworkProtocol.GUNCAT)){
                            attackWidth = 16;
                            attackHeight = 16;
                            playerAttack = new PlayerBullet(cid, originPlayer, worldX-attackWidth/2, worldY - attackHeight/2, 
                            normalizedX, normalizedY, attackDamage, true);
                        }
                        
                        if(playerAttack != null){
                            playerAttack.setCurrentRoom(originPlayer.getCurrentRoom());
                            addEntity(playerAttack); 
                        }
                    }
                };
                runAttack.start();
            }
        }        

  
    /**
     * Gets the normal vector created from two vectors by subtracting their x and
     * y-components.
     * @param v1 the first vector
     * @param v2 the second vector
     * @return an int array with index 0 as the x-component of the normal vector and
     * index 1 as the y-component of the normal vector
     */
    private int[] getNormalVector(int[] v1, int[] v2){
        int[] normalVector = new int[2];
        normalVector[0] = v2[0] - v1[0];
        normalVector[1] = v2[1] - v1[1];

        return normalVector;
    }
    
    /**
     * Gets the unit normal of a vector by dividing each of its components by
     * the normalVectorMagnitude
     * @param normalVector the normalVector to get the unit vector of
     * @param normalVectorMagnitude the magnitude of the normalVector, how long it is
     * @return an int array with index 0 as the x-component of the unit normal vector
     * and index 1 as the y-component of the unit normal vector.
     */
    private double[] getUnitNormal(int[] normalVector, double normalVectorMagnitude) {
        double[] unitNormal = new double[2];
        unitNormal[0] = normalVector[0] / normalVectorMagnitude;
        unitNormal[1] = normalVector[1] / normalVectorMagnitude;

        return unitNormal;
    }

    /**
     * Sets the value of the connectedPlayers ArrayList to the passed argument
     * @param connectedPlayers the ArrayList of ConnectedPlayers to set connectedPlayers to
     */
    public void setConnectedPlayers(ArrayList<GameServer.ConnectedPlayer> connectedPlayers) {
        this.connectedPlayers = connectedPlayers;
    }
    
    /**
     * Increments the static gameLevel field if it is less than MAX_LEVEL
     */
    public void incrementGameLevel(){
        if (gameLevel < MAX_LEVEL) gameLevel++;
    }

    /**
     * Creates a new dungeon and updates user player position
     * in the new starting room.
     */
    public DungeonMap generateNewDungeon(){
        dungeonMap = new DungeonMap(gameLevel);
        dungeonMap.generateRooms();
        return dungeonMap;
    }

    /**
     * Builds a string that is the serialized form of the map data.
     * 
     * @return a string in the form
     * M:{RoomCount}|R:{roomId},{roomX},{roomY},{isStart},{isEnd}|D:{doorId},{doorX},{doorY},{direction},{roomAId},{roomBId}|...|{startingRoomId}
     */
    public String getMapData() {
        return dungeonMap.serialize();
    }

    public DungeonMapDeserializeResult parseMapData(String message) {
        return dungeonMap.deserialize(message);
    }

    /**
     * Builds a string that serializes all the asset data (player and entities).
     * In the form
     * ClientId|P:clientId,playerX,playerY,currentRoom|E:entityIdentifier,entity1X,entity1Y,entityIdentifier,entity2x,entity2Y...|
     * 
     * @param cid id of the client
     * @return a serialized string containing the data of all entities
     */
    public String getAssetsData(int cid) {
        StringBuilder sb = new StringBuilder();

        sb.append(cid).append(NetworkProtocol.DELIMITER);

        // User Player String: P$:clientId,playerX,playerY 
        Player userPlayer = (Player) getPlayerFromClientId(cid);
        String userPlayerData = userPlayer.getAssetData(true);
        if (userPlayerData == null) return null;

        //UI elements
        Item heldItem = userPlayer.getHeldItem();
        String heldItemIdentifier = "0";
        if (heldItem != null) heldItemIdentifier = heldItem.getIdentifier();

        sb.append(userPlayer.getXPBarPercent()).append(NetworkProtocol.DELIMITER)
        .append(userPlayer.getCurrentLvl()).append(NetworkProtocol.DELIMITER).
        append(heldItemIdentifier).append(NetworkProtocol.DELIMITER)
        .append(bossHPPercent).append(NetworkProtocol.DELIMITER);


        if (userPlayerData.startsWith(NetworkProtocol.ROOM_CHANGE)) {
            // Handle room change logic here on the server side
            // System.out.println("String in room change of getassetdata: " + userPlayerData); // What does this say
            sb.append(handleRoomTransition(userPlayer, userPlayerData));
        } else {
            // Normal data without room change
            sb.append(NetworkProtocol.USER_PLAYER)
            .append(userPlayerData);
        }

        // Loop through entities array
        for (Entity entity : entities) {
            if (entity == null) continue;
            if ((entity instanceof Player) && (entity != userPlayer)) {
                // Player String: P:clientId,playerX,playerY
                sb.append(NetworkProtocol.PLAYER).append((entity.getAssetData(false)))
                .append(NetworkProtocol.DELIMITER);
            } else if (!(entity instanceof Player)) {
                // NPCs ex. G:B,id,x,y,currentRoomId| => Rat with id at currentRoomId (x,y)
                sb.append(NetworkProtocol.ENTITY)
                .append(entity.getAssetData(false));  
            } 
        }
        return sb.toString();
    }


    /**
     * Called when the userPlayer's getAssetData() string indicates a room change.
     * Allows the program to handle the userPlayer's room change logic on the server side
     * by setting the userPlayer's new room and coordinates.
     * @param userPlayer the Player object of the user who indicated the room change
     * @param userPlayerData the String returned by the getAssetData() method of the userPlayer
     * @return a serialized String to be sent to the client's data handler indicating the userPlayer's new position.
     * The returned string is in the form: P$:clientId,newx,newY,newRoomId|
     */
    private String handleRoomTransition(Player userPlayer, String userPlayerData) {
            StringBuilder sb = new StringBuilder();

            try {
                // Split the userPlayerData string by ","
                String[] dataParts = userPlayerData.split(NetworkProtocol.SUB_DELIMITER);
                
                // Extract data from it
                String identifier = dataParts[0].substring(NetworkProtocol.ROOM_CHANGE.length());
                int clientId = Integer.parseInt(dataParts[1]);
                int newX = Integer.parseInt(dataParts[2]);
                int newY = Integer.parseInt(dataParts[3]);
                int hp = Integer.parseInt(dataParts[4]);
                int newRoomId = Integer.parseInt(dataParts[5]);
                int currSprite = Integer.parseInt(dataParts[6]);
                int zIndex = Integer.parseInt(dataParts[7]);

                // Use the data to set relevant fields
                Room newRoom = dungeonMap.getRoomFromId(newRoomId);
                
                // Handle case where new room is null or invalid
                if (newRoom == null) {
                    // Keep player in current room if destination is invalid
                    newRoom = currentRoom;
                    newX = userPlayer.getWorldX();
                    newY = userPlayer.getWorldY();
                }

                userPlayer.setPosition(newX, newY);
                userPlayer.setCurrentRoom(newRoom);
                userPlayer.setHitPoints(hp);
                userPlayer.setzIndex(zIndex);
                currentRoom = newRoom;
                currentRoom.setVisited(true);
                handleSpawnersOnRoomChange(newRoom);
                
                // Only play boss music if it's an end room AND not cleared
                if (newRoom.isEndRoom() && !newRoom.isCleared()) {
                    SoundManager.getInstance().playMusic("bossMusic");
                } else if (newRoom.isEndRoom() && newRoom.isCleared()) {
                    // If it's a cleared end room, make sure boss music is stopped
                    SoundManager.getInstance().stopSound("bossMusic");
                }
                
                if (!currentRoom.isStartRoom() && !currentRoom.isCleared()) newRoom.closeDoors();

                // Build String to be returned
                sb.append(NetworkProtocol.USER_PLAYER) 
                .append(identifier).append(NetworkProtocol.SUB_DELIMITER)
                .append(clientId).append(NetworkProtocol.SUB_DELIMITER)
                .append(newX).append(NetworkProtocol.SUB_DELIMITER)
                .append(newY).append(NetworkProtocol.SUB_DELIMITER)
                .append(hp).append(NetworkProtocol.SUB_DELIMITER)
                .append(newRoom.getRoomId()).append(NetworkProtocol.SUB_DELIMITER)
                .append(currSprite).append(NetworkProtocol.SUB_DELIMITER)
                .append(zIndex).append(NetworkProtocol.DELIMITER);

                // Update minimap for the player
                miniMapManager.discoverRoom(clientId, newRoom);
                
                // Get visible rooms for the minimap update
                HashMap<Room, Point> visibleRooms = miniMapManager.getVisibleRooms(clientId);
                 
                // Add minimap data to the response
                StringBuilder minimapData = new StringBuilder();
                minimapData.append(NetworkProtocol.MINIMAP_UPDATE);
                for (Map.Entry<Room, Point> entry : visibleRooms.entrySet()) {
                    minimapData.append(entry.getKey().getRoomId())
                               .append(NetworkProtocol.SUB_DELIMITER)
                               .append(entry.getValue().x)
                               .append(NetworkProtocol.SUB_DELIMITER)
                               .append(entry.getValue().y)
                               .append(NetworkProtocol.MINIMAP_DELIMITER);
                } minimapData.append(NetworkProtocol.DELIMITER);
                
                // System.out.println("String built in handleRoomTransition: " + sb.toString() + minimapData.toString());
                return sb.toString() + minimapData.toString();
            } catch (NumberFormatException e) {
                // If any error occurs during room transition, keep player in current room
                System.err.println("Error during room transition: " + e.getMessage());
                return null;
            }
    }

    public void handleSpawnersOnRoomChange(Room next){
        if (next.getMobSpawner() != null && (!next.getMobSpawner().isSpawning())) next.getMobSpawner().spawn();
    }

    /**
     * Searches through the entities arrayList to look for a player object with the provided clientId
     * @param cid the clientId of the connectedPlayer
     * @return the Player object with the corresponding clientId
     */
    public Entity getPlayerFromClientId(int cid) {
        for (Entity entity : entities) {
            if (entity instanceof Player player && player.getClientId() == cid)
                return entity;
        }
        return null;
    }

    /**
     * Gets the value of userPlayerIndex
     * @return the int value of userPlayerIndex
     */
    public int getUserPlayerIndex() {
        return userPlayerIndex;
    }

    /**
     * Sets the value of userPlayerIndex to the passed argument
     * @param userPlayerIndex the int value to set userPlayerIndex to
     */
    public void setUserPlayerIndex(int userPlayerIndex) {
        this.userPlayerIndex = userPlayerIndex;
    }

    /**
     * Updates the value of userPlayerIndex by getting the index of 
     * the Player in the entities ArrayList with the client ID 
     * value cid
     * @param cid the client ID value of the userPlayer
     */
    public void updateUserPlayerIndex(int cid) {
        userPlayerIndex = entities.indexOf(getPlayerFromClientId(cid));
    }

    /**
     * Gets the current DungeonMap stored in dungeonMap
     * @return a DungeonMap object that is the value of dungeonMap
     */
    public DungeonMap getDungeonMap() {
        return dungeonMap;
    }

    /**
     * Sets the reference in dungeonMap to the passed argument
     * @param dungeonMap the reference to set dungeonMap to
     */
    public void setDungeonMap(DungeonMap dungeonMap) {
        this.dungeonMap = dungeonMap;
    }

    /**
     * Gets the Room reference of currentRoom
     * @return a Room reference to currentRoom
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Sets currentRoom to the passed Room reference
     * @param currentRoom the Room reference to set currentRoom to
     */
    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public void setIsItemCollisionAllowed(boolean b){
        isItemCollisionAllowed = b;
    }

    /**
     * Adds an entity to the entities ArrayList. Sets its currentRoom
     * field value to the ServerMaster's currentRoom if it is not null
     * If the entity is a Player, it increments playerNum.
     * @param e
     */
    public void addEntity(Entity e) {
        if (e.getCurrentRoom() == null) e.setCurrentRoom(currentRoom);
        if(e instanceof Player) playerNum++;
        entities.add(e);
    }

    /**
     * Removed an entity from the entities ArrayList
     * @param e the entity to be removed
     */
    public void removeEntity(Entity e) {
        entities.remove(e);
    }

    /**
     * Gets the entities CopyOnWriteArrayList
     * @return a CopyOnWriteArrayList containing all the game's entities
     */
    public CopyOnWriteArrayList<Entity> getEntities() {
        return entities;
    }

    /**
     * Gets the value of gameLevel
     * @return the int value of gameLevel
     */
    public int getGameLevel() {
        return gameLevel;
    }   

    public ItemsHandler getItemsHandler(){
        return itemsHandler;
    }

    /**
     * Stores the x and y coordinates, as well as the client id of the click input.
     */
    private static class ClickInput{
        public int x, y, cid;
        public String mouseButton;

        /**
         * Creates an instance of ClickInput with fields set to the parameters 
         * @param mouseButton "L" for left-click and "R" for right-click
         * @param x the x-coordinate
         * @param y the y-coordinate
         * @param cid the client ID of the Player initiating the click
         */
        public ClickInput(String mouseButton, int x, int y, int cid){
            this.mouseButton = mouseButton;
            this.x = x;
            this.y = y;
            this.cid = cid;
        }
    } 

    private void resetSoundFlags() {
        hasPlayedGameOverSound = false;
        hasPlayedRevivingSound = false;
        hasPlayedReviveSuccessSound = false;
    }

    /**
     * Gets the MiniMapManager instance
     * @return the MiniMapManager instance
     */
    public MiniMapManager getMiniMapManager() {
        return miniMapManager;
    }
}
