package emufog.reader;

import emufog.graph.Graph;
import emufog.graph.Router;
import emufog.settings.Settings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * The reader reads in a graph object from the BRITE file format specified
 * in the documentation (https://www.cs.bu.edu/brite/user_manual/node29.html).
 */
public class BriteFormatReader extends GraphReader {

    /**
     * Creates a new BriteFormatReader to read in the BRITE format.
     * The settings provided will be used for the read in graph.
     *
     * @param settings settings for the newly generated graph
     */
    public BriteFormatReader(Settings settings) {
        super(settings);
    }

    /**
     * Reads in all the nodes from the BRITE file and adds them to the given graph.
     *
     * @param graph  graph to add the nodes to
     * @param reader reader at the position to start
     * @throws IOException in case of an I/O error
     */
    private static void extractNodes(Graph graph, BufferedReader reader) throws IOException {
        String line = reader.readLine();

        while (line != null && !line.isEmpty()) {
            // split the line into pieces and parse them separately
            String[] values = line.split("\t");
            if (values.length >= 7) {
                int id = Integer.parseInt(values[0]);
                int as = Integer.parseInt(values[5]);
                // create a new router object
                graph.createRouter(id, as);
            }

            line = reader.readLine();
        }
    }

    /**
     * Reads in all the edges from the BRITE file and adds them to the given graph.
     * The required nodes have to present in the given graph.
     *
     * @param graph  graph to add the edges to
     * @param reader reader at the position to start
     * @throws IOException in case of an I/O error
     */
    private static void extractEdges(Graph graph, BufferedReader reader) throws IOException {
        String line = reader.readLine();

        while (line != null && !line.isEmpty()) {
            // split the line into pieces and parse them separately
            String[] values = line.split("\t");
            if (values.length >= 9) {
                int id = Integer.parseInt(values[0]);
                int from = Integer.parseInt(values[1]);
                int to = Integer.parseInt(values[2]);
                float delay = Float.parseFloat(values[4]);
                float bandwidth = Float.parseFloat(values[5]);

                // get the source and destination nodes from the existing graph
                Router fromNode = graph.getRouter(from);
                Router toNode = graph.getRouter(to);
                if (fromNode != null && toNode != null) {
                    // create the new edge object
                    graph.createEdge(id, fromNode, toNode, delay, bandwidth);
                }
            }

            line = reader.readLine();
        }
    }

    @Override
    public Graph readGraph(List<Path> files) throws IOException, IllegalArgumentException {
        if (files == null || files.size() < 1) {
            throw new IllegalArgumentException("No files given to read in.");
        }
        Graph graph = new Graph(settings);

        BufferedReader reader = new BufferedReader(new FileReader(files.get(0).toFile()));

        String currentLine = reader.readLine();
        while (currentLine != null) {
            // read in the nodes of the graph
            if (currentLine.startsWith("Nodes:")) {
                extractNodes(graph, reader);
            }

            // read in the edges of the graph
            if (currentLine.startsWith("Edges:")) {
                extractEdges(graph, reader);
            }

            currentLine = reader.readLine();
        }

        return graph;
    }
}
