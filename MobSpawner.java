import java.util.ArrayList;
import java.util.concurrent.*;

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
    }

    public void spawn() {

        Runnable enemySpawnThread = new Runnable() {
            @Override
            public void run() {
                try {
                    // Stop spawning if maxSpawned has been reached.
                    if (spawnedCount >= maxSpawned) return;

                    Enemy enemy = null;
                    if (inBossRoom && spawnedCount == 0) {
                        enemy = createBoss(level);
                        boss = enemy;
                        spawnEnemy(enemy);
                        for (int i = 0; i < 0; i++) {
                            enemy = createNormalEnemy(level);
                            spawnEnemy(enemy);
                        }
                    } else {
                        // Pick a random enemy to spawn out of the available in the list for the level
                        enemy = createNormalEnemy(level);
                        spawnEnemy(enemy);
                    }
                } catch (Exception e) {
                    System.out.println("Exception in spawn() method:" + e);
                }
            }            
        };
        spawnMobsScheduler.scheduleAtFixedRate(enemySpawnThread, INITIALSPAWNDELAY, spawnRate, TimeUnit.SECONDS);
    }

    public void stopSpawn() {
        try {
            spawnMobsScheduler.shutdownNow();    
        } catch (Exception e) {
            System.out.println("Exception in stop() method of MobSpawner");
        }
        
    }

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

    public boolean isSpawning() {
        return isSpawning;
    }
    
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
                System.out.println("Undetected enemy " + name );
                return new Rat(x, y);
        }
    }

    public boolean isInBossRoom() {
        return inBossRoom;
    }

    public void setInBossRoom(boolean inBossRoom) {
        this.inBossRoom = inBossRoom;
    }

    private int[] getRandomTileCoordinates () {
        spawnX =  parentRoom.getWorldX() + ((LOWESTX + (int) (Math.random() * ((HIGHESTX - LOWESTX) + 1))) * GameCanvas.TILESIZE);
        spawnY = parentRoom.getWorldY() + ((LOWESTY + (int) (Math.random() * ((HIGHESTY - LOWESTY) + 1))) * GameCanvas.TILESIZE);
        
        return new int[] {spawnX, spawnY};
    }

    private Enemy createBoss(int level){
        spawnX = parentRoom.getCenterX();
        spawnY = parentRoom.getCenterY();

        String bossType = bosses[level];
        return createEnemy(bossType, spawnX, spawnY);
    }

    public Enemy createNormalEnemy(int level){
        int[] spawnCoors = getRandomTileCoordinates();
        spawnX = spawnCoors[0];
        spawnY = spawnCoors[1];

        String toSpawn = spawnableEnemiesAtLevel[level][(int) (Math.random() * (spawnableEnemiesAtLevel[level].length))];
        return createEnemy(toSpawn, spawnX, spawnY);
    }

    public void spawnEnemy(Enemy enemy) {
        spawnedCount++;   
        isSpawning = true;
        enemy.setCurrentRoom(parentRoom);
        enemy.matchHitBoxBounds();
        spawnedEnemies.add(enemy);
        ServerMaster.getInstance().addEntity(enemy);
    }

    public Room getParentRoom() {
        return parentRoom;
    }

    public void setParentRoom(Room parentRoom) {
        this.parentRoom = parentRoom;
    }

    public boolean isBossKilled(){
        if (boss == null) return false;
        return boss.getHitPoints() <= 0;
    }

}
