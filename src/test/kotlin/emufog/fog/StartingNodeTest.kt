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

import emufog.graph.EdgeNode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StartingNodeTest {

    private val edgeNode = mockk<EdgeNode> {
        every { deviceCount } returns 42
    }

    @Test
    fun `test the initialization of a starting node`() {
        val startingNode = StartingNode(edgeNode)
        assertEquals(42, startingNode.deviceCount)
        assertEquals(0, startingNode.reachableNodes.size)
    }

    @Test
    fun `decreaseDeviceCount should reduce the device count by n`() {
        val startingNode = StartingNode(edgeNode)
        assertEquals(42, startingNode.deviceCount)
        startingNode.decreaseDeviceCount(20)
        assertEquals(22, startingNode.deviceCount)
    }

    @Test
    fun `addPossibleNode should add a new node to the set`() {
        val startingNode = StartingNode(edgeNode)
        assertEquals(0, startingNode.reachableNodes.size)

        val node = mockk<BaseNode>()
        startingNode.addPossibleNode(node)
        assertEquals(1, startingNode.reachableNodes.size)
        assertEquals(node, startingNode.reachableNodes.first())
    }

    @Test
    fun `removePossibleNode should remove a node from the set`() {
        val startingNode = StartingNode(edgeNode)
        assertEquals(0, startingNode.reachableNodes.size)

        val node = mockk<BaseNode>()
        startingNode.addPossibleNode(node)
        assertEquals(node, startingNode.reachableNodes.first())

        startingNode.removePossibleNode(node)
        assertEquals(0, startingNode.reachableNodes.size)
    }

    @Test
    fun `notifyPossibleNodes should call each node in the node set`() {
        val startingNode = StartingNode(edgeNode)
        assertEquals(0, startingNode.reachableNodes.size)

        val node1 = mockk<BaseNode> {
            every { removeStartingNode(any()) } returns Unit
        }
        val node2 = mockk<BaseNode> {
            every { removeStartingNode(any()) } returns Unit
        }
        startingNode.addPossibleNode(node1)
        startingNode.addPossibleNode(node2)
        assertEquals(2, startingNode.reachableNodes.size)

        startingNode.notifyPossibleNodes()
        verify(exactly = 1) { node1.removeStartingNode(startingNode) }
        verify(exactly = 1) { node2.removeStartingNode(startingNode) }
    }
}