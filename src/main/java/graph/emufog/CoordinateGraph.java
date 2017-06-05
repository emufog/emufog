package graph.emufog;

import docker.emufog.DeviceType;
import settings.emufog.Settings;

import java.util.HashMap;
import java.util.Map;

/**
 * The graph represents the topology of the network. The coordinate graph provides the option to associate
 * the nodes with coordinates to calculate the latency based on them.
 */
public class CoordinateGraph extends Graph {

    /* mapping of node IDs to their respective coordinates */
    private final Map<Integer, NodeCoordinates> coordinates;

    /**
     * Creates a new graph using coordinates to calculate latency.
     * Uses the given settings for the classification algorithms.
     *
     * @param settings settings to use for the graph
     * @throws IllegalArgumentException if the settings object is null
     */
    public CoordinateGraph(Settings settings) throws IllegalArgumentException {
        super(settings);

        this.coordinates = new HashMap<>();
    }

    /**
     * Creates a new router in the graph
     *
     * @param id   unique identifier
     * @param as   autonomous system the router belongs to
     * @param xPos x coordinate
     * @param yPos y coordinate
     * @return the newly created router
     * @throws IllegalArgumentException throws an exception if the ID is already in use
     */
    public Router createRouter(int id, int as, float xPos, float yPos) throws IllegalArgumentException {
        coordinates.put(id, new NodeCoordinates(xPos, yPos));

        return createRouter(id, as);
    }

    /**
     * Creates a new switch in the graph.
     *
     * @param id   unique identifier
     * @param as   autonomous system the switch belongs to
     * @param xPos x coordinate
     * @param yPos y coordinate
     * @return the newly created switch
     * @throws IllegalArgumentException throws an exception if the ID is already in use
     */
    public Switch createSwitch(int id, int as, float xPos, float yPos) throws IllegalArgumentException {
        coordinates.put(id, new NodeCoordinates(xPos, yPos));

        return createSwitch(id, as);
    }

    /**
     * Creates a new host device in the graph.
     *
     * @param id    unique identifier
     * @param as    autonomous system the device belongs to
     * @param xPos  x coordinate
     * @param yPos  y coordinate
     * @param image docker image to use for the host device
     * @return the newly created host device
     * @throws IllegalArgumentException if the id already in use or the image object is null
     */
    public HostDevice createHostDevice(int id, int as, float xPos, float yPos, DeviceType image)
            throws IllegalArgumentException {
        coordinates.put(id, new NodeCoordinates(xPos, yPos));

        return createHostDevice(id, as, image);
    }

    /**
     * Creates a new edge using a latency calculator by using the coordinates passed initially.
     * If there are no coordinates associated the method is unable to create a new edge.
     *
     * @param id               unique id of the edge
     * @param from             1st end of the edge
     * @param to               2nd end of the edge
     * @param latencyEstimator latency calculator to use
     * @param bandwidth        bandwidth of the edge
     * @return the newly created edge
     * @throws IllegalArgumentException if any of the objects is null or the nodes are not
     *                                  associated with coordinates
     */
    public Edge createEdge(int id, Node from, Node to, ILatencyCalculator latencyEstimator,
                           float bandwidth) throws IllegalArgumentException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("The source and destination nodes cannot be null.");
        }
        if (latencyEstimator == null) {
            throw new IllegalArgumentException("The latency estimator object in not initialized.");
        }

        NodeCoordinates fromCoords = coordinates.get(from);
        if (fromCoords == null) {
            throw new IllegalArgumentException("The source " + from + " is not associated with coordinates.");
        }
        NodeCoordinates toCoords = coordinates.get(to);
        if (toCoords == null) {
            throw new IllegalArgumentException("The source " + to + " is not associated with coordinates.");
        }

        float delay = latencyEstimator.getLatency(fromCoords.x, fromCoords.y, toCoords.x, toCoords.y);

        return createEdge(id, from, to, delay, bandwidth);
    }

    /**
     * Removes all stored coordinates. This is optional.
     */
    public void clearCoordinates() {
        coordinates.clear();
    }
}
