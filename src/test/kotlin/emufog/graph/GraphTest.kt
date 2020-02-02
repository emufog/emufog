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
package emufog.graph

import emufog.container.DeviceContainer
import emufog.container.FogContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GraphTest {

    private val defaultAddress = "1.2.3.4"

    private val defaultGraph = Graph(defaultAddress)

    @Test
    fun `test the constructor`() {
        assertEquals(defaultAddress, defaultGraph.baseAddress)
        assertTrue(defaultGraph.backboneNodes.isEmpty())
        assertTrue(defaultGraph.edges.isEmpty())
        assertTrue(defaultGraph.edgeNodes.isEmpty())
        assertTrue(defaultGraph.hostDevices.isEmpty())
        assertTrue(defaultGraph.nodes.isEmpty())
        assertTrue(defaultGraph.systems.isEmpty())
    }

    @Test
    fun `getEdgeDeviceNode with a not existing id should return null`() {
        val graph = Graph(defaultAddress)
        graph.getOrCreateAutonomousSystem(1)
        graph.getOrCreateAutonomousSystem(2)
        graph.getOrCreateAutonomousSystem(3)
        assertNull(graph.getEdgeDeviceNode(42))
    }

    @Test
    fun `getBackboneNode with a not existing id should return null`() {
        val graph = Graph(defaultAddress)
        graph.getOrCreateAutonomousSystem(1)
        graph.getOrCreateAutonomousSystem(2)
        graph.getOrCreateAutonomousSystem(3)
        assertNull(graph.getBackboneNode(42))
    }

    @Test
    fun `getEdgeNode with a not existing id should return null`() {
        val graph = Graph(defaultAddress)
        graph.getOrCreateAutonomousSystem(1)
        graph.getOrCreateAutonomousSystem(2)
        graph.getOrCreateAutonomousSystem(3)
        assertNull(graph.getEdgeNode(42))
    }

    @Test
    fun `getEdgeDeviceNode with an existing id should return the node`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(1)
        val container = DeviceContainer("abc", "tag", 1, 1F, 1, 1F)
        val node = graph.createEdgeDeviceNode(42, system, container)
        assertEquals(node, graph.getEdgeDeviceNode(42))
    }

    @Test
    fun `getBackboneNode with an existing id should return null`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(1)
        val node = graph.createBackboneNode(42, system)
        assertEquals(node, graph.getBackboneNode(42))
    }

    @Test
    fun `getEdgeNode with an existing id should return null`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(1)
        val node = graph.createEdgeNode(42, system)
        assertEquals(node, graph.getEdgeNode(42))
    }

    @Test
    fun `adding an edge node to a system that is not in the graph should fail`() {
        val graph = Graph(defaultAddress)
        val system = AS(1)
        assertThrows<IllegalArgumentException> {
            graph.createEdgeNode(42, system)
        }
    }

    @Test
    fun `adding a backbone node to a system that is not in the graph should fail`() {
        val graph = Graph(defaultAddress)
        val system = AS(1)
        assertThrows<IllegalArgumentException> {
            graph.createBackboneNode(42, system)
        }
    }

    @Test
    fun `adding an edge device node to a system that is not in the graph should fail`() {
        val graph = Graph(defaultAddress)
        val system = AS(1)
        val container = DeviceContainer("abc", "tag", 1, 1F, 1, 1F)
        assertThrows<IllegalArgumentException> {
            graph.createEdgeDeviceNode(42, system, container)
        }
    }

    @Test
    fun `createEdgeNode should fail if id is already in use`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(1)
        graph.createEdgeNode(0, system)
        assertThrows<IllegalArgumentException> {
            graph.createEdgeNode(0, system)
        }
    }

    @Test
    fun `createBackboneNode should fail if id is already in use`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(1)
        graph.createBackboneNode(0, system)
        assertThrows<IllegalArgumentException> {
            graph.createBackboneNode(0, system)
        }
    }

    @Test
    fun `createEdgeDeviceNode should fail if id is already in use`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(1)
        val container = DeviceContainer("abc", "tag", 1, 1F, 1, 1F)
        graph.createEdgeDeviceNode(0, system, container)
        assertThrows<IllegalArgumentException> {
            graph.createEdgeDeviceNode(0, system, container)
        }
    }

    @Test
    fun `createEdgeNode should create a node with the params and should be retrievable`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(1)
        val node = graph.createEdgeNode(0, system)
        assertEquals(0, node.id)
        assertEquals(system, node.system)
        assertEquals(node, graph.getEdgeNode(0))
        assertTrue(graph.edgeNodes.contains(node))
        assertTrue(graph.nodes.contains(node))
    }

    @Test
    fun `createBackboneNode should create a node with the params and should be retrievable`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(1)
        val node = graph.createBackboneNode(0, system)
        assertEquals(0, node.id)
        assertEquals(system, node.system)
        assertEquals(node, graph.getBackboneNode(0))
        assertTrue(graph.backboneNodes.contains(node))
        assertTrue(graph.nodes.contains(node))
    }

    @Test
    fun `createEdgeDeviceNode should create a node with the params and should be retrievable`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(1)
        val container = DeviceContainer("abc", "tag", 1, 1F, 1, 1F)
        val node = graph.createEdgeDeviceNode(0, system, container)
        assertEquals(0, node.id)
        assertEquals(system, node.system)
        assertEquals(node, graph.getEdgeDeviceNode(0))
        assertTrue(graph.hostDevices.contains(node))
        assertTrue(graph.nodes.contains(node))
    }

    @Test
    fun `getAutonomousSystem should return null if system is not part of the graph`() {
        assertNull(defaultGraph.getAutonomousSystem(123))
    }

    @Test
    fun `getAutonomousSystem should return the resp system if it is part of the graph`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(1)
        assertEquals(system, graph.getAutonomousSystem(1))
    }

    @Test
    fun `getOrCreateAutonomousSystem should return a new system if not present`() {
        val graph = Graph(defaultAddress)
        assertNull(graph.getAutonomousSystem(1))
        assertEquals(1, graph.getOrCreateAutonomousSystem(1).id)
    }

    @Test
    fun `getOrCreateAutonomousSystem returns an already existing system`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(1)
        assertEquals(system, graph.getOrCreateAutonomousSystem(1))
    }

    @Test
    fun `createEdge should fail on negative latency`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val source = graph.createBackboneNode(0, system)
        val destination = graph.createBackboneNode(1, system)
        assertThrows<IllegalArgumentException> {
            graph.createEdge(0, source, destination, -1F, 10F)
        }
    }

    @Test
    fun `createEdge should fail on negative bandwidth`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val source = graph.createBackboneNode(0, system)
        val destination = graph.createBackboneNode(1, system)
        assertThrows<IllegalArgumentException> {
            graph.createEdge(0, source, destination, 0F, -10F)
        }
    }

    @Test
    fun `createEdge should fail if source is not in graph`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val source = BackboneNode(0, AS(42))
        val destination = graph.createBackboneNode(1, system)
        assertThrows<IllegalArgumentException> {
            graph.createEdge(0, source, destination, 0F, -10F)
        }
    }

    @Test
    fun `createEdge should fail if destination is not in graph`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val source = graph.createBackboneNode(0, system)
        val destination = BackboneNode(1, AS(42))
        assertThrows<IllegalArgumentException> {
            graph.createEdge(0, source, destination, 0F, -10F)
        }
    }

    @Test
    fun `createEdge should fail if id is already in use`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val source = graph.createBackboneNode(0, system)
        val destination = graph.createBackboneNode(1, system)
        graph.createEdge(0, source, destination, 1F, 10F)
        assertThrows<IllegalArgumentException> {
            graph.createEdge(0, source, destination, 1F, 10F)
        }
    }

    @Test
    fun `createEdge should edge device node is not connected to the edge #1`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val container = DeviceContainer("name", "tag", 1024, 1F, 1, 1F)
        val source = graph.createEdgeDeviceNode(0, system, container)
        val destination = graph.createBackboneNode(1, system)
        assertThrows<IllegalArgumentException> {
            graph.createEdge(0, source, destination, 0F, 10F)
        }
    }

    @Test
    fun `createEdge should edge device node is not connected to the edge #2`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val container = DeviceContainer("name", "tag", 1024, 1F, 1, 1F)
        val source = graph.createBackboneNode(1, system)
        val destination = graph.createEdgeDeviceNode(0, system, container)
        assertThrows<IllegalArgumentException> {
            graph.createEdge(0, source, destination, 0F, 10F)
        }
    }

    @Test
    fun `createEdge should create a new edge and update the device counter #1`() {
        val graph = Graph(defaultAddress)
        assertNull(graph.getEdge(0))
        val system = graph.getOrCreateAutonomousSystem(0)
        val container = DeviceContainer("name", "tag", 1024, 1F, 42, 1F)
        val source = graph.createEdgeDeviceNode(0, system, container)
        val destination = graph.createEdgeNode(1, system)
        val edge = graph.createEdge(0, source, destination, 0F, 10F)
        assertEquals(edge, graph.getEdge(0))
        assertEquals(42, destination.deviceCount)
    }

    @Test
    fun `createEdge should create a new edge and update the device counter #2`() {
        val graph = Graph(defaultAddress)
        assertNull(graph.getEdge(0))
        val system = graph.getOrCreateAutonomousSystem(0)
        val container = DeviceContainer("name", "tag", 1024, 1F, 42, 1F)
        val source = graph.createEdgeNode(1, system)
        val destination = graph.createEdgeDeviceNode(0, system, container)
        val edge = graph.createEdge(0, source, destination, 0F, 10F)
        assertEquals(edge, graph.getEdge(0))
        assertEquals(42, source.deviceCount)
    }

    @Test
    fun `placeFogNode should fail if node is not in graph`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val container = FogContainer("name", "tag", 1024, 1F, 2, 5F)
        assertThrows<IllegalArgumentException> {
            graph.placeFogNode(BackboneNode(1, system), container)
        }
    }

    @Test
    fun `placeFogNode sets the emulation node with the given fog container`() {
        val graph = Graph(defaultAddress)
        val system = graph.getOrCreateAutonomousSystem(0)
        val container = FogContainer("name", "tag", 1024, 1F, 2, 5F)
        val node = graph.createBackboneNode(1, system)
        assertFalse(node.hasEmulationSettings())
        graph.placeFogNode(node, container)
        assertTrue(node.hasEmulationSettings())
        val emu = node.emulationNode
        assertNotNull(emu)
        assertEquals(container, emu!!.container)
        assertEquals("1.2.3.5", emu.ip)
    }
}