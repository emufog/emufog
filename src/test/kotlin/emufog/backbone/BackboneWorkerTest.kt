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
import emufog.container.DeviceContainer
import emufog.graph.Graph
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BackboneWorkerTest {

    private val config = mockk<Config> {
        every { baseAddress } returns "1.2.3.4"
    }

    @Test
    fun `as`() {
        val n = 10
        val graph = Graph(config)
        val system = graph.getOrCreateAutonomousSystem(0)
        for (i in 0 .. n) {
            val edgeNode = graph.createEdgeNode(i, system)
            if (i > 0) {
                graph.createEdge(i, edgeNode, graph.getEdgeNode(i - 1) !!, 1F, 1F)
            }
        }
        val container = DeviceContainer("a", "", 1, 1F, 100, 1F)
        val startNode = graph.getEdgeNode(0) !!
        var m = n
        for (i in 0 .. 5) {
            m++
            val edgeDeviceNode = graph.createEdgeDeviceNode(m, system, container)
            graph.createEdge(m, startNode, edgeDeviceNode, 1F, 1F)
        }

        val endNode = graph.getEdgeNode(n - 1) !!
        for (i in 0 .. 5) {
            m++
            val edgeDeviceNode = graph.createEdgeDeviceNode(m, system, container)
            graph.createEdge(m, edgeDeviceNode, endNode, 1F, 1F)
        }


        assertEquals(11, graph.edgeNodes.size)
        //assertEquals(2, graph.hostDevices.size)
        assertEquals(0, graph.backboneNodes.size)
        assertEquals(22, graph.edges.size)

        BackboneClassifier.identifyBackbone(graph)

        //assertEquals(2, graph.hostDevices.size)
        assertEquals(10, graph.backboneNodes.size)
        assertEquals(0, graph.edgeNodes.size)
        assertEquals(22, graph.edges.size)
    }
}