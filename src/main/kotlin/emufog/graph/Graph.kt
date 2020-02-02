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
import emufog.util.IDManager
import kotlin.math.abs
import kotlin.random.Random

/**
 * The graph represents the overall topology of the network. A graph contains of multiple autonomous systems of [AS]
 * and their respective nodes. Nodes can be connected via edges.
 *
 * @property baseAddress the base address of the IPv4 space to start assigning IPs
 * @property edges list of all edges in the graph
 * @property systems set of all autonomous systems
 * @property hostDevices all host devices of the graph
 * @property edgeNodes all edge nodes of the graph
 * @property backboneNodes all backbone nodes of the graph
 * @property nodes all nodes of the graph
 */
class Graph(val baseAddress: String) {

    private val edgesMutable: MutableList<Edge> = ArrayList()

    val edges: List<Edge>
        get() = edgesMutable

    private val systemsMutable: MutableSet<AS> = HashSet()

    val systems: Set<AS>
        get() = systemsMutable

    /**
     * provider of unique IP addresses for emulation
     */
    private val ipManager = IPManager(baseAddress)

    /**
     * provider of unique node IDs
     */
    private val nodeIdManager = IDManager()

    /**
     * provider of unique edge IDs
     */
    private val edgeIdManager = IDManager()

    val hostDevices: List<EdgeDeviceNode>
        get() = systems.map { it.edgeDeviceNodes }.flatMap { it.toList() }

    val edgeNodes: List<EdgeNode>
        get() = systems.map { it.edgeNodes }.flatMap { it.toList() }

    val backboneNodes: List<BackboneNode>
        get() = systems.map { it.backboneNodes }.flatMap { it.toList() }

    val nodes: Set<Node>
        get() = edgeNodes.union(backboneNodes).union(hostDevices)

    /**
     * Returns the edge with the given id or `null` if not in the graph.
     */
    fun getEdge(id: Int): Edge? = edgesMutable.firstOrNull { it.id == id }

    /**
     * Returns the edge device node with the given identifier or `null` if not in the graph.
     *
     * @param id identifier of the edge device node
     */
    fun getEdgeDeviceNode(id: Int): EdgeDeviceNode? {
        return systemsMutable.mapNotNull { it.getEdgeDeviceNode(id) }.firstOrNull()
    }

    /**
     * Returns the backbone node with the given identifier or `null` if not in the graph.
     *
     * @param id identifier of the backbone node
     */
    fun getBackboneNode(id: Int): BackboneNode? {
        return systemsMutable.mapNotNull { it.getBackboneNode(id) }.firstOrNull()
    }

    /**
     * Returns the edge node with the given identifier or `null` if not in the graph.
     *
     * @param id identifier of the edge node
     */
    fun getEdgeNode(id: Int): EdgeNode? {
        return systemsMutable.mapNotNull { it.getEdgeNode(id) }.firstOrNull()
    }

    /**
     * Returns the autonomous system with the given id or `null` if it's nor present.
     *
     * @param id id to query for
     * @return autonomous system or `null` if not present
     */
    fun getAutonomousSystem(id: Int): AS? = systemsMutable.firstOrNull { it.id == id }

    /**
     * Gets or creates a new autonomous system with the given id in the graph.
     *
     * @param id unique id of the as
     * @return autonomous system with the id
     */
    fun getOrCreateAutonomousSystem(id: Int): AS = getAutonomousSystem(id) ?: AS(id).also { systemsMutable.add(it) }

    /**
     * Creates a new edge node in the graph.
     *
     * @param id unique identifier
     * @param system autonomous system the edge node belongs to
     * @return the newly created edge node
     * @throws IllegalArgumentException if id is already in use or system not part of the graph
     */
    fun createEdgeNode(id: Int, system: AS): EdgeNode {
        validateCreateInput(id, system)

        return system.createEdgeNode(id)
    }

    /**
     * Creates a new backbone node in the graph.
     *
     * @param id unique identifier
     * @param system autonomous system the backbone node belongs to
     * @return the newly created backbone node
     * @throws IllegalArgumentException if id is already in use or system not part of the graph
     */
    fun createBackboneNode(id: Int, system: AS): BackboneNode {
        validateCreateInput(id, system)

        return system.createBackboneNode(id)
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
    fun createEdgeDeviceNode(id: Int, system: AS, image: DeviceContainer): EdgeDeviceNode {
        validateCreateInput(id, system)

        return system.createEdgeDeviceNode(id, EdgeEmulationNode(ipManager.nextIPV4Address(), image))
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
     * @throws IllegalArgumentException if id is already in use, the nodes are not in the graph or an edge device node
     * is connected to a non edge node
     */
    fun createEdge(id: Int, from: Node, to: Node, delay: Float, bandwidth: Float): Edge {
        require(!edgeIdManager.isUsed(id)) { "The edge id: $id is already in use." }
        validateNodeInGraph(from)
        validateNodeInGraph(to)
        // edge devices only connect to the edge
        if ((from is EdgeDeviceNode && to !is EdgeNode) || (to is EdgeDeviceNode && from !is EdgeNode)) {
            throw IllegalArgumentException("A device node can only connect to an edge node.")
        }

        val edge = Edge(id, from, to, delay, bandwidth)
        edgeIdManager.setUsed(id)
        from.addEdge(edge)
        to.addEdge(edge)
        edgesMutable.add(edge)

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
    fun assignEdgeDevices(config: Config) {
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
     * @param container fog type to set the node to
     */
    fun placeFogNode(node: Node, container: FogContainer) {
        validateNodeInGraph(node)

        node.setEmulationNode(EmulationNode(ipManager.nextIPV4Address(), container))
    }

    /**
     * Validates if the node's autonomous system is part of the graph and if that system contains the node.
     *
     * @param node node to check for
     * @throws IllegalArgumentException if the node and the node's system are not valid
     */
    private fun validateNodeInGraph(node: Node) {
        require(systems.contains(node.system)) {
            "The node's: ${node.id} autonomous system: ${node.system.id} is not part of the graph."
        }
        require(node.system.containsNode(node)) { "The node: ${node.id} is not part ot the graph." }
    }

    /**
     * Validates if the given id is is not already in use in [nodeIdManager]. If not sets it as used. Also validates if
     * the given [AS] is part of the graph.
     *
     * @param id id to validate and set
     * @param system autonomous system to validate
     * @throws IllegalArgumentException thrown if the id already in use or as is not part of the graph
     */
    private fun validateCreateInput(id: Int, system: AS) {
        require(systems.contains(system)) { "The as: ${system.id} is not part of the graph." }
        require(!nodeIdManager.isUsed(id)) { "The node ID: $id is already in use." }

        nodeIdManager.setUsed(id)
    }
}