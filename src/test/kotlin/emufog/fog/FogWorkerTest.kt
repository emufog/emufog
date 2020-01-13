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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FogWorkerTest {

    private val fogTypes = listOf(
        FogContainer("name", "big", 1024, 1F, 10, 3F),
        FogContainer("name", "small", 512, 1F, 6, 2.6F)
    )

    private val defaultConfig: Config = mockk {
        every { baseAddress } returns "1.2.3.4"
        every { maxFogNodes } returns 42
        every { fogNodeTypes } returns fogTypes
        every { costThreshold } returns 10F
    }

    @Test
    fun `findFogNodes on empty as should be empty`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(0)
        val classifier: FogNodeClassifier = mockk {
            every { config } returns defaultConfig
        }
        val worker = FogWorker(system, classifier)
        val result = worker.findFogNodes()
        assertTrue(result.status)
        assertEquals(0, result.placements.size)
    }

    @Test
    fun `findFogNodes on non device edge nodes should be empty`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(0)
        graph.createEdgeNode(0, system)
        graph.createEdgeNode(1, system)
        val classifier: FogNodeClassifier = mockk {
            every { config } returns defaultConfig
        }
        val worker = FogWorker(system, classifier)
        val result = worker.findFogNodes()
        assertTrue(result.status)
        assertEquals(0, result.placements.size)
    }

    @Test
    fun `findFogNodes on sample topology #1, fully covered nodes`() {
        val graph = Graph(defaultConfig)
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

        val worker = FogWorker(system, FogNodeClassifier(graph))
        val result = worker.findFogNodes()
        assertTrue(result.status)
        assertEquals(2, result.placements.size)
        assertEquals(fogTypes[0], result.placements[0].type)
        assertEquals(edge1, result.placements[0].node)
        assertEquals(fogTypes[1], result.placements[1].type)
        assertEquals(edge3, result.placements[1].node)
    }
}