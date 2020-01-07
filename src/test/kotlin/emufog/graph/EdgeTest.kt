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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EdgeTest {

    private val defaultAS = AS(1)

    private val defaultSource = defaultAS.createBackboneNode(2)

    private val defaultDestination = defaultAS.createEdgeNode(3)

    private val defaultEdge = Edge(4, defaultSource, defaultDestination, 10F, 1000F)

    @Test
    fun `test the constructor fields`() {
        assertEquals(defaultSource, defaultEdge.source)
        assertEquals(defaultDestination, defaultDestination)
        assertEquals(4, defaultEdge.id)
        assertEquals(10F, defaultEdge.latency)
        assertEquals(1000F, defaultEdge.bandwidth)
    }

    @Test
    fun `negative latency should fail`() {
        assertThrows<IllegalArgumentException> {
            Edge(0, defaultSource, defaultDestination, -1F, 10F)
        }
    }

    @Test
    fun `negative bandwidth should fail`() {
        assertThrows<IllegalArgumentException> {
            Edge(0, defaultSource, defaultDestination, 10F, -1F)
        }
    }

    @Test
    fun `hashCode should return the id`() {
        assertEquals(4, defaultEdge.hashCode())
    }

    @Test
    fun `toString should return the expected format`() {
        assertEquals("Edge: 4", defaultEdge.toString())
    }

    @Test
    fun `equals with different edge with same id should return true`() {
        val node = AS(4).createBackboneNode(42)
        val edge = Edge(4, node, defaultDestination, 1F, 1F)

        assertEquals(edge, defaultEdge)
        assertFalse(edge === defaultEdge)
    }

    @Test
    fun `equals with different edge with different id should return false`() {
        val node = AS(4).createBackboneNode(42)
        val edge = Edge(1024, node, defaultDestination, 1F, 1F)

        assertNotEquals(edge, defaultEdge)
        assertFalse(edge === defaultEdge)
    }

    @Test
    fun `equals with same object should return true`() {
        assertEquals(defaultEdge, defaultEdge)
    }

    @Test
    fun `equals with null should return false`() {
        assertNotEquals(defaultEdge, null)
    }

    @Test
    fun `isCrossASEdge should return true for different AS`() {
        val node = AS(4).createBackboneNode(42)
        val edge = Edge(1024, node, defaultDestination, 1F, 1F)

        assertTrue(edge.isCrossASEdge())
    }

    @Test
    fun `isCrossASEdge should return false for same AS`() {
        assertFalse(defaultEdge.isCrossASEdge())
    }

    @Test
    fun `getDestinationForSource should return the opposing end`() {
        assertEquals(defaultSource, defaultEdge.getDestinationForSource(defaultDestination))
        assertEquals(defaultDestination, defaultEdge.getDestinationForSource(defaultSource))
    }

    @Test
    fun `getDestinationForSource should return null if node is not part of edge`() {
        val node = AS(4).createBackboneNode(42)
        val edge = Edge(1024, node, defaultDestination, 1F, 1F)

        assertNull(edge.getDestinationForSource(defaultSource))
    }

    @Test
    fun `source and destination should update if node type changes`() {
        val system = AS(4)
        val node1 = system.createBackboneNode(41)
        val node2 = system.createBackboneNode(42)
        val edge = Edge(0, node1, node2, 1F, 1F)
        assertEquals(node1, edge.source)
        assertEquals(node2, edge.destination)

        val node1Replacement = system.replaceByEdgeNode(node1)
        val node2Replacement = system.replaceByEdgeNode(node2)
        assertEquals(node1Replacement, edge.source)
        assertEquals(node2Replacement, edge.destination)
    }
}