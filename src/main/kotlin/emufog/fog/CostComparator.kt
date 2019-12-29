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
package emufog.fog

/**
 * This comparator compares [BaseNode] based on their connection costs for a certain starting node. Connection costs
 * are based on [BaseNode.getCosts].
 *
 * @property startingNode node to get the connection costs for
 */
internal class CostComparator(private val startingNode: StartingNode) : Comparator<BaseNode> {

    override fun compare(o1: BaseNode, o2: BaseNode): Int {
        val costs1 = o1.getCosts(startingNode)
        checkNotNull(costs1) { "No costs for node: ${o1.node.id}" }
        val costs2 = o2.getCosts(startingNode)
        checkNotNull(costs2) { "No costs for node: ${o2.node.id}" }

        return costs1.compareTo(costs2)
    }
}
