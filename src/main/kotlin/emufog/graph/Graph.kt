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
package emufog.graph

import emufog.config.Config
import emufog.container.DeviceContainer
import emufog.container.FogContainer
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.random.Random

/**
 * The graph represents the topology of the network.
 */
class Graph(val config: Config) {

    companion object {

        private val LOG = LoggerFactory.getLogger(Graph::class.java)
    }

    private val edgesMutable: MutableList<Edge>

    /**
     * list of all edges in the graph
     */
    val edges: List<Edge>
        get() = edgesMutable

    private val systemsMutable: MutableList<AS>

    /**
     * list of all autonomous systems
     */
    val systems: List<AS>
        get() = systemsMutable

    /**
     * provider of unique IP addresses for emulation
     */
    private val ipManager: IPManager = IPManager(config)

    /**
     * provider of unique node IDs
     */
    private val nodeIdManager: IDManager = IDManager()

    /**
     * provider of unique edge IDs
     */
    private val edgeIdManager: IDManager = IDManager()

    /**
     * all host devices of the graph.
     */
    val hostDevices: List<EdgeDeviceNode>
        get() = systems.map { it.edgeDeviceNodes }.flatMap { it.toList() }

    /**
     * all edge nodes of the graph.
     */
    val edgeNodes: List<EdgeNode>
        get() = systems.map { it.edgeNodes }.flatMap { it.toList() }

    /**
     * all backbone nodes of the graph.
     */
    val backboneNodes: List<BackboneNode>
        get() = systems.map { it.backboneNodes }.flatMap { it.toList() }

    /**
     * all nodes of the graph
     */
    val nodes: Set<Node>
        get() = edgeNodes.union(backboneNodes).union(hostDevices)

    init {
        edgesMutable = ArrayList()
        systemsMutable = ArrayList()
    }

    /**
     * Returns the edge device node with the given identifier or `null` if not in the graph.
     *
     * @param id identifier of the edge device node
     */
    fun getEdgeDeviceNode(id: Int): EdgeDeviceNode? {
        return systemsMutable.firstOrNull { it.getEdgeDeviceNode(id) != null }?.getEdgeDeviceNode(id)
    }

    /**
     * Returns the backbone node with the given identifier or `null` if not in the graph.
     *
     * @param id identifier of the backbone node
     */
    fun getBackboneNode(id: Int): BackboneNode? {
        return systemsMutable.firstOrNull { it.getBackboneNode(id) != null }?.getBackboneNode(id)
    }

    /**
     * Returns the edge node with the given identifier or `null` if not in the graph.
     *
     * @param id identifier of the edge node
     */
    fun getEdgeNode(id: Int): EdgeNode? {
        return systemsMutable.firstOrNull { it.getEdgeNode(id) != null }?.getEdgeNode(id)
    }

    /**
     * Returns the autonomous system with the given id or `null` if it's nor present.
     *
     * @param id id to query for
     * @return autonomous system or `null` if not present
     */
    fun getAutonomousSystem(id: Int): AS? {
        return systemsMutable.firstOrNull { it.id == id }
    }

    /**
     * Gets or creates a new autonomous system with the given id in the graph.
     *
     * @param id unique id of the as
     * @return autonomous system with the id
     */
    fun getOrCreateAutonomousSystem(id: Int): AS {
        var system = getAutonomousSystem(id)
        if (system == null) {
            system = AS(id)
            systemsMutable.add(system)
        }

        return system
    }

    /**
     * Creates a new edge node in the graph.
     *
     * @param id unique identifier
     * @param system autonomous system the edge node belongs to
     * @return the newly created edge node
     * @throws IllegalArgumentException if id is already in use or system not part of the graph
     */
    @Throws(IllegalArgumentException::class)
    fun createEdgeNode(id: Int, system: AS): EdgeNode {
        require(containsAS(system)) { "The as: ${system.id} is not part of the graph." }
        setNodeId(id)
        val edgeNode = EdgeNode(NodeBaseAttributes(id, system))
        system.addEdgeNode(edgeNode)

        return edgeNode
    }

    /**
     * Creates a new backbone node in the graph.
     *
     * @param id unique identifier
     * @param system autonomous system the backbone node belongs to
     * @return the newly created backbone node
     * @throws IllegalArgumentException if id is already in use or system not part of the graph
     */
    @Throws(IllegalArgumentException::class)
    fun createBackboneNode(id: Int, system: AS): BackboneNode {
        require(containsAS(system)) { "The as: ${system.id} is not part of the graph." }
        setNodeId(id)
        val backboneNode = BackboneNode(NodeBaseAttributes(id, system))
        system.addBackboneNode(backboneNode)

        return backboneNode
    }

