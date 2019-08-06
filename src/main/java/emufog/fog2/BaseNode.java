package emufog.fog2;

import emufog.container.FogType;
import emufog.graph.Node;
import emufog.util.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract class BaseNode {

    final Node node;

    private final Map<StartingNode, Tuple<BaseNode, Float>> costs;

    boolean modified;

    private FogType type;

    private int coveredCount;

    private float averageConnectionCosts;

    BaseNode(Node node) {
        this.node = node;
        costs = new HashMap<>();
        modified = true;
        type = null;
        coveredCount = 0;
    }

    int getCoveredCount() {
        return coveredCount;
    }

    float getAverageConnectionCosts() {
        return averageConnectionCosts;
    }

    FogType getType() {
        return type;
    }

    float getCosts(StartingNode node) {
        Tuple<BaseNode, Float> tuple = costs.get(node);

        return tuple != null ? tuple.getValue() : Float.MAX_VALUE;
    }

    void setCosts(StartingNode node, BaseNode predecessor, float costs) {
        this.costs.put(node, new Tuple<>(predecessor, costs));
        node.addPossibleNode(this);
    }

    /**
     * Returns the average deployment costs for all edge nodes connected to this node.
     *
     * @return average deployment costs
     */
    float getAverageDeploymentCosts() {
        return type.costs / coveredCount;
    }

    List<Tuple<StartingNode, Integer>> getCoveredStartingNodes() {
        List<StartingNode> startingNodes = costs.keySet().stream()
                .map(k -> new Tuple<>(k, costs.get(k).getValue()))
                .sorted((a, b) -> Float.compare(a.getValue(), b.getValue()))
                .map(Tuple::getKey)
                .collect(Collectors.toList());

        List<Tuple<StartingNode, Integer>> result = new ArrayList<>();
        int remaining = coveredCount;
        for (int i = 0; i < startingNodes.size() && remaining > 0; ++i) {
            StartingNode node = startingNodes.get(i);

            result.add(new Tuple<>(node, Math.max(remaining, node.getDeviceCount())));
            remaining -= node.getDeviceCount();
        }

        return result;
    }

    void removeStartingNode(StartingNode node) {
        modified = costs.remove(node) != null;
    }

    void findFogType(Collection<FogType> fogTypes) {
        if (!modified) {
            return;
        }

        type = null;
        coveredCount = 0;

        float costsPerConnection = Float.MAX_VALUE;
        int deviceCount = 0;
        for (StartingNode n : costs.keySet()) {
            deviceCount += n.getDeviceCount();
        }

        for (FogType fogType : fogTypes) {
            int connections = Math.min(deviceCount, fogType.maxClients);

            if ((fogType.costs / connections) < costsPerConnection) {
                type = fogType;
                coveredCount = connections;
                costsPerConnection = type.costs / connections;
            }
        }

        calculateAverageCosts();
        modified = false;
    }

    /**
     * Removes all edge node connected to this fog node.
     */
    void clearAllEdgeNodes() {
        costs.clear();
        modified = true;
    }

    private void calculateAverageCosts() {
        float sum = 0.f;
        for (Tuple<BaseNode, Float> t : costs.values()) {
            sum += t.getValue();
        }

        averageConnectionCosts = sum / costs.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BaseNode)) {
            return false;
        }

        return node.equals(((BaseNode) obj).node);
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }
}
