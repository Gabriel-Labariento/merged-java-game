import java.awt.*;
import java.util.HashMap;
import javax.swing.*;

/**
 * The Minimap class extends JComponent and serves as a HUD element that displays
 * the current room and its connections in the upper right corner of the game canvas.
 * It shows a simplified view of the dungeon map with the current room highlighted.
 */
public class MiniMap extends JComponent {
    public static final int X = 586;
    public static final int Y = 30;
    public static final int WIDTH = 165;
    public static final int HEIGHT = 120;
    public static final int ROOM_SIZE = 15;
    public static final int ROWS = 8;
    public static final int COLS = 11;
    public static final Color CURRENT_ROOM_COLOR = new Color(255, 255, 255);
    public static final Color CONNECTED_ROOM_COLOR = new Color(100, 100, 100);
    public static final Color CLEARED_ROOM_COLOR = new Color(0, 255, 0, 150); // Semi-transparent green
    public static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 150);
    public static final Color BORDER_COLOR = new Color(255, 255, 255, 100);
    public static final int CENTER_CELL_X = X + COLS / 2 * ROOM_SIZE;
    public static final int CENTER_CELL_Y = Y + ROWS / 2 * ROOM_SIZE;

    private Room currentRoom;
    private HashMap<Room, Point> roomPositions; // Now populated by server data

    /**
     * Creates a new Minimap instance positioned in the upper right corner
     */
    public MiniMap() {
        setBounds(X, Y, WIDTH, HEIGHT);
        setOpaque(false);
        roomPositions = new HashMap<>();
    }

    /**
     * Updates the minimap with server-provided room data
     * @param currentRoom the room the player is currently in
     * @param visibleRooms map of rooms that should be visible on the minimap with their positions
     */
    public void update(Room currentRoom, HashMap<Room, Point> visibleRooms) {
        this.currentRoom = currentRoom;
        this.roomPositions = visibleRooms;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        // System.out.println("Painting minimap - Current room: " + currentRoom);
        // System.out.println("Room positions: " + roomPositions.size());
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing
        RenderingHints rh = new RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);

        // Draw semi-transparent background
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(X, Y, WIDTH, HEIGHT);

        // Draw border
        g2d.setColor(BORDER_COLOR);
        g2d.drawRect(X, Y, WIDTH, HEIGHT);

        if (currentRoom == null) return;

        // Draw all visible rooms
        for (Room room : roomPositions.keySet()) {
            Point pos = roomPositions.get(room);
            if (pos == null) continue;
            
            int drawX = CENTER_CELL_X + pos.x * ROOM_SIZE;
            int drawY = CENTER_CELL_Y + pos.y * ROOM_SIZE;
            
            // Choose color based on room state
            if (room == currentRoom) {
                g2d.setColor(CURRENT_ROOM_COLOR);
            } else if (ServerMaster.getInstance().getDungeonMap().getRoomFromId(room.getRoomId()).isCleared()) {
                g2d.setColor(CLEARED_ROOM_COLOR);
            } else {
                g2d.setColor(CONNECTED_ROOM_COLOR);
            }
            
            g2d.fillRect(drawX, drawY, ROOM_SIZE, ROOM_SIZE);
        }
    }
}
