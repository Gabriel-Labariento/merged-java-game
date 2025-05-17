import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class Room extends GameObject{
    
    public static final int WIDTH_TILES = 45;
    public static final int HEIGHT_TILES = 33;
    private int roomId;
    private int gameLevel;
    private int difficulty; // 0 => 3, easiest to hardest
    private boolean isStartRoom, isEndRoom, isClearedHandled, isCleared;
    private MobSpawner mobSpawner;
  
    private ArrayList<Room> connections;
    private HashMap<String, Room> doors;
    private ArrayList<Door> doorsArrayList;

    private BufferedImage backgroundImage;
    private boolean backgroundLoaded;

    /**
     * Creates a Room object with an ID, x and y coordinates, ArrayList of connections, and HashMap of doors.
     * @param roomId the unique int used to identify the room
     * @param x the x-coordinate of the room
     * @param y the y-coordinate of the room
     */
    public Room(int roomId, int x, int y, int gameLevel){
        this.roomId = roomId;
        this.worldX = x;
        this.worldY = y;
        height = GameCanvas.TILESIZE * HEIGHT_TILES;
        width = GameCanvas.TILESIZE * WIDTH_TILES;
        this.gameLevel = gameLevel;
        isStartRoom = false;
        isEndRoom = false;
        isClearedHandled = false;
        isCleared = false;

        connections = new ArrayList<>();
        doors = new HashMap<>();
        doorsArrayList = new ArrayList<>();
        backgroundLoaded = false;
    }

    /**
     * Returns a string containing the room data
     * @return a string in the format roomId,x,y,isStart,isEnd|door1Data|door2Data|...
     */
    public String serialize(){
        StringBuilder sb = new StringBuilder();

        // roomId,x,y,isStart,isEnd|
        sb.append(NetworkProtocol.ROOM)
        .append(roomId).append(NetworkProtocol.SUB_DELIMITER)
        .append(worldX).append(NetworkProtocol.SUB_DELIMITER)
        .append(worldY).append(NetworkProtocol.SUB_DELIMITER)
        .append(isStartRoom).append(NetworkProtocol.SUB_DELIMITER)
        .append(isEndRoom);

        for (Door door : doorsArrayList) {
            sb.append(NetworkProtocol.DELIMITER).append(door.serialize());
        }

        return sb.toString();
    }

    public void draw(Graphics2D g2d, int cameraX, int cameraY){
        // Border
        g2d.drawRect(worldX - cameraX, worldY - cameraY, width, height);

        loadBackgroundImage();
        g2d.drawImage(backgroundImage, worldX - cameraX, worldY - cameraY, null);
        
    }

    public void loadBackgroundImage(){
        if (backgroundLoaded) return;
        String path;
        switch (gameLevel) {
            case 0:
                path = "resources/Room Backgrounds/Junkyard.png";
                break;
            case 1:
                path = "resources/Room Backgrounds/Street.png";
                break;
            case 2:
                path = "resources/Room Backgrounds/Petshop.png";
                break;
            case 3:
                path = "resources/Room Backgrounds/Forest.png";
                break;
            case 4:
                path = "resources/Room Backgrounds/Mansion.png";
                break;
            case 5:
                path = "resources/Room Backgrounds/Building.png";
                break;
            case 6:
                path = "resources/Room Backgrounds/Sewer.png";
                break;
            default:
                throw new AssertionError("Assertion in assigning room background image path");
        }
        try {
            backgroundImage = ImageIO.read(getClass().getResourceAsStream(path));
            backgroundLoaded = true;
        } catch (IOException e) {
            System.out.println("IOException in loadBackgroundImage() of Room class: " + e);
        }
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
     * Opens all of the room's doors
     */
    public void openDoors(){
        // Safety first
        if (doorsArrayList == null || doorsArrayList.size() <= 0) return;

        for (Door d : doorsArrayList) {
            d.setIsOpen(true);            
        }
    }

    /**
     * Closes all of the room's doors
     */
    public void closeDoors(){
        // Safety first
        if (doorsArrayList == null || doorsArrayList.size() <= 0) return;

        for (Door d : doorsArrayList) {
            d.setIsOpen(false);            
        }
    }


    public void assignDifficulty(int gameLevel, int distanceFromStart){
        if (isStartRoom) { difficulty = 0; return; }
        if (isEndRoom) { difficulty = 3; return; }

        if (distanceFromStart <= 1) difficulty = 0;
        else if (distanceFromStart <= 3) difficulty = 1;
        else difficulty = 2;

        // Difficulty can be increased for normal rooms based on gameLevel, but capped at 2.
        difficulty = Math.min(2, difficulty + (gameLevel / 3));
    }



    //<----------------- METHODS BELOW USED FOR PROCEDURAL GENERATION LOGIC --------------------------------->>

    /**
     * Adds the "other" argument to the list of connections of the calling room and adds the calling room to the connections of "other."
     * Puts the corresponding room and direction of the other room to this room's doors hashmap, does the vice versa as well.
     * @param other the room to be connected to the calling room
     */
    public void connectRooms(String direction, Room other){
        // Add the other room to the list of connections
        connections.add(other);

        // Put in the room's hashmap, the direction and the connected room
        doors.put(direction, other);
        // doors.add(new Door)

        // Add this room to the other's connections 
        other.getConnections().add(this);
        
        // Determine the opposite direction of "direction" argument
        String oppositeDirection = getOppositeDirection(direction);

        // Put in other's hashmap, the opposite direction and this room.
        other.getDoors().put(oppositeDirection, this);
    }
    
    /**
     * Adds a door at a random direction of the room. Only one door can be placed in one direction.
     */
    public void addDoorToHashMap(){
        // Choose a random direction
        String direction;
        while (true){
            direction = chooseRandomDirection();
            if ((direction != null) && (!doors.containsKey(direction))){
                // If direction is not null, and the room does not yet have a door at that direction, add a door there
                doors.put(direction, null);
                break;
            }
        }
    }

    /**
     * Adds a door to the doorsArrayList field 
     * @param door the door object to be added
     */
    public void addDoorToArrayList(Door door){
        doorsArrayList.add(door);
    }

    /**
     * Chooses one random direction.
      * @return a String indicating the chosen direction: "T" "R" "B" "L". 
     */
    public String chooseRandomDirection(){
        int rand = (int) (Math.random() * 4);
        switch (rand) {
            case 0:
                return "T";
            case 1: 
                return "R";
            case 2:
                return "B";
            case 3:
                return "L";
            default:
                break;
        }
        return null;
    }


    /**
     * Checks whether the calling room has a direct connection to the passed room
     * @param other the room to which the calling room is checked for connection
     * @return
     */
    public boolean isConnected(Room other){
        return (connections.contains(other) && other.getConnections().contains(this));
    }

    /**
     * Checks if two rooms can be connected via the ff:
     * 1. Two rooms aren't the same room
     * 2. Two rooms aren't already connected
     * 3. The calling room doesn't already have a door at the specified direction.
     * 4. The passed room doesn't already have a door at the opposite direction.
     * @param direction where in the calling room the connection is to be made
     * @param other the room to be connected to the calling room
     * @return true / false whether or not the rooms can be connected
     */
    public boolean isConnectable(String direction, Room other){
        // Don't connect a room to itself
        if (this == other) return false;
        
        // Don't connect rooms if they're already connected
        if (isConnected(other)) return false;

        // Don't connect rooms at a direction that already has another door
        if ((doors.containsKey(direction)) && (doors.get(direction) != null)) return false;

        // Don't connect rooms if the other room already has a door at the opposite direction
        String oppositeDirection = getOppositeDirection(direction);
        if ( (other.getDoors().containsKey(oppositeDirection)) && (other.getDoors().get(oppositeDirection) != null)) return false;

        System.out.printf("Room %d and Room %d  can be connected.\n", roomId, other.getRoomId());
        return true;
    }

    /**
     * Return the ID of the room instance
     * @return the room's ID
     */
    public int getRoomId(){
        return roomId;
    }

    /**
     * Populates the doorsArrayList with new Door objects based on 
     * the contents of the doors HashMap. Called inside populateAllDoorsArrayList.
     */
    public void populateDoorsArrayList(){
        int centerX = getCenterX();
        int centerY = getCenterY();
        
        // Needed to position the door just right
        int doorHeight = Door.HEIGHT_TILES * GameCanvas.TILESIZE;
        int doorWidth = Door.WIDTH_TILES * GameCanvas.TILESIZE;
        Door d;

        for (HashMap.Entry<String, Room> door : doors.entrySet()) {
             switch (door.getKey()) {
                case "T":
                    d = new Door(centerX - doorWidth, worldY, door.getKey(), this, door.getValue());
                    doorsArrayList.add(d);
                    break;
                case "B":
                    d = new Door(centerX - doorWidth, worldY + height - doorHeight, door.getKey(), this, door.getValue());
                    doorsArrayList.add(d);
                    break;
                case "L":
                    d = new Door(worldX, centerY - doorHeight, door.getKey(), this, door.getValue());
                    doorsArrayList.add(d);
                    break;
                case "R":
                    d = new Door(worldX + width - doorWidth, centerY - doorHeight, door.getKey(), this, door.getValue());
                    doorsArrayList.add(d);
                    break;
                default:
                    throw new AssertionError("Error in populateDoorsArrayList method of Room " + roomId);
            }
        }
    }

    public Door createDoorFromDirection(String direction) {
        int centerX = getCenterX();
        int centerY = getCenterY();
        
        // Needed to position the door just right
        int doorHeight = Door.HEIGHT_TILES * GameCanvas.TILESIZE;
        int doorWidth = Door.WIDTH_TILES * GameCanvas.TILESIZE;
        Door d;

        switch (direction) {
            case "T":
                d = new Door(centerX - doorWidth, worldY, direction, this, null);
                doorsArrayList.add(d);
                break;
            case "B":
                d = new Door(centerX - doorWidth, worldY + height - doorHeight, direction, this, null);
                doorsArrayList.add(d);
                break;
            case "L":
                d = new Door(worldX, centerY - doorHeight, direction, this, null);
                doorsArrayList.add(d);
                break;
            case "R":
                d = new Door(worldX + width - doorWidth, centerY - doorHeight, direction, this, null);
                doorsArrayList.add(d);
                break;
            default:
                throw new AssertionError("Error in populateDoorsArrayList method of Room " + roomId);
        }

        return d;
    }

    /**
     * Checks if a door has less than two rooms
     * @return boolean true/false indicating whether a room has less than two doors
     */
    public boolean canAddMoreDoors(){
        return (doors.size() < 2);
    }

     /**
     * Gets the doors hashmap of the room object.
     * @return the doors hashmap in the form <String s, Room r> where s is the direction and r is the connected room
     */
    public HashMap<String, Room> getDoors(){
        return doors;
    }

    /**
     * Returns the ArrayList of Rooms that are connected to the colling room.
     * @return connections, an ArrayList of connected Rooms.
     */
    public ArrayList<Room> getConnections(){
        return connections;
    }

    /**
     * Returns the ArrayList of created Door objects of the calling Room.
     * @return doorsArrayList, an ArrayList of Door objects children to the Room.
     */
    public ArrayList<Door> getDoorsArrayList() {
        return doorsArrayList;
    }

    /**
     * Asks whether the calling room is the starting room of the map.
     * @return true if it is the starting room, false otherwise.
     */
    public boolean isStartRoom() {
        return isStartRoom;
    }

    /**
     * Asks whether the calling room is the end room of the map.
     * @return true if it is the end room, false otherwise.
     */
    public boolean isEndRoom() {
        return isEndRoom;
    }
    
    public void setIsStartRoom(boolean isStartRoom) {
        this.isStartRoom = isStartRoom;
    }


    public void setIsEndRoom(boolean isEndRoom) {
        this.isEndRoom = isEndRoom;
    }

    public MobSpawner getMobSpawner() {
        return mobSpawner;
    }

    public void setMobSpawner(MobSpawner mobSpawner) {
        this.mobSpawner = mobSpawner;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isClearedHandled() {
        return isClearedHandled;
    }

    public void setIsClearedHandled(boolean isClearedHandled) {
        this.isClearedHandled = isClearedHandled;
    }
    
    public boolean isCleared() {
        return isCleared;
    }

    public void setCleared(boolean isCleared) {
        this.isCleared = isCleared;
    }

    public int getGameLevel() {
        return gameLevel;
    }
    
}