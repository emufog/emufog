package emufog.export;

import emufog.docker.DockerType;
import emufog.graph.EmulationSettings;
import emufog.graph.Graph;
import emufog.graph.Node;
import emufog.settings.Settings;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class exports a graph object to a valid python file usable with the
 * MaxiNet (https://maxinet.github.io/) network emulation framework. The
 * input graph object will have the FogType coupled with DockerImage.
 */
public class DecoupledMaxiNetExporter extends MaxiNetExporter {

    private static final String PYTHON_HELPERS_FILEPATH = "./src/main/java/emufog/export/MaxiNetPythonHelpers.py";

    @Override
    public void exportGraph(Graph graph, Path path) throws IllegalArgumentException, IOException {
        if (graph == null) {
            throw new IllegalArgumentException("The given graph object does not exist.");
        }
        if(!validateGraph(graph)) {
            throw new IllegalArgumentException("The given graph object does not have the necessary data to export a " +
                    "decoupled version of the MaxiNet compatible python file.");
        }
        if (path == null) {
            throw new IllegalArgumentException("The given path is null. Please provide a valid path");
        }

        // check if file exists and can be overwritten
        Settings settings = graph.getSettings();

        File file = path.toFile();
        if (!settings.isOverwriteExperimentFile() && file.exists()) {
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
        addCommands(graph);
        setupExperiment();
        setupPythonHelpers();

        // set the overwrite option if feature is set in the settings file
        StandardOpenOption overwrite = settings.isOverwriteExperimentFile() ? StandardOpenOption.TRUNCATE_EXISTING : StandardOpenOption.APPEND;
        // write output in UTF-8 to the specified file
        Files.write(file.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, overwrite);
    }

    @Override
    boolean validateGraph(Graph graph) {
        if(graph.getImages() != null) {
            for(Node node : graph.getNodes()) {
                if(node.hasEmulationSettings() && !graph.getImages().containsKey(node.getID())
                        && !graph.getCommands().containsKey(node.getID())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    void addHosts(Graph graph) {
        addBlankLine();
        lines.add("# add hosts");

        Map<Integer, String> images = graph.getImages();

        for (Node n : graph.getNodes().stream().filter(Node::hasEmulationSettings).collect(Collectors.toList())) {
            EmulationSettings emu = n.getEmulationNode();
            DockerType docker = emu.getDockerType();
            String image = images.get(n.getID());
            lines.add(n.getName() + " = topo.addHost(\"" + n.getName() + "\", cls=Docker, ip=\"" + emu.getIP() +
                    "\", dimage=\"" + image + "\", mem_limit=" + docker.memoryLimit + ")");
        }
    }

    /**
     * Writes the commands of each node to the python file.
     *
     * @param graph graph to export
     */
    private void addCommands(Graph graph) {
        addBlankLine();
        lines.add("# add commands");

        Map<Integer, List<String>> commandsMap = graph.getCommands();

        for(Integer key : commandsMap.keySet()) {
            List<String> commands = commandsMap.get(key);
            for(String command : commands) {
                command = parseCommand(command);
                lines.add("exp.get_node(\"" + key + "\").cmd(" + command + ")");
            }
        }
    }

    /**
     * Replaces targets in the given command with python function calls.
     *
     * @param command command to parse
     * @return command string with targets replaced
     */
    private String parseCommand(String command) {
        int currentIndex = -1;
        boolean lastFound = false;
        while(!lastFound) {
            int index = command.indexOf("EMUFOG_", currentIndex);
            if(index > currentIndex) {
                currentIndex = index;
                int targetIndex = command.indexOf('(', currentIndex);
                int targetEndIndex = command.indexOf(')', currentIndex);
                String method = command.substring(index + 7, targetIndex);
                String target = command.substring(targetIndex + 1, targetEndIndex);
                command = "\"" + command.substring(0, currentIndex) + "%s" + command.substring(targetEndIndex + 1)
                        + "\"" + " % (get" + method + "(" + target + "))";
            } else {
                lastFound = true;
            }
        }
        return command;
    }

    /**
     * Writes the helper functions from python helper file to exported maxinet file.
     *
     * @throws IOException  throws exception if file cannot be opened or read from.
     */
    private void setupPythonHelpers() throws IOException {
        addBlankLine();
        FileReader fileReader = new FileReader(PYTHON_HELPERS_FILEPATH);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;

        while((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
    }
}
