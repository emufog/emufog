package emufog.fog2;

import emufog.container.ContainerType;
import emufog.graph.Node;
import emufog.util.Tuple;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FogResult {

    private boolean status;

    private final List<Tuple<Node, ContainerType>> placements;

    FogResult() {
        status = false;
        placements = new ArrayList<>();
    }

    public boolean getStatus() {
        return status;
    }

    public List<Tuple<Node, ContainerType>> getPlacements() {
        return placements;
    }

    void addPlacements(Collection<Tuple<Node, ContainerType>> placements) {
        this.placements.addAll(placements);
    }

    void setSucccess() {
        status = true;
    }

    void setFailure() {
        status = false;
    }
}
