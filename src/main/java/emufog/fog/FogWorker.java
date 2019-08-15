/*
 * MIT License
 *
 * Copyright (c) 2019 emufog contributors
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

import emufog.graph.AS;
import emufog.graph.Edge;
import emufog.graph.EdgeDeviceNode;
import emufog.graph.EdgeNode;
import emufog.graph.Node;
import emufog.settings.Settings;
import emufog.util.Tuple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static emufog.util.ConversionsUtils.intervalToString;

/**
 * This class isolates the fog node placement algorithm of one of the autonomous systems
 * to run it independent of others. Requires the autonomous system to process.
 */
class FogWorker {

    private static final Logger LOG = LoggerFactory.getLogger(FogWorker.class);

    /**
     * the autonomous system this worker processes
     */
    private final AS as;

    /**
     * the fog node classifier this worker is associated
     */
    private final FogNodeClassifier classifier;

    /**
     * the settings to use for the fog node placement algorithm
     */
    private final Settings settings;

    /**
     * mapping of nodes from the underlying graph to their respective base nodes
     */
    private final Map<Node, BaseNode> nodes;

    /**
     * Creates new worker for the fog node placement algorithm that processes the given
     * autonomous system. Uses the fog node classifier and its settings to run the
     * algorithm.
     *
     * @param as         autonomous system to process
     * @param classifier fog node classifier the worker is associated
     */
    FogWorker(AS as, FogNodeClassifier classifier) {
        this.as = as;
        this.classifier = classifier;
        settings = classifier.settings;
        nodes = new HashMap<>();
    }

    /**
     * Runs the fog node placement algorithm on the associated autonomous system
     * and returns the overall result object.
     *
     * @return result of the fog node placement
     */
    FogResult findFogNodes() {
        // map edge device nodes to their respective wrappers
        List<StartingNode> startingNodes = as.getEdgeNodes().stream().filter(EdgeNode::hasDevices).map(this::createStartingNode).collect(Collectors.toList());

        long start = System.nanoTime();
        // calculate connection costs from the edge device nodes
        startingNodes.forEach(this::calculateConnectionCosts);
        if (settings.timeMeasuring) {
            LOG.info("Time to calculate connection costs for edge devices for {}: {}", as, intervalToString(start, System.nanoTime()));
        }

        // initialize empty result set
        FogResult result = new FogResult();
        result.setSuccess();

        while (!startingNodes.isEmpty()) {
            // check if there are still fog nodes left to place
            if (!classifier.fogNodesLeft()) {
                result.setFailure();
                return result;
            }

            start = System.nanoTime();
            // find the next fog node for the remaining starting nodes
            BaseNode fogNode = getNextFogNode(startingNodes);
            if (settings.timeMeasuring) {
                LOG.info("Time to find next fog node for {}: {}", as, intervalToString(start, System.nanoTime()));
            }

            // reduce the remaining fog nodes available
            classifier.reduceRemainingNodes();

            start = System.nanoTime();
            // remove all covered nodes from the graph
            removeAllCoveredNodes(fogNode, startingNodes);
            if (settings.timeMeasuring) {
                LOG.info("Time to remove the covered nodes for {}: {}", as, intervalToString(start, System.nanoTime()));
            }

            // add the new fog node to the partial result
            result.addPlacement(new FogNodePlacement(fogNode));
        }

        return result;
    }

    /**
     * Calculates and returns the next fog node to place based on the given list of
     * starting nodes that needs to be covered.
     *
     * @param startingNodes starting nodes that needs to be covered
     * @return the next fog node to place
     */
    private BaseNode getNextFogNode(List<StartingNode> startingNodes) {
        LOG.debug("Remaining starting nodes to cover for {}: {}", as, startingNodes.size());

        long start = System.nanoTime();
        // find the optimal fog type for the remaining nodes in the graph
        List<BaseNode> fogNodes = new ArrayList<>(nodes.values());
        fogNodes.forEach(n -> n.findFogType(settings.fogNodeTypes));
        if (settings.timeMeasuring) {
            LOG.info("Time to find possible fog types for {}: {}", as, intervalToString(start, System.nanoTime()));
        }

        start = System.nanoTime();
        // sort the possible fog nodes with a FogComparator
        fogNodes.sort(new FogComparator());
        // retrieve the nextLevels optimal node
        BaseNode next = fogNodes.get(0);
        if (settings.timeMeasuring) {
            LOG.info("Time to find the fog node placement for {}: {}", as, intervalToString(start, System.nanoTime()));
        }

        return next;
    }

