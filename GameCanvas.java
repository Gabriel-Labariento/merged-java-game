import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GameCanvas extends JComponent {
    public static final int TILESIZE = 16;
    private static final int REFRESHINTERVAL = 16;
    private final int width, height;
    private GameClient gameClient;
    private ClientMaster clientMaster;
    private ScheduledExecutorService renderLoopScheduler;
    private ScheduledExecutorService sendInputsScheduler;
    public PlayerUI playerUI;
    private float screenOpacity;

    private static BufferedImage[] sprites;

    static {
        setSprites();
    }

    private static void setSprites() {
        try {
            BufferedImage gameOverScreen = ImageIO.read(GameCanvas.class.getResourceAsStream("resources/Misc/gameOver.png"));
            sprites = new BufferedImage[] {gameOverScreen};
        } catch (IOException e) {
            System.out.println("Exception in setSprites()" + e);
        }
    }

    public GameCanvas(int width, int height){
        this.width = width;
        this.height = height;
        renderLoopScheduler = Executors.newSingleThreadScheduledExecutor();
        sendInputsScheduler = Executors.newSingleThreadScheduledExecutor();
        clientMaster = new ClientMaster();
        gameClient = new GameClient(clientMaster);
        setPreferredSize(new Dimension(width, height));
        playerUI = new PlayerUI();
    }

    @Override
    protected void paintComponent(Graphics g){

        Graphics2D g2d = (Graphics2D) g;
        RenderingHints rh = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        //Load game menu ui
        Player userPlayer = clientMaster.getUserPlayer();
        if (clientMaster.getIsGameOver()){
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
        else{
            
            // Set the background/outside of the room
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, width, height);

            int scaleFactor = 1;
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
            playerUI.drawPlayerUI(g2d, userPlayer, clientMaster, scaleFactor);
        }
    }

    public GameClient getGameClient(){
        return gameClient;
    }

    public void startRenderLoop(){
        //Since putting Thread.sleep in a loop as necessary for this Loop is bad, use ScheduledExecutorService instead
        final Runnable renderLoop = this::repaint;
        renderLoopScheduler.scheduleAtFixedRate(renderLoop, 0, REFRESHINTERVAL, TimeUnit.MILLISECONDS);
    }

    public ClientMaster getClientState() {
        return clientMaster;
    }

    public void setClientState(ClientMaster clientMaster) {
        this.clientMaster = clientMaster;
    }

    public ScheduledExecutorService getRenderLoopScheduler() {
        return renderLoopScheduler;
    }

    public void setRenderLoopScheduler(ScheduledExecutorService renderLoopScheduler) {
        this.renderLoopScheduler = renderLoopScheduler;
    }

    public ScheduledExecutorService getSendInputsScheduler() {
        return sendInputsScheduler;
    }

    public void setSendInputsScheduler(ScheduledExecutorService sendInputsScheduler) {
        this.sendInputsScheduler = sendInputsScheduler;
    }

}
