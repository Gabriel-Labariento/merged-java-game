import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The SceneHandler class is a class that creates a 2D array of 
        buffereed images statically which it can then use to draw the scenes 
        with custom triggers

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
public class SceneHandler{
    private static BufferedImage[][] scenes;
    private static final int FRAME_DURATION = 7000;
    private static final int CHOICE_CD_DURATION = 2000;
    private int currentScene;
    private int currentFrame;
    private long lastFrameUpdate;
    private boolean isScenePlaying;
    private boolean isOnChoice;
    public int lastFinishedScene = -1;
    
    /**
     * Set sprites on class initialization
     */
    static {
        try {
            BufferedImage scene1A = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene1A.png"));
            BufferedImage scene1B = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene1B.png"));
            BufferedImage scene1C = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene1C.png"));
            BufferedImage scene2A = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene2A.png"));
            BufferedImage scene2B = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene2B.png"));
            BufferedImage scene3A = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene3A.png"));
            BufferedImage scene3B = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene3B.png"));
            BufferedImage scene3C = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene3C.png"));
            BufferedImage scene3D = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene3D.png"));
            BufferedImage scene4A = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene4A.png"));
            BufferedImage scene4B = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene4B.png"));
            BufferedImage scene5A = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene5A.png"));
            BufferedImage scene5B = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene5B.png"));
            BufferedImage scene5C = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene5C.png"));
            BufferedImage scene5D = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene5D.png"));
            BufferedImage scene5E = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene5E.png"));
            BufferedImage scene5F = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene5F.png"));
            BufferedImage scene5G = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene5G.png"));
            BufferedImage scene5H = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene5H.png"));
            BufferedImage scene6A = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene6A.png"));
            BufferedImage scene6B = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene6B.png"));
            BufferedImage scene6C = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene6C.png"));
            BufferedImage scene6D = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene6D.png"));
            BufferedImage scene7A = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene7A.png"));
            BufferedImage scene7B = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene7B.png"));
            BufferedImage scene7C = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene7C.png"));
            BufferedImage scene7D = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene7D.png"));
            BufferedImage scene7E = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene7E.png"));
            BufferedImage scene7F = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene7F.png"));
            BufferedImage scene7G = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene7G.png"));
            BufferedImage scene7H = ImageIO.read(SceneHandler.class.getResourceAsStream("resources/Scenes/scene7H.png"));
            scenes = new BufferedImage[][] {
                {scene1A, scene1B, scene1C},
                {scene2A, scene2B},
                {scene3A, scene3B, scene3C, scene3D},
                {scene4A, scene4B},
                {scene5A, scene5B, scene5C, scene5D, scene5E, scene5F, scene5G, scene5H},
                {scene6A, scene6B, scene6C, scene6D},
                {scene7A, scene7B, scene7C, scene7D, scene7E, scene7F, scene7G, scene7H},
            };

        } catch (IOException e) {
            System.out.println("Exception in SceneHandler setSprites()" + e);
        }
    }

    /**
     * Draws all of the scenes prompted by specific triggers
     * @param g2d the graphics2d object from gamecanvas
     * @param width the frame's width
     * @param height the frame's height
     * @param currentstage gamecanvas' currentstage to be used for triggers
     */
    public void drawScene(Graphics2D g2d, int width, int height, int currentStage){
        long now = System.currentTimeMillis();
        //Specify triggers
        switch (currentStage) {
            case 0: case 1: case 2:
                currentScene = currentStage;
                break;
            case 4: case 5: case 6: // case 7: 
                currentScene = currentStage - 1;
                break;
            default:
                return;
        }

        if (currentFrame >= scenes[currentScene].length){
            isScenePlaying = false;
            currentFrame = 0;
            lastFrameUpdate = 0;
            isOnChoice = false;
            lastFinishedScene = currentScene;
            return;
        }
    
        if (lastFrameUpdate == 0) {
            lastFrameUpdate = now;
        }
        
        // Always draw current frame
        g2d.drawImage(scenes[currentScene][currentFrame], 0, 0, width, height, null);

        //specific trigger for scene 5
        if(currentFrame == 3 && currentScene == 4){
            isOnChoice = true;
        }
        
        if (now - lastFrameUpdate > FRAME_DURATION && !isOnChoice){
            if(currentFrame == 4 && currentScene == 4){
                isScenePlaying = false;
                currentFrame = 0;
                lastFrameUpdate = 0;
                lastFinishedScene = currentScene;
                return;
            }
            currentFrame++;
            lastFrameUpdate = now; 
        }
    }

    /**
     * Gets the GameCanvas stored in the GameCanvas field
     * @param input keyinput identifier specificied in the gameframe
     */
    public void handleInput(String input){
        if (!isOnChoice){
            if (input.equals("ESC") && (System.currentTimeMillis() - lastFrameUpdate > CHOICE_CD_DURATION) &&
             !(currentScene == 4 && (currentFrame == 4 || currentFrame == 5))){
                isScenePlaying = false;
                currentFrame = 0;
                lastFrameUpdate = 0;
                lastFinishedScene = currentScene;
            }
        }
        else{
            if (input.equals("ESC")) currentFrame = 4;
            else currentFrame = 5;
            lastFrameUpdate = 0; 
            isOnChoice = false;
        }
        
    }

    /**
     * Gets isScenePlaying boolean
     * @return the isScenePlaying field
     */
    public boolean getIsScenePlaying(){
        return isScenePlaying;
    }

    /**
     * Sets the isScenePlaying field to b
     */
    public void setIsScenePlaying(boolean b){
        isScenePlaying = b;
    }
    
}