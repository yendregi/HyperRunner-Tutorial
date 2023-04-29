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
    public SoundManager() {
        init();
    }

    // init
    public void init(){

        soundsHash = new HashMap<String,Sound>();
        String sounds[][] = {
                                {"Stage 1 Music","audio/Sample game music 1.ogg"}, //could be loaded as "Music", for the moment we just use the "Sound" class for everything
                                {"player dies","audio/Player death.ogg"},
                                {"fire bullet","audio/fire_bullet.ogg"},
                                {"alien death","audio/alien_death.ogg"},
                                {"player win 1","audio/Woohoo.ogg"}
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
