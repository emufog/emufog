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
        this.configuration = new FogNodeConfiguration();
    }

    public FogNode(FogNodeType fogNodeType) {
        super();
        this.fogNodeType = fogNodeType;
        this.configuration = new FogNodeConfiguration();
    }

    public FogNode(int asID, FogNodeType fogNodeType) {
        super(asID);
        this.fogNodeType = fogNodeType;
        this.configuration = new FogNodeConfiguration();
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

}
