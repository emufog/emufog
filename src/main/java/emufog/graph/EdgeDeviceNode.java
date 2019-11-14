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

import emufog.container.DeviceContainer;

/**
 * This class represents a edge device. Each edge device has a container image
 * associated to run the application specific code.
 */
public class EdgeDeviceNode extends Node {

    EdgeDeviceNode(NodeAttributes attributes, EmulationSettings emulationSettings) {
        super(attributes);

        setEmulationSettings(emulationSettings);
    }

    @Override
    public NodeType getType() {
        return NodeType.EDGE_DEVICE_NODE;
    }

    @Override
    public String getName() {
        return "h" + getID();
    }

    /**
     * Returns the container type for this edge device node. The type is always a device type instance.
     *
     * @return device container type
     */
    DeviceContainer getContainerType() {
        return (DeviceContainer) getEmulationNode().getContainer();
    }
}
