package emufog.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an autonomous system of the network graph. Hence it's a sub graph of
 * the total graph providing access to its nodes.
 */
public class AS {

    /* unique identifier of the autonomous system */
    final int id;

    /* edge degree of the autonomous system */
    private int degree;

    /* mapping of routers in the autonomous system */
    private final Map<Integer, Router> routers;

    /* mapping of switches in the autonomous system */
    private final Map<Integer, Switch> switches;

    /* mapping of devices in the autonomous system */
    private final Map<Integer, HostDevice> devices;

    /**
     * Creates a new instance
     *
     * @param id unique ID of the AS
     */
    AS(int id) {
        this.id = id;
        degree = 0;
        routers = new HashMap<>();
        switches = new HashMap<>();
        devices = new HashMap<>();
    }

    /**
     * Returns the ID of the AS.
     *
     * @return AS's ID
     */
    public int getID() {
        return id;
    }

    /**
     * Returns the degree of the autonomous system.
     *
     * @return AS'S degree
     */
    public int getDegree() {
        return degree;
    }

    /**
     * Increments the degree of the autonomous system by 1.
     */
    void incrementDegree() {
        degree++;
    }

    /**
     * Returns the router associated with the given ID from the AS.
     *
     * @param id the router's ID
     * @return node object or null if not found
     */
    Router getRouter(int id) {
        return routers.get(id);
    }

    /**
     * Returns the switch associated with the given ID from the AS.
     *
     * @param id the switch's ID
     * @return node object or null if not found
     */
    Switch getSwitch(int id) {
        return switches.get(id);
    }

    /**
     * Returns the host device associated with the given ID from the AS.
     *
     * @param id the device's ID
     * @return node object or null if not found
     */
    HostDevice getDevice(int id) {
        return devices.get(id);
    }

    /**
     * Returns all routers from the AS.
     *
     * @return routers of the AS
     */
    public Collection<Router> getRouters() {
        return routers.values();
    }

    /**
     * Returns all switches from the AS.
     *
     * @return switches of the AS
     */
    public Collection<Switch> getSwitches() {
        return switches.values();
    }

    /**
     * Returns all host devices from the AS.
     *
     * @return devices of the AS
     */
    public Collection<HostDevice> getDevices() {
        return devices.values();
    }

    /**
     * Adds a router to the AS.
     *
     * @param r router to add
     */
    void addRouter(Router r) {
        routers.put(r.id, r);
    }

    /**
     * Adds a switch to the AS.
     *
     * @param s switch to add
     */
    void addSwitch(Switch s) {
        switches.put(s.id, s);
    }

    /**
     * Adds a host device to the AS.
     *
     * @param d device to add
     */
    void addDevice(HostDevice d) {
        devices.put(d.id, d);
    }

    /**
     * Removes a node from the AS.
     *
     * @param node node to remove
     * @return true if node could be deleted, false if not
     */
    boolean removeNode(Node node) {
        boolean result = routers.remove(node.id) != null;

        if (!result) {
            result = switches.remove(node.id) != null;
        }
        if (!result) {
            result = devices.remove(node.id) != null;
        }

        return result;
    }

    @Override
    public String toString() {
        return "AS: " + id;
    }
}
