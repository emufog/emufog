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
package emufog.graph;

/**
 * The edge node class represents a node of the graph host devices can connect to.
 */
public class EdgeNode extends Node {

    /**
     * number of devices connected to this edge node
     */
    private int deviceCount;

    EdgeNode(NodeAttributes attributes) {
        super(attributes);

        deviceCount = 0;
    }

    @Override
    public NodeType getType() {
        return NodeType.EDGE_NODE;
    }

    /**
     * Returns indication whether this edge node has devices connected.
     *
     * @return true if there are devices connected, false otherwise
     */
    public boolean hasDevices() {
        return deviceCount > 0;
    }

    /**
     * Increments the device counter by the given number.
     * Will be ignored if negative.
     *
     * @param n the number to increase the device count
     */
    void incrementDeviceCount(int n) {
        if (n < 0) {
            return;
        }

        deviceCount += n;
    }

    /**
     * Returns the device count of this edge node.
     * The count includes the scaling factor given by the config.
     *
     * @return device count
     */
    public int getDeviceCount() {
        return deviceCount;
    }

    @Override
    public String getName() {
        return "r" + getID();
    }
}
