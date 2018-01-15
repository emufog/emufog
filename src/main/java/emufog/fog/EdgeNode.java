/*
package emufog.fog;

import emufog.graph.Node;
import emufog.graph.Router;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

*/
/**
 * This node represents an edge node that needs to be connected to a fog node.
 *//*

class EdgeNode extends FogNode {

    */
/* list of possible fog nodeconfig in range of the threshold *//*

    private final List<FogNode> possibleNodes;

    */
/* number of devices to cover for this edge node *//*

    private int deviceCount;

    */
/**
     * Creates a new edge node for the fog node placement algorithm.
     *
     * @param graph  original graph instance
     * @param router edge node for the iteration
     *//*

    EdgeNode(FogGraph graph, Router router) {
        super(graph, router);

        possibleNodes = new ArrayList<>();
        deviceCount = router.getDeviceCount();
    }

    */
/**
     * Creates a new edge node for the fog node placement algorithm.
     *
     * @param graph         original graph instance
     * @param node          node representing the edge for the iteration
     * @param assignedNodes collection of all edge nodeconfig covered in the original graph
     *//*

    EdgeNode(FogGraph graph, Node node, Collection<EdgeNode> assignedNodes) {
        super(graph, node);

        possibleNodes = new ArrayList<>();

        assert !assignedNodes.isEmpty() : "no nodeconfig assigned";
        deviceCount = 0;
        for (EdgeNode e : assignedNodes) {
            deviceCount += e.getDeviceCount();
        }
    }

    */
/**
     * Adds a node to the list of possible nodeconfig for this edge node.
     *
     * @param node possible fog node
     *//*

    void addPossibleNode(FogNode node) {
        possibleNodes.add(node);
        setModified(true);
    }

    */
/**
     * Removes a fog node from the list of possible nodeconfig if it's not available any more.
     *
     * @param node fog node to remove
     *//*

    void removePossibleNode(FogNode node) {
        boolean result = possibleNodes.remove(node);

        assert result : "node was not found in possible list";

        setModified(true);
    }

    */
/**
     * Notifies all possible nodeconfig of this edge node that the node does not have to be covered any more.
     *//*

    void notifyPossibleNodes() {
        for (FogNode node : possibleNodes) {
            node.removeEdgeNode(this);
        }
    }

    */
/**
     * Clears the list of possible fog nodeconfig.
     *//*

    void clearPossibleNodes() {
        possibleNodes.clear();
    }

    */
/**
     * Checks if the edge node has a connection mapped to itself.
     *
     * @return true if the node has a connection to itself, false if not
     *//*

    boolean isMappedToItself() {
        return equals(connectedNodes.get(this).predecessor);
    }

    */
/**
     * Returns the count of devices connected to this edge node.
     *
     * @return number of connected devices
     *//*

    int getDeviceCount() {
        assert deviceCount > 0 : "count is 0";
        return deviceCount;
    }
}
*/
