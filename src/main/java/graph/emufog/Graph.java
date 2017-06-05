package graph.emufog;

import docker.emufog.DeviceType;
import settings.emufog.Settings;
import util.emufog.Logger;
import util.emufog.LoggerLevel;

import java.util.*;

/**
 * The graph represents the topology of the network.
 */
public class Graph {

    /* list of all edges in the graph */
    private final List<Edge> edges;

    /* mapping of autonomous systems by their unique ID */
    private final Map<Integer, AS> systems;

    /* mapping of node ID to AS ID */
    private final Map<Integer, Integer> nodes;

    /* settings associated with the graph */
    private final Settings settings;

    /* provider of unique IP addresses for emulation */
    private final UniqueIPProvider IPprovider;

    /* provider of unique node IDs */
    private final UniqueIDProvider nodeIDprovider;

    /* provider of unique edge IDs */
    private final UniqueIDProvider edgeIDprovider;

    /* logger instance to log warnings */
    private final Logger logger;

    /**
     * Creates a new basic graph instance.
     * Uses the given settings for the classification algorithms.
     *
     * @param settings settings to use for the graph
     * @throws IllegalArgumentException if the settings object is null
     */
    public Graph(Settings settings) throws IllegalArgumentException {
        if (settings == null) {
            throw new IllegalArgumentException("Unable to initialize the graph object without settings.");
        }

        edges = new ArrayList<>();
        systems = new HashMap<>();
        nodes = new HashMap<>();
        this.settings = settings;
        IPprovider = new UniqueIPProvider(settings);
        nodeIDprovider = new UniqueIDProvider();
        edgeIDprovider = new UniqueIDProvider();
        logger = Logger.getInstance();
    }

    /**
     * Returns the autonomous system instance mapped to the given id or create the new AS initially.
     *
     * @param id id of the AS to seek
     * @return the AS associated or a new instance
     */
    private AS getAS(int id) {
        return systems.computeIfAbsent(id, AS::new);
    }

    /**
     * Creates a new router in the graph
     *
     * @param id unique identifier
     * @param as autonomous system the belongs to
     * @return the newly created router
     * @throws IllegalArgumentException throws an exception if the ID is already in use
     */
    public Router createRouter(int id, int as) throws IllegalArgumentException {
        checkNodeID(id);

        Router router = new Router(id, getAS(as));
        nodeIDprovider.markIDused(id);
        nodes.put(id, as);

        return router;
    }

    /**
     * Creates a new switch in the graph.
     *
     * @param id unique identifier
     * @param as autonomous system the belongs to
     * @return the newly created switch
     * @throws IllegalArgumentException throws an exception if the ID is already in use
     */
    public Switch createSwitch(int id, int as) throws IllegalArgumentException {
        checkNodeID(id);

        Switch aSwitch = new Switch(id, getAS(as));
        nodeIDprovider.markIDused(id);
        nodes.put(id, as);

        return aSwitch;
    }

    /**
     * Creates a new host device in the graph.
     *
     * @param id    unique identifier
     * @param as    autonomous system the device belongs to
     * @param image docker image to use for the host device
     * @return the newly created host device
     * @throws IllegalArgumentException if the id already in use or the image object is null
     */
    public HostDevice createHostDevice(int id, int as, DeviceType image) throws IllegalArgumentException {
        checkNodeID(id);
        if (image == null) {
            throw new IllegalArgumentException("The given docker image is not initialized.");
        }

        EmulationSettings emulationSettings = new EmulationSettings(IPprovider.getNextIPV4Address(), image);
        HostDevice device = new HostDevice(id, getAS(as), emulationSettings);
        nodeIDprovider.markIDused(id);
        nodes.put(id, as);

        return device;
    }

    /**
     * Check the node's ID if it's already in use.
     * Throws an exception if the ID is used before.
     *
     * @param id ID to check
     * @throws IllegalArgumentException if the ID is already in use
     */
    private void checkNodeID(int id) throws IllegalArgumentException {
        if (nodeIDprovider.isUsed(id)) {
            throw new IllegalArgumentException("The ID: " + id + " is already in use.");
        }
    }

