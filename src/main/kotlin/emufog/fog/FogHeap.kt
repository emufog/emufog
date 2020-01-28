/*
 * MIT License
 *
 * Copyright (c) 2020 emufog contributors
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
package emufog.fog

import emufog.container.FogContainer
import emufog.util.Heap
import emufog.util.PriorityHeap

internal class FogHeap(baseNodes: Set<BaseNode>, private val fogTypes: Collection<FogContainer>) {

    private val heap: Heap<BaseNode> = PriorityHeap(FogComparator())

    init {
        if (fogTypes.isNotEmpty()) {
            baseNodes.forEach {
                it.determineFogType(fogTypes)
                heap.add(it)
            }
        }
    }

    internal fun isEmpty(): Boolean = heap.isEmpty()

    internal fun getNext(): BaseNode {
        val result = heap.pop()
        checkNotNull(result) { "The heap of fog nodes is empty." }

        updateFogHeap(result)

        return result
    }

    private fun updateFogHeap(result: BaseNode) {
        val nodes = HashSet<BaseNode>().also { it.add(result) }
        val toRemove = HashSet<BaseNode>().also { it.add(result) }

        // get covered nodes by the fog node placement
        result.coveredNodes.forEach { it.first.decreaseDeviceCount(it.second) }
        val coveredNodes = result.coveredNodes.map { it.first }.filter { it.deviceCount <= 0 }
        toRemove.addAll(coveredNodes)

        // track all involved nodes, remove the covered nodes from the graph
        coveredNodes.forEach {
            nodes.addAll(it.possibleNodes)
            it.removeFromPossibleNodes()
        }

        // filter all non connected nodes
        toRemove.addAll(nodes.filterNot { it.hasConnections() })

        // filter all nodes that need an update
        val toUpdate = nodes.filter { !toRemove.contains(it) && it.modified }

        toRemove.forEach { heap.remove(it) }
        toUpdate.forEach {
            it.determineFogType(fogTypes)
            heap.update(it)
        }
    }
}