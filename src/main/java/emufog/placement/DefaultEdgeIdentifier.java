package emufog.placement;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Traverser;
import emufog.topology.Link;
import emufog.topology.Node;
import emufog.topology.Router;
import emufog.util.Logger;
import emufog.util.LoggerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static emufog.topology.Types.RouterType.BACKBONE_ROUTER;
import static emufog.topology.Types.RouterType.ROUTER;

public class DefaultEdgeIdentifier implements IEdgeIdentifier {

    /* percentage of the average degree to compare to */
    private static final float BACKBONE_DEGREE_PERCENTAGE = 0.6f;

    private List<Router> routers = new ArrayList<>();

    /*
    TODO: Change naming convention. Initially every router parsed from the input topology is of type ROUTER. After the edge identification edge routers should have the type EDGE_ROUTER and Backbone Router the type BACKBONE_ROUTER.
     */
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

        routers.get(0).setType(ROUTER);

    }

    /**
     * This method marks all cross-AS edge's endpoints as BACKBONE_ROUTER's
     * @param t topology to work on.
     */
    private void markASEdgeNodes(MutableNetwork<Node, Link> t){

        for (Node node : t.nodes()){
            Set<Node> neighbors = t.adjacentNodes(node);

            for(Node neighbor : neighbors){
                if(isCrossASEdge(node, neighbor)){
                    if(node instanceof Router){
                        // TODO: Debug if statement feels wrong.
                        if(!isRouter((Router) node)){ ((Router) node).setType(BACKBONE_ROUTER);}
                    }
                }
            }
        }
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
     * Checks if the neighbor of the given node is member of another AS.
     * @param node current node
     * @param neighbor Neighbor node
     * @return true if neighbor is member in another AS
     */
    private boolean isCrossASEdge(Node node, Node neighbor){
        return node.getAsID() != neighbor.getAsID();
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

    //TODO: Finish implementation of buildSingleBackbone method.
    private void buildSingleBackbone(MutableNetwork<Node, Link> t){

        Traverser<Node> traverser = Traverser.forGraph(t);

        List<Router> backboneRouters = routers.stream().filter(node -> node.getType().equals(BACKBONE_ROUTER)).collect(Collectors.toList());

        for(Node node : traverser.breadthFirst(backboneRouters.get(1))){

            if(node instanceof Router){

                List<Router> adjacentNodes = new ArrayList<>();

                t.adjacentNodes(node).stream().filter(n-> n instanceof Router).forEach(n -> adjacentNodes.add(((Router) n)));

                if(isBackboneRouter((Router) node)){
                    //TODO: Check this statement. Could be unsafe.
                    if(!adjacentNodes.isEmpty()){
                        for(Router predecessor : adjacentNodes){
                            predecessor.setType(BACKBONE_ROUTER);
                        }
                    }
                }
            }
        }

    }

    /**
     * Simple Router enum type check.
     * @param router to check.
     * @return true if Router is of type ROUTER
     */
    private boolean isRouter(Router router){
        return ROUTER.equals(router.getType());
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
