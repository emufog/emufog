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
 * Represents a general node of the graph with the basic functionality. Can connect to other nodes via edges. Can also
 * hold an [EmulationNode] if that node needs emulation.
 */
abstract class Node internal constructor(
    /**
     * unique identifier of the node
     */
    val id: Int,
    /**
     * autonomous system this node belongs to
     */
    val system: AS,
    edges: List<Edge>,
    /**
     * emulation configuration of the node, can be unset
     */
    var emulationNode: EmulationNode?
) {

    private val edgesMutable: MutableList<Edge> = edges.toMutableList()

    /**
     * list of edges connected to that node
     */
    val edges: List<Edge>
        get() = edgesMutable

    /**
     * the edge degree of the node. Is based on the number of nodes this node is connected to via edges
     */
    val degree: Int
        get() = edges.size

    /**
     * the type of the node
     */
    abstract val type: NodeType

    /**
     * the name of the node
     */
    abstract val name: String

    /**
     * Returns identification if this node can be emulated with existing config.
     *
     * @return true if it can be emulated, false otherwise
     */
    fun hasEmulationSettings(): Boolean = emulationNode != null

    internal fun addEdge(edge: Edge) {
        edgesMutable.add(edge)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Node) {
            return false
        }

        return id == other.id
    }

    override fun hashCode(): Int = id

    override fun toString(): String = name
}
