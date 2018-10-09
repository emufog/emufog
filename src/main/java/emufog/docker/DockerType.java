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
package emufog.docker;

/**
 * Abstract object of a Docker image placeable in the graph.
 * Docker images can be specified with a memory and CPU limit.
 */
public abstract class DockerType {

    /* name of the docker image */
    public final String dockerImage;

    /* upper memory limit in Bytes for the docker image */
    public final int memoryLimit;

    /* maximum share of the underlying CPUs */
    public final float cpuShare;

    /**
     * Creates new Docker image object with the given limits for memory and CPU.
     *
     * @param dockerImage actual docker image to deploy
     * @param memoryLimit upper limit of memory to use in Bytes
     * @param cpuShare    share of the sum of available computing resources
     * @throws IllegalArgumentException the docker image name cannot be null and must
     *                                  match the pattern of a docker container name
     */
    DockerType(String dockerImage, int memoryLimit, float cpuShare) throws IllegalArgumentException {
        if (dockerImage == null) {
            throw new IllegalArgumentException("The given docker image object is not instantiated.");
        }
        // check if the image name has the right pattern
        if (!dockerImage.matches("([a-z,0-9]+/)?([a-z,0-9]+):([a-z,0-9]+)")) {
            throw new IllegalArgumentException("The docker image name: " + dockerImage + " does not match the standard.");
        }

        this.dockerImage = dockerImage;
        this.memoryLimit = memoryLimit;
        this.cpuShare = cpuShare;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof DockerType) {
            DockerType other = (DockerType) obj;
            result = dockerImage.equals(other.dockerImage) && memoryLimit == other.memoryLimit
                    && cpuShare == other.cpuShare;
        }

        return result;
    }
}
