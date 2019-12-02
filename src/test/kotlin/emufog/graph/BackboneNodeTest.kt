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

import emufog.container.FogContainer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BackboneNodeTest {

    private val defaultAS = AS(0)

    private val defaultAttributes = NodeBaseAttributes(1, defaultAS)

    private val defaultNode = BackboneNode(defaultAttributes)

    @Test
    fun `test the node type`() {
        assertEquals(NodeType.BACKBONE_NODE, defaultNode.type)
    }

    @Test
    fun `test the nodes name`() {
        assertEquals("s1", defaultNode.name)
    }

    @Test
    fun `test the nodes toString`() {
        assertEquals("s1", defaultNode.toString())
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
    fun `test the autonomous system of the node`() {
        assertEquals(defaultAS, defaultNode.system)
    }

    @Test
    fun `test the empty init function`() {
        assertTrue(defaultNode.edges.isEmpty())
        assertEquals(0, defaultNode.degree)
        assertNull(defaultNode.emulationNode)
        assertFalse(defaultNode.hasEmulationSettings())
    }

    @Test
    fun `add links to the node base attributes`() {
        val attributes = NodeBaseAttributes(2, AS(42))
        val node = BackboneNode(attributes)
        val node2 = BackboneNode(NodeBaseAttributes(3, AS(12)))
        assertTrue(node.edges.isEmpty())
        assertEquals(0, node.degree)

        Edge(1, node, node2, 2F, 3F)
        Edge(2, node2, node, 2.5F, 31F)

        assertFalse(node.edges.isEmpty())
        assertEquals(2, node.degree)
    }

    @Test
    fun `test the equals function`() {
        val node2 = BackboneNode(defaultAttributes)
        val node3 = BackboneNode(NodeBaseAttributes(35, defaultAS))

        assertTrue(node2 == defaultNode)
        assertFalse(node2 === defaultNode)
        assertFalse(node3 == defaultNode)
        assertFalse(node3 === defaultNode)
    }

    @Test
    fun `test the set of emulation configurations`() {
        val attributes = NodeBaseAttributes(2, AS(42))
        val node = BackboneNode(attributes)
        assertFalse(node.hasEmulationSettings())
        assertNull(node.emulationNode)
        val container = FogContainer("abc", "tag", 1024, 1F, 1, 1.5F)
        val emulationNode = EmulationNode("1.2.3.4", container)
        node.emulationNode = emulationNode
        assertTrue(node.hasEmulationSettings())
        assertEquals(emulationNode, node.emulationNode)
    }
}