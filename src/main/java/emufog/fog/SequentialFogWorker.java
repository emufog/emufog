/*
package emufog.fog;

import emufog.graph.AS;
import emufog.graph.Node;

import java.util.Collection;

*/
/**
 * This worker processes routers sequentially.
 *//*

class SequentialFogWorker extends Worker {

    */
/**
     * Creates a new sequential worker to identify fog nodeconfig in the given AS.
     *
     * @param as         AS to cover by this worker
     * @param classifier master classifier synchronizing the remaining nodeconfig to place
     *//*

    SequentialFogWorker(AS as, FogNodeClassifier classifier) {
        super(as, classifier);
    }

    @Override
    void iterateNodes(FogGraph g, Collection<Node> startingNodes, float t) {
        for (Node n : startingNodes) {
            processNode(g, n, t);
        }
    }
}
*/
