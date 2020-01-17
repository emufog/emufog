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
package emufog.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PriorityHeapTest {

    @Test
    fun `empty heap should return null for peek and poll`() {
        val heap = PriorityHeap<String>()
        assertNull(heap.peek())
        assertNull(heap.pop())
        assertTrue(heap.isEmpty())
    }

    @Test
    fun `remove should only remove once successfully`() {
        val heap = PriorityHeap(SampleComparator())
        assertTrue(heap.isEmpty())

        val data = SampleClass(0, 42)
        assertTrue(heap.add(data))
        assertFalse(heap.isEmpty())
        assertTrue(heap.remove(data))
        assertTrue(heap.isEmpty())
        assertFalse(heap.remove(data))
    }

    @Test
    fun `sample sorting of data and a comparator`() {
        val heap = PriorityHeap(SampleComparator())
        assertTrue(heap.isEmpty())

        val data0 = SampleClass(0, 42)
        assertTrue(heap.add(data0))
        assertFalse(heap.isEmpty())
        assertEquals(data0, heap.peek())

        val data1 = SampleClass(1, 43)
        assertTrue(heap.add(data1))
        assertFalse(heap.isEmpty())
        assertEquals(data0, heap.peek())

        val data2 = SampleClass(1, 41)
        assertTrue(heap.add(data2))
        assertFalse(heap.isEmpty())
        assertEquals(data2, heap.peek())

        assertEquals(data2, heap.pop())
        assertEquals(data0, heap.peek())
        assertEquals(data0, heap.pop())
        assertEquals(data1, heap.peek())
        assertEquals(data1, heap.pop())
        assertTrue(heap.isEmpty())
    }

    @Test
    fun `sorting should be based on compare set criteria on equals`() {
        val heap = PriorityHeap(SampleComparator())
        assertTrue(heap.isEmpty())

        val data0 = SampleClass(0, 42)
        assertTrue(heap.add(data0))
        assertFalse(heap.isEmpty())
        assertEquals(data0, heap.peek())

        val data1 = SampleClass(1, 42)
        assertTrue(heap.add(data1))
        assertFalse(heap.isEmpty())
        assertEquals(data0, heap.peek())

        val data2 = SampleClass(1, 41)
        assertTrue(heap.add(data2))
        assertFalse(heap.isEmpty())
        assertEquals(data2, heap.peek())

        assertEquals(data2, heap.pop())
        assertEquals(data0, heap.peek())
        assertEquals(data0, heap.pop())
        assertEquals(data1, heap.peek())
        assertEquals(data1, heap.pop())
        assertTrue(heap.isEmpty())
    }
}

internal class SampleClass(val id: Int, val x: Int) {

    override fun equals(other: Any?): Boolean {
        if (other !is SampleClass) {
            return false
        }

        return id == other.id
    }

    override fun hashCode(): Int = id
}

internal class SampleComparator : Comparator<SampleClass> {

    override fun compare(o1: SampleClass, o2: SampleClass): Int {
        return o1.x - o2.x
    }
}