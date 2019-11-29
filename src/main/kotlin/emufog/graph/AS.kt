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
 * This class represents an autonomous system of the network graph. Hence it's a sub graph of
 * the total graph providing access to its nodes.
 */
class AS internal constructor(
        /**
         * unique identifier of the autonomous system
         */
        val id: Int) {

    /**
     * mapping of edge nodes in the autonomous system
     */
    private val edges: MutableMap<Int,EdgeNode>

    val edgeNodes: Collection<EdgeNode>
        get()=edges.values

    /**
     * mapping of backbone nodes in the autonomous system
     */
    private val backbones: MutableMap<Int,BackboneNode>

    val backboneNodes: Collection<BackboneNode>
        get()=backbones.values

    /**
     * mapping of edge device nodes in the autonomous system
     */
    private val edgeDevices: MutableMap<Int,EdgeDeviceNode>

    val edgeDeviceNodes: Collection<EdgeDeviceNode>
        get()=edgeDevices.values

    init {
        edges=HashMap()
        backbones=HashMap()
        edgeDevices=HashMap()
    }

    /**
     * Returns the edge node associated with the given ID from the AS.
     *
     * @param id the edge node's ID
     * @return node object or `null` if not found
     */
    fun getEdgeNode(id: Int): EdgeNode?=edges[id]

    /**
     * Returns the backbone node associated with the given ID from the AS.
     *
     * @param id the backbone node's ID
     * @return node object or `null` if not found
     */
    fun getBackboneNode(id: Int): BackboneNode?=backbones[id]

    /**
     * Returns the edge device node associated with the given ID from the AS.
     *
     * @param id the edge device node's ID
     * @return node object or `null` if not found
     */
    fun getEdgeDeviceNode(id: Int): EdgeDeviceNode?=edgeDevices[id]

    /**
     * Adds an edge node to the AS.
     *
     * @param e edge node to add
     */
    internal fun addEdgeNode(e: EdgeNode) {
        edges[e.id]=e
    }

    /**
     * Adds a backbone node to the AS.
     *
     * @param b backbone node to add
     */
    internal fun addBackboneNode(b: BackboneNode) {
        backbones[b.id]=b
    }

    /**
     * Adds a edge device node to the AS.
     *
     * @param d edge device node to add
     */
    internal fun addDevice(d: EdgeDeviceNode) {
        edgeDevices[d.id]=d
    }

    /**
     * Removes a node from the AS.
     *
     * @param node node to remove
     * @return true if node could be deleted, false if not
     */
    internal fun removeNode(node: Node): Boolean {
        var result=edges.remove(node.id)!=null
        if (!result) {
            result=backbones.remove(node.id)!=null
        }
        if (!result) {
            result=edgeDevices.remove(node.id)!=null
        }

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AS) {
            return false
        }

        return id==other.id
    }

    override fun hashCode(): Int=Integer.hashCode(id)

    override fun toString(): String="AS: $id"
}