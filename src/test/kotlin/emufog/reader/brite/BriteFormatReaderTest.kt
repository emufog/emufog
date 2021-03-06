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
package emufog.reader.brite

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertThrows
import java.nio.file.Paths
import kotlin.math.abs

@TestInstance(Lifecycle.PER_CLASS)
internal class BriteFormatReaderTest {

    private val resourcePath = Paths.get("src", "test", "resources", "brite")

    private val defaultBaseAddress = "1.2.3.4"

    @Test
    fun `empty list of files should fail`() {
        assertThrows<IllegalArgumentException> {
            BriteFormatReader.readGraph(emptyList(), defaultBaseAddress)
        }
    }

    @Test
    fun `multiple files should fail`() {
        assertThrows<IllegalArgumentException> {
            BriteFormatReader.readGraph(listOf(Paths.get("file1"), Paths.get("file2")), defaultBaseAddress)
        }
    }

    private inline fun <reified T : Exception> assertExceptionForFile(file: String) {
        assertThrows<T> {
            BriteFormatReader.readGraph(listOf(resourcePath.resolve(file)), defaultBaseAddress)
        }
    }

    private fun assertBriteExceptionFor(file: String) = assertExceptionForFile<BriteFormatException>(file)

    private fun assertStateExceptionFor(file: String) = assertExceptionForFile<IllegalStateException>(file)

    @Test
    fun `too few node columns should fail`() {
        assertBriteExceptionFor("topo_nodes_column_few.brite")
    }

    @Test
    fun `too few edge columns should fail`() {
        assertBriteExceptionFor("topo_edges_column_few.brite")
    }

    @Test
    fun `wrong format for node id should fail`() {
        assertBriteExceptionFor("topo_nodes_id_format.brite")
    }

    @Test
    fun `wrong format for node as id should fail`() {
        assertBriteExceptionFor("topo_nodes_as_id_format.brite")
    }

    @Test
    fun `wrong format for edge id should fail`() {
        assertBriteExceptionFor("topo_edges_id_format.brite")
    }

    @Test
    fun `wrong format for edge from id should fail`() {
        assertBriteExceptionFor("topo_edges_from_id_format.brite")
    }

    @Test
    fun `wrong format for edge to id should fail`() {
        assertBriteExceptionFor("topo_edges_to_id_format.brite")
    }

    @Test
    fun `wrong format for latency should fail`() {
        assertBriteExceptionFor("topo_edges_latency_format.brite")
    }

    @Test
    fun `wrong format for bandwidth should fail`() {
        assertBriteExceptionFor("topo_edges_bandwidth_format.brite")
    }

    @Test
    fun `missing from node id should fail`() {
        assertStateExceptionFor("topo_edges_missing_from.brite")
    }

    @Test
    fun `missing to node id should fail`() {
        assertStateExceptionFor("topo_edges_missing_to.brite")
    }

    @Test
    fun `read a sample topology in`() {
        val file = resourcePath.resolve("topo.brite")
        val graph = BriteFormatReader.readGraph(listOf(file), defaultBaseAddress)
        assertEquals(20, graph.edgeNodes.size)
        assertEquals(0, graph.backboneNodes.size)
        assertEquals(0, graph.hostDevices.size)
        assertEquals(37, graph.edges.size)

        // test sample node
        val node11 = graph.getEdgeNode(11)
        requireNotNull(node11)
        assertEquals(11, node11.id)
        assertEquals(-1, node11.system.id)

        // test sample edge
        val edge23 = graph.edges.firstOrNull { it.id == 23 }
        requireNotNull(edge23)
        assertEquals(13, edge23.source.id)
        assertEquals(2, edge23.destination.id)
        assertTrue(floatEquals(0.8399260491500409F, edge23.latency))
        assertTrue(floatEquals(10F, edge23.bandwidth))
    }

    @Test
    fun `separate autonomous systems should contain their resp nodes`() {
        val file = resourcePath.resolve("topo2.brite")
        val graph = BriteFormatReader.readGraph(listOf(file), defaultBaseAddress)

        assertEquals(500, graph.edgeNodes.size)
        assertEquals(0, graph.backboneNodes.size)
        assertEquals(0, graph.hostDevices.size)
        assertEquals(1000, graph.edges.size)

        val system1 = graph.getAutonomousSystem(-1)
        requireNotNull(system1)
        assertEquals(380, system1.edgeNodes.size)
        val system2 = graph.getAutonomousSystem(42)
        requireNotNull(system2)
        assertEquals(120, system2.edgeNodes.size)
    }

    private fun floatEquals(x: Float, y: Float): Boolean {
        return abs(x - y) < 0.00001F
    }
}