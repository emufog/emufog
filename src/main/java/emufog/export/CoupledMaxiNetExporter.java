package emufog.export;

import emufog.docker.DockerType;
import emufog.graph.EmulationSettings;
import emufog.graph.Graph;
import emufog.graph.Node;
import emufog.settings.Settings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.stream.Collectors;

/**
 * This class exports a graph object to a valid python file usable with the
 * MaxiNet (https://maxinet.github.io/) network emulation framework. The
 * input graph object will have the FogType coupled with DockerImage.
 */
public class CoupledMaxiNetExporter extends MaxiNetExporter {

    @Override
    public void exportGraph(Graph graph, Path path) throws IllegalArgumentException, IOException {
        if (graph == null) {
            throw new IllegalArgumentException("The given graph object does not exist.");
        }
        if(!validateGraph(graph)) {
            throw new IllegalArgumentException("The given graph object does not have the necessary data to export a " +
                    "coupled version of the MaxiNet compatible python file.");
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
            throw new IllegalArgumentException("The file name for MaxiNet has to be a python file (.py)");
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
        StandardOpenOption overwrite = settings.overwriteExperimentFile
                ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.APPEND;
        // write output in UTF-8 to the specified file
        Files.write(file.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, overwrite);
    }

    @Override
    boolean validateGraph(Graph graph) {
        return graph.getEdges() != null
                && graph.getRouters() != null
                && graph.getSwitches() != null
                && graph.getSettings() != null
                && graph.getNodes() != null;
    }

    @Override
    void addHosts(Graph graph) {
        addBlankLine();
        lines.add("# add hosts");

        for (Node n : graph.getNodes().stream().filter(Node::hasEmulationSettings).collect(Collectors.toList())) {
            EmulationSettings emu = n.getEmulationNode();
            DockerType docker = emu.getDockerType();
            lines.add(n.getName() + " = topo.addHost(\"" + n.getName() + "\", cls=Docker, ip=\"" + emu.getIP() +
                    "\", dimage=\"" + docker.dockerImage + "\", mem_limit=" + docker.memoryLimit + ")");
        }
    }
}
