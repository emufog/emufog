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
package emufog.fog;

import emufog.container.FogType;
import emufog.graph.*;
import emufog.util.Tuple;

import java.util.*;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static emufog.util.ConversionsUtils.intervalToString;

/**
 * The worker class identifies fog nodes on the given AS. Therefore the worker uses a partly
 * sub graph to identify the fog nodes in a greedy algorithm.
 */
abstract class Worker implements Callable<FogResult> {

    private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

    /* AS associated for this worker */
    private final AS as;

    /* master classifier class synchronizing the remaining nodes to place */
    private final FogNodeClassifier classifier;

    /* mapping of fog types to all nodes with this type */
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
     * Calculates the costs and predecessors for the given node object.
     *
     * @param g         fog graph to set costs and predecessors in
     * @param n         current node to process
     * @param threshold cost function threshold
     */
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
                            }
                            else if (nextCosts < neighborCosts) {
                                // update an already discovered node
                                neighborNode.updatePredecessor(edgeNode, current, nextCosts);
                            }
                        }
                    }
                }
            }
        }

        long end = System.nanoTime();
        LOG.info("Time per router to build graph: " + intervalToString(start, end));
    }

    /**
     * Calculates the dependencies of fog levels and returns the first level to start with.
     *
     * @return first fog level to start with
     * @throws Exception
     */
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
            }
            else {
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

    /**
     * This methods checks whether two fog types a and b have the exact same dependencies.
     *
     * @param a first fog type to check
     * @param b second fog type to check
     * @return true if the dependencies are equal, false otherwise
     */
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
            LOG.info("Time to build fog graph for " + as + ": " + intervalToString(start, end));

            while (classifier.fogNodesLeft() && fogGraph.hasEdgeNodes()) {
                start = System.nanoTime();
                FogNode next = fogGraph.getNext();
                end = System.nanoTime();
                LOG.info("Time to find nextLevels fog node for " + as + ": " + intervalToString(start, end));

                // add the new fog node to the partial result
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
            }
            else {
                partResult.setSuccess(true);
            }

            if (partResult.getStatus()) {
                queue.addAll(level.getNextLevels());
            }
        }

        return partResult;
    }
}
