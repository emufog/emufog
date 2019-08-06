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

import emufog.graph.EdgeNode;

import java.util.HashSet;
import java.util.Set;

class StartingNode extends BaseNode {

    private final Set<BaseNode> reachableNodes;

    StartingNode(EdgeNode node) {
        super(node);

        reachableNodes = new HashSet<>();
    }

    int getDeviceCount() {
        return ((EdgeNode) node).getDeviceCount();
    }

    Set<BaseNode> getReachableNodes() {
        return reachableNodes;
    }

    /**
     * Adds a node to the list of possible nodes for this edge node.
     *
     * @param node possible fog node
     */
    void addPossibleNode(BaseNode node) {
        reachableNodes.add(node);
        modified = true;
    }

    /**
     * Removes a fog node from the list of possible nodes if it's not available any more.
     *
     * @param node fog node to remove
     */
    void removePossibleNode(BaseNode node) {
        modified = reachableNodes.remove(node);
    }

    /**
     * Notifies all possible nodes of this edge node that the node does not have to be covered any more.
     */
    void notifyPossibleNodes() {
        for (BaseNode node : reachableNodes) {
            node.removeStartingNode(this);
        }
    }
}
