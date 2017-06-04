package backbone.emufog;

import graph.emufog.AS;
import graph.emufog.Edge;
import graph.emufog.Graph;
import graph.emufog.SwitchConverter;
import util.emufog.Logger;
import util.emufog.LoggerLevel;
import util.emufog.Tuple;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * This class runs the backbone classification algorithm on a graph instance.
 */
public class BackboneClassifier {

    /* graph to run the algorithm on */
    private final Graph graph;

    /**
     * Creates a new classifier instance to run on the given graph instance.
     *
     * @param graph graph to run the classification algorithm on
     * @throws IllegalArgumentException if the graph object is null
     */
    public BackboneClassifier(Graph graph) throws IllegalArgumentException {
        if (graph == null) {
            throw new IllegalArgumentException("The graph object is not initialized.");
        }

        this.graph = graph;
    }

    /**
     * Starts the backbone classification algorithm on the associated graph.
     * Returns the graph including backbone and edge of the network.
     *
     * @return the modified graph
     */
    public Graph startClassification() {
        Logger logger = Logger.getInstance();

        // 1st step sequentially
        logger.log("Start Backbone Classification", LoggerLevel.ADVANCED);
        long start = System.nanoTime();
        markASEdgeNodes();
        long stop = System.nanoTime();
        logger.log("Graph Step 1 - Time: " + Logger.convertToMs(start, stop), LoggerLevel.ADVANCED);
        logger.log("Backbone Size: " + graph.getSwitches().size(), LoggerLevel.ADVANCED);
        logger.log("Edge Size: " + graph.getRouters().size(), LoggerLevel.ADVANCED);

        // rest in parallel
        Collection<AS> ASs = graph.getSystems();
        Tuple<AS, Future<?>>[] workers = new Tuple[ASs.size()];

        ExecutorService pool = newFixedThreadPool(graph.getSettings().threadCount);
        int count = 0;
        for (AS as : ASs) {
            BackboneWorker worker = new BackboneWorker(as);
            workers[count] = new Tuple<>(as, pool.submit(worker));
            count++;
        }

        for (Tuple<AS, Future<?>> t : workers) {
            try {
                t.getValue().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                logger.log("Backbone Thread for " + t.getKey() + " was interrupted.", LoggerLevel.ERROR);
                logger.log("Error message: " + e.getMessage(), LoggerLevel.ERROR);
            }
        }
        pool.shutdownNow();
        logger.log("Finished Backbone Classification.", LoggerLevel.ADVANCED);
        logger.logSeparator(LoggerLevel.ADVANCED);

        return graph;
    }

    /**
     * This methods marks all cross-AS edge's endpoints as backbone nodes.
     */
    private void markASEdgeNodes() {
        SwitchConverter converter = new SwitchConverter();

        for (Edge e : graph.getEdges()) {
            if (e.isCrossASEdge()) {
                converter.convert(e.getSource());
                converter.convert(e.getDestination());
            }
        }
    }
}