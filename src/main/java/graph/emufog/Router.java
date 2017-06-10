package graph.emufog;

/**
 * The router class represents a node of the graph host devices can connect to.
 */
public class Router extends Node {

    /* number of devices connected to this router */
    private int deviceCount;

    /**
     * Creates a new router instance.
     *
     * @param id unique identifier
     * @param as autonomous system the belongs to
     */
    Router(int id, AS as) {
        super(id, as);

        deviceCount = 0;
    }

    @Override
    void addToAS() {
        as.addRouter(this);
    }

    /**
     * Returns indication whether this router has devices connected.
     *
     * @return true if there are devices connected, false otherwise
     */
    public boolean hasDevices() {
        return deviceCount > 0;
    }

    /**
     * Increments the device counter by one.
     */
    void incrementDeviceCount() {
        deviceCount++;
    }

    @Override
    public String getName() {
        return "router" + id;
    }
}
