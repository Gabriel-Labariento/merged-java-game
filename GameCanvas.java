import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
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

public class GameCanvas extends JComponent{
    public static final int TILESIZE = 16;
    private static final int REFRESHINTERVAL = 16;
    private final int width, height;
    private final GameClient gameClient;
    private final ClientMaster clientMaster;
    private final ScheduledExecutorService renderLoopScheduler;
    public SceneHandler sceneHandler;
    public PlayerUI playerUI;
    private MiniMap minimap;

    private TutorialManager tutorialManager;
    private boolean hasTutorialStarted = false;

    private float screenOpacity;
    private int currentStage;
    private int mouseX, mouseY;
    private boolean showTooltips = false;
    private boolean isRightClickAllowed = false;
    private boolean hasClickedOnItem = false;

    private static BufferedImage[] sprites;

    /**
     * Calls the static setScreens() method
     */
    static {
        setScreens();
    }

    /**
     * Sets the different game screens statically
     */
    private static void setScreens() {
        try {
            BufferedImage gameOverScreen = ImageIO.read(GameCanvas.class.getResourceAsStream("resources/Misc/gameOver.png"));
            sprites = new BufferedImage[] {gameOverScreen};
        } catch (IOException e) {
            System.out.println("Exception in setScreens()" + e);
        }
    }

    /**
     * Creates a GameCanvas instance with width and height. Insantiates a gameClient,
     * a clientMaster, playerUI, scenehandler.
     * @param width the canvas' width
     * @param height the canvas' height
     */
    public GameCanvas(int width, int height){
        this.width = width;
        this.height = height;
        renderLoopScheduler = Executors.newSingleThreadScheduledExecutor();
        clientMaster = new ClientMaster();
        gameClient = new GameClient(clientMaster);
        setPreferredSize(new Dimension(width, height));
        playerUI = new PlayerUI();
        sceneHandler = new SceneHandler();
        tutorialManager = new TutorialManager(this);
        currentStage = -1;
        minimap = clientMaster.getMiniMap();
        add(minimap);
    }   

    public void handleRightClick(int clickX, int clickY){
        if (!isRightClickAllowed) return;

        Player originPlayer = clientMaster.getUserPlayer();
        // Convert screen click to world click based on player position
        Point worldClick = convertScreenToWorld(clickX, clickY, originPlayer);
        worldClick.x -= originPlayer.getWidth()/2;
        worldClick.y -= originPlayer.getHeight()/2;
        final int clickAllowance = 3;
        // System.out.println("Right click at world coordinates: (" + worldClick.x + ", " + worldClick.y + ")");
        
        for (Entity e : clientMaster.getEntities()) {
            if (e instanceof Item item) {
                int[] itemBounds = e.getHitBoxBounds();
                // System.out.println("Checking item " + item.getId() + " at bounds: [" + itemBounds[2] + "," + itemBounds[3] + "," + itemBounds[0] + "," + itemBounds[1] + "]");
                if ((worldClick.x >= itemBounds[2] - clickAllowance && worldClick.x <= itemBounds[3] + clickAllowance) &&
                    (worldClick.y >= itemBounds[0] - clickAllowance && worldClick.y <= itemBounds[1] + clickAllowance)) {
                    // System.out.println("Item " + item.getId() + " was clicked!");
                    toggleItemTooltip(item);
                    hasClickedOnItem = true;
                }
            }
        }
    }

    
    public boolean getHasClickedOnItem() {
        return hasClickedOnItem;
    }

    public void setRightClickAllowed(boolean isRightClickAllowed) {
        this.isRightClickAllowed = isRightClickAllowed;
    }

    private void toggleItemTooltip(Item item){
        if (item == null || item.getTooltip() == null) return;
        clientMaster.setActiveTooltipItem(item);
        clientMaster.toggleTooltipState(item.getId());
        item.getTooltip().setShowTooltips(clientMaster.getTooltipState(item.getId()));
        SoundManager.getInstance().playPooledSound("click");
    }
    
