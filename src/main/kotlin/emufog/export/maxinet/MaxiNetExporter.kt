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
package emufog.export.maxinet

import emufog.export.GraphExporter
import emufog.graph.Edge
import emufog.graph.Graph
import emufog.graph.Node
import emufog.graph.NodeType.BACKBONE_NODE
import emufog.graph.NodeType.EDGE_DEVICE_NODE
import emufog.graph.NodeType.EDGE_NODE
import java.io.BufferedWriter
import java.nio.file.FileSystems
import java.nio.file.Path

/**
 * This object exports a graph object to a valid python file usable with the [MaxiNet](https://maxinet.github.io/)
 * network emulation framework.
 */
object MaxiNetExporter : GraphExporter {

    override fun exportGraph(graph: Graph, path: Path) {
        MaxiNetExporterImpl(graph, path).exportGraph()
    }
}

private class MaxiNetExporterImpl internal constructor(private val graph: Graph, path: Path) {

    private val writer: BufferedWriter

    /**
     * mapping of edges to their respective connector
     */
    private val connectors: MutableMap<Edge, String> = HashMap()

    init {
        // check if file exists and can be overwritten
        val config = graph.config
        val file = path.toFile()
        require(!(!config.overWriteOutputFile && file.exists())) {
            "The given file already exist. Please provide a valid path."
        }

        writer = path.toFile().bufferedWriter()

        // check the file ending of the given path
        val matcher = FileSystems.getDefault().getPathMatcher("glob:**.py")
        require(matcher.matches(path)) { "The file name for MaxiNet has to be a python file (.py)." }
    }

    private fun BufferedWriter.writeln(s: String) {
        this.write(s)
        this.newLine()
    }

    internal fun exportGraph() {
        // begin to write the python file
        writer.writeln("#!/usr/bin/env python2")
        writer.newLine()
        writer.writeln("import time")
        writer.newLine()
        writer.writeln("from MaxiNet.Frontend import maxinet")
        writer.writeln("from MaxiNet.Frontend.container import Docker")
        writer.writeln("from mininet.topo import Topo")
        writer.writeln("from mininet.node import OVSSwitch")
        writer.newLine()
        writer.writeln("topo = Topo()")
        writer.newLine()
        addHosts()
        addSwitches()
        addConnectors()
        addLinks()
        writer.newLine()
        writer.writeln("# create experiment")
        writer.writeln("cluster = maxinet.Cluster()")
        writer.writeln("exp = maxinet.Experiment(cluster, topo, switch=OVSSwitch)")
        writer.writeln("exp.setup()")

        writer.flush()
        writer.close()
    }

    /**
     * Writes all container host nodes of the graph to the output file.
     */
    private fun addHosts() {
        writer.writeln("# add hosts")
        graph.nodes
            .filter { it.hasEmulationSettings() }
            .forEach {
                val emu = it.emulationNode!!
                val container = emu.container
                writer.writeln(
                    String.format(
                        "%s = topo.addHost(\"%s\", cls=Docker, ip=\"%s\", dimage=\"%s\", mem_limit=%d)",
                        getName(it),
                        getName(it),
                        emu.ip,
                        container.fullName(),
                        container.memoryLimit
                    )
                )
            }
    }

    /**
     * Writes all switches that do not require container to the output file.
     */
    private fun addSwitches() {
        writer.newLine()
        writer.writeln("# add switches")
        graph.edgeNodes.union(graph.backboneNodes)
            .filter { !it.hasEmulationSettings() }
            .forEach { writer.writeln(String.format("%s = topo.addSwitch(\"%s\")", getName(it), getName(it))) }
    }

    /**
     * Creates connectors between two hosts to run in MaxiNet.
     */
    private fun addConnectors() {
        writer.writeln("")
        writer.writeln("# add connectors")
        var counter = 0
        graph.edges
            .filter { it.source.hasEmulationSettings() && it.destination.hasEmulationSettings() }
            .forEach {
                val name = "c$counter"
                writer.writeln(String.format("%s = topo.addSwitch(\"%s\")", name, name))
                connectors[it] = name
                counter++
            }
    }

    /**
     * Established the connections between two nodes based on the edges of the graph.
     */
    private fun addLinks() {
        writer.writeln("")
        writer.writeln("# add links")
        graph.edges.forEach {
            if (connectors.containsKey(it)) {
                val connector = connectors[it]
                addLink(getName(it.source), connector, it.latency / 2, it.bandwidth)
                addLink(connector, getName(it.destination), it.latency / 2, it.bandwidth)
            } else {
                addLink(getName(it.source), getName(it.destination), it.latency, it.bandwidth)
            }
        }
    }

    /**
     * Adds a new link between two nodes to the document.
     *
     * @param source source of the link
     * @param destination destination of the link
     * @param latency latency applied to this link
     * @param bandwidth bandwidth limitations of this link
     */
    private fun addLink(source: String?, destination: String?, latency: Float, bandwidth: Float) {
        writer.writeln(
            String.format("topo.addLink(%s, %s, delay='%fms', bw=%f)", source, destination, latency, bandwidth)
        )
    }

    private fun getName(node: Node): String {
        val prefix = when (node.type) {
            BACKBONE_NODE -> "s"
            EDGE_NODE -> "r"
            EDGE_DEVICE_NODE -> "h"
        }

        return prefix + node.id
    }
}