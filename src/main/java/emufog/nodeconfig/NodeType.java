package emufog.nodeconfig;

/**
 * Parent class for FogNodeType and DeviceNodeType
 * in which memoryLimit and cpuShare
 * for the Node are configured.
 */
abstract class NodeType {

    private int memoryLimit;
    private int cpuShare;

    NodeType(int memoryLimit, int cpuShare) {
        this.memoryLimit = memoryLimit;
        this.cpuShare = cpuShare;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public int getCpuShare() {
        return cpuShare;
    }

    public void setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public void setCpuShare(int cpuShare) {
        this.cpuShare = cpuShare;
    }

}
