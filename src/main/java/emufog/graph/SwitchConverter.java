package emufog.graph;

/**
 * This class convert an existing node to a switch node in the graph.
 */
public class SwitchConverter extends NodeConverter {

    @Override
    protected Node createNewNode(Node node) {
        return new Switch(node.id, node.as);
    }

    @Override
    protected void addNodeToGraph(Node newNode) {
        newNode.as.addSwitch((Switch) newNode);
    }

    @Override
    protected boolean needsConversion(Node node) {
        return !(node instanceof Switch);
    }

    @Override
    public Switch convert(Node node) {
        return (Switch) super.convert(node);
    }
}
