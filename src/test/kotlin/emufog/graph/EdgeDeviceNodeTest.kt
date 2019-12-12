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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class EdgeDeviceNodeTest {

    private val defaultAS = AS(0)

    private val defaultAttributes = NodeBaseAttributes(1, defaultAS)

    private val defaultContainer = DeviceContainer("docker", "tag", 1, 1F, 1, 1F)

    private val defaultEmulationNode = EmulationNode("1.2.3.4", defaultContainer)

    private val defaultNode = EdgeDeviceNode(defaultAttributes, defaultEmulationNode)

    @Test
    fun `test the node type`() {
        assertEquals(NodeType.EDGE_DEVICE_NODE, defaultNode.type)
    }

    @Test
    fun `test the nodes name`() {
        assertEquals("h1", defaultNode.name)
    }

    @Test
    fun `test the nodes toString`() {
        assertEquals("h1", defaultNode.toString())
    }

    @Test
    fun `test the hashCode function`() {
        assertEquals(1, defaultNode.hashCode())
    }

    @Test
    fun `test the nodes id`() {
        assertEquals(1, defaultNode.id)
    }

    @Test
    fun `test the device container`() {
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
    fun `add links to the node base attributes`() {
        val attributes = NodeBaseAttributes(2, AS(42))
        val node = EdgeDeviceNode(attributes, defaultEmulationNode)
        val node2 = EdgeDeviceNode(NodeBaseAttributes(3, AS(12)), defaultEmulationNode)
        assertTrue(node.edges.isEmpty())
        assertEquals(0, node.degree)

        Edge(1, node, node2, 2F, 3F)
        Edge(2, node2, node, 2.5F, 31F)

        assertFalse(node.edges.isEmpty())
        assertEquals(2, node.degree)
    }

    @Test
    fun `test equals with different node with same id`() {
        val node = EdgeDeviceNode(defaultAttributes, defaultEmulationNode)

        assertTrue(node == defaultNode)
        assertFalse(node === defaultNode)
    }

    @Test
    fun `test equals with different node with different id`() {
        val node = EdgeDeviceNode(NodeBaseAttributes(35, defaultAS), defaultEmulationNode)

        assertFalse(node == defaultNode)
        assertFalse(node === defaultNode)
    }

    @Test
    fun `test equals with same object`() {
        assertTrue(defaultNode == defaultNode)
    }

    @Test
    fun `test equals with null`() {
        assertFalse(defaultNode == null)
    }

    @Test
    fun `test equals with backbone node`() {
        val node = BackboneNode(defaultAttributes)

        assertTrue(node.equals(defaultNode))
    }

    @Test
    fun `test equals with edge node`() {
        val node = EdgeNode(defaultAttributes)

        assertTrue(node.equals(defaultNode))
    }

    @Test
    fun `test the set of emulation configurations`() {
        val attributes = NodeBaseAttributes(2, AS(42))
        val node = EdgeDeviceNode(attributes, defaultEmulationNode)
        val container = FogContainer("abc", "tag", 1024, 1F, 1, 1.5F)
        val emulationNode = EmulationNode("1.2.3.4", container)
        node.emulationNode = emulationNode
        assertTrue(node.hasEmulationSettings())
        assertEquals(emulationNode, node.emulationNode)
    }
}