package emufog.fog;

import emufog.docker.FogType;
import emufog.graph.Node;
import emufog.graph.Router;
import emufog.util.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class FogLevel {

    private final Map<FogType, List<FogNode>> placements;

    final List<FogType> fogTypes;

    private final List<FogLevel> nextLevels;

    private List<Router> startingNodes;

    FogLevel(Map<FogType, List<FogNode>> placements, List<FogType> fogTypes, Collection<FogLevel> predecessors) {
        this.placements = placements;
        this.fogTypes = fogTypes;
        this.nextLevels = new ArrayList<>();

        if (predecessors != null) {
            for (FogLevel predecessor : predecessors) {
                predecessor.addNextLevel(this);
            }
        }
    }

    void addStartingRouters(Collection<Router> routers) {
        startingNodes = new ArrayList<>(routers);
    }

    List<Tuple<Node, List<EdgeNode>>> getStartNodes() {
        List<Tuple<Node, List<EdgeNode>>> result = new ArrayList<>();

        if (startingNodes != null) {
            for (Router r : startingNodes) {
                if (r.hasDevices()) {
                    result.add(new Tuple<>(r, null));
                }
            }
        } else {
            for (FogType dependency : fogTypes.get(0).dependencies) {
                List<FogNode> placedFogNodes = placements.get(dependency);
                if (placedFogNodes != null) {
                    for (FogNode f : placedFogNodes) {
                        result.add(new Tuple<>(f.oldNode, f.getCoveredEdgeNodes()));
                    }
                }
            }
        }

        return result;
    }

    private void addNextLevel(FogLevel next) {
        nextLevels.add(next);
    }

    List<FogLevel> getNextLevels() {
        return nextLevels;
    }
}
