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
package emufog.container;

import static emufog.util.StringUtils.nullOrEmpty;

/**
 * Abstract object of a container image placeable in the graph.
 * Container images can be specified with a memory and CPU limit.
 */
public abstract class ContainerType {

    /**
     * name of the container image
     */
    public final String name;

    /**
     * version of the container image
     */
    public final String version;

    /**
     * upper memory limit in Bytes for the container image
     */
    public final int memoryLimit;

    /**
     * maximum share of the underlying CPUs
     */
    public final float cpuShare;

    /**
     * Creates new container image object with the given limits for memory and CPU.
     * Version defaults to 'latest'. See {{@link #ContainerType(String, String, int, float)}}.
     *
     * @param name        name of container image to deploy
     * @param memoryLimit upper limit of memory to use in Bytes
     * @param cpuShare    share of the sum of available computing resources
     * @throws IllegalArgumentException the container image name cannot be null and must
     *                                  match the pattern of a container container name
     */
    ContainerType(String name, int memoryLimit, float cpuShare) throws IllegalArgumentException {
        this(name, "latest", memoryLimit, cpuShare);
    }

    /**
     * Creates new container image object with the given limits for memory and CPU.
     *
     * @param name        name of container image to deploy
     * @param version     version of container image to deploy
     * @param memoryLimit upper limit of memory to use in Bytes
     * @param cpuShare    share of the sum of available computing resources
     * @throws IllegalArgumentException the container image name cannot be null and must
     *                                  match the pattern of a container container name
     */
    ContainerType(String name, String version, int memoryLimit, float cpuShare) throws IllegalArgumentException {
        if (nullOrEmpty(name)) {
            throw new IllegalArgumentException("The given container name is not set.");
        }
        if (nullOrEmpty(version)) {
            throw new IllegalArgumentException("The given container version is not set.");
        }

        this.name = name;
        this.version = version;
        this.memoryLimit = memoryLimit;
        this.cpuShare = cpuShare;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ContainerType)) {
            return false;
        }

        ContainerType other = (ContainerType) obj;
        return name.equals(other.name) && version.equals(other.version) && memoryLimit == other.memoryLimit && cpuShare == other.cpuShare;
    }

    @Override
    public String toString() {
        return name + ':' + version;
    }
}
