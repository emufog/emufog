package emufog.placement;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Traverser;
import emufog.topology.Link;
import emufog.topology.Node;
import emufog.topology.Router;
import emufog.util.Logger;
import emufog.util.LoggerLevel;

import java.util.List;
import java.util.Set;

import static emufog.topology.Types.RouterType.BACKBONE_ROUTER;
import static emufog.topology.Types.RouterType.EDGE_ROUTER;
import static emufog.topology.Types.RouterType.ROUTER;

public class DefaultEdgeIdentifier implements IEdgeIdentifier {

    /* percentage of the average degree to compare to */
    private static final float BACKBONE_DEGREE_PERCENTAGE = 0.6f;

    private List<Router> routers;

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

    void identifyBackbone(MutableNetwork<Node, Link> t){

        Logger logger = Logger.getInstance();

        routers = (List<Router>) t.nodes().stream().filter(node -> node instanceof Router);

        long start = System.nanoTime();
        markASEdgeNodes(t);
        long end = System.nanoTime();
        logger.log("It took " + Logger.convertToMs(start,end) + "ms to run the markASEdgeNodes method", LoggerLevel.ADVANCED);


        start = System.nanoTime();
        convertHighDegrees(t);
        end = System.nanoTime();
        logger.log("It took " + Logger.convertToMs(start,end) + "ms to run the convertHighDegrees method", LoggerLevel.ADVANCED);

        start = System.nanoTime();
        buildSingleBackbone(t);
        end = System.nanoTime();
        logger.log("It took " + Logger.convertToMs(start,end) + "ms to run the buildSingleBackbone method", LoggerLevel.ADVANCED);

    }

    private void markASEdgeNodes(MutableNetwork<Node, Link> t){

        for (Node node : t.nodes()){
            Set<Node> neighbors = t.adjacentNodes(node);

            for(Node neighbor : neighbors){
                if(isCrossASEdge(node, neighbor)){
                    if(node instanceof Router){
                        if(!isRouter((Router) node)){ ((Router) node).setType(BACKBONE_ROUTER);}
                    }
                }
            }
        }
    }


    /**
     * Converts router nodes with an above average degree to a BACKBONE_ROUTER
     * @param t
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
     * @param t
     * @return the average degree
     */
    private float calculateAverageDegree(MutableNetwork<Node, Link> t){
        int sum = 0;

        for(Node router : routers){
            sum += t.degree(router);
        }
        return sum / routers.size();

    }

    private void buildSingleBackbone(MutableNetwork<Node, Link> t){

        Traverser<Node> traverser = Traverser.forGraph(t);

        List<Router> backboneRouters = (List<Router>) routers.stream().filter(node -> node.getType().equals(BACKBONE_ROUTER));

        for(Node node : traverser.breadthFirst(backboneRouters.get(1))){

            if(node instanceof Router){

                List<Router> predecessors = (List<Router>)t.predecessors(node).stream().filter(n -> n instanceof Router);

                if(isBackboneRouter((Router) node)){
                    if(!predecessors.isEmpty()){
                        for(Router predecessor : predecessors){
                            predecessor.setType(BACKBONE_ROUTER);
                        }
                    }
                }
            }


        }

    }

    private boolean isCrossASEdge(Node node, Node neighbor){
        return node.getAsID() != neighbor.getAsID();
    }

    private boolean isRouter(Router router){
        return ROUTER.equals(router.getType());
    }

    private boolean isBackboneRouter(Router router){
        return router.getType().equals(BACKBONE_ROUTER);
    }
}
