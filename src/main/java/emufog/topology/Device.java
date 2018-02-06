package emufog.topology;

import emufog.nodeconfig.DeviceNodeConfiguration;
import emufog.nodeconfig.DeviceNodeType;

/**
 * Device Node implementation.
 * Every device has a DeviceNodeType and a DeviceNodeConfiguration.
 */
public class Device extends Node{

    private DeviceNodeType deviceNodeType;
    private DeviceNodeConfiguration configuration;

    public Device(int id, int asID, DeviceNodeType deviceNodeType) {
        super(id, asID);
        this.deviceNodeType = deviceNodeType;
    }


    @Override
    public String getName() { return "d" + getID();}

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
