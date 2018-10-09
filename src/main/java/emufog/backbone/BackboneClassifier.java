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
package emufog.backbone;

import emufog.graph.AS;
import emufog.graph.Edge;
import emufog.graph.Graph;
import emufog.graph.SwitchConverter;
import emufog.util.Logger;
import emufog.util.LoggerLevel;
import emufog.util.Tuple;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * This class runs the backbone classification algorithm on a graph instance.
 */
public class BackboneClassifier {

    /**
     * Starts the backbone classification algorithm on the given graph.
     * Returns the graph including backbone and edge of the network.
     *
     * @return the modified graph
     */
    public static Graph identifyBackbone(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("The graph object is not initialized.");
        }

        Logger logger = Logger.getInstance();

        // 1st step sequentially
        logger.log("Start Backbone Classification", LoggerLevel.ADVANCED);
        long start = System.nanoTime();
        markASEdgeNodes(graph);
        long stop = System.nanoTime();
        logger.log("Graph Step 1 - Time: " + Logger.convertToMs(start, stop), LoggerLevel.ADVANCED);
        logger.log("Backbone Size: " + graph.getSwitches().size(), LoggerLevel.ADVANCED);
        logger.log("Edge Size: " + graph.getRouters().size(), LoggerLevel.ADVANCED);

        // rest in parallel
        Collection<AS> ASs = graph.getSystems();
        Tuple<AS, Future<?>>[] workers = new Tuple[ASs.size()];

        ExecutorService pool = newFixedThreadPool(graph.getSettings().threadCount);
        int count = 0;
        for (AS as : ASs) {
            BackboneWorker worker = new BackboneWorker(as);
            workers[count] = new Tuple<>(as, pool.submit(worker));
            count++;
        }

        for (Tuple<AS, Future<?>> t : workers) {
            try {
                t.getValue().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                logger.log("Backbone Thread for " + t.getKey() + " was interrupted.", LoggerLevel.ERROR);
                logger.log("Error message: " + e.getMessage(), LoggerLevel.ERROR);
            }
        }
        pool.shutdownNow();
        logger.log("Finished Backbone Classification.", LoggerLevel.ADVANCED);
        logger.logSeparator(LoggerLevel.ADVANCED);

        return graph;
    }

    /**
     * This methods marks all cross-AS edge's endpoints as backbone nodes.
     */
    private static void markASEdgeNodes(Graph graph) {
        SwitchConverter converter = new SwitchConverter();

        for (Edge e : graph.getEdges()) {
            if (e.isCrossASEdge()) {
                converter.convert(e.getSource());
                converter.convert(e.getDestination());
            }
        }
    }
}