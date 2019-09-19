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
     * @throws IllegalArgumentException if the emulation settings object is {@code null}
     */
    private EdgeDeviceNodeConverter(EmulationSettings emulationSettings) throws IllegalArgumentException {
        if (emulationSettings == null) {
            throw new IllegalArgumentException("The emulation settings object is not initialized.");
        }

        this.emulationSettings = emulationSettings;
    }

    @Override
    Node createNewNode(Node node) {
        return new EdgeDeviceNode(node.id, node.as, emulationSettings);
    }

    @Override
    void addNodeToGraph(Node newNode) {
        newNode.as.addDevice((EdgeDeviceNode) newNode);
    }

    @Override
    boolean needsConversion(Node node) {
        return !(node instanceof EdgeDeviceNode);
    }

    @Override
    EdgeDeviceNode convert(Node node) {
        return (EdgeDeviceNode) super.convert(node);
    }

    static EdgeDeviceNode convertToEdgeDeviceNode(Node node, EmulationSettings emulationSettings) {
        return new EdgeDeviceNodeConverter(emulationSettings).convert(node);
    }
}
