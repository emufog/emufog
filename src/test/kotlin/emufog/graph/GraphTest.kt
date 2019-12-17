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

import emufog.config.Config
import emufog.container.DeviceContainer
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GraphTest {

    private val defaultConfig = mockk<Config> {
        every { baseAddress } returns "1.2.3.4"
    }

    private val defaultGraph = Graph(defaultConfig)

    @Test
    fun `test the constructor`() {
        assertEquals(defaultConfig, defaultGraph.config)
        assertTrue(defaultGraph.backboneNodes.isEmpty())
        assertTrue(defaultGraph.edges.isEmpty())
        assertTrue(defaultGraph.edgeNodes.isEmpty())
        assertTrue(defaultGraph.hostDevices.isEmpty())
        assertTrue(defaultGraph.nodes.isEmpty())
        assertTrue(defaultGraph.systems.isEmpty())
    }

    @Test
    fun `getEdgeDeviceNode with a not existing id should return null`() {
        assertNull(defaultGraph.getEdgeDeviceNode(42))
    }

    @Test
    fun `getBackboneNode with a not existing id should return null`() {
        assertNull(defaultGraph.getBackboneNode(42))
    }

    @Test
    fun `getEdgeNode with a not existing id should return null`() {
        assertNull(defaultGraph.getEdgeNode(42))
    }

    @Test
    fun `getEdgeDeviceNode with an existing id should return the node`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(1)
        val container = DeviceContainer("abc", "tag", 1, 1F, 1, 1F)
        val node = graph.createEdgeDeviceNode(42, system, container)
        assertEquals(node, graph.getEdgeDeviceNode(42))
    }

    @Test
    fun `getBackboneNode with an existing id should return null`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(1)
        val node = graph.createBackboneNode(42, system)
        assertEquals(node, graph.getBackboneNode(42))
    }

    @Test
    fun `getEdgeNode with an existing id should return null`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(1)
        val node = graph.createEdgeNode(42, system)
        assertEquals(node, graph.getEdgeNode(42))
    }

    @Test
    fun `adding an edge node to a system that is not in the graph should fail`() {
        val graph = Graph(defaultConfig)
        val system = AS(1)
        assertThrows<IllegalArgumentException> {
            graph.createEdgeNode(42, system)
        }
    }

    @Test
    fun `adding a backbone node to a system that is not in the graph should fail`() {
        val graph = Graph(defaultConfig)
        val system = AS(1)
        assertThrows<IllegalArgumentException> {
            graph.createBackboneNode(42, system)
        }
    }

    @Test
    fun `adding an edge device node to a system that is not in the graph should fail`() {
        val graph = Graph(defaultConfig)
        val system = AS(1)
        val container = DeviceContainer("abc", "tag", 1, 1F, 1, 1F)
        assertThrows<IllegalArgumentException> {
            graph.createEdgeDeviceNode(42, system, container)
        }
    }

    @Test
    fun `createEdgeNode should fail if id is already in use`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(1)
        graph.createEdgeNode(0, system)
        assertThrows<IllegalArgumentException> {
            graph.createEdgeNode(0, system)
        }
    }

    @Test
    fun `createBackboneNode should fail if id is already in use`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(1)
        graph.createBackboneNode(0, system)
        assertThrows<IllegalArgumentException> {
            graph.createBackboneNode(0, system)
        }
    }

    @Test
    fun `createEdgeDeviceNode should fail if id is already in use`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(1)
        val container = DeviceContainer("abc", "tag", 1, 1F, 1, 1F)
        graph.createEdgeDeviceNode(0, system, container)
        assertThrows<IllegalArgumentException> {
            graph.createEdgeDeviceNode(0, system, container)
        }
    }

    @Test
    fun `createEdgeNode should create a node with the params and should be retrievable`() {
        val graph = Graph(defaultConfig)
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
        val graph = Graph(defaultConfig)
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
        val graph = Graph(defaultConfig)
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
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(1)
        assertEquals(system, graph.getAutonomousSystem(1))
    }

    @Test
    fun `getOrCreateAutonomousSystem should return a new system if not present`() {
        val graph = Graph(defaultConfig)
        assertNull(graph.getAutonomousSystem(1))
        assertEquals(1, graph.getOrCreateAutonomousSystem(1).id)
    }

    @Test
    fun `getOrCreateAutonomousSystem returns an already existing system`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(1)
        assertEquals(system, graph.getOrCreateAutonomousSystem(1))
    }
}