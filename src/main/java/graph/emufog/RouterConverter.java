package graph.emufog;

/**
 * This class convert an existing node to a router node in the graph.
 */
public class RouterConverter extends NodeConverter {

    @Override
    protected Node createNewNode(Node node) {
        return new Router(node.id, node.as);
    }

    @Override
    protected void addNodeToGraph(Node newNode) {
        newNode.as.addRouter((Router) newNode);
    }

    @Override
    protected boolean needsConversion(Node node) {
        return !(node instanceof Router);
    }

    @Override
    public Router convert(Node node) {
        return (Router) super.convert(node);
    }
}
