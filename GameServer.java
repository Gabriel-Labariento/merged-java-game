import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameServer {
    private static final int TICKSPERSECOND = 60;
    private ServerSocket ss;
    private ArrayList<Socket> sockets;
    private ServerMaster serverMaster;
    private ArrayList<ConnectedPlayer> connectedPlayers;
    private ScheduledExecutorService gameLoopScheduler;
    private ScheduledExecutorService sendAssetsScheduler;
    private int clientNum;
    private int port;

    public GameServer() {
        clientNum = 1;
        serverMaster = ServerMaster.getInstance();
        sockets = new ArrayList<>();
        connectedPlayers = new ArrayList<>();
        serverMaster.setConnectedPlayers(connectedPlayers);
        gameLoopScheduler = Executors.newSingleThreadScheduledExecutor();
        sendAssetsScheduler = Executors.newSingleThreadScheduledExecutor();
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
        
        System.out.println("GAMESERVER HAS BEEN CREATED.");
    }

    public int getPort() {
        return port;
    }
    
    public void startGameLoop(){
        final Runnable gameLoop = new Runnable(){
            @Override
            public void run(){
                try {
                    serverMaster.update();    
                    // System.out.println("called update on gsm");
                } catch (Exception e) {
                    System.err.println("Exception in game loop update():" + e);
                }

               try {
                 if (!connectedPlayers.isEmpty()){
                    for (ConnectedPlayer cp : connectedPlayers) {
                        Player p = (Player) serverMaster.getPlayerFromClientId(cp.cid);
                        if (p == null) continue;
                        String data = serverMaster.getAssetsData(cp.cid);
                        if (data != null) cp.promptAssetsThread(data);
                    }
                }
               } catch (Exception e) {
                System.out.println("Exception in asset dispersion to connected players: " + e);
                e.printStackTrace();
               }
            }
        };
        gameLoopScheduler.scheduleAtFixedRate(gameLoop, 0, Math.round(1000/TICKSPERSECOND), TimeUnit.MILLISECONDS);
    }


    public void closeSocketsOnShutdown(){
        Runtime.getRuntime().addShutdownHook( new Thread(() -> {
            try { 
                for (Socket skt:sockets) {
                    skt.close();
                }
            } catch(IOException e) {
                System.out.println("IOException fromcloseSocketsOnShutdown() method.");
            }
        }));  
    }

    public void waitForConnections() {
        Thread waitForConnectionsThread = new Thread(){        
            @Override
            public void run(){
                try {
                    System.out.println("NOW ACCEPTING CONNECTIONS...");
                    while (true){
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
                    
                    }        
                    } catch (IOException ex) {
                        System.out.println("IOException from waitForConnection() method.");
                }
            }
        };
        waitForConnectionsThread.start();

    }

    public class ConnectedPlayer {
        private Socket clientSocket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        //For halting operations within a thread
        private BlockingQueue<String> sendQueue;
        private int cid;
    
        public ConnectedPlayer(Socket sck, int n){
            clientSocket = sck;
            cid = n;
            sendQueue = new LinkedBlockingDeque<>();
            
            try {
                dataIn = new DataInputStream(clientSocket.getInputStream());
                dataOut = new DataOutputStream(clientSocket.getOutputStream());
            } catch (IOException ex) {
                System.out.println("IOException from ConnectedPlayer constructor");
            }
        }

        public void loadPreGameData(){
            try {
                int byteLength = dataIn.readInt();
                byte[] buffer = new byte[byteLength];
                dataIn.readFully(buffer);
                String playerType = new String(buffer, "UTF-8");

                Player chosenPlayer = null;
       
                if (playerType.equals(NetworkProtocol.HEAVYCAT)){
                    chosenPlayer = (new HeavyCat(cid, serverMaster.getCurrentRoom().getCenterX(), serverMaster.getCurrentRoom().getCenterY()));
                }
                else if (playerType.equals(NetworkProtocol.FASTCAT)){
                    chosenPlayer = (new FastCat(cid, serverMaster.getCurrentRoom().getCenterX(), serverMaster.getCurrentRoom().getCenterY()));
                }
                else if (playerType.equals(NetworkProtocol.GUNCAT)){
                    chosenPlayer = (new GunCat(cid, serverMaster.getCurrentRoom().getCenterX(), serverMaster.getCurrentRoom().getCenterY()));
                }
                
                
                serverMaster.addEntity(chosenPlayer);


            } catch (IOException ex){
                System.out.println("IOEception from receiveAssetsThread");
            }
         }

        public int getCid() {
            return cid;
        }

        public void startThreads(){
            startAssetsThread();
            startInputsThread();
        }

        /**
         * Calls the sendMapData() once and sendEntitiesData() continuously every 16 miliseconds. 
         */
        private void startAssetsThread(){
            System.out.println("NEW PLAYER HAS ENTERED");
            Thread sendAssetsThread = new Thread(){
                boolean mapDataSent = false;

                @Override
                public void run(){
                    while (true) { 
                        try {
                            //Only start the rest of the thread if data is sent
                            String assetsDataString = sendQueue.take();
                            // System.out.println(assetsDataString);

                            if (!mapDataSent){
                                sendMapData();
                                mapDataSent = true;
                                System.out.println("Map Data Sent");
                            }

                            byte[] assetsDataBytes = assetsDataString.getBytes("UTF-8");
                            dataOut.writeInt(assetsDataBytes.length);
                            dataOut.write(assetsDataBytes);

                        } catch (IOException ex) {
                            System.out.println("IOException from ConnectedPlayer's startAssetsThread method");
                            break;
                        } catch (InterruptedException ex) {
                            System.out.println("InterrupedException from ConnectedPlayer's startAssetsThread method");
                        }   
                    }
                }    
            };
            sendAssetsThread.start();
        }

        public void promptAssetsThread(String data){
            sendQueue.offer(data);
        }

        
        private void sendCustomData(String message){    
            try {
                byte[] dataBytes = message.getBytes("UTF-8");
                // System.out.println("Sending Custom Data:" + message);
                dataOut.writeInt(dataBytes.length);
                dataOut.write(dataBytes);
            } catch (IOException ex) {
                System.out.println("IOException from sendCustomData() method");
            }
        }

        /**
         * Sends the serialized map data by converting it to a byte array
         */
        private void sendMapData(){
            try {
                String mapDataString = serverMaster.getMapData();
                byte[] mapDataBytes = mapDataString.getBytes("UTF-8");
                System.out.println("Sending Map Data...");
                dataOut.writeInt(mapDataString.length());
                dataOut.write(mapDataBytes);
            } catch (IOException ex) {
                System.out.println("IOException from sendMapData() method");
            }
        }

        private void startInputsThread(){
            Thread getInputsThread = new Thread(){
                
                @Override
                public void run(){
                    while (true){
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
                                int x = Integer.parseInt(coors[0].substring(NetworkProtocol.CLICK.length()));
                                int y = Integer.parseInt(coors[1]);
                                // System.out.println("CLick at " + x + "," + y);
                                serverMaster.loadClickInput(x, y, cid); 
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

  
    // When GameServer is run, the main method instantiates a new 
    public static void main(String[] args) {
        GameServer cs = new GameServer();
        cs.startGameLoop();
        cs.closeSocketsOnShutdown();
        cs.waitForConnections();
    }
}
