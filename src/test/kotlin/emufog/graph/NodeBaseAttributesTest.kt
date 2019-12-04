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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class NodeBaseAttributesTest {

    private val defaultAS = AS(2)

    private val defaultAttributes = NodeBaseAttributes(1, defaultAS)

    @Test
    fun `test the init`() {
        assertEquals(1, defaultAttributes.id)
        assertEquals(defaultAS, defaultAttributes.system)
        assertTrue(defaultAttributes.edges.isEmpty())
        assertNull(defaultAttributes.node)
    }

    @Test
    fun `hashCode function should return id`() {
        assertEquals(1, defaultAttributes.hashCode())
    }

    @Test
    fun `equals function should return true`() {
        val other = NodeBaseAttributes(1, AS(42))
        assertTrue(defaultAttributes == other)
        assertFalse(defaultAttributes === other)
    }

    @Test
    fun `equals function should return false`() {
        val other = NodeBaseAttributes(2, AS(42))
        assertFalse(defaultAttributes == other)
        assertFalse(defaultAttributes === other)
    }

    @Test
    fun `addEdge should increase size by one`() {
        val attributes1 = NodeBaseAttributes(1, AS(42))
        val attributes2 = NodeBaseAttributes(2, AS(42))
        val attributes = NodeBaseAttributes(3, AS(42))
        val edge = Edge(0, BackboneNode(attributes1), BackboneNode(attributes2), 1F, 1F)
        assertTrue(attributes.edges.isEmpty())
        assertTrue(attributes.addEdge(edge))
        assertEquals(1, attributes.edges.size)
    }
}