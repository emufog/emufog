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
package emufog.fog

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CostComparatorTest {

    private val startingNode: StartingNode = mockk()

    private val comparator = CostComparator(startingNode)

    @Test
    fun `should sort according to costs and stable`() {
        val baseNode1: BaseNode = mockk {
            every { getCosts(startingNode) } returns 42F
        }
        val baseNode2: BaseNode = mockk {
            every { getCosts(startingNode) } returns 42F
        }
        val baseNode3: BaseNode = mockk {
            every { getCosts(startingNode) } returns 41.5F
        }
        val list = listOf(baseNode1, baseNode2, baseNode3)
        val sortedList = list.sortedWith(comparator)
        assertEquals(3, sortedList.size)
        assertEquals(baseNode3, sortedList[0])
        assertEquals(baseNode1, sortedList[1])
        assertEquals(baseNode2, sortedList[2])
    }

    @Test
    fun `should fail if no costs for o1 are associated`() {
        val baseNode1: BaseNode = mockk {
            every { getCosts(startingNode) } returns null
            every { node.id } returns 0
        }
        val baseNode2: BaseNode = mockk {
            every { getCosts(startingNode) } returns 42F
        }
        val list = listOf(baseNode1, baseNode2)
        assertThrows<IllegalStateException> {
            list.sortedWith(comparator)
        }
    }

    @Test
    fun `should fail if no costs for o2 are associated`() {
        val baseNode1: BaseNode = mockk {
            every { getCosts(startingNode) } returns 42F
        }
        val baseNode2: BaseNode = mockk {
            every { getCosts(startingNode) } returns null
            every { node.id } returns 1
        }
        val list = listOf(baseNode1, baseNode2)
        assertThrows<IllegalStateException> {
            list.sortedWith(comparator)
        }
    }
}