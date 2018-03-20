package emufog.topology;


import emufog.nodeconfig.FogNodeConfiguration;
import emufog.nodeconfig.FogNodeType;
import emufog.util.Logger;


import java.util.Comparator;

import static emufog.settings.Settings.getSettings;

public class FogNode extends Node {

    Logger logger = Logger.getInstance();

    private FogNodeType fogNodeType;
    private FogNodeConfiguration configuration;

    public FogNode() {
        super();
    }

    public FogNode(FogNodeType fogNodeType) {
        super();
        this.fogNodeType = fogNodeType;
    }

    public FogNode(int asID, FogNodeType fogNodeType) {
        super(asID);
        this.fogNodeType = fogNodeType;
    }

    @Override
    public String getName() {
        return "f" + getID();
    }

    public void setConfiguration(FogNodeConfiguration configuration) {
        this.configuration = configuration;
    }

    public FogNodeType getFogNodeType() {
        if (fogNodeType != null) {
            return fogNodeType;
        } else {
            logger.log("This Fog Node has no type assigned yet. Assigning default fogNodeType");
            return this.fogNodeType = getSettings().getFogNodeTypes().get(0);
        }
    }

    public FogNodeConfiguration getConfiguration() {

        return configuration;

    }

    /**
     * Custom comparator to sort a list of FogNodes.
     * The Comparator uses two properties. First the comparator sorts descending according
     * to the average deployment costs and in case of equal deployment costs descending to
     * the average connections costs.
     */
    class FogComparator implements Comparator<FogNode> {

        @Override
        public int compare(FogNode fogNode1, FogNode fogNode2) {

            float cost1 = averageDeploymentCosts(fogNode1);
            float cost2 = averageDeploymentCosts(fogNode2);

            if (cost1 < cost2) return -1;
            if (cost2 < cost1) return 1;


            cost1 = averageConnectionCosts(fogNode1);
            cost2 = averageConnectionCosts(fogNode2);

            if (cost1 < cost2) return -1;
            if (cost2 < cost1) return 1;

            return 0;
        }

        private float averageDeploymentCosts(FogNode fogNode) {
            float sum = 0.f;

            return 0;
        }

        private float averageConnectionCosts(FogNode fogNode) {
            return 0;
        }
    }


}
