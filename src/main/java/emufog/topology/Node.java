package emufog.topology;

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

    public int getAsID(){
        return asID;
    }

    public void setAs(AS as) {
        this.as = as;
    }

    /**
     * Returns the name of the node.
     *
     * @return name of the node
     */
    public abstract String getName();
}
