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

/**
 * A starting node represents a starting point in the fog node placement algorithm. Such a node is based on a
 * [EdgeNode] and wraps it for the algorithm execution.
 *
 * @property possibleNodes set of all nodes that reachable from this starting node
 * @property deviceCount number of devices connected to the edge node
 */
internal class StartingNode(node: EdgeNode) : BaseNode(node) {

    val possibleNodes: MutableSet<BaseNode> = HashSet()

    var deviceCount: Int = node.deviceCount
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
     * Adds a node to the list of possible nodes for this edge node.
     *
     * @param node possible fog node
     */
    fun addPossibleNode(node: BaseNode) {
        possibleNodes.add(node)
    }

    /**
     * Removes a fog node from the list of possible nodes if it's not available any more.
     *
     * @param node fog node to remove
     */
    fun removePossibleNode(node: BaseNode) {
        modified = possibleNodes.remove(node) || modified
    }

    /**
     * Notifies all possible nodes of this edge node that the node does not have to be covered any more.
     */
    fun removeFromPossibleNodes() {
        possibleNodes.forEach { it.removeStartingNode(this) }
        possibleNodes.clear()
    }
}