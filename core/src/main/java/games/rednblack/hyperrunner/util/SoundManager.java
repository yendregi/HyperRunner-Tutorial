package games.rednblack.hyperrunner.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;

/**
 * a basic sound manager
 *
 * @author Jédregi
 */
public class SoundManager {

    HashMap<String,Sound> soundsHash;
    private boolean mute = false;

    // sound stuff
    public static final String bulletSound = "fire bullet";
    public static final String playerWin = "player win 1";
    public static final String playerDies = "player dies";
    public static final String stage_1_music = "Stage 1 Music";
    public static final String alienDeath = "alien death";

    public SoundManager() {
        init();
    }

    // init
    public void init(){

        soundsHash = new HashMap<String,Sound>();
        String sounds[][] = {
                                {stage_1_music,"audio/Sample game music 1.ogg"}, //could be loaded as "Music", for the moment we just use the "Sound" class for everything
                                {playerDies,"audio/Player death.ogg"},
                                {bulletSound,"audio/fire_bullet.ogg"},
                                {alienDeath,"audio/alien_death.ogg"},
                                {playerWin,"audio/Woohoo.ogg"}
                            };
        for(int i=0;i<sounds.length;i++){
            soundsHash.put(sounds[i][0], Gdx.audio.newSound(Gdx.files.internal(sounds[i][1])));
        }
    }

    public void play(String soundName) {
        if(!mute) {
            Sound sound = soundsHash.get(soundName);
            sound.play(1.0f);
        }
    }

    public void playLooping(String soundName) {
        if(!mute) {
            Sound sound = soundsHash.get(soundName);
            sound.loop();
            sound.play(1.0f);
        }
    }

    public void mute() {
        mute = true;
    }

    public void unmute() {
        mute = false;
    }

    public boolean ismuted() {
        return mute;
    }

}
