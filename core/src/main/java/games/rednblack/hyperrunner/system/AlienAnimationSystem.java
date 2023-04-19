package games.rednblack.hyperrunner.system;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.physics.box2d.Body;

import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationComponent;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationStateComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.ItemWrapper;
import games.rednblack.hyperrunner.HyperRunner;
import games.rednblack.hyperrunner.component.AlienComponent;


@All(AlienComponent.class)
public class AlienAnimationSystem extends IteratingSystem {

    protected ComponentMapper<ParentNodeComponent> parentMapper;
    protected ComponentMapper<PhysicsBodyComponent> physicsMapper;
    protected ComponentMapper<AlienComponent> alienMapper;
    protected ComponentMapper<SpriteAnimationComponent> spriteMapper;
    protected ComponentMapper<SpriteAnimationStateComponent> spriteStateMapper;
    protected ComponentMapper<TransformComponent> transformMapper;
    protected ComponentMapper<MainItemComponent> mainItemMapper;


    @Override
    protected void process(int entity) {

        MainItemComponent mainItemComponent = mainItemMapper.get(entity);

        PhysicsBodyComponent mPhysicsBodyComponent = physicsMapper.get(entity);
        if(mPhysicsBodyComponent==null)
            return;
        Body body = mPhysicsBodyComponent.body;
        if (body == null)
            return;
        AlienComponent alienComponent = alienMapper.get(entity);

        //the long way to get the entities animation..
        ItemWrapper root = new ItemWrapper(HyperRunner.mSceneLoader.getRoot(), HyperRunner.mSceneLoader.getEngine());
        ItemWrapper alienItem = root.getChild(mainItemComponent.itemIdentifier);
        alienItem.getChild("alien-ani").getEntity();

        SpriteAnimationComponent spriteAnimationComponent = spriteMapper.get(alienItem.getChild("alien-ani").getEntity());
        SpriteAnimationStateComponent spriteAnimationStateComponent = spriteStateMapper.get(alienItem.getChild("alien-ani").getEntity());

        TransformComponent transformComponent = transformMapper.get(entity);

        if(spriteAnimationComponent == null || spriteAnimationStateComponent == null)
            return;

        if(alienComponent.isDead) {
            spriteAnimationComponent.playMode = Animation.PlayMode.LOOP;
            spriteAnimationComponent.currentAnimation = "death";
            spriteAnimationComponent.fps = Math.max(6, (int) Math.abs(body.getLinearVelocity().x) * 3);
            //we wait a couple millis then delete the alien
            if(alienComponent.deathTime==0){
                alienComponent.deathTime = System.currentTimeMillis();
            }else if(alienComponent.deathTime > 0) {
                if((System.currentTimeMillis() - alienComponent.deathTime) > alienComponent.deathPlayTime ) {
                    HyperRunner.mEngine.delete(entity); // delete this bullet on touch
                }
            }
        } else {

            if (Math.abs(body.getLinearVelocity().x) > 0.1f) {

                spriteAnimationComponent.playMode = Animation.PlayMode.LOOP;

                spriteAnimationComponent.currentAnimation = "run";
                spriteAnimationComponent.fps = Math.max(6, (int) Math.abs(body.getLinearVelocity().x) * 3);

                transformComponent.flipX = body.getLinearVelocity().x < 0;
            } else {
                spriteAnimationComponent.playMode = Animation.PlayMode.LOOP;
                spriteAnimationComponent.currentAnimation = "idle";

            }

            if (body.getLinearVelocity().y > 0.2f) {
                spriteAnimationComponent.currentAnimation = "jump";
                spriteAnimationComponent.playMode = Animation.PlayMode.NORMAL;
            } else if (body.getLinearVelocity().y < -0.2f) {
                spriteAnimationComponent.currentAnimation = "jump";
                spriteAnimationComponent.playMode = Animation.PlayMode.REVERSED;
            }
        }
        spriteAnimationStateComponent.set(spriteAnimationComponent);
    }
}
