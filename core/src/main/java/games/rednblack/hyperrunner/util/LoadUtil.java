package games.rednblack.hyperrunner.util;

import static games.rednblack.hyperrunner.HyperRunner.cameraSystem;
import static games.rednblack.hyperrunner.HyperRunner.mEngine;
import static games.rednblack.hyperrunner.HyperRunner.mSceneLoader;
import static games.rednblack.hyperrunner.HyperRunner.mViewport;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.ItemWrapper;
import games.rednblack.hyperrunner.component.AlienComponent;
import games.rednblack.hyperrunner.component.ChainAnchorComponent;
import games.rednblack.hyperrunner.component.ChainLinkComponent;
import games.rednblack.hyperrunner.component.DiamondComponent;
import games.rednblack.hyperrunner.component.PlayerComponent;
import games.rednblack.hyperrunner.component.PortalComponent;
import games.rednblack.hyperrunner.script.AlienScript;
import games.rednblack.hyperrunner.script.ChainLinkScript;
import games.rednblack.hyperrunner.script.PlayerScript;

/**
 * LoadUtil - basic loading utility for various loading needs with hyperlap and this tutorial
 *
 * @author Jédregi
 */
public class LoadUtil {


    /**
     * load the default scene
     *
     * @return PlayerScript - the main script controlling the player
     */
    public static PlayerScript loadDefaultScene() {

        //load the main scence
        mSceneLoader.loadScene("MainScene", mViewport);

        String playerElementName = "player_1";
        CompositeItemVO playerData = mSceneLoader.loadVoFromLibrary(playerElementName);
        PlayerScript playerScript = null;
        //load the player and put em' into the default position
        if (playerData != null) {
            //set layer & create unique name and identifier
            playerData.layerName = "Default";
            playerData.itemName = "player_1";
            playerData.itemIdentifier = "player_1";
            playerData.x = 3.83f;
            playerData.y = 3.25f;

            //create the entity & init
            int player = mSceneLoader.getEntityFactory().createEntity(mSceneLoader.getRoot(), playerData);
            mSceneLoader.getEntityFactory().initAllChildren(player, playerData);

            ItemWrapper root = new ItemWrapper(mSceneLoader.getRoot(), mEngine);
            ItemWrapper playerWrapper = root.getChild("player_1");
            ComponentRetriever.create(playerWrapper.getChild("player-anim").getEntity(), PlayerComponent.class, mEngine);
            playerScript = new PlayerScript();
            playerWrapper.addScript(playerScript);
            cameraSystem.setFocus(playerWrapper.getEntity());

            //debugging for entity creation... if you need it ;)
            //System.out.println("player=("+player+") player.entity("+playerWrapper.getEntity()+") player.getChild(\"player-anim\").getEntity()==("+playerWrapper.getChild("player-anim").getEntity()+")");

            //dynamically create some aliens:
            createAliens(playerWrapper.getEntity());

        }

        mSceneLoader.addComponentByTagName("diamond", DiamondComponent.class);
        mSceneLoader.addComponentByTagName("portal", PortalComponent.class);

        //build the chain link for demo fun!
        buildChainLinkWithAnchor(new Vector2(6.5f,10.19f));

        return playerScript;
    }

