package emufog.nodes;

import emufog.application.Application;

import java.util.List;

public class DeviceNode extends ApplicationNode {

    private int scalingFactor;
    private float averageDeviceCount;

    private List<Application> applications;

    /**
     * 
     * @param memoryLimit
     * @param cpuShare
     * @param scalingFactor
     * @param averageDeviceCount
     */

    public DeviceNode(int memoryLimit, int cpuShare, int scalingFactor, float averageDeviceCount) {
        super(memoryLimit, cpuShare);
        this.scalingFactor = scalingFactor;
        this.averageDeviceCount = averageDeviceCount;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }
}
