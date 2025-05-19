/**     
        The GameStarter class contains the main method used to start the game.
        It initializes a new GameFrame, establishes a connection to the client, 
        and calls necessary set up methods from GameFrame.

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

public class GameStarter {
    public static void main(String[] args) {
        GameFrame gameFrame = new GameFrame(800, 600, "Biting on Fish");
        GameClient gameClient = (gameFrame.getCanvas()).getGameClient();
        gameClient.closeSocketsOnShutdown();
        gameFrame.setUpGUI();
        gameFrame.setUpButtons();
        gameFrame.addKeyBindings();
        gameFrame.getCanvas().startRenderLoop();
    }
}
