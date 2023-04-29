package games.rednblack.hyperrunner.script;

import static games.rednblack.hyperrunner.script.ScriptGlobals.ALIEN;
import static games.rednblack.hyperrunner.script.ScriptGlobals.PLAYER;
import static games.rednblack.hyperrunner.script.ScriptGlobals.RIGHT;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.ScriptComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.physics.PhysicsContact;
import games.rednblack.editor.renderer.scripts.BasicScript;
import games.rednblack.editor.renderer.utils.ItemWrapper;
import games.rednblack.hyperrunner.HyperRunner;
import games.rednblack.hyperrunner.component.AlienComponent;
import games.rednblack.hyperrunner.component.BulletComponent;
import games.rednblack.hyperrunner.component.PlayerComponent;

/**
 * bullet script that allows game entities to shoot a projectile
 * @author Jédregi
 */
public class BulletScript extends BasicScript implements PhysicsContact {

    protected com.artemis.World mEngine;
    protected ComponentMapper<PhysicsBodyComponent> physicsMapper;
    protected ComponentMapper<MainItemComponent> mainItemMapper;
    protected ComponentMapper<BulletComponent> bulletMapper;
    protected ComponentMapper<AlienComponent> alienMapper;
    protected ComponentMapper<ScriptComponent> scriptMapper;

    protected int animEntity;

    private PhysicsBodyComponent mPhysicsBodyComponent;

    private final float maxSpeed = 5.1f;

    private int bulletDirection=-1;
    private int firedByEntity;

    @Override
    public void init(int item) {
        super.init(item);

        ItemWrapper itemWrapper = new ItemWrapper(item, mEngine);
        animEntity = itemWrapper.getChild("bullet-ani").getEntity();
        mPhysicsBodyComponent = physicsMapper.get(item);

        // probably a bad place to put this, alas, this represents the event every time an alien fires a bullet
        HyperRunner.soundManager.play("fire bullet");

    }

    @Override
    public void act(float delta) {
       moveBullet();
    }

    public void moveBullet() {

        Body body = mPhysicsBodyComponent.body;

        if (body == null)
            return;

        body.setLinearVelocity(((this.bulletDirection == RIGHT) ? maxSpeed : -1*maxSpeed), 0);

    }

    public BulletComponent getBulletComponent() {
        return bulletMapper.get(animEntity);
    }

    @Override
    public void dispose() { }

    @Override
    public void beginContact(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {

        MainItemComponent mainItemComponent = mainItemMapper.get(contactEntity);
        BulletComponent bulletComponent = bulletMapper.get(animEntity);

        if (mainItemComponent.tags.contains("platform"))
            bulletComponent.touchedPlatforms++;

    }

    @Override
    public void endContact(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {

        MainItemComponent mainItemComponent = mainItemMapper.get(contactEntity);
        BulletComponent bulletComponent = bulletMapper.get(animEntity);

        if (mainItemComponent.tags.contains("platform"))
            bulletComponent.touchedPlatforms--;

    }

    @Override
    public void preSolve(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {

    }

    @Override
    public void postSolve(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {

        MainItemComponent mainItemComponent = mainItemMapper.get(contactEntity);

        // if fired by alien, can only affect player, -> set player dead (could do hit points)
        // and vice versa, if fired by player, can only affect aliens, when alien hit, it dies (could do hit points)

        // general rule still applies that if platforms are hit, bullet is gone
        // if bullet hits another bullet, bullets cancel each other

        // what happens if fired by alien and hits alien ? -> for the moment, we just delete the bullet -> however, we could implement collision filtering to make alien fired bullets pass through other aliens (something like ? https://aurelienribon.wordpress.com/2011/07/01/box2d-tutorial-collision-filtering/)

        if(this.firedByEntity == ALIEN && mainItemComponent.tags.contains("player")) { //if hit obj is player, indicate he's dead
            ScriptComponent playerScript = scriptMapper.get(contactEntity);
            PlayerComponent playerComponent = ((PlayerScript)playerScript.scripts.get(0)).getPlayerComponent();
            playerComponent.isDead = true;
        }

        if(this.firedByEntity == ALIEN && mainItemComponent.tags.contains("alien")) {
            mEngine.delete(this.entity);  // delete this bullet on touch, aliens can't kill each other, they "absorb the bullets" (imagine were bullets take away player life, but heal alien)
        }

        if(this.firedByEntity == PLAYER && mainItemComponent.tags.contains("alien")) {
            mEngine.delete(this.entity);  // delete this bullet on touch
            ScriptComponent alienScript = scriptMapper.get(contactEntity);
            AlienComponent alienComponent = alienMapper.get(((AlienScript)alienScript.scripts.get(0)).getAlienAnimEntity());
            alienComponent.isDead = true; // indicate alien is dead
        }

        if (mainItemComponent.tags.contains("bullet")) {
            mEngine.delete(this.entity); // delete this bullet on touch
            mEngine.delete(contactEntity); // delete touched bullet on touch
        }

        if (mainItemComponent.tags.contains("platform"))
            mEngine.delete(this.entity); // delete this bullet on touch

    }

    public void setBulletDirection(int direction){
        this.bulletDirection = direction;
    }


    public void setFiredBy(int firedByEntity) {
        this.firedByEntity = firedByEntity;
    }
}
