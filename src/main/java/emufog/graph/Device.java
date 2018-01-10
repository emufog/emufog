package emufog.graph;

import emufog.nodeconfig.DeviceNodeConfiguration;
import emufog.nodeconfig.DeviceNodeType;

public class Device extends Node{

    private DeviceNodeType deviceNodeType;
    private DeviceNodeConfiguration configuration;


    public Device(int id, AS as, DeviceNodeType nodeType) {
        super(id, as);
    }

    public void setConfiguration(DeviceNodeConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    void addToAS() {

    }

    @Override
    public String getName() {
        return "d: " + getID();
    }

    public DeviceNodeConfiguration getConfiguration() {
        return configuration;
    }

    public DeviceNodeType getDeviceNodeType() {
        return deviceNodeType;
    }
}
