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

import emufog.graph.EdgeNode
import java.util.HashSet

/**
 * A starting node represents a starting point in the fog node placement algorithm.
 * Such a node is based on a [EdgeNode] and wraps it for the algorithm
 * execution.
 */
internal class StartingNode(node: EdgeNode) : BaseNode(node) {
    /**
     * set of all nodes that reachable from this starting node
     */
    private val reachableNodes: MutableSet<BaseNode?>
    /**
     * Returns the device count connected to the underlying [EdgeNode] remaining
     * to be covered by a fog node.
     *
     * @return device count to cover
     */
    /**
     * number of devices connected to the edge node
     */
    var deviceCount: Int
        private set

    /**
     * Decreases the number of devices to be covered by the given value `n`.
     *
     * @param n number to decrease the device count by
     */
    fun decreaseDeviceCount(n: Int) {
        deviceCount -= n
    }

    /**
     * Returns a set of all nodes reachable from this starting node given the connection
     * cost threshold in the configuration.
     *
     * @return all nodes that are reachable from this node
     */
    fun getReachableNodes(): Set<BaseNode?> {
        return reachableNodes
    }

    /**
     * Adds a node to the list of possible nodes for this edge node.
     *
     * @param node possible fog node
     */
    fun addPossibleNode(node: BaseNode?) {
        reachableNodes.add(node)
        modified = true
    }

    /**
     * Removes a fog node from the list of possible nodes if it's not available any more.
     *
     * @param node fog node to remove
     */
    fun removePossibleNode(node: BaseNode?) {
        modified = reachableNodes.remove(node)
    }

    /**
     * Notifies all possible nodes of this edge node that the node does not have to be covered any more.
     */
    fun notifyPossibleNodes() {
        for (node in reachableNodes) {
            node!!.removeStartingNode(this)
        }
    }

    /**
     * Creates a new starting node for the given edge node. The device count is
     * initialized with the edge node's device count.
     *
     * @param node edge node to create a starting node for
     */
    init {
        reachableNodes = HashSet()
        deviceCount = node.deviceCount
    }
}