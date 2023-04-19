package games.rednblack.hyperrunner.component;

import com.artemis.PooledComponent;

public class PlayerComponent extends PooledComponent {

    public int touchedPlatforms = 0;

    public int diamondsCollected = 0;

    public boolean isDead = false;
    public boolean levelDone = false;
    @Override
    public void reset() {
        touchedPlatforms = 0;
        diamondsCollected = 0;
        isDead = false;
        levelDone = false;
    }
}
