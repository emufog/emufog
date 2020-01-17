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

import emufog.container.FogContainer
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class FogHeapTest {

    private val fogType1 = FogContainer("name", "small", 1, 2F, 3, 4F)

    private val fogType2 = FogContainer("name", "big", 1, 2F, 5, 5F)

    @Test
    fun `empty set of fogTypes should be empty`() {
        val heap = FogHeap(createGraph(), emptySet())
        assertTrue(heap.isEmpty())
        assertThrows<IllegalStateException> {
            heap.getNext()
        }
    }

    @Test
    fun `empty set of baseNodes should be empty`() {
        val heap = FogHeap(emptySet(), setOf(fogType1))
        assertTrue(heap.isEmpty())
        assertThrows<IllegalStateException> {
            heap.getNext()
        }
    }

    @Test
    fun `simple topology with only the small container`() {
        val heap = FogHeap(createGraph(), setOf(fogType1))
        assertFalse(heap.isEmpty())

        val next1 = heap.getNext()
        assertEquals(5, next1.node.id)
        assertEquals(fogType1, next1.type)
        assertFalse(heap.isEmpty())

        val next2 = heap.getNext()
        assertEquals(3, next2.node.id)
        assertEquals(fogType1, next2.type)
        assertFalse(heap.isEmpty())

        val next3 = heap.getNext()
        assertEquals(0, next3.node.id)
        assertEquals(fogType1, next3.type)
        assertTrue(heap.isEmpty())
    }

    @Test
    fun `simple topology with both container`() {
        val heap = FogHeap(createGraph(), setOf(fogType1, fogType2))
        assertFalse(heap.isEmpty())

        val next1 = heap.getNext()
        assertEquals(5, next1.node.id)
        assertEquals(fogType2, next1.type)
        assertFalse(heap.isEmpty())

        val next2 = heap.getNext()
        assertEquals(0, next2.node.id)
        assertEquals(fogType1, next2.type)
        assertTrue(heap.isEmpty())
    }

    private fun createGraph(): Set<BaseNode> {
        val start0 = StartingNode(mockk {
            every { id } returns 0
            every { deviceCount } returns 2
        })
        val start5 = StartingNode(mockk {
            every { id } returns 5
            every { deviceCount } returns 5
        })
        val base1 = BaseNode(mockk { every { id } returns 1 })
        val base2 = BaseNode(mockk { every { id } returns 2 })
        val base3 = BaseNode(mockk { every { id } returns 3 })
        val base4 = BaseNode(mockk { every { id } returns 4 })
        val base6 = BaseNode(mockk { every { id } returns 6 })

        start0.setCosts(start0, 0F)
        start0.setCosts(start5, 6F)

        start5.setCosts(start0, 6F)
        start5.setCosts(start5, 0F)

        base1.setCosts(start0, 2F)
        base1.setCosts(start5, 4F)

        base2.setCosts(start0, 3F)
        base2.setCosts(start5, 3F)

        base3.setCosts(start0, 5F)
        base3.setCosts(start5, 1F)

        base4.setCosts(start0, 4F)
        base4.setCosts(start5, 3F)

        base6.setCosts(start0, 5F)
        base6.setCosts(start5, 5F)

        return setOf(start0, start5, base1, base2, base3, base4, base6)
    }
}