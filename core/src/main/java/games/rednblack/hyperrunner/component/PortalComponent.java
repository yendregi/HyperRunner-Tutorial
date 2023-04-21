package games.rednblack.hyperrunner.component;

import com.artemis.PooledComponent;

public class PortalComponent extends PooledComponent {

    public boolean playerTouched = false;

    @Override
    public void reset() {
        playerTouched = false;
    }

}
