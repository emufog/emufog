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
package emufog.reader.caida;

import emufog.config.Config;
import emufog.graph.AS;
import emufog.graph.Graph;
import emufog.graph.Node;
import emufog.reader.GraphReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This reader can read in the CAIDA topology an build a graph based on that data.
 */
public class CaidaFormatReader implements GraphReader {

    private static final Logger LOG = LoggerFactory.getLogger(CaidaFormatReader.class);

    /**
     * initialize charset according to https://en.wikipedia.org/wiki/ISO/IEC_8859-1
     */
    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    private static final int NODE_COLUMNS = 7;

    private static final int AS_COLUMNS = 3;

    private static final int EDGE_COLUMNS = 4;

    /* number of times AS field exceeds the Integer range */
    private int asNoInteger;

    /* number of times no node has been found with an ID to assign an AS */
    private int noNodeFoundForAS;

    /* number of times no node has been found with an ID to establish a connection */
    private int noNodeFoundForEdge;

    /* number of times ID field exceeds the Integer range */
    private int idsNoInteger;

    /* number of times a node line was skipped */
    private int nodeLineSkipped;

    /* number of times a AS line was skipped */
    private int asLineSkipped;

    /* number of times a link line was skipped */
    private int linkLineSkipped;

    /* number of times coordinate field exceeds the Float range */
    private int coordinatesNoFloats;

    /* mapping from ID's to coordinates of the nodes */
    private Map<Integer, Coordinates> nodeCoordinates;

    @Override
    public Graph readGraph(List<Path> files) throws IOException, IllegalArgumentException {
        if (files == null) {
            throw new IllegalArgumentException("No input files provided.");
        }
        Path nodesFile = getFileWithEnding(files, ".nodes.geo");
        if (nodesFile == null) {
            throw new IllegalArgumentException("The given files do not contain a .nodes.geo file.");
        }
        Path asFile = getFileWithEnding(files, ".nodes.as");
        if (asFile == null) {
            throw new IllegalArgumentException("The given files do not contain a .nodes.as file.");
        }
        Path linkFile = getFileWithEnding(files, ".links");
        if (linkFile == null) {
            throw new IllegalArgumentException("The given files do not contain a .links file.");
        }

        // initialize error counts
        asNoInteger = 0;
        coordinatesNoFloats = 0;
        idsNoInteger = 0;
        noNodeFoundForAS = 0;
        noNodeFoundForEdge = 0;
        nodeLineSkipped = 0;
        asLineSkipped = 0;
        linkLineSkipped = 0;

        nodeCoordinates = new HashMap<>();

        Graph graph = new Graph(Config.getConfig());

        // read in the nodes
        Files.lines(nodesFile, CHARSET).forEach(this::processNodeLine);

        // read in the AS
        Files.lines(asFile, CHARSET).forEach(x -> processASLine(graph, x));

        // read in the edges
        Files.lines(linkFile, CHARSET).forEach(x -> processLinkLine(graph, x));

        // log errors
        logResults();

        return graph;
    }

    /**
     * Logs the errors and not matching IDs while reading.
     */
    private void logResults() {
        // additional logging for debugging
        LOG.debug("ID out of Integer range: {}", idsNoInteger);
        LOG.debug("AS out of Integer range: {}", asNoInteger);
        LOG.debug("Coordinates out of Float range: {}", coordinatesNoFloats);
        LOG.debug("Number of times no nodes were found to assign an AS: {}", noNodeFoundForAS);
        LOG.debug("Number of times no nodes were found to build an edge: {}", noNodeFoundForEdge);
        LOG.debug("Nodes read without an AS: {}", nodeCoordinates.size());
        LOG.debug("Number of node lines skipped: {}", nodeLineSkipped);
        LOG.debug("Number of AS lines skipped: {}", asLineSkipped);
        LOG.debug("Number of link lines skipped: {}", linkLineSkipped);
    }

