package emufog.placement;

import emufog.topology.Graph;

public abstract class EdgeIdentifier {

    public abstract Graph identifyEdge(Graph topology, float delay, float bandwidth);

}
