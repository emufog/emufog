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
package emufog.reader.brite;

import emufog.config.Config;
import emufog.graph.AS;
import emufog.graph.EdgeNode;
import emufog.graph.Graph;
import emufog.reader.GraphReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static emufog.util.StringUtils.nullOrEmpty;

/**
 * The reader reads in a graph object from the BRITE file format specified
 * in the documentation (https://www.cs.bu.edu/brite/user_manual/node29.html).
 */
public class BriteFormatReader implements GraphReader {

    /**
     * number of columns defined for a line containing a node
     */
    private static final int NODE_COLUMNS = 7;

    /**
     * number of columns defined for a line containing an edge
     */
    private static final int EDGE_COLUMNS = 9;

    /**
     * Reads in all the nodes from the BRITE file and adds them to the given graph.
     *
     * @param graph  graph to add the nodes to
     * @param reader reader at the position to start
     * @throws IOException          in case of an I/O error
     * @throws BriteFormatException throw if format does not match the BRITE standard
     */
    private static void extractNodes(Graph graph, BufferedReader reader) throws IOException {
        for (String line = reader.readLine(); !nullOrEmpty(line); line = reader.readLine()) {
            // split the line into pieces and parse them separately
            String[] values = line.split("\t");
            if (values.length < NODE_COLUMNS) {
                throw new BriteFormatException("The node line '" + line + "' does not contain "
                    + NODE_COLUMNS + " columns.");
            }

            int id;
            try {
                id = Integer.parseInt(values[0]);
            } catch (NumberFormatException e) {
                throw new BriteFormatException("Failed to parse the id: " + values[0], e);
            }
            int as;
            try {
                as = Integer.parseInt(values[5]);
            } catch (NumberFormatException e) {
                throw new BriteFormatException("Failed to parse the autonomous system: " + values[5], e);
            }
            AS system = graph.getOrCreateAutonomousSystem(as);
            // create a new edge node
            graph.createEdgeNode(id, system);
        }
    }

    /**
     * Reads in all the edges from the BRITE file and adds them to the given graph.
     * The required nodes have to present in the given graph.
     *
     * @param graph  graph to add the edges to
     * @param reader reader at the position to start
     * @throws IOException          in case of an I/O error
     * @throws BriteFormatException throw if format does not match the BRITE standard
     */
    private static void extractEdges(Graph graph, BufferedReader reader) throws IOException {
        for (String line = reader.readLine(); !nullOrEmpty(line); line = reader.readLine()) {
            // split the line into pieces and parse them separately
            String[] values = line.split("\t");
            if (values.length < EDGE_COLUMNS) {
                throw new BriteFormatException("The edge node '" + line + "' does not contain "
                    + EDGE_COLUMNS + " columns.");
            }

            int id;
            try {
                id = Integer.parseInt(values[0]);
            } catch (NumberFormatException e) {
                throw new BriteFormatException("Failed to parse the id: " + values[0], e);
            }
            int from;
            try {
                from = Integer.parseInt(values[1]);
            } catch (NumberFormatException e) {
                throw new BriteFormatException("Failed to parse the link's source id: " + values[1], e);
            }
            int to;
            try {
                to = Integer.parseInt(values[2]);
            } catch (NumberFormatException e) {
                throw new BriteFormatException("Failed to parse the link's destinations id: " + values[2], e);
            }
            float delay;
            try {
                delay = Float.parseFloat(values[4]);
            } catch (NumberFormatException e) {
                throw new BriteFormatException("Failed to parse the link's latency: " + values[4], e);
            }
            float bandwidth;
            try {
                bandwidth = Float.parseFloat(values[5]);
            } catch (NumberFormatException e) {
                throw new BriteFormatException("Failed to parse the link's bandwidth: " + values[5], e);
            }

            // get the source and destination nodes from the existing graph
            EdgeNode fromNode = graph.getEdgeNode(from);
            EdgeNode toNode = graph.getEdgeNode(to);
            // create the new edge
            graph.createEdge(id, fromNode, toNode, delay, bandwidth);
        }
    }

    /**
     * Reads in a new graph object
     *
     * @param files list of files to read in
     * @return read in graph object
     * @throws IOException              in case of an I/O error
     * @throws IllegalArgumentException thrown if the given list of input files
     * @throws BriteFormatException     throw if format does not match the BRITE standard
     */
    @Override
    public Graph readGraph(List<Path> files) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files given to read in.");
        }
        if (files.size() != 1) {
            throw new IllegalArgumentException("The BRITE reader only supports one input file.");
        }

        Graph graph = new Graph(Config.getConfig());

        BufferedReader reader = Files.newBufferedReader(files.get(0));

        String line = reader.readLine();
        while (line != null) {
            // read in the nodes of the graph
            if (line.startsWith("Nodes:")) {
                extractNodes(graph, reader);
            }

            // read in the edges of the graph
            if (line.startsWith("Edges:")) {
                extractEdges(graph, reader);
            }

            line = reader.readLine();
        }

        return graph;
    }
}
