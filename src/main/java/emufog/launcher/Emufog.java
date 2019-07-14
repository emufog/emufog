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
package emufog.launcher;

import emufog.backbone.BackboneClassifier;
import emufog.container.FogType;
import emufog.export.IGraphExporter;
import emufog.export.MaxiNetExporter;
import emufog.fog.FogNodeClassifier;
import emufog.fog.FogResult;
import emufog.graph.Graph;
import emufog.graph.Node;
import emufog.reader.BriteFormatReader;
import emufog.reader.CaidaFormatReader;
import emufog.reader.GraphReader;
import emufog.settings.Settings;
import emufog.settings.YamlReader;
import emufog.util.Tuple;
import java.io.IOException;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        // parse the command line arguments
        Arguments arguments = new Arguments();
        try {
            //new CommandLine(arguments).parseArgs(args);
        } catch (Exception e) {
            LOG.error("Failed to read in command line arguments.", e);
            return;
        }

        Graph graph;
        try {
            // read in the settings file
            Settings settings = YamlReader.read(arguments.settingsPath);

            // determines the respective format reader
            GraphReader reader = getReader(arguments.inputType, settings);

            // read in the graph with the graph reader
            long start = System.nanoTime();
            graph = reader.readGraph(arguments.files);
            long end = System.nanoTime();
            LOG.info("Time to read in the graph: " + intervalToString(start, end));
            LOG.info("##############################################################");
            // print graph details for information purposes
            LOG.info("Number of nodes in the graph: " + graph.getRouters().size());
            LOG.info("Number of edges in the graph: " + graph.getEdges().size());
            LOG.info("##############################################################");

            // compute the backbone of the network
            start = System.nanoTime();
            BackboneClassifier.identifyBackbone(graph);
            end = System.nanoTime();
            LOG.info("Time to determine the backbone of the topology: " + intervalToString(start, end));
            LOG.info("##############################################################");
            LOG.info("Number of backbone nodes identified: " + graph.getSwitches().size());
            LOG.info("##############################################################");

            // assign devices to the edge
            graph.assignEdgeDevices();

            // find the fog node placements
            FogResult result = new FogNodeClassifier(settings).findFogNodes(graph);
            if (result.getStatus()) {
                for (Tuple<Node, FogType> tuple : result.getFogNodes()) {
                    graph.placeFogNode(tuple.getKey(), tuple.getValue());
                }

                IGraphExporter exporter = new MaxiNetExporter();
                exporter.exportGraph(graph, Paths.get(arguments.output));
            } else {
                // no fog placement found, aborting
                LOG.error("Unable to find a fog placement with the provided settings.");
                LOG.error("Consider using different settings.");
            }
        } catch (IOException e) {
            LOG.error("An exception stopped EmuFog!", e);
        }

        LOG.info("##############################################################");
        LOG.info("                     Closing EmuFog");
        LOG.info("##############################################################");
    }

    /**
     * Returns the reader matching the given type from the command line.
     *
     * @param type     topology type to read in
     * @param settings settings object to use for the reader
     * @return graph reader matching the type or null if not found
     */
    private static GraphReader getReader(String type, Settings settings) throws IllegalArgumentException {
        String s = type.trim().toLowerCase();
        switch (s) {
            case "brite":
                return new BriteFormatReader(settings);
            case "caida":
                return new CaidaFormatReader(settings);
            default:
                throw new IllegalArgumentException("Unsupported Input Format: " + s);
        }
    }
}
