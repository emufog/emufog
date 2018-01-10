package emufog.topology;

import com.google.common.graph.AbstractNetwork;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;

import java.util.Set;

public abstract class Topology extends AbstractNetwork {




    @Override
    public Set nodes() {
        return null;
    }

    @Override
    public Set edges() {
        return null;
    }

    @Override
    public boolean isDirected() {
        return false;
    }

    @Override
    public boolean allowsParallelEdges() {
        return false;
    }

    @Override
    public boolean allowsSelfLoops() {
        return false;
    }

    @Override
    public ElementOrder nodeOrder() {
        return null;
    }

    @Override
    public ElementOrder edgeOrder() {
        return null;
    }

    @Override
    public Set adjacentNodes(Object node) {
        return null;
    }

    @Override
    public Set predecessors(Object node) {
        return null;
    }

    @Override
    public Set successors(Object node) {
        return null;
    }

    @Override
    public Set incidentEdges(Object node) {
        return null;
    }

    @Override
    public Set inEdges(Object node) {
        return null;
    }

    @Override
    public Set outEdges(Object node) {
        return null;
    }

    @Override
    public EndpointPair incidentNodes(Object edge) {
        return null;
    }
}