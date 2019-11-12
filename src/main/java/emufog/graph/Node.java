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

import java.util.List;

/**
 * Represents a general node of  graph with the basic functionality.
 * Can connect to other nodes via edges and contains the geographically placement on a plain.
 */
public abstract class Node {

    final NodeAttributes attributes;

    /**
     * emulation config for this node
     */
    private EmulationSettings emulationSettings;

    Node(NodeAttributes attributes) {
        this.attributes = attributes;
        this.attributes.setNode(this);
    }

    /**
     * Returns the emulation config associated. If there are none returns {@code null}.
     *
     * @return emulation config or {@code null}
     */
    public EmulationSettings getEmulationNode() {
        return emulationSettings;
    }

    /**
     * Returns the unique identifier of the node.
     *
     * @return unique identifier
     */
    public int getID() {
        return attributes.id;
    }

    /**
     * Returns a list of all edges associated with the node.
     *
     * @return list of the node's edges
     */
    public List<Edge> getEdges() {
        return attributes.edges;
    }

    /**
     * Returns the name of the node.
     *
     * @return name of the node
     */
    public abstract String getName();

    public abstract NodeType getType();

    /**
     * Returns the edge degree of the node.
     *
     * @return number of edges associated with the node
     */
    public int getDegree() {
        return getEdges().size();
    }

    /**
     * Returns the autonomous system consisting the node.
     *
     * @return node's AS
     */
    public AS getAS() {
        return attributes.as;
    }

    /**
     * Returns identification if this node can be emulated with existing config.
     *
     * @return true if it can be emulated, false otherwise
     */
    public boolean hasEmulationSettings() {
        return emulationSettings != null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Node)) {
            return false;
        }

        Node other = (Node) o;

        return attributes.equals(other.attributes);
    }

    @Override
    public int hashCode() {
        return attributes.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }

    void setEmulationSettings(EmulationSettings settings) {
        emulationSettings = settings;
    }
}
