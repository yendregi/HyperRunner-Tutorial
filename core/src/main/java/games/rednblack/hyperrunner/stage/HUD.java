package games.rednblack.hyperrunner.stage;

import static games.rednblack.hyperrunner.script.ScriptGlobals.LEFT;
import static games.rednblack.hyperrunner.script.ScriptGlobals.RIGHT;
import static games.rednblack.hyperrunner.script.ScriptGlobals.JUMP;

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

/**
 * The user ui "hud stage" - controls various aspects of the simple GUI for this game
 * @author fgnm & Jédregi
 */
public class HUD extends Stage {

    private Label mDiamondsLabel;

    private PlayerScript mPlayerScript;

    private boolean leftClicked = false;
    private boolean rightClicked = false;

    private int diamonds = -1;

    private boolean playerRetry = false;

    private boolean playerDeathTrigger = false;
    private boolean level_1_trigger = false;
    private Label tryAgainLabel;

    private boolean screenGUIEnabled = false; // remove button ui for the moment .. this could be a switch to enable or disable

    public HUD(Skin skin, TextureAtlas atlas, Viewport viewport, Batch batch) {
        super(viewport, batch);

        Table root = new Table();
        root.pad(10, 20, 10, 20);
        root.setFillParent(true);

        Table gemCounter = new Table();
        Image diamond = new Image(atlas.findRegion("GemCounter"));
        gemCounter.add(diamond);

        tryAgainLabel = new Label("-TRY AGAIN-", skin);
        tryAgainLabel.setPosition(325,200);
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
// shouldn't this work ? https://stackoverflow.com/questions/23174722/mouse-hover-libgdx
/*
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, int button){

            }
*/
        });


        mDiamondsLabel = new Label("Diamonds", skin);
        gemCounter.add(mDiamondsLabel);

        root.add(gemCounter).expand().left().top().colspan(2);
        root.row();

        ImageButton leftButton = new ImageButton(skin, "left");
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

        ImageButton rightButton = new ImageButton(skin, "right");
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
        root.add(rightButton).left().bottom().padLeft(20); //on screen right

        ImageButton upButton = new ImageButton(skin, "up");
        upButton.setVisible(screenGUIEnabled);
        upButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mPlayerScript.movePlayer(JUMP);
            }
        });
        root.add(upButton).expand().right().bottom(); // on screen up

        addActor(root);
        addActor(tryAgainLabel);
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

        //check if we need to load a level then do so -
        if (playerRetry) {
            playerRetry = false;
            tryAgainLabel.setVisible(false);
            //load the main scene again and recreate the level
            this.setPlayerScript(HyperRunner.loadDefaultScene());
            playerDeathTrigger = false; //do not forget to reset all triggers!
            level_1_trigger = false;
        }


        if(mPlayerScript.getPlayerComponent() != null && mPlayerScript.getPlayerComponent().isDead) {
            if(!playerDeathTrigger) {
                playerDeathTrigger = true;
                HyperRunner.mSceneLoader.loadScene("PlayerDies", HyperRunner.mViewport);
                HyperRunner.soundManager.play("player dies");
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
