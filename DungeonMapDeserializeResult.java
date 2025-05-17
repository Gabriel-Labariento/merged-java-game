import java.util.*;

public class DungeonMapDeserializeResult {
    private final Room startRoom;
    private HashMap<Integer, Room> allRooms = new HashMap<>();

    public DungeonMapDeserializeResult(Room startRoom, HashMap<Integer, Room> allRooms){
        this.startRoom = startRoom;
        this.allRooms = allRooms;
    }

    public Room getStartRoom() {
        return startRoom;
    }

    public HashMap<Integer, Room> getAllRooms() {
        return allRooms;
    }
}