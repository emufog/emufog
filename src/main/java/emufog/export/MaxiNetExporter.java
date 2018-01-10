package emufog.export;

import emufog.graph.Edge;
import emufog.graph.Graph;
import emufog.graph.Node;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is a framework with helper methods for classes that export a
 * graph object to a valid python file usable with the MaxiNet
 * (https://maxinet.github.io/) network emulation framework.
 */
public abstract class MaxiNetExporter implements IGraphExporter {

    /* list of all lines of the respective file in top down order */
    final List<String> lines;

    /* blank line object to reuse for all blank lines */
    private final String blankLine;

    /* mapping of edges to their respective connector */
    final Map<Edge, String> connectors;

    /**
     * Creates a new MaxiNet exporter instance.
     */
    MaxiNetExporter() {
        lines = new ArrayList<>();
        blankLine = "";
        connectors = new HashMap<>();
    }

    @Override
    public abstract void exportGraph(Graph graph, Path path) throws IllegalArgumentException, IOException;

    /**
     * Validates graph's relevant data to ensure that it will be able to
     * generate the appropriate python file.
     *
     * @param graph graph to validate
     * @return graph validity
     */
    abstract boolean validateGraph(Graph graph);

    /**
     * Writes all docker host nodeconfig of the graph to the output file.
     *
     * @param graph graph to export
     */
    abstract void addHosts(Graph graph);

    /**
     * Adds a blank line to the output file.
     */
    void addBlankLine() {
        lines.add(blankLine);
    }


    /**
     * Writes all switches that do not require docker to the output file.
     *
     * @param graph graph to export
     */
    void addSwitches(Graph graph) {
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
    void addConnectors(Graph graph) {
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
     * Established the connections between two nodeconfig based on the edges of the graph.
     *
     * @param graph graph to export
     */
    void addLinks(Graph graph) {
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
     * Adds a new link between two nodeconfig to the document.
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
     * Adds a new link between MininetHosts inside a multiApplication Fog node.
     * @param source
     * @param destination
     */
    private void addFogLink(String source, String destination) {
        lines.add("topo.addLink(" + source + ", " + destination + ", delay='0ms', bw='10000')");
    }

    /**
     * Writes the necessary imports at the top of the output file.
     */
    void setupImports() {
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
    void setupExperiment() {
        addBlankLine();
        lines.add("# create experiment");
        lines.add("cluster = maxinet.Cluster()");
        lines.add("exp = maxinet.Experiment(cluster, topo, switch=OVSSwitch)");
        lines.add("exp.setup()");
    }
}