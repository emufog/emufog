package emufog.fog;

import emufog.docker.FogType;
import emufog.graph.AS;
import emufog.graph.Node;
import emufog.graph.Router;
import emufog.graph.Switch;
import emufog.util.Logger;
import emufog.util.LoggerLevel;
import emufog.util.Tuple;

import java.util.*;

/**
 * The graph represents a sub graph for the fog placement algorithm. It maps the nodes
 * of the underlying graph to the fog nodes.
 */
class FogGraph {

    /* list of possible Docker types for fog nodes */
    final List<FogType> servers;

    /* list of all edge nodes still to cover */
    private final List<EdgeNode> edgeNodes;

    /* mapping of nodes of the underlying graph to their respective fog nodes equivalent */
    private final Map<Node, FogNode> nodeMapping;

    /* fog comparator to sort the possible fog nodes optimal */
    private final Comparator<FogNode> comparator;

    /**
     * Creates a new sub graph with the given list of possible Docker images for fog nodes.
     *
     * @param servers list of Docker images
     */
    FogGraph(List<FogType> servers) {
        this.servers = servers;
        edgeNodes = new ArrayList<>();
        nodeMapping = new HashMap<>();
        comparator = new FogComparator();
    }

    /**
     * Returns the node of the sub graph for the given node. In case there is none so far
     * a new SwitchNode will be created and returned instead.
     *
     * @param node node of the original graph
     * @return sub graph equivalent
     */
    FogNode getNode(Node node) {
        return nodeMapping.get(node);
    }

    /**
     * Initializes the node equivalents of the given AS and adds them to the mapping.
     *
     * @param startNodes list of nodes with their respective edge nodes
     * @param as         AS instance to work on
     */
    void initNodes(List<Tuple<Node, List<EdgeNode>>> startNodes, AS as) {
        for (Router r : as.getRouters()) {
            nodeMapping.put(r, new SwitchNode(this, r));
        }

        for (Switch s : as.getSwitches()) {
            nodeMapping.put(s, new SwitchNode(this, s));
        }

        for (Tuple<Node, List<EdgeNode>> t : startNodes) {
            EdgeNode edgeNode;
            if (t.getKey() instanceof Router && t.getValue() == null) {
                edgeNode = new EdgeNode(this, (Router) t.getKey());
            } else {
                edgeNode = new EdgeNode(this, t.getKey(), t.getValue());
            }
            nodeMapping.put(t.getKey(), edgeNode);
            edgeNodes.add(edgeNode);
        }
    }

    /**
     * Removes the given node from the graph.
     *
     * @param node node to remove
     */
    void removeNode(FogNode node) {
        nodeMapping.remove(node.oldNode);
    }

    /**
     * Removes all unused nodes from the graph.
     */
    void trimNodes() {
        for (FogNode node : new ArrayList<>(nodeMapping.values())) {
            if (node.getConnectedEdgeNodes().isEmpty()) {
                nodeMapping.remove(node.oldNode);
            }
        }
    }

    /**
     * Indicates if there are edge nodes to cover in the fog graph left.
     *
     * @return true if there are still nodes, false otherwise
     */
    boolean hasEdgeNodes() {
        return !edgeNodes.isEmpty();
    }

    /**
     * Returns the nextLevels node of the fog placement algorithm.
     * Possible nodes get sorted with the FogComparator and the graph updated according to the node picked.
     *
     * @return nextLevels node picked
     */
    FogNode getNext() {
        Logger logger = Logger.getInstance();
        logger.log("Remaining Edge Nodes to cover: " + edgeNodes.size());
        long start = System.nanoTime();
        List<FogNode> fogNodes = new ArrayList<>(nodeMapping.values());
        assert !fogNodes.isEmpty() : "there are no more possible fog nodes available";

        for (FogNode n : fogNodes) {
            n.findFogType();
        }
        long end = System.nanoTime();
        logger.log("Find Types Time: " + Logger.convertToMs(start, end), LoggerLevel.ADVANCED);

        //TODO debug
        for (EdgeNode edgeNode : edgeNodes) {
            assert edgeNode != null : "edge node in the list is null";
            assert edgeNode.isMappedToItself() : "edge node is not mapped to itself";
        }

        start = System.nanoTime();
        // sort the possible fog nodes with a FogComparator
        fogNodes.sort(comparator);
        end = System.nanoTime();
        logger.log("Sort Time: " + Logger.convertToMs(start, end), LoggerLevel.ADVANCED);

        // retrieve the nextLevels optimal node
        FogNode next = fogNodes.get(0);

        // get covered nodes by the fog node placement
        Collection<EdgeNode> coveredNodes = next.getCoveredEdgeNodes();

        //TODO debug
        if (next instanceof EdgeNode && edgeNodes.contains(next)) {
            assert coveredNodes.contains(next) : "covered nodes set doesn't contain nextLevels node";
        }

        start = System.nanoTime();
        // remove the nextLevels node from the mapping
        nodeMapping.remove(next.oldNode);

        // update all edge nodes connected to nextLevels
        for (EdgeNode edgeNode : next.getConnectedEdgeNodes()) {
            edgeNode.removePossibleNode(next);
        }
        next.clearAllEdgeNodes();

        // update all fog nodes connected to the selected node set
        for (EdgeNode coveredNode : coveredNodes) {
            coveredNode.notifyPossibleNodes();
            coveredNode.clearPossibleNodes();
            nodeMapping.remove(coveredNode.oldNode);
        }

        // remove all covered nodes from the edge nodes set
        edgeNodes.removeAll(coveredNodes);
        end = System.nanoTime();
        logger.log("remove nodes Time: " + Logger.convertToMs(start, end), LoggerLevel.ADVANCED);

        assert edgeNodes.size() <= nodeMapping.size() : "weniger edge nodes als gesamt";

        end = System.nanoTime();
        logger.log("GetNext() Time: " + Logger.convertToMs(start, end), LoggerLevel.ADVANCED);

        return next;
    }
}
