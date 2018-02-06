package emufog.topology;

public abstract class Node {

    private int ID;

    private final int asID;


    public Node(int id, int asID) {
        this.ID = id;
        this.asID = asID;
    }

    public int getID() {
        return ID;
    }

    public int getAsID(){
        return asID;
    }

    /**
     * Returns the name of the node.
     *
     * @return name of the node
     */
    public abstract String getName();
}
