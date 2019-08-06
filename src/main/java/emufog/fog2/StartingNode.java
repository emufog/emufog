package emufog.fog2;

import emufog.graph.EdgeNode;

import java.util.HashSet;
import java.util.Set;

class StartingNode extends BaseNode {

    private final Set<BaseNode> reachableNodes;

    StartingNode(EdgeNode node) {
        super(node);

        reachableNodes = new HashSet<>();
    }

    int getDeviceCount() {
        return ((EdgeNode) node).getDeviceCount();
    }

    Set<BaseNode> getReachableNodes() {
        return reachableNodes;
    }

    /**
     * Adds a node to the list of possible nodes for this edge node.
     *
     * @param node possible fog node
     */
    void addPossibleNode(BaseNode node) {
        reachableNodes.add(node);
        modified = true;
    }

    /**
     * Removes a fog node from the list of possible nodes if it's not available any more.
     *
     * @param node fog node to remove
     */
    void removePossibleNode(BaseNode node) {
        modified = reachableNodes.remove(node);
    }

    /**
     * Notifies all possible nodes of this edge node that the node does not have to be covered any more.
     */
    void notifyPossibleNodes() {
        for (BaseNode node : reachableNodes) {
            node.removeStartingNode(this);
        }
    }
}
