import java.io.*;
import java.io.File;
import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**     
        The GameServer class handles the server-side networking logic.
        It accepts connections from clients (ConnectedPlayer) and synchronized
        game state.

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

public class GameServer {
    private static final int TICKSPERSECOND = 60;
    // Network
    private ServerSocket ss;
    private ArrayList<Socket> sockets;
    private int clientNum;
    private int port;
    private Thread waitForConnectionsThread;
    private boolean hasPlayerDisconnected;
    private boolean isSinglePlayer;

    // Game State
    private ServerMaster serverMaster;
    private ArrayList<ConnectedPlayer> connectedPlayers;
    // Threads
    private ScheduledExecutorService gameLoopScheduler;
    
    /**
     * Creates a GameServer instance, initializes server components, and 
     * looks for an available port for connection.
     */
    public GameServer(boolean isSinglePlayer) {
        clientNum = 1;
        serverMaster = ServerMaster.getInstance();
        serverMaster.setGameValues();
        serverMaster.setIsSinglePlayer(isSinglePlayer);
        sockets = new ArrayList<>();
        connectedPlayers = new ArrayList<>();
        gameLoopScheduler = Executors.newSingleThreadScheduledExecutor();
        this.isSinglePlayer = isSinglePlayer;

        serverMaster.setConnectedPlayers(connectedPlayers);
        
        port = 5000;
        while (true) { 
            try { ss = new ServerSocket(port); } 
            catch (IOException ex) { System.out.println("IOException from GameServer constructor"); }
    
            if (ss != null) {
                System.out.println("Routed to port " + port);
                break;
            } 
            port++;
        }
        // System.out.println("GAMESERVER HAS BEEN CREATED.");
    }

