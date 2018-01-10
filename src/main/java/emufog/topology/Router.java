package emufog.topology;

import emufog.graph.AS;


public class Router extends Node{

    enum RouterType {
        EDGE_ROUTER, BACKBONE_ROUTER
    }

    private RouterType type;

    private int deviceCount;

    public Router(int id, AS as, RouterType type, int deviceCount) {
        super(id, as);
        this.type = type;
        this.deviceCount = deviceCount;
    }
}
