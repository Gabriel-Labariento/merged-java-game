import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The SpecialFrameHandler class is a class that creates a 2D array of 
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
public class SpecialFrameHandler{
    private static BufferedImage[][] scenes;
    private static BufferedImage deathScreen;
    private static final int FRAME_DURATION = 8000;
    private int currentScene;
    private int currentFrame;
    private long lastFrameUpdate;
    private boolean isScenePlaying;
    private boolean isOnChoice;
    private boolean canReturnToMenu;
    
    /**
     * Set sprites on class initialization
     */
    static {
        try {
            BufferedImage scene1A = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene1A.png"));
            BufferedImage scene1B = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene1B.png"));
            BufferedImage scene1C = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene1C.png"));
            BufferedImage scene2A = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene2A.png"));
            BufferedImage scene2B = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene2B.png"));
            BufferedImage scene2C = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene2C.png"));
            BufferedImage scene3A = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene3A.png"));
            BufferedImage scene3B = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene3B.png"));
            BufferedImage scene3C = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene3C.png"));
            BufferedImage scene3D = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene3D.png"));
            BufferedImage scene4A = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene4A.png"));
            BufferedImage scene4B = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene4B.png"));
            BufferedImage scene5A = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene5A.png"));
            BufferedImage scene5B = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene5B.png"));
            BufferedImage scene6A = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene6A.png"));
            BufferedImage scene6B = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene6B.png"));
            BufferedImage scene6C = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene6C.png"));
            BufferedImage scene6D = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene6D.png"));
            BufferedImage scene6E = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene6E.png"));
            BufferedImage scene6F = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene6F.png"));
            BufferedImage scene6G = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene6G.png"));
            BufferedImage scene6H = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene6H.png"));
            BufferedImage scene6I = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene6I.png"));
            BufferedImage scene7A = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene7A.png"));
            BufferedImage scene7B = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene7B.png"));
            BufferedImage scene7C = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene7C.png"));
            BufferedImage scene7D = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene7D.png"));
            BufferedImage scene8A = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene8A.png"));
            BufferedImage scene8B = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene8B.png"));
            BufferedImage scene8C = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene8C.png"));
            BufferedImage scene8D = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene8D.png"));
            BufferedImage scene8E = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene8E.png"));
            BufferedImage scene8F = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene8F.png"));
            BufferedImage scene8G = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene8G.png"));
            BufferedImage scene8H = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene8H.png"));
            BufferedImage scene8I = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene8I.png"));
            BufferedImage scene8J = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Scenes/scene8J.png"));
            deathScreen = ImageIO.read(SpecialFrameHandler.class.getResourceAsStream("resources/Misc/gameOver.png"));
            scenes = new BufferedImage[][] {
                {scene1A, scene1B, scene1C},
                {scene2A, scene2B, scene2C},
                {scene3A, scene3B, scene3C, scene3D},
                {scene4A, scene4B},
                {scene5A, scene5B},
                {scene6A, scene6B, scene6C, scene6D, scene6E, scene6F, scene6G, scene6H, scene6I},
                {scene7A, scene7B, scene7C, scene7D},
                {scene8A, scene8B, scene8C, scene8D, scene8E, scene8F, scene8G, scene8H, scene8I, scene8J}
            };

        } catch (IOException e) {
            System.out.println("Exception in Rat setSprites()" + e);
        }
    }

    public void drawDeathScreen(Graphics2D g2d, int width, int height){
        g2d.drawImage(deathScreen, 0, 0, width, height, null);
        canReturnToMenu = true;
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
        currentScene = currentStage;
        canReturnToMenu = (currentScene == 7 && currentFrame == 9) || (currentScene == 5 && currentFrame == 8);
        
        //Initiate frame update variables
        if (lastFrameUpdate == 0) lastFrameUpdate = now;

        //Check if last frame
        if (currentFrame >= scenes[currentScene].length){
            isScenePlaying = false;
            currentFrame = 0;
            lastFrameUpdate = 0;
            isOnChoice = false;
            return;
        }
        
        // Always draw current frame
        g2d.drawImage(scenes[currentScene][currentFrame], 0, 0, width, height, null);

        //specific trigger for scene 5
        if(currentFrame == 3 && currentScene == 5){
            isOnChoice = true;
        }
        
        // System.out.println(now - lastFrameUpdate + " - " + FRAME_DURATION);
        if (now - lastFrameUpdate > FRAME_DURATION && !isOnChoice){
            if(currentFrame == 4 && currentScene == 5){
                isScenePlaying = false;
                currentFrame = 0;
                lastFrameUpdate = 0;
                return;
            }
            moveToNextFrame();
        }
    }

    /**
     * Gets the GameCanvas stored in the GameCanvas field
     * @param input keyinput identifier specificied in the gameframe
     */
    public void handleKeyInput(String input){
        boolean isEscEnabled = !(currentScene == 5 && currentFrame >= 4) && !(currentScene == 7);
        

        if (isOnChoice){
            if (input.equals("ESC")) currentFrame = 4;
            else currentFrame = 5;
            isOnChoice = false;
            lastFrameUpdate = 0;
        }
        else{
            if (input.equals("ESC") && isEscEnabled){
                if (currentScene == 5) currentFrame = 3;
                else{
                    isScenePlaying = false;
                    currentFrame = 0;
                }
                lastFrameUpdate = 0;
            }
        }   
    }

    public void handleClickInput(){
        moveToNextFrame();
    }

    public void moveToNextFrame(){
        boolean isOnRestrictedFrame = (currentScene == 7 && currentFrame == 9)
                                        || (currentScene == 5 && (currentFrame == 8 || currentFrame == 4));

        //Restrict frame movement during certain frames
        if (!isOnRestrictedFrame && !isOnChoice) {
            currentFrame++;
            lastFrameUpdate = 0;
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

    public boolean getCanReturnToMenu(){
        return canReturnToMenu;
    }

    public void setCanReturnToMenu(boolean b){
        canReturnToMenu = b;
    }
    
}