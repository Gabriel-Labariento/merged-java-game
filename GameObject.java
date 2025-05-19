/**     
        The GameObject class is an abstract class. It is the main class extended by
        Entity, Room, Door, and PlayerUI. It mainly holds methods for position,
        collision, and render order value.

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

public abstract class GameObject {
    protected int worldX, worldY, height, width;
    protected int zIndex;
    int[] hitBoxBounds;

    /**
     * Updates the GameObject's hitBoxBounds to match its 
     * current position. Important to call after updates
     * because hitbox is used to track entity collision
     */
    public abstract void matchHitBoxBounds();

    /**
     * Returns the x position of the object in terms of the entire game world. 
     * @return the object's world x-coordinate
     */
    public int getWorldX() {
        return worldX;
    }

    /**
     * Returns the y position of the object in terms of the entire game world
     * @return the object's world y-coordinate
     */
    public int getWorldY() {
        return worldY;
    }

    /**
     * Computes for the object's centerX via its x-position and width 
     * @return integer value of the object's center x coordinate
     */
    public int getCenterX() {
        return ( (int) (worldX + width / 2));
    }

    /**
     * Computes for the object's centerY via its y-position and height
     * @return integer value of the object's center y coordinate
     */
    public int getCenterY() {
        return ( (int) (worldY + height / 2));
    }

    /**
     * Gets the object's height
     * @return an integer which is the object's height 
     */
    public int getHeight() {
        return height;
    }

    public void setHeight(int h){
        height = h;
    }

    /**
     * Gets the object's width
     * @return an integer which is the object's width 
     */
    public int getWidth() {
        return width;
    }

    /**
     * Checks for collision with other GameObject
     * @param other object colliding/not colliding with
     * @return true if colliding with other, false otherwise.
     */
    public boolean isColliding(GameObject other){
        return !((worldX < other.getWorldX()) ||
                ( (worldX + width) > other.getWorldX() + other.getWidth()) ||
                (worldY < other.getWorldY()) ||
                ((worldY + height) > other.getWorldY() + other.getHeight())
        );
    }

    /**
     * Provides the opposite of the provided direction
     * @param direction the direction whose opposite is to be determined
     * @return the opposite of the passed direction "T" <-> "B" and "L" <-> "R"
     */
    public static String getOppositeDirection(String direction){
        switch (direction) {
            case "T":
                return "B";
            case "R":
                return "L";
            case "B":
                return "T";
            case "L":
                return "R";
            default:
                throw new AssertionError("Assertion in getOppositeDirection() method of the Room.");
        }
    }

    /**
     * Gets the object's hitBoxBounds
     * @return an int array corresponding to the object's hitbox. 
     * INDEX  | DIRECTION
     * 0      | TOP
     * 1      | BOTTOM
     * 2      | LEFT
     * 3      | RIGHT 
     */
    public int[] getHitBoxBounds() {
        // If hitboxBounds is not set, just use the object's area covered
        if (hitBoxBounds == null) {
            return new int[] {worldY, worldY + height, worldX, worldX + width};
        }
        return hitBoxBounds;
    }

    /**
     * Gets the object's position vector based in its center
     * @return an int array. Index 0 is the object's center x position. Index 1 is its center y position.
     */
    public int[] getPositionVector(){
        int[] positionVector = new int[2];
        positionVector[0] = getCenterX();
        positionVector[1] = getCenterY();

        return positionVector;
    }

    /**
     * Gets the squared distance between two objects based on their centers
     * @param a the first object
     * @param b the second object
     * @return a double representing the squared distance between the centers of a and b
     */
    public double getSquaredDistanceBetween(GameObject a, GameObject b) {
        int dx = a.getCenterX() - b.getCenterX();
        int dy = a.getCenterY() - b.getCenterY();
        return dx * dx + dy * dy;
    }

    /**
     * Gets the object's zIndex
     * @return always returns 0 for GameObjects that do not override this method 
     */
    public int getZIndex() {
        return 0;
    }

    /**
     * Sets the zIndex value of the object to the passed argument
     * @param zIndex the int value to set zIndex to
     */
    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

}