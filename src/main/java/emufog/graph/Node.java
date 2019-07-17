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

import java.util.Arrays;

/**
 * Represents a general node of graph with the basic functionality.
 * Can connect to other nodes via edges and contains the geographically placement on a plain.
 */
public abstract class Node {

    /* unique identifier of the node */
    final int id;

    /* autonomous system this node belongs to */
    final AS as;

    /* list of edges associated with the node */
    Edge[] edges;

    /* emulation settings for this node */
    EmulationSettings emulationSettings;

    /**
     * Creates a node of the graph with the initial parameter given.
     *
     * @param id unique identifier
     * @param as autonomous system the belongs to
     */
    Node(int id, AS as) {
        this.id = id;
        this.as = as;
        edges = new Edge[0];
    }

    /**
     * Returns the emulation settings associated. If there are none returns {@code null}.
     *
     * @return emulation settings or {@code null}
     */
    public EmulationSettings getEmulationNode() {
        return emulationSettings;
    }

    /**
     * Returns identification if this node can be emulated with existing settings.
     *
     * @return true if it can be emulated, false otherwise
     */
    public boolean hasEmulationSettings() {
        return emulationSettings != null;
    }

    /**
     * Returns the unique identifier of the node.
     *
     * @return unique identifier
     */
    public int getID() {
        return id;
    }

    /**
     * Returns an array of all edges associated with the node.
     *
     * @return array of the node's edges
     */
    public Edge[] getEdges() {
        return edges;
    }

    /**
     * Returns the name of the node.
     *
     * @return name of the node
     */
    public abstract String getName();

    /**
     * Returns the edge degree of the node.
     *
     * @return number of edges associated with the node
     */
    public int getDegree() {
        return edges.length;
    }

    /**
     * Returns the autonomous system consisting the node.
     *
     * @return node's AS
     */
    public AS getAS() {
        return as;
    }

    /**
     * Adds an edge to the array of edges associated with this node. Grows the array by one to add an edge.
     *
     * @param edge edge to add to node
     */
    void addEdge(Edge edge) {
        edges = Arrays.copyOf(edges, edges.length + 1);
        edges[edges.length - 1] = edge;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;

        if (o instanceof Node) {
            result = ((Node) o).id == id;
        }

        return result;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return getName();
    }
}
