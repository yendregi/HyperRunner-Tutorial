package games.rednblack.hyperrunner;

import static games.rednblack.hyperrunner.util.SoundManager.stage_1_music;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.resources.AsyncResourceManager;
import games.rednblack.editor.renderer.resources.ResourceManagerLoader;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.ItemWrapper;
import games.rednblack.editor.renderer.ExternalTypesConfiguration;

import games.rednblack.hyperrunner.component.AlienComponent;
import games.rednblack.hyperrunner.component.ChainAnchorComponent;
import games.rednblack.hyperrunner.component.ChainLinkComponent;
import games.rednblack.hyperrunner.component.DiamondComponent;
import games.rednblack.hyperrunner.component.PlayerComponent;
import games.rednblack.hyperrunner.component.BulletComponent;
import games.rednblack.hyperrunner.component.PortalComponent;
import games.rednblack.hyperrunner.script.AlienScript;
import games.rednblack.hyperrunner.script.ChainLinkScript;
import games.rednblack.hyperrunner.script.PlayerScript;
import games.rednblack.hyperrunner.stage.HUD;
import games.rednblack.hyperrunner.system.AlienAnimationSystem;
import games.rednblack.hyperrunner.system.CameraSystem;
import games.rednblack.hyperrunner.system.PlayerAnimationSystem;

//import games.rednblack.h2d.extension.talos.*;
import games.rednblack.h2d.extension.talos.TalosItemType;

import games.rednblack.hyperrunner.util.LoadUtil;
import games.rednblack.hyperrunner.util.SoundManager;

/**
 * Student challenge by fgnm@discord
 * AS A STUDENT EXERCISE TRY THE FOLLOWING:
 * ADD THE ALIEN AND SOME BASIC AI!
 * ADD AN ADDITIONAL SCREEN TO LOAD ON PLAYER TOUCHING LEVEL EXIT
 *
 * Thus!
 * This is my adaptation of the initial tutorial and the expansion...
 *
 * What does this demo add beyond the basic tutorial ?
 *
 *  - talos vfx add-ons (thanks @fgnm for making this possible!!!)
 *  -- the end portal is a talos vfx effect ... this is portal I made post the talos vfx portal tutorial
 *  -- the projectiles used, 'bullets', are talos vfx orbs with lighting!
 *  - "system" add-ons:
 *  -- aliens and player are dynamically added
 *  -- aliens have a basic ai
 *  -- aliens and player both can shoot a 'bullet'
 *  --- the bullet is an instance of a talos vfx orb effect with lighting!
 *  -- depending on whom shot the 'bullet', either the player or alien dies
 *  -- there is a concept of a basic "game loop":
 *  --- when player "dies" they sees a death screen & can retry the level
 *  --- when player "wins" (they exit the level) they sees a level complete & can retry the level
 *  -- there is a basic sound manager created which adds:
 *  --- ability to load and play "sounds"
 *  ---- level 1 stage is music I wrote based on the "beads" library
 *  ---- lazer sound is thanks to "Kenny" media
 *  ---- other sound effects are my own
 *
 * @author fgnm & Jédregi
 *
{@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class HyperRunner extends ApplicationAdapter {

    // globals for this game for use within other classes
    public static AssetManager mAssetManager;

    public static SceneLoader mSceneLoader;
    public static AsyncResourceManager mAsyncResourceManager;

    public static Viewport mViewport;
    public static OrthographicCamera mCamera;
    public static CameraSystem cameraSystem;

    protected boolean debugRendererEnabled = false; //change this to see the box2d debug rendering
    protected Box2DDebugRenderer box2DDebugRenderer;

    public static com.artemis.World mEngine;
    public static SoundManager soundManager;

    protected HUD mHUD;
    protected ExtendViewport mHUDViewport;

    @Override
    public void create() {

        //debug rendering if we need it
        box2DDebugRenderer = new Box2DDebugRenderer();

        //init asset manger
        mAssetManager = new AssetManager();

        //init the sound manager
        soundManager = new SoundManager();
        // soundManager.mute(); // to sound or not

        //setup the talos extension plug'in.
        ExternalTypesConfiguration externalItemTypes = new ExternalTypesConfiguration();
        externalItemTypes.addExternalItemType(new TalosItemType());

        //setup asset manager's resource manager & load the project
        mAssetManager.setLoader(AsyncResourceManager.class, new ResourceManagerLoader(externalItemTypes, mAssetManager.getFileHandleResolver()));
        mAssetManager.load("project.dt", AsyncResourceManager.class);
        mAssetManager.load("skin/skin.json", Skin.class);
        mAssetManager.finishLoading();

        mAsyncResourceManager = mAssetManager.get("project.dt", AsyncResourceManager.class);
        SceneConfiguration config = new SceneConfiguration();

        //add the talos extension plug'in
        config.setExternalItemTypes(externalItemTypes);
        config.setResourceRetriever(mAsyncResourceManager);

        //setup camera system
        cameraSystem = new CameraSystem(5, 40, 5, 20);
        config.addSystem(cameraSystem);

        //add the animation systems
        config.addSystem(new PlayerAnimationSystem());
        config.addSystem(new AlienAnimationSystem());

        //tells Runtime to automatically attach a component to entities loaded with a specific TAG.
        config.addTagTransmuter("diamond", DiamondComponent.class);
        config.addTagTransmuter("portal", PortalComponent.class);

        //core setup - scene loader & the physics engine
        mSceneLoader = new SceneLoader(config);
        mEngine = mSceneLoader.getEngine();

        // required mappers for own components :
        ComponentRetriever.addMapper(PlayerComponent.class);
        ComponentRetriever.addMapper(DiamondComponent.class);
        ComponentRetriever.addMapper(BulletComponent.class);
        ComponentRetriever.addMapper(AlienComponent.class);
        ComponentRetriever.addMapper(PortalComponent.class);
        ComponentRetriever.addMapper(ChainAnchorComponent.class);
        ComponentRetriever.addMapper(ChainLinkComponent.class);

        //setup the camera & view port
        mCamera = new OrthographicCamera();
        mViewport = new ExtendViewport(15, 8, mCamera);

        mHUDViewport = new ExtendViewport(768, 576);
        mHUD = new HUD(mAssetManager.get("skin/skin.json"), mAsyncResourceManager.getTextureAtlas("main"), mHUDViewport, mSceneLoader.getBatch());

        //load the default scene and setup player script

        PlayerScript playerScript = LoadUtil.loadDefaultScene();
        mHUD.setPlayerScript(playerScript);

        soundManager.playLooping(stage_1_music);

        //init the web gl bit
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

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mViewport.apply();
        mEngine.process();

        if(debugRendererEnabled)
            box2DDebugRenderer.render(mSceneLoader.getWorld(),mCamera.combined);

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
