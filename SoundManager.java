import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.*;

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
    masterVolume = 0.5f;
    musicVolume = 0.5f;
    sfxVolume = 0.5f;
    audioExecutor = Executors.newSingleThreadExecutor();
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
            applyVolume(clip, masterVolume * sfxVolume);
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
          applyVolume(clip, masterVolume * musicVolume);
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
   * @param poolSize the size of the sound pool
   */
  private void initializeSoundPool(String name, int poolSize) {
    List<Clip> pool = new ArrayList<>();
    for (int i = 0; i < poolSize; i++) {
      try {
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(
          getClass().getResource("/resources/Sounds/sfx/" + name));
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
                    applyVolume(clip, masterVolume * sfxVolume);
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
    initializeSoundPool("playerSlash.wav", 5);
    initializeSoundPool("playerBullet.wav", 5); 
    initializeSoundPool("playerSmash.wav", 5);   
    initializeSoundPool("spiderBullet.wav", 10);
    initializeSoundPool("snakeBullet.wav", 10);
    initializeSoundPool("enemyBite.wav", 10);
    initializeSoundPool("enemySlash.wav", 10);
    initializeSoundPool("laserBullet.wav", 30);


    // GAME OVER
    loadSound("gameOver", "gameOver.wav");

    // AMBIENT OR LEVEL-BASED
    loadMusic("level0", "level0.wav");
    loadMusic("level1", "level1.wav");
    loadMusic("level2", "level1.wav");
    loadMusic("level3", "level1.wav");
    loadMusic("level4", "level1.wav");
    loadMusic("level5", "level1.wav");
    loadMusic("level6", "level1.wav");
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
