package games.rednblack.hyperrunner.component;

import com.artemis.PooledComponent;

/**
 * the portal component
 * @author Jédregi
 */
public class PortalComponent extends PooledComponent {

    public boolean playerTouched = false;

    @Override
    public void reset() {
        playerTouched = false;
    }

}