    /**
     * Removes the not required nodes from the node mapping and the given starting nodes
     * prevents unnecessary iterations.
     *
     * @param node          next fog node found
     * @param startingNodes list of starting nodes to update
     */
    private void removeAllCoveredNodes(BaseNode node, List<StartingNode> startingNodes) {
        // get covered nodes by the fog node placement
        List<Tuple<StartingNode, Integer>> coveredNodes = node.getCoveredStartingNodes();

        for (Tuple<StartingNode, Integer> t : coveredNodes) {
            StartingNode coveredStartingNode = t.getKey();
            coveredStartingNode.decreaseDeviceCount(t.getValue());

            // node is fully covered
            if (coveredStartingNode.getDeviceCount() <= 0) {
                coveredStartingNode.notifyPossibleNodes();
                nodes.remove(coveredStartingNode.node);
            }

            coveredStartingNode.getReachableNodes().stream().filter(n -> !n.hasConnections()).forEach(n -> nodes.remove(n.node));
        }

        node.getStartingNodes().forEach(n -> n.removePossibleNode(node));
        nodes.remove(node.node);

        // remove all covered nodes from the edge nodes set
        startingNodes.removeAll(coveredNodes.stream().map(Tuple::getKey).collect(Collectors.toList()));
    }

    /**
     * Calculates the connection costs to all nodes that are within the cost threshold
     * defined in the associated settings {@link #settings}. To calculate the costs
     * the function uses the dijksta algorithm starting from the given node.
     *
     * @param node node to calculate the connection costs for
     */
    private void calculateConnectionCosts(StartingNode node) {
        // push the starting node as a starting point in the queue
        node.setCosts(node, node, 0f);
        Queue<BaseNode> queue = new PriorityQueue<>(new CostComparator(node));
        queue.add(node);

        // using the dijkstra algorithm to iterate the graph
        while (!queue.isEmpty()) {
            BaseNode current = queue.poll();
            float currentCosts = current.getCosts(node);

            // check all edges leaving the current node
            for (Edge e : current.node.getEdges()) {
                // ignore cross as edges
                if (e.isCrossASEdge()) {
                    continue;
                }

                Node neighbor = e.getDestinationForSource(current.node);
                // ignore host devices as they are not considered to be possible nodes
                if (neighbor instanceof EdgeDeviceNode) {
                    continue;
                }
                // abort on costs above the threshold
                float nextCosts = currentCosts + calculateCosts(e);
                if (nextCosts > settings.costThreshold) {
                    continue;
                }

                BaseNode neighborNode = getBaseNode(neighbor);
                float neighborCosts = neighborNode.getCosts(node);

                if (neighborCosts == Float.MAX_VALUE) {
                    // newly discovered node
                    neighborNode.setCosts(node, current, nextCosts);
                    queue.add(neighborNode);
                } else if (nextCosts < neighborCosts) {
                    // update an already discovered node
                    neighborNode.setCosts(node, current, nextCosts);
                }
            }
        }
    }

    /**
     * Returns the base node based on the given node object. If it not yet
     * mapped in {@link #nodes} the call will create new instance and return
     * it.
     *
     * @param node node to get the base node for
     * @return base node instance for the given node
     */
    private BaseNode getBaseNode(Node node) {
        return nodes.computeIfAbsent(node, BaseNode::new);
    }

    /**
     * Creates and returns a new starting node based on the given edge node.
     * Adds the newly created node to the mapping of nodes {@link #nodes}
     *
     * @param node edge node to create a starting node for
     * @return the newly created starting node
     */
    private StartingNode createStartingNode(EdgeNode node) {
        StartingNode startingNode = new StartingNode(node);
        nodes.put(node, startingNode);

        return startingNode;
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
}
