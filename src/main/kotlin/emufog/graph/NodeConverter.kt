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
package emufog.graph

/**
 * A node converter simplifies the conversion of a node to a different type.
 */
abstract class NodeConverter<T : Node> {

    /**
     * Creates a new node based on the given old node. The type of the new node is based on the subclass of [Node].
     *
     * @param oldNode node to create a new node from
     * @return the newly created node
     */
    protected abstract fun createNewNode(oldNode: Node): T

    /**
     * Adds the new node to the respective list in the graph.
     *
     * @param newNode the new node to add
     */
    protected abstract fun addNodeToGraph(newNode: T)

    /**
     * Converts the given node to a different type and replace it in the associated graph. If the node is already an
     * instance of the requested class the method just returns this object.
     *
     * @param oldNode node to convert
     * @return the replacing node
     */
    fun convert(oldNode: Node): T {
        // remove the old node from the graph
        oldNode.system.removeNode(oldNode)

        // create a new node of the requested type
        val newNode=createNewNode(oldNode)

        // add the new node to the graph
        addNodeToGraph(newNode)

        return newNode
    }
}