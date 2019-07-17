/*
 * MIT License
 *
 * Copyright (c) 2018 emufog contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package emufog.fog;

import emufog.container.FogType;
import emufog.graph.Node;
import emufog.graph.EdgeNode;
import emufog.util.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class represents a level or iteration of the fog placement algorithm. Each level starts with an initial
 * set of nodes and produces an output. Via dependencies the order of levels can be calculated.
 */
class FogLevel {

    /* mapping of fog types to a list of all nodes using this type */
    private final Map<FogType, List<FogNode>> placements;

    /* list of possible fog types for this level */
    final List<FogType> fogTypes;

    /* list of levels having this level as a predecessor */
    private final List<FogLevel> nextLevels;

    /* list of routers to start with */
    private List<EdgeNode> startingNodes;

    /**
     * Creates a new instance of a fog level. This requires information of previously
     * identified fog levels and information of placed fog nodes so far.
     *
     * @param placements   placement of fog nodes and their respective type
     * @param fogTypes     possible fog types to chose from
     * @param predecessors collection of predecessor levels
     */
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

    /**
     * Adds a collection of edgeNodes as a starting set.
     *
     * @param edgeNodes edgeNodes to start this level with
     */
    void addStartingRouters(Collection<EdgeNode> edgeNodes) {
        startingNodes = new ArrayList<>(edgeNodes);
    }

    /**
     * Returns a list of nodes to start for this fog level. The list consists of pairs of
     * nodes with their respective edge nodes.
     *
     * @return list of nodes to start this fog level
     */
    List<Tuple<Node, List<emufog.fog.EdgeNode>>> getStartNodes() {
        List<Tuple<Node, List<emufog.fog.EdgeNode>>> result = new ArrayList<>();

        if (startingNodes != null) {
            // starting with routers on the first iteration
            for (EdgeNode r : startingNodes) {
                if (r.hasDevices()) {
                    result.add(new Tuple<>(r, null));
                }
            }
        } else {
            // or from an existing set of placed fog nodes
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

    /**
     * Adds a new fog level to the list of next levels.
     *
     * @param next a fog level with this level as predecessor
     */
    private void addNextLevel(FogLevel next) {
        nextLevels.add(next);
    }

    /**
     * Returns the list of the next fog levels to process.
     *
     * @return list of following fog levels
     */
    List<FogLevel> getNextLevels() {
        return nextLevels;
    }
}
