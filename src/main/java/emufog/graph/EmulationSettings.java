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

import emufog.docker.DockerType;

/**
 * This class represents the settings to use emulating a node of the graph.
 */
public class EmulationSettings {

    /* IP address to use */
    final String IP;

    /* docker image containing the application to emulate */
    final DockerType dockerType;

    /**
     * Creates a new instance of the emulation settings object using the given IP and docker image.
     *
     * @param IP         IP address to use
     * @param dockerType docker image to use
     */
    EmulationSettings(String IP, DockerType dockerType) {
        this.IP = IP;
        this.dockerType = dockerType;
    }

    /**
     * Returns the IP address of the emulated node.
     *
     * @return IP address
     */
    public String getIP() {
        return IP;
    }

    /**
     * Returns the docker image to emulate.
     *
     * @return docker image
     */
    public DockerType getDockerType() {
        return dockerType;
    }
}
