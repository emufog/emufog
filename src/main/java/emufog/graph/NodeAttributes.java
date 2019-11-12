/*
 * MIT License
 *
 * Copyright (c) 2019 emufog contributors
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

import java.util.ArrayList;
import java.util.List;

class NodeAttributes {

    /**
     * unique identifier of the node
     */
    final int id;

    /**
     * autonomous system this node belongs to
     */
    final AS as;

    /**
     * list of edges associated with the node
     */
    final List<Edge> edges;

    /**
     * the node object tied to the unique id
     */
    private Node node;

    NodeAttributes(int id, AS as) {
        this.id = id;
        this.as = as;
        edges = new ArrayList<>();
    }

    Node getNode() {
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NodeAttributes)) {
            return false;
        }

        NodeAttributes other = (NodeAttributes) o;

        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    void addEdge(Edge e) {
        edges.add(e);
    }

    void setNode(Node node) {
        this.node = node;
    }
}
