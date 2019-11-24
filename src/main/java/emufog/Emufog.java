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
package emufog;

import emufog.backbone.BackboneClassifier;
import emufog.config.Config;
import emufog.export.GraphExporter;
import emufog.export.MaxiNetExporter;
import emufog.fog.FogNodeClassifier;
import emufog.fog.FogNodePlacement;
import emufog.fog.FogResult;
import emufog.graph.Graph;
import emufog.reader.GraphReader;
import emufog.reader.brite.BriteFormatReader;
import emufog.reader.caida.CaidaFormatReader;
import java.io.IOException;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import static emufog.util.ConversionsUtils.formatTimeInterval;
import static emufog.util.StringUtils.nullOrEmpty;

/**
 * The EmuFog main launcher class. Starts a new instance of the application with the given parameters
 * by the command line interface.
 */
public class Emufog {

    private static final Logger LOG = LoggerFactory.getLogger(Emufog.class);

    /**
     * Main function call to start EmuFog.
     *
     * @param args arguments of the command line
     */
    public static void main(String[] args) {
        LOG.info("##############################################################");
        LOG.info("                   Welcome to EmuFog");
        LOG.info("##############################################################");

        try {
            runEmuFog(args);
        } catch (Exception e) {
            LOG.error("An exception stopped EmuFog!", e);
        }

        LOG.info("##############################################################");
        LOG.info("                     Closing EmuFog");
        LOG.info("##############################################################");
    }

    /**
     * Runs an execution of emufog with the given arguments from the command line.
     *
     * @param args arguments passed to the execution
     * @throws IOException thrown in case of a problem during the execution
     */
    private static void runEmuFog(String... args) throws IOException {
        // parse the command line arguments
        Arguments arguments = new Arguments();
        try {
            new CommandLine(arguments).parseArgs(args);
        } catch (Exception e) {
            LOG.error("Failed to read in command line arguments.", e);
            return;
        }

        // check the read in arguments
        if (!checkArguments(arguments)) {
            LOG.error("The arguments provided are invalid.");
            LOG.error("Please see https://github.com/emufog/emufog/wiki for further information.");
            return;
        }

        // read in the config file
        try {
            Config.Companion.updateConfig(arguments.configPath);
        } catch (Exception e) {
            LOG.error("Failed to read in the configuration file: {}", arguments.configPath, e);
            return;
        }

        // determines the respective format reader
        GraphReader reader = getReader(arguments.inputType);

        // read in the graph with the graph reader
        long start = System.nanoTime();
        Graph graph = reader.readGraph(arguments.files);
        long end = System.nanoTime();
        LOG.debug("Time to read in the graph: {}", formatTimeInterval(start, end));
        LOG.info("##############################################################");
        // print graph details for information purposes
        LOG.info("Number of nodes in the graph: {}", graph.getEdgeNodes().size());
        LOG.info("Number of edges in the graph: {}", graph.getEdges().size());
        LOG.info("##############################################################");

        // compute the backbone of the network
        start = System.nanoTime();
        BackboneClassifier.identifyBackbone(graph);
        end = System.nanoTime();
        LOG.debug("Time to determine the backbone of the topology: {}", formatTimeInterval(start, end));
        LOG.info("##############################################################");
        LOG.info("Number of backbone nodes identified: {}", graph.getBackboneNodes().size());
        LOG.info("##############################################################");

        // assign devices to the edge
        graph.assignEdgeDevices();

        // find the fog node placements
        FogResult result = new FogNodeClassifier(graph).findPossibleFogNodes();
        if (result.getStatus()) {
            for (FogNodePlacement placement : result.getPlacements()) {
                graph.placeFogNode(placement.getNode(), placement.getType());
            }

            GraphExporter exporter = MaxiNetExporter.INSTANCE;
            exporter.exportGraph(graph, arguments.output);
        } else {
            // no fog placement found, aborting
            LOG.warn("Unable to find a fog placement with the provided config.");
            LOG.warn("Consider using different config.");
        }
    }

    /**
     * Checks the read in arguments from the command line. Either sets a default or prints an
     * error if no argument is specified.
     *
     * @param arguments arguments to check
     * @return {@code true} if arguments are valid to start, {@code false} if arguments are invalid
     */
    private static boolean checkArguments(Arguments arguments) {
        boolean valid = true;
        if (arguments.configPath == null) {
            arguments.configPath = Paths.get("src", "main", "resources", "application.yaml");
            LOG.warn("No '--config' argument found. Will use {} as default.", arguments.configPath);
        }
        if (nullOrEmpty(arguments.inputType)) {
            valid = false;
            LOG.error("No '--type' argument found. Specify a valid input format.");
        }
        if (arguments.output == null) {
            arguments.output = Paths.get("output.py");
            LOG.warn("No '--output' argument found. Will use {} as default.", arguments.output);
        }
        if (arguments.files == null || arguments.files.isEmpty()) {
            valid = false;
            LOG.error("No '--file' argument found. Provide at least one input file.");
        }

        return valid;
    }

    /**
     * Returns the reader matching the given type from the command line.
     *
     * @param type topology type to read in
     * @return graph reader matching the type
     * @throws IllegalArgumentException thrown if the type is unsupported
     */
    private static GraphReader getReader(String type) throws IllegalArgumentException {
        String s = type.trim().toLowerCase();
        switch (s) {
            case "brite":
                return BriteFormatReader.INSTANCE;
            case "caida":
                return CaidaFormatReader.INSTANCE;
            default:
                throw new IllegalArgumentException("Unsupported Input Format: " + s);
        }
    }
}
