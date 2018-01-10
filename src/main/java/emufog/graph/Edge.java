package emufog.graph;

/**
 * This class represents an edge in the network graph. The connection between the two nodeconfig is bidirectional.
 * Latency and bandwidth apply to both directions.
 */
public class Edge {

    /* unique identifier of the edge object */
    final int id;

    /* latency delay on this edge in ms */
    private final float delay;

    /* bandwidth of the connection on Mbit/s */
    private final float bandwidth;

    /* one end of the connection */
    Node from;

    /* the other end of the connection */
    Node to;

    /**
     * Creates a new edge instance connecting two ndoes of the graph.
     * The connection is based on the delay and the bandwidth given.
     *
     * @param id        unique identifier
     * @param from      starting node
     * @param to        ending node
     * @param delay     latency delay in ms
     * @param bandwidth bandwidth in Mbit/s
     */
    Edge(int id, Node from, Node to, float delay, float bandwidth) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.delay = delay;
        this.bandwidth = bandwidth;

        from.addEdge(this);
        to.addEdge(this);
    }

    /**
     * Returns the bandwidth limitation of the connection in Mbit/s.
     *
     * @return maximal bandwidth
     */
    public float getBandwidth() {
        return bandwidth;
    }

    /**
     * Returns the latency delay of the connection in ms.
     *
     * @return latency of the connection
     */
    public float getDelay() {
        return delay;
    }

    /**
     * Returns the other end of the connection for the given node.
     * In case the node is not part of the connection the method returns null.
     *
     * @param node node to find the partner for
     * @return the other end of the connection
     */
    public Node getDestinationForSource(Node node) {
        Node result = null;

        if (from.equals(node)) {
            result = to;
        }
        if (to.equals(node)) {
            result = from;
        }

        return result;
    }

    /**
     * Indicates whether this edge connects two different ASs or not.
     *
     * @return true if edge is connecting different ASs, false otherwise
     */
    public boolean isCrossASEdge() {
        return !from.as.equals(to.as);
    }

    /**
     * Returns the source/first node of the connection.
     *
     * @return source/first node
     */
    public Node getSource() {
        return from;
    }

    /**
     * Returns the destination/second node of the connection.
     *
     * @return destination/second node
     */
    public Node getDestination() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;

        if (o instanceof Edge) {
            result = ((Edge) o).id == id;
        }

        return result;
    }
}
