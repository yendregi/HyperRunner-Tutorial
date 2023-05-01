package games.rednblack.hyperrunner.stage;

import static games.rednblack.hyperrunner.script.ScriptGlobals.LEFT;
import static games.rednblack.hyperrunner.script.ScriptGlobals.RIGHT;
import static games.rednblack.hyperrunner.script.ScriptGlobals.UP;
import static games.rednblack.hyperrunner.util.SoundManager.playerDies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.Viewport;

import games.rednblack.hyperrunner.HyperRunner;
import games.rednblack.hyperrunner.script.PlayerScript;
import games.rednblack.hyperrunner.util.LoadUtil;

/**
 * The user ui "hud stage" - controls various aspects of the simple GUI for this game
 * @author fgnm & Jédregi
 */
public class HUD extends Stage {

    // the main player script
    private PlayerScript mPlayerScript;

    // player states
    private int diamonds = -1;
    // & stuff to keep track off
    private boolean playerRetry = false;
    private boolean playerDeathTrigger = false;
    private boolean level_1_trigger = false;

    // labels we care about
    private final Label mDiamondsLabel;
    private final Label tryAgainLabel;
    private final Label hudIcons;

    private boolean screenGUIEnabled = false;  //add or remove ui based on player choice (work in progress at the moment)

    //hud stuff
    private boolean leftClicked = false;
    private boolean rightClicked = false;
   // private boolean fireClicked = false;

    private ImageButton leftButton = null;
    private ImageButton upButton = null;
    private ImageButton rightButton = null;
    //private ImageButton fireButton = null;

