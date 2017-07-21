package emufog.fog;

import emufog.graph.AS;
import emufog.graph.Router;

import java.util.Collection;

/**
 * This worker processes routers sequentially.
 */
class SequentialFogWorker extends Worker {

    /**
     * Creates a new sequential worker to identify fog nodes in the given AS.
     *
     * @param as         AS to cover by this worker
     * @param classifier master classifier synchronizing the remaining nodes to place
     */
    SequentialFogWorker(AS as, FogNodeClassifier classifier) {
        super(as, classifier);
    }

    @Override
    void iterateRouters(FogGraph g, Collection<Router> routers, float t) {
        for (Router r : routers) {
            processRouter(g, r, t);
        }
    }
}
