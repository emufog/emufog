package graph.emufog;

/**
 * A node converter simplifies the conversion of a node to a different type.
 */
public abstract class NodeConverter {

    /**
     * Creates a new node based on the given old node.
     * The type of the new node is based on the subclass.
     *
     * @param node node to create a new node from
     * @return the newly created node
     */
    protected abstract Node createNewNode(Node node);

    /**
     * Adds the new node to the respective list in the graph.
     *
     * @param newNode the new node to add
     */
    protected abstract void addNodeToGraph(Node newNode);

    /**
     * Checks if the given node needs to be converted by the specific converter.
     *
     * @param node node to check
     * @return true if the given node needs to be converted
     */
    protected abstract boolean needsConversion(Node node);

    /**
     * Removes the given node from the graph.
     *
     * @param node node to remove
     */
    private void removeOldNode(Node node) {
        node.getAS().removeNode(node);
    }

    /**
     * Converts the given node to a different type and replace it in the associated graph.
     * If the node is already an instance of the requested class the method just returns this object.
     *
     * @param node node to convert
     * @return the replacing node or null if the given node is null
     */
    public Node convert(Node node) {
        if (node == null) {
            return null;
        }
        if (!needsConversion(node)) {
            return node;
        }

        Edge[] edges = node.edges;

        // remove the old node from the graph
        removeOldNode(node);

        Node newNode = createNewNode(node);

        // update edges associated with the node to convert
        for (Edge e : edges) {
            assert e.from.equals(node) || e.to.equals(node) : "node is not connected by this edge";

            if (e.from.equals(node)) {
                e.from = newNode;
            } else {
                e.to = newNode;
            }
        }

        // set the updated edges
        newNode.edges = edges;

        // add the new node to the graph
        addNodeToGraph(newNode);

        return newNode;
    }
}
