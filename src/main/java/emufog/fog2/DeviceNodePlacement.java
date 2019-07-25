package emufog.fog2;

import emufog.graph.Node;

public class DeviceNodePlacement extends NodePlacement {

    DeviceNodePlacement(Node node) {
        super(node, node.getEmulationNode().getContainerType());
    }
}
