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
 * This class convert an existing node to a host device node in the graph.
 */
public class HostDeviceConverter extends NodeConverter {

    private final EmulationSettings emulationSettings;

    /**
     * Creates a new HostDeviceConverter instance to convert an existing node and
     * replace it with the given emulation settings.
     *
     * @param emulationSettings emulation settings of the newly created node
     * @throws IllegalArgumentException if the emulation settings object is null
     */
    public HostDeviceConverter(EmulationSettings emulationSettings) throws IllegalArgumentException {
        if (emulationSettings == null) {
            throw new IllegalArgumentException("The emulation settings object is not initialized.");
        }

        this.emulationSettings = emulationSettings;
    }

    @Override
    protected Node createNewNode(Node node) {
        return new HostDevice(node.id, node.as, emulationSettings);
    }

    @Override
    protected void addNodeToGraph(Node newNode) {
        newNode.as.addDevice((HostDevice) newNode);
    }

    @Override
    protected boolean needsConversion(Node node) {
        return !(node instanceof HostDevice);
    }

    @Override
    public HostDevice convert(Node node) {
        return (HostDevice) super.convert(node);
    }
}
