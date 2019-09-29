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
import emufog.export.IGraphExporter;
import emufog.export.MaxiNetExporter;
import emufog.fog.FogNodeClassifier;
import emufog.fog.FogNodePlacement;
import emufog.fog.FogResult;
import emufog.graph.Graph;
import emufog.reader.BriteFormatReader;
import emufog.reader.CaidaFormatReader;
import emufog.reader.GraphReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import static emufog.util.ConversionsUtils.intervalToString;

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
    private static void runEmuFog(String[] args) throws IOException {
        // parse the command line arguments
        final Arguments arguments = new Arguments();
        try {
            new CommandLine(arguments).parseArgs(args);
        } catch (Exception e) {
            LOG.error("Failed to read in command line arguments.", e);
            return;
        }

        // read in the config file
        Config config;
        try {
            config = Config.updateConfig(arguments.configPath);
        } catch (Exception e) {
            LOG.error("Failed to read in the configuration file: {}", arguments.configPath, e);
            return;
        }

        // determines the respective format reader
        GraphReader reader = getReader(arguments.inputType, config);

        // read in the graph with the graph reader
        long start = System.nanoTime();
        Graph graph = reader.readGraph(arguments.files);
        long end = System.nanoTime();
        LOG.debug("Time to read in the graph: {}", intervalToString(start, end));
        LOG.info("##############################################################");
        // print graph details for information purposes
        LOG.info("Number of nodes in the graph: {}", graph.getEdgeNodes().size());
        LOG.info("Number of edges in the graph: {}", graph.getEdges().size());
        LOG.info("##############################################################");

        // compute the backbone of the network
        start = System.nanoTime();
        BackboneClassifier.identifyBackbone(graph);
        end = System.nanoTime();
        LOG.debug("Time to determine the backbone of the topology: {}", intervalToString(start, end));
        LOG.info("##############################################################");
        LOG.info("Number of backbone nodes identified: {}", graph.getBackboneNodes().size());
        LOG.info("##############################################################");

        // assign devices to the edge
        graph.assignEdgeDevices();

        // find the fog node placements
        FogResult result = new FogNodeClassifier(graph).placeFogNodes();
        if (result.getStatus()) {
            for (FogNodePlacement placement : result.getPlacements()) {
                graph.placeFogNode(placement.getNode(), placement.getType());
            }

            IGraphExporter exporter = new MaxiNetExporter();
            exporter.exportGraph(graph, arguments.output);
        } else {
            // no fog placement found, aborting
            LOG.warn("Unable to find a fog placement with the provided config.");
            LOG.warn("Consider using different config.");
        }
    }

    /**
     * Returns the reader matching the given type from the command line.
     *
     * @param type   topology type to read in
     * @param config config object to use for the reader
     * @return graph reader matching the type or null if not found
     */
    private static GraphReader getReader(String type, Config config) throws IllegalArgumentException {
        String s = type.trim().toLowerCase();
        switch (s) {
            case "brite":
                return new BriteFormatReader(config);
            case "caida":
                return new CaidaFormatReader(config);
            default:
                throw new IllegalArgumentException("Unsupported Input Format: " + s);
        }
    }
}
