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
 * This class convert an existing node to a edge device node in the graph.
 */
class EdgeDeviceNodeConverter extends NodeConverter {

    private final EmulationSettings emulationSettings;

    /**
     * Creates a new EdgeDeviceNodeConverter instance to convert an existing node and
     * replace it with the given emulation settings.
     *
     * @param emulationSettings emulation settings of the newly created node
     */
    private EdgeDeviceNodeConverter(EmulationSettings emulationSettings) {
        this.emulationSettings = emulationSettings;
    }

    @Override
    Node createNewNode(Node oldNode) {
        return new EdgeDeviceNode(oldNode.id, oldNode.as, emulationSettings);
    }

    @Override
    void addNodeToGraph(Node newNode) {
        newNode.as.addDevice((EdgeDeviceNode) newNode);
    }

    @Override
    boolean needsConversion(Node oldNode) {
        return !(oldNode instanceof EdgeDeviceNode);
    }

    @Override
    EdgeDeviceNode convert(Node oldNode) {
        return (EdgeDeviceNode) super.convert(oldNode);
    }

    /**
     * Converts an arbitrary node to an edge device node. If the node is already an edge device
     * node it will just be returned. Otherwise the old node will be removed from the AS and
     * replaced by the new node.
     *
     * @param oldNode           old node to replace by an edge device node
     * @param emulationSettings emulation settings to set for the new node
     * @return newly created edge device node instance
     */
    static EdgeDeviceNode convertToEdgeDeviceNode(Node oldNode, EmulationSettings emulationSettings) {
        return new EdgeDeviceNodeConverter(emulationSettings).convert(oldNode);
    }
}
