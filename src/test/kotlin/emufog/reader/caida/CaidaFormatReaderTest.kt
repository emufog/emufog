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
package emufog.reader.caida

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CaidaFormatReaderTest {

    private val resourcePath = Paths.get("src", "test", "resources", "caida")

    private val defaultNodesGeo = resourcePath.resolve("topo.nodes.geo")

    private val defaultNodesAs = resourcePath.resolve("topo.nodes.as")

    private val defaultLinks = resourcePath.resolve("topo.links")

    private val defaultBaseAddress = "1.2.3.4"

    @Test
    fun `empty list of files should fail`() {
        assertThrows<IllegalArgumentException> {
            CaidaFormatReader.readGraph(emptyList(), defaultBaseAddress)
        }
    }

    @Test
    fun `no nodes geo file should fail`() {
        assertThrows<IllegalArgumentException> {
            CaidaFormatReader.readGraph(listOf(defaultNodesAs, defaultLinks), defaultBaseAddress)
        }
    }

    @Test
    fun `no nodes as file should fail`() {
        assertThrows<IllegalArgumentException> {
            CaidaFormatReader.readGraph(listOf(defaultNodesGeo, defaultLinks), defaultBaseAddress)
        }
    }

    @Test
    fun `no links file should fail`() {
        assertThrows<IllegalArgumentException> {
            CaidaFormatReader.readGraph(listOf(defaultNodesAs, defaultNodesGeo), defaultBaseAddress)
        }
    }

    @Test
    fun `should skip lines if number of columns in link file is too small`() {
        expectEmptyLinkList("small_column.links")
    }

    @Test
    fun `should skip lines if the id of a link is no int`() {
        expectEmptyLinkList("id_no_int.links")
    }

    @Test
    fun `should skip lines if the id of a link's source is no int`() {
        expectEmptyLinkList("source_no_int.links")
    }

    @Test
    fun `should skip lines if the id of a link's destination is no int`() {
        expectEmptyLinkList("destination_no_int.links")
    }

    @Test
    fun `should skip lines if the source node is null`() {
        expectEmptyLinkList("missing_source.links")
    }

    @Test
    fun `should skip lines if the destination node is null`() {
        expectEmptyLinkList("missing_destination.links")
    }

    private fun expectEmptyLinkList(file: String) {
        val graph = CaidaFormatReader.readGraph(
            listOf(defaultNodesGeo, resourcePath.resolve(file), defaultNodesAs),
            defaultBaseAddress
        )
        assertEquals(0, graph.edges.size)
    }

    @Test
    fun `should skip lines if the number of columns in as file is too small`() {
        expectEmptyNodeList("small_column.nodes.as")
    }

    @Test
    fun `should skip lines if the id of a node is no int`() {
        expectEmptyNodeList("id_no_int.nodes.as")
    }

    @Test
    fun `should skip lines if the node's system is no int`() {
        expectEmptyNodeList("system_no_int.nodes.as")
    }

    private fun expectEmptyNodeList(file: String) {
        val graph = CaidaFormatReader.readGraph(
            listOf(defaultNodesGeo, defaultLinks, resourcePath.resolve(file)),
            defaultBaseAddress
        )
        assertEquals(0, graph.edgeNodes.size)
    }

    @Test
    fun `should skip lines if the number of columns in geo file is too small`() {
        verifySkipOnCoordinates("small_column.nodes.geo")
    }

    @Test
    fun `should skip lines if the id of a node in geo file is no int`() {
        verifySkipOnCoordinates("id_no_int.nodes.geo")
    }

    @Test
    fun `should skip lines if the node's latitude is no float`() {
        verifySkipOnCoordinates("lat_no_float.nodes.geo")
    }

    @Test
    fun `should skip lines if the node's longitude is no float`() {
        verifySkipOnCoordinates("lon_no_float.nodes.geo")
    }

    private fun verifySkipOnCoordinates(file: String) {
        CaidaFormatReader.readGraph(
            listOf(resourcePath.resolve(file), defaultLinks, defaultNodesAs),
            defaultBaseAddress
        )
    }

    @Test
    fun `read in a sample topology #1`() {
        val graph = CaidaFormatReader.readGraph(
            listOf(defaultNodesGeo, defaultLinks, defaultNodesAs),
            defaultBaseAddress
        )

        assertEquals(10, graph.edgeNodes.size)
        for (i in 1 until 11) {
            assertNotNull(graph.getEdgeNode(i))
        }
        assertEquals(0, graph.backboneNodes.size)
        assertEquals(0, graph.hostDevices.size)
        assertEquals(0, graph.hostDevices.size)

        assertEquals(8, graph.systems.size)
        assertNotNull(graph.getAutonomousSystem(5645))
        assertNotNull(graph.getAutonomousSystem(9381))
        assertNotNull(graph.getAutonomousSystem(1680))
        assertNotNull(graph.getAutonomousSystem(5384))
        assertNotNull(graph.getAutonomousSystem(6057))
        assertNotNull(graph.getAutonomousSystem(17547))
        assertNotNull(graph.getAutonomousSystem(6057))
        assertNotNull(graph.getAutonomousSystem(36149))
        assertNotNull(graph.getAutonomousSystem(1213))

        assertEquals(14, graph.edges.size)
        val link9707 = graph.edges.firstOrNull { it.id == 9707 }
        assertNotNull(link9707)
        assertEquals(9, link9707!!.source.id)
        assertEquals(2, link9707.destination.id)
    }
}