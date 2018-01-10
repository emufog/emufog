package emufog.nodeconfig;

import java.util.List;

public class FogNodeConfiguration extends NodeConfiguration {

    //TODO: Change Type to correct Fog Node datatype.
    private List<String> dependencies;

    public FogNodeConfiguration(String IP) {
        super(IP);
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(String fogNode){
        dependencies.add(fogNode);
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}