    public HUD(Skin skin, TextureAtlas atlas, Viewport viewport, Batch batch) {
        super(viewport, batch);

        Table root = new Table();
        root.pad(10, 20, 10, 20);
        root.setFillParent(true);

        Table gemCounter = new Table();
        Image diamond = new Image(atlas.findRegion("GemCounter"));
        gemCounter.add(diamond);

        hudIcons = new Label("iHUD "+(screenGUIEnabled?"On":"Off"), skin);
        hudIcons.setPosition(655,550); //this only works on "fixed screen resolution"
        hudIcons.setVisible(true);
        hudIcons.addListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                screenGUIEnabled = !screenGUIEnabled;
                hudIcons.setText("iHUD "+(screenGUIEnabled?"On":"Off"));
                leftButton.setVisible(screenGUIEnabled);
                rightButton.setVisible(screenGUIEnabled);
                upButton.setVisible(screenGUIEnabled);

            }
        });

        tryAgainLabel = new Label("-TRY AGAIN-", skin);
        tryAgainLabel.setPosition(325,200); //this only works on "fixed screen resolution"
        tryAgainLabel.setVisible(false);
        tryAgainLabel.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return super.touchDown(event, x, y, pointer, button);
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                playerRetry = true;
            }
        });


        mDiamondsLabel = new Label("Diamonds", skin);
        gemCounter.add(mDiamondsLabel);

        root.add(gemCounter).expand().left().top().colspan(2);
        root.row();

        leftButton = new ImageButton(skin, "left");
        leftButton.setVisible(screenGUIEnabled);
        leftButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                leftClicked = true;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                leftClicked = false;
            }
        });
        root.add(leftButton).left().bottom(); //on screen left

        rightButton = new ImageButton(skin, "right");
        rightButton.setVisible(screenGUIEnabled);
        rightButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                rightClicked = true;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                rightClicked = false;
            }
        });
        root.add(rightButton).left().bottom().padLeft(10); //on screen right

        /**
         * something to do in the future - learn the skin composer ui & it's capabilities .. see https://github.com/raeleus/skin-composer
         * this is basic code template to work on ..
        */
        /*
        fireButton = new ImageButton(skin, "bulletfire");
        fireButton.setVisible(screenGUIEnabled);
        fireButton.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                fireClicked = true;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                fireClicked = false;
            }
        });
        root.add(fireButton).left().bottom().padLeft(20); // fire bullet
*/

        upButton = new ImageButton(skin, "up");
        upButton.setVisible(screenGUIEnabled);
        upButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mPlayerScript.movePlayer(UP);
            }
        });
        root.add(upButton).expand().right().bottom(); // on screen up

        addActor(root);
        addActor(tryAgainLabel);
        //addActor(hudIcons);
    }

    public void setPlayerScript(PlayerScript playerScript) {
        mPlayerScript = playerScript;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (leftClicked)
            mPlayerScript.movePlayer(LEFT);

        if (rightClicked)
            mPlayerScript.movePlayer(RIGHT);

        if (mPlayerScript.getPlayerComponent() != null && diamonds != mPlayerScript.getPlayerComponent().diamondsCollected) {
            diamonds = mPlayerScript.getPlayerComponent().diamondsCollected;
            mDiamondsLabel.setText("x" + diamonds);
        }

        if(mPlayerScript.ropeJointDef != null && !mPlayerScript.playerAttached) {
            mPlayerScript.playerAttached = true;
            mPlayerScript.ropeJoint = HyperRunner.mSceneLoader.getWorld().createJoint(mPlayerScript.ropeJointDef);
        }

        if(mPlayerScript.ropeJointDef != null && mPlayerScript.unAttachPlayer) {
            mPlayerScript.playerAttached = false;
            mPlayerScript.unAttachPlayer = false;
            HyperRunner.mSceneLoader.getWorld().destroyJoint(mPlayerScript.ropeJoint);
            mPlayerScript.ropeJointDef = null;
            mPlayerScript.ropeJoint = null;
        }

        //check if we need to load a level then do so -
        if (playerRetry) {

            //reset ui
            playerRetry = false;
            tryAgainLabel.setVisible(false);

            //reset player triggers
            playerDeathTrigger = false;
            level_1_trigger = false;

            //load the main scene again and recreate the level
            this.setPlayerScript(LoadUtil.loadDefaultScene());

        }


        if(mPlayerScript.getPlayerComponent() != null && mPlayerScript.getPlayerComponent().isDead) {
            if(!playerDeathTrigger) {
                playerDeathTrigger = true;
                HyperRunner.mSceneLoader.loadScene("PlayerDies", HyperRunner.mViewport);
                HyperRunner.soundManager.play(playerDies);
                tryAgainLabel.setVisible(true);
            }
        }

        if(mPlayerScript.getPlayerComponent() != null && mPlayerScript.getPlayerComponent().level1Done) {
            if(!level_1_trigger) {
                level_1_trigger = true;
                HyperRunner.mSceneLoader.loadScene("LevelComplete", HyperRunner.mViewport);
                tryAgainLabel.setVisible(true);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            System.exit(0);
        }

    }

}


/*

 NOTES from  fgnm about loading scenes
Question: Creating a class that extends a stage and there manipulates the scenes ?

In a general form yes..
 I have the stage and scene2d widgets that can manipulate some aspects of the ECS engine like scene changing...
 But more in depth I've PureMVC, in which I there's a singleton class Facade which act as a shared event-bus between ECS and GUI...
 For example a click of a GUI button produce an event which can be handled by a global controller object, or by a script, or other GUI elements...
 Same from the ECS side, a particular situation (like catch of a special element) can produce a notification event on the bus which trigger a special GUI behavior
In this way ECS and GUI know nothing about them self, they just communicate by listening and sending notifications on a shared bus
Currently I've no project I can open source.. I could eventually arrange to fork the space platformer and make a small example 🤔
but if you are interested the official site of the pattern is very well documentated https://puremvc.org/
The PureMVC Framework
Code at the Speed of Thought
Stable and feature-frozen since 2008, PureMVC has been ported to most major development platforms.
Code at the Speed of Thought
And of course I did an optimized version for libGDX 😆 https://github.com/rednblackgames/gdx-puremvc
*/
