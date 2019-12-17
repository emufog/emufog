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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class EdgeDeviceNodeTest {

    private val defaultAS = AS(0)

    private val defaultContainer = DeviceContainer("docker", "tag", 1, 1F, 1, 1F)

    private val defaultEmulationNode = EdgeEmulationNode("1.2.3.4", defaultContainer)

    private val defaultNode = EdgeDeviceNode(1, defaultAS, emulationNode = defaultEmulationNode)

    @Test
    fun `the node type should be edge device`() {
        assertEquals(NodeType.EDGE_DEVICE_NODE, defaultNode.type)
    }

    @Test
    fun `the nodes name should return h1`() {
        assertEquals("h1", defaultNode.name)
    }

    @Test
    fun `toString should return h11`() {
        assertEquals("h1", defaultNode.toString())
    }

    @Test
    fun `the hashCode function should return the id`() {
        assertEquals(1, defaultNode.hashCode())
    }

    @Test
    fun `test the node's id getter`() {
        assertEquals(1, defaultNode.id)
    }

    @Test
    fun `the device's container should be equal to the emulation node`() {
        assertEquals(defaultContainer, defaultNode.containerType)
    }

    @Test
    fun `test the autonomous system of the node`() {
        assertEquals(defaultAS, defaultNode.system)
    }

    @Test
    fun `test the empty init function`() {
        assertTrue(defaultNode.edges.isEmpty())
        assertEquals(0, defaultNode.degree)
        assertEquals(defaultEmulationNode, defaultNode.emulationNode)
        assertTrue(defaultNode.hasEmulationSettings())
    }

    @Test
    fun `test the init function with list of edges`() {
        val edge = Edge(1, defaultNode, defaultNode, 1F, 1F)
        val node = BackboneNode(0, defaultAS, listOf(edge))
        assertFalse(node.edges.isEmpty())
        assertEquals(1, node.degree)
        Assertions.assertNull(node.emulationNode)
        assertFalse(node.hasEmulationSettings())
    }

    @Test
    fun `add links to the node base attributes`() {
        val node = EdgeDeviceNode(2, AS(42), emulationNode = defaultEmulationNode)
        assertTrue(node.edges.isEmpty())
        assertEquals(0, node.degree)

        node.addEdge(Edge(1, node, defaultNode, 2F, 3F))
        node.addEdge(Edge(2, defaultNode, node, 2.5F, 31F))

        assertFalse(node.edges.isEmpty())
        assertEquals(2, node.degree)
    }

    @Test
    fun `equals with different node with same id should return true`() {
        val node = EdgeDeviceNode(1, AS(42), emulationNode = defaultEmulationNode)

        assertEquals(node, defaultNode)
        assertFalse(node === defaultNode)
    }

    @Test
    fun `equals with different node with different id should return false`() {
        val node = EdgeDeviceNode(35, defaultAS, emulationNode = defaultEmulationNode)

        assertNotEquals(node, defaultNode)
        assertFalse(node === defaultNode)
    }

    @Test
    fun `equals with same object should return true`() {
        assertEquals(defaultNode, defaultNode)
    }

    @Test
    fun `equals with null should return false`() {
        assertNotEquals(defaultNode, null)
    }

    @Test
    fun `equals with edge device node with same id should return true`() {
        val node = BackboneNode(1, defaultAS)

        assertEquals(node, defaultNode)
    }

    @Test
    fun `equals with edge node with same id should return true`() {
        val node = EdgeNode(1, defaultAS)

        assertEquals(node, defaultNode)
    }

    @Test
    fun `set of emulation configurations should update`() {
        val node = EdgeDeviceNode(2, AS(42), emulationNode = defaultEmulationNode)
        val container = FogContainer("abc", "tag", 1024, 1F, 1, 1.5F)
        val emulationNode = EmulationNode("1.2.3.4", container)
        node.emulationNode = emulationNode
        assertTrue(node.hasEmulationSettings())
        assertEquals(emulationNode, node.emulationNode)
    }
}