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
 * A node converter simplifies the conversion of a node to a different type.
 */
abstract class NodeConverter {

    /**
     * Creates a new node based on the given old node.
     * The type of the new node is based on the subclass.
     *
     * @param oldNode node to create a new node from
     * @return the newly created node
     */
    abstract Node createNewNode(Node oldNode);

    /**
     * Adds the new node to the respective list in the graph.
     *
     * @param newNode the new node to add
     */
    abstract void addNodeToGraph(Node newNode);

    /**
     * Checks if the given node needs to be converted by the specific converter.
     *
     * @param oldNode node to check
     * @return true if the given node needs to be converted
     */
    abstract boolean needsConversion(Node oldNode);

    /**
     * Converts the given node to a different type and replace it in the associated graph.
     * If the node is already an instance of the requested class the method just returns this object.
     *
     * @param oldNode node to convert
     * @return the replacing node or {@code null} if the given node is {@code null}
     */
    Node convert(Node oldNode) {
        if (oldNode == null) {
            return null;
        }
        if (!needsConversion(oldNode)) {
            return oldNode;
        }

        // remove the old node from the graph
        oldNode.as.removeNode(oldNode);

        // create a new node of the requested type
        Node newNode = createNewNode(oldNode);

        // update links from the old node
        newNode.copyFromOldNode(oldNode);

        // add the new node to the graph
        addNodeToGraph(newNode);

        return newNode;
    }
}
