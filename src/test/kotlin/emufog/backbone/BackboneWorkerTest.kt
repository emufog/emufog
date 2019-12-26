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
package emufog.backbone

import emufog.config.Config
import emufog.graph.Graph
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class BackboneWorkerTest {

    private val config = mockk<Config> {
        every { baseAddress } returns "1.2.3.4"
    }

    @Test
    fun `identify a backbone for a line topology with one autonomous system`() {
        val n = 10
        val graph = Graph(config)
        val system = graph.getOrCreateAutonomousSystem(0)

        for (i in 0 until n) {
            val edgeNode = graph.createEdgeNode(i, system)
            if (i > 0) {
                graph.createEdge(i, edgeNode, graph.getEdgeNode(i - 1)!!, 1F, 1F)
            }
        }

        assertEquals(n, graph.edgeNodes.size)
        assertEquals(0, graph.backboneNodes.size)
        assertEquals(n - 1, graph.edges.size)

        BackboneClassifier.identifyBackbone(graph)

        assertEquals(n - 2, graph.backboneNodes.size)
        for (i in 1 until n - 1) {
            assertNotNull(graph.getBackboneNode(i))
        }
        assertEquals(2, graph.edgeNodes.size)
        assertEquals(n - 1, graph.edges.size)
    }

    @Test
    fun `identify cross as edges as backbone connections`() {
        val graph = Graph(config)
        val system0 = graph.getOrCreateAutonomousSystem(0)
        val system1 = graph.getOrCreateAutonomousSystem(1)

        val node0 = graph.createEdgeNode(0, system0)
        val node1 = graph.createEdgeNode(1, system1)
        graph.createEdge(0, node0, node1, 1F, 1000F)

        assertEquals(2, graph.edgeNodes.size)
        assertEquals(0, graph.backboneNodes.size)
        assertEquals(1, graph.edges.size)

        BackboneClassifier.identifyBackbone(graph)

        assertEquals(2, graph.backboneNodes.size)
        assertEquals(0, graph.edgeNodes.size)
        assertEquals(1, graph.edges.size)
    }

    @Test
    fun `identify a circle as no backbone connection`() {
        val graph = Graph(config)
        val system = graph.getOrCreateAutonomousSystem(0)

        val node0 = graph.createEdgeNode(0, system)
        val node1 = graph.createEdgeNode(1, system)
        val node2 = graph.createEdgeNode(2, system)
        graph.createEdge(0, node0, node1, 1F, 1000F)
        graph.createEdge(1, node2, node1, 1F, 1000F)
        graph.createEdge(2, node2, node0, 1F, 1000F)

        assertEquals(3, graph.edgeNodes.size)
        assertEquals(0, graph.backboneNodes.size)
        assertEquals(3, graph.edges.size)

        BackboneClassifier.identifyBackbone(graph)

        assertEquals(0, graph.backboneNodes.size)
        assertEquals(3, graph.edgeNodes.size)
        assertEquals(3, graph.edges.size)
    }

    @Test
    fun `the backbone should connect disjoint backbone parts`() {
        val graph = Graph(config)
        val system = graph.getOrCreateAutonomousSystem(0)

        val node0 = graph.createEdgeNode(0, system)
        val node1 = graph.createEdgeNode(1, system)
        val node2 = graph.createEdgeNode(2, system)
        val node3 = graph.createEdgeNode(3, system)
        val node4 = graph.createEdgeNode(4, system)
        val node5 = graph.createEdgeNode(5, system)
        val node6 = graph.createEdgeNode(6, system)
        val node7 = graph.createEdgeNode(7, system)
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

        assertEquals(10, graph.edgeNodes.size)
        assertEquals(0, graph.backboneNodes.size)
        assertEquals(10, graph.edges.size)

        BackboneClassifier.identifyBackbone(graph)

        assertEquals(4, graph.backboneNodes.size)
        assertNotNull(graph.getBackboneNode(2))
        assertNotNull(graph.getBackboneNode(7))
        assertNotNull(graph.getBackboneNode(1))
        assertNotNull(graph.getBackboneNode(5))
        assertEquals(6, graph.edgeNodes.size)
        assertEquals(10, graph.edges.size)
    }
}