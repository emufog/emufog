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
import org.slf4j.LoggerFactory
import kotlin.math.min

/**
 * A base node tracks connections from starting nodes in the graph up to this node. Therefore all such connections are
 * stored using the predecessor on the path to this node and the overall costs. It can also be a possible placement for
 * a fog node.
 *
 * @property node the node in the original graph model
 */
internal open class BaseNode(internal val node: Node) {

    companion object {
        private val LOG = LoggerFactory.getLogger(BaseNode::class.java)
    }

    /**
     * mapping of starting nodes to their respective tuple of predecessor in the path and the overall connection costs
     */
    private val costs: MutableMap<StartingNode, Float> = HashMap()

    /**
     * indicator if the connections have been updated and therefore needs to be reevaluated
     */
    var modified = true
        protected set

    /**
     * the fog type with the optimal costs, device count ration
     */
    var type: FogContainer? = null
        private set

    /**
     * average connection costs of all associated connections
     */
    var averageConnectionCosts = 0f
        private set

    /**
     * Returns the average deployment costs for all edge nodes connected to this node
     */
    var averageDeploymentCosts: Float? = null
        private set

    var coveredNodes: List<Pair<StartingNode, Int>> = emptyList()
        private set

    /**
     * Returns all starting nodes this node is connected to
     */
    val startingNodes: Set<StartingNode>
        get() = costs.keys

    /**
     * Returns the connection costs for the given starting node. If this node is not connected to the given node
     * `null` is returned.
     *
     * @param node node to retrieve connection costs for
     * @return connection costs for the given node
     */
    fun getCosts(node: StartingNode): Float? = costs[node]

    /**
     * Sets the connection costs for a path from the given starting node to this node. Requires the predecessor on the
     * path and the connection costs.
     *
     * @param node starting node in the connection path
     * @param costs connection costs
     */
    fun setCosts(node: StartingNode, costs: Float) {
        this.costs[node] = costs
        node.addPossibleNode(this)
    }

    /**
     * Returns whether there are connections to this node are available or not.
     *
     * @return `true` if connections are available, `false` otherwise
     */
    fun hasConnections(): Boolean = costs.isNotEmpty()

    /**
     * Returns a list of all covered starting nodes by this node. The list contains of a tuple of the starting node
     * that is covered and the number of devices that are covered by this node.
     *
     * @return list of covered starting node and their respective device count
     */
    private fun getCoveredStartingNodes(coveredCount: Int): List<Pair<StartingNode, Int>> {
        // sort the connections based on their connection costs in ascending order
        val startingNodes = costs.keys.sortedBy { costs[it]!! }.map { it }
        if (this is StartingNode && hasConnections() && startingNodes[0] != this) {
            val x = 5
        }
        val result = mutableListOf<Pair<StartingNode, Int>>()
        var remaining = coveredCount

        // pick starting nodes greedy that add up to coveredCount
        var i = 0
        while (i < startingNodes.size && remaining > 0) {
            val node = startingNodes[i]
            result.add(node to min(remaining, node.deviceCount))
            remaining -= node.deviceCount
            ++i
        }

        return result
    }

    /**
     * Removes the given starting node from the list of possible connections. Updates [.modified] according to the
     * outcome of the delete.
     *
     * @param node starting node to delete the connection for
     */
    internal fun removeStartingNode(node: StartingNode) {
        modified = costs.remove(node) != null || modified
    }

    /**
     * Finds a new optimal fog type for this node. Optimal solutions are calculated by the ratio of [FogContainer.costs]
     * and the number of connections to that node. Updates the [.modified] to `false` once set.
     *
     * @param fogTypes collection of possible fog types to assign
     */
    fun determineFogType(fogTypes: Collection<FogContainer>) {
        // skip reassigning on non modified nodes
        if (!modified) {
            return
        }

        type = null
        var coveredCount = 0
        var costsPerConnection = Float.MAX_VALUE
        val deviceCount = costs.keys.sumBy { it.deviceCount }

        for (fogType in fogTypes) {
            val connections = min(deviceCount, fogType.maxClients)

            if (connections != 0 && fogType.costs / connections < costsPerConnection) {
                type = fogType
                coveredCount = connections
                costsPerConnection = fogType.costs / connections
            }
        }

        checkNotNull(type) { "The node's type is null." }
        check(coveredCount > 0) { "No node is covered by this base node" }

        LOG.debug("Set the fog type for {} to {}", node, type)

        // update the fields depending on the determined type
        averageDeploymentCosts = type!!.costs / coveredCount
        coveredNodes = getCoveredStartingNodes(coveredCount)
        averageConnectionCosts = calculateAverageConnectionCosts()

        modified = false
    }

    private fun calculateAverageConnectionCosts(): Float {
        var result = 0F
        var count = 0

        coveredNodes.forEach {
            count++
            result += (getCosts(it.first)!! - result) / count
        }

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BaseNode) {
            return false
        }

        return node == other.node
    }

    override fun hashCode(): Int = node.hashCode()
}
