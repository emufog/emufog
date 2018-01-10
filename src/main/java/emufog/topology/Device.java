package emufog.topology;

import emufog.graph.AS;
import emufog.nodeconfig.DeviceNodeConfiguration;
import emufog.nodeconfig.DeviceNodeType;

public class Device extends Node{

    private DeviceNodeType deviceNodeType;
    private DeviceNodeConfiguration configuration;

    public Device(int id, AS as, DeviceNodeType deviceNodeType) {
        super(id, as);
        this.deviceNodeType = deviceNodeType;
    }

    public DeviceNodeType getDeviceNodeType() {
        return deviceNodeType;
    }


    public DeviceNodeConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(DeviceNodeConfiguration configuration) {
        this.configuration = configuration;
    }
}
