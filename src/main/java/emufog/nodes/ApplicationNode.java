package emufog.nodes;

public abstract class ApplicationNode {

    public int memoryLimit;
    public int cpuShare;

    public ApplicationNode(int memoryLimit, int cpuShare) {
        this.memoryLimit = memoryLimit;
        this.cpuShare = cpuShare;
    }
}
