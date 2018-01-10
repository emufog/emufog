package emufog.fog;

import java.util.Comparator;

/**
 * The cost comparator compares fog nodeconfig based on their associated costs for a given edge node.
 */
class CostComparator implements Comparator<FogNode> {

    /* edge node to compare costs for */
    private final EdgeNode edge;

    /**
     * Creates a new comparator to compare fog nodeconfig based on their cost from the given edge.
     *
     * @param edge edge node to sort for
     */
    CostComparator(EdgeNode edge) {
        this.edge = edge;
    }

    @Override
    public int compare(FogNode o1, FogNode o2) {
        float cost1 = o1.getCosts(edge);
        float cost2 = o2.getCosts(edge);

        if (cost1 < cost2) {
            return -1;
        }
        if (cost1 > cost2) {
            return 1;
        }

        return 0;
    }
}