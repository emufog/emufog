/*
package emufog.fog;

import emufog.docker.FogType;
import emufog.graph.Node;
import emufog.graph.Router;
import emufog.util.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

*/
/**
 * This class represents a level or iteration of the fog placement algorithm. Each level starts with an initial
 * set of nodeconfig and produces an output. Via dependencies the order of levels can be calculated.
 *//*

class FogLevel {

    */
/* mapping of fog types to a list of all nodeconfig using this type *//*

    private final Map<FogType, List<FogNode>> placements;

    */
/* list of possible fog types for this level *//*

    final List<FogType> fogTypes;

    */
/* list of levels having this level as a predecessor *//*

    private final List<FogLevel> nextLevels;

    */
/* list of routers to start with *//*

    private List<Router> startingNodes;

    */
/**
     * Creates a new instance of a fog level. This requires information of previously
     * identified fog levels and information of placed fog nodeconfig so far.
     *
     * @param placements   placement of fog nodeconfig and their respective type
     * @param fogTypes     possible fog types to chose from
     * @param predecessors collection of predecessor levels
     *//*

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

    */
/**
     * Adds a collection of routers as a starting set.
     *
     * @param routers routers to start this level with
     *//*

    void addStartingRouters(Collection<Router> routers) {
        startingNodes = new ArrayList<>(routers);
    }

    */
/**
     * Returns a list of nodeconfig to start for this fog level. The list consists of pairs of
     * nodeconfig with their respective edge nodeconfig.
     *
     * @return list of nodeconfig to start this fog level
     *//*

    List<Tuple<Node, List<EdgeNode>>> getStartNodes() {
        List<Tuple<Node, List<EdgeNode>>> result = new ArrayList<>();

        if (startingNodes != null) {
            // starting with routers on the first iteration
            for (Router r : startingNodes) {
                if (r.hasDevices()) {
                    result.add(new Tuple<>(r, null));
                }
            }
        } else {
            // or from an existing set of placed fog nodeconfig
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

    */
/**
     * Adds a new fog level to the list of next levels.
     *
     * @param next a fog level with this level as predecessor
     *//*

    private void addNextLevel(FogLevel next) {
        nextLevels.add(next);
    }

    */
/**
     * Returns the list of the next fog levels to process.
     *
     * @return list of following fog levels
     *//*

    List<FogLevel> getNextLevels() {
        return nextLevels;
    }
}
*/
