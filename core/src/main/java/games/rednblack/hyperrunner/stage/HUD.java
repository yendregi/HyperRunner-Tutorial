package games.rednblack.hyperrunner.stage;

import static games.rednblack.hyperrunner.script.ScriptGlobals.LEFT;
import static games.rednblack.hyperrunner.script.ScriptGlobals.RIGHT;
import static games.rednblack.hyperrunner.script.ScriptGlobals.JUMP;

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

public class HUD extends Stage {

    private Label mDiamondsLabel;

    private PlayerScript mPlayerScript;

    private boolean leftClicked = false;
    private boolean rightClicked = false;

    private int diamonds = -1;

    private int nextStage = 0;

    public HUD(Skin skin, TextureAtlas atlas, Viewport viewport, Batch batch) {
        super(viewport, batch);

        Table root = new Table();
        root.pad(10, 20, 10, 20);
        root.setFillParent(true);

        Table gemCounter = new Table();
        Image diamond = new Image(atlas.findRegion("GemCounter"));
        gemCounter.add(diamond);

        mDiamondsLabel = new Label("Diamonds", skin);
        gemCounter.add(mDiamondsLabel);


        ImageButton test = new ImageButton(skin, "left");
        test.setWidth(0.1f);
        test.setHeight(0.1f);
        test.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return super.touchDown(event, x, y, pointer, button);
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                nextStage++;
            }
        });

        root.add(gemCounter).expand().left().top().colspan(2);
       // root.add(test).expand().right().top().colspan(1);
        root.row();

        ImageButton leftButton = new ImageButton(skin, "left");
        leftButton.setVisible(false);
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
        rightButton.setVisible(false);
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
        upButton.setVisible(false);
        upButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mPlayerScript.movePlayer(JUMP);
            }
        });
        root.add(upButton).expand().right().bottom(); // on screen up

        addActor(root);
    }

    public void setPlayerScript(PlayerScript playerScript) {
        mPlayerScript = playerScript;
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if(nextStage == 0) {
            if (leftClicked)
                mPlayerScript.movePlayer(LEFT);
            if (rightClicked)
                mPlayerScript.movePlayer(RIGHT);

            if (diamonds != mPlayerScript.getPlayerComponent().diamondsCollected) {
                diamonds = mPlayerScript.getPlayerComponent().diamondsCollected;
                mDiamondsLabel.setText("x" + diamonds);
            }
        }

        int levNum = 3;
        if(nextStage>0)
            if(nextStage!=0 && nextStage%levNum==1) {
                HyperRunner.mSceneLoader.loadScene("LevelComplete", HyperRunner.mViewport);
            } else if(nextStage!=0 && nextStage%levNum==2) {
                HyperRunner.mSceneLoader.loadScene("PlayerDies", HyperRunner.mViewport);
            } else if(nextStage!=0 && nextStage%levNum==0) {
                HyperRunner.mSceneLoader.loadScene("MainScene", HyperRunner.mViewport);
            }

    }
}


/*

 // mSceneLoader.loadScene("LevelComplete", mViewport);
 // mSceneLoader.loadScene("PlayerDies", mViewport);

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
