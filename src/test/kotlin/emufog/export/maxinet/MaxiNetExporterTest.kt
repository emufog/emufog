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
package emufog.export.maxinet

import emufog.config.Config
import emufog.container.DeviceContainer
import emufog.container.FogContainer
import emufog.graph.EmulationNode
import emufog.graph.Graph
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkConstructor
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.io.BufferedWriter
import java.io.File
import java.nio.file.Path

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MaxiNetExporterTest {

    private val defaultConfig: Config = mockk {
        every { baseAddress } returns "1.2.3.4"
        every { overWriteOutputFile } returns false
    }

    private val defaultFile: File = mockk {
        every { exists() } returns true
    }

    private val defaultWriter: BufferedWriter = mockk {
        every { write(any<String>()) } returns Unit
        every { flush() } returns Unit
        every { close() } returns Unit
    }

    @BeforeAll
    fun initialize() {
        mockkStatic("emufog.export.maxinet.MaxiNetExporterKt")
        every { getBufferedWriter(any()) } returns defaultWriter
    }

    @AfterAll
    fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `if file exists and it should not be overwritten it should throw an exception`() {
        val path: Path = mockk {
            every { toFile() } returns defaultFile
        }

        val graph = Graph(defaultConfig)

        assertThrows<IllegalArgumentException> {
            MaxiNetExporter.exportGraph(graph, path)
        }
    }

    @Test
    fun `if file exists and it should be overwritten it should call the export`() {
        val path: Path = mockk {
            every { toFile() } returns defaultFile
            every { this@mockk.toString() } returns "file.py"
        }
        val config: Config = mockk {
            every { baseAddress } returns "1.2.3.4"
            every { overWriteOutputFile } returns true
        }
        val graph = Graph(config)

        mockkConstructor(MaxiNetExporterImpl::class)
        every { anyConstructed<MaxiNetExporterImpl>().exportGraph() } returns Unit

        MaxiNetExporter.exportGraph(graph, path)

        verify { MaxiNetExporterImpl(graph, defaultWriter).exportGraph() }

        unmockkConstructor(MaxiNetExporterImpl::class)
    }

    @Test
    fun `if the file is not py file it should throw an exception`() {
        val path: Path = mockk {
            every { toFile() } returns defaultFile
            every { this@mockk.toString() } returns "file.txt"
        }
        val graph = Graph(defaultConfig)

        assertThrows<IllegalArgumentException> {
            MaxiNetExporter.exportGraph(graph, path)
        }
    }

    @Test
    fun `empty graph should just call imports etc`() {
        val graph = Graph(defaultConfig)

        MaxiNetExporterImpl(graph, defaultWriter).exportGraph()

        // 23 lines
        verify(exactly = 23) { defaultWriter.write("\n") }
    }

    @Test
    fun `sample graph with different should be written as defined in maxinet`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(123)
        graph.createBackboneNode(0, system)
        graph.createEdgeNode(1, system)
        graph.createEdgeDeviceNode(2, system, DeviceContainer("name", "tag", 1, 1F, 2, 2.1F))

        MaxiNetExporterImpl(graph, defaultWriter).exportGraph()

        verify { defaultWriter.write("s0 = topo.addSwitch(\"s0\")") }
        verify { defaultWriter.write("r1 = topo.addSwitch(\"r1\")") }
        verify {
            defaultWriter.write(
                "h2 = topo.addHost(\"h2\", cls=Docker, ip=\"1.2.3.5\", dimage=\"name:tag\", mem_limit=1)"
            )
        }
    }

    @Test
    fun `connections between nodes without emulation on both sides should be written as defined in maxinet`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(123)
        val edge1 = graph.createEdgeNode(1, system)
        val device2 = graph.createEdgeDeviceNode(2, system, DeviceContainer("name", "tag", 1, 1F, 2, 2.1F))
        graph.createEdge(3, edge1, device2, 1.5F, 1025F)

        MaxiNetExporterImpl(graph, defaultWriter).exportGraph()

        verify { defaultWriter.write("r1 = topo.addSwitch(\"r1\")") }
        verify {
            defaultWriter.write(
                "h2 = topo.addHost(\"h2\", cls=Docker, ip=\"1.2.3.5\", dimage=\"name:tag\", mem_limit=1)"
            )
        }
        verify { defaultWriter.write("topo.addLink(r1, h2, delay='1.500000ms', bw=1025.000000)") }
    }

    @Test
    fun `connections between nodes with emulation on both sides should be written as defined in maxinet`() {
        val graph = Graph(defaultConfig)
        val system = graph.getOrCreateAutonomousSystem(123)
        val edge1 = graph.createEdgeNode(1, system)
        edge1.setEmulationNode(EmulationNode("4.3.2.1", FogContainer("name", "latest", 1, 1F, 1, 1F)))
        val device2 = graph.createEdgeDeviceNode(2, system, DeviceContainer("name", "tag", 1, 1F, 2, 2.1F))
        graph.createEdge(3, edge1, device2, 1.5F, 1025F)

        MaxiNetExporterImpl(graph, defaultWriter).exportGraph()

        verify { defaultWriter.write("c0 = topo.addSwitch(\"c0\")") }
        verify { defaultWriter.write("topo.addLink(r1, c0, delay='0.750000ms', bw=1025.000000)") }
        verify { defaultWriter.write("topo.addLink(c0, h2, delay='0.750000ms', bw=1025.000000)") }
    }
}