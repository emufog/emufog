package emufog.placement;

import com.google.common.graph.MutableNetwork;
import emufog.topology.Link;
import emufog.topology.Node;


public interface IEdgeIdentifier {

     MutableNetwork identifyEdge(MutableNetwork<Node, Link> topology);

}

