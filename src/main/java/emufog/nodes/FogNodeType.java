package emufog.nodes;

public class FogNodeType extends NodeType{

    private int maximumConections;
    private double costs;


    public FogNodeType(int memoryLimit, int cpuShare, int maximumConections, double costs) {
        super(memoryLimit, cpuShare);
        this.maximumConections = maximumConections;
        this.costs = costs;
    }

    public int getMaximumConections() {
        return maximumConections;
    }

    public double getCosts() {
        return costs;
    }
}
