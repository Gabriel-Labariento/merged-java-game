import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**     
        The GameFrame class extends JFrame. It serves as the main game window.
        It holds a reference to GameCanvas (for rendering) and GameClient (for Player
        connectivity). It also holds UI elements and handles input reception.

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

public class GameFrame extends JFrame{
    private final int width, height;
    private final String title;
    private String serverIP;
    private int serverPort;
    private String playerType;
    private final JLayeredPane lp;
    private final JPanel cp;  
    private GameCanvas gameCanvas;
    private GameClient gameClient;
    private ClientMaster clientMaster;
    private SpecialFrameHandler specialFrameHandler;
    private final ArrayList<JButton> btns;
    private final ArrayList<JButton> blackWhiteBtns;
    private final JLabel gameTitle;
    private final JLabel ipLabel;
    private final JLabel portLabel;
    private final JLabel catNameLabel;
    private final JLabel carouselLabel;
    private final JLabel startWarningLabel;
    // private final JLabel fishLabel;
    private final JLabel pauseTitleLabel;
    private final JLabel volumeMainLabel;
    private final JLabel volumeSubLabel;
    private final JTextField ipTextField;
    private final JTextField portTextField;
    private final JSlider sfxVolumeSlider;
    private final JSlider musicVolumeSlider;
    private final JSlider masterVolumeSlider;
    private int fishSlideNum;
    public boolean isGamePaused;
    private SoundManager soundManager;
    private static BufferedImage sliderThumb;
    public boolean isSinglePlayer;

    static{
        try {
            gameFont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/Fonts/PressStart2P-Regular.ttf"));
            sliderThumb = ImageIO.read(GameFrame.class.getResourceAsStream("resources/UserInterface/sliderThumb.png"));
        } catch (FontFormatException e) {
        } catch (IOException e) {
        }
    }

    private boolean isMultiPlayerMode;

    private static CatCarousel catCarousel;

    private static Font gameFont;
    private static Font gameFont6;
    private static Font gameFont11;
    private static Font gameFont12;
    private static Font gameFont14;
    private static Font gameFont15;
    private static Font gameFont16;
    private static Font gameFont20;
    private static Font gameFont35;
    
    // Set sprites and font on class
    static {
        setFont();
    }

    /**
     * Loads font from the resources folder
     */
    private static void setFont(){
        try {
            gameFont = Font.createFont(Font.TRUETYPE_FONT, 
                GameFrame.class.getResourceAsStream("/resources/Fonts/PressStart2P-Regular.ttf"));
            gameFont6 = gameFont.deriveFont(6f);
            gameFont11 = gameFont.deriveFont(11f);
            gameFont12 = gameFont.deriveFont(12f);
            gameFont14 = gameFont.deriveFont(14f);
            gameFont15 = gameFont.deriveFont(15f);
            gameFont16 = gameFont.deriveFont(16f);
            gameFont20 = gameFont.deriveFont(20f);
            gameFont35 = gameFont.deriveFont(35f);
            
        } catch (Exception ex) { 
            System.out.println("Exception in setFont for GameFrame");
        }
    }

    /**
     * Creates a GameFrame with a new GameCanvas and holds a reference 
     * to the GameCanvas' gameClient. It also creates UI elements for 
     * game initialization
     * @param width the frame's width
     * @param height the frame's height
     * @param title the title at the top of the frame
     */
    public GameFrame(int width, int height, String title){
        this.width = width;
        this.height = height;
        this.title = title;
        this.soundManager = SoundManager.getInstance();
        cp = (JPanel) this.getContentPane();
        gameCanvas = null;
        isGamePaused = false;

        //Set default values
        playerType = NetworkProtocol.HEAVYCAT;
        fishSlideNum = 1;

        // UI Elements
        btns = new ArrayList<>();
        btns.add(new JButton("MULTIPLAYER")); // 0
        btns.add(new JButton("QUIT")); // 1
        btns.add(new JButton("CONNECT")); // 2
        btns.add(new JButton("BACK")); // 3
        btns.add(new JButton("HOST SERVER")); // 4
        btns.add(new JButton("ENTER GAME")); // 5
        btns.add(new JButton("<")); // 6
        btns.add(new JButton(">")); // 7
        btns.add(new JButton()); // 8
        btns.add(new JButton("BACK")); // 9
        btns.add(new JButton("CONTINUE")); // 10
        btns.add(new JButton("START")); // 11
        btns.add(new JButton("YES")); // 12
        btns.add(new JButton("NO")); // 13

        blackWhiteBtns = new ArrayList<>();
        blackWhiteBtns.add(btns.get(0));
        blackWhiteBtns.add(btns.get(1));
        blackWhiteBtns.add(btns.get(4));
        blackWhiteBtns.add(btns.get(5));
        blackWhiteBtns.add(btns.get(6));
        blackWhiteBtns.add(btns.get(7));
        blackWhiteBtns.add(btns.get(8));
        blackWhiteBtns.add(btns.get(9));
        blackWhiteBtns.add(btns.get(10));
        blackWhiteBtns.add(btns.get(11));
        blackWhiteBtns.add(btns.get(12));
        blackWhiteBtns.add(btns.get(13));

        gameTitle = new JLabel("BITING ON FISH", SwingConstants.CENTER);
        ipLabel = new JLabel();
        portLabel = new JLabel();
        catNameLabel = new JLabel();
        //Use html for custom text styling
        startWarningLabel = new JLabel("<html><center>WARNING:<br>Your previous progress will be lost.<br><br><br>Continue anyway?</center></html>");
        carouselLabel = new JLabel("CHOOSE A FISH:");
        pauseTitleLabel = new JLabel("--- PAUSED ---", SwingConstants.CENTER);
        volumeMainLabel = new JLabel("MASTER VOLUME:");
        volumeSubLabel = new JLabel("<html>Sound Effects:<br><br>Music:</html>");
        
        ipTextField = new JTextField(12);
        portTextField = new JTextField(12);

        sfxVolumeSlider = new JSlider(0, 100, ((int)(soundManager.getSfxVolume()*100.0)));
        musicVolumeSlider = new JSlider(0, 100, ((int) (soundManager.getMusicVolume()*100.0)));
        masterVolumeSlider = new JSlider(0, 100, ((int) (soundManager.getMasterVolume()*100.0)));

        lp = new JLayeredPane();

        createClientClasses();
    }

