package emufog.fog2;

import emufog.fog.EdgeNode;
import emufog.fog.FogNode;
import java.util.Comparator;


class CostComparator implements Comparator<Node> {

    private final EdgeNode edge;


    CostComparator(EdgeNode edge) {
        this.edge = edge;
    }

    @Override
    public int compare(FogNode o1, FogNode o2) {
        float cost1 = o1.getCosts(edge);
        float cost2 = o2.getCosts(edge);

        return Float.compare(cost1, cost2);
    }
}