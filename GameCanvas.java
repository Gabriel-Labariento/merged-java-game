import java.awt.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.*;
import javax.swing.*;

/**     
        The GameCanvas class extends Jcomponent. It serves as the main
        object where other GameObjects and Entities are drawn. Additionally,
        UI and Map elements are drawn here as well.

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

public class GameCanvas extends JComponent {
    public static final int TILESIZE = 16;
    private static final int REFRESHINTERVAL = 16;
    private final int width, height;
    private GameClient gameClient;
    private ClientMaster clientMaster;
    private final ScheduledExecutorService renderLoopScheduler;
    public SpecialFrameHandler specialFrameHandler;
    public PlayerUI playerUI;
    private float screenOpacity;
    private int currentStage;
    private boolean isOnMenu;
    
    /**
     * Creates a GameCanvas instance with width and height. Insantiates a gameClient,
     * a clientMaster, playerUI, scenehandler.
     * @param width the canvas' width
     * @param height the canvas' height
     */
    public GameCanvas(int width, int height, Font gameFont, boolean isSinglePlayer){
        this.width = width;
        this.height = height;
        renderLoopScheduler = Executors.newSingleThreadScheduledExecutor();
        clientMaster = new ClientMaster();
        gameClient = new GameClient(clientMaster, isSinglePlayer);
        setPreferredSize(new Dimension(width, height));
        playerUI = new PlayerUI(gameFont);
        specialFrameHandler = new SpecialFrameHandler(gameFont);
        currentStage = -1;
        isOnMenu = true;
    }   

    @Override
    protected void paintComponent(Graphics g){

        Graphics2D g2d = (Graphics2D) g;
        RenderingHints rh = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        if (isOnMenu){
            //Temporary Background
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);
        } 
        else if (specialFrameHandler.getIsScenePlaying())
            specialFrameHandler.drawScene(g2d, width, height, currentStage, clientMaster);
        else if (clientMaster.getIsGameOver()){
            if (screenOpacity < 1f){
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, screenOpacity));
                screenOpacity += 0.005f;
            } 
            specialFrameHandler.drawDeathScreen(g2d, width, height);
        }
        else{
            Player userPlayer = clientMaster.getUserPlayer();
            if (userPlayer == null) return;

            //Detect scene changes and apply specific triggers
            // boolean isAdultCatDefeated = clientMaster.getIsAdultCatDefeated();
            boolean hasStageProgressed = currentStage < clientMaster.getCurrentStage();
            boolean isFinalBossDefeated = clientMaster.getIsFinalBossDead();

            // Check if any scene change condition is met
            if (hasStageProgressed || isFinalBossDefeated) {
                currentStage++;
                specialFrameHandler.setIsScenePlaying(true);
                return;
            }

            // Set the background/outside of the room
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);

            int scaleFactor = 2;
            g2d.scale(scaleFactor, scaleFactor);
            
            int screenX = (800/ (2 * scaleFactor) - userPlayer.getWidth() / (2 * scaleFactor));
            int screenY = (600/ (2 * scaleFactor) - userPlayer.getHeight() / (2 * scaleFactor));

            int cameraX = userPlayer.getWorldX() - screenX;
            int cameraY = userPlayer.getWorldY() - screenY;

            Room currentRoom = clientMaster.getCurrentRoom();
            currentRoom.draw(g2d, cameraX, cameraY);
            
            // Draw room doors
            for (Door door : currentRoom.getDoorsArrayList()) {
                door.draw(g2d, cameraX, cameraY);
            }

            // Draw enemies, projectiles, other players

            synchronized (clientMaster.getEntities()) {
                // Sort by z index for proper rendering
                ArrayList<Entity> sortedEntitiesByZ = new ArrayList<>(clientMaster.getEntities());
                sortedEntitiesByZ.sort(Comparator.comparingInt(Entity::getZIndex));

                for (Entity entity : sortedEntitiesByZ)    
                    entity.draw(g2d, entity.getWorldX() - userPlayer.getWorldX() + screenX, entity.getWorldY()- userPlayer.getWorldY() + screenY);    
            }
            
            //Draw current user's player
            userPlayer.draw(g2d, screenX, screenY); //CHANGE 50 BY ACTUAL ASSET SIZE

            //Draw UI elements
            playerUI.drawPlayerUI(g2d, clientMaster, scaleFactor);
        }
    }

    public void setIsOnMenu(boolean b){
        isOnMenu = b;
    }

    public boolean getIsOnMenu(){
        return isOnMenu;
    }

    /**
     * Gets a reference to gameClient
     * @return the GameClient object assigned to the canvas
     */
    public GameClient getGameClient(){
        return gameClient;
    }

    public void setGameClient(GameClient gameClient){
        this.gameClient = gameClient;
    }

     /**
     * Gets a reference to gameClient
     * @return the GameClient object assigned to the canvas
     */
    public SpecialFrameHandler getSpecialFrameHandler(){
        return specialFrameHandler;
    }

    public ClientMaster getClientMaster(){
        return clientMaster;
    }

    public void setClientMaster(ClientMaster clientMaster){
        this.clientMaster = clientMaster;
    }

    public void stopRenderLoop(){
        renderLoopScheduler.shutdown();
    }

    /**
     * Calls repaint on this GameCanvas every REFRESHINTERVAL milliseconds
     */
    public void startRenderLoop(){
        //Since putting Thread.sleep in a loop as necessary for this Loop is bad, use ScheduledExecutorService instead
        final Runnable renderLoop = this::repaint;
        renderLoopScheduler.scheduleAtFixedRate(renderLoop, 0, REFRESHINTERVAL, TimeUnit.MILLISECONDS);
    }

    public PlayerUI getPlayerUI(){
        return playerUI;
    }
}
