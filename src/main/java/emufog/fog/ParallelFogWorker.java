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

import emufog.graph.AS;
import emufog.graph.Node;

import java.util.Collection;

/**
 * This worker processes routers in parallel.
 */
class ParallelFogWorker extends Worker {

    /**
     * Creates a new parallel worker to identify fog nodes in the given AS.
     *
     * @param as         AS to cover by this worker
     * @param classifier master classifier synchronizing the remaining nodes to place
     */
    ParallelFogWorker(AS as, FogNodeClassifier classifier) {
        super(as, classifier);
    }

    @Override
    void iterateNodes(FogGraph g, Collection<Node> startingNodes, float t) {
        startingNodes.parallelStream().forEach(n -> processNode(g, n, t));
    }
}