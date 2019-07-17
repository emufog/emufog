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

import emufog.graph.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This node represents an edge node that needs to be connected to a fog node.
 */
class EdgeNode extends FogNode {

    /* list of possible fog nodes in range of the threshold */
    private final List<FogNode> possibleNodes;

    /* number of devices to cover for this edge node */
    private int deviceCount;

    /**
     * Creates a new edge node for the fog node placement algorithm.
     *
     * @param graph  original graph instance
     * @param edgeNode edge node for the iteration
     */
    EdgeNode(FogGraph graph, emufog.graph.EdgeNode edgeNode) {
        super(graph, edgeNode);

        possibleNodes = new ArrayList<>();
        deviceCount = edgeNode.getDeviceCount();
    }

    /**
     * Creates a new edge node for the fog node placement algorithm.
     *
     * @param graph         original graph instance
     * @param node          node representing the edge for the iteration
     * @param assignedNodes collection of all edge nodes covered in the original graph
     */
    EdgeNode(FogGraph graph, Node node, Collection<EdgeNode> assignedNodes) {
        super(graph, node);

        possibleNodes = new ArrayList<>();

        assert !assignedNodes.isEmpty() : "no nodes assigned";
        deviceCount = 0;
        for (EdgeNode e : assignedNodes) {
            deviceCount += e.getDeviceCount();
        }
    }

    /**
     * Adds a node to the list of possible nodes for this edge node.
     *
     * @param node possible fog node
     */
    void addPossibleNode(FogNode node) {
        possibleNodes.add(node);
        setModified(true);
    }

    /**
     * Removes a fog node from the list of possible nodes if it's not available any more.
     *
     * @param node fog node to remove
     */
    void removePossibleNode(FogNode node) {
        boolean result = possibleNodes.remove(node);

        assert result : "node was not found in possible list";

        setModified(true);
    }

    /**
     * Notifies all possible nodes of this edge node that the node does not have to be covered any more.
     */
    void notifyPossibleNodes() {
        for (FogNode node : possibleNodes) {
            node.removeEdgeNode(this);
        }
    }

    /**
     * Clears the list of possible fog nodes.
     */
    void clearPossibleNodes() {
        possibleNodes.clear();
    }

    /**
     * Checks if the edge node has a connection mapped to itself.
     *
     * @return true if the node has a connection to itself, false if not
     */
    boolean isMappedToItself() {
        return equals(connectedNodes.get(this).predecessor);
    }

    /**
     * Returns the count of devices connected to this edge node.
     *
     * @return number of connected devices
     */
    int getDeviceCount() {
        assert deviceCount > 0 : "count is 0";
        return deviceCount;
    }
}
