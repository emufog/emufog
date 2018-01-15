package emufog.nodeconfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FogNodeType extends NodeType{

    private int maximumConnections;
    private int id;
    private double costs;
    private String name;



    @JsonCreator
    public FogNodeType(@JsonProperty("memoryLimit") int memoryLimit, @JsonProperty("cpuShare") int cpuShare, @JsonProperty("maximumConnections") int maximumConections, @JsonProperty("id") int id, @JsonProperty("costs") double costs, @JsonProperty("name") String name) {
        super(memoryLimit, cpuShare);
        this.maximumConnections = maximumConections;
        this.id = id;
        this.costs = costs;
        this.name = name;
    }

    public int getMaximumConnections() {
        return maximumConnections;
    }

    public double getCosts() {
        return costs;
    }

    public void setMaximumConnections(int maximumConnections) {
        this.maximumConnections = maximumConnections;
    }

    public void setCosts(double costs) {
        this.costs = costs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