    /**
     * Creates a new edge device in the graph.
     *
     * @param id unique identifier
     * @param system autonomous system the device belongs to
     * @param image container image to use for the edge device
     * @return the newly created edge device
     * @throws IllegalArgumentException if id is already in use or system not part of the graph
     */
    @Throws(IllegalArgumentException::class)
    fun createEdgeDeviceNode(id: Int, system: AS, image: DeviceContainer): EdgeDeviceNode {
        require(containsAS(system)) { "The as: ${system.id} is not part of the graph." }
        setNodeId(id)
        val emulationSettings = EmulationNode(ipManager.nextIPV4Address(), image)
        val edgeDevice = EdgeDeviceNode(NodeBaseAttributes(id, system), emulationSettings)
        system.addDevice(edgeDevice)

        return edgeDevice
    }

    /**
     * Creates a new edge using the given latency and bandwidth. If the id is already used by another [Edge] a new id
     * will be assigned.
     *
     * @param id unique id of the edge
     * @param from first end of the edge
     * @param to second end of the edge
     * @param delay delay of the edge
     * @param bandwidth bandwidth of the edge
     * @return the newly created edge
     */
    fun createEdge(id: Int, from: Node, to: Node, delay: Float, bandwidth: Float): Edge {
        require(containsNode(from)) { "The node: ${from.id} is not part of this graph." }
        require(containsNode(to)) { "The node: ${to.id} is not part of this graph." }

        var edgeId = id
        if (edgeIdManager.isUsed(edgeId)) {
            LOG.warn("The edge id: {} is already in use", edgeId)
            edgeId = edgeIdManager.getNextID()
            LOG.warn("Assigning new edge id: {}", edgeId)
        }

        val edge = Edge(edgeId, from, to, delay, bandwidth)
        edgeIdManager.setUsed(edgeId)
        edgesMutable.add(edge)

        //TODO fix scaling factor
        if (from is EdgeNode && to is EdgeDeviceNode) {
            from.incrementDeviceCount(to.containerType.scalingFactor)
        }
        if (from is EdgeDeviceNode && to is EdgeNode) {
            to.incrementDeviceCount(from.containerType.scalingFactor)
        }

        return edge
    }

    /**
     * Assigns the devices specified in the [config] to the edge nodes on a random base.
     */
    fun assignEdgeDevices() {
        config.deviceNodeTypes.forEach {
            val upper = abs(it.averageDeviceCount) * 2
            for (r in edgeNodes) { // random distribution within the interval from 0 to count * 2
                val count = (Random.nextFloat() * upper).toInt()
                for (i in 0 until count) {
                    val device = createEdgeDeviceNode(nodeIdManager.getNextID(), r.system, it)
                    createEdge(
                        edgeIdManager.getNextID(), r, device, config.hostDeviceLatency, config.hostDeviceBandwidth
                    )
                }
            }
        }
    }

    /**
     * Places a fog node in the graph's topology. The graph has to contain the given node. Also a new unique IP address
     * will be assigned.
     *
     * @param node node to place a fog node at
     * @param type fog type to set the node to
     */
    fun placeFogNode(node: Node, type: FogContainer) {
        require(containsNode(node)) { "The node: ${node.id} is not part of this graph." }
        node.emulationNode = EmulationNode(ipManager.nextIPV4Address(), type)
    }

    /**
     * Checks if this instance contains the given [AS] object in the [systems].
     *
     * @param system autonomous system of the graph
     * @return `true` if this instance contains the given as, `false` otherwise
     */
    private fun containsAS(system: AS): Boolean = systems.contains(system)

    /**
     * Checks if any of the associated autonomous systems contains the given node.
     *
     * @param node node to check for
     * @return `true` if the node is part of the graph, `false` otherwise
     */
    private fun containsNode(node: Node): Boolean = systems.firstOrNull { it.containsNode(node) } != null

    /**
     * Validates if the given is is not already in use in [nodeIdManager]. If not sets it as used.
     *
     * @param id id to validate and set
     * @throws IllegalArgumentException thrown if the id already in use
     */
    @Throws(IllegalArgumentException::class)
    private fun setNodeId(id: Int) {
        require(!nodeIdManager.isUsed(id)) { "The node ID: $id is already in use." }
        nodeIdManager.setUsed(id)
    }
}