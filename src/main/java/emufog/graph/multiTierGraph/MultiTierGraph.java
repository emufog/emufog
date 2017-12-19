package emufog.graph.multiTierGraph;

import emufog.graph.*;
import emufog.settings.Settings;
import emufog.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiTierGraph {

    private final List<Edge> edges;
    private final Map<Integer, AS> systems;
    private final Map<Integer, Integer> nodes;

    private final Settings settings;

    private final UniqueIPProvider ipProvider;
    private final UniqueIDProvider nodeIdProvider;
    private final UniqueIDProvider edgeIdProvider;

    protected final Logger logger;

    public MultiTierGraph(Settings settings) {


        this.settings = settings;

        edges = new ArrayList<Edge>();
        systems = new HashMap<Integer, AS>();
        nodes = new HashMap<Integer, Integer>();
        ipProvider = new UniqueIPProvider(settings);
        nodeIdProvider = new UniqueIDProvider();
        edgeIdProvider = new UniqueIDProvider();
        logger = Logger.getInstance();

    }

    /**
     * Returns the autonomous system instance mapped to the given id or create the new AS initially.
     *
     * @param id id of the AS to seek
     * @return the AS associated or a new instance
     */
    private AS getAS(int id) {
        return systems.computeIfAbsent(id, AS::new);
    }

    /**
     * Check the node's ID if it's already in use.
     * Throws an exception if the ID is used before.
     *
     * @param id ID to check
     * @throws IllegalArgumentException if the ID is already in use
     */
    private void checkNodeID(int id) throws IllegalArgumentException {
        if (nodeIdProvider.isUsed(id)) {
            throw new IllegalArgumentException("The ID: " + id + " is already in use.");
        }
    }

    /**
     * Creates a new router in the graph
     *
     * @param id unique identifier
     * @param as autonomous system the belongs to
     * @return the newly created router
     * @throws IllegalArgumentException throws an exception if the ID is already in use
     */
    public Router createRouter(int id, int as) throws IllegalArgumentException {
        checkNodeID(id);

        Router router = new Router(id, getAS(as));
        nodeIdProvider.markIDused(id);
        nodes.put(id, as);

        return router;
    }

    /**
     * Creates a new switch in the graph.
     *
     * @param id unique identifier
     * @param as autonomous system the belongs to
     * @return the newly created switch
     * @throws IllegalArgumentException throws an exception if the ID is already in use
     */
    public Switch createSwitch(int id, int as) throws IllegalArgumentException {
        checkNodeID(id);

        Switch s = new Switch(id, getAS(as));
        nodeIdProvider.markIDused(id);
        nodes.put(id, as);

        return s;
    }

}



