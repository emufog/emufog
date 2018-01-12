package emufog.topology;

import emufog.graph.AS;

public abstract class Node {

    private int ID;

    private final int asID;

    private AS as;

    public Node(int id, int as) {
        this.ID = id;
        this.asID = as;
    }

    public int getID() {
        return ID;
    }
}
