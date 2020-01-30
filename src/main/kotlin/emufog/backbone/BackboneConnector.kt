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
import java.util.ArrayDeque
import java.util.BitSet
import java.util.Queue

/**
 * The backbone connector picks an arbitrary backbone node and connects it to any other backbone node it can find
 * within the given autonomous system. This way it can establish a connected backbone for the AS.
 */
internal class BackboneConnector(private val system: AS) {

    private val visited = BitSet()

    private val seen = BitSet()

    private val queue: Queue<Node> = ArrayDeque()

    private val predecessors: MutableMap<Node, Node?> = HashMap()

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


        while (queue.isNotEmpty()) {
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
            .forEach { updateNeighborNode(it!!, node) }
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