    /**
     * Reads in an edge of the graph.
     *
     * @param graph graph to add the edge to
     * @param line  current line to process
     */
    private void processLinkLine(Graph graph, String line) {
        String[] values = line.split(" ");
        if (values.length < EDGE_COLUMNS) {
            LOG.debug("There are not {} columns in the link line: {}", EDGE_COLUMNS, line);
            linkLineSkipped++;
            return;
        }

        String linkStr = values[1];
        linkStr = linkStr.substring(1, linkStr.length() - 1);
        int id;
        try {
            id = Integer.parseInt(linkStr);
        } catch (NumberFormatException e) {
            LOG.debug("Failed to parse the link id {} to an integer.", linkStr);
            idsNoInteger++;
            return;
        }

        for (int i = 3; i < values.length - 1; ++i) {
            String sourceStr = values[i];
            int end = sourceStr.indexOf(':');
            if (end == -1) {
                end = sourceStr.length();
            }
            sourceStr = sourceStr.substring(1, end);
            int sourceID;
            try {
                sourceID = Integer.parseInt(sourceStr);
            } catch (NumberFormatException e) {
                LOG.debug("Failed to parse the link's source id {} to an integer.", sourceStr);
                idsNoInteger++;
                return;
            }

            String destinationStr = values[i + 1];
            end = destinationStr.indexOf(':');
            if (end == -1) {
                end = destinationStr.length();
            }
            destinationStr = destinationStr.substring(1, end);
            int destinationID;
            try {
                destinationID = Integer.parseInt(destinationStr);
            } catch (NumberFormatException e) {
                LOG.debug("Failed to parse the link's destination id {} to an integer.", destinationStr);
                idsNoInteger++;
                return;
            }

            Node from = graph.getEdgeNode(sourceID);
            Node to = graph.getEdgeNode(destinationID);

            if (from != null && to != null) {
                graph.createEdge(id, from, to, getLatency(from, to), 1000);
            } else {
                LOG.debug("To create a link source and destination must be found.");
                noNodeFoundForEdge++;
            }
        }
    }

    /**
     * Adapts the AS field of the node identified in the current line.
     *
     * @param graph graph to modify the nodes from
     * @param line  current line to process
     */
    private void processASLine(Graph graph, String line) {
        String[] values = line.split(" ");
        if (values.length < AS_COLUMNS) {
            asLineSkipped++;
            LOG.debug("There are not {} columns in the autonomous system line: {}", AS_COLUMNS, line);
            return;
        }

        String nodeStr = values[1].substring(1);
        int id;
        try {
            id = Integer.parseInt(nodeStr);
        } catch (NumberFormatException e) {
            LOG.debug("Failed to parse the id {} to an integer.", nodeStr);
            idsNoInteger++;
            return;
        }

        int as;
        try {
            as = Integer.parseInt(values[2]);
        } catch (NumberFormatException e) {
            LOG.debug("Failed to parse the autonomous system id {} to an integer.", values[2]);
            asNoInteger++;
            return;
        }

        AS system = graph.getOrCreateAutonomousSystem(as);
        graph.createEdgeNode(id, system);
    }

    /**
     * Reads in and process a line of the input file to add a node to the graph given.
     *
     * @param line current line to process
     */
    private void processNodeLine(String line) {
        String[] values = line.split("\t");
        if (values.length < NODE_COLUMNS) {
            LOG.debug("There are not {} columns in the node line: {}", NODE_COLUMNS, line);
            nodeLineSkipped++;
            return;
        }

        String nodeStr = values[0];
        nodeStr = nodeStr.substring(10, nodeStr.length() - 1);
        int id;
        try {
            id = Integer.parseInt(nodeStr);
        } catch (NumberFormatException e) {
            LOG.debug("Failed to parse the id {} to an integer.", nodeStr);
            idsNoInteger++;
            return;
        }

        try {
            float xPos = Float.parseFloat(values[5]);
            float yPos = Float.parseFloat(values[6]);
            nodeCoordinates.put(id, new Coordinates(xPos, yPos));
        } catch (NumberFormatException e) {
            LOG.debug("Failed to parse coordinates {} and {} to floats.", values[5], values[6]);
            coordinatesNoFloats++;
        }
    }

    /**
     * Returns the path of the file matching the giving file extension at the end.
     *
     * @param files         list of possible files
     * @param fileExtension file extension to match
     * @return the file of the list matching the extension or {@code null} if not found
     */
    private static Path getFileWithEnding(List<Path> files, String fileExtension) {
        return files.stream()
            .filter(f -> f != null && f.toString().endsWith(fileExtension))
            .findFirst()
            .orElse(null);
    }

    private float getLatency(Node from, Node to) {
        return 1.f;
    }

    /**
     * Coordinates of a node in the graph. This class gets mapped to the respective ID of the node.
     */
    static class Coordinates {

        /**
         * x coordinate
         */
        final float xPos;

        /**
         * y coordinate
         */
        final float yPos;

        /**
         * Creates a new coordinate object in a 2D coordinate system.
         *
         * @param x x coordinate
         * @param y y coordinate
         */
        Coordinates(float x, float y) {
            xPos = x;
            yPos = y;
        }
    }
}
