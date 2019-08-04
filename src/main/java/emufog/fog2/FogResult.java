package emufog.fog2;

import emufog.container.FogType;
import emufog.graph.Node;
import emufog.util.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FogResult {

    private boolean status;

    private final List<Tuple<Node, FogType>> placements;

    FogResult() {
        status = false;
        placements = new ArrayList<>();
    }

    public boolean getStatus() {
        return status;
    }

    public List<Tuple<Node, FogType>> getPlacements() {
        return placements;
    }

    void addPlacement(Tuple<Node, FogType> placement) {
        placements.add(placement);
    }

    void addPlacements(Collection<Tuple<Node, FogType>> placements) {
        this.placements.addAll(placements);
    }

    void setSuccess() {
        status = true;
    }

    void setFailure() {
        status = false;
    }
}
