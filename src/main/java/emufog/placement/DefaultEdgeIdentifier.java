package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.topology.Link;
import emufog.topology.Node;
import emufog.topology.Router;
import emufog.util.Logger;
import emufog.util.LoggerLevel;

import java.util.*;
import java.util.stream.Collectors;

import static emufog.topology.Types.RouterType.*;

public class DefaultEdgeIdentifier implements IEdgeIdentifier {

    /* percentage of the average degree to compare to */
    //TODO: Add to settings file.
    private static final float BACKBONE_DEGREE_PERCENTAGE = 0.7f;

    private List<Router> routers = new ArrayList<>();

    @Override
    public MutableNetwork identifyEdge(MutableNetwork<Node, Link> topology) {

        identifyBackbone(topology);

        return topology;
    }

    private void identifyBackbone(MutableNetwork<Node, Link> t) {

        Logger logger = Logger.getInstance();

        t.nodes().stream().filter(node -> node instanceof Router).forEach(node -> routers.add((Router) node));

        long start = System.nanoTime();
        markASEdgeNodes(t);
        long end = System.nanoTime();
        logger.log("It took " + Logger.convertToMs(start, end) + " to run the markASEdgeNodes method", LoggerLevel.ADVANCED);


        start = System.nanoTime();
        convertHighDegrees(t);
        end = System.nanoTime();
        logger.log("It took " + Logger.convertToMs(start, end) + " to run the convertHighDegrees method", LoggerLevel.ADVANCED);

        start = System.nanoTime();
        buildSingleBackbone(t);
        end = System.nanoTime();
        logger.log("It took " + Logger.convertToMs(start, end) + " to run the buildSingleBackbone method", LoggerLevel.ADVANCED);

        start = System.nanoTime();
        convertRemainingRouters(routers);
        end = System.nanoTime();
        logger.log("It took " + Logger.convertToMs(start, end) + " to convert the remaining routers", LoggerLevel.ADVANCED);

        logger.logSeparator();
        logger.log("Identified Topology with: \n" + t.nodes()
                .stream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(EDGE_ROUTER))
                .count() + " EDGE_ROUTERs \n" + t.nodes()
                .stream()
                .filter(n -> n instanceof Router && ((Router) n).getType().equals(BACKBONE_ROUTER))
                .count() + " BACKBONE_ROUTERs");

    }

    /**
     * This method marks all cross-AS edge's endpoints as BACKBONE_ROUTER's
     *
     * @param t topology to work on.
     */
    private void markASEdgeNodes(MutableNetwork<Node, Link> t) {

        for (Node node : t.nodes()) {
            Set<Node> neighbors = t.adjacentNodes(node);

            for (Node neighbor : neighbors) {
                if (isCrossAsEdge(node, neighbor)) {
                    if (!isBackboneRouter((Router) node)) ((Router) node).setType(BACKBONE_ROUTER);
                    if (neighbor instanceof Router) ((Router) neighbor).setType(BACKBONE_ROUTER);
                }
            }
        }
    }

    /**
     * Checks if the neighbor of the given node is member of another AS.
     *
     * @param node     current node
     * @param neighbor Neighbor node
     * @return true if neighbor is member in another AS
     */
    private boolean isCrossAsEdge(Node node, Node neighbor) {

        return node.getAsID() != neighbor.getAsID();


    }


    /**
     * Converts router nodes with an above average degree to a BACKBONE_ROUTER
     *
     * @param t topology to work on.
     */
    private void convertHighDegrees(MutableNetwork<Node, Link> t) {
        float averageDegree = calculateAverageDegree(t) * BACKBONE_DEGREE_PERCENTAGE;

        for (Router router : routers) {
            boolean aboveAverage = t.degree(router) >= averageDegree;
            if (aboveAverage) {
                router.setType(BACKBONE_ROUTER);
            }
        }
    }


    /**
     * Returns the average degree of the system based on the # of router nodes.
     *
     * @param t topology to work on.
     * @return the average degree
     */
    private float calculateAverageDegree(MutableNetwork<Node, Link> t) {
        int sum = 0;

        for (Node router : routers) {
            sum += t.degree(router);
        }
        return sum / routers.size();

    }

    /**
     * Creates a single connected backbone by using the Breath-First-Algorithm.
     *
     * @param t
     */
    private void buildSingleBackbone(MutableNetwork<Node, Link> t) {

        Queue<Node> nodeQueue = new ArrayDeque<>();
        Map<Node, Node> predecessors = new HashMap<>();

        // manage visited nodes
        BitSet visited = new BitSet();
        BitSet seen = new BitSet();

        // start with first backbone router
        List<Router> backboneRouters = routers.stream()
                .filter(node -> node.getType().equals(BACKBONE_ROUTER))
                .collect(Collectors.toList());
        Node current = backboneRouters.get(0);
        nodeQueue.add(current);

        predecessors.put(current, current);

        while (!nodeQueue.isEmpty()) {

            visited.set(current.getID());

            for (Node neighbor : t.adjacentNodes(current)) {
                if (!visited.get(neighbor.getID())) {
                    if (seen.get(neighbor.getID())) {

                        if (nodeQueue.contains(neighbor) && !isCrossAsEdge(current, neighbor)) {

                            if (neighbor instanceof Router) {

                                if (isBackboneRouter((Router) current) && predecessors.get(neighbor) instanceof Router) {
                                    predecessors.put(neighbor, current);

                                }
                            }
                        } else {
                            predecessors.put(neighbor, current);
                            nodeQueue.add(neighbor);
                            seen.set(neighbor.getID());
                        }

                    }
                }
            }

            // follow a trace from one backbone router to another and convert intermediaries.
            if (isBackboneRouter((Router) current)
                    && predecessors.get(current) instanceof Router
                    && !isBackboneRouter((Router) predecessors.get(current))) {

                Node predecessor = predecessors.get(current);

                while (predecessor instanceof Router) {
                    ((Router) predecessor).setType(BACKBONE_ROUTER);
                    predecessor = predecessors.get(predecessor);
                }
            }
            current = nodeQueue.poll();
        }
    }

    /**
     * Convert remaining routers to EDGE_ROUTERS as a router can either belong to the backbone or the edge.
     *
     * @param routers
     */
    private void convertRemainingRouters(List<Router> routers) {
        for (Router router : routers) {
            if (router.getType().equals(ROUTER)) router.setType(EDGE_ROUTER);
        }
    }

    /**
     * Simple Router enum type check.
     *
     * @param router to check.
     * @return true if Router is of type BACKBONE_ROUTER
     */
    private boolean isBackboneRouter(Router router) {
        return router.getType().equals(BACKBONE_ROUTER);
    }
}
