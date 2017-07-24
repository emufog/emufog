package emufog.export;

import emufog.docker.DockerType;
import emufog.graph.Edge;
import emufog.graph.EmulationSettings;
import emufog.graph.Graph;
import emufog.graph.Node;
import emufog.settings.Settings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class exports a graph object to a valid python file usable with the
 * MaxiNet (https://maxinet.github.io/) network emulation framework.
 */
public class MaxiNetExporter extends GraphExporter {

    /* list of all lines of the respective file in top down order */
    private final List<String> lines;

    /* blank line object to reuse for all blank lines */
    private final String blankLine;

    /**
     * Creates a new MaxiNet exporter with the given parameter.
     *
     * @param path     path to the python file
     * @param settings settings that apply to that export
     * @throws IllegalArgumentException The file ending has to be .py for python files.
     */
    public MaxiNetExporter(Path path, Settings settings) throws IllegalArgumentException {
        super(path, settings);

        // check if the file format is a python file
        if (!file.getName().endsWith(".py")) {
            throw new IllegalArgumentException("The filename has to be a valid python filename.");
        }

        lines = new ArrayList<>();
        blankLine = "";
    }

    @Override
    public boolean exportGraph(Graph graph) throws IllegalArgumentException {
        if (graph == null) {
            throw new IllegalArgumentException("The given graph object does not exist.");
        }

        boolean result;

        // begin to write the python file
        setupImports();

        addBlankLine();
        lines.add("topo = Topo()");
        addHosts(graph);
        addSwitches(graph);
        addLinks(graph);
        setupExperiment();

        try {
            // set the overwrite option if feature is set in the settings file
            StandardOpenOption overwrite = settings.overwriteExperimentFile ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.APPEND;
            // write output in UTF-8 to the specified file
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, overwrite);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }

        return result;
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

        for (Node n : graph.getNodes()) {
            if (n.hasEmulationSettings()) {
                EmulationSettings emu = n.getEmulationNode();
                DockerType docker = emu.getDockerType();
                lines.add(n.getName() + " = topo.addHost(\"" + n.getName() + "\", cls=Docker, ip=\"" + emu.getIP() +
                        "\", dimage=\"" + docker.dockerImage + "\", mem_limit=" + docker.memoryLimit + ")");
            }
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
        for (Node n : nodes) {
            if (!n.hasEmulationSettings()) {
                lines.add(n.getName() + " = topo.addSwitch(\"" + n.getName() + "\")");
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
            lines.add("topo.addLink(" + e.getSource().getName() + ", " + e.getDestination().getName() +
                    ", delay='" + e.getDelay() + "ms', bw=" + e.getBandwidth() + ")");
        }
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

    @Override
    protected void validateFileName(Path path) throws IllegalArgumentException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.py");

        if (!matcher.matches(path)) {
            throw new IllegalArgumentException("The file name for MaxiNet hat to be a python file (.py)");
        }
    }
}
