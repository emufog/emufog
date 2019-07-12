/*
 * MIT License
 *
 * Copyright (c) 2018 emufog contributors
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
package emufog.fog;

import emufog.container.FogType;
import emufog.graph.Node;
import emufog.util.Tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class represents the result of a fog placement algorithm. It states the overall
 * success state as well as the list of found placements.
 */
public class FogResult {

    /* success state of the result */
    private boolean success;

    /* list of node to fog type mappings */
    private final List<Tuple<Node, FogType>> nodes;

    /**
     * Creates a new result object. By default the success state is 'failure'.
     */
    FogResult() {
        success = false;
        nodes = new ArrayList<>();
    }

    /**
     * Adds a fog node to the result list.
     *
     * @param node fog node to add
     */
    void addFogNode(FogNode node) {
        nodes.add(new Tuple<>(node.oldNode, node.getFogType()));
    }

    /**
     * Adds all node mappings from the given collection to the existing list.
     *
     * @param nodes collection of node mappings to add
     */
    void addAll(Collection<Tuple<Node, FogType>> nodes) {
        this.nodes.addAll(nodes);
    }

    /**
     * Returns the status of this result object.
     *
     * @return true if successful, false if not
     */
    public boolean getStatus() {
        return success;
    }

    /**
     * Returns a list of mappings from nodes to their respective fog type.
     *
     * @return list of node to type mappings
     */
    public List<Tuple<Node, FogType>> getFogNodes() {
        return nodes;
    }

    /**
     * Clears the list of fog nodes currently stored in this object.
     */
    void clearFogNodes() {
        nodes.clear();
    }

    /**
     * Sets the success state to the given state.
     *
     * @param success true for success, false for failure
     */
    void setSuccess(boolean success) {
        this.success = success;
    }
}
