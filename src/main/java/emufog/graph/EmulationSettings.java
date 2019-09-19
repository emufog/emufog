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

import emufog.container.ContainerType;

/**
 * This class represents the settings to use emulating a node of the graph.
 */
public class EmulationSettings {

    /**
     * IP address to use
     */
    private final String ip;

    /**
     * container image containing the application to emulate
     */
    private final ContainerType containerType;

    /**
     * Creates a new instance of the emulation settings object using the given IP and container image.
     *
     * @param ip            ip address to use
     * @param containerType container image to use
     */
    EmulationSettings(String ip, ContainerType containerType) {
        this.ip = ip;
        this.containerType = containerType;
    }

    /**
     * Returns the IP address of the emulated node.
     *
     * @return IP address
     */
    public String getIP() {
        return ip;
    }

    /**
     * Returns the container image to emulate.
     *
     * @return container image
     */
    public ContainerType getContainerType() {
        return containerType;
    }
}
