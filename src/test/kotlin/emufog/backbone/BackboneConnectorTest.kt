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
package emufog.backbone

import emufog.graph.Graph
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class BackboneConnectorTest {

    private val defaultBaseAddress = "1.2.3.4"

    @Test
    fun `update the predecessor if necessary`() {
        val graph = Graph(defaultBaseAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val backboneNode0 = graph.createBackboneNode(0, system)
        val edgeNode1 = graph.createEdgeNode(1, system)
        val backboneNode2 = graph.createBackboneNode(2, system)
        val backboneNode3 = graph.createBackboneNode(3, system)

        graph.createEdge(0, backboneNode0, edgeNode1, 1F, 10F)
        graph.createEdge(1, backboneNode0, backboneNode2, 1F, 10F)
        graph.createEdge(2, edgeNode1, backboneNode3, 1F, 10F)
        graph.createEdge(3, backboneNode2, backboneNode3, 1F, 10F)

        BackboneConnector(system).connectBackbone()

        assertNotNull(graph.getBackboneNode(0))
        assertNotNull(graph.getBackboneNode(2))
        assertNotNull(graph.getBackboneNode(3))
        assertNotNull(graph.getEdgeNode(1))
    }

    @Test
    fun `the backbone should connect disjoint backbone parts`() {
        val graph = Graph(defaultBaseAddress)
        val system = graph.getOrCreateAutonomousSystem(0)

        val node0 = graph.createEdgeNode(0, system)
        val node1 = graph.createEdgeNode(1, system)
        val node2 = graph.createBackboneNode(2, system)
        val node3 = graph.createEdgeNode(3, system)
        val node4 = graph.createEdgeNode(4, system)
        val node5 = graph.createEdgeNode(5, system)
        val node6 = graph.createEdgeNode(6, system)
        val node7 = graph.createBackboneNode(7, system)
        val node8 = graph.createEdgeNode(8, system)
        val node9 = graph.createEdgeNode(9, system)

        val latency = 1.5F
        val bandwidth = 1000F
        graph.createEdge(0, node0, node2, latency, bandwidth)
        graph.createEdge(1, node1, node2, latency, bandwidth)
        graph.createEdge(2, node3, node2, latency, bandwidth)
        graph.createEdge(3, node4, node2, latency, bandwidth)
        graph.createEdge(4, node1, node5, latency, bandwidth)
        graph.createEdge(5, node4, node8, latency, bandwidth)
        graph.createEdge(6, node5, node7, latency, bandwidth)
        graph.createEdge(7, node6, node7, latency, bandwidth)
        graph.createEdge(8, node8, node7, latency, bandwidth)
        graph.createEdge(9, node9, node7, latency, bandwidth)

        Assertions.assertEquals(8, graph.edgeNodes.size)
        Assertions.assertEquals(2, graph.backboneNodes.size)
        Assertions.assertEquals(10, graph.edges.size)

        BackboneConnector(system).connectBackbone()

        Assertions.assertEquals(4, graph.backboneNodes.size)
        assertNotNull(graph.getBackboneNode(2))
        assertNotNull(graph.getBackboneNode(7))
        assertNotNull(graph.getBackboneNode(1))
        assertNotNull(graph.getBackboneNode(5))
        Assertions.assertEquals(6, graph.edgeNodes.size)
        Assertions.assertEquals(10, graph.edges.size)
    }
}