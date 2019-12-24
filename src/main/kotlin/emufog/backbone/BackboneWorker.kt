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
 */
internal object BackboneWorker {

    private val LOG = LoggerFactory.getLogger(BackboneWorker::class.java)

    /**
     * percentage of the average degree to compare to
     */
    private const val BACKBONE_DEGREE_PERCENTAGE = 0.6f

    internal fun identifyBackbone(system: AS) {
        //2nd step
        var start = System.nanoTime()
        convertHighDegrees(system)
        LOG.info("{} Step 2 - Time: {}", system, formatTimeInterval(start, System.nanoTime()))
        LOG.info("{} Backbone Size: {}", system, system.backboneNodes.size)
        LOG.info("{} Edge Size: {}", system, system.edgeNodes.size)

        // 3rd step
        start = System.nanoTime()
        BackboneConnector(system).connectBackbone()
        LOG.info("{} Step 3 - Time: {}", system, formatTimeInterval(start, System.nanoTime()))
        LOG.info("{} Backbone Size: {}", system, system.backboneNodes.size)
        LOG.info("{} Edge Size: {}", system, system.edgeNodes.size)
    }

    /**
     * Converts nodes with an above average degree to a backbone node.
     */
    private fun convertHighDegrees(system: AS) {
        val averageDegree = calculateAverageDegree(system) * BACKBONE_DEGREE_PERCENTAGE
        system.edgeNodes
            .filter { it.degree >= averageDegree }
            .forEach { it.toBackboneNode() }
    }

    /**
     * Returns the average degree of the autonomous system based on the router and switch nodes.
     *
     * @return the average degree
     */
    private fun calculateAverageDegree(system: AS): Double {
        var average = 0.0
        var count = 0
        val lambda = { n: Node ->
            Unit
            count ++
            average += (n.degree - average) / count
        }

        system.edgeNodes.forEach { lambda(it) }
        system.backboneNodes.forEach { lambda(it) }

        return average
    }
}

private class BackboneConnector(private val system: AS) {

    private val visited = BitSet()

    private val seen = BitSet()

    private val queue: Queue<Node> = ArrayDeque()

    private val predecessors: MutableMap<Node, Node?> = hashMapOf()

    private fun Node?.isType(type: NodeType) = this != null && this.type == type

    /**
     * Creates a single connected backbone by using the Breadth-First-Algorithm.
     */
    internal fun connectBackbone() {
        val backboneNodes = system.backboneNodes
        if (backboneNodes.isEmpty()) {
            return
        }

        // start with any backbone node
        val node = backboneNodes.first()
        predecessors[node] = null
        queue.add(node)


        while (! queue.isEmpty()) {
            processNode(queue.poll())
        }
    }

    private fun processNode(node: Node) {
        if (visited[node.id]) {
            return
        }
        visited.set(node.id)

        // follow a trace via the predecessor to convert all on this way
        if (node.isType(BACKBONE_NODE) && predecessors[node].isType(EDGE_NODE)) {
            connectTwoBackbones(node)
        }

        // add or update neighborhood
        node.edges
            .filterNot { it.isCrossASEdge() }
            .map { it.getDestinationForSource(node) }
            .filterNot { it == null || visited[it.id] }
            .forEach { updateNeighborNode(it !!, node) }
    }

    private fun updateNeighborNode(neighbor: Node, node: Node) {
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

    private fun connectTwoBackbones(node: Node) {
        var predecessor = predecessors[node]
        while (predecessor.isType(EDGE_NODE)) {
            predecessor.toBackboneNode()
            predecessor = predecessors[predecessor]
        }
    }
}
