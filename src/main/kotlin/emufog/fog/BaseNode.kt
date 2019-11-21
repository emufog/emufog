/*
 * MIT License
 *
 * Copyright (c) 2019 emufog contributors
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
package emufog.fog

import emufog.container.FogContainer
import emufog.graph.Node
import emufog.util.Tuple
import org.slf4j.LoggerFactory
import java.util.ArrayList
import java.util.HashMap
import java.util.stream.Collectors

/**
 * A base node tracks connections from starting nodes in the graph up to this node.
 * Therefore all such connections are stored using the predecessor on the path to this
 * node and the overall costs. It can also be a possible placement for a fog node.
 */
internal open class BaseNode(
    /**
     * the node in the original graph model
     */
    val node: Node
) {
    /**
     * mapping of starting nodes to their respective tuple of predecessor in the path
     * and the overall connection costs
     */
    private val costs: MutableMap<StartingNode?, Tuple<BaseNode, Float>>
    /**
     * indicator if the connections have been updated and therefore needs to be
     * reevaluated
     */
    var modified: Boolean
    /**
     * Returns the fog type with the currently optimal cost ratio for this node.
     *
     * @return fog type associated
     */
    /**
     * the fog type with the optimal costs, device count ration
     */
    var type: FogContainer?
        private set
    /**
     * number of devices covered by this node
     */
    private var coveredCount: Int
    /**
     * Returns the average connection costs from all connected nodes.
     *
     * @return average connection costs for this node
     */
    /**
     * average connection costs of all associated connections
     */
    var averageConnectionCosts = 0f
        private set

    /**
     * Returns the connection costs for the given starting node.
     * If this node is not connected to the given node [Float.MAX_VALUE] is returned.
     *
     * @param node node to retrieve connection costs for
     * @return connection costs for the given node
     */
    fun getCosts(node: StartingNode?): Float {
        val tuple = costs[node]
        return tuple?.value ?: Float.MAX_VALUE
    }

    /**
     * Sets the connection costs for a path from the given starting node to this node.
     * Requires the predecessor on the path and the connection costs.
     *
     * @param node        starting node in the connection path
     * @param predecessor the predecessor on the connection path
     * @param costs       connection costs
     */
    fun setCosts(node: StartingNode, predecessor: BaseNode, costs: Float) {
        this.costs[node] = Tuple(predecessor, costs)
        node.addPossibleNode(this)
    }

    /**
     * Returns all starting nodes this node is connected to.
     *
     * @return set of all starting nodes connected
     */
    val startingNodes: Set<StartingNode?>
        get() = costs.keys

    /**
     * Returns whether there are connections to this node are available or not.
     *
     * @return `true` if connections are available, `false` otherwise
     */
    fun hasConnections(): Boolean {
        return !costs.isEmpty()
    }

    /**
     * Returns the average deployment costs for all edge nodes connected to this node.
     *
     * @return average deployment costs
     */
    val averageDeploymentCosts: Float
        get() = type!!.costs / coveredCount// sort the connections based on their connection costs in ascending order
    // pick starting nodes greedy that add up to coveredCount

    /**
     * Returns a list of all covered starting nodes by this node.
     * The list contains of a tuple of the starting node that is covered
     * and the number of devices that are covered by this node.
     *
     * @return list of covered starting node and their respective device count
     */
    val coveredStartingNodes: List<Tuple<StartingNode, Int>>
        get() { // sort the connections based on their connection costs in ascending order
            val startingNodes: List<StartingNode> = costs.keys
                .stream()
                .map { k: StartingNode? ->
                    Tuple<StartingNode?, Float?>(
                        k,
                        costs[k]!!.value
                    )
                }
                .sorted { (_, value), (_, value) ->
                    java.lang.Float.compare(
                        value!!,
                        value
                    )
                }
                .map(Tuple::key)
                .collect(Collectors.toList())
            val result: MutableList<Tuple<StartingNode, Int>> = ArrayList()
            var remaining = coveredCount
            // pick starting nodes greedy that add up to coveredCount
            var i = 0
            while (i < startingNodes.size && remaining > 0) {
                val node = startingNodes[i]
                result.add(Tuple(node, Math.min(remaining, node.deviceCount)))
                remaining -= node.deviceCount
                ++i
            }
            return result
        }

    /**
     * Removes the given starting node from the list of possible connections.
     * Updates [.modified] according to the outcome of the delete.
     *
     * @param node starting node to delete the connection for
     */
    fun removeStartingNode(node: StartingNode?) {
        modified = costs.remove(node) != null
    }

    /**
     * Finds a new optimal fog type for this node. Optimal solutions are calculated
     * by the ratio of [FogContainer.costs] and the number of connections to that node.
     * Updates the [.modified] to `false` once set.
     *
     * @param fogTypes collection of possible fog types to assign
     */
    fun findFogType(fogTypes: Collection<FogContainer>) { // skip reassigning on non modified nodes
        if (!modified) {
            return
        }
        type = null
        coveredCount = 0
        var costsPerConnection = Float.MAX_VALUE
        var deviceCount = 0
        for (n in costs.keys) {
            deviceCount += n.getDeviceCount()
        }
        for (fogType in fogTypes) {
            val connections = Math.min(deviceCount, fogType.maxClients)
            if (fogType.costs / connections < costsPerConnection) {
                type = fogType
                coveredCount = connections
                costsPerConnection = type!!.costs / connections
            }
        }
        LOG.debug("Set the fog type for {} to {}", node, type)
        calculateAverageCosts()
        modified = false
    }

    /**
     * Pre-calculates the average connection costs for all possible connections and
     * sets the [.averageConnectionCosts].
     * Can be retrieved by [.getAverageConnectionCosts]
     */
    private fun calculateAverageCosts() {
        var sum = 0.0
        for ((_, value) in costs.values) {
            sum += value
        }
        averageConnectionCosts = (sum / costs.size).toFloat()
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj !is BaseNode) {
            false
        } else node == obj.node
    }

    override fun hashCode(): Int {
        return node.hashCode()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BaseNode::class.java)
    }

    /**
     * Creates a new base node based on the given node from the original graph.
     *
     * @param node underlying node from the graph
     */
    init {
        costs = HashMap()
        modified = true
        type = null
        coveredCount = 0
    }
}