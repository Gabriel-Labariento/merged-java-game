import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.*;

    /**
     * Manages the game's tutorial system, handling the display and progression of tutorial steps.
     * The tutorial consists of a series of steps that guide the player through basic game mechanics.
     * Each step is displayed at the bottom of the screen with a semi-transparent background.
     */
    public class TutorialManager {
        /** Queue containing all tutorial steps to be displayed */
        private final Queue<TutorialStep> steps;
        /** The currently active tutorial step */
        private TutorialStep currentStep;
        /** Flag indicating whether the tutorial is currently active */
        private boolean isActive;
        private GameCanvas parentCanvas;
        private ServerMaster gameServerMaster;

        private boolean hasShownTutorialStep = false;
        private static final int TUTORIAL_STEP_DURATION = 4000;
        private long lastShowTutorialStepTime = 0;

        private Room roomBeforeBoss;

        /**
         * Constructs a new TutorialManager and initializes the tutorial steps.
         * The tutorial starts in an inactive state and must be explicitly started.
         */
        public TutorialManager(GameCanvas gc){
            parentCanvas = gc;
            gameServerMaster = ServerMaster.getInstance();
            steps = new LinkedList<>();
            initializeSteps();
            isActive = false;
            roomBeforeBoss = null;
        }

        /**
         * Initializes the tutorial steps by creating TutorialStep objects for each message.
         * This method is called during construction and when resetting the tutorial.
         */
        private void initializeSteps() {
            for (int i = 0; i < TutorialStep.messages.length; i++) {
                steps.add(new TutorialStep(i));
            }
        }

        /**
         * Starts the tutorial by showing the first step.
         * Does nothing if there are no steps to show.
         */
        public void startTutorial() {
            if (!steps.isEmpty()) {
                isActive = true;
                showNextStep();
            }
        }

        /**
         * Advances to the next tutorial step.
         * Hides the current step and shows the next one in the queue.
         * If there are no more steps, the tutorial becomes inactive.
         */
        public void showNextStep() {
            if (currentStep != null) {
                currentStep.setVisible(false);
                parentCanvas.remove(currentStep);
            }
            
            if (!steps.isEmpty()) {
                currentStep = steps.poll();
                parentCanvas.add(currentStep);
                currentStep.setVisible(true);
                parentCanvas.revalidate();
                parentCanvas.repaint();
            } else {
                currentStep = null; 
                isActive = false;
            }
        }

        /**
         * Hides the currently displayed tutorial step.
         */
        public void hideCurrentStep() {
            if (currentStep != null) {
                currentStep.setVisible(false);
            }
        }

        /**
         * Checks if all tutorial steps have been completed.
         * @return true if there are no more steps and no current step is being displayed
         */
        public boolean isTutorialComplete() {
            return steps.isEmpty() && currentStep == null;
        }

        /**
         * Resets the tutorial to its initial state.
         * Hides the current step and reinitializes all steps.
         */
        public void resetTutorial() {
            hideCurrentStep();
            currentStep = null;
            steps.clear();
            initializeSteps();
            isActive = false;
        }

        /**
         * Checks if the tutorial is currently active.
         * @return true if the tutorial is being displayed
         */
        public boolean isActive() {
            return isActive;
        }

        /**
         * Gets the number of the current tutorial step.
         * @return the step number (0-based), or -1 if no step is currently displayed
         */
        public int getCurrentStepNumber() {
            return currentStep != null ? currentStep.getStep() : -1;
        }

        /**
         * Gets the current tutorial step component.
         * @return the current TutorialStep component, or null if no step is active
         */
        public TutorialStep getCurrentStep() {
            return currentStep;
        }

        public void checkTutorialProgression() {
            if (!isActive) return;
            int cid = parentCanvas.getGameClient().getClientId();
            Player serverPlayer = (Player) gameServerMaster.getPlayerFromClientId(cid);
            Room startRoom = gameServerMaster.getDungeonMap().getStartRoom();
            

            switch (currentStep.getStep()) {
                    case 0:  // MOVEMENT
                        startRoom.closeDoors();
                        if (serverPlayer.hasMovedSignificantly()) {
                            handleTutorialProgression();
                        }
                        break;
                    case 1: // ATTACK
                        if (serverPlayer.hasReachedAttackThreshold()) {
                            handleTutorialProgression();
                            startRoom.openDoors();
                        }
                        break;
                    case 2: // MOVE OUT ROOM
                        if (serverPlayer.getCurrentRoom() != startRoom) {
                            handleTutorialProgression();
                            lastShowTutorialStepTime = System.currentTimeMillis();
                        }
                        break;
                    case 3: // KILL ENEMIES
                        long now = System.currentTimeMillis();
                        if (now - lastShowTutorialStepTime > TUTORIAL_STEP_DURATION) {
                            hideCurrentStep();
                            lastShowTutorialStepTime = 0;
                        }

                        Room current = serverPlayer.getCurrentRoom();

                        // Lock doors for now until item is picked up
                        current.closeDoors();

                        if (current != startRoom && current.isCleared()) {
                            handleTutorialProgression();
                        }
                        // Ensure that an item is always dropped
                        ItemsHandler.updateItemDropChance("None", 0);

                        // Don't allow item pickups yet
                        gameServerMaster.setIsItemCollisionAllowed(false);
                        break;
                    case 4: // ENEMIES CAN DROP ITEMS
                        now = System.currentTimeMillis();
                        if (now - lastShowTutorialStepTime > TUTORIAL_STEP_DURATION) {
                            handleTutorialProgression();
                            lastShowTutorialStepTime = 0;
                        } 
                        break;
                    case 5: // DOORS WILL REAPPEAR ONLY AFTER CLEARING
                        parentCanvas.setRightClickAllowed(true);
                        roomBeforeBoss = serverPlayer.getCurrentRoom();
                        if (parentCanvas.getHasClickedOnItem()) {
                            handleTutorialProgression();
                            roomBeforeBoss.openDoors();
                            lastShowTutorialStepTime = System.currentTimeMillis();
                            ItemsHandler.updateItemDropChance("None", 60);
                            gameServerMaster.setIsItemCollisionAllowed(true);
                        }
                        break;
                    case 6: // BOSS ROOM
                        current = serverPlayer.getCurrentRoom();
                        
                        if (current == gameServerMaster.getDungeonMap().getEndRoom() &&
                            !hasShownTutorialStep) {
                            // Only progress if we just entered the boss room
                            lastShowTutorialStepTime = System.currentTimeMillis();
                            handleTutorialProgression();
                            hasShownTutorialStep = true;
                        }
                        break;
                    case 7: 
                        now = System.currentTimeMillis();
                            if (now - lastShowTutorialStepTime > TUTORIAL_STEP_DURATION - 1500) {
                                hideCurrentStep();
                                parentCanvas.remove(currentStep);
                                isActive = false;
                            }
                default:
                    break;
            }
        }

    public void handleTutorialProgression() {
        if (isActive) {
            showNextStep();
            SoundManager.getInstance().playPooledSound("levelUp");
        }
    }
        /**
         * Represents a single step in the tutorial sequence.
         * Each step displays a message in a semi-transparent box at the bottom of the screen.
         */
        public class TutorialStep extends JPanel {
            private final int step;
            private final String message;
            private final JLabel textContent;
            
            /** Array of all tutorial messages in sequence */
            public static final String[] messages = {
                "Use the W, A, S, and D keys to move.",
                "Press the left mouse button to attack.",
                "Move out the room to face some monsters.",
                "Use attacks to kill the enemies in the room.",
                "Enemies can drop items to boost your player's stats.",
                "Right click on an item to see its effects.",
                "Doors will reappear only once after a room has been cleared.",
                "Survive and defeat the boss. Good luck!"
            };
        
            /** Custom font for tutorial text */
            private static Font font;

            static {
                try {
                    font = Font.createFont(Font.TRUETYPE_FONT,
                            TutorialManager.class.getResourceAsStream("/resources/Fonts/PressStart2P-Regular.ttf"));
                } catch (FontFormatException | IOException e) {
                    System.out.println("Exception in TutorialManager font setting");
                }
            }
        
            public TutorialStep(int step) {
                this.step = step;
                this.message = messages[step];
                this.setOpaque(false);
                this.setLayout(new BorderLayout());
                this.setBounds(60, 490, 680, 75); // Let GameCanvas control position
        
                // textContent = new JLabel("<html><div style='text-align:center;'>" + message + "</div></html>");
                textContent = new JLabel(message);
                textContent.setFont(font.deriveFont(11f));
                textContent.setForeground(Color.WHITE);
                textContent.setHorizontalAlignment(SwingConstants.CENTER);
                textContent.setVerticalAlignment(SwingConstants.CENTER);
        
                add(textContent, BorderLayout.CENTER);
            }
        
            public int getStep() {
                return step;
            }
        
            public String getMessage() {
                return message;
            }
        
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
                // Draw semi-transparent background
                g2d.setColor(new Color(0, 0, 0, 128));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        
                // Draw white border
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
        
                g2d.dispose();

                // System.out.println("Painting tutorial step:" + message);
            }
        }
    }
