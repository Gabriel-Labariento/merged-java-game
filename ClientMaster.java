import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientMaster {
    private Player userPlayer;
    private HashMap <Integer, Room> allRooms;
    private Room currentRoom;
    private CopyOnWriteArrayList<Entity> entities; 
    private int xpBarPercent;
    private int userLvl;
    private String heldItemIdentifier;
    private boolean isGameOver;
    private int bossHPPercent;
    
    public ClientMaster(){
        userPlayer = null;
        allRooms = null;
        currentRoom = null;
        entities = new CopyOnWriteArrayList<>();
    }

    public void setXPBarPercent(int percent){
        xpBarPercent = percent;
    }

    public int getXPBarPercent(){
        return xpBarPercent;
    }

    public int getBossHPPercent(){
        return bossHPPercent;
    }

    public void setBossHPBarPercemt(int i){
        bossHPPercent = i;
    }

    public void setUserLvl(int lvl){
        userLvl = lvl;
    }

    public int getUserLvl(){
        return userLvl;
    }

    public HashMap<Integer, Room> getAllRooms() {
        return allRooms;
    }

    public void setAllRooms(HashMap<Integer, Room> allRooms) {
        this.allRooms = allRooms;
    }

    public Player getUserPlayer() {
        return userPlayer;
    }

    public void setUserPlayer(Player userPlayer) {
        this.userPlayer = userPlayer;
    }


    public Entity getEntity(String identifier, int id, int x, int y){
        //Player entities
        if (identifier.equals( NetworkProtocol.HEAVYCAT)) return new HeavyCat(id, x, y);
        else if (identifier.equals( NetworkProtocol.FASTCAT)) return new FastCat(id, x, y);
        else if (identifier.equals( NetworkProtocol.GUNCAT)) return new GunCat(id, x, y);

        //Item entities
        else if (identifier.equals( NetworkProtocol.REDFISH)) return new RedFish(x, y);
        else if (identifier.equals( NetworkProtocol.CATTREAT)) return new CatTreat(x, y);
        else if (identifier.equals( NetworkProtocol.MILK)) return new Milk(x, y);
        else if (identifier.equals( NetworkProtocol.PREMIUMCATFOOD)) return new PremiumCatFood(x, y);
        else if (identifier.equals( NetworkProtocol.GOLDFISH)) return new Goldfish(x, y);
        else if (identifier.equals( NetworkProtocol.LIGHTSCARF)) return new LightScarf(x, y);
        else if (identifier.equals( NetworkProtocol.THICKSWEATER)) return new ThickSweater(x, y);
        else if (identifier.equals( NetworkProtocol.BAGOFCATNIP)) return new BagOfCatnip(x, y);
        else if (identifier.equals( NetworkProtocol.LOUDBELL)) return new LoudBell(x, y);
        else if (identifier.equals( NetworkProtocol.PRINGLESCAN)) return new PringlesCan(x,y);

        //Enemy entities
        //Normal 
        else if (identifier.equals( NetworkProtocol.SPIDER)) return new Spider(x, y);
        else if (identifier.equals( NetworkProtocol.COCKROACH)) return new Cockroach(x, y);
        else if (identifier.equals( NetworkProtocol.RAT)) return new Rat(x, y);
        else if (identifier.equals( NetworkProtocol.SMALLDOG)) return new SmallDog(x, y);
        else if (identifier.equals( NetworkProtocol.BUNNY)) return new Bunny(x, y);
        else if (identifier.equals( NetworkProtocol.FROG)) return new Frog(x,y);
        else if (identifier.equals( NetworkProtocol.BEE)) return new Bee(x, y);
        else if (identifier.equals( NetworkProtocol.SNAKELET)) return new Snakelet(x, y);
        else if (identifier.equals( NetworkProtocol.CLEANINGBOT)) return new CleaningRobot(x, y);
        else if (identifier.equals( NetworkProtocol.SECURITYBOT)) return new SecurityBot(x, y);
        else if (identifier.equals( NetworkProtocol.FERALRAT)) return new FeralRat(x,y);
        else if (identifier.equals( NetworkProtocol.SCREAMERRAT)) return new ScreamerRat(x, y);
        else if (identifier.equals( NetworkProtocol.MUTATEDANCHOVY)) return new MutatedAnchovy(x, y);
        else if (identifier.equals( NetworkProtocol.MUTATEDARCHERFISH)) return new MutatedArcherfish(x, y);
        else if (identifier.equals( NetworkProtocol.MUTATEDPUFFERFISH)) return new MutatedPufferfish(x, y);

        //Bosses
        else if (identifier.equals( NetworkProtocol.RATKING)) return new RatKing(x,y);
        else if (identifier.equals( NetworkProtocol.FERALDOG)) return new FeralDog(x, y);
        else if (identifier.equals( NetworkProtocol.TURTLE)) return new Turtle(x, y);
        else if (identifier.equals( NetworkProtocol.SNAKE)) return new Snake(x, y);
        else if (identifier.equals( NetworkProtocol.ADULTCAT)) return new AdultCat(x,y);
        else if (identifier.equals( NetworkProtocol.CONJOINEDRATS)) return new ConjoinedRats(x, y);
        else if (identifier.equals( NetworkProtocol.FISHMONSTER)) return new FishMonster(x, y);

        //Attack entities
        else if (identifier.equals( NetworkProtocol.PLAYERSMASH)) 
            return new PlayerSmash(id, null, x, y, 0, false);
        else if (identifier.equals( NetworkProtocol.PLAYERSLASH)) 
            return new PlayerSlash(id, null, x, y, 0, false);
        else if (identifier.equals( NetworkProtocol.PLAYERBULLET)) 
            return new PlayerBullet(id, null, x, y, 0, 0, 0, false);
        else if (identifier.equals(NetworkProtocol.SPIDERBULLET))
            return new SpiderBullet(null, x, y, 0, 0);
        else if (identifier.equals(NetworkProtocol.SNAKEBULLET))
            return new SnakeBullet(null, x, y, 0, 0);
        else if (identifier.equals(NetworkProtocol.LASERBULLET))
            return new LaserBullet(null, x, y, 0, 0);
        else if (identifier.equals(NetworkProtocol.ENEMYBITE))
            return new EnemyBite(null, x, y);
        else if (identifier.equals(NetworkProtocol.ENEMYSLASH))
            return new EnemySlash(null, x, y);
        else if (identifier.equals(NetworkProtocol.ENEMYSMASH))
            return new EnemySmash(null, x, y);
        else if (identifier.equals(NetworkProtocol.ENEMYBARK))
            return new EnemyBark(null, x, y);
        else return null;
    }

    public void loadEntity(String identifier, int id, int x, int y, int roomId, int sprite, int zIndex){
        // System.out.println("Loading entity " + identifier + " " + name + "at " + x + ", " + y);
        // if (name == null) System.out.println("Warning: unknown identity identifier " + identifier);
        Entity e = getEntity(identifier, id, x, y);
        if (e != null) {
            e.setId(id);
            e.setCurrSprite(sprite);
            e.matchHitBoxBounds();
            e.setCurrentRoom(getRoomById(roomId));
            e.setzIndex(zIndex);
            entities.add(e);
        }    
    }

    public void setHeldItemIdentifier(String identifier){
        heldItemIdentifier = identifier;
        // System.out.println(heldItemIdentifier);
    }
    
    public Entity generateUIItem(){
        return getEntity(heldItemIdentifier, 0, 0, 0);
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public CopyOnWriteArrayList<Entity> getEntities() {
        return entities;
    }

    public void setEntities(CopyOnWriteArrayList<Entity> entities) {
        this.entities = entities;
    }
    
    public void addEntity(Entity e){
        entities.add(e);
    }

    public Room getRoomById(int id) {
        return allRooms.get(id);
    }

    public void setIsGameOver(boolean b){
        isGameOver = b;
    }

    public boolean getIsGameOver(){
        return isGameOver;
    }
}
