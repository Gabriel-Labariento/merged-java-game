/**     
        The NetworkProtocol holds the essential information for data sending
        between client and server. It simplifies this communication by 
        creating public static final constants referenced by communication
        logic.

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

public class NetworkProtocol {
    public static final String PLAYER = "P:";  // Player data
    public static final String USER_PLAYER = "P$:"; // user player data
    public static final String ENTITY = "E:";  // Other entities
    public static final String HEAVYCAT = "A";
    public static final String FASTCAT = "B";
    public static final String GUNCAT = "C";
    public static final String PLAYERSMASH = "D";
    public static final String PLAYERSLASH = "E";
    public static final String PLAYERBULLET = "F";

    public static final String SPIDER = "G";
    public static final String COCKROACH = "H";
    public static final String RAT = "I";
    public static final String SMALLDOG = "J";
    public static final String BUNNY = "K";
    public static final String FROG = "L";
    public static final String BEE = "M";
    public static final String SNAKELET = "N";
    public static final String CLEANINGBOT = "O";
    public static final String SECURITYBOT = "P";
    public static final String FERALRAT = "Q";
    public static final String SCREAMERRAT = "R";
    public static final String MUTATEDANCHOVY = "S";
    public static final String MUTATEDARCHERFISH = "T"; // Rat
    public static final String MUTATEDPUFFERFISH= "U";
    public static final String RATKING = "V";
    public static final String FERALDOG = "W";
    public static final String TURTLE = "X";
    public static final String SNAKE = "Y";
    public static final String ADULTCAT = "Z";
    public static final String CONJOINEDRATS = "AA";
    public static final String FISHMONSTER = "AB";

    public static final String SPIDERBULLET = "AC";
    public static final String LASERBULLET = "AD";
    public static final String SNAKEBULLET = "ZW";
    public static final String ENEMYBITE = "AE";
    public static final String ENEMYSLASH = "AF";
    public static final String ENEMYSMASH = "ZX";
    
    public static final String REDFISH = "1"; // Rat
    public static final String CATTREAT = "2"; // Rat
    public static final String MILK = "3"; // Rat
    public static final String PREMIUMCATFOOD = "4";
    public static final String GOLDFISH = "5";
    public static final String LIGHTSCARF = "6";
    public static final String THICKSWEATER = "7";
    public static final String BAGOFCATNIP = "8";
    public static final String LOUDBELL = "9";
    public static final String PRINGLESCAN = "10";
    public static final String MAP_DATA = "M:";       // Full map
    public static final String ROOM = "R:";    // Room 
    public static final String DOOR = "D:"; // Door 
    public static final String ROOM_CHANGE = "RC:";
    public static final String LEVEL_CHANGE = "LC:";
    public static final String BOSS_KILLED = "BK:";
    public static final String GAME_OVER = "GO:";
    public static final String HP = "*";
    public static final String ATTACK = "ATK:";
    public static final String KEYS = "K:";
    public static final String CLICK = "CLK:";
    public static final String RIGHT_CLICK = "RCLK:";

    public static final String DELIMITER = "|";
    public static final String SUB_DELIMITER = ",";
    public static final String MINIMAP_DELIMITER = ";";

    // Message types
    public static final String MINIMAP_UPDATE = "MINIMAP:";
    public static final String SPAWN_INDICATOR = "1000";
}
