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
import emufog.graph.AS
import emufog.graph.Edge
import emufog.graph.EdgeDeviceNode
import emufog.graph.EdgeNode
import emufog.graph.Node
import emufog.util.BinaryMinHeap
import emufog.util.ConversionsUtils.formatTimeInterval
import emufog.util.Heap
import org.slf4j.LoggerFactory

/**
 * This class isolates the fog node placement algorithm of one of the autonomous systems to run it independent of
 * others. Requires the autonomous system to process.
 */
internal class FogWorker(
    /**
     * the autonomous system this worker processes
     */
    private val system: AS,
    /**
     * the fog node classifier this worker is associated
     */
    private val classifier: FogNodeClassifier
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(FogWorker::class.java)
    }

    /**
     * Runs the fog node placement algorithm on the associated autonomous system and returns the overall result object.
     *
     * @return result of the fog node placement
     */
    internal fun findFogNodes(): FogResult {
        // initialize empty result set
        val result = FogResult().also { it.setSuccess() }

        val heap = FogHeap(
            FogGraphBuilder(system, classifier.config.costThreshold).createFogGraph(),
            classifier.config.fogNodeTypes
        )

        while (!heap.isEmpty()) {
            // check if there are still fog nodes left to place
            if (!classifier.fogNodesLeft()) {
                return result.also { it.setFailure() }
            }

            val start = System.nanoTime()
            // find the next fog node for the remaining starting nodes
            val fogNode = heap.getNext()
            LOG.debug("Time to find next fog node for {}: {}", system, formatTimeInterval(start, System.nanoTime()))

            // reduce the remaining fog nodes available
            classifier.reduceRemainingNodes()

            result.addPlacement(FogNodePlacement(fogNode))
        }

        return result
    }
}

private class FogHeap(baseNodes: Collection<BaseNode>, private val fogTypes: Collection<FogContainer>) {

    private val heap: Heap<BaseNode> = BinaryMinHeap(FogComparator())

    init {
        for (it in baseNodes) {
            it.determineFogType(fogTypes)
            heap.add(it)
        }
    }

    internal fun isEmpty(): Boolean = heap.isEmpty()

    internal fun getNext(): BaseNode {
        val result = heap.pop()
        checkNotNull(result) { "The heap of fog nodes is empty." }

        updateFogHeap(result)

        return result
    }

    private fun updateFogHeap(result: BaseNode) {
        val nodes = HashSet<BaseNode>()
        val toRemove = HashSet<BaseNode>()
        // get covered nodes by the fog node placement
        result.coveredNodes.forEach { it.first.decreaseDeviceCount(it.second) }
        val coveredNodes = result.coveredNodes.map { it.first }.filter { it.deviceCount <= 0 }
        toRemove.addAll(coveredNodes)
        coveredNodes.forEach {
            nodes.addAll(it.possibleNodes)
            it.removeFromPossibleNodes()
        }

        toRemove.addAll(nodes.filterNot { it.hasConnections() })
        val toUpdate = nodes.filter { !toRemove.contains(it) && it.modified }

        coveredNodes.forEach {
            check(!toUpdate.contains(it)) { "toUpdate contains a covered starting node." }
        }

        check(toRemove.plus(toUpdate).size == toRemove.size + toUpdate.size) { "there is an overlap in remove and update" }
        toRemove.forEach { heap.remove(it) }
        toUpdate.forEach {
            it.determineFogType(fogTypes)
            heap.updateElement(it)
        }
    }
}

private class FogGraphBuilder(private val system: AS, private val threshold: Float) {

    private val nodes: MutableMap<Node, BaseNode> = HashMap()

    internal fun createFogGraph(): MutableCollection<BaseNode> {
        // map edge device nodes to their respective wrappers
        system.edgeNodes
            .filter { it.hasDevices() }
            .map { createStartingNode(it) }
            .forEach { calculateConnectionCosts(it) }

        return nodes.values
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
        val heap = BinaryMinHeap(CostComparator(startingNode)).also { it.add(startingNode) }

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
            heap.updateElement(neighborNode)
        }
    }

    /**
     * Creates and returns a new starting node based on the given edge node. Adds the newly created node to the mapping
     * of nodes [.nodes]
     *
     * @param node edge node to create a starting node for
     * @return the newly created starting node
     */
    private fun createStartingNode(node: EdgeNode): StartingNode {
        val startingNode = StartingNode(node)
        nodes[node] = startingNode

        return startingNode
    }
}

/**
 * Calculates the costs for a given edge of the graph.
 *
 * @return costs of the given edge
 */
private fun Edge.getCosts(): Float {
    // currently using latency as a cost function
    return this.delay
}
