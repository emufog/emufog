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
package emufog.container

/**
 * This container image represents a host device connected to an edge node router. By using the
 * scaling factor you can simulate a higher load of multiple devices. Will be distributed based on
 * the average count per router.
 *
 * @property name name of container image to deploy
 * @property tag version tag of container image to deploy
 * @property memoryLimit upper limit of memory to use in Bytes
 * @property cpuShare share of the sum of available computing resources
 * @property scalingFactor scaling factor of this container image, factor `>= 1`
 * @property averageDeviceCount average number of devices of this image deployed to each edge node
 */
data class DeviceContainer(
    override val name: String,
    override val tag: String,
    override val memoryLimit: Int,
    override val cpuShare: Float,
    val scalingFactor: Int,
    val averageDeviceCount: Float
) : Container
