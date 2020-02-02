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
package emufog.device

import emufog.config.Config
import emufog.container.DeviceContainer
import emufog.graph.AS
import emufog.graph.EdgeDeviceNode
import emufog.graph.EdgeNode
import emufog.graph.Graph
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random

@TestInstance(Lifecycle.PER_CLASS)
internal class DeviceManagerTest {

    @Test
    fun `getRandomCount with negative upper boundary should throw an exception`() {
        assertThrows<IllegalArgumentException> {
            getRandomCount(-1F)
        }
    }

    @Test
    fun `if nextFloat is 1 the result should be upper to int`() {
        mockkObject(Random)
        every { Random.nextFloat() } returns 1F

        val actual = getRandomCount(42.5F)
        assertEquals(42, actual)

        unmockkObject(Random)
    }

    @Test
    fun `if nextFloat is 0 the result should be 0`() {
        mockkObject(Random)
        every { Random.nextFloat() } returns 0F

        val actual = getRandomCount(42.5F)
        assertEquals(0, actual)

        unmockkObject(Random)
    }

    @Test
    fun `empty device node types should not do anything`() {
        val config: Config = mockk {
            every { deviceNodeTypes } returns emptyList()
        }
        val graph = mockGraph()

        assignDeviceNodes(graph, config)

        verify(exactly = 0) { graph.createEdgeDeviceNode(any(), any(), any()) }
        verify(exactly = 0) { graph.createEdge(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `random count of 0 should not do anything`() {
        mockkStatic("emufog.device.DeviceManagerKt")
        every { getRandomCount(84F) } returns 0

        val deviceNodeType: DeviceContainer = mockk {
            every { averageDeviceCount } returns 42F
        }
        val config: Config = mockk {
            every { deviceNodeTypes } returns listOf(deviceNodeType)
            every { hostDeviceLatency } returns 1F
            every { hostDeviceBandwidth } returns 1F
        }
        val graph = mockGraph()

        assignDeviceNodes(graph, config)

        verify(exactly = 0) { graph.createEdgeDeviceNode(any(), any(), any()) }
        verify(exactly = 0) { graph.createEdge(any(), any(), any(), any(), any()) }

        unmockkStatic("emufog.device.DeviceManagerKt")
    }

    @Test
    fun `random count of 1 should assign a type to ever edge node`() {
        mockkStatic("emufog.device.DeviceManagerKt")
        every { getRandomCount(any()) } returns 1

        val deviceNodeType: DeviceContainer = mockk {
            every { averageDeviceCount } returns 42F
        }
        val config: Config = mockk {
            every { deviceNodeTypes } returns listOf(deviceNodeType)
            every { hostDeviceLatency } returns 1F
            every { hostDeviceBandwidth } returns 1F
        }
        val graph = mockGraph()

        assignDeviceNodes(graph, config)

        verify(exactly = 1) { graph.createEdgeDeviceNode(3, any(), deviceNodeType) }
        verify(exactly = 1) { graph.createEdgeDeviceNode(4, any(), deviceNodeType) }
        verify(exactly = 1) { graph.createEdgeDeviceNode(5, any(), deviceNodeType) }
        verify(exactly = 1) { graph.createEdge(1, any(), any(), 1F, 1F) }
        verify(exactly = 1) { graph.createEdge(2, any(), any(), 1F, 1F) }
        verify(exactly = 1) { graph.createEdge(3, any(), any(), 1F, 1F) }

        unmockkStatic("emufog.device.DeviceManagerKt")
    }

    private fun mockGraph(): Graph {
        val system0: AS = mockk()
        val nodes: List<EdgeNode> = listOf(
            mockk {
                every { system } returns system0
            },
            mockk {
                every { system } returns system0
            },
            mockk {
                every { system } returns system0
            }
        )
        val deviceNode: EdgeDeviceNode = mockk()
        return mockk {
            every { edgeNodes } returns nodes
            every { createEdgeDeviceNode(0, system0, any()) } throws IllegalArgumentException()
            every { createEdgeDeviceNode(1, system0, any()) } throws IllegalArgumentException()
            every { createEdgeDeviceNode(2, system0, any()) } throws IllegalArgumentException()
            every { createEdgeDeviceNode(not(range(0, 2)), system0, any()) } returns deviceNode
            every { createEdge(0, any(), deviceNode, any(), any()) } throws IllegalArgumentException()
            every { createEdge(neq(0), any(), deviceNode, any(), any()) } returns mockk()
        }
    }
}