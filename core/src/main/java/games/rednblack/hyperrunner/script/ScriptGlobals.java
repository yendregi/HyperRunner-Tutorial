package games.rednblack.hyperrunner.script;

/**
 * script globals for various static vars
 * @author Jédregi
 */
public class ScriptGlobals {

    // player movement
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int UP = 2;
    public static final int DOWN = 3;

    // entity mapping
    public static final int ALIEN = 5;
    public static final int PLAYER = 6;

    // bullet stuff
    public static final String bulletElementName = "bullet_3";
    public static final float bulletOffset = 0.51f;
    public static final float bulletMaxSpeed = 5.1f;

    // alien stuff
    public static final float alienMaxSpeed = 2.1f;
    public static final float alienTriggerDistance = 5.5f;

}
