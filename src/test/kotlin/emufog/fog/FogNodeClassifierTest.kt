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

import emufog.config.Config
import emufog.container.FogContainer
import emufog.graph.Graph
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FogNodeClassifierTest {

    private val fogTypes1 = listOf(FogContainer("name", "tag", 1, 1F, 5, 1F))

    private val fogTypes2 = listOf(
        FogContainer("name", "big", 1024, 1F, 10, 3F),
        FogContainer("name", "small", 512, 1F, 6, 2.6F)
    )

    private val defaultConfig1: Config = mockk {
        every { maxFogNodes } returns 10
        every { baseAddress } returns "1.2.3.4"
        every { costThreshold } returns 100F
        every { fogNodeTypes } returns fogTypes1
    }

    private val defaultConfig2: Config = mockk {
        every { baseAddress } returns "1.2.3.4"
        every { maxFogNodes } returns 42
        every { fogNodeTypes } returns fogTypes2
        every { costThreshold } returns 10F
    }

    @Test
    fun `empty graph should return an empty result`() {
        val graph = Graph(defaultConfig1.baseAddress)

        val result = FogNodeClassifier(graph, defaultConfig1).findPossibleFogNodes()

        assertTrue(result.status)
        assertEquals(0, result.placements.size)
    }

    @Test
    fun `sample graph with two AS combines the partial results`() {
        val graph = createGraph(defaultConfig1)

        val result = FogNodeClassifier(graph, defaultConfig1).findPossibleFogNodes()

        assertTrue(result.status)
        assertEquals(4, result.placements.size)
    }

    @Test
    fun `if one system fails the result should be failed`() {
        val graph = Graph(defaultConfig1.baseAddress)
        val system0 = graph.getOrCreateAutonomousSystem(0)
        val system1 = graph.getOrCreateAutonomousSystem(1)
        mockkStatic("emufog.fog.FogNodeClassifierKt")
        every { findFogNodesIn(system0, any()) } returns FogResult().also { it.setSuccess() }
        every { findFogNodesIn(system1, any()) } returns FogResult().also { it.setFailure() }

        val result = FogNodeClassifier(graph, defaultConfig1).findPossibleFogNodes()

        assertFalse(result.status)
        unmockkStatic("emufog.fog.FogNodeClassifierKt")
    }

    @Test
    fun `if no more fog nodes are available the result should be failed`() {
        val config: Config = mockk {
            every { maxFogNodes } returns 1
            every { baseAddress } returns "1.2.3.4"
            every { fogNodeTypes } returns fogTypes1
            every { costThreshold } returns 100F
        }
        val graph = createGraph(config)

        val result = FogNodeClassifier(graph, config).findPossibleFogNodes()

        assertFalse(result.status)
    }

    private fun createGraph(config: Config): Graph {
        val graph = Graph(config.baseAddress)

        val system0 = graph.getOrCreateAutonomousSystem(0)
        val edge0 = graph.createEdgeNode(0, system0)
        val edge1 = graph.createEdgeNode(1, system0)
        val edge2 = graph.createEdgeNode(2, system0)
        val edge3 = graph.createEdgeNode(3, system0)
        val backbone4 = graph.createBackboneNode(4, system0)
        val backbone5 = graph.createBackboneNode(5, system0)
        val backbone6 = graph.createBackboneNode(6, system0)
        val device7 = graph.createEdgeDeviceNode(7, system0, mockk {
            every { scalingFactor } returns 4
        })
        val device8 = graph.createEdgeDeviceNode(8, system0, mockk {
            every { scalingFactor } returns 5
        })
        val device9 = graph.createEdgeDeviceNode(9, system0, mockk {
            every { scalingFactor } returns 1
        })
        val device10 = graph.createEdgeDeviceNode(10, system0, mockk {
            every { scalingFactor } returns 2
        })

        graph.createEdge(0, edge0, backbone4, 1F, 10F)
        graph.createEdge(1, edge1, backbone4, 1F, 10F)
        graph.createEdge(2, backbone5, backbone4, 1F, 10F)
        graph.createEdge(3, edge1, backbone5, 1F, 10F)
        graph.createEdge(4, edge2, backbone5, 1F, 10F)
        graph.createEdge(5, backbone6, backbone5, 1F, 10F)
        graph.createEdge(6, backbone6, edge2, 1F, 10F)
        graph.createEdge(7, backbone6, edge3, 1F, 10F)
        graph.createEdge(8, edge0, device7, 1F, 10F)
        graph.createEdge(9, edge1, device8, 1F, 10F)
        graph.createEdge(10, edge2, device9, 1F, 10F)
        graph.createEdge(11, edge3, device10, 1F, 10F)

        val system1 = graph.getOrCreateAutonomousSystem(1)
        val edge20 = graph.createEdgeNode(20, system1)
        val device21 = graph.createEdgeDeviceNode(21, system1, mockk {
            every { scalingFactor } returns 4
        })
        graph.createEdge(12, edge20, device21, 1F, 10F)

        return graph
    }

    @Test
    fun `findFogNodes on empty as should be empty`() {
        val graph = Graph(defaultConfig2.baseAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val classifier: FogNodeClassifier = mockk {
            every { config } returns defaultConfig2
        }
        val result = findFogNodesIn(system, classifier)
        assertTrue(result.status)
        assertEquals(0, result.placements.size)
    }

    @Test
    fun `findFogNodes on non device edge nodes should be empty`() {
        val graph = Graph(defaultConfig2.baseAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        graph.createEdgeNode(0, system)
        graph.createEdgeNode(1, system)
        val classifier: FogNodeClassifier = mockk {
            every { config } returns defaultConfig2
        }
        val result = findFogNodesIn(system, classifier)
        assertTrue(result.status)
        assertEquals(0, result.placements.size)
    }

    @Test
    fun `findFogNodes on sample topology #1, fully covered nodes`() {
        val graph = Graph(defaultConfig2.baseAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val edge0 = graph.createEdgeNode(0, system)
        val edge1 = graph.createEdgeNode(1, system)
        val edge2 = graph.createEdgeNode(2, system)
        val edge3 = graph.createEdgeNode(3, system)
        val backbone4 = graph.createBackboneNode(4, system)
        val backbone5 = graph.createBackboneNode(5, system)
        val backbone6 = graph.createBackboneNode(6, system)
        val device7 = graph.createEdgeDeviceNode(7, system, mockk {
            every { scalingFactor } returns 4
        })
        val device8 = graph.createEdgeDeviceNode(8, system, mockk {
            every { scalingFactor } returns 5
        })
        val device9 = graph.createEdgeDeviceNode(9, system, mockk {
            every { scalingFactor } returns 1
        })
        val device10 = graph.createEdgeDeviceNode(10, system, mockk {
            every { scalingFactor } returns 2
        })

        graph.createEdge(0, edge0, backbone4, 1F, 10F)
        graph.createEdge(1, edge1, backbone4, 1F, 10F)
        graph.createEdge(2, backbone5, backbone4, 1F, 10F)
        graph.createEdge(3, edge1, backbone5, 1F, 10F)
        graph.createEdge(4, edge2, backbone5, 1F, 10F)
        graph.createEdge(5, backbone6, backbone5, 1F, 10F)
        graph.createEdge(6, backbone6, edge2, 1F, 10F)
        graph.createEdge(7, backbone6, edge3, 1F, 10F)
        graph.createEdge(8, edge0, device7, 1F, 10F)
        graph.createEdge(9, edge1, device8, 1F, 10F)
        graph.createEdge(10, edge2, device9, 1F, 10F)
        graph.createEdge(11, edge3, device10, 1F, 10F)

        val result = findFogNodesIn(system, FogNodeClassifier(graph, defaultConfig2))
        assertTrue(result.status)
        assertEquals(2, result.placements.size)
        assertEquals(fogTypes2[0], result.placements[0].type)
        assertEquals(edge1, result.placements[0].node)
        assertEquals(fogTypes2[1], result.placements[1].type)
        assertEquals(edge3, result.placements[1].node)
    }
}