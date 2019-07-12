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

import java.util.ArrayList;
import java.util.List;

/**
 * This container image represents a fog computing node in the topology.
 * It can serve a fixed number of clients and is associated with deployment costs.
 */
public class FogType extends ContainerType {

    /* maximal number of clients this image can serve, including */
    public final int maxClients;

    /* costs to deploy an instance of this image */
    public final float costs;

    /* list of dependencies of this image */
    public final List<FogType> dependencies;

    /**
     * Creates a new fog computing node to be deployed in the network.
     *
     * @param containerName    name of the container image to deploy
     * @param containerVersion version of the container image to deploy
     * @param maxClients       maximum number of clients to serve
     * @param costs            costs to deploy this image
     * @param memoryLimit      upper limit of memory to use in Bytes
     * @param cpuShare         share of the sum of available computing resources
     * @throws IllegalArgumentException the container image name cannot be {@code null} and must
     *                                  match the pattern of a container container name
     */
    public FogType(String containerName, String containerVersion, int maxClients, float costs, int memoryLimit, float cpuShare) throws IllegalArgumentException {
        super(containerName, containerVersion, memoryLimit, cpuShare);

        this.maxClients = maxClients;
        this.costs = costs;
        dependencies = new ArrayList<>();
    }

    /**
     * Creates a new fog computing node to be deployed in the network.
     *
     * @param containerName    name of the container image to deploy
     * @param maxClients       maximum number of clients to serve
     * @param costs            costs to deploy this image
     * @param memoryLimit      upper limit of memory to use in Bytes
     * @param cpuShare         share of the sum of available computing resources
     * @throws IllegalArgumentException the container image name cannot be {@code null} and must
     *                                  match the pattern of a container container name
     */
    public FogType(String containerName, int maxClients, float costs, int memoryLimit, float cpuShare) throws IllegalArgumentException {
        super(containerName, memoryLimit, cpuShare);

        this.maxClients = maxClients;
        this.costs = costs;
        dependencies = new ArrayList<>();
    }

    /**
     * Adds
     *
     * @param type fog node type dependency to add
     * @throws IllegalArgumentException if type object is {@code null}
     */
    public void addDependency(FogType type) throws IllegalArgumentException {
        if (type == null) {
            throw new IllegalArgumentException("Fog type dependency is not initialized.");
        }

        dependencies.add(type);
    }

    /**
     * Returns identification whether there are dependencies for this fog node type.
     *
     * @return true if there are dependencies, false otherwise
     */
    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }
}
