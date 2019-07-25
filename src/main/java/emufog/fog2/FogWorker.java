package emufog.fog2;

import emufog.container.FogType;
import emufog.fog.FogNode;
import emufog.graph.AS;
import emufog.graph.Edge;
import emufog.graph.EdgeDeviceNode;
import emufog.graph.Node;
import java.util.ArrayDeque;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import static emufog.util.ConversionsUtils.intervalToString;

class FogWorker {

    private final AS as;

    private final FogNodeClassifier classifier;

    private final List<FogType> fogNodeTypes;

    FogWorker(AS as, FogNodeClassifier classifier) {
        this.as = as;
        this.classifier = classifier;
        fogNodeTypes = classifier.settings.fogNodeTypes;
    }

    FogResult processAS() {
        FogTree tree = new FogTree(as);

        FogResult result = new FogResult();
        result.setSucccess();

        while (tree.hasNextLevel()) {
            List<NodePlacement> level = tree.getNextLevel();
            FogResult partResult = processLevel(level);

            if (!partResult.getStatus()) {
                result.setFailure();
                break;
            }

            result.addPlacements(partResult.getPlacements());
        }

        return result;
    }

    private FogResult processLevel(List<NodePlacement> level) {
        FogResult result = new FogResult();
        result.setSucccess();

        Queue<NodePlacement> queue = new ArrayDeque<>(level);

        while (!queue.isEmpty()) {
            NodePlacement placement = queue.poll();


        }

        return result;
    }

    void processNode(NodePlacement node) {
        long start = System.nanoTime();

        // push the router as a starting point in the queue
        PriorityQueue<Node> queue = new PriorityQueue<>();
        edgeNode.initPredecessor(edgeNode, edgeNode, 0);
        queue.add(node);

        // using the dijkstra algorithm to iterate the graph
        while (!queue.isEmpty()) {
            Node current = queue.poll();

            float currentCosts = current.getCosts(edgeNode);

            // check all edges leaving the current node
            for (Edge e : current..getEdges()) {
                if (!e.isCrossASEdge()) {

                    Node neighbor = e.getDestinationForSource(current.oldNode);

                    // ignore host devices as they are not considered to be possible nodes
                    if (!(neighbor instanceof EdgeDeviceNode)) {
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
        LOG.info("Time per router to build graph: " + intervalToString(start, end));
    }
}
