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
package emufog.backbone

import emufog.graph.BackboneNodeConverter
import emufog.graph.Edge
import emufog.graph.Graph
import emufog.util.ConversionsUtils.formatTimeInterval
import org.slf4j.LoggerFactory

/**
 * This object runs the backbone classification algorithm on a graph instance.
 */
object BackboneClassifier {

    private val LOG = LoggerFactory.getLogger(BackboneClassifier::class.java)

    /**
     * Starts the backbone classification algorithm on the given graph.
     * Modifies the graph including backbone and edge of the network.
     */
    @JvmStatic
    fun identifyBackbone(graph: Graph) {
        // 1st step sequentially
        LOG.debug("Start Backbone Classification")
        var start = System.nanoTime()
        convertCrossAsEdges(graph.edges)
        LOG.debug("Graph Step 1 - Time: {}", formatTimeInterval(start, System.nanoTime()))
        LOG.debug("Backbone Size: {}", graph.backboneNodes.size)
        LOG.debug("Edge Size: {}", graph.edgeNodes.size)

        // 2nd step in parallel
        start = System.nanoTime()
        graph.systems.parallelStream().forEach { BackboneWorker.identifyBackbone(it) }
        LOG.debug("Graph Step 2 - Time: {}", formatTimeInterval(start, System.nanoTime()))
        LOG.debug("Backbone Size: {}", graph.backboneNodes.size)
        LOG.debug("Edge Size: {}", graph.edgeNodes.size)
        LOG.info("Finished Backbone Classification.")
    }

    /**
     * This methods converts all cross-AS edge's endpoints to backbone nodes.
     *
     * @param edges collection of edges to check
     */
    private fun convertCrossAsEdges(edges: Collection<Edge>) {
        edges.filter { it.isCrossASEdge() }.forEach {
            BackboneNodeConverter.convertToBackbone(it.source)
            BackboneNodeConverter.convertToBackbone(it.destination)
        }
    }
}