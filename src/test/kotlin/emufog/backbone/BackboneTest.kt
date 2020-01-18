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
import emufog.graph.Node
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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

        identifyBackbone(graph)

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

        convertCrossAsEdges(graph.edges)

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

        identifyBackbone(graph)

        assertEquals(0, graph.backboneNodes.size)
        assertEquals(3, graph.edgeNodes.size)
        assertEquals(3, graph.edges.size)
    }

    @Test
    fun `toBackboneNode should do nothing if null`() {
        val node: Node? = null
        assertNull(node.toBackboneNode())
    }

    @Test
    fun `toBackboneNode should replace a node with a backbone node`() {
        val graph = Graph(config)
        val system = graph.getOrCreateAutonomousSystem(0)

        val node = graph.createEdgeNode(0, system)
        val backboneNode = node.toBackboneNode()
        assertNotNull(backboneNode)
        assertEquals(system, backboneNode!!.system)
        assertEquals(0, backboneNode.id)
        assertNull(graph.getEdgeNode(0))
        assertNotNull(graph.getBackboneNode(0))
    }
}