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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ASTest {

    private val defaultSystem = AS(0)

    @Test
    fun `test the default init`() {
        assertEquals(0, defaultSystem.id)
        assertEquals(0, defaultSystem.backboneNodes.size)
        assertEquals(0, defaultSystem.edgeDeviceNodes.size)
        assertEquals(0, defaultSystem.edgeNodes.size)
    }

    @Test
    fun `hashCode function should return id`() {
        assertEquals(0, defaultSystem.hashCode())
    }

    @Test
    fun `toString function should include the id`() {
        assertEquals("AS: 0", defaultSystem.toString())
    }

    @Test
    fun `equals with different system with same id`() {
        val system = AS(0)

        assertEquals(system, defaultSystem)
        assertFalse(system === defaultSystem)
    }

    @Test
    fun `equals with different system with different id`() {
        val system = AS(1)

        assertNotEquals(system, defaultSystem)
        assertFalse(system === defaultSystem)
    }

    @Test
    fun `equals with same object`() {
        assertEquals(defaultSystem, defaultSystem)
    }

    @Test
    fun `equals with null`() {
        assertNotEquals(defaultSystem, null)
    }

    @Test
    fun `create an edge node`() {
        val system = AS(1)
        val edgeNode = system.createEdgeNode(42)
        assertEquals(42, edgeNode.id)
        val actual = system.getEdgeNode(42)
        assertNotNull(actual)
        assertEquals(42, actual!!.id)
        assertTrue(system.containsNode(edgeNode))
        assertTrue(system.edgeNodes.contains(edgeNode))
    }

    @Test
    fun `create a backbone node`() {
        val system = AS(1)
        val backboneNode = system.createBackboneNode(42)
        assertEquals(42, backboneNode.id)
        val actual = system.getBackboneNode(42)
        assertNotNull(actual)
        assertEquals(42, actual!!.id)
        assertTrue(system.containsNode(backboneNode))
        assertTrue(system.backboneNodes.contains(backboneNode))
    }

    @Test
    fun `create an edge device node`() {
        val system = AS(1)
        val container = DeviceContainer("docker", "tag", 1, 1F, 1, 1F)
        val edgeDeviceNode = system.createEdgeDeviceNode(42, EdgeEmulationNode("1.2.3.4", container))
        assertEquals(42, edgeDeviceNode.id)
        val actual = system.getEdgeDeviceNode(42)
        assertNotNull(actual)
        assertEquals(42, actual!!.id)
        assertTrue(system.containsNode(edgeDeviceNode))
        assertTrue(system.edgeDeviceNodes.contains(edgeDeviceNode))
    }

    @Test
    fun `containsNode on empty AS should return false`() {
        assertFalse(defaultSystem.containsNode(BackboneNode(1, defaultSystem)))
    }

    @Test
    fun `getBackboneNode that does not exist should return null`() {
        assertNull(defaultSystem.getBackboneNode(42))
    }

    @Test
    fun `getEdgeNode that does not exist should return null`() {
        assertNull(defaultSystem.getEdgeNode(42))
    }

    @Test
    fun `getEdgeDeviceNode that does not exist should return null`() {
        assertNull(defaultSystem.getEdgeDeviceNode(42))
    }

    @Test
    fun `replace a backbone node with an edge node`() {
        val system = AS(1)
        val backboneNode = system.createBackboneNode(42)
        val edgeNode = system.replaceByEdgeNode(backboneNode)
        assertEquals(backboneNode, edgeNode)
        assertEquals(42, edgeNode.id)
        assertEquals(system, edgeNode.system)
        assertNull(system.getBackboneNode(42))
        assertNull(system.backboneNodes.firstOrNull { it === backboneNode })
        assertEquals(edgeNode, system.getEdgeNode(42))
        assertTrue(system.edgeNodes.contains(edgeNode))
    }

    @Test
    fun `replace a backbone node with an edge device node`() {
        val system = AS(1)
        val backboneNode = system.createBackboneNode(42)
        val container = DeviceContainer("abc", "tag", 1024, 1F, 1, 1.5F)
        val emulationNode = EdgeEmulationNode("1.2.3.4", container)
        val edgeDeviceNode = system.replaceByEdgeDeviceNode(backboneNode, emulationNode)
        assertEquals(backboneNode, edgeDeviceNode)
        assertEquals(42, edgeDeviceNode.id)
        assertEquals(system, edgeDeviceNode.system)
        assertEquals(emulationNode, edgeDeviceNode.emulationNode)
        assertNull(system.getBackboneNode(42))
        assertNull(system.backboneNodes.firstOrNull { it === backboneNode })
        assertEquals(edgeDeviceNode, system.getEdgeDeviceNode(42))
        assertTrue(system.edgeDeviceNodes.contains(edgeDeviceNode))
    }


    @Test
    fun `replace an edge node with a backbone node`() {
        val system = AS(1)
        val edgeNode = system.createEdgeNode(42)
        val backboneNode = system.replaceByBackboneNode(edgeNode)
        assertEquals(edgeNode, backboneNode)
        assertEquals(42, backboneNode.id)
        assertEquals(system, backboneNode.system)
        assertNull(system.getEdgeNode(42))
        assertNull(system.edgeNodes.firstOrNull { it === edgeNode })
        assertEquals(backboneNode, system.getBackboneNode(42))
        assertTrue(system.backboneNodes.contains(backboneNode))
    }

    @Test
    fun `replacement call should fail with an exception of node is not part of the AS`() {
        val node = AS(1024).createBackboneNode(1)
        assertThrows<IllegalArgumentException> { defaultSystem.replaceByEdgeNode(node) }
    }

    @Test
    fun `replacement call should fail with an exception if node cannot be removed from the AS`() {
        val system1 = AS(0)
        val system2 = AS(0)
        val node = system1.createBackboneNode(1)
        assertThrows<IllegalStateException> { system2.replaceByEdgeNode(node) }
    }
}