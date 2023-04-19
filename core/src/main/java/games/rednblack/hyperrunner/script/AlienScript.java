package games.rednblack.hyperrunner.script;

import static games.rednblack.hyperrunner.script.ScriptGlobals.ALIEN;
import static games.rednblack.hyperrunner.script.ScriptGlobals.RIGHT;
import static games.rednblack.hyperrunner.script.ScriptGlobals.LEFT;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.physics.PhysicsContact;
import games.rednblack.editor.renderer.scripts.BasicScript;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.ItemWrapper;
import games.rednblack.hyperrunner.HyperRunner;
import games.rednblack.hyperrunner.component.AlienComponent;
import games.rednblack.hyperrunner.component.BulletComponent;


public class AlienScript extends BasicScript implements PhysicsContact {

    protected com.artemis.World mEngine;
    protected ComponentMapper<PhysicsBodyComponent> physicsMapper;
    protected ComponentMapper<MainItemComponent> mainItemMapper;
    protected ComponentMapper<BulletComponent> bulletMapper;
    protected ComponentMapper<AlienComponent> alienMapper;
    private int animEntity;

    private PhysicsBodyComponent mPhysicsBodyComponent;

    private final float maxSpeed = 2.1f;
    private float yDamp = 1.0f;

    private int playerEntity;

    private boolean alienTriggered = false;
    private int alienDirection;

    private int incG = 0;

    @Override
    public void init(int item) {
        super.init(item);

        ItemWrapper itemWrapper = new ItemWrapper(item, mEngine);

        animEntity = itemWrapper.getChild("bullet-ani").getEntity();

        mPhysicsBodyComponent = physicsMapper.get(item);

    }

    @Override
    public void act(float delta) {
        AlienComponent alienComponent = alienMapper.get(this.entity);
        if(!alienComponent.isDead)
            moveAllien();
    }

    private float lastX = -100.0f;
    private int lastXcc = 0;

    public void moveAllien() {
        //alien starts to react only when player triggers the alien by getting close, then alien sets impulses towards player, and with a 15% chance shoots towards the player

        PhysicsBodyComponent playerPhysicsBodyComponent = physicsMapper.get(playerEntity);
        Body playerBody = playerPhysicsBodyComponent.body;

        Body alienBody = mPhysicsBodyComponent.body;
        if (alienBody == null || playerBody==null)
            return;

        Vector2 alienBody_c = alienBody.getWorldCenter();
        Vector2 playBody_c = playerBody.getWorldCenter();
        float alienTriggerDistance = 4.5f;
        if(!alienTriggered) {
            alienTriggered = (getDistance(playBody_c, alienBody_c) < alienTriggerDistance);
        }
        if(lastX != alienBody_c.x){
            lastX = alienBody_c.x;
        } else {
            lastXcc++;
        }

        if(alienTriggered) {
            Vector2 alienV = alienBody.getLinearVelocity();
            if( Math.abs(alienV.x) < maxSpeed && Math.abs(alienV.y) < maxSpeed/2) {
                if(lastXcc > 10) {
                    yDamp = 1.0f;
                    lastXcc = 0;
                }
                float xS = ((playBody_c.x-alienBody_c.x) > 0 ? maxSpeed : -1*maxSpeed);
                float yS = ((playBody_c.y-alienBody_c.y) > 0 ? maxSpeed : -1*maxSpeed) * yDamp;

                alienBody.applyLinearImpulse(new Vector2(xS,yS), alienBody.getWorldCenter(), true);
            }
            if(Math.random()<0.005)
                alienShoot(((playBody_c.x-alienBody_c.x) > 0 ? RIGHT : LEFT));
        }
    }

    public void alienShoot(int lastPlayerFacingDirection) {
        if(HyperRunner.mSceneLoader!=null) {
            //load a bullet from the library
            String bulletElementName = "bullet_2";
            CompositeItemVO bulletData = HyperRunner.mSceneLoader.loadVoFromLibrary(bulletElementName);
            if (bulletData != null) {
                //set layer & create unique name and identifier
                bulletData.layerName = "Default";
                incG++;
                bulletData.itemName = "alien_bullet_"+incG;
                bulletData.itemIdentifier = "alien_bullet_id_"+incG;

                //grab player body and get world coordinates
                Body body = mPhysicsBodyComponent.body;
                Vector2 bodyCenter = body.getWorldCenter();

                //figure out direction and offset position
                bulletData.flipX = ((lastPlayerFacingDirection==RIGHT)? false : true);
                bulletData.x = bodyCenter.x+((lastPlayerFacingDirection==RIGHT)? 0.59f : -0.59f);
                bulletData.y = bodyCenter.y+0.3f;

                //create the entity & init
                ItemWrapper root = new ItemWrapper(HyperRunner.mSceneLoader.getRoot(), HyperRunner.mSceneLoader.getEngine());
                int bullet = HyperRunner.mSceneLoader.getEntityFactory().createEntity(root.getEntity(), bulletData);
                HyperRunner.mSceneLoader.getEntityFactory().initAllChildren(bullet, bulletData);
                HyperRunner.mSceneLoader.addComponentByTagName("bullet", BulletComponent.class); //add the bullet component to the created entity

                //create the bullet script and set some required elements
                BulletScript bulletScript = new BulletScript();
                bulletScript.setBulletDirection(lastPlayerFacingDirection);
                bulletScript.setFiredBy(ALIEN);

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

    public float getDistance(Vector2 p1, Vector2 p2){
        return (float)Math.sqrt((p2.x-p1.x)*(p2.x-p1.x)+(p2.y-p1.y)*(p2.y-p1.y));
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
        //BulletComponent bulletComponent = bulletMapper.get(animEntity);
        if (mainItemComponent.tags.contains("platform"))
            yDamp = 0.0f;

    }

    @Override
    public void endContact(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {

        MainItemComponent mainItemComponent = mainItemMapper.get(contactEntity);
        //BulletComponent bulletComponent = bulletMapper.get(animEntity);
        if (mainItemComponent.tags.contains("platform"))
            yDamp = 1.0f;

    }

    @Override
    public void preSolve(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {

    }

    @Override
    public void postSolve(int contactEntity, Fixture contactFixture, Fixture ownFixture, Contact contact) {

        //if the contact enemy is the player, set to indicate player is dead
/*
        MainItemComponent mainItemComponent = mainItemMapper.get(contactEntity);
        //BulletComponent bulletComponent = bulletMapper.get(animEntity);
        if (mainItemComponent.tags.contains("platform"))
            mEngine.delete(this.entity); // delete this bullet on touch
*/
    }

    public void setalienDirection(int direction){
        this.alienDirection = direction;
    }

    public void setPlayerEntity(int playerEntity) {
        this.playerEntity = playerEntity;
    }
}
