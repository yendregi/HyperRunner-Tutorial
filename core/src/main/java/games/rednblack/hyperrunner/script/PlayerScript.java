package games.rednblack.hyperrunner.script;

import static games.rednblack.hyperrunner.script.ScriptGlobals.LEFT;
import static games.rednblack.hyperrunner.script.ScriptGlobals.PLAYER;
import static games.rednblack.hyperrunner.script.ScriptGlobals.RIGHT;
import static games.rednblack.hyperrunner.script.ScriptGlobals.UP;
import static games.rednblack.hyperrunner.script.ScriptGlobals.DOWN;
import static games.rednblack.hyperrunner.script.ScriptGlobals.JUMP;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.viewport.Viewport;

import games.rednblack.editor.renderer.SceneLoader;
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

public class PlayerScript extends BasicScript implements PhysicsContact {

    protected com.artemis.World mEngine;
    protected ComponentMapper<PhysicsBodyComponent> physicsMapper;
    protected ComponentMapper<TransformComponent> transformMapper;
    protected ComponentMapper<PlayerComponent> playerMapper;
    protected ComponentMapper<MainItemComponent> mainItemMapper;
    protected ComponentMapper<DiamondComponent> diamondMapper;
    protected ComponentMapper<DimensionsComponent> dimensionsMapper;

    private int animEntity;
    private PhysicsBodyComponent mPhysicsBodyComponent;

    private final Vector2 impulse = new Vector2(0, 0);
    private final Vector2 speed = new Vector2(0, 0);


    private int lastPlayerFacingDirection=RIGHT; //by default we always face left
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
           // movePlayer(JUMP);
            playerShoot();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            playerShoot();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            System.exit(0);
        }
    }

    public void playerShoot() {
        if(HyperRunner.mSceneLoader!=null) {
            //load a bullet from the library
            String bulletElementName = "bullet_2";
            CompositeItemVO bulletData = HyperRunner.mSceneLoader.loadVoFromLibrary(bulletElementName);
            if (bulletData != null) {
                //set layer & create unique name and identifier
                bulletData.layerName = "Default";
                incG++;
                bulletData.itemName = "bullet_"+incG;
                bulletData.itemIdentifier = "bullet_id_"+incG;

                //grab player body and get world coordinates
                Body body = mPhysicsBodyComponent.body;
                Vector2 bodyCenter = body.getWorldCenter();

                //figure out direction and offset position
                bulletData.flipX = (this.lastPlayerFacingDirection != RIGHT);
                bulletData.x = bodyCenter.x+((this.lastPlayerFacingDirection==RIGHT)? 0.59f : -0.59f);
                bulletData.y = bodyCenter.y+0.3f;

                //create the entity & init
                ItemWrapper root = new ItemWrapper(HyperRunner.mSceneLoader.getRoot(), HyperRunner.mSceneLoader.getEngine());
                int bullet = HyperRunner.mSceneLoader.getEntityFactory().createEntity(root.getEntity(), bulletData);
                HyperRunner.mSceneLoader.getEntityFactory().initAllChildren(bullet, bulletData);
                HyperRunner.mSceneLoader.addComponentByTagName("bullet", BulletComponent.class); //add the bullet component to the created entity

                //create the bullet script and set some required elements
                BulletScript bulletScript = new BulletScript();
                bulletScript.setBulletDirection(this.lastPlayerFacingDirection);
                bulletScript.setFiredBy(PLAYER);

                //have to recreate the root element otherwise we don't find the bullet we just created? why is this?
                root = new ItemWrapper(HyperRunner.mSceneLoader.getRoot(), HyperRunner.mSceneLoader.getEngine());

                ItemWrapper bulletItem = root.getChild(bulletData.itemIdentifier);
                ComponentRetriever.create(bulletItem.getChild("bullet-ani").getEntity(), BulletComponent.class, HyperRunner.mSceneLoader.getEngine());
                bulletItem.addScript(bulletScript);

            }else{
                System.err.println("No '"+bulletElementName+"' composite found in library!");
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
                impulse.set(speed.x, 5);
                break;
            case DOWN:
                impulse.set(speed.x, -5);
                break;
            case JUMP:
                TransformComponent transformComponent = transformMapper.get(entity);
                //impulse.set(speed.x, transformComponent.y < 6 ? 5 : speed.y);
                impulse.set(speed.x, 5);
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
            mEngine.delete(contactEntity);

        }

    }

    @Override
    public void endContact(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {
        MainItemComponent mainItemComponent = mainItemMapper.get(contactEntity);

        PlayerComponent playerComponent = playerMapper.get(animEntity);

        if (mainItemComponent.tags.contains("platform"))
            playerComponent.touchedPlatforms--;
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

}
