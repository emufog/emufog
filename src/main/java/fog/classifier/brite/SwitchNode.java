package fog.classifier.brite;

import graph.emufog.Node;

/**
 * This class represents a backbone node of the AS. It can be used as a fog node but it
 * doesn't have to be connected to one.
 */
class SwitchNode extends FogNode {

    /**
     * Creates a backbone node of the AS with the given original node.
     *
     * @param graph original graph
     * @param node  backbone node
     */
    SwitchNode(FogGraph graph, Node node) {
        super(graph, node);
    }
}
