import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**     
        The PlayerUI class handles the rendering of UI elements related to Player.
        These include Player data like health, level, experience bar, and items.
        It also displays a health bar for bosses.

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

public class PlayerUI extends GameObject{
    
    private static BufferedImage[] sprites;
    private final Font levelFont;
    private final Font stageFont;
    private String currentStageName;

    // Set sprites and font on class
    static {
        setSprites();
    }

    public PlayerUI(Font gameFont){
        levelFont = gameFont.deriveFont(7f);
        stageFont = gameFont.deriveFont(4f);
    }

    /**
     * Set sprites on class initialization
     */
    private static void setSprites() {
        try {
            BufferedImage asset0 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/fullheart.png"));
            BufferedImage asset1 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/halfheart.png"));
            BufferedImage asset2 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/anchovy.png"));
            BufferedImage asset3 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/archerfish.png"));
            BufferedImage asset4 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/pufferfish.png"));
            BufferedImage asset5 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/emptyheart.png"));
            BufferedImage asset6 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/halfemptyheart.png"));
            sprites = new BufferedImage[] {asset0, asset1, asset2, asset3, asset4, asset5, asset6};
        } catch (IOException e) {
            System.out.println("Exception in PlayerUI setSprites()" + e);
        }
    }

    @Override
    public void matchHitBoxBounds() {}

    public void drawStartWarningTab(Graphics2D g2d){
        g2d.setColor(Color.WHITE);
        g2d.fillRect(161, 208, 477, 195);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(166, 212, 467, 188);
    }

    public void drawPauseTab(Graphics2D g2d, int sf){
        // g2d.setColor(Color.WHITE);
        // g2d.fillRect(161/sf, 163/sf, 477/sf, 316/sf);
        // g2d.setColor(Color.BLACK);
        // g2d.fillRect(165/sf, 167/sf, 468/sf, 308/sf);
    }

