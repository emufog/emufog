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
 * hold an [EmulationNode] if that node should be emulated by a container.
 *
 * @property id unique identifier of the node
 * @property system the autonomous system this node belongs to
 * @property emulationNode emulation configuration of the node, can be not set
 * @property edges list of edges connected to this node
 * @property degree the edge degree of the node. Is based on the number of nodes this node is connected to via edges
 * @property type the type of this node
 */
abstract class Node internal constructor(
    val id: Int,
    val system: AS,
    edges: List<Edge>,
    emulationNode: EmulationNode?
) {

    private val edgesMutable: MutableList<Edge> = edges.toMutableList()

    val edges: List<Edge>
        get() = edgesMutable

    val degree: Int
        get() = edges.size

    open val emulationNode: EmulationNode?
        get() = emulationNodeMutable

    private var emulationNodeMutable: EmulationNode? = emulationNode

    abstract val type: NodeType

    /**
     * Returns whether this node is associated with an emulation configuration.
     */
    fun hasEmulationSettings(): Boolean = emulationNode != null

    internal fun setEmulationNode(emulationNode: EmulationNode?) {
        emulationNodeMutable = emulationNode
    }

    /**
     * Adds an [Edge] instance to the list of edges [edges].
     */
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

    override fun toString(): String = "Node: $id"
}
