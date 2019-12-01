/*
 * MIT License
 *
 * Copyright (c) 2018 emufog contributors
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

import emufog.config.Config
import emufog.graph.Graph
import emufog.reader.GraphReader
import java.io.BufferedReader
import java.io.IOException
import java.nio.file.Path

/**
 * The reader reads in a graph object from the BRITE file format specified in the documentation
 * (https://www.cs.bu.edu/brite/user_manual/node29.html).
 */
object BriteFormatReader : GraphReader {

    /**
     * Reads in a new graph object from a brite format file. This call only supports one file.
     *
     * @param files the list of input files must only contain one file path
     * @throws BriteFormatException throw if format does not match the BRITE standard
     */
    @Throws(IOException::class, BriteFormatException::class)
    override fun readGraph(files: List<Path>): Graph {
        require(files.isNotEmpty()) { "No files given to read in." }
        require(files.size == 1) { "The BRITE reader only supports one input file." }

        return BriteFormatReaderImpl(files[0]).readGraph()
    }
}

private class BriteFormatReaderImpl internal constructor(path: Path) {

    private companion object {
        /**
         * number of columns defined for a line containing a node
         */
        private const val NODE_COLUMNS = 7

        /**
         * number of columns defined for a line containing an edge
         */
        private const val EDGE_COLUMNS = 9
    }

    private val reader: BufferedReader = path.toFile().bufferedReader()

    private val graph: Graph = Graph(Config.config!!)

    private val regex: Regex = "\t".toRegex()

    private fun nextLine(): String? = reader.readLine()

    @Throws(IOException::class, BriteFormatException::class)
    internal fun readGraph(): Graph {
        var line = nextLine()
        while (line != null) {
            // read in the nodes of the graph
            if (line.startsWith("Nodes:")) {
                parseNodes()
            }

            // read in the edges of the graph
            if (line.startsWith("Edges:")) {
                parseEdges()
            }

            line = nextLine()
        }

        return graph
    }

    /**
     * Reads in all the nodes from the BRITE file and adds them to the given graph.
     */
    @Throws(IOException::class, BriteFormatException::class)
    private fun parseNodes() {
        var line = nextLine()
        while (!line.isNullOrBlank()) {
            // split the line into pieces and parse them separately
            parseNode(line)
            line = nextLine()
        }
    }

    @Throws(BriteFormatException::class)
    private fun parseNode(line: String) {
        val values = line.split(regex)
        if (values.size < NODE_COLUMNS) {
            throw BriteFormatException("The node line '$line' does not contain $NODE_COLUMNS columns.")
        }

        val id = values[0].asInt { "Failed to parse the id: ${values[0]}" }
        val asId = values[5].asInt { "Failed to parse the autonomous system: ${values[5]}" }

        // create a new edge node
        graph.createEdgeNode(id, graph.getOrCreateAutonomousSystem(asId))
    }

    /**
     * Reads in all the edges from the BRITE file and adds them to the given graph. The
     * required nodes have to present in the given graph.
     */
    @Throws(IOException::class, BriteFormatException::class)
    private fun parseEdges() {
        var line = nextLine()
        while (!line.isNullOrBlank()) {
            // split the line into pieces and parse them separately
            parseEdge(line)
            line = nextLine()
        }
    }

    @Throws(BriteFormatException::class)
    private fun parseEdge(line: String) {
        val values = line.split(regex)
        if (values.size < EDGE_COLUMNS) {
            throw BriteFormatException("The edge node '$line' does not contain $EDGE_COLUMNS columns.")
        }

        val id = values[0].asInt { "Failed to parse the id: ${values[0]}" }
        val from = values[1].asInt { "Failed to parse the link's source id: ${values[1]}" }
        val to = values[2].asInt { "Failed to parse the link's destinations id: ${values[2]}" }
        val delay = values[4].asFloat { "Failed to parse the link's latency: ${values[4]}" }
        val bandwidth = values[5].asFloat { "Failed to parse the link's bandwidth: ${values[5]}" }

        // get the source and destination nodes from the existing graph
        val fromNode = graph.getEdgeNode(from)
        require(fromNode != null) { "The link starting node: $from is not part of the graph." }
        val toNode = graph.getEdgeNode(to)
        require(toNode != null) { "The link ending node: $from is not part of the graph." }

        // create the new edge
        graph.createEdge(id, fromNode, toNode, delay, bandwidth)
    }

    @Throws(BriteFormatException::class)
    private fun <T> String.toType(f: (String) -> T, exceptionMsg: () -> String): T {
        try {
            return f(this)
        } catch (e: NumberFormatException) {
            throw BriteFormatException(exceptionMsg(), e)
        }
    }

    @Throws(BriteFormatException::class)
    private fun String.asInt(msg: () -> String) = toType(String::toInt, msg)

    @Throws(BriteFormatException::class)
    private fun String.asFloat(msg: () -> String) = toType(String::toFloat, msg)
}
