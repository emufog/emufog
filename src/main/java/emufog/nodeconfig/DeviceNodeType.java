package emufog.nodeconfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The DeviceNodeType models the device specific limitations
 * e.g resource limitations, scaling factor, number of devices etc.
 */
public class DeviceNodeType extends NodeType{


    private int scalingFactor;
    private float averageDeviceCount;
    private String name;


    @JsonCreator
    public DeviceNodeType(@JsonProperty("memoryLimit") int memoryLimit, @JsonProperty("cpuShare") int cpuShare, @JsonProperty("scalingFactor") int scalingFactor, @JsonProperty("nodeLatency") float nodeLatency, @JsonProperty("nodeBandwidth") float nodeBandwidth,@JsonProperty("averageDeviceCount") float averageDeviceCount, @JsonProperty("name") String name) {
        super(memoryLimit, cpuShare, nodeLatency, nodeBandwidth);
        this.scalingFactor = scalingFactor;
        this.averageDeviceCount = averageDeviceCount;
        this.name = name;
    }

    public int getScalingFactor() {
        return scalingFactor;
    }

    public float getAverageDeviceCount() {
        return averageDeviceCount;
    }

    public void setScalingFactor(int scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    public void setAverageDeviceCount(float averageDeviceCount) {
        this.averageDeviceCount = averageDeviceCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
