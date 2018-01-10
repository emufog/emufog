package emufog.topology;

import emufog.graph.AS;
import emufog.nodeconfig.FogNodeConfiguration;
import emufog.nodeconfig.FogNodeType;

public class FogNode extends Node{

    private FogNodeType fogNodeType;
    private FogNodeConfiguration configuration;

    public FogNode(int id, AS as, FogNodeType fogNodeType) {
        super(id, as);
        this.fogNodeType = fogNodeType;
    }

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
