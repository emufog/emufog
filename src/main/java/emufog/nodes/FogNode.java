package emufog.nodes;

import emufog.application.Application;

import java.util.List;

public class FogNode extends Node {

    private int maximumConnections;
    private double costs;

    private List<Application> applications;
    private List<FogNode> dependencies;

    /**
     *
     * @param memoryLimit
     * @param cpuShare
     * @param maximumConnections
     * @param costs
     */

    public FogNode(int memoryLimit, int cpuShare, int maximumConnections, double costs) {
        super(memoryLimit, cpuShare);
        this.maximumConnections = maximumConnections;
        this.costs = costs;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public List<FogNode> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<FogNode> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(FogNode fogNode){
        dependencies.add(fogNode);
    }
}
