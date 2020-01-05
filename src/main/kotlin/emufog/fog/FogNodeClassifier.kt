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

import emufog.graph.Graph
import java.util.concurrent.atomic.AtomicInteger
import kotlin.streams.toList

/**
 * The fog node classifier is running the fog node placement algorithm on the given graph object.
 */
class FogNodeClassifier(private val graph: Graph) {

    /**
     * the graph's config to use for the fog node classification
     */
    internal val config = graph.config

    /**
     * counter of remaining fog nodes to place in the graph, atomic for parallel access
     */
    private val counter = AtomicInteger(config.maxFogNodes)

    /**
     * Runs the fog node placement algorithm on the graph associated with this instance. All autonomous systems of the
     * graph are processed in parallel and the partial results are combined to a final outcome. The [Graph] instance
     * provided to this class will not be modified.
     *
     * @return result object of the fog node placement
     */
    fun findPossibleFogNodes(): FogResult {
        // process all systems in parallel
        val results = graph.systems
            .map { FogWorker(it, this).findFogNodes() }
            .toList()

        // init empty failed result
        val result = FogResult()
        // check if all part results are success
        val failed = results.firstOrNull { !it.status }
        if (failed == null) {
            result.setSuccess()
            results.forEach { result.addPlacements(it.placements) }
        }

        return result
    }

    /**
     * Indicates if there are still fog nodes to place available.
     *
     * @return `true` if there are, `false` if 0
     */
    internal fun fogNodesLeft(): Boolean = counter.get() > 0

    /**
     * Decrements the remaining fog node to place by 1.
     */
    internal fun reduceRemainingNodes() {
        counter.decrementAndGet()
    }
}