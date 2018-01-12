package emufog.topology;

public class Router extends Node{

    enum RouterType {
        EDGE_ROUTER, BACKBONE_ROUTER
    }

    private RouterType type;

    private int deviceCount;

    public Router(int id, int asID) {
        super(id, asID);
        this.deviceCount = 0;
    }
}
