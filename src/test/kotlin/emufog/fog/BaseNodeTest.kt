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

import emufog.container.FogContainer
import emufog.graph.EdgeNode
import emufog.graph.Node
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class BaseNodeTest {

    private val node = mockk<Node> {
        every { this@mockk.hashCode() } returns 42
    }

    @Test
    fun `test the initialization of a base node`() {
        val baseNode = BaseNode(node)
        assertEquals(node, baseNode.node)
        assertEquals(0F, baseNode.averageConnectionCosts)
        assertNull(baseNode.averageDeploymentCosts)
        assertTrue(baseNode.modified)
        assertNull(baseNode.type)
        assertEquals(0, baseNode.startingNodes.size)
    }

    @Test
    fun `averageConnectionCosts should be reflecting the cost map`() {
        val baseNode = BaseNode(node)
        assertEquals(0F, baseNode.averageConnectionCosts)
        baseNode.setCosts(mockk {
            every { addPossibleNode(baseNode) } returns Unit
        }, 1F)
        assertEquals(1F, baseNode.averageConnectionCosts)
        baseNode.setCosts(mockk {
            every { addPossibleNode(baseNode) } returns Unit
        }, 23.5F)
        assertEquals(24.5F / 2, baseNode.averageConnectionCosts)
        baseNode.setCosts(mockk {
            every { addPossibleNode(baseNode) } returns Unit
        }, 100F)
        assertEquals(124.5F / 3, baseNode.averageConnectionCosts)
    }

    @Test
    fun `getCosts should return null if none are associated`() {
        val baseNode = BaseNode(node)
        val startingNode = mockk<StartingNode>()
        assertNull(baseNode.getCosts(startingNode))
    }

    @Test
    fun `setCosts should set the costs for the given starting node`() {
        val baseNode = BaseNode(node)
        val edgeNode = mockk<EdgeNode> {
            every { deviceCount } returns 10
        }
        val startingNode = StartingNode(edgeNode)
        assertNull(baseNode.getCosts(startingNode))
        assertFalse(baseNode.hasConnections())
        baseNode.setCosts(startingNode, 42F)
        assertEquals(42F, baseNode.getCosts(startingNode))
        assertTrue(baseNode.hasConnections())
        assertEquals(1, startingNode.reachableNodes.size)
        assertEquals(baseNode, startingNode.reachableNodes.first())
    }

    @Test
    fun `removeStartingNode should delete a cost mapping for that starting node`() {
        val baseNode = BaseNode(node)
        val edgeNode = mockk<EdgeNode> {
            every { deviceCount } returns 10
        }
        val startingNode = StartingNode(edgeNode)
        assertNull(baseNode.getCosts(startingNode))

        baseNode.setCosts(startingNode, 42F)
        assertEquals(42F, baseNode.getCosts(startingNode))

        baseNode.removeStartingNode(startingNode)
        assertNull(baseNode.getCosts(startingNode))
    }

    @Test
    fun `findFogType should abort on non modified nodes`() {
        val baseNode = BaseNode(node)
        baseNode.modified = false
        baseNode.findFogType(emptyList())
    }

    @Test
    fun `findFogType should fail on no connections`() {
        val baseNode = BaseNode(node)
        assertThrows<IllegalStateException> {
            val type = FogContainer("name", "tag", 1, 1F, 5, 1F)
            baseNode.findFogType(listOf(type))
        }
    }

    @Test
    fun `findFogType should fail on no given fog types`() {
        val baseNode = BaseNode(node)
        assertThrows<IllegalStateException> {
            baseNode.findFogType(emptyList())
        }
    }

    @Test
    fun `findFogType should set the optimal fog container1`() {
        val type1 = FogContainer("name", "tag", 1, 1F, 1, 1F)
        val type2 = FogContainer("name", "tag", 1, 1F, 2, 1.5F)
        val baseNode = findOptimalFogType(listOf(type1, type2))
        assertEquals(type2, baseNode.type)
        assertEquals(type2.costs / type2.maxClients, baseNode.averageDeploymentCosts)

        val coveredNodes = baseNode.getCoveredStartingNodes()
        assertEquals(1, coveredNodes.size)
        assertEquals(0, coveredNodes[0].first.node.id)
        assertEquals(2, coveredNodes[0].second)
    }

    @Test
    fun `findFogType should set the optimal fog container2`() {
        val type1 = FogContainer("name", "tag", 1, 1F, 1, 1F)
        val type2 = FogContainer("name", "tag", 1, 1F, 100, 20F)
        val baseNode = findOptimalFogType(listOf(type1, type2))
        assertEquals(type1, baseNode.type)
        assertEquals(1F, baseNode.averageDeploymentCosts)

        val coveredNodes = baseNode.getCoveredStartingNodes()
        assertEquals(1, coveredNodes.size)
        assertEquals(0, coveredNodes[0].first.node.id)
        assertEquals(1, coveredNodes[0].second)
    }

    @Test
    fun `findFogType should set the optimal fog container3`() {
        val type1 = FogContainer("name", "tag", 1, 1F, 100, 20F)
        val baseNode = findOptimalFogType(listOf(type1))
        assertEquals(type1, baseNode.type)
        assertEquals(20F / 11, baseNode.averageDeploymentCosts)

        val coveredNodes = baseNode.getCoveredStartingNodes()
        assertEquals(2, coveredNodes.size)
        assertEquals(0, coveredNodes[0].first.node.id)
        assertEquals(5, coveredNodes[0].second)
        assertEquals(1, coveredNodes[1].first.node.id)
        assertEquals(6, coveredNodes[1].second)
    }

    private fun findOptimalFogType(types: List<FogContainer>): BaseNode {
        val baseNode = BaseNode(node)
        val edgeNode1 = mockk<EdgeNode> {
            every { deviceCount } returns 5
            every { id } returns 0
        }
        baseNode.setCosts(StartingNode(edgeNode1), 1F)
        val edgeNode2 = mockk<EdgeNode> {
            every { deviceCount } returns 6
            every { id } returns 1
        }
        baseNode.setCosts(StartingNode(edgeNode2), 2F)

        assertNull(baseNode.type)
        assertNull(baseNode.averageDeploymentCosts)
        baseNode.findFogType(types)

        return baseNode
    }

    @Test
    fun `getCoveredStartingNodes should return empty list without any connections`() {
        val baseNode = BaseNode(node)
        val coveredNodes = baseNode.getCoveredStartingNodes()
        assertEquals(0, coveredNodes.size)
    }

    @Test
    fun `equals with same node should return true`() {
        val baseNode = BaseNode(node)
        assertTrue(baseNode == baseNode)
        assertTrue(baseNode === baseNode)
    }

    @Test
    fun `equals with different node but same underlying node should return true`() {
        val baseNode1 = BaseNode(node)
        val baseNode2 = BaseNode(node)
        assertTrue(baseNode1 == baseNode2)
        assertFalse(baseNode1 === baseNode2)
    }

    @Test
    fun `equals with different node should return false`() {
        val baseNode1 = BaseNode(node)
        val baseNode2 = BaseNode(mockk())
        assertFalse(baseNode1 == baseNode2)
        assertFalse(baseNode1 === baseNode2)
    }

    @Test
    fun `hashCode should return the hash code of the associated node`() {
        val baseNode = BaseNode(node)
        assertEquals(42, baseNode.hashCode())
    }
}