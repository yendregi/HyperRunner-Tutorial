package games.rednblack.hyperrunner.script;

import static games.rednblack.hyperrunner.script.ScriptGlobals.LEFT;
import static games.rednblack.hyperrunner.script.ScriptGlobals.PLAYER;
import static games.rednblack.hyperrunner.script.ScriptGlobals.RIGHT;
import static games.rednblack.hyperrunner.script.ScriptGlobals.UP;
import static games.rednblack.hyperrunner.script.ScriptGlobals.DOWN;
import static games.rednblack.hyperrunner.script.ScriptGlobals.bulletElementName;
import static games.rednblack.hyperrunner.script.ScriptGlobals.bulletOffset;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.physics.PhysicsContact;
import games.rednblack.editor.renderer.scripts.BasicScript;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.ItemWrapper;
import games.rednblack.hyperrunner.HyperRunner;
import games.rednblack.hyperrunner.component.BulletComponent;
import games.rednblack.hyperrunner.component.DiamondComponent;
import games.rednblack.hyperrunner.component.PlayerComponent;
import games.rednblack.hyperrunner.component.PortalComponent;

/**
 * the player script
 * @author fgnm & adapation by Jédregi
 */
public class PlayerScript extends BasicScript implements PhysicsContact {

    protected com.artemis.World mEngine;
    protected ComponentMapper<PhysicsBodyComponent> physicsMapper;
    protected ComponentMapper<TransformComponent> transformMapper;
    protected ComponentMapper<PlayerComponent> playerMapper;
    protected ComponentMapper<MainItemComponent> mainItemMapper;
    protected ComponentMapper<DiamondComponent> diamondMapper;
    protected ComponentMapper<DimensionsComponent> dimensionsMapper;
    protected ComponentMapper<PortalComponent> portalMapper;
    private int animEntity;
    private PhysicsBodyComponent mPhysicsBodyComponent;

    private final Vector2 impulse = new Vector2(0, 0);
    private final Vector2 speed = new Vector2(0, 0);

    private int lastPlayerFacingDirection = RIGHT; //by default we always face left
    private int incG = 0;

    @Override
    public void init(int item) {
        super.init(item);

        ItemWrapper itemWrapper = new ItemWrapper(item, mEngine);
        animEntity = itemWrapper.getChild("player-anim").getEntity();

        mPhysicsBodyComponent = physicsMapper.get(item);
    }

    @Override
    public void act(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            movePlayer(LEFT);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            movePlayer(RIGHT);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            movePlayer(UP);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            movePlayer(DOWN);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            playerShoot();
        }

    }

    public void playerShoot() {
        if (HyperRunner.mSceneLoader != null) {
            //load a bullet from the library
            CompositeItemVO bulletData = HyperRunner.mSceneLoader.loadVoFromLibrary(bulletElementName);
            if (bulletData != null) {
                //set layer & create unique name and identifier
                bulletData.layerName = "Default";
                incG++;
                bulletData.itemName = "bullet_" + incG;
                bulletData.itemIdentifier = "bullet_id_" + incG;

                //grab player body and get world coordinates
                Body body = mPhysicsBodyComponent.body;
                Vector2 bodyCenter = body.getWorldCenter();

                //figure out direction and offset position
                bulletData.flipX = (this.lastPlayerFacingDirection != RIGHT);
                bulletData.x = bodyCenter.x + ((this.lastPlayerFacingDirection == RIGHT) ? bulletOffset : -1 * bulletOffset);
                bulletData.y = bodyCenter.y + 0.3f;

                //create the entity & init
                int bullet = HyperRunner.mSceneLoader.getEntityFactory().createEntity(HyperRunner.mSceneLoader.getRoot(), bulletData);
                HyperRunner.mSceneLoader.getEntityFactory().initAllChildren(bullet, bulletData);

                //create the bullet script and set some required elements
                BulletScript bulletScript = new BulletScript();
                bulletScript.setBulletDirection(this.lastPlayerFacingDirection);
                bulletScript.setFiredBy(PLAYER);

                //create the root element to find the bullet created
                ItemWrapper root = new ItemWrapper(HyperRunner.mSceneLoader.getRoot(), mEngine);

                ItemWrapper bulletItem = root.getChild(bulletData.itemIdentifier);
                ComponentRetriever.create(bulletItem.getChild("bullet-ani").getEntity(), BulletComponent.class, mEngine);
                bulletItem.addScript(bulletScript);

                //System.out.println("bulletItem.getEntity()="+bulletItem.getEntity()+ "   bulletItem.getChild(\"bullet-ani\").getEntity()=="+ bulletItem.getChild("bullet-ani").getEntity());

            } else {
                System.err.println("No '" + bulletElementName + "' composite found in library!");
            }
        }

    }

    public void movePlayer(int direction) {
        Body body = mPhysicsBodyComponent.body;

        speed.set(body.getLinearVelocity());

        switch (direction) {
            case LEFT:
                impulse.set(-5, speed.y);
                this.lastPlayerFacingDirection = LEFT;
                break;
            case RIGHT:
                impulse.set(5, speed.y);
                this.lastPlayerFacingDirection = RIGHT;
                break;
            case UP:
                TransformComponent transformComponent = transformMapper.get(entity);
                //impulse.set(speed.x, 5);
                impulse.set(speed.x, transformComponent.y < 6 ? 5 : speed.y); //limit how high a player can go
                break;
            case DOWN:
                impulse.set(speed.x, -5);
                break;
        }
        body.applyLinearImpulse(impulse.sub(speed), body.getWorldCenter(), true);
    }

    public PlayerComponent getPlayerComponent() {
        return playerMapper.get(animEntity);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void beginContact(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {
        MainItemComponent mainItemComponent = mainItemMapper.get(contactEntity);

        PlayerComponent playerComponent = playerMapper.get(animEntity);

        if (mainItemComponent.tags.contains("platform"))
            playerComponent.touchedPlatforms++;

        DiamondComponent diamondComponent = diamondMapper.get(contactEntity);
        if (diamondComponent != null) {
            playerComponent.diamondsCollected += diamondComponent.value;
            diamondComponent.value = 0;
            mEngine.delete(contactEntity);
        }

    }

    @Override
    public void endContact(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {
        MainItemComponent mainItemComponent = mainItemMapper.get(contactEntity);

        PlayerComponent playerComponent = playerMapper.get(animEntity);

        if (mainItemComponent.tags.contains("platform"))
            playerComponent.touchedPlatforms--;

        if (mainItemComponent.tags.contains("portal_1")) {
            playerComponent.level1Done = true;
            HyperRunner.soundManager.play("player win 1");
        }

    }

    @Override
    public void preSolve(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {
        TransformComponent transformComponent = transformMapper.get(this.entity);

        TransformComponent colliderTransform = transformMapper.get(contactEntity);
        DimensionsComponent colliderDimension = dimensionsMapper.get(contactEntity);

        if (transformComponent.y < colliderTransform.y + colliderDimension.height) {
            contact.setFriction(0);
        } else {
            contact.setFriction(1);
        }

    }

    @Override
    public void postSolve(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {

    }

    public int getPlayerAnimEntity() {
        return animEntity;
    }

}
