package emufog.fog;

import emufog.graph.AS;
import emufog.graph.Router;

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
    void iterateRouters(FogGraph g, Collection<Router> routers, float t) {
        routers.parallelStream().forEach(r -> processRouter(g, r, t));
    }
}