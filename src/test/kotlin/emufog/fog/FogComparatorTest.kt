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

internal class FogComparatorTest {

    private val comparator = FogComparator()

    @Test
    fun `should sort according to deployment costs`() {
        val node1 = mockk<BaseNode> {
            every { averageDeploymentCosts } returns 42F
        }
        val node2 = mockk<BaseNode> {
            every { averageDeploymentCosts } returns 42.5F
        }
        val node3 = mockk<BaseNode> {
            every { averageDeploymentCosts } returns 41.5F
        }
        val list = listOf(node1, node2, node3)
        val sortedList = list.sortedWith(comparator)
        assertEquals(3, sortedList.size)
        assertEquals(node3, sortedList[0])
        assertEquals(node1, sortedList[1])
        assertEquals(node2, sortedList[2])
    }

    @Test
    fun `should sort according to connection costs if deployment costs are equal`() {
        val node1 = mockk<BaseNode> {
            every { averageDeploymentCosts } returns 42F
            every { averageConnectionCosts } returns 1F
        }
        val node2 = mockk<BaseNode> {
            every { averageDeploymentCosts } returns 42F
            every { averageConnectionCosts } returns 1F
        }
        val node3 = mockk<BaseNode> {
            every { averageDeploymentCosts } returns 42F
            every { averageConnectionCosts } returns 0.5F
        }
        val list = listOf(node1, node2, node3)
        val sortedList = list.sortedWith(comparator)
        assertEquals(3, sortedList.size)
        assertEquals(node3, sortedList[0])
        assertEquals(node1, sortedList[1])
        assertEquals(node2, sortedList[2])
    }

    @Test
    fun `should fail when no deployment costs for o1 are associated`() {
        val node1 = mockk<BaseNode> {
            every { averageDeploymentCosts } returns null
            every { node.id } returns 0
        }
        val node2 = mockk<BaseNode> {
            every { averageDeploymentCosts } returns 1F
        }
        val list = listOf(node1, node2)
        assertThrows<IllegalStateException> {
            list.sortedWith(comparator)
        }
    }

    @Test
    fun `should fail when no deployment costs for o2 are associated`() {
        val node1 = mockk<BaseNode> {
            every { averageDeploymentCosts } returns 1F
        }
        val node2 = mockk<BaseNode> {
            every { averageDeploymentCosts } returns null
            every { node.id } returns 1
        }
        val list = listOf(node1, node2)
        assertThrows<IllegalStateException> {
            list.sortedWith(comparator)
        }
    }
}