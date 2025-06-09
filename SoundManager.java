import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A singleton class that manages sound and music clips for the application.
 * It maintains separate HashMaps for sound effects and music, along with volume controls.
 * This class provides methods to load, play, and stop sounds and music, as well as manage volume levels.
 */
public class SoundManager {
  private static SoundManager instance;
  private Map<String, Clip> soundClips;
  private Map<String, Clip> musicClips;
  private float masterVolume;
  private float musicVolume;
  private float sfxVolume;
  
  private Map<String, List<Clip>> soundPools;
  
  private final ExecutorService audioExecutor;
  
  /**
   * Private constructor to enforce singleton pattern.
   * Initializes the HashMaps and sets default volume levels.
   */
  private SoundManager() {
    soundClips = new HashMap<>();
    musicClips = new HashMap<>();
    soundPools = new HashMap<>();
    masterVolume = 0.7f;
    musicVolume = 0.5f;
    sfxVolume = 0.5f;
    audioExecutor = Executors.newSingleThreadExecutor();
  }

  public void stopSound(String name){
    Clip sound = soundClips.get(name);
    if (sound == null) sound = musicClips.get(name);
    if (sound == null) sound = soundPools.get(name).get(0);

    if (sound == null) return;

    if (sound.isRunning()) sound.stop();
  }

  /**
   * Returns the singleton instance of SoundManager.
   * If the instance does not exist, it creates a new one.
   *
   * @return the singleton instance of SoundManager
   */
  public static SoundManager getInstance() {
    if (instance == null) {
      instance = new SoundManager();
    }
    return instance;
  }

  /**
   * Loads a sound effect from the specified path and associates it with the given name.
   *
   * @param name the name to associate with the sound effect
   * @param path the path to the sound effect file
   */
  public void loadSound(String name, String path) {
    try {
      AudioInputStream audioIn = AudioSystem.getAudioInputStream(
        getClass().getResource("/resources/Sounds/sfx/" + path));
      Clip clip = AudioSystem.getClip();
      clip.open(audioIn);
      soundClips.put(name, clip);
    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
      System.err.println("Error loading sound: " + name);
      e.printStackTrace();
    }
  }

  /**
   * Loads a music clip from the specified path and associates it with the given name.
   *
   * @param name the name to associate with the music clip
   * @param path the path to the music file
   */
  public void loadMusic(String name, String path) {
    try {
      AudioInputStream audioIn = AudioSystem.getAudioInputStream(
        getClass().getResource("/resources/Sounds/music/" + path));
      Clip clip = AudioSystem.getClip();
      clip.open(audioIn);
      musicClips.put(name, clip);
    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
      System.err.println("Error loading music: " + name);
      e.printStackTrace();
    }
  }

  /**
   * Plays a sound effect associated with the given name.
   *
   * @param name the name of the sound effect to play
   */
  public void playSound(String name) {
    Runnable playSoundTask = () -> {
        Clip clip = soundClips.get(name);
        if (clip != null) {
            clip.setFramePosition(0);       
            clip.start();
        }
    };
    audioExecutor.execute(playSoundTask);
  }

  /**
   * Plays a music clip associated with the given name.
   *
   * @param name the name of the music clip to play
   */
  public void playMusic(String name) {
    Runnable playMusicTask = () -> {
        stopMusic();
        Clip clip = musicClips.get(name);
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    };
    audioExecutor.execute(playMusicTask);
  }

  /**
   * Stops all currently playing music clips.
   */
  public void stopMusic() {
    for (Clip clip : musicClips.values()) {
      if (clip.isRunning()) {
        clip.stop();
      }
    }
  }

