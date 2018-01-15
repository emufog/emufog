/*
package emufog.fog;

import emufog.graph.AS;
import emufog.graph.Node;

import java.util.Collection;

*/
/**
 * This worker processes routers in parallel.
 *//*

class ParallelFogWorker extends Worker {

    */
/**
     * Creates a new parallel worker to identify fog nodeconfig in the given AS.
     *
     * @param as         AS to cover by this worker
     * @param classifier master classifier synchronizing the remaining nodeconfig to place
     *//*

    ParallelFogWorker(AS as, FogNodeClassifier classifier) {
        super(as, classifier);
    }

    @Override
    void iterateNodes(FogGraph g, Collection<Node> startingNodes, float t) {
        startingNodes.parallelStream().forEach(n -> processNode(g, n, t));
    }
}*/
