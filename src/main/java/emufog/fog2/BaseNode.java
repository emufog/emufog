/*
 * MIT License
 *
 * Copyright (c) 2019 emufog contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package emufog.fog2;

import emufog.container.FogType;
import emufog.graph.Node;
import emufog.util.Tuple;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base node tracks connections from starting nodes in the graph up to this node.
 * Therefore all such connections are stored using the predecessor on the path to this
 * node and the overall costs. It can also be a possible placement for a fog node.
 */
class BaseNode {

    private static final Logger LOG = LoggerFactory.getLogger(BaseNode.class);

    /**
     * the node in the original graph model
     */
    final Node node;

    /**
     * mapping of starting nodes to their respective tuple of predecessor in the path
     * and the overall connection costs
     */
    private final Map<StartingNode, Tuple<BaseNode, Float>> costs;

    /**
     * indicator if the connections have been updated and therefore needs to be
     * reevaluated
     */
    boolean modified;

    /**
     * the fog type with the optimal costs, device count ration
     */
    private FogType type;

    /**
     * number of devices covered by this node
     */
    private int coveredCount;

    /**
     * average connection costs of all associated connections
     */
    private float averageConnectionCosts;

    /**
     * Creates a new base node based on the given node from the original graph.
     *
     * @param node underlying node from the graph
     */
    BaseNode(Node node) {
        this.node = node;
        costs = new HashMap<>();
        modified = true;
        type = null;
        coveredCount = 0;
    }

    /**
     * Returns the average connection costs from all connected nodes.
     *
     * @return average connection costs for this node
     */
    float getAverageConnectionCosts() {
        return averageConnectionCosts;
    }

    /**
     * Returns the fog type with the currently optimal cost ratio for this node.
     *
     * @return fog type associated
     */
    FogType getType() {
        return type;
    }

    /**
     * Returns the connection costs for the given starting node.
     * If this node is not connected to the given node {@link Float#MAX_VALUE} is returned.
     *
     * @param node node to retrieve connection costs for
     * @return connection costs for the given node
     */
    float getCosts(StartingNode node) {
        Tuple<BaseNode, Float> tuple = costs.get(node);

        return tuple != null ? tuple.getValue() : Float.MAX_VALUE;
    }

    /**
     * Sets the connection costs for a path from the given starting node to this node.
     * Requires the predecessor on the path and the connection costs.
     *
     * @param node        starting node in the connection path
     * @param predecessor the predecessor on the connection path
     * @param costs       connection costs
     */
    void setCosts(StartingNode node, BaseNode predecessor, float costs) {
        this.costs.put(node, new Tuple<>(predecessor, costs));
        node.addPossibleNode(this);
    }

    /**
     * Returns all starting nodes this node is connected to.
     *
     * @return set of all starting nodes connected
     */
    Set<StartingNode> getStartingNodes() {
        return costs.keySet();
    }

    /**
     * Returns whether there are connections to this node are available or not.
     *
     * @return {@code true} if connections are available, {@code false} otherwise
     */
    boolean hasConnections() {
        return !costs.isEmpty();
    }

    /**
     * Returns the average deployment costs for all edge nodes connected to this node.
     *
     * @return average deployment costs
     */
    float getAverageDeploymentCosts() {
        return type.costs / coveredCount;
    }

    /**
     * Returns a list of all covered starting nodes by this node.
     * The list contains of a tuple of the starting node that is covered
     * and the number of devices that are covered by this node.
     *
     * @return list of covered starting node and their respective device count
     */
    List<Tuple<StartingNode, Integer>> getCoveredStartingNodes() {
        // sort the connections based on their connection costs in ascending order
        List<StartingNode> startingNodes = costs.keySet()
            .stream()
            .map(k -> new Tuple<>(k, costs.get(k).getValue()))
            .sorted((a, b) -> Float.compare(a.getValue(), b.getValue()))
            .map(Tuple::getKey)
            .collect(Collectors.toList());

        List<Tuple<StartingNode, Integer>> result = new ArrayList<>();
        int remaining = coveredCount;
        // pick starting nodes greedy that add up to coveredCount
        for (int i = 0; i < startingNodes.size() && remaining > 0; ++i) {
            StartingNode node = startingNodes.get(i);

            result.add(new Tuple<>(node, Math.min(remaining, node.getDeviceCount())));
            remaining -= node.getDeviceCount();
        }

        return result;
    }

    /**
     * Removes the given starting node from the list of possible connections.
     * Updates {@link #modified} according to the outcome of the delete.
     *
     * @param node starting node to delete the connection for
     */
    void removeStartingNode(StartingNode node) {
        modified = costs.remove(node) != null;
    }

    /**
     * Finds a new optimal fog type for this node. Optimal solutions are calculated
     * by the ratio of {@link FogType#costs} and the number of connections to that node.
     * Updates the {@link #modified} to {@code false} once set.
     *
     * @param fogTypes collection of possible fog types to assign
     */
    void findFogType(Collection<FogType> fogTypes) {
        // skip reassigning on non modified nodes
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
        LOG.debug("Set the fog type for {} to {}", node, type);

        calculateAverageCosts();
        modified = false;
    }

    /**
     * Pre-calculates the average connection costs for all possible connections and
     * sets the {@link #averageConnectionCosts}.
     * Can be retrieved by {@link #getAverageConnectionCosts()}
     */
    private void calculateAverageCosts() {
        double sum = 0.f;
        for (Tuple<BaseNode, Float> t : costs.values()) {
            sum += t.getValue();
        }

        averageConnectionCosts = (float) (sum / costs.size());
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
