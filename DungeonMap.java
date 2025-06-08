import java.util.*;

/**     
        The DungeonMap is the main map of the game. It contains
        rooms that it connects. The number of Rooms increase
        directly with the game level. The rooms are procedurally
        connected and checked for connections through a BFS algorithm.

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

public class DungeonMap {
    private final ArrayList<Room> rooms;
    private Room startRoom, endRoom;
    private int gameLevel;

    /**
     * Creates a DungeonMap instance setting the gameLevel field
     * @param gameLevel the value to set gameLevel to
     */
    public DungeonMap (int gameLevel) {
        rooms = new ArrayList<>();
        this.gameLevel = gameLevel;
    }

    /**
     * Creates a DungeonMap instance without setting gameLevel.
     * Used for client side rendering
     */
    public DungeonMap () {
        rooms = new ArrayList<>();
    }


    /**
     * Generates a certain number of rooms (numRooms <= 3). Connects them and calls pickStartAndEndRooms() and populateAllRoomsDoorsArrayList()
     * @param numRooms number of rooms to be made, minimum of 3.
     */
    public void generateRooms() {
        // Algorithm will not work for when rooms < 3. So ensure 3 is the minumum
        int numRooms = 3 + gameLevel;

        final int MAX_ATTEMPTS = 10;
        int attempts = 0;

        // Room generation
        while (attempts < MAX_ATTEMPTS) {
            attempts++;
            rooms.clear();
            createRoomsNoConnections(numRooms);
            
            // Room Connection
            for (Room room : rooms) {
                // In the current room, generate 2 doors at random directions
                int maxDoors = 2;
                while (room.getDoors().size() < maxDoors) {
                    room.addDoorToHashMap();
                }

                HashMap<String, Room> doors = room.getDoors();

                // For each door in the current room
                for (HashMap.Entry<String, Room> door : doors.entrySet()) {

                    String direction = door.getKey();

                    // Skip the door if a room is already conected to it
                    if (door.getValue() != null)
                        continue;
                
                    int MAX_CONNECTION_ATTEMPTS = 20;
                    int connectionAttempts = 0;

                    Room randomRoom;
                    while (connectionAttempts < MAX_CONNECTION_ATTEMPTS) {
                        connectionAttempts++;
                        // Try to add a room at the specified direction of the room
                        randomRoom = chooseRandomRoom();
                        if (room.isConnectable(direction, randomRoom) && (randomRoom.canAddMoreDoors())) {
                            room.connectRooms(direction, randomRoom);
                            break;
                        } 
                    }
                    
                    // If all rooms have been connected, stop.
                    if (areAllRoomsConnected()) {
                        pickStartAndEndRooms();
                        assignRoomDifficulties();
                        populateAllRoomsDoorsArrayList();
                        return;
                    } 
                }
            }
        }
    }

    
    /**
     * Assigns a room's "difficulty" (in terms of the enemies) depending on the current gameLevel
     * and its "distance" (how many connections away) it is from the starting room.
     */
    private void assignRoomDifficulties(){
        HashMap<Room, Integer> roomToDistanceFromStart = calculateDistancesFromStart();

        for (Room room : rooms) {
            // Assign difficulty of the room based on the current gameLevel and its distance from the start.
            int distance = roomToDistanceFromStart.get(room);
            room.assignDifficulty(gameLevel, distance);

            if (!(room.isStartRoom())) {
                MobSpawner spawner = new MobSpawner(gameLevel, room.getDifficulty());
                room.setMobSpawner(spawner);  // Delay spawning until player enters
                spawner.setParentRoom(room);
            }

            // Handle boss room
            if (room.isEndRoom()) {
                MobSpawner bossSpawner = new MobSpawner(gameLevel, 3);
                bossSpawner.setInBossRoom(true);
                room.setMobSpawner(bossSpawner);    
                bossSpawner.setParentRoom(room);
            }
        }

    }

    /**
     * Populates the doors ArrayList of all the rooms in the map only after they have been all connected.
     */
    private void populateAllRoomsDoorsArrayList(){
        for (Room room : rooms) {
            room.populateDoorsArrayList();
        }
    }

    /**
     * Uses a BFS algorithm to check if all the rooms of the map can be traversed to
     * from any other room.
     * 
     * @return a boolean corresponding that says if all the rooms are traversable
     */
    private boolean areAllRoomsConnected() {

        Queue<Room> queue = new LinkedList<>();
        Set<Room> visited = new HashSet<>();

        Room startingRoom = rooms.get(0);
        queue.add(startingRoom);
        visited.add(startingRoom);

        while (!queue.isEmpty()) {
            // Get (also removes) the first element of the queue
            Room currentRoom = queue.poll();

            for (Room neighbor : currentRoom.getConnections()) {
                // Check if all the rooms connected to this room have been visited
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    // If this is the first time visiting the neighbor, that means we haven't dealt
                    // with ITS neighbors so we add to the queue
                    queue.add(neighbor);
                }
            }
        }

        // If the number of visited rooms == the number of rooms there are, then all
        // rooms are reachable.
        return visited.size() == rooms.size();
    }

    /**
     * Returns a random room from the rooms ArrayList.
     * @return the random room chosen
     */
    private Room chooseRandomRoom() {
        return rooms.get((int) (Math.random() * rooms.size()));
    }

    /**
     * Creates numRooms number of distinct rooms laid out to prevent collisions
     * and adds them to the rooms ArrayList. 
     * @param numRooms the number of rooms to be made
     */
    private void createRoomsNoConnections(int numRooms) {
        int x = 0;
        int y = 0;

        // Create the rooms, no connections yet
        for (int i = 0; i < numRooms; i++) {
            Room r = new Room(i, x, y, gameLevel);
            rooms.add(r);
        }
    }

    /**
     * Randomly chooses a starting room and then sets the end room as the farthest from start room in terms of how many connections are in between them.
     */
    private void pickStartAndEndRooms() {
        // If called before all the rooms are connected, return.
        if (!areAllRoomsConnected()) {
            // System.out.println("Failed to pick start and end rooms. Not all rooms are connected yet.");
            return;
        }

        // Pick a room from the rooms
        Room a = chooseRandomRoom();

        // Determine the farthest point from that room, call that b
        Room b = getFurthestRoom(a);

        // Determine the farthest point from b, call that c
        Room c = getFurthestRoom(b);

        // b and c are the diameter end nodes of the graph.
        startRoom = b;
        endRoom = c;

        startRoom.setIsStartRoom(true);
        startRoom.setCleared(true);
        startRoom.setVisited(true);
        endRoom.setIsEndRoom(true);

        // System.out.println("Start Room is Room " + startRoom.getRoomId());
        // System.out.println("End Room is Room " + endRoom.getRoomId());
    }

    /**
     * Returns the furthest room from a given room in terms of how many connections are between them
     * @param start the room from which to start
     * @return the furthest room from the given argument
     */
    private Room getFurthestRoom(Room start) {

        Queue<Room> queue = new LinkedList<>();
        Set<Room> visited = new HashSet<>();
        HashMap<Room, Integer> distance = new HashMap<>();

        Room startingRoom = start;
        queue.add(startingRoom);
        visited.add(startingRoom);
        distance.put(start, 0);

        Room furthest = start;

        while (!queue.isEmpty()) {
            Room currentRoom = queue.poll();

            for (Room neighbor : currentRoom.getConnections()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    distance.put(neighbor, distance.get(currentRoom) + 1);

                    if (distance.get(neighbor) > distance.get(furthest)) {
                        furthest = neighbor;
                    }
                }
            }
        }
        return furthest;
    }

    /**
     * Calculates each Room's distance (in connections) from the determined starting room through BFS.
     * @return a HashMap that maps a room to its distance from the starting room.
     */
    private HashMap<Room, Integer> calculateDistancesFromStart(){
        Queue<Room> queue = new LinkedList<>();
        Set<Room> visited = new HashSet<>();
        HashMap<Room, Integer> distances = new HashMap<>();

        queue.add(startRoom);
        visited.add(startRoom);
        distances.put(startRoom, 0);

        while (!queue.isEmpty()) {
            Room currentRoom = queue.poll();

            for (Room neighbor : currentRoom.getConnections()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    distances.put(neighbor, distances.get(currentRoom) + 1);
                }
            }
        }
        return distances;
    } 


    /**
     * Returns a string containing the DungeonMap data
     * @return a string in the example format with interpretation.
     *  M:3| => Map for gameLevel 3
        R:0,0,0,false,false|    => RoomId 0 at 0,0. not start room. not end room.
        D:0,30,70,B,0,1|        => DoorId 0 at 30,70. Door at bottom of Room 0. Connects Room 0 and Room 1 
        D:1,30,0,T,0,2|         => DoorId 1 at 30,0. Door at top of Room 0. Connects Room 0 and Room 2
        R:1,100,100,true,false| => RoomId 1 at 100,100. is start room. not end room.
        D:2,130,100,T,1,0|      => DoorId 2 at 130, 100. Door at top of Room 1. Connects Room 1 and Room 0
        R:2,200,200,false,true| => RoomId 2 at 200,200. not start room. is end room.
        D:3,230,270,B,2,0|      => DoorId 2 at 230,270. Door at bottom of Room 2. Connect Room 2 and Room 0 
        1                       => Starting room is Room Id 1
     */
    public String serialize(){

        StringBuilder sb = new StringBuilder();
        // 1. Game level
        sb.append(NetworkProtocol.MAP_DATA).append(gameLevel).append(NetworkProtocol.DELIMITER);
        // 2. Data of each room
        for (Room room : rooms) {
            sb.append(room.serialize()).append(NetworkProtocol.DELIMITER);
        }
        // 3. Starting room
        sb.append(startRoom.getRoomId());

        return sb.toString();
    }


    /**
     * Deserializes the serialized map data string and returns the start room.
     * @param message the serialized map data string to be parsed
     * @return the starting room where the players will spawn.
     */
    public DungeonMapDeserializeResult deserialize(String message){
        
        // System.out.println("Received in deserialize(): " + message);
        // Clear data 
        rooms.clear();
        
       
        // Helper utils
        HashMap<Integer, Room> mapIdToRoom = new HashMap<>();
        ArrayList<DoorDataHolder> doorDataList = new ArrayList<>();
        Room startRoom = null;

        String[] messageParts = message.split("\\" + NetworkProtocol.DELIMITER); // Split at "|"
        
        // Part 1: Deserialize gamelevel, Rooms and Doors
        for (String part : messageParts) {
            if (part.startsWith(NetworkProtocol.MAP_DATA)) {
                this.gameLevel = Integer.parseInt(part.substring(NetworkProtocol.MAP_DATA.length()));
            } else if (part.startsWith(NetworkProtocol.ROOM )){
                // Parse roomData
                deserializeRooms(part.substring(NetworkProtocol.ROOM.length()), mapIdToRoom);
            } else if (part.startsWith(NetworkProtocol.DOOR)) {
                deserializeDoors(part.substring(NetworkProtocol.DOOR.length()), doorDataList);
            }
        }

        // Part 2: Connect Doors
        for (DoorDataHolder ddh : doorDataList) {
            try {
                Door door = ddh.createDoorFromDoorData(mapIdToRoom);
                door.getRoomA().addDoorToArrayList(door);
            } catch (Exception e) {
                System.out.println("Exception in adding doors for dungeon deserialize()");
            }
        }

        // Part 4: Set start room
        String lastPart = messageParts[messageParts.length - 1];
        if (!lastPart.contains(":")) {
            startRoom = mapIdToRoom.get(Integer.valueOf(lastPart));
        }

        // if (startRoom == null) System.out.println("Start room is null");
        
        return new DungeonMapDeserializeResult(startRoom, mapIdToRoom, gameLevel);
    }

    /**
     * Deserializes a part of the serialized String responsible for Room data.
     * @param messagePart a substring of the serialized string that contains Room data to be deserialized
     * @param mapIdToRoom a hashmap from the deserialize() method that maps a Room object to its ID. 
     */
    private void deserializeRooms(String messagePart, HashMap<Integer, Room> mapIdToRoom){
        String roomData[] = messagePart.split(NetworkProtocol.SUB_DELIMITER);
        int roomId = Integer.parseInt(roomData[0]);
        int roomX = Integer.parseInt(roomData[1]);
        int roomY = Integer.parseInt(roomData[2]);
        boolean isStart = Boolean.parseBoolean(roomData[3]);
        boolean isEnd = Boolean.parseBoolean(roomData[4]);

        Room r = new Room(roomId, roomX, roomY, gameLevel);
        r.setIsStartRoom(isStart);
        r.setIsEndRoom(isEnd);
        mapIdToRoom.put(roomId, r);
        rooms.add(r);
    }

    /**
     * Deserializes a part of the serialized String responsible for Door data and stores it in a doorDataHolder
     * that is added to the doorDataList.
     * @param messagePart a substring of the serialized string that contains Door data to be deserialized
     * @param doorDataList an ArrayList from the deserialize() method that contains a all doorDataHolders created
     */
    private void deserializeDoors(String messagePart, ArrayList<DoorDataHolder> doorDataList) {
        String doorData[] = messagePart.split(NetworkProtocol.SUB_DELIMITER);
        int doorId = Integer.parseInt(doorData[0]);
        int doorX = Integer.parseInt(doorData[1]);
        int doorY = Integer.parseInt(doorData[2]);
        String doorDirection = doorData[3];
        int roomAID = Integer.parseInt(doorData[4]);
        int roomBID = Integer.parseInt(doorData[5]);
        boolean isOpen = doorData.length > 6 ? Boolean.parseBoolean(doorData[6]) : true; // Default to true for backward compatibility
        if (roomBID == -1) return;
        doorDataList.add(new DoorDataHolder(doorId, doorX, doorY, doorDirection, roomAID, roomBID, isOpen));
    }

    /**
     * Searches for and returns a room based on its id
     * @param id the id of the room
     * @return the room object with the corresponding id
     */
    public Room getRoomFromId(int id) {
        for (Room room : rooms) {
            if (id == room.getRoomId()) return room;
        }
        return null;
    }

    /**
     * Gets the rooms ArrayList
     * @return the rooms ArrayList of the instance
     */
    public ArrayList<Room> getRooms() {
        return rooms;
    }

    /**
     * Gets a reference to the instance's starting Room
     * @return a Room reference to the startRoom
     */
    public Room getStartRoom() {
        return startRoom;
    }

    /**
     * Gets a reference to the instance's ending Room
     * @return a Room reference to the endRoom
     */
    public Room getEndRoom() {
        return endRoom;
    }

    /**
     * Gets the value of the gameLevel field
     * @return int value of gameLevel
     */
    public int getGameLevel() {
        return gameLevel;
    }

    /**
     * Holds door data after deserialization for door creation.
     * Needed to avoid null errors as in the parsed String door data
     * comes in between Room data. Door might try to connect to a Room
     * that does not yet exist. From the data stored in the instance of
     * DoorDataHolder, a new Door object can be made.
     */
    private class DoorDataHolder{
        private final int id, x, y, roomAId, roomBId;
        private final String direction;
        private final boolean isOpen;

        /**
         * Creates a DoorDataHolder instance with fields set to the passed arguments
         * @param id the door id
         * @param x the x-coordinate
         * @param y the y-coordinate
         * @param direction the direction in the room where the door appears
         * @param roomAId the id of RoomA
         * @param roomBId the id of RoomB
         * @param isOpen whether the door is open
         */
        public DoorDataHolder(int id, int x, int y, String direction, int roomAId, int roomBId, boolean isOpen) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.roomAId = roomAId;
            this.roomBId = roomBId;
            this.direction = direction;
            this.isOpen = isOpen;
        }

        /**
         * Using the data stored in the DoorDataHolder instance to create a Door.
         * @param mapIdToRoom a HashMap that maps a RoomId to the Room object
         * @return a Door created from the data stored in DoorDataHolder
         */
        public Door createDoorFromDoorData(HashMap<Integer, Room> mapIdToRoom ) {
            Door d = new Door(x, y, direction, mapIdToRoom.get(roomAId), mapIdToRoom.get(roomBId));
            d.setId(this.id);
            d.setIsOpen(this.isOpen);
            return d;
        }
    }
}
