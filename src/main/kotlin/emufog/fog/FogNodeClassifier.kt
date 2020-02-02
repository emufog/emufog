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

import emufog.config.Config
import emufog.graph.AS
import emufog.graph.Graph
import emufog.util.debugTiming
import emufog.util.getLogger
import java.util.concurrent.atomic.AtomicInteger

internal val LOG = getLogger("Fog Node Placement")

/**
 * Runs the fog node placement algorithm on the given graph instance. Returns a [FogResult] containing the outcome of
 * the algorithm. The graph instance will not be modified. The given config is used for the tuning of the algorithm.
 *
 * @param graph graph instance to run the algorithm on
 * @param config configuration for the algorithm
 * @return result set of the algorithm
 */
fun findPossibleFogNodes(graph: Graph, config: Config): FogResult {
    return FogNodeClassifier(graph, config).findPossibleFogNodes()
}

/**
 * The fog node classifier is running the fog node placement algorithm on the given graph object.
 */
internal class FogNodeClassifier(private val graph: Graph, internal val config: Config) {

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
        val results = graph.systems.map {
            LOG.debugTiming("Find fog nodes in $it") { findFogNodesIn(it, this) }
        }.toList()

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

/**
 * Runs the fog node placement algorithm on the associated autonomous system and returns the overall result object.
 *
 * @return result of the fog node placement
 */
internal fun findFogNodesIn(system: AS, classifier: FogNodeClassifier): FogResult {
    // initialize empty result set
    val result = FogResult().also { it.setSuccess() }

    val nodes = LOG.debugTiming("Calculate distances for edge nodes") {
        calculateShortestDistances(system, classifier.config.costThreshold)
    }
    val heap = FogHeap(nodes, classifier.config.fogNodeTypes)

    while (!heap.isEmpty()) {
        // check if there are still fog nodes left to place
        if (!classifier.fogNodesLeft()) {
            LOG.warn("No more fog nodes left to place. Aborting.")
            return result.also { it.setFailure() }
        }

        // find the next fog node for the remaining starting nodes
        val fogNode = LOG.debugTiming("$system Time to find next fog node") { heap.getNext() }
        LOG.debug("{} Next selected fog node: {}", system, fogNode)

        // reduce the remaining fog nodes available
        classifier.reduceRemainingNodes()

        result.addPlacement(FogNodePlacement(fogNode))
    }

    return result
}