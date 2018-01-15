package emufog.topology;

import emufog.nodeconfig.FogNodeConfiguration;
import emufog.nodeconfig.FogNodeType;

public class FogNode extends Node{

    private FogNodeType fogNodeType;
    private FogNodeConfiguration configuration;

    public FogNode(int id, int asID, FogNodeType fogNodeType) {
        super(id, asID);
        this.fogNodeType = fogNodeType;
    }

    @Override
    public String getName() { return "h" + getID();}

    public void setConfiguration(FogNodeConfiguration configuration) {
        this.configuration = configuration;
    }

    public FogNodeType getFogNodeType() {
        return fogNodeType;
    }

    public FogNodeConfiguration getConfiguration() {
        return configuration;
    }
}
