import java.awt.Point;
import java.util.*;

/**
 * Manages the minimap state on the server side, handling room positions
 * and player discoveries for the minimap display.
 */
public class MiniMapManager {
    private HashMap<Room, Point> roomPositions;
    private HashMap<Integer, Set<Room>> playerDiscoveredRooms; // Maps client IDs to discovered rooms
    private HashMap<Integer, Set<Room>> playerVisitedRooms; // Maps client IDs to visited rooms
    private static final int ROWS = 8; 
    private static final int COLS = 11;

    public MiniMapManager() {
        roomPositions = new HashMap<>();
        playerDiscoveredRooms = new HashMap<>();
        playerVisitedRooms = new HashMap<>();
    }

    /**
     * Initializes the minimap with a starting room
     * @param startRoom The first room in the dungeon
     */
    public void initializeMap(Room startRoom) {
        roomPositions.clear();
        playerDiscoveredRooms.clear();
        playerVisitedRooms.clear();

        // Place the start room at the center of the grid
        roomPositions.put(startRoom, new Point(0, 0));
        
        // Initialize the room's connections
        initializeRoomConnections(startRoom);
    }

    /**
     * Recursively initializes connections for a room and its connected rooms
     */
    private void initializeRoomConnections(Room room) {
        Point currentPos = roomPositions.get(room);
        if (currentPos == null) return;

        // Check all four directions
        String[] directions = {"T", "B", "L", "R"};
        for (String dir : directions) {
            Room connectedRoom = room.getConnectionAtDirection(dir);
            if (connectedRoom != null && !roomPositions.containsKey(connectedRoom)) {
                Point newPos = calculateNewPosition(currentPos, dir);
                if (isValidPosition(newPos)) {
                    roomPositions.put(connectedRoom, newPos);
                    initializeRoomConnections(connectedRoom);
                }
            }
        }
    }

    /**
     * Calculates the position of a connected room based on direction
     */
    private Point calculateNewPosition(Point currentPos, String direction) {
        Point newPos = new Point(currentPos);
        switch (direction) {
            case "T": newPos.y--; break;
            case "B": newPos.y++; break;
            case "L": newPos.x--; break;
            case "R": newPos.x++; break;
        }
        return newPos;
    }

    /**
     * Checks if a position is within the valid grid bounds
     */
    private boolean isValidPosition(Point pos) {
        return pos.x < COLS && pos.y < ROWS;
    }

    /**
     * Records that a player has discovered a room
     * @param clientId The ID of the client
     * @param room The room that was discovered
     */
    public void discoverRoom(int clientId, Room room) {
        // Add to discovered rooms
        playerDiscoveredRooms.computeIfAbsent(clientId, k -> new HashSet<>()).add(room);
        
        // Add to visited rooms
        playerVisitedRooms.computeIfAbsent(clientId, k -> new HashSet<>()).add(room);
        
        // Also discover connected rooms (but don't mark them as visited)
        String[] directions = {"T", "B", "L", "R"};
        for (String dir : directions) {
            Room connectedRoom = room.getConnectionAtDirection(dir);
            if (connectedRoom != null) {
                playerDiscoveredRooms.get(clientId).add(connectedRoom);
            }
        }
    }

    /**
     * Gets the visible rooms for a player
     * @param clientId The ID of the client
     * @return A map of rooms and their positions that should be visible to the player
     */
    public HashMap<Room, Point> getVisibleRooms(int clientId) {
        HashMap<Room, Point> visibleRooms = new HashMap<>();
        Set<Room> visitedRooms = playerVisitedRooms.get(clientId);
        
        if (visitedRooms != null) {
            for (Room room : visitedRooms) {
                Point pos = roomPositions.get(room);
                if (pos != null) {
                    visibleRooms.put(room, pos);
                }
            }
        }
        
        return visibleRooms;
    }

    /**
     * Gets the position of a specific room
     * @param room The room to get the position for
     * @return The position of the room, or null if not found
     */
    public Point getRoomPosition(Room room) {
        return roomPositions.get(room);
    }

    /**
     * Clears the discovered rooms for a client
     * @param clientId The ID of the client
     */
    public void clearDiscoveredRooms(int clientId) {
        playerDiscoveredRooms.remove(clientId);
        playerVisitedRooms.remove(clientId);
    }

    /**
     * Adds a new player to the minimap system and marks the starting room as visited
     * @param clientId The ID of the new client
     * @param startRoom The starting room of the current level
     */
    public void addNewPlayer(int clientId, Room startRoom) {
        // Initialize sets for the new player
        playerDiscoveredRooms.computeIfAbsent(clientId, k -> new HashSet<>()).add(startRoom);
        playerVisitedRooms.computeIfAbsent(clientId, k -> new HashSet<>()).add(startRoom);
        
        // Also discover connected rooms (but don't mark them as visited)
        String[] directions = {"T", "B", "L", "R"};
        for (String dir : directions) {
            Room connectedRoom = startRoom.getConnectionAtDirection(dir);
            if (connectedRoom != null) {
                playerDiscoveredRooms.get(clientId).add(connectedRoom);
            }
        }
    }
}