    private void createClientClasses(){
        //Account for already existing gameCanvas
        if (gameCanvas != null) {
            lp.remove(gameCanvas);
            //Stop from renderloop thread from lingering
            gameCanvas.stopRenderLoop();
        }

        //Create classes
        gameCanvas = new GameCanvas(width, height, gameFont, isSinglePlayer);
        clientMaster = gameCanvas.getClientMaster();
        gameClient = gameCanvas.getGameClient();
        specialFrameHandler = gameCanvas.getSpecialFrameHandler();

        //Configure new instance of gameCanvas
        gameCanvas.setBounds(0, 0, width, height);
        gameCanvas.setVisible(true);
        lp.add(gameCanvas, Integer.valueOf(0));
        gameCanvas.startRenderLoop();
    }
    /**
     * Sets up the main frame window components.
     * This is called after gameFrame creation to start the game
     */
    public void setUpGUI() {     
        setTitle(title);
        setResizable(false);
        cp.setFocusable(true);
        lp.setPreferredSize(new Dimension(width, height));

        setUpSliders();
        loadStartUI();
        refreshFrame();

        cp.add(lp, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    /**
     * Loads into the screen the Play and Quit buttons using absolute values
     */
    public void loadStartUI(){
        gameTitle.setForeground(Color.WHITE);
        gameTitle.setBounds(89, 115, 622, 56);
        gameTitle.setFont(getSizedGameFont(43));
        lp.add(gameTitle, Integer.valueOf(2));

        JButton continueBtn = btns.get(10);
        continueBtn.setBackground(Color.white);
        continueBtn.setForeground(Color.black);
        continueBtn.setBounds(280, 240, 240, 43);
        continueBtn.setFont(getSizedGameFont(15));
        lp.add(continueBtn, Integer.valueOf(2));

        if (doesSaveExist()) continueBtn.setEnabled(true);
        else continueBtn.setEnabled(false);

        JButton startBtn = btns.get(11);
        startBtn.setBackground(Color.white);
        startBtn.setForeground(Color.black);
        startBtn.setBounds(280, 312, 240, 43);
        startBtn.setFont(getSizedGameFont(15));
        lp.add(startBtn, Integer.valueOf(2));

        JButton multiPlayerBtn = btns.get(0);
        multiPlayerBtn.setBackground(Color.white);
        multiPlayerBtn.setForeground(Color.black);
        multiPlayerBtn.setBounds(280, 387, 240, 43);
        multiPlayerBtn.setFont(getSizedGameFont(15));
        lp.add(multiPlayerBtn, Integer.valueOf(2));

        JButton quitButton = btns.get(1);
        quitButton.setBackground(Color.white);
        quitButton.setForeground(Color.black);
        quitButton.setBounds(280, 458, 240, 43);
        quitButton.setFont(getSizedGameFont(15));
        lp.add(quitButton, Integer.valueOf(2));
    }

    public void loadWarningPanel(){
        warningPanel.setBounds(166, 212, 467, 188);
        warningPanel.setBackground(Color.BLACK);
        warningPanel.setForeground(Color.white);
        warningPanel.setBorder(new LineBorder(Color.white, 3));

        // Add warning text
        warningPanelLabel[0] = new JLabel("WARNING!");
        warningPanelLabel[0].setBounds(329, 230, 141, 19);

        warningPanelLabel[1] = new JLabel("Your previous progress will be lost.");
        warningPanelLabel[1].setBounds(183, 253, 434, 41);

        warningPanelLabel[2] = new JLabel("Continue anyway?");
        warningPanelLabel[2].setBounds(276, 318, 248, 19);

        for (JLabel wpl : warningPanelLabel) {
            wpl.setForeground(Color.WHITE);
            wpl.setFont(gameFont12);
            wpl.setHorizontalAlignment(SwingConstants.CENTER);
            lp.add(wpl, Integer.valueOf(3));
        }

        // Set up Yes button
        JButton yesQuitButton = btns.get(10);
        yesQuitButton.setBackground(Color.white);
        yesQuitButton.setForeground(Color.black);
        yesQuitButton.setBounds(255, 348, 126, 36);
        yesQuitButton.setFont(gameFont11);
        lp.add(yesQuitButton, Integer.valueOf(3));

        // Set up No button
        JButton noQuitButton = btns.get(11);
        noQuitButton.setBackground(Color.white);
        noQuitButton.setForeground(Color.black);
        noQuitButton.setBounds(422, 348, 126, 36);
        noQuitButton.setFont(gameFont11);
        lp.add(noQuitButton, Integer.valueOf(3));

        lp.add(warningPanel, Integer.valueOf(2));
    }
    
    /**
     * Sets up hover effect mouse listeners for different GUI screens
     * @param sourceMethod 0: loadStartUI, 1: loadClientUI, 2: loadPrePlayUI
     */
    private void setUpButtonHoverEffects(int sourceMethod){
        switch (sourceMethod) {
            case 0: // Start UI
                addColorHoverEffect(btns.get(0)); // Start button
                addColorHoverEffect(btns.get(1)); // Quit button
                addColorHoverEffect(btns.get(8)); // Continue button
                addColorHoverEffect(btns.get(9)); // Multiplayer button
                addColorHoverEffect(btns.get(10)); // Warning - Yes
                addColorHoverEffect(btns.get(11)); // Warning - No
                break;
            case 1: // Client UI
                addLineEffect(btns.get(2)); // Connect Button
                addLineEffect(btns.get(3)); // Back Button
                addColorHoverEffect(btns.get(4)); // Host Server
                break;
            case 2: // Pre-play UI
                addLineEffect(btns.get(3)); // Back Button
                addColorHoverEffect(btns.get(5)); // Enter Game Button
                addColorHoverEffect(btns.get(6)); // < Button
                addColorHoverEffect(btns.get(7)); // > Button
                break;
            default:
                throw new AssertionError("Assertion in setUpButtonHoverEffects()");
        }
    }
    
    /**
     * Loads the pre-game screen needed for client-server connection
     */
    public void loadClientUI(){
        isMultiPlayerMode = true;

        ipLabel.setForeground(Color.WHITE);
        ipLabel.setText("IP ADDRESS:");
        ipLabel.setBounds(85, 128, 244, 25);
        ipLabel.setFont(getSizedGameFont(16));
        lp.add(ipLabel, Integer.valueOf(2));

        
        portLabel.setForeground(Color.WHITE);
        portLabel.setText("PORT NUMBER:");
        portLabel.setBounds(85, 196, 244, 25);
        portLabel.setFont(getSizedGameFont(16));
        lp.add(portLabel, Integer.valueOf(2));

        ipTextField.setBounds(336, 128, 371, 26);
        ipTextField.setBackground(Color.black);
        ipTextField.setForeground(Color.white);
        ipTextField.setFont(getSizedGameFont(16));
        ipTextField.setFocusable(true);
        lp.add(ipTextField, Integer.valueOf(2));

        portTextField.setBounds(336, 196, 371, 26);
        portTextField.setBackground(Color.black);
        portTextField.setForeground(Color.white);
        portTextField.setFont(getSizedGameFont(16));
        lp.add(portTextField, Integer.valueOf(2));

        JButton connectButton = btns.get(2);
        connectButton.setBackground(Color.black);
        connectButton.setForeground(Color.white);
        connectButton.setBorderPainted(false);
        connectButton.setFont(getSizedGameFont(25));
        connectButton.setBounds(64, 274, 220, 31);
        lp.add(connectButton, Integer.valueOf(2));

        // Add white line below connect button
        JPanel connectLinePanel = new JPanel();
        connectLinePanel.setBackground(Color.WHITE);
        connectLinePanel.setBounds(88, 306, 166, 5);
        lp.add(connectLinePanel, Integer.valueOf(2));
        connectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                connectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                connectLinePanel.setBounds(162, 306, 24, 5);
            }
        
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                connectButton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                connectLinePanel.setBounds(88, 306, 166, 5);
            }
        });

        JButton backButton = btns.get(3);
        backButton.setBackground(Color.black);
        backButton.setForeground(Color.white);
        backButton.setBorderPainted(false);
        backButton.setFont(getSizedGameFont(25));
        backButton.setBounds(561, 274, 200, 31);
        lp.add(backButton, Integer.valueOf(2));

        // Add white line below connect button
        JPanel backLinePanel = new JPanel();
        backLinePanel.setBackground(Color.WHITE);
        backLinePanel.setBounds(610, 306, 97, 5);
        lp.add(backLinePanel, Integer.valueOf(2));

        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                backLinePanel.setBounds(645, 306, 24, 5);
            }
        
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backButton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                backLinePanel.setBounds(610, 306, 97, 5);
            }
        });

        JButton hostServerButton = btns.get(4);
        hostServerButton.setBackground(Color.white);
        hostServerButton.setForeground(Color.black);
        hostServerButton.setBounds(85, 396, 307, 58);
        hostServerButton.setFont(getSizedGameFont(20));
        lp.add(hostServerButton, Integer.valueOf(2));
    }

    public void loadSinglePrePlayUI(){
        JButton enterGameButton = btns.get(5);
        enterGameButton.setBackground(Color.white);
        enterGameButton.setForeground(Color.black);
        enterGameButton.setBounds(447, 253, 287, 56);
        enterGameButton.setFont(getSizedGameFont(20));
        lp.add(enterGameButton, Integer.valueOf(2));

         // QUIT BUTTON
        JButton backBtn = btns.get(3);
        backBtn.setBackground(Color.black);
        backBtn.setForeground(Color.white);
        backBtn.setBorderPainted(false);
        backBtn.setFont(getSizedGameFont(25));
        backBtn.setHorizontalAlignment(SwingConstants.CENTER);
        backBtn.setBounds(495, 338, 200, 31);
    
        // Add white line below connect button
        JPanel backLinePanel = new JPanel();
        backLinePanel.setBackground(Color.WHITE);
        backLinePanel.setOpaque(true);
        backLinePanel.setBounds(546, 369, 97, 5);
        lp.add(backLinePanel, Integer.valueOf(2));

        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                backLinePanel.setBounds(583, 369, 24, 5);
            }
        
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                backLinePanel.setBounds(546, 369, 97, 5);
            }
        });

        lp.add(backBtn, Integer.valueOf(2));

        

        catCarousel = new CatCarousel();
        catCarousel.setBounds(171, 242, 125, 117);
        lp.add(catCarousel, Integer.valueOf(2));
        
        
        
        
        updateFishCarousel();
        catNameLabel.setForeground(Color.WHITE);
        catNameLabel.setBounds(127, 381, 217, 12);
        catNameLabel.setFont(getSizedGameFont(11));
        catNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lp.add(catNameLabel, Integer.valueOf(2));

        carouselLabel.setBackground(Color.black);
        carouselLabel.setForeground(Color.white);
        carouselLabel.setBounds(73, 171, 349, 30);
        carouselLabel.setFont(getSizedGameFont(24));
        carouselLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lp.add(carouselLabel, Integer.valueOf(2));

        JPanel carouselBorder = new JPanel();
        carouselBorder.setBackground(Color.WHITE);
        carouselBorder.setOpaque(true);
        carouselBorder.setBounds(167, 238, 133, 124);
        lp.add(carouselBorder, Integer.valueOf(2));

        // LEFT BUTTON
        JButton leftButton = btns.get(6);
        leftButton.setBounds(170, 415, 61, 38);
        leftButton.setHorizontalAlignment(SwingConstants.CENTER);
        leftButton.setBackground(Color.black);
        leftButton.setForeground(Color.white);
        leftButton.setBorderPainted(false);
        leftButton.setFont(getSizedGameFont(19));
        lp.add(leftButton, Integer.valueOf(2));

        // RIGHT BUTTON
        JButton rightButton = btns.get(7);
        rightButton.setBounds(235, 415, 61, 38);
        rightButton.setHorizontalAlignment(SwingConstants.CENTER);
        rightButton.setBackground(Color.black);
        rightButton.setForeground(Color.white);
        rightButton.setBorderPainted(false);
        rightButton.setFont(getSizedGameFont(19));
        lp.add(rightButton, Integer.valueOf(2));

    }

    /**
     * Loads the pre-game UI displaying the serverIP address, the port number,
     * and allows the user to select a player type.
     */
    public void loadMultiPrePlayUI(){
        ipLabel.setForeground(Color.WHITE);
        ipLabel.setText("IP ADDRESS:     " + serverIP);
        ipLabel.setBounds(85, 128, 558, 25);
        lp.add(ipLabel, Integer.valueOf(2));
        
        // PORT NUMBER
        portLabel.setForeground(Color.WHITE);
        portLabel.setText("PORT NUMBER:    " + serverPort);
        portLabel.setBounds(85, 196, 558, 25);
        lp.add(portLabel, Integer.valueOf(2));

        // ENTER GAME BUTTON
        JButton enterGameButton = btns.get(5);
        enterGameButton.setBackground(Color.white);
        enterGameButton.setForeground(Color.black);
        enterGameButton.setBounds(447, 391, 287, 56);
        enterGameButton.setFont(getSizedGameFont(20));
        lp.add(enterGameButton, Integer.valueOf(2));


        catCarousel = new CatCarousel();
        lp.add(catCarousel, Integer.valueOf(2));

        updateFishCarousel();
        catNameLabel.setForeground(Color.WHITE);
        catNameLabel.setBounds(130, 504, 179, 10);
        catNameLabel.setFont(getSizedGameFont(9));
        catNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lp.add(catNameLabel, Integer.valueOf(2));

        carouselLabel.setBackground(Color.black);
        carouselLabel.setForeground(Color.white);
        carouselLabel.setBounds(80, 323, 288, 25);
        carouselLabel.setFont(getSizedGameFont(20));
        carouselLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lp.add(carouselLabel, Integer.valueOf(2));

        JPanel carouselBorder = new JPanel();
        carouselBorder.setBackground(Color.WHITE);
        carouselBorder.setOpaque(true);
        carouselBorder.setBounds(153, 369, 133, 124);
        lp.add(carouselBorder, Integer.valueOf(2));

        // LEFT BUTTON
        JButton leftButton = btns.get(6);
        leftButton.setBounds(168, 528, 50, 31);
        leftButton.setHorizontalAlignment(SwingConstants.CENTER);
        leftButton.setBackground(Color.black);
        leftButton.setForeground(Color.white);
        leftButton.setBorderPainted(false);
        leftButton.setFont(getSizedGameFont(16));
        lp.add(leftButton, Integer.valueOf(2));

        // RIGHT BUTTON
        JButton rightButton = btns.get(7);
        rightButton.setBounds(222, 528, 50, 31);
        rightButton.setHorizontalAlignment(SwingConstants.CENTER);
        rightButton.setBackground(Color.black);
        rightButton.setForeground(Color.white);
        rightButton.setBorderPainted(false);
        rightButton.setFont(getSizedGameFont(16));
        lp.add(rightButton, Integer.valueOf(2));

        // QUIT BUTTON
        JButton backBtn = btns.get(3);
        backBtn.setBackground(Color.black);
        backBtn.setForeground(Color.white);
        backBtn.setBorderPainted(false);
        backBtn.setFont(getSizedGameFont(25));
        backBtn.setHorizontalAlignment(SwingConstants.CENTER);
        backBtn.setBounds(495, 477, 200, 31);
        

        // Add white line below connect button
        JPanel backLinePanel = new JPanel();
        backLinePanel.setBackground(Color.WHITE);
        backLinePanel.setOpaque(true);
        backLinePanel.setBounds(546, 508, 97, 5);
        lp.add(backLinePanel, Integer.valueOf(2));

        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                backLinePanel.setBounds(583, 508, 24, 5);
            }
        
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                backLinePanel.setBounds(546, 508, 97, 5);
            }
        });

        lp.add(backBtn, Integer.valueOf(2));
    }

    public void loadPauseUI(){

        pauseTitleLabel.setForeground(Color.WHITE); 
        pauseTitleLabel.setBounds(187, 194, 426, 35);
        pauseTitleLabel.setFont(getSizedGameFont(28f));
        lp.add(pauseTitleLabel, Integer.valueOf(2));

        volumeMainLabel.setForeground(Color.WHITE);
        volumeMainLabel.setBounds(204, 328, 200, 16);
        volumeMainLabel.setFont(getSizedGameFont(14f));
        lp.add(volumeMainLabel, Integer.valueOf(2));

        volumeSubLabel.setForeground(Color.WHITE);
        volumeSubLabel.setBounds(204, 262, 269, 41);
        volumeSubLabel.setFont(getSizedGameFont(12f));
        lp.add(volumeSubLabel, Integer.valueOf(2));

        
        lp.add(sfxVolumeSlider, Integer.valueOf(2));
        lp.add(musicVolumeSlider, Integer.valueOf(2));
        lp.add(masterVolumeSlider, Integer.valueOf(2));

        JButton pauseDisconnectBtn = btns.get(8);
        if(isSinglePlayer) pauseDisconnectBtn.setText("MAIN MENU");
        else pauseDisconnectBtn.setText("DISCONNECT");

        pauseDisconnectBtn.setBackground(Color.white);
        pauseDisconnectBtn.setForeground(Color.black);
        pauseDisconnectBtn.setBounds(217, 400, 192, 41);
        pauseDisconnectBtn.setFont(getSizedGameFont(15f));
        lp.add(pauseDisconnectBtn, Integer.valueOf(2));        

        JButton pauseBackButton = btns.get(9);
        pauseBackButton.setBackground(Color.white);
        pauseBackButton.setForeground(Color.black);
        pauseBackButton.setFont(getSizedGameFont(15f));
        pauseBackButton.setBounds(482, 400, 96, 41);
        lp.add(pauseBackButton, Integer.valueOf(2)); 
        
        JPanel pauseTabBG2 = new JPanel();
        pauseTabBG2.setBackground(Color.BLACK);
        pauseTabBG2.setOpaque(true);
        pauseTabBG2.setBounds(165, 167, 468, 308);
        lp.add(pauseTabBG2, Integer.valueOf(2));

        JPanel pauseTabBG1 = new JPanel();
        pauseTabBG1.setBackground(Color.WHITE);
        pauseTabBG1.setOpaque(true);
        pauseTabBG1.setBounds(161, 163, 477, 316);
        lp.add(pauseTabBG1, Integer.valueOf(2));
    }

    public void setUpSliders(){
        // setup listeners
        addSliderListeners();

        // setup ui appearance
        sfxVolumeSlider.setBounds(418, 261, 178, 12);
        sfxVolumeSlider.setBackground(Color.black);
        sfxVolumeSlider.setUI(new javax.swing.plaf.basic.BasicSliderUI(sfxVolumeSlider) {
            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = thumbRect.x + (thumbRect.width - sliderThumb.getWidth(null)) / 2;
                int y = thumbRect.y + (thumbRect.height - sliderThumb.getHeight(null)) / 2;

                g2d.drawImage(sliderThumb, x, y, null);
                g2d.dispose();
            }

            @Override
            protected Dimension getThumbSize() {
                return new Dimension(sliderThumb.getWidth(null), sliderThumb.getHeight(null));
            }

            //Delete slider focus border
            @Override
            public void paintFocus(Graphics g){}

            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = trackRect.x;
                int y = trackRect.y + trackRect.height / 2 - 3; 
                int w = trackRect.width;
                int h = 5; 

                // Outer border
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, w, h, 5, 5);

                // Slider Background
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(x + 1, y + 1, w - 2, h - 2, 3, 3);

                // Dynamic filling
                int fillWidth = thumbRect.x + thumbRect.width / 2 - x;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x + 1, y + 1, fillWidth, h - 2, 3, 3);

                g2.dispose();
            }
        });

        musicVolumeSlider.setBounds(418, 291, 178, 12);
        musicVolumeSlider.setBackground(Color.black);
        musicVolumeSlider.setUI(new javax.swing.plaf.basic.BasicSliderUI(musicVolumeSlider) {
            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = thumbRect.x + (thumbRect.width - sliderThumb.getWidth(null)) / 2;
                int y = thumbRect.y + (thumbRect.height - sliderThumb.getHeight(null)) / 2;

                g2d.drawImage(sliderThumb, x, y, null);
                g2d.dispose();
            }

            @Override
            protected Dimension getThumbSize() {
                return new Dimension(sliderThumb.getWidth(null), sliderThumb.getHeight(null));
            }

            //Delete slider focus border
            @Override
            public void paintFocus(Graphics g){}

            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = trackRect.x;
                int y = trackRect.y + trackRect.height / 2 - 3; 
                int w = trackRect.width;
                int h = 5; 

                // Outer border
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, w, h, 5, 5);

                // Slider Background
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(x + 1, y + 1, w - 2, h - 2, 3, 3);

                // Dynamic filling
                int fillWidth = thumbRect.x + thumbRect.width / 2 - x;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x + 1, y + 1, fillWidth, h - 2, 3, 3);

                g2.dispose();
            }
        });


        masterVolumeSlider.setBounds(418, 330, 178, 12);
        masterVolumeSlider.setBackground(Color.black);
        masterVolumeSlider.setUI(new javax.swing.plaf.basic.BasicSliderUI(masterVolumeSlider) {
            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = thumbRect.x + (thumbRect.width - sliderThumb.getWidth(null)) / 2;
                int y = thumbRect.y + (thumbRect.height - sliderThumb.getHeight(null)) / 2;

                g2d.drawImage(sliderThumb, x, y, null);
                g2d.dispose();
            }

            @Override
            protected Dimension getThumbSize(){
                return new Dimension(sliderThumb.getWidth(null), sliderThumb.getHeight(null));
            }

            //Delete slider focus border
            @Override
            public void paintFocus(Graphics g){}

            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int x = trackRect.x;
                int y = trackRect.y + trackRect.height / 2 - 3; 
                int w = trackRect.width;
                int h = 5; 

                // Outer border
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, w, h, 5, 5);

                // Slider Background
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(x + 1, y + 1, w - 2, h - 2, 3, 3);

                // Dynamic filling
                int fillWidth = thumbRect.x + thumbRect.width / 2 - x;
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x + 1, y + 1, fillWidth, h - 2, 3, 3);

                g2.dispose();
            }
        });
    }


    /**
     * Updates the selected player display based on user input
     */
    public void updateFishCarousel(){
        if (fishSlideNum > 3) fishSlideNum = 1; 
        else if (fishSlideNum < 1) fishSlideNum = 3;

        switch(fishSlideNum){
            case 1:
                catNameLabel.setText("PUFFERFISH (HEAVY)");
                playerType = NetworkProtocol.HEAVYCAT;
                break;
            case 2:
                catNameLabel.setText("ANCHOVY (LIGHT)");
                playerType = NetworkProtocol.FASTCAT;
                break;
            case 3:
                catNameLabel.setText("ARCHERFISH (RANGED)");
                playerType = NetworkProtocol.GUNCAT;
                break;
        }
        
        if (catCarousel != null) {
            catCarousel.updatePlayer(fishSlideNum);
        }
        refreshFrame();
    }

    /**
     * Central button handler for all button actions in the game.
     * Handles play/quit buttons, connection input buttons, and 
     * player selection buttons
     */
    public void setUpButtons(){
        ActionListener btnListener = (ActionEvent ae) -> {
            SoundManager.getInstance().playPooledSound("click");

            Object o = ae.getSource();
            
            //multiplayer
            if (o == btns.get(0)){
                clearGUI();
                loadSoloPlayUI();
                refreshFrame();
            }
            //quit
            else if (o == btns.get(1)){
                loadWarningPanel();
                refreshFrame();                
            }
            //connect
            else if (o == btns.get(2)){
                serverIP = ipTextField.getText();
                serverPort = Integer.parseInt(portTextField.getText());
                clearGUI();
                loadMultiPrePlayUI();
                refreshFrame();
            }
            //back
            else if (o == btns.get(3)){
                if (catCarousel != null) catCarousel.stopCarouselRenderLoop();
                clearGUI();
                loadStartUI();
                refreshFrame();
                GameServer gs = gameClient.getGameServer();
                if (gs != null) gs.shutDownServer();
            }
            //host server
            else if (o == btns.get(4)){
                isSinglePlayer = false;
                gameClient.hostServer(isSinglePlayer);
                serverIP = gameClient.getServerIP();
                serverPort = gameClient.getServerPort();
                clearGUI();
                loadMultiPrePlayUI();
                refreshFrame();
            }
            //enter game
            else if (o == btns.get(5)){
                // TODO: CHANGE FOR PROPER SINGLE/MULTI PLAYER SYSTEM
                if (!isMultiPlayerMode) {
                    gameClient.hostServer();
                    serverIP = gameClient.getServerIP();
                    serverPort = gameClient.getServerPort();
                    clearGUI();
                }
                startPlay();
            }
            // LEFT ARROW
            else if (o == btns.get(6)){
                fishSlideNum--;
                updateFishCarousel();
            }
            // RIGHT ARROW
            else if (o == btns.get(7)){
                fishSlideNum++;
                updateFishCarousel();
            }
            //disconnect
            else if (o == btns.get(8)){
                //Return to main menu 
                gameCanvas.setIsOnMenu(true);
                gameClient.setWantsDisconnect(true);
                SoundManager.getInstance().stopAllSounds();
                isGamePaused = false;

                clearGUI();
                loadStartUI();
                refreshFrame();
                
                cp.requestFocusInWindow();
            }
            //pause tab back
            else if (o == btns.get(9)){
                
                clearGUI();
                refreshFrame();
                cp.requestFocusInWindow();
                isGamePaused = false;
            }
            //continue
            else if (o == btns.get(10)){
                if (doesSaveExist()) {
                    isSinglePlayer = true;
                    gameClient.hostServer(isSinglePlayer);
                    serverIP = gameClient.getServerIP();
                    serverPort = gameClient.getServerPort();
                    startPlay();
                }
            }
            //start
            else if (o == btns.get(11)){
                if (doesSaveExist()) {
                    loadStartWarningUI();
                    //Disable start ui buttons
                    switchAbleStartUIBtns();
                }
                else {
                    isSinglePlayer = true;
                    gameClient.hostServer(true);
                    serverIP = gameClient.getServerIP();
                    serverPort = gameClient.getServerPort();
                    clearGUI();
                    loadSinglePrePlayUI();
                    refreshFrame();
                }   
            }
            //yes
            else if (o == btns.get(12)){

                closeStartWarningUI();

                //Enable start ui btns
                switchAbleStartUIBtns();

                //Delete save file
                File saveFile = new File("save.dat");
                saveFile.delete();

                isSinglePlayer = true;
                gameClient.hostServer(isSinglePlayer);
                serverIP = gameClient.getServerIP();
                serverPort = gameClient.getServerPort();
                clearGUI();
                loadSinglePrePlayUI();
                refreshFrame();
            }
            //no
            else if (o == btns.get(13)){
                //Enable start ui btns
                closeStartWarningUI();
                switchAbleStartUIBtns();
                clearGUI();
                loadStartUI();
                refreshFrame();
            }

 
        };

        //Assign an event handler for all of the btns
        for (JButton btn:btns){
            btn.addActionListener(btnListener);
        }
    }



    public boolean doesSaveExist(){
        File saveFile = new File("save.dat");
        return (saveFile.exists());
    }
     

    // if start ui button is enabled, disable it; otherwise enable it
    private void switchAbleStartUIBtns(){
        if (btns.get(0).isEnabled()) btns.get(0).setEnabled(false);
        else btns.get(0).setEnabled(true);

        if (btns.get(1).isEnabled()) btns.get(1).setEnabled(false);
        else btns.get(1).setEnabled(true);
        
        if (btns.get(10).isEnabled()) btns.get(10).setEnabled(false);
        else btns.get(10).setEnabled(true);

        if (btns.get(11).isEnabled()) btns.get(11).setEnabled(false);
        else btns.get(11).setEnabled(true);
    }

    private void loadStartWarningUI(){
        JButton yesBtn = btns.get(12);
        yesBtn.setBackground(Color.white);
        yesBtn.setForeground(Color.black);
        yesBtn.setBounds(256, 348, 126, 36);
        yesBtn.setFont(getSizedGameFont(16));
        lp.add(yesBtn, Integer.valueOf(3));

        JButton noBtn = btns.get(13);
        noBtn.setBackground(Color.white);
        noBtn.setForeground(Color.black);
        noBtn.setBounds(422, 348, 126, 36);
        noBtn.setFont(getSizedGameFont(16));
        lp.add(noBtn, Integer.valueOf(3));

        startWarningLabel.setForeground(Color.WHITE);
        startWarningLabel.setBounds(200, 228, 410, 107);
        startWarningLabel.setFont(getSizedGameFont(17));
        lp.add(startWarningLabel, Integer.valueOf(3));

        JPanel warningTabBG2 = new JPanel();
        warningTabBG2.setBackground(Color.BLACK);
        warningTabBG2.setOpaque(true);
        warningTabBG2.setBounds(166, 212, 467, 188);
        lp.add(warningTabBG2, Integer.valueOf(3));

        JPanel warningTabBG1 = new JPanel();
        warningTabBG1.setBackground(Color.WHITE);
        warningTabBG1.setOpaque(true);
        warningTabBG1.setBounds(161, 208, 477, 195);
        lp.add(warningTabBG1, Integer.valueOf(3));
    }

    /**
     * Starts gameplay by establlishing a connection to the server
     * and enabling player inputs.
     */
    private void startPlay(){
        //Reset client classes
        createClientClasses();
        gameCanvas.setIsOnMenu(false);
        gameClient.connectToServer(serverIP, serverPort, playerType);
        clearGUI();
        addKeyBindings();
        refreshFrame();

        //Force reload
        cp.requestFocusInWindow();
        catCarousel.stopCarouselRenderLoop();
        

        // Start the level music after the scene is done playing
        if (!specialFrameHandler.getIsScenePlaying()) {
            SoundManager.getInstance().playLevelMusic(0);
            SoundManager.getInstance().playMusic("mainGameBGMusic");
        }
    }

    /**
     * Clears UI components from Layer 1
     */
    public void clearGUI(){
        Component[] components = lp.getComponentsInLayer(2);
        for (Component c:components) lp.remove(c);
    }

    public void closeStartWarningUI(){
        Component[] components = lp.getComponentsInLayer(3);
        for (Component c:components) lp.remove(c);
    }

    /**
     * Calls revalidate and repaint on the Frame
     */
    private void refreshFrame(){
        lp.revalidate();
        lp.repaint();
    }

    /**
     * Adds a mouseListener that tracks click inputs
     */
    public void addMouseListeners(){
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
                if (specialFrameHandler.getIsScenePlaying()) specialFrameHandler.handleClickInput();
                else if (!(isGamePaused || gameCanvas.getIsOnMenu())) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        // Disable game attack clicks until the movement tutorial step is finished
                        if (gameCanvas.getTutorialManager().getCurrentStep().getStep() < 1) return;

                        // Left click - send to server
                        gameClient.clickInput("L", e.getX(), e.getY());
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        // Right click - handle locally
                        gameCanvas.handleRightClick(e.getX(), e.getY());
                    }
                }
            }
        });

        // LOOP DATA PRIVY TO CHANGE (FOR SCALABILITY WITH OTHER BUTTONS)
        for (JButton blackWhiteBtn : blackWhiteBtns){
            blackWhiteBtn.setBorderPainted(false);
            blackWhiteBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                invertBasicBtnColors(blackWhiteBtn);
                blackWhiteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                invertBasicBtnColors(blackWhiteBtn);
                blackWhiteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
            });
        }
    }

    private void invertBasicBtnColors(JButton btnWithMouseHover){
        if(btnWithMouseHover.getBackground().equals(Color.white)) btnWithMouseHover.setBackground(Color.black);
        else btnWithMouseHover.setBackground(Color.white);
        
        if(btnWithMouseHover.getForeground().equals(Color.white)) btnWithMouseHover.setForeground(Color.black);
        else btnWithMouseHover.setForeground(Color.white);
    }

    public void addSliderListeners(){
        ChangeListener changeListener = (ChangeEvent ae) -> {
            Object o = ae.getSource();

            if (o == sfxVolumeSlider){
                soundManager.setSfxVolume(sfxVolumeSlider.getValue()/100f);
            }
            else if (o ==musicVolumeSlider){
                soundManager.setMusicVolume(musicVolumeSlider.getValue()/100f);
            }
            if (o == masterVolumeSlider){
                soundManager.setMasterVolume(masterVolumeSlider.getValue()/100f);
            }
        };

        sfxVolumeSlider.addChangeListener(changeListener);
        musicVolumeSlider.addChangeListener(changeListener);
        masterVolumeSlider.addChangeListener(changeListener);
    }

    /**
     * Sets up key bindings for player input control for keys
     * Q (interact) and W, A, S, D (movement)
     */ 

    public void addKeyBindings(){
        ActionMap am = cp.getActionMap(); 
        InputMap im = cp.getInputMap();

        AbstractAction keyInputQ = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                if (specialFrameHandler.getIsScenePlaying()) specialFrameHandler.handleKeyInput("Q");
                else if (!isGamePaused) gameClient.keyInput("Q", true);
                
            }
        };

        AbstractAction keyInputESC = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                //IMPOSE RESTRICTIONS
                if (!specialFrameHandler.getIsScenePlaying() && !gameCanvas.getIsOnMenu() 
                && !clientMaster.getIsGameOver()){
                    // System.out.println("ESC REGISTERED");
                    if (isGamePaused){
                        // System.out.println("unpaused");
                        clearGUI();
                        refreshFrame();
                        cp.requestFocusInWindow();
                        isGamePaused = false;
                    }
                    else {
                        loadPauseUI();
                        refreshFrame();
                        isGamePaused = true;
                        // System.out.println("paused");
                    }
                }

            }
        };

        AbstractAction keyInputSPACE = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                if (specialFrameHandler.getCanReturnToMenu()){
                    loadStartUI();
                    gameClient.disconnectFromServer();
                    gameCanvas.setIsOnMenu(true);
                    SoundManager.getInstance().stopAllSounds();
                    // gameCanvas.setSpecialFrameHandler(new SpecialFrameHandler());
                    clientMaster.setIsGameOver(false);
                    specialFrameHandler.setCanReturnToMenu(false);
                }
                specialFrameHandler.handleKeyInput("SPACE");
            }
        };

        AbstractAction keyInputW = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                if (specialFrameHandler.getIsScenePlaying()) specialFrameHandler.handleKeyInput("W");
                else if (isGamePaused) gameClient.keyInput("W", false);
                else gameClient.keyInput("W", true);
            }
        };

        AbstractAction keyInputS = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                if (specialFrameHandler.getIsScenePlaying()) specialFrameHandler.handleKeyInput("S");
                else if (isGamePaused) gameClient.keyInput("S", false);
                else gameClient.keyInput("S", true);
            }
        };

        AbstractAction keyInputA = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                if (specialFrameHandler.getIsScenePlaying()) specialFrameHandler.handleKeyInput("A");
                else if (isGamePaused) gameClient.keyInput("A", false);
                else gameClient.keyInput("A", true);
            }
        };

        AbstractAction keyInputD = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                if (specialFrameHandler.getIsScenePlaying()) specialFrameHandler.handleKeyInput("D");
                else if (isGamePaused) gameClient.keyInput("D", false);
                else gameClient.keyInput("D", true);
            }
        };

        AbstractAction stopInputQ = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("Q", false);
            }
        };

        AbstractAction stopInputW = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("W", false);
            }
        };

        AbstractAction stopInputS = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("S", false);
            }
        };

        AbstractAction stopInputA = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("A", false);
            }
        };

        AbstractAction stopInputD = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("D", false);
            }
        };

        am.put("keyInputQ", keyInputQ);
        am.put("keyInputESC", keyInputESC);
        am.put("keyInputSPACE", keyInputSPACE);
        am.put("keyInputW", keyInputW);
        am.put("keyInputS", keyInputS);
        am.put("keyInputA", keyInputA);
        am.put("keyInputD", keyInputD);
        am.put("stopInputQ", stopInputQ);
        am.put("stopInputW", stopInputW);
        am.put("stopInputS", stopInputS);
        am.put("stopInputA", stopInputA);
        am.put("stopInputD", stopInputD);

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0, false), "keyInputQ");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "keyInputESC");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "keyInputSPACE");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "keyInputW");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "keyInputA");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "keyInputS");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "keyInputD");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0, true), "stopInputQ");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "stopInputW");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "stopInputA");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "stopInputS");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "stopInputD");
    }

    /**
     * Adds hover effects to a button
     * @param button the button to add hover effects to
     */
    private void addColorHoverEffect(JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalBG = button.getBackground();
            Color alternateBG = (originalBG == Color.BLACK) ? Color.WHITE : Color.BLACK;

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(alternateBG);
                button.setForeground(originalBG);
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalBG);
                button.setForeground(alternateBG);
                button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
    
    private void addLineEffect(JButton button){
        // Add white line below connect button
        Rectangle buttonBounds = button.getBounds();
        
        int x = (int) (buttonBounds.getX() + 45);
        int y = (int) (buttonBounds.getY() + buttonBounds.getHeight() + 5);
        int width = (int) (buttonBounds.getWidth() - 100);
        int height = 5;


        JPanel linePanel = new JPanel();
        linePanel.setBackground(Color.WHITE);
        linePanel.setBounds(x, y, width, height);
        lp.add(linePanel, Integer.valueOf(1));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                linePanel.setBounds(button.getX(), button.getY() + button.getHeight() + 5, button.getWidth(), 5);
            }
        
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                linePanel.setBounds(x, y, width, height);
            }
        });
    }

    /**
     * Gets the GameCanvas stored in the GameCanvas field
     * @return the GameCanvas object in this class
     */
    public GameCanvas getCanvas(){
        return gameCanvas;
    }

    public Font getSizedGameFont(float size){
        //Account for canva vs Java Font rendering difference in font size
        // size *= 1.2;
        return gameFont.deriveFont(size);
    }

    private class CatCarousel extends JComponent {
        private static final int WIDTH = 125;
        private static final int HEIGHT = 117;
        private Player player;
        private Thread animationThread;
        private volatile boolean isRunning;
        private static final int ATTACK_INTERVAL = 2000; // 2 seconds between attacks
        private final ScheduledExecutorService renderLoopScheduler;
        private final List<Attack> activeAttacks;
        private long lastAttackTime = 0;

        public CatCarousel(){
            setBounds(157, 372, WIDTH, HEIGHT);
            setOpaque(true);
            setBackground(Color.decode("#7a7979"));
            activeAttacks = Collections.synchronizedList(new ArrayList<>());
            renderLoopScheduler = Executors.newSingleThreadScheduledExecutor();

            // Default player
            player = new HeavyCat(0, (WIDTH / 2) - 16, (HEIGHT / 2) - 8);
            
            // Start animation thread
            startRenderLoop();
        }

        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Enable antialiasing
            RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHints(rh);

            // Draw background
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            // Create defensive copy to avoid concurrent modification
            List<Attack> attacksCopy;
            synchronized(activeAttacks) {
                attacksCopy = new ArrayList<>(activeAttacks);
            }
            
            // Draw active attacks
            for (Attack attack : attacksCopy) {
                attack.draw(g2d, attack.getWorldX(), attack.getWorldY());
            }
            
            // Draw player
            if (player != null) {
                player.draw(g2d, player.getWorldX(), player.getWorldY());
            }
        }

        private void spawnAttack() {
            long now = System.currentTimeMillis();
            if (now - lastAttackTime < ATTACK_INTERVAL) {
                return;
            }
            lastAttackTime = now;

            // Calculate attack position (center of carousel)
            int centerX = WIDTH / 2;
            int centerY = HEIGHT / 2;

            // Create attack based on player type
            Attack attack = null;
            if (player instanceof HeavyCat) {
                attack = new PlayerSmash(0, player, centerX - PlayerSmash.WIDTH/2, centerY - PlayerSmash.HEIGHT/2, player.getDamage(), true);
            } else if (player instanceof FastCat) {
                attack = new PlayerSlash(0, player, centerX, centerY - PlayerSlash.HEIGHT/2, player.getDamage(), true);
            } else if (player instanceof GunCat) {
                // Calculate direction vector (shoot towards right side of carousel)
                double normalizedX = 1.0;
                double normalizedY = 0.0;
                attack = new PlayerBullet(0, player, centerX, centerY - PlayerBullet.HEIGHT/2, normalizedX, normalizedY, player.getDamage(), true);
            }

            if (attack != null) {
                synchronized(activeAttacks) {
                    activeAttacks.clear(); // Clear previous attacks
                    activeAttacks.add(attack);
                }
            }
        }

        public void updatePlayer(int fishSlideNum) {
            // Clear active attacks
            synchronized(activeAttacks) {
                activeAttacks.clear();
            }
            
            // Create new player
            switch(fishSlideNum) {
                case 1:
                    player = new HeavyCat(0, (WIDTH / 2) - 8, (HEIGHT / 2) - 8);
                    break;
                case 2:
                    player = new FastCat(0, (WIDTH / 2) - 8, (HEIGHT / 2) - 8);
                    break;
                case 3:
                    player = new GunCat(0, (WIDTH / 2) - 8, (HEIGHT / 2) - 8);
                    break;
            }
            
            // Reset attack timing
            lastAttackTime = 0;
        }

        /**
         * Calls repaint on this GameCanvas every REFRESHINTERVAL milliseconds
         * Also handles attack spawning and animation
         */
        private void startRenderLoop(){
            isRunning = true;
            
            Runnable renderLoop = new Runnable() {
                @Override
                public void run() {
                    if (!isRunning) return;
                    
                    // Handle attack spawning and animation
                    if (player != null && System.currentTimeMillis() - lastAttackTime > ATTACK_INTERVAL) {
                        player.runAttackFrames();
                        spawnAttack();
                    }
                    
                    // Create defensive copy for safe iteration
                    List<Attack> attacksCopy;
                    synchronized(activeAttacks) {
                        attacksCopy = new ArrayList<>(activeAttacks);
                    }
                    
                    // Update attacks
                    for (Attack attack : attacksCopy) {
                        attack.updateCarousel();
                    }
                    
                    // Remove expired attacks (this modifies the original list safely)
                    synchronized(activeAttacks) {
                        activeAttacks.removeIf(attack -> attack.getIsExpired());
                    }
                    
                    repaint();
                }
            };
            
            renderLoopScheduler.scheduleAtFixedRate(renderLoop, 0, 16, TimeUnit.MILLISECONDS);
        }

        public void stopCarouselRenderLoop(){
            isRunning = false;
            if (renderLoopScheduler != null && !renderLoopScheduler.isShutdown()) {
                renderLoopScheduler.shutdown();
                try {
                    if (!renderLoopScheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                        renderLoopScheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    renderLoopScheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    } 
}
