package emufog.graph;

import emufog.docker.DeviceType;

/**
 * This class represents a host device. Each host device has a docker image
 * associated to run the application specific code.
 */
public class HostDevice extends Node {

    /**
     * Creates a new host device.
     * The host device must have an docker image and IP address assigned.
     *
     * @param id                unique identifier
     * @param as                autonomous system the belongs to
     * @param emulationSettings emulation node associated with the host device
     */
    HostDevice(int id, AS as, EmulationSettings emulationSettings) {
        super(id, as);

        this.emulationSettings = emulationSettings;
    }

    @Override
    void addToAS() {
        as.addDevice(this);
    }

    /**
     * Returns the docker type for this host device. The type is always a device type instance.
     *
     * @return device docker type
     */
    DeviceType getDockerType() {
        return (DeviceType) emulationSettings.dockerType;
    }

    @Override
    public String getName() {
        return "h" + id;
    }
}
