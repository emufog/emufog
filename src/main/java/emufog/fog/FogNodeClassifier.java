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
package emufog.fog;

import emufog.container.FogType;
import emufog.graph.AS;
import emufog.graph.Graph;
import emufog.settings.Settings;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This classifier identifies a list of possible fog node placements in the given graph.
 */
public class FogNodeClassifier {

    private static final Logger LOG = LoggerFactory.getLogger(FogNodeClassifier.class);

    /* list of all available types of fog nodes */
    final List<FogType> fogTypes;

    /* remaining number of nodes to place in the graph */
    private final AtomicInteger remainingNodes;

    /* threshold for the cost function to limit the search */
    final float threshold;

    /**
     * Creates a new FogNodeClassifier using the given settings.
     *
     * @param settings settings to use for classification
     * @throws IllegalArgumentException throws exception if the settings object is {@code null}
     */
    public FogNodeClassifier(Settings settings) throws IllegalArgumentException {
        if (settings == null) {
            throw new IllegalArgumentException("No settings object given.");
        }

        fogTypes = settings.fogNodeTypes;
        remainingNodes = new AtomicInteger(settings.maxFogNodes);
        threshold = settings.costThreshold;
    }

    /**
     * This method identifies the fog nodes in the given graph. The settings passed to
     * the constructor are used for the algorithm. The result might not be optimal as the
     * method uses a greedy algorithm to approximate the optimal solution.
     *
     * @param graph graph to find fog nodes in
     * @return result object with the list of fog nodes or failure state
     * @throws IllegalArgumentException throws exception if the graph parameter is {@code null}
     */
    public FogResult findFogNodes(Graph graph) throws IllegalArgumentException {
        if (graph == null) {
            throw new IllegalArgumentException("The graph object is not initialized.");
        }

        // init result object
        FogResult result = new FogResult();

        Collection<AS> systems = graph.getSystems();
        List<FogResult> results = systems.parallelStream().map(as -> getWorker(as, graph.getSettings())).map(FogNodeClassifier::executeWorker).collect(Collectors.toList());

        Optional<FogResult> optional = results.parallelStream().filter(r -> !r.getStatus()).findFirst();
        if (!optional.isPresent()) {
            result.setSuccess(true);
            result.addAll(results.parallelStream().map(FogResult::getFogNodes).flatMap(List::stream).collect(Collectors.toList()));
        }

        return result;
    }

    private static FogResult executeWorker(Worker worker) {
        try {
            return worker.call();
        } catch (Exception e) {
            LOG.error("Fog placement failed for {}", worker.getAs(), e);
            return new FogResult();
        }
    }

    /**
     * Returns a new worker class depending on the settings given.
     *
     * @param as       as to work on
     * @param settings settings to determine parallel or sequential
     * @return worker class according to settings
     */
    private Worker getWorker(AS as, Settings settings) {
        if (settings.fogGraphParallel) {
            return new ParallelFogWorker(as, this);
        } else {
            return new SequentialFogWorker(as, this);
        }
    }

    /**
     * Indicates if there are still fog nodes to place available.
     *
     * @return {@code true} if there are, {@code false} if 0
     */
    boolean fogNodesLeft() {
        return remainingNodes.get() > 0;
    }

    /**
     * Decrements the remaining fog node to place by 1.
     */
    void reduceRemainingNodes() {
        remainingNodes.decrementAndGet();
    }
}
