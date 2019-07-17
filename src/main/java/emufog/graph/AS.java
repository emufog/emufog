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
package emufog.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an autonomous system of the network graph. Hence it's a sub graph of
 * the total graph providing access to its nodes.
 */
public class AS {

    /* unique identifier of the autonomous system */
    final int id;

    /* mapping of edge nodes in the autonomous system */
    private final Map<Integer, EdgeNode> edgeNodes;

    /* mapping of backbone nodes in the autonomous system */
    private final Map<Integer, BackboneNode> backboneNodes;

    /* mapping of edge device nodes in the autonomous system */
    private final Map<Integer, EdgeDeviceNode> edgeDeviceNodes;

    /**
     * Creates a new instance
     *
     * @param id unique ID of the AS
     */
    AS(int id) {
        this.id = id;
        edgeNodes = new HashMap<>();
        backboneNodes = new HashMap<>();
        edgeDeviceNodes = new HashMap<>();
    }

    /**
     * Returns the ID of the AS.
     *
     * @return AS's ID
     */
    public int getID() {
        return id;
    }

    /**
     * Returns the edge node associated with the given ID from the AS.
     *
     * @param id the edge node's ID
     * @return node object or {@code null} if not found
     */
    EdgeNode getEdgeNode(int id) {
        return edgeNodes.get(id);
    }

    /**
     * Returns the backbone node associated with the given ID from the AS.
     *
     * @param id the backbone node's ID
     * @return node object or {@code null} if not found
     */
    BackboneNode getBackboneNode(int id) {
        return backboneNodes.get(id);
    }

    /**
     * Returns the edge device node associated with the given ID from the AS.
     *
     * @param id the edge device node's ID
     * @return node object or {@code null} if not found
     */
    EdgeDeviceNode getEdgeDeviceNode(int id) {
        return edgeDeviceNodes.get(id);
    }

    /**
     * Returns all edgeNodes from the AS.
     *
     * @return edgeNodes of the AS
     */
    public Collection<EdgeNode> getEdgeNodes() {
        return edgeNodes.values();
    }

    /**
     * Returns all backboneNodes from the AS.
     *
     * @return backboneNodes of the AS
     */
    public Collection<BackboneNode> getBackboneNodes() {
        return backboneNodes.values();
    }

    /**
     * Returns all host edgeDeviceNodes from the AS.
     *
     * @return edgeDeviceNodes of the AS
     */
    public Collection<EdgeDeviceNode> getEdgeDeviceNodes() {
        return edgeDeviceNodes.values();
    }

    /**
     * Adds an edge node to the AS.
     *
     * @param e edge node to add
     */
    void addEdgeNode(EdgeNode e) {
        edgeNodes.put(e.id, e);
    }

    /**
     * Adds a backbone node to the AS.
     *
     * @param b backbone node to add
     */
    void addBackboneNode(BackboneNode b) {
        backboneNodes.put(b.id, b);
    }

    /**
     * Adds a edge device node to the AS.
     *
     * @param d edge device node to add
     */
    void addDevice(EdgeDeviceNode d) {
        edgeDeviceNodes.put(d.id, d);
    }

    /**
     * Removes a node from the AS.
     *
     * @param node node to remove
     * @return true if node could be deleted, false if not
     */
    boolean removeNode(Node node) {
        boolean result = edgeNodes.remove(node.id) != null;

        if (!result) {
            result = backboneNodes.remove(node.id) != null;
        }
        if (!result) {
            result = edgeDeviceNodes.remove(node.id) != null;
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AS)) {
            return false;
        }

        AS other = (AS) obj;

        return id == other.id;
    }

    @Override
    public String toString() {
        return "AS: " + id;
    }
}
