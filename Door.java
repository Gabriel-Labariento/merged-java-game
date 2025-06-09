import java.awt.Color;
import java.awt.Graphics2D;

/**     
        The Door class extends GameObject. Doors appear in Room
        objects and they simulate Room connection through Player
        collision. Doors can be open or closed.

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

public class Door extends GameObject {
    private int id;
    public static final int HEIGHT_TILES = 2;
    public static final int WIDTH_TILES = 2;
    private static int doorCount = 0;
    private String direction;
    private final Room roomA; // The door only appears on roomA, but is connected to another door in roomB 
    private Room roomB; 
    private boolean isOpen, isExitToNewDungeon;
    
    /**
     * Creates a Door instance with appropriate fields
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param direction the Direction in the Room where the Door is set: T, B, L, R
     * @param roomA the room where the Door appears
     * @param roomB the room where the Door in RoomA leads to
     */
    public Door(int x, int y, String direction, Room roomA, Room roomB){
        this.id = doorCount++;
        worldX = x;
        worldY = y;
        height = GameCanvas.TILESIZE * HEIGHT_TILES;
        width = GameCanvas.TILESIZE * WIDTH_TILES;
        isOpen = true;
        isExitToNewDungeon = false;
        this.direction = direction;
        this.roomA = roomA;
        this.roomB = roomB;
    }

    /**
     * Draws a black door taking into account the given offsets
     * @param g2d Graphics2D object for drawing
     * @param offsetX camera x offset in GameCanvas
     * @param offsetY camera y offset in GameCanvas
     */
    public void draw(Graphics2D g2d, int offsetX, int offsetY) {
        if (!ServerMaster.getInstance().getDungeonMap().getRoomFromId(roomA.getRoomId()).isCleared()) return;
        g2d.setColor(Color.black);
        g2d.fillRect(worldX - offsetX, worldY - offsetY, width, height);
    }

    /**
     * Returnes the Room to which the passed door is connected to by the calling Door instance
     * @param current the room currently known
     * @return the room the current room is connected to through this Door
     */
    public Room getOtherRoom(Room current){
        return (current == roomA) ? roomB : roomA;
    }

    @Override
    public void matchHitBoxBounds() {
        hitBoxBounds = new int[4];
        hitBoxBounds[0]= worldY;
        hitBoxBounds[1] = worldY + height;
        hitBoxBounds[2]= worldX;
        hitBoxBounds[3] = worldX + width;
    }

    
    /**
     * Returns a string containing the door data
     * @return a string with the format D:doorId,x,y,direction,roomAId,roomBId,isOpen
     */
    public String serialize(){
        int roomAID = (roomA == null) ? -1 : roomA.getRoomId();
        int roomBID = (roomB == null) ? -1 : roomB.getRoomId();

        StringBuilder sb = new StringBuilder();
        sb.append(NetworkProtocol.DOOR)
        .append(id).append(NetworkProtocol.SUB_DELIMITER)
        .append(worldX).append(NetworkProtocol.SUB_DELIMITER)
        .append(worldY).append(NetworkProtocol.SUB_DELIMITER)
        .append(direction).append(NetworkProtocol.SUB_DELIMITER)
        .append(roomAID).append(NetworkProtocol.SUB_DELIMITER)
        .append(roomBID).append(NetworkProtocol.SUB_DELIMITER)
        .append(isOpen);

        return sb.toString();
    }

    /**
     * Returns the value of the direction field
     * @return one of four string values: "T" "B" "L" or "R"
     */
    public String getDirection() {
        return direction;
    }

    /**
     * Sets the direction field of the instance to the passed String
     * @param direction the direction to set the direction field to
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * Gets the value of RoomA
     * @return a Room object reference to RoomA
     */
    public Room getRoomA() {
        return roomA;
    }

    /**
     * Gets the value of RoomB
     * @return a Room object reference to RoomB
     */
    public Room getRoomB() {
        return roomB;
    }

    /**
     * Gets the Door's ID
     * @return the ID of the Door
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the Id field value to the passed argument
     * @param id the int value to set Id to
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Checks the value of the isOpen field
     * @return true if the door is open, false otherwise
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Sets the value of isOpen to the provided boolean
     * @param isOpen true/false, the boolean value to set isOpen to
     */
    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    /**
     * Sets the value of roomB to the provided Room
     * @param roomB the Room to set RoomB to
     */
    public void setRoomB(Room roomB) {
        this.roomB = roomB;
    }

    /**
     * Gets the value of isExitToNewDungeon
     * @return true if the door is an exit to a new dungeon, false otherwise
     */
    public boolean isExitToNewDungeon() {
        return isExitToNewDungeon;
    }

    /**
     * Sets the field value of isExitToNewDungeon to the provided boolean
     * @param isExitToNewDungeon true/false, the value to set isExitToNewDungeon to
     */
    public void setIsExitToNewDungeon(boolean isExitToNewDungeon) {
        this.isExitToNewDungeon = isExitToNewDungeon;
    }
}