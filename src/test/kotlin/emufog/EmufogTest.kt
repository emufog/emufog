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
package emufog

import emufog.backbone.identifyBackbone
import emufog.config.Config
import emufog.config.readConfig
import emufog.container.DeviceContainer
import emufog.container.FogContainer
import emufog.device.assignDeviceNodes
import emufog.export.maxinet.MaxiNetExporter
import emufog.fog.FogNodePlacement
import emufog.fog.FogResult
import emufog.fog.findPossibleFogNodes
import emufog.graph.Graph
import emufog.reader.brite.BriteFormatReader
import emufog.reader.caida.CaidaFormatReader
import emufog.util.getLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertThrows
import org.slf4j.Logger
import java.lang.System.exit
import java.nio.file.Path

internal class EmufogTest {

    @TestInstance(Lifecycle.PER_CLASS)
    class EmuFogCommandTests {

        private val configPathString = "config.yaml"

        private val typeString = "brite"

        private val file1String = "file1.txt"

        @BeforeAll
        fun initialize() {
            mockkStatic("emufog.util.LoggingKt")
            every { getLogger("Emufog") } returns mockk()

            mockkStatic("java.lang.System")
            every { exit(any()) } throws RuntimeException()
        }

        @AfterAll
        fun cleanUp() {
            unmockkAll()
        }

        @Test
        fun `no parameter at all should abort`() {
            assertThrows<RuntimeException> {
                EmufogCommand().main(emptyArray())
            }
        }

        @Test
        fun `no --config at all should abort`() {
            val array = arrayOf("-t", typeString, "-f", file1String)
            assertThrows<RuntimeException> {
                EmufogCommand().main(array)
            }
        }

        @Test
        fun `no --type at all should abort`() {
            val array = arrayOf("-c", configPathString, "-f", file1String)
            assertThrows<RuntimeException> {
                EmufogCommand().main(array)
            }
        }

        @Test
        fun `no --file at all should abort`() {
            val array = arrayOf("-c", configPathString, "-t", typeString)
            assertThrows<RuntimeException> {
                EmufogCommand().main(array)
            }
        }
    }

    @TestInstance(Lifecycle.PER_CLASS)
    class EmufogExecutionTests {

        private val configPath: Path = mockk()

        private val outputPath: Path = mockk()

        private val config: Config = mockk {
            every { baseAddress } returns "1.2.3.4"
            every { overWriteOutputFile } returns true
        }

        private val log: Logger = mockk {
            every { info(any()) } returns Unit
            every { info(any(), any<Int>()) } returns Unit
            every { debug(any()) } returns Unit
            every { error(any(), any<Exception>()) } returns Unit
        }

        @BeforeAll
        fun initialize() {
            mockkStatic("emufog.config.ConfigKt")
            every { readConfig(configPath) } returns config

            mockkStatic("emufog.util.LoggingKt")
            every { getLogger("Emufog") } returns log

            mockkStatic("emufog.backbone.BackboneKt")
            mockkStatic("emufog.device.DeviceManagerKt")
            mockkStatic("emufog.fog.FogNodeClassifierKt")

            mockkObject(BriteFormatReader)
            mockkObject(MaxiNetExporter)
            every { MaxiNetExporter.exportGraph(any(), any(), any()) } returns Unit
        }

        @AfterAll
        fun cleanUp() {
            unmockkAll()
        }

        @Test
        fun `safelyRun should handle exceptions`() {
            val emufog = spyk(EmufogExecution(configPath, InputFormatTypes.BRITE, outputPath, emptyList()))

            every { emufog.run() } throws Exception("Oh no!")

            emufog.safelyRun()
        }

        @Test
        fun `run an empty graph run`() {
            val graph = Graph("1.2.3.4")
            every { BriteFormatReader.readGraph(any(), "1.2.3.4") } returns graph
            every { identifyBackbone(graph) } returns Unit
            every { assignDeviceNodes(graph, config) } returns Unit
            every { findPossibleFogNodes(graph, config) } returns FogResult().also { it.setSuccess() }

            val emufog = EmufogExecution(configPath, InputFormatTypes.BRITE, outputPath, emptyList())

            emufog.run()

            verify(exactly = 1) { log.info("Number of nodes in the graph: {}", 0) }
            verify(exactly = 1) { log.info("Number of edges in the graph: {}", 0) }
            verify(exactly = 1) { log.info("Number of backbone nodes identified: {}", 0) }
            verify(exactly = 1) { log.info("Number of devices assigned: {}", 0) }
            verify(exactly = 1) { log.info("Number of fog nodes identified: {}", 0) }
        }

        @Test
        fun `run a sample graph`() {
            val graph = spyk(Graph("1.2.3.4"))
            val system = graph.getOrCreateAutonomousSystem(1)
            val edgeNode0 = graph.createEdgeNode(0, system)
            val edgeNode1 = graph.createEdgeNode(1, system)
            val edgeNode2 = graph.createEdgeNode(2, system)
            graph.createEdge(0, edgeNode0, edgeNode1, 1F, 100F)
            graph.createEdge(1, edgeNode1, edgeNode2, 1.2F, 100F)

            every { BriteFormatReader.readGraph(any(), "1.2.3.4") } returns graph

            every { identifyBackbone(graph) } answers {
                system.replaceByBackboneNode(edgeNode2)
            }

            every { assignDeviceNodes(graph, config) } answers {
                val container = DeviceContainer("name", "tag", 1024, 1F, 1, 1F)
                val edgeDeviceNode3 = graph.createEdgeDeviceNode(3, system, container)
                graph.createEdge(2, edgeDeviceNode3, edgeNode0, 0F, 10F)
                val edgeDeviceNode4 = graph.createEdgeDeviceNode(4, system, container)
                graph.createEdge(3, edgeDeviceNode4, edgeNode1, 0F, 10F)
            }

            val fogType = FogContainer("name", "abc", 10, 1F, 5, 2F)
            val placement: FogNodePlacement = mockk {
                every { node } returns edgeNode0
                every { type } returns fogType
            }
            every { findPossibleFogNodes(any(), any()) } returns FogResult().also {
                it.setSuccess()
                it.addPlacement(placement)
            }

            every { graph.placeFogNode(edgeNode0, fogType) } returns Unit

            val emufog = EmufogExecution(configPath, InputFormatTypes.BRITE, outputPath, emptyList())

            emufog.run()

            verify(exactly = 1) { log.info("Number of nodes in the graph: {}", 3) }
            verify(exactly = 1) { log.info("Number of edges in the graph: {}", 2) }
            verify(exactly = 1) { log.info("Number of backbone nodes identified: {}", 1) }
            verify(exactly = 1) { log.info("Number of devices assigned: {}", 2) }
            verify(exactly = 1) { log.info("Number of fog nodes identified: {}", 1) }
        }
    }

    class InputFormatsTests {

        @Test
        fun `getReader should return BriteReader for brite`() {
            val reader = InputFormatTypes.BRITE.getReader()

            assertTrue(reader is BriteFormatReader)
        }

        @Test
        fun `getReader should return CaidaReader for caida`() {
            val reader = InputFormatTypes.CAIDA.getReader()

            assertTrue(reader is CaidaFormatReader)
        }
    }
}