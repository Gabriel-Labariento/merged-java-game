import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
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
    private final JLabel portLabel;
    private final JLabel IPLabel;
    private final JLabel fishLabel;
    private final JLabel pauseTitleLabel;
    private final JLabel volumeMainLabel;
    private final JLabel volumeSubLabel;
    private final JTextField textField1;
    private final JTextField textField2;
    private final JSlider sfxVolumeSlider;
    private final JSlider musicVolumeSlider;
    private final JSlider masterVolumeSlider;
    private int fishSlideNum;
    private static Font gameFont;
    public isGamePausedRef isGamePausedRef;
    private SoundManager soundManager;
    private static BufferedImage sliderThumb;

    static{
        try {
            gameFont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/Fonts/PressStart2P-Regular.ttf"));
            sliderThumb = ImageIO.read(GameFrame.class.getResourceAsStream("resources/UserInterface/sliderThumb.png"));
        } catch (FontFormatException e) {
        } catch (IOException e) {
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
        isGamePausedRef = new isGamePausedRef(false);

        //Set default values
        playerType = NetworkProtocol.HEAVYCAT;
        fishSlideNum = 1;

        // UI Elements
        btns = new ArrayList<>();
        btns.add(new JButton("Play")); // 0
        btns.add(new JButton("Quit")); // 1
        btns.add(new JButton("Connect")); // 2
        btns.add(new JButton("BACK")); // 3
        btns.add(new JButton("Host Server")); // 4
        btns.add(new JButton("Enter Game")); // 5
        btns.add(new JButton("<")); // 6
        btns.add(new JButton(">")); // 7
        btns.add(new JButton("DISCONNECT")); // 8
        btns.add(new JButton("BACK")); // 9

        IPLabel = new JLabel();
        portLabel = new JLabel();
        fishLabel = new JLabel();
        pauseTitleLabel = new JLabel("--- PAUSED ---");
        volumeMainLabel = new JLabel("MASTER VOLUME:");
        volumeSubLabel = new JLabel("<html>Sound Effects:<br><br>Music:</html>");
        
        textField1 = new JTextField(10);
        textField2 = new JTextField(10);

        sfxVolumeSlider = new JSlider(0, 100, ((int)(soundManager.getSfxVolume()*100.0)));
        musicVolumeSlider = new JSlider(0, 100, ((int) (soundManager.getMusicVolume()*100.0)));
        masterVolumeSlider = new JSlider(0, 100, ((int) (soundManager.getMasterVolume()*100.0)));

        lp = new JLayeredPane();

        createClientClasses();
    }

    public class isGamePausedRef{
        public boolean isGamePaused;

        public isGamePausedRef(boolean b){
            isGamePaused = b;
        }
    }

    private void createClientClasses(){
        //Account for already existing gameCanvas
        if (gameCanvas != null) {
            lp.remove(gameCanvas);
            //Stop from renderloop thread from lingering
            gameCanvas.stopRenderLoop();
        }

        //Create classes
        gameCanvas = new GameCanvas(width, height, gameFont, isGamePausedRef);
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
        btns.get(0).setBounds(54, 116, 158, 33);
        lp.add(btns.get(0), Integer.valueOf(2));

        btns.get(1).setBounds(54, 169, 158, 33);
        lp.add(btns.get(1) , Integer.valueOf(2));
    }

    /**
     * Loads the pre-game screen needed for client-server connection
     */
    public void loadClientUI(){

        IPLabel.setForeground(Color.WHITE);
        IPLabel.setText("IP Address: ");
        IPLabel.setBounds(17, 133, 232, 15);
        lp.add(IPLabel, Integer.valueOf(2));
        
        portLabel.setForeground(Color.WHITE);
        portLabel.setText("Port Number: ");
        portLabel.setBounds(17, 194, 232, 15);
        lp.add(portLabel, Integer.valueOf(2));

        textField1.setBounds(245, 131, 159,  28);
        lp.add(textField1, Integer.valueOf(2));

        textField2.setBounds(245, 192, 159, 28);
        lp.add(textField2, Integer.valueOf(2));
        
        btns.get(2).setBounds(54, 280, 159, 35);
        lp.add(btns.get(2), Integer.valueOf(2));

        btns.get(3).setBounds(245, 280, 159, 35);
        lp.add(btns.get(3), Integer.valueOf(2));

        btns.get(4).setBounds(54, 385, 159, 35);
        lp.add(btns.get(4), Integer.valueOf(2));
    }

    /**
     * Loads the pre-game UI displaying the serverIP address, the port number,
     * and allows the user to select a player type.
     */
    public void loadPrePlayUI(){
        IPLabel.setForeground(Color.WHITE);
        IPLabel.setText("IP Address: " + serverIP);
        IPLabel.setBounds(17, 133, 232, 15);
        lp.add(IPLabel, Integer.valueOf(2));
        
        portLabel.setForeground(Color.WHITE);
        portLabel.setText("Port: " + serverPort);
        portLabel.setBounds(17, 194, 232, 15);
        lp.add(portLabel, Integer.valueOf(2));

        updateFishCarousel();
        fishLabel.setForeground(Color.WHITE);
        fishLabel.setBounds(460, 255, 227, 15);
        lp.add(fishLabel, Integer.valueOf(2));

        btns.get(3).setBounds(245, 280, 159, 35);
        lp.add(btns.get(3), Integer.valueOf(2));

        btns.get(5).setBounds(54, 280, 159, 35);
        lp.add(btns.get(5), Integer.valueOf(2));

        btns.get(6).setBounds(517, 280, 48, 35);
        lp.add(btns.get(6), Integer.valueOf(2));

        btns.get(7).setBounds(582, 280, 48, 35);
        lp.add(btns.get(7), Integer.valueOf(2));
    }

    public void loadPauseUI(){

        pauseTitleLabel.setForeground(Color.WHITE);
        pauseTitleLabel.setBounds(235, 190, 350, 40);
        pauseTitleLabel.setFont(getSizedGameFont(24f));
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
        pauseDisconnectBtn.setBackground(Color.white);
        pauseDisconnectBtn.setForeground(Color.black);
        pauseDisconnectBtn.setBorderPainted(false);
        pauseDisconnectBtn.setBounds(217, 400, 192, 41);
        pauseDisconnectBtn.setFont(getSizedGameFont(15f));
        lp.add(pauseDisconnectBtn, Integer.valueOf(2));        

        JButton pauseBackButton = btns.get(9);
        pauseBackButton.setBackground(Color.white);
        pauseBackButton.setForeground(Color.black);
        pauseBackButton.setBorderPainted(false);
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
                fishLabel.setText("Pufferfish (Heavy)");
                playerType = NetworkProtocol.HEAVYCAT;
                break;
            case 2:
                fishLabel.setText("Anchovy (Light)");
                playerType = NetworkProtocol.FASTCAT;
                break;
            case 3:
                fishLabel.setText("Archerfish (Ranged)");
                playerType = NetworkProtocol.GUNCAT;
                break;
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
            Object o = ae.getSource();
            
            if (o == btns.get(0)){
                clearGUI();
                loadClientUI();
                refreshFrame();
            }
            else if (o == btns.get(1)){
                System.exit(0);                
            }
            else if (o == btns.get(2)){
                serverIP = textField1.getText();
                serverPort = Integer.parseInt(textField2.getText());
                clearGUI();
                loadPrePlayUI();
                refreshFrame();
            }
            else if (o == btns.get(3)){
                clearGUI();
                loadStartUI();
                refreshFrame();

                GameServer gs = gameClient.getGameServer();
                if (gs != null) gs.shutDownServer();
            }
            else if (o == btns.get(4)){
                gameClient.hostServer();
                serverIP = gameClient.getServerIP();
                serverPort = gameClient.getServerPort();
                clearGUI();
                loadPrePlayUI();
                refreshFrame();
                
            }
            else if (o == btns.get(5)){
                startPlay();
            }
            else if (o == btns.get(6)){
                fishSlideNum--;
                updateFishCarousel();
            }
            else if (o == btns.get(7)){
                fishSlideNum++;
                updateFishCarousel();
            }
            else if (o == btns.get(8)){
                //Return to main menu 
                gameCanvas.setIsOnMenu(true);
                gameClient.setWantsDisconnect(true);
                SoundManager.getInstance().stopAllSounds();
                isGamePausedRef.isGamePaused = false;

                clearGUI();
                loadStartUI();
                refreshFrame();
                
                cp.requestFocusInWindow();
                // isGamePaused = false;
            }
            else if (o == btns.get(9)){
                
                clearGUI();
                refreshFrame();
                cp.requestFocusInWindow();
                isGamePausedRef.isGamePaused = false;
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
        //Reset client classes
        createClientClasses();
        gameCanvas.setIsOnMenu(false);
        gameClient.connectToServer(serverIP, serverPort, playerType);
        clearGUI();
        addKeyBindings();
        addMouseListeners();
        refreshFrame();

        //Force reload
        cp.requestFocusInWindow();

        // Start the level music after the scene is done playing
        if (!specialFrameHandler.getIsScenePlaying()) {
            SoundManager.getInstance().playLevelMusic(0);
        }
    }

    /**
     * Clears UI components from Layer 1
     */
    public void clearGUI(){
        Component[] components = lp.getComponentsInLayer(2);
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
                else if (!(isGamePausedRef.isGamePaused || gameCanvas.getIsOnMenu())) {
                    gameClient.clickInput(e.getX(), e.getY());
                }
            }
        });

        // LOOP DATA PRIVY TO CHANGE (FOR SCALABILITY WITH OTHER BUTTONS)
        for (int i = 8; i < 10; i++){
            JButton btnWithMouseHover = btns.get(i);
            btnWithMouseHover.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                invertBasicBtnColors(btnWithMouseHover);
                btnWithMouseHover.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                invertBasicBtnColors(btnWithMouseHover);
                btnWithMouseHover.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
                else if (!isGamePausedRef.isGamePaused) gameClient.keyInput("Q", true);
                
            }
        };

        AbstractAction keyInputESC = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                //IMPOSE RESTRICTIONS
                if (!specialFrameHandler.getIsScenePlaying() && !gameCanvas.getIsOnMenu() 
                && !clientMaster.getIsGameOver()){
                    // System.out.println("ESC REGISTERED");
                    if (isGamePausedRef.isGamePaused){
                        // System.out.println("unpaused");
                        clearGUI();
                        refreshFrame();
                        cp.requestFocusInWindow();
                        isGamePausedRef.isGamePaused = false;
                    }
                    else {
                        loadPauseUI();
                        refreshFrame();
                        isGamePausedRef.isGamePaused = true;
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
                else if (isGamePausedRef.isGamePaused) gameClient.keyInput("W", false);
                else gameClient.keyInput("W", true);
            }
        };

        AbstractAction keyInputS = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                if (specialFrameHandler.getIsScenePlaying()) specialFrameHandler.handleKeyInput("S");
                else if (isGamePausedRef.isGamePaused) gameClient.keyInput("S", false);
                else gameClient.keyInput("S", true);
            }
        };

        AbstractAction keyInputA = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                if (specialFrameHandler.getIsScenePlaying()) specialFrameHandler.handleKeyInput("A");
                else if (isGamePausedRef.isGamePaused) gameClient.keyInput("A", false);
                else gameClient.keyInput("A", true);
            }
        };

        AbstractAction keyInputD = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                if (specialFrameHandler.getIsScenePlaying()) specialFrameHandler.handleKeyInput("D");
                else if (isGamePausedRef.isGamePaused) gameClient.keyInput("D", false);
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
    
}
