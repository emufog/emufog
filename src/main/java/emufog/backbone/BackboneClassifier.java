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
import emufog.util.Tuple;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static emufog.util.ConversionsUtils.intervalToString;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * This class runs the backbone classification algorithm on a graph instance.
 */
public class BackboneClassifier {

    private static final Logger LOG = LoggerFactory.getLogger(BackboneClassifier.class);

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

        // 1st step sequentially
        LOG.debug("Start Backbone Classification");
        long start = System.nanoTime();
        markASEdgeNodes(graph);
        long stop = System.nanoTime();
        LOG.debug("Graph Step 1 - Time: " + intervalToString(start, stop));
        LOG.info("Backbone Size: " + graph.getSwitches().size());
        LOG.info("Edge Size: " + graph.getRouters().size());

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
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                LOG.error("Backbone Thread for " + t.getKey() + " was interrupted.");
                LOG.error("Error message: " + e.getMessage());
            }
        }
        pool.shutdownNow();
        LOG.info("Finished Backbone Classification.");

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