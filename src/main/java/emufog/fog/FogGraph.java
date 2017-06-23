package emufog.fog;

import emufog.docker.FogType;
import emufog.graph.AS;
import emufog.graph.Node;
import emufog.graph.Router;
import emufog.graph.Switch;
import emufog.util.Logger;

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
     * @param as as to cover
     */
    void initNodes(AS as) {
        for (Router r : as.getRouters()) {
            EdgeNode edgeNode = new EdgeNode(this, r);
            nodeMapping.put(r, edgeNode);

            if (r.hasDevices()) {
                edgeNodes.add(edgeNode);
            }
        }

        for (Switch s : as.getSwitches()) {
            nodeMapping.put(s, new SwitchNode(this, s));
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
     * Returns the next node of the fog placement algorithm.
     * Possible nodes get sorted with the FogComparator and the graph updated according to the node picked.
     *
     * @return next node picked
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

        //TODO debug
        for (EdgeNode edgeNode : edgeNodes) {
            assert edgeNode != null : "edge node in the list is null";
            assert edgeNode.isMappedToItself() : "edge node is not mapped to itself";
        }

        // sort the possible fog nodes with a FogComparator
        fogNodes.sort(comparator);

        // retrieve the next optimal node
        FogNode next = fogNodes.get(0);

        // get covered nodes by the fog node placement
        Collection<EdgeNode> coveredNodes = next.getCoveredEdgeNodes();

        //TODO debug
        if (next instanceof EdgeNode && edgeNodes.contains(next)) {
            assert coveredNodes.contains(next) : "covered nodes set doesn't contain next node";
        }

        // remove the next node from the mapping
        nodeMapping.remove(next.oldNode);

        // update all edge nodes connected to next
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

        assert edgeNodes.size() <= nodeMapping.size() : "weniger edge nodes als gesamt";

        long end = System.nanoTime();
        logger.log("remove nodes Time: " + Logger.convertToMs(start, end));

        return next;
    }
}
