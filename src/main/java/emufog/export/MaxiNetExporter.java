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
package emufog.export;

import emufog.docker.DockerType;
import emufog.graph.Edge;
import emufog.graph.EmulationSettings;
import emufog.graph.Graph;
import emufog.graph.Node;
import emufog.settings.Settings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class exports a graph object to a valid python file usable with the
 * MaxiNet (https://maxinet.github.io/) network emulation framework.
 */
public class MaxiNetExporter implements IGraphExporter {

    /* list of all lines of the respective file in top down order */
    private final List<String> lines;

    /* blank line object to reuse for all blank lines */
    private final String blankLine;

    /* mapping of edges to their respective connector */
    private final Map<Edge, String> connectors;

    /**
     * Creates a new MaxiNet exporter instance.
     */
    public MaxiNetExporter() {
        lines = new ArrayList<>();
        blankLine = "";
        connectors = new HashMap<>();
    }

    @Override
    public void exportGraph(Graph graph, Path path) throws IllegalArgumentException, IOException {
        if (graph == null) {
            throw new IllegalArgumentException("The given graph object does not exist.");
        }
        if (path == null) {
            throw new IllegalArgumentException("The given path is null. Please provide a valid path");
        }

        // check if file exists and can be overwritten
        Settings settings = graph.getSettings();
        File file = path.toFile();
        if (!settings.overwriteExperimentFile && file.exists()) {
            throw new IllegalArgumentException("The given file already exist. Please provide a valid path");
        }

        // check the file ending of the given path
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.py");
        if (!matcher.matches(path)) {
            throw new IllegalArgumentException("The file name for MaxiNet hat to be a python file (.py)");
        }

        // initialize empty sets to start the writing
        lines.clear();
        connectors.clear();

        // begin to write the python file
        setupImports();

        addBlankLine();
        lines.add("topo = Topo()");
        addHosts(graph);
        addSwitches(graph);
        addConnectors(graph);
        addLinks(graph);
        setupExperiment();

        // set the overwrite option if feature is set in the settings file
        StandardOpenOption overwrite = settings.overwriteExperimentFile ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.APPEND;
        // write output in UTF-8 to the specified file
        Files.write(file.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, overwrite);
    }

    /**
     * Adds a blank line to the output file.
     */
    private void addBlankLine() {
        lines.add(blankLine);
    }

    /**
     * Writes all docker host nodes of the graph to the output file.
     *
     * @param graph graph to export
     */
    private void addHosts(Graph graph) {
        addBlankLine();
        lines.add("# add hosts");

        for (Node n : graph.getNodes().stream().filter(Node::hasEmulationSettings).collect(Collectors.toList())) {
            EmulationSettings emu = n.getEmulationNode();
            DockerType docker = emu.getDockerType();
            lines.add(n.getName() + " = topo.addHost(\"" + n.getName() + "\", cls=Docker, ip=\"" + emu.getIP() +
                    "\", dimage=\"" + docker.dockerImage + "\", mem_limit=" + docker.memoryLimit + ")");
        }
    }

    /**
     * Writes all switches that do not require docker to the output file.
     *
     * @param graph graph to export
     */
    private void addSwitches(Graph graph) {
        addBlankLine();
        lines.add("# add switches");

        List<Node> nodes = new ArrayList<>();
        nodes.addAll(graph.getRouters());
        nodes.addAll(graph.getSwitches());
        for (Node n : nodes.stream().filter(n -> !n.hasEmulationSettings()).collect(Collectors.toList())) {
            lines.add(n.getName() + " = topo.addSwitch(\"" + n.getName() + "\")");
        }
    }

    /**
     * Creates connectors between two hosts to run in MaxiNet.
     *
     * @param graph graph to export
     */
    private void addConnectors(Graph graph) {
        addBlankLine();
        lines.add("# add connectors");

        int counter = 0;
        for (Edge e : graph.getEdges()) {
            if (e.getSource().hasEmulationSettings() && e.getDestination().hasEmulationSettings()) {
                String name = "c" + counter;
                lines.add(name + " = topo.addSwitch(\"" + name + "\")");
                connectors.put(e, name);
                counter++;
            }
        }
    }

    /**
     * Established the connections between two nodes based on the edges of the graph.
     *
     * @param graph graph to export
     */
    private void addLinks(Graph graph) {
        addBlankLine();
        lines.add("# add links");

        for (Edge e : graph.getEdges()) {
            if (connectors.containsKey(e)) {
                String connector = connectors.get(e);
                addLink(e.getSource().getName(), connector, e.getDelay() / 2, e.getBandwidth());
                addLink(connector, e.getDestination().getName(), e.getDelay() / 2, e.getBandwidth());
            } else {
                addLink(e.getSource().getName(), e.getDestination().getName(), e.getDelay(), e.getBandwidth());
            }
        }
    }

    /**
     * Adds a new link between two nodes to the document.
     *
     * @param source      source of the link
     * @param destination destination of the link
     * @param latency     latency applied to this link
     * @param bandwidth   bandwidth limitations of this link
     */
    private void addLink(String source, String destination, float latency, float bandwidth) {
        lines.add("topo.addLink(" + source + ", " + destination +
                ", delay='" + latency + "ms', bw=" + bandwidth + ")");
    }

    /**
     * Writes the necessary imports at the top of the output file.
     */
    private void setupImports() {
        lines.add("#!/usr/bin/env python2");
        addBlankLine();
        lines.add("import time");
        addBlankLine();
        lines.add("from MaxiNet.Frontend import maxinet");
        lines.add("from MaxiNet.Frontend.container import Docker");
        lines.add("from mininet.topo import Topo");
        lines.add("from mininet.node import OVSSwitch");
    }

    /**
     * Writes the lines to setup and start an experiment in MaxiNet.
     */
    private void setupExperiment() {
        addBlankLine();
        lines.add("# create experiment");
        lines.add("cluster = maxinet.Cluster()");
        lines.add("exp = maxinet.Experiment(cluster, topo, switch=OVSSwitch)");
        lines.add("exp.setup()");
    }
}