    /**
     * Draws all of the UI elements
     * @param g2d graphics2d object
     * @param clientmaster the clientmaster
     * @param sf the scalefactor of gamecanvas
     */
    public void drawPlayerUI(Graphics2D g2d, ClientMaster clientMaster, int sf){
        Player userPlayer = clientMaster.getUserPlayer();
        int currentStage = clientMaster.getCurrentStage();
        int userHealth = userPlayer.getHitPoints();
        int userMaxHealth = userPlayer.getMaxHealth();
        double xpBarPercent = clientMaster.getXPBarPercent();
        int userLvl = clientMaster.getUserLvl();
        
        // System.out.println("UI HEALTH: " + userHealth);

        //PORTRAIT UI ELEMENTS
        // Ellipse2D.Double portraitBG1 = new Ellipse2D.Double(21.4/sf, 23/sf, 102.4/sf, 102.4/sf);
        // g2d.setColor(Color.decode("#003a40"));
        // g2d.fill(portraitBG1);

        // Ellipse2D.Double portraitBG2 = new Ellipse2D.Double(30.1/sf, 31.8/sf, 84.9/sf, 84.9/sf);
        // g2d.setColor(Color.decode("#005962"));
        // g2d.fill(portraitBG2);        

        String identifier = userPlayer.getIdentifier();
        if (identifier.equals(NetworkProtocol.HEAVYCAT)) g2d.drawImage(sprites[4], 43/sf, 39/sf, 71/sf, 71/sf, null);
        else if (identifier.equals(NetworkProtocol.GUNCAT)) g2d.drawImage(sprites[3], 43/sf, 39/sf, 71/sf, 71/sf, null);
        else if (identifier.equals(NetworkProtocol.FASTCAT)) g2d.drawImage(sprites[2], 43/sf, 39/sf, 71/sf, 71/sf, null);

        //ITEM UI ELEMENTS
        Entity heldItem = clientMaster.generateUIItem();
        if (heldItem != null) heldItem.draw(g2d, 80/sf, 76/sf);

        //LEVELING SYSTEM UI ELEMENTS
        g2d.setColor(Color.WHITE);
        g2d.setFont(levelFont);  
        g2d.drawString("LVL " + userLvl, 142/sf, 95/sf);

        Rectangle2D.Double xpBarBorder = new Rectangle2D.Double(143.7/sf, 98.1/sf, 195.5/sf, 10/sf);
        g2d.setColor(Color.WHITE);
        g2d.fill(xpBarBorder);

        Rectangle2D.Double xpBarBG = new Rectangle2D.Double(147.5/sf, 101.1/sf, 188.9/sf, 4.5/sf);
        g2d.setColor(Color.BLACK);
        g2d.fill(xpBarBG);

        double xpBarWidth = Math.floor(188.9*(xpBarPercent/100));
        Rectangle2D.Double xpBar = new Rectangle2D.Double(147.5/sf, 101.1/sf, xpBarWidth/sf, 4.5/sf);
        g2d.setColor(Color.decode("#1ed600"));
        g2d.fill(xpBar);

        //Stage information
        switch (currentStage){
            case 0:
                currentStageName = "JUNKYARD";
                break;
            case 1:
                currentStageName = "CITY STREETS";
                break;
            case 2:
                currentStageName = "PET STORE";
                break;
            case 3:
                currentStageName = "FOREST";
                break;
            case 4:
                currentStageName = "MANSION";
                break;
            case 5:
                currentStageName = "ABANDONED BUILDING";
                break;
            case 6:
                currentStageName = "SEWERS";
                break;
            default:
                currentStageName = "";
        }
        g2d.setColor(Color.WHITE);
        g2d.setFont(stageFont);
        g2d.drawString(currentStageName, 637/sf, 34/sf);
        //TODO: Map display

        //Empty Health
        double xOffset = 143.7 / sf;
        double yOffset = 33 / sf;
        int fullMaxHearts = userMaxHealth / 2;
        boolean hasHalfMaxHeart = (userMaxHealth % 2) == 1;

        for (int i = 0; i < fullMaxHearts; i++) {
            g2d.drawImage(sprites[5], (int)xOffset, (int)yOffset, 36 / sf, 36 / sf, null); 
            xOffset += 47 / sf; 
        }

        if (hasHalfMaxHeart) {
            g2d.drawImage(sprites[6], (int)xOffset, (int)yOffset, 36 / sf, 36 / sf, null); 
        }

        //Live Health
        xOffset = 143.7 / sf;
        int fullHearts = userHealth / 2;
        boolean hasHalfHeart = (userHealth % 2) == 1;
        for (int i = 0; i < fullHearts; i++) {
            g2d.drawImage(sprites[0], (int)xOffset, (int)yOffset, 36 / sf, 36 / sf, null); 
            xOffset += 47 / sf; 
        }

        if (hasHalfHeart) {
            g2d.drawImage(sprites[1], (int)xOffset, (int)yOffset, 36 / sf, 36 / sf, null); 
        }

        //BOSS HP BAR
        double bossHPBarPercent = clientMaster.getBossHPPercent();
        if (bossHPBarPercent > 0){
            Rectangle2D.Double bossBarBorder = new Rectangle2D.Double(88.1/sf, 521/sf, 623.7/sf, 19/sf);
            g2d.setColor(Color.WHITE);
            g2d.fill(bossBarBorder);

            Rectangle2D.Double bossBarBG = new Rectangle2D.Double(95.5/sf, 524.4/sf, 609/sf, 11/sf);
            g2d.setColor(Color.BLACK);
            g2d.fill(bossBarBG);

            double bossHPBarWidth = Math.floor(609*(bossHPBarPercent/100));
            Rectangle2D.Double bossHPBar = new Rectangle2D.Double(95.5/sf, 524.4/sf, bossHPBarWidth/sf, 11/sf);
            g2d.setColor(Color.decode("#e63d3d"));
            g2d.fill(bossHPBar);
        }
        
    }
}