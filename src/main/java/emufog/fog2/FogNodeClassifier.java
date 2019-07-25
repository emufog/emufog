package emufog.fog2;

import emufog.graph.AS;
import emufog.graph.Graph;
import emufog.settings.Settings;
import java.util.Collection;
import java.util.List;
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
        FogResult result = new FogResult();

        Collection<AS> systems = graph.getSystems();
        List<FogResult> results = systems.parallelStream().map(s -> new FogWorker(s, this).processAS()).collect(Collectors.toList());

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
