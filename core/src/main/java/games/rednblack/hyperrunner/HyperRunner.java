package games.rednblack.hyperrunner;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.resources.AsyncResourceManager;
import games.rednblack.editor.renderer.resources.ResourceManagerLoader;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.ItemWrapper;
import games.rednblack.editor.renderer.ExternalTypesConfiguration;


import games.rednblack.hyperrunner.component.AlienComponent;
import games.rednblack.hyperrunner.component.DiamondComponent;
import games.rednblack.hyperrunner.component.PlayerComponent;
import games.rednblack.hyperrunner.component.BulletComponent;
import games.rednblack.hyperrunner.script.AlienScript;
import games.rednblack.hyperrunner.script.PlayerScript;
import games.rednblack.hyperrunner.stage.HUD;
import games.rednblack.hyperrunner.system.AlienAnimationSystem;
import games.rednblack.hyperrunner.system.CameraSystem;
import games.rednblack.hyperrunner.system.PlayerAnimationSystem;

import games.rednblack.h2d.extension.talos.*;


/** AS A STUDENT EXERCISE TRY THE FOLLOWING:
 *  ADD THE ALIEN AND SOME BASIC AI!
 *  ADD AN ADDITIONAL SCREEN TO LOAD ON PLAYER TOUCHING LEVEL EXIT
 *  */


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class HyperRunner extends ApplicationAdapter {

    protected AssetManager mAssetManager;

    public static SceneLoader mSceneLoader;
    protected AsyncResourceManager mAsyncResourceManager;

    public static Viewport mViewport;
    protected OrthographicCamera mCamera;

    public static com.artemis.World mEngine;

    protected HUD mHUD;
    protected ExtendViewport mHUDViewport;

    @Override
    public void create() {
        mAssetManager = new AssetManager();

        //mAssetManager.setLoader(AsyncResourceManager.class, new ResourceManagerLoader(mAssetManager.getFileHandleResolver()));

        //setup the talos extension plug'in.
        ExternalTypesConfiguration externalItemTypes = new ExternalTypesConfiguration();
        externalItemTypes.addExternalItemType(new TalosItemType());

        mAssetManager.setLoader(AsyncResourceManager.class, new ResourceManagerLoader(externalItemTypes, mAssetManager.getFileHandleResolver()));
        mAssetManager.load("project.dt", AsyncResourceManager.class);
        mAssetManager.load("skin/skin.json", Skin.class);
        mAssetManager.finishLoading();

        mAsyncResourceManager = mAssetManager.get("project.dt", AsyncResourceManager.class);
        SceneConfiguration config = new SceneConfiguration();

        //add the talos extension plug'in.
        config.setExternalItemTypes(externalItemTypes);
        config.setResourceRetriever(mAsyncResourceManager);

        //setup camera system
        CameraSystem cameraSystem = new CameraSystem(5, 40, 5, 20);
        config.addSystem(cameraSystem);

        config.addSystem(new PlayerAnimationSystem());
        config.addSystem(new AlienAnimationSystem());

        //Tells Runtime to automatically attach a component to entites loaded with a specific TAG.
        config.addTagTransmuter("diamond", DiamondComponent.class);
        config.addTagTransmuter("bullet", BulletComponent.class);
        config.addTagTransmuter("alien", AlienComponent.class);

        mSceneLoader = new SceneLoader(config);
        mEngine = mSceneLoader.getEngine();

        ComponentRetriever.addMapper(PlayerComponent.class);
        ComponentRetriever.addMapper(DiamondComponent.class);
        ComponentRetriever.addMapper(BulletComponent.class);
        ComponentRetriever.addMapper(AlienComponent.class);

        mCamera = new OrthographicCamera();
        mViewport = new ExtendViewport(15, 8, mCamera);

        mSceneLoader.loadScene("MainScene", mViewport);

        ItemWrapper root = new ItemWrapper(mSceneLoader.getRoot(), mEngine);

        ItemWrapper player = root.getChild("player");

        ComponentRetriever.create(player.getChild("player-anim").getEntity(), PlayerComponent.class, mEngine); //why ?

        PlayerScript playerScript = new PlayerScript();
        player.addScript(playerScript);
        cameraSystem.setFocus(player.getEntity());

        //dynamically create some aliens:
        createAliens(player.getEntity());

        mSceneLoader.addComponentByTagName("diamond", DiamondComponent.class);

        mHUDViewport = new ExtendViewport(768, 576);
        mHUD = new HUD(mAssetManager.get("skin/skin.json"), mAsyncResourceManager.getTextureAtlas("main"), mHUDViewport, mSceneLoader.getBatch());

        mHUD.setPlayerScript(playerScript);

        InputAdapter webGlfullscreen = new InputAdapter() {
            @Override
            public boolean keyUp (int keycode) {
                if (keycode == Input.Keys.ENTER && Gdx.app.getType() == Application.ApplicationType.WebGL) {
                    if (!Gdx.graphics.isFullscreen()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayModes()[0]);
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (Gdx.app.getType() == Application.ApplicationType.WebGL) {
                    if (!Gdx.graphics.isFullscreen()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayModes()[0]);
                }
                return super.touchUp(screenX, screenY, pointer, button);
            }
        };

        Gdx.input.setInputProcessor(new InputMultiplexer(webGlfullscreen, mHUD));
    }

    private void createAliens(int playerEntity) {
        float alienLocations[][] = {{12.88f,5.61f},{9.68f,1.80f},{19.52f,4.75f},{26.12f,2.10f}, {29.16f,4.90f}}; //{{12.88f,5.61f}};

        for(int i=0; i<alienLocations.length; i++) {
            //load a bullet from the library
            String alienElementName = "alien_1";
            CompositeItemVO alienData = mSceneLoader.loadVoFromLibrary(alienElementName);
            if (alienData != null) {
                //set layer & create unique name and identifier
                alienData.layerName = "Default";
                alienData.itemName = "alien_" + i;
                alienData.itemIdentifier = "alien_id_" + i;

                alienData.x = alienLocations[i][0];
                alienData.y = alienLocations[i][1];

                //create the entity & init
                ItemWrapper root = new ItemWrapper(mSceneLoader.getRoot(), mSceneLoader.getEngine());
                int alien = mSceneLoader.getEntityFactory().createEntity(root.getEntity(), alienData);
                mSceneLoader.getEntityFactory().initAllChildren(alien, alienData);

                //create the alien script and set some required elements
                AlienScript alienScriptScript = new AlienScript();
                alienScriptScript.setPlayerEntity(playerEntity);

                //have to recreate the root element otherwise we don't find the alien we just created? why is this?
                root = new ItemWrapper(mSceneLoader.getRoot(), mSceneLoader.getEngine());

                ItemWrapper alienItem = root.getChild(alienData.itemIdentifier);
                ComponentRetriever.create(alienItem.getChild("alien-ani").getEntity(), AlienComponent.class, mSceneLoader.getEngine());
               // mSceneLoader.addComponentByTagName("alien", AlienComponent.class); //add the alien component to the created entity

                alienItem.addScript(alienScriptScript);

            }else{
                System.err.println("No '"+alienElementName+"' composite found in library!");
            }
        }

    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mViewport.apply();
        mEngine.process();

        mHUD.act(Gdx.graphics.getDeltaTime());
        mHUD.draw();
    }

    @Override
    public void resize(int width, int height) {
        mViewport.update(width, height);
        mHUDViewport.update(width, height, true);

        if (width != 0 && height != 0)
            mSceneLoader.resize(width, height);
    }

    @Override
    public void dispose() {
        mAssetManager.dispose();
        mSceneLoader.dispose();
    }
}

/*
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            //mSceneLoader.dispose();
            OrthographicCamera mCamera = new OrthographicCamera();
            ExtendViewport mViewport = new ExtendViewport(15, 8, mCamera);
            mSceneLoader.loadScene("WinScene", mViewport);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            //mSceneLoader.dispose();
            OrthographicCamera mCamera = new OrthographicCamera();
            ExtendViewport mViewport = new ExtendViewport(15, 8, mCamera);
            mSceneLoader.loadScene("DeadScene", mViewport);
        }
 */
