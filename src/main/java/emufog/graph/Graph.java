/*
 * MIT License
 *
 * Copyright (c) 2018 emufog contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package emufog.graph;

import emufog.container.DeviceType;
import emufog.container.FogType;
import emufog.settings.Settings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The graph represents the topology of the network.
 */
public class Graph {

    private static final Logger LOG = LoggerFactory.getLogger(Graph.class);

    /**
     * list of all edges in the graph
     */
    private final List<Edge> edges;

    /**
     * list of all autonomous systems
     */
    private final List<AS> systems;

    /**
     * settings associated with the graph
     */
    private final Settings settings;

    /**
     * provider of unique IP addresses for emulation
     */
    private final UniqueIPProvider IPprovider;

    /**
     * provider of unique node IDs
     */
    private final UniqueIDProvider nodeIDprovider;

    /**
     * provider of unique edge IDs
     */
    private final UniqueIDProvider edgeIDprovider;

    /**
     * Creates a new basic graph instance.
     * Uses the given settings for the classification algorithms.
     *
     * @param settings settings to use for the graph
     * @throws IllegalArgumentException if the settings object is {@code null}
     */
    public Graph(Settings settings) throws IllegalArgumentException {
        if (settings == null) {
            throw new IllegalArgumentException("Unable to initialize the graph object without settings.");
        }

        edges = new ArrayList<>();
        systems = new ArrayList<>();
        this.settings = settings;
        IPprovider = new UniqueIPProvider(settings);
        nodeIDprovider = new UniqueIDProvider();
        edgeIDprovider = new UniqueIDProvider();
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
     * Returns all autonomous systems of the graph.
     *
     * @return all autonomous systems
     */
    public List<AS> getSystems() {
        return systems;
    }

    /**
     * Returns all edges of the graph.
     *
     * @return edges of the graph
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Returns all host devices of the graph.
     *
     * @return host devices of the graph
     */
    public List<EdgeDeviceNode> getHostDevices() {
        return systems.stream().map(AS::getEdgeDeviceNodes).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * Returns all the edge nodes of the graph.
     *
     * @return edge nodes of the graph
     */
    public List<EdgeNode> getEdgeNodes() {
        return systems.stream().map(AS::getEdgeNodes).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * Returns all the backbone nodes of the graph.
     *
     * @return backbone nodes of the graph
     */
    public List<BackboneNode> getBackboneNodes() {
        return systems.stream().map(AS::getBackboneNodes).flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * Returns all nodes of the graph.
     *
     * @return nodes of the graph
     */
    public List<Node> getNodes() {
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(getEdgeNodes());
        nodes.addAll(getBackboneNodes());
        nodes.addAll(getHostDevices());

        return nodes;
    }

    /**
     * Returns the edge device node with the given identifier.
     *
     * @param id identifier of the edge device node
     * @return edge device node with the given ID
     */
    public EdgeDeviceNode getEdgeDeviceNode(int id) {
        return systems.stream().map(as -> as.getEdgeDeviceNode(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Returns the backbone node with the given identifier.
     *
     * @param id identifier of the backbone node
     * @return backbone node with the given ID
     */
    public BackboneNode getBackboneNode(int id) {
        return systems.stream().map(as -> as.getBackboneNode(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Returns the edge node with the given identifier.
     *
     * @param id identifier of the edge node
     * @return edge node with the given ID
     */
    public EdgeNode getEdgeNode(int id) {
        return systems.stream().map(as -> as.getEdgeNode(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Returns the autonomous system with the given id or {@code null}
     * if it's nor present.
     *
     * @param id id to query for
     * @return autonomous system or {@code null} if not present
     */
    public AS getAutonomousSystem(int id) {
        return systems.stream().filter(as -> as.id == id).findFirst().orElse(null);
    }

    /**
     * Gets or creates a new autonomous system with the given id in the graph.
     *
     * @param id unique id of the as
     * @return autonomous system with the id
     */
    public AS getOrCreateAutonomousSystem(int id) {
        AS as = getAutonomousSystem(id);
        if (as != null) {
            return as;
        }

        as = new AS(id);
        systems.add(as);

        return as;
    }

    /**
     * Creates a new edge node in the graph
     *
     * @param id unique identifier
     * @param as autonomous system the edge node belongs to
     * @return the newly created edge node
     * @throws IllegalArgumentException thrown if the ID is already in use or as is {@code null}
     */
    public EdgeNode createEdgeNode(int id, AS as) throws IllegalArgumentException {
        validateAndMarkNodeInput(id, as);

        EdgeNode edgeNode = new EdgeNode(id, as);
        as.addEdgeNode(edgeNode);

        return edgeNode;
    }

    /**
     * Creates a new backbone node in the graph.
     *
     * @param id unique identifier
     * @param as autonomous system the backbone node belongs to
     * @return the newly created backbone node
     * @throws IllegalArgumentException thrown if the ID is already in use or as is {@code null}
     */
    public BackboneNode createBackboneNode(int id, AS as) throws IllegalArgumentException {
        validateAndMarkNodeInput(id, as);

        BackboneNode backboneNode = new BackboneNode(id, as);
        as.addBackboneNode(backboneNode);

        return backboneNode;
    }

    /**
     * Creates a new edge device in the graph.
     *
     * @param id    unique identifier
     * @param as    autonomous system the device belongs to
     * @param image container image to use for the edge device
     * @return the newly created edge device
     * @throws IllegalArgumentException thrown if the ID is already in use or as or image is {@code null}
     */
    public EdgeDeviceNode createEdgeDeviceNode(int id, AS as, DeviceType image) throws IllegalArgumentException {
        if (image == null) {
            throw new IllegalArgumentException("The given container image is not initialized.");
        }
        validateAndMarkNodeInput(id, as);

        EmulationSettings emulationSettings = new EmulationSettings(IPprovider.getNextIPV4Address(), image);
        EdgeDeviceNode edgeDevice = new EdgeDeviceNode(id, as, emulationSettings);
        as.addDevice(edgeDevice);

        return edgeDevice;
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
     * @throws IllegalArgumentException if any of the objects is {@code null} or the nodes are not
     *                                  associated with coordinates
     */
    public Edge createEdge(int id, Node from, Node to, float delay, float bandwidth) throws IllegalArgumentException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("The source and destination nodes cannot be null.");
        }

        if (edgeIDprovider.isUsed(id)) {
            LOG.warn("The edge id: {} is already in use", id);
            id = edgeIDprovider.getNextID();
            LOG.warn("Assigning new edge id: {}", id);
        }

        Edge edge = new Edge(id, from, to, delay, bandwidth);
        edgeIDprovider.markIDused(id);
        edges.add(edge);

        //TODO fix scaling factor
        if (from instanceof EdgeNode && to instanceof EdgeDeviceNode) {
            ((EdgeNode) from).incrementDeviceCount(((EdgeDeviceNode) to).getContainerType().scalingFactor);
        }
        if (from instanceof EdgeDeviceNode && to instanceof EdgeNode) {
            ((EdgeNode) to).incrementDeviceCount(((EdgeDeviceNode) from).getContainerType().scalingFactor);
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

            for (EdgeNode r : getEdgeNodes()) {
                // random distribution within the interval from 0 to count * 2
                int count = (int) (random.nextFloat() * upper);

                for (int i = 0; i < count; ++i) {
                    EdgeDeviceNode device = createEdgeDeviceNode(nodeIDprovider.getNextID(), r.as, type);

                    createEdge(edgeIDprovider.getNextID(), r, device, settings.edgeDeviceDelay, settings.edgeDeviceBandwidth);
                }
            }
        }
    }

    /**
     * Places a fog node in the graph's topology. The graph has to contain the given node.
     * Also a new unique IP address will be assigned.
     *
     * @param node node to place a fog node at
     * @param type fog type to set the node to
     * @throws IllegalArgumentException if the parameters are {@code null}, the graph does not
     *                                  contain the given node
     */
    public void placeFogNode(Node node, FogType type) throws IllegalArgumentException {
        if (node == null) {
            throw new IllegalArgumentException("The given node is not initialized.");
        }
        if (type == null) {
            throw new IllegalArgumentException("The given fog type is not initialized.");
        }
        if (!nodeIDprovider.isUsed(node.id)) {
            throw new IllegalArgumentException("This graph object does not contain the given node.");
        }

        node.emulationSettings = new EmulationSettings(IPprovider.getNextIPV4Address(), type);
    }

    /**
     * Validates if the as is not {@code null} and the id still available.
     * If the input is valid the function marks the given id as used.
     *
     * @param id id to validate and mark
     * @param as as instance to validate
     * @throws IllegalArgumentException thrown if as is {@code null} or the id already in use
     */
    private void validateAndMarkNodeInput(int id, AS as) throws IllegalArgumentException {
        if (as == null) {
            throw new IllegalArgumentException("The autonomous system is null.");
        }
        if (nodeIDprovider.isUsed(id)) {
            throw new IllegalArgumentException("The node ID: " + id + " is already in use.");
        }

        nodeIDprovider.markIDused(id);
    }
}