  /**
   * Initializes a sound pool for the given sound effect.
   * Used when the sound effect is played multiple times in quick succession.
   *
   * @param name the name of the sound effect
   * @param path the path to the sound effect file
   * @param poolSize the size of the sound pool
   */
  private void initializeSoundPool(String name, String path, int poolSize) {
    List<Clip> pool = new ArrayList<>();
    for (int i = 0; i < poolSize; i++) {
      try {
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(
          getClass().getResource("/resources/Sounds/sfx/" + path));
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        pool.add(clip);
      } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
        System.err.println("Error creating sound pool for: " + name);
        e.printStackTrace();
      }
    }
    soundPools.put(name, pool);
  }

  /**
   * Plays a sound effect from the sound pool associated with the given name.
   *
   * @param name the name of the sound effect to play
   */
  public void playPooledSound(String name) {
    Runnable playSoundTask = () -> {
        List<Clip> pool = soundPools.get(name);
            if (pool != null) {
                for (Clip clip : pool) {
                if (!clip.isRunning()) {
                    clip.setFramePosition(0);
                    clip.start();
                    break;
                }
            }
        }
    };
    audioExecutor.execute(playSoundTask);
  }

  public void setUpAudio(){
    // ATTACKS
    initializeSoundPool("playerSlash.wav", "playerSlash.wav", 5);
    initializeSoundPool("playerBullet.wav", "playerBullet.wav", 5); 
    initializeSoundPool("playerSmash.wav", "playerSmash.wav", 5);   
    initializeSoundPool("spiderBullet.wav", "spiderBullet.wav", 10);
    initializeSoundPool("snakeBullet.wav", "snakeBullet.wav", 10);
    initializeSoundPool("enemyBite.wav", "enemyBite.wav", 10);
    initializeSoundPool("enemySlash.wav", "enemySlash.wav", 10);
    initializeSoundPool("laserBullet.wav", "laserBullet.wav", 30);
    initializeSoundPool("monsterSpawn.wav", "monsterSpawn.wav", 10);


    // SPECIAL EVENTS
    loadSound("gameOver", "gameOver.wav");
    initializeSoundPool("reviving", "reviving.wav", 3);
    initializeSoundPool("reviveSuccess", "reviveSuccess.wav", 3);
    initializeSoundPool("woodWalk", "woodWalk.wav", 5);
    initializeSoundPool("waterWalk", "waterWalk.wav", 5);
    initializeSoundPool("normalWalk", "normalWalk.wav", 5);
    loadMusic("bossMusic", "bossMusic.wav");
    initializeSoundPool("bossDefeat", "bossDefeat.wav", 3);
    initializeSoundPool("levelUp", "levelUp.wav", 3);
    initializeSoundPool("equipItem", "equipItem.wav", 3);
    initializeSoundPool("click", "click.wav", 5);

    // LEVEL-BASED AMBIENT
    loadMusic("level0", "level0.wav");
    loadMusic("level1", "level1.wav");
    loadMusic("level2", "level2.wav");
    loadMusic("level3", "level3.wav");
    loadMusic("level4", "level4.wav");
    loadMusic("level5", "level5.wav");
    loadMusic("level6", "level6.wav");

    // MUSIC BEATS
    loadMusic("preGameMusic", "preGameMusic.wav");
    loadMusic("mainGameBGMusic", "mainGameBGMusic.wav");


    // MONSTER NOISES
    initializeSoundPool("cockroach", "cockroach.wav", 5);
    initializeSoundPool("rat", "rat.wav", 5);
    initializeSoundPool("dogBark", "dogBark.wav", 5);
    initializeSoundPool("rabbitNoise", "rabbitNoise.wav", 5);
    initializeSoundPool("frogCroak", "frogCroak.wav", 5);
    initializeSoundPool("beeBuzz", "beeBuzz.wav", 5);
    initializeSoundPool("enemyDetected", "enemyDetected.wav", 5);
    initializeSoundPool("feralRatNoise", "feralRatNoise.wav", 5);
    initializeSoundPool("screamerRat", "screamerRat.wav", 5);
    initializeSoundPool("mutatedAnchovy", "mutatedAnchovy.wav", 5);
    initializeSoundPool("mutatedArcherfish", "mutatedArcherfish.wav", 5);
    initializeSoundPool("mutatedPufferfish", "mutatedPufferfish.wav", 5);
    initializeSoundPool("ratKingGrowl", "ratKingGrowl.wav", 5);
    initializeSoundPool("buffHowl", "buffHowl.wav", 5);
    initializeSoundPool("dogSnarl", "dogSnarl.wav", 5);
    initializeSoundPool("waterSplash", "waterSplash.wav", 5);
    initializeSoundPool("snakeHiss", "snakeHiss.wav", 5);
    initializeSoundPool("angryCat", "angryCat.wav", 5);
    initializeSoundPool("ratMonsterNoise", "ratMonsterNoise.wav", 5);
    initializeSoundPool("phaseOneNoise", "phaseOneNoise.wav", 5);
    initializeSoundPool("phaseTwoNoise", "phaseTwoNoise.wav", 5);
    initializeSoundPool("phaseThreeNoise", "phaseThreeNoise.wav", 5);

  }

  /**
   * Plays the ambient music for the specified level.
   * @param level the current game level (0-6)
   */
  public void playLevelMusic(int level) {
    String musicName = "level" + level;
    playMusic(musicName);
  }

  /**
   * Stops all currently playing sounds and music, except for the game over sound.
   * This is useful for transitioning between game states or when needing to clear
   * all audio except for critical game events.
   */
  public void stopAllSounds() {
    // Stop all music
    stopMusic();
    
    // Stop all sound effects except game over
    for (Map.Entry<String, Clip> entry : soundClips.entrySet()) {
        if (!entry.getKey().equals("gameOver") && entry.getValue().isRunning()) {
            entry.getValue().stop();
        }
    }
    
    // Stop all pooled sounds
    for (List<Clip> pool : soundPools.values()) {
        for (Clip clip : pool) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
    }
  }

  public void shutdown() {
    audioExecutor.shutdown();
  }

  public void reapplyVolume() {
    for (Clip clip : musicClips.values()) {
        if (clip.isRunning()) {
            applyVolume(clip, masterVolume * musicVolume);
        }
    }
    for (Clip clip : soundClips.values()) {
        if (clip.isRunning()) {
            applyVolume(clip, masterVolume * sfxVolume);
        }
    }
    for (List<Clip> pool : soundPools.values()) {
        for (Clip clip : pool) {
            if (clip.isRunning()) {
                applyVolume(clip, masterVolume * sfxVolume);
            }
        }
    }
}

    private void applyVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            //avoid 0 volume error
            float limitedVolume = Math.max(volume, 0.0001f);

            //convert volume factor into decibels
            float decibels = (float) (Math.log10(limitedVolume) * 20);

            //apply decibel change
            gainControl.setValue(decibels);
        }
    }

    public void setMasterVolume(float f){
    masterVolume = f;
    reapplyVolume();
    }

    public void setMusicVolume(float f){
    musicVolume = f;
    reapplyVolume();
    }

    public void setSfxVolume(float f){
    sfxVolume = f;
    reapplyVolume();
    }

    public float getMasterVolume(){
    return masterVolume;
    }

    public float getMusicVolume(){
    return musicVolume;
    }

    public float getSfxVolume(){
    return sfxVolume;
    }


}
