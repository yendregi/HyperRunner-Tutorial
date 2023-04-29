package games.rednblack.hyperrunner.component;

import com.artemis.PooledComponent;

/**
 * the bullet component
 * @author Jédregi
 */
public class BulletComponent extends PooledComponent {

    public boolean touched = false;
    public int touchedPlatforms = 0;

    @Override
    public void reset() {
        touched = false;
        touchedPlatforms = 0;
    }
}



