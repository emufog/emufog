package emufog.graph;

/**
 * A switch is part of the backbone of a network and cannot connect to host devices directly.
 * It can still though be a fog computing node.
 */
public class Switch extends Node {

    /**
     * Creates a new switch node.
     *
     * @param id unique identifier
     * @param as autonomous system the belongs to
     */
    Switch(int id, AS as) {
        super(id, as);
    }

    @Override
    void addToAS() {
        getAS().addSwitch(this);
    }

    @Override
    public String getName() {
        return "s" + getID();
    }
}
