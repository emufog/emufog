package emufog.fog;

import emufog.docker.FogType;
import emufog.graph.*;
import emufog.util.Logger;
import emufog.util.LoggerLevel;
import emufog.util.Tuple;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * The worker class identifies fog nodes on the given AS. Therefore the worker uses a partly
 * sub graph to identify the fog nodes in a greedy algorithm.
 */
abstract class Worker implements Callable<FogResult> {

    /* AS associated for this worker */
    private final AS as;

    /* master classifier class synchronizing the remaining nodes to place */
    private final FogNodeClassifier classifier;

    /* logger for advanced logging */
    private final Logger logger;

    private final Map<FogType, List<FogNode>> fogPlacements;

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
        fogPlacements = new HashMap<>();
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
     * starting nodes up to the threshold specified in the settings.
     *
     * @param level current level to build a fog graph for
     * @return sub graph based on the AS
     */
    private FogGraph buildFogGraph(FogLevel level) {
        FogGraph g = new FogGraph(level.fogTypes);

        List<Tuple<Node, List<EdgeNode>>> startNodes = level.getStartNodes();
        g.initNodes(startNodes, as);
        List<Node> nodes = new ArrayList<>();
        for (Tuple<Node, List<EdgeNode>> t : startNodes) {
            nodes.add(t.getKey());
        }
        iterateNodes(g, nodes, classifier.threshold);
        g.trimNodes();

        return g;
    }

    /**
     * Iterates over the collection of startingNodes and calls the processRouter function on each of them.
     *
     * @param g             graph to apply changes to
     * @param startingNodes collection of startingNodes to process
     * @param t             threshold of cost function
     */
    abstract void iterateNodes(FogGraph g, Collection<Node> startingNodes, float t);

    /**
     * Calculates the costs and predecessors for the given router object.
     *
     * @param g         fog graph to set costs and predecessors in
     * @param r         current router to process
     * @param threshold cost function threshold
     */
    void processNode(FogGraph g, Router r, float threshold) {
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

    void processNode(FogGraph g, Node n, float threshold) {
        long start = System.nanoTime();

        // push the router as a starting point in the queue
        EdgeNode edgeNode = (EdgeNode) g.getNode(n);
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

    private FogLevel getFirstLevel() throws Exception {
        List<FogType> remainingTypes = new ArrayList<>(classifier.fogTypes);
        Map<FogType, FogLevel> levelMap = new HashMap<>();

        // build first level
        List<FogType> startLevel = new ArrayList<>();
        for (FogType type : remainingTypes) {
            if (!type.hasDependencies()) {
                startLevel.add(type);
            }
        }
        FogLevel firstLevel = new FogLevel(fogPlacements, startLevel, null);
        firstLevel.addStartingRouters(as.getRouters());

        for (FogType type : startLevel) {
            levelMap.put(type, firstLevel);
        }
        remainingTypes.removeAll(startLevel);

        while (!remainingTypes.isEmpty()) {
            List<FogType> possibleNextLevels = new ArrayList<>();

            for (FogType type : remainingTypes) {

                boolean valid = true;
                for (int i = 0; i < type.dependencies.size() && valid; ++i) {
                    valid = !remainingTypes.contains(type.dependencies.get(i));
                }

                if (valid) {
                    possibleNextLevels.add(type);
                }
            }

            if (remainingTypes.isEmpty()) {
                throw new Exception("TODO"); //TODO
            } else {
                FogType next = possibleNextLevels.get(0);
                possibleNextLevels.remove(0);
                List<FogType> duplicates = new ArrayList<>();

                // find duplicates
                for (FogType type : possibleNextLevels) {
                    if (haveSameDependencies(next, type)) {
                        duplicates.add(type);
                    }
                }

                duplicates.add(next);
                Set<FogLevel> predecessors = new HashSet<>();
                for (FogType type : next.dependencies) {
                    predecessors.add(levelMap.get(type));
                }
                FogLevel nextLevel = new FogLevel(fogPlacements, duplicates, predecessors);
                for (FogType type : duplicates) {
                    levelMap.put(type, nextLevel);
                    remainingTypes.remove(type);
                }
            }
        }

        return firstLevel;
    }

    private static boolean haveSameDependencies(FogType a, FogType b) {
        return a.dependencies.containsAll(b.dependencies) && b.dependencies.containsAll(a.dependencies);
    }

    @Override
    public FogResult call() throws Exception {
        // init partial result for this AS
        FogResult partResult = new FogResult();

        // create fog levels
        Queue<FogLevel> queue = new ArrayDeque<>();
        queue.add(getFirstLevel());

        partResult.setSuccess(true);
        while (!queue.isEmpty() && partResult.getStatus()) {
            FogLevel level = queue.poll();

            // build an initial sub graph will all edge routers
            long start = System.nanoTime();
            FogGraph fogGraph = buildFogGraph(level);
            long end = System.nanoTime();
            logger.log("Time to build fog graph for " + as + ": " + Logger.convertToMs(start, end),
                    LoggerLevel.ADVANCED);

            while (classifier.fogNodesLeft() && fogGraph.hasEdgeNodes()) {
                start = System.nanoTime();
                FogNode next = fogGraph.getNext();
                end = System.nanoTime();
                logger.log("Time to find nextLevels fog node for " + as + ": " + Logger.convertToMs(start, end),
                        LoggerLevel.ADVANCED);

                // add the new fog node to the partial result
                next.setSelected();
                partResult.addFogNode(next);
                classifier.reduceRemainingNodes();
                // add placement to the map
                List<FogNode> nodes = fogPlacements.computeIfAbsent(next.getFogType(), k -> new ArrayList<>());
                nodes.add(next);
            }

            // check if result is positive
            if (fogGraph.hasEdgeNodes() && !classifier.fogNodesLeft()) {
                partResult.setSuccess(false);
                partResult.clearFogNodes();
            } else {
                partResult.setSuccess(true);
            }

            if (partResult.getStatus()) {
                queue.addAll(level.getNextLevels());
            }
        }

        return partResult;
    }
}
