package games.rednblack.hyperrunner.component;

import com.artemis.PooledComponent;

/**
 * the diamond component
 * @author fgnm
 */
public class DiamondComponent extends PooledComponent {

    public int value = 1;

    @Override
    public void reset() {
        value = 1;
    }
}
