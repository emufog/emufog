package emufog.fog;

import emufog.graph.*;
import emufog.util.Logger;
import emufog.util.LoggerLevel;

import java.util.Collection;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

/**
 * The worker class identifies fog nodes on the given AS. Therefore the worker uses a partly
 * sub graph to identify the fog nodes in a greedy algorithm.
 */
abstract class Worker implements Callable<FogResult> {

    /* AS associated for this worker */
    private final AS as;

    /* master classifier class synchronizing the remaining nodes to place */
    protected final FogNodeClassifier classifier;

    /* logger for advanced logging */
    private final Logger logger;

    /**
     * Creates a new worker to identify fog nodes in the given AS.
     *
     * @param as         AS to cover by this worker
     * @param classifier master classifier synchronizing the remaining nodes to place
     */
    Worker(AS as, FogNodeClassifier classifier) {
        this.as = as;
        this.classifier = classifier;
        logger = Logger.getInstance();
    }

    /**
     * Calculates the costs for a given edge of the graph.
     *
     * @param edge edge to calculate the costs for
     * @return costs of the given edge
     */
    private static float calculateCosts(Edge edge) {
        // currently using delay as a cost function
        return edge.getDelay();
    }

    /**
     * Calculates a graph by using breadth-first starting from the given
     * routers up to the threshold specified in the settings.
     *
     * @return sub graph based on the AS
     */
    private FogGraph buildFogGraph() {
        FogGraph g = new FogGraph(classifier.fogTypes);

        g.initNodes(as);
        iterateRouters(g, as.getRouters(), classifier.threshold);
        g.trimNodes();

        return g;
    }

    /**
     * Iterates over the collection of routers and calls the processRouter function on each of them.
     *
     * @param g       graph to apply changes to
     * @param routers collection of routers to process
     * @param t       threshold of cost function
     */
    abstract void iterateRouters(FogGraph g, Collection<Router> routers, float t);

    /**
     * Calculates the costs and predecessors for the given router object.
     *
     * @param g         fog graph to set costs and predecessors in
     * @param r         current router to process
     * @param threshold cost function threshold
     */
    void processRouter(FogGraph g, Router r, float threshold) {
        if (!r.hasDevices()) {
            return;
        }

        long start = System.nanoTime();

        // push the router as a starting point in the queue
        EdgeNode edgeNode = (EdgeNode) g.getNode(r);
        PriorityQueue<FogNode> queue = new PriorityQueue<>(new CostComparator(edgeNode));
        edgeNode.initPredecessor(edgeNode, edgeNode, 0);
        queue.add(edgeNode);

        // using the dijkstra algorithm to iterate the graph
        while (!queue.isEmpty()) {
            FogNode current = queue.poll();

            float currentCosts = current.getCosts(edgeNode);

            // check all edges leaving the current node
            for (Edge e : current.oldNode.getEdges()) {
                if (!e.isCrossASEdge()) {

                    Node neighbor = e.getDestinationForSource(current.oldNode);

                    // ignore host devices as they are not considered to be possible nodes
                    if (!(neighbor instanceof HostDevice)) {
                        float nextCosts = currentCosts + calculateCosts(e);
                        if (nextCosts <= threshold) {
                            FogNode neighborNode = g.getNode(neighbor);
                            float neighborCosts = neighborNode.getCosts(edgeNode);

                            if (neighborCosts == Float.MAX_VALUE) {
                                // newly discovered node
                                neighborNode.initPredecessor(edgeNode, current, nextCosts);
                                queue.add(neighborNode);
                            } else if (nextCosts < neighborCosts) {
                                // update an already discovered node
                                neighborNode.updatePredecessor(edgeNode, current, nextCosts);
                            }
                        }
                    }
                }
            }
        }

        long end = System.nanoTime();
        logger.log("Time per router to build graph: " + Logger.convertToMs(start, end),
                LoggerLevel.ADVANCED);
    }

    @Override
    public FogResult call() throws Exception {
        // init partial result for this AS
        FogResult partResult = new FogResult();

        // build an initial sub graph will all edge routers
        long start = System.nanoTime();
        FogGraph fogGraph = buildFogGraph();
        long end = System.nanoTime();
        logger.log("Time to build fog graph for " + as + ": " + Logger.convertToMs(start, end),
                LoggerLevel.ADVANCED);

        while (classifier.fogNodesLeft() && fogGraph.hasEdgeNodes()) {
            start = System.nanoTime();
            FogNode next = fogGraph.getNext();
            end = System.nanoTime();
            logger.log("Time to find next fog node for " + as + ": " + Logger.convertToMs(start, end),
                    LoggerLevel.ADVANCED);

            // add the new fog node to the partial result
            partResult.addFogNode(next);
            classifier.reduceRemainingNodes();
        }

        // check if result is positive
        if (fogGraph.hasEdgeNodes() && !classifier.fogNodesLeft()) {
            partResult.setSuccess(false);
            partResult.clearFogNodes();
        } else {
            partResult.setSuccess(true);
        }

        return partResult;
    }
}
