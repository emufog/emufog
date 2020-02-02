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
package emufog.device

import emufog.config.Config
import emufog.container.DeviceContainer
import emufog.graph.AS
import emufog.graph.Edge
import emufog.graph.EdgeDeviceNode
import emufog.graph.EdgeNode
import emufog.graph.Graph
import emufog.util.IDManager
import kotlin.math.abs
import kotlin.random.Random

/**
 * Randomly distributes [EdgeDeviceNode] in the [Graph] instance based on the provided configuration. Each
 * [DeviceContainer] from [Config.deviceNodeTypes] will be placed with the average distribution
 * [DeviceContainer.averageDeviceCount]. The latency and bandwidth is based on the configuration.
 *
 * @param graph graph to assign edge device nodes to
 * @param config configuration for device distribution
 */
fun assignDeviceNodes(graph: Graph, config: Config) {
    DeviceManager(graph, config).assignEdgeDevices()
}

internal fun getRandomCount(upper: Float): Int {
    require(upper >= 0) { "The upper limit must be positive." }

    return (Random.nextFloat() * upper).toInt()
}

internal class DeviceManager(private val graph: Graph, private val config: Config) {

    private val nodeIdManager = IDManager()

    private val edgeIdManager = IDManager()

    /**
     * Assigns the devices specified in the [config] to the edge nodes on a random base.
     */
    internal fun assignEdgeDevices() {
        config.deviceNodeTypes.forEach { assignDeviceType(it) }
    }

    /**
     * Random distribution within the interval from 0 to [DeviceContainer.averageDeviceCount] * 2
     */
    private fun assignDeviceType(deviceType: DeviceContainer) {
        val upper = abs(deviceType.averageDeviceCount) * 2
        graph.edgeNodes.forEach {
            val count = getRandomCount(upper)
            for (i in 0 until count) {
                val device = graph.createEdgeDeviceNode(it.system, deviceType)
                graph.createEdge(it, device)
            }
        }
    }

    private fun Graph.createEdgeDeviceNode(system: AS, deviceType: DeviceContainer): EdgeDeviceNode {
        var node: EdgeDeviceNode? = null
        while (node == null) {
            val id = nodeIdManager.getNextID()
            try {
                node = this.createEdgeDeviceNode(id, system, deviceType)
            } catch (e: IllegalArgumentException) {
            }
            nodeIdManager.setUsed(id)
        }

        return node
    }

    internal fun Graph.createEdge(edgeNode: EdgeNode, device: EdgeDeviceNode): Edge {
        var edge: Edge? = null
        while (edge == null) {
            val id = edgeIdManager.getNextID()
            try {
                edge = this.createEdge(id, edgeNode, device, config.hostDeviceLatency, config.hostDeviceBandwidth)
            } catch (e: IllegalArgumentException) {
            }
            edgeIdManager.setUsed(id)
        }

        return edge
    }
}