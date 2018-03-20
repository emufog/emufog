package emufog.topology;

import emufog.util.UniqueIDProvider;

public abstract class Node {

    private int ID;

    private int asID;

    public Node(){
        this.ID = UniqueIDProvider.getInstance().getNextID();
        UniqueIDProvider.getInstance().markIDused(this.ID);
    }

    public Node(int asID) {
        this.ID = UniqueIDProvider.getInstance().getNextID();
        UniqueIDProvider.getInstance().markIDused(this.ID);
        this.asID = asID;
    }

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
