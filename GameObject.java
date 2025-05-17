public abstract class GameObject {
    protected int worldX, worldY, height, width;
    protected int zIndex;
    int[] hitBoxBounds;

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

    public void setWidth(int w) {
        width = w;
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

    public int[] getHitBoxBounds() {
        if (hitBoxBounds == null) {
            return new int[] {worldY, worldY + height, worldX, worldX + width};
        }
        return hitBoxBounds;
    }

    public int[] getPositionVector(){
        int[] positionVector = new int[2];
        positionVector[0] = worldX + width / 2;
        positionVector[1] = worldY + height / 2;

        return positionVector;
    }

    public double getDistanceBetween(GameObject a, GameObject b){
        return Math.sqrt(
                    (Math.pow(a.getCenterX() - b.getCenterX(), 2) + 
                    Math.pow(a.getCenterY() - b.getCenterY(), 2))
                );
    } 

    public double getSquaredDistanceBetween(GameObject a, GameObject b) {
        int dx = a.getCenterX() - b.getCenterX();
        int dy = a.getCenterY() - b.getCenterY();
        return dx * dx + dy * dy;
    }

    public int getZIndex() {
        return 0;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

}