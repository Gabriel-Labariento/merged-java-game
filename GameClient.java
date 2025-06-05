
import java.io.*;
import java.net.*;
import java.nio.channels.NetworkChannel;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import jdk.net.NetworkPermission;

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
    private int clickedX;
    private int clickedY;
    private ScheduledExecutorService sendInputsScheduler;
    private Thread receiveAssetsThread;
    private GameServer gs;
    private boolean wantsDisconnect;

    /**
     * Creates a GameClient and binds it to a clientMaster instance that handles
     * client-side game state. It also creates a singleThreadScheduledExecutor assigned
     * to sendInputs Scheduler for sending Player inputs to the server. It also creates
     * a keyMap to assign keys Q, W, A, S, and D to false (not pressed)
     * @param clientMaster
     */
    public GameClient(ClientMaster clientMaster){
        this.clientMaster = clientMaster;
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
            disconnectFromServer();
        }));
    }

    public void disconnectFromServer(){
        //Stop threads    
        // Stop scheduler and wait for completion
        wantsDisconnect = true;

        if (sendInputsScheduler != null && !sendInputsScheduler.isShutdown()) {
            sendInputsScheduler.shutdown();
            try {
                if (!sendInputsScheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    sendInputsScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                sendInputsScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Stop receive thread
        if (receiveAssetsThread != null && receiveAssetsThread.isAlive()) {
            receiveAssetsThread.interrupt();
            try {
                receiveAssetsThread.join(2000); // Wait up to 2 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Close streams
        try {
            if (dataOut != null) {
                dataOut.close();
            }
            if (dataIn != null) {
                dataIn.close();
            }
            if (theSocket != null && !theSocket.isClosed()) {
                theSocket.close();
            }
        } catch (IOException ex) {
            System.err.println("IOException from disconnect method: " + ex.getMessage());
        }
        
        // Clear references
        dataIn = null;
        dataOut = null;
        theSocket = null;
        sendInputsScheduler = null;
        receiveAssetsThread = null;
        
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
            
            resetClient();
            
            //Disable Nagle's buffering algorithm: basically reduces latency
            theSocket.setTcpNoDelay(true);
            System.out.println("CONNECTION SUCCESSFUL!");

            dataIn = new DataInputStream(theSocket.getInputStream());
            dataOut = new DataOutputStream(theSocket.getOutputStream());
            sendInputsScheduler = Executors.newScheduledThreadPool(1);

            sendPreGameData(playerType);
            startAssetsThread();
            startInputsThread();
            // startRenderLoop();

        } catch (IOException ex) {
            System.out.println("IOException from connectToServer() method");
        }
    }

    public void resetClient() {
        //Reset all relevant variables

        if (clientMaster.getEntities() != null) {
            synchronized (clientMaster.getEntities()) {
                clientMaster.getEntities().clear();
            }
        }
       
        clientMaster.setXPBarPercent(0);
        clientMaster.setUserLvl(1);
        clientMaster.setHeldItemIdentifier("");
        clientMaster.setBossHPBarPercent(0);
        clientMaster.setCurrentRoom(null);
        clientMaster.setAllRooms(null);
        clientMaster.setCurrentStage(0);
        clientMaster.setUserPlayer(null);
        clientMaster.setIsGameOver(false);
        clientMaster.setIsFinalBossDead(false);
        
        //Reset inputs
        clickedX = 0;
        clickedY = 0;
        keyMap.clear();
        keyMap.put("Q", false);
        keyMap.put("W", false);
        keyMap.put("A", false);
        keyMap.put("S", false);
        keyMap.put("D", false);
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

    public GameServer getGameServer(){
        return gs;
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
        receiveAssetsThread = new Thread(){
            @Override
            public void run(){
                while (!receiveAssetsThread.isInterrupted()){
                    try {
                        int byteLength = dataIn.readInt();
                        byte[] buffer = new byte[byteLength];
                        dataIn.readFully(buffer);
                        String receivedMessage = new String(buffer, "UTF-8");
                        // System.out.println("ReceivedMessage: " + receivedMessage);
                        // If the received message starts with the protocol identifier for map data, parse the map data
                        if (receivedMessage.startsWith(NetworkProtocol.MAP_DATA)) {
                            parseMapData(receivedMessage);
                        } else if (receivedMessage.startsWith(NetworkProtocol.BOSS_KILLED)) {
                            parseBossKilledData(receivedMessage);
                        } else if (receivedMessage.startsWith(NetworkProtocol.GAME_OVER)) {
                            clientMaster.setIsGameOver(true);
                        } else if (receivedMessage.startsWith(NetworkProtocol.FINAL_BOSS_KILLED)){
                            clientMaster.setIsFinalBossDead(true);
                        } else if (receivedMessage.startsWith(NetworkProtocol.YES_TO_SCENE5_END)){
                            clientMaster.setHasChosenScene5End(true);
                            // System.out.println("YES RECEIVED");
                        } else if (receivedMessage.startsWith(NetworkProtocol.NO_TO_SCENE5_END)){
                            clientMaster.setHasChosenScene5End(false);
                            // System.out.println("NO RECEIVED");
                        }
                        else if (receivedMessage.startsWith(NetworkProtocol.LEVEL_CHANGE)) {
                            clientMaster.getEntities().clear();
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
                        break;
                    }
                }}};
        receiveAssetsThread.start();
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
                // System.out.println("Parsing player");
                String[] playerData = part.substring(NetworkProtocol.USER_PLAYER.length()).split(NetworkProtocol.SUB_DELIMITER);
                // System.out.println("User player data: " + part);
                String identifier = playerData[0];
                int playerId = Integer.parseInt(playerData[1]);
                int playerX = Integer.parseInt(playerData[2]);
                int playerY = Integer.parseInt(playerData[3]);
                int playerHealth = Integer.parseInt(playerData[4]);
                int playerMaxHp = Integer.parseInt(playerData[5]);
                int playerRoomId = Integer.parseInt(playerData[6]);
                int currsprite = Integer.parseInt(playerData[7]);
                int playerZIndex = Integer.parseInt(playerData[8]);
                boolean isSpriteWhite = Boolean.parseBoolean(playerData[9]);
        
                // System.out.println(" user Player loaded");
                try {
                    Room currentRoom = clientMaster.getRoomById(playerRoomId);
                    Player player = (Player) clientMaster.getEntity(identifier, playerId, playerX, playerY);
                    player.setCurrentRoom(currentRoom);
                    // player.setIsMaxHealthSet(true);
                    player.setMaxHealth(playerMaxHp);
                    player.setHitPoints(playerHealth);
                    player.setCurrSprite(currsprite);
                    player.setzIndex(playerZIndex);
                    player.setIsSpriteWhite(isSpriteWhite);
                    clientMaster.setUserPlayer(player);
                    clientMaster.setCurrentRoom(currentRoom);
                        
                } catch (Exception e) {
                    System.out.println("Exception in parseAssetData() when setting user player");
                } 
            } else if (part.startsWith(NetworkProtocol.PLAYER)) {
                String[] otherPlayerData = part.substring(NetworkProtocol.PLAYER.length()).split(NetworkProtocol.SUB_DELIMITER);
                // System.out.println("Other player data: " + part);

                // Don't load if not in the same room as the client
                int otherRoomId = Integer.parseInt(otherPlayerData[6]);
                if (otherRoomId != clientMaster.getCurrentRoom().getRoomId()) continue;

                String identifier = otherPlayerData[0];
                int otherId = Integer.parseInt(otherPlayerData[1]);
                int x = Integer.parseInt(otherPlayerData[2]);
                int y = Integer.parseInt(otherPlayerData[3]);
                int hp = Integer.parseInt(otherPlayerData[4]);
                int maxHp = Integer.parseInt(otherPlayerData[5]);
                int currsprite = Integer.parseInt(otherPlayerData[7]);
                int zIndex = Integer.parseInt(otherPlayerData[8]);
                boolean isSpriteWhite = Boolean.parseBoolean(otherPlayerData[9]);
                
                
                // Only load the player if it is not the user player and it is in the same room
                if ( (otherId != clientId) && (otherRoomId == clientMaster.getCurrentRoom().getRoomId()) ) {
                    Player other = (Player) clientMaster.getEntity(identifier, otherId, x, y);
                    other.setCurrentRoom(clientMaster.getRoomById(otherRoomId));
                    // other.setIsMaxHealthSet(true);
                    other.setHitPoints(hp);
                    other.setMaxHealth(maxHp);
                    other.setCurrSprite(currsprite);
                    other.setzIndex(zIndex);
                    other.setIsSpriteWhite(isSpriteWhite);
                    clientMaster.addEntity(other);
                } 
            } else if (part.startsWith(NetworkProtocol.ENTITY)) {
                // System.out.println("Whole entity string: " + part);
                String[] entityData = part.substring(NetworkProtocol.ENTITY.length()).split(NetworkProtocol.SUB_DELIMITER);
                
                // for (String string : entityData) {
                //     // System.out.println("Entity string: " + string);
                // }

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

        //Reset isadultcatdefeated boolean on stage change
        // int gameLevel = result.getGameLevel();
        // if (gameLevel == 5) clientMaster.setIsAdultCatDefeated(false);
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
                        // System.out.println(inputDataString);
                        byte[] inputDataBytes = inputDataString.getBytes("UTF-8");
                        dataOut.writeInt(inputDataBytes.length);
                        dataOut.write(inputDataBytes);

                        if(wantsDisconnect) {
                            wantsDisconnect = false;
                            disconnectFromServer();
                        }
                    }

                    } catch (IOException ex) {
                        System.out.println("IOException from startInputsThread");
                        sendInputsScheduler.shutdown();
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
        String choice = clientMaster.getScene5Choice();
        if (!choice.equals("")){
            str.append(NetworkProtocol.DELIMITER);
            if(choice.equals("YES")) str.append(NetworkProtocol.YES_TO_CHOICE);
            else if (choice.equals("NO")) str.append(NetworkProtocol.NO_TO_CHOICE);
            clientMaster.setScene5Choice("");
        }
        else if (wantsDisconnect){
            str.append(NetworkProtocol.DELIMITER);
            str.append(NetworkProtocol.DISCONNECT_REQUEST);
        }
        else{
            if(keyMap.get("Q")) str.append("Q");
            if(keyMap.get("W")) str.append("W");
            if(keyMap.get("A")) str.append("A");
            if(keyMap.get("S")) str.append("S");
            if(keyMap.get("D")) str.append("D");

            if(clickedX != 0 && clickedY != 0){
                str.append(NetworkProtocol.DELIMITER);
                str.append(NetworkProtocol.CLICK);
                str.append(clickedX);
                str.append(",");
                str.append(clickedY);
                clickedX = 0;
                clickedY = 0;
            }
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

    /**
     * Hold the x and y coordinate of a click input
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public void clickInput(int x, int y){
        clickedX = x;
        clickedY = y;
    }

    public boolean getWantsDisconnect(){
        return wantsDisconnect;
    }

    public void setWantsDisconnect(boolean b){
        wantsDisconnect = b;
    }

}