package graph.emufog;

/**
 * The router class represents a node of the graph host devices can connect to.
 */
public class Router extends Node {

    /**
     * Creates a new router instance.
     *
     * @param id unique identifier
     * @param as autonomous system the belongs to
     */
    Router(int id, AS as) {
        super(id, as);
    }

    @Override
    void addToAS() {
        as.addRouter(this);
    }

    public boolean hasDevices() {
        boolean result = false;

        for (int i = 0; i < edges.length && !result; ++i) {
            Node neighbor = edges[i].getDestinationForSource(this);
            result = neighbor instanceof HostDevice;
        }

        return result;
    }

    @Override
    public String getName() {
        return "router" + id;
    }
}