    /**
     * create some alien enemies
     * @param playerEntity
     */
    private static void createAliens(int playerEntity) {
        float[][] alienLocations = {
                {12.88f,5.95f},
                {9.68f,1.80f},
                {19.52f,5.24f},
                {26.12f,2.49f},
                {29.16f,5.27f},
                {37.67f,3.07f}

        };

        for(int i=0; i<alienLocations.length; i++) {
            //load a alien from the library
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
                int alien = mSceneLoader.getEntityFactory().createEntity(mSceneLoader.getRoot(), alienData);
                mSceneLoader.getEntityFactory().initAllChildren(alien, alienData);

                //create the alien script and setup the followed player entity --> as a note, this can be done better using the box2d sensors. I do this for the purposes of "quick and dirty"
                AlienScript alienScriptScript = new AlienScript();
                alienScriptScript.setPlayerEntity(playerEntity);

                //create the root to get the child item
                ItemWrapper root = new ItemWrapper(mSceneLoader.getRoot(), mEngine);
                ItemWrapper alienItem = root.getChild(alienData.itemIdentifier);
                ComponentRetriever.create(alienItem.getChild("alien-ani").getEntity(), AlienComponent.class, mEngine);

                //debugging for entity creation... if you need it ;)
                //System.out.println("alien=("+alien+") alienItem.entity("+alienItem.getEntity()+") alienItem.getChild(\"alien-ani\").getEntity()==("+alienItem.getChild("alien-ani").getEntity()+")");

                alienItem.addScript(alienScriptScript);

            }else{
                System.err.println("No '"+alienElementName+"' composite found in library!");
            }
        }

    }

    private static void buildChainLinkWithAnchor(Vector2 chainAnchorLocation) {
        ArrayList<Integer> chainLinkList = new ArrayList<Integer>();

        //the height of the chain link part
        float partHeight = 0.49f;

        float[][] chainLinkLocations = {
                {chainAnchorLocation.x,chainAnchorLocation.y-(partHeight*1)}
                ,{chainAnchorLocation.x,chainAnchorLocation.y-(partHeight*2)}
                ,{chainAnchorLocation.x,chainAnchorLocation.y-(partHeight*3)}
                ,{chainAnchorLocation.x,chainAnchorLocation.y-(partHeight*4)}
                ,{chainAnchorLocation.x,chainAnchorLocation.y-(partHeight*5)}
                ,{chainAnchorLocation.x,chainAnchorLocation.y-(partHeight*6)}
                ,{chainAnchorLocation.x,chainAnchorLocation.y-(partHeight*7)}
        };

        for(int i=0; i<chainLinkLocations.length; i++) {
            chainLinkList.add(generateChainLinkPart(chainLinkLocations[i][0], chainLinkLocations[i][1], (i==chainLinkLocations.length-1)));
        }
        generateChainAnchor(chainAnchorLocation, chainLinkList);

    }

    private static int generateChainLinkPart(float x, float y, boolean gravityEnabled) {

        String chainLinkElementName = gravityEnabled ? "chainLink_2" : "chainLink_1";
        CompositeItemVO chainLinkData = mSceneLoader.loadVoFromLibrary(chainLinkElementName);
        chainLinkData.layerName = "Default";
        chainLinkData.x = x;
        chainLinkData.y = y;

        //create the entity & init
        int chainEntityId = mSceneLoader.getEntityFactory().createEntity(mSceneLoader.getRoot(), chainLinkData);
        mSceneLoader.getEntityFactory().initAllChildren(chainEntityId, chainLinkData);

        ItemWrapper chainLinkPart = new ItemWrapper(chainEntityId, mEngine);
        ItemWrapper chainLinkChild = chainLinkPart.getChild("chainLinkImage");
        ComponentRetriever.create(chainLinkChild.getEntity(), ChainLinkComponent.class, mEngine);

        return chainLinkChild.getEntity();
    }

    private static void generateChainAnchor(Vector2 chainAnchorLocation, ArrayList<Integer> chainLinkList) {

        String chainAnchorElementName = "chainAnchor_1";
        CompositeItemVO chainAnchorData = mSceneLoader.loadVoFromLibrary(chainAnchorElementName);
        chainAnchorData.layerName = "Default";
        chainAnchorData.x = chainAnchorLocation.x;
        chainAnchorData.y = chainAnchorLocation.y;

        //create the entity & init
        int chainAnchorEntityId = mSceneLoader.getEntityFactory().createEntity(mSceneLoader.getRoot(), chainAnchorData);
        mSceneLoader.getEntityFactory().initAllChildren(chainAnchorEntityId, chainAnchorData);

        ItemWrapper chainAnchorPart = new ItemWrapper(chainAnchorEntityId, mEngine);
        ComponentRetriever.create(chainAnchorPart.getEntity(), ChainAnchorComponent.class, mEngine);

        //importantly, set the chain link script and set the anchor entity
        ChainLinkScript chainScript = new ChainLinkScript();
        chainScript.setChainLinkList(chainLinkList);
        chainScript.setAnchorEntity(chainAnchorPart.getEntity());

        chainAnchorPart.addScript(chainScript);

    }


}
