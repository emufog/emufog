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

import emufog.graph.AS
import emufog.graph.Edge
import emufog.graph.Graph
import emufog.graph.Node
import emufog.util.ConversionsUtils.formatTimeInterval
import org.slf4j.LoggerFactory

internal val LOG = LoggerFactory.getLogger("Backbone")

/**
 * Starts the backbone classification algorithm on the given graph. Modifies the graph including backbone and edge of
 * the network.
 */
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
    graph.systems.forEach { identifyBackbone(it) }
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
internal fun convertCrossAsEdges(edges: Collection<Edge>) {
    edges.filter { it.isCrossASEdge() }.forEach {
        it.source.toBackboneNode()
        it.destination.toBackboneNode()
    }
}

internal fun identifyBackbone(system: AS) {
    //2nd step
    var start = System.nanoTime()
    convertHighDegrees(system)
    LOG.info("{} Step 2 - Time: {}", system, formatTimeInterval(start, System.nanoTime()))
    LOG.info("{} Backbone Size: {}", system, system.backboneNodes.size)
    LOG.info("{} Edge Size: {}", system, system.edgeNodes.size)

    // 3rd step
    start = System.nanoTime()
    BackboneConnector(system).connectBackbone()
    LOG.info("{} Step 3 - Time: {}", system, formatTimeInterval(start, System.nanoTime()))
    LOG.info("{} Backbone Size: {}", system, system.backboneNodes.size)
    LOG.info("{} Edge Size: {}", system, system.edgeNodes.size)
}

/**
 * Converts nodes with an above average degree to a backbone node.
 */
internal fun convertHighDegrees(system: AS) {
    val averageDegree = calculateAverageDegree(system)
    system.edgeNodes.filter { it.degree > averageDegree }.forEach { it.toBackboneNode() }
}

/**
 * Returns the average degree of the autonomous system based on the router and switch nodes.
 *
 * @return the average degree
 */
private fun calculateAverageDegree(system: AS): Double {
    var average = 0.0
    var count = 0
    val updateAverage: (Node) -> Unit = {
        count++
        average += (it.degree - average) / count
    }

    system.edgeNodes.forEach { updateAverage(it) }
    system.backboneNodes.forEach { updateAverage(it) }

    return average
}

internal fun Node?.toBackboneNode() = this?.let { system.replaceByBackboneNode(this) }
