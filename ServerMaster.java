import java.util.*;
import java.util.concurrent.*;

public class ServerMaster {
    private CopyOnWriteArrayList<Entity> entities;
    private DungeonMap dungeonMap;
    private ItemsHandler itemsHandler;
    private int userPlayerIndex;
    private Room currentRoom;
    private static int gameLevel;
    private static final int MAX_LEVEL = 7;
    private ConcurrentHashMap<Character, Integer> keyInputQueue;
    private ConcurrentHashMap<Integer, Integer> availableRevives;
    private ArrayList<GameServer.ConnectedPlayer> connectedPlayers;
    private ArrayList<ClickInput> clickInputQueue;
    private int playerNum;
    private int downedPlayersNum;
    private static ServerMaster singleInstance = null;
    private int bossHPPercent;

    private ServerMaster(){
        playerNum = 0;
        downedPlayersNum = 0;
        gameLevel = 0;
        entities = new CopyOnWriteArrayList<>();
        userPlayerIndex = -1;
        dungeonMap = new DungeonMap(gameLevel);
        dungeonMap.generateRooms();
        currentRoom = dungeonMap.getStartRoom();
        //-----------------------------//
        keyInputQueue = new ConcurrentHashMap<>();
        availableRevives = new ConcurrentHashMap<>();
        clickInputQueue = new ArrayList<>();
        //-----------------------------//
        itemsHandler = new ItemsHandler();

    }

