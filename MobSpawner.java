import java.util.ArrayList;
import java.util.concurrent.*;

/**     
        The MobSpawner class handles enemy spawning within rooms.
        It controls enemy spawn rates, spawn locations, and spawn
        quantities based on the current game level and difficulty.
        Normal enemies are spawned randomly inside the room. In boss
        rooms, the boss is spawned at the center.

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

public class MobSpawner {
    
    private int spawnX, spawnY; // Where the mob will spawn in the room, x = (5 => 40), y = (5 => 28) 
    private final int level;
    private final int difficulty;
    private boolean inBossRoom;
    private int spawnRate; 
    private int spawnedCount;
    private int maxSpawned;
    private Room parentRoom;
    private ArrayList<Enemy> spawnedEnemies; 
    private boolean isSpawning;
    private Enemy boss;

    private static final int HIGHESTX = 40;
    private static final int LOWESTX = 5;
    private static final int HIGHESTY = 28;
    private static final int LOWESTY = 5;
    private static final int INITIALSPAWNDELAY = 1;
    private static final int SPAWN_INDICATOR_DELAY = 2000; // 2 seconds before spawn
    
    private static final String[][] spawnableEnemiesAtLevel = {
        {"Spider", "Cockroach"},
        {"Rat", "SmallDog"},
        {"Bunny", "Frog"},
        {"Bee", "Snakelet"},
        {"CleaningBot", "SecurityBot"},
        {"FeralRat", "ScreamerRat", "Rat"},
        {"MutatedAnchovy", "MutatedPufferfish", "MutatedArcherfish"}
    };

    private static final String[] bosses = {
        "RatKing",
        "FeralDog",
        "Turtle",
        "Snake",
        "AdultCat",
        "ConjoinedRats",
        "FishMonster"
    };

    private ScheduledExecutorService spawnMobsScheduler;

    /**
     * Creates a new MobSpawner with specified level and difficulty
     * @param level the room level where the MobSpawner is located
     * @param difficulty the difficulty of the room where MobSpawner is located
     */
    public MobSpawner(int level, int difficulty){
        this.level = level;
        this.difficulty = difficulty;
        spawnedCount = 0;
        spawnRate =  2; // Spawns one enemy per spawnRate seconds
        spawnMobsScheduler = Executors.newSingleThreadScheduledExecutor();
        
        // spawnRate = Math.max(2, 5 - (level / 2 - difficulty));
        maxSpawned = (difficulty == 3) ? 1 : (3 + level);
        spawnedEnemies = new ArrayList<>();
        // spawnedCount = 0;
        isSpawning = false;
        boss = null;
    }

    /**
     * Starts a scheduled task that spawns enemies at spawnRate intervals
     * until the maximum number of spawnable enemies at that room has been reached.
     */
    public void spawn() {
        Runnable enemySpawnThread = new Runnable() {
            @Override
            public void run() {
                try {
                    // Stop spawning if maxSpawned has been reached.
                    if (spawnedCount >= maxSpawned) return;

                    if (inBossRoom && spawnedCount == 0 && boss == null) {
                        boss = createBoss(level);
                        // Create spawn indicator for boss
                        SpawnIndicator indicator = new SpawnIndicator(spawnX, spawnY);
                        ServerMaster.getInstance().addEntity(indicator);
                        // Schedule boss spawn after indicator duration
                        ScheduledExecutorService bossSpawnScheduler = Executors.newSingleThreadScheduledExecutor();
                        bossSpawnScheduler.schedule(() -> {
                            spawnEnemy(boss);
                            bossSpawnScheduler.shutdown();
                        }, SPAWN_INDICATOR_DELAY, TimeUnit.MILLISECONDS);
                    } else {
                        // Pick a random enemy to spawn out of the available in the list for the level
                        final Enemy enemyToSpawn = createNormalEnemy(level);
                        // Create spawn indicator for normal enemy
                        SpawnIndicator indicator = new SpawnIndicator(spawnX, spawnY);
                        ServerMaster.getInstance().addEntity(indicator);
                        // Schedule enemy spawn after indicator duration
                        ScheduledExecutorService enemySpawnScheduler = Executors.newSingleThreadScheduledExecutor();
                        enemySpawnScheduler.schedule(() -> {
                            spawnEnemy(enemyToSpawn);
                            enemySpawnScheduler.shutdown();
                        }, SPAWN_INDICATOR_DELAY, TimeUnit.MILLISECONDS);
                    }
                } catch (Exception e) {
                    System.out.println("Exception in spawn() method:" + e);
                }
            }            
        };
        spawnMobsScheduler.scheduleAtFixedRate(enemySpawnThread, INITIALSPAWNDELAY, spawnRate, TimeUnit.SECONDS);
    }

    /**
     * Stops the scheduler responsible for spawning enemies
     */
    public void stopSpawn() {
        try {
            spawnMobsScheduler.shutdownNow();    
        } catch (Exception e) {
            System.out.println("Exception in stop() method of MobSpawner");
        }
        
    }

    /**
     * Gets the number of enemies who have died after spawning
     * @return the int number of enemies spawned who died
     */
    public int getKilledCount(){
        int killed = 0;

        for (Enemy e : spawnedEnemies) {
            if (e.isDead()) killed++;    
        }
        return killed;
    }

    /**
     * Checks if all the enemies spawned have been killed.
     * @return true if all the enemies spawned have been killed, false otherwise.
     */
    public boolean isAllKilled(){
        return (getKilledCount() >= maxSpawned);
    }

    /**
     * Gets the value of the isSpawning field
     * @return true if isSpawning, false otherwise
     */
    public boolean isSpawning() {
        return isSpawning;
    }
    
    /**
     * Creates an instance of the enemy corresponding to the provided name
     * @param name the name of the enemy to return
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return a new instance of the enemy corresponding to the passed name
     */
    private Enemy createEnemy(String name, int x, int y) {
        switch (name) {
            case "AdultCat":
                return new AdultCat(x, y);
            case "Bee":
                return new Bee(x, y);
            case "Bunny":
                return new Bunny(x, y);
            case "CleaningBot":
                return new CleaningRobot(x, y);
            case "Cockroach":
                return new Cockroach(x, y);
                case "ConjoinedRats":
                return new ConjoinedRats(x, y);
            case "FishMonster":
                return new FishMonster(x, y);
            case "FeralDog":
                return new FeralDog(x, y);
            case "FeralRat":
                return new FeralRat(x, y);
            case "Frog":
                return new Frog(x, y);
            case "MutatedAnchovy":
                return new MutatedAnchovy(x, y);
            case "MutatedArcherfish":
                return new MutatedArcherfish(x, y);
            case "MutatedPufferfish":
                return new MutatedPufferfish(x, y);
            case "Rat":
                return new Rat(x, y);
            case "RatKing":
                return new RatKing(x, y);
            case "ScreamerRat":
                return new ScreamerRat(x, y);
            case "SecurityBot":
                return new SecurityBot(x, y);
            case "SmallDog":
                return new SmallDog(x, y);
            case "Snakelet":
                return new Snakelet(x, y);
            case "Snake":
                return new Snake(x, y);
            case "Spider":
                return new Spider(x, y);
            case "Turtle":
                return new Turtle(x, y);
            default:
                // System.out.println("Undetected enemy " + name );
                return new Rat(x, y);
        }
    }

    /**
     * Gets the value of the inBossRoom field
     * @return true if inBossRoom is true, false otherwise
     */
    public boolean isInBossRoom() {
        return inBossRoom;
    }

    /**
     * Sets the value of inBossRoom to the passed argument
     * @param inBossRoom the boolean value to set inBossRoom to
     */
    public void setInBossRoom(boolean inBossRoom) {
        this.inBossRoom = inBossRoom;
    }

    /**
     * Uses the static LOWESTX, LOWESTY, HIGHESTX, AND HIGHESTY values to generate random
     * tile coordinate between the min and max values.
     * @return an int array. Index 0 is the x-coordinate, index 1 is the y-coordinate
     */
    private int[] getRandomTileCoordinates () {
        spawnX =  parentRoom.getWorldX() + ((LOWESTX + (int) (Math.random() * ((HIGHESTX - LOWESTX) + 1))) * GameCanvas.TILESIZE);
        spawnY = parentRoom.getWorldY() + ((LOWESTY + (int) (Math.random() * ((HIGHESTY - LOWESTY) + 1))) * GameCanvas.TILESIZE);
        
        return new int[] {spawnX, spawnY};
    }

    /**
     * Creates a boss based on level and sets its spawn point to the room's center
     * @param level the mobspawner's level
     * @return the created boss instance
     */
    private Enemy createBoss(int level){
        spawnX = parentRoom.getCenterX();
        spawnY = parentRoom.getCenterY();

        String bossType = bosses[level];
        return createEnemy(bossType, spawnX, spawnY);
    }

    /**
     * Creates a non-boss enemy based on the available enemies per level listed in spawnableEnemiesAtLevel
     * @param level the room's level
     * @return the created normal enemy with coordinates generated by getRandomTileCoordinates()
     */
    public Enemy createNormalEnemy(int level){
        int[] spawnCoors = getRandomTileCoordinates();
        spawnX = spawnCoors[0];
        spawnY = spawnCoors[1];

        String toSpawn = spawnableEnemiesAtLevel[level][(int) (Math.random() * (spawnableEnemiesAtLevel[level].length))];
        return createEnemy(toSpawn, spawnX, spawnY);
    }

    /**
     * Spawns an enemy into the game by adding it into the ServerMaster
     * entities ArrayList into the parentRoom
     * @param enemy
     */
    public void spawnEnemy(Enemy enemy) {
        spawnedCount++;   
        isSpawning = true;
        enemy.setCurrentRoom(parentRoom);
        enemy.matchHitBoxBounds();
        spawnedEnemies.add(enemy);
        ServerMaster.getInstance().addEntity(enemy);
    }

    /**
     * Gets the Room parentRoom where the mobSpawner is located
     * @return the Room reference held in parentRoom
     */
    public Room getParentRoom() {
        return parentRoom;
    }

    /**
     * Sets the value of parentRoom to the passed Room argument
     * @param parentRoom the Room to set parentRoom to
     */
    public void setParentRoom(Room parentRoom) {
        this.parentRoom = parentRoom;
    }

    /**
     * Checks if the boss created by the MobSpawner instance is already killed
     * @return true if the boss is killed, false otherwise
     */
    public boolean isBossKilled(){
        if (boss == null) return false;
        return boss.getHitPoints() <= 0;
    }

    public void setBoss(Enemy boss){
        this.boss = boss;
    }
}
