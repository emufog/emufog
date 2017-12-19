package emufog.nodes;

import emufog.application.Application;

import java.util.List;

public class DeviceNode extends Node{

    private int scalingFactor;
    private float averageDeviceCount;

    private List<Application> applications;

    public DeviceNode(int memoryLimit, int cpuShare, int scalingFactor, float averageDeviceCount, List<Application> applications) {
        super(memoryLimit, cpuShare);
        this.scalingFactor = scalingFactor;
        this.averageDeviceCount = averageDeviceCount;
        this.applications = applications;
    }
}
