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

    /**
     * list of all edges in the graph
     */
    private val edgesMutable: MutableList<Edge>

    val edges: List<Edge>
        get()=edgesMutable

    /**
     * list of all autonomous systems
     */
    private val systemsMutable: MutableList<AS>

    val systems: List<AS>
        get()=systemsMutable

    /**
     * provider of unique IP addresses for emulation
     */
    private val ipManager: IPManager=IPManager(config)

    /**
     * provider of unique node IDs
     */
    private val nodeIdManager: IDManager=IDManager()

    /**
     * provider of unique edge IDs
     */
    private val edgeIdManager: IDManager=IDManager()

    /**
     * Returns all host devices of the graph.
     *
     * @return host devices of the graph
     */
    val hostDevices: List<EdgeDeviceNode>
        get()=systems.map { it.edgeDeviceNodes }.flatMap { it.toList() }

    /**
     * Returns all the edge nodes of the graph.
     *
     * @return edge nodes of the graph
     */
    val edgeNodes: List<EdgeNode>
        get()=systems.map { it.edgeNodes }.flatMap { it.toList() }

    /**
     * Returns all the backbone nodes of the graph.
     *
     * @return backbone nodes of the graph
     */
    val backboneNodes: List<BackboneNode>
        get()=systems.map { it.backboneNodes }.flatMap { it.toList() }

    /**
     * Returns all nodes of the graph.
     *
     * @return nodes of the graph
     */
    val nodes: Set<Node>
        get()=edgeNodes.union(backboneNodes).union(hostDevices)

    /**
     * Returns the edge device node with the given identifier.
     *
     * @param id identifier of the edge device node
     * @return edge device node with the given ID
     */
    fun getEdgeDeviceNode(id: Int): EdgeDeviceNode? {
        return systemsMutable.firstOrNull { it.getEdgeDeviceNode(id)!=null }?.getEdgeDeviceNode(id)
    }

    /**
     * Returns the backbone node with the given identifier.
     *
     * @param id identifier of the backbone node
     * @return backbone node with the given ID
     */
    fun getBackboneNode(id: Int): BackboneNode? {
        return systemsMutable.firstOrNull { it.getBackboneNode(id)!=null }?.getBackboneNode(id)
    }

    /**
     * Returns the edge node with the given identifier.
     *
     * @param id identifier of the edge node
     * @return edge node with the given ID
     */
    fun getEdgeNode(id: Int): EdgeNode? {
        return systemsMutable.firstOrNull { it.getEdgeNode(id)!=null }?.getEdgeNode(id)
    }

    /**
     * Returns the autonomous system with the given id or `null`
     * if it's nor present.
     *
     * @param id id to query for
     * @return autonomous system or `null` if not present
     */
    fun getAutonomousSystem(id: Int): AS? {
        return systemsMutable.firstOrNull { it.id==id }
    }

    /**
     * Gets or creates a new autonomous system with the given id in the graph.
     *
     * @param id unique id of the as
     * @return autonomous system with the id
     */
    fun getOrCreateAutonomousSystem(id: Int): AS {
        var system=getAutonomousSystem(id)
        if (system!=null) {
            return system
        }

        system=AS(id)
        systemsMutable.add(system)

        return system
    }

    /**
     * Creates a new edge node in the graph
     *
     * @param id unique identifier
     * @param `as` autonomous system the edge node belongs to
     * @return the newly created edge node
     * @throws IllegalArgumentException thrown if the ID is already in use or as is `null`
     */
    @Throws(IllegalArgumentException::class)
    fun createEdgeNode(id: Int,system: AS): EdgeNode {
        validateAndMarkNodeInput(id)
        val edgeNode=EdgeNode(NodeBaseAttributes(id,system))
        system.addEdgeNode(edgeNode)

        return edgeNode
    }

    /**
     * Creates a new backbone node in the graph.
     *
     * @param id unique identifier
     * @param `as` autonomous system the backbone node belongs to
     * @return the newly created backbone node
     * @throws IllegalArgumentException thrown if the ID is already in use or as is `null`
     */
    @Throws(IllegalArgumentException::class)
    fun createBackboneNode(id: Int,system: AS): BackboneNode {
        validateAndMarkNodeInput(id)
        val backboneNode=BackboneNode(NodeBaseAttributes(id,system))
        system.addBackboneNode(backboneNode)

        return backboneNode
    }

    /**
     * Creates a new edge device in the graph.
     *
     * @param id    unique identifier
     * @param `as`    autonomous system the device belongs to
     * @param image container image to use for the edge device
     * @return the newly created edge device
     * @throws IllegalArgumentException thrown if the ID is already in use or as or image is `null`
     */
    @Throws(IllegalArgumentException::class)
    fun createEdgeDeviceNode(id: Int,system: AS,image: DeviceContainer?): EdgeDeviceNode {
        requireNotNull(image) { "The given container image is not initialized." }
        validateAndMarkNodeInput(id)
        val emulationSettings=EmulationSettings(ipManager.nextIPV4Address(),image)
        val edgeDevice=EdgeDeviceNode(NodeBaseAttributes(id,system),emulationSettings)
        system.addDevice(edgeDevice)

        return edgeDevice
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
     * @throws IllegalArgumentException if any of the objects is `null` or the nodes are not
     * associated with coordinates
     */
    @Throws(IllegalArgumentException::class)
    fun createEdge(id: Int,from: Node,to: Node,delay: Float,bandwidth: Float): Edge {
        var edgeId=id
        if (edgeIdManager.isUsed(edgeId)) {
            LOG.warn("The edge id: {} is already in use",edgeId)
            edgeId=edgeIdManager.getNextID()
            LOG.warn("Assigning new edge id: {}",edgeId)
        }
        val edge=Edge(edgeId,from,to,delay,bandwidth)
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
     * Assigns the devices specified in the config to the edge nodes on a random base.
     */
    fun assignEdgeDevices() {
        for (type in config.deviceNodeTypes) {
            val upper=abs(type.averageDeviceCount) * 2
            for (r in edgeNodes) { // random distribution within the interval from 0 to count * 2
                val count=(Random.nextFloat() * upper).toInt()
                for (i in 0 until count) {
                    val device=createEdgeDeviceNode(nodeIdManager.getNextID(),r.system,type)
                    createEdge(edgeIdManager.getNextID(),
                            r,
                            device,
                            config.hostDeviceLatency,
                            config.hostDeviceBandwidth)
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
     * @throws IllegalArgumentException if the parameters are `null`, the graph does not
     * contain the given node
     */
    @Throws(IllegalArgumentException::class)
    fun placeFogNode(node: Node,type: FogContainer) {
        require(nodeIdManager.isUsed(node.id)) { "This graph object does not contain the given node." }
        node.emulationSettings=EmulationSettings(ipManager.nextIPV4Address(),type)
    }

    /**
     * Validates if the as is not `null` and the id still available.
     * If the input is valid the function marks the given id as used.
     *
     * @param id id to validate and mark
     * @param `as` as instance to validate
     * @throws IllegalArgumentException thrown if as is `null` or the id already in use
     */
    @Throws(IllegalArgumentException::class)
    private fun validateAndMarkNodeInput(id: Int) {
        require(!nodeIdManager.isUsed(id)) { "The node ID: $id is already in use." }
        nodeIdManager.setUsed(id)
    }

    companion object {
        private val LOG=LoggerFactory.getLogger(Graph::class.java)
    }

    /**
     * Creates a new basic graph instance.
     * Uses the given config for the classification algorithms.
     *
     * @param config config to use for the graph
     */
    init {
        edgesMutable=ArrayList()
        systemsMutable=ArrayList()
    }
}