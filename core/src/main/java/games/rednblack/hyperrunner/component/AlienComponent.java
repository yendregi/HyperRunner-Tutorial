package games.rednblack.hyperrunner.component;

import com.artemis.PooledComponent;

public class AlienComponent extends PooledComponent {

        public boolean isDead = false;
        public long deathTime = 0;
        public long deathPlayTime = 425;

        @Override
        public void reset() {
                isDead = false;
                deathTime = 0;
                deathPlayTime = 700;
        }

}