    /**
     * Starts game loop at 60 FPS. Updates game state in serverMaster
     * and sends all entity data to all connectedPlayers.
     */
    public void startGameLoop(){
        final Runnable gameLoop = new Runnable(){
            @Override
            public void run(){
                try {
                    serverMaster.update();    
                } catch (Exception e) {
                    System.err.println("Exception in game loop update():" + e);
                }

               try {
                    //Check flags before sending assets data to players
                    if (serverMaster.getIsGameOver() || serverMaster.getIsFinalBossKilled() 
                    || serverMaster.getHasChosenScene5End()){
                        shutDownServer();
                    }
                    //Check disconnection flag for shutting down server if there are no remaining players in it
                    else if (hasPlayerDisconnected) {
                        //Remove disconnected player from arraylist
                        connectedPlayers.removeIf(cp -> cp.isDisconnected);

                        if (connectedPlayers.isEmpty()) shutDownServer();
                        else hasPlayerDisconnected = false;
                    }
                    else if (!connectedPlayers.isEmpty()){
                        for (ConnectedPlayer cp : connectedPlayers) {
                            Player p = (Player) serverMaster.getPlayerFromClientId(cp.cid);
                            if (p != null){
                                String data = serverMaster.getAssetsData(cp.cid);
                                if (data != null) cp.promptAssetsThread(data);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Exception in asset dispersion to connected players: " + e);
                }
            }
        };
        gameLoopScheduler.scheduleAtFixedRate(gameLoop, 0, Math.round(1000/TICKSPERSECOND), TimeUnit.MILLISECONDS);
    }

    public void shutDownServer(){

        //Disconnect players
        if(!connectedPlayers.isEmpty()) {
            for (ConnectedPlayer cp : connectedPlayers){
                //Shutdown auxiliary threads
                // serverMaster.getPlayerFromClientId(cp.cid).getCurrentRoom().getMobSpawner().stopSpawn();
                cp.disconnectFromServer();
                
            }
        }
        
        //Close socketss
        if (!waitForConnectionsThread.isInterrupted()) waitForConnectionsThread.interrupt();
        try { 
            if (ss != null && !ss.isClosed()) ss.close();
        } catch(IOException e) {
            System.out.println("IOException from shutDownServer method.");
        }

        gameLoopScheduler.shutdown();
        

    }


    /**
     * Closes sockets on game close
     */
    public void closeSocketsOnShutdown(){
        Runtime.getRuntime().addShutdownHook( new Thread(() -> {shutDownServer();}));  
    }

    /**
     * Continuously accepts client connections and creates a new ConnectedPlayer instance
     * for every client connection.
     */
    public void waitForConnections() {
        waitForConnectionsThread = new Thread(){        
            @Override
            public void run(){
                try {
                    System.out.println("NOW ACCEPTING CONNECTIONS...");
                    while (!waitForConnectionsThread.isInterrupted()){
                        // Create a socket for the client to use
                        Socket sock = ss.accept();
                        
                        // Disable Nagle's buffering algorithm: basically reduces latency
                        sock.setTcpNoDelay(true);
                        sockets.add(sock);
                        
                        ConnectedPlayer cp = new ConnectedPlayer(sock, clientNum);
                        clientNum++;
                        cp.loadPreGameData();
                        connectedPlayers.add(cp);
                        cp.startThreads();
                        
                        //Disable joining of other players for singleplayer mode
                        if (isSinglePlayer) waitForConnectionsThread.interrupt();
                    }        
                    } catch (IOException ex) {
                        System.out.println("IOException from waitForConnection() method.");
                }
            }
        };
        waitForConnectionsThread.start();
    }

    /**
     * Gets the port number field value
     * @return the int value of port where a connection was made
     */
    public int getPort() {
        return port;
    }

    /**
     * The ConnectedPlayer class is an inner class representing an individual
     * player connection. It handles all input and output threads for an 
     * individual client connection.
     */
    public class ConnectedPlayer {
        private boolean isDisconnected;
        private Socket clientSocket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private Thread getInputsThread;
        private Thread sendAssetsThread;
        private BlockingQueue<String> sendQueue;    // For halting operations within a thread
        private int cid;
        
        /**
         * Initializes the I/O streams of a client based on its clientId
         * @param sck the socket where a connection is made
         * @param cid the connecting client's Id 
         */
        public ConnectedPlayer(Socket sck, int cid){
            clientSocket = sck;
            this.cid = cid;
            System.out.println("CONNECTEDPLAYERCID:" + cid);
            sendQueue = new LinkedBlockingDeque<>();
            
    
            try {
                dataIn = new DataInputStream(clientSocket.getInputStream());
                dataOut = new DataOutputStream(clientSocket.getOutputStream());
            } catch (IOException ex) {
                System.out.println("IOException from ConnectedPlayer constructor");
            }
            
        }

        public void disconnectFromServer(){
            //Remove player from gamestate
            serverMaster.removeEntity(serverMaster.getPlayerFromClientId(cid));
            // serverMaster.setPlayerNum(serverMaster.getPlayerNum() - 1);

            //Close data streams
            try {
                if (dataOut != null) dataOut.close();
                if (dataIn != null) dataIn.close();
                if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            } catch (IOException e) {
                System.out.println("IOException from disconnectFromSever(): clientsocket closing");
            }

            //Properly close threads
            getInputsThread.interrupt();
            sendAssetsThread.interrupt();
            sendQueue.clear();
            sendQueue.offer("shutdown");
        
        }

        /**
         * Reads from the input stream the type of Player the client chose and creates a new
         * instance of the Player subclass. It sets its initial position to the center of 
         * serverMaster's currentRoom and adds it to serverMaster's entities ArrayList
         */
        public void loadPreGameData(){
            try {
                int byteLength = dataIn.readInt();
                byte[] buffer = new byte[byteLength];
                dataIn.readFully(buffer);
                String playerType = new String(buffer, "UTF-8");

                File saveFile = new File("save.dat");
                
                if (isSinglePlayer && saveFile.exists()){
                    serverMaster.loadGameState();
                }
                else{
                    //Set heavycat as default
                    Player chosenPlayer = serverMaster.createPlayer(playerType, cid);
                    Player player = (chosenPlayer != null) ? chosenPlayer : new HeavyCat(cid, serverMaster.getCurrentRoom().getCenterX(), serverMaster.getCurrentRoom().getCenterY());  
                    // Add the new player to the minimap system
                    
                    serverMaster.addEntity(player);
                }
                serverMaster.getMiniMapManager().addNewPlayer(cid, serverMaster.getCurrentRoom());
            } catch (IOException ex){
                System.out.println("IOEception from receiveAssetsThread");
            }
         }

         /**
          * Gets the value of Cid
          * @return the int value of cid
          */
        public int getCid() {
            return cid;
        }

        /**
         * Calls startAssetsThread() and startInputsThread()
         */
        public void startThreads(){
            startAssetsThread();
            startInputsThread();
        }

        /**
         * Calls the sendMapData() once and sendEntitiesData() continuously every 16 miliseconds. 
         */
        private void startAssetsThread(){
            System.out.println("NEW PLAYER HAS ENTERED");
            sendAssetsThread = new Thread(){
                boolean mapDataSent = false;

                @Override
                public void run(){
                    while (!sendAssetsThread.isInterrupted()) { 
                        try {
                            //Only start the rest of the thread if data is sent
                            //FLAG
                            String assetsDataString = sendQueue.take();
                            // System.out.println(assetsDataString);

                            //Clear blocking operation above
                            if (assetsDataString.equals("shutdown")) break;

                            if (!mapDataSent){
                                sendMapData();
                                mapDataSent = true;
                                // System.out.println("Map Data Sent");
                            }

                            byte[] assetsDataBytes = assetsDataString.getBytes("UTF-8");
                            dataOut.writeInt(assetsDataBytes.length);
                            dataOut.write(assetsDataBytes);

                        } catch (IOException ex) {
                            System.out.println("IOException from ConnectedPlayer's startAssetsThread method");
                            break;
                        } catch (InterruptedException ex) {
                            System.out.println("InterrupedException from ConnectedPlayer's startAssetsThread method");
                            break;
                        }   
                    }
                }    
            };
            sendAssetsThread.start();
        }

        /**
         * Takes a data String and offers it to sendQueue
         * @param data the data String to be offered
         */
        public void promptAssetsThread(String data){
            sendQueue.offer(data);
        }

        /**
         * Sends the serialized map data by converting it to a byte array
         */
        private void sendMapData(){
            try {
                String mapDataString = serverMaster.getMapData();
                byte[] mapDataBytes = mapDataString.getBytes("UTF-8");
                dataOut.writeInt(mapDataString.length());
                dataOut.write(mapDataBytes);
            } catch (IOException ex) {
                System.out.println("IOException from sendMapData() method");
            }
        }

        /**
         * Continuously receives and parses inputs and calls method from ServerMaster to process
         * both click and key inputs.
         */
        private void startInputsThread(){
            getInputsThread = new Thread(){
                
                @Override
                public void run(){
                    while (!getInputsThread.isInterrupted()){
                        String str = "";
                        try {
                            int byteLength = dataIn.readInt();
                            byte[] buffer = new byte[byteLength];
                            dataIn.readFully(buffer);
                            str = new String(buffer, "UTF-8");
                        } catch (IOException ex){
                            System.out.println("IOEception from getInputsData()");
                            break;
                        }
                        String[] inputStrParts = str.split("\\|");
                        
                        for (String part : inputStrParts) {
                            
                            if (part.isEmpty()) continue;
                            else if (part.startsWith(NetworkProtocol.CLICK)){
                                String[] coors = part.split(NetworkProtocol.SUB_DELIMITER);
                                String mouseButton = coors[0].substring(NetworkProtocol.CLICK.length());
                                int x = Integer.parseInt(coors[1]);
                                int y = Integer.parseInt(coors[2]);
                                serverMaster.loadClickInput(mouseButton, x, y, cid); 
                            } else if (part.startsWith(NetworkProtocol.NO_TO_CHOICE)){
                                serverMaster.pollChoice(false);
                            } else if (part.startsWith(NetworkProtocol.YES_TO_CHOICE)){
                                serverMaster.pollChoice(true);
                            } else if (part.startsWith(NetworkProtocol.DISCONNECT_REQUEST)){
                                disconnectFromServer();
                                hasPlayerDisconnected = true;
                                isDisconnected = true;
                            } else {
                                // System.out.println(part);
                                int keyInputNum = 0;

                                for (char keyInput:part.toCharArray()){

                                    //Load only two keyInputs for movement per reception
                                    if (keyInputNum == 2) break;
                                    serverMaster.loadKeyInput(keyInput, cid);

                                    //If not keyInput for movement, then skip input counting
                                    if(part.charAt(0) == 'Q') continue;
            
                                    keyInputNum++;
                                }            
                            }
                        }
                    }
            } 
         };
         getInputsThread.start();
    }
}
    


    /**
     * Main method used to instantiate and run GameServer
     * @param args command line arguments provided
     */
    public static void main(String[] args) {
        GameServer cs = new GameServer(false);
        cs.startGameLoop();
        cs.closeSocketsOnShutdown();
        cs.waitForConnections();
    }
}
