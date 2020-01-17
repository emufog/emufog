/*
 * MIT License
 *
 * Copyright (c) 2020 emufog contributors
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

import emufog.graph.AS
import emufog.graph.Edge
import emufog.graph.EdgeDeviceNode
import emufog.graph.Node
import emufog.util.Heap
import emufog.util.PriorityHeap


internal fun calculateShortestDistances(system: AS, threshold: Float): Set<BaseNode> {
    return FogGraphBuilder(system, threshold).createFogGraph()
}

/**
 * Calculates the costs for a given edge of the graph.
 *
 * @return costs of the given edge
 */
private fun Edge.getCosts(): Float {
    // currently using latency as a cost function
    return this.latency
}

internal fun <T> Heap<T>.update(element: T) {
    if (this.remove(element)) {
        this.add(element)
    }
}

private class FogGraphBuilder(private val system: AS, private val threshold: Float) {

    private val nodes: MutableMap<Node, BaseNode> = HashMap()

    internal fun createFogGraph(): Set<BaseNode> {
        // map edge device nodes to their respective wrappers
        val startingNodes = system.edgeNodes.filter { it.hasDevices() }.map { StartingNode(it) }
        startingNodes.forEach { nodes[it.node] = it }
        startingNodes.forEach { calculateConnectionCosts(it) }

        return nodes.values.toSet()
    }

    /**
     * Calculates the connection costs to all nodes that are within the cost threshold defined in the associated config
     * [.config]. To calculate the costs the function uses the dijkstra algorithm starting from the given node.
     *
     * @param startingNode node to calculate the connection costs for
     */
    private fun calculateConnectionCosts(startingNode: StartingNode) {
        // push the starting node as a starting point in the queue
        startingNode.setCosts(startingNode, 0F)
        val heap = PriorityHeap(CostComparator(startingNode)).also { it.add(startingNode) }

        // using the dijkstra algorithm to iterate the graph
        while (!heap.isEmpty()) {
            val current = heap.pop()
            checkNotNull(current) { "The heap of the Dijkstra Algorithm is empty." }

            val currentCosts = current.getCosts(startingNode)
            checkNotNull(currentCosts) { "No costs associated with this node in the graph." }

            // check all edges leaving the current node
            current.node.edges
                .filterNot { it.isCrossASEdge() }
                .forEach { processEdge(it, heap, currentCosts, current, startingNode) }
        }
    }

    private fun processEdge(
        edge: Edge,
        heap: Heap<BaseNode>,
        currentCosts: Float,
        current: BaseNode,
        startingNode: StartingNode
    ) {
        val neighbor = edge.getDestinationForSource(current.node)

        // ignore host devices as they are not considered to be possible nodes
        if (neighbor == null || neighbor is EdgeDeviceNode) {
            return
        }

        // abort on costs above the threshold
        val nextCosts = currentCosts + edge.getCosts()
        if (nextCosts > threshold) {
            return
        }

        val neighborNode = nodes.computeIfAbsent(neighbor) { BaseNode(it) }
        val neighborCosts = neighborNode.getCosts(startingNode)
        if (neighborCosts == null) {
            // newly discovered node
            neighborNode.setCosts(startingNode, nextCosts)
            heap.add(neighborNode)
        } else if (nextCosts < neighborCosts) {
            // update an already discovered node
            neighborNode.setCosts(startingNode, nextCosts)
            heap.update(neighborNode)
        }
    }
}
