package games.rednblack.hyperrunner.system;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.physics.box2d.Body;

import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationComponent;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationStateComponent;
import games.rednblack.hyperrunner.HyperRunner;
import games.rednblack.hyperrunner.component.AlienComponent;

/**
 * alien animation system based on the player animation system
 * @author Jédregi
 */
@All(AlienComponent.class)
public class AlienAnimationSystem extends IteratingSystem {

    protected ComponentMapper<ParentNodeComponent> parentMapper;
    protected ComponentMapper<PhysicsBodyComponent> physicsMapper;
    protected ComponentMapper<AlienComponent> alienMapper;
    protected ComponentMapper<SpriteAnimationComponent> spriteMapper;
    protected ComponentMapper<SpriteAnimationStateComponent> spriteStateMapper;
    protected ComponentMapper<TransformComponent> transformMapper;

    @Override
    protected void process(int entity) {

        ParentNodeComponent nodeComponent = parentMapper.get(entity);
        Body body = physicsMapper.get(nodeComponent.parentEntity).body;

        if (body == null)
            return;

        AlienComponent alienComponent = alienMapper.get(entity);
        SpriteAnimationComponent spriteAnimationComponent = spriteMapper.get(entity);
        SpriteAnimationStateComponent spriteAnimationStateComponent = spriteStateMapper.get(entity);
        TransformComponent transformComponent = transformMapper.get(entity);

        if(alienComponent.isDead) {
            spriteAnimationComponent.playMode = Animation.PlayMode.LOOP;
            spriteAnimationComponent.currentAnimation = "death";
            spriteAnimationComponent.fps = Math.max(6, (int) Math.abs(body.getLinearVelocity().x) * 3);
            //we wait a couple millis then delete the alien
            if(alienComponent.deathTime==0){
                alienComponent.deathTime = System.currentTimeMillis();
                HyperRunner.soundManager.play("alien death");
            }else if(alienComponent.deathTime > 0) {
                if((System.currentTimeMillis() - alienComponent.deathTime) > alienComponent.deathPlayTime ) {
                    HyperRunner.mEngine.delete(nodeComponent.parentEntity);
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
