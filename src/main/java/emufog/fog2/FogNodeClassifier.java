/*
 * MIT License
 *
 * Copyright (c) 2019 emufog contributors
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
package emufog.fog2;

import emufog.graph.Graph;
import emufog.settings.Settings;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FogNodeClassifier {

    private final Graph graph;

    final Settings settings;

    private final AtomicInteger counter;

    public FogNodeClassifier(final Graph graph) throws IllegalArgumentException {
        if (graph == null) {
            throw new IllegalArgumentException("The graph is null.");
        }

        this.graph = graph;
        settings = graph.getSettings();
        counter = new AtomicInteger(settings.maxFogNodes);
    }

    public FogResult placeFogNodes() {
        // init empty failed result
        FogResult result = new FogResult();
        result.setFailure();

        // process all systems in parallel
        List<FogResult> results = graph.getSystems().parallelStream().map(s -> new FogWorker(s, this).processAS()).collect(Collectors.toList());

        // check if all part results are success
        Optional<FogResult> optional = results.stream().filter(r -> !r.getStatus()).findFirst();
        if (!optional.isPresent()) {
            result.setSuccess();
            results.forEach(r -> result.addPlacements(r.getPlacements()));
        }

        return result;
    }

    /**
     * Indicates if there are still fog nodes to place available.
     *
     * @return {@code true} if there are, {@code false} if 0
     */
    boolean fogNodesLeft() {
        return counter.get() > 0;
    }

    /**
     * Decrements the remaining fog node to place by 1.
     */
    void reduceRemainingNodes() {
        counter.decrementAndGet();
    }
}
