package emufog.reader;

import emufog.graph.CoordinateGraph;
import emufog.graph.Graph;
import emufog.graph.ILatencyCalculator;
import emufog.graph.Node;
import emufog.settings.Settings;
import emufog.util.Logger;
import emufog.util.LoggerLevel;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This reader can read in the CAIDA topology an build a graph based on that data.
 */
public class CaidaFormatReader extends GraphReader {

    /* number of times AS field exceeds the Integer range */
    private int asOutOfRange;

    /* number of times no node has been found with an ID to assign an AS */
    private int noNodeFoundForAS;

    /* number of times no node has been found with an ID to establish a connection */
    private int noNodeFoundForEdge;

    /* number of times ID field exceeds the Integer range */
    private int idOutOfRange;

    /* number of times a node line was skipped */
    private int nodeLineSkipped;

    /* number of times a AS line was skipped */
    private int asLineSkipped;

    /* number of times a link line was skipped */
    private int linkLineSkipped;

    /* number of times coordinate field exceeds the Float range */
    private int coordinatesOutOfRange;

    /* charset to read in the caida files */
    private final Charset charset;

    /* mapping from ID's to coordinates of the nodes */
    private Map<Integer, Coordinates> nodeCoordinates;

    /**
     * Creates a new reader for the Caida topology. The given settings are used to create a new graph.
     *
     * @param settings settings to use for the read in graph.
     */
    public CaidaFormatReader(Settings settings) {
        super(settings);

        // initialize charset according to https://en.wikipedia.org/wiki/ISO/IEC_8859-1
        charset = Charset.forName("ISO-8859-1");
    }

    @Override
    public Graph readGraph(List<Path> files) throws IOException, IllegalArgumentException {
        Path nodesFile = getPathForEnding(files, ".nodes.geo");
        if (nodesFile == null) {
            throw new IllegalArgumentException("The given files do not contain a .nodes.geo file.");
        }
        Path asFile = getPathForEnding(files, ".nodes.as");
        if (asFile == null) {
            throw new IllegalArgumentException("The given files do not contain a .nodes.as file.");
        }
        Path linkFile = getPathForEnding(files, ".links");
        if (linkFile == null) {
            throw new IllegalArgumentException("The given files do not contain a .links file.");
        }

        // initialize error counts
        asOutOfRange = 0;
        coordinatesOutOfRange = 0;
        idOutOfRange = 0;
        noNodeFoundForAS = 0;
        noNodeFoundForEdge = 0;
        nodeLineSkipped = 0;
        asLineSkipped = 0;
        linkLineSkipped = 0;

        int counter;
        try (Stream<String> lines = Files.lines(asFile, charset)) {
            counter = (int) lines.count();
        }

        nodeCoordinates = new HashMap<>(counter);
        CoordinateGraph graph = new CoordinateGraph(settings);
        ILatencyCalculator calculator = new CaidaLatencyCalculator();

        // read in the nodes
        try (Stream<String> lines = Files.lines(nodesFile, charset)) {
            lines.forEach(this::processNodeLine);
        }

        // read in the AS
        try (Stream<String> lines = Files.lines(asFile, charset)) {
            lines.forEach(x -> processASLine(graph, x));
        }

        // read in the edges
        try (Stream<String> lines = Files.lines(linkFile, charset)) {
            lines.forEach(x -> processLinkLine(graph, x, calculator));
        }

        // log errors
        logResults();

        return graph;
    }

    /**
     * Logs the errors and not matching IDs while reading.
     */
    private void logResults() {
        Logger logger = Logger.getInstance();

        logger.log("ID out of Integer range: " + idOutOfRange, LoggerLevel.ADVANCED);
        logger.log("AS out of Integer range: " + asOutOfRange, LoggerLevel.ADVANCED);
        logger.log("Coordinates out of Float range: " + coordinatesOutOfRange, LoggerLevel.ADVANCED);
        logger.log("Number of times no nodes were found to assign an AS: " + noNodeFoundForAS, LoggerLevel.ADVANCED);
        logger.log("Number of times no nodes were found to build an edge: " + noNodeFoundForEdge, LoggerLevel.ADVANCED);
        logger.log("Nodes read without an AS: " + nodeCoordinates.size(), LoggerLevel.ADVANCED);
        logger.logSeparator();
        logger.log("Number of node lines skipped: " + nodeLineSkipped, LoggerLevel.ADVANCED);
        logger.log("Number of AS lines skipped: " + asLineSkipped, LoggerLevel.ADVANCED);
        logger.log("Number of link lines skipped: " + linkLineSkipped, LoggerLevel.ADVANCED);
    }

