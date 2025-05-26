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
    masterVolume = 1.0f;
    musicVolume = 0.7f;
    sfxVolume = 1.0f;
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
            clip.setFramePosition(0);
            FloatControl gainControl = 
                (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float volume = (float) (Math.log(masterVolume * sfxVolume) / Math.log(10.0) * 20.0);
            gainControl.setValue(volume);
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
    stopMusic();
    Clip clip = musicClips.get(name);
    if (clip != null) {
      FloatControl gainControl = 
        (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
      float volume = (float) (Math.log(masterVolume * musicVolume) / Math.log(10.0) * 20.0);
      gainControl.setValue(volume);
      clip.loop(Clip.LOOP_CONTINUOUSLY);
    }
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
                    clip.setFramePosition(0);
                    FloatControl gainControl = 
                    (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float volume = (float) (Math.log(masterVolume * sfxVolume) / Math.log(10.0) * 20.0);
                    gainControl.setValue(volume);
                    clip.start();
                    break;
                }
            }
        }
    };
    audioExecutor.execute(playSoundTask);
  }

  public void setUpAudio(){
    initializeSoundPool("playerSlash.wav", 10);
  }

  public void shutdown() {
    audioExecutor.shutdown();
  }
}
