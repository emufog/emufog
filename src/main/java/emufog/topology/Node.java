package emufog.topology;

import emufog.graph.AS;

public abstract class Node {

    private final int id;

    private final AS as;

    public Node(int id, AS as) {
        this.id = id;
        this.as = as;
    }
}
