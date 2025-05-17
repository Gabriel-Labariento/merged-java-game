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
