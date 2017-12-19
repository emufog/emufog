package emufog.nodes;

public abstract class Node {

    public int memoryLimit;
    public int cpuShare;

    public Node(int memoryLimit, int cpuShare) {
        this.memoryLimit = memoryLimit;
        this.cpuShare = cpuShare;
    }
}
