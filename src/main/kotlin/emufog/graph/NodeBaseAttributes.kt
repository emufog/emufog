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
package emufog.graph

/**
 * Basic attributes of a [Node] in the graph. The node is defined by the unique identifier [id]. The actual
 * representation of the [Node] can be retrieved via [node].
 */
internal class NodeBaseAttributes(
    /**
     * unique identifier of the node
     */
    val id: Int,
    /**
     * autonomous system this node belongs to
     */
    val system: AS
) {

    /**
     * private mutable list of edges to be modified
     */
    private val edgesMutable: MutableList<Edge>

    /**
     * list of edges associated with the node
     */
    val edges: List<Edge>
        get() = edgesMutable

    init {
        edgesMutable = ArrayList()
    }

    /**
     * the [Node] object tied to the unique id
     */
    var node: Node? = null

    /**
     * Adds an [Edge] to the list of edges [edges].
     */
    fun addEdge(e: Edge): Boolean = edgesMutable.add(e)

    override fun equals(other: Any?): Boolean {
        if (other !is NodeBaseAttributes) {
            return false
        }

        return id == other.id
    }

    override fun hashCode(): Int = id
}