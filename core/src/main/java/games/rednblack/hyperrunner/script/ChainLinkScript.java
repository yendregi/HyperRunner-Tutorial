package games.rednblack.hyperrunner.script;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.scripts.BasicScript;
import games.rednblack.hyperrunner.HyperRunner;

public class ChainLinkScript extends BasicScript {

    protected ArrayList<Integer> chainLinkList = null;
    protected ComponentMapper<PhysicsBodyComponent> physicsMapper;
    protected boolean chainInit = false;
    protected int anchorEntity = -1;
    protected Body lastLinkBody = null;


    @Override
    public void init(int item) {
        super.init(item);
    }

    @Override
    public void act(float delta) {
        /**
         * init function that is called only when the chain isn't initialized
         * this script importantly builds all the joint connections between the parts it's aware of
         */
        if(!chainInit && chainLinkList!=null && anchorEntity != -1 ) {
            chainInit = createChainLinks();
        }

    }

    public boolean createChainLinks(){

        PhysicsBodyComponent chainAnchor = physicsMapper.get(anchorEntity);
        Body anchorBody = chainAnchor.body;

        Array<Body> bodies =  new Array<Body>();
        HyperRunner.mSceneLoader.getWorld().getBodies(bodies);

        if(anchorBody==null)
            return false;

        /**
         * trick here is to weld join the initial link to the anchor, then use rope joint for other links, but the last link is the one the can be affected by gravity
         * something to note : depending on the behaviour you are after there are quite a few different joints to try from
         */
        for(int i=0; i<chainLinkList.size(); i++) {
            /** possible join definitions possible... up to the student to try these
             *  DistanceJointDef,
             *  FrictionJointDef,
             *  GearJointDef,
             *  MotorJointDef,
             *  MouseJointDef,
             *  PrismaticJointDef,
             *  PulleyJointDef,
             *  RevoluteJointDef,
             *  RopeJointDef,
             *  WeldJointDef,
             *  WheelJointDef
             */

            JointDef jointDef = ((i == 0) ? new WeldJointDef() : new RopeJointDef()); //the first join will be a weld type (after the second joint there still some oddities with how the physics are reacting .. something to sort out one day)

            if( i ==0 ) {
                jointDef.bodyA = anchorBody;
            } else {
                jointDef.bodyA = physicsMapper.get(chainLinkList.get(i-1)).body;
            }
            jointDef.collideConnected = true;

            jointDef.bodyB = physicsMapper.get(chainLinkList.get(i)).body;
            lastLinkBody = physicsMapper.get(chainLinkList.get(i)).body;

            if(i==0) {
                if( jointDef instanceof WeldJointDef ) {
                    ((WeldJointDef) jointDef).localAnchorA.set(0, -0.25f);
                    ((WeldJointDef) jointDef).localAnchorB.set(-0.37f, 0);
                } else if(jointDef instanceof RopeJointDef ) {
                    ((RopeJointDef) jointDef).localAnchorA.set(0, -0.25f);
                    ((RopeJointDef) jointDef).localAnchorB.set(-0.37f, 0);
                }
            } else {
                if( jointDef instanceof WeldJointDef ) {
                    ((WeldJointDef) jointDef).localAnchorA.set(0, -0.25f);
                    ((WeldJointDef) jointDef).localAnchorB.set(0, 0.25f);
                } else if(jointDef instanceof RopeJointDef ) {
                    ((RopeJointDef) jointDef).localAnchorA.set(0, -0.25f);
                    ((RopeJointDef) jointDef).localAnchorB.set(0, 0.25f);
                }
            }
            HyperRunner.mSceneLoader.getWorld().createJoint(jointDef);

            lastLinkBody.applyLinearImpulse(new Vector2(3,0),lastLinkBody.getWorldCenter(), true); //set some initial force on the last link to make the rope initially sway
        }
        return true;
    }

    @Override
    public void dispose() {

    }

    public void setChainLinkList(ArrayList<Integer> chainLinkList) {
        this.chainLinkList = chainLinkList;
    }


    public void setAnchorEntity(int anchorEntity) {
        this.anchorEntity = anchorEntity;
    }
}
