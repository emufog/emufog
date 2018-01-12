package emufog.topology;

import emufog.graph.AS;

public abstract class Node {

    private int ID;

    private final int asID;

    private AS as;

    public Node(int id, int asID) {
        this.ID = id;
        this.asID = asID;
    }

    public int getID() {
        return ID;
    }

    public AS getAs() {
        return as;
    }

    public void setAs(AS as) {
        this.as = as;
    }
}
