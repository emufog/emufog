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

/**
 * This class represents a placement of a fog node in the graph.
 * The result contains the node and the type of fog node to use.
 */
public class FogNodePlacement {

    /**
     * node where the fog node should be placed
     */
    private final Node node;

    /**
     * type of the fog node
     */
    private final FogType type;

    /**
     * Creates a new placement based on the given node and the associated
     * fog node type.
     *
     * @param node node to base the placement on
     */
    FogNodePlacement(BaseNode node) {
        this.node = node.node;
        type = node.getType();
    }

    /**
     * Returns the node object in the graph where to place the fog node.
     *
     * @return node instance in the graph
     */
    public Node getNode() {
        return node;
    }

    /**
     * Returns the fog node type that should be used.
     *
     * @return fog node type to use
     */
    public FogType getType() {
        return type;
    }
}
