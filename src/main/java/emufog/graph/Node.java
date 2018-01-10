package emufog.graph;

import java.util.Arrays;

/**
 * Represents a general node of graph with the basic functionality.
 * Can connect to other nodes via edges and contains the geographically placement on a plain.
 */
public abstract class Node {

    /* unique identifier of the node */
    private final int id;

    /* autonomous system this node belongs to */
    private final AS as;

    //TODO: Change list to set?

    /* list of edges associated with the node */
    Edge[] edges;

    /**
     * Creates a node of the graph with the initial parameter given.
     *
     * @param id unique identifier
     * @param as autonomous system the belongs to
     */
    Node(int id, AS as) {
        this.id = id;
        this.as = as;
        edges = new Edge[0];
        addToAS();
    }

    /**
     * Adds this node to the respective AS.
     */
    abstract void addToAS();

    /**
     * Returns the unique identifier of the node.
     *
     * @return unique identifier
     */
    public int getID() {
        return id;
    }

    /**
     * Returns an array of all edges associated with the node.
     *
     * @return array of the node's edges
     */
    public Edge[] getEdges() {
        return edges;
    }

    /**
     * Returns the name of the node.
     *
     * @return name of the node
     */
    public abstract String getName();

    /**
     * Returns the edge degree of the node.
     *
     * @return number of edges associated with the node
     */
    public int getDegree() {
        return edges.length;
    }

    /**
     * Returns the autonomous system consisting the node.
     *
     * @return node's AS
     */
    public AS getAS() {
        return as;
    }

	/**
     * Adds an edge to the array of edges associated with this node. Grows the array by one to add an edge.
     *
     * @param edge edge to add to node
     */
    void addEdge(Edge edge) {
        edges = Arrays.copyOf(edges, edges.length + 1);
        edges[edges.length - 1] = edge;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;

        if (o instanceof Node) {
            result = ((Node) o).id == id;
        }

        return result;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return getName();
    }
}
