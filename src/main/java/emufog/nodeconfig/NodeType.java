package emufog.nodeconfig;

abstract class NodeType {

    private int memoryLimit;
    private int cpuShare;

    public NodeType(int memoryLimit, int cpuShare) {
        this.memoryLimit = memoryLimit;
        this.cpuShare = cpuShare;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public int getCpuShare() {
        return cpuShare;
    }
}
