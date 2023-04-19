package games.rednblack.hyperrunner.script;

import static games.rednblack.hyperrunner.script.ScriptGlobals.ALIEN;
import static games.rednblack.hyperrunner.script.ScriptGlobals.PLAYER;
import static games.rednblack.hyperrunner.script.ScriptGlobals.RIGHT;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.physics.PhysicsContact;
import games.rednblack.editor.renderer.scripts.BasicScript;
import games.rednblack.editor.renderer.utils.ItemWrapper;
import games.rednblack.hyperrunner.component.AlienComponent;
import games.rednblack.hyperrunner.component.BulletComponent;

public class BulletScript extends BasicScript implements PhysicsContact {

    protected com.artemis.World mEngine;
    protected ComponentMapper<PhysicsBodyComponent> physicsMapper;
    protected ComponentMapper<MainItemComponent> mainItemMapper;
    protected ComponentMapper<BulletComponent> bulletMapper;
    protected ComponentMapper<AlienComponent> alienMapper;

    private int animEntity;

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
    public void dispose() {

    }

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

        AlienComponent alienComponent = alienMapper.get(contactEntity);
        //BulletComponent bulletComponent = bulletMapper.get(animEntity);

        // if fired by alien, can only affect player, -> set player dead (could do hit points)
        // and vice versa, if fired by player, can only affect aliens, when alien hit, it dies (could do hit points)

        // general rule still applies that if platforms are hit, bullet is gone
        // if bullet hits another bullet, bullets cancel each other
        if(this.firedByEntity == ALIEN ) { // get the player component and indicate death  |> load dead scene

        }
        if(this.firedByEntity == PLAYER && mainItemComponent.tags.contains("alien")) {
            mEngine.delete(this.entity);  // delete this bullet on touch
            alienComponent.isDead = true; // indicate alien is dead
        }

        if (mainItemComponent.tags.contains("bullet")) {
            mEngine.delete(this.entity); // delete this bullet on touch
            mEngine.delete(contactEntity); // delete this bullet on touch
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