    @Override
    protected void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        RenderingHints rh = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        Player userPlayer = clientMaster.getUserPlayer();
        if (sceneHandler.getIsScenePlaying()){
            sceneHandler.drawScene(g2d, width, height, currentStage);
        }
        else if (clientMaster.getIsGameOver()){
            if (screenOpacity < 1f){
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, screenOpacity));
                screenOpacity += 0.005f;
            } 
            g2d.drawImage(sprites[0], 0, 0, width, height, null);
        }
        else if ( (userPlayer == null) || (clientMaster.getCurrentRoom() == null) ){
            //Temporary Background
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);
        } 
        else {
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
                if (door.isOpen() && door.getRoomB() != null) {
                    door.draw(g2d, cameraX, cameraY);
                }
            }

            // Draw enemies, projectiles, other players
            synchronized (clientMaster.getEntities()) {
                ArrayList<Entity> sortedEntitiesByZ = new ArrayList<>(clientMaster.getEntities());
                sortedEntitiesByZ.sort(Comparator.comparingInt(Entity::getZIndex));

                for (Entity entity : sortedEntitiesByZ) {
                    if (entity == null) continue;
                    entity.draw(g2d, entity.getWorldX() - userPlayer.getWorldX() + screenX, entity.getWorldY()- userPlayer.getWorldY() + screenY);
                    renderActiveTooltip(g2d, cameraX, cameraY);
                }  
            }
            
            //Draw current user's player
            userPlayer.draw(g2d, screenX, screenY);

            //Draw UI elements
            playerUI.drawPlayerUI(g2d, clientMaster, scaleFactor);

            // Reset scale for UI elements
            g2d.scale(1.0/scaleFactor, 1.0/scaleFactor);

            // Update and draw minimap
            minimap.paint(g2d);

            //Draw scenes
            if(currentStage < clientMaster.getCurrentStage()){
                currentStage++;
                sceneHandler.setIsScenePlaying(true);
            }
        }
    }

    private void renderActiveTooltip(Graphics2D g2d, int cameraX, int cameraY){
        Item item = clientMaster.getActiveTooltipItem();

        if (item != null && item.getTooltip().getShowTooltips()) {
            item.getTooltip().setPosition(item.getWorldX() - cameraX + 16, item.getWorldY() - cameraY - 40);
            item.getTooltip().update(clientMaster.getCurrentRoom());
            item.getTooltip().draw(g2d, cameraX, cameraY); // render the tooltip
        }
    }

    /**
     * Gets a reference to gameClient
     * @return the GameClient object assigned to the canvas
     */
    public GameClient getGameClient(){
        return gameClient;
    }

     /**
     * Gets a reference to gameClient
     * @return the GameClient object assigned to the canvas
     */
    public SceneHandler getSceneHandler(){
        return sceneHandler;
    }


    /**
     * Calls repaint on this GameCanvas every REFRESHINTERVAL milliseconds
     */
    public void startRenderLoop(){
        renderLoopScheduler.scheduleAtFixedRate(() -> {
            // Start tutorial after first scene is done
            if (sceneHandler.lastFinishedScene != -1 && !tutorialManager.isActive() && !hasTutorialStarted) {
                tutorialManager.startTutorial();
                hasTutorialStarted = true;
            }

            if (sceneHandler.lastFinishedScene != -1 
                && tutorialManager != null 
                && tutorialManager.isActive()
                && !tutorialManager.isTutorialComplete())
                {
                    tutorialManager.checkTutorialProgression();
                } 

            repaint();
        }, 0, REFRESHINTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Converts screen coordinates to world coordinates based on player and camera offset.
     */
    public Point convertScreenToWorld(int clickX, int clickY, Player originPlayer) {
        int scaleFactor = 2;

        int screenX = (800 / (2 * scaleFactor) - originPlayer.getWidth() / (2 * scaleFactor));
        int screenY = (600 / (2 * scaleFactor) - originPlayer.getHeight() / (2 * scaleFactor));

        int cameraX = originPlayer.getWorldX() - screenX;
        int cameraY = originPlayer.getWorldY() - screenY;

        int worldX = (int)(clickX / (double) scaleFactor) + cameraX;
        int worldY = (int)(clickY / (double) scaleFactor) + cameraY;

        return new Point(worldX, worldY);
    }

    /**
     * Gets the tutorial manager instance.
     * @return the TutorialManager instance
     */
    public TutorialManager getTutorialManager() {
        return tutorialManager;
    }

    public ClientMaster getClientMaster() {
        return clientMaster;
    }
}
