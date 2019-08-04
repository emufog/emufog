package emufog.fog2;

import java.util.Comparator;

class CostComparator implements Comparator<BaseNode> {

    private final StartingNode startingNode;

    CostComparator(StartingNode startingNode) {
        this.startingNode = startingNode;
    }

    @Override
    public int compare(BaseNode o1, BaseNode o2) {
        return Float.compare(o1.getCosts(startingNode), o2.getCosts(startingNode));
    }
}