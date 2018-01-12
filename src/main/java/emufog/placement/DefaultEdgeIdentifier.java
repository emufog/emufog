package emufog.placement;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Traverser;
import emufog.topology.Link;
import emufog.topology.Node;
import emufog.topology.Router;

import java.util.List;
import java.util.Set;

import static emufog.topology.Types.RouterType.BACKBONE_ROUTER;
import static emufog.topology.Types.RouterType.EDGE_ROUTER;

public class DefaultEdgeIdentifier implements IEdgeIdentifier {

    /* percentage of the average degree to compare to */
    private static final float BACKBONE_DEGREE_PERCENTAGE = 0.6f;

    private List<Router> routers;

    @Override
    public MutableNetwork identifyEdge(MutableNetwork<Node, Link> topology) {

        identifyBackbone(topology);

        return topology;
    }

    void identifyBackbone(MutableNetwork<Node, Link> t){

        routers = (List<Router>) t.nodes().stream().filter(node -> node instanceof Router);

        markASEdgeNodes(t);
        convertHighDegrees(t);
        buildSingleBackbone(t);



    }

    private void markASEdgeNodes(MutableNetwork<Node, Link> t){

        for (Node node : t.nodes()){
            Set<Node> neighbors = t.adjacentNodes(node);

            for(Node neighbor : neighbors){

                // Checks if link between nodes connects different AS.
                boolean isCrossASEdge = node.getAsID() != neighbor.getAsID();

                //Checks if selected Router is already a EDGE_ROUTER
                boolean isEdgeRouter = EDGE_ROUTER.equals(((Router) node).getType());

                if(isCrossASEdge){
                    if(node instanceof Router){
                        if(!isEdgeRouter){ ((Router) node).setType(BACKBONE_ROUTER);}

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
     * @return
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

            boolean backboneRouter = ((Router) node).getType().equals(BACKBONE_ROUTER);

            if(node instanceof Router){

                List<Router> predecessors = (List<Router>)t.predecessors(node).stream().filter(n -> n instanceof Router);

                if(backboneRouter){
                    if(!predecessors.isEmpty()){
                        for(Router predecessor : predecessors){
                            predecessor.setType(BACKBONE_ROUTER);
                        }
                    }
                }
            }


        }

    }
}
