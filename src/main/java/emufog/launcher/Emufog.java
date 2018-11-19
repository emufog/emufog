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

import com.beust.jcommander.JCommander;
import emufog.backbone.BackboneClassifier;
import emufog.docker.FogType;
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
import emufog.settings.SettingsReader;
import emufog.util.Logger;
import emufog.util.LoggerLevel;
import emufog.util.Tuple;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * The EmuFog main launcher class. Starts a new instance of the application with the given parameters
 * by the command line interface.
 */
public class Emufog {

    /**
     * Main function call to start EmuFog.
     *
     * @param args arguments of the command line
     */
    public static void main(String[] args) {
        // logger to write to log file and command line
        Logger logger = Logger.getInstance();
        logger.logSeparator();
        logger.log("Welcome to EmuFog");
        logger.logSeparator();
        Arguments arguments = new Arguments();
        Graph graph;

        try {
            // parse the command line arguments
            JCommander.newBuilder().addObject(arguments).build().parse(args);

            // read in the settings file
            Settings settings = SettingsReader.read(arguments.settingsPath);

            // determines the respective format reader
            GraphReader reader = getReader(arguments.inputType, settings);

            // read in the graph with the graph reader
            long start = System.nanoTime();
            graph = reader.readGraph(arguments.files);
            long end = System.nanoTime();
            logger.log("Time to read in the graph: " + Logger.convertToMs(start, end));
            logger.logSeparator();
            // print graph details for information purposes
            logger.log("Number of nodes in the graph: " + graph.getRouters().size());
            logger.log("Number of edges in the graph: " + graph.getEdges().size());
            logger.logSeparator();

            // compute the backbone of the network
            start = System.nanoTime();
            BackboneClassifier.identifyBackbone(graph);
            end = System.nanoTime();
            logger.log("Time to determine the backbone of the topology: " + Logger.convertToMs(start, end));
            logger.logSeparator();
            logger.log("Number of backbone nodes identified: " + graph.getSwitches().size());
            logger.logSeparator();

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
                logger.log("Unable to find a fog placement with the provided settings.", LoggerLevel.ERROR);
                logger.log("Consider using different settings.", LoggerLevel.ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.log("An exception stopped EmuFog!", LoggerLevel.ERROR);
            logger.log("Error message: " + e.getMessage(), LoggerLevel.ERROR);
        } finally {
            logger.log("Closing EmuFog");
        }
    }

    /**
     * Returns the reader matching the given type from the command line.
     *
     * @param type     topology type to read in
     * @param settings settings object to use for the reader
     * @return graph reader matching the type or null if not found
     */
    private static GraphReader getReader(String type, Settings settings) {
        GraphReader reader = null;

        switch (type.toLowerCase()) {
            case "brite":
                reader = new BriteFormatReader(settings);
                break;
            case "caida":
                reader = new CaidaFormatReader(settings);
                break;
        }

        return reader;
    }
}