    public static synchronized ServerMaster getInstance() {
        if (singleInstance == null) singleInstance = new ServerMaster();
        return singleInstance;
    }

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
                    System.out.println("GAME OVER");
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
                    }

                    isInContact = true;
                    break;
                } 
            }

            //If the other player has moved away from the downed player
            if(!isInContact){
                player.setIsReviving(false);
                //Insert UI indicators
            }

            //After the revivaltime and without the living player moving away, revive the downed player with one health
            if(player.getIsReviving() && player.getIsRevived()){
                player.setHitPoints(1);
                player.setIsDown(false);

                //Remove death sprite
                player.setCurrSprite(3);
                
            }
        }
    
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
        incrementGameLevel();
        String doorData = addExitRoomGoingToNewDungeon();
        StringBuilder sb = new StringBuilder();
        sb.append(NetworkProtocol.BOSS_KILLED).append(doorData); //BK:D:doorId,x,y,direction,roomAId,roomBId
        sendMessageToClients(sb.toString());
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
        System.out.println("Direction: " + direction);

        Door d = currentRoom.createDoorFromDirection(direction);
        d.setIsExitToNewDungeon(true);
        d.setIsOpen(true);
        d.setRoomB(currentRoom);
        currentRoom.addDoorToArrayList(d);
        
        return d.serialize();
    }

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
        // TODO: TRANSITION TO GAME FINISH SCREEN
        
        ArrayList<Player> players = getAllPlayers();
        entities.clear();

        DungeonMap newDungeon = generateNewDungeon();
        dungeonMap = newDungeon;
        currentRoom = newDungeon.getStartRoom();

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

        for (GameServer.ConnectedPlayer cp : connectedPlayers) {
            cp.promptAssetsThread(getAssetsData(cp.getCid()));
        }

        // System.out.println("Message sent " + playersData.toString());        
        // sendMessageToClients(playersData.toString());
    }
    
    private void sendMessageToClients(String message){
        // System.out.println("Message in sendMessageToClients(): " + message);
        // System.out.println("Number of connected clients: " + connectedPlayers.size());
        for (GameServer.ConnectedPlayer cp : connectedPlayers) {
            cp.promptAssetsThread(message);
        }
    }

    // Checks for collisions between all objects inside the entity ArrayList
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

    private void applyItem(Item item, Player player){
        item.setOwner(player);
        
        if (item.getIsConsumable()){
            item.applyEffects();
            entities.remove(item);
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
            } 
        }
        
    }

    //Players generate i-frames when damaged
    private void damagePlayer(Player player, Entity entity){
        //Debouncing condition
        if(!player.getIsInvincible()){
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
    
    private void applyAttackEffectsToPlayer(Player player, Attack attack){
        for (StatusEffect se : attack.getAttackEffects()) {
            StatusEffect effectCopy = (StatusEffect) se.copy();
            player.addStatusffect(effectCopy); 
        }
    }

    //Enemy can only take one instance of damage per attack
    private void damageEnemy(Enemy enemy, Attack attack){
        int id = attack.getId();
        //Debouncing condition
        if(enemy.validateAttack(id)){
            enemy.setHitPoints(enemy.getHitPoints()-attack.getDamage());
            if (!enemy.isBoss) applyKnockBack(enemy, attack);
            enemy.loadAttack(id);
        }
    }

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

        clickInputQueue.forEach((clickInput) -> {processClickInput(clickInput.x, clickInput.y, clickInput.cid);});
        clickInputQueue.clear();
    }

    public void loadKeyInput(char input, int cid){
        keyInputQueue.put(input, cid);
        // System.out.println("Key: " + input);
        // System.out.println("cid: " + cid);
    }

    public void loadClickInput(int x, int y, int cid){
        clickInputQueue.add(new ClickInput(x, y, cid));
    }

    public void processClickInput(int clickX, int clickY, int cid){
        // System.out.println("Processing clickc input for player: " + cid);

        Player originPlayer = (Player) getPlayerFromClientId(cid);

        //Debouncing constraints
        if(originPlayer.getIsOnCoolDown() || originPlayer.getIsDown()) return;
        
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
        

        // System.out.println("Created PlayerSlash: " + playerAttack.getId() + " at (" + playerAttack.getWorldX() + ", " + playerAttack.getWorldX() + ")");
    }

    private int[] getNormalVector(int[] v1, int[] v2){
        int[] normalVector = new int[2];
        normalVector[0] = v2[0] - v1[0];
        normalVector[1] = v2[1] - v1[1];

        return normalVector;
    }

    private double[] getUnitNormal(int[] normalVector, double normalVectorMagnitude) {
        double[] unitNormal = new double[2];
        unitNormal[0] = normalVector[0] / normalVectorMagnitude;
        unitNormal[1] = normalVector[1] / normalVectorMagnitude;

        return unitNormal;
    }

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
            if ((entity instanceof Player) && (entity != userPlayer)) {
                // Player String: P:clientId,playerX,playerY
                sb.append(NetworkProtocol.PLAYER).append((entity.getAssetData(false)))
                .append(NetworkProtocol.DELIMITER);
            } else if (!(entity instanceof  Player)) {
                // NPCs ex. G:B,id,x,y,currentRoomId| => Rat with id at currentRoomId (x,y)
                if (entity == null) continue;
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

            // Split the userPlayerData string by ","
            String[] dataParts = userPlayerData.split(NetworkProtocol.SUB_DELIMITER);
            
            // Extract data from it
            String identifier = dataParts[0].substring(NetworkProtocol.ROOM_CHANGE.length());
            // System.out.println("identifier:" + identifier);
            int clientId = Integer.parseInt(dataParts[1]);
            int newX = Integer.parseInt(dataParts[2]);
            int newY = Integer.parseInt(dataParts[3]);
            int hp = Integer.parseInt(dataParts[4]);
            int newRoomId = Integer.parseInt(dataParts[5]);
            int currSprite = Integer.parseInt(dataParts[6]);
            int zIndex = Integer.parseInt(dataParts[7]);

            // Use the data to set relevant fields
            Room newRoom = dungeonMap.getRoomFromId(newRoomId);
            userPlayer.setPosition(newX, newY);
            userPlayer.setCurrentRoom(newRoom);
            userPlayer.setHitPoints(hp);
            userPlayer.setzIndex(zIndex);
            currentRoom = newRoom;
            handleSpawnersOnRoomChange(newRoom);
            if (!currentRoom.isStartRoom() && !currentRoom.isCleared()) newRoom.closeDoors();

            // Build String to be returned
            sb.append(NetworkProtocol.USER_PLAYER) 
            .append(identifier).append(NetworkProtocol.SUB_DELIMITER)
            .append(clientId).append(NetworkProtocol.SUB_DELIMITER)
            .append(newX).append(NetworkProtocol.SUB_DELIMITER)
            .append(newY).append(NetworkProtocol.SUB_DELIMITER)
            .append(hp).append(NetworkProtocol.SUB_DELIMITER)
            .append(newRoomId).append(NetworkProtocol.SUB_DELIMITER)
            .append(currSprite).append(NetworkProtocol.SUB_DELIMITER)
            .append(zIndex).append(NetworkProtocol.DELIMITER);
            // System.out.println("String returned by handleRoomTransition: " + sb.toString());

            return sb.toString();
    }

    public void handleSpawnersOnRoomChange(Room next){
        if (next.getMobSpawner() != null && (!next.getMobSpawner().isSpawning())) next.getMobSpawner().spawn();

        if (next.isEndRoom() && next.getMobSpawner().isAllKilled()) incrementGameLevel();
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

    public int getUserPlayerIndex() {
        return userPlayerIndex;
    }

    public void setUserPlayerIndex(int userPlayerIndex) {
        this.userPlayerIndex = userPlayerIndex;
    }

    public void updateUserPlayerIndex(int cid) {
        userPlayerIndex = entities.indexOf(getPlayerFromClientId(cid));
    }

      public DungeonMap getDungeonMap() {
        return dungeonMap;
    }

    public void setDungeonMap(DungeonMap dungeonMap) {
        this.dungeonMap = dungeonMap;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public void addEntity(Entity e) {
        if (e.getCurrentRoom() == null) e.setCurrentRoom(currentRoom);
        if(e instanceof Player) playerNum++;
        entities.add(e);
    }

    public void removeEntity(Entity e) {
        entities.remove(e);
    }

    public CopyOnWriteArrayList<Entity> getEntities() {
        return entities;
    }

    public int getGameLevel() {
        return gameLevel;
    }

    public Entity getAttackFromClientId(int cid){
        for (Entity entity : entities) {
            if (entity instanceof Attack && entity.getClientId() == cid) return entity;
        }
        return null;
    }

    // Stores the x and y coordinates, as well as the client id of the click input.
    private static class ClickInput{
        public int x, y, cid;

        public ClickInput(int x, int y, int cid){
            this.x = x;
            this.y = y;
            this.cid = cid;
        }
    } 
}
