package emufog.graph;

import emufog.nodeconfig.FogNodeConfiguration;
import emufog.nodeconfig.FogNodeType;

public class FogNode extends Node{

    private FogNodeType fogNodeType;
    private FogNodeConfiguration configuration;


    public FogNode(int id, AS as, FogNodeType fogNodeType) {
        super(id, as);
        this.fogNodeType = fogNodeType;
    }

    public void setConfiguration(FogNodeConfiguration configuration){this.configuration = configuration;}

    @Override
    void addToAS() {

    }

    @Override
    public String getName() {
        return "fn: " + id;
    }
}
