package emufog.fog2;

import emufog.graph.AS;
import java.util.ArrayList;
import java.util.List;

class FogTree {

    private final AS as;

    FogTree(AS as) {
        this.as = as;
    }

    boolean hasNextLevel() {
        return true;
    }

    List<NodePlacement> getNextLevel() {
        return new ArrayList<>();
    }
}
