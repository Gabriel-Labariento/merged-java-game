import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.awt.Point;

/**     
        The GameClient serves as the network and input handler
        for connections between client (Player) and server. It manages
        connections to the server, serializes and sends player input data,
        and receives and parses serialized entities and map data sent by
        the server. 

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

public class GameClient {
    public static final int TRANSFERINTERVAL = 16;
    private final ClientMaster clientMaster;
    private Socket theSocket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private int clientId;
    private final HashMap<String, Boolean> keyMap;
    private String mouseButton;
    private int clickedX;
    private int clickedY;
    private final ScheduledExecutorService sendInputsScheduler;
    private GameServer gs;

    /**
     * Creates a GameClient and binds it to a clientMaster instance that handles
     * client-side game state. It also creates a singleThreadScheduledExecutor assigned
     * to sendInputs Scheduler for sending Player inputs to the server. It also creates
     * a keyMap to assign keys Q, W, A, S, and D to false (not pressed)
     * @param clientMaster
     */
    public GameClient(ClientMaster clientMaster){
        this.clientMaster = clientMaster;
        sendInputsScheduler = Executors.newSingleThreadScheduledExecutor();

        keyMap = new HashMap<>();
        keyMap.put("Q", false);
        keyMap.put("W", false);
        keyMap.put("A", false);
        keyMap.put("S", false);
        keyMap.put("D", false);
    }

    /**
     * Closes sockets when game exits
     */
    public void closeSocketsOnShutdown(){
        Runtime.getRuntime().addShutdownHook(new Thread (()-> {
            try {
                theSocket.close();
            } catch (IOException ex) {
                System.err.println("IOException from closeSocketOnShutdown() method");
            } catch (NullPointerException ex2){
                System.err.println("NullPointerException from closeSocketOnShutdown() method");
            }
        }));
    }

    /**
     * Establishes a connection to the Server. Sends the type of Player who joined.
     * Initiates network threads.
     * @param ipAddress the host's IP address
     * @param portNum the port to connect to
     * @param playerType the type of Player chosen by the connecting client: Ranged, Tank, or Melee (Fast)
     */
    public void connectToServer(String ipAddress, int portNum, String playerType){
        try {
            System.out.println("ATTEMPTING TO CONNECT TO SERVER...");
            theSocket = new Socket(ipAddress, portNum);
            
            //Disable Nagle's buffering algorithm: basically reduces latency
            theSocket.setTcpNoDelay(true);
            System.out.println("CONNECTION SUCCESSFUL!");

            dataIn = new DataInputStream(theSocket.getInputStream());
            dataOut = new DataOutputStream(theSocket.getOutputStream());

            sendPreGameData(playerType);
            startAssetsThread();
            startInputsThread();
            // startRenderLoop();

        } catch (IOException ex) {
            System.out.println("IOException from connectToServer() method");
        }
    }

    /**
     * This method allows a client to host the game
     */
    public void hostServer(){
        gs = new GameServer();
        gs.waitForConnections();   
        gs.startGameLoop();
        gs.closeSocketsOnShutdown();
    }
    
    /**
     * Gets the server's IP address when hosting a gameServer 
     * @return a String: the host's IPv4 address
     */
    public String getServerIP(){
        //Get the local machine's IPv4 address
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface) e.nextElement();

                //Check if interface is down or is only visible to the host
                if (!networkInterface.isUp() || networkInterface.isLoopback())
                    continue;

                Enumeration<InetAddress> a = networkInterface.getInetAddresses();
                while (a.hasMoreElements()) {
                    InetAddress inetAddress = a.nextElement();

                    // Check if address is an Ipv4 address and is only visible to host
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("SocketException at GameClient's getServerIP() method");
        } 
        return null;  
    }

    /**
     * Gets the port number of the gameServer
     * @return an int pertaining to the gameServer's port number
     */
    public int getServerPort(){
        return gs.getPort();
    }

    /**
     * Sends the byte array representation of the client's chosen PlayerType through
     * the DataOutputStream.
     * @param playerType the String that represents the client's chosen Player
     */
    private void sendPreGameData(String playerType){
        try {
            byte[] preGameDataBytes = playerType.getBytes("UTF-8");
            dataOut.writeInt(preGameDataBytes.length);
            dataOut.write(preGameDataBytes);
            
        } catch (IOException ex) {
            System.out.println("IOException from startInputsThread");
        } 
    }

    /**
     * Reads through the DataInputStream through a byte buffer and parses data
     * depending on what the data String starts with as assigned by Network Protocol.
     * It handles parsing map data, level change and boss clearing, game over sequence,
     * and entity (player and non-player) data. 
     */
    private void startAssetsThread(){
        Thread receiveAssetsThread = new Thread(){
            @Override
            public void run(){
                while (true){
                    try {
                        int byteLength = dataIn.readInt();
                        byte[] buffer = new byte[byteLength];
                        dataIn.readFully(buffer);
                        String receivedMessage = new String(buffer, "UTF-8");
                        // System.out.println("ReceivedMessage: " + receivedMessage);
                        // If the received message starts with the protocol identifier for map data, parse the map data
                        if (receivedMessage.startsWith(NetworkProtocol.MAP_DATA)) {
                            parseMapData(receivedMessage);
                            clientMaster.getMiniMap().update(null, null);
                        } else if (receivedMessage.startsWith(NetworkProtocol.BOSS_KILLED)) {
                            parseBossKilledData(receivedMessage);
                        } else if (receivedMessage.startsWith(NetworkProtocol.GAME_OVER)) {
                            initiateGameOver();
                        } else if (receivedMessage.startsWith(NetworkProtocol.LEVEL_CHANGE)) {
                            clientMaster.getEntities().clear();
                            clientMaster.getMiniMap().update(null, null);
                            String mapData = receivedMessage.substring(NetworkProtocol.LEVEL_CHANGE.length());  // Receives a string containing map and player data
                            parseMapData(mapData);
                        } else {
                            synchronized (clientMaster.getEntities()) {                                         // Synchronize entities arraylist to remove flickering
                                clientMaster.getEntities().clear();
                                parseEntitiesData(receivedMessage);
                            }
                        }
                    } catch (IOException ex){
                        System.out.println("IOEception from receiveAssetsThread");
                    }
                }}};
        receiveAssetsThread.start();
    }
    
    /**
     * Sets the isGameOver boolean field of the assigned clientMaster
     * to true
     */
    private void initiateGameOver(){
        clientMaster.setIsGameOver(true);
    }

    /**
     * Parses a serialized string expected to be in the form:
     * ClientId|XPBarPercent|UserLvl|heldItemIdentifier|BossHPBarPercent|
     * P$:playerX,playerY,playerHealth,playerRoomId,playerSprite,playerZindex|
     * E:entity1X,entity1Y,entity2x,entity2Y...|
     * @param message a serialized string sent by ServerMaster that contains all existing entity data
     */
    private void parseEntitiesData(String message){
        // System.out.println(message);
        String[] messageParts = message.split("\\" + NetworkProtocol.DELIMITER); // Have to use \\ to escape. Turns out "|" is special for java
        this.clientId = Integer.parseInt(messageParts[0]);
        clientMaster.setXPBarPercent(Integer.parseInt(messageParts[1]));
        clientMaster.setUserLvl(Integer.parseInt(messageParts[2]));
        clientMaster.setHeldItemIdentifier(messageParts[3]);
        clientMaster.setBossHPBarPercent(Integer.parseInt(messageParts[4]));

        for (String part : messageParts) {
            if (part.startsWith(NetworkProtocol.USER_PLAYER)) {
                String[] playerData = part.substring(NetworkProtocol.USER_PLAYER.length()).split(NetworkProtocol.SUB_DELIMITER);
                String identifier = playerData[0];
                int playerId = Integer.parseInt(playerData[1]);
                int playerX = Integer.parseInt(playerData[2]);
                int playerY = Integer.parseInt(playerData[3]);
                int playerHealth = Integer.parseInt(playerData[4]);
                int playerRoomId = Integer.parseInt(playerData[5]);
                int currsprite = Integer.parseInt(playerData[6]);
                int playerZIndex = Integer.parseInt(playerData[7]);
        
                // System.out.println(" user Player loaded");
                try {
                    Room currentRoom = clientMaster.getRoomById(playerRoomId);
                    Player player = (Player) clientMaster.getEntity(identifier, playerId, playerX, playerY);
                    player.setCurrentRoom(currentRoom);
                    player.setIsMaxHealthSet(true);
                    player.setHitPoints(playerHealth);
                    player.setCurrSprite(currsprite);
                    player.setzIndex(playerZIndex);
                    clientMaster.setUserPlayer(player);
                    clientMaster.setCurrentRoom(currentRoom);
                        
                } catch (Exception e) {
                    System.out.println("Exception in parseAssetData() when setting user player");
                } 
            } else if (part.startsWith(NetworkProtocol.PLAYER)) {
                String[] otherPlayerData = part.substring(NetworkProtocol.PLAYER.length()).split(NetworkProtocol.SUB_DELIMITER);
                // System.out.println("Other player data: " + part);

                // Don't load if not in the same room as the client
                int otherRoomId = Integer.parseInt(otherPlayerData[5]);
                if (otherRoomId != clientMaster.getCurrentRoom().getRoomId()) continue;

                String identifier = otherPlayerData[0];
                int otherId = Integer.parseInt(otherPlayerData[1]);
                int x = Integer.parseInt(otherPlayerData[2]);
                int y = Integer.parseInt(otherPlayerData[3]);
                int hp = Integer.parseInt(otherPlayerData[4]);
                int currsprite = Integer.parseInt(otherPlayerData[6]);
                int zIndex = Integer.parseInt(otherPlayerData[7]);
                
                
                // Only load the player if it is not the user player and it is in the same room
                if ( (otherId != clientId) && (otherRoomId == clientMaster.getCurrentRoom().getRoomId()) ) {
                    Player other = (Player) clientMaster.getEntity(identifier, otherId, x, y);
                    other.setCurrentRoom(clientMaster.getRoomById(otherRoomId));
                    other.setIsMaxHealthSet(true);
                    other.setHitPoints(hp);
                    other.setCurrSprite(currsprite);
                    other.setzIndex(zIndex);
                    clientMaster.addEntity(other);
                } 
            } else if (part.startsWith(NetworkProtocol.ENTITY)) {
                // System.out.println("Whole entity string: " + part);
                String[] entityData = part.substring(NetworkProtocol.ENTITY.length()).split(NetworkProtocol.SUB_DELIMITER);
                
                if (entityData.length >= 7) {
                    int roomId = Integer.parseInt(entityData[4]);
                    if (!(roomId == clientMaster.getCurrentRoom().getRoomId())) continue;
                    
                    String identifier = entityData[0];
                    int id = Integer.parseInt(entityData[1]);
                    int x = Integer.parseInt(entityData[2]);
                    int y = Integer.parseInt(entityData[3]);
                    int sprite = Integer.parseInt(entityData[5]);
                    int zIndex = Integer.parseInt(entityData[6]);
                    clientMaster.loadEntity(identifier, id, x, y, roomId, sprite, zIndex);
                } else { 
                    // SPRITELESS OBJECTS
                    // Don't load if not in the same room as the client.
                    int roomId = Integer.parseInt(entityData[4]);
                    if (!(roomId == clientMaster.getCurrentRoom().getRoomId())) continue;
                    
                    String identifier = entityData[0];
                    int id = Integer.parseInt(entityData[1]);
                    int x = Integer.parseInt(entityData[2]);
                    int y = Integer.parseInt(entityData[3]);
                    clientMaster.loadEntity(identifier, id, x, y, roomId, 0, 0);
                }
            } else if (part.startsWith(NetworkProtocol.MINIMAP_UPDATE)) {
                // System.out.println("Recieved in parseEntitiesData:" + part);
                // Parse minimap data
                String[] minimapData = part.substring(NetworkProtocol.MINIMAP_UPDATE.length()).split(NetworkProtocol.MINIMAP_DELIMITER);
                HashMap<Room, Point> visibleRooms = new HashMap<>();
                
                for (String roomData : minimapData) {
                    if (roomData.isEmpty()) continue;
                    String[] roomInfo = roomData.split(NetworkProtocol.SUB_DELIMITER);
                    int roomId = Integer.parseInt(roomInfo[0]);
                    int x = Integer.parseInt(roomInfo[1]);
                    int y = Integer.parseInt(roomInfo[2]);
                    
                    Room room = clientMaster.getRoomById(roomId);
                    if (room != null) {
                        visibleRooms.put(room, new Point(x, y));
                    }
                }
                
                // Update the minimap with the new data
                if (clientMaster.getCurrentRoom() != null) {
                    clientMaster.getMiniMap().update(clientMaster.getCurrentRoom(), visibleRooms);
                }
            }
        
        }
        
    }

    /**
     * Parses a part of the String received by the receiveAssetsThread responsible for the map data.
     * After parsing, it sets the currentRoom, currentLevel, allRooms fields of the clientMaster.
     * @param message the substring containing map data
     */
    private void parseMapData(String message){
        DungeonMapDeserializeResult result = new DungeonMap().deserialize(message);
        clientMaster.setCurrentRoom(result.getStartRoom());
        clientMaster.setAllRooms(result.getAllRooms());
        clientMaster.setCurrentStage(result.getGameLevel());
    }
    
    /**
     * Parses a string in the format BK:roomId, doorId,x,y,direction to
     * create a new door in the end room after defeating a boss
     * @param message the substring containing the end room and new door data
     */
    private void parseBossKilledData(String message) {
        String[] dataParts = message.substring(NetworkProtocol.BOSS_KILLED.length() + NetworkProtocol.DOOR.length()).split(NetworkProtocol.SUB_DELIMITER);
        int doorId = Integer.parseInt(dataParts[0]);
        int x = Integer.parseInt(dataParts[1]);
        int y = Integer.parseInt(dataParts[2]);
        // System.out.println("Door Y: " + doorY);
        String direction = dataParts[3];
        int roomAID = Integer.parseInt(dataParts[4]);
        int roomBID = Integer.parseInt(dataParts[5]);
        Door d = new Door(x, y, direction, clientMaster.getRoomById(roomAID), clientMaster.getRoomById(roomBID));
        d.setId(doorId);
        clientMaster.getRoomById(roomAID).addDoorToArrayList(d);
    }
    
    /**
     * Continuously calls getInputsData and sends a byte array representation of its contents
     * to the server every TRANSFERINTERVAL milliseconds
     */
    public void startInputsThread(){
        final Runnable sendInputsData = new Runnable(){
            @Override
            public void run() {
                try {
                    String inputDataString = getInputsData();

                    // Send data if there are any actual inputs only
                    if (!inputDataString.isEmpty()) {
                        byte[] inputDataBytes = inputDataString.getBytes("UTF-8");
                        dataOut.writeInt(inputDataBytes.length);
                        dataOut.write(inputDataBytes);
                    }

                    } catch (IOException ex) {
                        System.out.println("IOException from startInputsThread");
                    }   
            }
        };
        sendInputsScheduler.scheduleAtFixedRate(sendInputsData, 0, TRANSFERINTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Builds a string containing all of the Player's Key inputs: Q, W, A, S, and D.
     * Then, appends to it a String containing a Player's Click input data (where in the screen they clicked)
     * @return a String containing a Player's Key and Click input data
     */
    public String getInputsData(){
        StringBuilder str = new StringBuilder();

        if(keyMap.get("Q")) str.append("Q");
        if(keyMap.get("W")) str.append("W");
        if(keyMap.get("A")) str.append("A");
        if(keyMap.get("S")) str.append("S");
        if(keyMap.get("D")) str.append("D");

        if(clickedX != 0 && clickedY != 0){
            str.append(NetworkProtocol.DELIMITER).append(NetworkProtocol.CLICK)
            .append(mouseButton).append(NetworkProtocol.SUB_DELIMITER)
            .append(clickedX).append(NetworkProtocol.SUB_DELIMITER)
            .append(clickedY).append(NetworkProtocol.SUB_DELIMITER);
            clickedX = 0;
            clickedY = 0;
        }

        return str.toString();
    }

    /**
     * Updates the keyMap depending on when a key isPressed (true) or released (false)
     * @param input the String representation of the pressed key
     * @param isPressed true if the key is currently pressed, false otherwise
     */
    public void keyInput(String input, Boolean isPressed){
        switch (input) {
            case "Q":
                keyMap.replace("Q", isPressed);
                break;     
            case "W":
                keyMap.replace("W", isPressed);
                break;
            case "A":
                keyMap.replace("A", isPressed);
                break;
            case "S":
                keyMap.replace("S", isPressed);
                break;
            case "D":
                keyMap.replace("D", isPressed);
                break;      
        }
    }

    public int getClientId() {
        return clientId;
    }

    /**
     * Hold the x and y coordinate of a click input
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public void clickInput(String mouseButton, int x, int y){
        this.mouseButton = mouseButton;
        clickedX = x;
        clickedY = y;
    }
}