import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.border.LineBorder;

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
    private final JPanel warningPanel;
    private ImageIcon btnBG;
    private final GameCanvas gameCanvas;
    private final GameClient gameClient;
    private final SceneHandler sceneHandler;
    private final ArrayList<JButton> btns;
    private final JLabel gameTitle;
    private final JLabel ipLabel;
    private final JLabel portLabel;
    private final JLabel catNameLabel;
    private final JLabel carouselLabel;
    private final JLabel[] warningPanelLabel;
    private final JTextField ipTextField;
    private final JTextField portTextField;
    private int fishSlideNum;

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
        
        cp = (JPanel) this.getContentPane();
        warningPanel = new JPanel();

        // btnBG = new ImageIcon("/UI Assets/btnBG");
        gameCanvas = new GameCanvas(width, height);
        gameClient = gameCanvas.getGameClient();
        sceneHandler = gameCanvas.getSceneHandler();
        
        //Set default values
        playerType = NetworkProtocol.HEAVYCAT;
        fishSlideNum = 1;

        // UI Elements
        btns = new ArrayList<>();
        btns.add(new JButton("START"));         //  0
        btns.add(new JButton("QUIT"));          //  1
        btns.add(new JButton("CONNECT"));       //  2
        btns.add(new JButton("BACK"));          //  3
        btns.add(new JButton("HOST SERVER"));   //  4
        btns.add(new JButton("ENTER GAME"));    //  5
        btns.add(new JButton("<"));             //  6
        btns.add(new JButton(">"));             //  7
        btns.add(new JButton("CONTINUE"));      //  8
        btns.add(new JButton("MULTIPLAYER"));   //  9
        btns.add(new JButton("YES"));           //  10
        btns.add(new JButton("NO"));            //  11
        for (JButton jButton : btns) {
            jButton.setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        gameTitle = new JLabel("BITING ON FISH", SwingConstants.CENTER);
        ipLabel = new JLabel();
        portLabel = new JLabel();
        catNameLabel = new JLabel();
        carouselLabel = new JLabel("CHOOSE A FISH:");
        warningPanelLabel = new JLabel[3];
        ipTextField = new JTextField(10);
        portTextField = new JTextField(10);
        lp = new JLayeredPane();

        isMultiPlayerMode = false;
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

        gameCanvas.setBounds(0, 0, width, height);
        lp.add(gameCanvas, Integer.valueOf(0)); 
        
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
        gameTitle.setBounds(89, 153, 622, 56);
        gameTitle.setFont(gameFont35);
        lp.add(gameTitle, Integer.valueOf(1));

        gameFont = gameFont.deriveFont(15f);

        JButton continueButton = btns.get(8);
        continueButton.setBackground(Color.white);
        continueButton.setForeground(Color.black);
        continueButton.setBounds(280, 240, 240, 43);
        continueButton.setFont(gameFont14);
        lp.add(continueButton, Integer.valueOf(1));

        JButton startButton = btns.get(0);
        startButton.setBackground(Color.white);
        startButton.setForeground(Color.black);
        startButton.setBounds(280, 312, 240, 43);
        startButton.setFont(gameFont14);
        lp.add(startButton, Integer.valueOf(1));

        JButton multiplayerButton = btns.get(9);
        multiplayerButton.setBackground(Color.white);
        multiplayerButton.setForeground(Color.black);
        multiplayerButton.setBounds(280, 387, 240, 43);
        multiplayerButton.setFont(gameFont14);
        lp.add(multiplayerButton, Integer.valueOf(1));

        JButton quitButton = btns.get(1);
        quitButton.setBackground(Color.white);
        quitButton.setForeground(Color.black);
        quitButton.setBounds(280, 458, 240, 43);
        quitButton.setFont(gameFont14);
        lp.add(quitButton, Integer.valueOf(1));

        setUpButtonHoverEffects(0);
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
        ipLabel.setFont(gameFont16);
        lp.add(ipLabel, Integer.valueOf(1));

        portLabel.setForeground(Color.WHITE);
        portLabel.setText("PORT NUMBER:");
        portLabel.setBounds(85, 196, 244, 25);
        portLabel.setFont(gameFont16);
        lp.add(portLabel, Integer.valueOf(1));

        ipTextField.setBounds(336, 128, 306, 26);
        ipTextField.setBackground(Color.black);
        ipTextField.setForeground(Color.white);
        ipTextField.setFont(gameFont16);
        ipTextField.setFocusable(true);
        lp.add(ipTextField, Integer.valueOf(1));

        portTextField.setBounds(336, 196, 306, 26);
        portTextField.setBackground(Color.black);
        portTextField.setForeground(Color.white);
        portTextField.setFont(gameFont16);
        lp.add(portTextField, Integer.valueOf(1));

        JButton connectButton = btns.get(2);
        connectButton.setBackground(Color.black);
        connectButton.setForeground(Color.white);
        connectButton.setBorderPainted(false);
        connectButton.setFont(gameFont20);
        connectButton.setBounds(105, 334, 189, 31);
        lp.add(connectButton, Integer.valueOf(1));

        JButton backButton = btns.get(3);
        backButton.setBackground(Color.black);
        backButton.setForeground(Color.white);
        backButton.setBorderPainted(false);
        backButton.setFont(gameFont20);
        backButton.setBounds(561, 334, 118, 31);
        lp.add(backButton, Integer.valueOf(1));

        JButton hostServerButton = btns.get(4);
        hostServerButton.setBackground(Color.white);
        hostServerButton.setForeground(Color.black);
        hostServerButton.setBounds(85, 426, 307, 58);
        hostServerButton.setFont(gameFont20);

        lp.add(hostServerButton, Integer.valueOf(1));

        setUpButtonHoverEffects(1);
    }

    /**
     * Loads the pre-game UI displaying the serverIP address, the port number,
     * and allows the user to select a player type.
     */
    public void loadPrePlayUI(){
        // IP ADDRESS
        ipLabel.setForeground(Color.WHITE);
        ipLabel.setText("IP ADDRESS: " + serverIP);
        ipLabel.setBounds(85, 128, 558, 25);
        lp.add(ipLabel, Integer.valueOf(1));
        
        // PORT NUMBER
        portLabel.setForeground(Color.WHITE);
        portLabel.setText("PORT NUMBER: " + serverPort);
        portLabel.setBounds(85, 196, 558, 25);
        lp.add(portLabel, Integer.valueOf(1));

        // BACK BUTTON
        JButton backButton = btns.get(3);
        backButton.setBackground(Color.black);
        backButton.setForeground(Color.white);
        backButton.setBorderPainted(false);
        backButton.setFont(gameFont20);
        backButton.setBounds(536, 477, 118, 31);
        lp.add(backButton, Integer.valueOf(1));

        // ENTER GAME BUTTON
        JButton enterGameButton = btns.get(5);
        enterGameButton.setBackground(Color.white);
        enterGameButton.setForeground(Color.black);
        enterGameButton.setBounds(447, 392, 287, 56);
        enterGameButton.setFont(gameFont20);
        lp.add(enterGameButton, Integer.valueOf(1));

        // CAROUSEL TEXT
        catCarousel = new CatCarousel();
        lp.add(catCarousel, Integer.valueOf(1));

        carouselLabel.setBackground(Color.black);
        carouselLabel.setForeground(Color.white);
        carouselLabel.setBounds(85, 323, 288, 25);
        carouselLabel.setFont(gameFont16);
        carouselLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lp.add(carouselLabel, Integer.valueOf(1));

        updateFishCarousel();
        catNameLabel.setForeground(Color.WHITE);
        catNameLabel.setBounds(135, 504, 179, 10);
        catNameLabel.setFont(gameFont6);
        catNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lp.add(catNameLabel, Integer.valueOf(1));

        // LEFT BUTTON
        JButton leftButton = btns.get(6);
        leftButton.setBounds(168, 528, 50, 31);
        leftButton.setHorizontalAlignment(SwingConstants.CENTER);
        leftButton.setBackground(Color.black);
        leftButton.setForeground(Color.white);
        leftButton.setBorderPainted(false);
        leftButton.setFont(gameFont16);
        lp.add(leftButton, Integer.valueOf(1));

        // RIGHT BUTTON
        JButton rightButton = btns.get(7);
        rightButton.setBounds(222, 528, 50, 31);
        rightButton.setHorizontalAlignment(SwingConstants.CENTER);
        rightButton.setBackground(Color.black);
        rightButton.setForeground(Color.white);
        rightButton.setBorderPainted(false);
        rightButton.setFont(gameFont16);
        lp.add(rightButton, Integer.valueOf(1));

        setUpButtonHoverEffects(2);
    }

    public void loadSoloPlayUI(){
        isMultiPlayerMode = false;

        // BACK BUTTON
        JButton backButton = btns.get(3);
        backButton.setBackground(Color.black);
        backButton.setForeground(Color.white);
        backButton.setBorderPainted(false);
        backButton.setFont(gameFont20);
        backButton.setBounds(536, 338, 118, 31);
        lp.add(backButton, Integer.valueOf(1));

        // ENTER GAME BUTTON
        JButton enterGameButton = btns.get(5);
        enterGameButton.setBackground(Color.white);
        enterGameButton.setForeground(Color.black);
        enterGameButton.setBounds(447, 253, 287, 56);
        enterGameButton.setFont(gameFont20);
        lp.add(enterGameButton, Integer.valueOf(1));

        // CAROUSEL TEXT
        catCarousel = new CatCarousel();
        catCarousel.setBounds(171, 242, 125, 117);
        lp.add(catCarousel, Integer.valueOf(1));

        carouselLabel.setBackground(Color.black);
        carouselLabel.setForeground(Color.white);
        carouselLabel.setBounds(73, 171, 349, 31);
        carouselLabel.setFont(gameFont20);
        carouselLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lp.add(carouselLabel, Integer.valueOf(1));

        updateFishCarousel();
        catNameLabel.setForeground(Color.WHITE);
        catNameLabel.setBounds(127, 381, 217, 12);
        catNameLabel.setFont(gameFont6);
        catNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lp.add(catNameLabel, Integer.valueOf(1));

        // LEFT BUTTON
        JButton leftButton = btns.get(6);
        leftButton.setBounds(170, 415, 61, 38);
        leftButton.setHorizontalAlignment(SwingConstants.CENTER);
        leftButton.setBackground(Color.black);
        leftButton.setForeground(Color.white);
        leftButton.setBorderPainted(false);
        leftButton.setFont(gameFont16);
        lp.add(leftButton, Integer.valueOf(1));

        // RIGHT BUTTON
        JButton rightButton = btns.get(7);
        rightButton.setBounds(235, 415, 61, 38);
        rightButton.setHorizontalAlignment(SwingConstants.CENTER);
        rightButton.setBackground(Color.black);
        rightButton.setForeground(Color.white);
        rightButton.setBorderPainted(false);
        rightButton.setFont(gameFont16);
        lp.add(rightButton, Integer.valueOf(1));

        setUpButtonHoverEffects(2);
    }


    /**
     * Updates the selected player display based on user input
     */
    public void updateFishCarousel(){
        if (fishSlideNum > 3) fishSlideNum = 1; 
        else if (fishSlideNum < 1) fishSlideNum = 3;

        switch(fishSlideNum){
            case 1:
                catNameLabel.setText("TUNA (HEAVY)");
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
            
            // START BUTTON
            if (o == btns.get(0)){                  
                clearGUI();
                loadSoloPlayUI();
                refreshFrame();
            }
            // QUIT BUTTON
            else if (o == btns.get(1)){
                loadWarningPanel();
                refreshFrame();                
            }
            // CONNECT BUTTON
            else if (o == btns.get(2)){
                serverIP = ipTextField.getText();
                serverPort = Integer.parseInt(portTextField.getText());
                clearGUI();
                loadPrePlayUI();
                refreshFrame();
            }
            // BACK BUTTON
            else if (o == btns.get(3)){
                clearGUI();
                loadStartUI();
                refreshFrame();
                catCarousel.stopCarouselRenderLoop();
            }
            // HOST SERVER
            else if (o == btns.get(4)){
                gameClient.hostServer();
                serverIP = gameClient.getServerIP();
                serverPort = gameClient.getServerPort();
                clearGUI();
                loadPrePlayUI();
                refreshFrame();
            }
            // ENTER GAME
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
            // CONTINUE
            else if (o == btns.get(8)){
                // TODO: IMPLEMENT CONTINUE BUTTON
                System.out.println("GameFrame.java: 567. Save system not yet supported. Please come back soon.");
            }
            // MULTIPLAYER
            else if (o == btns.get(9)){
                clearGUI();
                loadClientUI();
                refreshFrame();
            }
            // QUIT WARNING -> YES
            else if (o == btns.get(10)){
                System.exit(0);
            }
            // QUIT WARNING -> NO
            else if (o == btns.get(11)){
                lp.remove(warningPanel);
                lp.remove(btns.get(10));
                lp.remove(btns.get(11));
                for (JLabel wpl : warningPanelLabel) {
                    lp.remove(wpl);
                }
                refreshFrame();
            } 
        };

        //Assign an event handler for all of the btns
        for (JButton btn:btns){
            btn.addActionListener(btnListener);
        }
    }

    /**
     * Starts gameplay by establlishing a connection to the server
     * and enabling player inputs.
     */
    private void startPlay(){
        gameClient.connectToServer(serverIP, serverPort, playerType);
        clearGUI();
        addKeyBindings();
        addMouseListener();
        refreshFrame();
        //Force reload
        cp.requestFocusInWindow();
        catCarousel.stopCarouselRenderLoop();
        gameCanvas.startRenderLoop();
        
        // Start the level music after the scene is done playing
        if (!sceneHandler.getIsScenePlaying()) {
            SoundManager.getInstance().playLevelMusic(0);
            SoundManager.getInstance().playMusic("mainGameBGMusic");
        }
    }

    /**
     * Clears UI components from Layer 1
     */
    public void clearGUI(){
        Component[] components = lp.getComponentsInLayer(1);
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
    public void addMouseListener(){
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
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
        });
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
                gameClient.keyInput("Q", true);
                sceneHandler.handleInput("Q");
            }
        };

        AbstractAction keyInputESC = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                sceneHandler.handleInput("ESC");
            }
        };

        AbstractAction keyInputW = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("W", true);
                sceneHandler.handleInput("W");
            }
        };

        AbstractAction keyInputS = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("S", true);
                sceneHandler.handleInput("S");
            }
        };

        AbstractAction keyInputA = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("A", true);
                sceneHandler.handleInput("A");
            }
        };

        AbstractAction keyInputD = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("D", true);
                sceneHandler.handleInput("D");
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

    private class CatCarousel extends JComponent {
        private static final int WIDTH = 125;
        private static final int HEIGHT = 117;
        private Player player;
        private Thread animationThread;
        private volatile boolean isRunning;
        private static final int ATTACK_INTERVAL = 2000; // 2 seconds between attacks
        private final ScheduledExecutorService renderLoopScheduler;
        private final ArrayList<Attack> activeAttacks;
        private long lastAttackTime = 0;

        public CatCarousel(){
            setBounds(157, 372, WIDTH, HEIGHT);
            setOpaque(true);
            setBackground(Color.decode("#7a7979"));
            activeAttacks = new ArrayList<>();
            renderLoopScheduler = Executors.newSingleThreadScheduledExecutor();

            // Default player
            player = new HeavyCat(0, (WIDTH / 2) - 16, (HEIGHT / 2) - 8);
            
            // Start animation thread
            startRenderLoop();
        }

        private void startAnimationThread() {
            isRunning = true;
            animationThread = new Thread(() -> {
                while (isRunning) {
                    // Run attack animation and spawn attack
                    if (player != null && System.currentTimeMillis() - lastAttackTime > ATTACK_INTERVAL) {
                        player.runAttackFrames();
                        spawnAttack();
                        repaint();
                    } else {
                        
                    }
                }
            });
            animationThread.setDaemon(true);
            animationThread.start();
        }

        private void spawnAttack() {
            long now = System.currentTimeMillis();
            if (now - lastAttackTime < ATTACK_INTERVAL) {
                activeAttacks.clear();
                return;
            }
            lastAttackTime = now;

            // Clear expired attacks
            activeAttacks.clear();

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
                activeAttacks.add(attack);
            }
        }

        public void stopAnimation() {
            isRunning = false;
            if (animationThread != null) {
                animationThread.interrupt();
            }
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

            // Draw active attacks
            for (Attack attack : activeAttacks) {
                attack.draw(g2d, attack.getWorldX(), attack.getWorldY());
            }
            
            // Draw player
            if (player != null) {
                player.draw(g2d, player.getWorldX(), player.getWorldY());
            }
        }

        public void updatePlayer(int fishSlideNum) {
            // Stop current animation
            stopAnimation();
            
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
            
            // Clear active attacks
            activeAttacks.clear();
            
            // Start new animation
            startAnimationThread();
        }

        /**
         * Calls repaint on this GameCanvas every REFRESHINTERVAL milliseconds
         */
        private void startRenderLoop(){
            //Since putting Thread.sleep in a loop as necessary for this Loop is bad, use ScheduledExecutorService instead
            Runnable renderLoop = new Runnable() {
                @Override
                public void run() {
                    for (Attack attack : activeAttacks) {
                        attack.updateCarousel();
                    }
                    activeAttacks.removeIf(attack -> attack.getIsExpired());
                    repaint();
                }
                
            };
            renderLoopScheduler.scheduleAtFixedRate(renderLoop, 0, 16, TimeUnit.MILLISECONDS);
        }

        public void stopCarouselRenderLoop(){
            stopAnimation();
            renderLoopScheduler.shutdown();
        }
    }
}
