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

import emufog.graph.AS
import emufog.util.ConversionsUtils.formatTimeInterval
import org.slf4j.LoggerFactory

/**
 * This class isolates the fog node placement algorithm of one of the autonomous systems to run it independent of
 * others. Requires the autonomous system to process.
 */
internal class FogWorker(private val system: AS, private val classifier: FogNodeClassifier) {

    companion object {
        private val LOG = LoggerFactory.getLogger(FogWorker::class.java)
    }

    /**
     * Runs the fog node placement algorithm on the associated autonomous system and returns the overall result object.
     *
     * @return result of the fog node placement
     */
    internal fun findFogNodes(): FogResult {
        // initialize empty result set
        val result = FogResult().also { it.setSuccess() }

        val heap = FogHeap(
            calculateShortestDistances(system, classifier.config.costThreshold),
            classifier.config.fogNodeTypes
        )

        while (!heap.isEmpty()) {
            // check if there are still fog nodes left to place
            if (!classifier.fogNodesLeft()) {
                return result.also { it.setFailure() }
            }

            val start = System.nanoTime()
            // find the next fog node for the remaining starting nodes
            val fogNode = heap.getNext()
            LOG.debug("Time to find next fog node for {}: {}", system, formatTimeInterval(start, System.nanoTime()))

            // reduce the remaining fog nodes available
            classifier.reduceRemainingNodes()

            result.addPlacement(FogNodePlacement(fogNode))
        }

        return result
    }
}
