package emufog.placement;

import emufog.topology.Topology;

public abstract class EdgeIdentifier {

    public abstract Topology identifyEdge(Topology topology, float delay, float bandwidth);

}
