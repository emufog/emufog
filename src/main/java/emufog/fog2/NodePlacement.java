package emufog.fog2;

import emufog.container.ContainerType;
import emufog.graph.Node;

public abstract class NodePlacement {

    private final Node node;

    private final ContainerType containerType;

    NodePlacement(Node node, ContainerType containerType) {
        this.node = node;
        this.containerType = containerType;
    }

    public Node getNode() {
        return node;
    }

    public ContainerType getContainerType() {
        return containerType;
    }
}