    /**
     * Creates a new edge using the given latency and bandwidth.
     * If there are no coordinates associated the method is unable to create a new edge.
     *
     * @param id        unique id of the edge
     * @param from      1st end of the edge
     * @param to        2nd end of the edge
     * @param delay     delay of the edge
     * @param bandwidth bandwidth of the edge
     * @return the newly created edge
     * @throws IllegalArgumentException if any of the objects is null or the nodes are not
     *                                  associated with coordinates
     */
    public Edge createEdge(int id, Node from, Node to, float delay, float bandwidth) throws IllegalArgumentException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("The source and destination nodes cannot be null.");
        }

        if (edgeIDprovider.isUsed(id)) {
            logger.log("The edge ID: " + id + " is already in use", LoggerLevel.WARNING);
            id = edgeIDprovider.getNextID();
            logger.log("Assigning new edge ID: " + id, LoggerLevel.WARNING);
        }

        Edge edge = new Edge(id, from, to, delay, bandwidth);
        edgeIDprovider.markIDused(id);
        edges.add(edge);

        if (from.as.equals(to.as)) {
            from.as.incrementDegree();
            to.as.incrementDegree();
        }

        return edge;
    }

    /**
     * Assigns the devices specified in the settings to the edge nodes on a random base.
     */
    public void assignEdgeDevices() {
        Random random = new Random();

        for (DeviceType type : settings.deviceNodeTypes) {
            float upper = Math.abs(type.averageDeviceCount) * 2;

            for (Router r : getRouters()) {
                // random distribution within the interval from 0 to count * 2
                int count = (int) (random.nextFloat() * upper);

                for (int i = 0; i < count; ++i) {
                    HostDevice device = createHostDevice(nodeIDprovider.getNextID(), r.as.id, type);

                    createEdge(edgeIDprovider.getNextID(), r, device, settings.edgeDeviceDelay,
                            settings.edgeDeviceBandwidth);
                }
            }
        }
    }

    /**
     * Returns the settings the graph object uses.
     *
     * @return settings object
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Returns all nodes of the graph.
     *
     * @return nodes of the graph
     */
    public Collection<Node> getNodes() {
        //TODO
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(getRouters());
        nodes.addAll(getSwitches());
        nodes.addAll(getHostDevices());

        return nodes;
    }

    /**
     * Returns the host device with the given identifier.
     *
     * @param id identifier of the host device
     * @return host device with the given ID
     */
    public HostDevice getHostDevice(int id) {
        return nodes.containsKey(id) ? getASforNode(id).getDevice(id) : null;
    }

    /**
     * Returns the switch with the given identifier.
     *
     * @param id identifier of the switch
     * @return switch with the given ID
     */
    public Switch getSwitch(int id) {
        return nodes.containsKey(id) ? getASforNode(id).getSwitch(id) : null;
    }

    /**
     * Returns the router with the given identifier.
     *
     * @param id identifier of the router
     * @return router with the given ID
     */
    public Router getRouter(int id) {
        return nodes.containsKey(id) ? getASforNode(id).getRouter(id) : null;
    }

    /**
     * Returns the AS instance the node is part of.
     *
     * @param id ID of the node
     * @return node's AS
     */
    private AS getASforNode(int id) {
        return getAS(nodes.get(id));
    }

    /**
     * Returns all edges of the graph.
     *
     * @return edges of the graph
     */
    public Collection<Edge> getEdges() {
        return edges;
    }

    /**
     * Returns all host devices of the graph.
     *
     * @return host devices of the graph
     */
    public Collection<HostDevice> getHostDevices() {
        List<HostDevice> devices = new ArrayList<>();

        for (AS as : systems.values()) {
            devices.addAll(as.getDevices());
        }

        return devices;
    }

    /**
     * Returns all the routers of the graph.
     *
     * @return routers of the graph
     */
    public Collection<Router> getRouters() {
        List<Router> routers = new ArrayList<>();

        for (AS as : systems.values()) {
            routers.addAll(as.getRouters());
        }

        return routers;
    }

    /**
     * Returns all the switches of the graph.
     *
     * @return switches of the graph
     */
    public Collection<Switch> getSwitches() {
        List<Switch> switches = new ArrayList<>();

        for (AS as : systems.values()) {
            switches.addAll(as.getSwitches());
        }

        return switches;
    }

    /**
     * Returns a set of all autonomous system identifiers.
     *
     * @return set of AS IDs
     */
    public Set<Integer> getASIdentifiers() {
        return systems.keySet();
    }

    /**
     * Returns all autonomous systems of the graph.
     *
     * @return all autonomous systems
     */
    public Collection<AS> getSystems() {
        return systems.values();
    }
}