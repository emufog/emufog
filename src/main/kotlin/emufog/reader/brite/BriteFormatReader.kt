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
class BriteFormatReader : GraphReader {

    /**
     * Reads in a new graph object from a brite format file. This call only supports one file.
     *
     * @param files the list of input files must only contain one file path
     * @throws BriteFormatException throw if format does not match the BRITE standard
     */
    @Throws(IOException::class)
    override fun readGraph(files: List<Path>): Graph {
        require(files.isNotEmpty()) { "No files given to read in." }
        require(files.size == 1) { "The BRITE reader only supports one input file." }

        val graph = Graph(Config.config)

        val reader = files[0].toFile().bufferedReader()

        var line: String? = reader.readLine()
        while (line != null) {
            // read in the nodes of the graph
            if (line.startsWith("Nodes:")) {
                extractNodes(graph, reader)
            }

            // read in the edges of the graph
            if (line.startsWith("Edges:")) {
                extractEdges(graph, reader)
            }

            line = reader.readLine()
        }

        return graph
    }

    companion object {

        /**
         * number of columns defined for a line containing a node
         */
        private const val NODE_COLUMNS = 7

        /**
         * number of columns defined for a line containing an edge
         */
        private const val EDGE_COLUMNS = 9

        /**
         * Reads in all the nodes from the BRITE file and adds them to the given graph.
         */
        @Throws(IOException::class)
        private fun extractNodes(graph: Graph, reader: BufferedReader) {
            var line: String? = reader.readLine()
            while (!line.isNullOrBlank()) {
                // split the line into pieces and parse them separately
                val values = line.split("\t".toRegex())
                if (values.size < NODE_COLUMNS) {
                    throw BriteFormatException(
                        "The node line '$line' does not contain $NODE_COLUMNS columns."
                    )
                }

                val id: Int
                try {
                    id = values[0].toInt()
                } catch (e: NumberFormatException) {
                    throw BriteFormatException("Failed to parse the id: ${values[0]}", e)
                }

                val asId: Int
                try {
                    asId = values[5].toInt()
                } catch (e: NumberFormatException) {
                    throw BriteFormatException(
                        "Failed to parse the autonomous system: ${values[5]}",
                        e
                    )
                }

                // create a new edge node
                graph.createEdgeNode(id, graph.getOrCreateAutonomousSystem(asId))
                line = reader.readLine()
            }
        }

        /**
         * Reads in all the edges from the BRITE file and adds them to the given graph. The
         * required nodes have to present in the given graph.
         */
        @Throws(IOException::class)
        private fun extractEdges(graph: Graph, reader: BufferedReader) {
            var line: String? = reader.readLine()
            while (!line.isNullOrBlank()) {
                // split the line into pieces and parse them separately
                val values = line.split("\t".toRegex())
                if (values.size < EDGE_COLUMNS) {
                    throw BriteFormatException(
                        "The edge node '$line' does not contain $EDGE_COLUMNS columns."
                    )
                }

                val id: Int
                try {
                    id = values[0].toInt()
                } catch (e: NumberFormatException) {
                    throw BriteFormatException("Failed to parse the id: ${values[0]}", e)
                }

                val from: Int
                try {
                    from = values[1].toInt()
                } catch (e: NumberFormatException) {
                    throw BriteFormatException(
                        "Failed to parse the link's source id: ${values[1]}",
                        e
                    )
                }

                val to: Int
                try {
                    to = values[2].toInt()
                } catch (e: NumberFormatException) {
                    throw BriteFormatException(
                        "Failed to parse the link's destinations id: ${values[2]}",
                        e
                    )
                }

                val delay: Float
                try {
                    delay = values[4].toFloat()
                } catch (e: NumberFormatException) {
                    throw BriteFormatException(
                        "Failed to parse the link's latency: ${values[4]}",
                        e
                    )
                }

                val bandwidth: Float
                try {
                    bandwidth = values[5].toFloat()
                } catch (e: NumberFormatException) {
                    throw BriteFormatException(
                        "Failed to parse the link's bandwidth: ${values[5]}",
                        e
                    )
                }

                // get the source and destination nodes from the existing graph
                val fromNode = graph.getEdgeNode(from)
                val toNode = graph.getEdgeNode(to)
                // create the new edge
                graph.createEdge(id, fromNode, toNode, delay, bandwidth)
                line = reader.readLine()
            }
        }
    }
}