    /**
     * Reads in an edge of the graph.
     *
     * @param graph graph to add the edge to
     * @param line  current line to process
     */
    private void processLinkLine(CoordinateGraph graph, String line, ILatencyCalculator calculator) {
        if (line.startsWith("link ")) {
            String[] values = line.split(" ");
            if (values.length >= 4) {
                String linkStr = values[1];
                linkStr = linkStr.substring(1, linkStr.length() - 1);
                int id;
                try {
                    id = Integer.parseInt(linkStr);
                } catch (NumberFormatException e) {
                    idOutOfRange++;
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
                        idOutOfRange++;
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
                        idOutOfRange++;
                        return;
                    }

                    Node from = graph.getRouter(sourceID);
                    Node to = graph.getRouter(destinationID);

                    if (from != null && to != null) {
                        graph.createEdge(id, from, to, calculator, 1000);
                    } else {
                        noNodeFoundForEdge++;
                    }
                }
            } else {
                linkLineSkipped++;
            }
        }
    }

    /**
     * Adapts the AS field of the node identified in the current line.
     *
     * @param graph graph to modify the nodes from
     * @param line  current line to process
     */
    private void processASLine(CoordinateGraph graph, String line) {
        if (line.startsWith("node.AS ")) {
            String[] values = line.split(" ");
            if (values.length >= 3) {
                String nodeStr = values[1];
                nodeStr = nodeStr.substring(1);
                int id;
                try {
                    id = Integer.parseInt(nodeStr);
                } catch (NumberFormatException e) {
                    idOutOfRange++;
                    return;
                }
                int as;
                try {
                    as = Integer.parseInt(values[2]);
                } catch (NumberFormatException e) {
                    asOutOfRange++;
                    return;
                }

                Coordinates coordinates = null;
                try {
                    coordinates = nodeCoordinates.get(id);
                } catch (IllegalArgumentException e) {
                    noNodeFoundForAS++;
                }
                if (coordinates == null) {
                    noNodeFoundForAS++;
                } else {
                    graph.createRouter(id, as, coordinates.xPos, coordinates.yPos);
                    nodeCoordinates.remove(id);
                }
            } else {
                asLineSkipped++;
            }
        }
    }

    /**
     * Reads in and process a line of the input file to add a node to the graph given.
     *
     * @param line current line to process
     */
    private void processNodeLine(String line) {
        if (line.startsWith("node.geo ")) {
            String[] values = line.split("\t");
            if (values.length >= 7) {
                String nodeStr = values[0];
                nodeStr = nodeStr.substring(10, nodeStr.length() - 1);
                int id;
                try {
                    id = Integer.parseInt(nodeStr);
                } catch (NumberFormatException e) {
                    idOutOfRange++;
                    return;
                }

                float xPos, yPos;
                try {
                    xPos = Float.parseFloat(values[5]);
                    yPos = Float.parseFloat(values[6]);
                } catch (NumberFormatException e) {
                    coordinatesOutOfRange++;
                    return;
                }

                nodeCoordinates.put(id, new Coordinates(xPos, yPos));
            } else {
                nodeLineSkipped++;
            }
        }
    }

    /**
     * Returns the path of the file matching the giving file extension at the end.
     *
     * @param files         list of possible files
     * @param fileExtension file extension to match
     * @return the file of the list matching the extension or null if not found
     */
    private static Path getPathForEnding(List<Path> files, String fileExtension) {
        Optional<Path> query = files.stream().filter(x -> x.toString().endsWith(fileExtension)).findFirst();

        return query.orElse(null);
    }

    /**
     * Coordinates of a node in the graph. This class gets mapped to the respective ID of the node.
     */
    class Coordinates {

        /* x coordinate */
        final float xPos;

        /* y coordinate */
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

    /**
     * Latency calculator for the Caida topology.
     */
    class CaidaLatencyCalculator implements ILatencyCalculator {

        @Override
        public float getLatency(float x1, float y1, float x2, float y2) {
            return 1.f;
        }
    }
}
