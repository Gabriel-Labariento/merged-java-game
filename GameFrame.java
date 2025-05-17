import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class GameFrame extends JFrame{
    private final int width, height;
    private final String title;
    private String serverIP;
    private int serverPort;
    private String playerType;
    private final JLayeredPane lp;
    private final JPanel cp;  
    private ImageIcon btnBG;
    private final GameCanvas gameCanvas;
    private final GameClient gameClient;
    private final ArrayList<JButton> btns;
    private final JLabel label1;
    private final JLabel label2;
    private final JLabel label3;
    private final JTextField textField1;
    private final JTextField textField2;
    private int fishSlideNum;


    public GameFrame(int width, int height, String title){
        this.width = width;
        this.height = height;
        this.title = title;
        cp = (JPanel) this.getContentPane();
        // btnBG = new ImageIcon("/UI Assets/btnBG");
        gameCanvas = new GameCanvas(width, height);
        gameClient = gameCanvas.getGameClient();

        //Set default values
        playerType = NetworkProtocol.HEAVYCAT;
        fishSlideNum = 1;

        btns = new ArrayList<>();
        btns.add(new JButton("Play"));
        btns.add(new JButton("Quit"));
        btns.add(new JButton("Connect"));
        btns.add(new JButton("Back"));
        btns.add(new JButton("Host Server"));
        btns.add(new JButton("Enter Game"));
        btns.add(new JButton("<"));
        btns.add(new JButton(">"));

        label1 = new JLabel();
        label2 = new JLabel();
        label3 = new JLabel();

        textField1 = new JTextField(10);
        textField2 = new JTextField(10);
        lp = new JLayeredPane();
    }

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

    public void loadStartUI(){
        btns.get(0).setBounds(54, 116, 158, 33);
        lp.add(btns.get(0), Integer.valueOf(1));

        btns.get(1).setBounds(54, 169, 158, 33);
        lp.add(btns.get(1) , Integer.valueOf(1));
    }

    public void loadClientUI(){

        label1.setForeground(Color.WHITE);
        label1.setText("IP Address: ");
        label1.setBounds(17, 133, 232, 15);
        lp.add(label1, Integer.valueOf(1));
        
        label2.setForeground(Color.WHITE);
        label2.setText("Port Number: ");
        label2.setBounds(17, 194, 232, 15);
        lp.add(label2, Integer.valueOf(1));

        textField1.setBounds(245, 131, 159,  28);
        lp.add(textField1, Integer.valueOf(1));

        textField2.setBounds(245, 192, 159, 28);
        lp.add(textField2, Integer.valueOf(1));
        
        btns.get(2).setBounds(54, 280, 159, 35);
        lp.add(btns.get(2), Integer.valueOf(1));

        btns.get(3).setBounds(245, 280, 159, 35);
        lp.add(btns.get(3), Integer.valueOf(1));

        btns.get(4).setBounds(54, 385, 159, 35);
        lp.add(btns.get(4), Integer.valueOf(1));
    }

    public void loadPrePlayUI(){
        label1.setForeground(Color.WHITE);
        label1.setText("IP Address: " + serverIP);
        label1.setBounds(17, 133, 232, 15);
        lp.add(label1, Integer.valueOf(1));
        
        label2.setForeground(Color.WHITE);
        label2.setText("Port: " + serverPort);
        label2.setBounds(17, 194, 232, 15);
        lp.add(label2, Integer.valueOf(1));

        updateFishCarousel();
        label3.setForeground(Color.WHITE);
        label3.setBounds(460, 255, 227, 15);
        lp.add(label3, Integer.valueOf(1));

        btns.get(3).setBounds(245, 280, 159, 35);
        lp.add(btns.get(3), Integer.valueOf(1));

        btns.get(5).setBounds(54, 280, 159, 35);
        lp.add(btns.get(5), Integer.valueOf(1));

        btns.get(6).setBounds(517, 280, 48, 35);
        lp.add(btns.get(6), Integer.valueOf(1));

        btns.get(7).setBounds(582, 280, 48, 35);
        lp.add(btns.get(7), Integer.valueOf(1));
    }

    public void updateFishCarousel(){
        if (fishSlideNum > 3) fishSlideNum = 1; 
        else if (fishSlideNum < 1) fishSlideNum = 3;

        switch(fishSlideNum){
            case 1:
                label3.setText("Tuna (Heavy)");
                playerType = NetworkProtocol.HEAVYCAT;
                break;
            case 2:
                label3.setText("Anchovy (Light)");
                playerType = NetworkProtocol.FASTCAT;
                break;
            case 3:
                label3.setText("Archerfish (Ranged)");
                playerType = NetworkProtocol.GUNCAT;
                break;
        }
        refreshFrame();
    }



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

 
        };
        //Assign an event handler for all of the btns
        for (JButton btn:btns){
            btn.addActionListener(btnListener);
        }
 
    }

    private void startPlay(){
        gameClient.connectToServer(serverIP, serverPort, playerType);
        clearGUI();
        addKeyBindings();
        addMouseListener();
        refreshFrame();
        //Force reload
        cp.requestFocusInWindow();
    }

    public void clearGUI(){
        Component[] components = lp.getComponentsInLayer(1);
        for (Component c:components) lp.remove(c);
    }

    private void refreshFrame(){
        lp.revalidate();
        lp.repaint();
    }

    public void addMouseListener(){
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
                gameClient.clickInput(e.getX(), e.getY());
            }
        });
    }

    public void addKeyBindings(){
        ActionMap am = cp.getActionMap(); 
        InputMap im = cp.getInputMap();

        AbstractAction keyInputQ = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("Q", true);
            }
        };

        AbstractAction keyInputW = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("W", true);
            }
        };

        AbstractAction keyInputS = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("S", true);
            }
        };

        AbstractAction keyInputA = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("A", true);
            }
        };

        AbstractAction keyInputD = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae){
                gameClient.keyInput("D", true);
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

    public GameCanvas getCanvas(){
        return gameCanvas;
    }
}
