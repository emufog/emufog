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

/**
 * This class convert an existing node to a edge node in the graph.
 */
public class EdgeNodeConverter extends NodeConverter<EdgeNode> {

    private static final EdgeNodeConverter INSTANCE = new EdgeNodeConverter();

    @Override
    EdgeNode createNewNode(Node oldNode) {
        return new EdgeNode(oldNode.attributes);
    }

    @Override
    void addNodeToGraph(EdgeNode newNode) {
        newNode.getAS().addEdgeNode(newNode);
    }

    /**
     * Converts an arbitrary node to an edge node. If the node is already an edge node
     * it will just be returned. Otherwise the old node will be removed from the AS and
     * replaced by the new node.
     *
     * @param oldNode old node to replace by an edge node
     * @return newly created edge node instance
     */
    public static EdgeNode convertToEdgeNode(Node oldNode) {
        return INSTANCE.convert(oldNode);
    }
}
