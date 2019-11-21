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
package emufog.backbone

import emufog.graph.AS
import emufog.graph.BackboneNodeConverter
import emufog.graph.Node
import emufog.graph.NodeType
import emufog.graph.NodeType.BACKBONE_NODE
import emufog.graph.NodeType.EDGE_NODE
import emufog.util.ConversionsUtils.formatTimeInterval
import org.slf4j.LoggerFactory
import java.util.ArrayDeque
import java.util.BitSet
import java.util.Queue

/**
 * This worker class operates on a single AS of the graph so it can used in parallel.
 * Executes the 2nd and 3rd step of the classification algorithm.
 *
 * @property system autonomous system to process
 */
internal class BackboneWorker(private val system: AS) {

    companion object {

        private val LOG = LoggerFactory.getLogger(BackboneWorker::class.java)

        /**
         * percentage of the average degree to compare to
         */
        private const val BACKBONE_DEGREE_PERCENTAGE = 0.6f

        private fun Node?.isType(type: NodeType) = this != null && this.type == type
    }

    internal fun identifyBackbone() {
        //2nd step
        var start = System.nanoTime()
        convertHighDegrees()
        LOG.info("{} Step 2 - Time: {}", system, formatTimeInterval(start, System.nanoTime()))
        LOG.info("{} Backbone Size: {}", system, system.backboneNodes.size)
        LOG.info("{} Edge Size: {}", system, system.edgeNodes.size)

        // 3rd step
        start = System.nanoTime()
        connectBackbone()
        LOG.info("{} Step 3 - Time: {}", system, formatTimeInterval(start, System.nanoTime()))
        LOG.info("{} Backbone Size: {}", system, system.backboneNodes.size)
        LOG.info("{} Edge Size: {}", system, system.edgeNodes.size)
    }

    /**
     * Converts nodes with an above average degree to a backbone node.
     */
    private fun convertHighDegrees() {
        val averageDegree = calculateAverageDegree() * BACKBONE_DEGREE_PERCENTAGE
        val toConvert = system.edgeNodes.filter { it.degree >= averageDegree }
        toConvert.forEach { BackboneNodeConverter.convertToBackbone(it) }
    }

    /**
     * Creates a single connected backbone by using the Breadth-First-Algorithm.
     */
    private fun connectBackbone() {
        val backboneNodes = system.backboneNodes
        if (backboneNodes.isEmpty()) {
            return
        }

        // bit sets to check for visited nodes and nodes in the queue
        val visited = BitSet()
        val seen = BitSet()
        val queue: Queue<Node> = ArrayDeque()
        // map nodes to their respective predecessors
        val predecessors: MutableMap<Node, Node?> = HashMap()
        // start with any backbone node
        var node: Node = backboneNodes.first()
        predecessors[node] = null
        queue.add(node)
        while (!queue.isEmpty()) {
            node = queue.poll()
            if (visited[node.id]) {
                continue
            }
            visited.set(node.id)

            // follow a trace via the predecessor to convert all on this way
            if (node.isType(BACKBONE_NODE) && predecessors[node].isType(EDGE_NODE)) {
                var predecessor = predecessors[node]
                while (predecessor.isType(EDGE_NODE)) {
                    BackboneNodeConverter.convertToBackbone(predecessor)
                    predecessor = predecessors[predecessor]
                }
            }

            // add or update neighborhood
            for (e in node.edges) {
                if (e.isCrossASEdge) {
                    continue
                }
                val neighbor = e.getDestinationForSource(node)
                // avoid visiting twice
                if (visited[neighbor.id]) {
                    continue
                }
                if (seen[neighbor.id]) {
                    // update the predecessor if necessary
                    if (node.isType(BACKBONE_NODE) && predecessors[neighbor].isType(EDGE_NODE)) {
                        predecessors[neighbor] = node
                    }
                } else {
                    // push a new node to the queue
                    predecessors[neighbor] = node
                    queue.add(neighbor)
                    seen.set(neighbor.id)
                }
            }
        }
    }

    /**
     * Returns the average degree of the autonomous system based on the router and switch nodes.
     *
     * @return the average degree
     */
    private fun calculateAverageDegree(): Double {
        val edgeNodes = system.edgeNodes
        val backboneNodes = system.backboneNodes
        val n = backboneNodes.size + edgeNodes.size
        if (n == 0) {
            return 0.0
        }

        var sum: Long = 0
        edgeNodes.forEach { sum += it.degree }
        backboneNodes.forEach { sum += it.degree }
        return sum.toDouble() / n
    }
}
