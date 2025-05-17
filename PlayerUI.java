import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PlayerUI extends GameObject{
    
    private static BufferedImage[] sprites;
    private static Font gameFont;

    static {
        setSprites();
        setFont();
    }

    private static void setFont(){
        try {
            gameFont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/Fonts/PressStart2P-Regular.ttf"));
            gameFont = gameFont.deriveFont(7f);
        } catch (FontFormatException ex) {
        } catch (IOException ex) {
        }
            
        
    }

    private static void setSprites() {
        try {
            BufferedImage asset0 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/fullheart.png"));
            BufferedImage asset1 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/halfheart.png"));
            BufferedImage asset2 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/anchovy.png"));
            BufferedImage asset3 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/archerfish.png"));
            BufferedImage asset4 = ImageIO.read(PlayerUI.class.getResourceAsStream("resources/UserInterface/pufferfish.png"));
            sprites = new BufferedImage[] {asset0, asset1, asset2, asset3, asset4};
        } catch (IOException e) {
            System.out.println("Exception in RatKing setSprites()" + e);
        }
    }

    @Override
    public void matchHitBoxBounds() {}

    public void drawPlayerUI(Graphics2D g2d, Player userPlayer, ClientMaster clientMaster, int sf){
        int userHealth = userPlayer.getHitPoints();
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
        g2d.setFont(gameFont);  
        g2d.drawString("LVL " + userLvl, 142/sf, 95/sf);

        Rectangle2D.Double xpBarBorder = new Rectangle2D.Double(143.7/sf, 98.1/sf, 195.5/sf, 10/sf);
        g2d.setColor(Color.WHITE);
        g2d.fill(xpBarBorder);

        Rectangle2D.Double xpBarBG = new Rectangle2D.Double(147.5/sf, 101.1/sf, 188.9/sf, 4.5/sf);
        g2d.setColor(Color.decode("#808080"));
        g2d.fill(xpBarBG);

        double xpBarWidth = Math.floor(188.9*(xpBarPercent/100));
        Rectangle2D.Double xpBar = new Rectangle2D.Double(147.5/sf, 101.1/sf, xpBarWidth/sf, 4.5/sf);
        g2d.setColor(Color.decode("#1ed600"));
        g2d.fill(xpBar);

        double xOffset = 143.7 / sf;
        double yOffset = 33 / sf;

        int fullHearts = userHealth / 2;
        boolean hasHalfHeart = (userHealth % 2) == 1;

        for (int i = 0; i < fullHearts; i++) {
            g2d.drawImage(sprites[0], (int)xOffset, (int)yOffset, 36 / sf, 36 / sf, null); 
            xOffset += 50 / sf; 
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
            g2d.setColor(Color.decode("#808080"));
            g2d.fill(bossBarBG);

            double bossHPBarWidth = Math.floor(609*(bossHPBarPercent/100));
            Rectangle2D.Double bossHPBar = new Rectangle2D.Double(95.5/sf, 524.4/sf, bossHPBarWidth/sf, 11/sf);
            g2d.setColor(Color.decode("#e63d3d"));
            g2d.fill(bossHPBar);
        }
        
    }




}