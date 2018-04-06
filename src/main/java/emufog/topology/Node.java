package emufog.topology;

import emufog.util.UniqueIDProvider;

import java.util.LinkedList;
import java.util.List;

public abstract class Node {

    private int ID;

    private int asID;

    private List<Node> shortestPath = new LinkedList<>();

    //Delay
    private Float distance = Float.valueOf(Integer.MAX_VALUE);

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

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    public Float getDistance() {
        return distance;
    }

    public List<Node> getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(List<Node> shortestPath) {
        this.shortestPath = shortestPath;
    }
}
