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
    private static final float BACKBONE_DEGREE_PERCENTAGE = 0.6f;

    private List<Router> routers = new ArrayList<>();

    @Override
    public MutableNetwork identifyEdge(MutableNetwork<Node, Link> topology) {

        Logger logger = Logger.getInstance();

        long start = System.nanoTime();
        identifyBackbone(topology);
        long end = System.nanoTime();
        logger.log("It took: " + Logger.convertToMs(start,end) + "ms to identify the Edge", LoggerLevel.ADVANCED);
        logger.log(
                "Backbone Size: " +
                        topology.nodes()
                                .stream()
                                .filter(node -> node instanceof Router)
                                .filter(node -> ((Router) node).getType().equals(BACKBONE_ROUTER))
                                .count() + " Nodes", LoggerLevel.ADVANCED);
        logger.log(
                "Edge Size: " +
                        topology.nodes()
                                .stream()
                                .filter(node -> node instanceof Router)
                                .filter(node -> ((Router) node).getType().equals(EDGE_ROUTER))
                                .count() + " Nodes", LoggerLevel.ADVANCED);
        logger.log("Remaining routers: " +
                        topology.nodes()
                                .stream()
                                .filter(node -> node instanceof Router)
                                .filter(node -> ((Router) node).getType().equals(ROUTER))
                                .count() + " Nodes", LoggerLevel.ADVANCED);

        return topology;
    }

    private void identifyBackbone(MutableNetwork<Node, Link> t){

        Logger logger = Logger.getInstance();

        t.nodes().stream().filter(node -> node instanceof Router).forEach(node -> routers.add((Router) node));

        long start = System.nanoTime();
        markASEdgeNodes(t);
        long end = System.nanoTime();
        logger.log("It took " + Logger.convertToMs(start,end) + " to run the markASEdgeNodes method", LoggerLevel.ADVANCED);


        start = System.nanoTime();
        convertHighDegrees(t);
        end = System.nanoTime();
        logger.log("It took " + Logger.convertToMs(start,end) + " to run the convertHighDegrees method", LoggerLevel.ADVANCED);

        start = System.nanoTime();
        buildSingleBackbone(t);
        end = System.nanoTime();
        logger.log("It took " + Logger.convertToMs(start,end) + " to run the buildSingleBackbone method", LoggerLevel.ADVANCED);

        start = System.nanoTime();
        convertRemainingRouters(routers);
        end = System.nanoTime();
        logger.log("It took " + Logger.convertToMs(start,end) + " to convert the remaining routers", LoggerLevel.ADVANCED);

    }

    /**
     * This method marks all cross-AS edge's endpoints as BACKBONE_ROUTER's
     * @param t topology to work on.
     */
    private void markASEdgeNodes(MutableNetwork<Node, Link> t){

        for (Node node : t.nodes()){
            Set<Node> neighbors = t.adjacentNodes(node);

            for(Node neighbor : neighbors){
                if(isCrossAsEdge(node, neighbor)){
                    if(!isBackboneRouter((Router) node)) ((Router) node).setType(BACKBONE_ROUTER);
                    if(neighbor instanceof Router) ((Router) neighbor).setType(BACKBONE_ROUTER);
                }
            }
        }
    }

    /**
     * Checks if the neighbor of the given node is member of another AS.
     * @param node current node
     * @param neighbor Neighbor node
     * @return true if neighbor is member in another AS
     */
    private boolean isCrossAsEdge(Node node, Node neighbor){
        return node.getAsID() != neighbor.getAsID();
    }


    /**
     * Converts router nodes with an above average degree to a BACKBONE_ROUTER
     * @param t topology to work on.
     */
    private void convertHighDegrees(MutableNetwork<Node, Link> t){
        float averageDegree = calculateAverageDegree(t) * BACKBONE_DEGREE_PERCENTAGE;

        for(Router router : routers){
            boolean aboveAverage = t.degree(router) >= averageDegree;
            if(aboveAverage){
                router.setType(BACKBONE_ROUTER);
            }
        }
    }


    /**
     * Returns the average degree of the system based on the # of router nodes.
     * @param t topology to work on.
     * @return the average degree
     */
    private float calculateAverageDegree(MutableNetwork<Node, Link> t){
        int sum = 0;

        for(Node router : routers){
            sum += t.degree(router);
        }
        return sum / routers.size();

    }

    /**
     * Creates a single connected backbone by using the Breath-First-Algorithm (kinda)
     * @param t
     */
    private void buildSingleBackbone(MutableNetwork<Node, Link> t){

        BitSet visited = new BitSet();
        BitSet seen = new BitSet();

        List<Router> backboneRouters = routers.stream().filter(node -> node.getType().equals(BACKBONE_ROUTER)).collect(Collectors.toList());

        Queue<Node> nodeQueue = new ArrayDeque<>();

        Map<Node, Node> predecessors = new HashMap<>();

        Node current = backboneRouters.iterator().next();

        if(current == null){
            return;
        }

        predecessors.put(current, current);

        while (current != null){

            visited.set(current.getID());

            if(isBackboneRouter((Router)current) && predecessors.get(current) instanceof Router) {
                Node predecessor = predecessors.get(current);
                while (predecessor instanceof Router){
                    ((Router) predecessor).setType(BACKBONE_ROUTER);

                    predecessor = predecessors.get(predecessor);
                }
            }


            for(Node neighbor : t.adjacentNodes(current)){

                if(!isCrossAsEdge(current,neighbor)){
                    if(!visited.get(neighbor.getID())){
                        if(seen.get(neighbor.getID())){

                            if(predecessors.get(neighbor) instanceof Router){
                                predecessors.put(neighbor, current);
                            }
                        } else {
                            predecessors.put(neighbor,current);
                            nodeQueue.add(neighbor);
                            seen.set(neighbor.getID());
                        }
                    }
                }
            }

            current = nodeQueue.poll();

        }
    }

    /**
     * Convert remaining routers to EDGE_ROUTERS as a router can either belong to the backbone or the edge.
     * @param routers
     */
    private void convertRemainingRouters(List<Router> routers){
        for(Router router : routers){
            if(router.getType().equals(ROUTER)) router.setType(EDGE_ROUTER);
        }
    }

    /**
     * Simple Router enum type check.
     * @param router to check.
     * @return true if Router is of type BACKBONE_ROUTER
     */
    private boolean isBackboneRouter(Router router){
        return router.getType().equals(BACKBONE_ROUTER);
    }
}
