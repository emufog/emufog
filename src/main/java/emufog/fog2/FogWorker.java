package emufog.fog2;

import emufog.container.FogType;
import emufog.fog.FogNode;
import emufog.graph.AS;
import emufog.graph.Edge;
import emufog.graph.EdgeDeviceNode;
import emufog.graph.EdgeNode;
import emufog.graph.Node;
import emufog.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import static emufog.util.ConversionsUtils.intervalToString;

class FogWorker {

    private static final Logger LOG = LoggerFactory.getLogger(FogWorker.class);

    private final AS as;

    private final FogNodeClassifier classifier;

    private final List<FogType> fogNodeTypes;

    private final Map<Node, BaseNode> nodes;

    private final Comparator<BaseNode> comparator;


    FogWorker(AS as, FogNodeClassifier classifier) {
        this.as = as;
        this.classifier = classifier;
        fogNodeTypes = classifier.settings.fogNodeTypes;
        nodes = new HashMap<>();
        comparator = new FogComparator();
    }

    FogResult processAS() {
        // map edge device nodes to their respective wrappers
        List<StartingNode> startingNodes = as.getEdgeNodes().stream()
                .filter(EdgeNode::hasDevices)
                .map(this::createStartingNode)
                .collect(Collectors.toList());
        // calculate connection costs from the edge device nodes
        long start = System.nanoTime();
        startingNodes.forEach(this::calculateConnectionCosts);
        if (classifier.settings.timeMeasuring) {
            LOG.info("Time to calculate connection costs for edge devices: {}", intervalToString(start, System.nanoTime()));
        }

        // init empty result set
        FogResult result = new FogResult();
        result.setSuccess();

        while (!startingNodes.isEmpty()) {
            if (!classifier.fogNodesLeft()) {
                result.setFailure();
                return result;
            }

            start = System.nanoTime();
            Tuple<Node, FogType> fogNode = getNextFogNode(startingNodes);
            if (classifier.settings.timeMeasuring) {
                LOG.info("Time to find next fog node for {}: {}", as, intervalToString(start, System.nanoTime()));
            }

            // add the new fog node to the partial result
            result.addPlacement(fogNode);
            classifier.reduceRemainingNodes();
            // add placement to the map
            //List<FogNode> nodes = fogPlacements.computeIfAbsent(fogNode.getFogType(), k -> new ArrayList<>());
            //nodes.add(fogNode);
        }

        return result;
    }

    private Tuple<Node, FogType> getNextFogNode(List<StartingNode> startingNodes) {
        LOG.debug("Remaining Edge Nodes to cover: {}", startingNodes.size());

        long functionStart = System.nanoTime();
        List<BaseNode> fogNodes = new ArrayList<>(nodes.values());
        fogNodes.forEach(n -> n.findFogType(fogNodeTypes));
        if (classifier.settings.timeMeasuring) {
            LOG.info("Time to find possible fog nodes: {}", intervalToString(functionStart, System.nanoTime()));
        }

        long start = System.nanoTime();
        // sort the possible fog nodes with a FogComparator
        fogNodes.sort(comparator);
        // retrieve the nextLevels optimal node
        BaseNode next = fogNodes.get(0);
        if (classifier.settings.timeMeasuring) {
            LOG.info("Find the optimal fog node placement: " + intervalToString(start, System.nanoTime()));
        }

        // get covered nodes by the fog node placement
        List<Tuple<StartingNode, Integer>> coveredNodes = next.getCoveredStartingNodes();

        start = System.nanoTime();
        for (Tuple<StartingNode, Integer> t : coveredNodes) {
            StartingNode coveredStartingNode = t.getKey();
            coveredStartingNode.removePossibleNode(next);
            // node is fully covered
            if (coveredStartingNode.getDeviceCount() == t.getValue()) {
                coveredStartingNode.notifyPossibleNodes();
                nodes.remove(coveredStartingNode.node);
            } else { // partially covered node
                //TODO rethink
            }
        }

        next.clearAllEdgeNodes();

        // remove all covered nodes from the edge nodes set
        startingNodes.removeAll(coveredNodes.stream().map(Tuple::getKey).collect(Collectors.toList()));
        if (classifier.settings.timeMeasuring) {
            LOG.info("Time to remove the determined fog node from the graph: {}", intervalToString(start, System.nanoTime()));
            LOG.info("Time to calculate the next fog node Time: {}", intervalToString(functionStart, System.nanoTime()));
        }

        return new Tuple<>(next.node, next.getType());
    }

    private void calculateConnectionCosts(StartingNode node) {
        // push the router as a starting point in the queue
        node.setCosts(node, node, 0f);
        PriorityQueue<BaseNode> queue = new PriorityQueue<>(new CostComparator(node));
        queue.add(node);

        // using the dijkstra algorithm to iterate the graph
        while (!queue.isEmpty()) {
            BaseNode current = queue.poll();
            float currentCosts = current.getCosts(node);

            // check all edges leaving the current node
            for (Edge e : current.node.getEdges()) {
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
                if (nextCosts > classifier.settings.costThreshold) {
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

    private BaseNode getBaseNode(Node node) {
        return nodes.computeIfAbsent(node, NetworkNode::new);
    }

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
