package emufog.fog;

import emufog.docker.FogType;
import emufog.graph.Node;

import java.util.*;

/**
 * This node represents a node of the sub graph used to identify fog nodes.
 * The original node of the underlying graph identifies this node.
 */
abstract class FogNode {

    /* the original node of the underlying graph */
    final Node oldNode;

    /* the fog sub graph this node is part of */
    private final FogGraph graph;

    /* mapping the connected edge node to a tuple of <predecessor, costs> */
    final Map<EdgeNode, Connection> connectedNodes;

    /* number of nodes covered with the current associated type */
    private int coveredCount;

    /* list of edge nodes that are covered by this node */
    private List<EdgeNode> coveredNodes;

    /* average connection costs for  */
    private float averageConnectionCosts;

    /* the optimal Docker type to use */
    private FogType type;
    
    //private String image;

	/* identifier whether the fog node has changed since the last fog type calculation */
    private boolean modified;

    /**
     * Creates a new node for the sub graph based on the underlying graph.
     *
     * @param graph   sub graph instance this node belongs to
     * @param oldNode the node of the underlying graph
     */
    FogNode(FogGraph graph, Node oldNode) {
        this.graph = graph;
        this.oldNode = oldNode;
        connectedNodes = new HashMap<>();
        modified = true;
    }

    /**
     * Updates the predecessor and costs for a connection starting by the giving start edge node.
     *
     * @param start edge node this connection started
     * @param pre   predecessor
     * @param costs associated costs to set
     */
    void updatePredecessor(EdgeNode start, FogNode pre, float costs) {
        Connection connection = connectedNodes.get(start);
        connection.predecessor = pre;
        connection.costs = costs;
    }

    /**
     * Initializes a connection between a start node and the fog node.
     * Starting from the given node with the predecessor and the current costs.
     *
     * @param start edge node this connection started
     * @param pre   predecessor
     * @param costs associated costs to set
     */
    void initPredecessor(EdgeNode start, FogNode pre, float costs) {
        assert !connectedNodes.containsKey(start) : "node already known";
        connectedNodes.put(start, new Connection(start, pre, costs));
        start.addPossibleNode(this);
    }

    /**
     * Removes the given edge node from the connected nodes.
     * Deletes the node from the graph if there are no connected nodes left.
     *
     * @param start covered edge node
     */
    void removeEdgeNode(EdgeNode start) {
        assert connectedNodes.containsKey(start) : "node to remove was not found in map";

        connectedNodes.remove(start);
        //connectionCount -= start.getDeviceCount();
        setModified(true);

        if (connectedNodes.isEmpty()) {
            graph.removeNode(this);
        }
    }

    /**
     * Removes all edge node connected to this fog node.
     */
    void clearAllEdgeNodes() {
        connectedNodes.clear();
        setModified(true);
    }

    /**
     * Returns the currently chosen fog type for this fog node.
     *
     * @return fog type object currently chosen
     */
    FogType getFogType() {
        return type;
    }

    /**
     * Returns a collection of all connected edge nodes.
     *
     * @return collection of edge nodes
     */
    Collection<EdgeNode> getConnectedEdgeNodes() {
        return connectedNodes.keySet();
    }

    /**
     * Returns the list of covered nodes if this node is picked as a placement.
     * Result is sorted based on their connection costs.
     *
     * @return list of covered edge nodes
     */
    List<EdgeNode> getCoveredEdgeNodes() {
        if (coveredNodes == null) {
            List<Connection> connections = new ArrayList<>(connectedNodes.values());
            connections.sort(new ConnectionComparator());

            coveredNodes = new ArrayList<>();
            int remaining = coveredCount;
            for (int i = 0; i < connections.size() && remaining > 0; ++i) {
                Connection connection = connections.get(i);

                if (connection.edge.getDeviceCount() <= remaining) {
                    coveredNodes.add(connection.edge);
                    remaining -= connection.edge.getDeviceCount();
                }
            }

            //TODO debug
            if (this instanceof EdgeNode) {
                assert coveredNodes.contains((EdgeNode) this) : "edge node is not part of its own covered nodes set";
            }
        }

        return coveredNodes;
    }

    /**
     * Returns the costs associated with the given start node.
     * In case there are none associated yet the method returns Float.MAX_VALUE.
     *
     * @param start edge node connected to this node
     * @return the associated costs or Float.MAX_VALUE if not set
     */
    float getCosts(EdgeNode start) {
        Connection connection = connectedNodes.get(start);

        return connection == null ? Float.MAX_VALUE : connection.costs;
    }

    /**
     * Updates the modified field with the given value.
     *
     * @param modified true if it has been modified since last check, false otherwise
     */
    void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Returns the average connection costs for all edge nodes connected to this node.
     *
     * @return average connection costs
     */
    float getAverageConnectionCosts() {
        return averageConnectionCosts;
    }

    /**
     * Calculates the optimal Docker image type to use and the average connection costs to use the function
     * getAverageConnectionCosts().
     */
    private void calculateAverageCosts() {
        float sum = 0.f;
        for (Connection c : connectedNodes.values()) {
            sum += c.costs;
        }

        averageConnectionCosts = sum / connectedNodes.size();
    }

    /**
     * Seeks and sets the optimal fog type available for this fog node.
     */
    void findFogType() {
        if (modified) {
            type = null;
            coveredCount = 0;

            float costsPerConnection = Float.MAX_VALUE;
            int deviceCount = 0;
            for (EdgeNode n : connectedNodes.keySet()) {
                deviceCount += n.getDeviceCount();
            }

            for (FogType fogType : graph.servers) {
                int connections = deviceCount;

                if (connections > fogType.maxClients) {
                    connections = fogType.maxClients;
                }

                if ((fogType.costs / connections) < costsPerConnection) {
                    type = fogType;
                    coveredCount = connections;
                    costsPerConnection = type.costs / connections;
                }
            }

            assert type != null : "no fog type set";

            calculateAverageCosts();
            setModified(false);
        }
    }

    /**
     * Returns the average deployment costs for all edge nodes connected to this node.
     *
     * @return average deployment costs
     */
    float getAverageDeploymentCosts() {
        assert type != null : "no type to use";
        return type.costs / coveredCount;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;

        if (o instanceof FogNode) {
            result = oldNode.equals(((FogNode) o).oldNode);
        }

        return result;
    }

    @Override
    public int hashCode() {
        return oldNode.hashCode();
    }
    
    public String getImage() {
		return type.dockerImage;
	}

	public void setImage(String image) {
		this.type.dockerImage = image;
	}

    /**
     * This class represents a connection from the edge to the fog node instance.
     */
    class Connection {

        /* edge node to start the connection */
        final EdgeNode edge;

        /* predecessor node */
        FogNode predecessor;

        /* current cost function value */
        float costs;

        /**
         * Creates a connection class for a connection from the given edge node
         * and the initial predecessor and costs.
         *
         * @param edge        starting edge node
         * @param predecessor current predecessor
         * @param costs       current costs
         */
        Connection(EdgeNode edge, FogNode predecessor, float costs) {
            this.edge = edge;
            this.predecessor = predecessor;
            this.costs = costs;
        }
    }

    /**
     * Compares connections based on their costs in an ascending order.
     */
    class ConnectionComparator implements Comparator<Connection> {

        @Override
        public int compare(Connection o1, Connection o2) {
            if (o1.costs < o2.costs) {
                return -1;
            }
            if (o2.costs < o1.costs) {
                return 1;
            }

            return 0;
        }
    }
}
