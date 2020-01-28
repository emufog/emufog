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
import emufog.container.DeviceContainer
import emufog.graph.AS
import emufog.graph.EdgeNode
import emufog.graph.Graph
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class FogGraphBuilderTest {

    @Test
    fun `empty autonomous system should return an empty list`() {
        val system: AS = mockk {
            every { edgeNodes } returns emptyList()
        }
        assertEquals(0, calculateShortestDistances(system, 10F).size)
    }

    @Test
    fun `autonomous system with edge nodes without devices should return an empty list`() {
        val nodes: List<EdgeNode> = listOf(
            mockk { every { hasDevices() } returns false },
            mockk { every { hasDevices() } returns false }
        )
        val system: AS = mockk {
            every { edgeNodes } returns nodes
        }
        assertEquals(0, calculateShortestDistances(system, 10F).size)
    }

    @Test
    fun `createFogGraph with threshold 0 should return just the edge`() {
        val system = getTestGraph()
        val fogGraph = calculateShortestDistances(system, 0F)
        assertEquals(2, fogGraph.size)

        val startingNodes = fogGraph.filterIsInstance<StartingNode>()
        assertEquals(2, startingNodes.size)

        val startingNode0 = startingNodes.first { it.node.id == 0 }
        val startingNode5 = startingNodes.first { it.node.id == 5 }

        assertEquals(0F, startingNode0.getCosts(startingNode0))
        assertNull(startingNode0.getCosts(startingNode5))

        assertEquals(0F, startingNode5.getCosts(startingNode5))
        assertNull(startingNode5.getCosts(startingNode0))
    }

    @Test
    fun `createFogGraph with threshold 1`() {
        val system = getTestGraph()
        val fogGraph = calculateShortestDistances(system, 1F)
        assertEquals(3, fogGraph.size)

        val startingNodes = fogGraph.filterIsInstance<StartingNode>()
        assertEquals(2, startingNodes.size)

        val startingNode0 = startingNodes.first { it.node.id == 0 }
        val startingNode5 = startingNodes.first { it.node.id == 5 }

        val backbone3 = fogGraph.first { it.node.id == 3 }

        assertEquals(0F, startingNode0.getCosts(startingNode0))
        assertNull(startingNode0.getCosts(startingNode5))

        assertEquals(0F, startingNode5.getCosts(startingNode5))
        assertNull(startingNode5.getCosts(startingNode0))

        assertEquals(1F, backbone3.getCosts(startingNode5))
        assertNull(backbone3.getCosts(startingNode0))
    }

    @Test
    fun `createFogGraph with threshold 3`() {
        val system = getTestGraph()
        val fogGraph = calculateShortestDistances(system, 3F)
        assertEquals(6, fogGraph.size)

        val startingNodes = fogGraph.filterIsInstance<StartingNode>()
        assertEquals(2, startingNodes.size)

        val startingNode0 = startingNodes.first { it.node.id == 0 }
        val startingNode5 = startingNodes.first { it.node.id == 5 }

        val backbone1 = fogGraph.first { it.node.id == 1 }
        val backbone2 = fogGraph.first { it.node.id == 2 }
        val backbone3 = fogGraph.first { it.node.id == 3 }
        val backbone4 = fogGraph.first { it.node.id == 4 }

        assertEquals(0F, startingNode0.getCosts(startingNode0))
        assertNull(startingNode0.getCosts(startingNode5))

        assertEquals(0F, startingNode5.getCosts(startingNode5))
        assertNull(startingNode5.getCosts(startingNode0))

        assertEquals(2F, backbone1.getCosts(startingNode0))
        assertNull(backbone1.getCosts(startingNode5))

        assertEquals(3F, backbone2.getCosts(startingNode0))
        assertEquals(3F, backbone2.getCosts(startingNode5))

        assertEquals(1F, backbone3.getCosts(startingNode5))
        assertNull(backbone3.getCosts(startingNode0))

        assertEquals(3F, backbone4.getCosts(startingNode5))
        assertNull(backbone4.getCosts(startingNode0))
    }

    @Test
    fun `createFogGraph with threshold 10`() {
        val system = getTestGraph()
        val fogGraph = calculateShortestDistances(system, 10F)
        assertEquals(7, fogGraph.size)

        val startingNodes = fogGraph.filterIsInstance<StartingNode>()
        assertEquals(2, startingNodes.size)

        val startingNode0 = startingNodes.first { it.node.id == 0 }
        val startingNode5 = startingNodes.first { it.node.id == 5 }

        val backbone1 = fogGraph.first { it.node.id == 1 }
        val backbone2 = fogGraph.first { it.node.id == 2 }
        val backbone3 = fogGraph.first { it.node.id == 3 }
        val backbone4 = fogGraph.first { it.node.id == 4 }
        val backbone6 = fogGraph.first { it.node.id == 6 }

        assertEquals(0F, startingNode0.getCosts(startingNode0))
        assertEquals(6F, startingNode0.getCosts(startingNode5))

        assertEquals(0F, startingNode5.getCosts(startingNode5))
        assertEquals(6F, startingNode5.getCosts(startingNode0))

        assertEquals(2F, backbone1.getCosts(startingNode0))
        assertEquals(4F, backbone1.getCosts(startingNode5))

        assertEquals(3F, backbone2.getCosts(startingNode0))
        assertEquals(3F, backbone2.getCosts(startingNode5))

        assertEquals(1F, backbone3.getCosts(startingNode5))
        assertEquals(5F, backbone3.getCosts(startingNode0))

        assertEquals(3F, backbone4.getCosts(startingNode5))
        assertEquals(4F, backbone4.getCosts(startingNode0))

        assertEquals(5F, backbone6.getCosts(startingNode5))
        assertEquals(5F, backbone6.getCosts(startingNode0))
    }

    @Test
    fun `cross AS edges should be ignored`() {
        val config: Config = mockk {
            every { baseAddress } returns "1.2.3.4"
        }
        val graph = Graph(config)
        val system0 = graph.getOrCreateAutonomousSystem(0)
        val system1 = graph.getOrCreateAutonomousSystem(1)
        val edgeNode0 = graph.createEdgeNode(0, system0)
        val edgeNode1 = graph.createEdgeNode(1, system1)
        graph.createEdge(0, edgeNode0, edgeNode1, 1F, 10F)
        val container = DeviceContainer("name", "tag", 1, 1F, 1, 1F)
        val device2 = graph.createEdgeDeviceNode(2, system0, container)
        graph.createEdge(1, edgeNode0, device2, 1F, 10F)

        val nodes = calculateShortestDistances(system0, 10F)

        assertEquals(1, nodes.size)
        assertEquals(edgeNode0, nodes.first().node)
    }

    private fun getTestGraph(): AS {
        val config: Config = mockk {
            every { baseAddress } returns "1.2.3.4"
        }
        val graph = Graph(config)
        val system = graph.getOrCreateAutonomousSystem(0)
        val edge0 = graph.createEdgeNode(0, system)
        val edge5 = graph.createEdgeNode(5, system)
        val backbone1 = graph.createBackboneNode(1, system)
        val backbone2 = graph.createBackboneNode(2, system)
        val backbone3 = graph.createBackboneNode(3, system)
        val backbone4 = graph.createBackboneNode(4, system)
        val backbone6 = graph.createBackboneNode(6, system)

        val container = DeviceContainer("name", "tag", 1, 1F, 1, 1F)
        val device7 = graph.createEdgeDeviceNode(7, system, container)
        val device8 = graph.createEdgeDeviceNode(8, system, container)

        graph.createEdge(0, device7, edge0, 0F, 1F)
        graph.createEdge(1, edge0, backbone1, 2F, 1F)
        graph.createEdge(2, backbone1, backbone2, 1F, 1F)
        graph.createEdge(3, backbone2, backbone6, 2F, 1F)
        graph.createEdge(4, backbone2, backbone3, 2F, 1F)
        graph.createEdge(5, backbone2, backbone4, 1F, 1F)
        graph.createEdge(6, backbone4, edge5, 3F, 1F)
        graph.createEdge(7, backbone3, edge5, 1F, 1F)
        graph.createEdge(8, device8, edge5, 0F, 1F)

        return system
    }
}