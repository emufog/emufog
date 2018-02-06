package emufog.nodeconfig;

import emufog.topology.FogNode;

import java.util.List;

public class FogNodeConfiguration extends NodeConfiguration {

    private List<FogNode> dependencies;

    public FogNodeConfiguration(){}

    public void setDependencies(List<FogNode> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(FogNode fogNode){
        dependencies.add(fogNode);
    }

    public List<FogNode> getDependencies() {
        return dependencies;
    }
}
