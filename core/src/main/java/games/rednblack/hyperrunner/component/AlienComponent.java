package games.rednblack.hyperrunner.component;

import com.artemis.PooledComponent;

/**
 * the alien component
 * @author Jédregi
 */
public class AlienComponent extends PooledComponent {

        public boolean alienTriggered = false;
        public boolean isDead = false;
        public long deathTime = 0;
        public long deathPlayTime = 425;

        @Override
        public void reset() {
                alienTriggered = false;
                isDead = false;
                deathTime = 0;
                deathPlayTime = 425;
        }

}
